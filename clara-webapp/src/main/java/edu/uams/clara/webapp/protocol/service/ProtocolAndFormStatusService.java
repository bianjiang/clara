package edu.uams.clara.webapp.protocol.service;

import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

public interface ProtocolAndFormStatusService {
	String getProtocolPriorityLevel(Protocol protocol);
	String getProtocolFormPriorityLevel(ProtocolForm protocolForm);
}
