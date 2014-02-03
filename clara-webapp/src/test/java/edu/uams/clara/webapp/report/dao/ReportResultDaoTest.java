package edu.uams.clara.webapp.report.dao;

import java.util.List;

import javax.xml.transform.Source;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.domain.result.AuditReport;
import edu.uams.clara.webapp.report.dao.ReportResultDao;
import edu.uams.clara.webapp.report.service.customreport.CustomReportServiceContainer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/report/dao/ReportResultDaoTest-context.xml"})
public class ReportResultDaoTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportResultDaoTest.class);

	private ReportResultDao reportResultDao;

	private ReportTemplateDao reportTemplateDao;

	private CustomReportServiceContainer customReportServiceContainer;

	//@Test
	public void testGetAuditReport(){
		String lastApprovalDateSt =  " meta_data_xml.exist('/protocol/most-recent-study/approval-date[. > \"02/18/2013\"]')=1";
		String nextApprovalDateSt =  " meta_data_xml.exist('/protocol/most-recent-study/approval-end-date[. > \"2013-12-01\"]')=1";
		//String nextApprovalDateSt =  " meta_data_xml.exist('/protocol/most-recent-study/approval-end-date[. "+ filterMap.get(nextApprovalDateFilter) +" \""+ nextApprovalDate +"\"]')=1";

		List<AuditReport> audiReportLst = reportResultDao.getAuditReport(0, 0, "", "", "Full Board");
		String reportResult = "";
		for (AuditReport ar : audiReportLst){
			reportResult += "<tr><td class=\"identifier\">"+ ar.getProtocolId() +"</td>";
			reportResult += "<td class=\"status\">"+ ar.getCurrentStatus() +"</td>";
			reportResult += "<td class=\"pi\">"+ ar.getPiName() +"</td>";
			reportResult += "<td class=\"title\">"+ StringEscapeUtils.escapeXml(ar.getProtocolTitle()) +"</td></tr>";
		}

		logger.debug("final: " + reportResult);
	}

	@Test
	public void testGenerateReport() {
		ReportTemplate report = reportTemplateDao.findById(85l);

		String source = customReportServiceContainer.getCustomReportService(report.getTypeDescription()).generateReportResult(report);
	}

	public ReportResultDao getReportResultDao() {
		return reportResultDao;
	}

	@Autowired(required = true)
	public void setReportResultDao(ReportResultDao reportResultDao) {
		this.reportResultDao = reportResultDao;
	}

	public ReportTemplateDao getReportTemplateDao() {
		return reportTemplateDao;
	}

	@Autowired(required = true)
	public void setReportTemplateDao(ReportTemplateDao reportTemplateDao) {
		this.reportTemplateDao = reportTemplateDao;
	}

	public CustomReportServiceContainer getCustomReportServiceContainer() {
		return customReportServiceContainer;
	}

	@Autowired(required = true)
	public void setCustomReportServiceContainer(
			CustomReportServiceContainer customReportServiceContainer) {
		this.customReportServiceContainer = customReportServiceContainer;
	}

}
