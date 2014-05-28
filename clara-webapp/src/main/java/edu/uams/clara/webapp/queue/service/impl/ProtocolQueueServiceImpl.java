package edu.uams.clara.webapp.queue.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.dao.usercontext.UserRoleDao;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.Role.DepartmentLevel;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.UserRole;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem.AgendaItemStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormDetailContentService;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.queue.service.QueueService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolQueueServiceImpl extends QueueService {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolQueueServiceImpl.class);

	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolFormDao protocolFormDao;	
	
	private ProtocolStatusDao protocolStatusDao;

	private IRBReviewerDao irbReviewerDao;
	
	private AgendaDao agendaDao;
	
	private AgendaStatusDao agendaStatusDao;
	
	private UserRoleDao userRoleDao;
	
	private ProtocolFormDetailContentService protocolFormDetailContentService;
	
	private ProtocolFormService protocolFormService;

	@Value("${queue.template.xml.uri}")
	private String queueTemplateXmlUri;
	
	private Set<String> protocolMetaXpathList = new HashSet<String>();{
		protocolMetaXpathList.add("/protocol/title");
		protocolMetaXpathList.add("/protocol/study-type");
	}
	
	private List<ProtocolFormCommitteeStatusEnum> pendingAssignmentStatusList = Lists.newArrayList();{
		pendingAssignmentStatusList.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_ASSIGNMENT);
		pendingAssignmentStatusList.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
		pendingAssignmentStatusList.add(ProtocolFormCommitteeStatusEnum.POTENTIAL_NON_COMPLIANCE_IN_REVIEW);
	}
	
	private String getWarningList(ProtocolForm protocolForm) {
		String warnings = "<warnings>";
		
		if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION) || protocolForm.getProtocolFormType().equals(ProtocolFormType.MODIFICATION)) {
			Map<String, Boolean> nctNumberValidationMap = protocolFormService.nctNumberValidation(protocolForm);
			if (nctNumberValidationMap.get("needNctNumber")) {
				if (!nctNumberValidationMap.get("nctNumberEntered")) {
					warnings += "<warning category=\"NCT\">NCT number required but not entered.</warning>";
				}
			}
		}
		
		warnings += "</warnings>";
		
		return warnings;
	}
	
	private List<AgendaStatusEnum> unapprovedAgendaStatuses = Lists.newArrayList();{
		unapprovedAgendaStatuses.add(AgendaStatusEnum.CANCELLED);
		unapprovedAgendaStatuses.add(AgendaStatusEnum.AGENDA_INCOMPLETE);
		unapprovedAgendaStatuses.add(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);
	}

	@Override
	public String getFormsInQueueByUser(String queueIdentifier, User user, boolean showHistory) {
		
		Map<DepartmentLevel, List<Long>> departMap = new HashMap<DepartmentLevel, List<Long>>();
		
		Set<String> res = new HashSet<String>();

		Set<String> lookupPaths = new HashSet<String>();
		
		for (UserRole ur : user.getUserRoles()){
			if(!ur.isRetired()) {				
				// add role search path for searching corresponding roles in the queue template xml.
				if(ur.getRole().getCommitee() != null){ // system admin is not a committee
					lookupPaths.add("/queues/queue[@identifier='" + queueIdentifier
							+ "']/roles/role[@identifier='"
							+ ur.getRole().getRolePermissionIdentifier() + "']/.");
				}
				//set department list
				if(ur.getRole().getDepartmentLevel() != null){
					DepartmentLevel departmentLevel = ur.getRole().getDepartmentLevel();
					if(!departMap.containsKey(departmentLevel)){
						departMap.put(departmentLevel, new ArrayList<Long>());
					}
					
					switch(ur.getRole().getDepartmentLevel()){
					case SUB_DEPARTMENT:
						if (ur.getSubDepartment() != null) {
							departMap.get(departmentLevel).add(ur.getSubDepartment().getId());
						}
						break;
					case DEPARTMENT:
						if (ur.getDepartment() != null) {
							departMap.get(departmentLevel).add(ur.getDepartment().getId());
						}
						break;
					case COLLEGE:
						if (ur.getCollege() != null) {
							departMap.get(departmentLevel).add(ur.getCollege().getId());
						}
						break;
					}
				}
				
			}
		}
		
		logger.debug("showHistory: " + showHistory);
		
		XmlProcessor xmlProcessor = this.getXmlProcessor();

		String queueTemplateXml;
		List<Element> roles;
		try {
			queueTemplateXml = xmlProcessor.loadXmlFile(queueTemplateXmlUri);
			roles = xmlProcessor.listDomElementsByPaths(lookupPaths,
					queueTemplateXml);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "<list></list>";
		}
		
		String xmlResult = "<list>";
		
		for (Element role : roles) {

			// logger.debug("x: " + DomUtils.elementToString(role));
			
			Committee committee = Committee.valueOf(role
					.getAttribute("committee"));

			String roleName = role.getAttribute("name");
			//logger.debug("protocolFormId: " + protocolForm.getId() + "; protocolFormCommitteeStatus.xmlData: " + protocolFormCommitteeStatus.getXmlData());
			Permission roleId = null;
			try{
				// make it safer..
				roleId = Permission.valueOf(role.getAttribute("identifier"));
			}catch(Exception ex){
				// don't care...
			}

			NodeList formNodes = role.getElementsByTagName("form");

			for (int i = 0; i < formNodes.getLength(); i++) {
				Element formElement = (Element) formNodes.item(i);
				ProtocolFormType formType = ProtocolFormType
						.valueOf(formElement.getAttribute("type"));
				ProtocolFormStatusEnum formStatus = ProtocolFormStatusEnum
						.valueOf(formElement.getAttribute("status"));

				//List<ProtocolFormCommitteeStatusEnum> formCommitteeStatuses = new ArrayList<ProtocolFormCommitteeStatusEnum>();
				Set<ProtocolFormCommitteeStatusEnum> formCommitteeStatuses = Sets.newHashSet();

				NodeList formCommitteeStatusNodes = formElement
						.getElementsByTagName("form-committee-status");

				for (int j = 0; j < formCommitteeStatusNodes.getLength(); j++) {
					Element formCommitteeStatusElement = (Element) formCommitteeStatusNodes
							.item(j);

					formCommitteeStatuses.add(ProtocolFormCommitteeStatusEnum
							.valueOf(formCommitteeStatusElement
									.getTextContent()));
					//formCommitteeStatuses.add(ProtocolFormCommitteeStatusEnum.ANY);
				}

				if(showHistory){
					formStatus = ProtocolFormStatusEnum.ANY;
				}
				
				//logger.debug("committee: " + committee + "; formType:"
						//+ formType + "; formStatus: " + formStatus);
				
				outerloop:
				for (ProtocolFormCommitteeStatus protocolFormCommitteeStatus : protocolFormCommitteeStatusDao
						.listByCommitteeAndFormTypeAndStatuses(committee, formStatus,
								formCommitteeStatuses, formType, showHistory)) {

					ProtocolForm protocolForm = protocolFormCommitteeStatus
							.getProtocolForm();
					
					
					if (protocolForm.isRetired()){
						continue;
					}
					
					ProtocolStatus latestProtocolStatus = protocolStatusDao.findProtocolStatusByProtocolId(protocolForm.getProtocol().getId());
					
					if (latestProtocolStatus.getProtocolStatus().equals(ProtocolStatusEnum.CLOSED) && !protocolForm.getProtocolFormType().equals(ProtocolFormType.STUDY_RESUMPTION)) {
						continue;
					}

					ProtocolFormXmlData lastProtocolFormXmlData = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolFormIdAndType(
									protocolForm.getId(), protocolForm
											.getProtocolFormType()
											.getDefaultProtocolFormXmlDataType());
					
					
					
					// for departmental roles, we need to check whether the study is in this department..
					// do this early... no point to do anything else, if this is a departmental level role
					if (roleId != null) {
						
						
						switch(roleId){
						case ROLE_SUB_DEPARTMENT_CHIEF:
						case ROLE_DEPARTMENT_CHAIR:
						case ROLE_COLLEGE_DEAN:
							
							long collegeId, departmentId, subDepartmentId; 
							collegeId = departmentId = subDepartmentId = 0;
							
							try{
								Document xmlDataDoc = xmlProcessor.loadXmlStringToDOM(lastProtocolFormXmlData.getXmlData());
								XPath xPath = xmlProcessor.getXPathInstance();
								
								Element departmentEl = (Element) xPath.evaluate("/protocol/responsible-department",
										xmlDataDoc, XPathConstants.NODE);
								
								collegeId = Long.valueOf((departmentEl!=null && departmentEl.getAttribute("collegeid")!=null && !departmentEl.getAttribute("collegeid").isEmpty())?departmentEl.getAttribute("collegeid"):"0");
								departmentId = Long.valueOf((departmentEl!=null && departmentEl.getAttribute("deptid")!=null && !departmentEl.getAttribute("deptid").isEmpty())?departmentEl.getAttribute("deptid"):"0");
								subDepartmentId = Long.valueOf((departmentEl!=null && departmentEl.getAttribute("subdeptid")!=null && !departmentEl.getAttribute("subdeptid").isEmpty())?departmentEl.getAttribute("subdeptid"):"0");
							} catch (Exception e){
								e.printStackTrace();
							}
							logger.debug("ur collegeId: " + departMap.get("college") + " collegeId: " + collegeId);
							logger.debug("ur deptId: " + departMap.get("department") + " deptId: " + departmentId);
							logger.debug("ur subId: " + departMap.get("subDepartment") + " deptId: " + subDepartmentId);
							
							//if the roleId in the current loop is checking for subdeprtment, continoue if this user doesn't have this sub department approval right 
							if(roleId.equals(Permission.ROLE_SUB_DEPARTMENT_CHIEF) && !departMap.get(DepartmentLevel.SUB_DEPARTMENT).contains(subDepartmentId)){
								continue;
							}
							if(roleId.equals(Permission.ROLE_DEPARTMENT_CHAIR) && !departMap.get(DepartmentLevel.DEPARTMENT).contains(departmentId)){
								continue;
							}
							if(roleId.equals(Permission.ROLE_COLLEGE_DEAN) && !departMap.get(DepartmentLevel.COLLEGE).contains(collegeId)){
								continue;
							}
							break;
						default:
							// not department level.
							break;
						}					

					}
					
					boolean isMine = false;
					String extraXmlData = protocolForm.getMetaDataXml();
					
					String assignedReviewersXml = "";
					if (extraXmlData != null && !extraXmlData.isEmpty()){
						try {
							Document extraXmlDataDoc = xmlProcessor.loadXmlStringToDOM(extraXmlData);
							
							XPath xPath = xmlProcessor.getXPathInstance();
							
							String xpath = "//assigned-reviewer[@assigning-committee='"+ committee +"' or @user-role-committee='" + committee + "']";
							
							if (committee.equals(Committee.PROTOCOL_LEGAL_REVIEW)) {
								xpath = "//assigned-reviewer";
							}
							
							NodeList assignedReviewers = (NodeList) xPath.evaluate(xpath, extraXmlDataDoc, XPathConstants.NODESET);
							
							//NodeList assignedReviewers = (NodeList) xPath.evaluate("//assigned-reviewer", extraXmlDataDoc, XPathConstants.NODESET);
							
							for (int j = 0; j < assignedReviewers.getLength(); j ++){
								
								Element assignedReviewerEl = (Element)assignedReviewers.item(j);
								
								assignedReviewersXml += DomUtils.elementToString(assignedReviewerEl);
								//logger.debug("protocolId: " + protocolForm.getProtocol().getId() + " assignedReviewer xml: " + assignedReviewersXml);
								if (Long.parseLong(assignedReviewerEl.getAttribute("user-id")) == user.getId()){
									isMine = true;
									//break;
								}								
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (XPathExpressionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
					
					String formTypeMachineReadable = protocolForm.getProtocolFormType()
							.getUrlEncoded();

					ProtocolFormStatus pformStatus = protocolFormStatusDao
							.getLatestProtocolFormStatusByFormId(protocolForm.getId());					
					
					if (committee.equals(Committee.IRB_REVIEWER)){
						//let reviewers see the assigned items in their queue after agenda is approved
						Agenda agenda = null;
						
						try {
							agenda = agendaDao.getAgendaByProtocolFormIdAndAgendaItemStatus(protocolForm.getId(), AgendaItemStatus.NEW);
							
							if (unapprovedAgendaStatuses.contains(agendaStatusDao.getAgendaStatusByAgendaId(agenda.getId()).getAgendaStatus())) {
								continue outerloop;
							}
						} catch (Exception e) {
							//e.printStackTrace();
							
							continue outerloop;
						}
						
						List<IRBReviewer> irbReviewerLst = irbReviewerDao.listAgendaIRBReviewersByProtocolFormId(protocolForm.getId());
						
						String assignedIRBReviewers = "";
						if (irbReviewerLst != null && !irbReviewerLst.isEmpty()){
							for (IRBReviewer irbReviewer : irbReviewerLst){
								//UserRole userRole = userRoleDao.getUserRolesByUserIdAndCommittee(irbReviewer.getUser().getId(), Committee.IRB_REVIEWER);

								assignedIRBReviewers += "<assigned-reviewer assigning-committee=\""+ Committee.IRB_REVIEWER +"\" user-fullname=\""+ irbReviewer.getUser().getPerson().getFullname() +"\" user-id=\""+ irbReviewer.getUser().getId() +"\" user-role=\""+ Permission.ROLE_IRB_REVIEWER +"\" user-role-committee=\""+ Committee.IRB_REVIEWER +"\" user-role-id=\"0\">"+ irbReviewer.getUser().getPerson().getFullname() +"</assigned-reviewer>";
								
								if (user.getId() == irbReviewer.getUser().getId()){
									isMine = true;
								}
							}
						}
						
						if (!isMine){
							continue outerloop;
						}
						
						assignedReviewersXml += assignedIRBReviewers;
						
					}
					
					String formXml = "";
					formXml += "<form committee-name=\"" + committee.getDescription() + "\" committee=\"" + committee + "\" role-name=\"" + roleName + "\" role-id=\""+roleId.toString()+"\" form-id=\""
							+ protocolForm.getId() + "\" last-version-id=\""
							+ lastProtocolFormXmlData.getId() + "\""
							+ " is-mine=\"" + isMine + "\">";
					formXml += "<assigned-reviewers>";
					
					if (!assignedReviewersXml.isEmpty()){
						formXml += assignedReviewersXml;
					}
					formXml += "</assigned-reviewers>";
					
					String formTypeDesc = protocolForm.getProtocolFormType().getDescription();
					
					Protocol protocol = protocolForm.getProtocol();
					
					try {
						Map<String, List<String>> protocolMetaDataValueLst = xmlProcessor.listElementStringValuesByPaths(protocolMetaXpathList, protocol.getMetaDataXml());
						
						String title = (protocolMetaDataValueLst.get("/protocol/title")!=null && !protocolMetaDataValueLst.get("/protocol/title").isEmpty())?protocolMetaDataValueLst.get("/protocol/title").get(0):"";
						
						String studyType = (protocolMetaDataValueLst.get("/protocol/study-type")!=null && !protocolMetaDataValueLst.get("/protocol/study-type").isEmpty())?protocolMetaDataValueLst.get("/protocol/study-type").get(0):"";
						
						List<String> suggestedReviewTypeValuse = xmlProcessor.listElementStringValuesByPath("//summary/irb-determination/suggested-type",extraXmlData);
						
						String suggestedReviewType = (suggestedReviewTypeValuse!=null && !suggestedReviewTypeValuse.isEmpty())?suggestedReviewTypeValuse.get(0):"";
						
						formXml += "<meta id=\""+ protocol.getId() +"\" identifier=\""+ protocol.getProtocolIdentifier() +"\" type=\""+ formTypeDesc +"\">";
						
						String statusPriority = pformStatus.getProtocolFormStatus().getPriorityLevel();
						
						formXml += "<status priority=\""+ statusPriority +"\" />";
						formXml += "<title>"+ org.apache.commons.lang.StringEscapeUtils
								.escapeXml(title) +"</title>";
						formXml += "<study-type>"+ studyType +"</study-type>";
						formXml += "<summary><irb-determination><suggested-type>"+ suggestedReviewType +"</suggested-type></irb-determination></summary>";
						formXml += getWarningList(protocolForm);
						formXml += "</meta>";
						//formXml += xmlProcessor.replaceRootTagWith(protocolForm.getMetaDataXml(), "meta");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					formXml += "<form-type id=\"" + formTypeMachineReadable
							+ "\">"
							+ formTypeDesc
							+ "</form-type>";
					formXml += "<form-status><description>"
							+ org.apache.commons.lang.StringEscapeUtils
									.escapeXml(pformStatus.getProtocolFormStatus()
											.getDescription())
							+ "</description><modified-at>" + pformStatus.getModified()
							+ "</modified-at></form-status>";
					formXml += "<form-committee-status><description>"
							+ org.apache.commons.lang.StringEscapeUtils
									.escapeXml(protocolFormCommitteeStatus.getProtocolFormCommitteeStatus()
											.getDescription())
							+ "</description><modified-at>" + protocolFormCommitteeStatus.getModified()
							+ "</modified-at><xml-data>" + protocolFormCommitteeStatus.getXmlData() + "</xml-data></form-committee-status>";
					formXml += "<actions>";
					
					//@TODO need to change to switch or something...
					if (pendingAssignmentStatusList.contains(protocolFormCommitteeStatus
							.getProtocolFormCommitteeStatus())){
						formXml += "<action><name>ASSIGN_AGENDA</name><url></url></action>";
					} else if(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT.equals(protocolFormCommitteeStatus
							.getProtocolFormCommitteeStatus())) {
						//logger.debug("c:" + protocolFormCommitteeStatus.getCommittee());
						String assignToRole = "";
						switch(protocolFormCommitteeStatus.getCommittee()){
						case BUDGET_MANAGER:
							assignToRole = "ROLE_BUDGET_REVIEWER";
							break;
						case COVERAGE_MANAGER:
							assignToRole = "ROLE_COVERAGE_REVIEWER";
							break;
						default:
							break;
						}
						formXml += "<action><name>ASSIGN_REVIEWER</name><assign-to-role>" +assignToRole + "</assign-to-role><url></url></action>";
					} else {
						formXml += "<action><name>REVIEW</name><url>/protocols/"
								+ protocolForm.getProtocol().getId() + "/protocol-forms/"
								+ protocolForm.getId() + "/review?committee="
								+ committee + "</url></action>";
					}
					

					formXml += "</actions>";
					formXml += protocolFormDetailContentService
							.getDetailContent(protocolForm);
					formXml += "</form>";
					
					res.add(formXml);
				}
			}

		}
		
		for (String formS:res){
			xmlResult += formS;
		}

		xmlResult += "</list>";

		return xmlResult;
	}
	
	protected synchronized void saveOrUpdateFormMetaDataXml(Form form, String metaDataXml){
		ProtocolForm protocolForm = (ProtocolForm) form;
		protocolForm.setMetaDataXml(metaDataXml);
		protocolFormDao.saveOrUpdate(protocolForm);
	}
	
	@Override
	public Form getForm(long formId){
		return protocolFormDao.findById(formId);
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	public String getQueueTemplateXmlUri() {
		return queueTemplateXmlUri;
	}

	public void setQueueTemplateXmlUri(String queueTemplateXmlUri) {
		this.queueTemplateXmlUri = queueTemplateXmlUri;
	}

	@Autowired(required = true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public UserRoleDao getUserRoleDao() {
		return userRoleDao;
	}
	
	@Autowired(required = true)
	public void setUserRoleDao(UserRoleDao userRoleDao) {
		this.userRoleDao = userRoleDao;
	}

	public ProtocolFormDetailContentService getProtocolFormDetailContentService() {
		return protocolFormDetailContentService;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDetailContentService(
			ProtocolFormDetailContentService protocolFormDetailContentService) {
		this.protocolFormDetailContentService = protocolFormDetailContentService;
	}

	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}
	
	@Autowired(required = true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}
	
	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}
	
	@Autowired(required = true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}
	
}
