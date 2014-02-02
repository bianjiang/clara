package edu.uams.clara.webapp.common.service.impl;

import java.util.HashMap;
import java.util.Map;

import edu.uams.clara.webapp.common.service.ErrorMessageHandlerService;

public class ErrorMessageHandlerServiceImpl implements
		ErrorMessageHandlerService {

	@Override
	public Map<String, Object> showErrorMessage(boolean error, String message,
			String redirect) {
		Map<String, Object> res = new HashMap<String, Object>();
		res.put("error", error);
		res.put("message", message);
		res.put("redirect", redirect);
		
		return res;
	}

}
