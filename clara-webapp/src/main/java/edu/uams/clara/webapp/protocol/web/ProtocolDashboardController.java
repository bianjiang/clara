package edu.uams.clara.webapp.protocol.web;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.uams.clara.webapp.common.dao.usercontext.RoleDao;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;

@Controller
public class ProtocolDashboardController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolDashboardController.class);
	
	private ProtocolDao protocolDao;	
	
	private ObjectAclService objectAclService;
	
	private RoleDao roleDao;
	
	private Set<Permission> getUserObjectSpecificPermissions(
			Class<?> objectClass, long objectId, User user) {
		Set<Permission> permissions = new HashSet<Permission>(0);

		if (objectClass == null) {
			return permissions;
		}
		logger.debug("getUserObjectSpecificPermissions("
				+ objectClass.toString() + ", " + objectId + ", "
				+ user.getId() + ")");

		if (objectAclService.isObjectAccessible(objectClass, objectId, user)) {

			Role studyStaffRole = roleDao.getRoleByCommittee(Committee.PI);

			permissions.addAll(studyStaffRole.getDefaultPermissions());
			
		}

		return permissions;
	}

	@RequestMapping(value = "/protocols/{protocolId}/dashboard")
	public String getProtocolDashboard(
			@PathVariable("protocolId") long protocolId, ModelMap modelMap) {
		
		Protocol protocol = protocolDao.findById(protocolId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		//ProtocolFormXmlData protocolXmlData = protocolDao.getLastestProtocolXmlDataByProtocolId(protocolId);
		
		//@ToDo this might not be needed, since the status is in the protocol.metaDataXml
		ProtocolStatus protocolStatus = protocolDao.getLatestProtocolStatusByProtocolId(protocolId);

		modelMap.put("protocolStatus", protocolStatus);
		modelMap.put("protocol", protocol);
		
		Set<Permission> objectPermissions = getUserObjectSpecificPermissions(
				Protocol.class, protocolId, u);
		
		if (objectPermissions != null && !objectPermissions.isEmpty()){
			modelMap.put("objectPermissions", objectPermissions);
		}
		
		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "protocol/dashboard";
	}
	
	@RequestMapping(value = "/protocols/{protocolId}/summary")
	public String getSummaryPage(@PathVariable("protocolId") long protocolId, ModelMap modelMap){
		Protocol protocol = protocolDao.findById(protocolId);
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		modelMap.put("protocol", protocol);
		modelMap.put("user", u);
		
		return "protocol/summary";
		
	}
	
	@RequestMapping(value = "/protocols/{protocolId}/form-flow")
	public String getProtocolFormFlow(
			@PathVariable("protocolId") long protocolId, ModelMap modelMap) {
	
		Protocol protocol = protocolDao.findById(protocolId);
		
		//ProtocolFormXmlData protocolXmlData = protocolDao.getLastestProtocolXmlDataByProtocolId(protocolId);
		
		//@ToDo this might not be needed, since the status is in the protocol.metaDataXml
		ProtocolStatus protocolStatus = protocolDao.getLatestProtocolStatusByProtocolId(protocolId);

		modelMap.put("protocolStatus", protocolStatus);
		modelMap.put("protocol", protocol);
		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "protocol/form-flow";
	}
	
	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}


	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}
	
	@Autowired(required=true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}
	
	@Autowired(required=true)
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}	

}
