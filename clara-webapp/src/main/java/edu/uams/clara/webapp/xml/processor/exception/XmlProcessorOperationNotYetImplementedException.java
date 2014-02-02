package edu.uams.clara.webapp.xml.processor.exception;

public class XmlProcessorOperationNotYetImplementedException extends RuntimeException {

	private static final long serialVersionUID = -7286552689913516037L;

	public XmlProcessorOperationNotYetImplementedException(String message){
		super(message);
	}
	
	public XmlProcessorOperationNotYetImplementedException(String message, Throwable throwable){
		super(message, throwable);
	}
}
