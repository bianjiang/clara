package edu.uams.clara.webapp.common.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.RoleDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailService;

public class EmailServiceImpl implements EmailService {
	private final static Logger logger = LoggerFactory
			.getLogger(EmailServiceImpl.class);

	private JavaMailSender mailSender;

	private VelocityEngine velocityEngine;

	private UserDao userDao;

	private RoleDao roleDao;

	@Value("${fileserver.remote.dir.path}")
	private String fileRemoteDirPath;
	
	private boolean shouldSendEmail = false;

	private void removeEmptyAndNull(List<String> in) {
		Iterator<String> i = in.iterator();
		while (i.hasNext()) {
			String s = i.next();
			if (s == null | s.trim().isEmpty()) {
				i.remove();
			}
		}

	}

	@Override
	public void sendEmail(final String text, final List<String> mailTo,
			final List<String> cc, final String realSubject,
			final List<String> files) {
		if (!this.shouldSendEmail) return;

		if (mailTo != null) {
			removeEmptyAndNull(mailTo);
		}

		if (cc != null) {
			removeEmptyAndNull(cc);
		}

		logger.debug("mailTo Size: " + ((mailTo != null) ? mailTo.size() : 0));
		logger.debug("CC Size: " + ((cc != null) ? cc.size() : 0));
		final String[] ccTo = (cc != null && !cc.isEmpty()) ? cc
				.toArray(new String[cc.size()]) : null;
		final String[] mailToString = mailTo.toArray(new String[mailTo.size()]); // use
																					// https://code.google.com/p/guava-libraries/wiki/CollectionUtilitiesExplained#Sets
																					// filter
																					// empty
																					// string
		/*
		for (String m : mailToString) {
			logger.debug("" + m);
		}
		*/

		final String[] bccTo = { "JBian@uams.edu", "FYu2@uams.edu", "JYuan@uams.edu", "MBaker@uams.edu" };

		// for (final User user: mailTo){
		if (mailToString != null && mailToString.length > 0) {
			MimeMessagePreparator preparator = new MimeMessagePreparator() {
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
					message.setTo(mailToString);
					if (ccTo != null) {
						message.setCc(ccTo);
					}
					message.setBcc(bccTo);

					message.setFrom("CLARA - Clinical Research Administration System <NoReply@clara.uams.edu>");
					message.setSubject(realSubject);

					String testText = "";

					if (getFileRemoteDirPath().contains("/training")
							|| getFileRemoteDirPath().contains("/dev")) {
						testText = "<h2>TEST ONLY!</h2>";
					}

					message.setText(testText + text.replace("{0}", ""), true);

					// TODO add attachment
					/*
					 * FileSystemResource file; if (files.size() > 0){ for(String
					 * s:files) { file = new FileSystemResource(new File(s));
					 * message.addAttachment(s, file); } }
					 */
				}
			};
			this.mailSender.send(preparator);
		}
		
