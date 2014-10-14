package edu.uams.clara.webapp.common.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public abstract class EmailDataService<T> {
	private final static Logger logger = LoggerFactory
			.getLogger(EmailDataService.class);
	
	private XmlProcessor xmlProcessor;
	
	private UserDao userDao;
	
	private EmailTemplateDao emailTemplateDao;
	
	private VelocityEngine velocityEngine;
	
	private EmailService emailService;
	
	private FormService formService;
	
	@Value("${application.host}")
	private String appHost;
	
	/*
	public Map<String, List<String>> getValues(String xmlData, List<String> xPathList){
		Map<String, List<String>> values = null;
		
		if (StringUtils.hasText(xmlData)) {
			Set<String> valueKeys = new HashSet<String>(xPathList);

			try {
				values = xmlProcessor.listElementStringValuesByPaths(valueKeys,
						xmlData);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
		return values;
	}
	
	public String safeStringValueByKey(Map<String, List<String>> values, String key){
		if (values == null) return "";
		return values.get(key) != null && values.get(key).size() > 0?values.get(key).get(0):"N/A";
	}
	*/
	
	//@TODO: Move the group to committee, and do postconstruct on this bean
	protected static Map<String, Committee> committeeMatchMap = new HashMap<String, Committee>();{
		committeeMatchMap.put("realComplianceReviewer", Committee.COMPLIANCE_REVIEW);
		committeeMatchMap.put("realHospitalServiceReviewer", Committee.HOSPITAL_SERVICES);
		committeeMatchMap.put("realProtocolLegalReviewer", Committee.PROTOCOL_LEGAL_REVIEW);
		committeeMatchMap.put("realDepartmentReviewer", Committee.DEPARTMENT_CHAIR);
		committeeMatchMap.put("realCollegeReviewer", Committee.COLLEGE_DEAN);
		committeeMatchMap.put("realIRBAssigner", Committee.IRB_ASSIGNER);
		committeeMatchMap.put("realPharmacyReviewer", Committee.PHARMACY_REVIEW);
		committeeMatchMap.put("realContractManager", Committee.CONTRACT_MANAGER);
		committeeMatchMap.put("realGatekeeper", Committee.GATEKEEPER);
		committeeMatchMap.put("realACHGatekeeper", Committee.ACHRI);
		committeeMatchMap.put("realBudgetManager", Committee.BUDGET_MANAGER);
		committeeMatchMap.put("realPTL", Committee.PTL);
		committeeMatchMap.put("realProtocolLegalReviewer", Committee.PROTOCOL_LEGAL_REVIEW);
		committeeMatchMap.put("realACHPharmacyReviewer", Committee.ACH_PHARMACY_REVIEWER);
		committeeMatchMap.put("realIRBOffice", Committee.IRB_OFFICE);
		committeeMatchMap.put("realMonitioring", Committee.MONITORING_REGULATORY_QA);
		committeeMatchMap.put("realBeaconTeam", Committee.BEACON_TEAM);
		committeeMatchMap.put("realWillowTeam", Committee.WILLOW_TEAM);
		committeeMatchMap.put("realLegalReviewer", Committee.CONTRACT_LEGAL_REVIEW);
		committeeMatchMap.put("realContractReviewer", Committee.CONTRACT_ADMIN);
	}
	
	protected static Map<String, String> realRecipientMatchMap = new HashMap<String, String>();{
		realRecipientMatchMap.put("AssignedReviewer", "assignedReviewer");
		realRecipientMatchMap.put("RevisionRequestedAssignedReviewer", "requestedReviewer");
		realRecipientMatchMap.put("studyPI", "studyPI");
		realRecipientMatchMap.put("onlyPI", "onlyPI");
		realRecipientMatchMap.put("studyBudgetManager", "studyBudgetManager");
		realRecipientMatchMap.put("budgetAdmin", "budgetAdmin");
	}
	
	protected static Map<String, Permission> assignedReviewerMatchMap = new HashMap<String, Permission>();{
		assignedReviewerMatchMap.put("AssignedCoverageReviewer", Permission.ROLE_COVERAGE_REVIEWER);
		assignedReviewerMatchMap.put("AssignedBudgetReviewer", Permission.ROLE_BUDGET_REVIEWER);
		assignedReviewerMatchMap.put("AssignedRegulatoryReviewer", Permission.ROLE_MONITORING_REGULATORY_QA_REVIEWER);
		assignedReviewerMatchMap.put("AssignedIRBOfficeReviewer", Permission.ROLE_IRB_OFFICE);
		assignedReviewerMatchMap.put("AssignedContractReviewer", Permission.ROLE_CONTRACT_ADMIN);
		assignedReviewerMatchMap.put("AssignedLegalReviewer", Permission.ROLE_CONTRACT_LEGAL_REVIEW);
	}
	
	protected static List<Committee> noResubmissionNotificationCommitteeList = Lists.newArrayList();{
		noResubmissionNotificationCommitteeList.add(Committee.IRB_PREREVIEW);
		noResubmissionNotificationCommitteeList.add(Committee.IRB_OFFICE);
		noResubmissionNotificationCommitteeList.add(Committee.IRB_EXPEDITED_REVIEWER);
		noResubmissionNotificationCommitteeList.add(Committee.IRB_EXEMPT_REVIEWER);
	}
	
	protected String getLogReceipientLst(String logReceipient, EmailRecipient er, List<String> real, List<String> realCc) {
		StringBuilder sb = new StringBuilder();

		try {
			List<String> newList = new ArrayList<String>(real);
			newList.addAll(realCc);
			
			newList = new ArrayList<String>(new HashSet<String>(newList));

			List<EmailRecipient> emailRecipients = getEmailService()
					.getEmailRecipients(newList.toString());
			
			for (EmailRecipient erp: emailRecipients) {
				if(sb.length() > 0){
			        sb.append(';');
			    }
			    sb.append(erp.getDesc());
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String desc = er.getType().equals(EmailRecipient.RecipientType.INDIVIDUAL)?"INDIVIDUAL":er.getDesc();
		
		logReceipient = logReceipient + "<strong>" + desc + "</strong>" + " (" + sb.toString() + ") ";
		//logger.debug("!!!!!!!!!!!!!!!! " + logReceipient);
		
		return logReceipient;
	}
	
	public EmailTemplate resolveEmailTemplate(Form form,
			EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues) {
		Committee revisionRequestedCommittee = null;
		
		Committee nextCommittee = null;
		
		List<Committee>  optionalCommittees = null;
		
		try{
			revisionRequestedCommittee = Committee.valueOf(attributeRawValues.get(
					"REVISION_REQUEST_COMMITTEE").toString());
		} catch (Exception e){
			//don't care... down stream handles null
		}
		
		try{
			nextCommittee = Committee.valueOf(attributeRawValues.get(
					"NEXT_COMMITTEE").toString());
		} catch (Exception e){
			//don't care... down stream handles null
		}
		
		try{ 
			optionalCommittees = (List<Committee>) attributeRawValues.get(
						"SELECTED_COMMITTEES");
		}catch(Exception e){
			//don't care... down stream handles null
		}
		
		List<String> real = new ArrayList<String>();
		List<String> realCc = Lists.newArrayList();
		
		//List<EmailRecipient> emailRecipients = Lists.newArrayList();
		List<EmailRecipient> toEmailRecipients = Lists.newArrayList();
		List<EmailRecipient> ccEmailRecipients = Lists.newArrayList();
		
		Map<String, List<EmailRecipient>> receipientsMap = Maps.newHashMap();
		
		try{
			toEmailRecipients = getEmailService().getEmailRecipients(emailTemplate.getTo());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		receipientsMap.put("to", toEmailRecipients);
		
		try{
			ccEmailRecipients = getEmailService().getEmailRecipients(emailTemplate.getCc());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		receipientsMap.put("cc", ccEmailRecipients);
		
		String logReceipient = "";
		
		for (Entry<String, List<EmailRecipient>> mapEntry : receipientsMap.entrySet()){
			for (EmailRecipient er : mapEntry.getValue()){
				List<String> logReceipientTolist = Lists.newArrayList();
				List<String> logReceipientCclist = Lists.newArrayList();
				
				if (er.getType().equals(EmailRecipient.RecipientType.INDIVIDUAL)){
					if (mapEntry.getKey().equals("to")){
						real.add(er.getJsonString());
						logReceipientTolist.add(er.getJsonString());
					} else {
						realCc.add(er.getJsonString());
						logReceipientCclist.add(er.getJsonString());
					}					
				} else {
					for (Entry<String, String> realRecipientEntry : realRecipientMatchMap.entrySet()){
						if (er.getAddress().contains(realRecipientEntry.getKey())){
							List<String> realRecipientLst = getReviewersOrPIMailToList(committee, form.getMetaXml(), realRecipientEntry.getValue());
							if (realRecipientLst != null && !realRecipientLst.isEmpty()){
								for (String s : realRecipientLst){
									if (mapEntry.getKey().equals("to")){
										real.add(s);
										logReceipientTolist.add(s);
									} else {
										realCc.add(s);
										logReceipientCclist.add(s);
									}
								}
							}
						}
					}
					
					for (Entry<String, Committee> entry : committeeMatchMap.entrySet()){
						if (er.getAddress().contains(entry.getKey())){
							List<String> committeeMatchLst = getNextCommitteeMailToList(form, entry.getValue());
							if (committeeMatchLst != null && !committeeMatchLst.isEmpty()){
								for (String s : committeeMatchLst){
									if (mapEntry.getKey().equals("to")){
										real.add(s);
										logReceipientTolist.add(s);
									} else {
										realCc.add(s);
										logReceipientCclist.add(s);
									}
									
								}
							}
						}
					}
					
					for (Entry<String, Permission> assignedReviewerEntry : assignedReviewerMatchMap.entrySet()){
						
						if (er.getAddress().contains(assignedReviewerEntry.getKey())){
							List<String> assignedReviewerLst = getSpecificReviewerList(form, assignedReviewerEntry.getValue());
							if (assignedReviewerLst != null && !assignedReviewerLst.isEmpty()){
								for (String s : assignedReviewerLst){
									if (mapEntry.getKey().equals("to")){
										real.add(s);
										logReceipientTolist.add(s);
									} else {
										realCc.add(s);
										logReceipientCclist.add(s);
									}
								}
							}
						}
					}
					/*
					if (er.getAddress().contains("realRecipient")){
						List<String> realLst = getRealMailToList(form);
						if (realLst != null && !realLst.isEmpty()){
							for (String s : realLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
								} else {
									realCc.add(s);
								}
							}
						}
					}
					*/

					if (er.getAddress().contains("SelectedCommittees") && optionalCommittees != null && !optionalCommittees.isEmpty()){
						List<String> selectCommitteesLst = getSelectedCommitteeMailToList(optionalCommittees);
						if (selectCommitteesLst != null && !selectCommitteesLst.isEmpty()){
							for (String s : selectCommitteesLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
									logReceipientTolist.add(s);
								} else {
									realCc.add(s);
									logReceipientCclist.add(s);
								}
							}
						}
					}
					
					//if (er.getAddress().contains("NextCommittee") && nextCommittee != null && !nextCommittee.isAssignable()){
					if (er.getAddress().contains("NextCommittee") && nextCommittee != null){
						List<String> nextCommitteeLst = getNextCommitteeMailToList(form, nextCommittee);
						if (nextCommitteeLst != null && !nextCommitteeLst.isEmpty()){
							for (String s : nextCommitteeLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
									logReceipientTolist.add(s);
								} else {
									realCc.add(s);
									logReceipientCclist.add(s);
								}
							}
						}
					}
					
					if (er.getAddress().contains("RevisionRequestedCommittee") && revisionRequestedCommittee != null && !noResubmissionNotificationCommitteeList.contains(revisionRequestedCommittee)){
						List<String> revisionReqCommitteeLst = getNextCommitteeMailToList(form, revisionRequestedCommittee);
						if (revisionReqCommitteeLst != null && !revisionReqCommitteeLst.isEmpty()){
							for (String s : revisionReqCommitteeLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
									logReceipientTolist.add(s);
								} else {
									realCc.add(s);
									logReceipientCclist.add(s);
								}
							}
						}
					}
				}
				
				logReceipient = getLogReceipientLst(logReceipient, er, logReceipientTolist, logReceipientCclist);
			}
		}
		
		if (real != null && !real.isEmpty()){
			emailTemplate.setRealRecipient(real.toString());
		} else {
			emailTemplate.setRealRecipient(emailTemplate.getTo());
		}
		
		if (realCc != null && !realCc.isEmpty()){
			emailTemplate.setRealCCRecipient(realCc.toString());
		} else {
			emailTemplate.setRealCCRecipient(emailTemplate.getCc());
		}
		
		emailTemplate.setLogRecipient(logReceipient);
		
		return emailTemplate;
	}
	
	public User getSpecifiRoleUser(String formXmlData, String roleName){
		User piUser = null;
		try{
			Document doc = xmlProcessor.loadXmlStringToDOM(formXmlData);
			XPath xpath = xmlProcessor.getXPathInstance();
			
			String lookupPath = "//staffs/staff/user[roles/role=\""+ roleName +"\"]";
			logger.debug("loouoPath: " + lookupPath);
			Element piEl = (Element) xpath.evaluate(lookupPath,
					doc, XPathConstants.NODE);

			long userId =  Long.valueOf(piEl.getAttribute("id"));
			piUser = userDao.findById(userId);
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		return piUser;
	}
	
	public List<User> getStudyStaffs(String formXmlData){
		List<User> studyStaffs = new ArrayList<User>();
		
		try{
			Document doc = xmlProcessor.loadXmlStringToDOM(formXmlData);
			XPath xpath = xmlProcessor.getXPathInstance();
			
			String lookupPath = "//staffs/staff/user";
			
			NodeList staffsLst = (NodeList) xpath.evaluate(lookupPath,
					doc, XPathConstants.NODESET);

			if (staffsLst.getLength() > 0){
				for (int i=0; i < staffsLst.getLength(); i++){
					Element userEl = (Element) staffsLst.item(i);
					
					long userId = Long.valueOf(userEl.getAttribute("id"));
					User staffUser = userDao.findById(userId);
					logger.debug("user: " + staffUser);
					studyStaffs.add(staffUser);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return studyStaffs;
	}
	
	public List<User> getReviewers(String formMetaData, Permission permission){
		List<User> reviewers = new ArrayList<User>();
		
		try{
			Document doc = xmlProcessor.loadXmlStringToDOM(formMetaData);
			XPath xpath = xmlProcessor.getXPathInstance();
			
			String lookupPath = "//committee-review/committee/assigned-reviewers/assigned-reviewer[@user-role='"+ permission.toString() +"']";
			
			NodeList reviewersLst = (NodeList) xpath.evaluate(lookupPath,
					doc, XPathConstants.NODESET);

			if (reviewersLst.getLength() > 0){
				for (int i=0; i < reviewersLst.getLength(); i++){
					Element userEl = (Element) reviewersLst.item(i);
					
					long userId = Long.valueOf(userEl.getAttribute("user-id"));
					User reviewer = userDao.findById(userId);
					
					reviewers.add(reviewer);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return reviewers;
	}
	
	public String fillSubject(String content, Map<String, String> subjectAttributesValues){
		for (Entry<String, String> attributeValue : subjectAttributesValues.entrySet()) {
			content = content.replaceAll(attributeValue.getKey().replace("{", "\\{").replace("}", "\\}"), attributeValue.getValue());
		}  
		
		return content;
	}
	
	public List<String> getReviewersOrPIMailToList(Committee committee, String metaData, String type){
		List<String> realMailToLst = new ArrayList<String>();
		
		String lookupPath = "";
		String idAttribute = "";
		
		if (type.equals("assignedReviewer")){
			lookupPath = "//committee-review/committee/assigned-reviewers/assigned-reviewer[@assigning-committee=\""+ committee.toString() +"\"]";
			idAttribute = "user-id";
		} else if (type.equals("requestedReviewer")){
			lookupPath = "//committee-review/committee/assigned-reviewers/assigned-reviewer[@user-role-committee=\""+ committee.toString() +"\"]";
			idAttribute = "user-id";
		} else if (type.equals("studyPI")){
			lookupPath = "//staffs/staff[notify='true' or (user/roles/role='Principal Investigator' or user/roles/role='principal investigator' or user/roles/role='Mentor/Faculty Advisor' or user/roles/role='Treating Physician' or user/roles/role='treating physician')]/user";
			idAttribute = "id";
		} else if (type.equals("onlyPI")){
			lookupPath = "//staffs/staff/user[roles/role='Principal Investigator' or roles/role='principal investigator' or user/roles/role='Mentor/Faculty Advisor' or roles/role='Treating Physician' or roles/role='treating physician']";
			idAttribute = "id";
		}else if (type.equals("studyBudgetManager")){
			lookupPath = "//staffs/staff/user[roles/role='Budget Manager' or roles/role='budget manager' or reponsibilities/responsibility='Budget Manager']";
			idAttribute = "id";
		} else if (type.equals("budgetAdmin")){
			lookupPath = "//staffs/staff/user[roles/role='Budget Administrator' or reponsibilities/responsibility='Budget Administrator']";
			idAttribute = "id";
		}
		
		try{
			Document doc = xmlProcessor.loadXmlStringToDOM(metaData);
			XPath xpath = xmlProcessor.getXPathInstance();
			
			NodeList reviewersLst = (NodeList) xpath.evaluate(lookupPath,
					doc, XPathConstants.NODESET);

			if (reviewersLst.getLength() > 0){
				for (int i=0; i < reviewersLst.getLength(); i++){
					Element userEl = (Element) reviewersLst.item(i);
					
					long userId = Long.valueOf(userEl.getAttribute(idAttribute));
					User reviewer = userDao.findById(userId);
					
					String finalS = "{\"address\":\"INDIVIDUAL_" + reviewer.getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ reviewer.getPerson().getFullname() +"\"}";
					
					realMailToLst.add(finalS);
					
					if (!reviewer.getAlternateEmail().isEmpty()) {
						String altFinalS = "{\"address\":\"INDIVIDUAL_" + reviewer.getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ reviewer.getPerson().getFullname() +"\"}";
						realMailToLst.add(altFinalS);
					}
						
				}
			}
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		logger.debug("realMailToLst: " + realMailToLst);
		return realMailToLst;
	}
	
	public List<String> getSelectedCommitteeMailToList(List<Committee> optionalCommittees){
		List<String> realMailToLst = new ArrayList<String>();
		
		for (Committee committee : optionalCommittees){
			List<User> users = userDao.getUsersByUserRole(committee.getRolePermissionIdentifier());
			
			for (User u : users){
				String finalS = "{\"address\":\"INDIVIDUAL_" + u.getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ u.getPerson().getFullname() +"\"}";
				
				realMailToLst.add(finalS);
				
				if (!u.getAlternateEmail().isEmpty()) {
					String altFinalS = "{\"address\":\"INDIVIDUAL_" + u.getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ u.getPerson().getFullname() +"\"}";
					realMailToLst.add(altFinalS);
				}
			}
		}
		
		return realMailToLst;
	}
	
	public List<String> getNextCommitteeMailToList(Form form, Committee committee){
		logger.debug("committee: " + committee.toString());
		List<String> realMailToLst = new ArrayList<String>();
		
		List<User> users = userDao.getUsersByUserRole(committee.getRolePermissionIdentifier());
		
		if (form != null && (committee.equals(Committee.DEPARTMENT_CHAIR) || committee.equals(Committee.COLLEGE_DEAN))){
			long collegeId, departmentId, subDepartmentId; 
			collegeId = departmentId = subDepartmentId = 0;
			
			try{
				Document xmlDataDoc = xmlProcessor.loadXmlStringToDOM(form.getMetaXml());
				XPath xPath = xmlProcessor.getXPathInstance();
				
				Element departmentEl = (Element) xPath.evaluate("//responsible-department",
						xmlDataDoc, XPathConstants.NODE);
				
				collegeId = Long.valueOf((departmentEl!=null && departmentEl.getAttribute("collegeid")!=null && !departmentEl.getAttribute("collegeid").isEmpty())?departmentEl.getAttribute("collegeid"):"0");
				departmentId = Long.valueOf((departmentEl!=null && departmentEl.getAttribute("deptid")!=null && !departmentEl.getAttribute("deptid").isEmpty())?departmentEl.getAttribute("deptid"):"0");
				subDepartmentId = Long.valueOf((departmentEl!=null && departmentEl.getAttribute("subdeptid")!=null && !departmentEl.getAttribute("subdeptid").isEmpty())?departmentEl.getAttribute("subdeptid"):"0");
			} catch (Exception e){
				e.printStackTrace();
			}
			logger.debug(" collegeId: " + collegeId + " departmentId: " + departmentId);
			for (User user : users){
				if (committee.equals(Committee.DEPARTMENT_CHAIR)){
					for (UserRole ur : user.getUserRoles()){
						if (ur.getDepartment() != null && !ur.isBusinessAdmin()){
							if (ur.getDepartment().getId() == departmentId){
								String finalDepartS = "{\"address\":\"INDIVIDUAL_" + user.getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ user.getPerson().getFullname() +"\"}";
								
								realMailToLst.add(finalDepartS);
								
								if (!user.getAlternateEmail().isEmpty()) {
									String altFinalS = "{\"address\":\"INDIVIDUAL_" + user.getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ user.getPerson().getFullname() +"\"}";
									realMailToLst.add(altFinalS);
								}
							}
						}
					}
				}
				
				if (committee.equals(Committee.COLLEGE_DEAN)){
					for (UserRole ur : user.getUserRoles()){
						//logger.debug("userId: " + ur.getUser().getId() + " college: " + ur.getCollege().getId());
						if (ur.getCollege() != null){
							if (ur.getCollege().getId() == collegeId){
								String finalCollegeS = "{\"address\":\"INDIVIDUAL_" + user.getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ user.getPerson().getFullname() +"\"}";
		
								realMailToLst.add(finalCollegeS);
								
								if (!user.getAlternateEmail().isEmpty()) {
									String altFinalS = "{\"address\":\"INDIVIDUAL_" + user.getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ user.getPerson().getFullname() +"\"}";
									realMailToLst.add(altFinalS);
								}
							}
						}
					}
				}
			}
		} else {
			if (committee.equals(Committee.PI)) {
				users = getFormService().getUsersByKeywordAndSearchField("Principal Investigator", form.getMetaXml(), UserSearchField.ROLE);
			}
			
			for (User u : users){
				String finalS = "{\"address\":\"INDIVIDUAL_" + u.getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ u.getPerson().getFullname() +"\"}";
				logger.debug("finalS: " + finalS);
				realMailToLst.add(finalS);
				
				if (!u.getAlternateEmail().isEmpty()) {
					String altFinalS = "{\"address\":\"INDIVIDUAL_" + u.getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ u.getPerson().getFullname() +"\"}";
					realMailToLst.add(altFinalS);
				}
			}
		}
		logger.debug("realMailToLst: " + realMailToLst);
		return realMailToLst;
	}
	
	public List<String> getSpecificReviewerList(Form form, Permission permission){
		List<String> realMailToLst = new ArrayList<String>();
		
		String lookupPath = "//committee-review/committee/assigned-reviewers/assigned-reviewer[@user-role='"+ permission.toString() +"']";
		
		try{
			Document doc = xmlProcessor.loadXmlStringToDOM(form.getMetaXml());
			XPath xpath = xmlProcessor.getXPathInstance();
			
			NodeList reviewersLst = (NodeList) xpath.evaluate(lookupPath,
					doc, XPathConstants.NODESET);

			if (reviewersLst.getLength() > 0){
				for (int i=0; i < reviewersLst.getLength(); i++){
					Element userEl = (Element) reviewersLst.item(i);
					
					long userId = Long.valueOf(userEl.getAttribute("user-id"));
					User reviewer = userDao.findById(userId);
					
					String finalS = "{\"address\":\"INDIVIDUAL_" + reviewer.getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ reviewer.getPerson().getFullname() +"\"}";
					
					realMailToLst.add(finalS);
					
					if (!reviewer.getAlternateEmail().isEmpty()) {
						String altFinalS = "{\"address\":\"INDIVIDUAL_" + reviewer.getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ reviewer.getPerson().getFullname() +"\"}";
						realMailToLst.add(altFinalS);
					}
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		logger.debug("realSpecificMailToLst: " + realMailToLst);
		return realMailToLst;
	}
	
	public EmailTemplate loadEmailTemplate(
			String identifier,
			Form form,
			Committee committee,
			Map<String, Object> attributeRawValues,
			User user,
			String emailComment){

		return loadEmailTemplateInMeeting(identifier, form, committee, attributeRawValues, user, emailComment, null);
	}
	
	public EmailTemplate loadObjectEmailTemplate(
			String identifier,
			Protocol protocol,
			Agenda agenda,
			Committee committee,
			Map<String, Object> attributeRawValues,
			User user,
			String emailComment){

		EmailTemplate emailTemplate = emailTemplateDao
				.findByIdentifier(identifier);
		
		Map<String, Object> model = new HashMap<String, Object>();
		
		if (agenda == null){
			model = getProtocolEmailData(protocol, committee, attributeRawValues, user, emailComment);
			emailTemplate = setObjectRealSubjectAndReceipt(protocol, emailTemplate, committee, attributeRawValues);
		} 
		
		if (protocol == null){
			model = getAgendaEmailData(agenda, committee, attributeRawValues, user, emailComment);
			emailTemplate = setAgendaRealSubjectAndReceipt(agenda, emailTemplate, committee, attributeRawValues);
		} 
		
		final String templateContent = VelocityEngineUtils
				.mergeTemplateIntoString(velocityEngine,
						emailTemplate.getVmTemplate(), model);

		emailTemplate.setTemplateContent(templateContent);

		return emailTemplate;
	}
	
	public EmailTemplate loadEmailTemplateInMeeting(String identifier,
			Form form, Committee committee, Map<String, Object> attributeRawValues, User user,
			String emailComment, Agenda agenda){
		EmailTemplate emailTemplate = emailTemplateDao
				.findByIdentifier(identifier);
		
		emailTemplate = setRealSubjectAndReceipt(form, emailTemplate, committee, attributeRawValues);
		
		Map<String, Object> model = (agenda!=null)?getEmailData(form, committee, attributeRawValues, user, emailComment, agenda):getEmailData(form, committee, attributeRawValues, user, emailComment);

		final String templateContent = VelocityEngineUtils
				.mergeTemplateIntoString(velocityEngine,
						emailTemplate.getVmTemplate(), model);

		emailTemplate.setTemplateContent(templateContent);

		return emailTemplate;
		
	}
	
	public abstract List<String> getXpathList();
	
	public abstract List<String> getMetaDataXpathList();
	
	public abstract List<String> getFormMetaDataXpathList(String formBaseTag);
	
	public abstract Map<String, Object> getEmailData(Form form, Committee committee, Map<String, Object> attributeRawValues, User user, String emailComment);
	
	public abstract Map<String, Object> getEmailData(Form form, Committee committee, Map<String, Object> attributeRawValues, User user, String emailComment, Agenda agenda);
	
	public abstract Map<String, Object> getProtocolEmailData(Protocol protocol, Committee committee, Map<String, Object> attributeRawValues, User user, String emailComment);
	
	public abstract Map<String, Object> getAgendaEmailData(Agenda agenda, Committee committee, Map<String, Object> attributeRawValues, User user, String emailComment);
	
	public abstract EmailTemplate setRealSubjectAndReceipt(Form form, EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues);
	
	public abstract EmailTemplate setObjectRealSubjectAndReceipt(Protocol protocol, EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues);
	
	public abstract EmailTemplate setAgendaRealSubjectAndReceipt(Agenda agenda, EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues);

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public UserDao getUserDao() {
		return userDao;
	}
	
	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	public EmailTemplateDao getEmailTemplateDao() {
		return emailTemplateDao;
	}
	
	@Autowired(required=true)
	public void setEmailTemplateDao(EmailTemplateDao emailTemplateDao) {
		this.emailTemplateDao = emailTemplateDao;
	}
	
	public VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}
	
	@Autowired(required=true)
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public String getAppHost() {
		return appHost;
	}

	public void setAppHost(String appHost) {
		this.appHost = appHost;
	}

	public EmailService getEmailService() {
		return emailService;
	}
	
	@Autowired(required=true)
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required=true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}
}
