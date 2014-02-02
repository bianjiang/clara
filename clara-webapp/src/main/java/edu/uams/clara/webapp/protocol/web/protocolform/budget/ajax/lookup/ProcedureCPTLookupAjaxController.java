package edu.uams.clara.webapp.protocol.web.protocolform.budget.ajax.lookup;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeMappingDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianChargeProcedureDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.ProcedureWrapper;

@Controller
public class ProcedureCPTLookupAjaxController {

	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(ProcedureCPTLookupAjaxController.class);

	private CPTCodeDao cptCodeDao;

	private CPTCodeMappingDao cptCodeMappingDao;

	private HospitalChargeProcedureDao hospitalChargeProcedureDao;

	private PhysicianChargeProcedureDao physicianChargeProcedureDao;

	/*
	 * @RequestMapping(value =
	 * "/ajax/protocols/budgets/procedures/find-by-code-and-keyword", method =
	 * RequestMethod.GET) public @ResponseBody List<ProcedureWrapper>
	 * findProceduresByKeyword(
	 * 
	 * @RequestParam("code") String code,
	 * 
	 * @RequestParam("keyword") String keyword) {
	 * 
	 * // TODO: Change this function to search by the specified "code": //
	 * possible values are "CPT","SNOMED","SOFTID" // TODO: Move
	 * shortDescription and longDescription outside of cptCode // object, into
	 * procedure object (to allow short/long descriptions from // other codes)
	 * // TODO: Create snomed, softid objects to store code (just like cptCode
	 * // JSON in current procedure object): // "snomed": { // "code":
	 * "WHATEVER.SNOMED.CODE.LOOKS.LIKE", // "id": 740, // "retired": false, //
	 * "concurrentVersion": 2 // } // NOTE: To get the UI working, it's okay if
	 * this function returns CPT // info in the snomed object (snomed.code can
	 * have a cpt code for // example), // just as long at the snomed and softid
	 * objects exist. Also, when I // pass "snomed" as the code type, only
	 * return a snomedCode object (do // not have cptCode or softid object in
	 * the JSON) // Hospital and physician charges should exist, but be null
	 * if/when // necessary.
	 * 
	 * List<CPTCode> cptCodes = cptCodeDao.findByKeyword(keyword);
	 * 
	 * ProcedureWrapper procedureWrapper = null; List<ProcedureWrapper>
	 * procedures = new ArrayList<ProcedureWrapper>();
	 * 
	 * for (CPTCode cptCode : cptCodes) {
	 * 
	 * logger.debug(cptCode.getCode() + ": " + cptCode.getShortDescription());
	 * HospitalChargeProcedure hospitalProcedure = null;
	 * List<PhysicianChargeProcedure> physicianProcedures = null;
	 * 
	 * try { hospitalProcedure = hospitalChargeProcedureDao
	 * .findFirstByCptCode(cptCode.getCode()); } catch (Exception ex) {
	 * logger.warn("no hospital procedure found for cptCode: " + cptCode);
	 * continue;
	 * 
	 * }
	 * 
	 * try { physicianProcedures = physicianChargeProcedureDao
	 * .findByCptCode(cptCode.getCode()); } catch (Exception ex) { // if no
	 * professional charge... sreturn hostipal only... }
	 * 
	 * procedureWrapper = new ProcedureWrapper();
	 * procedureWrapper.setCptCode(cptCode);
	 * procedureWrapper.setHospitalProcedure(hospitalProcedure);
	 * 
	 * if (physicianProcedures != null && physicianProcedures.size() > 0) {
	 * procedureWrapper.setPhysicianProcedures(physicianProcedures); }
	 * 
	 * procedures.add(procedureWrapper); }
	 * 
	 * return procedures; }
	 */
	@RequestMapping(value = "/ajax/protocols/budgets/procedures/find-by-keyword", method = RequestMethod.GET)
	public @ResponseBody
	List<ProcedureWrapper> findProceduresByKeyword(
			@RequestParam("keyword") String keyword) {
		List<CPTCode> cptCodes = cptCodeDao.findByKeyword(keyword);

		ProcedureWrapper procedureWrapper = null;
		List<ProcedureWrapper> procedures = new ArrayList<ProcedureWrapper>();

		for (CPTCode cptCode : cptCodes) {

			logger.debug(cptCode.getCode() + ": "
					+ cptCode.getShortDescription());
			HospitalChargeProcedure hospitalProcedure = null;
			List<PhysicianChargeProcedure> physicianProcedures = null;

			try {
				hospitalProcedure = hospitalChargeProcedureDao.findFirstByCptCode(cptCode.getCode());
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.warn("no hospital procedure found for cptCode: "
						+ cptCode);
				//continue;

			}

			try {
				physicianProcedures = physicianChargeProcedureDao
						.findByCptCode(cptCode.getCode());
			} catch (Exception ex) {
				// if no professional charge... sreturn hostipal only...
			}

			procedureWrapper = new ProcedureWrapper();
			procedureWrapper.setCptCode(cptCode);
			procedureWrapper.setHospitalProcedure(hospitalProcedure);

			if (physicianProcedures != null && physicianProcedures.size() > 0) {
				procedureWrapper.setPhysicianProcedures(physicianProcedures);
			}

			procedures.add(procedureWrapper);
		}

		return procedures;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(
			HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setPhysicianChargeProcedureDao(
			PhysicianChargeProcedureDao physicianChargeProcedureDao) {
		this.physicianChargeProcedureDao = physicianChargeProcedureDao;
	}

	public PhysicianChargeProcedureDao getPhysicianChargeProcedureDao() {
		return physicianChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	public CPTCodeMappingDao getCptCodeMappingDao() {
		return cptCodeMappingDao;
	}
	
	public void setCptCodeMappingDao(CPTCodeMappingDao cptCodeMappingDao) {
		this.cptCodeMappingDao = cptCodeMappingDao;
	}
}
