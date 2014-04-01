package edu.uams.clara.webapp.protocol.dao.protocolform;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument.Status;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.web.ajax.ProtocolDocumentAjaxController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/protocolform/ProtocolFormXmlDataDocumentDaoTest-context.xml" })
public class ProtocolFormXmlDataDocumentDaoTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormXmlDataDocumentDaoTest.class);

	// @Test
	public void testListFormXmlDataDocumentRevisions() {
		ProtocolFormXmlDataDocument parentFormXmlDataDocument = protocolFormXmlDataDocumentDao
				.findById(1);

		List<ProtocolFormXmlDataDocumentWrapper> revisions = protocolFormXmlDataDocumentDao
				.listProtocolFormXmlDataDocumentRevisionsByParentId(parentFormXmlDataDocument
						.getId());

		logger.debug("protocol: "
				+ parentFormXmlDataDocument.getProtocolFormXmlData()
						.getProtocolForm().getProtocol().getId());
		/*
		 * for(ProtocolFormXmlDataDocument r:revisions){ logger.debug("d: " +
		 * r.getId() + "; " + r.getTitle()); }
		 */
	}

	//@Test
	public void testListDocuments() throws JsonGenerationException,
			JsonMappingException, IOException {
		
		for (int i = 0; i < 50; i++) {
			List<ProtocolFormXmlDataDocumentWrapper> protocolDocuments = protocolDocumentDao
					.listProtocolFormXmlDataDocumentsByProtocolId(201498l);

			logger.debug("cnt: " + protocolDocuments.size());
			ObjectMapper objectMapper = new ObjectMapper();

			logger.debug(objectMapper.writeValueAsString(protocolDocuments));
		}

	}
	
	//@Test
	public void testCountDocumentRevisionsByParentId() throws Exception{
		long lastVersionId = protocolFormXmlDataDocumentDao.countDocumentRevisionsByParentId(753);
		
		logger.debug("lastVersionId: " + lastVersionId);
	}
	
	//@Test
	public void testListDocumentsByProtocolFormIdAndStatus() throws Exception{
		List<ProtocolFormXmlDataDocumentWrapper> lst = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormIdAndStatus(1662, Status.DRAFT);
		logger.debug("$$$$$$$$$ " + lst.size());
	}
	
	@Test
	public void testGetLatestDocumentByProtocolFormId() throws Exception{
		List<ProtocolFormXmlDataDocument> lst = protocolFormXmlDataDocumentDao.getLatestDocumentByProtocolFormId(11252);
		logger.debug("$$$$$$$$$ " + lst.size());
		for (ProtocolFormXmlDataDocument pfxd : lst){
			logger.debug("title: " + pfxd.getTitle());
		}
		
	}

	// @Test
	public void testListDocumentsAjaxController()
			throws JsonGenerationException, JsonMappingException, IOException {

		// List<ProtocolFormXmlDataDocument> protocolDocuments =
		// protocolDocumentAjaxController.listProtocolDocuments(201006l);

		// logger.debug("cnt: " + protocolDocuments.size());

		// ObjectMapper objectMapper = new ObjectMapper();

		// logger.debug(objectMapper.writeValueAsString(protocolDocuments));

	}

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;

	private ProtocolDocumentDao protocolDocumentDao;

	private ProtocolDocumentAjaxController protocolDocumentAjaxController;

	@Autowired(required = true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	public ProtocolDocumentDao getProtocolDocumentDao() {
		return protocolDocumentDao;
	}

	@Autowired(required = true)
	public void setProtocolDocumentDao(ProtocolDocumentDao protocolDocumentDao) {
		this.protocolDocumentDao = protocolDocumentDao;
	}

	public ProtocolDocumentAjaxController getProtocolDocumentAjaxController() {
		return protocolDocumentAjaxController;
	}

	@Autowired(required = true)
	public void setProtocolDocumentAjaxController(
			ProtocolDocumentAjaxController protocolDocumentAjaxController) {
		this.protocolDocumentAjaxController = protocolDocumentAjaxController;
	}

}
