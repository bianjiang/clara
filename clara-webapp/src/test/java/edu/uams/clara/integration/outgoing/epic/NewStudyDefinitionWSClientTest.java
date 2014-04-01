package edu.uams.clara.integration.outgoing.epic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.service.ProtocolService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/epic/NewStudyDefinitionWSClientTest-context.xml" })
public class NewStudyDefinitionWSClientTest {
	private final static Logger logger = LoggerFactory
			.getLogger(NewStudyDefinitionWSClientTest.class);
	
	private StudyDefinitionWSClient studyDefinitionWSClient;
	private ProtocolDao protocolDao;
	private ProtocolService	protocolService;
	
	//@Test
	public void testMessageGeneration() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String irbNumber = "201923";
		long protocolId = 201923;
		Protocol protocol = protocolDao.findById(protocolId);
		String epicTitle = "TEST CLARA EPIC INTERFACE";
		String epicSummary = "Whatever summary";
		
		Method method2 = studyDefinitionWSClient.getClass().getDeclaredMethod("getProtocolUserInfoXml", String.class);
		method2.setAccessible(true);
		String userinfo =  (String) method2.invoke(studyDefinitionWSClient, protocol.getMetaDataXml());
		Method method = studyDefinitionWSClient.getClass().getDeclaredMethod("createRetrieveProtocolDefResponseRequestPayload", String.class, String.class, String.class,String.class);
		method.setAccessible(true);
		String result = DomUtils.toString((Source) method.invoke(studyDefinitionWSClient, irbNumber, epicTitle, epicSummary,userinfo));
				logger.debug(result);
		//logger.debug(DomUtils.toString(studyDefinitionWSClient.createRetrieveProtocolDefResponseRequestPayload(irbNumber, epicTitle, epicSummary)));
	}
	
	@Test
	public void testRetrieveProtocolDefResponse() throws Exception{
		long protocolId = 138725;

		Protocol protocol = protocolDao.findById(protocolId);
		//logger.debug(protocol.getMetaDataXml());
		
		String protocolMetaData = protocol.getMetaDataXml();
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			String epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-title");
			epicTitle=epicTitle.trim();
			String epicDesc = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-desc");
			epicDesc =epicDesc.trim();
			if (epicTitle.isEmpty()) {
				epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/title");
			}
			
			if (epicDesc.isEmpty()) {
				epicDesc = protocolService.populateEpicDesc(protocolMetaData);
			}
			
			//logger.debug("title: " + title);
			studyDefinitionWSClient.retrieveProtocolDefResponse(""+protocolId, epicTitle, epicDesc, protocolMetaData);
			
			//protocolService.addPushedToEpic(protocol);
			
		} catch (Exception e) {

			logger.error("failed: " + e);
			
		}
		
		//studyDefinitionWSClient.retrieveProtocolDefResponse(irbNumber, epicTitle, epicSummary);
		
	}
	


	public StudyDefinitionWSClient getStudyDefinitionWSClient() {
		return studyDefinitionWSClient;
	}

	@Autowired(required=true)
	public void setStudyDefinitionWSClient(StudyDefinitionWSClient studyDefinitionWSClient) {
		this.studyDefinitionWSClient = studyDefinitionWSClient;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required=true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}

}
