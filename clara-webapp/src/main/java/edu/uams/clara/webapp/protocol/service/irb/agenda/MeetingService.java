package edu.uams.clara.webapp.protocol.service.irb.agenda;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

public interface MeetingService {
	String generateActionByMotion(ProtocolFormType protocolFormType, String xmlData, long agendaItemId);
	String getEmailTemplateIdentifier(ProtocolForm protocolForm, Committee committee, String action);
	ProtocolForm addLatestMotionToProtocolForm(ProtocolForm protocolForm, String xmlData, long agendaItemId);
}
