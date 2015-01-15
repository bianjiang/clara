package edu.uams.clara.scheduler.task.incoming.sap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.integration.incoming.sap.service.ImportSalaryService;
import edu.uams.clara.scheduler.task.AbstractTask;
import edu.uams.clara.scheduler.task.incoming.user.UserCOIUpdateTasks;

@Service
public class DailySalaryUpdateTasks extends AbstractTask {

	private final static Logger logger = LoggerFactory
			.getLogger(UserCOIUpdateTasks.class);
	
	private ImportSalaryService importSalaryService;
	
	@Scheduled(cron = "0 30 5 * * ?")
	// 5:30 every day
	public void updateUserSalary() {
		try {
			log(TaskEvent.STARTED, "UPDATE Salary Information");
			importSalaryService.addandUpateSalaryForPerson();
			log(TaskEvent.FINISHED, "UPDATE Salary Information");
		} catch (Exception ex) {
			logger.error("failed to run Salary info update: ", ex);
			log(TaskEvent.FAILED,
					"UPDATE Salary information failed ? Check server log for exception....");
		}
	}

	public ImportSalaryService getImportSalaryService() {
		return importSalaryService;
	}

	@Autowired(required = true)
	public void setImportSalaryService(ImportSalaryService importSalaryService) {
		this.importSalaryService = importSalaryService;
	}
}
