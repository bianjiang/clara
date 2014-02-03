package edu.uams.clara.integration.outgoing.epic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Lists;

import au.com.bytecode.opencsv.CSVWriter;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/epic/GenerateHopsitalFeeFromCrimsonTest-context.xml" })
public class GenerateHopsitalFeeFromCrimsonTest {
	private final static Logger logger = LoggerFactory
			.getLogger(GenerateHopsitalFeeFromCrimsonTest.class);

	private EntityManager em;
	private ProtocolDao protocolDao;

	@Test
	public void generateHBFee() throws IOException {
		String qry = "SELECT ct.num_ct_ID,ct.[num_ct_type_ID],ct.[num_irb_ID], LEFT(CONVERT(VARCHAR, [date_completed], 120),10), CAST(ct.txt_title as VARCHAR(max)) as title FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[ct_report_info] as ri inner join [HOSP_SQL1].[ClinicalResearch].[dbo].[ct] as ct on ct.[num_ct_ID]=ri.[num_ct_ID] and ri.[num_ct_ID] in (select MAX([num_ct_ID]) from [HOSP_SQL1].[ClinicalResearch].[dbo].[ct] where  [num_ct_ID] in (select [num_ct_ID] from [HOSP_SQL1].[ClinicalResearch].[dbo].[budget]) and [num_ct_ID] in (SELECT [num_ct_ID] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[ct_report_info] where date_completed is not null) and [num_irb_ID] in( select distinct [num_irb_ID] from [HOSP_SQL1].[ClinicalResearch].[dbo].[ct] where [num_ct_status] = 13  and  [num_irb_ID] in (select [num_irb_ID] from [HOSP_SQL1].[ClinicalResearch].[dbo].[irb] where [txt_irb_Status] not like 'Closed')) group by [num_irb_ID])";
		Query queryStart = em.createNativeQuery(qry);
		List<Object[]> resultList = (List<Object[]>) queryStart.getResultList();
		List<String> targeList=Lists.newArrayList();
		targeList.add("135334");
		/*targeList.add("78076");
		targeList.add("104727");
		targeList.add("110396");
		targeList.add("112317");
		targeList.add("113036");
		targeList.add("113234");
		targeList.add("113322");
		targeList.add("130654");
		targeList.add("130766");
		targeList.add("131390");
		targeList.add("131471");
		targeList.add("131573");
		targeList.add("131753");
		targeList.add("132754");
		targeList.add("132762");
		targeList.add("132766");
		targeList.add("132843");
		targeList.add("133398");
		targeList.add("133529");
		targeList.add("133965");
		targeList.add("134190");
		targeList.add("112748");
		targeList.add("134633");
		targeList.add("134768");
		targeList.add("134922");
		targeList.add("134928");
		targeList.add("135691");
		targeList.add("135910");
		targeList.add("136191");
		targeList.add("138724");*/

		List<String[]> results = Lists.newArrayList();
		for (Object[] infoobj : resultList) {
			try {
				// logger.debug(strLine);
				String date = (String) infoobj[3];
				int irb = (Integer) infoobj[2];
				int ctId = (Integer) infoobj[0];
				short ctType = (short) infoobj[1];
				String title =(String)infoobj[4];
				if(!targeList.contains(irb+"")){
					continue;
				}

				try {
					protocolDao.findById(irb);
				} catch (Exception e) {
					logger.debug(irb + " not exist in CLARA!!!  Skip it!!!");
					continue;
				}
				List<String> uniqueResukt =Lists.newArrayList();
				File writeFile = new File("C:\\Data\\Crimson2\\" + irb + " HB "
						+ date + ".csv");
				CSVWriter writer = new CSVWriter(new FileWriter(writeFile));
				String[] titleName ={"Title: "+title};
				writer.writeNext(titleName);
				String[] titleLine ={"IRB#","CPT Code","Cost","Notes"};
				writer.writeNext(titleLine);


				qry = "select [first]+'  '+[lname] from [HOSP_SQL1].[ClinicalResearch].[dbo].aria_users where [pi_serial] =(select [num_pi] from [HOSP_SQL1].[ClinicalResearch].[dbo].[ct] where num_ct_ID ="+ctId+")";
				Query query = em.createNativeQuery(qry);
				String piname =  (String) query.getSingleResult();

				qry ="select txt_reg_num from [HOSP_SQL1].[ClinicalResearch].[dbo].[ct_reg] where num_ct_ID ="+ctId;
				Query query2 = em.createNativeQuery(qry);
				String nct =  (String) query2.getSingleResult();
				String [] entry ={irb+"",title,piname,nct};
				results.add(entry);
				qry = "SELECT fee.[num_fee_amount_fixed] ,fee.[num_fee_amount_formula],fee.[txt_fee_cpt], arm.txt_arm_name ,item.[txt_item_notes] "
						+ " FROM [HOSP_SQL1].[ClinicalResearch].[dbo].[fee] fee "
						+ "inner join [HOSP_SQL1].[ClinicalResearch].[dbo].[item] item on fee.num_fee_id  = item.[num_fee_ID]  "
						+ "inner join [HOSP_SQL1].[ClinicalResearch].[dbo].[arm] arm on arm.[num_arm_ID] = item.num_arm_id "
						+ "and arm.[num_budget_ID] in (select [num_budget_ID] FROM [HOSP_SQL1].[ClinicalResearch].[dbo].budget where num_ct_ID ="+ctId+")";
				query = em.createNativeQuery(qry);
				List<Object[]> feeList = (List<Object[]>) query.getResultList();
				for (Object[] obj : feeList) {

					String notes = (String)obj[4];
					String cpt = (String) obj[2];
					String cost = "";

					if (ctType != 1) {
						cost="" + (double) obj[0];
					} else {
						cost="" + (double) obj[1];
					}
					String[] entry2 = {irb+"",cpt,cost,notes};
					if(uniqueResukt.contains((irb+cpt+cost+notes).trim())){
						continue;
					}
					writer.writeNext(entry2);
					uniqueResukt.add((irb+cpt+cost+notes).trim());
				}
				writer.flush();
				writer.close();

			} catch (Exception e) {
				e.printStackTrace();

			}
			File writeFile = new File("C:\\Data\\Crimson2\\" + " HB "
					+  ".csv");
			CSVWriter writer = new CSVWriter(new FileWriter(writeFile));
			for(String[] entry: results){


				writer.writeNext(entry);
			}
			writer.flush();
			writer.close();
		}

	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}
}
