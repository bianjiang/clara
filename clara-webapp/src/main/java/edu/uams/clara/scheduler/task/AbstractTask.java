package edu.uams.clara.scheduler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.service.audit.AuditService;

/***
 * All scheduled tasks should extend this class (doing some basic book keeping
 * and logging) Currently this has a hard dependency on the auditService, which
 * probably isn't right.
 * 
 * 
 */

public abstract class AbstractTask {
	
	private final static Logger logger = LoggerFactory
			.getLogger(AbstractTask.class);

	private AuditService auditService;
	
	public enum TaskEvent {
		STARTED, FINISHED, FAILED;
	}
	
	protected void log(TaskEvent event, String taskName, String msg, String extraData){
		logger.error("[TASK: "
				+ taskName + "] has just " + event.toString() + "!!!!!");
		
		auditService.auditEvent("SCHEDULED_JOB_" + event.toString(), "[TASK: "
				+ taskName + "]: [" + msg + "]", extraData);
	}

	protected void log(TaskEvent event, String taskName, String msg){
		log(event, taskName, msg, null);
	}
	
	protected void log(TaskEvent event, String taskName){
		log(event, taskName, null, null);
	}
	
	protected double toSeconds(long nanoSeconds){
		return ((double) nanoSeconds) / 1000000000;  
	}

	public AuditService getAuditService() {
		return auditService;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

}
