package edu.uams.clara.migration.service;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.migration.service.impl.ARIALetterMigrationImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/migration/service/ARIALetterMigrationImplTest-context.xml" })
public class ARIALetterMigrationImplTest {
	private ARIALetterMigrationImpl ariaLetterMigrationService;

	@Test
	public void test() throws IOException {
		ariaLetterMigrationService.migrateLetter();
	}

	//@Test
	public void delete() throws IOException, XPathExpressionException {
		ariaLetterMigrationService.deleteErrorLetter();;
	}

	public ARIALetterMigrationImpl getAriaLetterMigrationService() {
		return ariaLetterMigrationService;
	}

	@Autowired(required=true)
	public void setAriaLetterMigrationService(ARIALetterMigrationImpl ariaLetterMigrationService) {
		this.ariaLetterMigrationService = ariaLetterMigrationService;
	}

}
