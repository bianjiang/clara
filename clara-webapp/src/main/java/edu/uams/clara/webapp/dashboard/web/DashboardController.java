package edu.uams.clara.webapp.dashboard.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.uams.clara.webapp.common.domain.usercontext.User;

@Controller
public class DashboardController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(DashboardController.class);

	@RequestMapping(value = "/index")
	public String getDashboard(ModelMap modelMap) {
		
		User u = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		modelMap.put("user", u);
		
		logger.debug("userID:" + u.getId());
		return "index";
	}
	

}
