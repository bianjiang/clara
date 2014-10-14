package edu.uams.clara.webapp.protocol.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.service.ProtocolService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class SendAuditReminderNotificationSerivce {
	private TrackDao trackDao;
	
	private ProtocolDao protocolDao;
	
	private ProtocolEmailService protocolEmailService;
	
	private ProtocolTrackService protocolTrackService;
	
	private ProtocolService protocolService;
	
	private EmailService emailService;
	
	private XmlProcessor xmlProcessor;
	
	@Value("${fileserver.url}")
	private String fileServer;
	
	@Value("${fileserver.remote.dir.path}")
	private String fileRemoteDirPath;
	
	private boolean shouldRun = false;

	public void sendAuditReponseReminder() {
		if(!this.isShouldRun()) return;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		
		String oneWeekBeforeCurrentDate = DateFormatUtil.formateDateToMDY(cal.getTime());
		
		List<Track> tracks = trackDao.findAllByEmailTypeAndDate("RECEIPT_OF_AUDIT_REPORT_LETTER", oneWeekBeforeCurrentDate);
		
		String achCCList = "BrackeenMargieI@uams.edu,StormentJanetS@uams.edu,HollowayAmanda@uams.edu";
		
		String achWithDrugCCList = "FurgersonBillyC@uams.edu";
		
		if (tracks != null && !tracks.isEmpty()){
			for (Track track : tracks){
				Protocol protocol = protocolDao.findById(track.getRefObjectId());
				
				String protocolMetaData = protocol.getMetaDataXml();
				
				boolean isAchStudy = protocolService.checkStudyCharacteristic(protocolMetaData).get("isAchStudy");
				
				boolean isAchStudyWithDrug = protocolService.checkStudyCharacteristic(protocolMetaData).get("isAchStudyWithDrug");
				
				try{
					String ccList = "";
					
					if (isAchStudy) {
						ccList = achCCList;
					}
					
					if (isAchStudyWithDrug) {
						ccList = achCCList + "," + achWithDrugCCList;
					}
					
					EmailTemplate emailTemplate = protocolEmailService.sendProtocolLetter(protocol, null, null, null, "AUDIT_RESPONSE_REMINDER_LETTER", "", "Audit Reponse Reminder", "Letter", null, ccList, "Audit Response Reminder");
					
					Date now = new Date();
					
					Document trackDoc = xmlProcessor.loadXmlStringToDOM(track.getXmlData());
					
					XPath xpath = xmlProcessor.getXPathInstance();
					
					Element receiptOfAduitEl = (Element) xpath.evaluate("//log[@email-template-identifier=\"RECEIPT_OF_AUDIT_REPORT_LETTER\" and @date=\""+ oneWeekBeforeCurrentDate +"\"]",
							trackDoc, XPathConstants.NODE);
					
					Track reminderTrack = protocolTrackService.getOrCreateTrack("PROTOCOL",
							track.getRefObjectId());

					Document logsDoc = protocolTrackService.getLogsDocument(reminderTrack);

					Element logEl = logsDoc.createElement("log");
					
					String logId = UUID.randomUUID().toString();

					logEl.setAttribute("action-user-id", "0");
					logEl.setAttribute("actor", "System");
					logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
					logEl.setAttribute("date", DateFormatUtil.formateDateToMDY(now));
					logEl.setAttribute("event-type", "SEND_AUDIT_RESPONSE_REMINDER_LETTER");
					logEl.setAttribute("form-id", "0");
					logEl.setAttribute("parent-form-id", "0");
					logEl.setAttribute("form-type", "PROTOCOL");
					logEl.setAttribute("log-type", "LETTER");
					logEl.setAttribute("email-template-identifier", "AUDIT_RESPONSE_REMINDER_LETTER");
					logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
					logEl.setAttribute("id", logId);
					logEl.setAttribute("parent-id", receiptOfAduitEl.getAttribute("parent-id"));
					
					List<EmailRecipient> emailRecipients = emailService
							.getEmailRecipients(
									(emailTemplate.getRealRecipient() != null) ? emailTemplate
											.getRealRecipient() : emailTemplate
											.getTo());

					String recipents = "";
					for (EmailRecipient emailRecipient : emailRecipients) {
						// emailRecipient.getType().equals(EmailRecipient.RecipientType.)
						recipents += "<b>" + emailRecipient.getDesc() + "</b>; ";
					}
					
					/*
					String remotePath = "";
					
					if (getFileRemoteDirPath().contains("/training")){
						remotePath = "/training";
					} else if (getFileRemoteDirPath().contains("/dev")){
						remotePath = "/dev";
					}
					*/
					
					UploadedFile uploadedFile = emailTemplate.getUploadedFile();
					String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
							+ uploadedFile.getPath()
							+ uploadedFile.getIdentifier() + "."
							+ uploadedFile.getExtension()
							+ "\">View Letter</a>";

					String message = "System has send a Reminder of Audit Response Letter"+ emailLog +" <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

					logEl.setTextContent(message);

					logsDoc.getDocumentElement().appendChild(logEl);

					reminderTrack = protocolTrackService.updateTrack(reminderTrack, logsDoc);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}
	
	@Autowired(required=true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}
	
	@Autowired(required=true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required=true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public EmailService getEmailService() {
		return emailService;
	}
	
	@Autowired(required=true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public String getFileServer() {
		return fileServer;
	}

	public void setFileServer(String fileServer) {
		this.fileServer = fileServer;
	}

	public String getFileRemoteDirPath() {
		return fileRemoteDirPath;
	}

	public void setFileRemoteDirPath(String fileRemoteDirPath) {
		this.fileRemoteDirPath = fileRemoteDirPath;
	}

	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required=true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
