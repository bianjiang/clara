package edu.uams.clara.webapp.protocol.service;

import java.util.List;

import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

public interface ProtocolFormDetailContentService {
	String getDetailContent(ProtocolForm protocolForm);
	String getFormDetailInfo(List<ProtocolForm> protocolFormLst);
}
