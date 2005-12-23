/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.aspectj;

import java.util.Iterator;
import java.util.List;

import org.springframework.aop.Advisor;
import org.springframework.aop.interceptor.ExposeInvocationInterceptor;

/**
 * Utility methods for working with AspectJ proxies.
 * @author Rod Johnson
 *
 */
public abstract class AspectJProxyUtils {
	
	/**
	 * Add special advisors if necessary to work with a proxy chain
	 * that contains AspectJ advisors. This will expose the current Spring AOP invocation
	 * (necessary for some AspectJ pointcut matching) and expose the current AspectJ
	 * JoinPoint. The call will have no effect if there are no AspectJ advisors
	 * in the advisor chain.
	 * @param advisors Advisors available
	 */
	public static void makeAdvisorChainAspectJCapableIfNecessary(List advisors) {
		// Don't add advisors to an empty list; may indicate that proxying is just not required
		if (!advisors.isEmpty()) {
			boolean foundAspectJAdvice = false;
			for (Iterator it = advisors.iterator(); it.hasNext() && !foundAspectJAdvice; ) {
				Advisor advisor = (Advisor) it.next();
				// Be careful not to get the Advice without a guard, as
				// this might eagerly instantiate a non-singleton AspectJ aspect
				if ( 	(advisor instanceof InstantiationModelAwarePointcutAdvisor) || 
						(advisor.getAdvice() instanceof AbstractAspectJAdvice)) {
					foundAspectJAdvice = true;
				}
			}
			if (foundAspectJAdvice && !advisors.contains(ExposeInvocationInterceptor.ADVISOR)) {
				advisors.add(0, ExposeInvocationInterceptor.ADVISOR);
				advisors.add(1, ExposeJoinPointInterceptor.ADVISOR);
			}
		}
	}

}
