package edu.uams.clara.webapp.queue.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uams.clara.webapp.common.domain.usercontext.User;

@Controller
public class QueuesDashboardController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(QueuesDashboardController.class);

	
	@RequestMapping(value="/queues")
	public String getQueuesDashboard(
			@RequestParam(value = "fromQueue", required = false) String fromQueue,
			ModelMap modelMap){
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		modelMap.put("fromQueue",fromQueue);
		return "queues/index";
	}

}
