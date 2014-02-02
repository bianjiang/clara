package edu.uams.clara.webapp.contract.businesslogic.impl;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.contract.businesslogic.ContractBusinessObjectStatusHelper;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

public class NewContractBusinessObjectStatusHelperImpl extends
		ContractBusinessObjectStatusHelper {
	
	private final static Logger logger = LoggerFactory
			.getLogger(NewContractBusinessObjectStatusHelperImpl.class);
	
	private FormService formService;

	public NewContractBusinessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		ContractForm contractForm = (ContractForm) form;
		
		String formStatus = getFormStatus(form);
		
		if (formStatus.equals("")) return "";
		
		ContractFormStatusEnum contractFormStatus = ContractFormStatusEnum.valueOf(formStatus);
		
		String condition = "";
		logger.debug("contractFormStatus: " + contractFormStatus + " committee: " + committee.getDescription() + "action: " + action);
		switch(contractFormStatus){
		case UNDER_REVISION:
			if (committee.equals(Committee.PI) && action.equals("SIGN_SUBMIT")){
				condition = formService.isCurrentUserSpecificRoleOrNot(contractForm, user, "Principal Investigator")?"IS_PI":"IS_NOT_PI";
			}
			break;
		}
		logger.debug("conditon: " + condition);
		return condition;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

}
