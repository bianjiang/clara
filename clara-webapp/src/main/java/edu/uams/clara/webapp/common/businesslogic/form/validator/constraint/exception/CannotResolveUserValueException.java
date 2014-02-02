package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.exception;

public class CannotResolveUserValueException extends RuntimeException {

	private static final long serialVersionUID = 3061991758012153527L;

	public CannotResolveUserValueException(String message){
		super(message);
	}

	public CannotResolveUserValueException(String message, Throwable throwable){
		super(message, throwable);
	}
}
