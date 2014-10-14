package edu.uams.clara.webapp.report.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uams.clara.webapp.report.dao.ReportTemplateDao;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate.Status;
import edu.uams.clara.webapp.report.service.customreport.CustomReportServiceContainer;

public class ReportTaskExecutor implements Runnable {
	private final static Logger logger = LoggerFactory
			.getLogger(ReportTaskExecutor.class);

	CustomReportServiceContainer customReportServiceContainer;
	ReportTemplateDao reportTemplateDao;
	ReportTemplate reportTemplate;
	//EmailService emailService;
	//String email;
	//String parameter;
	//String applicationHost;
	//String dateString;

	public ReportTaskExecutor(ReportTemplate reportTemplate,
			CustomReportServiceContainer customReportServiceContainer,
			ReportTemplateDao reportTemplateDao) {
		this.reportTemplate = reportTemplate;
		this.customReportServiceContainer = customReportServiceContainer;
		this.reportTemplateDao = reportTemplateDao;
		//this.emailService = emailService;
		//this.parameter = parameter;
		//this.applicationHost = applicationHost;
		//this.dateString = dateString;
	}

	@Override
	public void run() {
		logger.debug("report is running");
		try {
			if (!reportTemplate.isRetired()) {
				customReportServiceContainer.getCustomReportService(
						reportTemplate.getTypeDescription())
						.uploadResultToFileServer(reportTemplate);
				reportTemplate.setStatus(Status.READY);
				reportTemplate = reportTemplateDao.saveOrUpdate(reportTemplate);
				//sendEmail();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("report has completed");

	}
	
	/*
	private void sendEmail() {
		String emailText = "<html><head><link href=\"/clara-webapp/static/styles/letters.css\" media=\"screen\" type=\"text/css\" rel=\"stylesheet\"/></head><body>";
		emailText += "<div class=\"email-template\">";
		emailText += "<br/>Following report is ready in CLARA:<br/><br/><br/><strong>Report Type:</strong>  "
				+ reportTemplate.getDescription()
				+ "<br/><br/><strong>Date:</strong>  "
				+ dateString
				+ "<br/><br/>";
		emailText += "</div></body></html>";

		List<String> mailTo = new ArrayList<String>();
		mailTo.add(email);

		String subject = "CLARA Report Completed";

		emailService.sendEmail(emailText, mailTo, null, subject, null);
	}
	*/
}
