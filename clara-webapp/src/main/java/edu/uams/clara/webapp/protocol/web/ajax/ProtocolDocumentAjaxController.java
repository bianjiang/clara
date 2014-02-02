package edu.uams.clara.webapp.protocol.web.ajax;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.ProtocolDocumentDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;

@Controller
public class ProtocolDocumentAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolDocumentAjaxController.class);	

	private ProtocolDocumentDao protocolDocumentDao;
	
	/**
	 * List all the latest files for the entire protocol
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/documents/list")
	/*
	@Cacheable(cacheName = "protocolDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
			@Property(name = "includeMethod", value = "false") }))
			*/
	public @ResponseBody
	List<ProtocolFormXmlDataDocumentWrapper> listProtocolDocuments(
			@PathVariable("protocolId") long protocolId) {

		//Protocol protocol = new Protocol();
		//protocol.setId(protocolId);
		
		
		List<ProtocolFormXmlDataDocumentWrapper> protocolDocuments = protocolDocumentDao.listProtocolFormXmlDataDocumentsByProtocolId(protocolId);
		
		logger.debug("protocolId: " + protocolDocuments.size());
		
		return protocolDocuments;
	}	

	@Autowired(required=true)
	public void setProtocolDocumentDao(ProtocolDocumentDao protocolDocumentDao) {
		this.protocolDocumentDao = protocolDocumentDao;
	}


	public ProtocolDocumentDao getProtocolDocumentDao() {
		return protocolDocumentDao;
	}

	
	
}
