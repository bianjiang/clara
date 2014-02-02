package edu.uams.clara.webapp.protocol.service.protocolform.audit.impl;

import java.util.HashMap;
import java.util.Map;

import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicService;

public class AuditReviewLogicServiceImpl extends ProtocolFormReviewLogicService {

	@Override
	public String getExtraContent(long protocolFormId,
			String reviewFormIdentifier) {
		String resultXml = "";
		
		resultXml = getFinalReviewExtralContentPanel(protocolFormId, reviewFormIdentifier);

		return resultXml;
	}
	
	private Map<String, String> extraContentPanel = new HashMap<String, String>();{
		extraContentPanel.put("audit-irb-prereview-review", "<panels><panel xtype=\"clarareviewerauditirbprereviewpanel\" id=\"AuditIRBPrereviewFinalReviewPanel\"><formdata>");
	}
	
	private String getFinalReviewExtralContentPanel(long protocolFormId, String reviewFormIdentifier){
		String resultXml = "";
		
		resultXml = extraContentPanel.get(reviewFormIdentifier);
		
		resultXml += "</formdata></panel></panels>";
		
		return resultXml;
	}

}
