package edu.uams.clara.core.jpa.hibernate.dialect;

import java.sql.Types;

/**
 * This class implements our custom Hibernate Dialect that fixes the mapping of
 * Hibernate Type BIGINT (default is NUMERIC(19,0)) to a SQL BIGINT
 * 
 * @author Jiang Bian
 */
public class SQLServerDialect extends
		org.hibernate.dialect.SQLServer2012Dialect {
	public SQLServerDialect() {
		super();
		//registerColumnType(Types.BIGINT, "BIGINT"); // Overwrite SQL Server
													// datatype BIGINT		
		registerColumnType(Types.SQLXML, "XML");
		//registerColumnType(Types.BOOLEAN, "BIT");
		//registerColumnType(Types.DATE, "datetime");
		
	}
}