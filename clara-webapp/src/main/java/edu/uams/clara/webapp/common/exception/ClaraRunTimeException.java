package edu.uams.clara.webapp.common.exception;

public class ClaraRunTimeException extends RuntimeException{

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 4908988839002620501L;
	
	public enum ErrorType{
		NO_AGENDA_ASSIGNED("No agenda is available at this time!  New Agenda needs to be created!");
		
		private String message;
		
		private ErrorType(String message){
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	
	private ErrorType errorType;
	
	public ClaraRunTimeException(String message, ErrorType errorType){
		super(message);
		this.errorType = errorType;
	}
	
	public ClaraRunTimeException(String message, Throwable cause, ErrorType errorType){
		super(message, cause);
		this.errorType = errorType;
	}
	
	public ClaraRunTimeException(Throwable cause, ErrorType errorType){
		super(cause);
		this.errorType = errorType;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

}
