package edu.uams.clara.webapp.protocol.web.protocolform;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.contract.service.ContractService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.protocol.service.ProtocolMetaDataXmlService;
import edu.uams.clara.webapp.protocol.service.ProtocolService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicServiceContainer;
import edu.uams.clara.webapp.xml.processor.DocumentListToXmlService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;
import edu.uams.clara.webapp.xml.processor.impl.*;

@Controller
public class ProtocolFormController {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormController.class);

	private ProtocolService protocolService;
	private ProtocolFormService protocolFormService;
	private ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer;
	private ProtocolMetaDataXmlService protocolMetaDataXmlService;

	private ContractService contractService;

	private XmlProcessor xmlProcessor;

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private UserDao userDao;
	
	private AuthenticationManager authenticationManager;
	
	private UserAuthenticationService userAuthenticationService;

	private UserService userService;
	private MutexLockService mutexLockService;
	private FormService formService;

	private ProtocolFormXmlDifferServiceImpl protocolFormXmlDifferServiceImpl;
	private DocumentListToXmlService documentListToXmlServiceImpl;
	
	private ProtocolTrackService protocolTrackService;

	private static final int timeOutPeriod = 45;

	public ProtocolFormController() {

	}

	@RequestMapping(value = "/protocols/protocol-forms/{protocolFormUrlName}/create", method = RequestMethod.GET)
	public String startNewProtocol(
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {
		// final ProtocolFormXmlData protocolFormXmlData =
		// protocolService.creatNewProtocol(ProtocolFormType.valueOf(protocolFormUrlName.replace("-",
		// "_").toUpperCase()));
		final ProtocolFormXmlData protocolFormXmlData = protocolService
				.creatNewProtocol(ProtocolFormType
						.getProtocolFormTypeByUrlCode(protocolFormUrlName));

		Assert.notNull(protocolFormXmlData);
		Assert.notNull(protocolFormXmlData.getProtocolForm());
		Assert.notNull(protocolFormXmlData.getProtocolForm().getProtocol());

		return "redirect:/protocols/"
				+ protocolFormXmlData.getProtocolForm().getProtocol().getId()
				+ "/protocol-forms/"
				+ protocolFormXmlData.getProtocolForm().getId() + "/"
				+ protocolFormUrlName + "/protocol-form-xml-datas/"
				+ protocolFormXmlData.getId() + "/first-page";
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormUrlName}/create", method = RequestMethod.GET)
	public String startNewForm(
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			@PathVariable("protocolId") long protocolId, ModelMap modelMap)
			throws XPathExpressionException, IOException, SAXException {
		// final ProtocolFormXmlData protocolFormXmlData =
		// protocolFormService.createNewForm(ProtocolFormType.valueOf(protocolFormUrlName.replace("-",
		// "_").toUpperCase()), protocolId);
		final ProtocolFormXmlData protocolFormXmlData = protocolFormService
				.createNewForm(ProtocolFormType
						.getProtocolFormTypeByUrlCode(protocolFormUrlName),
						protocolId);

		Assert.notNull(protocolFormXmlData);
		Assert.notNull(protocolFormXmlData.getProtocolForm());
		Assert.notNull(protocolFormXmlData.getProtocolForm().getProtocol());

		String pagename = "first-page";
		if (protocolFormUrlName.endsWith("modification")) {
			pagename = "modification";
		}

		return "redirect:/protocols/"
				+ protocolFormXmlData.getProtocolForm().getProtocol().getId()
				+ "/protocol-forms/"
				+ protocolFormXmlData.getProtocolForm().getId() + "/"
				+ protocolFormUrlName + "/protocol-form-xml-datas/"
				+ protocolFormXmlData.getId() + "/" + pagename;
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/protocol-form-xml-datas/{formXmlDataId}/", method = RequestMethod.GET)
	public String viewForm(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("formXmlDataId") long formXmlDataId,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) {

		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		String pagename = "first-page";
		if (protocolFormUrlName.endsWith("modification")) {
			pagename = "modification";
		}

		return "redirect:/protocols/" + protocolId + "/protocol-forms/"
				+ protocolFormId + "/" + protocolFormUrlName
				+ "/protocol-form-xml-datas/" + formXmlDataId + "/" + pagename
				+ "?noheader=" + noheader;
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/protocol-form-xml-datas/{formXmlDataId}/{page}", method = RequestMethod.GET)
	public String getPageByViewName(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@RequestParam(value = "committee", required = false) Committee committee,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("formXmlDataId") long formXmlDataId,
			@PathVariable("page") String page,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap,
			RedirectAttributes redirectAttributes) {

		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao
				.findById(formXmlDataId);

		
		modelMap.put("protocolFormXmlData", protocolXmlData);
		modelMap.put("committee", committee);
		modelMap.put("protocolId", protocolXmlData.getProtocolForm()
				.getProtocol().getId());
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		
		if (page.equals("epic")){
			String epicDescription = protocolService.populateEpicDesc(protocolXmlData.getXmlData());

			modelMap.put("epicDescription", epicDescription);
		}

		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		modelMap.put("noheader", noheader);

		if (noheader) {
			page += "-noheader";
		}

		return "protocol/protocolform/" + protocolFormUrlName.replace("-", "")
				+ "/" + page;
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/protocol-form-xml-datas/{formXmlDataId}/review", method = RequestMethod.GET)
	public String getReviewPage(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@RequestParam(value = "committee", required = false) Committee committee,
			@RequestParam(value = "authenticated", required = false) Boolean authenticated,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("formXmlDataId") long formXmlDataId,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) {

		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao
				.findById(formXmlDataId);
		
		ProtocolForm protocolForm = protocolXmlData.getProtocolForm();

		User currentUser = userService.getCurrentUser();

		String workflow = protocolFormService
				.workFlowDetermination(protocolXmlData);
		logger.debug("workflow: " + workflow);
		String isSpecficRole = protocolFormService.finalSignOffDetermination(protocolForm, currentUser);
		logger.debug("is pi or not: " + isSpecficRole);
		try {
			if (!isSpecficRole.isEmpty()) {
				String updateXml = xmlProcessor.replaceOrAddNodeValueByPath("/"
						+ protocolForm.getProtocolFormType().getBaseTag()
						+ "/signed-by", protocolXmlData.getXmlData(),
						isSpecficRole);
				updateXml = xmlProcessor.replaceOrAddNodeValueByPath("/"
						+ protocolForm.getProtocolFormType().getBaseTag()
						+ "/workflow", updateXml, workflow);
				protocolXmlData.setXmlData(updateXml);
				protocolFormXmlDataDao.saveOrUpdate(protocolXmlData);
			}
		} catch (Exception e) {
			logger.error("failed to process pi roles; protocolFormXmlDataId: " + formXmlDataId , e);
		}

		modelMap.put("protocolFormXmlData", protocolXmlData);
		modelMap.put("committee", committee);
		modelMap.put("protocolId", protocolXmlData.getProtocolForm()
				.getProtocol().getId());
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		
		String page = "review";
		
		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		modelMap.put("noheader", noheader);

		if (noheader) {
			page += "-noheader";
		}
		
		if(authenticated != null){
			logger.debug("authenticated: " + authenticated);
			modelMap.put("authenticated", authenticated);
		}	

		return "protocol/protocolform/" + protocolFormUrlName.replace("-", "")
				+ "/" + page;
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/protocol-form-xml-datas/{protocolFormXmlDataId}/sign", method = RequestMethod.POST)
	// @TriggersRemove(cacheName = "protocolCache", removeAll = true)
	public String signForm(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {
		String baseUrl = "/protocols/" + protocolId + "/protocol-forms/"
				+ protocolFormId + "/" + protocolFormUrlName
				+ "/protocol-form-xml-datas/" + protocolFormXmlDataId;
		
		User currentUser = userService.getCurrentUser();

		if (noheader == null) {
			noheader = Boolean.FALSE;
		}

		boolean authenticated = false;

		try {
			authenticated = userAuthenticationService.isAuthenticated(username, password);

			ProtocolForm protocolForm = protocolFormDao
					.findById(protocolFormId);
			
			ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao
					.findById(protocolFormXmlDataId);

			if (authenticated) {
				//@ticket: 2824
//				if (protocolFormUrlName.equals("new-submission")) {
//					
//					if (protocolFormReviewLogicServiceContainer
//							.getProtocolFormReviewLogicService(
//									protocolForm.getFormType().toString())
//							.isInvolvedByType(protocolForm, "Contract")) {
//						Protocol p = protocolDao.findById(protocolId);
//
//						Contract relatedContract = contractService.createNewFormFromProtocol(p);
//
//						String protocolFormMetaDataString = protocolForm.getMetaDataXml();
//
//						protocolFormMetaDataString = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/related-contract", protocolFormMetaDataString, String.valueOf(relatedContract.getId()));
//
//						protocolForm.setMetaDataXml(protocolFormMetaDataString);
//
//						protocolForm = protocolFormDao.saveOrUpdate(protocolForm);
//					}
//					// protocolFormReviewLogicService.addExtraContentToProtocolFormXmlData(protocolForm);
//				}

				protocolMetaDataXmlService.consolidateProtocolFormXmlData(protocolXmlData,
						protocolXmlData
								.getProtocolFormXmlDataType());

				String isSpecficRole = "";
				String workflow = "";
				try {
					List<String> specificRoleLst = xmlProcessor
							.listElementStringValuesByPath("/"
									+ protocolForm.getProtocolFormType()
											.getBaseTag() + "/signed-by",
									protocolXmlData.getXmlData());
					isSpecficRole = (specificRoleLst != null && !specificRoleLst
							.isEmpty()) ? specificRoleLst.get(0) : "";

					List<String> workflowLst = xmlProcessor
							.listElementStringValuesByPath("/"
									+ protocolForm.getProtocolFormType()
											.getBaseTag() + "/workflow",
									protocolXmlData.getXmlData());
					workflow = (workflowLst != null && !workflowLst.isEmpty()) ? workflowLst
							.get(0) : "";
				} catch (Exception e) {
					e.printStackTrace();
				}

				workflow = (isSpecficRole.contains("NOT")) ? "" : workflow;

				protocolFormService.triggerPIAction("SIGN_SUBMIT",
						isSpecficRole, workflow, protocolForm, currentUser,
						null);

				unlockProtocolForm(protocolFormId, currentUser);
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

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/protocol-form-xml-datas/{protocolFormXmlDataId}/signed", method = RequestMethod.GET)
	public String getSignedPage(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormXmlDataId") long protocolFormXmlDataId,
			@RequestParam("authenticated") boolean authenticated,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {

		ProtocolFormXmlData protocolXmlData = protocolFormXmlDataDao
				.findById(protocolFormXmlDataId);

		modelMap.put("protocolFormXmlData", protocolXmlData);
		modelMap.put("protocolId", protocolXmlData.getProtocolForm()
				.getProtocol().getId());
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());
		modelMap.put("authenticated", authenticated);


		return "protocol/protocolform/" + protocolFormUrlName.replace("-", "")
				+ "/signed";

	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/summary", method = RequestMethod.GET)
	public String getSummaryPage(
			@RequestParam(value = "committee", required = false) Committee committee,
			@RequestParam(value = "compareto", required = false) Long comparetoProtocolFormId,
			@RequestParam(value = "historyid", required = false) String historyId,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) {

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		
		logger.debug("getting docs for pfid "+protocolFormId);
		List<ProtocolFormXmlDataDocumentWrapper> documents = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormId(protocolFormId);
		logger.debug("# for docs: "+documents.size());
		String documentsXML = documentListToXmlServiceImpl.transformProtocolFormXmlDataDocumentListToXML(documents);
		logger.debug(documentsXML);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Map<String, ProtocolFormXmlData> protocolFormXmlDatas = new HashMap<String, ProtocolFormXmlData>();

		for (Entry<ProtocolFormXmlDataType, ProtocolFormXmlData> x : protocolForm
				.getTypedProtocolFormXmlDatas().entrySet()) {

			ProtocolFormXmlData currentXmlData = x.getValue();

			if (comparetoProtocolFormId != null
					&& x.getKey().equals(
							protocolForm.getProtocolFormType()
									.getDefaultProtocolFormXmlDataType())) {
				ProtocolFormXmlData oldXmlData = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolFormIdAndType(
								comparetoProtocolFormId, protocolFormDao
										.findById(comparetoProtocolFormId)
										.getProtocolFormType()
										.getDefaultProtocolFormXmlDataType());

				String result = currentXmlData.getXmlData();
				// differ xmldata dstring
				try {
					result = protocolFormXmlDifferServiceImpl
							.differProtocolFormXml(protocolForm
									.getProtocolFormType().getBaseTag(),
									oldXmlData.getXmlData(), currentXmlData
											.getXmlData());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				modelMap.put("compareFormA", protocolFormDao
						.findById(comparetoProtocolFormId));
				modelMap.put("compareFormB", protocolForm);
				
				
				currentXmlData.setXmlData(result);
			}
			protocolFormXmlDatas.put(x.getKey().toString(), currentXmlData);
		}

		logger.debug("checking who is editing...");
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
				ProtocolForm.class, protocolForm.getId(), u)) {
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
		
		String formXmlData = protocolForm
				.getTypedProtocolFormXmlDatas()
				.get(protocolForm.getProtocolFormType()
						.getDefaultProtocolFormXmlDataType())
				.getXmlData();
		
		formXmlData = formService.addExtraStaffInformation(protocolForm.getProtocolFormType().getBaseTag(), formXmlData);

		modelMap.put("isLocked", isLocked);
		modelMap.put("isLockedUserString", isLockedUserString);
		modelMap.put("isLockedUserId", isLockedUserId);
		// modelMap.put("protocolForm", protocolForm);
		modelMap.put("committee", committee);
		// modelMap.put("protocolFormXmlDatas", protocolFormXmlDatas);
		modelMap.put("user", u);
		
		modelMap.put("documentsXml",documentsXML);
		
		if (historyId != null && !historyId.isEmpty()){
			String historyXml = protocolTrackService.getSingleLog("PROTOCOL", protocolId, historyId);
			
			modelMap.put("historyXml", historyXml);
		}

		modelMap.put("objectType", "protocol");
		modelMap.put("objectMetaDataXml", protocolForm.getProtocol()
				.getMetaDataXml());
		modelMap.put("objectFormMetaDataXml", protocolForm.getMetaDataXml());
		modelMap.put("form", protocolForm);
		modelMap.put("formType", protocolForm.getProtocolFormType());
		modelMap.put(
				"formXmlData",
				formXmlData);
		modelMap.put(
				"formXmlDataId",
				protocolForm
						.getTypedProtocolFormXmlDatas()
						.get(protocolForm.getProtocolFormType()
								.getDefaultProtocolFormXmlDataType()).getId());

		if (protocolForm.getProtocolFormType() != null) {
			modelMap.put(
					"formUrlName",
					(protocolForm.getProtocolFormType().getUrlEncoded() != null && !protocolForm
							.getProtocolFormType().getUrlEncoded().isEmpty()) ? protocolForm
							.getProtocolFormType().getUrlEncoded() : "0");
			modelMap.put(
					"formType",
					(protocolForm.getProtocolFormType().getDescription() != null && !protocolForm
							.getProtocolFormType().getDescription().isEmpty()) ? protocolForm
							.getProtocolFormType().getDescription() : "0");
			modelMap.put(
					"formBasetag",
					(protocolForm.getProtocolFormType().getBaseTag() != null && !protocolForm
							.getProtocolFormType().getBaseTag().isEmpty()) ? protocolForm
							.getProtocolFormType().getBaseTag() : "0");
		} else {
			modelMap.put("formUrlName", "0");
			modelMap.put("formType", "0");
			modelMap.put("formBasetag", "0");
		}

		return "form/summary";
	}
	
	private List<ProtocolFormStatusEnum> toCreateRevisionFormList = Lists.newArrayList();{
		toCreateRevisionFormList.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		toCreateRevisionFormList.add(ProtocolFormStatusEnum.RETURN_FOR_BUDGET_NEGOTIATIONS);
		toCreateRevisionFormList.add(ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES);
		toCreateRevisionFormList.add(ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MINOR_CONTINGENCIES);
		toCreateRevisionFormList.add(ProtocolFormStatusEnum.IRB_TABLED);
	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/{protocolFormUrlName}/revise", method = RequestMethod.GET)
	public String reviseForm(
			@RequestParam(value = "noheader", required = false) Boolean noheader,
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee,
			@PathVariable("protocolFormUrlName") String protocolFormUrlName,
			ModelMap modelMap) throws XPathExpressionException, IOException,
			SAXException {

		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);

		ProtocolFormStatus protocolFormStatus = protocolFormStatusDao
				.getLatestProtocolFormStatusByFormId(protocolFormId);

		ProtocolForm nv = protocolForm;

		// only PI can createa a revision when it's revision requested.
		if (toCreateRevisionFormList.contains(protocolFormStatus.getProtocolFormStatus())
				&& Committee.PI.equals(committee)) {
			nv = protocolFormService.createRevision(protocolForm);
		}

		// ProtocolFormXmlData protocolFormXmlData =
		// nv.getTypedProtocolFormXmlDatas().get(nv.getProtocolFormType().getDefaultProtocolFormXmlDataType());

		if (nv != protocolForm) {
			User currentUser = userService.getCurrentUser();

			// String isPIOrNotPI =
			// protocolFormService.isCurrentUserPIOrNot(protocolFormXmlData,
			// currentUser)?"IS_PI":"IS_NOT_PI";

			// try{
			// String updateSignedByXml =
			// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/signed-by",
			// protocolFormXmlData.getXmlData(), isPIOrNotPI);
			// protocolFormXmlData.setXmlData(updateSignedByXml);
			// protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
			//
			// } catch(Exception e){
			// e.printStackTrace();
			// }
			//
			// protocolFormService.triggerPIAction("REVISE", isPIOrNotPI, nv,
			// currentUser, null);

			protocolFormService
					.triggerPIAction("REVISE", nv, currentUser, null);

		} else {
			nv = protocolFormDao.getLatestProtocolFormByProtocolFormId(protocolFormId);
		}
		// modelMap.put("protocolForm", nv);
		modelMap.put("committee", committee);
		// modelMap.put("user",
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "redirect:/protocols/" + protocolId + "/protocol-forms/"
				+ nv.getId() + "/review";

	}

	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/close", method = RequestMethod.GET)
	public String closeProtocolForm(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId) {
		User currentUser = userService.getCurrentUser();

		unlockProtocolForm(protocolFormId, currentUser);

		return "redirect:/protocols/" + protocolId + "/dashboard";
	}

	private void unlockProtocolForm(long protocolFormId, User currentUser) {
		try {
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
					ProtocolForm.class, protocolFormId);

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
		// pageTemplateService.getPageSetById("protocol/newsubmission");
	}

	public ProtocolService getProtocolService() {
		return protocolService;
	}

	@Autowired(required = true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}

	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}

	@Autowired(required = true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
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

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolMetaDataXmlService getProtocolMetaDataXmlService() {
		return protocolMetaDataXmlService;
	}

	@Autowired(required = true)
	public void setProtocolMetaDataXmlService(
			ProtocolMetaDataXmlService protocolMetaDataXmlService) {
		this.protocolMetaDataXmlService = protocolMetaDataXmlService;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ContractService getContractService() {
		return contractService;
	}

	@Autowired(required = true)
	public void setContractService(ContractService contractService) {
		this.contractService = contractService;
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

	public ProtocolFormReviewLogicServiceContainer getProtocolFormReviewLogicServiceContainer() {
		return protocolFormReviewLogicServiceContainer;
	}

	@Autowired(required = true)
	public void setProtocolFormReviewLogicServiceContainer(
			ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer) {
		this.protocolFormReviewLogicServiceContainer = protocolFormReviewLogicServiceContainer;
	}

	public ProtocolFormXmlDifferServiceImpl getProtocolFormXmlDifferServiceImpl() {
		return protocolFormXmlDifferServiceImpl;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDifferServiceImpl(
			ProtocolFormXmlDifferServiceImpl protocolFormXmlDifferServiceImpl) {
		this.protocolFormXmlDifferServiceImpl = protocolFormXmlDifferServiceImpl;
	}
	
	
	public DocumentListToXmlService getDocumentListToXmlService() {
		return documentListToXmlServiceImpl;
	}

	@Autowired(required = true)
	public void setDocumentListToXmlServiceImpl(
			DocumentListToXmlServiceImpl documentListToXmlServiceImpl) {
		this.documentListToXmlServiceImpl = documentListToXmlServiceImpl;
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

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required = true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}
}
