package edu.uams.clara.webapp.common.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import edu.uams.clara.webapp.report.dao.ReportTemplateDao;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate.ScheduleType;
import edu.uams.clara.webapp.report.service.customreport.CustomReportServiceContainer;
import edu.uams.clara.webapp.report.task.ReportTaskExecutor;

@Service
public class ScheduledReportService {
	private final static Logger logger = LoggerFactory
			.getLogger(ScheduledReportService.class);
	
	private ReportTemplateDao reportTemplateDao;
	
	private ThreadPoolTaskExecutor  taskExecutor;
	
	private CustomReportServiceContainer customReportServiceContainer;
	
	private void run(ScheduleType scheduleType) {
		List<ReportTemplate> reportTemplates = reportTemplateDao.listReportTemplatesByScheduleType(scheduleType);
		
		if (reportTemplates.size() > 0) {
			for (ReportTemplate reportTemplate : reportTemplates) {
				taskExecutor.execute(new ReportTaskExecutor(reportTemplate, customReportServiceContainer, reportTemplateDao));
			}
		}
	}
	
	public void runDailyReport() {
		run(ScheduleType.DAILY);
	}
	
	public void runWeeklyReport() {
		run(ScheduleType.WEEKLY);
	}
	
	public void runMonthlyReport() {
		run(ScheduleType.MONTHLY);
	}
	
	public ReportTemplateDao getReportTemplateDao() {
		return reportTemplateDao;
	}
	
	@Autowired(required = true)
	public void setReportTemplateDao(ReportTemplateDao reportTemplateDao) {
		this.reportTemplateDao = reportTemplateDao;
	}

	public ThreadPoolTaskExecutor getTaskExecutor() {
		return taskExecutor;
	}
	
	@Autowired(required = true)
	public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
	
	public CustomReportServiceContainer getCustomReportServiceContainer() {
		return customReportServiceContainer;
	}
	
	@Autowired(required=true)
	public void setCustomReportServiceContainer(
			CustomReportServiceContainer customReportServiceContainer) {
		this.customReportServiceContainer = customReportServiceContainer;
	}
}
