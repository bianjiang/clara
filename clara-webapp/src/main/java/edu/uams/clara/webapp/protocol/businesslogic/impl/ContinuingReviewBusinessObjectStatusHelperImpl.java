package edu.uams.clara.webapp.protocol.businesslogic.impl;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.businesslogic.ProtocolBusinessObjectStatusHelper;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ContinuingReviewBusinessObjectStatusHelperImpl extends
		ProtocolBusinessObjectStatusHelper {
	private final static Logger logger = LoggerFactory
			.getLogger(ContinuingReviewBusinessObjectStatusHelperImpl.class);

	public ContinuingReviewBusinessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
	}

	@Override
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		logger.debug("checkCondition->extraDataXml:" + extraDataXml);

		ProtocolForm protocolForm = (ProtocolForm) form;

		String formStatus = getFormStatus(form);

		if (formStatus.equals(""))
			return "";

		ProtocolFormStatusEnum protocolFormStatus = ProtocolFormStatusEnum
				.valueOf(formStatus);

		XmlProcessor xmlProcessor = getXmlProcessor();
		XPath xPath = xmlProcessor.getXPathInstance();

		String condition = "";

		switch (protocolFormStatus) {
		case UNDER_IRB_PREREVIEW:
			if (Committee.IRB_ASSIGNER.equals(committee)
					&& action.equals("ASSIGN_REVIEWER")) {

				try {
					Document extraDataXmlDoc = xmlProcessor
							.loadXmlStringToDOM(form.getMetaXml());
					logger.debug("xmlData: " + form.getMetaXml());
					Boolean hasConsentReviewerAssigned = (Boolean) (xPath
							.evaluate(
									"boolean(count(//assigned-reviewers/assigned-reviewer[@assigning-committee='IRB_ASSIGNER' and @user-role-committee='IRB_CONSENT_REVIEWER']) > 0)",
									extraDataXmlDoc, XPathConstants.BOOLEAN));
					condition = hasConsentReviewerAssigned ? "WITH_CONSENT"
							: "NO_CONSENT";

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			break;
		}

		/*
		 * switch (protocolFormStatus){ case UNDER_IRB_PREREVIEW: if
		 * (committee.equals(Committee.IRB_PROTOCOL_REVIEWER)) {
		 * ProtocolFormCommitteeStatus irbProtocolReviewStatus = this
		 * .getProtocolFormCommitteeStatusDao()
		 * .getLatestByCommitteeAndProtocolFormId(
		 * Committee.IRB_CONSENT_REVIEWER, protocolForm.getId());
		 * 
		 * if (irbProtocolReviewStatus.getProtocolFormCommitteeStatus()
		 * .equals(ProtocolFormCommitteeStatusEnum.APPROVED)) { condition =
		 * "IRB_PREREVIEW_COMPLETED"; } else { condition =
		 * "IRB_PREREVIEW_NOT_COMPLETED"; } }
		 * 
		 * if (committee.equals(Committee.IRB_CONSENT_REVIEWER)) {
		 * ProtocolFormCommitteeStatus irbConsentReviewStatus = this
		 * .getProtocolFormCommitteeStatusDao()
		 * .getLatestByCommitteeAndProtocolFormId(
		 * Committee.IRB_PROTOCOL_REVIEWER, protocolForm.getId());
		 * logger.debug("status:" + irbConsentReviewStatus
		 * .getProtocolFormCommitteeStatus()); if
		 * (irbConsentReviewStatus.getProtocolFormCommitteeStatus()
		 * .equals(ProtocolFormCommitteeStatusEnum.APPROVED)) { condition =
		 * "IRB_PREREVIEW_COMPLETED"; } else { condition =
		 * "IRB_PREREVIEW_NOT_COMPLETED"; } } break; }
		 */
		return condition;
	}
}
