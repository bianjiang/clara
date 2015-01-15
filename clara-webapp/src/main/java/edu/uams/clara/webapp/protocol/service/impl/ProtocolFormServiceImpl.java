package edu.uams.clara.webapp.protocol.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.businesslogic.BusinessObjectStatusHelperContainer;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.security.ObjectAclService;
import edu.uams.clara.webapp.common.service.UserService;
import edu.uams.clara.webapp.common.service.form.FormService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormType;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolFormServiceImpl implements ProtocolFormService {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormService.class);
	
	private FormService formService;
	
	private ObjectAclService objectAclService;
	
	private ProtocolFormStatusDao protocolFormStatusDao;

	private Map<ProtocolFormType, Set<ProtocolFormXmlDataType>> copyLists = new HashMap<ProtocolFormType, Set<ProtocolFormXmlDataType>>();
	{
		Set<ProtocolFormXmlDataType> lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.PROTOCOL);
		lists.add(ProtocolFormXmlDataType.BUDGET);
		lists.add(ProtocolFormXmlDataType.PHARMACY);
		// lists.add(ProtocolFormXmlDataType.COMMITTEE_ASSIGNMENT_QUEUE);
		copyLists.put(ProtocolFormType.NEW_SUBMISSION, lists);

		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.HUMAN_SUBJECT_RESEARCH_DETERMINATION);
		copyLists.put(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION,
				lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.STAFF);
		copyLists.put(ProtocolFormType.STAFF,
				lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.PRIVACY_BOARD);
		copyLists.put(ProtocolFormType.PRIVACY_BOARD,
				lists);

		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.EMERGENCY_USE);
		copyLists.put(ProtocolFormType.EMERGENCY_USE, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.CONTINUING_REVIEW);
		copyLists.put(ProtocolFormType.CONTINUING_REVIEW, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.REPORTABLE_NEW_INFORMATION);
		copyLists.put(ProtocolFormType.REPORTABLE_NEW_INFORMATION, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.MODIFICATION);
		lists.add(ProtocolFormXmlDataType.BUDGET);
		lists.add(ProtocolFormXmlDataType.PHARMACY);
		copyLists.put(ProtocolFormType.MODIFICATION, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.STUDY_CLOSURE);
		copyLists.put(ProtocolFormType.STUDY_CLOSURE, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.STUDY_RESUMPTION);
		copyLists.put(ProtocolFormType.STUDY_RESUMPTION, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.OFFICE_ACTION);
		copyLists.put(ProtocolFormType.OFFICE_ACTION, lists);
		
		lists = new HashSet<ProtocolFormXmlDataType>();
		lists.add(ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE_RENEWAL);
		copyLists.put(ProtocolFormType.HUMANITARIAN_USE_DEVICE_RENEWAL, lists);

	}
	
	private Map<String, String> protocolFormXPathPairMap = new HashMap<String, String>();
	{
		// newSubmissionXPathPairs.put("/protocol/submission-type",
		// "/protocol/submission-type");
		protocolFormXPathPairMap.put("/protocol/title", "/protocol/title");
		protocolFormXPathPairMap.put("/protocol/study-type",
				"/protocol/study-type");
		protocolFormXPathPairMap.put("/protocol/study-nature",
				"/protocol/study-nature");
		protocolFormXPathPairMap.put("/protocol/crimson/has-budget",
				"/protocol/crimson/has-budget");
		protocolFormXPathPairMap.put("/protocol/crimson/crimsonId",
				"/protocol/crimson/crimsonId");
		protocolFormXPathPairMap.put("/protocol/crimson/crimson-status",
				"/protocol/crimson/crimson-status");
		protocolFormXPathPairMap.put("/protocol/accrual-goal",
				"/protocol/subjects/total-number");
		protocolFormXPathPairMap.put("/protocol/sites",
				"/protocol/sites");
		protocolFormXPathPairMap.put("/protocol/staffs", "/protocol/staffs");
		protocolFormXPathPairMap.put("/protocol/modification", "/protocol/modification");
	}
	
	private static Map<String, String> staffRoleMap = new HashMap<String, String>();
	{
		staffRoleMap.put("Blinded CIBIC Rater", "Support Staff");
		staffRoleMap.put("Co-Principal Investigator", "Co-Investigator");
		staffRoleMap.put("Consultant", "Support Staff");
		staffRoleMap.put("Data Manager", "Support Staff");
		staffRoleMap.put("Dissertaion Chair", "Support Staff");
		staffRoleMap.put("Laboratory Staff", "Support Staff");
		staffRoleMap.put("Nurse", "Support Staff");
		staffRoleMap.put("Pharmacist", "Research Pharmacist");
		staffRoleMap.put("Primary Contact", "Study Coordinator");
		staffRoleMap.put("Research Assistant", "Support Staff");
		staffRoleMap.put("Research Coordinator", "Support Staff");
		staffRoleMap.put("Research Librarian", "Support Staff");
		staffRoleMap.put("Research Nurse", "Support Staff");
		staffRoleMap.put("Researcher", "Support Staff");
		staffRoleMap.put("Responsible Staff", "Support Staff");
		staffRoleMap.put("Statistical Consultant", "Support Staff");
		staffRoleMap.put("Statistician", "Support Staff");
		staffRoleMap.put("Study CoordinatorPrimary Contact", "Study Coordinator");
		staffRoleMap.put("Study Physician", "Support Staff");
		staffRoleMap.put("Study Staff", "Support Staff");
		staffRoleMap.put("Subject Advocate", "Support Staff");
		staffRoleMap.put("principal investigator", "Principal Investigator");
		staffRoleMap.put("study coordinator", "Study Coordinator");
	}
	
	private String updateMigratedStaffInfo(String protocolFormXmlData){
		List<String> roles = null;
		Map<String, Object> resultMap = null;
		
		try{
			roles = xmlProcessor.listElementStringValuesByPath("//staff/user/roles/role", protocolFormXmlData);
			resultMap = xmlProcessor.deleteElementByPath("//staff[user/roles/role='Former PI' or user/roles/role='Former Staff' or user/roles/role='Not employed at UAMS' or user/roles/role='former staff']", protocolFormXmlData);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		if (resultMap != null){
			protocolFormXmlData = resultMap.get("finalXml").toString();
		}
		
		if (roles != null){
			for (String role : roles){
				if (staffRoleMap.containsKey(role)){
					protocolFormXmlData = protocolFormXmlData.replace("<role>"+role+"</role>", "<role>"+staffRoleMap.get(role)+"</role>");
				}
			}
		}
		
		return protocolFormXmlData;
	}

	private UserService userService;

	private XmlProcessor xmlProcessor;

	private ProtocolDao protocolDao;

	private ProtocolFormDao protocolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;

	private BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer;
	
	private String unlockBudget(String budgetXmlData) {
		try {
			budgetXmlData = xmlProcessor.replaceAttributeValueByPathAndAttributeName("/budget", "locked", budgetXmlData, "false");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return budgetXmlData;
	}

	@Override
	public ProtocolForm createRevision(ProtocolForm protocolForm) {

		Date today = new Date();
		ProtocolForm nv = new ProtocolForm();
		nv.setCreated(today);
		nv.setParent(protocolForm.getParent());
		nv.setProtocol(protocolForm.getProtocol());
		nv.setProtocolFormType(protocolForm.getProtocolFormType());
		nv.setMetaDataXml(protocolForm.getMetaDataXml());

		nv = protocolFormDao.saveOrUpdate(nv);

		Map<ProtocolFormXmlDataType, ProtocolFormXmlData> protocolFormXmlDatas = new HashMap<ProtocolFormXmlDataType, ProtocolFormXmlData>(
				0);
		
		
		for (ProtocolFormXmlDataType pfxdt : copyLists.get(protocolForm
				.getProtocolFormType())) {
			ProtocolFormXmlData opfxd = protocolForm
					.getTypedProtocolFormXmlDatas().get(pfxdt);

			if (opfxd == null)
				continue;

			ProtocolFormXmlData npfxd = new ProtocolFormXmlData();
			npfxd.setCreated(today);
			npfxd.setProtocolForm(nv);
			npfxd.setParent(opfxd);
			npfxd.setProtocolFormXmlDataType(opfxd.getProtocolFormXmlDataType());
			npfxd.setXmlData(opfxd.getXmlData());
			npfxd.setRetired(false);

			npfxd = protocolFormXmlDataDao.saveOrUpdate(npfxd);

			protocolFormXmlDatas.put(npfxd.getProtocolFormXmlDataType(), npfxd);
		}
		
		nv.setTypedProtocolFormXmlDatas(protocolFormXmlDatas);

		return nv;

	}
	
	/*
	private boolean isExistingInNewSubmission(String listPath,
			String originalXml) {

		String resultXml = null;

		try {
			resultXml = xmlProcessor.listElementsByPath(listPath, originalXml,
					true);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (resultXml.equals("<list/>"))
			return false;
		else
			return true;
	}
	*/

	private List<ProtocolFormStatusEnum> protocolFormStatuses = new ArrayList<ProtocolFormStatusEnum>();
	{
		protocolFormStatuses.add(ProtocolFormStatusEnum.IRB_APPROVED);
		protocolFormStatuses.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		protocolFormStatuses.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
	};
	
	private Map<ProtocolFormType, String> questionVersionMap = Maps.newHashMap();{
		//questionVersionMap.put(ProtocolFormType.NEW_SUBMISSION, "<question-version-id>1.0</question-version-id>");
		questionVersionMap.put(ProtocolFormType.CONTINUING_REVIEW, "1.3");
		questionVersionMap.put(ProtocolFormType.MODIFICATION, "1.0");
		questionVersionMap.put(ProtocolFormType.STUDY_CLOSURE, "1.0");
		questionVersionMap.put(ProtocolFormType.REPORTABLE_NEW_INFORMATION, "1.0");
		//questionVersionMap.put(ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION, "<question-version-id>1.0</question-version-id>");
		//questionVersionMap.put(ProtocolFormType.EMERGENCY_USE, "<question-version-id>1.0</question-version-id>");
		questionVersionMap.put(ProtocolFormType.HUMANITARIAN_USE_DEVICE_RENEWAL, "1.0");
		questionVersionMap.put(ProtocolFormType.STAFF, "1.0");
		questionVersionMap.put(ProtocolFormType.STUDY_RESUMPTION, "1.0");
		questionVersionMap.put(ProtocolFormType.OFFICE_ACTION, "1.1");
	}

	@Override
	public ProtocolFormXmlData createNewForm(ProtocolFormType protocolFormType,
			long protocolId) throws XPathExpressionException, IOException,
			SAXException {
		Date created = new Date();
		Protocol p = protocolDao.findById(protocolId);

		p.setProtocolIdentifier("" + p.getId());

		p = protocolDao.saveOrUpdate(p);

		String protocolFormXmlStringStart = "<" + protocolFormType.getBaseTag()
				+ " id=\"" + p.getId() + "\" identifier=\""
				+ p.getProtocolIdentifier() + "\" type=\""+ protocolFormType.getDescription() +"\">";
		String protocolFormXmlStringEnd = "</" + protocolFormType.getBaseTag()
				+ ">";
		
		String versionIdXmlString = "<question-version-id>" + questionVersionMap.get(protocolFormType) + "</question-version-id>";
		
		String protocolFormXmlString = versionIdXmlString;

		String finalXmlString = "";
		
		ProtocolForm originalProtocolForm = null;
		ProtocolFormXmlData originalProtocolFormXmlData = null;
		ProtocolFormXmlData originalBudgetXmlData = null;
		ProtocolFormXmlData originalPharmacyXmlData = null;

		try {
			List<ProtocolForm> protocolFormLst = protocolFormDao.listProtocolFormsByProtocolIdAndProtocolFormType(protocolId, ProtocolFormType.MODIFICATION);
					
			if (protocolFormLst == null || protocolFormLst.size() == 0){
				/*
				originalProtocolForm = protocolFormDao
						.getProtocolFormByProtocolIdAndProtocolFormType(protocolId,
								ProtocolFormType.NEW_SUBMISSION);
								*/
				List<ProtocolFormType> protocolFormTypes = new ArrayList<ProtocolFormType>();
				protocolFormTypes.add(ProtocolFormType.NEW_SUBMISSION);
				protocolFormTypes.add(ProtocolFormType.ARCHIVE);
				
				originalProtocolForm = protocolFormDao.getProtocolFormByProtocolIdAndProtocolFormTypes(protocolId, protocolFormTypes);
				
				originalProtocolFormXmlData = originalProtocolForm
						.getTypedProtocolFormXmlDatas().get(
								(originalProtocolForm.getFormType().equals("NEW_SUBMISSION"))?ProtocolFormXmlDataType.PROTOCOL:ProtocolFormXmlDataType.ARCHIVE);
				
				originalBudgetXmlData = originalProtocolForm
						.getTypedProtocolFormXmlDatas().get(
								ProtocolFormXmlDataType.BUDGET);
				originalPharmacyXmlData = originalProtocolForm
						.getTypedProtocolFormXmlDatas().get(
								ProtocolFormXmlDataType.PHARMACY);
				
			} else {
				originalProtocolForm = protocolFormDao.getLatestProtocolFormByProtocolIdAndProtocolFormType(protocolId, ProtocolFormType.MODIFICATION);
				originalProtocolFormXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(originalProtocolForm.getId(), ProtocolFormXmlDataType.MODIFICATION);
				originalBudgetXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(originalProtocolForm.getId(), ProtocolFormXmlDataType.BUDGET);
				originalPharmacyXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(originalProtocolForm.getId(), ProtocolFormXmlDataType.PHARMACY);

			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			logger.debug("no previous form carried over... ignore... don't care...");
		}
		
		switch(protocolFormType){
		case CONTINUING_REVIEW:
			String newSubmissionPulledXml = "";
			newSubmissionPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			
			newSubmissionPulledXml += formService.pullFromOtherForm("/protocol/accrual-goal",
					p.getMetaDataXml());
			
			newSubmissionPulledXml += formService.pullFromOtherForm("/protocol/original-study", p.getMetaDataXml());
			newSubmissionPulledXml += formService.pullFromOtherForm("/protocol/most-recent-study", p.getMetaDataXml());
			newSubmissionPulledXml += formService.pullFromOtherForm("/protocol/funding", p.getMetaDataXml());

			protocolFormXmlString += newSubmissionPulledXml;
			break;
		case MODIFICATION:
			protocolFormXmlString += "<committee-review>" + formService.pullFromOtherForm("//committee-review/committee", originalProtocolForm.getMetaDataXml()) + "</committee-review>";
			//protocolFormXmlString += formService.pullFromOtherForm("/protocol/staffs", p.getMetaDataXml());
			break;
		case STUDY_CLOSURE:
			String newSubmissionXml = "";
			/*
			ProtocolForm crProtocolForm = null;
			
			String crPulledXmlData = "";
			
			try {
				crProtocolForm = protocolFormDao
						.getLatestProtocolFormByProtocolIdAndProtocolFormTypeAndProtocolFormStatues(
								protocolId, ProtocolFormType.CONTINUING_REVIEW,
								protocolFormStatuses);
			} catch (Exception e){
				e.printStackTrace();
			}
			
			if (crProtocolForm != null){
				ProtocolFormXmlData pfxd = crProtocolForm
						.getTypedProtocolFormXmlDatas().get(
								ProtocolFormXmlDataType.CONTINUING_REVIEW);

				crPulledXmlData += formService.pullFromOtherForm(
						"/continuing-review/subject-accrual", pfxd.getXmlData());
				crPulledXmlData += formService.pullFromOtherForm(
						"/continuing-review/study-report", pfxd.getXmlData());
				crPulledXmlData += formService.pullFromOtherForm(
						"/continuing-review/funding-source", pfxd.getXmlData());
			}
			*/
			
			newSubmissionXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			newSubmissionXml += formService.pullFromOtherForm("/protocol/original-study", p.getMetaDataXml());
			newSubmissionXml += formService.pullFromOtherForm("/protocol/most-recent-study", p.getMetaDataXml());
			
			protocolFormXmlString += newSubmissionXml;
			break;
		case REPORTABLE_NEW_INFORMATION:
			protocolFormXmlString = "<is-reportable>n</is-reportable>";
			
			String nsfToRNIPulledXml = "";
			nsfToRNIPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			
			protocolFormXmlString += nsfToRNIPulledXml;
			break;
		case STUDY_RESUMPTION:
			String nsfToSRPulledXml = "";
			nsfToSRPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			
			protocolFormXmlString += nsfToSRPulledXml;
			break;
		case OFFICE_ACTION:
			String nsfToOAPulledXml = "";
			nsfToOAPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			
			protocolFormXmlString += nsfToOAPulledXml;
			break;
		case AUDIT:
			String metaToAuditPulledXml = "";
			metaToAuditPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			
			protocolFormXmlString += metaToAuditPulledXml;
			break;
		case STAFF:
			String metaToStaffPulledXml = "";
			metaToStaffPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			
			protocolFormXmlString += metaToStaffPulledXml;
			break;
		case HUMANITARIAN_USE_DEVICE_RENEWAL:
			String nsfPulledXml = formService.pullFromOtherForm(
					"/protocol/title", p.getMetaDataXml());
			nsfPulledXml += formService.pullFromOtherForm("/protocol/staffs",
					p.getMetaDataXml());
			//initialApplicationPulledXmlData += formService.pullFromOtherForm(
			//		"/protocol/device-name", p.getMetaDataXml());
			//initialApplicationPulledXmlData += formService.pullFromOtherForm(
			//		"/protocol/device-desc", p.getMetaDataXml());
			//initialApplicationPulledXmlData += formService.pullFromOtherForm(
			//		"/protocol/treatment-location", p.getMetaDataXml());

			protocolFormXmlString += nsfPulledXml;
			break;
		case EMERGENCY_USE:
			ProtocolForm pf = protocolFormDao
			.getProtocolFormByProtocolIdAndProtocolFormType(protocolId,
					ProtocolFormType.EMERGENCY_USE);
			ProtocolFormXmlData pfxd = pf.getTypedProtocolFormXmlDatas().get(
					ProtocolFormXmlDataType.EMERGENCY_USE);
		
			String euBasicPulledXml = "<basic-details>";
			euBasicPulledXml += formService.pullFromOtherForm(
					"/emergency-use/basic-details/eu-title", pfxd.getXmlData());
			euBasicPulledXml += formService.pullFromOtherForm(
					"/emergency-use/basic-details/test-article-name",
					pfxd.getXmlData());
			euBasicPulledXml += formService.pullFromOtherForm(
					"/emergency-use/basic-details/test-article-desc",
					pfxd.getXmlData());
			euBasicPulledXml += formService.pullFromOtherForm(
					"/emergency-use/basic-details/patient-full-name",
					pfxd.getXmlData());
			euBasicPulledXml += formService.pullFromOtherForm(
					"/emergency-use/basic-details/patient-diagnosis",
					pfxd.getXmlData());
			euBasicPulledXml += formService.pullFromOtherForm(
					"/emergency-use/basic-details/treatment-location",
					pfxd.getXmlData());
			euBasicPulledXml += "<ieu-or-eu>emergency-use-follow-up-report</ieu-or-eu>";
			euBasicPulledXml += "</basic-details>";
		
			euBasicPulledXml += formService.pullFromOtherForm("/emergency-use/staffs",
					pfxd.getXmlData());
		
			protocolFormXmlString += "<type>follow-up</type>" + euBasicPulledXml;
			break;
		default:
			break;
		}

		if (protocolFormXmlString == null) {
			finalXmlString = protocolFormXmlStringStart
					+ protocolFormXmlStringEnd;
		} else {
			finalXmlString = protocolFormXmlStringStart + protocolFormXmlString
					+ protocolFormXmlStringEnd;
		}

		ProtocolForm f = new ProtocolForm();
		f.setProtocolFormType(protocolFormType);
		f.setProtocol(p);

		/*if (protocolFormType.equals(ProtocolFormType.MODIFICATION)) {
			f.setMetaDataXml(originalProtocolForm.getMetaDataXml());
		} else {		
			f.setMetaDataXml(finalXmlString);
		}*/
		
		f.setMetaDataXml(finalXmlString);
		f.setParent(f);
		f.setCreated(created);
		f.setLocked(false);

		f = protocolFormDao.saveOrUpdate(f);

		ProtocolFormXmlData fxd = new ProtocolFormXmlData();
		fxd.setProtocolForm(f);

		Map<String, Object> resultMap = new HashMap<String, Object>(0);
		if (protocolFormType.equals(ProtocolFormType.MODIFICATION)) {
			if (originalProtocolForm.getProtocolFormType().equals(ProtocolFormType.ARCHIVE)){
				String protocolFormXmlDataXml = xmlProcessor.mergeByXPaths(
						finalXmlString, originalProtocolFormXmlData.getXmlData(),
						XmlProcessor.Operation.UPDATE_IF_EXIST,
						protocolFormXPathPairMap);
				protocolFormXmlDataXml = updateMigratedStaffInfo(protocolFormXmlDataXml);
				
				protocolFormXmlDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/migrated", protocolFormXmlDataXml, "y");
				
				protocolFormXmlDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/initial-mod", protocolFormXmlDataXml, "y");
				
				protocolFormXmlDataXml = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/question-version-id", protocolFormXmlDataXml, questionVersionMap.get(protocolFormType));
				
				/*//delete staffs
				resultMap=xmlProcessor.deleteElementByPath("/protocol/staffs", protocolFormXmlDataXml);
				protocolFormXmlDataXml = resultMap.get("finalXml").toString();
				
				//add staffs from protocol metadata
				Map<String,String> addProtocolStaffMap = new HashMap<String,String>();
				addProtocolStaffMap.put("/protocol/staffs", "/protocol/staffs");
				protocolFormXmlDataXml = xmlProcessor.mergeByXPaths(
						protocolFormXmlDataXml, p.getMetaDataXml(),
						XmlProcessor.Operation.REPLACE_BY_XPATH_LIST,
						addProtocolStaffMap);*/
				
				fxd.setXmlData(protocolFormXmlDataXml);
			} else {
				String originalXmlData = originalProtocolFormXmlData.getXmlData();
				originalXmlData = xmlProcessor.replaceAttributeValueByPathAndAttributeName("/protocol", "type", originalXmlData, protocolFormType.getDescription());
				
				List<ProtocolFormStatusEnum> protocolFormStatusLst = Lists.newArrayList();
				protocolFormStatusLst.add(ProtocolFormStatusEnum.COMPLIANCE_APPROVED);
				
				try {
					ProtocolForm complianceApprovedMod = protocolFormDao.getLatestProtocolFormByProtocolIdAndProtocolFormTypeAndProtocolFormStatues(protocolId, ProtocolFormType.MODIFICATION, protocolFormStatusLst);
					
					if (complianceApprovedMod != null) {
						originalXmlData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/compliance-approved", originalXmlData, "y");
					}
				} catch (Exception e) {
					
				}
				
				
				//delete staffs
				resultMap = xmlProcessor.deleteElementByPath("/protocol/staffs", originalXmlData);
				
				try {
					resultMap = xmlProcessor.deleteElementByPath("/protocol/initial-mod", resultMap.get("finalXml").toString());
				} catch (Exception e) {
					logger.info("initial-mod element does not exist.");
				}
				
				originalXmlData = resultMap.get("finalXml").toString();
				
				//add staffs from protocol metadata
				Map<String,String> addProtocolStaffMap = new HashMap<String,String>();
				addProtocolStaffMap.put("/protocol/staffs", "/protocol/staffs");
				originalXmlData = xmlProcessor.mergeByXPaths(
						originalXmlData, p.getMetaDataXml(),
						XmlProcessor.Operation.REPLACE_BY_XPATH_LIST,
						addProtocolStaffMap);
				
				//remove the modification info in previous modification
				resultMap=xmlProcessor.deleteElementByPath("/protocol/modification", originalXmlData);
				originalXmlData = resultMap.get("finalXml").toString();
				
				originalXmlData = xmlProcessor.replaceOrAddNodeValueByPath("/protocol/question-version-id", originalXmlData, questionVersionMap.get(protocolFormType));
				
				//if NCT number is entered in summary, it needs to be pulled to modification form
				try {
					XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
					
					String nctNumber = xmlHandler.getSingleStringValueByXPath(p.getMetaDataXml(), "/protocol/summary/clinical-trials-determinations/nct-number");
					
					if (!nctNumber.isEmpty()) {
						originalXmlData = xmlHandler.replaceOrAddNodeValueByPath("/protocol/misc/is-registered-at-clinical-trials", originalXmlData, "y");
						originalXmlData = xmlHandler.replaceOrAddNodeValueByPath("/protocol/misc/is-registered-at-clinical-trials/nct-number", originalXmlData, nctNumber);
					}
				} catch (Exception e) {
					//don't care
				}
				
				fxd.setXmlData(originalXmlData);
			}
			
			fxd.setParent(originalProtocolFormXmlData);
		} else {
			fxd.setXmlData(finalXmlString);
			fxd.setParent(fxd);
		}
		
		fxd.setProtocolFormXmlDataType(f.getProtocolFormType()
				.getDefaultProtocolFormXmlDataType());
		fxd.setCreated(created);
		fxd = protocolFormXmlDataDao.saveOrUpdate(fxd);

		ProtocolFormXmlData bfxd = null;
		if (protocolFormType.equals(ProtocolFormType.MODIFICATION)
				&& originalBudgetXmlData != null) {
			bfxd = new ProtocolFormXmlData();
			bfxd.setProtocolForm(f);
			bfxd.setXmlData(this.unlockBudget(originalBudgetXmlData.getXmlData()));
			bfxd.setParent(originalBudgetXmlData);
			bfxd.setProtocolFormXmlDataType(originalBudgetXmlData
					.getProtocolFormXmlDataType());
			bfxd.setCreated(created);
			bfxd = protocolFormXmlDataDao.saveOrUpdate(bfxd);
		}
		
		ProtocolFormXmlData phfxd = null;
		if (protocolFormType.equals(ProtocolFormType.MODIFICATION)
				&& originalPharmacyXmlData != null) {
			phfxd = new ProtocolFormXmlData();
			phfxd.setProtocolForm(f);
			phfxd.setXmlData(originalPharmacyXmlData.getXmlData());
			phfxd.setParent(originalPharmacyXmlData);
			phfxd.setProtocolFormXmlDataType(originalPharmacyXmlData
					.getProtocolFormXmlDataType());
			phfxd.setCreated(created);
			phfxd = protocolFormXmlDataDao.saveOrUpdate(phfxd);
		}

		Map<ProtocolFormXmlDataType, ProtocolFormXmlData> protocolFormXmlDatas = new HashMap<ProtocolFormXmlDataType, ProtocolFormXmlData>(
				0);
		protocolFormXmlDatas.put(fxd.getProtocolFormXmlDataType(), fxd);
		if (bfxd != null)
			protocolFormXmlDatas.put(bfxd.getProtocolFormXmlDataType(), bfxd);
		if (phfxd != null)
			protocolFormXmlDatas.put(phfxd.getProtocolFormXmlDataType(), phfxd);

		f.setTypedProtocolFormXmlDatas(protocolFormXmlDatas);

		User currentUser = userService.getCurrentUser();

		triggerPIAction("CREATE", f, currentUser, null);
		
		objectAclService.updateObjectAclByUser(Protocol.class, protocolId, currentUser);

		return fxd;
	}
	
	private Set<String> pathList = Sets.newHashSet();{
		pathList.add("/protocol/crimson/has-budget");
		pathList.add("/protocol/budget/potentially-billed");
		pathList.add("/protocol/budget/need-budget-in-clara");
		pathList.add("/protocol/modification/to-modify-section/is-audit");
		pathList.add("/protocol/modification/to-modify-section/involve-change-in/budget-modified");
		pathList.add("/protocol/modification/to-modify-section/involve-change-in/contract-modified");
		pathList.add("/protocol/modification/to-modify-section/involve-change-in/pi-modified");
		pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure");
		pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy");
		pathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects");
		pathList.add("/protocol/modification/to-modify-section/amendment-to-injury");
		pathList.add("/protocol/modification/to-modify-section/submit-to-medicare");
		pathList.add("/protocol/modification/to-modify-section/conduct-under-uams");
		pathList.add("/protocol/study-nature");
		pathList.add("/protocol/study-nature/hud-use/where");
		pathList.add("/protocol/study-type");
		pathList.add("/protocol/site-responsible");
		pathList.add("/protocol/migrated");
		pathList.add("/protocol/modification/to-modify-section/complete-budget-migration");
		pathList.add("/protocol/need-budget");
		pathList.add("/emergency-use/basic-details/ieu-or-eu");
	}
	
	@Override
	public String workFlowDetermination(ProtocolFormXmlData protocolFormXmlData) {
		String workflow = "";
		
		String protocolFormXmlDataString = protocolFormXmlData.getXmlData();
		
		try{
			Map<String, List<String>> values = getXmlProcessor().listElementStringValuesByPaths(pathList, protocolFormXmlDataString);
			
			switch(protocolFormXmlData.getProtocolFormXmlDataType()){
			case PROTOCOL:
				String studyNatureValue = (values.get("/protocol/study-nature") != null && !values.get("/protocol/study-nature").isEmpty())?values.get("/protocol/study-nature").get(0):"";
				
				List<String> siteIds = xmlProcessor.getAttributeValuesByPathAndAttributeName("/protocol/study-sites/site", protocolFormXmlDataString, "site-id");

				if (studyNatureValue.equals("hud-use")){
					String hudSite = (values.get("/protocol/study-nature/hud-use/where") != null && !values.get("/protocol/study-nature/hud-use/where").isEmpty())?values.get("/protocol/study-nature/hud-use/where").get(0):"";
					
					if (hudSite.equals("uams")) {
						workflow = "HUD";
					} else if (hudSite.equals("ach/achri")) {
						workflow = "ACH";
					}
					
				} else {
					String primaryResValue = (values.get("/protocol/site-responsible") != null && !values.get("/protocol/site-responsible").isEmpty())?values.get("/protocol/site-responsible").get(0):"";
					
					if (primaryResValue.equals("ach-achri") || (siteIds != null && !siteIds.isEmpty() && (siteIds.contains("2") || siteIds.contains("1")))){
						workflow = "ACH";
					} else if (primaryResValue.equals("uams")){
						String studyTypeValue = (values.get("/protocol/study-type") != null && !values.get("/protocol/study-type").isEmpty())?values.get("/protocol/study-type").get(0):"";
						
						if (studyTypeValue.equals("investigator-initiated")){
							workflow = "INVESTIGATOR";
						} else {
							workflow = "NOT_INVESTIGATOR";
						}
					} else if (primaryResValue.equals("other")){
						String needBudget = (values.get("/protocol/need-budget") != null && !values.get("/protocol/need-budget").isEmpty())?values.get("/protocol/need-budget").get(0):"";
						
						if (needBudget.equals("y")) {
							workflow = "NOT_INVESTIGATOR";
						} else {
							workflow = "INVESTIGATOR";
						}
					}
				}
				break;
			case EMERGENCY_USE:
				String euValue = (values.get("/emergency-use/basic-details/ieu-or-eu") != null && !values.get("/emergency-use/basic-details/ieu-or-eu").isEmpty())?values.get("/emergency-use/basic-details/ieu-or-eu").get(0):"";
				
				if (euValue.equals("intended-emergency-use")){
					workflow = "INTENDED";
				} else if (euValue.equals("emergency-use-follow-up-report")){
					workflow = "FOLLOW-UP";
				}
				break;
			case MODIFICATION:
				try {
					//String hasCrimsonBudget = (values.get("/protocol/crimson/has-budget") != null && !values.get("/protocol/crimson/has-budget").isEmpty())?values.get("/protocol/crimson/has-budget").get(0):"";
					//String potentiallyBilled = (values.get("/protocol/budget/potentially-billed") != null && !values.get("/protocol/budget/potentially-billed").isEmpty())?values.get("/protocol/budget/potentially-billed").get(0):"";
					//String needBudgetInClara = (values.get("/protocol/budget/need-budget-in-clara") != null && !values.get("/protocol/budget/need-budget-in-clara").isEmpty())?values.get("/protocol/budget/need-budget-in-clara").get(0):"";
					String isAudit = (values.get("/protocol/modification/to-modify-section/is-audit") != null && !values.get("/protocol/modification/to-modify-section/is-audit").isEmpty())?values.get("/protocol/modification/to-modify-section/is-audit").get(0):"";
					String budgetModified = (values.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified") != null && !values.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-change-in/budget-modified").get(0):"";
					//String contractModified = (values.get("/protocol/modification/to-modify-section/involve-change-in/contract-modified") != null && !values.get("/protocol/modification/to-modify-section/involve-change-in/contract-modified").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-change-in/contract-modified").get(0):"";
					//String piModified = (values.get("/protocol/modification/to-modify-section/involve-change-in/pi-modified") != null && !values.get("/protocol/modification/to-modify-section/involve-change-in/pi-modified").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-change-in/pi-modified").get(0):"";
					String procedureDeleted = (values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure") != null && !values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure").get(0):"";
					String pharmacyDeleted = (values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy") != null && !values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy").get(0):"";
					//String subjectDeleted = (values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects") != null && !values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects").isEmpty())?values.get("/protocol/modification/to-modify-section/involve-addition-deletion-of/subjects").get(0):"";
					//String amendToInjury = (values.get("/protocol/modification/to-modify-section/amendment-to-injury") != null && !values.get("/protocol/modification/to-modify-section/amendment-to-injury").isEmpty())?values.get("/protocol/modification/to-modify-section/amendment-to-injury").get(0):"";
					//String submitToMedicare = (values.get("/protocol/modification/to-modify-section/submit-to-medicare") != null && !values.get("/protocol/modification/to-modify-section/submit-to-medicare").isEmpty())?values.get("/protocol/modification/to-modify-section/submit-to-medicare").get(0):"";
					String conductUnderUams = (values.get("/protocol/modification/to-modify-section/conduct-under-uams") != null && !values.get("/protocol/modification/to-modify-section/conduct-under-uams").isEmpty())?values.get("/protocol/modification/to-modify-section/conduct-under-uams").get(0):"";
					String studyType = (values.get("/protocol/study-type") != null && !values.get("/protocol/study-type").isEmpty())?values.get("/protocol/study-type").get(0):"";
					String respSite = (values.get("/protocol/site-responsible") != null && !values.get("/protocol/site-responsible").isEmpty())?values.get("/protocol/site-responsible").get(0):"";
					String migrated = (values.get("/protocol/migrated") != null && !values.get("/protocol/migrated").isEmpty())?values.get("/protocol/migrated").get(0):"";
					String budgetConvert = (values.get("/protocol/modification/to-modify-section/complete-budget-migration") != null && !values.get("/protocol/modification/to-modify-section/complete-budget-migration").isEmpty())?values.get("/protocol/modification/to-modify-section/complete-budget-migration").get(0):"";
					String needBudget = (values.get("/protocol/need-budget") != null && !values.get("/protocol/need-budget").isEmpty())?values.get("/protocol/need-budget").get(0):"";
					
					if (isAudit.equals("y")) {
						workflow = "IRB";
					} else if (migrated.equals("y")) {
						if (budgetConvert.equals("y")) {
							workflow = "COMPLIANCE";
						} else {
							workflow = "IRB";
						}
					} else {
						//if (budgetModified.equals("y") || contractModified.equals("y") || piModified.equals("y") || procedureDeleted.equals("y") || pharmacyDeleted.equals("y") || subjectDeleted.equals("y") || amendToInjury.equals("y") || submitToMedicare.equals("y")) {
						if (budgetModified.equals("y") || procedureDeleted.equals("y") || pharmacyDeleted.equals("y")) {
							if (respSite.equals("uams")) {
								if (studyType.equals("investigator-initiated")) {
									if (conductUnderUams.equals("y")) {
										workflow = "GATEKEEPER";
									} else {
										workflow = "BUDGET_ONLY";
									}
								} else if (studyType.equals("industry-sponsored") || studyType.equals("cooperative-group")) {
									workflow = "BUDGET_ONLY";
								}
							} else {
								if (needBudget.equals("y")) {
									workflow = "BUDGET_ONLY";
								} else {
									workflow = "IRB";
								}
							}
						} else {
							workflow = "IRB";
						}
					}
					
				} catch (Exception e) {
					
				}
				break;
			default:
				break;
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		logger.debug("workflow: " + workflow);
		return workflow;
	}

	/*
	@Override
	public String isIndustryOrInvestigatorInitiated(
			ProtocolFormXmlData protocolFormXmlData) {
		String x = "/protocol/study-type";

		List<String> values = null;
		try {
			values = xmlProcessor.listElementStringValuesByPath(x,
					protocolFormXmlData.getXmlData());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String value = (values != null && !values.isEmpty()) ? values.get(0) : "";

		String isIndustryOrInvestigatorInitiated = "";

		if (value != null && !value.isEmpty()) {
			isIndustryOrInvestigatorInitiated = (value
					.equals("investigator-initiated")) ? "INVESTIGATOR"
					: "NOT_INVESTIGATOR";
		}

		return isIndustryOrInvestigatorInitiated;
	}
	
	@Override
	public String isUAMSOrACH(ProtocolFormXmlData protocolFormXmlData) {
		String x = "/protocol/site-responsible";
		
		List<String> values = null;
		try {
			values = xmlProcessor.listElementStringValuesByPath(x,
					protocolFormXmlData.getXmlData());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String value = (values != null && values.size() > 0) ? values.get(0) : "";
		
		String isUamsOrAch = "";
		
		if (value != null && !value.isEmpty()) {
			if (value.equals("ach-achri")) isUamsOrAch = "ACH";
			if (value.equals("uams")) isUamsOrAch = "UAMS";
		}
		
		return isUamsOrAch;
	}

	@Override
	public String isNotificationOrFollowUp(
			ProtocolFormXmlData protocolFormXmlData) {
		String workflow = "";

		List<String> values = null;
		try {
			values = xmlProcessor.listElementStringValuesByPath("//ieu-or-eu",
					protocolFormXmlData.getXmlData());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String formType = values != null ? values.get(0) : "";

		if (formType.equals("intended-emergency-use"))
			workflow = "INTENDED";
		if (formType.equals("emergency-use-follow-up-report"))
			workflow = "FOLLOW-UP";

		logger.debug("eu workflow: " + workflow);
		return workflow;
	}
	
	@Override
	public String isBudgetModificationOrNot(
			ProtocolFormXmlData protocolFormXmlData) {
		String workflow = "";

		List<String> values = null;
		try {
			values = xmlProcessor.listElementStringValuesByPath("//modification/to-modify-sections",
					protocolFormXmlData.getXmlData());
		} catch (Exception e) {
			e.printStackTrace();
		}

		String formType = values != null ? values.get(0) : "";

		if (formType.equals("budget-documents-only") || formType.equals("other")){
			workflow = "BUDGET_ONLY";
		} else {
			workflow = "IRB";
		}

		logger.debug("modification workflow: " + workflow);
		return workflow;
	}
	*/

	@Override
	public void triggerPIAction(String action, ProtocolForm protocolForm,
			User currentUser, String message) throws XPathExpressionException,
			IOException, SAXException {
		businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(
				protocolForm.getProtocolFormType().toString()).triggerAction(
				protocolForm, Committee.PI, currentUser, action, message, null);
	}

	@Override
	public void triggerPIAction(String action, String condition,
			String workflow, ProtocolForm protocolForm, User currentUser,
			String message) throws XPathExpressionException, IOException,
			SAXException {
		businessObjectStatusHelperContainer.getBusinessObjectStatusHelper(
				protocolForm.getProtocolFormType().toString()).triggerAction(
				protocolForm, Committee.PI, currentUser, action, condition,
				workflow, message, null);
	}
	
	@Value("${irbfees.xml.uri}")
	private String irbFeeRulesXmlFilePath;
	
	@Override
	public void generateIRBFees(ProtocolFormXmlData protocolFormXmlData) throws IOException,
			SAXException, XPathExpressionException {
		
		String value = "";
		
		List<String> lst = xmlProcessor.listElementStringValuesByPath("/protocol/study-type", protocolFormXmlData.getXmlData());
		List<String> primarySiteLst = xmlProcessor.listElementStringValuesByPath("/protocol/site-responsible", protocolFormXmlData.getXmlData());
		List<String> investigatorDescLst = xmlProcessor.listElementStringValuesByPath("/protocol/study-type/investigator-initiated/investigator-description", protocolFormXmlData.getXmlData());
		List<String> otherCategoriesLst = xmlProcessor.listElementStringValuesByPath("/protocol/study-type/investigator-initiated/sub-type", protocolFormXmlData.getXmlData());
		
		if (primarySiteLst != null && !primarySiteLst.isEmpty()){
			if (primarySiteLst.get(0).equals("ach-achri")){
				value = "ach-achri";
			}
		}
		
		if (!value.equals("ach-achri")){
			if (lst != null && !lst.isEmpty() && (lst.get(0).equals("industry-sponsored") || lst.get(0).equals("cooperative-group"))){
				value = lst.get(0);
			} else {
				if (investigatorDescLst != null && !investigatorDescLst.isEmpty() && investigatorDescLst.get(0).equals("student-fellow-resident-post-doc")){
					value = investigatorDescLst.get(0);
				} else {
					if (otherCategoriesLst != null && !otherCategoriesLst.isEmpty()){
						if (otherCategoriesLst.contains("other")){
							value = "other";
						} else if (otherCategoriesLst.contains("federal-sub-contract-from-another-institution") || otherCategoriesLst.contains("federal-grant-directly-to-uams-achri")){
							if (otherCategoriesLst.indexOf("federal-sub-contract-from-another-institution") >= 0){
								value = "federal-sub-contract-from-another-institution";
							} else if (otherCategoriesLst.indexOf("federal-grant-directly-to-uams-achri") >= 0){
								value = "federal-grant-directly-to-uams-achri";
							}
						} else if (otherCategoriesLst.contains("industry-support-full-funding") || otherCategoriesLst.contains("industry-support-partial-funding") || otherCategoriesLst.contains("industry-support-drug-device-only")){				
							if (otherCategoriesLst.indexOf("industry-support-full-funding") >= 0){
								value = "industry-support-full-funding";
							} else if (otherCategoriesLst.indexOf("industry-support-partial-funding") >= 0){
								value = "industry-support-partial-funding";
							} else if (otherCategoriesLst.indexOf("industry-support-drug-device-only") >= 0){
								value = "industry-support-drug-device-only";
							}
						} else if (otherCategoriesLst.contains("non-federal-grant")){
							value = "non-federal-grant";
						} else if (otherCategoriesLst.contains("internal-support") || otherCategoriesLst.contains("no-designated-support")){
							if (otherCategoriesLst.indexOf("internal-support") >= 0){
								value = "internal-support";
							} else if (otherCategoriesLst.indexOf("no-designated-support") >= 0){
								value = "no-designated-support";
							}
						}
					}
				}
			}
		}
		
		
		String irbFeeListXml="<irb-fees type=\""+ value +"\">";
		
		XPath xPath = xmlProcessor.getXPathInstance();

		Document irbFeesXmlDocument = xmlProcessor
				.loadXmlFileToDOM(irbFeeRulesXmlFilePath);

		String feeXpath = "/irb-fees/category[@value=\""+ value +"\"]/form";
		
		NodeList feeNodes = (NodeList) xPath.evaluate(feeXpath,
				irbFeesXmlDocument, XPathConstants.NODESET);

		if (feeNodes != null && feeNodes.getLength() > 0){
			for (int i=0; i < feeNodes.getLength(); i++){
				Element feeElement = (Element) feeNodes.item(i);
				if (feeElement != null){
					Element feeParentEl = (Element) feeElement.getParentNode();
					
					irbFeeListXml += "<category><name>"+ feeParentEl.getAttribute("name") + "(" + feeElement.getAttribute("type") + ")</name><fee>" +  feeElement.getAttribute("fee") + "</fee></category>";
				}	
			}
		}
		
		irbFeeListXml +="</irb-fees>";
		logger.debug("final IRB fee list:" + irbFeeListXml);
		
		Map<String, Object> resultMap = new HashMap<String, Object>(0);
		String irbFeeByTypePath = "/"+ protocolFormXmlData.getProtocolForm().getProtocolFormType().getBaseTag() +"/irb-fees[@type = \""+ value +"\"]";
		
		String irbFeePath = "/"+ protocolFormXmlData.getProtocolForm().getProtocolFormType().getBaseTag() +"/irb-fees";
		
		String getExistingIrbFeeByTypeElements = xmlProcessor.listElementsByPath(irbFeeByTypePath, protocolFormXmlData.getXmlData(),
				true);

		String getExistingIrbFeeElements = xmlProcessor.listElementsByPath(irbFeePath, protocolFormXmlData.getXmlData(),
				true);
		
		String protocolFormXmlDataString = protocolFormXmlData.getXmlData();
		
		if (!getExistingIrbFeeElements.equals("<list/>")) {
			if (getExistingIrbFeeByTypeElements.equals("<list/>")) {
				resultMap = xmlProcessor.deleteElementByPath(irbFeePath, protocolFormXmlData.getXmlData());
				
				protocolFormXmlDataString = resultMap.get("finalXml").toString();
				
				try{
					resultMap = xmlProcessor.addElementByPath(irbFeePath, protocolFormXmlDataString, irbFeeListXml, false);
					
					protocolFormXmlDataString = resultMap.get("finalXml").toString();
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		} else {
			try{
				resultMap = xmlProcessor.addElementByPath(irbFeePath, protocolFormXmlDataString, irbFeeListXml, false);
				
				protocolFormXmlDataString = resultMap.get("finalXml").toString();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		/*
		if (!getExistingIrbFeeElements.equals("<list/>")){
			resultMap = xmlProcessor.deleteElementByPath(irbFeePath, protocolFormXmlData.getXmlData());
			
			protocolFormXmlData.setXmlData(resultMap.get("finalXml").toString());
			protocolFormXmlData = protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
		}	
		
		try{
			resultMap = xmlProcessor.addElementByPath(irbFeePath, protocolFormXmlData.getXmlData(), irbFeeListXml, false);
		} catch (Exception e){
			e.printStackTrace();
		}
		*/
		
		protocolFormXmlData.setXmlData(protocolFormXmlDataString);
		
		protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);

	}
	
	private String generateIRBExternalExpenses(long protocolFormId) throws IOException, SAXException, XPathExpressionException {
		ProtocolForm protocolForm =  protocolFormDao.findById(protocolFormId);
		ProtocolFormXmlData protocolFormXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());
		
		XPath xPath = xmlProcessor.getXPathInstance();
		Document currentProtocolFormXmlDataDom = xmlProcessor.loadXmlStringToDOM(protocolFormXmlData.getXmlData());
		
		String expenseXml = "";
		
		String categoryXPath = "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/irb-fees/category";
		
		NodeList categoriesList = (NodeList) (xPath.evaluate(categoryXPath,
				currentProtocolFormXmlDataDom, XPathConstants.NODESET));
		
		if (categoriesList.getLength() > 0){
			for (int i=0; i<categoriesList.getLength(); i++){
				if (categoriesList.item(i).getNodeType() == Node.ELEMENT_NODE){
					Element currentCategoryElement = (Element) categoriesList
							.item(i);
					String nameValue = currentCategoryElement.getFirstChild().getTextContent();
					String feeValue = currentCategoryElement.getLastChild().getTextContent();
					
					String externalOrNot = "true";
					if (feeValue.isEmpty()){
						externalOrNot = "false";
					}
					
					String expenseType = "Initial Cost";
					if (!nameValue.contains("New Submission")){
						expenseType = "Invoicable";
					}
					
					expenseXml += "<expense fa=\"0\" faenabled=\"false\" external=\""+ externalOrNot +"\" count=\"1\" cost=\""+ feeValue +"\" type=\""+ expenseType +"\" subtype=\"IRB Fee\" description=\"IRB Fee\" notes=\""+ nameValue +"\"></expense>";
				}
			}
		}
		
		return expenseXml;
	}
	
	@Override
	public void updateIRBExpensesInBudget(ProtocolForm protocolForm,
			String xmlData) {
		logger.debug("Updating budget expenses ...");
		try{
			ProtocolFormXmlData budgetXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.BUDGET);
			
			if (budgetXmlData != null && budgetXmlData.getXmlData() != null && !budgetXmlData.getXmlData().isEmpty()){
				Document irbFeesDom = xmlProcessor.loadXmlStringToDOM(xmlData);
				XPath xPath = xmlProcessor.getXPathInstance();
				NodeList categoriesList = (NodeList) (xPath.evaluate("/protocol/irb-fees/category",
						irbFeesDom, XPathConstants.NODESET));
				
				Document budgetXmlDom = xmlProcessor.loadXmlStringToDOM(budgetXmlData.getXmlData());
				xPath.reset();
				
				for (int i=0; i<categoriesList.getLength(); i++){
					Element currentCategoryElement = (Element) categoriesList
							.item(i);
					String nameValue = currentCategoryElement.getFirstChild().getTextContent();
					String feeValue = currentCategoryElement.getLastChild().getTextContent();
					
					String externalOrNot = "true";
					if (feeValue.isEmpty() || feeValue.equals("0")){
						externalOrNot = "false";
					}
					
					String expenseType = "Initial Cost";
					if (!nameValue.contains("New Submission")){
						expenseType = "Invoicable";
					}

					Element expensesEl = (Element) (xPath.evaluate("/budget/expenses", budgetXmlDom, XPathConstants.NODE));
					
					Element expenseEl = (Element) (xPath.evaluate("/budget/expenses/expense[@type=\""+ expenseType +"\" and @subtype=\"IRB Fee\"]",
							budgetXmlDom, XPathConstants.NODE));

					if (expenseEl != null){
						expenseEl.setAttribute("cost", feeValue);
						expenseEl.setAttribute("external", externalOrNot);
						//expenseEl.setAttribute("notes", nameValue);
					} else {
						Element newExpenseNode = budgetXmlDom.createElement("expense");
						newExpenseNode.setAttribute("type", expenseType);
						newExpenseNode.setAttribute("subtype", "IRB Fee");
						newExpenseNode.setAttribute("cost", feeValue);
						newExpenseNode.setAttribute("fa", "0");
						newExpenseNode.setAttribute("faenabled", "false");
						newExpenseNode.setAttribute("external", externalOrNot);
						newExpenseNode.setAttribute("count", "1");
						newExpenseNode.setAttribute("description", "IRB Fee");
						newExpenseNode.setAttribute("notes", nameValue);
						
						expensesEl.appendChild(newExpenseNode);
					}
				}
				
				budgetXmlData.setXmlData(DomUtils.elementToString(budgetXmlDom));
				budgetXmlData = protocolFormXmlDataDao.saveOrUpdate(budgetXmlData);
				
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	private String generatePharmacyExternalExpenses(long protocolFormId) throws IOException, SAXException, XPathExpressionException {
		ProtocolFormXmlData protocolFormXmlData = null;
		
		try{
			protocolFormXmlData = protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.PHARMACY);
		} catch (Exception e){
			e.printStackTrace();
		}
				
		String expenseXml = "";
		
		if (protocolFormXmlData !=  null){
			XPath xPath = xmlProcessor.getXPathInstance();
			Document currentProtocolFormXmlDataDom = xmlProcessor.loadXmlStringToDOM(protocolFormXmlData.getXmlData());
			
			String pharmacyXPath = "/pharmacy";
			
			Element pharmacyEl = (Element) (xPath.evaluate(pharmacyXPath,
					currentProtocolFormXmlDataDom, XPathConstants.NODE));
			
			String initialWaivedOrNot = (pharmacyEl.getAttribute("initial-waived").equals("true"))?"(WAIVED)":"";
			
			expenseXml += "<expense fa=\"0\" faenabled=\"false\" external=\"true\" count=\"1\" cost=\""+ pharmacyEl.getAttribute("total") +"\" type=\"Initial Cost\" subtype=\"Pharmacy Fee\" description=\"Pharmacy Fee "+ initialWaivedOrNot +"\" notes=\"\"></expense>";
			
			String otherPharmacyExpensesXPath = "/pharmacy/expenses/expense[@type!='simc' and @type!='drug']";
			
			xPath.reset();
			
			NodeList pharmacyExpensesLst = (NodeList) (xPath.evaluate(otherPharmacyExpensesXPath,
					currentProtocolFormXmlDataDom, XPathConstants.NODESET));
			
			if (pharmacyExpensesLst.getLength() > 0){
				for (int i=0; i<pharmacyExpensesLst.getLength(); i++){
					Element otherPharmacyFeeEl = (Element) pharmacyExpensesLst.item(i);
					String otherFeeWaivedOrNot = (otherPharmacyFeeEl.getAttribute("waived").equals("true"))?"(WAIVED)":"";
					
					long cost = Long.valueOf(otherPharmacyFeeEl.getAttribute("cost")) * Long.valueOf(otherPharmacyFeeEl.getAttribute("count"));
					
					if (otherFeeWaivedOrNot.equals("(WAIVED)")){
						cost = 0;
					}
					
					expenseXml += "<expense fa=\"0\" faenabled=\"false\" external=\"true\" count=\"1\" cost=\""+ String.valueOf(cost) +"\" type=\"Invoicable\" subtype=\"Pharmacy Fee\" description=\"Pharmacy: "+ otherPharmacyFeeEl.getAttribute("description") + otherFeeWaivedOrNot +"\" notes=\"\"></expense>";
				}
			}
		}
		
		return expenseXml;
	}
	
	@Override
	public String generateExternalExpenses(long protocolFormId) {
		
		String finalXml = "<expenses>";
		try{
			finalXml += generateIRBExternalExpenses(protocolFormId);
			finalXml += generatePharmacyExternalExpenses(protocolFormId);
		} catch (Exception e){
			e.printStackTrace();
			logger.error("can't generate expenses, probably haven't been filled?", e);
		}
		
		finalXml += "</expenses>";

		return finalXml;
	}
	
	@Override
	public String finalSignOffDetermination(ProtocolForm protocolForm, User user) {
		ProtocolFormXmlData protocolXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType());
		
		String workflow = workFlowDetermination(protocolXmlData);
		logger.debug("workflow: " + workflow);
		
		String isSpecficRole = "";
		/*
		if (protocolForm.getProtocolFormType().equals(
				ProtocolFormType.HUMAN_SUBJECT_RESEARCH_DETERMINATION)) {
			isSpecficRole = formService.isCurrentUserSpecificRoleOrNot(
					protocolForm, user, "Project Leader") ? "IS_PL"
					: "IS_NOT_PL";
		} else */
			
		if (protocolForm.getProtocolFormType().equals(
				ProtocolFormType.HUMANITARIAN_USE_DEVICE_RENEWAL)) {
			isSpecficRole = formService.isCurrentUserSpecificRoleOrNot(
					protocolForm, user, "Treating Physician") ? "IS_TP"
					: "IS_NOT_TP";
		} else if (protocolForm.getProtocolFormType().equals(
				ProtocolFormType.PRIVACY_BOARD) || protocolForm.getProtocolFormType().equals(
						ProtocolFormType.OFFICE_ACTION)) {
			isSpecficRole = "IS_PI";
		} else {
			if (workflow.equals("HUD")) {
				isSpecficRole = formService.isCurrentUserSpecificRoleOrNot(
						protocolForm, user, "Treating Physician") ? "IS_TP"
						: "IS_NOT_TP";
			} else {
				/*
				if (!protocolForm.getProtocolFormType().equals(
						ProtocolFormType.AUDIT)) {
					isSpecficRole = formService
							.isCurrentUserSpecificRoleOrNot(protocolForm,
									user, "Principal Investigator") ? "IS_PI"
							: "IS_NOT_PI";
				}
				*/
				
				//Redmine ticket#2830 Study member submitting revisions back to budget or converge review without requiring PI signature
				ProtocolFormStatus latestFormStatus = protocolFormStatusDao.getLatestProtocolFormStatusByFormId(protocolForm.getId());

				String requestedCommittee = "";
				
				try {
					requestedCommittee = xmlProcessor.getAttributeValueByPathAndAttributeName("//committee-review/revisition-requested-status-track", protocolForm.getMetaDataXml(), "requested-committee");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				List<ProtocolFormStatusEnum> needToCheckRevisionRequestCommitteeStatusLst = Lists.newArrayList();		
				needToCheckRevisionRequestCommitteeStatusLst.add(ProtocolFormStatusEnum.UNDER_REVISION);
				needToCheckRevisionRequestCommitteeStatusLst.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
				
				if (needToCheckRevisionRequestCommitteeStatusLst.contains(latestFormStatus.getProtocolFormStatus()) && (requestedCommittee.equals("BUDGET_REVIEW") || requestedCommittee.equals("BUDGET_MANAGER") || requestedCommittee.equals("GATEKEEPER"))) {
					isSpecficRole = "IS_PI";
				} else {
					isSpecficRole = formService
							.isCurrentUserSpecificRoleOrNot(protocolForm,
									user, "Principal Investigator") ? "IS_PI"
							: "IS_NOT_PI";
				}
				
			}

		}
		
		return isSpecficRole;
		
	}
	
	@Override
	public ProtocolForm consolidateProtocolForm(ProtocolForm protocolForm, List<String> xPathList) {
		String protocolFormMetaData = protocolForm.getMetaDataXml();
		
		Map<String, Object> resultMap = Maps.newHashMap();
		
		for (String xPath : xPathList) {
			try {
				resultMap = xmlProcessor.deleteElementByPath(xPath, protocolFormMetaData);
			} catch (Exception e) {
				
			}
			
			if (resultMap != null && !resultMap.isEmpty())
				protocolFormMetaData = resultMap.get("finalXml").toString();
		}
		
		protocolForm.setMetaDataXml(protocolFormMetaData);
		
		protocolForm = protocolFormDao.saveOrUpdate(protocolForm);
		
		return protocolForm;
	}
	
	private Set<String> xPathExpressions = Sets.newHashSet();{
		xPathExpressions.add("/protocol/budget/potentially-billed");
		xPathExpressions.add("/protocol/budget/involves/uams-clinics");
		xPathExpressions.add("/protocol/budget/involves/uams-inpatient-units");
		xPathExpressions.add("/protocol/budget/involves/uams-ss-ou");
		xPathExpressions.add("/protocol/budget/involves/uams-clinicallab");
		xPathExpressions.add("/protocol/budget/involves/uams-radiology");
		xPathExpressions.add("/protocol/budget/involves/uams-pharmacy");
		xPathExpressions.add("/protocol/budget/involves/uams-other");
		xPathExpressions.add("/protocol/budget/involves/uams-supplies");
		xPathExpressions.add("/protocol/budget/involves/fgp-fees");
		xPathExpressions.add("/protocol/budget/involves/industry-support");
		xPathExpressions.add("/protocol/migrated");
		xPathExpressions.add("/protocol/initial-mod");
		xPathExpressions.add("/protocol/budget-created");
		xPathExpressions.add("/protocol/budget/need-budget-in-clara");
		xPathExpressions.add("/protocol/site-responsible");
		xPathExpressions.add("/protocol/study-nature");
		xPathExpressions.add("/protocol/study-nature/hud-use/where");
		xPathExpressions.add("/protocol/site-responsible/enroll-subject-in-uams");
	}
	
	@Override
	public Map<String, Boolean> budgetRelatedDetermination(ProtocolFormXmlData protocolFormXmlData) {
		Map<String, Boolean> budgetRelatedDeterminationMap = Maps.newHashMap();
		
		String xmlData = protocolFormXmlData.getXmlData();
		
		boolean budgetSectionEnabled = false;
		boolean requireBudget = false;
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			Map<String, String> values = xmlHandler.getFirstStringValuesByXPaths(xmlData, xPathExpressions);
			
			String migratedStudy = values.get("/protocol/migrated");
			String initialMod = values.get("/protocol/initial-mod");
			String budgetCreated = values.get("/protocol/budget-created");
			String recreateBudget = values.get("/protocol/budget/need-budget-in-clara");
			String responsibleSite = values.get("/protocol/site-responsible");
			String enrollSubject = values.get("/protocol/site-responsible/enroll-subject-in-uams");
			String studyNature = values.get("/protocol/study-nature");
			String hudLocation = values.get("/protocol/study-nature/hud-use/where");
			
			String potentialBilled = values.get("/protocol/budget/potentially-billed");
			String involveUamsClinics = values.get("/protocol/budget/involves/uams-clinics");
			String involveUamsInpatientUnits = values.get("/protocol/budget/involves/uams-inpatient-units");
			String involveUamsSsOu = values.get("/protocol/budget/involves/uams-ss-ou");
			String involveUamsClinicallab = values.get("/protocol/budget/involves/uams-clinicallab");
			String involveUamsRadiology = values.get("/protocol/budget/involves/uams-radiology");
			String involveUamsPharmacy = values.get("/protocol/budget/involves/uams-pharmacy");
			String involveUamsOther = values.get("/protocol/budget/involves/uams-other");
			String involveUamsSupplies = values.get("/protocol/budget/involves/uams-supplies");
			String involveFgpFees = values.get("/protocol/budget/involves/fgp-fees");
			String involveIndustrySupport = values.get("/protocol/budget/involves/industry-support");
			
			boolean budgetScreening = (potentialBilled.equals("y") || involveUamsClinics.equals("y") || involveUamsInpatientUnits.equals("y") 
					|| involveUamsSsOu.equals("y") || involveUamsClinicallab.equals("y") || involveUamsRadiology.equals("y") 
					|| involveUamsPharmacy.equals("y") || involveUamsOther.equals("y") || involveUamsSupplies.equals("y") || involveFgpFees.equals("y")
					|| involveIndustrySupport.equals("y"))?true:false;
			
			if (responsibleSite.equals("uams")) {
				budgetSectionEnabled = true;
				
				if (protocolFormXmlData.getProtocolForm().getProtocolFormType().equals(ProtocolFormType.NEW_SUBMISSION)) {
					if (budgetScreening) requireBudget = true;
				} else {
					if (migratedStudy.equals("y")) {
						if (!initialMod.equals("y")) {
							if (!budgetCreated.equals("y")) {
								if (recreateBudget.equals("y")) {
									requireBudget = true;
								}
							} else {
								if (budgetScreening) {
									requireBudget = true;
								}
							}
						}
					} else {
						if (budgetScreening) {
							requireBudget = true;
						}
					}
				}
			} else {
				if (enrollSubject.equals("y") || (studyNature.equals("hud-use") && hudLocation.equals("uams"))) {
					budgetSectionEnabled = true;
					
					if (budgetScreening) requireBudget = true;
				}
			}
			
			budgetRelatedDeterminationMap.put("budgetSectionEnabled", budgetSectionEnabled);
			budgetRelatedDeterminationMap.put("budgetRequired", requireBudget);
			
			
			//String potentialBilled = xmlHandler.getSingleStringValueByXPath(protocolFormMetaData, "/protocol/extra/has-budget-or-not");
			
			//String siteResponsible = xmlHandler.getSingleStringValueByXPath(protocolFormMetaData, "/protocol/site-responsible");
			
			//if (potentialBilled.equals("y") && siteResponsible.equals("uams")) {
			//	requireBudget = true;
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return budgetRelatedDeterminationMap;
	}
	
	@Override
	public Map<String, Boolean> nctNumberValidation(ProtocolForm protocolForm) {
		Map<String, Boolean> nctNumberValidationMap = Maps.newHashMap();
		
		boolean ifNeedNctNumber = false;
		boolean ifNctNumberExistInFormMetaData = false;
		
		String xmlData = protocolForm.getTypedProtocolFormXmlDatas().get(protocolForm.getProtocolFormType().getDefaultProtocolFormXmlDataType()).getXmlData();
		
		//String formMetaData = protocolForm.getMetaDataXml();
		
		String metaData = protocolForm.getObjectMetaData();
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			List<String> needNucNumberValues = xmlHandler.getStringValuesByXPath(xmlData, "/protocol/misc/is-registered-at-clinical-trials");
			
			String needNctNumber = (needNucNumberValues!=null && !needNucNumberValues.isEmpty())?needNucNumberValues.get(0):"";
			
			List<String> nctNumberValues = xmlHandler.getStringValuesByXPath(xmlData, "/protocol/misc/is-registered-at-clinical-trials/nct-number");
			
			String nctNumber = (nctNumberValues!=null && !nctNumberValues.isEmpty())?nctNumberValues.get(0):"";
			
			String nctNumberInMetaData = xmlHandler.getSingleStringValueByXPath(metaData, "/protocol/summary/clinical-trials-determinations/nct-number");
			
			if (needNctNumber.equals("y")) {
				ifNeedNctNumber = true;
				
				if (!nctNumber.isEmpty() || !nctNumberInMetaData.isEmpty()) {
					ifNctNumberExistInFormMetaData = true;
				} else {
					ifNctNumberExistInFormMetaData = false;
				}
			} else {
				ifNeedNctNumber = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		nctNumberValidationMap.put("needNctNumber", ifNeedNctNumber);
		nctNumberValidationMap.put("nctNumberEntered", ifNctNumberExistInFormMetaData);
		
		return nctNumberValidationMap;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
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
	public void setBusinessObjectStatusHelperContainer(
			BusinessObjectStatusHelperContainer businessObjectStatusHelperContainer) {
		this.businessObjectStatusHelperContainer = businessObjectStatusHelperContainer;
	}

	public BusinessObjectStatusHelperContainer getBusinessObjectStatusHelperContainer() {
		return businessObjectStatusHelperContainer;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public UserService getUserService() {
		return userService;
	}

	@Autowired(required = true)
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public FormService getFormService() {
		return formService;
	}
	
	@Autowired(required = true)
	public void setFormService(FormService formService) {
		this.formService = formService;
	}

	public String getIrbFeeRulesXmlFilePath() {
		return irbFeeRulesXmlFilePath;
	}

	public void setIrbFeeRulesXmlFilePath(String irbFeeRulesXmlFilePath) {
		this.irbFeeRulesXmlFilePath = irbFeeRulesXmlFilePath;
	}

	public ObjectAclService getObjectAclService() {
		return objectAclService;
	}
	
	@Autowired(required = true)
	public void setObjectAclService(ObjectAclService objectAclService) {
		this.objectAclService = objectAclService;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

}
