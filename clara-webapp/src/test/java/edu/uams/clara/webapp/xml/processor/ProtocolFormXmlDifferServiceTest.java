package edu.uams.clara.webapp.xml.processor;


import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.web.ajax.ProtocolDashboardAjaxControllerTest;
import edu.uams.clara.webapp.xml.processor.impl.ProtocolFormXmlDifferServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/xml/processor/ProtocolFormXmlDifferServiceTest-context.xml" })

public class ProtocolFormXmlDifferServiceTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolDashboardAjaxControllerTest.class);
	private ProtocolFormXmlDifferServiceImpl protocolFormXmlDifferServiceImpl;
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	@Test
	public void protocolFormXmlDiffertest() throws ParserConfigurationException {
		ProtocolFormXmlData oldProtocolFormXmlData = getProtocolFormXmlDataDao().findById(15090);
		ProtocolFormXmlData newProtocolFormXmlData = protocolFormXmlDataDao
				.findById(17743);

		String oldProtocolFormXm = oldProtocolFormXmlData.getXmlData();
		String newProtocolFormXm = newProtocolFormXmlData.getXmlData();
		String baseTage =oldProtocolFormXmlData.getProtocolForm().getProtocolFormType().getBaseTag();

		try {
			oldProtocolFormXm = protocolFormXmlDifferServiceImpl
					.differProtocolFormXml(baseTage,oldProtocolFormXm, newProtocolFormXm);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.debug(oldProtocolFormXm);

	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDifferServiceImpl getProtocolFormXmlDifferServiceImpl() {
		return protocolFormXmlDifferServiceImpl;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDifferServiceImpl(
			ProtocolFormXmlDifferServiceImpl protocolFormXmlDifferServiceImpl) {
		this.protocolFormXmlDifferServiceImpl = protocolFormXmlDifferServiceImpl;
	}



}
