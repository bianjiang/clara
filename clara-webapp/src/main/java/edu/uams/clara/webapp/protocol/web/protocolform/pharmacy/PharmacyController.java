package edu.uams.clara.webapp.protocol.web.protocolform.pharmacy;


import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.usercontext.RoleDao;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class PharmacyController {
	
	private final static Logger logger = LoggerFactory.getLogger(PharmacyController.class);

	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;	
	
	private ObjectAclService objectAclService;
	
	private RoleDao roleDao;
	
	private XmlProcessor xmlProcessor;
	
	private Set<Permission> getUserObjectSpecificPermissions(
			long objectId, User user) {
		Set<Permission> permissions = new HashSet<Permission>(0);

		if (objectAclService.isObjectAccessible(Protocol.class, objectId, user)) {

			Role studyStaffRole = roleDao.getRoleByCommittee(Committee.PI);

			permissions.addAll(studyStaffRole.getDefaultPermissions());
			
		}

		return permissions;
	}
	
	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/pharmacy/pharmacybuilder", method = RequestMethod.GET)
	public String getPharmacyBuilderPage(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam(value = "coversheet", required = false) String coversheet,
			@RequestParam(value = "readOnly", required = false) Boolean readOnly,
			ModelMap modelMap) {
		
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Set<Permission> objectPermissions = getUserObjectSpecificPermissions(protocolId, user);
		
		if (readOnly != null && readOnly){
			if (objectPermissions == null || objectPermissions.isEmpty()){
				/*for(UserRole ur:user.getUserRoles()){
					//if(ur.isRetired()) continue;
					if (!ur.isRetired()){
						//add in role default rights
						objectPermissions.addAll(ur.getRole().getDefaultPermissions());
						
						//add in user specific rights
						//objectPermissions.addAll(ur.getUserRolePermissions());
					}
					
				}*/
				objectPermissions.addAll((Collection)user.getAuthorities());
			}			
			objectPermissions.remove(Permission.EDIT_PHARMACY);
		}
		
		ProtocolFormXmlData pharmacyXmlData = null;
		ProtocolFormXmlData protocolXmlData = null;
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);

		logger.debug("protocolFormId:" + protocolFormId);
		
		if (protocolForm.getProtocolFormType().equals(ProtocolFormType.MODIFICATION)){
			protocolXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.MODIFICATION);
		} else {
			protocolXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.PROTOCOL);
		}
		
		logger.debug("protocolXmlDataId: " + protocolXmlData.getId() );
		try{
			pharmacyXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.PHARMACY);
			
		}catch(EmptyResultDataAccessException ex){
			ex.printStackTrace();
			ProtocolForm protocolform = protocolFormDao.findById(protocolFormId);
					
			
			pharmacyXmlData = new ProtocolFormXmlData();
			
			pharmacyXmlData.setCreated(new Date());
			pharmacyXmlData.setParent(pharmacyXmlData);
			pharmacyXmlData.setProtocolForm(protocolform);
			pharmacyXmlData.setProtocolFormXmlDataType(ProtocolFormXmlDataType.PHARMACY);
			pharmacyXmlData.setXmlData("<pharmacy></pharmacy>");
			
			pharmacyXmlData = protocolFormXmlDataDao.saveOrUpdate(pharmacyXmlData);
			
			try {
				String protocolFormXmlDataXml = protocolXmlData.getXmlData();
				
				protocolFormXmlDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/pharmacy-created", protocolFormXmlDataXml, "y");
				
				protocolXmlData.setXmlData(protocolFormXmlDataXml);
				protocolXmlData = protocolFormXmlDataDao.saveOrUpdate(protocolXmlData);
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
		modelMap.put("protocolForm", pharmacyXmlData.getProtocolForm());
		modelMap.put("protocolXmlData", protocolXmlData);
		
		modelMap.put("protocolId", pharmacyXmlData.getProtocolForm().getProtocol()
				.getId());
		/*modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());*/
		modelMap.put("user", user);
		modelMap.put("objectPermissions", objectPermissions);
		modelMap.put("readOnly", readOnly);

		return (coversheet != null)?"protocol/protocolform/pharmacy/coversheet":"protocol/protocolform/pharmacy/pharmacybuilder";
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
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
