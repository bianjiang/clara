package edu.uams.clara.webapp.common.service.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public abstract class BusinessObjectTrackService<T> extends
		ObjectTrackService<T> {

	private final static Logger logger = LoggerFactory
			.getLogger(BusinessObjectTrackService.class);

	private String getAssignedReviewer(Form form, Committee committee, boolean assigning) {
		String assignedReviewer = "";
		
		String lookupPath = "";
		
		if (assigning){
			lookupPath = "//committee-review/committee/assigned-reviewers/assigned-reviewer[@assigning-committee='"
					+ committee + "']";
		} else {
			lookupPath = "//committee-review/committee/assigned-reviewers/assigned-reviewer[@user-role-committee='"
					+ committee + "']";
		}

		try {
			Document doc = getXmlProcessor().loadXmlStringToDOM(
					form.getMetaXml());
			XPath xpath = getXmlProcessor().getXPathInstance();

			NodeList reviewersLst = (NodeList) xpath.evaluate(lookupPath, doc,
					XPathConstants.NODESET);

			if (reviewersLst.getLength() > 0) {
				for (int i = 0; i < reviewersLst.getLength(); i++) {
					Element userEl = (Element) reviewersLst.item(i);

					String userName = userEl.getAttribute("user-fullname");
					String userRole = userEl.getAttribute("user-role");
					
					if (committee.toString().contains("CONTRACT")){
						assignedReviewer += userName + ", ";
					} else {
						assignedReviewer += userName + "(" + userRole + "), ";
					}	
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return assignedReviewer;
	}

	private Map<String, Committee> assignedReviewerByCommittee = new HashMap<String, Committee>();
	{
		assignedReviewerByCommittee.put("{BUDGET_MANAGER_ASSIGNED_REVIEWER}",
				Committee.BUDGET_MANAGER);
		assignedReviewerByCommittee.put(
				"{REGULATORY_MANAGER_ASSIGNED_REVIEWER}",
				Committee.REGULATORY_MANAGER);
		assignedReviewerByCommittee.put("{IRB_ASSIGNER_ASSIGNED_REVIEWER}",
				Committee.IRB_ASSIGNER);
		assignedReviewerByCommittee.put("{CONTRACT_MANAGER_ASSIGNED_REVIEWER}",
				Committee.CONTRACT_MANAGER);
		assignedReviewerByCommittee.put(
				"{CONTRACT_LEGAL_REVIEW_ASSIGNED_REVIEWER}",
				Committee.CONTRACT_LEGAL_REVIEW);
	}

	protected Map<String, String> getAttributeValues(Form form, User user,
			Map<String, Object> attributeRawValues) {

		Committee committee = (Committee) attributeRawValues.get("COMMITTEE");
		String committeeNote = attributeRawValues.get("COMMITTEE_NOTE") != null ? attributeRawValues
				.get("COMMITTEE_NOTE").toString() : "";

		String oldFormStatus = getOldFormStatus(attributeRawValues);

		String formStatus = getFormStatus(attributeRawValues);

		String selectedCommitteesString = "";

		if (attributeRawValues.get("SELECTED_COMMITTEES") != null) {
			List<Committee> selectedCommittees = (List) attributeRawValues
					.get("SELECTED_COMMITTEES");

			for (Committee involvedCommittee : selectedCommittees) {
				selectedCommitteesString += "<b>"
						+ involvedCommittee.getDescription() + "</b>, ";
			}

			selectedCommitteesString = selectedCommitteesString.substring(0,
					selectedCommitteesString.length() - 2);
		}

		Date now = new Date();
		Map<String, String> attributeValues = new HashMap<String, String>();

		attributeValues.put("{FORM_TYPE}", form.getFormType());
		attributeValues.put("{FORM_ID}", Long.toString(form.getFormId()));
		attributeValues.put("{PARENT_FORM_ID}", Long.toString(form.getParentFormId()));
		attributeValues.put("{FORM_STATUS_DESC}",
				formStatus != null ? formStatus : "");
		attributeValues.put("{OLD_FORM_STATUS_DESC}",
				oldFormStatus != null ? oldFormStatus : "");
		attributeValues.put("{USER_ID}", "" + user.getId());
		attributeValues.put("{USER_NAME}", user.getPerson().getFullname());
		attributeValues.put("{USER_WITH_EMAIL_LINK}", "<a href=\"mailto:"
				+ user.getPerson().getEmail() + "\">"
				+ user.getPerson().getLastname() + ", "
				+ user.getPerson().getFirstname() + "</a>");
		attributeValues.put("{COMMITTEE_DESC}", committee.getDescription());
		attributeValues.put("{CONTRACT_IDENTIFIER}", form.getIdentifier());
		attributeValues.put("{PROTOCOL_IDENTIFIER}", form.getIdentifier());
		attributeValues.put("{NOW_TIMESTAMP}", "" + now.getTime());
		attributeValues.put("{NOW_DATETIME}", DateFormatUtil.formateDate(now));
		attributeValues.put("{SELECTED_COMMITTEES}", selectedCommitteesString);
		attributeValues.put("{ASSIGNED_EXPEDITED_REVIEWER_FULLNAME}", getAssignedReviewer(form, Committee.IRB_EXPEDITED_REVIEWER, false));
		attributeValues.put("{ASSIGNED_EXEMPT_REVIEWER_FULLNAME}", getAssignedReviewer(form, Committee.IRB_EXEMPT_REVIEWER, false));
		for (Entry<String, Committee> entry : assignedReviewerByCommittee
				.entrySet()) {
			attributeValues.put(entry.getKey(),
					getAssignedReviewer(form, entry.getValue(), true));
		}
		attributeValues.put("{COMMITTEE_NOTE}", committeeNote); // (getCommitteeNote(form,
																// committee)!=null)?getCommitteeNote(form,
																// committee):"");
		attributeValues.put("{CANCEL_REASON}", (attributeRawValues
				.get("REASON") != null) ? attributeRawValues.get("REASON")
				.toString() : "");
		attributeValues
				.put("{CANCEL_SUB_REASON}",
						(attributeRawValues.get("SUB_REASON") != null) ? attributeRawValues
								.get("SUB_REASON").toString() : "");
		attributeValues
				.put("{REVISION_REQUEST_COMMITTEE}",
						(attributeRawValues.get("REVISION_REQUEST_COMMITTEE") != null) ? attributeRawValues
								.get("REVISION_REQUEST_COMMITTEE").toString()
								: "");
		
		attributeValues
		.put("{NEXT_COMMITTEE}",
				(attributeRawValues.get("NEXT_COMMITTEE") != null) ? attributeRawValues
						.get("NEXT_COMMITTEE").toString()
						: "");
		
		attributeValues = getFormCommitteeStatusAttributeValues(form, committee, attributeValues);

		return attributeValues;
	}

	public void logStatusChange(Form form, User user,
			Map<String, Object> attributeRawValues, String logsTemplate)
			throws IOException, SAXException {

		Track track = getOrCreateTrackFromChildObject(form);

		Document logsDoc = getLogsDocument(track);

		// @TODO common elements should be only loaded once...
		Map<String, String> attributeValues = getAttributeValues(form, user,
				attributeRawValues);

		XmlProcessor xmlProcess = getXmlProcessor();

		Document logsTemplateDoc = xmlProcess.loadXmlStringToDOM(logsTemplate);

		NodeList logs = logsTemplateDoc.getDocumentElement()
				.getElementsByTagName("log");

		for (int i = 0; i < logs.getLength(); i++) {
			Map<String, String> attributes = new HashMap<String, String>();
			logger.debug("i: " + i);
			Element logEl = (Element) logs.item(i);

			String logType = logEl.getAttribute("log-type");

			NamedNodeMap attributesList = logEl.getAttributes();

			for (int j = 0; j < attributesList.getLength(); j++) {
				attributes.put(attributesList.item(j).getNodeName(),
						attributesList.item(j).getTextContent());
			}

			if (logType.equals("NOTIFICATION") || logType.equals("LETTER")) {
				logger.debug("it's a " + logType);
				appendNotificastionAttributesValues(logType, logEl,
						attributeValues, attributeRawValues);
			}

			attributes = fillAttributeValue(attributes, attributeValues);

			String logTextContent = fillMessage(logEl.getTextContent(),
					attributeValues);

			logger.debug("logTextContent: " + logTextContent);

			logsDoc = appendLogToLogsDoc(logsDoc, user, logTextContent,
					attributes);

		}

		track = updateTrack(track, logsDoc);

	}

	public void appendNotificastionAttributesValues(String logType,
			Element logEl, Map<String, String> attributeValues,
			Map<String, Object> attributeRawValues) throws IOException,
			SAXException {
		List<EmailTemplate> emailTemplates = attributeRawValues
				.get("EMAIL_TEMPLATES") != null ? ((ArrayList<EmailTemplate>) attributeRawValues
				.get("EMAIL_TEMPLATES")) : null;

		if (emailTemplates == null) {
			return;
		}

		String emailTemplateIdentifier = (logEl
				.getAttribute("email-template-identifier") != null && !logEl
				.getAttribute("email-template-identifier").isEmpty()) ? logEl
				.getAttribute("email-template-identifier") : logEl
				.getAttribute("event-type");
		/*
		 * XmlProcessor xmlProcessor = getXmlProcessor();
		 * 
		 * XPath xPath = xmlProcessor.getXPathInstance(); Document
		 * logTemplateXmlDocument = xmlProcessor
		 * .loadXmlFileToDOM(getLogTemplateXmlFilePath());
		 */

		String notificationLogTemplate = "<br/>The following committees/groups/individuals were notified: <br/>";
		// String letterLogTemplate =
		// "<br/>The following committees/groups/individuals were notified: <br/>";

		for (EmailTemplate emailTemplate : emailTemplates) {
			if (emailTemplate.getIdentifier().equals(emailTemplateIdentifier)) {
				
				String recipents = "";
				recipents += "<b>" + emailTemplate.getLogRecipient() + "</b>; ";

				attributeValues.put("{EMAIL_NOTIFICATION_LOG}",
						notificationLogTemplate + recipents);
				
				/*
				String remotePath = "";
				
				if (getFileRemoteDirPath().contains("/training")){
					remotePath = "/training";
				} else if (getFileRemoteDirPath().contains("/dev")){
					remotePath = "/dev";
				}
				*/
				
				if (logType.equals("LETTER")) {
					UploadedFile uploadedFile = emailTemplate.getUploadedFile();
					if (uploadedFile != null) {
						attributeValues.put(
								"{LETTER_LINK}",
								"<a target=\"_blank\" href=\"" + this.getFileServer()
										+ uploadedFile.getPath()
										+ uploadedFile.getIdentifier() + "."
										+ uploadedFile.getExtension()
										+ "\">View Letter</a>");
					}
				}
			}
		}
	}
	
	public String getSingleLog(String objectType, long objectId, String logId){
		String logXml = "";
		try {
			Track track = getOrCreateTrack(objectType,
					objectId);
			
			String trackXml = track.getXmlData();
			
			logXml = getXmlProcessor().listElementsByPath("/logs/log[@id='"+ logId +"']", trackXml, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return logXml;
	}
	
	public Document addNewNotification(Document actionXmlDoc, String notificationType, String emailTemplateIdentifier, String xPathCondition, String formXPathCondition, String logType, String eventType, String logText){
		try {
			Element rootActionEl = actionXmlDoc.getDocumentElement();
			
			XPath xPath = getXmlProcessor().getXPathInstance();
			
			Element notificationsEl = (Element) (xPath.evaluate(
					"//notifications", actionXmlDoc,
					XPathConstants.NODE));
			
			if (notificationsEl == null) {
				notificationsEl = actionXmlDoc.createElement("notifications");
				
				rootActionEl.appendChild(notificationsEl);
			}
			
			Element notificationEl = actionXmlDoc.createElement("notification");
			if (xPathCondition != null) {
				notificationEl.setAttribute("xpath-condition", xPathCondition);
			} 
			
			if (formXPathCondition != null) {
				notificationEl.setAttribute("form-xpath-condition", formXPathCondition);
			}
			
			notificationEl.setAttribute("notification-type", notificationType);
			notificationEl.setAttribute("email-template-identifier", emailTemplateIdentifier);
			
			notificationsEl.appendChild(notificationEl);
			
			Element notificationLogsEl = actionXmlDoc.createElement("logs");
			notificationEl.appendChild(notificationLogsEl);
			
			Element notificationLogEl = actionXmlDoc.createElement("log");
			notificationLogEl.setAttribute("log-type", logType);
			notificationLogEl.setAttribute("event-type", eventType);
			notificationLogEl.setAttribute("form-type", "{FORM_TYPE}");
			notificationLogEl.setAttribute("form-id", "{FORM_ID}");
			notificationLogEl.setAttribute("parent-form-id", "{PARENT_FORM_ID}");
			notificationLogEl.setAttribute("action-user-id", "{USER_ID}");
			notificationLogEl.setAttribute("actor", "{COMMITTEE_DESC}");
			notificationLogEl.setAttribute("timestamp", "{NOW_TIMESTAMP}");
			notificationLogEl.setAttribute("date-time", "{NOW_DATETIME}");
			notificationLogEl.setTextContent("<span class=\"history-log-message\">" + logText + "</span>");
			
			notificationLogsEl.appendChild(notificationLogEl);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return actionXmlDoc;
	}
	
	public Document addNewEvent(Document actionXmlDoc, String eventName){
		try {
			Element rootActionEl = actionXmlDoc.getDocumentElement();
			
			XPath xPath = getXmlProcessor().getXPathInstance();
			
			Element eventsEl = (Element) (xPath.evaluate(
					"//events", actionXmlDoc,
					XPathConstants.NODE));
			
			if (eventsEl == null) {
				eventsEl = actionXmlDoc.createElement("events");
				
				rootActionEl.appendChild(eventsEl);
			}
			
			Element eventEl = actionXmlDoc.createElement("event");
			eventEl.setTextContent(eventName);
			
			eventsEl.appendChild(eventEl);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return actionXmlDoc;
	}
	
	/*
	 * Used to retrieve certain amount of the latest logs
	 * */
	public List<String> getLatestLogs(String type, long objectId, int rowNumber) {
		return getTrackDao().getLastestLogByObjectIdAndRowNumber(type, objectId, rowNumber);
	}

	public abstract String getFormStatus(Map<String, Object> attributeRawValues);

	public abstract String getOldFormStatus(
			Map<String, Object> attributeRawValues);

	public abstract Track getOrCreateTrackFromChildObject(Form form);

	//public abstract String getCommitteeNote(Form form, Committee committee);
	
	public abstract Map<String, String> getFormCommitteeStatusAttributeValues(Form form, Committee committee,
			Map<String, String> attributeValues);
}
