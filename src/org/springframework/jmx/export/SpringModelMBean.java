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

package org.springframework.jmx.export;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.AttributeNotFoundException;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.InvalidAttributeValueException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * Extension of the {@link RequiredModelMBean} class that ensures the
 * {@link Thread#getContextClassLoader() context ClassLoader} is switched
 * for the managed resources {@link ClassLoader} before any invocations
 * occur.
 *
 * @author Rob Harrop
 * @see RequiredModelMBean
 */
public class SpringModelMBean extends RequiredModelMBean {

	/**
	 * Stores the {@link ClassLoader} to use for invocations. Defaults
	 * to the current thread {@link ClassLoader}.
	 */
	private ClassLoader managedResourceClassLoader = Thread.currentThread().getContextClassLoader();

	public SpringModelMBean() throws MBeanException, RuntimeOperationsException {
	}

	public SpringModelMBean(ModelMBeanInfo mbi) throws MBeanException, RuntimeOperationsException {
		super(mbi);
	}

	/**
	 * Sets managed resource to expose and stores its {@link ClassLoader}.
	 */
	public void setManagedResource(Object managedResource, String managedResourceType) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException {
		this.managedResourceClassLoader = managedResource.getClass().getClassLoader();
		super.setManagedResource(managedResource, managedResourceType);
	}

	/**
	 * Switches the {@link Thread#getContextClassLoader() context ClassLoader} for
	 * the managed resources {@link ClassLoader} before allowing the invocation to
	 * occur.
	 * @see javax.management.modelmbean.ModelMBean#invoke
	 * @see #managedResourceClassLoader
	 */
	public Object invoke(final String opName, final Object[] opArgs, final String[] sig) throws MBeanException, ReflectionException {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.managedResourceClassLoader);
			return super.invoke(opName, opArgs, sig);
		}
		finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

	/**
	 * Switches the {@link Thread#getContextClassLoader() context ClassLoader} for
	 * the managed resources {@link ClassLoader} before allowing the invocation to
	 * occur.
	 * @see javax.management.modelmbean.ModelMBean#getAttribute
	 * @see #managedResourceClassLoader
	 */
	public Object getAttribute(final String attrName) throws AttributeNotFoundException, MBeanException, ReflectionException {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.managedResourceClassLoader);
			return super.getAttribute(attrName);
		}
		finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

	/**
	 * Switches the {@link Thread#getContextClassLoader() context ClassLoader} for
	 * the managed resources {@link ClassLoader} before allowing the invocation to
	 * occur.
	 * @see javax.management.modelmbean.ModelMBean#getAttributes
	 * @see #managedResourceClassLoader
	 */
	public AttributeList getAttributes(String[] attrNames) {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.managedResourceClassLoader);
			return super.getAttributes(attrNames);
		}
		finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

	/**
	 * Switches the {@link Thread#getContextClassLoader() context ClassLoader} for
	 * the managed resources {@link ClassLoader} before allowing the invocation to
	 * occur.
	 * @see javax.management.modelmbean.ModelMBean#setAttribute
	 * @see #managedResourceClassLoader
	 */
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.managedResourceClassLoader);
			super.setAttribute(attribute);
		}
		finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

	/**
	 * Switches the {@link Thread#getContextClassLoader() context ClassLoader} for
	 * the managed resources {@link ClassLoader} before allowing the invocation to
	 * occur.
	 * @see javax.management.modelmbean.ModelMBean#setAttributes
	 * @see #managedResourceClassLoader
	 */
	public AttributeList setAttributes(AttributeList attributes) {
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.managedResourceClassLoader);
			return super.setAttributes(attributes);
		}
		finally {
			Thread.currentThread().setContextClassLoader(currentClassLoader);
		}
	}

}
