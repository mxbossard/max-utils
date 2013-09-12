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

package fr.mby.utils.common.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

/**
 * This executor allow execution of code in a JPA transaction context. <br/>
 * Properly open and close resources.
 * 
 * @author Maxime Bossard - 2013
 * 
 */
public abstract class TxCallback {

	/**
	 * Execute some code in a JPA transaction. Commit it or rollback it on error. <br/>
	 * Does not close the EntityManager !
	 * 
	 * @param em
	 *            the EntityManager to use.
	 * @throws PersistenceException
	 *             which may occured during transaction processing.
	 */
	public TxCallback(final EntityManager em) throws PersistenceException {
		super();

		this.execute(em);
	}

	/**
	 * Execute some code in a JPA transaction. Commit it or rollback it on error. <br/>
	 * Create and close properly the EntityManager.
	 * 
	 * @param emf
	 *            the EntityManagerFactory to use.
	 * @throws PersistenceException
	 *             which may occured during transaction processing.
	 */
	public TxCallback(final EntityManagerFactory emf) throws PersistenceException {
		super();

		final EntityManager em = emf.createEntityManager();

		try {
			this.execute(em);
		} finally {
			if (em != null && em.isOpen()) {
				em.close();
			}
		}
	}

	/**
	 * Execute the abstract method executeInTransaction() in a JPA transaction context.
	 * 
	 * @param em
	 * @throws PersistenceException
	 *             which may occured during transaction processing.
	 */
	protected void execute(final EntityManager em) throws PersistenceException {
		try {
			final EntityTransaction tx = em.getTransaction();

			tx.begin();

			this.executeInTransaction(em);

			tx.commit();
		} catch (final RollbackException e) {
			if (em != null && em.isOpen() && em.getTransaction() != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		} finally {
			if (em != null && em.isOpen() && em.getTransaction() != null && em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
		}
	}

	/**
	 * This method is executed in a JPA transaction when the Object is construct.
	 * 
	 * @param em
	 *            the EntityManager to use
	 * @return the object returned can be retrieved with getReturnedValue().
	 * @Throws PersistenceException
	 */
	protected abstract void executeInTransaction(final EntityManager em) throws PersistenceException;

}
