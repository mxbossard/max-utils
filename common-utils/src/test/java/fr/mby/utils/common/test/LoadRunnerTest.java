/**
 * Copyright 2013 Maxime Bossard
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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * @author Maxime Bossard - 2013.
 *
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class LoadRunnerTest {

	private int count = 0;
	
	private int countWithError = 0;
	
	protected synchronized void increment() {
		this.count ++;
	}
	
	protected synchronized void incrementWithException() throws Exception {
		this.countWithError ++;
		
		if (countWithError % 1000 == 0) {
			throw new TestException();
		}
	}
	
	@Test
	public void testLoadRunnerWithoutException() throws Exception {
		LoadRunner<LoadRunnerTest, Void> loadRunner = 
				new LoadRunner<LoadRunnerTest, Void>(10420, 50, this) {

			@Override
			protected Void loadTest(LoadRunnerTest test) throws Exception {
				test.increment();
				return null;
			}
		};
		
		Assert.assertEquals("Bad count for multithread increment !", 10420, this.count);
		Assert.assertEquals("Bad count of finished without error threads !", 10420, loadRunner.getFinishedThreadWithoutErrorCount());
	}
	
	@Test(expected=TestException.class)
	public void testLoadRunnerWithException() throws Exception {
		@SuppressWarnings("unused")
		LoadRunner<LoadRunnerTest, Void> loadRunner = 
				new LoadRunner<LoadRunnerTest, Void>(10420, 50, this) {

			@Override
			protected Void loadTest(LoadRunnerTest test) throws Exception {
				test.incrementWithException();
				return null;
			}
		};

	}
	
	/**
	 * Exception for testing purpose
	 * 
	 * @author Maxime BOSSARD.
	 *
	 */
	@SuppressWarnings("serial")
	private class TestException extends Exception {

	}
}
