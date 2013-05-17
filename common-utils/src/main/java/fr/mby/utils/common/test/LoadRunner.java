/**
 * Copyright 2012 Maxime Bossard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.mby.utils.common.test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper to build a load unit test in multi-threaded environment. This class can be seen as a runner for a common test
 * method.
 * 
 * 
 * @author Maxime Bossard - 2012.
 * @param <T>
 *            The test class in which the unit test method is.
 * @param <V>
 *            the return type of each test
 */
public abstract class LoadRunner<T, V> {

	/** Logger. */
	private static final Logger LOG = LogManager.getLogger(LoadRunner.class);

	/** Message d'erreur en cas d'exception jetee par un test. */
	private static final String MESSAGE_EXCEPTION_DURING_TEST = "----- Error during load test. -----";

	/** Message indicating a load test success. */
	private static final String MESSAGE_TEST_SUCCEED = "----- Load test successfull. -----";

	/** Nombre d'iterations par defaut. */
	private static final Integer NB_ITERATIONS_DEFAUT = 10000;

	/** Default thread pool size. */
	private static final Integer DEFAUT_THREAD_POOL_SIZE = 50;

	/** Nombre d'iteration de la methode loadTest(). */
	private int iterations = LoadRunner.NB_ITERATIONS_DEFAUT;

	/** Thread pool size. */
	private int threadPoolSize = LoadRunner.DEFAUT_THREAD_POOL_SIZE;

	/** Thread pool executor. */
	private ThreadPoolExecutor threadPoolExecutor;

	/** Nombre de threads terminé avec succes. */
	private int finishedTestCount = 0;

	/** List of results for each unit test of the load test. */
	private List<V> resultList;

	/** True si une demande d'arret des threads. */
	private Boolean shutdownTestRequested = Boolean.FALSE;

	/** Unit test in which load test logic is implemented. */
	private T unitTest;

	/** Handler des exceptions jetées par les threads. */
	private UncaughtExceptionHandler exceptionHandlerLoadTest;

	/** Exception imprévu retournée par un Thread. */
	private Throwable threadUncauchtException;

	/** Thread eventuel qui a retournée threadUncauchtException. */
	private Thread threadWithException;

	/** Is the test currently running ?. */
	private boolean isRunningTest = false;

	/**
	 * Constructeur with default params. 10,000 exectutions with 50 threads in thread pool.
	 * 
	 * @throws Exception
	 */
	public LoadRunner(final T pTest) throws Exception {
		this(LoadRunner.NB_ITERATIONS_DEFAUT, LoadRunner.DEFAUT_THREAD_POOL_SIZE, pTest);
	}

	/**
	 * Constructeur.
	 * 
	 * @param pIterations
	 *            nombre d'iterations du test.
	 * @param poolSize
	 *            size of the thread pool to use.
	 * @param pUnitTest
	 *            classe qui lance le test.
	 * @throws Throwable
	 *             Throwable
	 */
	public LoadRunner(final int pIterations, final int poolSize, final T pUnitTest) throws Exception {
		this.iterations = pIterations;
		this.threadPoolSize = poolSize;
		this.unitTest = pUnitTest;
		this.exceptionHandlerLoadTest = new ExceptionHandlerLoadTest(this);
		this.resultList = new ArrayList<V>(pIterations);

		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(this.iterations);
		this.threadPoolExecutor = new ThreadPoolExecutor(this.threadPoolSize, this.threadPoolSize, 0, TimeUnit.SECONDS,
				workQueue, new LoadRunnerThreadFactory());

		this.launchLoadTest();

	}

	/**
	 * Arrete le test en cours à cause d'une exception.
	 * 
	 * @param theTread
	 *            le thread qui remonte l'exception.
	 * @param theException
	 *            l'exception.
	 */
	public synchronized void stopCurrentTestWithException(final Thread theTread, final Throwable theException) {
		if (!this.shutdownTestRequested) {
			this.threadPoolExecutor.shutdownNow();
			this.threadUncauchtException = theException;
			this.threadWithException = theTread;
			this.shutdownTestRequested = Boolean.TRUE;
		}
	}

	public int getFinishedTestWithoutErrorCount() {
		if (this.isRunningTest) {
			throw new IllegalAccessError("Running Load test !");
		}
		return this.finishedTestCount;
	}

	public List<V> getResultList() {
		if (this.isRunningTest) {
			throw new IllegalAccessError("Running Load test !");
		}
		return this.resultList;
	}

	/**
	 * Accesseur de numberFinishedThread.
	 * 
	 */
	private synchronized final void addFinishedTest() {
		if (!this.isShutdownTestRequested()) {
			this.finishedTestCount++;
		}
	}

