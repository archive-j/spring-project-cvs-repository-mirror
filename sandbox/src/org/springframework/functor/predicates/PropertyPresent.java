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
package org.springframework.functor.predicates;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.functor.UnaryPredicate;
import org.springframework.util.Assert;

/**
 * Predicate that tests if the specified bean property is "present" - that is,
 * passes the "Required" test.
 * 
 * @author Keith Donald
 * @see Required
 */
public class PropertyPresent implements UnaryPredicate {
    private String propertyName;

    /**
     * Constructs a property present predicate for the specified property.
     * 
     * @param propertyName
     *            The bean property name.
     */
    public PropertyPresent(String propertyName) {
        Assert.notNull(propertyName);
        this.propertyName = propertyName;
    }

    public boolean test(Object bean) {
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        Object value = wrapper.getPropertyValue(propertyName);
        return Required.instance().test(value);
    }

}