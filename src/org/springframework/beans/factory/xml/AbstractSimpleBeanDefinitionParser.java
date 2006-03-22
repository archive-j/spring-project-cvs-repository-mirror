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

package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractSimpleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	protected final void doParse(Element element, BeanDefinitionBuilder builder) {

		NamedNodeMap attributes = element.getAttributes();
		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attribute = (Attr) attributes.item(x);
			String name = attribute.getLocalName();

			if (ID_ATTRIBUTE.equals(name)) {
				continue;
			}

			builder.addPropertyValue(extractPropertyName(name), attribute.getValue());
		}
		postProcess(builder, element);
	}

	protected String extractPropertyName(String attributeName) {
		Assert.notNull(attributeName, "'attributeName' cannot be null.");
		if (attributeName.indexOf("-") == -1) {
			return attributeName;
		}
		String[] parts = StringUtils.split(attributeName, "-");
		StringBuffer result = new StringBuffer(attributeName.length());
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (i == 0) {
				result.append(part);
			}
			else {
				result.append(Character.toUpperCase(part.charAt(0)));
				result.append(part.substring(1));
			}
		}
		return result.toString();
	}

	protected void postProcess(BeanDefinitionBuilder beanDefinition, Element element) {
	}
}
