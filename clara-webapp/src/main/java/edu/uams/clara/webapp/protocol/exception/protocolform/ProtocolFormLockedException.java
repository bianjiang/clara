package edu.uams.clara.webapp.protocol.exception.protocolform;

import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

public class ProtocolFormLockedException extends RuntimeException {

	private static final long serialVersionUID = 7134526817150783008L;

	private ProtocolForm protocolForm;
	
	public ProtocolFormLockedException(String message, ProtocolForm protocolForm){		
		super(message);
		this.protocolForm = protocolForm;
	}	
	
	public ProtocolFormLockedException(String message, Throwable cause, ProtocolForm protocolForm){
		super(message, cause);
		this.protocolForm = protocolForm;
	}
	
	public ProtocolFormLockedException(Throwable cause, ProtocolForm protocolForm){
		super(cause);
		this.protocolForm = protocolForm;
	}

	public void setProtocolForm(ProtocolForm protocolForm) {
		this.protocolForm = protocolForm;
	}

	public ProtocolForm getProtocolForm() {
		return protocolForm;
	}
}
