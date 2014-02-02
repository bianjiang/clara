package edu.uams.clara.core.util.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class HttpServletUtils {

	private static Map<String, String> _ajaxRequestTokens = new HashMap<String, String>();
	static {
		_ajaxRequestTokens.put("X-Requested-With", "XMLHttpRequest");
	};

	
	public static boolean isAjaxRequest(HttpServletRequest request) {
		// test with our ajax request pairs
		Set<String> keys = _ajaxRequestTokens.keySet();
		for (String key : keys) {
			String value = _ajaxRequestTokens.get(key);
			if (value.equalsIgnoreCase(request.getHeader(key)))
				return true;
		}
		return false;
	}
}
