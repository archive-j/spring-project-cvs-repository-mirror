/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.freemarker;

import org.springframework.core.NestedRuntimeException;


/**
 * Exception thrown on Freemarker initialization failure 
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id$
 */
public class FreemarkerInitializationException extends NestedRuntimeException {
	
	public FreemarkerInitializationException(String msg) {
		super(msg);
	}
	
	public FreemarkerInitializationException(String msg, Exception ex) {
		super(msg, ex);
	}
}
