package edu.uams.clara.webapp.protocol.dao.budget.code;

import java.io.IOException;
import java.util.ArrayList;
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

import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.ProcedureWrapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/ProcedureWrapperTest-context.xml" })
public class ProcedureWrapperTest {

	private final static Logger logger  = LoggerFactory
	.getLogger(ProcedureWrapperTest.class);

	private CPTCodeDao cptCodeDao;

	private HospitalChargeProcedureDao hospitalChargeProcedureDao;

	private PhysicianChargeProcedureDao physicianChargeProcedureDao;

	//@Test
	public void testCPTCodeDaofindByKeyword(){

		List<CPTCode> cptCodes = cptCodeDao.findByKeyword("cbc");

		for(CPTCode cptCode:cptCodes){
			logger.debug(cptCode.getCode() + ": " + cptCode.getShortDescription());
		}
	}

	@Test
	public void testListProcedures() throws JsonGenerationException, JsonMappingException, IOException{
		List<CPTCode> cptCodes = cptCodeDao.findByKeyword("cbc");

		ProcedureWrapper procedureWrapper = null;
		List<ProcedureWrapper> procedures = new ArrayList<ProcedureWrapper>();

		for(CPTCode cptCode:cptCodes){

			logger.debug(cptCode.getCode() + ": " + cptCode.getShortDescription());

			List<HospitalChargeProcedure> hospitalProcedures = hospitalChargeProcedureDao.findByCptCode(cptCode.getCode());
			logger.debug("hospitalProcedures: " + hospitalProcedures.size());

			List<PhysicianChargeProcedure> physicianProcedures = physicianChargeProcedureDao.findByCptCode(cptCode.getCode());
			logger.debug("physicianProcedures: " + physicianProcedures.size());

			if(hospitalProcedures.size() > 0 || physicianProcedures.size() > 0){
				procedureWrapper = new ProcedureWrapper();
				procedureWrapper.setCptCode(cptCode);
			}else{
				continue;
			}
			if(hospitalProcedures.size() > 0) {
				procedureWrapper.setHospitalProcedure(hospitalProcedures.get(0));
			}
			procedureWrapper.setPhysicianProcedures(physicianProcedures);


			procedures.add(procedureWrapper);
		}

		ObjectMapper objectMapper = new ObjectMapper();

		logger.debug(objectMapper.writeValueAsString(procedures));
	}

	@Autowired(required=true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	@Autowired(required=true)
	public void setHospitalChargeProcedureDao(HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required=true)
	public void setPhysicianChargeProcedureDao(
			PhysicianChargeProcedureDao physicianChargeProcedureDao) {
		this.physicianChargeProcedureDao = physicianChargeProcedureDao;
	}

	public PhysicianChargeProcedureDao getPhysicianChargeProcedureDao() {
		return physicianChargeProcedureDao;
	}
}
