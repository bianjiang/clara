package edu.uams.clara.webapp.protocol.businesslogic;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.history.ProtocolTrackService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/protocol/businesslogic/ModificationBusinessObjectStatusHelperTest-context.xml" })
public class ModificationBusinessObjectStatusHelperTest {

	private final static Logger logger = LoggerFactory
			.getLogger(ModificationBusinessObjectStatusHelperTest.class);

	private ProtocolBusinessObjectStatusHelper modificationBusinessObjectStatusHelper;

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private EmailTemplateDao emailTemplateDao;

	private ProtocolTrackService protocolTrackService;

	private UserDao userDao;


	@Test
	public void testTriggerAction() throws XPathExpressionException,
			IOException, SAXException {
		ProtocolForm protocolForm = protocolFormDao.findById(1592l);
		logger.debug("protocolformId: "
				+ protocolForm.getId()
				+ "; protocolFormStatus: "
				+ protocolFormStatusDao.getProtocolFormStatusByFormId(
						protocolForm.getId()).getProtocolFormStatus()
				+ "; protocolId: " + protocolForm.getProtocol().getId());
		User user = userDao.findById(1l);
		
		String action = "ASSIGN_TO_COMMITTEES";
				
		
		String extraDataXml = "<committee-review><committee type=\"BUDGET_REVIEW\"><extra-content><invovled-committees><committee>COVERAGE_REVIEW</committee><committee>HOSPITAL_SERVICES</committee><committee>COMPLIANCE_REVIEW</committee><committee>IRB_ASSIGNER</committee></invovled-committees></extra-content><actor>BUDGET_REVIEW</actor><action>ASSIGN_TO_COMMITTEES</action><letter></letter></committee></committee-review>"; // "<committee-review><committee type=\"BUDGET_MANAGER\"><extra-content/><actor>BUDGET_MANAGER</actor><action>COMPLETE</action><letter/></committee></committee-review>"; //"<committee-review><committee type=\"BUDGET_REVIEW\"><extra-content><invovled-committees><committee>COVERAGE_REVIEW</committee><committee>HOSPITAL_SERVICES</committee><committee>IRB_ASSIGNER</committee><committee>COMPLIANCE_REVIEW</committee></invovled-committees></extra-content><actor>BUDGET_REVIEW</actor><action>ASSIGN_TO_COMMITTEES</action><letter></letter></committee></committee-review>";
		
		/*
		extraDataXml = modificationBusinessObjectStatusHelper.preProcessCommitteeReviewXml(protocolForm, Committee.BUDGET_REVIEW, user,
				action, extraDataXml);
		
		logger.debug("extraDataXml: " + extraDataXml);
		
		String condition = modificationBusinessObjectStatusHelper.checkCondition(protocolForm, Committee.BUDGET_REVIEW, user, action, extraDataXml);
		
		logger.debug("condition: " + condition);

		String workflow = modificationBusinessObjectStatusHelper.checkWorkflow(protocolForm, Committee.BUDGET_REVIEW, user, action,
				extraDataXml);
		
		logger.debug("workflow: " + workflow);
		*/
		
		modificationBusinessObjectStatusHelper.triggerAction(protocolForm, Committee.BUDGET_REVIEW, user, action, null, extraDataXml);
		
		//newsubmssionBusinessObjectStatusHelper.triggerAction(protocolForm,
		//		Committee.PI, user, "SIGN_SUBMIT", "IS_PI", null, null);


		/*
		 * newsubmssionBusinessObjectStatusHelper.triggerAction(protocolForm,
		 * Committee.GATEKEEPER, user, "ASSIGN_TO_COMMITTEES", null, null,
		 * extraDataXml);
		 */
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

	public ProtocolBusinessObjectStatusHelper getModificationBusinessObjectStatusHelper() {
		return modificationBusinessObjectStatusHelper;
	}

	@Autowired(required = true)
	public void setModificationBusinessObjectStatusHelper(
			ProtocolBusinessObjectStatusHelper modificationBusinessObjectStatusHelper) {
		this.modificationBusinessObjectStatusHelper = modificationBusinessObjectStatusHelper;
	}
}
