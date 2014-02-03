package edu.uams.clara.integration.incoming.billingcodes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Sets;

import edu.uams.clara.integration.incoming.billingcodes.dao.HospitalChargeUpdateDao;
import edu.uams.clara.integration.incoming.billingcodes.dao.PhysicianChargeUpdateDao;
import edu.uams.clara.integration.incoming.billingcodes.domain.HospitalChargeUpdate;
import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeMappingDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCodeMapping;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/billingcodes/BillingCodesUpdateTest-context.xml" })
public class BillingCodesUpdateTest {

	private HospitalChargeProcedureDao hospitalChargeProcedureDao;
	private HospitalChargeUpdateDao hospitalChargeUpdateDao;
	private PhysicianChargeUpdateDao physicianChargeUpdateDao;
	private CPTCodeDao cptCodeDao;
	private CPTCodeMappingDao cptCodeMappingDao;
	private ResourceLoader resourceLoader;

	private final static Logger logger = LoggerFactory
			.getLogger(BillingCodesUpdateTest.class);

	//@Test
	public void readHospitalCharge() {
		try {
			String rawdata = null;
			float price = 0;

			// read csv from file
			BufferedReader reader = new BufferedReader(
					new FileReader(
							"C:\\DOCUME~1\\yuanjiawei\\Desktop\\April.csv"));

			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				HospitalChargeUpdate hospitalChargeUpdate = new HospitalChargeUpdate();

				hospitalChargeUpdate.setCptCode(item[0]);
				if (!item[1].isEmpty()) {

					Number number = NumberFormat.getInstance().parse(item[1]);

					// System.out.println(number);

					price = (float) (number.floatValue() * 1.05);

					BigDecimal bdPrice = new BigDecimal((double) price);
					bdPrice = bdPrice.setScale(2, 4);

					hospitalChargeUpdate.setCost(bdPrice);
					if(item.length>2){
					hospitalChargeUpdate.setDescription(item[2]);
					}

				} else {
					//set cost to 0.00 if the procedure has no cost
					int noValue = 0;
					BigDecimal bdPrice = new BigDecimal(noValue);
					hospitalChargeUpdate.setCost(bdPrice);
					if(item.length>2){
						hospitalChargeUpdate.setDescription(item[2]);
						}
				}

				hospitalChargeUpdateDao.saveOrUpdate(hospitalChargeUpdate);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void updateHospitalCost() {
		Set<String> newCptCodeSet = Sets.newHashSet();
		Set<String> oldCptCodeSet = Sets.newHashSet();

		List<HospitalChargeUpdate> hospitalChargeUpdateList = hospitalChargeUpdateDao.findAll();

		for (HospitalChargeUpdate hospitalCharageUpdate : hospitalChargeUpdateList){
			String newCptCode = hospitalCharageUpdate.getCptCode();
			BigDecimal cost = hospitalCharageUpdate.getCost();
			String desc = hospitalCharageUpdate.getDescription();

			newCptCodeSet.add(newCptCode);

			try {
				HospitalChargeProcedure hospitalChargeProcedure = hospitalChargeProcedureDao.findByCptCodeOnly(newCptCode);

				hospitalChargeProcedure.setCost(cost);
				hospitalChargeProcedure.setDescription(desc);
				hospitalChargeProcedure.setRetired(Boolean.FALSE);
				hospitalChargeProcedure.setRetiredDate(null);

				hospitalChargeProcedureDao.saveOrUpdate(hospitalChargeProcedure);
			} catch (Exception e) {
				HospitalChargeProcedure newHospitalChargeProcedure = new HospitalChargeProcedure();

				newHospitalChargeProcedure.setCost(cost);
				newHospitalChargeProcedure.setCptCode(newCptCode);
				newHospitalChargeProcedure.setDescription(desc);
				//newHospitalChargeProcedure.setEffectiveDate(null);
				newHospitalChargeProcedure.setHospitalOnly(Boolean.TRUE);
				newHospitalChargeProcedure.setOverwritten(Boolean.FALSE);
				newHospitalChargeProcedure.setRetired(Boolean.FALSE);

				hospitalChargeProcedureDao.saveOrUpdate(newHospitalChargeProcedure);
			}
		}

		List<HospitalChargeProcedure> hospitalChargeProcedureList = hospitalChargeProcedureDao
				.findAll();

		for (HospitalChargeProcedure hcp : hospitalChargeProcedureList){
			oldCptCodeSet.add(hcp.getCptCode());
		}

		Set<String> toDeleteSet = Sets.difference(newCptCodeSet, oldCptCodeSet);

		for (String toDeleteCptCode : toDeleteSet){
			HospitalChargeProcedure toDeletehospitalChargeProcedure = hospitalChargeProcedureDao.findByCptCodeOnly(toDeleteCptCode);

			toDeletehospitalChargeProcedure.setRetired(Boolean.TRUE);
			toDeletehospitalChargeProcedure.setRetiredDate(new Date());

			hospitalChargeProcedureDao.saveOrUpdate(toDeletehospitalChargeProcedure);
		}
		/*
		try {
			//update records existed in database
			List<HospitalChargeProcedure> hospitalChargeProcedureList = hospitalChargeProcedureDao
					.findAll();
			for (int procedureId = 0; procedureId < hospitalChargeProcedureList
					.size(); procedureId++) {
				HospitalChargeProcedure hospitalChargeProcedure = hospitalChargeProcedureList
						.get(procedureId);
				String cptcode = hospitalChargeProcedure.getCptCode();



				if (hospitalChargeUpdateDao.findByCptCode(cptcode).size() > 0) {
					int index = 0;
					for (int cptForUp = 0; cptForUp < hospitalChargeUpdateDao
							.findByCptCode(cptcode).size(); cptForUp++) {
						BigDecimal updateCost = hospitalChargeUpdateDao
								.findByCptCode(cptcode).get(cptForUp).getCost();

						float updateCostFloat = updateCost.floatValue();

						if (updateCostFloat > 0 || updateCostFloat == 0) {

							hospitalChargeProcedure.setCost(updateCost);
							hospitalChargeProcedureDao
									.saveOrUpdate(hospitalChargeProcedure);
							index++;
						} else if (cptForUp == hospitalChargeUpdateDao
								.findByCptCode(cptcode).size() - 1
								&& index == 0) {
							// delete for cost is null
							hospitalChargeProcedure.setRetired(true);
							hospitalChargeProcedure.setRetiredDate(new Date());
							hospitalChargeProcedureDao
									.saveOrUpdate(hospitalChargeProcedure);
						}

					}
				} else {
					// delete for cpt not here anymore
					hospitalChargeProcedure.setRetired(true);
					hospitalChargeProcedure.setRetiredDate(new Date());
					hospitalChargeProcedureDao
							.saveOrUpdate(hospitalChargeProcedure);
				}

			}

			//insert record not in database
			List<HospitalChargeUpdate> hospitalChargeUpdateList = hospitalChargeUpdateDao.findAll();
			for(int j=0;j<hospitalChargeUpdateList.size();j++){
				HospitalChargeUpdate hospitalChargeUpdate = hospitalChargeUpdateList.get(j);
				String cptCode = hospitalChargeUpdate.getCptCode();
				List<HospitalChargeProcedure> procesureList =null;
				try{
					procesureList=hospitalChargeProcedureDao.findByCptCode(cptCode);
				}catch(Exception e){
					procesureList =null;
				}
				if(procesureList.size()==0){
					HospitalChargeProcedure hospitalChargeProcedure = new HospitalChargeProcedure();
					hospitalChargeProcedure.setCptCode(hospitalChargeUpdate.getCptCode());
					hospitalChargeProcedure.setCost(hospitalChargeUpdate.getCost());
					hospitalChargeProcedure.setDescription(hospitalChargeUpdate.getDescription());
					hospitalChargeProcedure.setRetired(false);
					hospitalChargeProcedure.setHospitalOnly(true);
					hospitalChargeProcedureDao
					.saveOrUpdate(hospitalChargeProcedure);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		*/

	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(
			HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}

	public HospitalChargeUpdateDao getHospitalChargeUpdateDao() {
		return hospitalChargeUpdateDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeUpdateDao(
			HospitalChargeUpdateDao hospitalChargeUpdateDao) {
		this.hospitalChargeUpdateDao = hospitalChargeUpdateDao;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public PhysicianChargeUpdateDao getPhysicianChargeUpdateDao() {
		return physicianChargeUpdateDao;
	}

	@Autowired(required = true)
	public void setPhysicianChargeUpdateDao(
			PhysicianChargeUpdateDao physicianChargeUpdateDao) {
		this.physicianChargeUpdateDao = physicianChargeUpdateDao;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}

	public CPTCodeMappingDao getCptCodeMappingDao() {
		return cptCodeMappingDao;
	}

	public void setCptCodeMappingDao(CPTCodeMappingDao cptCodeMappingDao) {
		this.cptCodeMappingDao = cptCodeMappingDao;
	}

}
