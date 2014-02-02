package edu.uams.clara.webapp.common.service;

import java.util.ArrayList;
import java.util.HashMap;
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
import edu.uams.clara.webapp.common.dao.email.EmailTemplateDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.service.form.FormService;
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
			lookupPath = "//staffs/staff[notify='true' or (user/roles/role='Principal Investigator' or user/roles/role='principal investigator' or user/roles/role='Treating Physician' or user/roles/role='treating physician')]/user";
			idAttribute = "id";
		} else if (type.equals("onlyPI")){
			lookupPath = "//staffs/staff/user[roles/role='Principal Investigator' or roles/role='principal investigator' or roles/role='Treating Physician' or roles/role='treating physician']";
			idAttribute = "id";
		}else if (type.equals("studyBudgetManager")){
			lookupPath = "//staffs/staff/user[roles/role='Budget Manager' or roles/role='budget manager']";
			idAttribute = "id";
		} else if (type.equals("budgetAdmin")){
			lookupPath = "//staffs/staff/user[reponsibilities/responsibility='Budget Manager' or reponsibilities/responsibility='Budget Administrator']";
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
						if (ur.getDepartment() != null){
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
