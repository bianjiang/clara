package edu.uams.clara.integration.incoming.billingcodes;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.billingcodes.service.ImportHospitalChargeDataService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/billingcodes/ImportHospitalChargeCostServiceTest-context.xml" })
public class ImportHospitalChargeCostServiceTest {

	private ImportHospitalChargeDataService importHospitalChargeDataService;

	@Test
	public void test() {
		importHospitalChargeDataService.run();
	}

	public ImportHospitalChargeDataService getImportHospitalChargeDataService() {
		return importHospitalChargeDataService;
	}

	@Autowired(required = true)
	public void setImportHospitalChargeDataService(
			ImportHospitalChargeDataService importHospitalChargeDataService) {
		this.importHospitalChargeDataService = importHospitalChargeDataService;
	}

}
