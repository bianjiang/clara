package edu.uams.clara.webapp.report.web.ajax;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.util.response.JsonResponse;
import edu.uams.clara.webapp.report.dao.ReportResultDao;
import edu.uams.clara.webapp.report.dao.ReportTemplateDao;
import edu.uams.clara.webapp.report.domain.ReportResult;

@Controller
public class ReportResultAjaxController {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportResultAjaxController.class);
	
	private ReportTemplateDao reportTemplateDao;
	
	private ReportResultDao reportResultDao;
	
	@RequestMapping(value = "/ajax/reports/{report-template-id}/list-results", method = RequestMethod.GET)
	public @ResponseBody JsonResponse listReportResults(@PathVariable("report-template-id") long reportTemplateId) {

		try {
			List<ReportResult> resultList = reportResultDao.listReportResultsByReportId(reportTemplateId);
			
			return new JsonResponse(false, resultList);
		} catch (Exception e) {
			return new JsonResponse(true, "Failed to load results!", "", false);
		}
		
	}

	public ReportTemplateDao getReportTemplateDao() {
		return reportTemplateDao;
	}
	
	@Autowired(required = true)
	public void setReportTemplateDao(ReportTemplateDao reportTemplateDao) {
		this.reportTemplateDao = reportTemplateDao;
	}

	public ReportResultDao getReportResultDao() {
		return reportResultDao;
	}
	
	@Autowired(required = true)
	public void setReportResultDao(ReportResultDao reportResultDao) {
		this.reportResultDao = reportResultDao;
	}
}
