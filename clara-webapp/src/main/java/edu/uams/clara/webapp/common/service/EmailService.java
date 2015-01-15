package edu.uams.clara.webapp.common.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;

import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;

public interface EmailService{

	void sendEmail(String templateName,
			List<String> mailTo, List<String> cc, String subject, List<String> files);

	List<EmailRecipient> getEmailRecipients(String jsonEncodedTo) throws JsonParseException, IOException;
	List<String> getRecipientsAddress(List<String> recipientLst);
	List<String> getTemplateRecipientsAddress(List<EmailRecipient> EmailRecipientLst);
	List<String> setRealReceiptByEmailAddress(List<String> mailToList);
	Map<String, Object> addInputRecipentsToRawAttributes(Map<String, Object> attributeRawValues, String mailTo, String cc);
}