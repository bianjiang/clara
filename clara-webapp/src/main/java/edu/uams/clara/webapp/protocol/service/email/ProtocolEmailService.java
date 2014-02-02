package edu.uams.clara.webapp.protocol.service.email;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

public interface ProtocolEmailService {


	EmailTemplate sendNotification(ProtocolForm protocolForm, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment, String mailTo, String cc);

	EmailTemplate sendLetter(ProtocolForm protocolForm, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment, String letterName, String docType, String mailTo, String cc) throws IOException;
	
	EmailTemplate sendProtocolLetter(Protocol protocol, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment, String letterName, String docType, String mailTo, String cc, String subject) throws IOException;
	
	EmailTemplate sendProtocolNotification(Protocol protocol, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment, String letterName, String docType, String mailTo, String cc, String subject) throws IOException;

	
	EmailTemplate sendAgendaLetter(Agenda agenda, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment, String letterName, String docType, String mailTo, String cc, String subject) throws IOException;

}
