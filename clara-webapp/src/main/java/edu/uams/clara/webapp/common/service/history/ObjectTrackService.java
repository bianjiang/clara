package edu.uams.clara.webapp.common.service.history;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.service.EmailService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public abstract class ObjectTrackService<T> {
	private final static Logger logger = LoggerFactory
			.getLogger(ObjectTrackService.class);

	@SuppressWarnings("unchecked")
	public ObjectTrackService() {
		this.refObjectClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	private EmailService emailService;
	
	private String logTemplateXmlFilePath;
	
	private TrackDao trackDao;

	private XmlProcessor xmlProcessor;

	private Class<T> refObjectClass;
	
	private String fileServer;
	
	@Value("${fileserver.remote.dir.path}")
	private String fileRemoteDirPath;

	protected Track createTrack(String type, String xmlData, long refObjectId) {
		Date now = new Date();
		Track track = new Track();
		track.setModified(now);
		track.setType(type);
		track.setXmlData(xmlData);
		track.setRefObjectClass(refObjectClass);
		track.setRefObjectId(refObjectId);

		// track = trackDao.saveOrUpdate(track); //add track into the hibernate
		// container...
		return track;
	}
	
	public Track getOrCreateTrack(String type, long refObjectId) throws IOException, SAXException{
		Track track = trackDao.getTrackByTypeAndRefObjectClassAndId(type,
				refObjectClass, refObjectId);
		
		if (track == null) {
			track = createTrack(type, null, refObjectId);
			track.setXmlData(DomUtils.elementToString(getLogsDocument(track)));
		}
		
		track = trackDao.saveOrUpdate(track); //add track into the hibernate
		
		return track;		
	}

	public Document getLogsDocument(Track track) throws IOException, SAXException {
		
		String xmlData = track.getXmlData();

		Document logsDoc = null;

		if (xmlData == null || xmlData.isEmpty()) {
			logsDoc = xmlProcessor.newDocument();
			Element logsRoot = logsDoc.createElement("logs");
			logsRoot.setAttribute("object-type",
					this.refObjectClass.getSimpleName());
			logsRoot.setAttribute("object-id", "" + track.getRefObjectId());
			logsDoc.appendChild(logsRoot);

		} else {
			logsDoc = xmlProcessor.loadXmlStringToDOM(track.getXmlData());
		}
		
		return logsDoc;
	}

	protected Document appendLogToLogsDoc(Document logsDoc, User user, 
			String logTextContent, Map<String, String> attributes) {
		Element logEl = logsDoc.createElement("log");

		for (Entry<String, String> attribute : attributes.entrySet()) {
			logEl.setAttribute(attribute.getKey(), attribute.getValue());
		}
		
		String logId = UUID.randomUUID().toString();
		
		logEl.setAttribute("id", logId);
		
		logEl.setAttribute("parent-id", logId);
		
		logEl.setTextContent(logTextContent);

		logsDoc.getDocumentElement().appendChild(logEl);
		
		return logsDoc;
	}
	
	public Track updateTrack(Track track, Document logsDoc){
		track.setXmlData(DomUtils.elementToString(logsDoc));

		track = trackDao.saveOrUpdate(track);
		return track;
	}

	public Track appendLog(String type, long refObjectId, User user, 
			String message, Map<String, String> attributes) throws IOException,
			SAXException {
		
		Track track = getOrCreateTrack(type, refObjectId);
		
		Document logsDoc = getLogsDocument(track);	
		
		logsDoc = appendLogToLogsDoc(logsDoc, user, message, attributes);

		track  = updateTrack(track, logsDoc);

		return track;
	}
	
	
	protected Map<String, String> fillAttributeValue(
			Map<String, String> attributes, Map<String, String> attributeValues) {

		for (Entry<String, String> attribute : attributes.entrySet()) {
			if (attributeValues.containsKey(attribute.getValue())) {
				attribute.setValue(attributeValues.get(attribute.getValue()));
			}
		}

		return attributes; 
	}
	
	/***
	 * replace placeholders with real values, @TODO might need a more efficient implementations...
	 * @param attributeValues
	 * @return
	 */
	protected String fillMessage(String message, Map<String, String> attributeValues){		
		for (Entry<String, String> attributeValue : attributeValues.entrySet()) {
			message = message.replaceAll(attributeValue.getKey().replace("{", "\\{").replace("}", "\\}"), attributeValue.getValue());
		}
		
		return message;		
	}
	
	public TrackDao getTrackDao() {
		return trackDao;
	}

	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public Class<T> getRefObjectClass() {
		return refObjectClass;
	}

	public void setRefObjectClass(Class<T> refObjectClass) {
		this.refObjectClass = refObjectClass;
	}

	public String getLogTemplateXmlFilePath() {
		return logTemplateXmlFilePath;
	}

	public void setLogTemplateXmlFilePath(String logTemplateXmlFilePath) {
		this.logTemplateXmlFilePath = logTemplateXmlFilePath;
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

	public String getFileRemoteDirPath() {
		return fileRemoteDirPath;
	}

	public void setFileRemoteDirPath(String fileRemoteDirPath) {
		this.fileRemoteDirPath = fileRemoteDirPath;
	}

}
