package edu.uams.clara.integration.incoming.billingcodes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import edu.uams.clara.integration.incoming.billingcodes.dao.PhysicianChargeUpdateDao;
import edu.uams.clara.integration.incoming.billingcodes.domain.PhysicianChargeUpdate;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianLocationCodeDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianChargeProcedure;
import edu.uams.clara.webapp.protocol.domain.budget.code.PhysicianLocationCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/PhysicianChargeProcedureDaoTest-context.xml" })
public class PhysicianChargeProcedureDaoTest {

	private final static Logger logger = LoggerFactory
			.getLogger(PhysicianChargeProcedureDaoTest.class);

	private PhysicianChargeProcedureDao physicianChargeProcedureDao;
	private ResourceLoader resourceLoader;
	private PhysicianLocationCodeDao physicianLocationCodeDao;
	private PhysicianChargeUpdateDao physicianChargeUpdateDao;

	// @Test
	public void testFindByCptCode() {

		List<PhysicianChargeProcedure> physicianChargeProcedures = physicianChargeProcedureDao
				.findByCptCode("M0076    ");
		/*
		 * logger.debug("111" +
		 * physicianChargeProcedureDao.findbywithMultiple("M7006", "1376",
		 * "4MRE").getId());
		 */
		for (PhysicianChargeProcedure pcp : physicianChargeProcedures) {
			logger.debug("*****" + pcp.getId() + "; cptCode: "
					+ pcp.getCptCode() + ":" + pcp.getDescription());
			logger.info("" + pcp.getLocationCode().getCode());

		}
	}

