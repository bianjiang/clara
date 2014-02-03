package edu.uams.clara.integration.incoming.billingcodes;

import static org.junit.Assert.*;

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

import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.CategoryCodeDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;
import edu.uams.clara.webapp.protocol.domain.budget.code.CategoryCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/CategoryCodeUpdateTest-context.xml" })
public class CategoryCodeUpdateTest {

	private ResourceLoader resourceLoader;
	private CPTCodeDao cptCodeDao;
	private CategoryCodeDao categoryCodeDao;
	private final static Logger logger = LoggerFactory
			.getLogger(CategoryCodeUpdateTest.class);

	//@Test
	public void updateCategory() {
		try {
			String rawdata = null;

			String csvFileName = "import data/UB92-Revenue Codes.csv";

			Resource csvFile = resourceLoader.getResource(csvFileName);

			InputStream in = csvFile.getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));

			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				if (item.length < 2)
					continue;

				if(item[0].trim().length()<4)
				item[0] = "0" + item[0].trim();

				CategoryCode categoryCode = categoryCodeDao.findByCode(item[0]);

				if (categoryCode == null) {
					categoryCode = new CategoryCode();
				}

				categoryCode.setCode(item[0]);
				categoryCode.setDescription(item[1]);
				// logger.debug(item[0]);
				categoryCodeDao.saveOrUpdate(categoryCode);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void updateCptCategory() {
		try {
			String rawdata = null;

			String csvFileName = "import data/CategoryCode.csv";

			Resource csvFile = resourceLoader.getResource(csvFileName);

			InputStream in = csvFile.getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));

			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				if (item.length < 2)
					continue;

				CPTCode cptCode = cptCodeDao.findByCode(item[0].trim());

				if(cptCode == null)
					continue;

				if(item[1].trim().length()<4)
				item[1] = "0" + item[1].trim();

				CategoryCode categoryCode = categoryCodeDao.findByCode(item[1]);

				cptCode.setCategoryCode(categoryCode);

				cptCodeDao.saveOrUpdate(cptCode);


			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

	@Autowired(required = true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}

	public CategoryCodeDao getCategoryCodeDao() {
		return categoryCodeDao;
	}

	@Autowired(required = true)
	public void setCategoryCodeDao(CategoryCodeDao categoryCodeDao) {
		this.categoryCodeDao = categoryCodeDao;
	}

}
