package edu.uams.clara.webapp.report.service.customreport.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class GatekeeperActionReportImpl  extends CustomReportService {
	private final static Logger logger = LoggerFactory
			.getLogger(GatekeeperActionReportImpl.class);

	private ProtocolDao protocolDao;
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private ReportFieldDao reportFieldDao;
	private AgendaStatusDao agendaStatusDao;
	private AgendaItemDao agendaItemDao;
	private CommitteeActions committeeactions = new CommitteeActions();
	private EntityManager em;
	private UserDao userDao;
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		
		//get result new submission form parent ids 
		List<BigInteger> protocolFormParentIds = generateFormParentIdsWithGatekeeper();
		
		List<Long> pfParentIdList = Lists.newArrayList();
		List<ProtocolForm> pfs = Lists.newArrayList();
		for(BigInteger pfid : protocolFormParentIds){
			ProtocolForm pf= protocolFormDao.findById(pfid.longValue());
			long parentId = pf.getParentFormId();
			if(!pfParentIdList.contains(parentId)){
				pfParentIdList.add(parentId);
				pfs.add(pf);
			}
		}
		
		
		String finalResultXml = "<report-results>";
		finalResultXml += "<report-result id =\"Gatekeeper Action Report\">";
		finalResultXml += "<title>";
		finalResultXml += "Protocol: Gatekeeper Action Report";
		finalResultXml += "</title>";
		finalResultXml += "</report-result>";
		
		List<Committee> committees =  getRequiredCommittees();
		
		int headlineCounter = 1;
		
		for(ProtocolForm pf :pfs){
			try{
				long pfId = pf.getParentFormId();
				long pid = pf.getProtocol().getId();
				finalResultXml +=getDetailInfoForEachProtocol(pfId,pid,committees,headlineCounter);
				headlineCounter++;
			}catch(Exception e){
				logger.debug("error: ProtofolFormId"+pf.getParentFormId());
				e.printStackTrace();
			}
		}
		
		//in case last section do not have 10 protocols
		if(headlineCounter%10!=1){
			finalResultXml += "</report-result>";
		}
		
		finalResultXml += "</report-results>";
		finalResultXml = finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml = finalResultXml.replace("null&lt;br&gt;", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		
		logger.debug(finalResultXml);
		return finalResultXml;
	}
	
	private List<BigInteger> generateFormParentIdsWithGatekeeper(){
		
		//String queryStatement = "select distinct parent_id, protocol_id from protocol_form where retired = 0 and protocol_form_type = 'NEW_SUBMISSION' and id in (select distinct protocol_form_id  from protocol_form_committee_status where committee = 'GATEKEEPER' and retired = 0) and protocol_id not in (select distinct protocol_id from protocol_status where retired = 0 and protocol_status = 'CANCELLED')";
		String queryStatement = "select protocol_form_id  from protocol_form_committee_status where committee = 'GATEKEEPER' and protocol_form_committee_status = 'IN_REVIEW' and retired = 0 and protocol_form_id in (select distinct id from protocol_form where retired = 0 and protocol_form_type = 'NEW_SUBMISSION' and protocol_id not in (select distinct protocol_id from protocol_status where retired = 0 and protocol_status = 'CANCELLED')) order by modified";
		
		Query query = em.createNativeQuery(queryStatement);
		
		List<BigInteger> protocolFormParentIds = (List<BigInteger>) query.getResultList();
		
		return protocolFormParentIds;
	}
	
	private List<Committee> getRequiredCommittees(){
		List<Committee> committees = Lists.newArrayList();
		committees.add(Committee.PHARMACY_REVIEW);
		committees.add(Committee.BUDGET_REVIEW);
		committees.add(Committee.PROTOCOL_LEGAL_REVIEW);
		committees.add(Committee.BIOSAFETY);
		committees.add(Committee.PRMC);
		committees.add(Committee.RADIATION_SAFETY);
		committees.add(Committee.COI);
		committees.add(Committee.MONITORING_REGULATORY_QA);
		committees.add(Committee.ACHRI);
		committees.add(Committee.CLINICAL_TRIALS_REVIEW);
		committees.add(Committee.IRB_ASSIGNER);
		committees.add(Committee.BUDGET_MANAGER);
		committees.add(Committee.REGULATORY_MANAGER);
		
		return committees;
		
	}
	
	
	
	private String getDetailInfoForEachProtocol(long protocolFormId,long protocolId, List<Committee> committees,int headlineCounter){
		
		String internalResult = "";
		List<ProtocolFormCommitteeStatus> gatekeeperPfcss = protocolFormCommitteeStatusDao
				.listAllByCommitteeAndProtocolFormId(Committee.GATEKEEPER,protocolFormId);
		
		List<ProtocolFormCommitteeStatusEnum> GatekeeperStartActions = committeeactions
				.getGateKeeperAssignedstartCommitteeStatusMap().get(Committee.GATEKEEPER);
		
		String GatekeeperStartTime = "";
		for(ProtocolFormCommitteeStatus pfcs: gatekeeperPfcss){
			if(GatekeeperStartActions.contains(pfcs.getProtocolFormCommitteeStatus())){
				GatekeeperStartTime =pfcs.getModifiedDateTime(); 
				break;
			}
		}
		
		Map<ProtocolFormCommitteeStatus,ProtocolFormCommitteeStatus> actionPairs = Maps.newHashMap();
		
		for (Committee committee : committees) {
			List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
					.listAllByCommitteeAndProtocolFormId(committee, protocolFormId);
			
			
			
			ProtocolFormCommitteeStatus startActionPfcs = null;
			ProtocolFormCommitteeStatus endActionPfcs = null;
			
			List<ProtocolFormCommitteeStatusEnum> startActions = committeeactions
					.getGateKeeperAssignedstartCommitteeStatusMap().get(committee);
			List<ProtocolFormCommitteeStatusEnum> endActions = committeeactions
					.getGateKeeperAssignedendCommitteeStatusMap().get(committee);
			
			for (int i = 0; i < pfcss.size(); i++) {
				ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
				
				//caused by gatekeeper
				if (startActionPfcs ==null && startActions.contains(pfcs.getProtocolFormCommitteeStatus())&&pfcs.getCausedByCommittee().equals(Committee.GATEKEEPER)) {
					startActionPfcs = pfcs;
				}else if(startActionPfcs!= null && endActions.contains(pfcs.getProtocolFormCommitteeStatus())){
					endActionPfcs = pfcs;
				}
				
				if (startActionPfcs!=null && endActionPfcs == null && i == (pfcss.size() - 1)) {
					actionPairs.put(startActionPfcs, null);
				}
				
				if(startActionPfcs!=null&&endActionPfcs!=null){
					actionPairs.put(startActionPfcs, endActionPfcs);
					startActionPfcs = null;
					endActionPfcs = null;
				}
			}
			
		}
		
		internalResult += getXmlForEachProtocol(protocolId,GatekeeperStartTime,actionPairs,headlineCounter);
		return internalResult;
		
	}
	
	
	private String getXmlForEachProtocol(long protocolId,String GatekeeperStartTime, Map<ProtocolFormCommitteeStatus,ProtocolFormCommitteeStatus> actionPairs, int headlineCounter) {
		String reportResultForOneProtocol = "";
		
		if(headlineCounter%10==1){
		reportResultForOneProtocol += "<report-result>";
		reportResultForOneProtocol += "<fields>";
		reportResultForOneProtocol += "<field id =\"protocolId\" desc=\"IRB #\" hidden=\"false\"/>";
		reportResultForOneProtocol += "<field id =\"beginByGatekeeper\" desc=\"Work Began by Gatekeeper\" hidden=\"false\"/>";
		reportResultForOneProtocol += "<field id =\"dateSentToCommittee\" desc=\"Date sent to Committees\" hidden=\"false\"/>";
		reportResultForOneProtocol += "<field id =\"committee\" desc=\"Committee\" hidden=\"false\"/>";
		reportResultForOneProtocol += "<field id =\"gatekeeper\" desc=\"Gatekeeper Name\" hidden=\"false\"/>";
		reportResultForOneProtocol += "<field id =\"committeeActionDate\" desc=\"Committee Outcome Action Date\" hidden=\"false\"/>";
		reportResultForOneProtocol += "<field id =\"committeeOutcome\" desc=\"Committee Outcome Action\" hidden=\"false\"/>";
		reportResultForOneProtocol += "</fields>";
		}
		
		//add gate keeper info
		reportResultForOneProtocol += "<report-items>";
		reportResultForOneProtocol += "<report-item>";
		reportResultForOneProtocol += "<field id=\"" + "protocolId" + "\">";
		reportResultForOneProtocol +="<![CDATA[<a target=\"_blank\" href=\""+this.getAppHost()+"/clara-webapp/protocols/"+protocolId+"/dashboard\">"+protocolId+"</a>]]>";
		reportResultForOneProtocol += "</field>";
		
		reportResultForOneProtocol += "<field id=\"" + "beginByGatekeeper" + "\">";
		reportResultForOneProtocol += GatekeeperStartTime;
		reportResultForOneProtocol += "</field>";

		
		boolean firstRow = true;
		for (ProtocolFormCommitteeStatus startPfcs : actionPairs.keySet()) {
			
			
			
			if(firstRow){
				firstRow = false;
			}else{
				reportResultForOneProtocol += "<report-item>";
				reportResultForOneProtocol += "<field id=\"" + "protocolId" + "\">";
				reportResultForOneProtocol +="";
				reportResultForOneProtocol += "</field>";
				
				reportResultForOneProtocol += "<field id=\"" + "beginByGatekeeper" + "\">";
				reportResultForOneProtocol += "";
				reportResultForOneProtocol += "</field>";
			}
			
			reportResultForOneProtocol += "<field id=\"" + "dateSentToCommittee" + "\">";
			reportResultForOneProtocol += startPfcs.getModifiedDateTime();
			reportResultForOneProtocol += "</field>";
			reportResultForOneProtocol += "<field id=\"" + "committee" + "\">";
			reportResultForOneProtocol += startPfcs.getCommitteeDescription();
			reportResultForOneProtocol += "</field>";
			
			reportResultForOneProtocol += "<field id=\"" + "gatekeeper" + "\">";
			
			try{
				long userId = startPfcs.getCausedByUserId();
				reportResultForOneProtocol += userDao.findById(userId).getPerson().getFullname();
			}catch(Exception e){
				reportResultForOneProtocol += "";
			}
			
			reportResultForOneProtocol += "</field>";
			
			if(actionPairs.get(startPfcs)!=null){
				reportResultForOneProtocol += "<field id=\"" + "committeeActionDate" + "\">";
				reportResultForOneProtocol += actionPairs.get(startPfcs).getModifiedDateTime();
				reportResultForOneProtocol += "</field>";
				reportResultForOneProtocol += "<field id=\"" + "committeeOutcome" + "\">";
				reportResultForOneProtocol += actionPairs.get(startPfcs).getProtocolFormCommitteeStatus().getDescription();
				reportResultForOneProtocol += "</field>";
			}else{
				reportResultForOneProtocol += "<field id=\"" + "committeeActionDate" + "\">";
				reportResultForOneProtocol += "";
				reportResultForOneProtocol += "</field>";
				
				reportResultForOneProtocol += "<field id=\"" + "committeeOutcome" + "\">";
				reportResultForOneProtocol +="";
				reportResultForOneProtocol += "</field>";
				
			
			}
			
			
			reportResultForOneProtocol += "</report-item>";
		}
		
		//in case only gate keeper row
		if(actionPairs.size()==0){
			reportResultForOneProtocol += "<field id=\"" + "dateSentToCommittee" + "\">";
			reportResultForOneProtocol +="";
			reportResultForOneProtocol += "</field>";
			reportResultForOneProtocol += "<field id=\"" + "committee" + "\">";
			reportResultForOneProtocol +="";
			reportResultForOneProtocol += "</field>";
			reportResultForOneProtocol += "<field id=\"" + "gatekeeper" + "\">";
			reportResultForOneProtocol +="";
			reportResultForOneProtocol += "</field>";
			
			reportResultForOneProtocol += "<field id=\"" + "committeeActionDate" + "\">";
			reportResultForOneProtocol += "";
			reportResultForOneProtocol += "</field>";
			reportResultForOneProtocol += "<field id=\"" + "committeeOutcome" + "\">";
			reportResultForOneProtocol +="";
			reportResultForOneProtocol += "</field>";
			
			
			reportResultForOneProtocol += "</report-item>";
		}
		
		reportResultForOneProtocol += "</report-items>";
		
		if(headlineCounter%10==0){
			reportResultForOneProtocol += "</report-result>";
		}
		return reportResultForOneProtocol;

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

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	@Autowired(required = true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}
	
	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

}
