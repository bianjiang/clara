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
import edu.uams.clara.webapp.contract.domain.contractform.ContractFormXmlData;

@Controller
public class ContractIndexController {

	private final static Logger logger = LoggerFactory
			.getLogger(ContractIndexController.class);

	private ContractDao contractDao;
	
	@RequestMapping(value = "/contracts/index")
	public String getContractDashboard(ModelMap modelMap) {

		User u = (User) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();

		modelMap.put("user", u);

		logger.debug("userID:" + u.getId());
		return "contract/index";
	}
	
	
	@RequestMapping(value = "/contracts/edit-new-contract-form-by-protocol-id/{protocolId}")
	public String editNewContractFormByProtocolId(@PathVariable("protocolId") long protocolId, ModelMap modelMap) {
		
		Contract contract = contractDao.getContractByProtocolId(protocolId);
		ContractFormXmlData contractXmlData = contractDao.getLastestContractXmlDataByContractId(contract.getId());
		
		String baseUrl = "/contracts/" + contract.getId() + "/contract-forms/" + contractXmlData.getContractForm().getId() + "/new-contract/contract-form-xml-datas/" + contractXmlData.getId();
		
		return "redirect:" + baseUrl + "/first-page";
	}


	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required=true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}
}
