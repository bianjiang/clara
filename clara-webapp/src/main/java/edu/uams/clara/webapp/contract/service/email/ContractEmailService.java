package edu.uams.clara.webapp.contract.service.email;

import java.io.IOException;
import java.util.Map;

import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

public interface ContractEmailService {


	EmailTemplate sendNotification(ContractForm contractForm, Committee committee, Map<String, Object> attributeRawValues, 
			User user, String emailTemplateIdentifier, String emailComment, String mailTo, String cc);

	EmailTemplate sendLetter(ContractForm contractForm, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailTemplateIdentifier, String emailComment,
			String letterName, String docType, String mailTo, String cc) throws IOException;

}
