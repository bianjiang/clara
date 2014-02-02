package edu.uams.clara.webapp.contract.web.ajax;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.relation.RelatedObject;
import edu.uams.clara.webapp.common.domain.security.MutexLock;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.security.MutexLockService;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.relation.RelationService;
import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Controller
public class ContractDashboardAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractDashboardAjaxController.class);

	private ContractDao contractDao;
	
	private RelationService relationService;

	private ContractFormDao contractFormDao;

	private ContractFormXmlDataDao contractFormXmlDataDao;

	private ContractFormStatusDao contractFormStatusDao;
	
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;

	private XmlProcessor xmlProcessor;
	
	private UserDao userDao;
	
	private MutexLockService mutexLockService;
	
	private AuditService auditService;
	
	private ObjectAclService objectAclService;
	
	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private FormService formService;
	
	private static final int timeOutPeriod = 45;
	
	private List<ContractFormStatusEnum> canEditContractFormStatusLst = new ArrayList<ContractFormStatusEnum>();{
		canEditContractFormStatusLst.add(ContractFormStatusEnum.DRAFT);
		canEditContractFormStatusLst.add(ContractFormStatusEnum.PENDING_PI_ENDORSEMENT);
	}
	
	private String getActionXml(ContractForm contractForm, ContractFormStatus contractFormStatus, ContractFormXmlData lastContractFormXmlData, User u, String formTypeMachineReadable, String xmlResult){
		switch (contractFormStatus.getContractFormStatus()) {
		case UNDER_REVISION:
			if (objectAclService.hasEditObjectAccess(Contract.class,
					contractForm.getContract().getId(), u)){
				xmlResult += "<action cls='green'><name>Continue Revision</name><url>/contracts/"
						+ contractForm.getContract().getId()
						+ "/contract-forms/"
						+ contractForm.getId()
						+ "/"
						+ formTypeMachineReadable
						+ "/revise?committee=PI</url></action>";
			}
			
			break;
		case DRAFT:
		case PENDING_PI_ENDORSEMENT:
			if (objectAclService.hasEditObjectAccess(Contract.class,
					contractForm.getContract().getId(), u) || u.getAuthorities().contains(Permission.DELETE_CONTRACT_DOCUMENT)){
				xmlResult += "<action cls='green'><name>Edit "+ contractForm.getContractFormType().getDescription() +"</name><url>/contracts/"
						+ contractForm.getContract().getId()
						+ "/contract-forms/" + contractForm.getId() + "/"
						+ formTypeMachineReadable + "/contract-form-xml-datas/"
						+ lastContractFormXmlData.getId() + "/</url></action>";
			}
			
			break;
		case REVISION_REQUESTED:
			if (objectAclService.hasEditObjectAccess(Contract.class,
					contractForm.getContract().getId(), u)){
				xmlResult += "<action cls='green'><name>Revise</name><url>/contracts/"
						+ contractForm.getContract().getId()
						+ "/contract-forms/" + contractForm.getId() + "/"
						+ formTypeMachineReadable
						+ "/revise?committee=PI</url></action>";
			}
			break;
			default:
				break;
		}
				
		return xmlResult;
	}

	/**
	 * return a list of forms associated with this contract. the controller will
	 * decide "Action" cell, which defines what the user can do based on the
	 * user's role and rights.
	 * 
	 * @param contractId
	 * @return
	 */
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/list.xml", method = RequestMethod.GET)
	public @ResponseBody
	String listContractForms(@PathVariable("contractId") long contractId,
			@RequestParam("userId") long userId) {
		/*
		 * User u =
		 * (User)SecurityContextHolder.getContext().getAuthentication().
		 * getPrincipal();
		 * 
		 * for(GrantedAuthority ga:u.getAuthorities()){
		 * logger.debug(ga.getAuthority()); }
		 */
		
		User user = userDao.findById(userId);

		List<ContractForm> contractForms = contractDao
				.listLatestContractFormsByContractId(contractId);

		logger.debug("size: " + contractForms.size());
		String xmlResult = "<list>";

		for (ContractForm contractForm : contractForms) {
			String formTypeMachineReadable = contractForm.getContractFormType()
					.getUrlEncoded();

			ContractFormStatus pformStatus = contractFormStatusDao
					.getLatestContractFormStatusByFormId(contractForm.getId());

			ContractFormXmlData lastContractFormXmlData = contractFormXmlDataDao
					.getLastContractFormXmlDataByContractFormIdAndType(
							contractForm.getId(), contractForm
									.getContractFormType()
									.getDefaultContractFormXmlDataType());
			
			String index = "";
			
			try {
				index = xmlProcessor.getAttributeValueByPathAndAttributeName("/contract", contractForm.getMetaDataXml(), "index");
			} catch (Exception e) {
				e.printStackTrace();
			}
			

			// ContractFormXmlData latestContractFormXmlData =
			// contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType());

			xmlResult += "<contract-form contractFormId=\""
					+ contractForm.getId() + "\" lastVersionId=\""
					+ lastContractFormXmlData.getId() + "\" index=\""+ index +"\">";
			xmlResult += "<contract-form-type id=\"" + formTypeMachineReadable
					+ "\">"
					+ contractForm.getContractFormType().getDescription()
					+ "</contract-form-type>";
			xmlResult += "<url>/contracts/"
					+ contractForm.getContract().getId() + "/contract-forms/"
					+ contractForm.getId() + "/dashboard</url>";
			// xmlResult += "<editurl>/contracts/" +
			// contractForm.getContract().getId() + "/contract-forms/" +
			// contractForm.getId() +
			// "/"+formTypeMachineReadable+"/contract-form-xml-datas/" +
			// lastContractFormXmlData.getId() + "/</editurl>";
			
			logger.debug("checking who is editing...");

			//User editing = mutexLockService.whoIsEditing(ContractForm.class, contractForm.getId());
			MutexLock mutexLock = mutexLockService.getLockedByObjectClassAndId(ContractForm.class, contractForm.getId());

			Date currentDate = new Date();
			Date lastModifiedDate = mutexLock != null?mutexLock.getModified():null;
			DateTime currentDateTime = new DateTime(currentDate);
			DateTime lastModifiedDateTime = mutexLock != null?new DateTime(lastModifiedDate):null;
			
			String lockedOrNot = "false";
			String ifEditingMessage = "";
			long editingUserId = (mutexLock!=null)?mutexLock.getUser().getId():0;		
			
//			if(mutexLock != null){
//				
//				ifEditingMessage= mutexLock.getUser().getPerson().getFullname() + " is editing...";
//				
//			}
			
			xmlResult += "<actions>";
			
			if (!mutexLockService.isLockedByObjectClassAndIdForCurrentUser(ContractForm.class, contractForm.getId(), user)
					) {
				xmlResult = getActionXml(contractForm, pformStatus, lastContractFormXmlData, user, formTypeMachineReadable, xmlResult);
			} else {
				if (mutexLock != null){
					logger.debug("time period after last access: " + Minutes.minutesBetween(lastModifiedDateTime, currentDateTime).getMinutes());
					if (Minutes.minutesBetween(lastModifiedDateTime, currentDateTime).getMinutes() > timeOutPeriod){
						xmlResult = getActionXml(contractForm, pformStatus, lastContractFormXmlData, user, formTypeMachineReadable, xmlResult);
					} else {
						lockedOrNot = "true";
						ifEditingMessage= mutexLock.getUser().getPerson().getFullname() + " is editing...";
					}
				}
			}
			
			xmlResult += "<action cls='white'><name>View Summary</name><url>/contracts/"
					+ contractForm.getContract().getId() + "/contract-forms/"
					+ contractForm.getId() + "/" + formTypeMachineReadable
					+ "/summary?noheader=true&amp;review=false</url></action>";
			
			if (user.getAuthorities().contains(Permission.FORWARD_CONTRACT)){
				xmlResult += "<action cls='blue'><name>Forward</name><url type='javascript'>Clara.Application.FormController.ForwardSelectedContract();</url></action>";
			}	
			
			if (user.getAuthorities().contains(Permission.ROLE_CONTRACT_ADMIN)) {
				xmlResult += "<action cls='blue'><name>Execute Contract</name><url type='javascript'>Clara.Application.FormController.ExecuteForm();</url></action>";
			}

			
			if (user.getAuthorities().contains(Permission.CAN_UPLOAD_DOCUMENT)){
				xmlResult += "<action cls='blue'><name>Upload Document</name><url type='javascript'>Clara.Application.FormController.ChooseUploadDocumentRole({formId:"
							+ contractForm.getId() + "});</url></action>";
			}
			
			try {
				List<ContractFormCommitteeStatus> contractFormCommitteeStatusLst = contractFormCommitteeStatusDao
						.listLatestByContractFormId(contractForm.getId());
				ContractFormCommitteeStatus lastestCommitteeStatus = contractFormCommitteeStatusDao.getLatestByCommitteeAndContractFormId(Committee.CONTRACT_MANAGER, contractForm.getId());
				
				if (lastestCommitteeStatus != null && lastestCommitteeStatus.getContractFormCommitteeStatus().equals(ContractFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT)){
					if (user.getAuthorities().contains(Permission.ROLE_CONTRACT_MANAGER)){
						xmlResult += "<action cls='blue'><name>Contract: Assign Reviewer</name><url type='javascript'>Clara.Application.FormController.Assign({roleId:'ROLE_CONTRACT_MANAGER',formTypeId:\'"+ formTypeMachineReadable +"\',formId:"
								+ contractForm.getId()
								+ ",committee:'CONTRACT_MANAGER'},{objectType:'Contract'});</url></action>";
					}
				}
				
				if (user.getAuthorities().contains(Permission.ROLE_REVIEWER) && contractFormCommitteeStatusLst != null
						&& !contractFormCommitteeStatusLst.isEmpty()) {
					xmlResult += "<action cls='blue'><name>Review</name><url type='javascript'>Clara.Application.FormController.ChooseReviewRole({formId:"
							+ contractForm.getId() + "});</url></action>";
					
					for (ContractFormCommitteeStatus cfcs : contractFormCommitteeStatusLst) {
						if ((cfcs.getCommittee().equals(Committee.CONTRACT_MANAGER) || cfcs.getCommittee().equals(Committee.CONTRACT_LEGAL_REVIEW)) && cfcs.getContractFormCommitteeStatus().equals(ContractFormCommitteeStatusEnum.REVIEWER_ASSIGNED)) {
							if (user.getAuthorities().contains(
									Permission.ROLE_CONTRACT_LEGAL_REVIEW)) {
								xmlResult += "<action cls='blue'><name>Contract: Re-assign Reviewer</name><url type='javascript'>Clara.Application.QueueController.Reassign({roleId:'ROLE_CONTRACT_LEGAL_REVIEW',formTypeId:\'"+ formTypeMachineReadable +"\',formId:"
										+ contractForm.getId()
										+ ",committee:'CONTRACT_LEGAL_REVIEW'},{objectType:'Contract'});</url></action>";
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (user.getAuthorities().contains(Permission.ROLE_CONTRACT_LEGAL_REVIEW) && !pformStatus.equals(ContractFormStatusEnum.CANCELLED) && lockedOrNot.equals("false")){
				xmlResult += "<action cls='red'><name>Cancel</name><url type='javascript'>Clara.Application.FormController.CancelForm();</url></action>";
			}
			if (user.getAuthorities().contains(Permission.DELETE_CONTRACT_FORM_ANY_TIME)) {
				xmlResult += "<action cls='red'><name>Delete "+ contractForm.getContractFormType().getDescription() +"</name><url type='javascript'>Clara.Application.FormController.RemoveForm();</url></action>";
			}
			
			xmlResult += "</actions>";
			
			xmlResult += "<status><description>"
					+ org.apache.commons.lang.StringEscapeUtils
							.escapeXml(pformStatus.getContractFormStatus()
									.getDescription())
					+ "</description><modified>" + pformStatus.getModified()
					+ "</modified><lock value=\""+ lockedOrNot +"\" modified=\""+lastModifiedDate+"\" userid=\""+ editingUserId +"\" message=\""+ ifEditingMessage +"\" /></status>";
			
			xmlResult += formService.getAssignedReviewers(contractForm);
			
			xmlResult += "</contract-form>";
		}

		xmlResult += "</list>";

		return xmlResult;
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/get-user-role-list", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody Source getUserCommitteeList(@PathVariable("contractFormId") long contractFormId,
			@RequestParam("userId") long userId){
		User currentUser = userDao.findById(userId);
		String finalCommitteeXml = "<committees>";
		try{
			List<ContractFormCommitteeStatus> contractFormCommitteeStatusLst = contractFormCommitteeStatusDao.listLatestByContractFormId(contractFormId);
			
			List<Committee> reviewCommitteeList = new ArrayList<Committee>();
			if (contractFormCommitteeStatusLst != null && !contractFormCommitteeStatusLst.isEmpty()){
				for (ContractFormCommitteeStatus cfcs : contractFormCommitteeStatusLst){
					if (!cfcs.getCommittee().equals(Committee.PI)){
						reviewCommitteeList.add(cfcs.getCommittee());
					}		
				}
			} else {
				return XMLResponseHelper.newErrorResponseStub("No committee has reviewed this form yet!");
			}
			
			String committeeXml = "";
			if (reviewCommitteeList != null && !reviewCommitteeList.isEmpty()){
				for (Committee c : reviewCommitteeList){
					if (currentUser.getAuthorities().contains(c.getRolePermissionIdentifier())){
						committeeXml += "<committee name=\""+ c.toString() +"\" desc=\""+ c.getDescription() +"\" />";
					}
				}
			} else {
				return XMLResponseHelper.newErrorResponseStub("");
			}
			
			finalCommitteeXml += committeeXml + "</committees>";

			return XMLResponseHelper.newDataResponseStub(finalCommitteeXml);
		} catch (Exception e){
			e.printStackTrace();
			return XMLResponseHelper.newErrorResponseStub("Failed to load committee list!");
		}
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/contract-executed", method = RequestMethod.POST, produces="application/xml")
	public @ResponseBody Source executeContract(@PathVariable("contractFormId") long contractFormId,
			@RequestParam("userId") long userId,
			@RequestParam("note") String note){
		User currentUser = userDao.findById(userId);

		ContractForm contractForm = contractFormDao.findById(contractFormId);
		try{
			businessObjectStatusHelperContainer
			.getBusinessObjectStatusHelper(contractForm.getContractFormType().toString())
			.triggerAction(
					contractForm,
					Committee.CONTRACT_ADMIN,
					currentUser,
					"CONTRACT_EXECUTED_WITHOUT_WORKFLOW",
					note, "");

			return XMLResponseHelper.newSuccessResponseStube("Successfully execute contract!");
		} catch (Exception e){
			e.printStackTrace();
			return XMLResponseHelper.newErrorResponseStub("Failed execute contract!");
		}
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/get-upload-documentuser-role-list", method = RequestMethod.GET, produces="application/xml")
	public @ResponseBody Source getUserCommitteeListForUploadDocument(@PathVariable("contractFormId") long contractFormId,
			@RequestParam("userId") long userId){
		User currentUser = userDao.findById(userId);
		String finalCommitteeXml = "<committees>";
		try{
			Set<UserRole> userRoles = currentUser.getUserRoles();
			String committeeXml = "";
			for (UserRole ur : userRoles){	
				if (ur.getRole().getCommitee() != null){
					committeeXml += "<committee name=\""+ ur.getRole().getCommitee().toString() +"\" desc=\""+ ur.getRole().getCommitee().getDescription() +"\" />";
				}
			}
			
			finalCommitteeXml += committeeXml + "</committees>";

			return XMLResponseHelper.newDataResponseStub(finalCommitteeXml);
		} catch (Exception e){
			e.printStackTrace();
			return XMLResponseHelper.newErrorResponseStub("Failed to load committee list!");
		}
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/add-related-contract", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse addRelatedContract(@PathVariable("contractId") long contractId,
			@RequestParam("relatedContractId") long relatedContractId,
			@RequestParam("userId") long userId) {
		//might need to restrict roles
//		try{
//			Contract relatedContract = contractDao.findById(relatedContractId);
//			Contract contract = contractDao.findById(contractId);
//
//			RelatedContract newRelatedContract = new RelatedContract();
//			newRelatedContract.setContract(contract);
//			newRelatedContract.setRelated_contract(relatedContract);
//
//			//relatedContract.setContract(contract);
//			relatedContractDao.saveOrUpdate(newRelatedContract);
//			
//			return new JsonResponse(false, "", "");
//		} catch (Exception e){
//			e.printStackTrace();
//			return new JsonResponse(true, "Failed to add related contract!", "");
//		}
		try{
			RelatedObject relatedObject = relationService.addRelationByIdAndType(contractId, relatedContractId, "contract", "contract");	
			return new JsonResponse(false, "", "", false, relatedObject);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to add related contract!", "", false, null);
		}
		
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/delete-related-contract", method = RequestMethod.POST)
	public @ResponseBody
	JsonResponse deleteRelatedContract(@PathVariable("contractId") long contractId,
			@RequestParam("relatedContractId") long relatedContractId,
			@RequestParam("userId") long userId) {
		//might need to restrict roles
//		try{
//			RelatedContract exRelatedContract = relatedContractDao.getRelatedContractByContractIdAndRelatedContractId(contractId, relatedContractId);
//			exRelatedContract.setRetired(true);
//			
//			relatedContractDao.saveOrUpdate(exRelatedContract);
//			
//			return new JsonResponse(false, "", "");
//		} catch (Exception e){
//			e.printStackTrace();
//			return new JsonResponse(true, "Failed to remove related contract!", "");
//		}
		
		try{
			RelatedObject relatedObject = relationService.removeRelationByIdAndType(contractId, relatedContractId, "contract", "contract");
			
			return new JsonResponse(false, "", "", false, null);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to remove related contract!", "", false, null);
		}
		
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/new-form-types.xml", method = RequestMethod.GET)
	public @ResponseBody String getNewFormList(@PathVariable("contractId") long contractId){
		Contract contract = contractDao.findById(contractId);
		
		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		
		String newFormList = "<forms>";
		
		if (objectAclService.hasEditObjectAccess(Contract.class, contractId, u) || u.getAuthorities().contains(Permission.ROLE_CONTRACT_ADMIN) || u.getAuthorities().contains(Permission.ROLE_CONTRACT_LEGAL_REVIEW) || u.getAuthorities().contains(Permission.ROLE_CONTRACT_MANAGER)){
			newFormList += "<form type=\"contract\" id=\"amendment\" title=\"Contract Amendment\"><description></description></form>";
		}
		
		newFormList += "</forms>";
		return newFormList;
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/update-summary", method = RequestMethod.POST)
	public @ResponseBody JsonResponse updateContractSummary(@PathVariable("contractId") long contractId,
			@RequestParam("path") String path,
			@RequestParam("value") String value,
			@RequestParam("userId") long userId){
		Contract contract = contractDao.findById(contractId);
		String contractMetaXml = contract.getMetaDataXml();
		
		User currentUser = userDao.findById(userId);
		
		try{
			contractMetaXml = xmlProcessor.replaceOrAddNodeValueByPath(path, contractMetaXml, value);
			
			contract.setMetaDataXml(contractMetaXml);
			contractDao.saveOrUpdate(contract);
			
			auditService.auditEvent("CONTRACT_SUMMARY_UPDATED",
					currentUser.getPerson().getFullname() + " has updated answer of " + path + " to " + value);
			
			
			return new JsonResponse(false, "", "", false, null);
		} catch (Exception e){
			e.printStackTrace();
			return new JsonResponse(true, "Failed to update contract summary!", "", false, null);
		}

	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(
			ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(
			ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public MutexLockService getMutexLockService() {
		return mutexLockService;
	}
	
	@Autowired(required = true)
	public void setMutexLockService(MutexLockService mutexLockService) {
		this.mutexLockService = mutexLockService;
	}

	public RelationService getRelationService() {
		return relationService;
	}
	
	@Autowired(required = true)
	public void setRelationService(RelationService relationService) {
		this.relationService = relationService;
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

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}
	
	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

}
