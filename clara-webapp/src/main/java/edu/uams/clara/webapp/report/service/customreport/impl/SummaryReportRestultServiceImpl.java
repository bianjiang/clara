package edu.uams.clara.webapp.report.service.customreport.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.report.domain.CommitteeActions;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class SummaryReportRestultServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(SummaryReportRestultServiceImpl.class);
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	private AgendaStatusDao agendaStatusDao;
	private AgendaItemDao agendaItemDao;
	private CommitteeActions committeeActions = new CommitteeActions();
	
	private long roundUp(long a,long b) {
		return(((double)a/(double)b)>(a/b)?a/b+1:a/b);
	}
	
	private int roundUpInt(int a,int b) {
		return(((double)a/(double)b)>(a/b)?a/b+1:a/b);
	}
	
	private Map<String, Map<String,Long>> getTimeInEachQueueByProtocolId(Set<ProtocolForm> pfms,
			List<Committee> committees) {
		Map<String, Map<String,Long>> resultMap = Maps.newTreeMap();
		Map<String, Long> finalTimeMap = Maps.newTreeMap();
		Map<String, List<Long>> finalTimeListMap = Maps.newTreeMap();
		Map<String, Long> finalCountMap = Maps.newTreeMap();
		
		long totalTimeForConsentLegalReview = 0;
		long countForConsentLegalReview =0;
		
		for (ProtocolForm pf : pfms) {
			if(pf.getId()!=pf.getParent().getId()){
				continue;
			}
			Map<String, Long> timeMap = Maps.newTreeMap();
			Map<String, Long> countMap = Maps.newTreeMap();
			try{
			for (Committee committee : committees) {
				long count=0;
				long totalTime=0;
				long startTime = 0;
				long endTime = 0;
				List<ProtocolFormCommitteeStatus> pfcss = protocolFormCommitteeStatusDao
						.listAllByCommitteeAndProtocolFormId(committee,
								pf.getFormId());
				for (int i = 0; i < pfcss.size(); i++) {
					ProtocolFormCommitteeStatus pfcs = pfcss.get(i);
					List<ProtocolFormCommitteeStatusEnum> startActions = committeeActions.getStartCommitteeStatusMap()
							.get(committee);
					List<ProtocolFormCommitteeStatusEnum> endActions = committeeActions.getEndCommitteeStatusMap()
							.get(committee);
					if (startActions == null) {
						continue;
					}
					if (startActions.contains(pfcs
							.getProtocolFormCommitteeStatus())) {
						startTime = pfcs.getModified().getTime();
						
						//chage the start time to agenda approved
						if(committee.equals(Committee.IRB_REVIEWER)){
							try{
								AgendaStatus agendaStatus = null;
								if(agendaItemDao.listbyProtocolFormId(pfcs.getProtocolFormId()).size()>1){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);
								}else{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
								}
								startTime = agendaStatus.getModified().getTime();

							}catch(Exception e){
								startTime=0;
							}
						}
						
						
					} else if (startTime > 0
							&& endActions.contains(pfcs
									.getProtocolFormCommitteeStatus())) {
						endTime = pfcs.getModified().getTime();
						if(committee.equals(Committee.IRB_OFFICE)&&pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED)){
							try{
								AgendaStatus agendaStatus = null;
								if(agendaItemDao.listbyProtocolFormId(pfcs.getProtocolFormId()).size()>1){
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormIdAndAgendaItemStatus(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId(),AgendaItemStatus.REMOVED);
								}else{
									agendaStatus = agendaStatusDao
											.getAgendaStatusByAgendaStatusAndProtocolFormId(
													AgendaStatusEnum.AGENDA_APPROVED,
													pfcs.getProtocolFormId());
								}
								endTime = agendaStatus.getModified().getTime();
							}catch(Exception e){
								e.printStackTrace();
							if(i!=(pfcss.size()-1)){
								if(pfcss.get(i+1).getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT)){
									endTime=0;
									continue;
								}
							}
							endTime = new Date().getTime();
							}
						}
						
						if(committee.equals(Committee.COVERAGE_REVIEW)&&pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW)){
							
							long startTimeForConsentLegal = pfcs.getModified().getTime();
							long endTimeForConsentLegal =new Date().getTime();
							if(i<pfcss.size()-1){
							 endTimeForConsentLegal = pfcss.get(i+1).getModified().getTime();
							}
							
							totalTimeForConsentLegalReview+= endTimeForConsentLegal - startTimeForConsentLegal;
							countForConsentLegalReview++;
						}
						
					} 
					
					if (startTime > 0 && endTime == 0
							&& i == (pfcss.size() - 1)) {
						endTime = new Date().getTime();
					}
					

					if (startTime > 0 && endTime > 0) {
						if(count==0){
						count++;
						}
						totalTime += endTime - startTime;
						startTime = 0;
						endTime = 0;

					}
				}
				if (totalTime > 0) {
					totalTime = 1 + totalTime / (24 * 60 * 60 * 1000);
					countMap.put(committee.getDescription(), count);
					timeMap.put(committee.getDescription(), totalTime);
					/*if(totalTime ==35){
						logger.debug(pf.getProtocol().getId()+"!!!");
					}*/
				}
				
			}
			if(totalTimeForConsentLegalReview>0){
				
				totalTimeForConsentLegalReview = 1 + totalTimeForConsentLegalReview / (24 * 60 * 60 * 1000);
				countMap.put(Committee.PROTOCOL_LEGAL_REVIEW.getDescription(), countForConsentLegalReview);
				timeMap.put(Committee.PROTOCOL_LEGAL_REVIEW.getDescription(), totalTimeForConsentLegalReview);
			}
			
			for (Entry<String, Long> values : timeMap.entrySet()) {
				String key = values.getKey();
				if(finalTimeMap.containsKey(key)){
					List<Long> timeList = finalTimeListMap.get(key);
					timeList.add(values.getValue());
					finalTimeListMap.put(key, timeList);
					
					
					long tempTimeForQueue = finalTimeMap.get(key)+(values.getValue());
					finalTimeMap.remove(key);
					finalTimeMap.put(key,tempTimeForQueue );
					long count = finalCountMap.get(key)+countMap.get(key);
					finalCountMap.put(key, count);
				}
				else{
					List<Long> timeList = Lists.newArrayList();
					timeList.add(values.getValue());
					finalTimeListMap.put(key, timeList);
					
					finalTimeMap.put(key, values.getValue());
					finalCountMap.put(key, countMap.get(key));
				}
			}
			}catch(Exception e){
			}
			
		}
		
		for(Entry<String, List<Long>> values:finalTimeListMap.entrySet()){
			String key = values.getKey();
			List<Long> timeList = finalTimeListMap.get(key);
			Collections.sort(timeList);
			/*if(key==Committee.IRB_OFFICE.getDescription() ){
				for(long tee : timeList){
					logger.debug(tee+"");
				}
			
			}*/
			int medianIndex= timeList.size()/2;
			int quterIndex= timeList.size()/4;
			int quter3Index= timeList.size()*3/4;
			Map<String, Long> resultForEachCommittee = Maps.newHashMap();
			long meanTime=0;
			if(finalCountMap.get(key)>0){
				if((finalTimeMap.get(key)%finalCountMap.get(key))==0){
					meanTime =(finalTimeMap.get(key)/finalCountMap.get(key));
				}else{
					meanTime =(1+(finalTimeMap.get(key)/finalCountMap.get(key)));
				}
			 
			}
			
			//Calculate Median Value
			long medianValue = 0;
			if(timeList.size()%2!=0){
				medianIndex= (timeList.size()-1)/2;
				medianValue = timeList.get(medianIndex);
			}else{
				medianValue = roundUp(timeList.get(medianIndex)+timeList.get(medianIndex-1),2);
			}
			
			resultForEachCommittee.put("min", timeList.get(0));
			resultForEachCommittee.put("max", timeList.get(timeList.size()-1));
			resultForEachCommittee.put("median", medianValue);
			resultForEachCommittee.put("quter", timeList.get(quterIndex));
			resultForEachCommittee.put("3quter", timeList.get(quter3Index));
			resultForEachCommittee.put("mean", meanTime);
				
			resultMap.put(key, resultForEachCommittee);
		}
		
		
		return resultMap;
	}
	
	private Map<String,Integer> getSummaryTimeFromSubmissionToComplete(Set<ProtocolForm> pfms){
		List<Integer> tiemForSubmission =Lists.newArrayList();
		Map<String,Integer> results = Maps.newHashMap();
		long totalTime = 0;
		int totalNumber = 0;
		int singleTime =0;
		int cancelledNumber =0;
		for(ProtocolForm pf:pfms){
			if(pf.getId()!=pf.getParent().getId()){
				continue;
			}
			try{
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());
			
			long startTime = 0;
			long endTime = 0;
			for(int i=0;i<pfss.size();i++){
				ProtocolFormStatus pfs = pfss.get(i);
				if(committeeActions.getDraftFormStatus().contains(pfs.getProtocolFormStatus())){
					continue;
				}else{
				startTime = pfs.getModified().getTime();
				break;
				}
			}
			if(committeeActions.getCompleteFormStatus().contains(pfss.get(pfss.size()-1).getProtocolFormStatus())){
				
				endTime =  pfss.get(pfss.size()-1).getModified().getTime();
				totalNumber++;
				singleTime =(int) (1+(endTime - startTime)/(24*60*60*1000));
				totalTime+=singleTime;
				tiemForSubmission.add(singleTime);
			}else if(pfss.get(pfss.size()-1).getProtocolFormStatus().equals(ProtocolFormStatusEnum.CANCELLED)){
				cancelledNumber++;
			}}
			catch(Exception e){
				
			}
		}
		
		Collections.sort(tiemForSubmission);
		int medianIndex= tiemForSubmission.size()/2;
		int quterIndex= tiemForSubmission.size()/4;
		int quter3Index= tiemForSubmission.size()*3/4;
		
		int meanTime =0;

		int medianValue = 0;
		
		if(totalNumber>0){
			if(totalTime%totalNumber==0){
				meanTime=(int) (totalTime/totalNumber);
			}else{
			meanTime=(int) (1+(totalTime/totalNumber));
			}
		}
		if(tiemForSubmission.size()>0){
			//Calculate Median Value
			if(tiemForSubmission.size()%2!=0){
				medianIndex= (tiemForSubmission.size()-1)/2;
				medianValue = tiemForSubmission.get(medianIndex);
			}else{
				medianValue = roundUpInt(tiemForSubmission.get(medianIndex)+tiemForSubmission.get(medianIndex-1),2);
			}
		results.put("min", tiemForSubmission.get(0));
		results.put("max", tiemForSubmission.get(tiemForSubmission.size()-1));
		results.put("median", medianValue);
		results.put("quter", tiemForSubmission.get(quterIndex));
		results.put("3quter", tiemForSubmission.get(quter3Index));
		results.put("mean", meanTime);
		
		}else{
			results.put("min", 0);
			results.put("max", 0);
			results.put("median", 0);
			results.put("quter", 0);
			results.put("3quter", 0);
			results.put("mean", 0);
		}
		results.put("completedNum", tiemForSubmission.size());
		results.put("cancelledNum", cancelledNumber);
		
		return results;
	}
	
	private Map<String,Integer> getSummaryTimeFromCreateToSubmission(Set<ProtocolForm> pfms ){
		List<Integer> tiemForSubmission =Lists.newArrayList();
		Map<String,Integer> results = Maps.newHashMap();
		
		long totalTime = 0;
		int totalNumber = 0;
		int singleTime =0;
		int cancelBeforeSubmit = 0;
		int totalCreated = pfms.size();
		for(ProtocolForm pf:pfms){
			if(pf.getId()!=pf.getParent().getId()){
				continue;
			}
			List<ProtocolFormStatus> pfss = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(pf.getFormId());
			long startTime = 0;
			long endTime = 0;
			
			List<ProtocolFormStatusEnum> pfssEms = Lists.newArrayList();
			for(int i=0;i<pfss.size();i++){
				pfssEms.add(pfss.get(i).getProtocolFormStatus());
			}
			for(int i=0;i<pfss.size();i++){
				if(pfssEms.contains(ProtocolFormStatusEnum.CANCELLED)&&committeeActions.getDraftFormStatus().contains(pfss.get(pfss.size()-2).getProtocolFormStatus())){
					cancelBeforeSubmit++;
					break;
				}
				ProtocolFormStatus pfs = pfss.get(i);
				if (pfs.getProtocolFormStatus().equals(ProtocolFormStatusEnum.DRAFT)&&startTime==0) {
					startTime = pfs.getModified().getTime();
				}else if (!committeeActions.getDraftFormStatus().contains(pfs.getProtocolFormStatus())&&startTime > 0 && endTime==0) {
					endTime = pfs.getModified().getTime();
				} 
				
				if (startTime > 0 && endTime > 0) {
					totalNumber++;
					singleTime =(int) (1+(endTime - startTime)/(24*60*60*1000));
					totalTime+=singleTime;
					tiemForSubmission.add(singleTime);
					if (totalTime < 0) {
						//logger.debug(protocolFormId + "#######" + totalTime);
					}
					break;
				}
				
				
			}
		}
		
		Collections.sort(tiemForSubmission);
		int medianIndex= tiemForSubmission.size()/2;
		int quterIndex= tiemForSubmission.size()/4;
		int quter3Index= tiemForSubmission.size()*3/4;
		int meanTime =0;
		if(totalNumber>0){
			if(totalTime%totalNumber==0){
				meanTime=(int) (totalTime/totalNumber);
			}else{
			meanTime=(int) (1+(totalTime/totalNumber));
			}
		}
		if(tiemForSubmission.size()>0){
			int medianValue = 0;
			//Calculate Median Value
			if(tiemForSubmission.size()%2!=0){
				medianIndex= (tiemForSubmission.size()-1)/2;
				medianValue = tiemForSubmission.get(medianIndex);
			}else{
				medianValue = roundUpInt(tiemForSubmission.get(medianIndex)+tiemForSubmission.get(medianIndex-1),2);
			}			
		results.put("min", tiemForSubmission.get(0));
		results.put("max", tiemForSubmission.get(tiemForSubmission.size()-1));
		results.put("median",medianValue);
		results.put("quter", tiemForSubmission.get(quterIndex));
		results.put("3quter", tiemForSubmission.get(quter3Index));
		results.put("mean", meanTime);
		}else{
			results.put("min", 0);
			results.put("max", 0);
			results.put("median", 0);
			results.put("quter", 0);
			results.put("3quter", 0);
			results.put("mean", 0);	
		}
		results.put("createdNum", totalCreated);
		results.put("submittedNum", tiemForSubmission.size());
		results.put("cancelBeforeSubmit", cancelBeforeSubmit);
		return results;
	}
	
	private String getTimeInReviewSummary(ReportTemplate reportTemplate,List<Long> protocolIds,String summaryTableXml){
		
		Set<ProtocolForm> pfms = Sets.newHashSet();
		for(long protocolId :protocolIds){
			try{
				pfms.addAll(protocolFormDao.listProtocolFormsByProtocolIdAndProtocolFormType(protocolId, ProtocolFormType.NEW_SUBMISSION));
			}catch(Exception e){
				
			}
		}
		List<Committee> committees = Arrays.asList(Committee.values());
		Map<String,Map<String,Long>> averageTimeInQueueSummary = getTimeInEachQueueByProtocolId(pfms,committees);
		Map<String,Integer> createToSubmissionSummary = getSummaryTimeFromCreateToSubmission(pfms);
		Map<String,Integer> submissionToCompleteSummary = getSummaryTimeFromSubmissionToComplete(pfms);
		int submittedNumber = createToSubmissionSummary.get("submittedNum");
		int createdNumber = createToSubmissionSummary.get("createdNum");
		int completedNumber = submissionToCompleteSummary.get("completedNum");
		int cancelledNumberBeforeSubmit = createToSubmissionSummary.get("cancelBeforeSubmit");
		int cancelledAfterSubmitNumber = submissionToCompleteSummary.get("cancelledNum")-cancelledNumberBeforeSubmit;
		int studyinreview = submittedNumber-completedNumber-cancelledAfterSubmitNumber;
		
		int createdToSubmissionMin = createToSubmissionSummary.get("min");
		int createdToSubmissionMax = createToSubmissionSummary.get("max");
		int createdToSubmissionMean = createToSubmissionSummary.get("mean");
		int createdToSubmissionMedian = createToSubmissionSummary.get("median");
		int createdToSubmissionQuter = createToSubmissionSummary.get("quter");
		int createdToSubmissionQuter3 = createToSubmissionSummary.get("3quter");
		
		int submissionToCompleteMin = submissionToCompleteSummary.get("min");
		int submissionToCompleteMax = submissionToCompleteSummary.get("max");
		int submissionToCompleteMean = submissionToCompleteSummary.get("mean");
		int submissionToCompleteMedian = submissionToCompleteSummary.get("median");
		int submissionToCompleteQuter = submissionToCompleteSummary.get("quter");
		int submissionToCompleteQuter3 = submissionToCompleteSummary.get("3quter");
		
		String finalResultXml = "<report-results>";
		finalResultXml += summaryTableXml;
		
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\" desc=\""+ "" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "totalnumber" +"\" desc=\""+ "Total Number" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "</fields>";
		
		finalResultXml += "<report-items>";
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Number of studies created";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "totalnumber" +"\">";
		finalResultXml += createdNumber;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";
		
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Number of studies submitted";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "totalnumber" +"\">";
		finalResultXml += submittedNumber;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";

		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Number of studies completed";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "totalnumber" +"\">";
		finalResultXml += completedNumber;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";
		
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Number of submitted studies in review";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "totalnumber" +"\">";
		finalResultXml += studyinreview;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";
		
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Number of studies cancelled Before Submission";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "totalnumber" +"\">";
		finalResultXml += cancelledNumberBeforeSubmit;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";

		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Number of studies cancelled After Submission";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "totalnumber" +"\">";
		finalResultXml += cancelledAfterSubmitNumber;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";

		finalResultXml += "</report-items>";
		
		finalResultXml += "</report-result>";
		
		
		
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ "" +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\" desc=\""+ "" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "meantime" +"\" desc=\""+ "Mean Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "25%Time" +"\" desc=\""+ "25% Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "mediantime" +"\" desc=\""+ "Median Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "75%Time" +"\" desc=\""+ "75% Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "range" +"\" desc=\""+ "Range(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "</fields>";
		
		finalResultXml += "<report-items>";
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Total time from created to submitted";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "meantime" +"\">";
		finalResultXml += createdToSubmissionMean;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "25%Time" +"\">";
		finalResultXml += createdToSubmissionQuter;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "mediantime" +"\">";
		finalResultXml += createdToSubmissionMedian;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "75%Time" +"\">";
		finalResultXml += createdToSubmissionQuter3;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "range" +"\">";
		finalResultXml += createdToSubmissionMin+"~"+createdToSubmissionMax;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";

		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "summaryfield" +"\">";
		finalResultXml += "Total time from submitted to completed";
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "meantime" +"\">";
		finalResultXml += submissionToCompleteMean;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "25%Time" +"\">";
		finalResultXml += submissionToCompleteQuter;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "mediantime" +"\">";
		finalResultXml += submissionToCompleteMedian;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "75%Time" +"\">";
		finalResultXml += submissionToCompleteQuter3;
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "range" +"\">";
		finalResultXml += submissionToCompleteMin+"~"+submissionToCompleteMax;
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";

		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ "Time in Committees" +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\""+ "queuename" +"\" desc=\""+ "Queue" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "meantime" +"\" desc=\""+ "Mean Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "25%Time" +"\" desc=\""+ "25% Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "mediantime" +"\" desc=\""+ "Median Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "75%Time" +"\" desc=\""+ "75% Time(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "<field id=\""+ "range" +"\" desc=\""+ "Range(days)" +"\" hidden=\""+ "false" +"\" />";
		finalResultXml += "</fields>";
		
		finalResultXml += "<report-items>";
		for(Entry<String,Map<String,Long>> value : averageTimeInQueueSummary.entrySet()){
		finalResultXml += "<report-item>";
		finalResultXml += "<field id=\""+ "queuename" +"\">";
		finalResultXml += value.getKey();
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "meantime" +"\">";
		finalResultXml += value.getValue().get("mean");
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "25%Time" +"\">";
		finalResultXml += value.getValue().get("quter");
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "mediantime" +"\">";
		finalResultXml += value.getValue().get("median");
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "75%Time" +"\">";
		finalResultXml += value.getValue().get("3quter");
		finalResultXml += "</field>";
		finalResultXml += "<field id=\""+ "range" +"\">";
		finalResultXml += value.getValue().get("min")+"~"+value.getValue().get("max");
		finalResultXml += "</field>";
		finalResultXml += "</report-item>";
		}
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		
		
		finalResultXml += "</report-results>";
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		return finalResultXml;
	}
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		Map<String, String> fieldsRealXPathMap = Maps.newHashMap();
		
		Map<String,String> queryCriteriasValueMap = Maps.newHashMap();
		
		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportCriteriaField = new ReportFieldTemplate();
			
			try {
				reportCriteriaField = objectMapper.readValue(rc.getCriteria(), ReportFieldTemplate.class);
				
				String fieldIdentifier = reportCriteriaField.getFieldIdentifier();
				
				String value = reportCriteriaField.getValue();
				
				if(reportCriteriaField.getOperator().toString().equals("AFTER")){
					queryCriteriasValueMap.put(reportCriteriaField.getFieldDisplayName(), "AFTER: "+reportCriteriaField.getDisplayValue());
				}else if(reportCriteriaField.getOperator().toString().equals("BEFORE")){
					queryCriteriasValueMap.put(reportCriteriaField.getFieldDisplayName(), "BEFORE: "+reportCriteriaField.getDisplayValue());
				}else{
					queryCriteriasValueMap.put(reportCriteriaField.getFieldDisplayName(), reportCriteriaField.getDisplayValue());
				}
				if (value != null && !value.isEmpty()) {
					String realXpath = "";
					
					if(reportCriteriaField.getOperator().toString().equals("AFTER")||reportCriteriaField.getOperator().toString().equals("BEFORE")){
							realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					}else if(reportCriteriaField.getOperator().toString().equals("BETWEEN")){
						realXpath =reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(0,value.toUpperCase().indexOf(",")) +"'");
						realXpath = realXpath.replace("{operator}", ">");
						realXpath = realXpath +" AND "+reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase().substring(value.toUpperCase().indexOf(",")+1,value.length()) +"'");;
						realXpath = realXpath.replace("{operator}", "<");
						
					}
					else{
						
						if(value.contains("|")){
							String[] values = value.split("\\|");
							realXpath += "(";
						
							for(int i=0;i<values.length;i++){
								
								if(i>0){
									realXpath+=" OR ";
								}
								if (reportCriteriaField.getNodeXPath().contains(".exist") || reportCriteriaField.getNodeXPath().contains(".value")) {
									if(value.equals("=1")||value.equals("=0")){
										realXpath += reportCriteriaField.getNodeXPath().replace("{value}", values[i].toUpperCase());
									}else{
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", "\""+ values[i].toUpperCase() +"\"");
									}
								} else if(values[i].contains("'")){
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", values[i].toUpperCase());
								}
								else{
									realXpath += reportCriteriaField.getNodeXPath().replace("{value}", "'"+ values[i].toUpperCase() +"'");
								}
							}
							realXpath += ")";
						}else{
						if (reportCriteriaField.getNodeXPath().contains(".exist") || reportCriteriaField.getNodeXPath().contains(".value")) {
							if(value.equals("=1")||value.equals("=0")){
								realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value.toUpperCase());
							}else{
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "\""+ value.toUpperCase() +"\"");
							};
					} else if(value.contains("'")){
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value.toUpperCase());
					}else if(value.toUpperCase().equals("IN")||value.toUpperCase().equals("NOT IN")){
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", value);
					}
					else{
						realXpath = reportCriteriaField.getNodeXPath().replace("{value}", "'"+ value.toUpperCase() +"'");
					}}
						
						
					}
					
					if(!reportCriteriaField.getOperator().toString().equals("BETWEEN")){
						realXpath = realXpath.replace("{operator}", reportCriteriaField.getOperator().getRealOperator());
					}
					fieldsRealXPathMap.put("{" + fieldIdentifier + ".search-xpath}" , realXpath);
					//fieldsRealXPathMap.put("{" + fieldIdentifier + ".report-xpath}", reportField.getReportableXPath());
				}
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		String rawQeury = generateRawQeury(reportTemplate, fieldsRealXPathMap);
		List<String> rawQueryResultSearchFields  =Lists.newArrayList();
		rawQueryResultSearchFields.add("protocolid");
		rawQeury = rawQeury.replace("{reportstatment}", generateReportStatement(reportTemplate,rawQueryResultSearchFields));
		
		String realQeury = fillMessage(rawQeury, fieldsRealXPathMap);

		logger.debug("real query: " + realQeury);
		List<Map> resultObjectLst = getReportResultDao().generateResult(
				realQeury);
		List<Long> protocolIds = Lists.newArrayList();
		for (Map rowObject : resultObjectLst) {
			long protocolId = Long.valueOf((String) rowObject
					.get("protocolId"));
			protocolIds.add(protocolId);
			
		}
		
		String summaryTableXml = generateSummaryCriteriaTable( reportTemplate,queryCriteriasValueMap);
		String finalResultXml= getTimeInReviewSummary(reportTemplate,protocolIds, summaryTableXml);
		return finalResultXml;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	@Autowired(required=true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	@Autowired(required=true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required=true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

}
