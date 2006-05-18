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

package org.springframework.instrument.classloading;

import org.springframework.core.io.ResourceLoader;

/**
 * Class loader that can be used to load classes without bringing
 * them into the parent loader. Intended to support JPA "temp class loader"
 * requirement, but not JPA-specific.
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class ThrowawayClassLoader extends AbstractOverridingClassLoader {

	public ThrowawayClassLoader(ClassLoader parent) {
		super(parent);
	}

	public ThrowawayClassLoader(ClassLoader parent, ResourceLoader resourceLoader) {
		super(parent, resourceLoader);
	}

	@Override
	public byte[] transformIfNecessary(String name, String internalName, byte[] bytes) {
		if (debug) {
			logger.debug("Throwaway class loader loading class [" + name + "]");
		}
		return bytes;
	}

}
