package edu.uams.clara.webapp.contract.web.contractform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

@Controller
public class ContractFormDashboardController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContractFormDashboardController.class);
	
	private ContractDao contractDao;
	
	private ContractFormDao contractFormDao;
	
	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/dashboard")
	public String getFormDashboard(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId, ModelMap modelMap) {
		
		ContractForm contractForm = contractFormDao.findById(contractFormId);
				
		modelMap.put("contractForm", contractForm);		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "contract/contractform/dashboard";
	}

	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}


	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}	

}
