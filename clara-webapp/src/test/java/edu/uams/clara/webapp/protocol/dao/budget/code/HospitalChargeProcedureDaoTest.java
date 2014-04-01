package edu.uams.clara.webapp.protocol.dao.budget.code;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/HospitalChargeProcedureDaoTest-context.xml" })

public class HospitalChargeProcedureDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(HospitalChargeProcedureDaoTest.class);

	private HospitalChargeProcedureDao hospitalChargeProcedureDao;
	
	@Test
	public void generateDatabases(){
		List<HospitalChargeProcedure> hospitalCharges = hospitalChargeProcedureDao.findAll();
		for(HospitalChargeProcedure hospitalChargeProcedure:hospitalCharges){
			logger.debug("" + hospitalChargeProcedure.getCptCode());
		}
	}
	
	@Autowired(required=true)
	public void setHospitalChargeProcedureDao(HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}
}
