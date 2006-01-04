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

package org.springframework.scripting;

/**
 * Interface that defines a script-based resounce that can be used
 * to create an arbitrary java <code>Object</code>. Created by a
 * {@link AbstractScriptFactory factory} object.
 *
 * @author Rob Harrop
 * @author Rod Johnson
 * @since 2.0M2
 */
public interface Script {

	/**
	 * Creates the instance of the scripted object.
	 */
	Object createObject() throws Exception;

}
