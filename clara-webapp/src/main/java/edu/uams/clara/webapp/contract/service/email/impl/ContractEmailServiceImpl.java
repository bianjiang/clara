package edu.uams.clara.webapp.contract.service.email.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.service.email.ContractEmailService;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.service.email.ContractEmailDataService;
import edu.uams.clara.webapp.contract.service.email.impl.ContractEmailServiceImpl;

public class ContractEmailServiceImpl implements ContractEmailService {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractEmailServiceImpl.class);
	
	private ContractDao contractDao;
	private ContractFormDao contractFormDao;
	private ContractFormXmlDataDao contractFormXmlDataDao;
	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	private UserDao userDao;
	private ContractEmailDataService contractEmailDataService;
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	private EmailService emailService;
	
	private void sendEmail(EmailTemplate emailTemplate, Map<String, Object> attributeRawValues, User user, String emailComment, String mailTo, String cc){

		List<String> mailToList = (!mailTo.isEmpty())?Arrays.asList(mailTo.split(",")):null;

		List<String> ccList = (!cc.isEmpty())?Arrays.asList(cc.split(",")):null;
		
		try{
			List<EmailRecipient> emailRecipients = Lists.newArrayList();
			List<String> mailToLst = Lists.newArrayList();
			List<String> ccLst = Lists.newArrayList();
			
			if (!emailTemplate.getRealRecipient().isEmpty()){
				emailRecipients = emailService.getEmailRecipients(emailTemplate.getRealRecipient());
				mailToLst = emailService.getTemplateRecipientsAddress(emailRecipients);
			} else {
				if (mailToList != null && !mailToList.isEmpty()){
					emailTemplate.setRealRecipient(emailService.setRealReceiptByEmailAddress(mailToList).toString());
					mailToLst = emailService.getRecipientsAddress(mailToList);
				}
			}
			
			if (!emailTemplate.getRealCCRecipient().isEmpty()){
				emailRecipients = emailService.getEmailRecipients(emailTemplate.getRealCCRecipient());
				ccLst = emailService.getTemplateRecipientsAddress(emailRecipients);
			} else {
				if (ccList != null && !ccList.isEmpty()){
					emailTemplate.setRealCCRecipient(emailService.setRealReceiptByEmailAddress(ccList).toString());
					ccLst = emailService.getRecipientsAddress(ccList);
				}
			}

			emailService.sendEmail(emailTemplate.getTemplateContent(), mailToLst, ccLst, emailTemplate.getRealSubject(), null);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public EmailTemplate sendNotification(ContractForm contractForm, Committee committee, Map<String, Object> attributeRawValues, User user, String emailTemplateIdentifier,
			String emailComment, String mailTo, String cc) {
		EmailTemplate emailTemplate = notify(contractForm, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, mailTo, cc);
		
		sendEmail(emailTemplate, attributeRawValues, user, emailComment, mailTo, cc);
		
		return emailTemplate;
	}

	@Override
	public EmailTemplate sendLetter(ContractForm contractForm,
			Committee committee, Map<String, Object> attributeRawValues, User user, String emailTemplateIdentifier,
			String emailComment, String letterName, String docType, String mailTo, String cc)
			throws IOException {
		EmailTemplate emailTemplate = notify(contractForm, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, mailTo, cc);
		
		UploadedFile uploadedFile = fileGenerateAndSaveService
				.processFileGenerateAndSave(contractForm.getContract(),
						letterName,
						IOUtils.toInputStream(emailTemplate.getTemplateContent().replace("{0}", "")),
						"html",
						"text/html");
		
		emailTemplate.setUploadedFile(uploadedFile);
		
		sendEmail(emailTemplate, attributeRawValues, user, emailComment, mailTo, cc);
		
		return emailTemplate;
	}
	
	public EmailTemplate notify(ContractForm contractForm, Committee committee, Map<String, Object> attributeRawValues, User user, String emailTemplateIdentifier, String emailComment, String mailTo, String cc){
		
		//String identifier = contractF//contractFormStatus.getContractFormStatus() + "_" + committee.toString() + "_" + action;
		attributeRawValues = emailService.addInputRecipentsToRawAttributes(attributeRawValues, mailTo, cc);

		EmailTemplate emailTemplate = contractEmailDataService.loadEmailTemplate(emailTemplateIdentifier, contractForm, committee, attributeRawValues, user, emailComment);
		
		/*
		String emailComment = "";
		
		try{
			emailComment = xmlProcessor.listElementStringValuesByPath("//letter/message/body", reviewPageXmlData).get(0);
		} catch(Exception e){
			e.printStackTrace();
		}
		*/
		//String finalEmailBody = emailTemplate.getTemplateContent();
		
		
		
		return emailTemplate;		
		
		//@TODO need to send the email out here...
	}
	
	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
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

	public EmailService getEmailService() {
		return emailService;
	}
	
	@Autowired(required=true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

}
