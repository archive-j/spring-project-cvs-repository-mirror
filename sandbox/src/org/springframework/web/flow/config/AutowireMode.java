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
package org.springframework.web.flow.config;

import org.springframework.util.enums.support.ShortCodedLabeledEnum;

public class AutowireMode extends ShortCodedLabeledEnum {
	public static final AutowireMode BY_NAME = new AutowireMode(1, "byName");

	public static final AutowireMode BY_TYPE = new AutowireMode(2, "byType");

	public static final AutowireMode CONSTRUCTOR = new AutowireMode(3, "constructor");

	public static final AutowireMode AUTODETECT = new AutowireMode(4, "autodetect");

	public static final AutowireMode NONE = new AutowireMode(5, "none");

	public static final AutowireMode DEFAULT = new AutowireMode(6, "default");

	private AutowireMode(int code, String label) {
		super(code, label);
	}
}