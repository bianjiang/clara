package edu.uams.clara.webapp.common.web.ajax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.RoleDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.Role;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;

@Controller
public class EmailTemplateAjaxController {
	
	private final static Logger logger = LoggerFactory
			.getLogger(EmailTemplateAjaxController.class);
	
	private PersonDao personDao;
	private RoleDao roleDao;
	
	@RequestMapping(value = "/ajax/email-templates/recipients/search")
	public @ResponseBody Map<String, List<EmailRecipient>> searchForEmailReceipt(@RequestParam("query") String query){
		
		Map<String, List<EmailRecipient>> results = new HashMap<String, List<EmailRecipient>>();
		
		List<EmailRecipient> recipients = new ArrayList<EmailRecipient>();
		
		String desc = "";
		String emailAddress = "";
		
		//trim whitespace
		query = query.replaceAll("\\s","");
		
		List<Person> persons = personDao.searchForPersons(query);
		if (persons.size() > 0){
			for (Person p:persons){
				desc = p.getLastname() + "," + p.getFirstname();
				emailAddress = p.getEmail();
				
				EmailRecipient individualER = new EmailRecipient(EmailRecipient.RecipientType.INDIVIDUAL, desc, emailAddress);
				recipients.add(individualER);
			}
		}
		
		List<Role> roles = roleDao.searchForRoles(query);
		if (roles.size() > 0){
			for (Role r:roles){
				desc = r.getCommitee().getDescription();
				
				EmailRecipient groupER = new EmailRecipient(EmailRecipient.RecipientType.GROUP, desc, EmailRecipient.RecipientType.GROUP + "_" + r.getCommitee());
				recipients.add(groupER);
			}
		}
		
		/**
		 * Protocol Specific email group... need to think about Contract module...
		 */
		//recipients.add(new EmailRecipient(EmailRecipient.RecipientType.GROUP, "Principal Investigator(s)", EmailRecipient.RecipientType.GROUP + "_PI"));
		//recipients.add(new EmailRecipient(EmailRecipient.RecipientType.GROUP, "Protocol Notification Group"));
		//recipients.add(new EmailRecipient(EmailRecipient.RecipientType.GROUP, "Primary Contact(s)"));
		//recipients.add(new EmailRecipient(EmailRecipient.RecipientType.GROUP, "Budget Manager(s)"));
		
		
		//EmailRecipient e = new EmailRecipient(EmailRecipient.RecipientType.INDIVIDUAL, "Bian, Jiang", "jbian@uams.edu");
		//recipients.add(e);
		
		results.put("recipients", recipients);
		
		return results;
	}


	public PersonDao getPersonDao() {
		return personDao;
	}
	
	@Autowired(required=true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}
	
	@Autowired(required=true)
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
}