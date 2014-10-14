package edu.uams.clara.webapp.protocol.service.protocolform;

import java.util.List;

import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;

public interface ProtocolFormValidationService {
	List<ValidationResponse> getExtraValidationResponses(ProtocolFormXmlData protocolFormXmlData, List<ValidationResponse> validationResponses);
}
