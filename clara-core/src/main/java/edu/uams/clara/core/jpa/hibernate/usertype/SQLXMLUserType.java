package edu.uams.clara.core.jpa.hibernate.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate by default dosen't support xml type
 * 
 * @author bianjiang
 *
 */
public class SQLXMLUserType implements UserType, Serializable  {

	private static final Logger logger = LoggerFactory.getLogger(SQLXMLUserType.class);
	
	private static final long serialVersionUID = 825459504530928658L;
	private static final int[] SQL_TYPES = {Types.SQLXML};

	@Override
	public Object assemble(Serializable cached, Object owner)
			throws HibernateException {
		
		return cached;
	}

	@Override
	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		
		return (java.io.Serializable)value;
	}

	@Override
	public boolean equals(Object x, Object y) throws HibernateException {
		if (x == y) {
			return true;
		} else if (x == null || y == null) {
			return false;
		} else {
			return x.equals(y);
		}

	}

	@Override
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		
		//logger.debug("i'm here!!!" + names[0]);
		//logger.debug("value: " + rs.getString(names[0]));
		if(rs.wasNull()) return null;
		else return rs.getString(names[0]);
	}


	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		
		if(value == null){
			st.setString(index, "");
		}else{
			if(value instanceof String){
				st.setString(index, (String)value);
			}else{
				st.setString(index, value.toString());
			}
		}
	}

	@Override
	public Class<?> returnedClass() {
		return String.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

}
