package edu.uams.clara.core.jpa.hibernate.dialect;

import java.sql.Types;

public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {
	public PostgreSQLDialect(){
		super();
		//registerColumnType(Types.BIGINT, "bigint"); // Overwrite SQL Server datatype BIGINT
		registerColumnType(Types.SQLXML, "xml");
	}
}
