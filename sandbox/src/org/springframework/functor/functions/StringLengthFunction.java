/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.functor.functions;

import org.springframework.functor.UnaryFunction;

/**
 * Returns the length of an object's string form, or zero if the object is null.
 * 
 * @author Keith Donald
 */
public class StringLengthFunction implements UnaryFunction {
    private static final StringLengthFunction INSTANCE = new StringLengthFunction();

    public Object evaluate(Object value) {
        if (value == null) {
            return new Integer(0);
        }
        return new Integer(String.valueOf(value).length());
    }

    /**
     * Returns the shared instance--this is possible as the default functor for
     * this class is immutable and stateless.
     * 
     * @return the shared instance
     */
    public static UnaryFunction instance() {
        return INSTANCE;
    }

}