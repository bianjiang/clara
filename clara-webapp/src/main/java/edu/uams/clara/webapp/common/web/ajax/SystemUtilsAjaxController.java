package edu.uams.clara.webapp.common.web.ajax;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SystemUtilsAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(SystemUtilsAjaxController.class);
	
	@RequestMapping(value = "/ajax/system/get-current-time", method = RequestMethod.GET)
	public @ResponseBody
	Date getCurrentTime() {		

		return new Date();
	}
}
