/*
 * Copyright 2002-2004 the original author or authors.
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
package org.springframework.samples.phonebook.web.flow.action;

import org.springframework.samples.phonebook.domain.PhoneBook;
import org.springframework.samples.phonebook.domain.PhoneBookQuery;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.FlowExecutionContext;
import org.springframework.web.flow.action.AbstractAction;

public class ExecuteQueryAction extends AbstractAction {

	private PhoneBook phoneBook;

	public void setPhoneBook(PhoneBook phoneBook) {
		this.phoneBook = phoneBook;
	}

	protected Event doExecuteAction(FlowExecutionContext context) throws Exception {
		PhoneBookQuery query = (PhoneBookQuery)context.getRequestScope().getRequiredAttribute("query",
				PhoneBookQuery.class);
		context.getRequestScope().setAttribute("persons", phoneBook.query(query));
		return success();
	}
}