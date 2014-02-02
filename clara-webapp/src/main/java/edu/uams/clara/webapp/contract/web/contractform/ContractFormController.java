package edu.uams.clara.webapp.contract.web.contractform;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.xml.xpath.XPathExpressionException;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormType;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.contract.exception.contractform.ContractFormLockedException;
import edu.uams.clara.webapp.contract.service.ContractFormService;
import edu.uams.clara.webapp.contract.service.ContractMetaDataXmlService;
import edu.uams.clara.webapp.contract.service.ContractService;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.xml.processor.DocumentListToXmlService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;
import edu.uams.clara.webapp.xml.processor.impl.*;

@Controller
public class ContractFormController {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractFormController.class);

	private ContractService contractService;
	private ContractFormService contractFormService;

	private ContractMetaDataXmlService contractMetaDataXmlService;

	private XmlProcessor xmlProcessor;

	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	private ContractFormXmlDataDao contractFormXmlDataDao;
	private ContractDao contractDao;
	private ContractFormDao contractFormDao;
	private ContractFormStatusDao contractFormStatusDao;
	
	private UserDao userDao;

	private AuthenticationManager authenticationManager;
	
	private UserAuthenticationService userAuthenticationService;

	private UserService userService;

	private FormService formService;

	private MutexLockService mutexLockService;
	
	private DocumentListToXmlService documentListToXmlServiceImpl;

	private static final int timeOutPeriod = 45;

	public ContractFormController() {

	}

	@RequestMapping(value = "/contracts/contract-forms/{contractFormUrlName}/create", method = RequestMethod.GET)
	public String startNewContract(
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {
		// final ContractFormXmlData contractFormXmlData =
		// contractService.creatNewContract(ContractFormType.valueOf(contractFormUrlName.replace("-",
		// "_").toUpperCase()));
		final ContractFormXmlData contractFormXmlData = contractService
				.creatNewContract(ContractFormType
						.getContractFormTypeByUrlCode(contractFormUrlName));

		Assert.notNull(contractFormXmlData);
		Assert.notNull(contractFormXmlData.getContractForm());
		Assert.notNull(contractFormXmlData.getContractForm().getContract());

		return "redirect:/contracts/"
				+ contractFormXmlData.getContractForm().getContract().getId()
				+ "/contract-forms/"
				+ contractFormXmlData.getContractForm().getId() + "/"
				+ contractFormUrlName + "/contract-form-xml-datas/"
				+ contractFormXmlData.getId() + "/first-page";
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormUrlName}/create", method = RequestMethod.GET)
	public String startNewForm(
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			@PathVariable("contractId") long contractId, ModelMap modelMap)
			throws XPathExpressionException, IOException, SAXException {
		logger.debug("!!!!!!!!!!!!!!! " + contractFormUrlName);
		// final ContractFormXmlData contractFormXmlData =
		// contractFormService.createNewForm(ContractFormType.valueOf(contractFormUrlName.replace("-",
		// "_").toUpperCase()), contractId);
		final ContractFormXmlData contractFormXmlData = contractFormService
				.createNewForm(ContractFormType
						.getContractFormTypeByUrlCode(contractFormUrlName),
						contractId);

		Assert.notNull(contractFormXmlData);
		Assert.notNull(contractFormXmlData.getContractForm());
		Assert.notNull(contractFormXmlData.getContractForm().getContract());

		return "redirect:/contracts/"
				+ contractFormXmlData.getContractForm().getContract().getId()
				+ "/contract-forms/"
				+ contractFormXmlData.getContractForm().getId() + "/"
				+ contractFormUrlName + "/contract-form-xml-datas/"
				+ contractFormXmlData.getId() + "/first-page";
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/contract-form-xml-datas/{formXmlDataId}/", method = RequestMethod.GET)
	public String viewForm(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("formXmlDataId") long formXmlDataId,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) {

		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		return "redirect:/contracts/" + contractId + "/contract-forms/"
				+ contractFormId + "/" + contractFormUrlName
				+ "/contract-form-xml-datas/" + formXmlDataId
				+ "/first-page?noheader=" + noheader;
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/contract-form-xml-datas/{formXmlDataId}/{page}", method = RequestMethod.GET)
	public String getPageByViewName(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@RequestParam(value = "committee", required = false) Committee committee,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("formXmlDataId") long formXmlDataId,
			@PathVariable("page") String page,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) {

		ContractFormXmlData contractXmlData = contractFormXmlDataDao
				.findById(formXmlDataId);

		if (contractXmlData.getContractForm().isLocked()
				&& !page.equals("summary")) {

			throw new ContractFormLockedException(
					"The contract is locked, cannot be modified!",
					contractXmlData.getContractForm());
		}

		modelMap.put("contractFormXmlData", contractXmlData);
		modelMap.put("committee", committee);
		modelMap.put("contractId", contractXmlData.getContractForm()
				.getContract().getId());
		modelMap.put("contractIdentifier", contractXmlData.getContractForm()
				.getContract().getContractIdentifier());
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());

		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		modelMap.put("noheader", noheader);

		if (noheader) {
			page += "-noheader";
		}

		return "contract/contractform/" + contractFormUrlName.replace("-", "")
				+ "/" + page;
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/contract-form-xml-datas/{formXmlDataId}/review", method = RequestMethod.GET)
	public String getReviewPage(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@RequestParam(value = "committee", required = false) Committee committee,
			@RequestParam(value = "authenticated", required = false) Boolean authenticated,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("formXmlDataId") long formXmlDataId,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) {

		ContractFormXmlData contractXmlData = contractFormXmlDataDao
				.findById(formXmlDataId);

		modelMap.put("contractFormXmlData", contractXmlData);
		modelMap.put("committee", committee);
		modelMap.put("contractId", contractXmlData.getContractForm()
				.getContract().getId());
		modelMap.put("contractIdentifier", contractXmlData.getContractForm()
				.getContract().getContractIdentifier());
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());

		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		modelMap.put("noheader", noheader);

		String page = "review";

		if (noheader) {
			page += "-noheader";
		}

		if (authenticated != null) {
			logger.debug("authenticated: " + authenticated);
			modelMap.put("authenticated", authenticated);
		}

		return "contract/contractform/" + contractFormUrlName.replace("-", "")
				+ "/" + page;
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/contract-form-xml-datas/{contractFormXmlDataId}/sign", method = RequestMethod.POST)
	// @TriggersRemove(cacheName = "contractCache", removeAll = true)
	public String signForm(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {
		String baseUrl = "/contracts/" + contractId + "/contract-forms/"
				+ contractFormId + "/" + contractFormUrlName
				+ "/contract-form-xml-datas/" + contractFormXmlDataId;
		if (noheader == null) {
			noheader = Boolean.FALSE;
		}
		
		User currentUser = userService.getCurrentUser();
		
		boolean authenticated = false;

		try {
			authenticated = userAuthenticationService.isAuthenticated(username, password);

			if (authenticated) {
				ContractFormXmlData contractXmlData = contractFormXmlDataDao
						.findById(contractFormXmlDataId);
				ContractForm contractForm = contractXmlData.getContractForm();

				contractMetaDataXmlService.consolidateContractFormXmlData(
						contractXmlData,
						contractXmlData.getContractFormXmlDataType());

				String workflow = "";

				// String isPIOrNotPI = "IS_PI";

				/*
				 * if
				 * (contractForm.getContractFormType().equals(ContractFormType
				 * .NEW_CONTRACT)){ isPIOrNotPI =
				 * formService.isCurrentUserSpecificRoleOrNot(contractForm,
				 * currentUser, "Principal Investigator")?"IS_PI":"IS_NOT_PI"; }
				 */

				/*
				 * try{ String updateSignedByXml =
				 * xmlProcessor.replaceOrAddNodeValueByPath("/"+
				 * contractForm.getContractFormType().getBaseTag()
				 * +"/signed-by", contractXmlData.getXmlData(), isPIOrNotPI);
				 * contractXmlData.setXmlData(updateSignedByXml);
				 * contractFormXmlDataDao.saveOrUpdate(contractXmlData);
				 * 
				 * } catch(Exception e){ e.printStackTrace(); }
				 */

				contractFormService.triggerPIAction("SIGN_SUBMIT", "",
						workflow, contractForm, currentUser, null);

				unlockContractForm(contractFormId, currentUser);

			}

		} catch (Exception ex) {

			authenticated = false;
			modelMap.put("authenticated", authenticated);
			return "redirect:" + baseUrl + "/review?noheader=" + noheader;

		}

		modelMap.put("authenticated", authenticated);
		if (authenticated) {
			return "redirect:" + baseUrl + "/signed?noheader=" + noheader;
		} else {
			return "redirect:" + baseUrl + "/review?noheader=" + noheader;
		}

	}


	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/contract-form-xml-datas/{contractFormXmlDataId}/signed", method = RequestMethod.GET)
	public String getSignedPage(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormXmlDataId") long contractFormXmlDataId,
			@RequestParam("authenticated") boolean authenticated,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {

		ContractFormXmlData contractXmlData = contractFormXmlDataDao
				.findById(contractFormXmlDataId);

		modelMap.put("contractFormXmlData", contractXmlData);
		modelMap.put("contractId", contractXmlData.getContractForm()
				.getContract().getId());
		modelMap.put("contractIdentifier", contractXmlData.getContractForm()
				.getContract().getContractIdentifier());
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		modelMap.put("authenticated", authenticated);

		return "contract/contractform/" + contractFormUrlName.replace("-", "")
				+ "/signed";

	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/summary", method = RequestMethod.GET)
	public String getSummaryPage(
			@RequestParam(value = "committee", required = false) Committee committee,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) {

		ContractForm contractForm = contractFormDao.findById(contractFormId);

		logger.debug("getting docs for pfid "+contractFormId);
		List<ContractFormXmlDataDocumentWrapper> documents = contractFormXmlDataDocumentDao.listDocumentsByContractFormId(contractFormId);
		logger.debug("# for docs: "+documents.size());
		String documentsXML = documentListToXmlServiceImpl.transformContractFormXmlDataDocumentListToXML(documents);
		logger.debug(documentsXML);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		Map<String, ContractFormXmlData> contractFormXmlDatas = new HashMap<String, ContractFormXmlData>();

		for (Entry<ContractFormXmlDataType, ContractFormXmlData> x : contractForm
				.getTypedContractFormXmlDatas().entrySet()) {
			contractFormXmlDatas.put(x.getKey().toString(), x.getValue());
		}

		logger.debug("checking who is editing...");
		MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
				ContractForm.class, contractForm.getId());

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
				ContractForm.class, contractForm.getId(), u)) {
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

		modelMap.put("isLocked", isLocked);
		modelMap.put("isLockedUserString", isLockedUserString);
		modelMap.put("isLockedUserId", isLockedUserId);
		modelMap.put("contractForm", contractForm);
		modelMap.put("committee", committee);
		modelMap.put("contractFormXmlDatas", contractFormXmlDatas);
		modelMap.put("user", u);
		modelMap.put("documentsXml",documentsXML);
		modelMap.put("objectType", "contract");
		modelMap.put(
				"formXmlData",
				contractForm
						.getTypedContractFormXmlDatas()
						.get(contractForm.getContractFormType()
								.getDefaultContractFormXmlDataType())
						.getXmlData());
		modelMap.put("objectMetaDataXml", contractForm.getContract()
				.getMetaDataXml());
		modelMap.put("form", contractForm);
		modelMap.put("formType", contractForm.getContractFormType());
		modelMap.put("objectFormMetaDataXml", contractForm.getMetaDataXml());
		modelMap.put(
				"formXmlDataId",
				contractForm
						.getTypedContractFormXmlDatas()
						.get(contractForm.getContractFormType()
								.getDefaultContractFormXmlDataType()).getId());

		if (contractForm.getContractFormType() != null) {
			modelMap.put(
					"formUrlName",
					(contractForm.getContractFormType().getUrlEncoded() != null && !contractForm
							.getContractFormType().getUrlEncoded().isEmpty()) ? contractForm
							.getContractFormType().getUrlEncoded() : "0");
			modelMap.put(
					"formType",
					(contractForm.getContractFormType().getDescription() != null && !contractForm
							.getContractFormType().getDescription().isEmpty()) ? contractForm
							.getContractFormType().getDescription() : "0");
			modelMap.put(
					"formBasetag",
					(contractForm.getContractFormType().getBaseTag() != null && !contractForm
							.getContractFormType().getBaseTag().isEmpty()) ? contractForm
							.getContractFormType().getBaseTag() : "0");
		} else {
			modelMap.put("formUrlName", "0");
			modelMap.put("formType", "0");
			modelMap.put("formBasetag", "0");
		}

		return "form/summary";
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/{contractFormUrlName}/revise", method = RequestMethod.GET)
	public String reviseForm(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("committee") Committee committee,
			@PathVariable("contractFormUrlName") String contractFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {

		ContractForm contractForm = contractFormDao.findById(contractFormId);

		ContractFormStatus contractFormStatus = contractFormStatusDao
				.getLatestContractFormStatusByFormId(contractFormId);

		ContractForm nv = contractForm;

		// only PI can create revision
		if (ContractFormStatusEnum.REVISION_REQUESTED.equals(contractFormStatus
				.getContractFormStatus())
				&& committee.equals(Committee.PI)) {
			nv = contractFormService.createRevision(contractForm);
		}

		// ContractFormXmlData contractFormXmlData =
		// nv.getTypedContractFormXmlDatas().get(nv.getContractFormType().getDefaultContractFormXmlDataType());

		if (nv != contractForm) {
			User currentUser = userService.getCurrentUser();

			// String isPIOrNotPI =
			// contractFormService.isCurrentUserPIOrNot(contractFormXmlData,
			// currentUser)?"IS_PI":"IS_NOT_PI";

			// try{
			// String updateSignedByXml =
			// xmlProcessor.replaceOrAddNodeValueByPath("/contract/signed-by",
			// contractFormXmlData.getXmlData(), isPIOrNotPI);
			// contractFormXmlData.setXmlData(updateSignedByXml);
			// contractFormXmlDataDao.saveOrUpdate(contractFormXmlData);
			//
			// } catch(Exception e){
			// e.printStackTrace();
			// }
			//
			// contractFormService.triggerPIAction("REVISE", isPIOrNotPI, nv,
			// currentUser, null);
			contractFormService
					.triggerPIAction("REVISE", nv, currentUser, null);

		}
		// modelMap.put("contractForm", nv);
		modelMap.put("committee", committee);
		// modelMap.put("user",
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "redirect:/contracts/" + contractId + "/contract-forms/"
				+ nv.getId() + "/review";

	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/close", method = RequestMethod.GET)
	public String closeContractForm(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId) {
		User currentUser = userService.getCurrentUser();

		unlockContractForm(contractFormId, currentUser);

		return "redirect:/contracts/" + contractId + "/dashboard";
	}

	private void unlockContractForm(long contractFormId, User currentUser) {
		try {
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
					ContractForm.class, contractFormId);

			if (mutexLock != null) {
				if (mutexLock.getUser().getId() == currentUser.getId()) {
					mutexLockService.unlockMutexLock(mutexLock);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@PostConstruct
	public void init() throws Exception {
		// pageSet =
		// pageTemplateService.getPageSetById("contract/newsubmission");
	}

	public ContractService getContractService() {
		return contractService;
	}

	@Autowired(required = true)
	public void setContractService(ContractService contractService) {
		this.contractService = contractService;
	}

	public ContractFormService getContractFormService() {
		return contractFormService;
	}

	@Autowired(required = true)
	public void setContractFormService(ContractFormService contractFormService) {
		this.contractFormService = contractFormService;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(
			ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	@Autowired(required = true)
	public void setAuthenticationManager(
			AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractMetaDataXmlService getContractMetaDataXmlService() {
		return contractMetaDataXmlService;
	}

	@Autowired(required = true)
	public void setContractMetaDataXmlService(
			ContractMetaDataXmlService contractMetaDataXmlService) {
		this.contractMetaDataXmlService = contractMetaDataXmlService;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(
			ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public FormService getFormService() {
		return formService;
	}

	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}

	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserAuthenticationService getUserAuthenticationService() {
		return userAuthenticationService;
	}
	
	@Autowired(required = true)
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService) {
		this.userAuthenticationService = userAuthenticationService;
	}
	
	public DocumentListToXmlService getDocumentListToXmlService() {
		return documentListToXmlServiceImpl;
	}

	@Autowired(required = true)
	public void setDocumentListToXmlServiceImpl(
			DocumentListToXmlServiceImpl documentListToXmlServiceImpl) {
		this.documentListToXmlServiceImpl = documentListToXmlServiceImpl;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}
}
