package edu.uams.clara.webapp.contract.exception.contractform;

import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

public class ContractFormLockedException extends RuntimeException {

	private static final long serialVersionUID = 7134526817150783634L;

	private ContractForm contractForm;
	
	public ContractFormLockedException(String message, ContractForm contractForm){		
		super(message);
		this.contractForm = contractForm;
	}	
	
	public ContractFormLockedException(String message, Throwable cause, ContractForm contractForm){
		super(message, cause);
		this.contractForm = contractForm;
	}
	
	public ContractFormLockedException(Throwable cause, ContractForm contractForm){
		super(cause);
		this.contractForm = contractForm;
	}

	public void setContractForm(ContractForm contractForm) {
		this.contractForm = contractForm;
	}

	public ContractForm getContractForm() {
		return contractForm;
	}
}
