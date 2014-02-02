package edu.uams.clara.webapp.queue.service;

public interface QueueServiceContainer {

	QueueService getQueueService(String objectType);

}
