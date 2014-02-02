package edu.uams.clara.webapp.protocol.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormDetailContentService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolFormDetailContentServiceImpl implements ProtocolFormDetailContentService {
	private XmlProcessor xmlProcessor;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private List<String> needToShowResponseStatusList = Lists.newArrayList();{
		needToShowResponseStatusList.add(ProtocolFormStatusEnum.IRB_AGENDA_ASSIGNED.getDescription());
		needToShowResponseStatusList.add(ProtocolFormStatusEnum.PENDING_EXPEDITED_IRB_REVIEW.getDescription());
		needToShowResponseStatusList.add(ProtocolFormStatusEnum.PENDING_IRB_REVIEW_ASSIGNMENT.getDescription());
		needToShowResponseStatusList.add(ProtocolFormStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT.getDescription());
	}
	//Used in the dashboard to show extra information
	@Override
	public String getDetailContent(ProtocolForm protocolForm) {
		ProtocolFormType protocolFormType = protocolForm.getProtocolFormType();
		
		String protocolFormMetaData = protocolForm.getMetaDataXml();
		
		String detailXml = "<details>";
		
		List<String> values = null;
		switch(protocolFormType){
		case EMERGENCY_USE:
			try{
				values = xmlProcessor.listElementStringValuesByPath("/emergency-use/ieu-or-eu", protocolFormMetaData);
				
				String formType = (values!=null && !values.isEmpty())?values.get(0):"";
				
				if (formType.equals("intended-emergency-use")) formType = "Intended Emergency Use";
				if (formType.equals("emergency-use-follow-up-report")) formType = "Follow-up Emergency Use";
				
				detailXml += "<value name=\"Form Type\">" + formType + "</value>";
			} catch (Exception e){
				e.printStackTrace();
			}
			break;
		case NEW_SUBMISSION:
			try{
				values = xmlProcessor.listElementStringValuesByPath("/protocol/study-nature", protocolFormMetaData);
				
				String studyNature = (values!=null && !values.isEmpty())?values.get(0):"";
				
				detailXml += "<study-nature>" + studyNature + "</study-nature>";
			} catch (Exception e){
				e.printStackTrace();
			}
			break;
		case MODIFICATION:
			Set<String> pathList = Sets.newHashSet();
			pathList.add("/protocol/modification/to-modify-section/is-audit");
			pathList.add("/protocol/modification/to-modify-section/complete-budget-migration");
			pathList.add("/protocol/modification/to-modify-section/involve-change-in/budget-modified");
			pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure");
			pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy");
			pathList.add("/protocol/study-type");
			
			try{
				//values = xmlProcessor.listElementStringValuesByPath("/protocol/modification/to-modify-section/is-audit", protocolFormMetaData);
				
				//String studyNature = (values!=null && !values.isEmpty())?values.get(0):"";
				
				Map<String, List<String>> answerValues = getXmlProcessor().listElementStringValuesByPaths(pathList, protocolFormMetaData);
				
				String isAudit = (answerValues.get("/protocol/modification/to-modify-section/is-audit") != null && !answerValues.get("/protocol/modification/to-modify-section/is-audit").isEmpty())?answerValues.get("/protocol/modification/to-modify-section/is-audit").get(0):"";
				String budgetMigration = (answerValues.get("/protocol/modification/to-modify-section/complete-budget-migration") != null && !answerValues.get("/protocol/modification/to-modify-section/complete-budget-migration").isEmpty())?answerValues.get("/protocol/modification/to-modify-section/complete-budget-migration").get(0):"";
				String budgetModified = (answerValues.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified") != null && !answerValues.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified").isEmpty())?answerValues.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified").get(0):"";
				String procedureDeleted = (answerValues.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure") != null && !answerValues.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure").isEmpty())?answerValues.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure").get(0):"";
				String pharmacyDeleted = (answerValues.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy") != null && !answerValues.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy").isEmpty())?answerValues.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy").get(0):"";
				
				if (isAudit.equals("y")){
					detailXml += "<value name=\"Form Type\">Audit Response</value>";
				} else {
					if (budgetMigration.equals("y") || budgetModified.equals("y") || procedureDeleted.equals("y") || pharmacyDeleted.equals("y")) {
						detailXml += "<value name=\"Form Type\">Budget Modification</value>";
					}
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			String submissionType = xmlHandler.getSingleStringValueByXPath(protocolFormMetaData, "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/recent-motion");
			
			String formStatus = xmlHandler.getSingleStringValueByXPath(protocolFormMetaData, "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/status");
			
			if (needToShowResponseStatusList.contains(formStatus)) {
				if (submissionType.equals("Defer with minor contingencies") || submissionType.equals("Defer with minor contingencies by Expedited Review"))
					detailXml += "<value name=\"Submission Type\">Response to Minor Contingency</value>";
				
				if (submissionType.equals("Defer with major contingencies"))
					detailXml += "<value name=\"Submission Type\">Response to Major Contingency</value>";
			}
			
		} catch (Exception e) {
			//don't care
		}
		
		detailXml += "</details>";
		
		return detailXml;
	}
	
	@Override
	public String getFormDetailInfo(List<ProtocolForm> protocolFormLst) {
		String finalXml = "<details>";
		try{
			for (ProtocolForm protocolForm : protocolFormLst){
				List<String> titleLst = xmlProcessor.listElementStringValuesByPath("/protocol/title", protocolForm.getProtocol().getMetaDataXml());
				
				String title = titleLst.get(0);
				String department = xmlProcessor.getAttributeValueByPathAndAttributeName("/protocol/responsible-department", protocolForm.getProtocol().getMetaDataXml(), "deptdesc");
				
				Date startDate = protocolForm.getProtocol().getCreated();
				
				ProtocolFormStatus protocolFormStatus = protocolFormStatusDao.getProtocolFormStatusByFormIdAndProtocolFormStatus(protocolForm.getId(), ProtocolFormStatusEnum.IRB_AGENDA_ASSIGNED);
				
				Date endDate = protocolFormStatus.getModified();
				
				DateTime startDateTime = new DateTime(startDate);
				DateTime endDateTime = new DateTime(endDate);
				
				int totalDays = Days.daysBetween(startDateTime, endDateTime).getDays();
				
				finalXml += "<study id=\""+ protocolForm.getProtocol().getId() +"\" identifier=\""+ protocolForm.getProtocol().getProtocolIdentifier() +"\" title=\""+ title +"\" department=\""+ department +"\" submission-date=\""+ startDate +"\" total-days=\""+ totalDays +"\" />";
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		finalXml += "</details>";
		return finalXml;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}
	
	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}
}
