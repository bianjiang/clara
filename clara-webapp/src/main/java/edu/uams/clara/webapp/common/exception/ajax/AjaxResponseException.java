package edu.uams.clara.webapp.common.exception.ajax;

public class AjaxResponseException extends RuntimeException {
	
	private static final long serialVersionUID = 3457602290082698106L;

	public AjaxResponseException(String message){
		super(message);
	}
	
	public AjaxResponseException(String message, Throwable cause){
		super(message, cause);
	}
	
	public AjaxResponseException(Throwable cause){
		super(cause);
	}

}
