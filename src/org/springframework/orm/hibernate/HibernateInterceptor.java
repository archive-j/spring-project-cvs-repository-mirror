package org.springframework.orm.hibernate;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.Session;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * This interceptor binds a new Hibernate Session to the thread before a method
 * call, closing and removing it afterwards in case of any method outcome.
 * If there already was a pre-bound Session (e.g. from HibernateTransactionManager,
 * or from a surrounding Hibernate-intercepted method), the interceptor simply
 * takes part in it.
 *
 * <p>Application code must retrieve a Hibernate Session via SessionFactoryUtils'
 * getSession method, to be able to detect a thread-bound Session. It is preferable
 * to use getSession with allowCreate=false, as the code relies on the interceptor
 * to provide proper Session handling. Typically the code will look as follows:
 *
 * <p><code>
 * public void doHibernateAction() {<br>
 * &nbsp;&nbsp;Session session = SessionFactoryUtils.getSession(this.sessionFactory, false);<br>
 * &nbsp;&nbsp;try {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;...<br>
 * &nbsp;&nbsp;}<br>
 * &nbsp;&nbsp;catch (HibernateException ex) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;throw SessionFactoryUtils.convertHibernateAccessException(ex);<br>
 * &nbsp;&nbsp;}<br>
 * }
 * </code>
 *
 * <p>Note that the application must care about handling HibernateExceptions itself,
 * preferably via delegating to SessionFactoryUtils' convertHibernateAccessException
 * that converts them to ones that are compatible with the org.springframework.dao
 * exception hierarchy (like HibernateTemplate does).
 *
 * <p>Unfortunately, this interceptor cannot convert checked HibernateExceptions
 * to unchecked dao ones automatically. The intercepted method would have to throw
 * HibernateException to be able to achieve this - thus the caller would still have
 * to catch or rethrow it, even if it will never be thrown if intercepted.
 *
 * <p>This class can be considered a declarative alternative to HibernateTemplate's
 * callback approach. The advantages are:
 * <ul>
 * <li>no anonymous classes necessary for callback implementations;
 * <li>the possibility to throw any application exceptions from within data access code.
 * </ul>
 * The drawbacks are:
 * <ul>
 * <li>the dependency on interceptor configuration;
 * <li>the delegating try/catch blocks.
 * </ul>
 *
 * <p>Note: This class, like all of Spring's Hibernate support, requires
 * Hibernate 2.0 (initially developed with RC1).
 *
 * @author Juergen Hoeller
 * @since 13.06.2003
 * @see SessionFactoryUtils#getSession
 * @see HibernateTransactionManager
 * @see HibernateTemplate
 */
public class HibernateInterceptor extends HibernateAccessor implements MethodInterceptor {

	public Object invoke(MethodInvocation methodInvocation) throws Throwable {
		boolean existingTransaction = false;
		Session session = SessionFactoryUtils.getSession(getSessionFactory(), getEntityInterceptor());
		if (SessionFactoryUtils.isSessionBoundToThread(session, getSessionFactory())) {
			logger.debug("Found thread-bound session for Hibernate interceptor");
			existingTransaction = true;
		}
		else {
			logger.debug("Using new session for Hibernate interceptor");
			if (getFlushMode() == FLUSH_NEVER) {
				session.setFlushMode(FlushMode.NEVER);
			}
			SessionFactoryUtils.getThreadObjectManager().bindThreadObject(getSessionFactory(),
			                                                              new SessionHolder(session));
		}
		try {
			Object retVal = methodInvocation.proceed();
			if (isFlushNecessary(existingTransaction)) {
				logger.debug("Eagerly flushing Hibernate session in HibernateInterceptor");
				session.flush();
			}
			return retVal;
		}
		finally {
			if (existingTransaction) {
				logger.debug("Not closing pre-bound Hibernate session after interceptor");
			}
			else {
				SessionFactoryUtils.getThreadObjectManager().removeThreadObject(getSessionFactory());
				SessionFactoryUtils.closeSessionIfNecessary(session, getSessionFactory());
			}
		}
	}

}
