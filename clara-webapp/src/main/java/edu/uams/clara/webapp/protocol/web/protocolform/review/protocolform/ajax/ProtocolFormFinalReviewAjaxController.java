package edu.uams.clara.webapp.protocol.web.protocolform.review.protocolform.ajax;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.exception.ClaraRunTimeException;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicServiceContainer;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolFormFinalReviewAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormFinalReviewAjaxController.class);
	
	private ProtocolDao protocolDao;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	
	private XmlProcessor xmlProcessor;	
	
	private UserAuthenticationService userAuthenticationService;
	
	private UserDao userDao;
	
	private EmailTemplateDao emailTemplateDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer;
	
	private MutexLockService mutexLockService;
	
	private FormService formService;
	
	private ProtocolFormService protocolFormService;
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/committee-review-form", method = RequestMethod.GET)
	public @ResponseBody String getCommitteesListForReview(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("reviewFormIdentifier") String reviewFormIdentifier,
			@RequestParam("reviewFormType") String reviewFormType,
			ModelMap modelMap) {
		String extraContractResultXml = protocolFormReviewLogicServiceContainer.getProtocolFormReviewLogicService(reviewFormType).getExtraContent(protocolFormId, reviewFormIdentifier);

		return extraContractResultXml;
	}
	
	
	/***
	 * General committee review sign
	 * @param protocolId
	 * @param protocolFormId
	 * @param committee
	 * @param username
	 * @param password
	 * @param userId
	 * @param xmlData
	 * @param action
	 * @param modelMap
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/{protocolFormUrlName}/{committeeReviewPage}/sign", method = RequestMethod.POST)
	public @ResponseBody JsonResponse signAndSubmitGeneralCommittee(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("committeeReviewPage") String committeeReviewPage,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			@RequestParam("committee") Committee committee,
			@RequestParam(value="username", required=false) String username,
			@RequestParam(value="password", required=false) String password,
			@RequestParam("userId") long userId,
			@RequestParam(value="xmlData", required=false) String xmlData,
			@RequestParam(value="note", required=false) String note,
			@RequestParam(value="action", required=false) String action,
			ModelMap modelMap) throws Exception {
		if (action == null || action.isEmpty()){
			return new JsonResponse(true, "You need to make a decision!", "", false, null);
		}
		
		User user = (User) ((SecurityContext) SecurityContextHolder.getContext())
				.getAuthentication().getPrincipal();
	
		
		boolean authenticated = userAuthenticationService.isAuthenticated(username, password);
		
		if (authenticated) {
			
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			
			ProtocolFormStatus latestFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolFormId);
			
			if (committee.equals(Committee.PI) && !latestFormStatus.getProtocolFormStatus().equals(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF)){
				try {
					//String workflow = protocolFormService
					//		.workFlowDetermination(protocolXmlData);
					//logger.debug("workflow: " + workflow);
					String isSpecficRole = protocolFormService.finalSignOffDetermination(protocolForm, user);

					//workflow = (isSpecficRole.contains("NOT")) ? "" : workflow;

					protocolFormService.triggerPIAction("SIGN_SUBMIT",
							isSpecficRole, "", protocolForm, user,
							null);
				} catch (Exception e) {
					return new JsonResponse(true, e.getMessage(), "", false, null);
				}
			} else {
				try{
					businessObjectStatusHelperContainer
					.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
					.triggerAction(
							protocolForm,
							committee,
							user,
							action,
							note, xmlData);
				} catch (ClaraRunTimeException e){
					return new JsonResponse(true, e.getMessage(), "", false, null);
				}
			}
				
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(ProtocolForm.class, protocolFormId);
			
			if (mutexLock != null){
				if (mutexLock.getUser().getId() == user.getId()){
					mutexLockService.unlockMutexLock(mutexLock);
				}
			}
			
			return new JsonResponse(false, "", "", false, null);
			
			
		}else{
			return new JsonResponse(true, "Your username or password is not correct!", "", false, null);
		}		
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/{protocolFormUrlName}/{committeeReviewPage}/assign-committee", method = RequestMethod.POST)
	public @ResponseBody JsonResponse assignIndividualCommittee(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("committeeReviewPage") String committeeReviewPage,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			@RequestParam("committee") Committee committee,
			@RequestParam("userId") long userId,
			@RequestParam(value="xmlData", required=false) String xmlData,
			@RequestParam(value="note", required=false) String note,
			@RequestParam(value="action", required=false) String action,
			ModelMap modelMap) throws Exception {
		if (action == null || action.isEmpty()){
			return new JsonResponse(true, "You need to make a decision!", "", false, null);
		}
		
		User user = (User) ((SecurityContext) SecurityContextHolder.getContext())
				.getAuthentication().getPrincipal();
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			
		try{
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
			.triggerAction(
					protocolForm,
					committee,
					user,
					action,
					note, xmlData);
		} catch (ClaraRunTimeException e){
			return new JsonResponse(true, "Failed to assign individual committee!", "", false, null);
		}
		
		/*
		MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(ProtocolForm.class, protocolFormId);
		
		if (mutexLock != null){
			if (mutexLock.getUser() == user){
				mutexLockService.unlockMutexLock(mutexLock);
			}
		}
		*/
		
		return new JsonResponse(false, "", "", false, null);
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/{protocolFormUrlName}/sign", method = RequestMethod.POST)
	public @ResponseBody JsonResponse signAndSendLetter(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("committeeReviewPage") String committeeReviewPage,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			@RequestParam("committee") Committee committee,
			@RequestParam(value="username", required=false) String username,
			@RequestParam(value="password", required=false) String password,
			@RequestParam("userId") long userId,
			@RequestParam(value="xmlData", required=false) String xmlData,
			@RequestParam(value="note", required=false) String note,
			@RequestParam(value="action", required=false) String action,
			ModelMap modelMap) throws Exception {
		if (action == null || action.isEmpty()){
			return new JsonResponse(true, "You need to make a decision!", "", false, null);
		}
		
		boolean authenticated = userAuthenticationService.isAuthenticated(username, password);
		
		if (authenticated) {
			
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			
			User user = userDao.findById(userId);
			
			try{
				businessObjectStatusHelperContainer
				.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
				.triggerAction(
						protocolForm,
						committee,
						user,
						action,
						note, xmlData);
			} catch (ClaraRunTimeException e){
				return new JsonResponse(true, e.getMessage(), "", false, null);
			}
			
			
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(ProtocolForm.class, protocolFormId);
			
			if (mutexLock != null){
				if (mutexLock.getUser().getId() == user.getId()){
					mutexLockService.unlockMutexLock(mutexLock);
				}
			}
			
			return new JsonResponse(false, "", "", false, null);
			
			
		}else{
			return new JsonResponse(true, "Your username or password is not correct!", "", false, null);
		}		
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/review/{protocolFormUrlName}/request-review", method = RequestMethod.POST)
	public @ResponseBody String requestPharmacyReview(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee,
			@RequestParam("committeeRequest") Committee committeeRequest,
			@RequestParam("userId") long userId,
			@RequestParam("action") String action,
			@RequestParam(value="notes", required=false) String notes,
			ModelMap modelMap) throws Exception {			
		
		try{
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			
			User user = userDao.findById(userId);
			
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(protocolForm.getProtocolFormType().toString())
			.triggerAction(
					protocolForm,
					committee,
					user,
					action,
					notes, null);
			
			ProtocolFormCommitteeStatus protocolFormCommitteeStatus = protocolFormCommitteeStatusDao
					.getLatestByCommitteeAndProtocolFormId(committeeRequest,
							protocolFormId);
			if (protocolFormCommitteeStatus != null) {
				// return
				// XMLResponseHelper.xmlResult(protocolFormCommitteeStatus.getProtocolFormCommitteeStatus());
				return XMLResponseHelper.xmlResult(protocolFormCommitteeStatus.getProtocolFormCommitteeStatus());
			} else {
				return XMLResponseHelper.xmlResult("");
			}
			
		} catch (Exception e){
			e.printStackTrace();
			return XMLResponseHelper.xmlResult("");
		}
	
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}
	
	@Autowired(required=true)
	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required=true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}
	
	@Autowired(required=true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public UserAuthenticationService getUserAuthenticationService() {
		return userAuthenticationService;
	}

	@Autowired(required=true)
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService) {
		this.userAuthenticationService = userAuthenticationService;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}

	@Autowired(required=true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}


	public ProtocolFormReviewLogicServiceContainer getProtocolFormReviewLogicServiceContainer() {
		return protocolFormReviewLogicServiceContainer;
	}

	@Autowired(required=true)
	public void setProtocolFormReviewLogicServiceContainer(
			ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer) {
		this.protocolFormReviewLogicServiceContainer = protocolFormReviewLogicServiceContainer;
	}


	public FormService getFormService() {
		return formService;
	}

	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}


	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}

	@Autowired(required=true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}


	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}
}
