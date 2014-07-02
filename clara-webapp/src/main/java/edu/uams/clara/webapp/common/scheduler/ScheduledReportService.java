package edu.uams.clara.webapp.common.scheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.queue.service.QueueService;
import edu.uams.clara.webapp.queue.service.QueueServiceContainer;
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
	
	private QueueServiceContainer queueServiceContainer;
	
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	private UserDao userDao;
	
	private boolean shouldRun = false;
	
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
	
	public void runQueueReport() {	
		if(!this.isShouldRun()) return;
		
		try {
			Date now = new Date();
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			
			QueueService queueService = getQueueServiceContainer().getQueueService("Contract");
			
			User user = userDao.findById(80);
			
			String legalQueueItemXml = queueService.getFormsInQueueByUser(
					"QUEUE_CONTRACT_LEGAL_REVIEWER", user, false);
			
			fileGenerateAndSaveService
			.processFileGenerateAndSave("/queue/contract-legal-" + df.format(now) + ".xml",
					IOUtils.toInputStream(legalQueueItemXml));
			
			String contractAdminQueueItemXml = queueService.getFormsInQueueByUser(
					"QUEUE_CONTRACT_ADMIN", user, false);
			
			fileGenerateAndSaveService
			.processFileGenerateAndSave("/queue/contract-admin-" + df.format(now) + ".xml",
					IOUtils.toInputStream(contractAdminQueueItemXml));
		} catch (Exception e) {
			
		}
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

	public QueueServiceContainer getQueueServiceContainer() {
		return queueServiceContainer;
	}
	
	@Autowired(required=true)
	public void setQueueServiceContainer(QueueServiceContainer queueServiceContainer) {
		this.queueServiceContainer = queueServiceContainer;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}
	
	@Autowired(required=true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
