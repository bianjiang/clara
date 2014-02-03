package edu.uams.clara.webapp.protocol.dao;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/ProtocolDocumentDaoTest-context.xml" })
public class ProtocolDocumentDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolDocumentDaoTest.class);

	private ProtocolDao protocolDao;

	private ProtocolDocumentDao protocolDocumentDao;

	@Test
	public void testListProtocolFormXmlDataDocumentCategories(){

		Protocol protocol = protocolDao.findById(1);

		List<String> categories = protocolDocumentDao.listProtocolFormXmlDataDocumentCategories(protocol);

		logger.debug("protocol: " + protocol.getId());
		for(String c:categories){
			logger.debug(" {" + c + "} ");
		}
	}

	@Test
	public void testListProtocolFormXmlDataDocuments(){

		//Protocol protocol = protocolDao.findById(1);

		//List<ProtocolFormXmlDataDocument> protocolFiles = protocolDocumentDao.listProtocolFormXmlDataDocumentsByProtocolId(1l);

		//logger.debug("protocol: " + protocol.getId());
		//for(ProtocolFormXmlDataDocument pf:protocolFiles){
		//	logger.debug(" {" + pf.getId() +"; " + pf.getTitle() + "} ");
		//}
	}

	@Test
	public void testListProtocolFormXmlDataDocumentRevisions(){

		ProtocolFormXmlDataDocument protocolDocument = protocolDocumentDao.findById(1);

		List<ProtocolFormXmlDataDocumentWrapper> protocolFiles = protocolDocumentDao.listProtocolFormXmlDataDocumentRevisionsByParentId(protocolDocument.getId());

		logger.debug("protocol: " + protocolDocument.getProtocolFormXmlData().getProtocolForm().getProtocol().getId());
		/*
		for(ProtocolFormXmlDataDocumentWrapper pf:protocolFiles){
			logger.debug(" {" + pf.getId() +"; " + pf.getTitle() + "} ");
		}
		*/
	}


	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolDocumentDao(ProtocolDocumentDao protocolDocumentDao) {
		this.protocolDocumentDao = protocolDocumentDao;
	}

	public ProtocolDocumentDao getProtocolDocumentDao() {
		return protocolDocumentDao;
	}




}