		// }
	}

	private JavaType listOfEmailRecipient = TypeFactory.defaultInstance()
			.constructCollectionType(ArrayList.class, EmailRecipient.class);

	@Override
	public List<EmailRecipient> getEmailRecipients(String jsonEncodedTo)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		// List<EmailRecipient> emailRecipients =
		// objectMapper.readValue(jsonEncodedTo, new
		// TypeReference<ArrayList<EmailRecipient>>(){});
		List<EmailRecipient> emailRecipients = Lists.newArrayList();
		
		try {
			emailRecipients = objectMapper.readValue(
					jsonEncodedTo, listOfEmailRecipient);
		} catch (Exception e){
			e.printStackTrace();
			logger.warn("Cannot parse email recipient json: " + jsonEncodedTo);
		}

		return emailRecipients;
	}
	
	private List<String> parseEmailTemplateEmailAddress(
			EmailRecipient emailRecipient) {
		List<String> realEmailAddresses = new ArrayList<String>();
		if (emailRecipient.getAddress().contains("INDIVIDUAL")) {
			String realEmailAddress = emailRecipient.getAddress().substring(11);
			realEmailAddresses.add(realEmailAddress);
		}

		if (emailRecipient.getAddress().contains("GROUP")) {
			String gropuName = emailRecipient.getAddress().substring(6);

			for (Committee c : Committee.values()) {
				if (c.toString().equals(gropuName)) {
					Permission p = roleDao.getRoleByCommittee(c)
							.getRolePermissionIdentifier();
					if (p != null) {
						List<User> users = userDao.getUsersByUserRole(p);
						for (User u : users) {
							String realGroupEmailAddress = u.getPerson()
									.getEmail();
							
							realEmailAddresses.add(realGroupEmailAddress);
							
							if (!u.getAlternateEmail().isEmpty())
								realEmailAddresses.add(u.getAlternateEmail());
						}
					}
				}
			}
		}

		return realEmailAddresses;
	}

	private List<String> parseEmailAddress(String emailRecipient) {
		List<String> realEmailAddresses = new ArrayList<String>();
		if (emailRecipient.contains("INDIVIDUAL")) {
			String realEmailAddress = emailRecipient.substring(11);
			realEmailAddresses.add(realEmailAddress);
		} else if (emailRecipient.contains("GROUP")) {
			String gropuName = emailRecipient.substring(6);

			for (Committee c : Committee.values()) {
				if (c.toString().equals(gropuName)) {
					Permission p = roleDao.getRoleByCommittee(c)
							.getRolePermissionIdentifier();
					if (p != null) {
						List<User> users = userDao.getUsersByUserRole(p);
						for (User u : users) {
							String realGroupEmailAddress = u.getPerson()
									.getEmail();
							
							realEmailAddresses.add(realGroupEmailAddress);
							
							if (!u.getAlternateEmail().isEmpty())
								realEmailAddresses.add(u.getAlternateEmail());
						}
					}
				}
			}
		} else {
			realEmailAddresses.add(emailRecipient);
		}

		return realEmailAddresses;
	}

	@Override
	public List<String> getRecipientsAddress(List<String> recipientLst) {
		List<String> emailLst = new ArrayList<String>();

		for (String recipient : recipientLst) {
			List<String> emailList = parseEmailAddress(recipient);
			for (String email : emailList) {
				emailLst.add(email);
			}
		}

		return emailLst;
	}

	@Override
	public List<String> getTemplateRecipientsAddress(
			List<EmailRecipient> recipientLst) {
		List<String> emailLst = new ArrayList<String>();

		for (EmailRecipient recipient : recipientLst) {
			List<String> emailList = parseEmailTemplateEmailAddress(recipient);
			for (String email : emailList) {
				emailLst.add(email);
			}
		}

		return emailLst;
	}

	@Override
	public List<String> setRealReceiptByEmailAddress(List<String> mailToList) {
		List<String> realReceiptLst = new ArrayList<String>();

		for (String mailTo : mailToList) {
			if (mailTo.contains("GROUP")) {
				Committee committee = Committee.valueOf(mailTo.substring(6));
				// Role role = roleDao.getRoleByCommittee(committee);
				// Permission p = role.getRolePermissionIdentifier();
				// List<User> userList = userDao.getUsersByUserRole(p);

				String group = "{\"address\":\"GROUP_" + committee.toString()
						+ "\",\"type\":\"GROUP\",\"desc\":\""
						+ committee.getDescription() + "\"}";
				realReceiptLst.add(group);
			} else {
				User u = userDao.getUserByEmail(mailTo);

				String individual = "{\"address\":\"INDIVIDUAL_" + mailTo
						+ "\",\"type\":\"INDIVIDUAL\",\"desc\":\""
						+ u.getPerson().getFullname() + "\"}";
				realReceiptLst.add(individual);
			}
		}

		return realReceiptLst;
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	@Autowired(required = true)
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	@Autowired(required = true)
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	@Autowired(required = true)
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public String getFileRemoteDirPath() {
		return fileRemoteDirPath;
	}

	public void setFileRemoteDirPath(String fileRemoteDirPath) {
		this.fileRemoteDirPath = fileRemoteDirPath;
	}

	public boolean isShouldSendEmail() {
		return shouldSendEmail;
	}
	
	@Value("${should.sendemail}")
	public void setShouldSendEmail(boolean shouldSendEmail) {
		this.shouldSendEmail = shouldSendEmail;
	}
}