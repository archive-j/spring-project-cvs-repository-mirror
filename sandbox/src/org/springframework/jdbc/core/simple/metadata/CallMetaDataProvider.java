/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple.metadata;

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.CallMetaDataContext;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author trisberg
 */
public interface CallMetaDataProvider {

	void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException;
	 
	void initializeWithProcedureColumnMetaData(DatabaseMetaData databaseMetaData, String catalogName, String schemaName, String procedureName) throws SQLException;

	String procedureNameToUse(String procedureName);

	String catalogNameToUse(String catalogName);

	String schemaNameToUse(String schemaName);

	String metaDataCatalogNameToUse(String catalogName) ;

	String metaDataSchemaNameToUse(String catalogName) ;

	String parameterNameToUse(String parameterName);

	SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta);

	SqlParameter createDefaultInParameter(String parameterName, CallParameterMetaData meta);

	String getUserName();

	boolean isProcedureColumnMetaDataUsed();

	boolean byPassReturnParameter(String parameterName);
	
	List<CallParameterMetaData> getCallParameterMetaData();

}
