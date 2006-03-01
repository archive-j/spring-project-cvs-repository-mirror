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

package org.springframework.transaction.config;

import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.aop.config.NamespaceHandlerUtils;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.interceptor.TransactionAttributeSourceAdvisor;
import org.w3c.dom.Element;

/**
 * @author Rob Harrop
 * @since 2.0
 */
class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	private static final String TRANSACTION_ATTRIBUTE_SOURCE_ADVISOR_NAME = ".transactionAttributeSourceAdvisor";

	public static final String TRANSACTION_INTERCEPTOR = "transactionInterceptor";

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionRegistry registry = parserContext.getRegistry();

		// register the APC if needed
		NamespaceHandlerUtils.registerAutoProxyCreatorIfNecessary(registry);

		String transactionManagerName = element.getAttribute(TxNamespaceHandler.TRANSACTION_MANAGER);

		// create the TransactionInterceptor definition
		RootBeanDefinition interceptorDefinition = new RootBeanDefinition(TransactionInterceptor.class);
		interceptorDefinition.setPropertyValues(new MutablePropertyValues());
		interceptorDefinition.getPropertyValues().addPropertyValue(TxNamespaceHandler.TRANSACTION_MANAGER, new RuntimeBeanReference(transactionManagerName));
		interceptorDefinition.getPropertyValues().addPropertyValue(TxNamespaceHandler.TRANSACTION_ATTRIBUTE_SOURCE, new RootBeanDefinition(TxNamespaceHandler.getAnnotationSourceClass()));

		// create the TransactionAttributeSourceAdvisor definition
		RootBeanDefinition advisorDefinition = new RootBeanDefinition(TransactionAttributeSourceAdvisor.class);
		advisorDefinition.setPropertyValues(new MutablePropertyValues());
		advisorDefinition.getPropertyValues().addPropertyValue(TRANSACTION_INTERCEPTOR, interceptorDefinition);

		registry.registerBeanDefinition(TRANSACTION_ATTRIBUTE_SOURCE_ADVISOR_NAME, advisorDefinition);

		return null;
	}
}
