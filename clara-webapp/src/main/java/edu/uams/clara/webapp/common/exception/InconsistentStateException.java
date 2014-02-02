package edu.uams.clara.webapp.common.exception;

public class InconsistentStateException extends RuntimeException {

	private static final long serialVersionUID = -2120069998605470313L;
	
	public InconsistentStateException(String message){
		super(message);
	}
	
	public InconsistentStateException(String message, Throwable cause){
		super(message, cause);
	}
	
	public InconsistentStateException(Throwable cause){
		super(cause);
	}

}
