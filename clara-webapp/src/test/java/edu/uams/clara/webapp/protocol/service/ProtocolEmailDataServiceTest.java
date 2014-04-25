package edu.uams.clara.webapp.protocol.service;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.scheduler.SendContinuingReviewerReminderService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailDataService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
//import edu.uams.clara.webapp.protocol.service.email.impl.ProtocolEmailDataServiceImpl;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/service/ProtocolEmailDataServiceTest-context.xml" })
public class ProtocolEmailDataServiceTest {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolEmailDataServiceTest.class);

	private XmlProcessor xmlProcessor;
	
	private UserDao userDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolDao protocolDao;
	
	private AgendaDao agendaDao;
	
	private ProtocolEmailDataService protocolEmailDataService;
	
	private ProtocolEmailService protocolEmailService;
	
	private SendContinuingReviewerReminderService crService;
	
	//private ProtocolEmailDataServiceImpl protocolEmailDataServiceImpl;
	
	//@Test
	public void testGetRealMailToList() throws XPathExpressionException, IOException, SAXException{
		ProtocolForm protocolForm = protocolFormDao.findById(1286);
		
		//List<String> testLst = protocolEmailDataServiceImpl.getRealMailToList(protocolForm);
		
		//logger.debug("@@@@@@@@@@@@@@@@ " + testLst.toString());
	}
	
	@Test
	public void sendEmail() throws Exception {
		//Agenda agenda = agendaDao.findById(53l);
		User currentUser = userDao.findById(68l);
		
		ProtocolForm protocolForm = protocolFormDao.findById(11820l);
		
		Protocol protocol = protocolDao.findById(202028l);
		
		//Protocol p = protocolDao.findById(134922l);
		//logger.debug("Send agenda approval letter to chair ...");
		// EmailTemplate emailTemplate = protocolEmailDataService.loadObjectEmailTemplate("AGENDA_APPROVED_LETTER", null, agenda, null, null, currentUser, "");
		//protocolEmailService.sendAgendaLetter(agenda, null, null, currentUser, emailTemplate.getIdentifier(), "", "Agenda Approved Letter", "Letter", emailTemplate.getTo(), emailTemplate.getCc(), emailTemplate.getRealSubject());
		//protocolEmailService.sendLetter(protocolForm, Committee.IRB_PREREVIEW, null, currentUser, "STAFF_ONLY_MODIFICATION_ACKNOWLEDGED_LETTER", "", "Staff Only Modification Acknowledged Letter", "Staff Only Modification Acknowledged Letter", "[{\"address\":\"GROUP_studyPI\",\"type\":\"GROUP\",\"desc\":\"Study PI and Staffs\"}]", "");
		//protocolEmailService.sendNotification(protocolForm, Committee.IRB_REVIEWER, null, currentUser, "NEW_SUBMISSION_FULL_BOARD_APPROVE_TO_PBS", "", "[{\"address\":\"INDIVIDUAL_BeasleyJanetA@uams.edu\",\"type\":\"INDIVIDUAL\",\"desc\":\"Janet Beasley\"},{\"address\":\"INDIVIDUAL_CooperSaundraG@uams.edu\",\"type\":\"INDIVIDUAL\",\"desc\":\"Saundra Cooper\"}]", "");
		//protocolEmailService.sendProtocolLetter(p, null, null, null, "EXPIRATION_OF_APPROVAL_LETTER", "", "Expiration of Approval Letter", "Letter", null, "", "Expiration of Approval Letter");
		protocolEmailService.sendProtocolNotification(protocol, null, null,
				null, "BUDGET_APPROVED_NOTIFICATION_FOR_HB", "", null, null,
				null, "", null);
	}
	
	//@Test
	public void sendExpiringCRReminderEmail() throws Exception {
		crService.sendReminderLetter();
	}

	public XmlProcessor getXmlProcessor(){
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}
	
	@Autowired(required=true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public ProtocolEmailDataService getProtocolEmailDataService() {
		return protocolEmailDataService;
	}
	
	@Autowired(required=true)
	public void setProtocolEmailDataService(ProtocolEmailDataService protocolEmailDataService) {
		this.protocolEmailDataService = protocolEmailDataService;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}
	
	@Autowired(required=true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public SendContinuingReviewerReminderService getCrService() {
		return crService;
	}
	
	@Autowired(required=true)
	public void setCrService(SendContinuingReviewerReminderService crService) {
		this.crService = crService;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	//public ProtocolEmailDataServiceImpl getProtocolEmailDataServiceImpl() {
	//	return protocolEmailDataServiceImpl;
	//}
	
	//@Autowired(required=true)
	//public void setProtocolEmailDataServiceImpl(
	//		ProtocolEmailDataServiceImpl protocolEmailDataServiceImpl) {
	//	this.protocolEmailDataServiceImpl = protocolEmailDataServiceImpl;
	//}

}
