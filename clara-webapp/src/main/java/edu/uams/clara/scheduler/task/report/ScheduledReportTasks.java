package edu.uams.clara.scheduler.task.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.scheduler.task.AbstractTask;
import edu.uams.clara.webapp.common.scheduler.ScheduledReportService;

@Service
public class ScheduledReportTasks extends AbstractTask {
	private final static Logger logger = LoggerFactory
			.getLogger(ScheduledReportTasks.class);
	
	private ScheduledReportService scheduledReportService;
	
	@Scheduled(cron = "0 30 22 1/1 * ?")
	// run at 22:30 every day
	public void runDailyReport() {
		try {
			log(TaskEvent.STARTED, "RUN DAILY REPORT");
			scheduledReportService.runDailyReport();
			log(TaskEvent.FINISHED, "RUN DAILY REPORT");
		} catch (Exception ex) {
			logger.error("failed to run daily report: ", ex);
			log(TaskEvent.FAILED,
					"RUN DAILY REPORT failed ? Check server log for exception....");
		}
	}
	
	@Scheduled(cron = "0 40 22 ? * SUN")
	// run at 22:40 every Sunday
	public void runWeeklyReport() {
		try {
			log(TaskEvent.STARTED, "RUN WEEKLY REPORT");
			scheduledReportService.runWeeklyReport();
			log(TaskEvent.FINISHED, "RUN WEEKLY REPORT");
		} catch (Exception ex) {
			logger.error("failed to run weekly report: ", ex);
			log(TaskEvent.FAILED,
					"RUN WEEKLY REPORT failed ? Check server log for exception....");
		}
	}
	
	@Scheduled(cron = "0 0 5 1 1/1 ?")
	// run at 5:00 first day of every month
	public void runMonthlyReport() {
		try {
			log(TaskEvent.STARTED, "RUN MONTHLY REPORT");
			scheduledReportService.runMonthlyReport();
			log(TaskEvent.FINISHED, "RUN MONTHLY REPORT");
		} catch (Exception ex) {
			logger.error("failed to run monthly report: ", ex);
			log(TaskEvent.FAILED,
					"RUN MONTHLY REPORT failed ? Check server log for exception....");
		}
	}
	
	@Scheduled(cron = "0 45 7 1/1 * ?")
	// run at 7:45 every day
	public void runDailyQueueReport() {
		try {
			log(TaskEvent.STARTED, "RUN DAILY QUEUE REPORT");
			scheduledReportService.runQueueReport();
			log(TaskEvent.FINISHED, "RUN DAILY QUEUE REPORT");
		} catch (Exception ex) {
			logger.error("failed to run daily queue report: ", ex);
			log(TaskEvent.FAILED,
					"RUN DAILY QUEUE REPORT failed ? Check server log for exception....");
		}
	}

	public ScheduledReportService getScheduledReportService() {
		return scheduledReportService;
	}
	
	@Autowired(required = true)
	public void setScheduledReportService(ScheduledReportService scheduledReportService) {
		this.scheduledReportService = scheduledReportService;
	}
}