	// @Test
	public void createTable() {
		try {
			String rawdata = null;
			float price = 0;

			BufferedReader reader = new BufferedReader(new FileReader(
					"C:\\DOCUME~1\\yuanjiawei\\Desktop\\Book1.csv"));
			// headline
			// reader.readLine();

			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date verifyDate = new Date();
			verifyDate = sdf.parse("2011-01-01");

			// int i =0;
			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				PhysicianChargeProcedure physicianChargeProcedure = new PhysicianChargeProcedure();

				String s[] = item[5].split("/");
				/*
				 * item[0] tmcode item[1] Description item[2] LocationCode
				 * item[3] cptcode item[4] cost item[5] effectivedate item[6]
				 * location Code description
				 */

				/*
				 * i++; logger.debug(i+"th s is *** "+item[1]);
				 */
				item[5] = s[2] + "-" + s[0] + "-" + s[1];
				Date effectiveDate = new Date();

				effectiveDate = sdf.parse(item[5]);

				if (item[2].equals("0"))
					item[2] = "00000";

				if (item[2].contains("4M")
						|| (item[2].equals("00000") && effectiveDate
								.after(verifyDate))) {
					Number number = NumberFormat.getInstance().parse(item[4]);
					price = (float) (number.floatValue());
					physicianChargeProcedure.setTmCode(item[0].trim());
					String subs = item[0].substring((item[0].length() - 2),
							item[0].length());
					if (item[0].length() > 6 && subs.contains("28"))
						physicianChargeProcedure.setPhysicianOnly(true);
					else
						physicianChargeProcedure.setPhysicianOnly(false);

					physicianChargeProcedure.setDescription(item[1]);

					// add new location code if location not exist
					if (physicianLocationCodeDao.findByLocationCode(item[2]
							.trim()) == null) {
						PhysicianLocationCode physicianLocationCode = new PhysicianLocationCode();
						physicianLocationCode.setCode(item[2].trim());
						physicianLocationCode.setDescription(item[6]);

						physicianLocationCodeDao
								.saveOrUpdate(physicianLocationCode);
					}

					physicianChargeProcedure
							.setLocationCode(physicianLocationCodeDao
									.findByLocationCode(item[2].trim()));
					item[3] = item[3].trim();
					if (item[3].length() > 5)
						item[3] = item[3].substring(0, 5);
					// logger.debug(item[3]);
					physicianChargeProcedure.setCptCode(item[3].trim());

					BigDecimal bdPrice = new BigDecimal((double) price);
					bdPrice = bdPrice.setScale(2, 4);

					physicianChargeProcedure.setCost(bdPrice);
					physicianChargeProcedure.setEffectiveDate(effectiveDate);

					physicianChargeProcedureDao
							.saveOrUpdate(physicianChargeProcedure);

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void CreateupdateTable() {
		try {
			String rawdata = null;
			float price = 0;

			// read csv from file
			BufferedReader reader = new BufferedReader(new FileReader(
					"C:\\DOCUME~1\\yuanjiawei\\Desktop\\Book1.csv"));

			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date verifyDate = new Date();
			verifyDate = sdf.parse("2011-01-01");

			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				PhysicianChargeUpdate physicianChargeUpdate = new PhysicianChargeUpdate();

				String s[] = item[5].split("/");
				/*
				 * item[0] tmcode item[1] Description item[2] LocationCode
				 * item[3] cptcode item[4] cost item[5] effectivedate item[6]
				 * location Code description
				 */
				item[5] = s[2] + "-" + s[0] + "-" + s[1];
				Date effectiveDate = new Date();

				effectiveDate = sdf.parse(item[5]);

				if (item[2].equals("0"))
					item[2] = "00000";

				if (item[2].contains("4M")
						|| (item[2].equals("00000") && effectiveDate
								.after(verifyDate))) {
					Number number = NumberFormat.getInstance().parse(item[4]);
					price = (float) (number.floatValue());
					physicianChargeUpdate.setTmCode(item[0].trim());
					String subs = item[0].substring((item[0].length() - 2),
							item[0].length());
					if (item[0].length() > 6 && subs.contains("28"))
						physicianChargeUpdate.setPhysicianOnly(true);
					else
						physicianChargeUpdate.setPhysicianOnly(false);

					physicianChargeUpdate.setDescription(item[1]);

					// add new location code if location not exist
					if (physicianLocationCodeDao.findByLocationCode(item[2]
							.trim()) == null) {
						PhysicianLocationCode physicianLocationCode = new PhysicianLocationCode();
						physicianLocationCode.setCode(item[2].trim());
						physicianLocationCode.setDescription(item[6]);

						physicianLocationCodeDao
								.saveOrUpdate(physicianLocationCode);
					}

					physicianChargeUpdate
							.setLocationCode(physicianLocationCodeDao
									.findByLocationCode(item[2].trim()));
					if (item[3].length() > 5)
						item[3] = item[3].substring(0, 5);
					physicianChargeUpdate.setCptCode(item[3].trim());
					BigDecimal bdPrice = new BigDecimal((double) price);
					bdPrice = bdPrice.setScale(2, 4);

					physicianChargeUpdate.setCost(bdPrice);
					physicianChargeUpdate.setEffectiveDate(effectiveDate);

					if (physicianChargeProcedureDao.findbywithMultiple(
							item[3].trim(), item[0].trim(), item[2].trim()) != null)
						physicianChargeUpdate
								.setPhysicanProcedureId(physicianChargeProcedureDao
										.findbywithMultiple(item[3].trim(),
												item[0].trim(), item[2].trim())
										.getId());
					else
						physicianChargeUpdate.setPhysicanProcedureId(0);

					physicianChargeUpdateDao
							.saveOrUpdate(physicianChargeUpdate);

				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Test
	public void updateInfo() {
		
		
		List<PhysicianChargeUpdate> updateList = physicianChargeUpdateDao
				.findAll();
		for (int i = 0; i < updateList.size(); i++) {
			PhysicianChargeProcedure physicianChargeProcedure = null;
			PhysicianChargeUpdate physicianChargeUpdate = updateList.get(i);
			try{
				physicianChargeProcedure  = physicianChargeProcedureDao.findbywithMultiple(physicianChargeUpdate.getCptCode(), physicianChargeUpdate.getTmCode(), physicianChargeUpdate.getLocationCode()+"");
			}
			catch (Exception e){
				physicianChargeProcedure= null;
			}
			if(physicianChargeProcedure== null)
				physicianChargeProcedure= new PhysicianChargeProcedure();
				physicianChargeProcedure.setCost( updateList.get(i).getCost());
				physicianChargeProcedure.setCptCode(updateList.get(i)
						.getCptCode());
				physicianChargeProcedure.setDescription(updateList.get(i)
						.getDescription());
				physicianChargeProcedure.setEffectiveDate(updateList.get(i)
						.getEffectiveDate());
				physicianChargeProcedure.setLocationCode(updateList.get(i)
						.getLocationCode());
				physicianChargeProcedure.setTmCode(updateList.get(i)
						.getTmCode());

				String subs = updateList
						.get(i)
						.getTmCode()
						.substring(
								(updateList.get(i).getTmCode().length() - 2),
								updateList.get(i).getTmCode().length());
				if (updateList.get(i).getTmCode().length() > 6
						&& subs.contains("28"))
					physicianChargeProcedure.setPhysicianOnly(true);
				else
					physicianChargeProcedure.setPhysicianOnly(false);
				physicianChargeProcedureDao
						.saveOrUpdate(physicianChargeProcedure);

		}
		
		List<PhysicianChargeProcedure> existingList = physicianChargeProcedureDao.findAll();
		for(int j=0;j<existingList.size();j++){
			PhysicianChargeProcedure physicianChargeProcedure = existingList.get(j);
			if(physicianChargeUpdateDao.findbywithMultiple(physicianChargeProcedure.getCptCode(), physicianChargeProcedure.getTmCode(), physicianChargeProcedure.getLocationCode()+"")==null);{
				physicianChargeProcedure.setRetired(true);
			}
		}

	}

	@Autowired(required = true)
	public void setPhysicianChargeProcedureDao(
			PhysicianChargeProcedureDao physicianChargeProcedureDao) {
		this.physicianChargeProcedureDao = physicianChargeProcedureDao;
	}

	public PhysicianChargeProcedureDao getPhysicianChargeProcedureDao() {
		return physicianChargeProcedureDao;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public PhysicianLocationCodeDao getPhysicianLocationCode() {
		return physicianLocationCodeDao;
	}

	@Autowired(required = true)
	public void setPhysicianLocationCode(
			PhysicianLocationCodeDao physicianLocationCodeDao) {
		this.physicianLocationCodeDao = physicianLocationCodeDao;
	}

	public PhysicianChargeUpdateDao getPhysicianChargeUpdateDao() {
		return physicianChargeUpdateDao;
	}

	@Autowired(required = true)
	public void setPhysicianChargeUpdateDao(
			PhysicianChargeUpdateDao physicianChargeUpdateDao) {
		this.physicianChargeUpdateDao = physicianChargeUpdateDao;
	}
}
