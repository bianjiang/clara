package edu.uams.clara.integration.outgoing.clarabasic;

import java.io.IOException;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/clarabasic/ClaraBasicInfoTest-context.xml" })
public class ClaraBasicInfoTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ClaraBasicInfoTest.class);

	private XmlProcessor xmlProcessor;
	private XmlHandler xmlHandler;
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	@Test
	public void generateBasicInfo() throws XPathExpressionException, SAXException, IOException{
		Protocol p = protocolDao.findById(201959);
		ProtocolFormXmlData pfxd = protocolFormXmlDataDao.findById(11);
		String protocolXml = p.getMetaDataXml();
		String phase= xmlHandler.getSingleStringValueByXPath(protocolXml, "/protocol/phases/phase");
		String typeofStudy =xmlProcessor.listElementStringValuesByPath("/protocol/study-type", protocolXml).get(0);

		String budget = pfxd.getXmlData();
		Set<String> pathSet = Sets.newHashSet();
		pathSet.add("//epochs/epoch");
		int epochNum = xmlProcessor.listDomElementsByPaths(pathSet, budget).size();
		pathSet.clear();
		pathSet.add("//cycles/cycle/visits/visit");
		int visitNum = xmlProcessor.listDomElementsByPaths(pathSet, budget).size();
		logger.debug(phase+" "+typeofStudy+" "+epochNum+" "+visitNum);
	}


	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}


	public XmlHandler getXmlHandler() {
		return xmlHandler;
	}

	@Autowired(required = true)
	public void setXmlHandler(XmlHandler xmlHandler) {
		this.xmlHandler = xmlHandler;
	}
}
