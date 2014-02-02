package edu.uams.clara.webapp.protocol.service;

import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormUserElementTemplate.TemplateType;

public interface ProtocolFormUserElementTemplateService {

	String updateTemplateXMLData(String xmlData, TemplateType templateType);
}
