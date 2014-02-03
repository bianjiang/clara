package edu.uams.clara.webapp.protocol.web.ajax.budget;

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

import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.ProcedureWrapper;
import edu.uams.clara.webapp.protocol.web.protocolform.budget.ajax.lookup.ProcedureCPTLookupAjaxController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/ajax/budget/ProcedureCPTLookupAjaxControllerTest-context.xml"})
public class ProcedureCPTLookupAjaxControllerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ProcedureCPTLookupAjaxControllerTest.class);

	private ProcedureCPTLookupAjaxController procedureCPTLookupAjaxController;

	private HospitalChargeProcedureDao hospitalChargeProcedureDao;

	//@Test
	public void testFindProceduresByKeyword() throws JsonGenerationException, JsonMappingException, IOException {
		List<ProcedureWrapper> procedures = procedureCPTLookupAjaxController.findProceduresByKeyword("cbc");

		ObjectMapper objectMapper = new ObjectMapper();

		logger.debug(objectMapper.writeValueAsString(procedures));
	}

	@Test
	public void testHospitalProcedureDao() throws JsonGenerationException, JsonMappingException, IOException {
		List<HospitalChargeProcedure> procedures = hospitalChargeProcedureDao.findByCptCode("71030");

		//ObjectMapper objectMapper = new ObjectMapper();

		logger.debug("count: " + procedures.size());
	}

	@Autowired(required=true)
	public void setProcedureCPTLookupAjaxController(
			ProcedureCPTLookupAjaxController procedureCPTLookupAjaxController) {
		this.procedureCPTLookupAjaxController = procedureCPTLookupAjaxController;
	}

	public ProcedureCPTLookupAjaxController getProcedureCPTLookupAjaxController() {
		return procedureCPTLookupAjaxController;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required=true)
	public void setHospitalChargeProcedureDao(HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}
}
