package edu.uams.clara.webapp.protocol.dao.thing;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.core.dao.thing.ThingDao;
import edu.uams.clara.core.dao.thing.etl.ThingUpdateDao;
import edu.uams.clara.core.domain.thing.Thing;
import edu.uams.clara.core.domain.thing.etl.ThingUpdate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/dao/thing/ICD9UpdateTest-context.xml" })
public class ICD9UpdateTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ICD9UpdateTest.class);

	private ResourceLoader resourceLoader;
	private ThingDao thingDao;
	private ThingUpdateDao thingUpdateDao;

	// @Test
	public void addAndUpdateICD9DX() throws IOException, InvalidFormatException {

		// read data from file
		Workbook workbook = WorkbookFactory.create(new FileInputStream(
				"c://CMS30_DESC_LONG_SHORT_DX 080612.xlsx"));
		Sheet sheet = workbook.getSheetAt(0);

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			Row row = sheet.getRow(i);

			Thing icd9DX = new Thing();

			icd9DX.setType("ICD_9_DIAG");
			icd9DX.setApproved(true);
			icd9DX.setRetired(false);
			String identifier = row.getCell(0).getStringCellValue();
			identifier = identifier.substring(0, 3) + "."
					+ identifier.substring(3, identifier.length());
			icd9DX.setIdentifier(identifier);
			icd9DX.setDescription(row.getCell(1).getStringCellValue());
			icd9DX.setValue(row.getCell(2).getStringCellValue());

			thingDao.saveOrUpdate(icd9DX);
		}

		workbook = WorkbookFactory.create(new FileInputStream(
				"c://CMS30_DESC_LONG_SHORT_SG 051812.xlsx"));
		sheet = workbook.getSheetAt(0);

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			Row row = sheet.getRow(i);

			Thing icd9SG = new Thing();
			icd9SG.setType("ICD_9_PROC");

			icd9SG.setApproved(true);
			icd9SG.setRetired(false);
			String identifier = row.getCell(0).getStringCellValue();
			identifier = identifier.substring(0, 2) + "."
					+ identifier.substring(2, identifier.length());
			icd9SG.setIdentifier(identifier);
			icd9SG.setDescription(row.getCell(1).getStringCellValue());
			icd9SG.setValue(row.getCell(2).getStringCellValue());

			thingDao.saveOrUpdate(icd9SG);
		}

	}

	@Test
	public void addAndUpdateICD9DXForUpdateTable() throws IOException,
			InvalidFormatException {

		// read data from file
		Workbook workbook = WorkbookFactory.create(new FileInputStream(
				"c://CMS30_DESC_LONG_SHORT_DX 080612.xlsx"));
		Sheet sheet = workbook.getSheetAt(0);

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			Row row = sheet.getRow(i);

			ThingUpdate icd9DX = new ThingUpdate();

			icd9DX.setType("ICD_9_DIAG");
			icd9DX.setApproved(true);
			icd9DX.setRetired(false);
			String identifier = row.getCell(0).getStringCellValue();
			identifier = identifier.substring(0, 3) + "."
					+ identifier.substring(3, identifier.length());
			icd9DX.setIdentifier(identifier);
			icd9DX.setDescription(row.getCell(1).getStringCellValue());
			icd9DX.setValue(row.getCell(2).getStringCellValue());

			thingUpdateDao.saveOrUpdate(icd9DX);
		}

		workbook = WorkbookFactory.create(new FileInputStream(
				"c://CMS30_DESC_LONG_SHORT_SG 051812.xlsx"));
		sheet = workbook.getSheetAt(0);

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			Row row = sheet.getRow(i);

			ThingUpdate icd9SG = new ThingUpdate();

			icd9SG.setType("ICD_9_PROC");
			icd9SG.setApproved(true);
			icd9SG.setRetired(false);
			String identifier = row.getCell(0).getStringCellValue();
			identifier = identifier.substring(0, 2) + "."
					+ identifier.substring(2, identifier.length());
			icd9SG.setIdentifier(identifier);
			icd9SG.setDescription(row.getCell(1).getStringCellValue());
			icd9SG.setValue(row.getCell(2).getStringCellValue());

			thingUpdateDao.saveOrUpdate(icd9SG);
		}

	}

	//@Test
	public void updateICD9ForDeletedItem() {
		List<Thing> ICD9OldList = new ArrayList<Thing>();
		List<String> types = new ArrayList<String>();
		types.add("ICD_9_PROC");
		types.add("ICD_9_DIAG");
		ICD9OldList = thingDao.searchByTypes(types);

		for (int i = 0; i < ICD9OldList.size(); i++) {
			if (thingUpdateDao.findByIdentifierAndType(ICD9OldList.get(i)
					.getIdentifier(), ICD9OldList.get(i).getType()) == null) {
				ICD9OldList.get(i).setRetired(true);
				thingDao.saveOrUpdate(ICD9OldList.get(i));
			}
		}

	}

	// @Test
	public void searchBykeyword() {
		List<String> types = new ArrayList<String>();

		types.add("ICD_9_PROC");
		types.add("ICD_9_DIAG");

		logger.debug(""
				+ thingDao.searchByKeywordsAndTypes("Therapeutic", types)
						.size());
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ThingDao getThingDao() {
		return thingDao;
	}

	@Autowired(required = true)
	public void setThingDao(ThingDao thingDao) {
		this.thingDao = thingDao;
	}

	public ThingUpdateDao getThingUpdateDao() {
		return thingUpdateDao;
	}

	@Autowired(required = true)
	public void setThingUpdateDao(ThingUpdateDao thingUpdateDao) {
		this.thingUpdateDao = thingUpdateDao;
	}

}
