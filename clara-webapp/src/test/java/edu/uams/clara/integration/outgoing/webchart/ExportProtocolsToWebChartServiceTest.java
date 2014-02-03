package edu.uams.clara.integration.outgoing.webchart;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.outgoing.webchart.dao.WebChartARIAUserDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/webchart/ExportProtocolsToWebChartServiceTest-context.xml" })
public class ExportProtocolsToWebChartServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ExportProtocolsToWebChartServiceTest.class);

	private WebChartARIAUserDao webChartARIAUserDao;
	private ExportProtocolsToWebChartService exportProtocolsToWebChartService;

	@Test
	public void test(){
		exportProtocolsToWebChartService.updateWebChartIntegration();
	}

	public WebChartARIAUserDao getWebChartARIAUserDao() {
		return webChartARIAUserDao;
	}

	@Autowired(required=true)
	public void setWebChartARIAUserDao(WebChartARIAUserDao webChartARIAUserDao) {
		this.webChartARIAUserDao = webChartARIAUserDao;
	}

	public ExportProtocolsToWebChartService getExportProtocolsToWebChartService() {
		return exportProtocolsToWebChartService;
	}

	@Autowired(required=true)
	public void setExportProtocolsToWebChartService(
			ExportProtocolsToWebChartService exportProtocolsToWebChartService) {
		this.exportProtocolsToWebChartService = exportProtocolsToWebChartService;
	}
}
