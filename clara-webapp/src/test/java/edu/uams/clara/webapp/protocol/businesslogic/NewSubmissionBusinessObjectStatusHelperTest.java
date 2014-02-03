package edu.uams.clara.webapp.protocol.businesslogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/businesslogic/NewSubmissionBusinessObjectStatusHelperTest-context.xml" })
public class NewSubmissionBusinessObjectStatusHelperTest {

	private final static Logger logger = LoggerFactory
			.getLogger(NewSubmissionBusinessObjectStatusHelperTest.class);

	private ProtocolBusinessObjectStatusHelper newsubmssionBusinessObjectStatusHelper;

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private EmailTemplateDao emailTemplateDao;

	private ProtocolTrackService protocolTrackService;

	private UserDao userDao;

	@Test
	public void testConsentReviewerCondition() throws XPathExpressionException, IOException, SAXException {
		ProtocolForm protocolForm = protocolFormDao.findById(1662l);

		User user = userDao.findById(1l);

		newsubmssionBusinessObjectStatusHelper.triggerAction(protocolForm,
				Committee.IRB_ASSIGNER, user, "ASSIGN_REVIEWER", null, null);

	}

	// @Test
	public void testPreProcessCommitteeReviewXml() {

		String extraDataXml = "<committee-review><protocol-gatekeeper-review><GatekeeperAssignCommitteePanel><invovled-committees><committee>PHARMACY_REVIEW</committee><committee>BUDGET_REVIEW</committee><committee>COVERAGE_REVIEW</committee><committee>SUB_DEPARTMENT_CHIEF</committee><committee>DEPARTMENT_CHAIR</committee><committee>COLLEGE_DEAN</committee><committee>HOSPITAL_SERVICES</committee><committee>BIOSAFETY</committee></invovled-committees></GatekeeperAssignCommitteePanel></protocol-gatekeeper-review><actor>GATEKEEPER</actor><action>ASSIGN_TO_COMMITTEES</action></committee-review>";
		ProtocolForm protocolForm = protocolFormDao.findById(621l);
		logger.debug("protocolformId: "
				+ protocolForm.getId()
				+ "; protocolFormStatus: "
				+ protocolFormStatusDao.getProtocolFormStatusByFormId(
						protocolForm.getId()).getProtocolFormStatus()
				+ "; protocolId: " + protocolForm.getProtocol().getId());
		User user = userDao.findById(1l);

		newsubmssionBusinessObjectStatusHelper.preProcessCommitteeReviewXml(
				protocolForm, Committee.GATEKEEPER, user,
				"ASSIGN_TO_COMMITTEES", extraDataXml);

	}

	// @Test
	public void testGetEmailRecipients() throws JsonParseException, IOException {
		EmailTemplate emailTemplate = emailTemplateDao
				.findByIdentifier("NEW_SUBMISSION_SUBMITTED_TO_GATEKEEPER");
		// protocolTrackService.getEmailRecipients(emailTemplate.getTo());
		ObjectMapper objectMapper = new ObjectMapper();
		JavaType listOfEmailRecipient = TypeFactory.defaultInstance()
				.constructCollectionType(ArrayList.class, EmailRecipient.class);
		List<EmailRecipient> emailRecipients = objectMapper.readValue(
				emailTemplate.getTo(), listOfEmailRecipient);

		for (EmailRecipient e : emailRecipients) {
			logger.debug("desc: " + e.getDesc());
		}
	}

	// @Test
	public void testTriggerAction() throws XPathExpressionException,
			IOException, SAXException {
		ProtocolForm protocolForm = protocolFormDao.findById(621l);
		logger.debug("protocolformId: "
				+ protocolForm.getId()
				+ "; protocolFormStatus: "
				+ protocolFormStatusDao.getProtocolFormStatusByFormId(
						protocolForm.getId()).getProtocolFormStatus()
				+ "; protocolId: " + protocolForm.getProtocol().getId());
		User user = userDao.findById(1l);

		/*
		 * String extraDataXml = "<extra-xml-data><invovled-committees>";
		 * extraDataXml +=
		 * "<committee type=\"REQUIRED_BEFORE_IRB\">BIOSAFETY</committee>";
		 * extraDataXml += "</invovled-committees></extra-xml-data>";
		 */

		// newsubmssionBusinessObjectStatusHelper.triggerAction(protocolForm,
		// Committee.PI, user, "SIGN_SUBMIT", "IS_PI", null, null);

		/*
		 * newsubmssionBusinessObjectStatusHelper.triggerAction(protocolForm,
		 * Committee.GATEKEEPER, user, "ASSIGN_TO_COMMITTEES", null, null,
		 * extraDataXml);
		 */
	}

	public ProtocolBusinessObjectStatusHelper getNewsubmssionBusinessObjectStatusHelper() {
		return newsubmssionBusinessObjectStatusHelper;
	}

	@Autowired(required = true)
	public void setNewsubmssionBusinessObjectStatusHelper(
			ProtocolBusinessObjectStatusHelper newsubmssionBusinessObjectStatusHelper) {
		this.newsubmssionBusinessObjectStatusHelper = newsubmssionBusinessObjectStatusHelper;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}

	@Autowired(required = true)
	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}

	public ProtocolTrackService getProtocolTrackService() {
		return protocolTrackService;
	}

	@Autowired(required = true)
	public void setProtocolTrackService(
			ProtocolTrackService protocolTrackService) {
		this.protocolTrackService = protocolTrackService;
	}
}
