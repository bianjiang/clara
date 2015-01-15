package edu.uams.clara.webapp.report.service.customreport.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.dao.department.DepartmentDao;
import edu.uams.clara.webapp.common.dao.department.SubDepartmentDao;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.report.dao.ReportFieldDao;
import edu.uams.clara.webapp.report.domain.ReportCriteria;
import edu.uams.clara.webapp.report.domain.ReportFieldTemplate;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class OverallWorkloadReportServiceImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(OverallWorkloadReportServiceImpl.class);
	private ReportFieldDao reportFieldDao;
	
	private ProtocolFormDao protocolFormDao;
	private ProtocolFormStatusDao protocolFormStatusDao;
	private CollegeDao collegeDao;
	private DepartmentDao departmentDao;
	private SubDepartmentDao subDepartmentDao;
	private UserDao userDao;
	private EntityManager em;
	
	private List<ProtocolFormStatusEnum> requiredActions = Lists.newArrayList();{
		requiredActions.add(ProtocolFormStatusEnum.IRB_APPROVED);
		requiredActions.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		requiredActions.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		requiredActions.add(ProtocolFormStatusEnum.DETERMINED_HUMAN_SUBJECT_RESEARCH);
		requiredActions.add(ProtocolFormStatusEnum.DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH);
	}

	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		List<ReportCriteria> criterias = reportTemplate.getReportCriterias();
		
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();
		
		String date1 = "1940-10-10";
		String date2 = "2060-10-10";
		String searchConditions="";
		String college = "";
		String department = "";
		
		List<ProtocolFormType> formTypes = Arrays.asList(ProtocolFormType.values());
		boolean allCollege = false;
		
		
		for (ReportCriteria rc : criterias) {
			ReportFieldTemplate reportCriteriaField = new ReportFieldTemplate();
			try {
				reportCriteriaField = objectMapper.readValue(rc.getCriteria(),
						ReportFieldTemplate.class);
				String value = reportCriteriaField.getValue();
				switch (reportCriteriaField.getFieldIdentifier()) {
				case "college":
					
					String combSearchStr = "";
					
					//if selected as all college
					if(value.contains("*")){
						
						/*List<College> colleges = collegeDao.findAll();
						for(College c : colleges){
							if(combSearchStr.isEmpty()){
								combSearchStr +="@collegeid = \"" + c.getId() + "\"";
							}else{
								combSearchStr += " or "+"@collegeid = \"" + c.getId() + "\"";
							}
						}
						searchConditions += "select id from protocol where retired =0 and meta_data_xml.exist('/protocol/responsible-department["
								+ combSearchStr + "]') = 1";*/
						allCollege = true;
						break;
					}


					if (!value.contains(",")) {
						combSearchStr = "@collegeid = \"" + value + "\"";
						queryCriteriasValueMap.put("College", collegeDao
								.findById(Long.valueOf(value)).getName());
						college = (collegeDao.findById(Long.valueOf(value)).getName().split(" "))[0];
					} else {
						List<String> combList = Arrays.asList(value.split(","));

						combSearchStr = "@collegeid = \"" + combList.get(0)
								+ "\" and @deptid = \"" + combList.get(1)
								+ "\"";
						queryCriteriasValueMap.put("College", collegeDao
								.findById(Long.valueOf(combList.get(0)))
								.getName());
						
						department = departmentDao.findById(Long.valueOf(combList.get(1))).getName();
						queryCriteriasValueMap.put("Department", department);
						
						

						if (combList.size() == 3) {
							combSearchStr = combSearchStr
									+ " and @subdeptid = \"" + combList.get(2)
									+ "\"";
							queryCriteriasValueMap.put(
									"SubDepartment",
									subDepartmentDao.findById(
											Long.valueOf(combList.get(2)))
											.getName());
						}
					}
					searchConditions += "select id from protocol where retired =0 and meta_data_xml.exist('/protocol/responsible-department["
							+ combSearchStr + "]') = 1";
					break;
				case "approveddaterange":
					if (reportCriteriaField.getOperator().toString()
							.equals("BEFORE")) {
						date2 = value;
						queryCriteriasValueMap.put("Time Span", "BEFORE: "
								+ value);
					} else if (reportCriteriaField.getOperator().toString()
							.equals("AFTER")) {
						date1 = value;
						queryCriteriasValueMap.put("Time Span", "AFTER: "
								+ value);
					} else if (reportCriteriaField.getOperator().toString()

					.equals("BETWEEN")) {
						date1 = value.toUpperCase().substring(0,
								value.toUpperCase().indexOf(","));
						date2 = value.toUpperCase().substring(
								value.toUpperCase().indexOf(",") + 1,
								value.length());
						queryCriteriasValueMap.put("Time Span", "BETWEEN: "
								+ date1 + "~" + date2);
					}

					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(!searchConditions.isEmpty()){
			searchConditions = " AND protocol_id in ("+searchConditions+") ";
		}
		
		//get college and department pi list for hsrd
		List<Long> validUsersId = Lists.newArrayList();
		for(User user :userDao.findAll()){
			try{
			String collegeAndDept = user.getPerson().getDepartment();
			if(collegeAndDept == null){
				continue;
			}
			if(!department.isEmpty()&&collegeAndDept.equals(department)){
				validUsersId.add(user.getId());
				continue;
			}
			if(department.isEmpty()&&collegeAndDept.contains(college+" ")){
				validUsersId.add(user.getId());
				continue;
			}
			}catch(Exception e){
				
			}
		}
		
		//get search criteria table
		finalResultXml = finalResultXml+generateSummaryCriteriaTable(reportTemplate,
				queryCriteriasValueMap);
		
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
	
		finalResultXml += "<fields>";
				
		finalResultXml += "<field id=\"college\" desc=\"College\" hidden=\"false\" />";
		finalResultXml += "<field id=\"department\" desc=\"Department\" hidden=\"false\" />";
		finalResultXml += "<field id=\"subdepartment\" desc=\"SubDepartment\" hidden=\"false\" />";
		finalResultXml += "<field id=\"formtype\" desc=\"Form Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"reviewtype\" desc=\"Review Type\" hidden=\"false\" />";
		finalResultXml += "<field id=\"number\" desc=\"Number\" hidden=\"false\" />";
			
		finalResultXml += "</fields>";
			
		finalResultXml += "<report-items>";
		
		
		List<College> colleges = Lists.newArrayList();
		if(allCollege){
			colleges = collegeDao.findAll();
		}else{
			colleges.add(new College());
		}
		
		
		for(College c : colleges){
			if(allCollege){
			searchConditions = " AND protocol_id in (select id from protocol where retired =0 and meta_data_xml.exist('/protocol/responsible-department["
					+ "@collegeid = \"" + c.getId() + "\"" + "]') = 1) ";
			queryCriteriasValueMap.put("College", c.getName());
			}
		
		List<BigInteger> expediatedFullBoard = Lists.newArrayList();
		int fullBoardApprovedFromExpedited = 0;
		for(ProtocolFormStatusEnum pfStatusEnum:requiredActions){
			String queryStr = "";
			if(pfStatusEnum.equals(ProtocolFormStatusEnum.DETERMINED_HUMAN_SUBJECT_RESEARCH)||pfStatusEnum.equals(ProtocolFormStatusEnum.DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH)){
				queryStr = "select protocol_form_id as pfid from protocol_form_status where retired= 0  AND Datediff(day, '"+date2+"',modified)<0 AND Datediff(day, '"+date1+"',modified)>0 AND protocol_form_id is not null and protocol_form_status  ='"+pfStatusEnum+"'";
			}else{
				queryStr = "select protocol_form_id as pfid from protocol_form_status where retired= 0  AND Datediff(day, '"+date2+"',modified)<0 AND Datediff(day, '"+date1+"',modified)>0 AND protocol_form_id is not null and protocol_form_status  ='"+pfStatusEnum+"' and protocol_form_id in (select id from protocol_form where retired = 0"+searchConditions+")";
			}
			logger.debug(queryStr);
			Query query = em.createNativeQuery(queryStr);
			
			List<BigInteger> pfIds =(List<BigInteger>) query.getResultList();
			if(pfStatusEnum.equals(ProtocolFormStatusEnum.IRB_APPROVED)){
				pfIds.addAll(expediatedFullBoard);
			}
			
			Map<ProtocolFormType,Integer> formTypeAndNumberMap = Maps.newHashMap();
			
			formTypeAndNumberMap.put(ProtocolFormType.NEW_SUBMISSION,0);
			formTypeAndNumberMap.put(ProtocolFormType.CONTINUING_REVIEW,0);
			formTypeAndNumberMap.put(ProtocolFormType.MODIFICATION,0);
			formTypeAndNumberMap.put(ProtocolFormType.REPORTABLE_NEW_INFORMATION,0);
			formTypeAndNumberMap.put(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION,0);
			Set<ProtocolFormType> pfTypes = formTypeAndNumberMap.keySet();
			for(BigInteger pfIdBig : pfIds){
				try{
					long pfId = pfIdBig.longValue();
					ProtocolForm pf = protocolFormDao.findById(pfId);
					
					//put expedited with minor contingencies to full board
					if(pfStatusEnum.equals(ProtocolFormStatusEnum.EXPEDITED_APPROVED)){
						try{
							protocolFormStatusDao.getProtocolFormStatusByFormIdAndProtocolFormStatus(pf.getId(), ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MINOR_CONTINGENCIES);
							expediatedFullBoard.add(new BigInteger(String.valueOf(pfId)));
						}catch(Exception e){
							
						}
					}
					
					if(pfStatusEnum.equals(ProtocolFormStatusEnum.DETERMINED_HUMAN_SUBJECT_RESEARCH)||pfStatusEnum.equals(ProtocolFormStatusEnum.DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH)){
						//make sure the form is from the correct college and department
						String formXmlData = pf.getMetaDataXml();
						XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
						String piUserID = xmlHandler.getSingleStringValueByXPath(formXmlData,
										"/hsrd/staffs/staff/user[roles/role/text()=\"Principal Investigator\"]/@id");
						if(!piUserID.isEmpty()&&!validUsersId.contains(Long.valueOf(piUserID))){
							continue;
						}
					}
					
					ProtocolFormType pfType = pf.getProtocolFormType();
					if(pfTypes.contains(pfType)){
							formTypeAndNumberMap.put(pfType, formTypeAndNumberMap.get(pfType)+1);
						
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
			for(ProtocolFormType pfType :pfTypes){
				//we only care type with form >0
				if(formTypeAndNumberMap.get(pfType)==0){
					continue;
				}
				finalResultXml += "<report-item>";
				finalResultXml += "<field id=\"college\">";
				finalResultXml += queryCriteriasValueMap.get("College")!=null?queryCriteriasValueMap.get("College"):"All Colleges";
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"department\">";
				finalResultXml += queryCriteriasValueMap.get("Department")!=null?queryCriteriasValueMap.get("Department"):"";
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"subdepartment\">";
				finalResultXml += queryCriteriasValueMap.get("SubDepartment")!=null?queryCriteriasValueMap.get("SubDepartment"):"";
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"formtype\">";
				finalResultXml += pfType.getDescription();
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"reviewtype\">";
				finalResultXml += pfStatusEnum.getDescription();
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"number\">";
				finalResultXml += formTypeAndNumberMap.get(pfType);
				finalResultXml += "</field>";
				finalResultXml += "</report-item>";
			}
			
		}	
		
		}
		finalResultXml += "</report-items>";
		
		finalResultXml += "</report-result>";
		
		finalResultXml += "</report-results>";
		
		finalResultXml = finalResultXml.replaceAll("&", "&amp;");
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		finalResultXml =finalResultXml.replace("null&lt;", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		
		
		finalResultXml = finalResultXml.replace("Approved by IRB", "Full Board");
		finalResultXml = finalResultXml.replace("Expedited Approved", "Expedited");
		finalResultXml = finalResultXml.replace("Exempt Approved", "Exempt");
		
		logger.debug(finalResultXml);
		return finalResultXml;
	}

	public ReportFieldDao getReportFieldDao() {
		return reportFieldDao;
	}

	@Autowired(required=true)
	public void setReportFieldDao(ReportFieldDao reportFieldDao) {
		this.reportFieldDao = reportFieldDao;
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

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	@Autowired(required=true)
	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public DepartmentDao getDepartmentDao() {
		return departmentDao;
	}

	@Autowired(required=true)
	public void setDepartmentDao(DepartmentDao departmentDao) {
		this.departmentDao = departmentDao;
	}

	public SubDepartmentDao getSubDepartmentDao() {
		return subDepartmentDao;
	}

	@Autowired(required=true)
	public void setSubDepartmentDao(SubDepartmentDao subDepartmentDao) {
		this.subDepartmentDao = subDepartmentDao;
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

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
}
