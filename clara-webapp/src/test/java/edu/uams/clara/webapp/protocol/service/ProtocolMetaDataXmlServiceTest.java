package edu.uams.clara.webapp.protocol.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.impl.ProtocolMetaDataXmlServiceImpl;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/service/ProtocolMetaDataXmlServiceTest-context.xml" })
public class ProtocolMetaDataXmlServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolMetaDataXmlServiceTest.class);

	private ProtocolMetaDataXmlService protocolMetaDataXmlService;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormDao protocolFormDao;

	private ProtocolDao protocolDao;

	private XmlProcessor xmlProcessor;

//	@Test
//	public void testConsolidateProtocolFormXmlData()  throws IOException, SAXException,
//	XPathExpressionException {
//		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao.findById(1116);
//		logger.debug("origianl protocolFormXmlData: " + protocolFormXmlData.getXmlData());
//
//		protocolFormXmlData = protocolMetaDataXmlService.consolidateProtocolFormXmlData(protocolFormXmlData, ProtocolFormXmlDataType.PROTOCOL);
//
//		logger.debug("fina protocolFormXmlData: " + protocolFormXmlData.getXmlData());
//	}


	@Test
	public void getAllProtocolPaths(){
		ProtocolMetaDataXmlServiceImpl protocolMetaDataXmlServiceImpl = new ProtocolMetaDataXmlServiceImpl();
		List<ProtocolFormXmlDataType> types = Lists.newArrayList();
		types.add(ProtocolFormXmlDataType.CONTINUING_REVIEW);
		types.add(ProtocolFormXmlDataType.PROTOCOL);
		types.add(ProtocolFormXmlDataType.MODIFICATION);
		types.add(ProtocolFormXmlDataType.EMERGENCY_USE);
		types.add(ProtocolFormXmlDataType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
		types.add(ProtocolFormXmlDataType.AUDIT);
		types.add(ProtocolFormXmlDataType.STAFF);
		List<String> paths = Lists.newArrayList();

		for(ProtocolFormXmlDataType type :types){
			Map<String, String> ProtocolFromToProtocolMetaDataMapping  = protocolMetaDataXmlServiceImpl
					.getProtocolFromToProtocolMetaDataMapping(type);
			for (Object key : ProtocolFromToProtocolMetaDataMapping.keySet()) {
				if(paths.contains(ProtocolFromToProtocolMetaDataMapping.get(key))){
					continue;
				}

				paths.add(ProtocolFromToProtocolMetaDataMapping.get(key));
				logger.debug(ProtocolFromToProtocolMetaDataMapping.get(key));
			}
		}

	}


	//@Test
	public void testParseExtraDataXml() throws Exception{
		String extraDataXml = "<committee-review><committee type=\"MONITORING_REGULATORY_QA\"><extra-content><ind>457821</ind><ide>6971254</ide></extra-content><actor>MONITORING_REGULATORY_QA</actor><action>APPROVE</action><letter/></committee></committee-review>";
		ProtocolForm protocolForm = protocolFormDao.findById(1349);

		//String output = protocolMetaDataXmlService.parseExtraDataXml(protocolForm, extraDataXml);

		//logger.debug("$$$$$$$$$$$ output: " + output);
	}

	//@Test
	public void testconsolidateProtocolFormXmlData() throws Exception{
		//String extraDataXml = "<committee-review><committee type=\"MONITORING_REGULATORY_QA\"><extra-content><ind>457821</ind><ide>6971254</ide></extra-content><actor>MONITORING_REGULATORY_QA</actor><action>APPROVE</action><letter/></committee></committee-review>";
		ProtocolFormXmlData protocolFormXmlData = protocolFormXmlDataDao.findById(12028l);

		ProtocolFormXmlData finalProtocolFormXmlData = protocolMetaDataXmlService.consolidateProtocolFormXmlData(protocolFormXmlData, ProtocolFormXmlDataType.PROTOCOL);

		logger.debug("finalXml: " + finalProtocolFormXmlData.getXmlData());

		//String output = protocolMetaDataXmlService.parseExtraDataXml(protocolForm, extraDataXml);

		//logger.debug("$$$$$$$$$$$ output: " + output);
	}

	private Map<String, String> protocolFormXPathPairMap = new HashMap<String, String>();
	{
		// newSubmissionXPathPairs.put("/protocol/submission-type",
		// "/protocol/submission-type");
		protocolFormXPathPairMap.put("/protocol/misc/epic-title", "/protocol/epic/epic-title");
		protocolFormXPathPairMap.put("/protocol/misc/epic-desc",
				"/protocol/epic/epic-desc");
	}

	//@Test
	/*
	public void copyFromOldPathToNewPath(){
		List<ProtocolFormXmlData> lst = protocolDao.getPath();

		logger.debug("$$$$$$$$$ size:" + lst.size());

		for (ProtocolFormXmlData pfxd : lst){
			String xmlData = pfxd.getXmlData();
			try{
				List<String> titleValues = xmlProcessor.listElementStringValuesByPath("/protocol/misc/epic-title", pfxd.getXmlData());
				List<String> descValues = xmlProcessor.listElementStringValuesByPath("/protocol/misc/epic-desc", pfxd.getXmlData());

				String title = (titleValues != null && !titleValues.isEmpty())?titleValues.get(0):"";
				String desc = (descValues != null && !descValues.isEmpty())?descValues.get(0):"";

				xmlData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/epic/epic-title", xmlData, title);
				xmlData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/epic/epic-desc", xmlData, desc);

				pfxd.setXmlData(xmlData);
				protocolFormXmlDataDao.saveOrUpdate(pfxd);
			} catch (Exception e){
				e.printStackTrace();
			}



		}
	}*/

	public ProtocolMetaDataXmlService getProtocolMetaDataXmlService() {
		return protocolMetaDataXmlService;
	}

	@Autowired(required=true)
	public void setProtocolMetaDataXmlService(ProtocolMetaDataXmlService protocolMetaDataXmlService) {
		this.protocolMetaDataXmlService = protocolMetaDataXmlService;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}