package edu.uams.clara.webapp.protocol.web.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.xpath.XPathExpressionException;

import org.jasig.cas.client.validation.TicketValidationException;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.integration.outgoing.epic.StudyDefinitionWSClient;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.service.UserAuthenticationService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.relation.RelationService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.common.util.JsonResponseHelper;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractStatusDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormDetailContentService;
import edu.uams.clara.webapp.protocol.service.ProtocolService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.webservice.cas.service.CASService;
import edu.uams.clara.webapp.webservice.psc.PSCServiceBroker;
import edu.uams.clara.webapp.xml.processor.BudgetXmlTransformService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ProtocolDashboardAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolDashboardAjaxController.class);

	private ProtocolDao protocolDao;

	private ProtocolFormDao protocolFormDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private ContractStatusDao contractStatusDao;

	private UserDao userDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private AgendaItemDao agendaItemDao;

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

	private MutexLockService mutexLockService;

	private XmlProcessor xmlProcessor;

	private PSCServiceBroker pscServiceBroker;

	private CASService casService;

	private BudgetXmlTransformService budgetXmlTransformService;

	private ProtocolFormDetailContentService protocolFormDetailContentService;

	private AuditService auditService;

	private ObjectAclService objectAclService;

	private FormService formService;
	
	private RelationService relationService;

	private ProtocolTrackService protocolTrackService;
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private ProtocolEmailService protocolEmailService;
	
	private EmailService emailService;
	
	private UserAuthenticationService userAuthenticationService;
	
	private ProtocolService protocolService;

	private static final int timeOutPeriod = 45;
	
	@Value("${fileserver.local.dir.path}")
	private String uploadDirResourcePath;
	
	@Value("${fileserver.url}")
	private String fileServer;
	
	@Value("${fileserver.remote.dir.path}")
	private String fileRemoteDirPath;
	
	private boolean isRelatedContractSubmitted(ProtocolForm protocolForm) {
		if (!protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION)) {
			return true;
		}
		
		boolean isRelatedContractSubmitted = true;
		
		/*
		String protocolFormMetaData = protocolForm.getMetaDataXml();
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			String requireContractSubmitBeforeIRB = xmlHandler.getSingleStringValueByXPath(protocolFormMetaData, "/protocol/contract/require-contract-before-irb");
			
			if (requireContractSubmitBeforeIRB.equals("y")) {
				String relatedContractIdStr = xmlHandler.getSingleStringValueByXPath(protocolFormMetaData, "/protocol/related-contract");
				
				if (!relatedContractIdStr.isEmpty()) {
					boolean isSubmitted = contractStatusDao.checkContractStatusByContractIdAndStatus(Long.valueOf(relatedContractIdStr), ContractStatusEnum.UNDER_CONTRACT_MANAGER_REVIEW);
					
					if (isSubmitted)
						isRelatedContractSubmitted = true;
				}
			} else {
				isRelatedContractSubmitted = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		List<RelatedObject> relatedContractList = Lists.newArrayList();
		
		List<Long> relatedContractIdList = Lists.newArrayList();
		
		try {
			relatedContractList = relationService.getRelationsByIdAndType(protocolForm.getProtocol().getId(), "protocol");
			
			if (relatedContractList != null && relatedContractList.size() > 0) {
				for (RelatedObject ro : relatedContractList) {
					if (ro.getObjectType().equals("contract")) {
						relatedContractIdList.add(ro.getObjectId());
					} else if (ro.getRelatedObjectType().equals("contract")) {
						relatedContractIdList.add(ro.getRelatedObjectId());
					}
						
				}
				
				int notSubmittedContractCount = 0;
				
				for (Long relatedContractId : relatedContractIdList) {
					isRelatedContractSubmitted = contractStatusDao.checkIfContractIsSubmitted(relatedContractId);

					if (!isRelatedContractSubmitted) notSubmittedContractCount += 1;
				}
				
				if (notSubmittedContractCount > 0) {
					isRelatedContractSubmitted = false;
				}
			} else {
				isRelatedContractSubmitted = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			isRelatedContractSubmitted = true;
		}
		
		return isRelatedContractSubmitted;
	}
	
	private List<ProtocolFormStatusEnum> canEditProtocolFormStatusLst = new ArrayList<ProtocolFormStatusEnum>();{
		canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.DRAFT);
		//canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.REVISED);
		canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION);
		canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
		//canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		canEditProtocolFormStatusLst.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
	}
	
	private List<ProtocolFormStatusEnum> canEditBudgetStatusLst = new ArrayList<ProtocolFormStatusEnum>();{
		canEditBudgetStatusLst.add(ProtocolFormStatusEnum.DRAFT);
		canEditBudgetStatusLst.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		canEditBudgetStatusLst.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
	}

	private String getActionXml(long protocolId, ProtocolForm protocolForm,
			ProtocolFormStatus protocolFormStatus,
			ProtocolFormXmlData lastProtocolFormXmlData, User u,
			String formTypeMachineReadable, String xmlResult, boolean hasEditPermission) {
		long protocolFormId = protocolForm.getId();
		
		switch (protocolFormStatus.getProtocolFormStatus()) {
		case REVISION_PENDING_PI_ENDORSEMENT:
		case UNDER_REVISION:
		case UNDER_REVISION_MAJOR_CONTINGENCIES:
		case UNDER_REVISION_MINOR_CONTINGENCIES:
		case REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT:
		case REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT:
		case UNDER_REVISION_RESPONSE_TO_TABLED:
		case RESPONSE_TO_TABLED_PENDING_PI_ENDORSEMENT:
			if (hasEditPermission) {
				xmlResult += "<action cls='green'><name>Continue Revision</name><url target='_blank'>/protocols/"
						+ protocolId
						+ "/protocol-forms/"
						+ protocolFormId
						+ "/"
						+ formTypeMachineReadable
						+ "/revise?committee=PI</url></action>";
			}
			break;
		case DRAFT:
		case PENDING_PI_ENDORSEMENT:
		case PENDING_TP_ENDORSEMENT:
		//case PENDING_PL_ENDORSEMENT:
			if (hasEditPermission || u.getAuthorities().contains(Permission.ROLE_SYSTEM_ADMIN) || u.getAuthorities().contains(Permission.ROLE_BUDGET_REVIEWER)) {
				xmlResult += "<action cls='green'><name>Edit "+ protocolForm.getProtocolFormType().getDescription() +"</name><url>/protocols/"
						+ protocolId
						+ "/protocol-forms/"
						+ protocolFormId
						+ "/"
						+ formTypeMachineReadable
						+ "/protocol-form-xml-datas/"
						+ lastProtocolFormXmlData.getId() + "/</url></action>";
			}
			
			if (hasEditPermission || u.getAuthorities().contains(Permission.ROLE_SYSTEM_ADMIN) || u.getAuthorities().contains(Permission.DELETE_FORM_ANY_TIME)){
				xmlResult += "<action cls='red'><name>Delete "+ protocolForm.getProtocolFormType().getDescription() +"</name><url type='javascript'>Clara.Application.FormController.RemoveForm();</url></action>";
			}

			break;
		case REVISION_REQUESTED:
		case IRB_DEFERRED:
		case IRB_DEFERRED_WITH_MINOR_CONTINGENCIES:
		case IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES:
		case IRB_TABLED:
			if (hasEditPermission) {
				xmlResult += "<action cls='green'><name>Revise</name><url>/protocols/"
						+ protocolId
						+ "/protocol-forms/"
						+ protocolFormId
						+ "/"
						+ formTypeMachineReadable
						+ "/revise?committee=PI</url></action>";
			}
			break;
		case PENDING_PI_SIGN_OFF:
			//if the study with budget has related contract, need to wait till contract is submitted before it can be submitted to IRB
			if (isRelatedContractSubmitted(protocolForm)) {
				if (hasEditPermission) {
					xmlResult += "<action cls='green'><name>Sign Off</name><url>/protocols/"
							+ protocolId
							+ "/protocol-forms/"
							+ protocolFormId
							+ "/review?committee=PI</url></action>";
				}
			}else{
				xmlResult += "<action cls='red'><name>A contact has to be submitted and linked to this study before you can sign off. </name><url>/protocols/"
						+ protocolId
						+ "/dashboard</url></action>";
			}
			
			break;
		default:
			break;
		}

		return xmlResult;
	}
	
	/*
	private boolean canPushToEpic(long protocolId, User u) {
		boolean canPushToEpic = false;
		
		Protocol protocol = protocolDao.findById(protocolId);
		
		ProtocolStatus latestProtocolStatus = protocolStatusDao.findProtocolStatusByProtocolId(protocolId);
		
		String protocolMetaData = protocol.getMetaDataXml();
		
		boolean isUAMSStudy = protocolService.checkStudyCharacteristic(protocolMetaData).get("isUAMSStudy");
		
		boolean isPushedToEpic = protocolService.isPushedToEpic(protocolMetaData);
		
		if (u.getAuthorities().contains(Permission.PUSH_TO_EPIC) && latestProtocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.OPEN) && isUAMSStudy && !isPushedToEpic) {
			canPushToEpic = true;
		}
		
		return canPushToEpic;
	}
	*/

	/**
	 * return a list of forms associated with this protocol. the controller will
	 * decide "Action" cell, which defines what the user can do based on the
	 * user's role and rights.
	 * 
	 * @param protocolId
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	Source listProtocolForms(@PathVariable("protocolId") long protocolId) {
		/*
		 * User u =
		 * (User)SecurityContextHolder.getContext().getAuthentication().
		 * getPrincipal();
		 * 
		 * for(GrantedAuthority ga:u.getAuthorities()){
		 * logger.debug(ga.getAuthority()); }
		 */
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		boolean hasEditPermission = objectAclService.hasEditObjectAccess(Protocol.class,
				protocolId, u);

		List<ProtocolForm> protocolForms = protocolDao
				.listLatestProtocolFormsByProtocolId(protocolId);

		//logger.info("size: " + protocolForms.size());
		String xmlResult = "<list>";

		for (ProtocolForm protocolForm : protocolForms) {
			String formTypeMachineReadable = protocolForm.getProtocolFormType()
					.getUrlEncoded();

			ProtocolFormStatus pformStatus = protocolFormStatusDao
					.getLatestProtocolFormStatusByFormId(protocolForm.getId());

			//logger.info("GETTING PROTOCOL FORM STATUS FOR ID: " +protocolForm.getId()+" TYPE: "+protocolForm.getProtocolFormType()+" XMLDATATYPE: "+protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());
			/*
			ProtocolFormXmlData lastProtocolFormXmlData = protocolFormXmlDataDao
					.getLastProtocolFormXmlDataByProtocolFormIdAndType(
							protocolForm.getId(), protocolForm
									.getProtocolFormType()
									.getDefaultProtocolFormXmlDataType());*/
			
			ProtocolFormXmlData lastProtocolFormXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());

			long protocolFormId = protocolForm.getId();

			// ProtocolFormXmlData latestProtocolFormXmlData =
			// protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());

			xmlResult += "<protocol-form protocolFormId=\""
					+ protocolFormId + "\" lastVersionId=\""
					+ lastProtocolFormXmlData.getId() + "\">";
			xmlResult += "<protocol-form-type id=\"" + formTypeMachineReadable
					+ "\">"
					+ protocolForm.getProtocolFormType().getDescription()
					+ "</protocol-form-type>";
			xmlResult += "<url>/protocols/"
					+ protocolId + "/protocol-forms/"
					+ protocolFormId + "/dashboard</url>";
			// xmlResult += "<editurl>/protocols/" +
			// protocolForm.getProtocol().getId() + "/protocol-forms/" +
			// protocolForm.getId() +
			// "/"+formTypeMachineReadable+"/protocol-form-xml-datas/" +
			// lastProtocolFormXmlData.getId() + "/</editurl>";
			//logger.debug("checking who is editing...");
			// User editing =
			// mutexLockService.whoIsEditing(protocolForm.getProtocolFormType().getClass(),
			// protocolForm.getId());
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(
					ProtocolForm.class, protocolFormId);

			Date currentDate = new Date();
			Date lastModifiedDate = (mutexLock != null) ? mutexLock
					.getModified() : null;
			DateTime currentDateTime = new DateTime(currentDate);
			DateTime lastModifiedDateTime = (mutexLock != null) ? new DateTime(
					lastModifiedDate) : null;

			long editingUserId = (mutexLock != null) ? mutexLock.getUser()
					.getId() : 0;
			String lockedOrNot = "false";

			String ifEditingMessage = "";

			// if(mutexLock != null){
			// ifEditingMessage= mutexLock.getUser().getPerson().getFullname() +
			// " is editing...";
			// }

			xmlResult += "<actions>";

			xmlResult += "<action cls='white'><name>View Summary</name><url>/protocols/"
					+ protocolId
					+ "/protocol-forms/"
					+ protocolFormId
					+ "/"
					+ formTypeMachineReadable
					+ "/summary?noheader=true&amp;review=false</url></action>";
			
			/*
			if (u.getAuthorities().contains(Permission.CAN_UPLOAD_FINAL_LEGAL_APPROVAL_CONSENT)) {
				xmlResult += "<action cls='red'><name>Upload Final Legal Approval Consent</name><url type='javascript'>Clara.Application.FormController.ShowWarningMessage({message:'Only used for uploading Final Legal Approval consent!', url:'/protocols/"
						+ protocolId
						+ "/protocol-forms/"
						+ protocolFormId
						+ "/review?committee=CONTRACT_LEGAL_REVIEW&amp;docAction=UPLOAD_FINAL_LEGAL_APPROVAL_CONSENT'});</url></action>";
			}
			*/
			
			if (u.getAuthorities().contains(Permission.ROLE_PHARMACY_REVIEW)) {
				xmlResult += "<action cls='blue'><name>Upload Epic Pharmacy Information</name><url type='javascript'>Clara.Application.FormController.ShowWarningMessage({message:'Only used for uploading Epic Pharmacy Information.', url:'/protocols/"
						+ protocolId
						+ "/protocol-forms/"
						+ protocolFormId
						+ "/review?committee=PHARMACY_REVIEW&amp;docAction=UPLOAD_EPIC_PHARMACY_INFORMATION'});</url></action>";
			}
			
			String assignAgendaDate = "";
			
			try {
				Date agendaDate = agendaItemDao.getAgendaDateByProtocolFormId(protocolFormId);

				assignAgendaDate = DateFormatUtil.formateDateToMDY(agendaDate);
			} catch (Exception e) {
				logger.info("No assigned agenda!");
			}

			if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION) || protocolForm.getProtocolFormType().equals(ProtocolFormType.ARCHIVE)){
				ProtocolStatus protocolStatus = protocolDao.getLatestProtocolStatusByProtocolId(protocolId);
				
				if (protocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.OPEN)){
					if (hasEditPermission){
						/*
						xmlResult += "<action cls='blue'><name>Upload Packet</name><url>/protocols/"
								+ protocolId
								+ "/protocol-forms/"
								+ protocolFormId
								+ "/review?committee=PI&amp;docAction=UPLOAD_PACKET</url></action>";
						*/
						xmlResult += "<action cls='red'><name>Upload Packet</name><url type='javascript'>Clara.Application.FormController.ShowWarningMessage({message:'Documents uploaded as packets will not be reviewed by the IRB.', url:'/protocols/"
								+ protocolId
								+ "/protocol-forms/"
								+ protocolFormId
								+ "/review?committee=PI&amp;docAction=UPLOAD_PACKET'});</url></action>";
					}
					
				}
				
				if (!protocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.CLOSED)) {
					if (u.getAuthorities().contains(Permission.ROLE_IRB_OFFICE)) {
						xmlResult += "<action cls='red'><name>Administratively Close Study</name><url type='javascript'>Clara.Application.FormController.CloseStudy({protocolId:"
						+ protocolId + "});</url></action>";
						
						//xmlResult += "<action cls='red'><name>Administratively Hold</name><url type='javascript'>Clara.Application.FormController.HoldForm({formId:"
								//+ protocolFormId + "});</url></action>";
					}
				}
				
				if (protocolForm.getProtocolFormType().equals(ProtocolFormType.ARCHIVE)) {
					xmlResult += "<action cls='blue'><name>Update Committee Review Note</name><url type='javascript'>Clara.Application.FormController.ChooseReviewRole({formId:"
							+ protocolFormId + "});</url></action>";
				}
				
			}
			
			if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION) || protocolForm.getProtocolFormType().equals(ProtocolFormType.MODIFICATION)) {
				try {
					ProtocolFormXmlData budgetXmlData = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolFormIdAndType(
									protocolFormId,
									ProtocolFormXmlDataType.BUDGET);
					
					if (budgetXmlData != null) {
						if (u.getAuthorities().contains(Permission.VIEW_BUDGET) || objectAclService.isObjectAccessible(Protocol.class,
								protocolId, u)){
							xmlResult += "<action cls='white'><name>View Budget</name><url target='_blank'>/protocols/"
									+ protocolId
									+ "/protocol-forms/"
									+ protocolFormId
									+ "/budgets/budgetbuilder?readOnly=true</url></action>";
						}
						
						boolean piCanEditBudget = false;
						boolean budgetHasBeenApproved = false;
						
						boolean canEditBudget = false;
						boolean canDeleteBudget = false;
						
						XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
						
						String budgetApprovedDate = xmlHandler.getSingleStringValueByXPath(protocolForm.getObjectMetaData(), "/protocol/summary/budget-determination/approval-date");
						
						if (!budgetApprovedDate.isEmpty()) {
							budgetHasBeenApproved = true;
						} else {
							budgetHasBeenApproved = false;
						}
						
						if (hasEditPermission){
							
							if (!budgetHasBeenApproved){
								if (canEditProtocolFormStatusLst.contains(pformStatus.getProtocolFormStatus())){
									piCanEditBudget = true;
								} else {
									piCanEditBudget = false;
								}
							} else {
								if (canEditBudgetStatusLst.contains(pformStatus.getProtocolFormStatus())){
									piCanEditBudget = true;
								} else {
									piCanEditBudget = false;
								}
								
							}
						}
						
						//Redmine #3000: Let user with budget reviewer role be able to edit budget before IRB meeting
						if (piCanEditBudget) {
							canEditBudget = true;
						} else {
							if (u.getAuthorities().contains(Permission.ROLE_BUDGET_REVIEWER) || u.getAuthorities().contains(Permission.ROLE_COVERAGE_REVIEWER)) {
								if (assignAgendaDate == null || assignAgendaDate.isEmpty()) {
									canEditBudget = true; 
								} else {
									canEditBudget = false; 
								}
							}
						}
						
						if (canEditBudget) {
							xmlResult += "<action cls='white'><name>Edit Budget</name><url target='_blank'>/protocols/"
									+ protocolId
									+ "/protocol-forms/"
									+ protocolFormId
									+ "/budgets/budgetbuilder</url></action>";
						}
						
						/*
						if ((u.getAuthorities().contains(Permission.EDIT_BUDGET) || piCanEditBudget) && !budgetHasBeenApproved) {
							xmlResult += "<action cls='white'><name>Edit Budget</name><url target='_blank'>/protocols/"
									+ protocolId
									+ "/protocol-forms/"
									+ protocolFormId
									+ "/budgets/budgetbuilder</url></action>";
						}
						*/
						
						if (u.getAuthorities().contains(Permission.DELETE_BUDGET)) {
							if (!budgetHasBeenApproved) {
								canDeleteBudget = true;
							}
						} else if (piCanEditBudget) {
							canDeleteBudget = true;
						}
						
						if (canDeleteBudget) {
							xmlResult += "<action cls='red'><name>Delete Budget</name><url type='javascript'>Clara.Application.FormController.RemoveBudget();</url></action>";
						}
						
						/*
						if ((u.getAuthorities().contains(Permission.DELETE_BUDGET) || piCanEditBudget) && !budgetHasBeenApproved) {
							xmlResult += "<action cls='red'><name>Delete Budget</name><url type='javascript'>Clara.Application.FormController.RemoveBudget();</url></action>";
						}
						*/
						
						String protocolXml= protocolForm.getObjectMetaData();
						
						if (u.getAuthorities().contains(Permission.PUSH_TO_PSC)) {
							boolean pushed= protocolService.isPushedToPSC(protocolXml);
							boolean cancelledForm = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolFormId).getProtocolFormStatus().equals(ProtocolFormStatusEnum.CANCELLED);
							if (!pushed&&!cancelledForm) {
								xmlResult += "<action cls='rose'><name>Push to PSC</name><url response='json' type='ajax'>/ajax/protocols/"
										+ protocolId
										+ "/transform-budget-xml-to-psc</url></action>";
								
								xmlResult += "<action cls='rose'><name>Generate PSC Template</name><url>/ajax/protocols/"
										+ protocolId
										+ "/transform-budget-xml-to-psctem</url></action>";
							}
						}
					}
				} catch (Exception e) {
					//e.printStackTrace();
					
				}
			}

			try {
				if (u.getAuthorities().contains(Permission.ROLE_REVIEWER)) {
					List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatusLst = protocolFormCommitteeStatusDao
							.listLatestByProtocolFormId(protocolFormId);
					
					if (protocolFormCommitteeStatusLst != null
							&& !protocolFormCommitteeStatusLst.isEmpty()) {
						boolean canUpdateNote = true;
						
						boolean budgetCanReAssign = false;
						boolean regulatoryCanReAssign = false;
						boolean irbAssignerCanReAssign = false;

						//ProtocolFormCommitteeStatus latestCommitteeStatus = null;
						for (ProtocolFormCommitteeStatus pfcs : protocolFormCommitteeStatusLst) {
							switch (pfcs.getCommittee()) {
							case BUDGET_MANAGER:
								//latestCommitteeStatus = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.BUDGET_MANAGER, protocolFormId);
								
								if (!pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT)){
									if (u.getAuthorities().contains(
											Permission.ROLE_BUDGET_MANAGER)) {
										budgetCanReAssign = true;
										
									}
								} else {
									canUpdateNote = false;
								}

								break;
							case REGULATORY_MANAGER:
								//latestCommitteeStatus = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.REGULATORY_MANAGER, protocolFormId);

								if (!pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT)){
									if (u.getAuthorities().contains(
											Permission.ROLE_REGULATORY_MANAGER)) {
										regulatoryCanReAssign = true;
										
									}
								} else {
									canUpdateNote = false;
								}

								break;							
							case IRB_ASSIGNER:
								//latestCommitteeStatus = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.IRB_ASSIGNER, protocolFormId);
								
								if (!pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT)){
									if (u.getAuthorities().contains(
											Permission.ROLE_IRB_ASSIGNER)) {
										irbAssignerCanReAssign = true;
										
									}
								} else {
									canUpdateNote = false;
								}

								break;
							default:
								break;
							}
						}
						
						if (budgetCanReAssign) {
							xmlResult += "<action cls='blue'><name>Budget: Re-assign Reviewer</name><url type='javascript'>Clara.Application.QueueController.Reassign({roleId:'ROLE_BUDGET_MANAGER',formTypeId:\'"+ formTypeMachineReadable +"\',formId:"
									+ protocolFormId
									+ ",committee:'BUDGET_MANAGER'},{objectType:'Protocol'});</url></action>";
						}
						
						if (regulatoryCanReAssign) {
							xmlResult += "<action cls='blue'><name>Regulatory: Re-assign Reviewer</name><url type='javascript'>Clara.Application.QueueController.Reassign({roleId:'ROLE_REGULATORY_MANAGER',formTypeId:\'"+ formTypeMachineReadable +"\',formId:"
									+ protocolFormId
									+ ",committee:'REGULATORY_MANAGER'},{objectType:'Protocol'});</url></action>";
						}
						
						if (irbAssignerCanReAssign) {
							xmlResult += "<action cls='blue'><name>IRB: Re-assign Reviewer</name><url type='javascript'>Clara.Application.QueueController.Reassign({roleId:'ROLE_IRB_ASSIGNER',formTypeId:\'"+ formTypeMachineReadable +"\',formId:"
									+ protocolFormId
									+ ",committee:'IRB_ASSIGNER'},{objectType:'Protocol'});</url></action>";
						}
						
						if (canUpdateNote){
							xmlResult += "<action cls='blue'><name>Update Committee Review Note</name><url type='javascript'>Clara.Application.FormController.ChooseReviewRole({formId:"
									+ protocolFormId + "});</url></action>";
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (!mutexLockService.isLockedByObjectClassAndIdForCurrentUser(
					ProtocolForm.class, protocolFormId, u)) {
				xmlResult = getActionXml(protocolId, protocolForm, pformStatus,
						lastProtocolFormXmlData, u, formTypeMachineReadable,
						xmlResult, hasEditPermission);

			} else {
				if (mutexLock != null) {

					//logger.debug("time period after last access: "
							//+ Minutes.minutesBetween(lastModifiedDateTime,
									//currentDateTime).getMinutes());
					if (Minutes.minutesBetween(lastModifiedDateTime,
							currentDateTime).getMinutes() > timeOutPeriod) {
						xmlResult = getActionXml(protocolId, protocolForm, pformStatus,
								lastProtocolFormXmlData, u,
								formTypeMachineReadable, xmlResult, hasEditPermission);
					} else {
						lockedOrNot = "true";
						ifEditingMessage = mutexLock.getUser().getPerson()
								.getFullname()
								+ " is editing...";
					}
				}
			}

			/*
			 * Cancel form
			 * */
			if ((this.canEditProtocolFormStatusLst.contains(pformStatus.getProtocolFormStatus()) && hasEditPermission) || u.getAuthorities().contains(Permission.CANCEL_PROTOCOL_FORM)){
				if (!protocolForm.getProtocolFormType().equals(ProtocolFormType.ARCHIVE)) {
					xmlResult += "<action cls='red'><name>Cancel "+ protocolForm.getProtocolFormType().getDescription() +"</name><url type='javascript'>Clara.Application.FormController.CancelForm();</url></action>";
				}
				
			}

			/**
			 * Push to epic...
			 */
			/* study will be pushed to epic by system
			if(canPushToEpic(protocolId, u)){
				xmlResult += "<action cls='rose'><name>Push Study Definition to EPIC</name><url response='json' type='ajax'>/ajax/protocols/"
						+ protocolId + "/epic/push-study-definition</url></action>";				
			}
			*/

			xmlResult += "</actions>";

			xmlResult += "<status><description>"
					+ org.apache.commons.lang.StringEscapeUtils
							.escapeXml(pformStatus.getProtocolFormStatus()
									.getDescription())
					+ "</description><modified>" + pformStatus.getModified()
					+ "</modified><lock value=\"" + lockedOrNot
					+ "\" modified=\"" + lastModifiedDate + "\" userid=\""
					+ editingUserId + "\" message=\"" + ifEditingMessage
					+ "\" /><agenda><assigned-date>"+ assignAgendaDate +"</assigned-date></agenda></status>";

			xmlResult += formService.getAssignedReviewers(protocolForm);

			xmlResult += protocolFormDetailContentService
					.getDetailContent(protocolForm);

			xmlResult += "</protocol-form>";
		}

		xmlResult += "</list>";		
		
		return DomUtils.toSource(xmlResult);
	}
	
	private StudyDefinitionWSClient studyDefinitionWSClient;
	
	@RequestMapping(value="/ajax/protocols/{protocolId}/epic/push-study-definition", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody JsonResponse pushStudyDefinitionToEpic(@PathVariable("protocolId") long protocolId){
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		Protocol protocol = protocolDao.findById(protocolId);
		
		//String protocolMetaData = protocol.getMetaDataXml();
		
		try {
			protocolService.pushToEpic(protocol);
			
			return JsonResponseHelper.newSuccessResponseStube("Successfully pushed!");
		} catch (Exception e) {
			logger.error("failed: ", e);
			return JsonResponseHelper.newErrorResponseStub("failed: " + e.getMessage());
		}
		
		/*
		if(canPushToEpic(protocolId, u)) {
			try {
				XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
				String epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-title");
				String epicSummary = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-desc");
				
				if (epicTitle.isEmpty()) {
					epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/title");
				}
				
				if (epicSummary.isEmpty()) {
					epicSummary = protocolService.populateEpicDesc(protocolMetaData);
				}
				
				studyDefinitionWSClient.retrieveProtocolDefResponse("" + protocolId, epicTitle,epicSummary, protocolMetaData);
				
				protocolService.addPushedToEpic(protocol);

				return JsonResponseHelper.newSuccessResponseStube("Successfully pushed!");
			} catch (Exception e) {

				logger.error("failed: ", e);
				return JsonResponseHelper.newErrorResponseStub("failed: " + e.getMessage());
			}
		} else {
			return JsonResponseHelper.newErrorResponseStub("Don't have the right to push to EPIC!");
		}
		*/
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/get-user-role-list", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody
	Source getUserCommitteeList(
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("userId") long userId) {
		User currentUser = userDao.findById(userId);
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		
		String finalCommitteeXml = "<committees>";
		
		if (protocolForm.getProtocolFormType().equals(ProtocolFormType.ARCHIVE)) {
			Set<UserRole> userRoles = currentUser.getUserRoles();
			
			if (userRoles == null || userRoles.isEmpty()) {
				return XMLResponseHelper.newErrorResponseStub("You cannot update committee note!"); 
			}
			
			String committeeXml = "";
			
			try {
				for (UserRole ur : userRoles) {
					committeeXml += "<committee name=\"" + ur.getRole().getCommitee().toString()
							+ "\" desc=\"" + ur.getRole().getCommitee().getDescription() + "\" />";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			finalCommitteeXml += committeeXml;
		} else {
			try {
				List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatusLst = protocolFormCommitteeStatusDao
						.listLatestByProtocolFormId(protocolFormId);

				List<Committee> reviewCommitteeList = new ArrayList<Committee>();
				if (protocolFormCommitteeStatusLst != null
						&& !protocolFormCommitteeStatusLst.isEmpty()) {
					for (ProtocolFormCommitteeStatus pfcs : protocolFormCommitteeStatusLst) {
						if (!pfcs.getCommittee().equals(Committee.PI)) {
							reviewCommitteeList.add(pfcs.getCommittee());
						}
					}
				} else {
					return XMLResponseHelper.newErrorResponseStub("No committee has reviewed this form yet!");
				}

				String committeeXml = "";
				if (reviewCommitteeList != null && !reviewCommitteeList.isEmpty()) {
					for (Committee c : reviewCommitteeList) {
						if (currentUser.getAuthorities().contains(
								c.getRolePermissionIdentifier())) {
							committeeXml += "<committee name=\"" + c.toString()
									+ "\" desc=\"" + c.getDescription() + "\" />";
						}
					}
				} else {
					return XMLResponseHelper.newErrorResponseStub("");
				}

				finalCommitteeXml += committeeXml;

				
			} catch (Exception e) {
				//e.printStackTrace();
				logger.error("failed to load committee list", e);
				return XMLResponseHelper.newErrorResponseStub("Failed to load committee list!");
			}
		}
		finalCommitteeXml += "</committees>";
		
		return XMLResponseHelper.newDataResponseStub(finalCommitteeXml);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/metadata", method = RequestMethod.GET)
	public @ResponseBody
	String getProtocolMetaDataXml(@PathVariable("protocolId") long protocolId) {
		String metaDataXml = protocolDao.findById(protocolId).getMetaDataXml();
		return metaDataXml;
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/get-budget.xml", method = RequestMethod.GET)
	public @ResponseBody
	String getBudgetXmlByProtocolId(@PathVariable("protocolId") long protocolId) {

		// Protocol protocol = protocolDao.findById(protocolId);
		ProtocolForm newsubmissionForm = protocolDao
				.getLatestProtocolFormByProtocolIdAndProtocolFormType(
						protocolId, ProtocolFormType.NEW_SUBMISSION);

		ProtocolFormXmlData budgetXmlData = newsubmissionForm
				.getTypedProtocolFormXmlDatas().get(
						ProtocolFormXmlDataType.BUDGET);

		return budgetXmlData.getXmlData();
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/transform-budget-xml-to-psctem", method = RequestMethod.GET)
	public @ResponseBody
	byte[] templateBudgetXmlToPSCFormatByProtocolId(
			@PathVariable("protocolId") long protocolId,
			HttpServletResponse response) throws IOException, SAXException,
			ParserConfigurationException, TicketValidationException {

		ProtocolFormXmlData budgetXmlData = protocolFormXmlDataDao
				.getLastProtocolFormXmlDataByProtocolIdAndType(protocolId,
						ProtocolFormXmlDataType.BUDGET);

		String budgetXml = budgetXmlData.getXmlData();

		String pscXml = budgetXmlTransformService
				.outputCLARABudgetToPSCTemplate(budgetXml,
						String.valueOf(protocolId));

		byte[] bytes = pscXml.getBytes();

		response.setHeader("Content-Disposition",
				"attachment; filename=\"CLARA_TO_PSC_" + protocolId + ".xml\"");
		response.setContentLength(bytes.length);
		response.setContentType("text/xml");
		return bytes;


	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/transform-budget-xml-to-psc", method = RequestMethod.GET, produces="application/json")
	public @ResponseBody
	JsonResponse transformBudgetXmlToPSCFormatByProtocolId(
			@PathVariable("protocolId") long protocolId) throws IOException,
			SAXException, ParserConfigurationException,
			TicketValidationException {

		String msg = "";

		ProtocolFormXmlData budgetXmlData = protocolFormXmlDataDao
				.getLastProtocolFormXmlDataByProtocolIdAndType(protocolId,
						ProtocolFormXmlDataType.BUDGET);

		String budgetXml = budgetXmlData.getXmlData();
		Protocol protocol = protocolDao.findById(protocolId);
		
		String protocolMetaData = protocol.getMetaDataXml();
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		try {
			String pscXml = budgetXmlTransformService
					.transformCLARABudgetToPSCTemplate(budgetXml,
							String.valueOf(protocolId));
			//logger.debug("ok3");
			msg = pscServiceBroker.runPost(pscXml);
			//logger.debug("ok4");
		} catch (Exception ex) {
			logger.error("failed to push to psc on protocolId: " + protocolId, ex);
			return JsonResponseHelper.newErrorResponseStub("error: " + ex.getMessage());
		}

		//logger.debug(msg);
		if (msg.isEmpty()) {
			msg = "Successfully pushed...";
		}
		if(msg.equals("Successfully pushed!")){
			try {
				protocolMetaData=xmlProcessor.replaceOrAddNodeValueByPath("/protocol/pushed-to-psc", protocolMetaData, "y");
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			protocol.setMetaDataXml(protocolMetaData);
			protocolDao.saveOrUpdate(protocol);
			
			Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
                    protocol.getId());

			Document logsDoc = protocolTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
      
			String logId = UUID.randomUUID().toString();
      
			Date now = new Date();
            logEl.setAttribute("id", logId);
            logEl.setAttribute("parent-id", logId);
            logEl.setAttribute("action-user-id", "0");
            logEl.setAttribute("actor", u.getUsername());
            logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
            logEl.setAttribute("event-type", "PUSH_TO_PSC");
            logEl.setAttribute("form-id", "0");
            logEl.setAttribute("parent-form-id", "0");
            logEl.setAttribute("form-type", "PROTOCOL");
            logEl.setAttribute("log-type", "ACTION");
            logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
            
            String message = "Protocol has been pushed to PSC by "+u.getUsername()+".";
            logEl.setTextContent(message);

            logsDoc.getDocumentElement().appendChild(logEl);

            track = protocolTrackService.updateTrack(track, logsDoc);

		}
		
		
		// String result="transform successfully";
		return JsonResponseHelper.newSuccessResponseStube(msg);
	}

	@RequestMapping(value = "/ajax/protocols/{protocolId}/update-summary", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse updateProtocolSummary(
			@PathVariable("protocolId") long protocolId,
			@RequestParam("path") String path,
			@RequestParam("value") String value,
			@RequestParam("userId") long userId,
			@RequestParam("label") String questionLable) {
		Protocol protocol = protocolDao.findById(protocolId);
		String protocolMetaXml = protocol.getMetaDataXml();

		User currentUser = userDao.findById(userId);
		Date now = new Date();

		try {
			protocolMetaXml = xmlProcessor.replaceOrAddNodeValueByPath(path,
					protocolMetaXml, value);

			protocol.setMetaDataXml(protocolMetaXml);
			protocol = protocolDao.saveOrUpdate(protocol);

			Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
					protocolId);

			Document logsDoc = protocolTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id", logId);
			logEl.setAttribute("action-user-id", String.valueOf(userId));
			logEl.setAttribute("actor", currentUser.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("event-type", "PROTOCOL_SUMMARY_UPDATE");
			logEl.setAttribute("form-id", "0");
			logEl.setAttribute("parent-form-id", "0");
			logEl.setAttribute("form-type", "PROTOCOL");
			logEl.setAttribute("log-type", "ACTION");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));

			String message = currentUser.getPerson().getFullname() + " has changed the answer of \""+ questionLable +"\" to "+ value +".";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = protocolTrackService.updateTrack(track, logsDoc);

			auditService.auditEvent("PROTOCOL_SUMMARY_UPDATED", currentUser
					.getPerson().getFullname()
					+ " has updated answer of "
					+ path + " to " + value);
			
			if (path.equals("/protocol/summary/hospital-service-determinations/corporate-gurantor-code") || path.equals("/protocol/summary/hospital-service-determinations/insurance-plan-code")){
				if (value != null && !value.isEmpty()){
					List<String> gurantorCodeValues = xmlProcessor.listElementStringValuesByPath("/protocol/summary/hospital-service-determinations/corporate-gurantor-code", protocol.getMetaDataXml());
					List<String> planCodeValues = xmlProcessor.listElementStringValuesByPath("/protocol/summary/hospital-service-determinations/insurance-plan-code", protocol.getMetaDataXml());
					
					String gurantorCode = (gurantorCodeValues!=null && !gurantorCodeValues.isEmpty())?gurantorCodeValues.get(0):"";
					String planCode = (planCodeValues!=null && !planCodeValues.isEmpty())?planCodeValues.get(0):"";
					
					if (!gurantorCode.isEmpty() && !planCode.isEmpty()){
						logger.debug("Send pbs notification ...");
						EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("PBS_NOTIFICATION", protocol, null, null, null, currentUser, "");
						emailTemplate = protocolEmailService.sendProtocolLetter(protocol, null, null, currentUser, emailTemplate.getIdentifier(), "", "PBS Notification", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getSubject());
					}
				}
			}

			return new JsonResponse(false);
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to update protocol summary!",
					"", false, null);
		}

	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/close-study", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse closeStudy(
			@PathVariable("protocolId") long protocolId,
			@RequestParam("userId") long userId,
			@RequestParam(value="reason", required=false) String reason) {
		Protocol protocol = protocolDao.findById(protocolId);
		String protocolMetaXml = protocol.getMetaDataXml();

		User currentUser = userDao.findById(userId);
		Date now = new Date();

		try {
			//add new protocol status
			ProtocolStatus protocolStatus = new ProtocolStatus();
			protocolStatus.setProtocol(protocol);
			protocolStatus.setModified(now);
			protocolStatus.setCausedByCommittee(Committee.IRB_OFFICE);
			protocolStatus.setCauseByUser(currentUser);
			protocolStatus.setProtocolStatus(ProtocolStatusEnum.CLOSED);

			protocolStatusDao.saveOrUpdate(protocolStatus);
			
			//change status in the meta data xml
			protocolMetaXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/status", protocolMetaXml, ProtocolStatusEnum.CLOSED.getDescription());
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", ProtocolStatusEnum.CLOSED.getPriorityLevel());

			protocolMetaXml = xmlProcessor.addAttributesByPath(
					"/protocol/status", protocolMetaXml, attributes);
			
			protocol.setMetaDataXml(protocolMetaXml);
			
			protocol = protocolDao.saveOrUpdate(protocol);
			
			String closeReason = (reason != null)?reason:"";
			
			Map<String, Object> attributeRawValues = new HashMap<String, Object>();
			attributeRawValues.put("closeReason", closeReason);
			
			//send email
			EmailTemplate emailTemplate = protocolEmailService.sendProtocolLetter(protocol, Committee.IRB_OFFICE, attributeRawValues, currentUser, "ADMINISTRATIVELY_CLOSURE_LETTER", "", "Administratively Closure Letter", "Letter", null, null, "Administratively Closure Letter");
			
			//add log
			Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
					protocolId);

			Document logsDoc = protocolTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("action-user-id", String.valueOf(userId));
			logEl.setAttribute("actor", currentUser.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("date", DateFormatUtil.formateDateToMDY(now));
			logEl.setAttribute("event-type", "ADMINISTRATIVELY_CLOSURE");
			logEl.setAttribute("form-id", "0");
			logEl.setAttribute("parent-form-id", "0");
			logEl.setAttribute("form-type", "PROTOCOL");
			logEl.setAttribute("log-type", "LETTER");
			logEl.setAttribute("email-template-identifier", "ADMINISTRATIVELY_CLOSURE_LETTER");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id",logId);

			List<EmailRecipient> emailRecipients = emailService
					.getEmailRecipients(
							(emailTemplate.getRealRecipient() != null) ? emailTemplate
									.getRealRecipient() : emailTemplate
									.getTo());

			String recipents = "";
			for (EmailRecipient emailRecipient : emailRecipients) {
				recipents += "<b>" + emailRecipient.getDesc() + "</b>; ";
			}
			
			UploadedFile uploadedFile = emailTemplate.getUploadedFile();
			
			/*
			String filePath = uploadedFile.getPath();
			
			if (filePath.equals(uploadDirResourcePath)) {
				filePath = "/protocol/" + protocolId + "/";
			}
			*/
			
			String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
					+ uploadedFile.getPath()
					+ uploadedFile.getIdentifier() + "."
					+ uploadedFile.getExtension()
					+ "\">View Letter</a>";

			String message = "Study has been administratively closed by IRB Office.  Reason: "+ closeReason +".  An Administravely Closure Letter "+ emailLog +" has been sent. <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = protocolTrackService.updateTrack(track, logsDoc);
			
			//add audit
			auditService.auditEvent("PROTOCOL_ADMINISTRATIVELY_CLOSURE", currentUser
					.getPerson().getFullname()
					+ " has administratively close the protocol: "+ protocolId +" Reason: " + closeReason);

			return new JsonResponse(false);
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to close the study!",
					"", false, null);
		}

	}
	
	/*
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/hold", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse holdForm(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("userId") long userId,
			@RequestParam(value="reason", required=false) String reason) {
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		String protocolFormMetaData = protocolForm.getMetaDataXml();
		
		Protocol protocol = protocolForm.getProtocol();

		User currentUser = userDao.findById(userId);
		Date now = new Date();

		try {
			ProtocolFormStatus protocolFormStatus = new ProtocolFormStatus();
			protocolFormStatus.setProtocolForm(protocolForm);
			protocolFormStatus.setCauseByUser(currentUser);
			protocolFormStatus.setCausedByCommittee(Committee.IRB_OFFICE);
			protocolFormStatus.setModified(new Date());
			protocolFormStatus.setNote("Administratively Hold by IRB Office");
			protocolFormStatus.setProtocolFormStatus(ProtocolFormStatusEnum.ON_HOLD);
			protocolFormStatus.setRetired(Boolean.FALSE);
			
			protocolFormStatusDao.saveOrUpdate(protocolFormStatus);
			
			if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION)) {
				//add new protocol status
				ProtocolStatus protocolStatus = new ProtocolStatus();
				protocolStatus.setProtocol(protocolForm.getProtocol());
				protocolStatus.setModified(now);
				protocolStatus.setCausedByCommittee(Committee.IRB_OFFICE);
				protocolStatus.setCauseByUser(currentUser);
				protocolStatus.setProtocolStatus(ProtocolStatusEnum.ON_HOLD);
				
				protocolStatusDao.saveOrUpdate(protocolStatus);
			}
			
			//change status in the meta data xml
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/status", protocolFormMetaData, ProtocolFormStatusEnum.ON_HOLD.getDescription());
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", ProtocolFormStatusEnum.ON_HOLD.getPriorityLevel());

			protocolFormMetaData = xmlProcessor.addAttributesByPath(
					"/"+ protocolForm.getProtocolFormType().getBaseTag() +"/status", protocolFormMetaData, attributes);
			
			protocolForm.setMetaDataXml(protocolFormMetaData);
			
			protocolForm = protocolFormDao.saveOrUpdate(protocolForm);
			
			if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION)) {
				
				String protocolMetaData = protocol.getMetaDataXml();
				
				protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/status", protocolMetaData, ProtocolStatusEnum.ON_HOLD.getDescription());
				
				Map<String, String> protocolAttributes = new HashMap<String, String>();
				protocolAttributes.put("priority", ProtocolStatusEnum.ON_HOLD.getPriorityLevel());

				protocolMetaData = xmlProcessor.addAttributesByPath(
						"/protocol/status", protocolMetaData, attributes);
				
				protocol.setMetaDataXml(protocolMetaData);
				
				protocol = protocolDao.saveOrUpdate(protocol);
			}
			
			String holdReason = (reason != null)?reason:"";
			
			Map<String, Object> attributeRawValues = new HashMap<String, Object>();
			attributeRawValues.put("holdReason", holdReason);
			
			//send email
			EmailTemplate emailTemplate = protocolEmailService.sendProtocolLetter(protocol, Committee.IRB_OFFICE, attributeRawValues, currentUser, "ADMINISTRATIVELY_CLOSURE_LETTER", "", "Administratively Closure Letter", "Letter", null, null, "Administratively Closure Letter");
			
			//add log
			Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
					protocolId);

			Document logsDoc = protocolTrackService.getLogsDocument(track);

			Element logEl = logsDoc.createElement("log");
			
			String logId = UUID.randomUUID().toString();
			
			logEl.setAttribute("action-user-id", String.valueOf(userId));
			logEl.setAttribute("actor", currentUser.getPerson().getFullname());
			logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
			logEl.setAttribute("date", DateFormatUtil.formateDateToMDY(now));
			logEl.setAttribute("event-type", "ADMINISTRATIVELY_HOLD");
			logEl.setAttribute("form-id", String.valueOf(protocolFormId));
			logEl.setAttribute("parent-form-id", "0");
			logEl.setAttribute("form-type", "PROTOCOL");
			logEl.setAttribute("log-type", "LETTER");
			logEl.setAttribute("email-template-identifier", "ADMINISTRATIVELY_CLOSURE_LETTER");
			logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
			logEl.setAttribute("id", logId);
			logEl.setAttribute("parent-id",logId);
			
			List<EmailRecipient> emailRecipients = emailService
					.getEmailRecipients(
							(emailTemplate.getRealRecipient() != null) ? emailTemplate
									.getRealRecipient() : emailTemplate
									.getTo());

			String recipents = "";
			for (EmailRecipient emailRecipient : emailRecipients) {
				recipents += "<b>" + emailRecipient.getDesc() + "</b>; ";
			}
			
			UploadedFile uploadedFile = emailTemplate.getUploadedFile();
			
			String filePath = uploadedFile.getPath();
			
			if (filePath.equals(uploadDirResourcePath)) {
				filePath = "/protocol/" + protocolId + "/";
			}
			
			String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
					+ "/files" + filePath
					+ uploadedFile.getIdentifier() + "."
					+ uploadedFile.getExtension()
					+ "\">View Letter</a>";

			String message = ""+ protocolForm.getProtocolFormType().getDescription() +" form has been administratively held by IRB Office.  Reason: "+ holdReason +".  An Administravely Closure Letter "+ emailLog +" has been sent. <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

			logEl.setTextContent(message);

			logsDoc.getDocumentElement().appendChild(logEl);

			track = protocolTrackService.updateTrack(track, logsDoc);
			
			//add audit
			auditService.auditEvent("PROTOCOL_FORM_ADMINISTRATIVELY_HOLD", currentUser
					.getPerson().getFullname()
					+ " has administratively hold the protocol form: "+ protocolFormId +" Reason: " + holdReason);
				

			return new JsonResponse(false);
		} catch (Exception e) {
			e.printStackTrace();
			return new JsonResponse(true, "Failed to close the study!",
					"", false, null);
		}

	}
	*/
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/email-templates/{emailTemplateIdentifier}", method = RequestMethod.POST)
	public @ResponseBody JsonResponse getEmailTempates(@PathVariable("protocolId") long protocolId,
			@PathVariable("emailTemplateIdentifier") String emailTemplateIdentifier,
			@RequestParam("userId") long userId){
		User user = userDao.findById(userId);
		
		EmailTemplate emailTemplate = null;
		
		try{
			if (user.getAuthorities().contains(Permission.CAN_SEND_IRB_LETTER)){
				Protocol protocol = protocolDao.findById(protocolId);
				emailTemplate = protocolEmailDataService.loadObjectEmailTemplate(emailTemplateIdentifier, protocol, null, null, null, user, "");
			}
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to load email template.", "", false, null);
		}
		
		return new JsonResponse(false, "", "", false, emailTemplate);
	}
	
	private final String modificationFormDesc = "Use this form to complete the entry of a migrated study in the CLARA system, to submit a change to an ongoing study, or to respond to an audit on a study.";
	private final String continuingFormDesc = "Use this form to submit a continuing review for ongoing studies.";
	private final String staffOnlyModificationFormDesc = "Use this form to add and/or remove study staff, or to change staff roles, responsibilities, or notifications ONLY.";
	private final String reportableNewInfoFormDesc = "<![CDATA[Use this form to report study events to the IRB, including adverse events, deviations, and notifications. See <a href=\"http://www.uams.edu/irb/03-23-2011%20IRB%20Policy%20Updates/IRB%20Policy%2010.2.pdf\" target=\"_blank\">UAMS IRB Policy 10.2</a> for more information.]]>";
	private final String studyClosureFormDesc = "Use this form to request closure of a study.";
	private final String studyResumptionFormDesc = "Use this form to reopen a closed study, for study resumption.";
	private final String officeActionFormDesc = "For use by IRB Office Staff only.";
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/new-form-types.xml", method = RequestMethod.GET)
	public @ResponseBody String getNewFormList(@PathVariable("protocolId") long protocolId){
		Protocol protocol = protocolDao.findById(protocolId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		ProtocolStatus latestProtocolStatus = protocolStatusDao.findProtocolStatusByProtocolId(protocolId);
		
		String newFormList = "<forms>";
		
		//List<ProtocolForm> protocolFormLst = null;
		
		//ProtocolForm originalArchivedProtocolForm = null;
		
		String studyNature = "";
		
		if (objectAclService.hasEditObjectAccess(Protocol.class,
				protocolId, u) || u.getAuthorities().contains(Permission.ROLE_SECRET_ADMIN)){
			try{
				String type = xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol", protocol.getMetaDataXml(), "type");

				if (type == null || type.isEmpty()){
					boolean onlyAllowModForm = false;
					
					boolean allowModForm = protocolService.allowForm(protocol, ProtocolFormType.MODIFICATION);
					boolean allowCrForm = protocolService.allowForm(protocol, ProtocolFormType.CONTINUING_REVIEW);
					boolean allowStaffOnlyModForm = protocolService.allowForm(protocol, ProtocolFormType.STAFF);
					boolean allowStudyClosureForm = protocolService.allowForm(protocol, ProtocolFormType.STUDY_CLOSURE);
					
					if (allowModForm) {
						ProtocolForm protocolForm = protocolFormDao.getLatestProtocolFormByProtocolIdOnly(protocol.getId());;
						
						if (protocolForm.getProtocolFormType().equals(ProtocolFormType.ARCHIVE)) {
							onlyAllowModForm = true;
						}
					}
					
					if (latestProtocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.OPEN)){
						List<String> values = xmlProcessor.listElementStringValuesByPath("/protocol/study-nature", protocol.getMetaDataXml());
						
						studyNature = (values!=null && !values.isEmpty())?values.get(0):"";
						
						if (!studyNature.isEmpty() && studyNature.equals("hud-use")){
							boolean allowHudRenewalForm = protocolService.allowForm(protocol, ProtocolFormType.HUMANITARIAN_USE_DEVICE_RENEWAL);
							
							if (allowHudRenewalForm) {
								newFormList += "<form type=\"protocol\" id=\"humanitarian-use-device-renewal\" title=\"Humanitarian Use Device: Renewal Application\"><description></description></form>";
							}
							
							if (allowModForm && allowStaffOnlyModForm){
								newFormList += "<form type=\"protocol\" id=\"modification\" title=\"Modification\"><description>"+ this.modificationFormDesc +"</description></form>";
								newFormList += "<form type=\"protocol\" id=\"staff\" title=\"Staff Only Modification\"><description>"+ this.staffOnlyModificationFormDesc +"</description></form>";
								newFormList += "<form type=\"protocol\" id=\"reportable-new-information\" title=\"Reportable New Information\"><description>"+ this.reportableNewInfoFormDesc +"</description></form>";
								if (allowStudyClosureForm) newFormList += "<form type=\"protocol\" id=\"study-closure\" title=\"Study Closure\"><description>"+ this.studyClosureFormDesc +"</description></form>";
								//newFormList += "<form type=\"protocol\" id=\"audit\" title=\"Audit\"><description></description></form>";
							} else {
								newFormList += "<form type=\"protocol\" id=\"reportable-new-information\" title=\"Reportable New Information\"><description>"+ this.reportableNewInfoFormDesc +"</description></form>";
								if (allowStudyClosureForm) newFormList += "<form type=\"protocol\" id=\"study-closure\" title=\"Study Closure\"><description>"+ this.studyClosureFormDesc +"</description></form>";
								//newFormList += "<form type=\"protocol\" id=\"audit\" title=\"Audit\"><description></description></form>";
							}
							
							/*
							newFormList += "<form type=\"protocol\" id=\"humanitarian-use-device-renewal\" title=\"Humanitarian Use Device: Renewal Application\"><description></description></form>";
							newFormList += "<form type=\"protocol\" id=\"modification\" title=\"Modification\"><description>Use this form to complete the entry of a migrated study in the CLARA system, to submit a change to an ongoing study, or to respond to an audit on a study.</description></form>";
							newFormList += "<form type=\"protocol\" id=\"reportable-new-information\" title=\"Reportable New Information\"><description><![CDATA[Use this form to report study events to the IRB, including adverse events, deviations, and notifications. See <a href=\"http://www.uams.edu/irb/03-23-2011%20IRB%20Policy%20Updates/IRB%20Policy%2010.2.pdf\" target=\"_blank\">UAMS IRB Policy 10.2</a> for more information.]]></description></form>";
							if (allowStudyClosureForm) newFormList += "<form type=\"protocol\" id=\"study-closure\" title=\"Study Closure\"><description>Use this form to request closure of a study.</description></form>";
							*/
							return newFormList + "</forms>";
						} else {
							if (onlyAllowModForm){
								newFormList += "<form type=\"protocol\" id=\"modification\" title=\"Modification\"><description>"+ this.modificationFormDesc +"</description></form>";
							} else {
								//#2925 make it impossible to submit modifications on a study while it has a continuing review in review
								//#2932 CHANGE BACK: No concurrent submission for modifications and continuing reviews
								if (allowModForm && allowCrForm && allowStaffOnlyModForm){
									newFormList += "<form type=\"protocol\" id=\"continuing-review\" title=\"Continuing Review\"><description>"+ this.continuingFormDesc +"</description></form>";
									newFormList += "<form type=\"protocol\" id=\"modification\" title=\"Modification\"><description>"+ this.modificationFormDesc +"</description></form>";
									newFormList += "<form type=\"protocol\" id=\"staff\" title=\"Staff Only Modification\"><description>"+ this.staffOnlyModificationFormDesc +"</description></form>";
									newFormList += "<form type=\"protocol\" id=\"reportable-new-information\" title=\"Reportable New Information\"><description>"+ this.reportableNewInfoFormDesc +"</description></form>";
									if (allowStudyClosureForm) newFormList += "<form type=\"protocol\" id=\"study-closure\" title=\"Study Closure\"><description>"+ this.studyClosureFormDesc +"</description></form>";
									//newFormList += "<form type=\"protocol\" id=\"audit\" title=\"Audit\"><description></description></form>";
								} else {
									newFormList += "<form type=\"protocol\" id=\"reportable-new-information\" title=\"Reportable New Information\"><description>"+ this.reportableNewInfoFormDesc +"</description></form>";
									if (allowStudyClosureForm) newFormList += "<form type=\"protocol\" id=\"study-closure\" title=\"Study Closure\"><description>"+ this.studyClosureFormDesc +"</description></form>";
									//newFormList += "<form type=\"protocol\" id=\"audit\" title=\"Audit\"><description></description></form>";
								}
							}
						}
					} else if (latestProtocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.EXPIRED)) {
						
						if (onlyAllowModForm){
							newFormList += "<form type=\"protocol\" id=\"modification\" title=\"Modification\"><description>"+ this.modificationFormDesc +"</description></form>";
						} else {
							if (allowCrForm && allowModForm) {
								newFormList += "<form type=\"protocol\" id=\"continuing-review\" title=\"Continuing Review\"><description>"+ this.continuingFormDesc +"</description></form>";
							} 
							
							if (allowStudyClosureForm) {
								newFormList += "<form type=\"protocol\" id=\"study-closure\" title=\"Study Closure\"><description>"+ this.studyClosureFormDesc +"</description></form>";
							}
						}
					} else if (latestProtocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.CLOSED)) {
						newFormList += "<form type=\"protocol\" id=\"study-resumption\" title=\"Study Resumption\"><description>"+ this.studyResumptionFormDesc +"</description></form>";
					}
					
				}  else {
					if (type.equals("Emergency Use")){
						newFormList += "<form type=\"protocol\" id=\"emergency-use\" title=\"Emergency Use: Followup\"><description></description></form>";
						return newFormList + "</forms>";
					}
				}
			} catch (Exception e){
				e.printStackTrace();
				logger.error("something goes wrong...", e);
			}
		}
		
		if (u.getAuthorities().contains(Permission.ROLE_IRB_OFFICE)) {
			newFormList += "<form type=\"protocol\" id=\"office-action\" title=\"Office Action\"><description>"+ officeActionFormDesc +"</description></form>";
		}
		
		newFormList += "</forms>";
		return newFormList;
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/email-templates/{emailTemplateIdentifier}/send-letter", method = RequestMethod.POST)
	public @ResponseBody JsonResponse sendProtocolLetter(@PathVariable("protocolId") long protocolId,
			@PathVariable("emailTemplateIdentifier") String emailTemplateIdentifier,
			@RequestParam("userId") long userId,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam("xmlData") String xml,
			@RequestParam(value="parentId", required=false) String parentId){
		User user = userDao.findById(userId);
		Date now = new Date();
		
		EmailTemplate emailTemplate = null;
		
		String emailComment = "";
		String mailToLst = "";
		String ccLst = "";
		String subject = "";
		
		boolean authenticated = userAuthenticationService.isAuthenticated(username, password);
		
		if (authenticated){
			try {
				List<String> emailCommentLst = xmlProcessor.listElementStringValuesByPath(
						"//message/body", xml);
				emailComment = (emailCommentLst != null && !emailCommentLst.isEmpty()) ? emailCommentLst.get(0) : "";
				
				List<String> mailToLstValues = xmlProcessor.listElementStringValuesByPath(
						"//message/to", xml);			
				mailToLst = (mailToLstValues != null && !mailToLstValues.isEmpty()) ? mailToLstValues.get(0) : "";
				
				List<String> ccLstValues = xmlProcessor.listElementStringValuesByPath(
						"//message/cc", xml);
				ccLst = (ccLstValues != null && !ccLstValues.isEmpty()) ? ccLstValues.get(0) : "";
				
				List<String> subjectLst = xmlProcessor.listElementStringValuesByPath(
						"//message/subject", xml);
								
				subject = (subjectLst != null && !subjectLst.isEmpty()) ? subjectLst.get(0) : "";
								
				String letterName = "IRB Letter";
				
				if (emailTemplateIdentifier.equals("IRB_CORRECTION_LETTER")){
					letterName = "IRB Correction Letter";
				} else if (emailTemplateIdentifier.equals("RECEIPT_OF_AUDIT_REPORT_LETTER")){
					letterName = "Receipt of Audit Report Letter";
				}
				
				if (user.getAuthorities().contains(Permission.CAN_SEND_IRB_LETTER)){
					Protocol protocol = protocolDao.findById(protocolId);

					emailTemplate = protocolEmailService.sendProtocolLetter(protocol, null, null, user, emailTemplateIdentifier, emailComment, letterName, "Letter", mailToLst, ccLst, subject);
				} else {
					return new JsonResponse(true, "You don't have permission to send letter!", null, false);
				}
				
				Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
						protocolId);

				Document logsDoc = protocolTrackService.getLogsDocument(track);

				Element logEl = logsDoc.createElement("log");
				
				String logId = UUID.randomUUID().toString();

				logEl.setAttribute("action-user-id", String.valueOf(userId));
				logEl.setAttribute("actor", user.getPerson().getFullname());
				logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
				logEl.setAttribute("date", DateFormatUtil.formateDateToMDY(now));
				logEl.setAttribute("event-type", "SEND_IRB_LETTER");
				logEl.setAttribute("form-id", "0");
				logEl.setAttribute("parent-form-id", "0");
				logEl.setAttribute("form-type", "PROTOCOL");
				logEl.setAttribute("log-type", "LETTER");
				logEl.setAttribute("email-template-identifier", emailTemplateIdentifier);
				logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
				logEl.setAttribute("id", logId);
				logEl.setAttribute("parent-id", (parentId != null && !parentId.isEmpty())?parentId:logId);
				
				List<EmailRecipient> emailRecipients = emailService
						.getEmailRecipients(
								(emailTemplate.getRealRecipient() != null) ? emailTemplate
										.getRealRecipient() : emailTemplate
										.getTo());

				String recipents = "";
				for (EmailRecipient emailRecipient : emailRecipients) {
					// emailRecipient.getType().equals(EmailRecipient.RecipientType.)
					recipents += "<b>" + emailRecipient.getDesc() + "</b>; ";
				}
				
				/*
				String remotePath = "";
				
				if (getFileRemoteDirPath().contains("/training")){
					remotePath = "/training";
				} else if (getFileRemoteDirPath().contains("/dev")){
					remotePath = "/dev";
				}
				*/
				
				UploadedFile uploadedFile = emailTemplate.getUploadedFile();
				String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
						+ uploadedFile.getPath()
						+ uploadedFile.getIdentifier() + "."
						+ uploadedFile.getExtension()
						+ "\">View Letter</a>";

				String message = "IRB Office has send a "+ letterName +""+ emailLog +" <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

				logEl.setTextContent(message);

				logsDoc.getDocumentElement().appendChild(logEl);

				track = protocolTrackService.updateTrack(track, logsDoc);
				
			} catch (Exception e) {
				logger.error("xml: " + xml, e);
				e.printStackTrace();
				
				return new JsonResponse(true, "Failed to send letter!", null, false);
			}
		} else {
			return new JsonResponse(true, "Username or password is not correct!", null, false);
		}
		
		return new JsonResponse(false);
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public PSCServiceBroker getPscServiceBroker() {
		return pscServiceBroker;
	}

	@Autowired(required = true)
	public void setPscServiceBroker(PSCServiceBroker pscServiceBroker) {
		this.pscServiceBroker = pscServiceBroker;
	}

	public BudgetXmlTransformService getBudgetXmlTransformService() {
		return budgetXmlTransformService;
	}

	@Autowired(required = true)
	public void setBudgetXmlTransformService(
			BudgetXmlTransformService budgetXmlTransformService) {
		this.budgetXmlTransformService = budgetXmlTransformService;
	}

	public CASService getCasService() {
		return casService;
	}

	@Autowired(required = true)
	public void setCasService(CASService casService) {
		this.casService = casService;
	}

	public ProtocolFormDetailContentService getProtocolFormDetailContentService() {
		return protocolFormDetailContentService;
	}

	@Autowired(required = true)
	public void setProtocolFormDetailContentService(
			ProtocolFormDetailContentService protocolFormDetailContentService) {
		this.protocolFormDetailContentService = protocolFormDetailContentService;
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

	public AuditService getAuditService() {
		return auditService;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}

	@Autowired(required = true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public FormService getFormService() {
		return formService;
	}

	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}

	@Autowired(required = true)
	public void setProtocolTrackService(
			ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required = true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}
	
	@Autowired(required = true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public EmailService getEmailService() {
		return emailService;
	}
	
	@Autowired(required = true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public String getFileServer() {
		return fileServer;
	}

	public void setFileServer(String fileServer) {
		this.fileServer = fileServer;
	}

	public UserAuthenticationService getUserAuthenticationService() {
		return userAuthenticationService;
	}
	
	@Autowired(required = true)
	public void setUserAuthenticationService(UserAuthenticationService userAuthenticationService) {
		this.userAuthenticationService = userAuthenticationService;
	}

	public String getFileRemoteDirPath() {
		return fileRemoteDirPath;
	}

	public void setFileRemoteDirPath(String fileRemoteDirPath) {
		this.fileRemoteDirPath = fileRemoteDirPath;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public StudyDefinitionWSClient getStudyDefinitionWSClient() {
		return studyDefinitionWSClient;
	}

	@Autowired(required = true)
	public void setStudyDefinitionWSClient(StudyDefinitionWSClient studyDefinitionWSClient) {
		this.studyDefinitionWSClient = studyDefinitionWSClient;
	}

	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required = true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}

	public RelationService getRelationService() {
		return relationService;
	}
	
	@Autowired(required = true)
	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
	}

	public ContractStatusDao getContractStatusDao() {
		return contractStatusDao;
	}
	
	@Autowired(required = true)
	public void setContractStatusDao(ContractStatusDao contractStatusDao) {
		this.contractStatusDao = contractStatusDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}
	
	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}
	
	public String getUploadDirResourcePath() {
		return uploadDirResourcePath;
	}

	public void setUploadDirResourcePath(String uploadDirResourcePath) {
		this.uploadDirResourcePath = uploadDirResourcePath;
	}

}
