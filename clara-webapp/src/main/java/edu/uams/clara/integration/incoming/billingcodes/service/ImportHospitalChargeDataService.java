package edu.uams.clara.integration.incoming.billingcodes.service;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import au.com.bytecode.opencsv.CSVReader;
import edu.uams.clara.integration.incoming.billingcodes.dao.HospitalChargeUpdateDao;
import edu.uams.clara.integration.incoming.billingcodes.domain.HospitalChargeUpdate;
import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;
import edu.uams.clara.webapp.protocol.domain.budget.code.HospitalChargeProcedure;

public class ImportHospitalChargeDataService {
	private final static Logger logger = LoggerFactory
			.getLogger(ImportHospitalChargeDataService.class);

	private HospitalChargeUpdateDao hospitalChargeUpdateDao;
	private HospitalChargeProcedureDao hospitalChargeProcedureDao;
	private EntityManager em;
	private CPTCodeDao cptCodeDao;

	private final static String[] mappings = new String[] { "cptCode",
			"description", "cost","si","apc" };

	private List<HospitalChargeUpdate> fetchHospitalCostData() {
		List<HospitalChargeUpdate> hospitalChargeUpdates = new ArrayList<HospitalChargeUpdate>();
		try {
			CSVReader csvReader = new CSVReader(new FileReader(
					"C:\\Data\\hospitalCost.csv"));

			int numberOfColumns = mappings.length;
			csvReader.readNext(); // skip header
			String[] nextLine;
			Map<String,String> importedCptMap = Maps.newHashMap();

			while ((nextLine = csvReader.readNext()) != null) {
				HospitalChargeUpdate hospitalChargeUpdate = new HospitalChargeUpdate();
				Map<String, Object> properties;
				try {
					
					if(importedCptMap.containsKey(nextLine[0].trim())&&!importedCptMap.get(nextLine[0].trim()).isEmpty()){
						continue;
					}
					properties = BeanUtils.describe(hospitalChargeUpdate);
					for (int i = 0; i < numberOfColumns; i++) {
						if (i == 2) {
							String cost = nextLine[i].trim();
							importedCptMap.put(nextLine[0].trim(), cost);
							if (cost.isEmpty()) {
								cost = "0";
							}
							Number number = NumberFormat.getInstance().parse(
									cost);
							Float price = (float) (number.floatValue() * 1.05);
							BigDecimal bdPrice = new BigDecimal((double) price);
							bdPrice = bdPrice.setScale(2, 4);
							properties.put(mappings[i], bdPrice);

						} else {
							properties.put(mappings[i], nextLine[i].trim());
						}
					}
					BeanUtils.populate(hospitalChargeUpdate, properties);
					hospitalChargeUpdates.add(hospitalChargeUpdate);
					
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hospitalChargeUpdates;
	}

	private void updateHospitalCost() {
		Set<String> newCptCodeSet = Sets.newHashSet();
		Set<String> toDeleteSet = Sets.newHashSet();

		List<HospitalChargeUpdate> hospitalChargeUpdateList = hospitalChargeUpdateDao
				.findAll();

		for (HospitalChargeUpdate hospitalCharageUpdate : hospitalChargeUpdateList) {
			String newCptCode = hospitalCharageUpdate.getCptCode();
			BigDecimal cost = hospitalCharageUpdate.getCost();
			String desc = hospitalCharageUpdate.getDescription();
			String si = hospitalCharageUpdate.getSi();
			String apc = hospitalCharageUpdate.getApc();

			newCptCodeSet.add(newCptCode);

			try {
				HospitalChargeProcedure hospitalChargeProcedure = hospitalChargeProcedureDao
						.findByCptCodeOnly(newCptCode);

				hospitalChargeProcedure.setCost(cost);
				hospitalChargeProcedure.setDescription(desc);
				hospitalChargeProcedure.setRetired(Boolean.FALSE);
				hospitalChargeProcedure.setRetiredDate(null);
				hospitalChargeProcedure.setSi(si);
				hospitalChargeProcedure.setApc(apc);

				hospitalChargeProcedureDao
						.saveOrUpdate(hospitalChargeProcedure);
			} catch (Exception e) {
				HospitalChargeProcedure newHospitalChargeProcedure = new HospitalChargeProcedure();

				newHospitalChargeProcedure.setCost(cost);
				newHospitalChargeProcedure.setCptCode(newCptCode);
				newHospitalChargeProcedure.setDescription(desc);
				// newHospitalChargeProcedure.setEffectiveDate(null);
				newHospitalChargeProcedure.setHospitalOnly(Boolean.TRUE);
				newHospitalChargeProcedure.setOverwritten(Boolean.FALSE);
				newHospitalChargeProcedure.setRetired(Boolean.FALSE);
				newHospitalChargeProcedure.setSi(si);
				newHospitalChargeProcedure.setApc(apc);

				hospitalChargeProcedureDao
						.saveOrUpdate(newHospitalChargeProcedure);
			}
		}

		List<HospitalChargeProcedure> hospitalChargeProcedureList = hospitalChargeProcedureDao
				.findAll();

		for (HospitalChargeProcedure hcp : hospitalChargeProcedureList) {
			if (!newCptCodeSet.contains(hcp.getCptCode()))
				toDeleteSet.add(hcp.getCptCode());
		}
		for (String toDeleteCptCode : toDeleteSet) {
			HospitalChargeProcedure toDeletehospitalChargeProcedure = hospitalChargeProcedureDao
					.findByCptCodeOnly(toDeleteCptCode);

			toDeletehospitalChargeProcedure.setRetired(Boolean.TRUE);
			toDeletehospitalChargeProcedure.setRetiredDate(new Date());

			hospitalChargeProcedureDao
					.saveOrUpdate(toDeletehospitalChargeProcedure);
		}
	}
	
	public void addHospitalCptToCPTTable(){
		List<HospitalChargeProcedure> hcps =hospitalChargeProcedureDao.findAll();
		for(HospitalChargeProcedure hcp : hcps){
			CPTCode cptCode = null;
			try{
				cptCode = cptCodeDao.findByCode(hcp.getCptCode());
			}catch(Exception e){
				
			}
				
			if(cptCode == null){
				cptCode= new CPTCode();
				cptCode.setCode(hcp.getCptCode());
				cptCode.setLongDescription(hcp.getDescription());
				cptCode.setMediumDescription(hcp.getDescription());
				cptCode.setShortDescription(hcp.getDescription());
				cptCode.setRetired(false);
				cptCodeDao.saveOrUpdate(cptCode);
				logger.debug("CPTCode added: "+ cptCode.getCode());
			}
		}
	}

	public void run() {
		// clear the update table first
		String qry = "delete from hospital_charge_update";
		Query query = em.createNativeQuery(qry);
		try {
			// here should be query.executeUpdate(), but cannot work, donot know
			// reason now
			query.getSingleResult();
		} catch (Exception e) {
			// do nothing
		}
		
		// update the update table first
		List<HospitalChargeUpdate> hospitalChargeUpdates = fetchHospitalCostData();
		Map<String, HospitalChargeUpdate> finalUpdateMap = new HashMap<String, HospitalChargeUpdate>();
		List<String> finalCptCodeList = new ArrayList<String>();
		for (HospitalChargeUpdate hospitalChargeUpdate : hospitalChargeUpdates) {
			if (finalUpdateMap.containsKey(hospitalChargeUpdate.getCptCode())) {
				if (hospitalChargeUpdate.getCost().toString().equals("0.00")) {
					continue;
				}
			}
			finalUpdateMap.put(hospitalChargeUpdate.getCptCode(),
					hospitalChargeUpdate);
			finalCptCodeList.add(hospitalChargeUpdate.getCptCode());
		}
		for (String cptCode : finalCptCodeList) {
			HospitalChargeUpdate hospitalChargeUpdate = finalUpdateMap
					.get(cptCode);
			try {
				HospitalChargeUpdate existHospitalChargeUpdate = hospitalChargeUpdateDao
						.findByCptCode(hospitalChargeUpdate.getCptCode())
						.get(0);
				existHospitalChargeUpdate.setCost(hospitalChargeUpdate
						.getCost());
				existHospitalChargeUpdate.setDescription(hospitalChargeUpdate
						.getDescription());
				hospitalChargeUpdateDao.saveOrUpdate(existHospitalChargeUpdate);
			} catch (Exception e) {
				hospitalChargeUpdate.setRetired(false);
				hospitalChargeUpdate = hospitalChargeUpdateDao
						.saveOrUpdate(hospitalChargeUpdate);
				logger.debug("added" + " " + hospitalChargeUpdate.getCptCode()
						+ ": " + hospitalChargeUpdate.getCost());
			}

		}

		// update hospital_charge according to update table
		updateHospitalCost();
		
		//add cptcode not in cpt table
		addHospitalCptToCPTTable();
	}

	public HospitalChargeUpdateDao getHospitalChargeUpdateDao() {
		return hospitalChargeUpdateDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeUpdateDao(
			HospitalChargeUpdateDao hospitalChargeUpdateDao) {
		this.hospitalChargeUpdateDao = hospitalChargeUpdateDao;
	}

	public HospitalChargeProcedureDao getHospitalChargeProcedureDao() {
		return hospitalChargeProcedureDao;
	}

	@Autowired(required = true)
	public void setHospitalChargeProcedureDao(
			HospitalChargeProcedureDao hospitalChargeProcedureDao) {
		this.hospitalChargeProcedureDao = hospitalChargeProcedureDao;
	}
	
	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	@Autowired(required = true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}
}
