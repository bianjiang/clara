package edu.uams.clara.webapp.common.businesslogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.exception.ClaraRunTimeException;
import edu.uams.clara.webapp.common.exception.ClaraRunTimeException.ErrorType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public abstract class BusinessObjectStatusHelper {

	private final static Logger logger = LoggerFactory
			.getLogger(BusinessObjectStatusHelper.class);

	private XmlProcessor xmlProcessor;

	private String workflowXmlFilePath;
	
	private String committeeListXmlFilePath;

	private String objectType;
	
	@Value("${application.host}")
	private String applicationHost;

	/***
	 * by default the does not check for conditions
	 * 
	 * @param form
	 * @param committee
	 * @param user
	 * @param action
	 * @param extraDataXml
	 * @return
	 */
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		return "";
	}

	public String checkWorkflow(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		return "";
	}

	/**
	 * this gives the sub-class a chance to clean up the committeeReviewXml
	 * before it gets sent forward, this also is used to make sure it's a valid
	 * xml @TODO needs to use xml validator not try catch......
	 * 
	 * @param form
	 * @param committee
	 * @param user
	 * @param action
	 * @param extraDataXml
	 * @return
	 */
	public String preProcessCommitteeReviewXml(Form form, Committee committee,
			User user, String action, String extraDataXml) {
		logger.debug("super->preProcessCommitteeReviewXml");

		if (extraDataXml == null || extraDataXml.isEmpty()) { // if no
																// extradataxml
																// comes in use
																// the
																// form.metadataxml
																// committee-review
																// part
			/*
			 * logger.debug("form.metadataxml -> " + form.getMetaXml()); XPath
			 * xpath = xmlProcessor.getXPathInstance();
			 * 
			 * try {
			 * 
			 * Document formMetaXmlDoc = xmlProcessor.loadXmlStringToDOM(form
			 * .getMetaXml()); Element committeeReviewEl = (Element)
			 * (xpath.evaluate( "//committee-review", formMetaXmlDoc,
			 * XPathConstants.NODE));
			 * 
			 * if (committeeReviewEl != null) { extraDataXml =
			 * DomUtils.elementToString(committeeReviewEl); } else {
			 * logger.warn(
			 * "super->preProcessCommitteeReviewXml: no extraXmlData in form.metadata either..."
			 * ); extraDataXml = "<committee-review></committee-review>"; }
			 * 
			 * } catch (Exception ex) { // TODO Auto-generated catch block
			 * ex.printStackTrace(); logger.warn(
			 * "super->preProcessCommitteeReviewXml: no extraXmlData in form.metadata either..."
			 * ); extraDataXml = "<committee-review></committee-review>"; }
			 */

			// not applicable...

			extraDataXml = "<committee-review></committee-review>";
		}

		// if it's none, it should just generate one... for committee-review...
		Document extraDataXmlDoc = null;
		try {
			extraDataXmlDoc = xmlProcessor.loadXmlStringToDOM(extraDataXml);
			// XPath xpath = xmlProcessor.getXPathInstance();

			if (!"committee-review".equals(extraDataXmlDoc.getDocumentElement()
					.getNodeName())) {
				throw new Exception("root is not committee-review");
			}
		} catch (Exception e) {
			extraDataXml = "<committee-review></committee-review>";
			/*
			 * extraDataXml = "<committee-review>" + extraDataXml +
			 * "</committee-review>"; try { extraDataXmlDoc = xmlProcessor
			 * .loadXmlStringToDOM(extraDataXml); } catch (Exception ex) {
			 * ex.printStackTrace();
			 * 
			 * }
			 */
		}

		return extraDataXml;
	}

	/***
	 * 
	 * @param form
	 * @param committee
	 * @param user
	 * @param action
	 * @param note
	 * @param extraDataXml
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public void triggerAction(Form form, Committee committee, User user,
			String action, String note, String extraDataXml)
			throws IOException, SAXException, XPathExpressionException {

		// let the sub-class has a chance to pre-process the xml-data
		extraDataXml = preProcessCommitteeReviewXml(form, committee, user,
				action, extraDataXml);

		String condition = checkCondition(form, committee, user, action,
				extraDataXml);

		String workflow = checkWorkflow(form, committee, user, action,
				extraDataXml);

		__triggerAction(form, committee, user, action, condition, workflow,
				note, extraDataXml);

	}

	/***
	 * 
	 * @param form
	 * @param committee
	 * @param user
	 * @param action
	 * @param note
	 * @param extraDataXml
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public void triggerAction(Form form, Committee committee, User user,
			String action, String condition, String workflow, String note,
			String extraDataXml) throws IOException, SAXException,
			XPathExpressionException {

		// let the sub-class has a chance to pre-process the xml-data
		extraDataXml = preProcessCommitteeReviewXml(form, committee, user,
				action, extraDataXml);

		__triggerAction(form, committee, user, action, condition, workflow,
				note, extraDataXml);

	}

	public String getEmailIdentifier(Form form, Committee committee,
			String action) throws IOException, SAXException,
			XPathExpressionException {
		String formType = form.getFormType();

		String formStatus = getFormStatus(form);

		String formCommitteeStatus = getFormCommitteeStatus(form, committee);

		String actionXpath = "/business-object-status/business-object[@type='"
				+ objectType + "']/form[@type='" + formType
				+ "' or @type='ANY']/form-status" + "[@value='" + formStatus
				+ "' or @value='ANY'][@form-committee-status='"
				+ formCommitteeStatus
				+ "' or @form-committee-status='ANY']/committee[@name='"
				+ committee.toString()
				+ "']/review-page/actions/action[@type='" + action + "']";

		logger.debug("email-actionXpath: " + actionXpath);

		XPath xPath = xmlProcessor.getXPathInstance();

		Document workflowXmlDocument = xmlProcessor
				.loadXmlFileToDOM(getWorkflowXmlFilePath());

		Element actionXmlEl = (Element) xPath.evaluate(actionXpath,
				workflowXmlDocument, XPathConstants.NODE);
		
		boolean needToLoadEmailId = Boolean.valueOf(actionXmlEl.getAttribute("load-email-template-identifier"));
		
		String emailTemplateIdentifier = "";
		
		if (!needToLoadEmailId) {
			emailTemplateIdentifier = actionXmlEl.getAttribute("email-template-identifier");
		} else {
			NodeList emailIds = actionXmlEl.getElementsByTagName("email-template-identifier");
			
			for (int i = 0; i < emailIds.getLength(); i++) {

				Element emailIdentifierEl = (Element) emailIds.item(i);
				
				String xpathCondition = emailIdentifierEl
						.getAttribute("xpath-condition");
				
				String formXpathCondition = emailIdentifierEl
						.getAttribute("form-xpath-condition");
				if (xpathCondition != null && !xpathCondition.isEmpty()){
					if (!xpathConditionCheck(form.getObjectMetaData(), xpathCondition)){
						continue;
					}
					
					//emailTemplateIdentifier = emailIdentifierEl.getTextContent();
				} 
				
				if (formXpathCondition != null && !formXpathCondition.isEmpty()) {
					if (!xpathConditionCheck(form.getMetaXml(), formXpathCondition)){
						continue;
					}
					
					//emailTemplateIdentifier = emailIdentifierEl.getTextContent();
				} /*else {
					emailTemplateIdentifier = emailIdentifierEl.getTextContent();
				}*/
				emailTemplateIdentifier = emailIdentifierEl.getTextContent();
			}
		}

		return emailTemplateIdentifier;

	}
	
	public String getEmailIdentifierByDecision(Form form, Committee committee,
			String decision) throws IOException, SAXException,
			XPathExpressionException {
		String emailTemplateIdentifier = "";
		
		String formType = form.getFormType();

		String formStatus = getFormStatus(form);

		String formCommitteeStatus = getFormCommitteeStatus(form, committee);

		String actionXpath = "/business-object-status/business-object[@type='"
				+ objectType + "']/form[@type='" + formType
				+ "' or @type='ANY']/form-status" + "[@value='" + formStatus
				+ "' or @value='ANY'][@form-committee-status='"
				+ formCommitteeStatus
				+ "' or @form-committee-status='ANY']/committee[@name='"
				+ committee.toString()
				+ "']/review-page/decisions/decision[value='" + decision + "']/email-template-identifiers/email-template-identifier";

		logger.debug("email-actionXpath: " + actionXpath);

		XPath xPath = xmlProcessor.getXPathInstance();

		Document workflowXmlDocument = xmlProcessor
				.loadXmlFileToDOM(getWorkflowXmlFilePath());

		NodeList emailIdentifiers = (NodeList) xPath.evaluate(actionXpath,
				workflowXmlDocument, XPathConstants.NODESET);

		for (int i = 0; i < emailIdentifiers.getLength(); i++) {

			Element emailIdentifierEl = (Element) emailIdentifiers.item(i);
			
			String xpathCondition = emailIdentifierEl
					.getAttribute("xpath-condition");
			
			String formXpathCondition = emailIdentifierEl
					.getAttribute("form-xpath-condition");
			
			if (xpathCondition != null && !xpathCondition.isEmpty()){
				if (!xpathConditionCheck(form.getObjectMetaData(), xpathCondition)){
					continue;
				}
				
				//emailTemplateIdentifier = emailIdentifierEl.getTextContent();
			} 
			
			if (formXpathCondition != null && !formXpathCondition.isEmpty()) {
				if (!xpathConditionCheck(form.getMetaXml(), formXpathCondition)){
					continue;
				}
				
				//emailTemplateIdentifier = emailIdentifierEl.getTextContent();
			} /*else {
				emailTemplateIdentifier = emailIdentifierEl.getTextContent();
			}*/
			emailTemplateIdentifier = emailIdentifierEl.getTextContent();
		}
		
		return emailTemplateIdentifier;

	}

	public String getReviewPageXml(Form form, Committee committee,
			String committeeReviewPage) throws IOException, SAXException,
			XPathExpressionException {
		String formType = form.getFormType();

		String formStatus = getFormStatus(form);

		String formCommitteeStatus = getFormCommitteeStatus(form, committee);

		Document workflowXmlDocument = xmlProcessor
				.loadXmlFileToDOM(getWorkflowXmlFilePath());

		logger.debug("get review page formStatus:" + formStatus);

		String committeeFormActionSectionXml = getCommitteeFormActionSectionByXpathCondition(
				workflowXmlDocument, form, committee, formType, formStatus,
				formCommitteeStatus, "");

		Document committeeFormActionSectionXmlDocument = xmlProcessor
				.loadXmlStringToDOM(committeeFormActionSectionXml);
		/*
		 * String decisionsXpath =
		 * "/business-object-status/business-object[@type='" + objectType +
		 * "']/form[@type='" + formType + "' or @type='ANY']/form-status" +
		 * "[@value='" + formStatus +
		 * "' or @value='ANY'][@form-committee-status='"+ formCommitteeStatus
		 * +"' or @form-committee-status='ANY']/committee[@name='" +
		 * committee.toString() + "']/review-page[@page-name='" +
		 * committeeReviewPage + "' or @page-name='ANY']/.";
		 */
		logger.debug("committeeFormActionSectionEl: "
				+ committeeFormActionSectionXml);
		String decisionsXpath = "//review-page[@page-name='"
				+ committeeReviewPage + "' or @page-name='ANY']/.";
		logger.debug("decisionsXpath: " + decisionsXpath);

		XPath xPath = xmlProcessor.getXPathInstance();

		//Node decisionsNode = (Node) xPath.evaluate(decisionsXpath,
				//committeeFormActionSectionXmlDocument, XPathConstants.NODE);
		
		/*
		 * Load different final review page depending on xpath condition
		 * */
		NodeList decisionsNodeList = (NodeList) xPath.evaluate(decisionsXpath,
				committeeFormActionSectionXmlDocument, XPathConstants.NODESET);
		
		for (int i = 0; i < decisionsNodeList.getLength(); i ++) {
			Element decisionsEl = (Element) decisionsNodeList.item(i);
			
			String xPathCondition = decisionsEl.getAttribute("xpath-condition");
			
			String formXpathCondition = decisionsEl
					.getAttribute("form-xpath-condition");
			
			if (xPathCondition != null && !xPathCondition.isEmpty()){
				if (!xpathConditionCheck(form.getObjectMetaData(), xPathCondition)){
					continue;
				}
			} 
			
			if (formXpathCondition != null && !formXpathCondition.isEmpty()) {
				if (!xpathConditionCheck(form.getMetaXml(), formXpathCondition)){
					continue;
				}
			} 
			
			return DomUtils.elementToString(decisionsNodeList.item(i));
			/*
			if (xPathCondition != null && !xPathCondition.isEmpty()) {
				if (xpathConditionCheck(form.getMetaXml(), xPathCondition)) {
					return DomUtils.elementToString(decisionsNodeList.item(i));
				} 
			} else {
				return DomUtils.elementToString(decisionsNodeList.item(i));
			}
			*/
		}
		
		return DomUtils.elementToString(decisionsNodeList.item(0));
		
		// check whether the final review custom panel needs to be loaded based
		// on certain condition
		// the final review custom panel is loaded based on the review-form-name
		// attribute on the review-page element
		// for modifiation form, if it's industry and coop, the Budget Review
		// actually needs to assign committees, for other studies, budget review
		// doesn't
		// condition is always checked against the form metadata xml

		//Element decisionsEl = ((Element) decisionsNode);
		/*
		String hasConditionalReviewPanel = decisionsEl
				.getAttribute("conditional-review-panel");

		if (Boolean.parseBoolean(hasConditionalReviewPanel)) {
			NodeList panelIdentifierNodes = (NodeList) xPath.evaluate(
					"//review-page-panel/panel-identifier", decisionsEl,
					XPathConstants.NODESET);
			String xpathCondition = null;
			for (int i = 0; i < panelIdentifierNodes.getLength(); i++) {
				Element panelIdentifierNode = (Element) panelIdentifierNodes
						.item(i);
				xpathCondition = panelIdentifierNode
						.getAttribute("xpath-condition");
				if (xpathCondition != null) {
					logger.debug("xpath-condition=" + xpathCondition);
					logger.debug("form.meta: " + form.getMetaXml());
					Document formMetaXmlDocument = xmlProcessor
							.loadXmlStringToDOM(form.getMetaXml());

					if ((Boolean) xPath.evaluate(xpathCondition,
							formMetaXmlDocument, XPathConstants.BOOLEAN)) {
						decisionsEl.setAttribute("review-form-name",
								panelIdentifierNode.getTextContent());
						break;
					}
				}
			}
		}*/
		/*
		String xPathCondition = decisionsEl.getAttribute("xpath-condition");
		
		if (xPathCondition != null && !xPathCondition.isEmpty()) {
			Document formMetaXmlDocument = xmlProcessor
					.loadXmlStringToDOM(form.getMetaXml());
			
			if ((Boolean) xPath.evaluate(xPathCondition,
							formMetaXmlDocument, XPathConstants.BOOLEAN)) {
				return DomUtils.elementToString(decisionsNode);
			} 
		}

		return DomUtils.elementToString(decisionsNode);*/
	}
	
	protected Boolean xpathConditionCheck(String metaData, String xpathCondition){
		boolean check = false;
		
		try{
			XPath xPath = xmlProcessor.getXPathInstance();
			
			logger.debug("xpath-condition=" + xpathCondition);
			logger.debug("meta: " + metaData);
			Document metaDocument = xmlProcessor
					.loadXmlStringToDOM(metaData);

			check = (Boolean) xPath.evaluate(xpathCondition,
					metaDocument, XPathConstants.BOOLEAN);
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return check;
	}

	private String getCommitteeFormActionSectionByXpathCondition(
			Document workflowXmlDocument, Form form, Committee committee,
			String formType, String formStatus, String formCommitteeStatus, String action)
			throws XPathExpressionException, IOException, SAXException {

		XPath xPath = xmlProcessor.getXPathInstance();

		String committeeFormActionSectionXPath = "/business-object-status/business-object[@type='"
				+ objectType
				+ "']/form[@type='"
				+ formType
				+ "' or @type='ANY']/form-status"
				+ "[@value='"
				+ formStatus
				+ "' or @value='ANY'][@form-committee-status='"
				+ formCommitteeStatus
				+ "' or @form-committee-status='ANY']/committee[@name='"
				+ committee.toString() + "']/.";
		
		if (action.equals("CREATE")){
			committeeFormActionSectionXPath = "/business-object-status/business-object[@type='"
					+ objectType
					+ "']/form[@type='"
					+ formType
					+ "' or @type='ANY']/form-status"
					+ "[@value=''][@form-committee-status='ANY']/committee[@name='"
					+ committee.toString() + "']/.";
		}

		logger.debug("committeeFormActionSectionXPath: "
				+ committeeFormActionSectionXPath);
		NodeList committeeFormActionSections = (NodeList) xPath.evaluate(
				committeeFormActionSectionXPath, workflowXmlDocument,
				XPathConstants.NODESET);

		Element defaultCommitteeFormActionEl = null;
		// you can have multiple actionSection with xpath-condition, but only
		// one default
		// it will return the first one which xpath-condition meets
		for (int i = 0; i < committeeFormActionSections.getLength(); i++) {

			Element committeeFormActionEl = (Element) committeeFormActionSections
					.item(i);
			
			Document committeeFormActionElDoc = xmlProcessor.loadXmlStringToDOM(DomUtils.elementToString(committeeFormActionEl));

			if (!action.isEmpty()){
				String actionPath = "boolean(count(/committee/review-page/actions/action[@type[.= \""+ action +"\"]])>0)";

				Boolean hasAction = false;
				logger.debug("path: " + actionPath);
				try{
					hasAction = (Boolean) (xPath
							.evaluate(
									actionPath,
									committeeFormActionElDoc, XPathConstants.BOOLEAN));
				} catch (Exception e){
					e.printStackTrace();
				}

				if (!hasAction){
					continue;
				}
			} else {
				//if review page is null, continue...
				String reviewPagePath = "boolean(count(/committee/review-page[@page-name[.=\"null\"]])>0)";

				Boolean noReviewPage = false;
				logger.debug("path: " + reviewPagePath);
				try{
					noReviewPage = (Boolean) (xPath
							.evaluate(
									reviewPagePath,
									committeeFormActionElDoc, XPathConstants.BOOLEAN));
				} catch (Exception e){
					e.printStackTrace();
				}

				if (noReviewPage){
					continue;
				}
			}
		
			String xpathCondition = committeeFormActionEl
					.getAttribute("xpath-condition");
			
			//String seperateOut = committeeFormActionEl.getAttribute("seperate-out");
			
			logger.debug("committeeFormActionEl: " + DomUtils.elementToString(committeeFormActionEl));
			if (xpathCondition != null && !xpathCondition.isEmpty()) {

				if (xpathConditionCheck(form.getObjectMetaData(),xpathCondition)) {
					return DomUtils.elementToString(committeeFormActionEl);
				}
			} else{
				Element formElm = (Element) committeeFormActionEl.getParentNode();
				if (formElm.getAttribute("value").equals("ANY")
						&& formElm.getAttribute("form-committee-status")
								.equals("ANY") && committeeFormActionEl.getAttribute("name").equals("ANY"))
					continue;
				
				defaultCommitteeFormActionEl = committeeFormActionEl;
			}
		}
		
		//still no action.. let them update notes
		if (defaultCommitteeFormActionEl == null){
			String defaultCommitteeFormActionXPath = "/business-object-status/business-object[@type='"
					+ objectType
					+ "']/form[@type='"
				+ formType
				+ "' or @type='ANY']/form-status"
					+ "[@value='ANY'][@form-committee-status='ANY']/committee[@name='ANY'][@default='true']/.";

			logger.debug("defaultCommitteeFormActionXPath: "
					+ defaultCommitteeFormActionXPath);
			
			defaultCommitteeFormActionEl = (Element) xPath.evaluate(
					defaultCommitteeFormActionXPath, workflowXmlDocument,
					XPathConstants.NODE);
		}

		return DomUtils.elementToString(defaultCommitteeFormActionEl);
	}

	/*record mandatory committe and return optional ones */
	private List<Committee> generateWorkflowPath(Document extraDataXmlDocument, Committee firstCommittee) {
		XPath xPath = getXmlProcessor().getXPathInstance();	

		try {
			Document committeeListXmlDocument = xmlProcessor
					.loadXmlFileToDOM(getCommitteeListXmlFilePath());
			
			logger.debug("committeeList: " + DomUtils.elementToString(committeeListXmlDocument));
			Element workflowControlEl = (Element) (xPath.evaluate(
					"/committee-review/workflow-control", extraDataXmlDocument,
					XPathConstants.NODE));

			if (workflowControlEl == null) {
				workflowControlEl = extraDataXmlDocument
						.createElement("workflow-control");
				extraDataXmlDocument.getFirstChild().appendChild(
						workflowControlEl);
			}

			Element recordedWorkflowPathEl = (Element) (xPath.evaluate(
					"//recorded-workflow-path", workflowControlEl,
					XPathConstants.NODE));

			if (recordedWorkflowPathEl != null) {
				workflowControlEl.removeChild(recordedWorkflowPathEl);
			}

			recordedWorkflowPathEl = extraDataXmlDocument
					.createElement("recorded-workflow-path");
			workflowControlEl.appendChild(recordedWorkflowPathEl);

			NodeList invovledCommittees = (NodeList) xPath.evaluate(
					"//invovled-committees/committee", extraDataXmlDocument,
					XPathConstants.NODESET);
			
			SortedMap<Integer, Committee> sortedRequiredCommitees = new TreeMap<Integer, Committee>();
			
			List<Committee> optionalCommittees = new ArrayList<Committee>();
			
			//have to... otherwise limbo...
			sortedRequiredCommitees.put(0, firstCommittee);
			if (invovledCommittees.getLength() > 0) {
				for (int i = 0; i < invovledCommittees.getLength(); i++) {
					Element currentCommitteeEl = (Element) invovledCommittees
							.item(i);
					
					Committee currentCommittee = Committee
							.valueOf(currentCommitteeEl.getTextContent());
					
					Element committeeEl = (Element)xPath.evaluate("/committees/committee[@name='" + currentCommittee + "']", committeeListXmlDocument, XPathConstants.NODE);
					
					if(committeeEl == null) continue;
					
					if(Boolean.parseBoolean(committeeEl.getAttribute("required"))){
						int order = Integer.parseInt(committeeEl.getAttribute("order"));
						
						sortedRequiredCommitees.put(order, currentCommittee);
					}else{
						optionalCommittees.add(currentCommittee);
					}					
				}
			}
			
			List<Integer> keyList = new ArrayList<Integer>(sortedRequiredCommitees.keySet());
			
			for(int i = 0; i < keyList.size() - 1; i ++) {				
				
				logger.debug("i:" + i);
				Committee currentCommittee = sortedRequiredCommitees.get(keyList.get(i));
				Committee nextCommittee = sortedRequiredCommitees.get(keyList.get(i+1));
				Element stepEl = extraDataXmlDocument.createElement("step");
				stepEl.setAttribute("current-committee",
						currentCommittee.toString());
				
				stepEl.setAttribute("next-committee", nextCommittee.toString());
				
				stepEl.setAttribute("next-object-status",getCommitteeReviewObjectStatus(nextCommittee));
				stepEl.setAttribute("next-form-status",getCommitteeReviewFormStatus(nextCommittee));
				stepEl.setAttribute("next-form-committee-status",getCommitteeReviewFormCommitteeStatus(nextCommittee));
				recordedWorkflowPathEl.appendChild(stepEl);
			}
			
			return optionalCommittees;
					

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	protected abstract String getCommitteeReviewFormStatus(
			Committee nextCommittee);
	
	protected abstract String getCommitteeReviewObjectStatus(
			Committee nextCommittee);
	
	protected abstract String getCommitteeReviewFormCommitteeStatus(
			Committee nextCommittee);

	/***
	 * 
	 * @param form
	 * @param committee
	 * @param user
	 * @param action
	 * @param condition
	 * @param note
	 * @param extraDataXml
	 * @throws IOException
	 * @throws SAXException
	 * @throws XPathExpressionException
	 */
	private void __triggerAction(Form form, Committee committee, User user,
			String action, String condition, String workflow,
			String committeeNote, String extraDataXml) {
		try {

			Date now = new Date();
			String formType = form.getFormType();

			String formStatus = getFormStatus(form);

			String formCommitteeStatus = getFormCommitteeStatus(form, committee);

			logger.debug("formStatus:" + formStatus);

			Document workflowXmlDocument = xmlProcessor
					.loadXmlFileToDOM(getWorkflowXmlFilePath());

			logger.debug("get review page formStatus:" + formStatus);

			String committeeFormActionSectionXml = getCommitteeFormActionSectionByXpathCondition(
					workflowXmlDocument, form, committee, formType, formStatus,
					formCommitteeStatus, action);

			logger.debug("selected committeeFormActionSectionXml: "
					+ committeeFormActionSectionXml);
			Document committeeFormActionSectionXmlDocument = xmlProcessor
					.loadXmlStringToDOM(committeeFormActionSectionXml);

			/*
			 * String actionXpath =
			 * "/business-object-status/business-object[@type='" + objectType +
			 * "']/form[@type='" + formType + "' or @type='ANY']/form-status" +
			 * "[@value='" + formStatus +
			 * "' or @value='ANY'][@form-committee-status='"+
			 * formCommitteeStatus
			 * +"' or @form-committee-status='ANY']/committee[@name='" +
			 * committee.toString() + "']/review-page/actions/";
			 */

			String actionXpath = "//review-page/actions/";
			if ((condition == null || condition.isEmpty())
					&& (workflow == null || workflow.isEmpty())) {
				actionXpath += "action[@type='" + action + "']";
			} else if (condition == null || condition.isEmpty()) {
				actionXpath += "action[@type='" + action + "' and @workflow='"
						+ workflow + "']";
			} else if (workflow == null || workflow.isEmpty()) {
				actionXpath += "action[@type='" + action + "' and (@condition='"
						+ condition + "' or @condition='ANY')]";
			} else {
				actionXpath += "action[@type='" + action + "' and (@condition='"
						+ condition + "' or @condition='ANY')][@workflow='"
						+ workflow + "']";
			}

			logger.debug("actionXpath: " + actionXpath);

			XPath xPath = xmlProcessor.getXPathInstance();

			logger.debug("what's come in: extraDataXml: " + extraDataXml);

			Document extraDataXmlDocument = null;

			try {
				extraDataXmlDocument = xmlProcessor
						.loadXmlStringToDOM(extraDataXml);
			} catch (Exception ex) {
				logger.warn("extraDataXml doesn't exist!");
			}

			Node actionXmlNode = (Node) xPath.evaluate(actionXpath,
					committeeFormActionSectionXmlDocument, XPathConstants.NODE);

			Map<String, Object> attributeRawValues = new HashMap<String, Object>();
			attributeRawValues.put("COMMITTEE", committee);
			attributeRawValues.put("COMMITTEE_NOTE", committeeNote);
			attributeRawValues.put("OLD_FORM_STATUS", formStatus); // original
																	// status

			Element cancelReasonEl = (Element) xPath.evaluate(
					"//cancel-reason", extraDataXmlDocument,
					XPathConstants.NODE);

			if (cancelReasonEl != null) {
				attributeRawValues.put("REASON",
						(cancelReasonEl.getAttribute("type").equals("CONTRACT"))?cancelReasonEl.getAttribute("text"):cancelReasonEl.getTextContent());
				attributeRawValues.put("SUB_REASON",
						(cancelReasonEl.getAttribute("type").equals("CONTRACT"))?cancelReasonEl.getAttribute("subtext"):cancelReasonEl.getTextContent());
			}
			
			//request code
			Element requestCodeEl = (Element) xPath.evaluate(
					"//request-code", extraDataXmlDocument,
					XPathConstants.NODE);
			
			if (requestCodeEl != null) {
				attributeRawValues.put("CPT",
						requestCodeEl.getAttribute("cpt"));
				//attributeRawValues.put("CDM",
						//requestCodeEl.getAttribute("cdm"));
				attributeRawValues.put("DESCRIPTION",
						requestCodeEl.getAttribute("description"));
				//attributeRawValues.put("COST",
						//requestCodeEl.getAttribute("cost"));
				//attributeRawValues.put("OFFER",
						//requestCodeEl.getAttribute("offer"));
				//attributeRawValues.put("PRICE",
						//requestCodeEl.getAttribute("price"));
				attributeRawValues.put("NOTE",
						requestCodeEl.getAttribute("note"));
			}

			// check if revision requested committee exist in the meta data xml
			Document formMetaDataXmlDoc = xmlProcessor.loadXmlStringToDOM(form
					.getMetaXml());

			// boolean storeMetaData = false;
			if (actionXmlNode != null) {
				Element actionXmlEl = (Element) actionXmlNode;
				logger.debug("found action: "
						+ actionXmlEl.getAttribute("type") + "; condition: "
						+ actionXmlEl.getAttribute("condition")
						+ "; store-xml-data-in-form: "
						+ actionXmlEl.getAttribute("store-xml-data-in-form")
						+ "; workflow: " + workflow);

				logger.debug("actionXml: "
						+ DomUtils.elementToString(actionXmlNode));
				
				//re-submission of revision should not go to pharmacy if pharmacy request revision during the time another committee request revision
				Boolean needToUpdateRevisionRequestedInfo = (actionXmlEl
						.getAttribute("update-revision-requested-info").equals("false"))?false:true;

				if ((action.equals("REVISION_REQUESTED") || action.equals("RETURN_FOR_BUDGET_NEGOTIATIONS")) && needToUpdateRevisionRequestedInfo) {
					/*
					if (committee.equals(Committee.IRB_EXPEDITED_REVIEWER)) {
						setMinorContingencyFlag(form);
					}
					*/

					Element formStatusTrackEl = (Element) (xPath
							.evaluate(
									"/committee-review/revisition-requested-status-track",
									extraDataXmlDocument, XPathConstants.NODE));

					if (formStatusTrackEl == null) {
						formStatusTrackEl = extraDataXmlDocument
								.createElement("revisition-requested-status-track");
					}

					formStatusTrackEl
							.setAttribute(
									"original-object-status",
									(actionXmlEl
											.getAttribute("original-object-status") != null && !actionXmlEl
											.getAttribute(
													"original-object-status")
											.isEmpty()) ? actionXmlEl
											.getAttribute("original-object-status")
											: getObjectStatus(form));

					formStatusTrackEl
							.setAttribute(
									"original-form-status",
									(actionXmlEl
											.getAttribute("original-form-status") != null && !actionXmlEl
											.getAttribute(
													"original-form-status")
											.isEmpty()) ? actionXmlEl
											.getAttribute("original-form-status")
											: getFormStatus(form));
					formStatusTrackEl
							.setAttribute(
									"original-form-committee-status",
									(actionXmlEl
											.getAttribute("original-form-committee-status") != null && !actionXmlEl
											.getAttribute(
													"original-form-committee-status")
											.isEmpty()) ? actionXmlEl
											.getAttribute("original-form-committee-status")
											: getFormCommitteeStatus(form,
													committee));

					formStatusTrackEl
							.setAttribute(
									"requested-committee",
									(actionXmlEl
											.getAttribute("requested-committee") != null && !actionXmlEl
											.getAttribute("requested-committee")
											.isEmpty()) ? actionXmlEl
											.getAttribute("requested-committee")
											: committee.toString());

					extraDataXmlDocument.getDocumentElement().appendChild(
							formStatusTrackEl);
				}
				
				List<Committee> optionalCommittees = null;
				
				boolean startOptionalWorkflowPath = false;
				
				boolean workflowControlMode = false;

				if (Boolean.parseBoolean(actionXmlEl
						.getAttribute("record-workflow-path"))) {
					logger.debug("record-workflow-path");
					optionalCommittees = generateWorkflowPath(extraDataXmlDocument, committee);
					
					startOptionalWorkflowPath = true;
					workflowControlMode = true;
					//logger.debug("extraDataXml: " + extraDataXml);
				}
				
				if(Boolean.parseBoolean(actionXmlEl.getAttribute("controlled-workflow-path"))){
					workflowControlMode = true;
				}
				
				extraDataXml = DomUtils.elementToString(extraDataXmlDocument);
				
				boolean updateMetaData = Boolean.valueOf(actionXmlEl
						.getAttribute("update-meta-data"));
				
				Element changeStatus = (Element) xPath.evaluate(
						"change-status", actionXmlNode, XPathConstants.NODE);

				if (changeStatus != null) {

					logger.debug("need to change object statuses...");
					Element formStatusTrackEl = null;
					Element nextCommitteeTrackEl = null;
					
					if(workflowControlMode){
						Document workflowControlDocument = startOptionalWorkflowPath?extraDataXmlDocument:formMetaDataXmlDoc;						
						logger.debug("workflowControlDocument: " + DomUtils.elementToString(workflowControlDocument));
						nextCommitteeTrackEl = (Element) xPath
								.evaluate(
										"//workflow-control/recorded-workflow-path/step[@current-committee='" + committee + "']",
										workflowControlDocument,
										XPathConstants.NODE);
						
						logger.debug("nextCommitteeTrackEl: " + DomUtils.elementToString(nextCommitteeTrackEl));
						
						//if(true) return;
					}
					
					NodeList ss = changeStatus.getElementsByTagName("status");
					if (ss.getLength() > 0) {
						Element objectStatusEl = (Element) ss.item(0);
						logger.debug("change object status to: "
								+ objectStatusEl.getAttribute("status"));

						String objectStatus = objectStatusEl
								.getAttribute("status");

						if (objectStatus
								.equals("BEFORE_REVISION_REQUESTED_STATUS")) {
							xPath.reset();
							formStatusTrackEl = (Element) xPath
									.evaluate(
											"//revisition-requested-status-track",
											formMetaDataXmlDoc,
											XPathConstants.NODE);

							if (formStatusTrackEl != null
									&& formStatusTrackEl
											.hasAttribute("original-object-status")) {
								objectStatus = formStatusTrackEl
										.getAttribute("original-object-status");
							}
						}else if ("NEXT_COMMITTEE_OBJECT_STATUS".equals(objectStatus)){
							logger.debug("here");						
							
							if(workflowControlMode && nextCommitteeTrackEl != null && nextCommitteeTrackEl.hasAttribute("next-object-status")){
								objectStatus = nextCommitteeTrackEl
										.getAttribute("next-object-status");
							}
							
							logger.debug("objectStatus:" + objectStatus);
							
						}

						changeObjectStatus(form, now, committee, user,
								objectStatus);

						attributeRawValues.put("OBJECT_STATUS", objectStatus);
					}

					NodeList fs = changeStatus
							.getElementsByTagName("form-status");
					if (fs.getLength() > 0) {
						Element formStatusEl = (Element) fs.item(0);
						logger.debug("change object form-status to: "
								+ formStatusEl.getAttribute("status"));

						String objectFormStatus = formStatusEl
								.getAttribute("status");

						if (objectFormStatus
								.equals("BEFORE_REVISION_REQUESTED_STATUS")) {
							xPath.reset();
							
							if(formStatusTrackEl == null) {
								formStatusTrackEl = (Element) xPath
									.evaluate(
											"//revisition-requested-status-track",
											formMetaDataXmlDoc,
											XPathConstants.NODE);
							}

							if (formStatusTrackEl != null
									&& formStatusTrackEl
											.hasAttribute("original-form-status")) {
								objectFormStatus = formStatusTrackEl
										.getAttribute("original-form-status");
							}
						}else if("NEXT_COMMITTEE_FORM_STATUS".equals(objectFormStatus)){
							
							if(workflowControlMode && nextCommitteeTrackEl != null && nextCommitteeTrackEl.hasAttribute("next-form-status")){
								objectFormStatus = nextCommitteeTrackEl
										.getAttribute("next-form-status");
							}
						}

						changeObjectFormStatus(form, now, committee, user,
								objectFormStatus);

						attributeRawValues.put("FORM_STATUS", objectFormStatus);
					}

					NodeList formCommitteeStatuses = (NodeList) xPath
							.evaluate(
									"change-status/form-committee-statuses/form-committee-status",
									actionXmlNode, XPathConstants.NODESET);

					for (int i = 0; i < formCommitteeStatuses.getLength(); i++) {
						if (formCommitteeStatuses.item(i).getNodeType() == Node.ELEMENT_NODE) {
							Element formCommitteeStatusEl = (Element) formCommitteeStatuses
									.item(i);

							logger.debug("change object form-committee-status for committee: "
									+ formCommitteeStatusEl
											.getAttribute("involved-committee")
									+ " to status: "
									+ formCommitteeStatusEl
											.getAttribute("status")
									+ "; store-xml-data: "
									+ formCommitteeStatusEl
											.getAttribute("store-xml-data"));

							boolean storeXmlData = Boolean
									.valueOf(formCommitteeStatusEl
											.getAttribute("store-xml-data"));
							// changeObjectFormCommitteeStatus(form, now,
							// committee,
							// user, objectStatus.getAttribute("status"));

							// for SELECTED_COMMITTEESS

							if (formCommitteeStatusEl.getAttribute(
									"involved-committee").equals(
									"SELECTED_COMMITTEES")) {

								NodeList invovledCommittees = (NodeList) xPath
										.evaluate(
												"//invovled-committees/committee",
												extraDataXmlDocument,
												XPathConstants.NODESET);
								List<Committee> selectedCommittees = new ArrayList<Committee>();							

								for (int j = 0; j < invovledCommittees
										.getLength(); j++) {
									Element invovledCommitteeEl = (Element) invovledCommittees
											.item(j);
									logger.debug("invovled-committee: "
											+ invovledCommitteeEl
													.getTextContent()
											+ "; type: "
											+ invovledCommitteeEl
													.getAttribute("type"));

									Committee involvedCommittee = Committee
											.valueOf(invovledCommitteeEl
													.getTextContent());

									if (formCommitteeStatusEl.hasChildNodes()) {
										NodeList selectedCommitteeNsl = formCommitteeStatusEl
												.getElementsByTagName("selected-committee");

										for (int k = 0; k < selectedCommitteeNsl
												.getLength(); k++) {
											Element selectedCommitteeEl = (Element) selectedCommitteeNsl
													.item(k);

											Committee selectedCommittee = Committee
													.valueOf(selectedCommitteeEl
															.getAttribute("belong-to-committee"));

											Committee realCommittee = Committee
													.valueOf(selectedCommitteeEl
															.getAttribute("committee"));

											if (involvedCommittee == selectedCommittee) {
												changeObjectFormCommitteeStatus(
														form,
														now,
														committee,
														user,
														realCommittee,
														selectedCommitteeEl
																.getAttribute("status"),
														null,
														storeXmlData ? extraDataXml
																: null, action);
												
												selectedCommittees.add(realCommittee);
											} else {
												changeObjectFormCommitteeStatus(
														form,
														now,
														committee,
														user,
														involvedCommittee,
														formCommitteeStatusEl
																.getAttribute("status"),
														null,
														storeXmlData ? extraDataXml
																: null, action);
												
												selectedCommittees.add(involvedCommittee);
											}
										}
									} else {
										changeObjectFormCommitteeStatus(
												form,
												now,
												committee,
												user,
												involvedCommittee,
												formCommitteeStatusEl
														.getAttribute("status"),
												null,
												storeXmlData ? extraDataXml
														: null, action);
										
										selectedCommittees.add(involvedCommittee);
									}

									//selectedCommittees.add(involvedCommittee);
								}
								// if selectedCommittees is empty, no need to
								// put it into the Map
								if (!selectedCommittees.isEmpty()) {
									attributeRawValues.put(
											"SELECTED_COMMITTEES",
											selectedCommittees);
									
									updateAssignedCommittees(form, selectedCommittees);
								}
							} else if (formCommitteeStatusEl.getAttribute(
									"involved-committee").equals(
									"OPTIONAL_COMMITTEES")) {
								
								if (!optionalCommittees.isEmpty()) {
									
									for(Committee optionalCommittee:optionalCommittees){
										changeObjectFormCommitteeStatus(
												form,
												now,
												committee,
												user,
												optionalCommittee,
												formCommitteeStatusEl
														.getAttribute("status"),
												null,
												storeXmlData ? extraDataXml
														: null, action);
									}									
									
									attributeRawValues.put(
											"SELECTED_COMMITTEES",
											optionalCommittees);
								}
							}
								else if (formCommitteeStatusEl.getAttribute(
							
									"involved-committee").equals(
									"REVISION_REQUEST_COMMITTEE")) {
								if( formStatusTrackEl == null){
									formStatusTrackEl = (Element) xPath
										.evaluate(
												"//revisition-requested-status-track",
												formMetaDataXmlDoc,
												XPathConstants.NODE);
								}

								if (formStatusTrackEl != null
										&& formStatusTrackEl
												.hasAttribute("requested-committee")) {

									String revisionRequestedCommittee = formStatusTrackEl
											.getAttribute("requested-committee");

									attributeRawValues
											.put("REVISION_REQUEST_COMMITTEE",
													formStatusTrackEl
															.getAttribute("requested-committee"));

									String status = formCommitteeStatusEl
											.getAttribute("status");

									if ("ORIGINAL_FORM_COMMITTEE_STATUS"
											.equals(status)) {
										status = formStatusTrackEl
												.getAttribute("original-form-committee-status");
									}

									logger.debug("Revision Request Committee: "
											+ revisionRequestedCommittee
											+ "; status: " + status);

									changeObjectFormCommitteeStatus(
											form,
											now,
											committee,
											user,
											Committee
													.valueOf(revisionRequestedCommittee),
											status, committeeNote,
											storeXmlData ? extraDataXml : null,
											action);
									
									if (formCommitteeStatusEl.hasChildNodes()) {
										NodeList revisionRequestCommitteeNsl = formCommitteeStatusEl
												.getElementsByTagName("revision-request-committee");
										
										for (int j = 0; j < revisionRequestCommitteeNsl.getLength(); j++) {
											Element revisionRequestCommitteeEl = (Element) revisionRequestCommitteeNsl.item(j);
											
											String realRevisionRequestCommittee = revisionRequestCommitteeEl
													.getAttribute("committee");
											
											if (realRevisionRequestCommittee.equals(revisionRequestedCommittee)) {
												changeObjectFormCommitteeStatus(
														form,
														now,
														committee,
														user,
														Committee
																.valueOf(revisionRequestCommitteeEl
																		.getAttribute("involved-committee")),
														revisionRequestCommitteeEl.getAttribute("status"), committeeNote,
														storeXmlData ? extraDataXml : null,
														action);
											}
										}
									}

								}

							} else if (formCommitteeStatusEl.getAttribute(
									"involved-committee").equals(
									"NEXT_COMMITTEE")){						
								
								
							
								if(workflowControlMode && nextCommitteeTrackEl != null && nextCommitteeTrackEl.hasAttribute("next-committee")){
									//String status = formCommitteeStatusEl
											//.getAttribute("status"); 
									
									String status = nextCommitteeTrackEl.getAttribute("next-form-committee-status");
									
									logger.debug("next-committee: " + nextCommitteeTrackEl.getAttribute("next-committee"));
									
									changeObjectFormCommitteeStatus(
											form,
											now,
											committee,
											user,
											Committee
													.valueOf(nextCommitteeTrackEl.getAttribute("next-committee")),
											status, committeeNote,
											storeXmlData ? extraDataXml : null,
											action);
									
									attributeRawValues
									.put("NEXT_COMMITTEE",
											nextCommitteeTrackEl
													.getAttribute("next-committee"));
									
									if (nextCommitteeTrackEl.getAttribute("next-committee").equals("IRB_ASSIGNER")) {
										actionXmlNode = processActionXmlNode(form, actionXmlNode);
									}
								}
								
							}else {
								changeObjectFormCommitteeStatus(
										form,
										now,
										committee,
										user,
										Committee.valueOf(formCommitteeStatusEl
												.getAttribute("involved-committee")),
										formCommitteeStatusEl
												.getAttribute("status"),
										committeeNote,
										storeXmlData ? extraDataXml : null,
										action);

							}
						}
					}
					
					NodeList documentStatusNst = changeStatus
							.getElementsByTagName("document-status");
					
					if (documentStatusNst != null && documentStatusNst.getLength() > 0){
						for (int p=0; p < documentStatusNst.getLength(); p++){
							Element documentStatusEl = (Element) documentStatusNst.item(p);
							
							changeObjectFormDocumentStatus(form, now, committee, user, documentStatusEl.getAttribute("status"), documentStatusEl);
						}
					}

				}

				logger.debug("updating meta data xml ...");
				updateMetaDataXml(form, extraDataXml, updateMetaData);
				
				/*
				//get approval status and date after meta data is 
				String clockStart = actionXmlEl.getAttribute("clock-start");
				boolean updateApprovalDateOrStatus = Boolean.valueOf(actionXmlEl
						.getAttribute("update-approval-date-or-status"));
				
				if (updateApprovalDateOrStatus) {
					if (clockStart == null || clockStart.isEmpty()) {
						clockStart = "";
					}
						
					updateApprovalStatusAndDate(form, action, clockStart, condition);
				}
				*/
				
				logger.debug("trigger events ...");
				Element eventsEl = (Element) xPath.evaluate(
						"events", actionXmlNode, XPathConstants.NODE);
				
				if (eventsEl != null) {
					try {
						this.triggerEvents(form, user, committee, DomUtils.elementToString(eventsEl), action, condition,attributeRawValues);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				logger.debug("notifications...");
				Element notificationsEl = (Element) xPath.evaluate(
						"notifications", actionXmlNode, XPathConstants.NODE);
				
				if (notificationsEl != null) {

					logger.debug("need to send notifications...");
					try {
						sendNotifications(form, user, attributeRawValues,
								DomUtils.elementToString(notificationsEl),
								extraDataXml);
						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				logger.debug("logging...");
				NodeList logsNL = null;
				
				if (notificationsEl != null){
					Document notificationsTemplateDoc = xmlProcessor
							.loadXmlStringToDOM(DomUtils.elementToString(notificationsEl));
					
					NodeList notifications = notificationsTemplateDoc.getDocumentElement()
							.getElementsByTagName("notification");
					
					String notificationLogs = "<notifications>";
					
					for (int p=0; p < notifications.getLength(); p++){
						Element nfEl = (Element) notifications.item(p);
						
						if (nfEl.getAttribute("xpath-condition") != null && !nfEl.getAttribute("xpath-condition").isEmpty()){
							if (!this.xpathConditionCheck(form.getObjectMetaData(), nfEl.getAttribute("xpath-condition"))){
								continue;
							}
							
							notificationLogs += DomUtils.elementToString(nfEl);
						} else if (nfEl.getAttribute("form-xpath-condition") != null && !nfEl.getAttribute("form-xpath-condition").isEmpty()) {
							if (!this.xpathConditionCheck(form.getMetaXml(), nfEl.getAttribute("form-xpath-condition"))){
								continue;
							}
							
							notificationLogs += DomUtils.elementToString(nfEl);
						} else {
							notificationLogs += DomUtils.elementToString(nfEl);
						}

					}
					
					notificationLogs += "</notifications>";

					Document notificationLogDoc = xmlProcessor.loadXmlStringToDOM(notificationLogs);
					
					logsNL = (NodeList) xPath.evaluate(
							"//notification/logs/log",
							notificationLogDoc, XPathConstants.NODESET);
				}
				
				if (logsNL == null){
					logsNL = (NodeList) xPath.evaluate(
							"logs/log",
							actionXmlNode, XPathConstants.NODESET);
				}
				
				//String logXpath = "logs/log | notifications/notificaiton/logs/log";

				//NodeList logsNL = (NodeList) xPath.evaluate(
						//logXpath,
						//actionXmlNode, XPathConstants.NODESET);

				if (logsNL.getLength() > 0) {

					logger.debug("logs: " + logsNL.getLength());
					Document logsDoc = xmlProcessor.newDocument();

					Element logsEl = logsDoc.createElement("logs");

					for (int k = 0; k < logsNL.getLength(); k++) {
						logger.debug("log: "
								+ DomUtils.elementToString(logsNL.item(k)));
						logsEl.appendChild(logsDoc.importNode(logsNL.item(k),
								true));
					}

					logger.debug("need to insert object logs...");
					logger.debug("logsXml: " + DomUtils.elementToString(logsEl));
					
					logStatusChange(form, user, attributeRawValues,
							DomUtils.elementToString(logsEl));

				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
			
			if(ex instanceof ClaraRunTimeException && ClaraRunTimeException.ErrorType.NO_AGENDA_ASSIGNED.equals(((ClaraRunTimeException)ex).getErrorType())){
				throw new ClaraRunTimeException(ClaraRunTimeException.ErrorType.NO_AGENDA_ASSIGNED.getMessage(), ErrorType.NO_AGENDA_ASSIGNED);
			} 
		}

	}

	public abstract void updateMetaDataXml(Form form, String extraDataXml, boolean updateMetaData);

	public abstract void sendNotifications(Form form, User user,
			Map<String, Object> attributeRawValues,
			String notificationsTemplate, String extraDataXml)
			throws IOException, SAXException;

	public abstract void logStatusChange(Form form, User user,
			Map<String, Object> attributeRawValues, String logsTemplate)
			throws IOException, SAXException;

	public abstract String getObjectStatus(Form form);

	public abstract String getFormStatus(Form form);

	public abstract String getFormCommitteeStatus(Form form, Committee committee);

	public abstract void changeObjectStatus(Form form, Date now,
			Committee committee, User user, String status);

	public abstract void changeObjectFormStatus(Form form, Date now,
			Committee committee, User user, String status);

	public abstract void changeObjectFormCommitteeStatus(Form form, Date now,
			Committee committee, User user, Committee involvedCommittee,
			String status, String committeeNote, String xmlData, String action);
	
	public abstract void changeObjectFormDocumentStatus(Form form, Date now,
			Committee committee, User user, String status, Element documentStatusEl);

	public abstract void triggerEvents(Form form, User user, Committee committee, String eventsTemplate, String action, String condition, Map<String, Object> attributeRawValues) throws IOException, SAXException;
	
	public abstract void updateAssignedCommittees(Form form, List<Committee> selectedCommittees);
	
	public abstract Node processActionXmlNode(Form form, Node actionXmlNode);

	public String getWorkflowXmlFilePath() {
		return workflowXmlFilePath;
	}

	public void setWorkflowXmlFilePath(String workflowXmlFilePath) {
		this.workflowXmlFilePath = workflowXmlFilePath;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public String getCommitteeListXmlFilePath() {
		return committeeListXmlFilePath;
	}

	public void setCommitteeListXmlFilePath(String committeeListXmlFilePath) {
		this.committeeListXmlFilePath = committeeListXmlFilePath;
	}

	public String getApplicationHost() {
		return applicationHost;
	}

	public void setApplicationHost(String applicationHost) {
		this.applicationHost = applicationHost;
	}
}
