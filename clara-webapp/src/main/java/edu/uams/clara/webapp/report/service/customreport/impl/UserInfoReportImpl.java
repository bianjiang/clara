package edu.uams.clara.webapp.report.service.customreport.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.CitiMemberDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserCOIDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserCOI;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class UserInfoReportImpl  extends CustomReportService{
	private final static Logger logger = LoggerFactory.getLogger(UserInfoReportImpl.class);
	
	private CitiMemberDao citiMemberDao = new CitiMemberDao();
	private UserDao userDao = new UserDao();
	private UserCOIDao userCOIDao = new UserCOIDao();
	
	private String getExipredCitiTrainingRecords(User user) {
		List<CitiMember> cms = citiMemberDao.listCitiMemberByUser(user);
		String reportItemXml = "";
		String tempItemXml = "";
		for (CitiMember cm : cms) {
			try {
				String expDateStr = cm.getDateCompletionExpires().split(" ")[0];
				if(expDateStr.isEmpty()){
					continue;
				}
				SimpleDateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
				Date expDate = sdf.parse(expDateStr);
				//Date expDate = DateFormat.getInstance().parse(expDateStr);
				if (expDate.before(new Date())) {
					// expired
					long expDates = 1+((new Date()).getTime()-expDate.getTime())/ (24 * 60 * 60 * 1000);
					tempItemXml += "<item><![CDATA["+"Completion Report Number: "+cm.getCompletionReportNumber()+"<br/>"
									+"Expired Dates: "+expDates+" Days"
									+"]]></item>";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		if(!tempItemXml.isEmpty()){
			reportItemXml += "<field id=\"citiexpired\"><list>";
			reportItemXml +=tempItemXml;
			reportItemXml += "</list></field>";
		}
		return reportItemXml;

	}

	private String getExpiredCOI(User user){
		List<UserCOI> cois = userCOIDao.getUserCOIBySAP(user.getPerson().getSap());
		String reportItemXml = "";
		String tempItemXml = "";
		
		for(UserCOI coi:cois){
			try{
				
				Date expDate = coi.getExpirationDate();
				if(expDate.before(new Date())){
					//expired
					// expired
					long expDates = 1+((new Date()).getTime()-expDate.getTime())/ (24 * 60 * 60 * 1000);
					tempItemXml += "<item><![CDATA["+"Disclosure Name: "+coi.getDisclosureName()+"<br/>"
									+"Expired Dates: "+expDates+" Days"
									+"]]></item>";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!tempItemXml.isEmpty()){
			reportItemXml += "<field id=\"coiexpired\"><list>";
			reportItemXml +=tempItemXml;
			reportItemXml += "</list></field>";
		}
		return reportItemXml;
	}
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		
		String finalResultXml = "<report-results>";
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"user\" desc=\"User\" hidden=\"false\" />";
		finalResultXml += "<field id=\"sap\" desc=\"SAP ID\" hidden=\"false\" />";
		finalResultXml += "<field id=\"email\" desc=\"Email\" hidden=\"false\" />";
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		List<String> expCases = Lists.newArrayList();
			
		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportCriteriaField = new ReportFieldTemplate();
			
			try {
				reportCriteriaField = objectMapper.readValue(rc.getCriteria(), ReportFieldTemplate.class);
				
				expCases.add(reportCriteriaField.getFieldIdentifier());
				finalResultXml += "<field id=\""+reportCriteriaField.getFieldIdentifier()+"\" desc=\""+reportCriteriaField.getFieldDisplayName()+"\" hidden=\"false\" />";
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";

		List<User> users = userDao.findAll();
		/*List<User> users = Lists.newArrayList();
		users.add(userDao.findById(1));*/
		for (User user : users) {
			String expiredInfo ="";
			for (String expCase : expCases) {
				switch (expCase) {
				case "citiexpired":
					expiredInfo += getExipredCitiTrainingRecords(user);
					break;
				case "coiexpired":
					expiredInfo +=getExpiredCOI(user);
					break;
				default:
					break;
				}
			}
			if(!expiredInfo.isEmpty()){
				finalResultXml += "<report-item>";
				finalResultXml += "<field id=\"user\">";
				finalResultXml += user.getPerson().getFirstname()+", "+user.getPerson().getLastname();
				finalResultXml += "</field>";
				finalResultXml += "<field id=\"sap\">";
				finalResultXml += user.getPerson().getSap();
				finalResultXml += "</field>";
				finalResultXml += "<field id=\"email\">";
				finalResultXml += user.getPerson().getEmail();
				finalResultXml += "</field>";
				finalResultXml +=expiredInfo;
				
				finalResultXml += "</report-item>";
			}
		}
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		//logger.debug(finalResultXml);
		if(finalResultXml.contains("&")){
			finalResultXml=finalResultXml.replaceAll("&", "&amp;");
		}
		return finalResultXml;
	}

	public CitiMemberDao getCitiMemberDao() {
		return citiMemberDao;
	}

	@Autowired(required = true)
	public void setCitiMemberDao(CitiMemberDao citiMemberDao) {
		this.citiMemberDao = citiMemberDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserCOIDao getUserCOIDao() {
		return userCOIDao;
	}

	@Autowired(required = true)
	public void setUserCOIDao(UserCOIDao userCOIDao) {
		this.userCOIDao = userCOIDao;
	}

}
