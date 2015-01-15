package edu.uams.clara.webapp.protocol.service.email.impl;

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
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;

public class ProtocolEmailServiceImpl implements ProtocolEmailService {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolEmailServiceImpl.class);

	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	private UserDao userDao;
	private ProtocolEmailDataService protocolEmailDataService;
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	private EmailService emailService;
	private AgendaItemDao agendaItemDao;
	
	private void sendEmail(EmailTemplate emailTemplate, Map<String, Object> attributeRawValues, User user, String emailComment, String mailTo, String cc){

		List<String> mailToList = (mailTo != null && !mailTo.isEmpty())?Arrays.asList(mailTo.split(",")):null;

		List<String> ccList = (cc != null && !cc.isEmpty())?Arrays.asList(cc.split(",")):null;
		
		try{
			List<EmailRecipient> emailRecipients = Lists.newArrayList();
			List<String> mailToLst = Lists.newArrayList();
			List<String> ccLst = Lists.newArrayList();
			
			if (!emailTemplate.getRealRecipient().isEmpty()){
				emailRecipients = emailService.getEmailRecipients(emailTemplate.getRealRecipient());
				mailToLst = emailService.getTemplateRecipientsAddress(emailRecipients);
			} else {
				if (mailToList != null && !mailToList.isEmpty()){
					//emailTemplate.setRealCCRecipient(emailService.setRealReceiptByEmailAddress(mailToList).toString());
					mailToLst = emailService.getRecipientsAddress(mailToList);
				}
			}
			
			if (!emailTemplate.getRealCCRecipient().isEmpty()){
				emailRecipients = emailService.getEmailRecipients(emailTemplate.getRealCCRecipient());
				ccLst = emailService.getTemplateRecipientsAddress(emailRecipients);
			} else {
				if (ccList != null && !ccList.isEmpty()){
					//emailTemplate.setRealCCRecipient(emailService.setRealReceiptByEmailAddress(mailToList).toString());
					ccLst = emailService.getRecipientsAddress(ccList);
				}
			}

			emailService.sendEmail(emailTemplate.getTemplateContent(), mailToLst, ccLst, emailTemplate.getRealSubject(), null);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public EmailTemplate sendLetter(ProtocolForm protocolForm, Committee committee, Map<String, Object> attributeRawValues, User user, String emailTemplateIdentifier, String emailComment, String letterName, String docType, String mailTo, String cc) throws IOException{
		
		EmailTemplate emailTemplate = notify(protocolForm, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, mailTo, cc);
		
		UploadedFile uploadedFile = fileGenerateAndSaveService
				.processFileGenerateAndSave(protocolForm.getProtocol(),
						letterName,
						IOUtils.toInputStream(emailTemplate.getTemplateContent().replace("{0}", "")),
						"html",
						"text/html");
		
		emailTemplate.setUploadedFile(uploadedFile);
		
		sendEmail(emailTemplate, attributeRawValues, user, emailComment, mailTo, cc);
		
		return emailTemplate;
	}
	
	private EmailTemplate sendObjectLetter(Protocol protocol, Agenda agenda,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment,
			String letterName, String docType, String mailTo, String cc, String subject) throws IOException{
		//attributeRawValues = emailService.addInputRecipentsToRawAttributes(attributeRawValues, mailTo, cc);
		
		EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate(emailTemplateIdentifier, protocol, agenda, committee, attributeRawValues, user, emailComment);
		
		UploadedFile uploadedFile = null;
		
		if (protocol != null){
			uploadedFile = fileGenerateAndSaveService
					.processFileGenerateAndSave(protocol,
							letterName,
							IOUtils.toInputStream(emailTemplate.getTemplateContent().replace("{0}", "")),
							"html",
							"text/html");
		}
		
		emailTemplate.setUploadedFile(uploadedFile);
		
		sendEmail(emailTemplate, attributeRawValues, user, emailComment, mailTo, cc);
		
		return emailTemplate;
	}
	
	private EmailTemplate sendObjectNotification(Protocol protocol,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment,
			String letterName, String docType, String mailTo, String cc, String subject) throws IOException{
		//attributeRawValues = emailService.addInputRecipentsToRawAttributes(attributeRawValues, mailTo, cc);
		
		EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate(emailTemplateIdentifier, protocol, null, committee, attributeRawValues, user, emailComment);
		
		sendEmail(emailTemplate, attributeRawValues, user, emailComment, mailTo, cc);
		
		return emailTemplate;
	}
	
	@Override
	public EmailTemplate sendProtocolLetter(Protocol protocol,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment,
			String letterName, String docType, String mailTo, String cc, String subject) throws IOException{
		EmailTemplate emailTemplate = sendObjectLetter(protocol, null, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, letterName, docType, mailTo, cc, subject);
		
		return emailTemplate;
	}
	
	@Override
	public EmailTemplate sendProtocolNotification(Protocol protocol,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment,
			String letterName, String docType, String mailTo, String cc, String subject) throws IOException{
		EmailTemplate emailTemplate = sendObjectNotification(protocol, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, letterName, docType, mailTo, cc, subject);
		
		return emailTemplate;
	}
	
	@Override
	public EmailTemplate sendAgendaLetter(Agenda agenda, Committee committee,
			Map<String, Object> attributeRawValues, User user,
			String emailTemplateIdentifier, String emailComment,
			String letterName, String docType, String mailTo, String cc,
			String subject) throws IOException {
		EmailTemplate emailTemplate = sendObjectLetter(null, agenda, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, letterName, docType, mailTo, cc, subject);
		
		return emailTemplate;
	}
	
	@Override
	public EmailTemplate sendNotification(ProtocolForm protocolForm, Committee committee, Map<String, Object> attributeRawValues, User user, String emailTemplateIdentifier, String emailComment, String mailTo, String cc){
		EmailTemplate emailTemplate = notify(protocolForm, committee, attributeRawValues, user, emailTemplateIdentifier, emailComment, mailTo, cc);
		
		sendEmail(emailTemplate, attributeRawValues, user, emailComment, mailTo, cc);
		
		return emailTemplate;
	}
	
	public EmailTemplate notify(ProtocolForm protocolForm, Committee committee, Map<String, Object> attributeRawValues, User user, String emailTemplateIdentifier, String emailComment, String mailTo, String cc){
		
		//String identifier = protocolF//protocolFormStatus.getProtocolFormStatus() + "_" + committee.toString() + "_" + action;
		attributeRawValues = emailService.addInputRecipentsToRawAttributes(attributeRawValues, mailTo, cc);
		
		EmailTemplate emailTemplate = null;
		
		if (committee != null && committee.equals(Committee.IRB_REVIEWER)){
			AgendaItem agendaItem = agendaItemDao.getLatestByProtocolFormId(protocolForm.getId());
			
			emailTemplate = protocolEmailDataService.loadEmailTemplateInMeeting(emailTemplateIdentifier, protocolForm, committee, null, user, "", agendaItem.getAgenda());
		} else {
			emailTemplate = protocolEmailDataService.loadEmailTemplate(emailTemplateIdentifier, protocolForm, committee, attributeRawValues, user, emailComment);
		}
		
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

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
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

	public EmailService getEmailService() {
		return emailService;
	}
	
	@Autowired(required=true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}
	
	@Autowired(required=true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}
}
