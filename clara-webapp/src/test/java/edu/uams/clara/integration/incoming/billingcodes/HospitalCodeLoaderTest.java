package edu.uams.clara.integration.incoming.billingcodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/HospitalCodeLoaderTest-context.xml" })
public class HospitalCodeLoaderTest {

	private final static Logger logger = LoggerFactory
			.getLogger(HospitalCodeLoaderTest.class);

	private ResourceLoader resourceLoader;

	@PersistenceUnit(unitName = "defaultPersistenceUnit")
	private EntityManagerFactory emf;

	public enum Field {
		DEPT_CODE(3, 7),
		DESCRIPTION(7, 47),
		CLAIM_CLASS(47, 50),
		ACTIVE_CODE(50, 51),
		TRANSACTION_CODE(51, 59),
		BILLING_DESCRIPTION(59, 90),
		INPATIENT_PRICE(90, 101),
		OUTPATIENT_PRICE(101, 113),
		INSURANCE_COVERAGE_CODE(113,117),
		MEDICARE_CODE(117, 124),
		MEDICAID_CODE(126, 133),
		COMMERCIAL_CODE(135, 142),
		CAMPUS_CODE(144, 151),
		BLUE_CROSS_CODE(153, 160),
		CDM_UB82(162, 166),
		CDM_STRUCTURE(166, 167),
		CDM_OVERRIDE(167, 168),
		CDM_UB82_B(244, 248),
		EFFECTIVE_DATE(168, 176);
		
		private int start;
		private int end;
		
		private Field(int start, int end){
			this.start = start;
			this.end = end;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getStart() {
			return start;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		public int getEnd() {
			return end;
		}
		
	}
	
	private String getFieldValue(final String strLine, Field field){
		return strLine.substring(field.start, field.end).trim();
	}
	
	
	@Test
	public void loadCDMintoTempTable() throws IOException {
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		String q = "IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[MPAC_CDM]') AND type in (N'U'))";
		q += " DROP TABLE [dbo].[MPAC_CDM]";

		Query query = em.createNativeQuery(q);
		query.executeUpdate();
		q = "CREATE TABLE [dbo].[MPAC_CDM] ( ";
		
		for (Field field : Field.values()) {
			switch (field) {
			case TRANSACTION_CODE:
				q += "[" + field + "] [varchar](" + (field.end - field.start)
				+ ") NOT NULL, ";
				break;
			case EFFECTIVE_DATE:
				q += "[" + field + "] [datetime] NULL";
				break;
			default:
				q += "[" + field + "] [varchar](" + (field.end - field.start)
						+ ") NULL, ";
				break;
			}

		}
		
		//q += " CONSTRAINT [PK_" + Field.TRANSACTON_CODE + "] PRIMARY KEY CLUSTERED (";
		//q += " [" + Field.TRANSACTON_CODE + "] ASC) ";
		q += " ) ON [PRIMARY]";
		
		query = em.createNativeQuery(q);
		
		query.executeUpdate();
		
		em.flush();
		
		Resource cdmTxtFile = resourceLoader
				.getResource("budgetcode/KPCDMDLD.txt");

		InputStream in = cdmTxtFile.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine = null;
		
		String value = null;
		
		String insertQuery = "INSERT INTO [dbo].[MPAC_CDM] (";
		
		for (Field field : Field.values()) {
			switch (field) {
			case EFFECTIVE_DATE://last one
				insertQuery += "[" + field + "] ";
				break;
			default:
				insertQuery += "[" + field + "], ";
				break;
			}			
		}
		
		insertQuery += ") VALUES (";
		
		for (Field field : Field.values()) {
			switch (field) {
			case EFFECTIVE_DATE://last one
				insertQuery += ":" + field + " ";
				break;
			default:
				insertQuery += ":" + field + ", ";
				break;
			}			
		}
		insertQuery += ")";
		
		query = em.createNativeQuery(insertQuery);
		
		int i = 0;
		while((strLine = br.readLine()) != null){
						
			for(Field field:Field.values()){
				value = getFieldValue(strLine, field);
				query.setParameter(field.toString(), value);
			}	
						
			query.executeUpdate();
			
			if(i % 100 == 0){
				em.flush();
				em.clear();
			}
			
			i ++;
		}
				
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
