package edu.uams.clara.migration.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.crimson.dao.CrimsonContractDao;
import edu.uams.clara.webapp.contract.dao.ContractDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/migration/service/ConstactMigrationServiceTest-context.xml" })
public class ContractMigrationServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractMigrationServiceTest.class);

	private CrimsonContractDao crimsonContractDao;
	private ContractMigrationService constactMigrationService;
	private ContractDao contractDao;

	//@Test
	public void crimsonContractDaoFindAll() {
		List<Object[]> result = crimsonContractDao.findAllContract();
		BigInteger  id = (BigInteger) result.get(0)[0];
		logger.debug(id.toString()+""+result.size());
	}

	//@Test
	public void findContractInfo() {
		Object[] result = crimsonContractDao.findContractInfoByContractId(BigInteger.valueOf(178));

		BigInteger  id = (BigInteger) result[0];
		logger.debug(id.toString());
	}

	//@Test
	public void contractInsertTest(){
		Date date = new Date();
		contractDao.disableIdentyInsert(222222, 1, date, "test", 201628, "C22222");
	}

	@Test
		public void contractsMigration() {
			constactMigrationService.migrateContract();
		}



	public CrimsonContractDao getCrimsonContractDao() {
		return crimsonContractDao;
	}

	@Autowired(required=true)
	public void setCrimsonContractDao(CrimsonContractDao crimsonContractDao) {
		this.crimsonContractDao = crimsonContractDao;
	}

	public ContractMigrationService getConstactMigrationService() {
		return constactMigrationService;
	}

	@Autowired(required=true)
	public void setConstactMigrationService(ContractMigrationService constactMigrationService) {
		this.constactMigrationService = constactMigrationService;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

}
