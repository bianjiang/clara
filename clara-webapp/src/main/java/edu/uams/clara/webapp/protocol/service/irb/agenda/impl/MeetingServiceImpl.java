package edu.uams.clara.webapp.protocol.service.irb.agenda.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.irb.agenda.MeetingService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class MeetingServiceImpl implements MeetingService {
	private final static Logger logger = LoggerFactory
			.getLogger(MeetingServiceImpl.class);
	
	private XmlProcessor xmlProcessor;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolDao protocolDao;
	
	private AgendaItemDao agendaItemDao;
	
	private Map<String, String> getMotion(ProtocolFormType protocolFormType, String xmlData, long agendaItemId){
		Map<String, String> motionRelatedValues = new HashMap<String, String>();
		
		try{
			Document doc = xmlProcessor.loadXmlStringToDOM(xmlData);
			XPath xPath = xmlProcessor.getXPathInstance();
			
			NodeList nl = (NodeList) (xPath.evaluate("//item[@agendaitemid='"+ agendaItemId +"']/motions/motion",
					doc, XPathConstants.NODESET));
			
			String maxTs = "";

			if (nl.getLength() > 0){
				for(int i=0; i<nl.getLength(); i++){
					Element currentEl = (Element) nl.item(i);
					String currentTs = currentEl.getAttribute("ts");
					logger.debug("currentTs: " + currentTs);
					String nextTs = "";
					if (currentEl.getNextSibling() != null){
						Element nextEl = (Element) currentEl.getNextSibling();
						nextTs = nextEl.getAttribute("ts");
					} else {
						nextTs = currentTs;
					}
					
					maxTs = currentTs;
					
					if (Long.valueOf(nextTs) > Long.valueOf(currentTs)){
						maxTs = nextTs;
					}
				}
			}

			Element maxTsEl = (Element) xPath.evaluate("//motion[@ts='"+ maxTs +"']", doc, XPathConstants.NODE);
			
			motionRelatedValues.put("value", maxTsEl.getAttribute("value"));
			motionRelatedValues.put("review-period", maxTsEl.getAttribute("reviewperiod"));
			motionRelatedValues.put("adult-risk", maxTsEl.getAttribute("adultrisk"));
			motionRelatedValues.put("ped-risk", maxTsEl.getAttribute("pedrisk"));
			motionRelatedValues.put("consent-waived", maxTsEl.getAttribute("consentwaived"));
			motionRelatedValues.put("consent-document-waived", maxTsEl.getAttribute("consentdocumentationwaived"));
			motionRelatedValues.put("assent-waived", maxTsEl.getAttribute("assentwaived"));
			motionRelatedValues.put("assent-document-waived", maxTsEl.getAttribute("assentdocumentationwaived"));
			motionRelatedValues.put("hipaa-waived", maxTsEl.getAttribute("hipaawaived"));
			motionRelatedValues.put("hipaa-applicable", maxTsEl.getAttribute("hipaa"));
			motionRelatedValues.put("suggested-next-review-type", maxTsEl.getAttribute("reviewtype"));
			motionRelatedValues.put("non-compliance_assessment", maxTsEl.getAttribute("ncdetermination"));
			motionRelatedValues.put("reportable-to-ohrp", maxTsEl.getAttribute("ncreportable"));
			motionRelatedValues.put("upirtso", maxTsEl.getAttribute("UPIRTSO"));

		} catch (Exception e){
			e.printStackTrace();
		}
		
		return motionRelatedValues;
	}

	@Override
	public String generateActionByMotion(ProtocolFormType protocolFormType, String xmlData, long agendaItemId) {

		String action = "";
		String motion = getMotion(protocolFormType, xmlData, agendaItemId).get("value");

		if (motion.equals("Approve")){
			action = "APPROVE";
		}
		
		if (motion.equals("Decline")){
			action = "DECLINE";
		}
		
//		if (protocolFormType.equals(ProtocolFormType.NEW_SUBMISSION)){
//			if (action.contains("Defer")){
//				action = "DEFER";
//			}
//		} else {
//			if (action.equals("Defer with minor contingencies")){
//				action = "DEFER_WITH_MINOR";
//			}
//			
//			if (action.equals("Defer with major contingencies")){
//				action = "DEFER_WITH_MAJOR";
//			}
//		}
		
		if (motion.equals("Defer with minor contingencies")){
			action = "DEFER_WITH_MINOR";
		}
		
		if (motion.equals("Defer with major contingencies")){
			action = "DEFER_WITH_MAJOR";
		}
	
		
		if (motion.equals("Table")){
			action = "TABLE";
		}
		
		if (motion.equals("Acknowledge")){
			action = "ACKNOWLEDGE";
		}
		
		if (motion.equals("Administratively Remove")){
			action = "WITHDRAW";
		}
		
		if (motion.equals("Suspended for Cause")){
			action = "SUSPENDED_FOR_CAUSE";
		}
		
		if (motion.equals("Terminated for Cause")){
			action = "TERMINATED_FOR_CAUSE";
		}

		return action;
	}

	@Override
	public ProtocolForm addLatestMotionToProtocolForm(
			ProtocolForm protocolForm, String xmlData, long agendaItemId) {
		String protocolFormMetaData = protocolForm.getMetaDataXml();
		String motion = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("value");
		String reviewPeriod = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("review-period");
		String adultRisk = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("adult-risk");
		String pedRisk = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("ped-risk");
		String consentWaived = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("consent-waived");
		String consentDocumentWaived = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("consent-document-waived");
		String assentWaived = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("assent-waived");
		String assentDocumentWaived = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("assent-document-waived");
		String hippaWaived = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("hipaa-waived");
		String hippaNotApplicable = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("hipaa-applicable");
		String suggestedReviewType = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("suggested-next-review-type");
		String nonComplianceAssessment = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("non-compliance_assessment");
		String reportableToOHRP = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("reportable-to-ohrp");
		String upirtso = getMotion(protocolForm.getProtocolFormType(), xmlData, agendaItemId).get("upirtso");
		String hippaWaivedDate = (hippaWaived.toLowerCase().equals("yes"))?DateFormatUtil.formateDateToMDY(agendaItemDao.findById(agendaItemId).getAgenda().getDate()):"";
		
		try{
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/agenda-date", protocolFormMetaData, DateFormatUtil.formateDateToMDY(agendaItemDao.findById(agendaItemId).getAgenda().getDate()));
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/recent-motion", protocolFormMetaData, motion);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/review-period", protocolFormMetaData, reviewPeriod);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/adult-risk", protocolFormMetaData, adultRisk);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/ped-risk", protocolFormMetaData, pedRisk);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/consent-waived", protocolFormMetaData, consentWaived);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/consent-document-waived", protocolFormMetaData, consentDocumentWaived);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/assent-waived", protocolFormMetaData, assentWaived);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/assent-document-waived", protocolFormMetaData, assentDocumentWaived);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-waived", protocolFormMetaData, hippaWaived);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-applicable", protocolFormMetaData, hippaNotApplicable);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/suggested-next-review-type", protocolFormMetaData, suggestedReviewType);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/non-compliance-assessment", protocolFormMetaData, nonComplianceAssessment);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/reportable-to-ohrp", protocolFormMetaData, reportableToOHRP);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-waived-date", protocolFormMetaData, hippaWaivedDate);
			protocolFormMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/upirtso", protocolFormMetaData, upirtso);
			protocolForm.setMetaDataXml(protocolFormMetaData);
			
			protocolForm = protocolFormDao.saveOrUpdate(protocolForm);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		Protocol protocol = protocolForm.getProtocol();
		String protocolMetaData = protocol.getMetaDataXml();
		
		try{
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/agenda-date", protocolMetaData, DateFormatUtil.formateDateToMDY(agendaItemDao.findById(agendaItemId).getAgenda().getDate()));
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/recent-motion", protocolMetaData, motion);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/review-period", protocolMetaData, reviewPeriod);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/adult-risk", protocolMetaData, adultRisk);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/ped-risk", protocolMetaData, pedRisk);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/consent-waived", protocolMetaData, consentWaived);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/consent-document-waived", protocolMetaData, consentDocumentWaived);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/assent-waived", protocolMetaData, assentWaived);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/assent-document-waived", protocolMetaData, assentDocumentWaived);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/hipaa-waived", protocolMetaData, hippaWaived);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/hipaa-applicable", protocolMetaData, hippaNotApplicable);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/suggested-next-review-type", protocolMetaData, suggestedReviewType);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/non-compliance-assessment", protocolMetaData, nonComplianceAssessment);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/reportable-to-ohrp", protocolMetaData, reportableToOHRP);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/hipaa-waived-date", protocolMetaData, hippaWaivedDate);
			protocolMetaData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/summary/irb-determination/upirtso", protocolMetaData, upirtso);
			protocol.setMetaDataXml(protocolMetaData);
			
			protocolDao.saveOrUpdate(protocol);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return protocolForm;
	}
	
	@Override
	public String getEmailTemplateIdentifier(ProtocolForm protocolForm,
			Committee committee, String action) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}
	
	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

}
