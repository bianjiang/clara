package edu.uams.clara.webapp.protocol.scheduler;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
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
public class SendExpirationOfApprovalLetterService {
	private final static Logger logger = LoggerFactory
			.getLogger(SendExpirationOfApprovalLetterService.class);
	
	private TrackDao trackDao;
	
	private ProtocolDao protocolDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private UserDao userDao;
	
	private ProtocolEmailService protocolEmailService;
	
	private ProtocolService protocolService;
	
	private ProtocolTrackService protocolTrackService;
	
	private EmailService emailService;
	
	private XmlProcessor xmlProcessor;
	
	@Value("${fileserver.url}")
	private String fileServer;
	
	@Value("${application.host}")
	private String applicationHost;
	
	private boolean shouldRun = false;
	
	public void sendExpireLetter() {
		if(!this.isShouldRun()) return;
		
		List<Protocol> expiredProtocolLst = protocolDao.listExpiredProtocol();
		
		String achCCList = "BrackeenMargieI@uams.edu,StormentJanetS@uams.edu,HollowayAmanda@uams.edu,GrayhamKS@archildrens.org";
		
		String achWithDrugCCList = "FurgersonBillyC@uams.edu";

		if (expiredProtocolLst.size() > 0) {
			for (Protocol p : expiredProtocolLst) {
				String protocolMetaData = p.getMetaDataXml();
				
				boolean isAchStudy = protocolService.checkStudyCharacteristic(protocolMetaData).get("isAchStudy");
				
				boolean isAchStudyWithDrug = protocolService.checkStudyCharacteristic(protocolMetaData).get("isAchStudyWithDrug");
				
				boolean isInvestigatorStudy = protocolService.checkStudyCharacteristic(protocolMetaData).get("isInvestigatorStudy");
				
				protocolService.setProtocolStatus(p, ProtocolStatusEnum.EXPIRED, null, null, "Updated to Expired by System");
				
				//add exipred-date path to protocol metadata
				try{
					XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
					
					ProtocolStatus ps = protocolStatusDao.findProtocolStatusByProtocolId(p.getId());
					
					String updatedXml = xmlHandler.replaceOrAddNodeValueByPath("/protocol/original-study/expired-date", p.getMetaDataXml(),DateFormatUtil.formateDateToMDY(ps.getModified()));
					
					p.setMetaDataXml(updatedXml);
					
					protocolDao.saveOrUpdate(p);
				}catch(Exception e){
					
				}
				
				try {
					List<String> ccList = Lists.newArrayList();
					
					if (isAchStudy) {
						ccList.add(achCCList);
					}
					
					if (isAchStudyWithDrug) {
						ccList.add(achWithDrugCCList);
					}
					
					if (isInvestigatorStudy) {
						List<User> users = userDao.getUsersByUserRole(Committee.MONITORING_REGULATORY_QA.getRolePermissionIdentifier());
						
						for (User u : users) {
							ccList.add(u.getPerson().getEmail());
						}
					}
					
					String ccString = "";
					
					if (ccList != null && !ccList.isEmpty()) {
						ccString = StringUtils.join(ccList, ",");
					}
					
					EmailTemplate emailTemplate = protocolEmailService.sendProtocolLetter(p, null, null, null, "EXPIRATION_OF_APPROVAL_LETTER", "", "Expiration of Approval Letter", "Letter", null, ccString, "Expiration of Approval Letter");
					
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
					logEl.setAttribute("event-type", "SEND_EXPIRATION_OF_APPROVAL_LETTER");
					logEl.setAttribute("form-id", "0");
					logEl.setAttribute("parent-form-id", "0");
					logEl.setAttribute("form-type", "PROTOCOL");
					logEl.setAttribute("log-type", "LETTER");
					logEl.setAttribute("email-template-identifier", "EXPIRATION_OF_APPROVAL_LETTER");
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
					String emailLog = "<a target=\"_blank\" href=\"" + this.getFileServer()
							+ uploadedFile.getPath()
							+ uploadedFile.getIdentifier() + "."
							+ uploadedFile.getExtension()
							+ "\">View Letter</a>";

					String message = "System has send an Expiration of Approval Letter"+ emailLog +" <br/>The following committees/groups/individuals were notified:<br/>"+ recipents +"";

					logEl.setTextContent(message);

					logsDoc.getDocumentElement().appendChild(logEl);

					track = protocolTrackService.updateTrack(track, logsDoc);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}

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

	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required = true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
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

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
