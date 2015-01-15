package edu.uams.clara.webapp.protocol.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.service.ProtocolService;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public class SendStudyCloseLetterService {
	private ProtocolDao protocolDao;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolService protocolService;
	
	private ProtocolEmailService protocolEmailService;
	
	private ProtocolTrackService protocolTrackService;
	
	private EmailService emailService;
	
	private XmlProcessor xmlProcessor;
	
	@Value("${fileserver.url}")
	private String fileServer;
	
	private boolean shouldRun = false;
	
	/*
	private List<ProtocolFormStatusEnum> draftOrApprovedFormStatuses = Lists.newArrayList();{
		draftOrApprovedFormStatuses.add(ProtocolFormStatusEnum.DRAFT);
		draftOrApprovedFormStatuses.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		draftOrApprovedFormStatuses.add(ProtocolFormStatusEnum.IRB_APPROVED);
		draftOrApprovedFormStatuses.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		draftOrApprovedFormStatuses.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
	}
	*/
	//temporary excluded study list, need to remove the list when those study is done
	private List<Long> excludedStudyIds = Lists.newArrayList();{
		excludedStudyIds.add(201914l);
		excludedStudyIds.add(136344l);
	}
	
	public void autoCloseStudy() {
		if(!this.isShouldRun()) return;
		
		Date now = new Date();
		
		try {
			List<Protocol> expiredMoreThanOneMonthProtocolLst = protocolDao.listExpiredMoreThanOneMonthProtocol();
			
			if (expiredMoreThanOneMonthProtocolLst.size() > 0) {
				for (Protocol p : expiredMoreThanOneMonthProtocolLst) {
					/*
					try {
						ProtocolForm cr = protocolFormDao.getLatestProtocolFormByProtocolIdAndProtocolFormType(p.getId(), ProtocolFormType.CONTINUING_REVIEW);
						
						if (cr != null) {
							ProtocolFormStatus latestCrFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(cr.getId());
							
							if (!draftOrApprovedFormStatuses.contains(latestCrFormStatus.getProtocolFormStatus())) {
								continue;
							}
						}
					} catch (Exception e) {
						
					}
					*/
					
					if (excludedStudyIds.contains(p.getId())) continue;
					
					protocolService.setProtocolStatus(p, ProtocolStatusEnum.CLOSED, null, null, "Updated to Closed by System");
					
					Map<String, Object> attributeRawValues = new HashMap<String, Object>();
					attributeRawValues.put("closeReason", "The study has been expired for 30 days.");
					
					//send email
					EmailTemplate emailTemplate = protocolEmailService.sendProtocolLetter(p, null, attributeRawValues, null, "ADMINISTRATIVELY_CLOSURE_LETTER", "", "Administratively Closure Letter", "Letter", null, null, "Administratively Closure Letter");
					
					//add log
					Track track = protocolTrackService.getOrCreateTrack("PROTOCOL",
							p.getId());

					Document logsDoc = protocolTrackService.getLogsDocument(track);

					Element logEl = logsDoc.createElement("log");
					
					String logId = UUID.randomUUID().toString();
					
					logEl.setAttribute("action-user-id", "0");
					logEl.setAttribute("actor", "System");
					logEl.setAttribute("date-time", DateFormatUtil.formateDate(now));
					logEl.setAttribute("date", DateFormatUtil.formateDateToMDY(now));
					logEl.setAttribute("event-type", "ADMINISTRATIVELY_CLOSURE");
					logEl.setAttribute("form-id", "0");
					logEl.setAttribute("parent-form-id", "0");
					logEl.setAttribute("form-type", "PROTOCOL");
					logEl.setAttribute("log-type", "LETTER");
					logEl.setAttribute("email-template-identifier", "ADMINISTRATIVELY_CLOSURE_LETTER");
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
						recipents += "<b>" + emailRecipient.getDesc() + "</b>; ";
					}
					
					UploadedFile uploadedFile = emailTemplate.getUploadedFile();
					
					/*
					String filePath = uploadedFile.getPath();
					
					if (filePath.equals(uploadDirResourcePath)) {
						filePath = "/protocol/" + protocolId + "/";
					}
					*/
					
					String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
							+ uploadedFile.getPath()
							+ uploadedFile.getIdentifier() + "."
							+ uploadedFile.getExtension()
							+ "\">View Letter</a>";

					String message = "Study has been auto closed by system. An Administravely Closure Letter "+ emailLog +" has been sent. <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

					logEl.setTextContent(message);

					logsDoc.getDocumentElement().appendChild(logEl);

					track = protocolTrackService.updateTrack(track, logsDoc);
				}
			}
		} catch (Exception e) {
			
		}
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}
	
	@Autowired(required = true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}
	
	@Autowired(required = true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
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

	public String getFileServer() {
		return fileServer;
	}

	public void setFileServer(String fileServer) {
		this.fileServer = fileServer;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required = true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}
}
