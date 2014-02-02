package edu.uams.clara.core.springmvc.extension.async;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;


public class TimeoutCallableProcessingInterceptor extends CallableProcessingInterceptorAdapter {
	private final static Logger logger = LoggerFactory
			.getLogger(TimeoutCallableProcessingInterceptor.class);
	@Override
	public <T> Object handleTimeout(NativeWebRequest request, Callable<T> task) throws Exception {
		logger.warn("[" + task.getClass().getName() + "] timed out");
		throw new IllegalStateException("[" + task.getClass().getName() + "] timed out");
	}

}