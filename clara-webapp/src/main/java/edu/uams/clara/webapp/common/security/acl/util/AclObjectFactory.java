package edu.uams.clara.webapp.common.security.acl.util;

import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;

public class AclObjectFactory {

	public synchronized static SecurableObject createSecurableObject(Class<?> objectClass, long objectId){
		SecurableObject securableObject = new SecurableObject();
		securableObject.setObjectClass(objectClass);
		securableObject.setObjectId(objectId);
		securableObject.setObjectIdExpression(null);
		securableObject.setUseObjectIdExpression(false);
		return securableObject;
	}
	
	public synchronized static SecurableObject createSecurableObject(Class<?> objectClass, long objectId, String objectIdExpression){
		SecurableObject securableObject = new SecurableObject();
		securableObject.setObjectClass(objectClass);
		securableObject.setObjectId(objectId);
		securableObject.setObjectIdExpression(objectIdExpression);
		securableObject.setUseObjectIdExpression(true);
		return securableObject;
	}
	
	public synchronized static SecurableObject createSecurableObject(Class<?> objectClass){
		SecurableObject securableObject = new SecurableObject();
		securableObject.setObjectClass(objectClass);
		securableObject.setUseObjectIdExpression(true);
		return securableObject;
	}
	
	public synchronized static SecurableObjectAcl createSecurableObjectAcl(SecurableObject securableObject, Permission permission, Class<?> ownerClass, long ownerId){
		SecurableObjectAcl aclObject = new SecurableObjectAcl();
		aclObject.setSecurableObject(securableObject);
		aclObject.setPermission(permission);
		aclObject.setOwnerClass(ownerClass);
		aclObject.setOwnerId(ownerId);
		//aclObject.setOwnerGrantedAuthority(false);	
		return aclObject;
	}
	
}
