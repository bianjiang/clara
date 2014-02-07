package edu.uams.clara.webapp.protocol.scheduler;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/scheduler/SendLettersServiceTest-context.xml" })
public class SendLettersServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(SendLettersServiceTest.class);

	private SendExpirationOfApprovalLetterService sendExpireLetterService;
	private SendRevisionRequestedReminderNotificationService sendRevisionRequestedReminderNotificationService;
	private SendEpicSecurityTeamNotificationService sendEpicSecurityTeamNotificationService;
	private SendBudgetApprovedNotificationForPBService sendBudgetApprovedNotificationForPBService;

	private ProtocolDao protocolDao;

	private ProtocolEmailService protocolEmailService;
	private UserDao userDao;
	private ProtocolFormDao protocolFormDao;

	private EntityManager em;
	private EmailTemplateDao emailTemplateDao;

	//@Test
	public void testSendLetter() throws Exception {
		sendBudgetApprovedNotificationForPBService.sendBudgetApprovedNotification(protocolDao.findById(99075));

	}

	//@Test
	public void sendEmailOfForms() throws Exception {
		User currentUser = userDao.findById(19);
		ProtocolForm protocolForm = protocolFormDao.findById(16733);
		logger.debug(protocolForm.getFormType());

		//protocolEmailService.sendLetter(protocolForm, Committee.IRB_OFFICE, null, currentUser, "AUDIT_RESPONSE_FULL_BOARD_APPROVED_MINOR_MET_LETTER", "", "Audit response has been approved with minor met", "Audit response has been approved with minor met", "[{\"address\":\"GROUP_studyPI\",\"type\":\"GROUP\",\"desc\":\"Study PI and staffs\"}]", "");

		//protocolEmailService.sendLetter(protocolForm, Committee.IRB_OFFICE, null, currentUser, "ADUIT_FULL_BOARD_APPROVED_LETTER", "", "Audit response has been approved by IRB Full Board", "Audit response has been approved by IRB Full Board", "[{\"address\":\"GROUP_studyPI\",\"type\":\"GROUP\",\"desc\":\"Study PI and staffs\"}]", "");
		//protocolEmailService.sendLetter(protocolForm, Committee.IRB_OFFICE, null, currentUser, "AUDIT_FULL_BOARD_DEFERRED_WITH_MINOR_CONTINGENCIES_LETTER", "", "Audit response has been deffered with minor contingencies by IRB Full Board", "Audit response has been deffered with minor contingencies by IRB Full Board", "[{\"address\":\"GROUP_studyPI\",\"type\":\"GROUP\",\"desc\":\"Study PI and staffs\"}]", "");
		//protocolEmailService.sendLetter(protocolForm, Committee.IRB_OFFICE, null, currentUser, "AUDIT_FULL_BOARD_DEFERRED_WITH_MAJOR_CONTINGENCIES_LETTER", "", "Audit response has been deffered with major contingencies by IRB Full Board", "Audit response has been deffered with major contingencies by IRB Full Board", "[{\"address\":\"GROUP_studyPI\",\"type\":\"GROUP\",\"desc\":\"Study PI and staffs\"}]", "");
		protocolEmailService.sendLetter(protocolForm, Committee.IRB_OFFICE, null, currentUser, "INFORMATIONAL_REPORT_ACKNOWLEDGED_LETTER", "", "", "", "[{\"address\":\"GROUP_studyPI\",\"type\":\"GROUP\",\"desc\":\"Study PI and staffs\"}]", "");

	}

	@Test
	public void fixEmailTemplate() {
		String qryStr = "SELECT [identifier],[subject] ,[vm_template] FROM [clara_dev].[dbo].[email_template]";
		Query query = em.createNativeQuery(qryStr);
		List<Object[]> resultsTraining= (List<Object[]>)query.getResultList();

		List<EmailTemplate> emailTemplatePros = emailTemplateDao.findAll();
		for(int i=0;i<resultsTraining.size();i++){
			Object[] result = resultsTraining.get(i);
			String identifier = (String)result[0];
			String key =  (String)result[1]+ (String)result[2];
			for(EmailTemplate emailTemplate:emailTemplatePros){
				String key2 = emailTemplate.getSubject()+emailTemplate.getVmTemplate();
				if(key2.equals(key)){
					emailTemplate.setIdentifier(identifier);
					try{
					emailTemplateDao.saveOrUpdate(emailTemplate);
					logger.debug(identifier);
					break;
					}catch(Exception e){
						logger.debug("!!!error:   "+identifier);
						break;
					}

				}
		}

		}



	}

	public SendExpirationOfApprovalLetterService getSendExpireLetterService() {
		return sendExpireLetterService;
	}

	@Autowired(required = true)
	public void setSendExpireLetterService(SendExpirationOfApprovalLetterService sendExpireLetterService) {
		this.sendExpireLetterService = sendExpireLetterService;
	}



	public SendRevisionRequestedReminderNotificationService getSendRevisionRequestedReminderNotificationService() {
		return sendRevisionRequestedReminderNotificationService;
	}


	@Autowired(required = true)
	public void setSendRevisionRequestedReminderNotificationService(
			SendRevisionRequestedReminderNotificationService sendRevisionRequestedReminderNotificationService) {
		this.sendRevisionRequestedReminderNotificationService = sendRevisionRequestedReminderNotificationService;
	}



	public SendEpicSecurityTeamNotificationService getSendEpicSecurityTeamNotificationService() {
		return sendEpicSecurityTeamNotificationService;
	}

	@Autowired(required = true)
	public void setSendEpicSecurityTeamNotificationService(
			SendEpicSecurityTeamNotificationService sendEpicSecurityTeamNotificationService) {
		this.sendEpicSecurityTeamNotificationService = sendEpicSecurityTeamNotificationService;
	}



	public SendBudgetApprovedNotificationForPBService getSendBudgetApprovedNotificationForPBService() {
		return sendBudgetApprovedNotificationForPBService;
	}


	@Autowired(required = true)
	public void setSendBudgetApprovedNotificationForPBService(
			SendBudgetApprovedNotificationForPBService sendBudgetApprovedNotificationForPBService) {
		this.sendBudgetApprovedNotificationForPBService = sendBudgetApprovedNotificationForPBService;
	}



	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}


	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}

	@Autowired(required = true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}

	@Autowired(required = true)
	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

}
