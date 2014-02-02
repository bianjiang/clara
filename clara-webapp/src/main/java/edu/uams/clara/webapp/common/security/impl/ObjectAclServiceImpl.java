package edu.uams.clara.webapp.common.security.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.security.acl.SecurableObjectAclDao;
import edu.uams.clara.webapp.common.dao.security.acl.SecurableObjectDao;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObject;
import edu.uams.clara.webapp.common.domain.security.acl.SecurableObjectAcl;
import edu.uams.clara.webapp.common.domain.security.acl.enums.Permission;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.security.acl.util.AclObjectFactory;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ObjectAclServiceImpl implements ObjectAclService {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ObjectAclService.class);

	private SecurableObjectDao securableObjectDao;
	private SecurableObjectAclDao securableObjectAclDao;
	
	private XmlProcessor xmlProcessor;
	
	private SecurableObject updateOrSaveSecurableObject(Class<?> objectClass, long objectId){
		SecurableObject thisObject = securableObjectDao
				.getSecurableObjectByClassAndId(objectClass, objectId);
		
		if(thisObject == null){
			thisObject = securableObjectDao.saveOrUpdate(AclObjectFactory
					.createSecurableObject(objectClass, objectId));
		}
		return thisObject;
	}
	
	private SecurableObjectAcl updateOrSaveSecurableObjectAcl(SecurableObject securableObject, Class<?> ownerClass, long ownerObjectId, Permission permission){
		SecurableObjectAcl aclObject = securableObjectAclDao
				.getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(
						ownerClass, ownerObjectId, securableObject, permission);
		
		if(aclObject == null){
			aclObject = AclObjectFactory.createSecurableObjectAcl(securableObject,
					permission, ownerClass, ownerObjectId);			
		}
		aclObject.setRetired(false);
		
		return securableObjectAclDao.saveOrUpdate(aclObject);
	}
	
    @Override
    public void updateObjectAclByUser(Class<?> objectClass, long objectId,
                    User user) {
            logger.debug("update " + objectClass.getSimpleName() + "; objectId: " + objectId + "; user: " + user.getId());
            SecurableObject thisObject = updateOrSaveSecurableObject(objectClass, objectId);
                            
            updateOrSaveSecurableObjectAcl(thisObject, user.getClass(), user.getId(), Permission.READ);
            updateOrSaveSecurableObjectAcl(thisObject, user.getClass(), user.getId(), Permission.WRITE);    
            updateOrSaveSecurableObjectAcl(thisObject, user.getClass(), user.getId(), Permission.ACCESS);
            
    }
    
    @Override
    public void updateObjectAclByUserAndPermissions(Class<?> objectClass, long objectId,
                    User user, Set<Permission> permissions) {
            logger.debug("update " + objectClass.getSimpleName() + "; objectId: " + objectId + "; user: " + user.getId());
            SecurableObject thisObject = updateOrSaveSecurableObject(objectClass, objectId);
            
            for (Permission p:permissions){
                    updateOrSaveSecurableObjectAcl(thisObject, user.getClass(), user.getId(), p);
            }       
            
    }
	
	@Override
	public void deleteObjectAclByXPathAndElementIdAndXml(Class<?> objectClass, long objectId, String xPath, String elementId, String xml) {
		logger.debug("to be deleted");
		try {
			String elementXml = xmlProcessor.getElementByPathById(xPath, xml, elementId);
			List<String> userIds = xmlProcessor.listElementStringValuesByPath(
					"//staff/user/@id",
					elementXml);
			
			logger.debug("elementXml: " + elementXml);
			
			SecurableObject thisObject = updateOrSaveSecurableObject(objectClass, objectId);
			
			for (String userId : userIds) {
				
				long ownerObjectId = Long.parseLong(userId);
				
				logger.debug("userId: " + ownerObjectId);
				
				SecurableObjectAcl aclObject = securableObjectAclDao
						.getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(
								User.class, ownerObjectId, thisObject, Permission.READ);
				
				aclObject.setRetired(true);
				
				securableObjectAclDao.saveOrUpdate(aclObject);
				
				SecurableObjectAcl writeAclObject = securableObjectAclDao
						.getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(
								User.class, ownerObjectId, thisObject, Permission.WRITE);
				
				if (writeAclObject != null){
					writeAclObject.setRetired(true);
					
					securableObjectAclDao.saveOrUpdate(writeAclObject);
				}
				
				logger.debug("remove userId: " + ownerObjectId + "; from objectId: " + thisObject.getId());

			}
			
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public void deleteObjectAclByUserId(Class<?> objectClass, long objectId,
			long userId) {
		SecurableObject thisObject = updateOrSaveSecurableObject(objectClass, objectId);
		
		try {
			SecurableObjectAcl aclObject = securableObjectAclDao
					.getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(
							User.class, userId, thisObject, Permission.READ);
			
			if (aclObject != null){
				aclObject.setRetired(true);
				
				securableObjectAclDao.saveOrUpdate(aclObject);
			}		
			
			SecurableObjectAcl writeAclObject = securableObjectAclDao
					.getSecurableObjectAclByOwnerAndSecurableObjectAndPermission(
							User.class, userId, thisObject, Permission.WRITE);
			
			if (writeAclObject != null){
				writeAclObject.setRetired(true);
				
				securableObjectAclDao.saveOrUpdate(writeAclObject);
			}
			
			logger.debug("remove userId: " + userId + "; from objectId: " + thisObject.getId());
		} catch (Exception e){
			
		}
		
		
	}	
	
	
	@Override
	public void updateObjectAclByStaffXml(Class<?> objectClass, long objectId, String staffXml) {
		List<String> userIds = null;
		List<String> roles = null;
		List<String> responsibilities = null;
		try {
			logger.debug("update " + objectClass.getSimpleName() + "; objectId: " + objectId + "; staffXml: " + staffXml);
			userIds = xmlProcessor.listElementStringValuesByPath(
					"//staff/user/@id",
					staffXml);
			roles = xmlProcessor.listElementStringValuesByPath("//staff/user/roles/role", staffXml);
			responsibilities = xmlProcessor.listElementStringValuesByPath("//staff/user/reponsibilities/responsibility", staffXml);
			
			logger.debug("find " + userIds.size() + " users!!!");
			
			
			SecurableObject thisObject = updateOrSaveSecurableObject(objectClass, objectId);
			
			for (String userId : userIds) {
				
				long ownerObjectId = Long.parseLong(userId);
				
				if (roles.contains("Principal Investigator") || roles.contains("Treating Physician") || roles.contains("Study Coordinator")){
					updateOrSaveSecurableObjectAcl(thisObject, User.class, ownerObjectId, Permission.WRITE);
					updateOrSaveSecurableObjectAcl(thisObject, User.class, ownerObjectId, Permission.READ);
				} else if (responsibilities.contains("Managing CLARA submission")){
					updateOrSaveSecurableObjectAcl(thisObject, User.class, ownerObjectId, Permission.WRITE);
					updateOrSaveSecurableObjectAcl(thisObject, User.class, ownerObjectId, Permission.READ);
				} else {
					updateOrSaveSecurableObjectAcl(thisObject, User.class, ownerObjectId, Permission.READ);
				}
				
				//updateOrSaveSecurableObjectAcl(thisObject, User.class, ownerObjectId, Permission.READ);

			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Override
	public boolean isObjectAccessible(Class<?> objectClass, long objectId,
			User user) {
		
		List<Permission> userObjectPermissions = null;
		try{
			userObjectPermissions = securableObjectAclDao.getUserPermissionsOnSecurableObjectByOwnerAndObject(user.getClass(), user.getId(), objectClass, objectId);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}

		//any permission is fine...
		if (userObjectPermissions != null && !userObjectPermissions.isEmpty()){
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean hasEditObjectAccess(Class<?> objectClass, long objectId,
			User user) {		
		List<Permission> userObjectPermissions = null;
		try{
			userObjectPermissions = securableObjectAclDao.getUserPermissionsOnSecurableObjectByOwnerAndObject(user.getClass(), user.getId(), objectClass, objectId);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}

		//any permission is fine...
		if (userObjectPermissions != null && !userObjectPermissions.isEmpty()){
			if (userObjectPermissions.contains(Permission.WRITE) || userObjectPermissions.contains(Permission.ACCESS)){
				return true;
			}
		}
		
		return false;
	}

	public SecurableObjectDao getSecurableObjectDao() {
		return securableObjectDao;
	}

	@Autowired(required=true)
	public void setSecurableObjectDao(SecurableObjectDao securableObjectDao) {
		this.securableObjectDao = securableObjectDao;
	}

	public SecurableObjectAclDao getSecurableObjectAclDao() {
		return securableObjectAclDao;
	}

	@Autowired(required=true)
	public void setSecurableObjectAclDao(SecurableObjectAclDao securableObjectAclDao) {
		this.securableObjectAclDao = securableObjectAclDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}
