/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.integrationtest.ejbtest.hibernate.tx.ejb;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

import org.springframework.dao.DataAccessException;

/**
 * CmtJtaNoSpringTx EJB Remote interface
 *
 * @author colin sampaleanu
 * @version $Id$
 */
public interface CmtJtaNoSpringTxRemote extends EJBObject {

	public String echo(String input) throws RemoteException;

	public void testSameSessionReceivedInTwoHibernateCallbacks()
			throws TestFailureException, RemoteException;

	public void throwExceptionSoSessionUnbindCanBeVerified()
			throws DataAccessException, RemoteException;

}