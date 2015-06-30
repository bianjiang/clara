package edu.uams.clara.webapp.protocol.service.protocolform.newsubmission.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicService;

public class NewSubmissionReviewLogicServiceImpl extends
	ProtocolFormReviewLogicService {
	private final static Logger logger = LoggerFactory
			.getLogger(NewSubmissionReviewLogicServiceImpl.class);

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolFormDao protocolFormDao;

	private ProtocolDao protocolDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	@Value("${newsubmission.committeesListXml.url}")
	private String committeesListXmlPath;

	@Override
	public String getExtraContent(long protocolFormId,
			String reviewFormIdentifier) {
		String resultXml = "";
		logger.debug("Checking for extra content for: " + reviewFormIdentifier);
		if (reviewFormIdentifier.equals("gatekeeper-review")
				|| reviewFormIdentifier.equals("budget-review")
				|| reviewFormIdentifier.equals("ach-gatekeeper-review")
				|| reviewFormIdentifier.equals("irb-office-review")
				|| reviewFormIdentifier.equals("irb-prereview")) {
			resultXml = getCommitteesList(protocolFormId, reviewFormIdentifier);
		} else {
			resultXml = getFinalReviewExtralContentPanel(protocolFormId, reviewFormIdentifier);
		}

		return resultXml;
	}
	
	private Map<String, List<String>> getExtraContentValues(long protocolFormId, Set<String> paths) {
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		Protocol protocol = protocolForm.getProtocol();

		String protocolMetaData = protocol.getMetaDataXml();

		Map<String, List<String>> values = null;
		try {
			values = getXmlProcessor().listElementStringValuesByPaths(paths, protocolMetaData);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return values;
	}
	
	private Map<String, String> extraContentPanel = new HashMap<String, String>();{
		//extraContentPanel.put("irb-prereview", "<panels><panel xtype=\"clarareviewernewsubirbprereviewpanel\" id=\"IRBPrereviewFinalReviewPanel\"></panel><panel xtype=\"clarareviewerirbprereviewassigncommitteepanel\" id=\"IRBPrereviewAssignCommitteePanel\">{extraFormData}</panel></panels>");
		extraContentPanel.put("irb-prereview", "<panels><panel xtype=\"clarareviewernewsubirbprereviewpanel\" id=\"IRBPrereviewFinalReviewPanel\"></panel></panels>");
		extraContentPanel.put("irb-expedited-review", "<panels><panel xtype=\"clara.reviewer.newsubmission.irb.expedited.review.panel\" id=\"NewSubmissionIRBExpeditedFinalReviewPanel\"></panel></panels>");
		extraContentPanel.put("irb-exempt-review", "<panels><panel xtype=\"clara.reviewer.newsubmission.irb.exempt.review.panel\" id=\"NewSubmissionIRBExemptFinalReviewPanel\"></panel></panels>");
		extraContentPanel.put("regulatory-review", "<panels><panel xtype=\"clara.reviewer.newsubmission.ragulatory.review.panel\" id=\"RagulatoryFinalReviewPanel\">{extraFormData}</panel></panels>");
		extraContentPanel.put("coverage-review", "<panels><panel xtype=\"clarareviewercoveragepanel\" id=\"CoverageFinalReviewPanel\">{extraFormData}</panel></panels>");
		extraContentPanel.put("gatekeeper-review", "<panels><panel xtype=\"clarareviewergatekeeperassigncommitteepanel\" id=\"GatekeeperAssignCommitteePanel\">{extraFormData}</panel></panels>");
		extraContentPanel.put("budget-review", "<panels><panel xtype=\"clarareviewerbudgetmanagerassigncommitteepanel\" id=\"BudgetManagerAssignCommitteePanel\">{extraFormData}</panel></panels>");
		extraContentPanel.put("ach-gatekeeper-review", "<panels><panel xtype=\"clarareviewerachriassigncommitteepanel\" id=\"ACHRIAssignCommitteePanel\">{extraFormData}</panel></panels>");
		extraContentPanel.put("irb-office-review", "<panels><panel xtype=\"clarareviewerirbofficeassigncommitteepanel\" id=\"IRBOfficeAssignCommitteePanel\"><formdata><committees>");
		extraContentPanel.put("audit-irb-office-review", "<panels><panel xtype=\"clarareviewerauditirbofficepanel\" id=\"AuditIRBOfficeFinalReviewPanel\"><committees>");
		extraContentPanel.put("clinicaltrials-review", "<panels><panel xtype=\"clarareviewerclinicaltrialsreviewpanel\" id=\"ClinicalTrialsReviewPanel\"></panel></panels>");
	}
	
	private Set<String> extraContentAnswerPaths = new HashSet<String>();{
		extraContentAnswerPaths.add("/protocol/summary/drugs-and-devices/ind");
		extraContentAnswerPaths.add("/protocol/summary/drugs-and-devices/ide");
		extraContentAnswerPaths.add("/protocol/summary/coverage-determination/medicare-benefit");
		extraContentAnswerPaths.add("/protocol/summary/coverage-determination/theraputic-intent");
		extraContentAnswerPaths.add("/protocol/summary/coverage-determination/enrolled-diagnosed");
		extraContentAnswerPaths.add("/protocol/summary/coverage-determination/trial-category");
	}
	
	private String getFinalReviewExtralContentPanel(long protocolFormId, String reviewFormIdentifier){
		String resultXml = "";
		
		resultXml = extraContentPanel.get(reviewFormIdentifier);
		
		String extraFormData = "<formdata>";
		
		if (reviewFormIdentifier.equals("regulatory-review") || reviewFormIdentifier.equals("coverage-review")){
			Map<String, List<String>> answers = getExtraContentValues(protocolFormId, extraContentAnswerPaths);

			try{
				if (reviewFormIdentifier.equals("regulatory-review")){
					String ind = (answers.get("/protocol/summary/drugs-and-devices/ind") != null && answers.get("/protocol/summary/drugs-and-devices/ind").size() > 0)?answers.get("/protocol/summary/drugs-and-devices/ind").get(0):"";
					String ide = (answers.get("/protocol/summary/drugs-and-devices/ide") != null && answers.get("/protocol/summary/drugs-and-devices/ide").size() > 0)?answers.get("/protocol/summary/drugs-and-devices/ide").get(0):"";
					extraFormData += "<ind>" + ind + "</ind><ide>" + ide + "</ide>";
				}
			
			
				if (reviewFormIdentifier.equals("coverage-review")){
					String medicareBenefit = (answers.get("/protocol/summary/coverage-determination/medicare-benefit") != null && answers.get("/protocol/summary/coverage-determination/medicare-benefit").size() > 0)?answers.get("/protocol/summary/coverage-determination/medicare-benefit").get(0):"";
					String theraputicIntent = (answers.get("/protocol/summary/coverage-determination/theraputic-intent") != null && answers.get("/protocol/summary/coverage-determination/theraputic-intent").size() > 0)?answers.get("/protocol/summary/coverage-determination/theraputic-intent").get(0):"";
					String enrolledDiagnosed = (answers.get("/protocol/summary/coverage-determination/enrolled-diagnosed") != null && answers.get("/protocol/summary/coverage-determination/enrolled-diagnosed").size() > 0)?answers.get("/protocol/summary/coverage-determination/enrolled-diagnosed").get(0):"";
					String trialCategory = (answers.get("/protocol/summary/coverage-determination/trial-category") != null && answers.get("/protocol/summary/coverage-determination/trial-category").size() > 0)?answers.get("/protocol/summary/coverage-determination/trial-category").get(0):"";
					extraFormData += "<medicare-benefit>" + medicareBenefit + "</medicare-benefit><theraputic-intent>" + theraputicIntent + "</theraputic-intent><enrolled-diagnosed>" + enrolledDiagnosed + "</enrolled-diagnosed><trial-category>" + trialCategory + "</trial-category>";
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			
		}
		
		extraFormData += "</formdata>";
		
		resultXml = resultXml.replace("{extraFormData}", extraFormData);

		return resultXml;
	}
	
	private Map<String, String> committeeLookupPaths = new HashMap<String, String>();{
		committeeLookupPaths.put("gatekeeper-review", "/committees/committee[@assigned-by[contains(., \"GATEKEEPER\")]]");
		committeeLookupPaths.put("budget-review", "/committees/committee[@assigned-by[contains(., \"BUDGET\")]]");
		committeeLookupPaths.put("ach-gatekeeper-review", "/committees/committee[@assigned-by[contains(., \"ACHRI\")]]");
		committeeLookupPaths.put("irb-office-review", "/committees/committee[@assigned-by[contains(., \"IRB\")]]");
		committeeLookupPaths.put("irb-prereview", "/committees/committee[@assigned-by[contains(., \"IRB_PREREVIEW\")]]");
	}

	private String getCommitteesList(long protocolFormId,
			String reviewFormIdentifier) {
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		List<Committee> checkedCommittees = new ArrayList<Committee>(0);

		if (reviewFormIdentifier.equals("gatekeeper-review")
				&& isInvolvedByType(protocolForm,
						"Budget")) {
			//checkedCommittees.add(Committee.PHARMACY_REVIEW);
			checkedCommittees.add(Committee.PRECOVERAGE_REVIEW);
			checkedCommittees.add(Committee.BUDGET_REVIEW);
			checkedCommittees.add(Committee.COVERAGE_REVIEW);
			checkedCommittees.add(Committee.SUB_DEPARTMENT_CHIEF);
			checkedCommittees.add(Committee.DEPARTMENT_CHAIR);
			checkedCommittees.add(Committee.COLLEGE_DEAN);
			checkedCommittees.add(Committee.HOSPITAL_SERVICES);
			checkedCommittees.add(Committee.COI);
		}

		if (isInvolvedByType(protocolForm, "Biosafety")) {
			checkedCommittees.add(Committee.BIOSAFETY);
		}
		
		/*
		List<ProtocolFormCommitteeStatus> pfcsLst = protocolFormCommitteeStatusDao.listAllByCommitteeAndProtocolFormId(Committee.PHARMACY_REVIEW, protocolFormId);

		if (pfcsLst == null || pfcsLst.isEmpty()){
			if (isInvolvedByType(protocolForm, "Drug")) {
				checkedCommittees.add(Committee.PHARMACY_REVIEW);
			}
		}*/

		if (isInvolvedByType(protocolForm, "Contract")) {
			checkedCommittees.add(Committee.PROTOCOL_LEGAL_REVIEW);
		}
		
		if (isInvolvedByType(protocolForm, "Device")) {
			checkedCommittees.add(Committee.MONITORING_REGULATORY_QA);
		}

		if (isInvolvedByType(protocolForm, "PRMC")) {
			checkedCommittees.add(Committee.PRMC);
		}

		if (isInvolvedByType(protocolForm, "Radiation")) {
			checkedCommittees.add(Committee.RADIATION_SAFETY);
		}
		
		if (isInvolvedByType(protocolForm, "ClinicalTrials")) {
			checkedCommittees.add(Committee.CLINICAL_TRIALS_REVIEW);
		}	
		
		String resultXml = extraContentPanel.get(reviewFormIdentifier);

		String extraFormData = "<formdata><committees>";
		
		String lookupPath = committeeLookupPaths.get(reviewFormIdentifier);

		String checked = "";

		// String lookupPath = "/committees/committee[@assigned-by='"+
		// assingedBy +"' or @assigned-by='ANY']";

		try {
			Document committeesListDoc = getXmlProcessor()
					.loadXmlFileToDOM(committeesListXmlPath);

			XPath xpath = getXmlProcessor().getXPathInstance();

			NodeList committeesList = (NodeList) (xpath.evaluate(lookupPath,
					committeesListDoc, XPathConstants.NODESET));
			
			Map<String, String> alreadyAssignedCommitteeMap = getAssignedCommittee(protocolForm);

			String assigned = "";
			String status = "";

			for (int i = 0; i < committeesList.getLength(); i++) {
				if (committeesList.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element currentCommitteeElement = (Element) committeesList
							.item(i);
					if (checkedCommittees.contains(Committee
							.valueOf(currentCommitteeElement
									.getAttribute("name")))) {
						checked = "true";
					} else {
						checked = "false";
					}

					if (alreadyAssignedCommitteeMap.containsKey(currentCommitteeElement
									.getAttribute("name"))) {
						assigned = "true";
						status = alreadyAssignedCommitteeMap.get(currentCommitteeElement
									.getAttribute("name"));
					} else {
						assigned = "false";
					}

					extraFormData += "<committee><name>"
							+ currentCommitteeElement.getAttribute("name")
							+ "</name>";
					extraFormData += "<desc>"
							+ currentCommitteeElement.getAttribute("desc")
							+ "</desc>";
					extraFormData += "<checked>" + checked
							+ "</checked>";
					extraFormData += "<individual-assignment>" + currentCommitteeElement.getAttribute("individual-assignment")
							+ "</individual-assignment>";
					extraFormData += "<assigned>"+ assigned + "</assigned>";
					extraFormData += "<status>"+ status + "</status></committee>";
					
					logger.debug("resultXml: " + resultXml);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		extraFormData += "</committees></formdata>";

		resultXml = resultXml.replace("{extraFormData}", extraFormData);

		return resultXml;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}
}
