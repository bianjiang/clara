package edu.uams.clara.webapp.report.service.customreport.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class ClinicalTrialInQueueReportServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory
			.getLogger(ClinicalTrialInQueueReportServiceImpl.class);
	
	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private ReportFieldDao reportFieldDao;

	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"timeinqueue\" desc=\"Date Entered CT.gov Queue\" hidden=\"false\" />";
		finalResultXml += "<field id=\"othercommittee\" desc=\"Other Committees\" hidden=\"false\" />";
		finalResultXml += "<field id=\"othercmmstatus\" desc=\"Other Committees' Review Status \" hidden=\"false\" />";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		
		
		//get all protocolForms
		List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao.listByCommitteeAndStatus(Committee.CLINICAL_TRIALS_REVIEW, ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		List<ProtocolFormCommitteeStatus> pfcssForPending = protocolFormCommitteeStatusDao.listByCommitteeAndStatus(Committee.CLINICAL_TRIALS_REVIEW, ProtocolFormCommitteeStatusEnum.PENDING_OUTCOME_OF_OTHER_COMMITTEE);
		pfcss.addAll(pfcssForPending);
		
		//process each protocol
		for(ProtocolFormCommitteeStatus pfcs : pfcss){
			//ProtocolForm pf = pfcs.getProtocolForm();
			long pfid = pfcs.getProtocolFormId();
			
			List<ProtocolFormCommitteeStatus> pfcsListofForm = protocolFormCommitteeStatusDao.listLatestByProtocolFormId(pfid);
			finalResultXml += processEachProtocol (pfcsListofForm,pfcs);
		}
		
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		
		return finalResultXml;
	}
	
	
	private String processEachProtocol(List<ProtocolFormCommitteeStatus> pfcsListofForm, ProtocolFormCommitteeStatus clinicalGovStatus){
		String xmlForEachProtocol = "";
		xmlForEachProtocol += "<report-item>";
		
		long protocolId = clinicalGovStatus.getProtocolForm().getProtocol().getId();
		xmlForEachProtocol += "<field id=\"" + "protocolId" + "\">";
		xmlForEachProtocol +="<![CDATA[<a target=\"_blank\" href=\""+this.getAppHost()+"/clara-webapp/protocols/"+protocolId+"/dashboard\">"+protocolId+"</a>]]>";
		xmlForEachProtocol += "</field>";
		
		xmlForEachProtocol += "<field id=\"" + "timeinqueue" + "\">";
		xmlForEachProtocol += clinicalGovStatus.getModifiedDateTime();
		xmlForEachProtocol += "</field>";
		
		
		boolean firstRow = true;
		for(ProtocolFormCommitteeStatus pfcs :pfcsListofForm){
			try{
				
				if(pfcs.getCommittee().equals(Committee.CLINICAL_TRIALS_REVIEW)){
					continue;
				}
				
				if(!firstRow){
					//add two empty columns
					xmlForEachProtocol += "<report-item>";
					xmlForEachProtocol += "<field id=\"" + "protocolId" + "\">";
					xmlForEachProtocol += "</field>";
					
					xmlForEachProtocol += "<field id=\"" + "timeinqueue" + "\">";
					xmlForEachProtocol += "</field>";
				}else{
					firstRow = false;
				}
				
				xmlForEachProtocol += "<field id=\"" + "othercommittee" + "\">";
				xmlForEachProtocol += pfcs.getCommitteeDescription();
				xmlForEachProtocol += "</field>";
				
				xmlForEachProtocol += "<field id=\"" + "othercmmstatus" + "\">";
				xmlForEachProtocol += pfcs.getProtocolFormCommitteeStatus().getDescription();
				xmlForEachProtocol += "</field>";
				xmlForEachProtocol += "</report-item>";
				
			}catch(Exception e){
				logger.debug("error:  "+pfcs.getProtocolFormId());
				e.printStackTrace();
			}
		}
		
		
		
		return xmlForEachProtocol;
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

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required = true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
	}

}
