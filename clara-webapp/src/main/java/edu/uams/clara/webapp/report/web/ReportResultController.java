package edu.uams.clara.webapp.report.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.report.dao.ReportResultDao;
import edu.uams.clara.webapp.report.dao.ReportTemplateDao;
import edu.uams.clara.webapp.report.domain.ReportResult;
import edu.uams.clara.webapp.report.service.customreport.impl.TimeInReviewReportServiceImpl;

@Controller
public class ReportResultController {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportResultController.class);
	
	private ReportTemplateDao reportTemplateDao;
	
	private ReportResultDao reportResultDao;
	
	private SFTPService sFTPService;
	
	private ProtocolFormDao protocolFormDao;
	
	private TimeInReviewReportServiceImpl timeInReviewReportServiceImpl;
	@Value("${application.host}")
	private String applicationHost;
	
	@Value("${fileserver.url}")
	private String fileserver;
	
	@RequestMapping(value = "/reports/results/detail/timeinreview/{protocolforms}", method = RequestMethod.GET)
	public String generateDetailReport(
			@PathVariable("protocolforms") long protocolFormId,
			ModelMap modelMap) {
		
		
		try {
			String finalXmlString = timeInReviewReportServiceImpl.generateDetailReport(protocolFormDao.findById(protocolFormId).getParent().getFormId());
			modelMap.put("result", finalXmlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "report/results/view-detail";
	}
	@RequestMapping(value = "/reports/results/{result-id}/view", method = RequestMethod.GET)
	public String generateReport(
			@PathVariable("result-id") long reportResultId,
			ModelMap modelMap) {
		ReportResult result = reportResultDao.findById(reportResultId);
		logger.debug("Viewing "+reportResultId);
		UploadedFile uploadFile = result.getUploadedFile();

		String srcFilePath = uploadFile.getPath() + uploadFile.getIdentifier()
				+ "." + uploadFile.getExtension();

		try {
			String finalXmlString = sFTPService.downloadFileFromRemoteAndConvertToXml(srcFilePath);

			modelMap.put("result", finalXmlString);
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return "report/results/view";
	}
	
	@RequestMapping(value = "/reports/results/{result-id}/download.xls", method = RequestMethod.GET)
	public String generateExcelHtmlReport(
			@PathVariable("result-id") long reportResultId,
			ModelMap modelMap) {
		ReportResult result = reportResultDao.findById(reportResultId);
		logger.debug("Viewing "+reportResultId);
		UploadedFile uploadFile = result.getUploadedFile();
		
		String srcFilePath = uploadFile.getPath() + uploadFile.getIdentifier()
				+ "." + uploadFile.getExtension();
		
		try {
			String finalXmlString = sFTPService.downloadFileFromRemoteAndConvertToXml(srcFilePath);
			finalXmlString = finalXmlString.replace("<![CDATA[<a target=\"_blank\" href=\""+applicationHost+"/clara-webapp/protocols/", "");
			finalXmlString = finalXmlString.replaceAll("/dashboard\">(.*?)</a>]]>", "");
			finalXmlString = finalXmlString.replace("&lt;br&gt;", "&lt;br style=\"mso-data-placement:same-cell;\" /&gt;");
			
			finalXmlString = finalXmlString.replaceAll("<div class=\"field-detail pull-right\"><a (.*?)Details</a></div>", "");
			modelMap.put("result", finalXmlString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "report/results/view-excelhtml";
	}
	

	public ReportTemplateDao getReportTemplateDao() {
		return reportTemplateDao;
	}
	
	@Autowired(required=true)
	public void setReportTemplateDao(ReportTemplateDao reportTemplateDao) {
		this.reportTemplateDao = reportTemplateDao;
	}

	public ReportResultDao getReportResultDao() {
		return reportResultDao;
	}
	
	@Autowired(required=true)
	public void setReportResultDao(ReportResultDao reportResultDao) {
		this.reportResultDao = reportResultDao;
	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}
	
	@Autowired(required=true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}
	
	public TimeInReviewReportServiceImpl getTimeInReviewReportServiceImpl() {
		return timeInReviewReportServiceImpl;
	}

	@Autowired(required=true)
	public void setTimeInReviewReportServiceImpl(
			TimeInReviewReportServiceImpl timeInReviewReportServiceImpl) {
		this.timeInReviewReportServiceImpl = timeInReviewReportServiceImpl;
	}
	
	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}
	public String getApplicationHost() {
		return applicationHost;
	}
	public void setApplicationHost(String applicationHost) {
		this.applicationHost = applicationHost;
	}
	public String getFileServer() {
		return fileserver;
	}
	public void setFileServer(String fileserver) {
		this.fileserver = fileserver;
	}


}
