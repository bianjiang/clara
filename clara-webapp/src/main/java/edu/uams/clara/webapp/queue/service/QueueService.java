package edu.uams.clara.webapp.queue.service;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Service
public abstract class QueueService {

	private final static Logger logger = LoggerFactory
			.getLogger(QueueService.class);

	private XmlProcessor xmlProcessor;

	public abstract String getFormsInQueueByUser(String queueIdentifier,
			User user, boolean showHistory) throws XPathExpressionException,
			SAXException, IOException;

	public abstract Form getForm(long formId);

	protected synchronized Element __generateAssignedReviewerXMLStub(Form form,
			Committee committee, User currentUser, UserRole reviewerUserRole) {

		String assignedReviewerXmlData = "<assigned-reviewer assigning-committee=\""
				+ committee
				+ "\" user-role-committee=\""
				+ reviewerUserRole.getRole().getCommitee()
				+ "\" user-role=\""
				+ reviewerUserRole.getRole().getRolePermissionIdentifier()
				+ "\" user-role-id=\""
				+ reviewerUserRole.getId()
				+ "\" user-id=\""
				+ reviewerUserRole.getUser().getId()
				+ "\" user-fullname=\""
				+ reviewerUserRole.getUser().getPerson().getFullname()
				+ "\" user-firstname=\""+ reviewerUserRole.getUser().getPerson().getFirstname() +"\" user-lastname=\""+ reviewerUserRole.getUser().getPerson().getLastname() +"\">"
				+ reviewerUserRole.getUser().getPerson().getFullname()
				+ "</assigned-reviewer>";
		Element assignedReviewerEl = null;

		try {
			assignedReviewerEl = xmlProcessor.loadXmlStringToDOM(
					assignedReviewerXmlData).getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return assignedReviewerEl;
	}

	/**
	 * @TODO do we need to worry about race condition? we make sure the
	 *       /<root>/committee-review/committee element exist in the form medata
	 * @param formMetaDataXml
	 * @param assignToCommittee
	 * @return
	 */
	private synchronized Element __ensureCommitteeReviewElement(
			Document formMetaXmlDoc, Committee assignToCommittee) {

		try {

			XPath xPath = xmlProcessor.getXPathInstance();

			Element rootElement = formMetaXmlDoc.getDocumentElement();

			logger.debug("rootElement is:" + rootElement.getNodeName());

			String committeeReviewElXPath = "committee-review";

			Element committeeReviewEl = (Element) xPath.evaluate(
					committeeReviewElXPath, rootElement, XPathConstants.NODE);

			// no committeeReviewEl... create
			if (committeeReviewEl == null) {
				committeeReviewEl = formMetaXmlDoc
						.createElement("committee-review");
				rootElement.appendChild(committeeReviewEl);
			}

			String committeeElXPath = "committee[@type='"
					+ assignToCommittee.toString() + "']";

			Element committeeEl = (Element) xPath.evaluate(committeeElXPath,
					committeeReviewEl, XPathConstants.NODE);

			// no committee[@type=""]... create
			if (committeeEl == null) {
				committeeEl = formMetaXmlDoc.createElement("committee");
				committeeEl.setAttribute("type", assignToCommittee.toString());
				committeeReviewEl.appendChild(committeeEl);
			}

			String assignedReviewersXPath = "assigned-reviewers";

			Element assignedReviewersEl = (Element) xPath.evaluate(
					assignedReviewersXPath, committeeEl, XPathConstants.NODE);

			if (assignedReviewersEl == null) {
				assignedReviewersEl = formMetaXmlDoc
						.createElement("assigned-reviewers");
				committeeEl.appendChild(assignedReviewersEl);
			}

			return assignedReviewersEl;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	public synchronized void addAssignedReviewerToMetaData(Form form,
			Committee assigingCommittee, User currentUser,
			UserRole reviewerUserRole) {
		Committee assignToCommittee = reviewerUserRole.getRole().getCommitee();

		try {
			XPath xPath = xmlProcessor.getXPathInstance();
			Document formMetaXmlDoc = xmlProcessor.loadXmlStringToDOM(form
					.getMetaXml());

			String existingAssignedReviewerPath = "committee-review/committee[@type='"
					+ reviewerUserRole.getRole().getCommitee()
					+ "']/assigned-reviewers/assigned-reviewer[@assigning-committee='"
					+ assigingCommittee.toString()
					+ "' and @user-role-id='"
					+ reviewerUserRole.getId() + "']";

			Element assignedReviewerEl = (Element) xPath.evaluate(
					existingAssignedReviewerPath,
					formMetaXmlDoc.getDocumentElement(), XPathConstants.NODE);

			if (assignedReviewerEl == null) {

				Element assignedReviewersEl = __ensureCommitteeReviewElement(
						formMetaXmlDoc, assignToCommittee);

				assignedReviewerEl = __generateAssignedReviewerXMLStub(form,
						assigingCommittee, currentUser, reviewerUserRole);
				assignedReviewersEl.appendChild(formMetaXmlDoc.importNode(
						assignedReviewerEl, true));

				// logger.debug(DomUtils.elementToString(formMetaXmlDoc));

				saveOrUpdateFormMetaDataXml(form,
						DomUtils.elementToString(formMetaXmlDoc));

			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("failed to add assigned reviewer: " + e.getMessage());
		}

	}

	public synchronized void removeAssignedReviewerFromMetaData(Form form,
			Committee assigingCommittee, User currentUser,
			UserRole reviewerUserRole) {

		try {
			XPath xPath = xmlProcessor.getXPathInstance();

			Document formMetaXmlDoc = xmlProcessor.loadXmlStringToDOM(form
					.getMetaXml());

			/*String existingAssignedReviewerPath = "committee-review/committee[@type='"
					+ reviewerUserRole.getRole().getCommitee()
					+ "']/assigned-reviewers/assigned-reviewer[@assigning-committee='"
					+ assigingCommittee.toString()
					+ "' and @user-role-id='"
					+ reviewerUserRole.getId() + "']";*/
			String existingAssignedReviewerPath = "committee-review/committee[@type='"
					+ reviewerUserRole.getRole().getCommitee()
					+ "']/assigned-reviewers/assigned-reviewer";

			Element assignedReviewerEl = (Element) xPath.evaluate(
					existingAssignedReviewerPath,
					formMetaXmlDoc.getDocumentElement(), XPathConstants.NODE);

			// logger.warn("whate???");
			// logger.debug(DomUtils.elementToString(assignedReviewerEl));

			if (assignedReviewerEl != null) {
				assignedReviewerEl.getParentNode().removeChild(
						assignedReviewerEl);
				logger.debug(DomUtils.elementToString(assignedReviewerEl));

				saveOrUpdateFormMetaDataXml(form,
						DomUtils.elementToString(formMetaXmlDoc));
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("failed to add assigned reviewer: " + e.getMessage());
		}

	}

	public synchronized void updateAssignedReviewersToMetaData(Form form,
			Committee assigingCommittee, User currentUser,
			List<UserRole> reviewersUserRole) {
		if (reviewersUserRole.size() == 0) {
			logger.warn("empty reviewersUserRole???");
			return;
		}
		// they all should be in the same committee, so only take the first
		// one...
		Committee assignToCommittee = reviewersUserRole.get(0).getRole()
				.getCommitee();
		try {
			Document formMetaXmlDoc = xmlProcessor.loadXmlStringToDOM(form
					.getMetaXml());

			Element assignedReviewersEl = __ensureCommitteeReviewElement(
					formMetaXmlDoc, assignToCommittee);

			for (UserRole reviewerUserRole : reviewersUserRole) {

				Element assignedReviewerEl = __generateAssignedReviewerXMLStub(
						form, assigingCommittee, currentUser, reviewerUserRole);
				assignedReviewersEl.appendChild(formMetaXmlDoc.importNode(
						assignedReviewerEl, true));
			}

			saveOrUpdateFormMetaDataXml(form,
					DomUtils.elementToString(formMetaXmlDoc));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("failed to update assigned reviewers: "
					+ e.getMessage());
		}
	}

	protected abstract void saveOrUpdateFormMetaDataXml(Form form,
			String metaDataXml);

	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;

	public void triggerAssignReviewerAction(Form form, Committee committee,
			User currentUser, String action) throws XPathExpressionException,
			IOException, SAXException {

		businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(
				form.getFormType()).triggerAction(form, committee, currentUser,
				action, null, null);
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	@Autowired(required = true)
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}
}
