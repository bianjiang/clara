package edu.uams.clara.webapp.report.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/report/web/ReportControllerTest-context.xml"})
public class ReportControllerTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportControllerTest.class);

	private ReportController reportController;

	public ReportController getReportController() {
		return reportController;
	}

	@Test
	public void testGetPIReport(){
		String xml = "<report name=\"PI\" template=\"report-pi\" detailed=\"false\"><pi id=\"68\" /><study-type name=\"investigator-initiated\" /><startdate>05/01/2012</startdate><enddate>10/20/2012</enddate></report>";

		//String resultXml = reportController.getPiReport(xml);

		//logger.debug("resultXml: " + resultXml);
	}

	@Autowired(required=true)
	public void setReportController(ReportController reportController) {
		this.reportController = reportController;
	}
}
