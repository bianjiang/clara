package edu.uams.clara.webapp.protocol.service.protocolform.staff.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicService;

public class StaffReviewLogicServiceImpl extends
ProtocolFormReviewLogicService {
	
	private Map<String, String> extraContentPanel = new HashMap<String, String>();{
		extraContentPanel.put("irb-prereview", "<panels><panel xtype=\"clarareviewermodirbprereviewpanel\" id=\"IRBPrereviewFinalReviewPanel\"><formdata>");
		extraContentPanel.put("irb-audit-prereview", "<panels><panel xtype=\"clarareviewerirbprereviewauditresponse\" id=\"IRBPrereviewFinalReviewPanel\"><formdata>");
		extraContentPanel.put("irb-expedited-review", "<panels><panel xtype=\"clara.reviewer.newsubmission.irb.expedited.review.panel\" id=\"NewSubmissionIRBExpeditedFinalReviewPanel\"><formdata>");
		extraContentPanel.put("irb-exempt-review", "<panels><panel xtype=\"clara.reviewer.newsubmission.irb.exempt.review.panel\" id=\"NewSubmissionIRBExemptFinalReviewPanel\"><formdata>");
		extraContentPanel.put("regulatory-review", "<panels><panel xtype=\"clara.reviewer.newsubmission.ragulatory.review.panel\" id=\"RagulatoryFinalReviewPanel\"><formdata>");
		extraContentPanel.put("coverage-review", "<panels><panel xtype=\"clarareviewercoveragepanel\" id=\"CoverageFinalReviewPanel\"><formdata>");
		extraContentPanel.put("budget-review", "<panels><panel xtype=\"clarareviewerbudgetmanagerassigncommitteepanel\" id=\"BudgetManagerAssignCommitteePanel\"><formdata><committees>");
		extraContentPanel.put("irb-office-review", "<panels><panel xtype=\"clarareviewerirbofficeassigncommitteepanel\" id=\"IRBOfficeAssignCommitteePanel\"><formdata><committees>");
		extraContentPanel.put("gatekeeper-review", "<panels><panel xtype=\"clarareviewergatekeeperassigncommitteepanel\" id=\"GatekeeperAssignCommitteePanel\"><formdata><committees>");
	}
	
	private String getFinalReviewExtralContentPanel(long protocolFormId, String reviewFormIdentifier){
		String resultXml = "";
		
		resultXml = extraContentPanel.get(reviewFormIdentifier);
		
		resultXml += "</formdata></panel></panels>";
		
		return resultXml;
	}

	@Override
	public String getExtraContent(long protocolFormId,
			String reviewFormIdentifier) {
		String resultXml = getFinalReviewExtralContentPanel(protocolFormId, reviewFormIdentifier);
		
		return resultXml;
	}

}
