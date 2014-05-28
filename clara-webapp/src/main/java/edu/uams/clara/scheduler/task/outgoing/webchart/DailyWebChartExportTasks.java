package edu.uams.clara.scheduler.task.outgoing.webchart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.integration.outgoing.webchart.ExportProtocolsToWebChartService;
import edu.uams.clara.scheduler.task.AbstractTask;

@Service
public class DailyWebChartExportTasks extends AbstractTask {

	private final static Logger logger = LoggerFactory
			.getLogger(DailyWebChartExportTasks.class);

	private ExportProtocolsToWebChartService exportProtocolsToWebChartService;
	
	private boolean shouldRun = false;
	
	/*@Scheduled(cron = "0 0 2 * * ?")
	public void exportToWebChart() {
		try {
			logger.debug("about to run export to Webchart; but shouldRun? : " + this.isShouldRun());
			if(!this.isShouldRun()) return;

			log(TaskEvent.STARTED, "Export to Webchart started...");
			long startTime = System.nanoTime();
			exportProtocolsToWebChartService.updateWebChartIntegration();
			long endTime = System.nanoTime();
			log(TaskEvent.FINISHED, "Export to Webchart finished and it took: " + toSeconds(endTime - startTime) + " seconds");
		} catch (Exception ex) {
			logger.error("failed to run webchart export: ", ex);
			log(TaskEvent.FAILED,
					"failed to run webchart export: Check server log for exception....");
		}
	}*/


	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
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
