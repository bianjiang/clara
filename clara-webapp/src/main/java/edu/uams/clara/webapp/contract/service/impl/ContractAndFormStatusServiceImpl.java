package edu.uams.clara.webapp.contract.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.contract.dao.ContractDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormStatusDao;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractStatusDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;
import edu.uams.clara.webapp.contract.service.ContractAndFormStatusService;

public class ContractAndFormStatusServiceImpl implements
		ContractAndFormStatusService {
	private final static Logger logger = LoggerFactory
			.getLogger(ContractAndFormStatusServiceImpl.class);
	
	private ContractStatusDao contractStatusDao;
	private ContractFormStatusDao contractFormStatusDao;
	private ContractDao contractDao;
	
	private List<String> normalList = new ArrayList<String>();{		
		normalList.add(ContractStatusEnum.DRAFT.getDescription());
	}
	
	private List<String> infoList = new ArrayList<String>();{	
		for(ContractStatusEnum pse : ContractStatusEnum.values()){
			if (pse.toString().startsWith("UNDER")){
				infoList.add(pse.getDescription());
			}
		}
	}
	
	private List<String> warnList = new ArrayList<String>();{	
		for(ContractStatusEnum pse : ContractStatusEnum.values()){
			if (pse.toString().contains("PENDING") || pse.toString().contains("PENDING_PI_ENDORSEMENT")){
				warnList.add(pse.getDescription());
			}
		}
	}
	
	private List<String> errorList = new ArrayList<String>();{	
		for(ContractStatusEnum pse : ContractStatusEnum.values()){
			if (pse.toString().contains("DECLINE")){
				errorList.add(pse.getDescription());
			}
		}
	}

	@Override
	public String getContractPriorityLevel(Contract contract) {
		String priorityLevel = "";
		
		ContractStatus contractStatus = contractDao.getLatestContractStatusByContractId(contract.getId());
		
		ContractStatusEnum pse = contractStatus.getContractStatus();
		
		if (normalList.contains(pse.getDescription())) priorityLevel = "NORMAL";
		if (infoList.contains(pse.getDescription())) priorityLevel = "INFO";
		if (warnList.contains(pse.getDescription())) priorityLevel = "WARN";
		if (errorList.contains(pse.getDescription())) priorityLevel = "ERROR";
		
		return priorityLevel;
	}
	
	//need to re-do this one later, cause it's using contract status
	@Override
	public String getContractFormPriorityLevel(ContractForm contractForm) {
		String priorityLevel = "";
		
		ContractFormStatus contractFormStatus = contractFormStatusDao.getLatestContractFormStatusByFormId(contractForm.getId());
		
		ContractFormStatusEnum pfse = contractFormStatus.getContractFormStatus();
		
		if (normalList.contains(pfse.getDescription())) priorityLevel = "NORMAL";
		if (infoList.contains(pfse.getDescription())) priorityLevel = "INFO";
		if (warnList.contains(pfse.getDescription())) priorityLevel = "WARN";
		if (errorList.contains(pfse.getDescription())) priorityLevel = "ERROR";
		
		return priorityLevel;
	}

	public ContractStatusDao getContractStatusDao() {
		return contractStatusDao;
	}
	
	@Autowired(required = true)
	public void setContractStatusDao(ContractStatusDao contractStatusDao) {
		this.contractStatusDao = contractStatusDao;
	}

	public ContractFormStatusDao getContractFormStatusDao() {
		return contractFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setContractFormStatusDao(ContractFormStatusDao contractFormStatusDao) {
		this.contractFormStatusDao = contractFormStatusDao;
	}

	public ContractDao getContractDao() {
		return contractDao;
	}
	
	@Autowired(required = true)
	public void setContractDao(ContractDao contractDao) {
		this.contractDao = contractDao;
	}

}
