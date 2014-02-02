package edu.uams.clara.webapp.report.web;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.service.ProtocolFormDetailContentService;
import edu.uams.clara.webapp.report.service.ReportCriteriaService;

@Controller
public class ReportController {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ReportController.class);

	private ProtocolDao protocolDao;
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ReportCriteriaService reportCriteriaService;
	
	private ProtocolFormDetailContentService protocolFormDetailContentService;
	
	@RequestMapping(value="/reports")
	public String getReportDashboard(ModelMap modelMap){
		modelMap.put("user", (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		return "reports/index";
	}
	
	@RequestMapping(value = "/report/timeline")
	public String getTimelineReport(@RequestParam("type") String type, 
			@RequestParam("userId") long userId, 
			@RequestParam("startDate") Date startDate,
			@RequestParam("endDate") Date endDate,
			ModelMap modelMap){
		User user = new User();
		user.setId(userId);
		return "report-timeline";
	}
	
	/*
	@RequestMapping(value = "/report/pi")
	public String getPiReport(@RequestParam("xml") String reportCriteria){
		
		ProtocolReportSearchCritieria prsc = null;
		
		try{
			prsc = reportCriteriaService.getProtocolReportSearchCritieria(reportCriteria);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String resultXml = "<report detailed=\""+ prsc.getDetailedOrNot() +"\">";

		if (!prsc.getStudyType().isEmpty()){
			List<ProtocolForm> pfLst = protocolDao.listProtocolFormByReportSearchCriteriaAndStartAndEndDate(prsc, ProtocolFormStatusEnum.IRB_AGENDA_ASSIGNED);
			
			resultXml += "<studies type=\""+ prsc.getStudyType() +"\" count=\""+ pfLst.size() +"\">";
			resultXml += protocolFormDetailContentService.getFormDetailInfo(pfLst);
			
			Set<Committee> involvedCommittees = new HashSet<Committee>();
			for (ProtocolForm pf : pfLst){
				int studyCount = 0;
				int durationCount = 0;
				List<Integer> durationSet = new ArrayList<Integer>();
				List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatusLst = protocolFormCommitteeStatusDao.listLatestByProtocolFormId(pf.getId());
				
				for (ProtocolFormCommitteeStatus pfcs : protocolFormCommitteeStatusLst){
					involvedCommittees.add(pfcs.getCommittee());
				}
				
				int duration = 0;
				for (Committee committee : involvedCommittees){
					List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatusByCommitteeLst = protocolFormCommitteeStatusDao.listAllByCommitteeAndProtocolFormId(committee, pf.getId());
					
					if (protocolFormCommitteeStatusByCommitteeLst != null){
						studyCount += 1;
					}
					
					for (ProtocolFormCommitteeStatus pfcStatus : protocolFormCommitteeStatusByCommitteeLst){
						int indx = protocolFormCommitteeStatusByCommitteeLst.indexOf(pfcStatus);
						Date start = null;
						Date end = null;
						
						start = protocolFormCommitteeStatusByCommitteeLst.get(indx).getModified();
						
						if (indx < protocolFormCommitteeStatusByCommitteeLst.size()-1){
							end = protocolFormCommitteeStatusByCommitteeLst.get(indx+1).getModified();
						} else {
							end = start;
						}
						
						DateTime startDateTime = new DateTime(start);
						DateTime endDateTime = new DateTime(end);

						duration +=	Days.daysBetween(startDateTime, endDateTime).getDays();
						durationSet.add(duration);
					}
					durationCount = duration;
					
					int max = (Integer) Collections.max(durationSet);
					int min = (Integer) Collections.min(durationSet);
					int median = 0;
					int average = durationCount/studyCount;
					
					if (durationSet.size() % 2 == 1){
						int index = (int) durationSet.size()/2;
						median = durationSet.get(index);			
					} else {
						int index1 = (int) durationSet.size()/2;
						int index2 = index1 - 1;
						
						median = (durationSet.get(index1) + durationSet.get(index2))/2;
					}
					resultXml += "<involved-committee name=\""+ committee.getDescription() +"\" average=\""+ average +"\"  median=\""+ median +"\" min=\""+ min +"\" max=\""+ max +"\" />";
				}
			}
			resultXml += "</studies>";
		}
		
		
		resultXml += "</report>";
		

		
		logger.debug("resultXml: " + resultXml);

		return "report-pi";
	}
	*/
	
	@RequestMapping(value = "/report/department", method=RequestMethod.POST)
	public String getDepartmentReport(@RequestParam("xml") String xml,
			ModelMap modelMap){

		return "report-department";
	}


	
	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ReportCriteriaService getReportCriteriaService() {
		return reportCriteriaService;
	}
	
	@Autowired(required = true)
	public void setReportCriteriaService(ReportCriteriaService reportCriteriaService) {
		this.reportCriteriaService = reportCriteriaService;
	}

	public ProtocolFormDetailContentService getProtocolFormDetailContentService() {
		return protocolFormDetailContentService;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDetailContentService(
			ProtocolFormDetailContentService protocolFormDetailContentService) {
		this.protocolFormDetailContentService = protocolFormDetailContentService;
	}

}
