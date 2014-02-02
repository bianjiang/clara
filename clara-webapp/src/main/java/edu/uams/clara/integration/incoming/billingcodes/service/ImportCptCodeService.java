package edu.uams.clara.integration.incoming.billingcodes.service;

import java.io.FileReader;
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

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.integration.incoming.billingcodes.dao.CPTCodeUpdateDao;
import edu.uams.clara.integration.incoming.billingcodes.domain.CPTCodeUpdate;
import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;

public class ImportCptCodeService {
	private final static Logger logger = LoggerFactory
			.getLogger(ImportCptCodeService.class);

	private CPTCodeDao cptCodeDao;
	private CPTCodeUpdateDao cptCodeUpdateDao;
	private EntityManager em;

	private final static String[] longmappings = new String[] { "code",
			"longDescription" };

	private final static String[] mediummappings = new String[] { "code",
			"mediumDescription", "shortDescription" };

	public void run() {
		
		// clear the cptcodeupdate table first
		String qry = "delete from cpt_code_update";
		Query query = em.createNativeQuery(qry);
		try {
			// here should be query.executeUpdate(), but cannot work, donot know
			// reason now
			query.getSingleResult();
		} catch (Exception e) {
			// do nothing
		}
		// get data from file
		List<CPTCodeUpdate> cptCodeUpdates = fetchCptData();
		
		// update cptcode according the cptupdatetable
		updateCptCode(cptCodeUpdates);

	}

	private void updateCptCode(List<CPTCodeUpdate> cptCodeUpdates) {
		// add new cptcode
		Set<CPTCode> cptUpdateSet = Sets.newHashSet();
		for (CPTCodeUpdate cptCodeUpdate : cptCodeUpdates) {
			CPTCode cptCode = new CPTCode();
			try {
				cptCode = cptCodeDao.findByCode(cptCodeUpdate.getCode());
			} catch (Exception e) {
				// do nothing
			}
			if (cptCode == null) {
				cptCode = new CPTCode();
				// in the cd copy, we donot have the short description, so keep
				// the old short description if existing
				cptCode.setShortDescription(cptCodeUpdate.getShortDescription());
			}
			cptCode.setRetired(false);
			cptCode.setCode(cptCodeUpdate.getCode());
			cptCode.setLongDescription(cptCodeUpdate.getLongDescription());
			cptCode.setMediumDescription(cptCodeUpdate.getMediumDescription());
			try{
			cptCodeDao.saveOrUpdate(cptCode);

			cptUpdateSet.add(cptCode);
			}catch(Exception e){
				logger.debug("error occurs: "+cptCode.getCode());
			}
		}

		// retire cptcode not in latest copy
		List<CPTCode> cpts = cptCodeDao.findAll();
		Set<CPTCode> cptFullSet = Sets.newHashSet(cpts);

		Set<CPTCode> retireCptSets = Sets.difference(cptFullSet, cptUpdateSet);
		for (CPTCode cpt : retireCptSets) {
			cpt.setRetired(true);
			cptCodeDao.saveOrUpdate(cpt);
		}
	}

	private List<CPTCodeUpdate> fetchCptData() {
		List<CPTCodeUpdate> cptCodeUpdates = Lists.newArrayList();
		try {
			CSVReader csvReader = new CSVReader(new FileReader(
					"C:\\Data\\cpt2013.csv"));
			int numberOfColumns = longmappings.length;
			csvReader.readNext(); // skip header
			String[] nextLine;

			while ((nextLine = csvReader.readNext()) != null) {
				CPTCodeUpdate cptCodeUpdate = new CPTCodeUpdate();
				Map<String, Object> properties;
				properties = BeanUtils.describe(cptCodeUpdate);
				for (int i = 0; i < numberOfColumns; i++) {
					properties.put(longmappings[i], nextLine[i].trim());
				}
				BeanUtils.populate(cptCodeUpdate, properties);
				cptCodeUpdates.add(cptCodeUpdate);
				cptCodeUpdateDao.saveOrUpdate(cptCodeUpdate);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("long inported....");
		//add medium and short description
		try {
			CSVReader csvReader = new CSVReader(new FileReader(
					"C:\\Data\\cpt2013-medium.csv"));
			int numberOfColumns = mediummappings.length;
			csvReader.readNext(); // skip header
			String[] nextLine;

			while ((nextLine = csvReader.readNext()) != null) {
				CPTCodeUpdate cptCodeUpdate = new CPTCodeUpdate();
				Map<String, Object> properties;
				properties = BeanUtils.describe(cptCodeUpdate);
				for (int i = 0; i < numberOfColumns; i++) {
					properties.put(mediummappings[i], nextLine[i].trim());
				}
				BeanUtils.populate(cptCodeUpdate, properties);
				cptCodeUpdates.add(cptCodeUpdate);
				CPTCodeUpdate existingcptCodeUpdate =new CPTCodeUpdate();
				try{
					existingcptCodeUpdate=cptCodeUpdateDao.findCPTCodeUpdateByCptCode(cptCodeUpdate.getCode());
					existingcptCodeUpdate.setMediumDescription(cptCodeUpdate.getMediumDescription());
					existingcptCodeUpdate.setShortDescription(cptCodeUpdate.getShortDescription());
					cptCodeUpdateDao.saveOrUpdate(existingcptCodeUpdate);
				}catch(Exception e){
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		


		return cptCodeUpdateDao.findAll();
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	@Autowired(required = true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}

	public CPTCodeUpdateDao getCptCodeUpdateDao() {
		return cptCodeUpdateDao;
	}

	@Autowired(required = true)
	public void setCptCodeUpdateDao(CPTCodeUpdateDao cptCodeUpdateDao) {
		this.cptCodeUpdateDao = cptCodeUpdateDao;
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

}
