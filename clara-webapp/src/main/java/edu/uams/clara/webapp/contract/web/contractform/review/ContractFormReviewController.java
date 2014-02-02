package edu.uams.clara.webapp.contract.web.contractform.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.contractform.ContractFormDao;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

@Controller
public class ContractFormReviewController {

	private ContractFormDao contractFormDao;

	private ContractDao contractDao;

	private ContractFormStatusDao contractFormStatusDao;

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/review", method = RequestMethod.GET)
	public String getContractFormReviewPage(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam(value="fromQueue", required=false) String fromQueue,
			@RequestParam("committee") Committee committee, ModelMap modelMap) {

		ContractForm contractForm = contractFormDao.findById(contractFormId);

		modelMap.put("contractForm", contractForm);
		modelMap.put("formId", contractFormId);
		modelMap.put("fromQueue", fromQueue);
		modelMap.put("id", contractId);
		modelMap.put("committee", committee);
		modelMap.put("user", (User) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal());

		return "contract/contractform/review";
	}

	@RequestMapping(value = "/contracts/{contractId}/contract-forms/{contractFormId}/review/complete", method = RequestMethod.GET)
	public String getCompleteReviewPage(
			@PathVariable("contractId") long contractId,
			@PathVariable("contractFormId") long contractFormId,
			@RequestParam(value="fromQueue", required=false) String fromQueue,
			@RequestParam("committee") Committee committee, ModelMap modelMap) {

		ContractForm contractForm = contractFormDao.findById(contractFormId);

		String url = "redirect:/contracts/" + contractId + "/contract-forms/"
				+ contractFormId + "/review/";
		
		url += contractForm.getContractFormType().getUrlEncoded() + "/committee-review";

		modelMap.put("fromQueue", fromQueue);
		modelMap.put("committee", committee);
		// modelMap.put("user",
		// (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return url;

	}

	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}

	@Autowired(required = true)
	public void setContractFormDao(ContractFormDao contractFormDao) {
		this.contractFormDao = contractFormDao;
	}

	public ContractFormDao getContractFormDao() {
		return contractFormDao;
	}

	@Autowired(required = true)
	public void setContractFormStatusDao(
			ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}

}
