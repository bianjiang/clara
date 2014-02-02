package edu.uams.clara.webapp.common.service;

import java.util.Map;

public interface ErrorMessageHandlerService {
	Map<String, Object> showErrorMessage(boolean error, String message, String redirect);
}
