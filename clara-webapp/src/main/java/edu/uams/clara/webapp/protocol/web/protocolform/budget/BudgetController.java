package edu.uams.clara.webapp.protocol.web.protocolform.budget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
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
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class BudgetController {
	
	private final static Logger logger = LoggerFactory.getLogger(BudgetController.class);

	private ProtocolFormDao protocolFormDao;
	private ProtocolDao protocolDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;	
	
	private ObjectAclService objectAclService;
	
	private RoleDao roleDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private XmlProcessor xmlProcessor;
	
	private MutexLockService mutexLockService;
	
	private AuditService auditService;
	
	private static final int timeOutPeriod = 45;
	
	private Set<Permission> getUserObjectSpecificPermissions(
			long objectId, User user) {
		Set<Permission> permissions = new HashSet<Permission>(0);

		if (objectAclService.isObjectAccessible(Protocol.class, objectId, user)) {

			Role studyStaffRole = roleDao.getRoleByCommittee(Committee.PI);

			permissions.addAll(studyStaffRole.getDefaultPermissions());
			
		}

		return permissions;
	}
	
	private List<ProtocolFormStatusEnum> protocolFormStatusLst = new ArrayList<ProtocolFormStatusEnum>();{
		protocolFormStatusLst.add(ProtocolFormStatusEnum.DRAFT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_BUDGET_NEGOTIATIONS);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_BUDGET_NEGOTIATIONS_PENDING_PI_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.RETURN_FOR_BUDGET_NEGOTIATIONS);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		protocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		//protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT);
		//protocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT);
		//protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION_MAJOR_CONTINGENCIES);
		//protocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION_MINOR_CONTINGENCIES);
	}
	
	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/budgets/budgetbuilder", method = RequestMethod.GET)
	public String getBudgetBuilderPage(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam(value = "coversheet", required = false) String coversheet,
			@RequestParam(value = "readOnly", required = false) Boolean readOnly,
			ModelMap modelMap) {
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		ProtocolFormStatus latestProtocolFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolFormId);
		
		boolean signOffLock = false;
		
		List<ProtocolFormStatus> existingProtocolFormStatusLst = protocolFormStatusDao.getAllProtocolFormStatusByFormId(protocolFormId);
		List<ProtocolFormStatusEnum> existingProtocolFormStatusEnumLst = new ArrayList<ProtocolFormStatusEnum>();
		
		for (ProtocolFormStatus pfs : existingProtocolFormStatusLst){
			existingProtocolFormStatusEnumLst.add(pfs.getProtocolFormStatus());
		}
		
		if (existingProtocolFormStatusEnumLst.contains(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF)){
			signOffLock = true;
		}
		
		Set<Permission> objectPermissions = getUserObjectSpecificPermissions(protocolId, user);
		
		if (objectPermissions != null && !objectPermissions.isEmpty()){
			if (!protocolFormStatusLst.contains(latestProtocolFormStatus.getProtocolFormStatus()) || signOffLock){
				objectPermissions.remove(Permission.EDIT_BUDGET);
				objectPermissions.remove(Permission.EDIT_LOCKED_BUDGET);
			}
		}

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
			objectPermissions.remove(Permission.EDIT_BUDGET);
			objectPermissions.remove(Permission.EDIT_LOCKED_BUDGET);
		}
		
		ProtocolFormXmlData budgetXmlData = null;
		ProtocolFormXmlData protocolXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());		

		//logger.debug("protocolXmlDataId: " + protocolXmlData.getId());
		
		try{
			budgetXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.BUDGET);			
		}catch(EmptyResultDataAccessException ex){
			ex.printStackTrace();
							
			budgetXmlData = new ProtocolFormXmlData();
			
			budgetXmlData.setCreated(new Date());
			budgetXmlData.setProtocolForm(protocolForm);
			budgetXmlData.setProtocolFormXmlDataType(ProtocolFormXmlDataType.BUDGET);
			budgetXmlData.setXmlData("<budget></budget>");

			budgetXmlData = protocolFormXmlDataDao.saveOrUpdate(budgetXmlData);
			
			// 9/9/2013 - moved try/catch below
			
		}
		
		// 9/9/2013 - Make "budget-created" path, regardless of if budget xml is found (for modifications of protocols with previous budgets)
		try {
			String protocolFormXmlDataXml = protocolXmlData.getXmlData();
			
			protocolFormXmlDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/budget-created", protocolFormXmlDataXml, "y");
			
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
		
		//logger.debug("checking who is editing...");
		MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
				ProtocolForm.class, protocolForm.getId());

		Date currentDate = new Date();
		Date lastModifiedDate = (mutexLock != null) ? mutexLock.getModified()
				: null;
		DateTime currentDateTime = new DateTime(currentDate);
		DateTime lastModifiedDateTime = (mutexLock != null) ? new DateTime(
				lastModifiedDate) : null;

		String isLocked = "true";
		String isLockedUserString = "";
		long isLockedUserId = 0;

		if (!mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
				ProtocolForm.class, protocolForm.getId(), user)) {
			isLocked = "false";
		} else {
			if (mutexLock != null) {

				logger.debug("time period after last access: "
						+ Minutes.minutesBetween(lastModifiedDateTime,
								currentDateTime).getMinutes());
				if (Minutes.minutesBetween(lastModifiedDateTime,
						currentDateTime).getMinutes() > timeOutPeriod) {
					isLocked = "false";
				} else {
					isLocked = "true";
					isLockedUserString = mutexLock.getUser().getPerson()
							.getFullname();
					isLockedUserId = mutexLock.getUser().getId();
				}
			}
		}
		
		String message = "ProtocolId: " + protocolId + "Form Id: "+ protocolFormId +" UserID: " + user.getId();
		
		if (readOnly != null && readOnly) {
			auditService.auditEvent(AuditService.AuditEvent.VIEW_BUDDGET.toString(), message, budgetXmlData.getXmlData());
		} else {
			auditService.auditEvent(AuditService.AuditEvent.EDIT_BUDGET.toString(), message, budgetXmlData.getXmlData());
		}
		
		modelMap.put("isLocked", isLocked);
		modelMap.put("isLockedUserString", isLockedUserString);
		modelMap.put("isLockedUserId", isLockedUserId);
		modelMap.put("protocolForm", protocolForm);
		modelMap.put("protocolMetaDataXml",protocolDao.findById(protocolId).getMetaDataXml());
		modelMap.put("protocolXmlData", protocolXmlData);
		modelMap.put("budgetXmlData", budgetXmlData);
		modelMap.put("protocolId", protocolId);
		//modelMap.put("protocolXmlDataId", protocolXmlData.getId());
		modelMap.put("user", user);
		modelMap.put("objectPermissions", objectPermissions);
		modelMap.put("readOnly", readOnly);
		
		return (coversheet != null)?"protocol/protocolform/budget/coversheet":"protocol/protocolform/budget/budgetbuilder";
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


	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
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

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}
	
	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public AuditService getAuditService() {
		return auditService;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

}
