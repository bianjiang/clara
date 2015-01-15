package edu.uams.clara.scheduler.task.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.uams.clara.scheduler.task.AbstractTask;
import edu.uams.clara.webapp.common.security.impl.MutexLockServiceImpl;

@Service
public class ScheduledMutexLockUnlock extends AbstractTask{
	private final static Logger logger = LoggerFactory
			.getLogger(ScheduledMutexLockUnlock.class);
	
	private MutexLockServiceImpl mutexlockService;

	@Scheduled(cron = "0 10 5  * * ?")
	// run at 05:10 every day
	public void runDailyReport() {
		try {
			log(TaskEvent.STARTED, "RUN MUTEXLOCK UNLOCK");
			mutexlockService.unlockExpiredMutexLock();
			log(TaskEvent.FINISHED, "RUN MUTEXLOCK UNLOCK");
		} catch (Exception ex) {
			logger.error("failed to run mutexlock unlock: ", ex);
			log(TaskEvent.FAILED,
					"RUN MUTEXLOCK UNLOCK Failed ? Check server log for exception....");
		}
	}
	
	public MutexLockServiceImpl getMutexlockService() {
		return mutexlockService;
	}

	@Autowired(required=true)
	public void setMutexlockService(MutexLockServiceImpl mutexlockService) {
		this.mutexlockService = mutexlockService;
	}
}
