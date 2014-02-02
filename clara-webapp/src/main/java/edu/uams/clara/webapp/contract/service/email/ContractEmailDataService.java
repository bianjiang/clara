package edu.uams.clara.webapp.contract.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailDataService;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeCommentDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeComment;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;

public class ContractEmailDataService extends EmailDataService<Contract> {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractEmailDataService.class);
	
	private ContractFormDao contractFormDao;
	
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;
	
	private ContractFormCommitteeCommentDao contractFormCommitteeCommentDao;
	
	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	@Override
	public List<String> getXpathList() {
		List<String> contractXmlDataXPaths = new ArrayList<String>(0);
		
		contractXmlDataXPaths.add("/contract/basic-information/nature");
		contractXmlDataXPaths.add("/contract/basic-information/contract-type");
		contractXmlDataXPaths.add("/contract/protocol");
		
		return contractXmlDataXPaths;
	}
	
	private List<String> getContactList(String contractXmlDataXml){
		List<String> contactList = new ArrayList<String>();
		
		try{
			Document doc = getXmlProcessor().loadXmlStringToDOM(contractXmlDataXml);
			XPath xpath = getXmlProcessor().getXPathInstance();
			
			String lookupPath = "//sponsors/sponsor/company";
			
			NodeList contactLst = (NodeList) xpath.evaluate(lookupPath,
					doc, XPathConstants.NODESET);
			
			if (contactLst.getLength() > 0){
				for (int i=0; i < contactLst.getLength(); i++){
					Element contactEl = (Element) contactLst.item(i);
					
					String contactName = contactEl.getTextContent();
					
					contactList.add(contactName);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return contactList;
	}
	
	@Override
	public Map<String, Object> getEmailData(Form form, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment) {
		ContractForm contractForm = (ContractForm) form;
		String contractXmlDataXml = "";
		
		String contractMetaData = contractForm.getMetaDataXml();

		//ContractForm contractForm = contractFormDao.findById(contractFormId);
		
		List<ContractFormCommitteeComment> contractFormCommitteeCommentList = Lists.newArrayList();
		
		contractFormCommitteeCommentList = contractFormCommitteeCommentDao.listCommentsByContractFormIdAndCommitteeAndInLetterOrNot(contractForm.getId(), committee, true);

		List<ContractFormXmlDataDocumentWrapper> contractFormDocumentList = Lists.newArrayList();
		
		contractFormDocumentList = contractFormXmlDataDocumentDao.listDocumentsByContractFormId(contractForm.getId());

		List<ContractFormXmlDataDocumentWrapper> contractFormDocumentByCommitteeList = Lists.newArrayList();
		
		contractFormDocumentByCommitteeList = contractFormXmlDataDocumentDao.listDocumentsByContractFormIdAndCommittee(contractForm.getId(), committee);
		
		ContractFormXmlData contractFormXmlData = contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType());
		
		contractXmlDataXml = contractFormXmlData.getXmlData();	
		
		Map<String, Object> finalTemplateValues = new HashMap<String, Object>();
		
		Map<String, List<String>> values = getFormService().getValuesFromXmlString(contractXmlDataXml, getXpathList());

		logger.debug("values: " + values);
		
		String cancelReason= "";
		String cancelSubReason= "";
		 try{
			 List<String> cancelReasonLst = getXmlProcessor().listElementStringValuesByPath("/contract/cancel-reason", contractMetaData);
			 
			 cancelReason = (cancelReasonLst!=null && !cancelReasonLst.isEmpty())?cancelReasonLst.get(0):"";
			 
			 List<String> cancelSubReasonLst = getXmlProcessor().listElementStringValuesByPath("/contract/cancel-sub-reason", contractMetaData);
			 
			 cancelSubReason = (cancelSubReasonLst!=null && !cancelSubReasonLst.isEmpty())?cancelSubReasonLst.get(0):"";
		 } catch (Exception e){
			 
		 }
		
		User piUser = getSpecifiRoleUser(contractXmlDataXml, "Principal Investigator");
		
		String contractFormLink = getAppHost() + "/clara-webapp/contracts/" + contractForm.getContract().getId() + "/dashboard";
		String queueLink = getAppHost() + "/clara-webapp/queues";

		finalTemplateValues.put("committeeDesc", committee.getDescription());
		finalTemplateValues.put("contractId", contractForm.getContract().getContractIdentifier());
		finalTemplateValues.put("contractDescritpion", getFormService().getSafeStringValueByKey(values, "/contract/basic-information/nature", ""));
		finalTemplateValues.put("contractType", getFormService().getSafeStringValueByKey(values, "/contract/basic-information/contract-type", ""));
		finalTemplateValues.put("protocolId", getFormService().getSafeStringValueByKey(values, "/contract/protocol", ""));
		finalTemplateValues.put("piUser", piUser);
		finalTemplateValues.put("studyStaffList", getStudyStaffs(contractXmlDataXml));
		finalTemplateValues.put("contractContactList", getContactList(contractXmlDataXml));
		finalTemplateValues.put("assignedLegal", getReviewers(contractMetaData, Permission.ROLE_CONTRACT_LEGAL_REVIEW));
		finalTemplateValues.put("assignedContract", getReviewers(contractMetaData, Permission.ROLE_CONTRACT_ADMIN));
		finalTemplateValues.put("reviewerName", user.getPerson().getFirstname() + " " + user.getPerson().getLastname());
		finalTemplateValues.put("dashboardLink", contractFormLink);
		finalTemplateValues.put("queueLink", queueLink);
		finalTemplateValues.put("emailComment", emailComment.replace("{0}", ""));
		finalTemplateValues.put("cancelReason", cancelReason);
		finalTemplateValues.put("cancelSubReason", cancelSubReason);
		finalTemplateValues.put("inLetterComments", contractFormCommitteeCommentList);
		finalTemplateValues.put("contractFormDocuments", contractFormDocumentList);	
		finalTemplateValues.put("contractFormCommitteeDocuments", contractFormDocumentByCommitteeList);

		return finalTemplateValues;
	}
	
	private static Map<String, Committee> committeeMatchMap = new HashMap<String, Committee>();{
		committeeMatchMap.put("realComplianceReviewer", Committee.COMPLIANCE_REVIEW);
		committeeMatchMap.put("realHospitalServiceReviewer", Committee.HOSPITAL_SERVICES);
		committeeMatchMap.put("realProtocolLegalReviewer", Committee.PROTOCOL_LEGAL_REVIEW);
		committeeMatchMap.put("realDepartmentReviewer", Committee.DEPARTMENT_CHAIR);
		committeeMatchMap.put("realCollegeReviewer", Committee.COLLEGE_DEAN);
		committeeMatchMap.put("realIRBAssigner", Committee.IRB_ASSIGNER);
		committeeMatchMap.put("realPharmacyReviewer", Committee.PHARMACY_REVIEW);
		committeeMatchMap.put("realContractManager", Committee.CONTRACT_MANAGER);
		committeeMatchMap.put("realGatekeeper", Committee.GATEKEEPER);
		committeeMatchMap.put("realACHGatekeeper", Committee.ACHRI);
		committeeMatchMap.put("realBudgetManager", Committee.BUDGET_MANAGER);
		committeeMatchMap.put("realPTL", Committee.PTL);
		committeeMatchMap.put("realProtocolLegalReviewer", Committee.PROTOCOL_LEGAL_REVIEW);
		committeeMatchMap.put("realACHPharmacyReviewer", Committee.ACH_PHARMACY_REVIEWER);
		committeeMatchMap.put("realContractManager", Committee.CONTRACT_MANAGER);
		committeeMatchMap.put("realLegalReviewer", Committee.CONTRACT_LEGAL_REVIEW);
		committeeMatchMap.put("realContractReviewer", Committee.CONTRACT_ADMIN);
	}
	
	private static Map<String, String> realRecipientMatchMap = new HashMap<String, String>();{
		realRecipientMatchMap.put("AssignedReviewer", "assignedReviewer");
		realRecipientMatchMap.put("RevisionRequestedAssignedReviewer", "requestedReviewer");
		realRecipientMatchMap.put("studyPI", "studyPI");
		realRecipientMatchMap.put("onlyPI", "onlyPI");
	}
	
	private static Map<String, Permission> assignedReviewerMatchMap = new HashMap<String, Permission>();{
		assignedReviewerMatchMap.put("AssignedCoverageReviewer", Permission.ROLE_COVERAGE_REVIEWER);
		assignedReviewerMatchMap.put("AssignedBudgetReviewer", Permission.ROLE_BUDGET_REVIEWER);
		assignedReviewerMatchMap.put("AssignedRegulatoryReviewer", Permission.ROLE_MONITORING_REGULATORY_QA_REVIEWER);
		assignedReviewerMatchMap.put("AssignedIRBOfficeReviewer", Permission.ROLE_IRB_OFFICE);
		assignedReviewerMatchMap.put("AssignedContractReviewer", Permission.ROLE_CONTRACT_ADMIN);
		assignedReviewerMatchMap.put("AssignedLegalReviewer", Permission.ROLE_CONTRACT_LEGAL_REVIEW);
	}
	
	@Override
	public EmailTemplate setRealSubjectAndReceipt(Form form,
			EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues) {
		ContractForm contractForm = (ContractForm) form;
		
		Committee revisionRequestedCommittee = null;
		
		try{
			revisionRequestedCommittee = Committee.valueOf(attributeRawValues.get(
				"REVISION_REQUEST_COMMITTEE").toString());
			
		}catch(Exception e){
			//e.printStackTrace();
			logger.warn("No Revision Requested Committee!");
		}
		
		Map<String, String> subjectAttributsValues = new HashMap<String, String>();
		subjectAttributsValues.put("{contractId}", contractForm.getContract().getContractIdentifier());
		subjectAttributsValues.put("{contractType}", getFormService().getSafeStringValueByKey(getFormService().getValuesFromXmlString(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), getXpathList()), "/contract/basic-information/contract-type", ""));
		
		String studyPi = (getSpecifiRoleUser(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), "Principal Investigator") != null)?getSpecifiRoleUser(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), "Principal Investigator").getPerson().getFullname():"";

		subjectAttributsValues.put("{studyPI}", studyPi);
		
		emailTemplate.setRealSubject(fillSubject(emailTemplate.getSubject(), subjectAttributsValues));
		
		List<String> real = new ArrayList<String>();
		List<String> realCc = Lists.newArrayList();
		
		//List<EmailRecipient> emailRecipients = Lists.newArrayList();
		List<EmailRecipient> toEmailRecipients = Lists.newArrayList();
		List<EmailRecipient> ccEmailRecipients = Lists.newArrayList();
		
		Map<String, List<EmailRecipient>> receipientsMap = Maps.newHashMap();
		
		try{
			toEmailRecipients = getEmailService().getEmailRecipients(emailTemplate.getTo());
		} catch (Exception e){
			//e.printStackTrace();
			
		}
		
		receipientsMap.put("to", toEmailRecipients);
		
		try{
			ccEmailRecipients = getEmailService().getEmailRecipients(emailTemplate.getCc());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		receipientsMap.put("cc", ccEmailRecipients);
		
		for (Entry<String, List<EmailRecipient>> mapEntry : receipientsMap.entrySet()){
			for (EmailRecipient er : mapEntry.getValue()){
				if (er.getType().equals(EmailRecipient.RecipientType.INDIVIDUAL)){
					if (mapEntry.getKey().equals("to")){
						real.add(er.getJsonString());
					} else {
						realCc.add(er.getJsonString());
					}					
				} else {
					for (Entry<String, String> realRecipientEntry : realRecipientMatchMap.entrySet()){
						if (er.getAddress().contains(realRecipientEntry.getKey())){
							List<String> realRecipientLst = getReviewersOrPIMailToList(committee, contractForm.getMetaDataXml(), realRecipientEntry.getValue());
							if (realRecipientLst != null && !realRecipientLst.isEmpty()){
								for (String s : realRecipientLst){
									if (mapEntry.getKey().equals("to")){
										real.add(s);
									} else {
										realCc.add(s);
									}
								}
							}
						}
					}
					
					for (Entry<String, Committee> entry : committeeMatchMap.entrySet()){
						if (er.getAddress().contains(entry.getKey())){
							List<String> committeeMatchLst = getNextCommitteeMailToList(contractForm, entry.getValue());
							if (committeeMatchLst != null && !committeeMatchLst.isEmpty()){
								for (String s : committeeMatchLst){
									if (mapEntry.getKey().equals("to")){
										real.add(s);
									} else {
										realCc.add(s);
									}
									
								}
							}
						}
					}
					
					for (Entry<String, Permission> assignedReviewerEntry : assignedReviewerMatchMap.entrySet()){
						if (er.getAddress().contains(assignedReviewerEntry.getKey())){
							List<String> assignedReviewerLst = getSpecificReviewerList(contractForm, assignedReviewerEntry.getValue());
							if (assignedReviewerLst != null && !assignedReviewerLst.isEmpty()){
								for (String s : assignedReviewerLst){
									if (mapEntry.getKey().equals("to")){
										real.add(s);
									} else {
										realCc.add(s);
									}
								}
							}
						}
					}
					
					if (er.getAddress().contains("RevisionRequestedCommittee") && revisionRequestedCommittee != null){
						List<String> revisionReqCommitteeLst = getNextCommitteeMailToList(contractForm, revisionRequestedCommittee);
						if (revisionReqCommitteeLst != null && !revisionReqCommitteeLst.isEmpty()){
							for (String s : revisionReqCommitteeLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
								} else {
									realCc.add(s);
								}
							}
						}
					}
				}
			}
		}
		
		if (real != null && !real.isEmpty()){
			emailTemplate.setRealRecipient(real.toString());
		} else {
			emailTemplate.setRealRecipient(emailTemplate.getTo());
		}
		
		if (realCc != null && !realCc.isEmpty()){
			emailTemplate.setRealCCRecipient(realCc.toString());
		} else {
			emailTemplate.setRealCCRecipient(emailTemplate.getCc());
		}
		
		logger.debug("real recipient: " + emailTemplate.getRealRecipient());
		
		return emailTemplate;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}
	
	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

	public ContractFormCommitteeCommentDao getContractFormCommitteeCommentDao() {
		return contractFormCommitteeCommentDao;
	}

	@Autowired(required=true)
	public void setContractFormCommitteeCommentDao(
			ContractFormCommitteeCommentDao contractFormCommitteeCommentDao) {
		this.contractFormCommitteeCommentDao = contractFormCommitteeCommentDao;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	@Override
	public Map<String, Object> getEmailData(Form form, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment, Agenda agenda) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getProtocolEmailData(Protocol protocol,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EmailTemplate setObjectRealSubjectAndReceipt(Protocol protocol,
			EmailTemplate emailTemplate, Committee committee,
			Map<String, Object> attributeRawValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getMetaDataXpathList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> getAgendaEmailData(Agenda agenda,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EmailTemplate setAgendaRealSubjectAndReceipt(Agenda agenda,
			EmailTemplate emailTemplate, Committee committee,
			Map<String, Object> attributeRawValues) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getFormMetaDataXpathList(String formBaseTag) {
		// TODO Auto-generated method stub
		return null;
	}
}
