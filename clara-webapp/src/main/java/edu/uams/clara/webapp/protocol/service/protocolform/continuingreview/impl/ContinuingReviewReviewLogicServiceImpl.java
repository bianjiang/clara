package edu.uams.clara.webapp.protocol.service.protocolform.continuingreview.impl;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicService;

public class ContinuingReviewReviewLogicServiceImpl extends
	ProtocolFormReviewLogicService {
	private final static Logger logger = LoggerFactory
			.getLogger(ContinuingReviewReviewLogicServiceImpl.class);

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	private ProtocolFormDao protocolFormDao;

	private ProtocolDao protocolDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

	@Override
	public String getExtraContent(long protocolFormId,
			String reviewFormIdentifier) {
		String resultXml = "";
		logger.debug("Checking for extra content for: " + reviewFormIdentifier);
		
		resultXml = getFinalReviewExtralContentPanel(protocolFormId, reviewFormIdentifier);

		return resultXml;
	}
	
	private Map<String, String> extraContentPanel = new HashMap<String, String>();{
		extraContentPanel.put("cr-irb-prereview", "<panels><panel xtype=\"clarareviewercrirbprereviewpanel\" id=\"IRBPrereviewFinalReviewPanel\"><formdata>");
		extraContentPanel.put("irb-cr-expedited-review", "<panels><panel xtype=\"clara.reviewer.continuingreview.irb.expedited.review.panel\" id=\"ContinuingReviewIRBExpeditedFinalReviewPanel\"><formdata>");
		extraContentPanel.put("irb-cr-exempt-review", "<panels><panel xtype=\"clara.reviewer.continuingreview.irb.exempt.review.panel\" id=\"ContinuingReviewIRBExemptFinalReviewPanel\"><formdata>");
	}
	
	private String getFinalReviewExtralContentPanel(long protocolFormId, String reviewFormIdentifier){
		String resultXml = "";
		
		resultXml = extraContentPanel.get(reviewFormIdentifier);
		
		resultXml += "</formdata></panel></panels>";
		
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
