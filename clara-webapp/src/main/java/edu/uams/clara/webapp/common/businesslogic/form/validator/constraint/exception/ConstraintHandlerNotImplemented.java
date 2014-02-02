package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.exception;

public class ConstraintHandlerNotImplemented extends RuntimeException {

	private static final long serialVersionUID = 3061991758012153527L;

	public ConstraintHandlerNotImplemented(String message){
		super(message);
	}

	public ConstraintHandlerNotImplemented(String message, Throwable throwable){
		super(message, throwable);
	}
}