	/**
	 * Methode à implémenter pour effectuer un test de charge.
	 * 
	 * @param unitTest
	 *            the unit test in which the load test logic is implemented
	 * @throws Exception
	 *             Exception
	 */
	protected abstract V loadTest(T unitTest) throws Exception;

	/**
	 * Lance le test de charge.
	 * 
	 * @throws Throwable
	 *             Throwable
	 */
	public void launchLoadTest() throws Exception {
		this.isRunningTest = true;

		this.threadPoolExecutor.prestartAllCoreThreads();

		final Callable<V> runner = new LoadTestRunner(this.unitTest, this);
		final List<Callable<V>> tasks = Collections.nCopies(this.iterations, runner);

		final Collection<Future<V>> futures = this.threadPoolExecutor.invokeAll(tasks);

		// Temporisation on attend que tous les threads soient terminés.
		try {
			for (final Future<V> future : futures) {
				if (this.shutdownTestRequested) {
					// Arret des threads demandé
					continue;
				}

				// Timeout to process tasks
				this.resultList.add(future.get(1, TimeUnit.SECONDS));
			}
		} catch (final ExecutionException e) {
			LoadRunner.LOG.warn("Error during load test ! ", e.getCause());
			final Throwable cause = e.getCause();
			if (Exception.class.isAssignableFrom(cause.getClass())) {
				throw (Exception) cause;
			} else {
				throw e;
			}
		} catch (final Exception e) {
			LoadRunner.LOG.warn("Error during load test ! ", e);
			throw e;
		} finally {
			// Shutdown all tasks when finished
			this.threadPoolExecutor.shutdownNow();
			this.isRunningTest = false;
			this.rapportErreurThreadException();
		}

		if (this.threadUncauchtException != null) {
			throw new Exception(this.threadUncauchtException);
		}

	}

	/**
	 * S'occupe du rapport d'erreur.
	 */
	private void rapportErreurThreadException() {
		if (this.threadWithException != null) {
			LoadRunner.LOG.info(LoadRunner.MESSAGE_EXCEPTION_DURING_TEST);
			LoadRunner.LOG.info("Thread with error: [{}].", this.threadWithException.getName());
		} else {
			LoadRunner.LOG.info(LoadRunner.MESSAGE_TEST_SUCCEED);
		}
		LoadRunner.LOG.info("[{}] completed tests.", this.finishedTestCount);
	}

	/**
	 * Objet Runnable qui execute la methode loadTest().
	 * 
	 * @author mBossard
	 */
	private class LoadTestRunner implements Callable<V> {

		/** Test. */
		private final T test;

		/** loadTest. */
		private final LoadRunner<T, V> loadTest;

		/**
		 * Constructeur du LoadTestRunner.
		 * 
		 * @param pTest
		 *            test
		 * @param pLoadTest
		 *            loadTest
		 */
		public LoadTestRunner(final T pTest, final LoadRunner<T, V> pLoadTest) {
			this.test = pTest;
			this.loadTest = pLoadTest;
		}

		/** {@inheritDoc} */
		@Override
		public V call() throws Exception {
			// Effectue un test.
			V result = null;

			if (!this.loadTest.isShutdownTestRequested()) {
				// Si pas de stop execution de loadTest().
				result = this.loadTest.loadTest(this.test);

				// Un test terminé de plus.
				this.loadTest.addFinishedTest();
			}

			return result;
		}
	}

	/**
	 * Handler d'exceptions des Threads LoadTestRunner.
	 * 
	 * @author mBossard
	 */
	private class ExceptionHandlerLoadTest implements UncaughtExceptionHandler {

		/** LoadTest. */
		private final LoadRunner<T, V> loadTest;

		/**
		 * ExceptionHandlerLoadTest.
		 * 
		 * @param pLoadTest
		 *            loadTest
		 */
		public ExceptionHandlerLoadTest(final LoadRunner<T, V> pLoadTest) {
			this.loadTest = pLoadTest;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void uncaughtException(final Thread theThread, final Throwable theException) {
			this.loadTest.stopCurrentTestWithException(theThread, theException);

		}
	}

	private class LoadRunnerThreadFactory implements ThreadFactory {

		private int i = 1;

		@Override
		public Thread newThread(final Runnable r) {
			final Thread thread = new Thread(r);
			thread.setName("Thread-" + this.i++);
			thread.setUncaughtExceptionHandler(LoadRunner.this.exceptionHandlerLoadTest);

			return thread;
		}

	}

	/**
	 * Accesseur de stopThreadRequested.
	 * 
	 * @return obtention du stopThreadRequested.
	 */
	public final Boolean isShutdownTestRequested() {
		return this.shutdownTestRequested;
	}

}
