<<<<<<< HEAD
package edu.uams.clara.scheduler.task.reminders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.scheduler.task.AbstractTask;
import edu.uams.clara.webapp.protocol.scheduler.SendAuditReminderNotificationSerivce;
import edu.uams.clara.webapp.protocol.scheduler.SendContinuingReviewerReminderService;
import edu.uams.clara.webapp.protocol.scheduler.SendEpicSecurityTeamNotificationService;
import edu.uams.clara.webapp.protocol.scheduler.SendExpirationOfApprovalLetterService;
import edu.uams.clara.webapp.protocol.scheduler.SendRevisionRequestedReminderNotificationService;

@Service
public class DailyReminderTasks extends AbstractTask {

	private final static Logger logger = LoggerFactory
			.getLogger(DailyReminderTasks.class);

	private SendAuditReminderNotificationSerivce sendAuditReminderNotificationSerivce;
	private SendRevisionRequestedReminderNotificationService sendRevisionRequestedReminderNotificationService;
	private SendEpicSecurityTeamNotificationService sendEpicSecurityTeamNotificationService;
	
	private boolean shouldRun = false;
	
	@Scheduled(cron = "0 0 5  * * ?")
	// run at 5:00 am every day
	public void sendAuditReponseReminderLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND AUDIT REPONSE REMINDER");
			sendAuditReminderNotificationSerivce.sendAuditReponseReminder();
			log(TaskEvent.FINISHED, "SEND AUDIT REPONSE REMINDER");
		} catch (Exception ex) {
			logger.error("failed to send audit response reminder: ", ex);
			log(TaskEvent.FAILED,
					"SEND AUDIT REPONSE REMINDER failed ? Check server log for exception....");
		}
	}

	private SendContinuingReviewerReminderService sendContinuingReviewerReminderService;

	@Scheduled(cron = "0 0 6  * * ?")
	// run at 6:00 am every day
	public void sendContinuingReviewReminderLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND CONTINUING REVIEW REMINDER");
			sendContinuingReviewerReminderService.sendReminderLetter();
			log(TaskEvent.FINISHED, "SEND CONTINUING REVIEW REMINDER");
		} catch (Exception ex) {
			logger.error("failed to send continuing reviewer reminder: ", ex);
			log(TaskEvent.FAILED,
					"SEND CONTINUING REVIEW REMINDER failed ? Check server log for exception....");
		}
	}

	private SendExpirationOfApprovalLetterService sendExpirationOfApprovalLetterService;

	@Scheduled(cron = "0 0 7  * * ?")
	// run at 7:00 am every day
	public void sendExpirationOfApprovalLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND EXPIRATION OF APPROVAL LETTER");
			sendExpirationOfApprovalLetterService.sendExpireLetter();
			log(TaskEvent.FINISHED, "SEND EXPIRATION OF APPROVAL LETTER");
		} catch (Exception ex) {
			logger.error("failed to expiration of approval letter: ", ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF APPROVAL failed ? Check server log for exception....");
		}
	}

	@Scheduled(cron = "0 30 7  * * ?")
	// run at 7:00 am every day
	public void sendRevisionRequestReminderNotification() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED,
					"SEND REVISION REQUEST REMINDER NOTIFICATION");
			sendRevisionRequestedReminderNotificationService
					.sendRevisionReminder();
			log(TaskEvent.FINISHED,
					"SEND REVISION REQUEST REMINDER NOTIFICATION");
		} catch (Exception ex) {
			logger.error(
					"failed to expiration of revision reminder notification: ",
					ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF Revision Reminder failed ? Check server log for exception....");
		}
	}
	
	@Scheduled(cron = "0 30 23  * * ?")
	// run at 23:30 am every day
	public void sendITSecurityNotification() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED,
					"SEND IT SECURITY NOTIFICATION");
			sendEpicSecurityTeamNotificationService.run();
			log(TaskEvent.FINISHED,
					"SEND IT SECURITY NOTIFICATION");
		} catch (Exception ex) {
			logger.error(
					"failed to expiration of send IT security notification: ",
					ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF IT security notification failed ? Check server log for exception....");
		}
	}

	public SendAuditReminderNotificationSerivce getSendAuditReminderNotificationSerivce() {
		return sendAuditReminderNotificationSerivce;
	}

	@Autowired(required = true)
	public void setSendAuditReminderNotificationSerivce(
			SendAuditReminderNotificationSerivce sendAuditReminderNotificationSerivce) {
		this.sendAuditReminderNotificationSerivce = sendAuditReminderNotificationSerivce;
	}

	public SendContinuingReviewerReminderService getSendContinuingReviewerReminderService() {
		return sendContinuingReviewerReminderService;
	}

	@Autowired(required = true)
	public void setSendContinuingReviewerReminderService(
			SendContinuingReviewerReminderService sendContinuingReviewerReminderService) {
		this.sendContinuingReviewerReminderService = sendContinuingReviewerReminderService;
	}

	public SendExpirationOfApprovalLetterService getSendExpirationOfApprovalLetterService() {
		return sendExpirationOfApprovalLetterService;
	}

	@Autowired(required = true)
	public void setSendExpirationOfApprovalLetterService(
			SendExpirationOfApprovalLetterService sendExpirationOfApprovalLetterService) {
		this.sendExpirationOfApprovalLetterService = sendExpirationOfApprovalLetterService;
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
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
=======
package edu.uams.clara.scheduler.task.reminders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.scheduler.task.AbstractTask;
import edu.uams.clara.webapp.protocol.scheduler.SendAuditReminderNotificationSerivce;
import edu.uams.clara.webapp.protocol.scheduler.SendContinuingReviewerReminderService;
import edu.uams.clara.webapp.protocol.scheduler.SendEpicSecurityTeamNotificationService;
import edu.uams.clara.webapp.protocol.scheduler.SendExpirationOfApprovalLetterService;
import edu.uams.clara.webapp.protocol.scheduler.SendRevisionRequestedReminderNotificationService;
import edu.uams.clara.webapp.protocol.scheduler.SendStudyCloseLetterService;

@Service
public class DailyReminderTasks extends AbstractTask {

	private final static Logger logger = LoggerFactory
			.getLogger(DailyReminderTasks.class);

	private SendAuditReminderNotificationSerivce sendAuditReminderNotificationSerivce;
	private SendRevisionRequestedReminderNotificationService sendRevisionRequestedReminderNotificationService;
	private SendEpicSecurityTeamNotificationService sendEpicSecurityTeamNotificationService;
	
	private boolean shouldRun = false;
	
	@Scheduled(cron = "0 0 5  * * ?")
	// run at 5:00 am every day
	public void sendAuditReponseReminderLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND AUDIT REPONSE REMINDER");
			sendAuditReminderNotificationSerivce.sendAuditReponseReminder();
			log(TaskEvent.FINISHED, "SEND AUDIT REPONSE REMINDER");
		} catch (Exception ex) {
			logger.error("failed to send audit response reminder: ", ex);
			log(TaskEvent.FAILED,
					"SEND AUDIT REPONSE REMINDER failed ? Check server log for exception....");
		}
	}

	private SendContinuingReviewerReminderService sendContinuingReviewerReminderService;

	@Scheduled(cron = "0 0 6  * * ?")
	// run at 6:00 am every day
	public void sendContinuingReviewReminderLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND CONTINUING REVIEW REMINDER");
			sendContinuingReviewerReminderService.sendReminderLetter();
			log(TaskEvent.FINISHED, "SEND CONTINUING REVIEW REMINDER");
		} catch (Exception ex) {
			logger.error("failed to send continuing reviewer reminder: ", ex);
			log(TaskEvent.FAILED,
					"SEND CONTINUING REVIEW REMINDER failed ? Check server log for exception....");
		}
	}

	private SendExpirationOfApprovalLetterService sendExpirationOfApprovalLetterService;

	//@Scheduled(cron = "0 0 7  * * ?")
	@Scheduled(cron = "0 05 6  * * ?")
	// run at 7:00 am every day
	public void sendExpirationOfApprovalLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND EXPIRATION OF APPROVAL LETTER");
			sendExpirationOfApprovalLetterService.sendExpireLetter();
			log(TaskEvent.FINISHED, "SEND EXPIRATION OF APPROVAL LETTER");
		} catch (Exception ex) {
			logger.error("failed to expiration of approval letter: ", ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF APPROVAL failed ? Check server log for exception....");
		}
	}
	
	//@Scheduled(cron = "0 10 7  * * ?")
	@Scheduled(cron = "0 10 6  * * ?")
	// run at 7:10 am every day
	public void sendExpirationOfApprovalSecondNoticeLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND EXPIRATION OF APPROVAL SECOND NOTICE LETTER");
			sendExpirationOfApprovalLetterService.sendSecondNotice();
			log(TaskEvent.FINISHED, "SEND EXPIRATION OF APPROVAL SECOND NOTICE LETTER");
		} catch (Exception ex) {
			logger.error("failed to expiration of approval second notice letter: ", ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF APPROVAL SECOND NOTICE failed ? Check server log for exception....");
		}
	}
	
	private SendStudyCloseLetterService sendStudyCloseLetterService;
	
	//@Scheduled(cron = "0 15 7  * * ?")
	@Scheduled(cron = "0 15 6  * * ?")
	// run at 7:15 am every day
	public void sendStudyCloseLetter() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED, "SEND ADMINISTRATIVELY CLOSURE LETTER");
			sendStudyCloseLetterService.autoCloseStudy();
			log(TaskEvent.FINISHED, "SEND ADMINISTRATIVELY CLOSURE LETTER");
		} catch (Exception ex) {
			logger.error("failed to send administratively closure letter: ", ex);
			log(TaskEvent.FAILED,
					"SEND ADMINISTRATIVELY CLOSURE LETTER failed ? Check server log for exception....");
		}
	}

	//@Scheduled(cron = "0 30 7  * * ?")
	@Scheduled(cron = "0 20 6  * * ?")
	// run at 7:00 am every day
	public void sendRevisionRequestReminderNotification() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED,
					"SEND REVISION REQUEST REMINDER NOTIFICATION");
			sendRevisionRequestedReminderNotificationService
					.sendRevisionReminder();
			log(TaskEvent.FINISHED,
					"SEND REVISION REQUEST REMINDER NOTIFICATION");
		} catch (Exception ex) {
			logger.error(
					"failed to expiration of revision reminder notification: ",
					ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF Revision Reminder failed ? Check server log for exception....");
		}
	}
	
	@Scheduled(cron = "0 30 23  * * ?")
	// run at 23:30 am every day
	public void sendITSecurityNotification() {
		if(!this.isShouldRun()) return;
		
		try {
			log(TaskEvent.STARTED,
					"SEND IT SECURITY NOTIFICATION");
			sendEpicSecurityTeamNotificationService.run();
			log(TaskEvent.FINISHED,
					"SEND IT SECURITY NOTIFICATION");
		} catch (Exception ex) {
			logger.error(
					"failed to expiration of send IT security notification: ",
					ex);
			log(TaskEvent.FAILED,
					"SEND EXPIRATION OF IT security notification failed ? Check server log for exception....");
		}
	}

	public SendAuditReminderNotificationSerivce getSendAuditReminderNotificationSerivce() {
		return sendAuditReminderNotificationSerivce;
	}

	@Autowired(required = true)
	public void setSendAuditReminderNotificationSerivce(
			SendAuditReminderNotificationSerivce sendAuditReminderNotificationSerivce) {
		this.sendAuditReminderNotificationSerivce = sendAuditReminderNotificationSerivce;
	}

	public SendContinuingReviewerReminderService getSendContinuingReviewerReminderService() {
		return sendContinuingReviewerReminderService;
	}

	@Autowired(required = true)
	public void setSendContinuingReviewerReminderService(
			SendContinuingReviewerReminderService sendContinuingReviewerReminderService) {
		this.sendContinuingReviewerReminderService = sendContinuingReviewerReminderService;
	}

	public SendExpirationOfApprovalLetterService getSendExpirationOfApprovalLetterService() {
		return sendExpirationOfApprovalLetterService;
	}

	@Autowired(required = true)
	public void setSendExpirationOfApprovalLetterService(
			SendExpirationOfApprovalLetterService sendExpirationOfApprovalLetterService) {
		this.sendExpirationOfApprovalLetterService = sendExpirationOfApprovalLetterService;
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
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public SendStudyCloseLetterService getSendStudyCloseLetterService() {
		return sendStudyCloseLetterService;
	}
	
	@Autowired(required = true)
	public void setSendStudyCloseLetterService(
			SendStudyCloseLetterService sendStudyCloseLetterService) {
		this.sendStudyCloseLetterService = sendStudyCloseLetterService;
	}
}
>>>>>>> claraoriginal/master
