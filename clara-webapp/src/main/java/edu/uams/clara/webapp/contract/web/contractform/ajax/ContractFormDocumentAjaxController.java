package edu.uams.clara.webapp.contract.web.contractform.ajax;

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
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDocumentDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;

@Controller
public class ContractFormDocumentAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormDocumentAjaxController.class);
	
	private ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao;
	
	
	/**
	 * list all the latest files for this specific contract-form (contractFormId) including all versions
	 *
	 **/
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/documents/list-all")
	//@Cacheable(cacheName = "contractDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ContractFormXmlDataDocumentWrapper> listContractFormDocuments(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId)
			{
		
		logger.debug("contractFormId: " + contractFormId);
		List<ContractFormXmlDataDocumentWrapper> documents = contractFormXmlDataDocumentDao.listDocumentsByContractFormId(contractFormId);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/documents/list-by-category")
	//@Cacheable(cacheName = "contractDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ContractFormXmlDataDocumentWrapper> listContractFormDocumentsByType(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("category") String category) {
		
		logger.debug("contractFormId: " + contractFormId);
		List<ContractFormXmlDataDocumentWrapper> documents = contractFormXmlDataDocumentDao.listDocumentsByContractFormIdAndCategory(contractFormId, category);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/documents/list-by-committee")
	//@Cacheable(cacheName = "contractDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ContractFormXmlDataDocumentWrapper> listContractFormDocumentsByCommittee(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("committee") Committee committee) {
		
		logger.debug("contractFormId: " + contractFormId);
		List<ContractFormXmlDataDocumentWrapper> documents = contractFormXmlDataDocumentDao.listDocumentsByContractFormIdAndCommittee(contractFormId, committee);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@RequestMapping(value = "/ajax/contracts/{contractId}/contract-forms/{contractFormId}/documents/list-by-later-than-date")
	//@Cacheable(cacheName = "contractDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
		//	@Property(name = "includeMethod", value = "false") }))
	public @ResponseBody
	List<ContractFormXmlDataDocumentWrapper> listContractFormDocumentsEarlierThanDate(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam("date") Date date) {
		
		logger.debug("contractFormId: " + contractFormId);
		List<ContractFormXmlDataDocumentWrapper> documents = contractFormXmlDataDocumentDao.listDocumentsByContractFormIdAndLaterThanDate(contractFormId, date);
				
		logger.debug("#docs: " + documents.size());

		return documents;
	}
	
	@Autowired(required=true)
	public void setContractFormXmlDataDocumentDao(
			ContractFormXmlDataDocumentDao contractFormXmlDataDocumentDao) {
		this.contractFormXmlDataDocumentDao = contractFormXmlDataDocumentDao;
	}

	public ContractFormXmlDataDocumentDao getContractFormXmlDataDocumentDao() {
		return contractFormXmlDataDocumentDao;
	}	
}
