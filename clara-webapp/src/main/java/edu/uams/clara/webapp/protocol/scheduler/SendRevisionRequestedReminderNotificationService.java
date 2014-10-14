package edu.uams.clara.webapp.protocol.scheduler;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class SendRevisionRequestedReminderNotificationService {
	private TrackDao trackDao;

	private ProtocolDao protocolDao;

	private ProtocolFormDao protocolFormDao;

	private ProtocolEmailService protocolEmailService;

	private ProtocolTrackService protocolTrackService;

	private EmailService emailService;

	private XmlProcessor xmlProcessor;

	private EntityManager em;

	@Value("${application.host}")
	private String applicationHost;
	
	private boolean shouldRun = false;

	public void sendRevisionReminder() {
		if(!this.isShouldRun()) return;
		
		String qry = "select protocol_form_id from protocol_form_status where protocol_form_id in (SELECT max (id) FROM protocol_form  where  retired  =0 group by parent_id) and protocol_form_status in ('UNDER_REVISION','REVISION_REQUESTED') and caused_by_committee in('BUDGET_MANAGER','BUDGET_REVIEW','COVERAGE_REVIEW','DEPARTMENT_CHAIR','COLLEGE_DEAN','HOSPITAL_SERVICES','GATEKEEPER') and id in (select max(id) from protocol_form_status where protocol_form_id in (SELECT max (id) FROM protocol_form  where  retired  =0 group by parent_id)) and retired =0 and  DATEDIFF(day, 0,GETDATE()-modified )>7";
		Query query = em.createNativeQuery(qry);
		List<BigInteger> formIds = Lists.newArrayList();
		try {
			formIds = (List<BigInteger>) query.getResultList();
		} catch (Exception e) {
			// do nothing, if no protocol_form exist
		}

		if (formIds != null && !formIds.isEmpty()) {
			for (BigInteger formId : formIds) {

				ProtocolForm pf = protocolFormDao.findById(formId.longValue());
				try {

					EmailTemplate emailTemplate = protocolEmailService
							.sendNotification(pf, null, null, null,
									"REVISION_REQUESTED_REMINDER_NOTIFICATION",
									"", null, null);

					Date now = new Date();

					Track track = protocolTrackService.getOrCreateTrack(
							"PROTOCOL", pf.getProtocol().getId());

					Document logsDoc = protocolTrackService
							.getLogsDocument(track);

					Element logEl = logsDoc.createElement("log");

					String logId = UUID.randomUUID().toString();

					logEl.setAttribute("action-user-id", "0");
					logEl.setAttribute("actor", "System");
					logEl.setAttribute("date-time",
							DateFormatUtil.formateDate(now));
					logEl.setAttribute("date",
							DateFormatUtil.formateDateToMDY(now));
					logEl.setAttribute("event-type",
							"REVISION_REQUESTED_REMINDER");
					logEl.setAttribute("form-id", "0");
					logEl.setAttribute("parent-form-id", "0");
					logEl.setAttribute("form-type", "PROTOCOL");
					logEl.setAttribute("log-type", "NOTIFICATION");
					logEl.setAttribute("email-template-identifier",
							"REVISION_REQUESTED_REMINDER_NOTIFICATION");
					logEl.setAttribute("timestamp",
							String.valueOf(now.getTime()));
					logEl.setAttribute("id", logId);
					logEl.setAttribute("parent-id", logId);

					List<EmailRecipient> emailRecipients = emailService
							.getEmailRecipients((emailTemplate
									.getRealRecipient() != null) ? emailTemplate
									.getRealRecipient() : emailTemplate.getTo());

					String recipents = "";
					for (EmailRecipient emailRecipient : emailRecipients) {
						// emailRecipient.getType().equals(EmailRecipient.RecipientType.)
						recipents += "<b>" + emailRecipient.getDesc()
								+ "</b>; ";
					}

					String message = "System has send a Reminder of Revision Requested Notification. <br/>The following committees/groups/individuals were notified:<br/>"
							+ recipents + "";

					logEl.setTextContent(message);

					logsDoc.getDocumentElement().appendChild(logEl);

					track = protocolTrackService.updateTrack(track, logsDoc);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

	}

	@Value("${fileserver.remote.dir.path}")
	private String fileRemoteDirPath;

	public TrackDao getTrackDao() {
		return trackDao;
	}

	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
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
	public void setProtocolEmailService(
			ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}

	@Autowired(required = true)
	public void setProtocolTrackService(
			ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}

	public EmailService getEmailService() {
		return emailService;
	}

	@Autowired(required = true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public String getFileRemoteDirPath() {
		return fileRemoteDirPath;
	}

	public void setFileRemoteDirPath(String fileRemoteDirPath) {
		this.fileRemoteDirPath = fileRemoteDirPath;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public String getApplicationHost() {
		return applicationHost;
	}

	public void setApplicationHost(String applicationHost) {
		this.applicationHost = applicationHost;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

}
