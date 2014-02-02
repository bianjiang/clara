package edu.uams.clara.webapp.contract.web.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.uams.clara.webapp.common.domain.usercontext.User;

@Controller
public class ContractQueueController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContractQueueController.class);

	
	@RequestMapping(value="/contracts/queues")
	public String getAdminDashboard(ModelMap modelMap){
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return "contract/queues/index";
	}
}
