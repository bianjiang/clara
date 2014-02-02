package edu.uams.clara.webapp.contract.web;

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
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.protocol.domain.Protocol;

@Controller
public class ContractDashboardController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ContractDashboardController.class);
	
	private ContractDao contractDao;	

	@RequestMapping(value = "/contracts/{contractId}/dashboard")
	public String getContractDashboard(
			@PathVariable("contractId") long contractId, ModelMap modelMap) {
	
		Contract contract = contractDao.findById(contractId);
		
		//ContractFormXmlData contractXmlData = contractDao.getLastestContractXmlDataByContractId(contractId);
		
		//@ToDo this might not be needed, since the status is in the contract.metaDataXml
		ContractStatus contractStatus = contractDao.getLatestContractStatusByContractId(contractId);

		modelMap.put("contractStatus", contractStatus);
		modelMap.put("contract", contract);
		
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return "contract/dashboard";
	}
	
	@RequestMapping(value = "/contracts/{contractId}/summary")
	public String getSummaryPage(@PathVariable("contractId") long contractId, ModelMap modelMap){
		Contract contract = contractDao.findById(contractId);
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		modelMap.put("contract", contract);
		modelMap.put("user", u);
		
		return "contract/summary";
		
	}
	
	
	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}


	public ContractDao getContractDao() {
		return contractDao;
	}	

}
