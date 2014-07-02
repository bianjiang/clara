package edu.uams.clara.integration.outgoing.epic;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

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

import au.com.bytecode.opencsv.CSVWriter;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.service.ProtocolService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/epic/StudyDefinitionWSClientTest-context.xml" })
public class StudyDefinitionWSClientTest {

	private final static Logger logger = LoggerFactory
			.getLogger(StudyDefinitionWSClientTest.class);

	private ProtocolDao protocolDao;
	
	private StudyDefinitionWSClient studyDefinitionWSClient;
	
	private ProtocolService protocolService;
	
	//@Test
	public void testGenerateMessage() throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String irbNumber = "99075";
		long protocolId = 99075;
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
		long protocolId = 203136l;

		Protocol protocol = protocolDao.findById(protocolId);
		
		protocolService.pushToEpic(protocol);
		//logger.debug(protocol.getMetaDataXml());
		
		/*
		String protocolMetaData = protocol.getMetaDataXml();
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			String epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-title");
			String epicDesc = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-desc");
			
			if (epicTitle.isEmpty()) {
				epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/title");
			}
			
			if (epicDesc.isEmpty()) {
				epicDesc = protocolService.populateEpicDesc(protocolMetaData);
			}
			
			//logger.debug("title: " + title);
			studyDefinitionWSClient.retrieveProtocolDefResponse(""+protocolId, epicTitle, epicDesc, protocolMetaData);
			
			protocolService.addPushedToEpic(protocol);
			
			//protocolService.addPushedToEpic(protocol);
			
		} catch (Exception e) {

			logger.error("failed: " + e);
			
		}
		*/
		
		//studyDefinitionWSClient.retrieveProtocolDefResponse(irbNumber, epicTitle, epicSummary);
		
	}
	
	//@Test
	public void bulkLoadToEpic() throws Exception {
		List<Protocol> protocolList = protocolDao.listOpenNotPushedToEpicProtocol();
		
		logger.debug("total size: " + protocolList.size());
		
		CSVWriter successWriter = new CSVWriter(new FileWriter("C:\\Data\\epicpushnewsuccessed.csv"));
		
		CSVWriter failedWriter = new CSVWriter(new FileWriter("C:\\Data\\epicpushnewfailed.csv"));
		
		int failedCount = 0;
		
		for (Protocol p : protocolList){
			//String protocolMetaData = p.getMetaDataXml();
			
			//protocolService.pushToEpic(p);
			/*
			try {
				XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
				String epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-title");
				String epicDesc = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/epic/epic-desc");
				
				if (epicTitle.isEmpty()) {
					epicTitle = xmlHandler.getSingleStringValueByXPath(protocolMetaData, "/protocol/title");
				}
				
				if (epicDesc.isEmpty()) {
					epicDesc = protocolService.populateEpicDesc(protocolMetaData);
				}
				
				//logger.debug("title: " + title);
				studyDefinitionWSClient.retrieveProtocolDefResponse(""+p.getId(), epicTitle, epicDesc, protocolMetaData);
				
				protocolService.addPushedToEpic(p);
				
				String[] successEntry = {String.valueOf(p.getId()), epicTitle, epicDesc};
				
				successWriter.writeNext(successEntry);
				
			} catch (Exception e) {

				logger.error("failed: " + e);
				
				String[] entry = {String.valueOf(p.getId())};
				
				failedWriter.writeNext(entry);
				
				failedCount++;
			}
			*/
		}
		
		successWriter.close();
		failedWriter.close();
		
		logger.debug("Finished! Failed: " + failedCount);
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public StudyDefinitionWSClient getStudyDefinitionWSClient() {
		return studyDefinitionWSClient;
	}

	@Autowired(required=true)
	public void setStudyDefinitionWSClient(StudyDefinitionWSClient studyDefinitionWSClient) {
		this.studyDefinitionWSClient = studyDefinitionWSClient;
	}
	
	public ProtocolService getProtocolService() {
		return protocolService;
	}
	
	@Autowired(required=true)
	public void setProtocolService(ProtocolService protocolService) {
		this.protocolService = protocolService;
	}
}
