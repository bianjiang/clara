package edu.uams.clara.scheduler.task.incoming.click;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.integration.incoming.cititraining.ImportCitiTrainingDataService;
import edu.uams.clara.integration.incoming.click.service.ImportCLICKGrantService;
import edu.uams.clara.integration.incoming.click.service.ImportClickSponsorAgencyService;
import edu.uams.clara.scheduler.task.AbstractTask;

@Service
public class DailyCLICKImportTasks extends AbstractTask {

	private final static Logger logger = LoggerFactory
			.getLogger(DailyCLICKImportTasks.class);

	private ImportClickSponsorAgencyService sponsorUpdateService;
	
	@Scheduled(cron = "0 0 1 * * ?")
	// 1:00 am every day
	public void updateSponsorInfo() {
		try {
			log(TaskEvent.STARTED, "UPDATE SPONSOR");
			sponsorUpdateService.updateSponsorList();
			log(TaskEvent.FINISHED, "UPDATE SPONSOR");
		} catch (Exception ex) {
			logger.error("failed to run sponsor import: ", ex);
			log(TaskEvent.FAILED,
					"UPDATE SPONSOR failed ? Check server log for exception....");
		}
	}

	public ImportClickSponsorAgencyService getSponsorUpdateService() {
		return sponsorUpdateService;
	}

	@Autowired(required=true)
	public void setSponsorUpdateService(ImportClickSponsorAgencyService sponsorUpdateService) {
		this.sponsorUpdateService = sponsorUpdateService;
	}
	
	private ImportCitiTrainingDataService importCitiTrainingDataService;
	
	@Scheduled(cron = "0 0 2 1/1 * ?")
	// 2:00 am every day
	public void importCitiTraining() {
		try {
			log(TaskEvent.STARTED, "IMPORT CITI TRAINING");
			importCitiTrainingDataService.run();
			log(TaskEvent.FINISHED, "IMPORT CITI TRAINING");
		} catch (Exception ex) {
			logger.error("failed to run citi import: ", ex);
			log(TaskEvent.FAILED,
					"IMPORT CITI TRAINING failed ? Check server log for exception....");
		}
	}
	
	public ImportCitiTrainingDataService getImportCitiTrainingDataService() {
		return importCitiTrainingDataService;
	}

	@Autowired(required=true)
	public void setImportCitiTrainingDataService(
			ImportCitiTrainingDataService importCitiTrainingDataService) {
		this.importCitiTrainingDataService = importCitiTrainingDataService;
	}
	
	private ImportCLICKGrantService importCLICKGrantService;
	
	@Scheduled(cron = "0 0 23 1/1 * ?")
	// 23:00 every day
	public void updateGrantInfo() {
		try {
			log(TaskEvent.STARTED, "UPDATE Grant Information");
			importCLICKGrantService.importProjectToClickGrant();
			log(TaskEvent.FINISHED, "UPDATE Grant Information");
		} catch (Exception ex) {
			logger.error("failed to run grant info import: ", ex);
			log(TaskEvent.FAILED,
					"UPDATE grant information failed ? Check server log for exception....");
		}
	}

	public ImportCLICKGrantService getImportCLICKGrantService() {
		return importCLICKGrantService;
	}

	@Autowired(required=true)
	public void setImportCLICKGrantService(ImportCLICKGrantService importCLICKGrantService) {
		this.importCLICKGrantService = importCLICKGrantService;
	}



	
	
}
