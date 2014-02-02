package edu.uams.clara.webapp.protocol.exception.protocolform;

public class ProtocolFormNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -2120069998605470313L;
	
	public ProtocolFormNotFoundException(String message){
		super(message);
	}
	
	public ProtocolFormNotFoundException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ProtocolFormNotFoundException(Throwable cause){
		super(cause);
	}

}
