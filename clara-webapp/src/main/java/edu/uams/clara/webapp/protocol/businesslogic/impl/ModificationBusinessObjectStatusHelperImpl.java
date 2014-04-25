package edu.uams.clara.webapp.protocol.businesslogic.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.protocol.businesslogic.ProtocolBusinessObjectStatusHelper;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicServiceContainer;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ModificationBusinessObjectStatusHelperImpl extends
		ProtocolBusinessObjectStatusHelper {
	private final static Logger logger = LoggerFactory
			.getLogger(ModificationBusinessObjectStatusHelperImpl.class);
	
	private ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer;
	
	private ProtocolFormService protocolFormService;
	
	private FormService formService;
	
	public ModificationBusinessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
	}
	
	@Override
	public String preProcessCommitteeReviewXml(Form form, Committee committee,
			User user, String action, String extraDataXml) {
		
		extraDataXml = super.preProcessCommitteeReviewXml(form, committee, user,
			action, extraDataXml);
		
		ProtocolForm protocolForm = (ProtocolForm) form;
		
		String formStatus = getFormStatus(form);
		
		if ("".equals(formStatus)) return extraDataXml;
		
		ProtocolFormStatusEnum protocolFormStatus = ProtocolFormStatusEnum
				.valueOf(formStatus);
		
		XmlProcessor xmlProcessor = getXmlProcessor();
		XPath xPath = xmlProcessor.getXPathInstance();
		
		try {
			Document extraDataXmlDoc = xmlProcessor.loadXmlStringToDOM(extraDataXml);
			
			/**
			 * Need to check whether budget is involved, if so, remove BUDGET_REVIEW -> COVERAGE_REVIEW -> etc., from involved-committees list, add has_budget flag in the xml, so checkCondition function will 
			 * know what to do...
			 * changed the client, so it will not have other committees sent to the controller... does the controller need to double-check?
			 */
			
			if(committee.equals(Committee.GATEKEEPER) && ProtocolFormStatusEnum.UNDER_PREREVIEW.equals(protocolFormStatus) && "ASSIGN_TO_COMMITTEES".equals(action)){
				NodeList invovledCommittees = (NodeList) xPath
						.evaluate(
								"//invovled-committees/committee",
								extraDataXmlDoc,
								XPathConstants.NODESET);
				
				List<Committee> selectedCommittees = new ArrayList<Committee>();

				for (int j = 0; j < invovledCommittees.getLength(); j++) {
					Element invovledCommitteeEl = (Element) invovledCommittees
							.item(j);
					logger.debug("preProcessCommitteeReviewXml->invovled-committee: "
							+ invovledCommitteeEl.getTextContent()
							+ "; type: "
							+ invovledCommitteeEl
									.getAttribute("type"));

					Committee involvedCommittee = Committee
							.valueOf(invovledCommitteeEl
									.getTextContent());

					selectedCommittees.add(involvedCommittee);
				}
				
				if (selectedCommittees.contains(Committee.PHARMACY_REVIEW)){
					String protocolFormMeta = protocolForm.getMetaDataXml();
					
					protocolFormMeta = getXmlProcessor().replaceOrAddNodeValueByPath("/protocol/pharmacy-review-requested", protocolFormMeta, "y");
					
					protocolForm.setMetaDataXml(protocolFormMeta);
					
					protocolForm = getProtocolFormDao().saveOrUpdate(protocolForm);
					
					Protocol protocol = protocolForm.getProtocol();
					
					String protocolMeta = protocol.getMetaDataXml();
					
					if (selectedCommittees.contains(Committee.BUDGET_REVIEW)) {
						protocolMeta = getXmlProcessor().replaceOrAddNodeValueByPath("/protocol/next-committee-after-pharmacy-review", protocolMeta, Committee.BUDGET_MANAGER.toString());
					} else {
						protocolMeta = getXmlProcessor().replaceOrAddNodeValueByPath("/protocol/next-committee-after-pharmacy-review", protocolMeta, Committee.IRB_ASSIGNER.toString());
					}
					
					protocol.setMetaDataXml(protocolMeta);
					
					protocol = getProtocolDao().saveOrUpdate(protocol);
					
				}
				
				/**
				 * this thing has a budget...
				 */
				if (selectedCommittees.contains(Committee.BUDGET_REVIEW)){
					
			
					//set the has_budget flag in
					String type = "BUDGET_REVIEW";
//					if (selectedCommittees.contains(Committee.BUDGET_REVIEW)){
//						type = "BUDGET_REVIEW";
//					}else if (selectedCommittees.contains(Committee.BUDGET_DEVELOP)) {
//						type = "BUDGET_DEVELOP";
//					}
					Element hasBudgetEl = extraDataXmlDoc.createElement("has-budget");
					hasBudgetEl.setTextContent("y");
					
					hasBudgetEl.setAttribute("type", type);
					extraDataXmlDoc.getDocumentElement().appendChild(hasBudgetEl);
					
					//replace //invovled-committees/ with an updated list
					selectedCommittees.remove(Committee.BUDGET_REVIEW);
					//selectedCommittees.remove(Committee.BUDGET_DEVELOP);
					
					
					selectedCommittees.remove(Committee.COVERAGE_REVIEW);
					selectedCommittees.remove(Committee.SUB_DEPARTMENT_CHIEF);
					selectedCommittees.remove(Committee.DEPARTMENT_CHAIR);
					selectedCommittees.remove(Committee.COLLEGE_DEAN);
					selectedCommittees.remove(Committee.HOSPITAL_SERVICES);
					
					Element invovledCommitteesEl = extraDataXmlDoc.createElement("invovled-committees");
					
					for(Committee c:selectedCommittees){
						Element cEl = extraDataXmlDoc.createElement("committee");
						cEl.setTextContent(c.toString());
						invovledCommitteesEl.appendChild(cEl);
					}
					
					Element oInvovledCommitteesEl = (Element) xPath
							.evaluate(
									"//invovled-committees",
									extraDataXmlDoc,
									XPathConstants.NODE);
					
					Node oP = oInvovledCommitteesEl.getParentNode();
					oP.removeChild(oInvovledCommitteesEl);
					oP.appendChild(invovledCommitteesEl);					
					
					//logger.debug("preProcessCommitteeReviewXml->extraDataXml: " + DomUtils.elementToString(extraDataXmlDoc));
				}
				
			}
			
			if (committee.equals(Committee.BUDGET_REVIEW) && action.equals("ROUTE_TO_PHARMACY")) {
				Protocol protocol = protocolForm.getProtocol();
				
				String protocolMeta = protocol.getMetaDataXml();

				protocolMeta = getXmlProcessor().replaceOrAddNodeValueByPath("/protocol/next-committee-after-pharmacy-review", protocolMeta, Committee.BUDGET_REVIEW.toString());
				
				protocol.setMetaDataXml(protocolMeta);
				
				protocol = getProtocolDao().saveOrUpdate(protocol);
			}
			
			if (committee.equals(Committee.BUDGET_REVIEW) && action.equals("ASSIGN_TO_COMMITTEES")) {
				NodeList invovledCommittees = (NodeList) xPath
						.evaluate(
								"//invovled-committees/committee",
								extraDataXmlDoc,
								XPathConstants.NODESET);
				
				List<Committee> selectedCommittees = new ArrayList<Committee>();

				for (int j = 0; j < invovledCommittees.getLength(); j++) {
					Element invovledCommitteeEl = (Element) invovledCommittees
							.item(j);
					logger.debug("preProcessCommitteeReviewXml->invovled-committee: "
							+ invovledCommitteeEl.getTextContent()
							+ "; type: "
							+ invovledCommitteeEl
									.getAttribute("type"));

					Committee involvedCommittee = Committee
							.valueOf(invovledCommitteeEl
									.getTextContent());

					selectedCommittees.add(involvedCommittee);
				}
				
				if (!selectedCommittees.contains(Committee.PI)) {
					selectedCommittees.add(Committee.IRB_ASSIGNER);
				}
				
				Element invovledCommitteesEl = extraDataXmlDoc.createElement("invovled-committees");
				
				for(Committee c:selectedCommittees){
					Element cEl = extraDataXmlDoc.createElement("committee");
					cEl.setTextContent(c.toString());
					invovledCommitteesEl.appendChild(cEl);
				}
				
				Element oInvovledCommitteesEl = (Element) xPath
						.evaluate(
								"//invovled-committees",
								extraDataXmlDoc,
								XPathConstants.NODE);
				
				Node oP = oInvovledCommitteesEl.getParentNode();
				oP.removeChild(oInvovledCommitteesEl);
				oP.appendChild(invovledCommitteesEl);	
					
			}
		
			extraDataXml =  DomUtils.elementToString(extraDataXmlDoc);
			
			
			logger.debug("preProcessCommitteeReviewXml->extraDataXml: " + extraDataXml);
			
		} catch (Exception e){
			e.printStackTrace();
			logger.warn("no extraDataXml...what's happening...");
			return extraDataXml;
		}
		
		return extraDataXml;
	}
	
	
	@Override
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		
		logger.debug("checkCondition->extraDataXml:" + extraDataXml);

		ProtocolForm protocolForm = (ProtocolForm) form;

		String formStatus = getFormStatus(form);
		
		if (formStatus.equals("")) return "";
		
		ProtocolFormStatusEnum protocolFormStatus = ProtocolFormStatusEnum
				.valueOf(formStatus);
		
		XmlProcessor xmlProcessor = getXmlProcessor();
		XPath xPath = xmlProcessor.getXPathInstance();	
		

		String condition = "";

		switch (protocolFormStatus){
		case UNDER_REVISION_MAJOR_CONTINGENCIES:
		case UNDER_REVISION_MINOR_CONTINGENCIES:
		case UNDER_REVISION:
			if (committee.equals(Committee.PI) && action.equals("SIGN_SUBMIT")){
				condition = formService.isCurrentUserSpecificRoleOrNot(protocolForm, user, "Principal Investigator")?"IS_PI":"IS_NOT_PI";
			}
			break;
		case UNDER_IRB_PREREVIEW:
			if(Committee.IRB_ASSIGNER.equals(committee) && action.equals("ASSIGN_REVIEWER")){
				
				try {
					Document extraDataXmlDoc = xmlProcessor.loadXmlStringToDOM(form.getMetaXml());
					logger.debug("xmlData: " + form.getMetaXml());
					Boolean hasConsentReviewerAssigned = (Boolean) (xPath
							.evaluate(
									"boolean(count(//assigned-reviewers/assigned-reviewer[@assigning-committee='IRB_ASSIGNER' and @user-role-committee='IRB_CONSENT_REVIEWER']) > 0)",
									extraDataXmlDoc, XPathConstants.BOOLEAN));
					condition = hasConsentReviewerAssigned?"WITH_CONSENT":"NO_CONSENT";

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
		/*
		case UNDER_IRB_PREREVIEW:
			if (committee.equals(Committee.IRB_PROTOCOL_REVIEWER)) {
				ProtocolFormCommitteeStatus irbProtocolReviewStatus = this
						.getProtocolFormCommitteeStatusDao()
						.getLatestByCommitteeAndProtocolFormId(
								Committee.IRB_CONSENT_REVIEWER,
								protocolForm.getId());

				if (irbProtocolReviewStatus.getProtocolFormCommitteeStatus()
						.equals(ProtocolFormCommitteeStatusEnum.APPROVED)) {
					condition = "IRB_PREREVIEW_COMPLETED";
				} else {
					condition = "IRB_PREREVIEW_NOT_COMPLETED";
				}
			}

			if (committee.equals(Committee.IRB_CONSENT_REVIEWER)) {
				ProtocolFormCommitteeStatus irbConsentReviewStatus = this
						.getProtocolFormCommitteeStatusDao()
						.getLatestByCommitteeAndProtocolFormId(
								Committee.IRB_PROTOCOL_REVIEWER,
								protocolForm.getId());
				logger.debug("status:"
						+ irbConsentReviewStatus
								.getProtocolFormCommitteeStatus());
				if (irbConsentReviewStatus.getProtocolFormCommitteeStatus()
						.equals(ProtocolFormCommitteeStatusEnum.APPROVED)) {
					condition = "IRB_PREREVIEW_COMPLETED";
				} else {
					condition = "IRB_PREREVIEW_NOT_COMPLETED";
				}
			}
			break;
			*/
		case UNDER_PREREVIEW:
			/**
			 * check whether there is a budget or not... and check to see if it's budget develop or review 
			 */
			if(Committee.GATEKEEPER.equals(committee) && action.equals("ASSIGN_TO_COMMITTEES")){
				
					try {
						XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
						
						String requestPharmacyReview = xmlHandler.getSingleStringValueByXPath(protocolForm.getMetaDataXml(), "/protocol/pharmacy-review-requested");
						
						if (requestPharmacyReview.equals("y")) {
							condition = "PHARMACY";
						} else {
							Document extraDataXmlDoc = xmlProcessor.loadXmlStringToDOM(extraDataXml);
							Element hasBudgetEl = (Element)xPath.evaluate("//has-budget", extraDataXmlDoc, XPathConstants.NODE);
							
							if(hasBudgetEl != null){
								String flag = hasBudgetEl.getTextContent();
								if ("y".equals(flag)){
									logger.debug("has protocol; set condition");
									condition = hasBudgetEl.getAttribute("type"); //either BUDGET_REVIEW or BUDGET_DEVELOPE
								}
							}/*else{
								if (protocolFormReviewLogicServiceContainer.getProtocolFormReviewLogicService("NEW_SUBMISSION").isInvolvedByType(protocolForm, "Drug")){
									condition = "NO_BUDGET_HAVE_DRUG";
								} else {
									condition = "NO_BUDGET_NO_DRUG";
								}	
							}*/
							else {
								condition = "NO_BUDGET";
							}
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XPathExpressionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				
			}
			break;
		case UNDER_PHARMACY_REVIEW:
			if (committee.equals(Committee.PHARMACY_REVIEW) && action.equals("APPROVE")) {
				try {
					XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
					
					String requestBudgetReview = xmlHandler.getSingleStringValueByXPath(form.getObjectMetaData(), "/protocol/next-committee-after-pharmacy-review");
					
					if (requestBudgetReview.equals(Committee.BUDGET_MANAGER.toString())) {
						condition = "BUDGET_MANAGER";
					} else if (requestBudgetReview.equals(Committee.BUDGET_REVIEW.toString())) {
						condition = "BUDGET_REVIEW";
					}
					else {
						condition = "NO_BUDGET_REVIEW";
					}
				} catch (Exception e) {
					
				}
			}
			
			break;
		case UNDER_BUDGET_MANAGER_REVIEW: 
			
			if(Committee.BUDGET_MANAGER.equals(committee)){
				try {
					Document extraDataXmlDoc = xmlProcessor.loadXmlStringToDOM(extraDataXml);
					Element needPharmacyEl = (Element)xPath.evaluate("//need-pharmacy", extraDataXmlDoc, XPathConstants.NODE);
					
					if(needPharmacyEl != null){
						String flag = needPharmacyEl.getTextContent();
						if ("y".equals(flag)){
							logger.debug("has protocol; set condition");
							condition = needPharmacyEl.getAttribute("type");
						} else {
							condition = "NO_NEED_PHARMACY_REVIEW";
						}
					} else {
						condition = "NO_NEED_PHARMACY_REVIEW";
					}
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
					
				if (action.equals("ASSIGN_REVIEWER")){
					ProtocolFormCommitteeStatus budgetManagerCommitteeStatus = getProtocolFormCommitteeStatusDao()
							.getLatestByCommitteeAndProtocolFormId(
									Committee.BUDGET_MANAGER,
									protocolForm.getId());
						logger.debug("budget manager status: " + budgetManagerCommitteeStatus.getProtocolFormCommitteeStatus().getDescription());
						if (ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT.equals(budgetManagerCommitteeStatus.getProtocolFormCommitteeStatus())){
							if (protocolFormReviewLogicServiceContainer.getProtocolFormReviewLogicService("MODIFICATION").isInvolvedByType(protocolForm, "Device")){
								condition = "HAVE_DEVICE";
							} else {
								condition = "NO_DEVICE";
							}
							
						}	
				}
								
			}
			break;
		}
		
		return condition;
	}
	
	/*
	@Override
	public String checkWorkflow(Form form, Committee committee, User user,
			String action, String extraDataXml){
		logger.debug("enter workflow...");
		String workflow = "";
		
		ProtocolForm protocolform = (ProtocolForm) form;
		
		ProtocolFormXmlData protocolXmlData = protocolform.getTypedProtocolFormXmlDatas().get(protocolform.getProtocolFormType().getDefaultProtocolFormXmlDataType());
		
		if (committee.equals(Committee.IRB_PREREVIEW) && action.equals("APPROVE")){
			workflow = protocolFormService.workFlowDetermination(protocolXmlData);
		}
		
		return workflow;
		
	}
	*/

	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}

	@Autowired(required=true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}


	public FormService getFormService() {
		return formService;
	}

	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}


	public ProtocolFormReviewLogicServiceContainer getProtocolFormReviewLogicServiceContainer() {
		return protocolFormReviewLogicServiceContainer;
	}

	@Autowired(required=true)
	public void setProtocolFormReviewLogicServiceContainer(
			ProtocolFormReviewLogicServiceContainer protocolFormReviewLogicServiceContainer) {
		this.protocolFormReviewLogicServiceContainer = protocolFormReviewLogicServiceContainer;
	}

}
