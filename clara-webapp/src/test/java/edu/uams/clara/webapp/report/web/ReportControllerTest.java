package edu.uams.clara.webapp.report.web;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Maps;

import edu.uams.clara.webapp.report.dao.ReportTemplateDao;
import edu.uams.clara.webapp.report.domain.ReportTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/report/web/ReportControllerTest-context.xml"})
public class ReportControllerTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportControllerTest.class);
	
	private ReportController reportController;
	private ReportTemplateDao reportTemplateDao;

	public ReportController getReportController() {
		return reportController;
	}
	
	private Map<String,Integer> reportTypeMap = Maps.newHashMap();{
		reportTypeMap.put("Audit", 1);
		reportTypeMap.put("Time in Review On Action", 2);
		reportTypeMap.put("Summary Information", 3);
		reportTypeMap.put("Cancer Related COM Study", 4);
		reportTypeMap.put("Overdue Notice", 5);
		reportTypeMap.put("Human Subject Research Dashboard-Summary of protocols by type of review and initiator", 6);
		reportTypeMap.put("Human Subject Research Dashboard-Summary of protocols by type of review and sources of funding", 7);
		reportTypeMap.put("Human Subject Research Dashboard-Summary of Principal Investigators", 8);
		reportTypeMap.put("Human Subject Research Dashboard-Summary of Protocols Open", 9);
		reportTypeMap.put("Human Subject Research Dashboard-Studies Submitted for Continuing Review", 10);
		reportTypeMap.put("Human Subject Research Dashboard-Patient Enrollment on Studies Submitted for Continuing Review", 11);
		reportTypeMap.put("Human Subject Research Dashboard-Protocols closed to enrollment and follow-up", 12);
		reportTypeMap.put("Human Subject Research Dashboard-Full Board Review Studies Closed-Reasons for Closure", 13);
		reportTypeMap.put("Human Subject Research Dashboard-Full Board Review Studies Closed-Cumulative Patients Enrolled", 14);
		reportTypeMap.put("Human Subject Research Dashboard-Process Measures", 15);
		reportTypeMap.put("Agenda Report", 16);
		reportTypeMap.put("IRB Billing Report", 17);
		
	}
	
	//@Test
	public void testGetPIReport(){
		String xml = "<report name=\"PI\" template=\"report-pi\" detailed=\"false\"><pi id=\"68\" /><study-type name=\"investigator-initiated\" /><startdate>05/01/2012</startdate><enddate>10/20/2012</enddate></report>";
		
		//String resultXml = reportController.getPiReport(xml);
		
		//logger.debug("resultXml: " + resultXml);
	}
	
	@Test
	public void updateReportType(){
		List<ReportTemplate> reports = reportTemplateDao.findAll();
		for(ReportTemplate  report:reports){
			
			try{
			report.setTypeId(reportTypeMap.get(report.getTypeDescription()));
			reportTemplateDao.saveOrUpdate(report);
			}catch(Exception e){
				logger.debug(report.getTypeDescription()+"  @@@   "+report.getId());
				e.printStackTrace();
			}
		}
	}
	
	
	@Autowired(required=true)
	public void setReportController(ReportController reportController) {
		this.reportController = reportController;
	}

	public ReportTemplateDao getReportTemplateDao() {
		return reportTemplateDao;
	}

	@Autowired(required=true)
	public void setReportTemplateDao(ReportTemplateDao reportTemplateDao) {
		this.reportTemplateDao = reportTemplateDao;
	}
}
