package edu.uams.clara.webapp.contract.service;

import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

public interface ContractAndFormStatusService {
	String getContractPriorityLevel(Contract contract);
	String getContractFormPriorityLevel(ContractForm contractForm);
}
