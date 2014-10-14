package edu.uams.clara.webapp.protocol.service.email;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.email.EmailTemplate;
import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.common.objectwrapper.email.EmailRecipient;
import edu.uams.clara.webapp.common.service.EmailDataService;
import edu.uams.clara.webapp.common.service.form.impl.FormServiceImpl.UserSearchField;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeCommentDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeComment;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.CommentType;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.IRBReviewer;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;

public class ProtocolEmailDataService extends EmailDataService<Protocol>{
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolEmailDataService.class);
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao;
	
	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	
	private IRBReviewerDao irbReviewerDao;
	
	@Value("${email.realrecipients}")
	private String emailRealRecipients;
	
	private String dashbaordLink(long protocolId) {
		return getAppHost() + "/clara-webapp/protocols/" + protocolId + "/dashboard";
	}
	
	private String queueLink() {
		return getAppHost() + "/clara-webapp/queues";
	}
	
	@Override
	public List<String> getXpathList() {
		List<String> protocolXmlDataXPaths = new ArrayList<String>(0);
		
		//protocolXmlDataXPaths.add("/protocol/title");
		//protocolXmlDataXPaths.add("/protocol/study-type");
		protocolXmlDataXPaths.add("/protocol/hipaa/is-phi-obtained/y/desc");
		protocolXmlDataXPaths.add("/protocol/hipaa/access-existing-phi/y/desc");
		protocolXmlDataXPaths.add("/protocol/original-study/approval-date");
		protocolXmlDataXPaths.add("/protocol/study-nature");
		protocolXmlDataXPaths.add("/emergency-use/basic-details/treatment-location");
		protocolXmlDataXPaths.add("/emergency-use/basic-details/test-article-name");
		protocolXmlDataXPaths.add("/emergency-use/basic-details/patient-full-name");
		protocolXmlDataXPaths.add("/emergency-use/basic-details/patient-diagnosis");
		protocolXmlDataXPaths.add("/protocol/epic/involve-chemotherapy");
		
		return protocolXmlDataXPaths;
	}
	
	@Override
	public List<String> getFormMetaDataXpathList(String formBaseTag) {
		List<String> protocolFormMetaXPaths = new ArrayList<String>(0);
		
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/expedited-category");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/exempt-category");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/adult-risk");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/ped-risk");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/hipaa-waived");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/consent-waived");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/consent-document-waived");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/suggested-next-review-type");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/non-compliance-assessment");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/summary/irb-determination/upirtso");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/cancel-reason");
		protocolFormMetaXPaths.add("/"+ formBaseTag +"/form-submit-date");
		
		return protocolFormMetaXPaths;
	}
	
	@Override
	public List<String> getMetaDataXpathList() {
		List<String> protocolMetaXPaths = new ArrayList<String>(0);
		
		protocolMetaXPaths.add("/protocol/title");
		protocolMetaXPaths.add("/protocol/study-type");
		protocolMetaXPaths.add("/protocol/original-study/approval-date");
		protocolMetaXPaths.add("/protocol/summary/irb-determination/review-period");
		protocolMetaXPaths.add("/protocol/summary/irb-determination/agenda-date");
		protocolMetaXPaths.add("/protocol/study-nature");
		protocolMetaXPaths.add("/protocol/summary/hospital-service-determinations/insurance-plan-code");
		protocolMetaXPaths.add("/protocol/summary/hospital-service-determinations/corporate-gurantor-code");
		protocolMetaXPaths.add("/protocol/subjects/age-ranges/age-range");
		protocolMetaXPaths.add("/protocol/epic/involve-chemotherapy");
		
		return protocolMetaXPaths;
	}
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	private Map<String, Object> getApprovalRelatedDate(ProtocolForm protocolForm){
		Map<String, List<String>> values = getFormService().getValuesFromXmlString(protocolForm.getProtocol().getMetaDataXml(), getMetaDataXpathList());
		
		Map<String, Object> approvalEndDateMap = new HashMap<String, Object>();
		
		//get review period
		int reiewPeriod = 0;
		
		try{
			List<String> reviewPeriodLst = values.get("/protocol/summary/irb-determination/review-period");
			
			reiewPeriod = (reviewPeriodLst!=null && !reviewPeriodLst.isEmpty())?Integer.valueOf(reviewPeriodLst.get(0)):0;
		} catch (Exception e){
			//e.printStackTrace();
			//logger.warn("Review period could not be found!");
		}
		
		//get agenda date from meta data
		String agendaDateStr = "";
		String agendaDatePlusReviewerPeriodMinusOneDayStr = "";
		
		try {
			agendaDateStr = getFormService().getSafeStringValueByKey(values,"/protocol/summary/irb-determination/agenda-date", "");
			
			if (!agendaDateStr.isEmpty()) {
				Date agendaDate = dateFormat.parse(agendaDateStr);
				
				LocalDate agendaLocalDate = new LocalDate(agendaDate);
				
				agendaDateStr = DateFormatUtil.formateDateToMDY(agendaDate);
				agendaDatePlusReviewerPeriodMinusOneDayStr = DateFormatUtil.formateDateToMDY(agendaLocalDate.plusMonths(reiewPeriod).minusDays(1).toDate());
			}
		} catch (Exception e) {
			//logger.warn("Agenda date could not be found!");
		}
		
		approvalEndDateMap.put("agendaDate", agendaDateStr);
		approvalEndDateMap.put("agendaDatePlusReviewerPeriodMinusOneDay", agendaDatePlusReviewerPeriodMinusOneDayStr);
		
		//get original review date
		String originalReviewDateSt = "";
		String originalReviewDatePlusReviewPeriodMinusOneDayStr = "";
		try{
			originalReviewDateSt = getFormService().getSafeStringValueByKey(values,"/protocol/original-study/approval-date", "");
			
			if (!originalReviewDateSt.isEmpty()) {
				Date originalReviewDate = dateFormat.parse(originalReviewDateSt);
				
				LocalDate originalReviewLocalDate = new LocalDate(originalReviewDate);
				
				originalReviewDateSt = DateFormatUtil.formateDateToMDY(originalReviewDate);
				originalReviewDatePlusReviewPeriodMinusOneDayStr = DateFormatUtil.formateDateToMDY(originalReviewLocalDate.plusMonths(reiewPeriod).minusDays(1).toDate());
			}
		} catch (Exception e){
			//don't care
		}
		
		if (protocolForm.getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION)) {
			approvalEndDateMap.put("originalReviewDate", agendaDateStr);
			approvalEndDateMap.put("originalReviewDatePlusReviewPeriodMinusOneDay", agendaDatePlusReviewerPeriodMinusOneDayStr);
		} else {
			approvalEndDateMap.put("originalReviewDate", originalReviewDateSt);
			approvalEndDateMap.put("originalReviewDatePlusReviewPeriodMinusOneDay", originalReviewDatePlusReviewPeriodMinusOneDayStr);
		}
		
		//get approval date
		Date approvalDate = new Date();
		
		LocalDate approvalLocalDate = new LocalDate(approvalDate);
		
		approvalEndDateMap.put("approvalDate", DateFormatUtil.formateDateToMDY(approvalDate));
		approvalEndDateMap.put("approvalEndDate", DateFormatUtil.formateDateToMDY(approvalLocalDate.plusMonths(reiewPeriod).toDate()));
		approvalEndDateMap.put("approvalEndDateMinusOneDay", DateFormatUtil.formateDateToMDY(approvalLocalDate.plusMonths(reiewPeriod).minusDays(1).toDate()));

		return approvalEndDateMap;
	}
	
	private Map<String, Object> getAgendaRelatedDate(Form form, Agenda agenda){
		ProtocolForm protocolForm = (ProtocolForm) form;
		Map<String, Object> agendaRelatedDateMap = new HashMap<String, Object>();
		
		List<String> reviewPeriodLst = null;
		
		try{
			reviewPeriodLst = getXmlProcessor().listElementStringValuesByPath("//irb-determination/review-period", protocolForm.getProtocol().getMetaDataXml());
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		int reiewPeriod = (reviewPeriodLst!=null && !reviewPeriodLst.isEmpty())?Integer.valueOf(reviewPeriodLst.get(0)):0;
		logger.debug("review period: " + reiewPeriod);
		
		Date agendaDate = agenda.getDate();
		
		LocalDate agendaLocalDate = new LocalDate(agendaDate);
		
		logger.debug("agendaDate: " + agendaDate);
		
		agendaRelatedDateMap.put("agendaDate", DateFormatUtil.formateDateToMDY(agendaDate));
		agendaRelatedDateMap.put("agendaDatePlusReviewerPeriodMinusOneDay", DateFormatUtil.formateDateToMDY(agendaLocalDate.plusMonths(reiewPeriod).minusDays(1).toDate()));
		
		return agendaRelatedDateMap;
	}
	
	private List<String> budgetCategoryList =Lists.newArrayList();{
		budgetCategoryList.add("budget-document-full");
		budgetCategoryList.add("budget-document-calendar-only");
		budgetCategoryList.add("budget-document-price-only");
		budgetCategoryList.add("budget-document-no-notes");
		budgetCategoryList.add("budget-document-notes-only");
	}
	
	private Map<String, String> riskPairMap = Maps.newHashMap();{
		riskPairMap.put("RISK_ADULT_1", "Minimal");
		riskPairMap.put("RISK_ADULT_2", "Greater than minimal");
		riskPairMap.put("RISK_ADULT_DEFERRED", "Deferred");
		riskPairMap.put("RISK_ADULT_NA", "N/A");
		riskPairMap.put("RISK_PED_1", "1");
		riskPairMap.put("RISK_PED_2", "2");
		riskPairMap.put("RISK_PED_3", "3");
		riskPairMap.put("RISK_PED_4", "4");
		riskPairMap.put("RISK_PED_DEFERRED", "Deferred");
		riskPairMap.put("RISK_PED_NA", "N/A");
		riskPairMap.put("N/A", "N/A");
	}
	
	private Map<String, String> nonComplianceDeterminationPairMap = Maps.newHashMap();{
		nonComplianceDeterminationPairMap.put("NA", "N/A");
		nonComplianceDeterminationPairMap.put("na", "N/A");
		nonComplianceDeterminationPairMap.put("N/A", "N/A");
		nonComplianceDeterminationPairMap.put("no", "No Evidence of Non-Compliance");
		nonComplianceDeterminationPairMap.put("yes", "Non-Compliance");
		nonComplianceDeterminationPairMap.put("yes_continuing", "Continuing Non-Compliance");
		nonComplianceDeterminationPairMap.put("yes_serious", "Serious Non-Compliance");
		nonComplianceDeterminationPairMap.put("yes_serious_continuing", "Serious and Continuing Non-Compliance");
	}

	@Override
	public Map<String, Object> getEmailData(Form form, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment, Agenda agenda) {
		ProtocolForm protocolForm = (ProtocolForm) form;
		
		Protocol protocol = protocolForm.getProtocol();
		
		String protocolFormBaseTag = protocolForm.getProtocolFormType().getBaseTag();
		
		String protocolformMetaData = protocolForm.getMetaDataXml();
		
		String protocolXmlDataXml = "";
		
		List<CommentType> commentTypeList = new ArrayList<CommentType>();
		commentTypeList.add(CommentType.CONTINGENCY);
		commentTypeList.add(CommentType.CONTINGENCY_MAJOR);
		commentTypeList.add(CommentType.CONTINGENCY_MINOR);

		//ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
		//List<ProtocolFormCommitteeComment> protocolFormCommitteeCommentList = new ArrayList<ProtocolFormCommitteeComment>();
		List<ProtocolFormCommitteeComment> protocolFormCommitteeCommentList = protocolFormCommitteeCommentDao.listCommentsByProtocolFormIdAndCommitteeAndInLetterOrNot(protocolForm.getId(), committee, true, commentTypeList);
		
		//List<ProtocolFormCommitteeComment> protocolFormCommitteeContigencyList = new ArrayList<ProtocolFormCommitteeComment>();
		List<ProtocolFormCommitteeComment> protocolFormCommitteeContigencyList = protocolFormCommitteeCommentDao.listCommentsByProtocolFormIdAndCommitteeAndCommentTypeList(protocolForm.getId(), committee, commentTypeList);
		
		//List<ProtocolFormCommitteeComment> fullBoardContigencyList = new ArrayList<ProtocolFormCommitteeComment>();
		List<ProtocolFormCommitteeComment> fullBoardContigencyList = protocolFormCommitteeCommentDao.listCommentsByProtocolFormIdAndCommitteeAndCommentTypeList(protocolForm.getId(), Committee.IRB_REVIEWER, commentTypeList);
		
		//List<ProtocolFormCommitteeComment> protocolFormCommitteeMinorContigencyList = new ArrayList<ProtocolFormCommitteeComment>();
		List<ProtocolFormCommitteeComment> protocolFormCommitteeMinorContigencyList = protocolFormCommitteeCommentDao.listCommentsByProtocolFormIdAndCommitteeAndCommentType(protocolForm.getId(), committee, CommentType.CONTINGENCY_MINOR);
		
		//List<ProtocolFormCommitteeComment> protocolFormCommitteeMajorContigencyList = new ArrayList<ProtocolFormCommitteeComment>();
		List<ProtocolFormCommitteeComment> protocolFormCommitteeMajorContigencyList = protocolFormCommitteeCommentDao.listCommentsByProtocolFormIdAndCommitteeAndCommentType(protocolForm.getId(), committee, CommentType.CONTINGENCY_MAJOR);

		List<ProtocolFormXmlDataDocument> protocolFormDocumentList = protocolFormXmlDataDocumentDao.getLatestDocumentExcludeCertainTypesByProtocolFormId(protocolForm.getId(), budgetCategoryList);
		//protocolFormDocumentList = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormIdAndStatus(protocolForm.getId(), Status.DRAFT);
		/*
		protocolFormDocumentList = protocolFormXmlDataDocumentDao.getLatestDocumentByProtocolFormId(protocolForm.getId());
		for(int i =0;i<protocolFormDocumentList.size();i++){
			ProtocolFormXmlDataDocument pfdd = protocolFormDocumentList.get(i);
			if(budgetCategoryList.contains(pfdd.getCategory())){
				protocolFormDocumentList.remove(i);
				i--;
			}
		}
		*/
		
		//List<ProtocolFormXmlDataDocumentWrapper> protocolFormDocumentByCommitteeList = new ArrayList<ProtocolFormXmlDataDocumentWrapper>();
		List<ProtocolFormXmlDataDocumentWrapper> protocolFormDocumentByCommitteeList = protocolFormXmlDataDocumentDao.listDocumentsByProtocolFormIdAndCommittee(protocolForm.getId(), committee);
		
		ProtocolFormXmlData protoclFormXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());
		
		protocolXmlDataXml = protoclFormXmlData.getXmlData();	

		Map<String, Object> finalTemplateValues = new HashMap<String, Object>();

		Map<String, List<String>> values = getFormService().getValuesFromXmlString(protocolXmlDataXml, getXpathList());
		
		Map<String, List<String>> formMetaDatavalues = getFormService().getValuesFromXmlString(protocolformMetaData, getFormMetaDataXpathList(protocolFormBaseTag));
		
		Map<String, List<String>> metaDataValues = getFormService().getValuesFromXmlString(protocol.getMetaDataXml(), getMetaDataXpathList());

		logger.debug("values: " + values);
		
		//String roleName = "";
		
		/*
		if (protoclFormXmlData.getProtocolFormXmlDataType().equals(ProtocolFormXmlDataType.HUMAN_SUBJECT_RESEARCH_DETERMINATION)){
			roleName = "Project Leader";
		} else {
			roleName = "Principal Investigator";
		}
		*/
		
		User piUser = null;
		User treatingPhysicianUser = null;
		List<User> studyCoordinatorLst = null;
		
		try{
			//piUser = getSpecifiRoleUser(protocolXmlDataXml, "Principal Investigator");
			List<User> piUserLst = getFormService().getUsersByKeywordAndSearchField("Principal Investigator", protocolXmlDataXml, UserSearchField.ROLE);
			
			piUser = (piUserLst != null)?piUserLst.get(0):null;
			
			List<User> treatingPhysicianUserLst = getFormService().getUsersByKeywordAndSearchField("Treating Physician", protocolXmlDataXml, UserSearchField.ROLE);
			
			treatingPhysicianUser = (treatingPhysicianUserLst != null)?treatingPhysicianUserLst.get(0):null;
			//treatingPhysicianUser = getSpecifiRoleUser(protocolXmlDataXml, "Treating Physician");
			studyCoordinatorLst = getFormService().getUsersByKeywordAndSearchField("Study Coordinator", protocolXmlDataXml, UserSearchField.ROLE);
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		boolean involvePed = false;
		boolean involveAdult = false;
		
		if (metaDataValues != null && metaDataValues.get("/protocol/subjects/age-ranges/age-range") != null && !metaDataValues.get("/protocol/subjects/age-ranges/age-range").isEmpty()) {
			
			if (metaDataValues.get("/protocol/subjects/age-ranges/age-range").contains("birth-6") || metaDataValues.get("/protocol/subjects/age-ranges/age-range").contains("7-17")) {
				involvePed = true;
			} else {
				involvePed = false;
			}
			
			if (metaDataValues.get("/protocol/subjects/age-ranges/age-range").contains("18+")) {
				involveAdult = true;
			} else {
				involveAdult = false;
			}
		}
		
		Date currentDate = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, +3);
		String threeYearsFromCurrentDate = DateFormatUtil.formateDateToMDY(cal.getTime());
		
		Calendar expCal = Calendar.getInstance();
		expCal.add(Calendar.YEAR, +1);
		expCal.add(Calendar.DATE, -1);
		String expeditedReviewEndDate = DateFormatUtil.formateDateToMDY(expCal.getTime());
		
		//String appContextLink = "/clara-webapp/static/styles/letters.css";
		
		String signature = (user != null && user.getSignaturePath()!=null && !user.getSignaturePath().isEmpty())?"<img src=\""+ user.getSignaturePath() +"\"/>":"";
		
		finalTemplateValues.put("committeeDesc", (committee != null)?committee.getDescription():"");
		finalTemplateValues.put("protocolId", String.valueOf(protocol.getId()));
		finalTemplateValues.put("submitDate", getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/form-submit-date", ""));
		finalTemplateValues.put("protocolFormType", protocolForm.getProtocolFormType().getDescription());
		finalTemplateValues.put("protocolTitle", org.apache.commons.lang.StringEscapeUtils.escapeHtml(getFormService().getSafeStringValueByKey(metaDataValues, "/protocol/title", "")));
		finalTemplateValues.put("protocolType", getFormService().getSafeStringValueByKey(metaDataValues, "/protocol/study-type", ""));
		
		String phiDesc = "";

		if (!getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/is-phi-obtained/y/desc","").isEmpty() && !getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/access-existing-phi/y/desc", "").isEmpty()) {
			phiDesc = getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/is-phi-obtained/y/desc", "") + " and " + getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/access-existing-phi/y/desc", "");
		} else {
			if (getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/is-phi-obtained/y/desc", "").isEmpty()) {
				phiDesc = getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/access-existing-phi/y/desc", "");
			}
			
			if (getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/access-existing-phi/y/desc", "").isEmpty()) {
				phiDesc = getFormService().getSafeStringValueByKey(values, "/protocol/hipaa/is-phi-obtained/y/desc", "");
			}
		}
		finalTemplateValues.put("phiDesc", phiDesc);
		finalTemplateValues.put("testArticleName", getFormService().getSafeStringValueByKey(values, "/emergency-use/basic-details/test-article-name", ""));
		finalTemplateValues.put("patientInitials", getFormService().getSafeStringValueByKey(values, "/emergency-use/basic-details/patient-full-name", ""));
		finalTemplateValues.put("patientDiagnosis", getFormService().getSafeStringValueByKey(values, "/emergency-use/basic-details/patient-diagnosis", ""));
		finalTemplateValues.put("treatmentLocation", getFormService().getSafeStringValueByKey(values, "/emergency-use/basic-details/treatment-location", ""));
		finalTemplateValues.put("hudOrNot", getFormService().getSafeStringValueByKey(metaDataValues, "/protocol/study-nature", ""));
		finalTemplateValues.put("piUser", (piUser!=null)?piUser:"");
		finalTemplateValues.put("treatingPhysicianUser", (treatingPhysicianUser!=null)?treatingPhysicianUser:"");
		finalTemplateValues.put("studyCoordinatorLst", studyCoordinatorLst);
		finalTemplateValues.put("reviewerName", (user!=null)?"<a href=\"mailto:"+ user.getPerson().getEmail() +"\">"+ user.getPerson().getFullname() +"</a>":"");
		finalTemplateValues.put("protocolLink", this.dashbaordLink(protocol.getId()));
		finalTemplateValues.put("formType", protocolForm.getProtocolFormType().getDescription());
		finalTemplateValues.put("queueLink", this.queueLink());
		finalTemplateValues.put("pedDetermination", (involvePed)?"The IRB determined the risk for children who enter this study to be Peds 1.":"");
		finalTemplateValues.put("adultDetermination", (involveAdult)?"The IRB determined the risk for adults who enter this study to be minimal.":"");
		finalTemplateValues.put("signature", signature);
		finalTemplateValues.put("involveChemotheray", getFormService().getSafeStringValueByKey(values, "/protocol/epic/involve-chemotherapy", ""));
		finalTemplateValues.put("emailComment", org.apache.commons.lang.StringEscapeUtils.escapeHtml(emailComment));
		finalTemplateValues.put("currentDate", DateFormatUtil.formateDateToMDY(currentDate));
		finalTemplateValues.put("threeYearsFromCurrentDate", threeYearsFromCurrentDate);
		finalTemplateValues.put("inLetterComments", protocolFormCommitteeCommentList);
		finalTemplateValues.put("protocolFormDocuments", protocolFormDocumentList);	
		finalTemplateValues.put("protocolFormCommitteeDocuments", protocolFormDocumentByCommitteeList);
		finalTemplateValues.put("contengencies", protocolFormCommitteeContigencyList);
		finalTemplateValues.put("fullBoardContengencies", fullBoardContigencyList);
		finalTemplateValues.put("minorContengencies", protocolFormCommitteeMinorContigencyList);
		finalTemplateValues.put("majorContengencies", protocolFormCommitteeMajorContigencyList);
		finalTemplateValues.put("expeditedReviewEndDate", expeditedReviewEndDate);
		finalTemplateValues.put("approvalEndDate", getApprovalRelatedDate(protocolForm).get("approvalEndDate"));
		finalTemplateValues.put("approvalEndDateMinusOneDay", getApprovalRelatedDate(protocolForm).get("approvalEndDateMinusOneDay"));
		finalTemplateValues.put("approvalDate", getApprovalRelatedDate(protocolForm).get("approvalDate"));
		finalTemplateValues.put("assignedBudgetReviewer", getReviewers(protocolformMetaData, Permission.ROLE_BUDGET_REVIEWER));
		finalTemplateValues.put("assignedCoverageReviewer", getReviewers(protocolformMetaData, Permission.ROLE_COVERAGE_REVIEWER));
		
		finalTemplateValues.put("originalReviewDate", (getApprovalRelatedDate(protocolForm).get("originalReviewDate")!=null)?getApprovalRelatedDate(protocolForm).get("originalReviewDate").toString():"N/A");
		
		finalTemplateValues.put("originalReviewDatePlusReviewPeriodMinusOneDay", (getApprovalRelatedDate(protocolForm).get("originalReviewDatePlusReviewPeriodMinusOneDay")!=null)?getApprovalRelatedDate(protocolForm).get("originalReviewDatePlusReviewPeriodMinusOneDay").toString():"N/A");
		if(agenda != null){
			finalTemplateValues.put("agenda", agenda);
			finalTemplateValues.put("agendaDate", getAgendaRelatedDate(form,agenda).get("agendaDate"));
			finalTemplateValues.put("agendaDatePlusReviewerPeriodMinusOneDay", getAgendaRelatedDate(form,agenda).get("agendaDatePlusReviewerPeriodMinusOneDay"));
		} else {
			finalTemplateValues.put("agendaDate", getApprovalRelatedDate(protocolForm).get("agendaDate"));
			finalTemplateValues.put("agendaDatePlusReviewerPeriodMinusOneDay", getApprovalRelatedDate(protocolForm).get("agendaDatePlusReviewerPeriodMinusOneDay"));
		}
		
		finalTemplateValues.put("cancelReason", getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/cancel-reason", ""));
		
		finalTemplateValues.put("expeditedCategory", getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/expedited-category", "N/A"));
		finalTemplateValues.put("exemptCategory", getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/exempt-category", "N/A"));
		finalTemplateValues.put("adultRisk", riskPairMap.get(getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/adult-risk", "N/A")));
		finalTemplateValues.put("pedRisk", riskPairMap.get(getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/ped-risk", "N/A")));
		finalTemplateValues.put("nextReviewType", getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/suggested-next-review-type", "N/A"));
		finalTemplateValues.put("hipaaWaiver", (!getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/hipaa-waived", "").isEmpty())?getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/hipaa-waived", "").toLowerCase():"");
		finalTemplateValues.put("consentWaiver", (!getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/consent-waived", "").isEmpty())?getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/consent-waived", "").toLowerCase():"");
		finalTemplateValues.put("consentDocWaiver", (!getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/consent-document-waived", "").isEmpty())?getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/consent-document-waived", "").toLowerCase():"");
		finalTemplateValues.put("nonComplianceAssessment", (!getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/non-compliance-assessment", "").isEmpty())?nonComplianceDeterminationPairMap.get(getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/non-compliance-assessment", "")):"");
		finalTemplateValues.put("upirtso", (!getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/upirtso", "").isEmpty())?getFormService().getSafeStringValueByKey(formMetaDatavalues, "/"+ protocolFormBaseTag +"/summary/irb-determination/upirtso", "").toLowerCase():"");
		
		if (attributeRawValues != null){
			finalTemplateValues.put("cpt", (attributeRawValues.get("CPT")!=null)?attributeRawValues.get("CPT"):"");
			//finalTemplateValues.put("cdm", (attributeRawValues.get("CDM")!=null)?attributeRawValues.get("CDM"):"");
			finalTemplateValues.put("description", (attributeRawValues.get("DESCRIPTION")!=null)?attributeRawValues.get("DESCRIPTION"):"");
			//finalTemplateValues.put("cost", (attributeRawValues.get("COST")!=null)?attributeRawValues.get("COST"):"");
			//finalTemplateValues.put("offer", (attributeRawValues.get("OFFER")!=null)?attributeRawValues.get("OFFER"):"");
			//finalTemplateValues.put("price", (attributeRawValues.get("PRICE")!=null)?attributeRawValues.get("PRICE"):"");
			finalTemplateValues.put("note", (attributeRawValues.get("NOTE")!=null)?attributeRawValues.get("NOTE"):"");
		}
		
		finalTemplateValues.put("currentUser", user);
		
		return finalTemplateValues;
	}
	
	/*
	private List<String> getRealMailToList(ProtocolForm protocolForm){
		List<String> realMailToLst = new ArrayList<String>();
		
		String lookupPath = "//treatment-location";
		List<String> values = null;
		
		try{
			values = getXmlProcessor().listElementStringValuesByPath(lookupPath, protocolForm.getMetaDataXml());
		}catch(Exception e){
			e.printStackTrace();
		}
		
		String treatmentLocation = (values.size()>0)?values.get(0):"";
		
		try{
			//String testPath = "file:src/test/java/edu/uams/clara/webapp/protocol/service/real-recipient.xml";
			//Document doc = xmlProcessor.loadXmlFileToDOM(testPath);
			Document doc = getXmlProcessor().loadXmlFileToDOM(emailRealRecipients);
			XPath xpath = getXmlProcessor().getXPathInstance();
			
			String path = "/forms/form[@type=\""+ protocolForm.getProtocolFormType() +"\"]/dependencies/treatment-locations/dependency[@value=\""+ treatmentLocation +"\"]/recipients/recipient";
			logger.debug(path);
			NodeList nlst = (NodeList) xpath.evaluate(path,
					doc, XPathConstants.NODESET);

			for (int i=0; i < nlst.getLength(); i++){
				Element currentEl = (Element) nlst.item(i);
				
				String finalS = "{\"address\":\""+ currentEl.getAttribute("type") + "_" + currentEl.getAttribute("value") +"\",\"type\":\""+ currentEl.getAttribute("type") +"\",\"desc\":\""+ currentEl.getAttribute("name") +"\"}";
				
				realMailToLst.add(finalS);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		logger.debug("realMailToLst: " + realMailToLst);
		return realMailToLst;
	}
	*/
	

	@Override
	public EmailTemplate setRealSubjectAndReceipt(Form form,
			EmailTemplate emailTemplate, Committee committee, Map<String, Object> attributeRawValues) {
		ProtocolForm protocolForm = (ProtocolForm) form;
		
		Map<String, String> subjectAttributsValues = new HashMap<String, String>();
		subjectAttributsValues.put("{protocolId}", String.valueOf(protocolForm.getProtocol().getId()));
		subjectAttributsValues.put("{protocolFormType}", protocolForm.getProtocolFormType().getDescription());
		if (committee != null) {
			subjectAttributsValues.put("{committee}", committee.getDescription());
		}
		
		emailTemplate.setRealSubject(fillSubject(emailTemplate.getSubject(), subjectAttributsValues));
		
		emailTemplate = this.resolveEmailTemplate(protocolForm, emailTemplate, committee, attributeRawValues);
		
		logger.debug("real recipient: " + emailTemplate.getRealRecipient());
		return emailTemplate;
	}
	
	@Override
	public Map<String, Object> getProtocolEmailData(Protocol protocol,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment) {
		String protocolMetaDataXml = protocol.getMetaDataXml();	

		Map<String, Object> finalTemplateValues = new HashMap<String, Object>();

		Map<String, List<String>> values = getFormService().getValuesFromXmlString(protocolMetaDataXml, getMetaDataXpathList());
		
		Date currentDate = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, +14);
		String twoWeeksFromCurrentDate = DateFormatUtil.formateDateToMDY(cal.getTime());
		
		Calendar oneBeforeCal = Calendar.getInstance();
		oneBeforeCal.add(Calendar.DATE, +7);
		String oneWeekFromCurrentDate = DateFormatUtil.formateDateToMDY(oneBeforeCal.getTime());
		
		Calendar oneWeekBeforeCal = Calendar.getInstance();
		oneWeekBeforeCal.add(Calendar.DATE, -7);
		String oneWeekBeforeCurrentDate = DateFormatUtil.formateDateToMDY(oneWeekBeforeCal.getTime());
		
		User piUser = null;
		User treatingPhysicianUser = null;
		List<User> studyCoordinatorLst = null;
		
		try{
			List<User> piUserLst = getFormService().getUsersByKeywordAndSearchField("Principal Investigator", protocolMetaDataXml, UserSearchField.ROLE);
			
			piUser = (piUserLst != null)?piUserLst.get(0):null;
			
			List<User> treatingPhysicianUserLst = getFormService().getUsersByKeywordAndSearchField("Treating Physician", protocolMetaDataXml, UserSearchField.ROLE);
			
			treatingPhysicianUser = (treatingPhysicianUserLst != null)?treatingPhysicianUserLst.get(0):null;

			studyCoordinatorLst = getFormService().getUsersByKeywordAndSearchField("Study Coordinator", protocolMetaDataXml, UserSearchField.ROLE);
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		String approvalEndDate = "";
		String agendaDate = "";
		
		try{
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			approvalEndDate = xmlHandler.getSingleStringValueByXPath(protocolMetaDataXml, "/protocol/most-recent-study/approval-end-date");
			
			agendaDate = xmlHandler.getSingleStringValueByXPath(protocolMetaDataXml, "/protocol/summary/irb-determination/agenda-date");
			
			if (agendaDate.isEmpty()) {
				agendaDate = xmlHandler.getSingleStringValueByXPath(protocolMetaDataXml, "/protocol/original-study/IRBAgendaDate");
			}
			
		} catch (Exception e){
			//
		}
		
		//String appContextLink = "/clara-webapp/static/styles/letters.css";
		
		String signature = (user != null && user.getSignaturePath()!=null && !user.getSignaturePath().isEmpty())?"<img src=\""+ user.getSignaturePath() +"\"/>":"";
		
		finalTemplateValues.put("committeeDesc", (committee != null)?committee.getDescription():"");
		finalTemplateValues.put("originalReviewDate", getFormService().getSafeStringValueByKey(values, "/protocol/original-study/approval-date", ""));
		finalTemplateValues.put("planCode", getFormService().getSafeStringValueByKey(values, "/protocol/summary/hospital-service-determinations/insurance-plan-code", ""));
		finalTemplateValues.put("gurantorCode", getFormService().getSafeStringValueByKey(values, "/protocol/summary/hospital-service-determinations/corporate-gurantor-code", ""));
		finalTemplateValues.put("involveChemotheray", getFormService().getSafeStringValueByKey(values, "/protocol/epic/involve-chemotherapy", ""));
		finalTemplateValues.put("twoWeeksFromCurrentDate", twoWeeksFromCurrentDate);
		finalTemplateValues.put("oneWeekFromCurrentDate", oneWeekFromCurrentDate);
		finalTemplateValues.put("oneWeekBeforeCurrentDate", oneWeekBeforeCurrentDate);
		finalTemplateValues.put("protocolId", String.valueOf(protocol.getId()));
		finalTemplateValues.put("protocolTitle", org.apache.commons.lang.StringEscapeUtils.escapeHtml(getFormService().getSafeStringValueByKey(values, "/protocol/title", "")));
		finalTemplateValues.put("protocolType", getFormService().getSafeStringValueByKey(values, "/protocol/study-type", ""));
		finalTemplateValues.put("emailComment", emailComment);
		finalTemplateValues.put("currentDate", DateFormatUtil.formateDateToMDY(currentDate));
		finalTemplateValues.put("approvalEndDate", approvalEndDate);
		finalTemplateValues.put("protocolLink", this.dashbaordLink(protocol.getId()));
		//finalTemplateValues.put("currentUser", user);
		finalTemplateValues.put("piUser", (piUser!=null)?piUser:"");
		finalTemplateValues.put("month", (attributeRawValues!=null && (attributeRawValues.get("month")!=null))?attributeRawValues.get("month").toString():"");
		finalTemplateValues.put("treatingPhysicianUser", (treatingPhysicianUser!=null)?treatingPhysicianUser:"");
		finalTemplateValues.put("studyCoordinatorLst", studyCoordinatorLst);
		finalTemplateValues.put("signature", signature);
		finalTemplateValues.put("agendaDate", agendaDate);
		
		if(attributeRawValues!=null && !attributeRawValues.isEmpty()) {
			if (attributeRawValues.get("itSecurityUserinfo") != null) {
				finalTemplateValues.put("itSecurityUserinfo", attributeRawValues.get("itSecurityUserinfo"));
			}
			
			if (attributeRawValues.get("closeReason") != null) {
				finalTemplateValues.put("closeReason", attributeRawValues.get("closeReason"));
			}
		}
		
		return finalTemplateValues;
	}
	
	@Override
	public EmailTemplate setObjectRealSubjectAndReceipt(Protocol protocol,
			EmailTemplate emailTemplate, Committee committee,
			Map<String, Object> attributeRawValues) {		
		
		Map<String, String> subjectAttributsValues = new HashMap<String, String>();
		subjectAttributsValues.put("{protocolId}", String.valueOf(protocol.getId()));
		
		emailTemplate.setRealSubject(fillSubject(emailTemplate.getSubject(), subjectAttributsValues));
		
		List<String> real = new ArrayList<String>();
		List<String> realCc = Lists.newArrayList();
		
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
					if (er.getAddress().contains("studyPI")){
						List<String> piStaffLst = getReviewersOrPIMailToList(committee, protocol.getMetaDataXml(), "studyPI");
						if (piStaffLst != null && !piStaffLst.isEmpty()){
							for (String s : piStaffLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
									logReceipientTolist.add(s);
								} else {
									realCc.add(s);
									logReceipientCclist.add(s);
								}
								
							}
							
							//logReceipient = getLogReceipientLst(logReceipient, er, real, realCc);
						}
						//realRecipient += getReviewersOrPIFromProtocolMailToList(committee, protocol, "studyPI").toString();
					}
					
					if (er.getAddress().contains("budgetAdmin")){
						List<String> budgetAdminLst = getReviewersOrPIMailToList(committee, protocol.getMetaDataXml(), "budgetAdmin");
						if (budgetAdminLst != null && !budgetAdminLst.isEmpty()){
							for (String s : budgetAdminLst){
								if (mapEntry.getKey().equals("to")){
									real.add(s);
									logReceipientTolist.add(s);
								} else {
									realCc.add(s);
									logReceipientCclist.add(s);
								}
							}
							
							//logReceipient = getLogReceipientLst(logReceipient, er, real, realCc);
						}
						//realRecipient += getReviewersOrPIFromProtocolMailToList(committee, protocol, "budgetAdmin").toString();
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

		//logger.debug("real recipient: " + emailTemplate.getRealRecipient());
		//logger.debug("real cc recipient: " + emailTemplate.getRealCCRecipient());
		return emailTemplate;
	}
	
	private List<String> getAgendaRelatedMailToList(Agenda agenda, String type){
		List<String> realMailToLst = new ArrayList<String>();
		
		if (type.equals("irbChair")){
			List<IRBReviewer> chairLst = irbReviewerDao.listIRBChairsByIRBRoster(agenda.getIrbRoster());
			
			for (IRBReviewer irbReviewer : chairLst){
				String finalS = "{\"address\":\"INDIVIDUAL_" + irbReviewer.getUser().getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ irbReviewer.getUser().getPerson().getFullname() +"\"}";
				
				realMailToLst.add(finalS);
				
				if (!irbReviewer.getUser().getAlternateEmail().isEmpty()) {
					String altFinalS = "{\"address\":\"INDIVIDUAL_" + irbReviewer.getUser().getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ irbReviewer.getUser().getPerson().getFullname() +"\"}";
					realMailToLst.add(altFinalS);
				}
			}
		}
		
		if (type.equals("committeeMember")){
			List<IRBReviewer> memberLst = irbReviewerDao.listIRBReviewersByIRBRoster(agenda.getIrbRoster());
			
			for (IRBReviewer irbReviewer : memberLst){
				if (irbReviewer.getUser().getPerson().getEmail() != null && !irbReviewer.getUser().getPerson().getEmail().isEmpty()){
					String finalS = "{\"address\":\"INDIVIDUAL_" + irbReviewer.getUser().getPerson().getEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ irbReviewer.getUser().getPerson().getFullname() +"\"}";
					
					realMailToLst.add(finalS);
					
					if (!irbReviewer.getUser().getAlternateEmail().isEmpty()) {
						String altFinalS = "{\"address\":\"INDIVIDUAL_" + irbReviewer.getUser().getAlternateEmail() +"\",\"type\":\"INDIVIDUAL\",\"desc\":\""+ irbReviewer.getUser().getPerson().getFullname() +"\"}";
						realMailToLst.add(altFinalS);
					}
				}
			}
		}
		
		//logger.debug("realAgendaMailToLst: " + realMailToLst);
		return realMailToLst;
	}
	
	@Override
	public EmailTemplate setAgendaRealSubjectAndReceipt(Agenda agenda,
			EmailTemplate emailTemplate, Committee committee,
			Map<String, Object> attributeRawValues) {
		Map<String, String> subjectAttributsValues = new HashMap<String, String>();
		subjectAttributsValues.put("{agendaDate}", DateFormatUtil.formateDateToMDY(agenda.getDate()));
		
		emailTemplate.setRealSubject(fillSubject(emailTemplate.getSubject(), subjectAttributsValues));
		
		List<String> real = new ArrayList<String>();
		List<String> realCc = Lists.newArrayList();
		
		List<EmailRecipient> toEmailRecipients = Lists.newArrayList();
		List<EmailRecipient> ccEmailRecipients = Lists.newArrayList();
		
		Map<String, List<EmailRecipient>> receipientsMap = Maps.newHashMap();
		
		try{
			if (!emailTemplate.getTo().isEmpty())
				toEmailRecipients = getEmailService().getEmailRecipients(emailTemplate.getTo());
		} catch (Exception e){
			e.printStackTrace();
		}
		
		receipientsMap.put("to", toEmailRecipients);
		
		try{
			if (!emailTemplate.getCc().isEmpty())
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
					if (er.getAddress().contains("irbChair")){
						List<String> chairLst = getAgendaRelatedMailToList(agenda, "irbChair");
						if (chairLst != null && !chairLst.isEmpty()){
							for (String s : chairLst){
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
					
					if (er.getAddress().contains("committeeMember")){
						List<String> memberLst = getAgendaRelatedMailToList(agenda, "committeeMember");
						if (memberLst != null && !memberLst.isEmpty()){
							for (String s : memberLst){
								//logger.debug("key: " + mapEntry.getKey());
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
					
					if (er.getAddress().contains("realIRBOffice")){
						List<String> committeeLst = getNextCommitteeMailToList(null, Committee.IRB_OFFICE);
						
						if (committeeLst != null && !committeeLst.isEmpty()){
							for (String s : committeeLst){
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
		
		//logger.debug("real agenda recipient: " + emailTemplate.getRealRecipient());
		//logger.debug("real cc recipient: " + emailTemplate.getRealCCRecipient());
		return emailTemplate;
	}
	
	@Override
	public Map<String, Object> getAgendaEmailData(Agenda agenda,
			Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment) {
		Map<String, Object> finalTemplateValues = new HashMap<String, Object>();
		
		//String appContextLink = "/clara-webapp/static/styles/letters.css";
		
		String agendaLink = getAppHost() + "/clara-webapp/agendas";
		String agendaSummaryLink = getAppHost() + "/clara-webapp/agendas/"+ agenda.getId() +"/summary";
		
		String reason = (attributeRawValues != null && !attributeRawValues.isEmpty() && attributeRawValues.get("reason") != null)?attributeRawValues.get("reason").toString():"";
		
		String signature = (user.getSignaturePath()!=null && !user.getSignaturePath().isEmpty())?"<img src=\""+ user.getSignaturePath() +"\"/>":"";
		
		finalTemplateValues.put("agendaDate", DateFormatUtil.formateDateToMDY(agenda.getDate()));
		finalTemplateValues.put("currentDate", DateFormatUtil.formateDateToMDY(new Date()));
		finalTemplateValues.put("agendaLink", agendaLink);
		finalTemplateValues.put("agendaSummaryLink", agendaSummaryLink);
		finalTemplateValues.put("reason", reason);
		finalTemplateValues.put("signature", signature);
		
		return finalTemplateValues;
	}	

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}
	
	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public ProtocolFormCommitteeCommentDao getProtocolFormCommitteeCommentDao() {
		return protocolFormCommitteeCommentDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeCommentDao(
			ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao) {
		this.protocolFormCommitteeCommentDao = protocolFormCommitteeCommentDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}
	
	public String getEmailRealRecipients() {
		return emailRealRecipients;
	}

	public void setEmailRealRecipients(String emailRealRecipients) {
		this.emailRealRecipients = emailRealRecipients;
	}

	@Override
	public Map<String, Object> getEmailData(Form form, Committee committee, Map<String, Object> attributeRawValues,
			User user, String emailComment) {
		// TODO Auto-generated method stub
		return getEmailData(form, committee, attributeRawValues, user, emailComment, null);
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}
	
	@Autowired(required=true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}
}
