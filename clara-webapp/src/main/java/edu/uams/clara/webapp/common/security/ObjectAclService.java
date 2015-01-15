package edu.uams.clara.webapp.common.security;

import java.util.Set;

import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;
import edu.uams.clara.webapp.common.domain.usercontext.User;

public interface ObjectAclService {

	void updateObjectAclByUser(Class<?> objectClass, long objectId, User user);
	void updateObjectAclByUserAndPermissions(Class<?> objectClass, long objectId, User user, Set<Permission> permissions);
	
	
	boolean isObjectAccessible(Class<?> objectClass, long objectId, User user);

	void updateObjectAclByStaffXml(Class<?> objectClass, long objectId,
			String staffXml, Boolean ifConsiderEditPermission);

	void deleteObjectAclByXPathAndElementIdAndXml(Class<?> objectClass,
			long objectId, String xPath, String elementId, String xml);
	
	void deleteObjectAclByUserId(Class<?> objectClass,
			long objectId, long userId);
	
	boolean hasEditObjectAccess(Class<?> objectClass, long objectId, User user);

}