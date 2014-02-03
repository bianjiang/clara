package edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.velocity.VelocityEngineUtils;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.service.EmailService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/protocolform/newsubmission/ajax/NewSubmissionReviewAjaxControllerTest-context.xml"})
public class NewSubmissionReviewAjaxControllerTest {
	private final static Logger logger = LoggerFactory
			.getLogger(NewSubmissionReviewAjaxControllerTest.class);
	private EmailService emailService;
	//private EmailDataService emailDataService;

	private UserDao userDao;
	private VelocityEngine velocityEngine;

	@Test
	public void requestPharmacyReview(){
		//emailDataService.setEmailData(203);

		String templateName = "pharmacyreview.vm";

		List<User> users = userDao.getUsersByUserRole(Permission.ROLE_COLLEGE_DEAN);

		List<String> cc = new ArrayList<String>();
		cc.add("jbian@uams.edu");
		cc.add("fyu2@uams.edu");

		List<String> mailTo = new ArrayList<String>();
		for (User u:users){
			mailTo.add(u.getPerson().getEmail());
		}

		final String templateContent = VelocityEngineUtils
				.mergeTemplateIntoString(velocityEngine,
						"pharmacyreview.vm", null);

		emailService.sendEmail(templateContent, mailTo, cc, "Test", null);

	}

	public EmailService getEmailService() {
		return emailService;
	}

	@Autowired(required=true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}



	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}

	@Autowired(required=true)
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

}