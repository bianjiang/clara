package edu.uams.clara.webapp.contract.dao;

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

import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlDataDocumentWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/contract/dao/ContractDocumentDaoTest-context.xml" })
public class ContractDocumentDaoTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractDocumentDaoTest.class);
	
	private ContractDocumentDao contractDocumentDao;

	@Test
	public void testListDocuments() throws JsonGenerationException, JsonMappingException, IOException{
		
		List<ContractFormXmlDataDocumentWrapper> contractDocuments = contractDocumentDao.listContractFormXmlDataDocuments(13050l);
		
		logger.debug("cnt: " + contractDocuments.size());
		ObjectMapper objectMapper = new ObjectMapper();
		
		logger.debug(objectMapper.writeValueAsString(contractDocuments));
		
	}

	public ContractDocumentDao getContractDocumentDao() {
		return contractDocumentDao;
	}

	@Autowired(required=true)
	public void setContractDocumentDao(ContractDocumentDao contractDocumentDao) {
		this.contractDocumentDao = contractDocumentDao;
	}
}
