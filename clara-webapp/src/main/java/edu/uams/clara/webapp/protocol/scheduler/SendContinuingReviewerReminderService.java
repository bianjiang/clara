package edu.uams.clara.webapp.protocol.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.service.ProtocolService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class SendContinuingReviewerReminderService {
	private TrackDao trackDao;
	
	private ProtocolDao protocolDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private ProtocolEmailService protocolEmailService;
	
	private ProtocolTrackService protocolTrackService;
	
	private ProtocolService protocolService;
	
	private EmailService emailService;
	
	private XmlProcessor xmlProcessor;
	
	@Value("${fileserver.url}")
	private String fileServer;
	
	@Value("${application.host}")
	private String applicationHost;	
	
	@Value("${fileserver.local.dir.path}")
	private String uploadDirResourcePath;
	
	private boolean shouldRun = false;

	public void sendReminderLetter(){
		if(!this.isShouldRun()) return;
		
		Map<String, List<Protocol>> toBeRemindMap = new HashMap<String, List<Protocol>>();
		
		List<Protocol> toBeExpiredInOneMonthProtocols = new ArrayList<Protocol>();
		
		List<Protocol> toBeExpiredInTwoMonthProtocols = new ArrayList<Protocol>();
		
		List<Protocol> toBeExpiredInThreeMonthProtocols = new ArrayList<Protocol>();
		
		String achCCList = "BrackeenMargieI@uams.edu,StormentJanetS@uams.edu,HollowayAmanda@uams.edu,JContorno@uams.edu";
		
		String achWithDrugCCList = "FurgersonBillyC@uams.edu";
		
		try{
			toBeExpiredInOneMonthProtocols = protocolDao.listExpiredProtocolByMonths(1);
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		try{
			toBeExpiredInTwoMonthProtocols = protocolDao.listExpiredProtocolByMonths(2);
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		try{
			toBeExpiredInThreeMonthProtocols = protocolDao.listExpiredProtocolByMonths(3);
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		if (toBeExpiredInOneMonthProtocols != null && !toBeExpiredInOneMonthProtocols.isEmpty()){
			toBeRemindMap.put("One Month", toBeExpiredInOneMonthProtocols);
		}
		
		if (toBeExpiredInTwoMonthProtocols != null && !toBeExpiredInTwoMonthProtocols.isEmpty()){
			toBeRemindMap.put("Two Months", toBeExpiredInTwoMonthProtocols);
		}
		
		if (toBeExpiredInThreeMonthProtocols != null && !toBeExpiredInThreeMonthProtocols.isEmpty()){
			toBeRemindMap.put("Three Months", toBeExpiredInThreeMonthProtocols);
		}
		
		for (Entry<String, List<Protocol>> entry : toBeRemindMap.entrySet()){
			Map<String, Object> attributeRawValues = new HashMap<String, Object>();
			
			for (Protocol p : entry.getValue()){
				String protocolMetaData = p.getMetaDataXml();
				
				ProtocolStatus latestProtocolStatus = protocolStatusDao.findProtocolStatusByProtocolId(p.getId());
				
				if (!latestProtocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.OPEN)){
					continue;
				}
				
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
					
					attributeRawValues.put("month", entry.getKey());
					
					EmailTemplate emailTemplate = protocolEmailService.sendProtocolLetter(p, null, attributeRawValues, null, "CONTINUING_REVIEW_REMINDER_LETTER", "", "Continuing Review Reminder Letter", "Letter", null, ccList, "Continuing Reviewer Reminder Letter");
					
					Date now = new Date();
					
					Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
							p.getId());

					Document logsDoc = protocolTrackService.getLogsDocument(track);

					Element logEl = logsDoc.createElement("log");
					
					String logId = UUID.randomUUID().toString();

					logEl.setAttribute("action-user-id", "0");
					logEl.setAttribute("actor", "System");
					logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
					logEl.setAttribute("date", DateFormatUtil.formateDateToMDY(now));
					logEl.setAttribute("event-type", "CONTINUING_REVIEW_REMINDER");
					logEl.setAttribute("form-id", "0");
					logEl.setAttribute("parent-form-id", "0");
					logEl.setAttribute("form-type", "PROTOCOL");
					logEl.setAttribute("log-type", "LETTER");
					logEl.setAttribute("email-template-identifier", "CONTINUING_REVIEW_REMINDER_LETTER");
					logEl.setAttribute("timestamp", String.valueOf(now.getTime()));
					logEl.setAttribute("id", logId);
					logEl.setAttribute("parent-id",logId);

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
					
					UploadedFile uploadedFile = emailTemplate.getUploadedFile();
					
					/*
					String filePath = uploadedFile.getPath();
					
					if (filePath.equals(uploadDirResourcePath)) {
						filePath = "/protocol/" + p.getId() + "/";
					}
					*/
					
					String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
							+ uploadedFile.getPath()
							+ uploadedFile.getIdentifier() + "."
							+ uploadedFile.getExtension()
							+ "\">View Letter</a>";

					String message = "System has send a Reminder of Continuing Review Letter"+ emailLog +" <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

					logEl.setTextContent(message);

					logsDoc.getDocumentElement().appendChild(logEl);

					track = protocolTrackService.updateTrack(track, logsDoc);
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

	public String getApplicationHost() {
		return applicationHost;
	}

	public void setApplicationHost(String applicationHost) {
		this.applicationHost = applicationHost;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required=true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required=true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}

	public String getUploadDirResourcePath() {
		return uploadDirResourcePath;
	}

	public void setUploadDirResourcePath(String uploadDirResourcePath) {
		this.uploadDirResourcePath = uploadDirResourcePath;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

}
