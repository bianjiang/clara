package edu.uams.clara.webapp.protocol.web.protocolform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Controller
public class ProtocolFormDashboardController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormDashboardController.class);
	
	private ProtocolDao protocolDao;
	
	private ProtocolFormDao protocolFormDao;
	
	@RequestMapping(value = "/protocols/{protocolId}/protocol-forms/{protocolFormId}/dashboard")
	public String getFormDashboard(
			@PathVariable("protocolId") long protocolId,
			@PathVariable("protocolFormId") long protocolFormId, ModelMap modelMap) {
		
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
				
		modelMap.put("protocolForm", protocolForm);		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "protocol/protocolform/dashboard";
	}

	@Autowired(required=true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}


	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}	

}
