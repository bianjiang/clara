package edu.uams.clara.webapp.contract.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.contract.dao.contractform.ContractFormXmlDataDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;
import edu.uams.clara.webapp.contract.domain.contractform.enums.ContractFormXmlDataType;
import edu.uams.clara.webapp.contract.service.ContractMetaDataXmlService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/contract/service/ContractMetaDataServiceTest-context.xml" })
public class ContractMetaDataServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractMetaDataServiceTest.class);

	private ContractMetaDataXmlService contractMetaDataXmlService;

	private ContractFormXmlDataDao contractFormXmlDataDao;

	@Test
	public void testConsolidateContractFormXmlData() throws Exception {
		ContractFormXmlData contractFormXmlData = contractFormXmlDataDao.findById(265l);

		contractFormXmlData = contractMetaDataXmlService.consolidateContractFormXmlData(contractFormXmlData, ContractFormXmlDataType.CONTRACT);

		logger.debug("after: " + contractFormXmlData.getXmlData());
	}

	public ContractMetaDataXmlService getContractMetaDataXmlService() {
		return contractMetaDataXmlService;
	}

	@Autowired(required = true)
	public void setContractMetaDataXmlService(ContractMetaDataXmlService contractMetaDataXmlService) {
		this.contractMetaDataXmlService = contractMetaDataXmlService;
	}

	public ContractFormXmlDataDao getContractFormXmlDataDao() {
		return contractFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setContractFormXmlDataDao(ContractFormXmlDataDao contractFormXmlDataDao) {
		this.contractFormXmlDataDao = contractFormXmlDataDao;
	}
}
