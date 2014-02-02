package edu.uams.clara.webapp.contract.web.ajax;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.contract.dao.ContractDocumentDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;

@Controller
public class ContractDocumentAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(ContractDocumentAjaxController.class);	

	private ContractDocumentDao contractDocumentDao;
	
	/**
	 * List all the latest files for the entire contract
	 */
	@RequestMapping(value = "/ajax/contracts/{contractId}/documents/list")
	/*
	@Cacheable(cacheName = "contractDocumentsCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
			@Property(name = "includeMethod", value = "false") }))
			*/
	public @ResponseBody
	List<ContractFormXmlDataDocumentWrapper> listContractDocuments(
			@PathVariable("contractId") long contractId) {
		
		List<ContractFormXmlDataDocumentWrapper> contractDocuments = contractDocumentDao.listContractFormXmlDataDocuments(contractId);

		return contractDocuments;
	}	

	@Autowired(required=true)
	public void setContractDocumentDao(ContractDocumentDao contractDocumentDao) {
		this.contractDocumentDao = contractDocumentDao;
	}


	public ContractDocumentDao getContractDocumentDao() {
		return contractDocumentDao;
	}

	
	
}
