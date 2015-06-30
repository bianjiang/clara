package edu.uams.clara.scheduler.task.incoming.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.integration.incoming.user.usercontext.service.ImportandUpdateUserCOI;
import edu.uams.clara.scheduler.task.AbstractTask;

@Service
public class UserCOIUpdateTasks extends AbstractTask {

	private final static Logger logger = LoggerFactory
			.getLogger(UserCOIUpdateTasks.class);

	private ImportandUpdateUserCOI importandUpdateUserCOI;

	//@Scheduled(cron = "0 30 4 * * ?")
	// 4:30 every day
	public void updateUserCOI() {
		try {
			log(TaskEvent.STARTED, "UPDATE UserCOI Information");
			importandUpdateUserCOI.updateUserCOI();
			log(TaskEvent.FINISHED, "UPDATE UserCOI Information");
		} catch (Exception ex) {
			logger.error("failed to run UserCOI info update: ", ex);
			log(TaskEvent.FAILED,
					"UPDATE UserCOI information failed ? Check server log for exception....");
		}
	}

	public ImportandUpdateUserCOI getImportandUpdateUserCOI() {
		return importandUpdateUserCOI;
	}

	@Autowired(required = true)
	public void setImportandUpdateUserCOI(
			ImportandUpdateUserCOI importandUpdateUserCOI) {
		this.importandUpdateUserCOI = importandUpdateUserCOI;
	}

}
