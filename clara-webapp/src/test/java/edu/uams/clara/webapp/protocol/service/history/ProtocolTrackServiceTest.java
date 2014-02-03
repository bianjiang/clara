package edu.uams.clara.webapp.protocol.service.history;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/service/history/ProtocolTrackServiceTest-context.xml" })
public class ProtocolTrackServiceTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolTrackServiceTest.class);

	private ProtocolTrackService protocolTrackService;

	private UserDao userDao;

	private ProtocolFormDao protocolFormDao;

	private XmlProcessor xmlProcess;

	//@Test
	public void testFillMessage(){
		String message = "A new protocol# {PROTOCOL_IDENTIFIER} has been created by {USER_WITH_EMAIL_LINK}.";

		ProtocolForm protocolFrom = protocolFormDao.findById(589l);
		Committee committee = Committee.PI;
		User user = userDao.findById(1l);

		//Map<String, String> attributeValues = protocolTrackService.getAttributeValues(protocolFrom, committee, user, "");
		//message = protocolTrackService.fillMessage(message, attributeValues);

		logger.debug("message: " + message);
	}

	//@Test
	public void generateLogElement() throws IOException, SAXException{
		String logsTemplate = "<logs><log event-type=\"NEW_PROTOCOL_CREATED\" form-type=\"{FORM_TYPE}\" form-id=\"{FORM_ID}\" action-user-id=\"{USER_ID}\" actor=\"{USER_NAME}\" timestamp=\"{NOW_TIMESTAMP}\" date-time=\"{NOW_DATETIME}\">A new protocol# {PROTOCOL_IDENTIFIER} has been created by {USER_WITH_EMAIL_LINK}.</log></logs>";

		ProtocolForm protocolFrom = protocolFormDao.findById(589l);
		Committee committee = Committee.PI;
		User user = userDao.findById(1l);

		//Map<String, String> attributeValues = protocolTrackService.getAttributeValues(protocolFrom, committee, user, null);

		//Track track = protocolTrackService.createTrack("PROTOCOL", null, protocolFrom.getProtocol().getId());

		//Document logsDoc = protocolTrackService.getLogsDocument(track);


		Document logsTemplateDoc = xmlProcess.loadXmlStringToDOM(logsTemplate);

		NodeList logs = logsTemplateDoc.getDocumentElement()
				.getElementsByTagName("log");

		for (int i = 0; i < logs.getLength(); i++) {
			Map<String, String> attributes = new HashMap<String, String>();

			Element logEl = (Element) logs.item(i);

			NamedNodeMap attributesList = logEl.getAttributes();

			for (int j = 0; j < attributesList.getLength(); j++) {
				attributes.put(attributesList.item(j).getNodeName(),
						attributesList.item(j).getTextContent());
			}

			//attributes = protocolTrackService.fillAttributeValue(attributes, attributeValues);

			//String logTextContent = protocolTrackService.fillMessage(logEl.getTextContent(), attributeValues);

			//logsDoc = protocolTrackService.appendLogToLogsDoc(logsDoc, user, logTextContent, attributes);

		}

		//logger.debug("final logs:" + DomUtils.elementToString(logsDoc));

	}

	@Test
	public void testLogStatusChange() throws IOException, SAXException{
		String logsTemplate = "<logs><log event-type=\"NEW_PROTOCOL_CREATED\" form-type=\"{FORM_TYPE}\" form-id=\"{FORM_ID}\" action-user-id=\"{USER_ID}\" actor=\"{USER_NAME}\" timestamp=\"{NOW_TIMESTAMP}\" date-time=\"{NOW_DATETIME}\">A new protocol# {PROTOCOL_IDENTIFIER} has been created by {USER_WITH_EMAIL_LINK}.</log></logs>";

		ProtocolForm protocolFrom = protocolFormDao.findById(589l);
		Committee committee = Committee.PI;
		User user = userDao.findById(1l);

		for(int i = 0; i< 1000; i++){
			//protocolTrackService.logStatusChange(protocolFrom, committee, user, null, logsTemplate);
		}

	}


	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}

	@Autowired(required = true)
	public void setProtocolTrackService(ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public XmlProcessor getXmlProcess() {
		return xmlProcess;
	}

	@Autowired(required = true)
	public void setXmlProcess(XmlProcessor xmlProcess) {
		this.xmlProcess = xmlProcess;
	}

}
