package edu.uams.clara.webapp.protocol.web.protocolform.ajax;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;

@Controller
public class ProtocolFormDocumentAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormDocumentAjaxController.class);
	
	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	
	
	/**
	 * list all the latest files for this specific protocol-form (protocolFormId) including all versions
	 *
	 **/
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/documents/list-all")
	//@Cacheable(cacheName = "protocolDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormDocuments(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId)
			{
		
		logger.debug("protocolFormId: " + protocolFormId);
		List<ProtocolFormXmlDataDocumentWrapper> documents = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormId(protocolFormId);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/documents/list-by-category")
	//@Cacheable(cacheName = "protocolDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormDocumentsByType(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("category") String category) {
		
		logger.debug("protocolFormId: " + protocolFormId);
		List<ProtocolFormXmlDataDocumentWrapper> documents = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormIdAndCategory(protocolFormId, category);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/documents/list-by-committee")
	//@Cacheable(cacheName = "protocolDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormDocumentsByCommittee(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("committee") Committee committee) {
		
		logger.debug("protocolFormId: " + protocolFormId);
		List<ProtocolFormXmlDataDocumentWrapper> documents = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormIdAndCommittee(protocolFormId, committee);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/documents/list-by-later-than-date")
	//@Cacheable(cacheName = "protocolDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormDocumentsEarlierThanDate(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId,
			@RequestParam("date") Date date) {
		
		logger.debug("protocolFormId: " + protocolFormId);
		List<ProtocolFormXmlDataDocumentWrapper> documents = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormIdAndLaterThanDate(protocolFormId, date);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}	
}
