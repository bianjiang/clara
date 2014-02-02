package edu.uams.clara.webapp.queue.service.impl;

import java.util.HashMap;
import java.util.Map;

import edu.uams.clara.webapp.queue.service.QueueService;
import edu.uams.clara.webapp.queue.service.QueueServiceContainer;


public class QueueServiceContainerImpl implements QueueServiceContainer {

	private Map<String, QueueService> queueServices = new HashMap<String, QueueService>(0);
	
	@Override
	public QueueService getQueueService(String objectType){
		return queueServices.get(objectType);
	}

	public Map<String, QueueService> getQueueServices() {
		return queueServices;
	}

	public void setQueueServices(Map<String, QueueService> queueServices) {
		this.queueServices = queueServices;
	}

}