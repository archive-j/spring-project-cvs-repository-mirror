/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.orm.jpa;

import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of JPA operations.
 * Implemented by JpaTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides JpaTemplate's data access methods that mirror various
 * EntityManager methods. See the JPA EntityManager javadocs for details
 * on those methods.
 *
 * <p>Note that lazy loading will just work with an open JPA EntityManager,
 * either within a Spring-driven transaction (with JpaTransactionManager or
 * JtaTransactionManager) or within OpenEntityManagerInViewFilter/Interceptor.
 * Furthermore, some operations just make sense within transactions,
 * for example: <code>flush</code>.
 *
 * @author Juergen Hoeller
 * @since 1.3
 * @see JpaTemplate
 * @see javax.persistence.EntityManager
 * @see JpaTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see JpaDialect
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor
 */
public interface JpaOperations {

	Object execute(JpaCallback action) throws DataAccessException;

	List executeFind(JpaCallback action) throws DataAccessException;

	<T> T find(Class<T> entityClass, Object id) throws DataAccessException;

	<T> T getReference(Class<T> entityClass, Object id) throws DataAccessException;

	boolean contains(Object entity) throws DataAccessException;

	void refresh(Object entity) throws DataAccessException;

	void persist(Object entity) throws DataAccessException;

	<T> T merge(T entity) throws DataAccessException;

	void remove(Object entity) throws DataAccessException;

	void flush() throws DataAccessException;

	List find(String queryString) throws DataAccessException;

	List find(String queryString, Object value) throws DataAccessException;

	List find(String queryString, Object[] values) throws DataAccessException;

	List findByNamedQuery(String queryName) throws DataAccessException;

	List findByNamedQuery(String queryName, Object value) throws DataAccessException;

	List findByNamedQuery(String queryName, Object[] values) throws DataAccessException;

}
