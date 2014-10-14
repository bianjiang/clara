package edu.uams.clara.webapp.contract.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.service.EmailDataService;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;
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
		
		//User piUser = getSpecifiRoleUser(contractXmlDataXml, "Principal Investigator");
		 
		User piUser = null;
			
		try{
			List<User> piUserLst = getFormService().getUsersByKeywordAndSearchField("Principal Investigator", contractFormXmlData.getXmlData(), UserSearchField.ROLE);
			
			piUser = (piUserLst != null)?piUserLst.get(0):null;
		} catch (Exception e){
			//e.printStackTrace();
		}
		
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
	
	@Override
	public EmailTemplate setRealSubjectAndReceipt(Form form,
			EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues) {
		ContractForm contractForm = (ContractForm) form;
		
		Map<String, String> subjectAttributsValues = new HashMap<String, String>();
		subjectAttributsValues.put("{contractId}", contractForm.getContract().getContractIdentifier());
		subjectAttributsValues.put("{contractType}", getFormService().getSafeStringValueByKey(getFormService().getValuesFromXmlString(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), getXpathList()), "/contract/basic-information/contract-type", ""));
		
		//String studyPi = (getSpecifiRoleUser(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), "Principal Investigator") != null)?getSpecifiRoleUser(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), "Principal Investigator").getPerson().getFullname():"";
		User studyPi = null;
		
		try{
			List<User> piUserLst = getFormService().getUsersByKeywordAndSearchField("Principal Investigator", contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()).getXmlData(), UserSearchField.ROLE);
			
			studyPi = (piUserLst != null)?piUserLst.get(0):null;
		} catch (Exception e){
			//e.printStackTrace();
		}

		subjectAttributsValues.put("{studyPI}", (studyPi != null)?studyPi.getPerson().getFullname():"");
		
		emailTemplate.setRealSubject(fillSubject(emailTemplate.getSubject(), subjectAttributsValues));
		
		emailTemplate = this.resolveEmailTemplate(contractForm, emailTemplate, committee, attributeRawValues);
		
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
