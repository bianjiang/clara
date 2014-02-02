package edu.uams.clara.webapp.contract.exception.contractform;

public class ContractFormNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2120069998605470914L;
	
	public ContractFormNotFoundException(String message){
		super(message);
	}
	
	public ContractFormNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ContractFormNotFoundException(Throwable cause){
		super(cause);
	}

}
