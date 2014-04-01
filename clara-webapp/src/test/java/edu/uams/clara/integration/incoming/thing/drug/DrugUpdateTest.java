package edu.uams.clara.integration.incoming.thing.drug;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.thing.DrugDao;
import edu.uams.clara.webapp.protocol.domain.thing.Drug;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/thing/drug/DrugUpdateTest-context.xml" })
public class DrugUpdateTest {

	private DrugDao drugDao;
	private ResourceLoader resourceLoader;
	private final static Logger logger = LoggerFactory
			.getLogger(DrugUpdateTest.class);

	@Test
	public void drugUpdate() {
		try {
			String rawdata = null;

			String csvFileName = "import data/drug.csv";

			Resource csvFile = resourceLoader.getResource(csvFileName);

			InputStream in = csvFile.getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));

			while ((rawdata = reader.readLine()) != null) {

				String item[] = rawdata.split(",");
				
				if(item.length<2)
					continue;

				Drug drug = drugDao.findByIdentifier(item[0].trim());

				//create a new drug if drug is not existed
				if (drug == null) {
					drug = new Drug();

				}

				drug.setApproved(true);
				drug.setIdentifier(item[0].trim());
				drug.setDescription(item[1]);
				drug.setValue(item[1]);
				
				//logger.debug(item[0]);

				drugDao.saveOrUpdate(drug);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DrugDao getDrugDao() {
		return drugDao;
	}

	@Autowired(required = true)
	public void setDrugDao(DrugDao drugDao) {
		this.drugDao = drugDao;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
