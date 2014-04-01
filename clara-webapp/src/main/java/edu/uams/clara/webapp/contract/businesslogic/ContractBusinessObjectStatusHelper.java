package edu.uams.clara.webapp.contract.businesslogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelper;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocument;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.contract.service.ContractMetaDataXmlService;
import edu.uams.clara.webapp.contract.service.email.ContractEmailService;
import edu.uams.clara.webapp.contract.service.history.ContractTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContractBusinessObjectStatusHelper extends
BusinessObjectStatusHelper {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractBusinessObjectStatusHelper.class);
	
	public ContractBusinessObjectStatusHelper()
			throws ParserConfigurationException {
		super();
		setObjectType("CONTRACT");
	}
	
	private ContractStatusDao contractStatusDao;

	private ContractFormStatusDao contractFormStatusDao;
	
	private ContractDao contractDao;

	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;
	
	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	private ContractTrackService contractTrackService;
	
	private ContractMetaDataXmlService contractMetaDataXmlService;
	
	private ContractEmailService contractEmailService;
	
	@Override
	public void logStatusChange(Form form, User user,  Map<String, Object> attributeRawValues, String logsTemplate) throws IOException, SAXException{
		ContractForm contractForm = (ContractForm)form;
		
		contractTrackService.logStatusChange(contractForm, user, attributeRawValues, logsTemplate);
		
	}
	
	/**
	 * 
	 */
	@Override
	public void sendNotifications(Form form, User user,  Map<String, Object> attributeRawValues, String notificationsTemplate, String extraDataXml) throws IOException, SAXException{
		ContractForm contractForm = (ContractForm)form;
		
		XmlProcessor xmlProcessor = getXmlProcessor();

		Document notificationsTemplateDoc = xmlProcessor.loadXmlStringToDOM(notificationsTemplate);

		NodeList notifications = notificationsTemplateDoc.getDocumentElement()
				.getElementsByTagName("notification");
	
		//String userMessage = attributeRawValues.get("USER_MESSAGE") != null?attributeRawValues.get("USER_MESSAGE").toString():""; 
		
		Committee committee = null;
		
		try{
			committee = Committee.valueOf(attributeRawValues.get(
					"COMMITTEE").toString());
		}catch(Exception e){
			//don't care... down stream handles null
		}
		
		List<EmailTemplate> emailTemplates = new ArrayList<EmailTemplate>();
		
		String emailComment = "";
		String mailToLst = "";
		String ccLst = "";
		String committeeNote = attributeRawValues.get("COMMITTEE_NOTE") != null ? attributeRawValues
				.get("COMMITTEE_NOTE").toString() : "";
		
		try{
			emailComment = (xmlProcessor.listElementStringValuesByPath("//message/body", extraDataXml)!=null && !xmlProcessor.listElementStringValuesByPath("//message/body", extraDataXml).isEmpty())?xmlProcessor.listElementStringValuesByPath("//message/body", extraDataXml).get(0):"";
			mailToLst = (xmlProcessor.listElementStringValuesByPath("//message/to", extraDataXml)!=null && !xmlProcessor.listElementStringValuesByPath("//message/to", extraDataXml).isEmpty())?xmlProcessor.listElementStringValuesByPath("//message/to", extraDataXml).get(0):"";
			ccLst = (xmlProcessor.listElementStringValuesByPath("//message/cc", extraDataXml)!=null && !xmlProcessor.listElementStringValuesByPath("//message/cc", extraDataXml).isEmpty())?xmlProcessor.listElementStringValuesByPath("//message/cc", extraDataXml).get(0):"";
		} catch(Exception e){
			logger.debug("no letter body");
			//e.printStackTrace();
		}
		
		if (emailComment.isEmpty()){
			emailComment = committeeNote;
		}
		
		for (int i = 0; i < notifications.getLength(); i++) {

			Element notificationEl = (Element) notifications.item(i);
			
			String notificationType = notificationEl.getAttribute("notification-type");
			
			String emailTemplateIdentifier = notificationEl.getAttribute("email-template-identifier");
			
			String xpathCondition = notificationEl
					.getAttribute("xpath-condition");
			
			String formXpathCondition = notificationEl
					.getAttribute("form-xpath-condition");
			
			logger.debug("emailTemplateIdentifier: " + emailTemplateIdentifier);
			
			if (notificationType == null || notificationType.isEmpty()
					|| emailTemplateIdentifier == null
					|| emailTemplateIdentifier.isEmpty())
				continue;
			
			if (xpathCondition != null && !xpathCondition.isEmpty()){
				if (!xpathConditionCheck(contractForm.getObjectMetaData(), xpathCondition)){
					continue;
				}
			}
			
			if (formXpathCondition != null && !formXpathCondition.isEmpty()){
				if (!xpathConditionCheck(contractForm.getMetaDataXml(), formXpathCondition)){
					continue;
				}
			}
			
			if (notificationType == null || notificationType.isEmpty() || emailTemplateIdentifier == null || emailTemplateIdentifier.isEmpty()) continue;
			
			 //notification-type="NOTIFICATION" email-template-identifier="NEW_SUBMISSION_SUBMITTED_TO_GATEKEEPER"
			EmailTemplate emailTemplate = null;
			
			if (notificationType.equals("NOTIFICATION")){
				emailTemplate = contractEmailService.sendNotification(contractForm, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, mailToLst, ccLst);
				
			}else if (notificationType.equals("LETTER")){
				String letterName = notificationEl.getAttribute("letter-name"); 
				String docType = notificationEl.getAttribute("doc-type");
				
				if(letterName == null || letterName.isEmpty()){
					letterName = emailTemplateIdentifier;
				}
				
				if(docType == null || docType.isEmpty()){
					docType = "Letter";
				}

				emailTemplate = contractEmailService.sendLetter(contractForm, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, letterName, docType, mailToLst, ccLst);		
				
			}
			
			if (emailTemplate != null){ //impossible to be null
			
				emailTemplates.add(emailTemplate);
			
				logger.debug("emailContent: " + emailTemplate.getTemplateContent());
			}

		}
		
		logger.debug("emailTemplates: " + emailTemplates);
		attributeRawValues.put("EMAIL_TEMPLATES", emailTemplates);
	}


	@Override
	public void changeObjectStatus(Form form, Date now, Committee committee, User user, String status) {
		
		logger.debug("changing ObjectStatus to " + status);
		ContractForm contractForm = (ContractForm)form;		
		
		ContractStatus contractStatus = new ContractStatus();
		contractStatus.setContract(contractForm.getContract());
		contractStatus.setModified(now);
		contractStatus.setCausedByCommittee(committee);
		contractStatus.setCauseByUser(user);
		contractStatus.setContractStatus(ContractStatusEnum.valueOf(status));

		contractStatusDao.saveOrUpdate(contractStatus);
		
	}

	@Override
	public void changeObjectFormStatus(Form form, Date now, Committee committee, User user, String status) {
		logger.debug("changing ObjectFormStatus to " + status);
		
		ContractForm contractForm = (ContractForm)form;
		
		ContractFormStatus contractFormStatus = new ContractFormStatus();
		contractFormStatus.setContractForm(contractForm);
		contractFormStatus.setModified(now);
		contractFormStatus.setCausedByCommittee(committee);
		contractFormStatus.setCauseByUser(user);
		contractFormStatus.setContractFormStatus(ContractFormStatusEnum
				.valueOf(status));

		contractFormStatusDao.saveOrUpdate(contractFormStatus);
		
	}


	/**
	 * @TODO need to save xmldata here 
	 */
	@Override
	public void changeObjectFormCommitteeStatus(Form form, Date now, Committee committee, User user, Committee involvedCommittee, String status, String commiteeNote, String xmlData, String action) {
		
		logger.debug("changing ObjectFormCommitteeStatus to " + status);
		
		ContractForm contractForm = (ContractForm)form;
		ContractFormCommitteeStatus contractFormCommitteeStatus = new ContractFormCommitteeStatus();
		contractFormCommitteeStatus.setContractForm(contractForm);
		contractFormCommitteeStatus.setModified(now);
		contractFormCommitteeStatus.setCauseByUser(user);
		contractFormCommitteeStatus.setCausedByCommittee(committee);
		
		if (committee.equals(involvedCommittee)) {
			contractFormCommitteeStatus.setNote(commiteeNote);
		}
		
		contractFormCommitteeStatus.setCommittee(involvedCommittee);
		contractFormCommitteeStatus.setXmlData(xmlData);
		contractFormCommitteeStatus.setAction(action);

		contractFormCommitteeStatus
				.setContractFormCommitteeStatus(ContractFormCommitteeStatusEnum
						.valueOf(status));

		contractFormCommitteeStatusDao
				.saveOrUpdate(contractFormCommitteeStatus);
	}
	
	//need to change to event driven later ...
	@Override
	public void changeObjectFormDocumentStatus(Form form, Date now,
			Committee committee, User user, String status, boolean changeBudgetDocStatus, boolean changeProtocolDocStatus,
			boolean changeConsentDocStatus) {
		logger.debug("changing ObjectFormDocumentStatus to " + status);
		
		ContractForm contractForm = (ContractForm) form;
		
		List<ContractFormXmlDataDocumentWrapper> contractFormXmlDataDocuments = contractFormXmlDataDocumentDao.listDocumentsByContractFormId(contractForm.getId());
		
		for (ContractFormXmlDataDocumentWrapper wrapper : contractFormXmlDataDocuments){
			ContractFormXmlDataDocument contractFormXmlDataDocument = contractFormXmlDataDocumentDao.findById(wrapper.getId());
			
			contractFormXmlDataDocument.setStatus(ContractFormXmlDataDocument.Status.valueOf(status));
			
			contractFormXmlDataDocumentDao.saveOrUpdate(contractFormXmlDataDocument);
		}
		
	}
	@Override
	public String getFormStatus(Form form){
		ContractForm contractForm = (ContractForm)form;
		
		ContractFormStatus contractFormStatus = null;
		
		try {
			contractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(contractForm.getId());
		}catch(Exception ex){
			return ""; // no status 
		}
		
		
		return contractFormStatus.getContractFormStatus().toString();
	}
	
	@Override
	public void updateMetaDataXml(Form form, String extraDataXml, boolean updateMetaData) {
		ContractForm contractForm = (ContractForm) form;
		
//		contractMetaDataXmlService.updateContractMetaDataXml(contractForm,
//				contractForm.getContractFormType()
//						.getDefaultContractFormXmlDataType());
		
		logger.debug("extraDataXml for update: " + extraDataXml);
		
		contractForm = contractMetaDataXmlService.updateContractFormMetaDataXml(contractForm.getTypedContractFormXmlDatas().get(contractForm.getContractFormType().getDefaultContractFormXmlDataType()), extraDataXml);
		
		//contractMetaDataXmlService.updateContractFormStatus(contractForm);
		
		contractMetaDataXmlService.updateContractMetaDataXml(contractForm);
		
	}

	public ContractStatusDao getContractStatusDao() {
		return contractStatusDao;
	}

	@Autowired(required=true)
	public void setContractStatusDao(ContractStatusDao contractStatusDao) {
		this.contractStatusDao = contractStatusDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

	@Autowired(required=true)
	public void setContractFormStatusDao(ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}
	
	
	public ContractMetaDataXmlService getContractMetaDataXmlService() {
		return contractMetaDataXmlService;
	}

	@Autowired(required=true)
	public void setContractMetaDataXmlService(ContractMetaDataXmlService contractMetaDataXmlService) {
		this.contractMetaDataXmlService = contractMetaDataXmlService;
	}



	public ContractTrackService getContractTrackService() {
		return contractTrackService;
	}


	@Autowired(required=true)
	public void setContractTrackService(ContractTrackService contractTrackService) {
		this.contractTrackService = contractTrackService;
	}
	
	public ContractEmailService getContractEmailService() {
		return contractEmailService;
	}

	@Autowired(required=true)
	public void setContractEmailService(ContractEmailService contractEmailService) {
		this.contractEmailService = contractEmailService;
	}

	@Override
	public String getObjectStatus(Form form) {
		ContractForm contractForm = (ContractForm)form;
		
		ContractStatus contractStatus = null;
		
		try {
			contractStatus = contractDao.getLatestContractStatusByContractId(contractForm.getContract().getId());
		}catch(Exception ex){
			return ""; // no status 
		}
		logger.debug("original object status: " + contractStatus.getContractStatus().toString());
		
		return contractStatus.getContractStatus().toString();
	}

	@Override
	public String getFormCommitteeStatus(Form form, Committee committee) {
		ContractForm contractForm = (ContractForm)form;
		
		ContractFormCommitteeStatus contractFormCommitteeStatus = null;
		
		try {
			contractFormCommitteeStatus = contractFormCommitteeStatusDao.getLatestByCommitteeAndContractFormId(committee, contractForm.getId());
		}catch(Exception ex){
			return ""; // no status 
		}
		
		
		return (contractFormCommitteeStatus!=null)?contractFormCommitteeStatus.getContractFormCommitteeStatus().toString():"";
	}

	public ContractDao getContractDao() {
		return contractDao;
	}
	
	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	@Override
	protected String getCommitteeReviewFormStatus(Committee nextCommittee) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getCommitteeReviewObjectStatus(Committee nextCommittee) {
		// TODO Auto-generated method stub
		return null;
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
	public void updateAssignedCommittees(Form form,
			List<Committee> selectedCommittees) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void triggerEvents(Form form, User user, Committee committee,
			String eventsTemplate, String action, String condition)
			throws IOException, SAXException {
		// TODO Auto-generated method stub
		
	}

}
