package edu.uams.clara.integration.incoming.billingcodes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeMappingDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianChargeProcedureDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCodeMapping;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.ProcedureWrapper;
import edu.uams.clara.webapp.protocol.web.protocolform.budget.ajax.lookup.ProcedureCPTLookupAjaxController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/CPTCodeMappingImportTest-context.xml" })
public class CPTCodeMappingImportTest {
	private ResourceLoader resourceLoader;

	private CPTCodeDao cptCodeDao;

	private CPTCodeMappingDao cptCodeMappingDao;

	private HospitalChargeProcedureDao hospitalChargeProcedureDao;

	private PhysicianChargeProcedureDao physicianChargeProcedureDao;


	private final static Logger logger = LoggerFactory
			.getLogger(CPTCodeMappingImportTest.class);

	//@Test
	public void importGCode() {
		try {
			String rawdata = null;

			String csvFileName = "budgetcode/GCode.csv";
			Resource csvFile = resourceLoader.getResource(csvFileName);

			InputStream in = csvFile.getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));



			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				if (item.length < 4)
					continue;

				CPTCodeMapping gCode= new CPTCodeMapping();

				gCode.setTmID(item[0].trim());
				gCode.setTmDescription(item[1].trim());
				gCode.setOtherCode(item[2].trim());
				gCode.setCptCode(item[3].trim());

				cptCodeMappingDao.saveOrUpdate(gCode);
			}


		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testHospitalChargeProcedureDao() {
		HospitalChargeProcedure hospitalChargeProcedure = hospitalChargeProcedureDao.findFirstByCptCode("77056");

		logger.debug("hospitalChargeProcedure: " + hospitalChargeProcedure.getCptCode());
	}

	private ProcedureCPTLookupAjaxController procedureCPTLookupAjaxController;

	//@Test
	public void testProcedureCPTLookupAjaxController() throws JsonProcessingException{
		List<ProcedureWrapper> procedures = procedureCPTLookupAjaxController.findProceduresByKeyword("77056");
		ObjectMapper objectMapper = new ObjectMapper();
		logger.debug(objectMapper.writeValueAsString(procedures));

	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public CPTCodeMappingDao getCptCodeMappingDao() {
		return cptCodeMappingDao;
	}

	@Autowired(required = true)
	public void setCptCodeMappingDao(CPTCodeMappingDao cptCodeMappingDao) {
		this.cptCodeMappingDao = cptCodeMappingDao;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	@Autowired(required = true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}


	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public PhysicianChargeProcedureDao getPhysicianChargeProcedureDao() {
		return physicianChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setPhysicianChargeProcedureDao(
			PhysicianChargeProcedureDao physicianChargeProcedureDao) {
		this.physicianChargeProcedureDao = physicianChargeProcedureDao;
	}

	public ProcedureCPTLookupAjaxController getProcedureCPTLookupAjaxController() {
		return procedureCPTLookupAjaxController;
	}

	@Autowired(required = true)
	public void setProcedureCPTLookupAjaxController(
			ProcedureCPTLookupAjaxController procedureCPTLookupAjaxController) {
		this.procedureCPTLookupAjaxController = procedureCPTLookupAjaxController;
	}

}
