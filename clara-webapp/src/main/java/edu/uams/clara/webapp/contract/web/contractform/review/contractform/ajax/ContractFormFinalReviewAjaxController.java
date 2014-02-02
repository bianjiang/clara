package edu.uams.clara.webapp.contract.web.contractform.review.contractform.ajax;

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
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.service.contractform.newcontract.NewContractReviewPageExtraContentService;
import edu.uams.clara.webapp.contract.service.email.ContractEmailDataService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractFormFinalReviewAjaxController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormFinalReviewAjaxController.class);
	
	private ContractDao contractDao;
	
	private ContractFormDao contractFormDao;
	
	private ContractFormXmlDataDao contractFormXmlDataDao;
	
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;
	
	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	private XmlProcessor xmlProcessor;	
	
	private UserAuthenticationService userAuthenticationService;
	
	private UserDao userDao;
	
	private EmailTemplateDao emailTemplateDao;
	
	private ContractEmailDataService contractEmailDataService;
	
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	private MutexLockService mutexLockService;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private NewContractReviewPageExtraContentService newContractReviewPageExtraContentService;
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/committee-review-form", method = RequestMethod.GET)
	public @ResponseBody String getReviewPanel(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("reviewFormIdentifier") String reviewFormIdentifier,
			ModelMap modelMap) {

		return newContractReviewPageExtraContentService.getExtraContent(contractFormId, reviewFormIdentifier);
	}
	
	
	/***
	 * General committee review sign
	 * @param contractId
	 * @param contractFormId
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
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/{contractFormUrlName}/{committeeReviewPage}/sign", method = RequestMethod.POST)
	public @ResponseBody JsonResponse signAndSubmitGeneralCommittee(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("committeeReviewPage") String committeeReviewPage,
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
			
			ContractForm contractForm = contractFormDao.findById(contractFormId);
				
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(contractForm.getContractFormType().toString())
			.triggerAction(
					contractForm,
					committee,
					user,
					action,
					note, xmlData);
			
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(ContractForm.class, contractFormId);
			
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
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/review/{contractFormUrlName}/sign", method = RequestMethod.POST)
	public @ResponseBody JsonResponse signAndForwardContract(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
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
			
			ContractForm contractForm = contractFormDao.findById(contractFormId);
			
			User user = userDao.findById(userId);
			
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(contractForm.getContractFormType().toString())
			.triggerAction(
					contractForm,
					committee,
					user,
					action,
					note, xmlData);
			
			return new JsonResponse(false, "", "", false, null);
			
			
		}else{
			return new JsonResponse(true, "Your username or password is not correct!", "", false, null);
		}		
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
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
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
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
	public void setContractFormCommitteeStatusDao(ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}
	
	@Autowired(required=true)
	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

	public ContractEmailDataService getContractEmailDataService() {
		return contractEmailDataService;
	}
	
	@Autowired(required=true)
	public void setContractEmailDataService(ContractEmailDataService contractEmailDataService) {
		this.contractEmailDataService = contractEmailDataService;
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}
	
	@Autowired(required=true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public UserAuthenticationService getUserAuthenticationService() {
		return userAuthenticationService;
	}

	@Autowired(required=true)
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService) {
		this.userAuthenticationService = userAuthenticationService;
	}


	public NewContractReviewPageExtraContentService getNewContractReviewPageExtraContentService() {
		return newContractReviewPageExtraContentService;
	}

	@Autowired(required=true)
	public void setNewContractReviewPageExtraContentService(
			NewContractReviewPageExtraContentService newContractReviewPageExtraContentService) {
		this.newContractReviewPageExtraContentService = newContractReviewPageExtraContentService;
	}


	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}

	@Autowired(required=true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}
}
