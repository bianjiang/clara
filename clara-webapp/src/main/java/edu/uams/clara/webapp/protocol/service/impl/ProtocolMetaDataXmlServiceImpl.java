package edu.uams.clara.webapp.protocol.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.protocol.service.ProtocolAndFormStatusService;
import edu.uams.clara.webapp.protocol.service.ProtocolMetaDataXmlService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

/**
 * @ToDo need to ensure the root of protocol metadataxml is /protocol/
 * @author bianjiang
 * 
 */
public class ProtocolMetaDataXmlServiceImpl implements
		ProtocolMetaDataXmlService {

	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolMetaDataXmlService.class);

	private ProtocolDao protocolDao;

	private ProtocolFormDao procotolFormDao;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;

	private ProtocolFormStatusDao protocolFormStatusDao;

	private XmlProcessor xmlProcessor;

	private ResourceLoader resourceLoader;

	private ProtocolAndFormStatusService protocolAndFormStatusService;
	
	@Value("${validationXmlPath}")
	private String validationXmlPath;
	
	public Map<String, String> getProtocolFromToProtocolMetaDataMapping(ProtocolFormXmlDataType type){
		return xPathPairMap.get(type);
	}

	// define the fields of protocol metaData...
	private Map<ProtocolFormXmlDataType, Map<String, String>> xPathPairMap = new EnumMap<ProtocolFormXmlDataType, Map<String, String>>(
			ProtocolFormXmlDataType.class);
	{
		Map<String, String> newSubmissionXPathPairs = new HashMap<String, String>();
		// newSubmissionXPathPairs.put("/protocol/submission-type",
		// "/protocol/submission-type");
		newSubmissionXPathPairs.put("/protocol/title", "/protocol/title");
		newSubmissionXPathPairs.put("/protocol/study-type",
				"/protocol/study-type");
		newSubmissionXPathPairs.put("/protocol/site-responsible",
				"/protocol/site-responsible");
		newSubmissionXPathPairs.put("/protocol/lay-summary",
				"/protocol/lay-summary");
		newSubmissionXPathPairs.put("/protocol/responsible-department",
				"/protocol/responsible-department");
		newSubmissionXPathPairs.put("/protocol/funding", "/protocol/funding");
		// newSubmissionXPathPairs.put("/protocol/staffs/staff/user[roles/role='Principal Investigator']",
		// "/protocol/staffs/staff/user");
		newSubmissionXPathPairs.put("/protocol/study-sites",
				"/protocol/study-sites");
		newSubmissionXPathPairs.put("/protocol/sites",
				"/protocol/sites");
		newSubmissionXPathPairs.put("/protocol/staffs", "/protocol/staffs");
		newSubmissionXPathPairs.put("/protocol/drugs", "/protocol/drugs");
		newSubmissionXPathPairs.put("/protocol/devices", "/protocol/devices");
		newSubmissionXPathPairs.put("/protocol/phases", "/protocol/phases");
		newSubmissionXPathPairs.put("/protocol/extra/has-budget-or-not",
				"/protocol/extra/has-budget-or-not");
		newSubmissionXPathPairs.put("/protocol/extra/has-contract-or-not",
				"/protocol/extra/has-contract-or-not");
		newSubmissionXPathPairs.put("/protocol/extra/prmc-related-or-not",
				"/protocol/extra/prmc-related-or-not");
		newSubmissionXPathPairs.put("/protocol/committee-review",
				"/protocol/committee-review");
//		newSubmissionXPathPairs.put("/protocol/original-study",
//				"/protocol/original-study");
//		newSubmissionXPathPairs.put("/protocol/approval-status",
//				"/protocol/approval-status");
		newSubmissionXPathPairs.put("/protocol/accrual-goal",
				"/protocol/accrual-goal");
		newSubmissionXPathPairs.put("/protocol/study-nature",
				"/protocol/study-nature");
		newSubmissionXPathPairs.put("/protocol/hud/device-desc",
				"/protocol/hud/device-desc");
		newSubmissionXPathPairs.put("/protocol/summary/drugs-and-devices",
				"/protocol/summary/drugs-and-devices");
		newSubmissionXPathPairs.put("/protocol/summary/irb-determination",
				"/protocol/summary/irb-determination");
		newSubmissionXPathPairs.put("/protocol/summary/coverage-determination",
				"/protocol/summary/coverage-determination");
		newSubmissionXPathPairs.put("/protocol/summary/budget-determination/approval-date",
				"/protocol/summary/budget-determination/approval-date");
		newSubmissionXPathPairs.put("/protocol/accural-goal-local",
				"/protocol/accural-goal-local");
		newSubmissionXPathPairs.put("/protocol/epic",
				"/protocol/epic");
		newSubmissionXPathPairs.put("/protocol/diseases",
				"/protocol/diseases");
		newSubmissionXPathPairs.put("/protocol/budget-created",
				"/protocol/budget-created");
		newSubmissionXPathPairs.put("/protocol/can-notify-regulatory",
				"/protocol/can-notify-regulatory");
		newSubmissionXPathPairs.put("/protocol/subjects/age-ranges/age-range",
				"/protocol/subjects/age-ranges/age-range");
		newSubmissionXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/inclusion",
				"/protocol/inclusion-criteria");
		newSubmissionXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/exclusion",
				"/protocol/exclusion-criteria");
		newSubmissionXPathPairs.put("/protocol/summary/clinical-trials-determinations/nct-number",
				"/protocol/summary/clinical-trials-determinations/nct-number");
		
		xPathPairMap.put(ProtocolFormXmlDataType.PROTOCOL,
				newSubmissionXPathPairs);

		Map<String, String> hrsdXPathPairs = new HashMap<String, String>();
		hrsdXPathPairs.put("/hsrd/title", "/protocol/title");
		hrsdXPathPairs.put("/hsrd/staffs", "/protocol/staffs");
		hrsdXPathPairs.put("/hsrd/committee-review",
				"/protocol/committee-review");

		xPathPairMap.put(
				ProtocolFormXmlDataType.HUMAN_SUBJECT_RESEARCH_DETERMINATION,
				hrsdXPathPairs);

		Map<String, String> continuingReviewXPathPairs = new HashMap<String, String>();
//		continuingReviewXPathPairs.put("/continuing-review/most-recent-study",
//				"/protocol/most-recent-study");
//		continuingReviewXPathPairs.put("/continuing-review/approval-status",
//				"/protocol/approval-status");
		continuingReviewXPathPairs.put("/continuing-review/committee-review", "/protocol/committee-review");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/review-period",
				"/protocol/summary/irb-determination/review-period");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/fda",
				"/protocol/summary/irb-determination/fda");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/adult-risk",
				"/protocol/summary/irb-determination/adult-risk");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/ped-risk",
				"/protocol/summary/irb-determination/ped-risk");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/consent-waived",
				"/protocol/summary/irb-determination/consent-waived");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/consent-document-waived",
				"/protocol/summary/irb-determination/consent-document-waived");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/hipaa-applicable",
				"/protocol/summary/irb-determination/hipaa-applicable");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/hipaa-waived",
				"/protocol/summary/irb-determination/hipaa-waived");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/suggested-next-review-type",
				"/protocol/summary/irb-determination/suggested-next-review-type");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/suggested-type",
				"/protocol/summary/irb-determination/suggested-type");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/finding",
				"/protocol/summary/irb-determination/finding");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/finding-other",
				"/protocol/summary/irb-determination/finding-other");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/reportable",
				"/protocol/summary/irb-determination/reportable");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/irb",
				"/protocol/summary/irb-determination/irb");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/hipaa",
				"/protocol/summary/irb-determination/hipaa");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/agenda-date",
				"/protocol/summary/irb-determination/agenda-date");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/recent-motion",
				"/protocol/summary/irb-determination/recent-motion");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/non-compliance-assessment",
				"/protocol/summary/irb-determination/non-compliance-assessment");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/reportable-to-ohrp",
				"/protocol/summary/irb-determination/reportable-to-ohrp");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/hipaa-waived-date",
				"/protocol/summary/irb-determination/hipaa-waived-date");
		continuingReviewXPathPairs.put("/continuing-review/staffs", "/protocol/staffs");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/first-subject-enrolled-date", "/protocol/summary/irb-determination/first-subject-enrolled-date");
		continuingReviewXPathPairs.put("/continuing-review/summary/irb-determination/subject-accrual/enrollment/local/since-approval", "/protocol/summary/irb-determination/subject-accrual/enrollment/local/since-approval");

		
		//continuingReviewXPathPairs.put("/continuing-review/most-recent-study/approval-end-date",
				//"/protocol/most-recent-study/approval-end-date");
		xPathPairMap.put(ProtocolFormXmlDataType.CONTINUING_REVIEW,
				continuingReviewXPathPairs);

		Map<String, String> reportableNewInformationXPathPairs = new HashMap<String, String>();
		reportableNewInformationXPathPairs.put("/reportable-new-information/committee-review", "/protocol/committee-review");
		reportableNewInformationXPathPairs.put("/reportable-new-information/summary/irb-determination",
				"/protocol/summary/irb-determination");
		xPathPairMap.put(ProtocolFormXmlDataType.REPORTABLE_NEW_INFORMATION,
				reportableNewInformationXPathPairs);

		Map<String, String> modificationXPathPairs = new HashMap<String, String>();
		modificationXPathPairs.put("/protocol/title", "/protocol/title");
		modificationXPathPairs.put("/protocol/study-type",
				"/protocol/study-type");
		modificationXPathPairs.put("/protocol/site-responsible",
				"/protocol/site-responsible");
		modificationXPathPairs.put("/protocol/lay-summary",
				"/protocol/lay-summary");
		modificationXPathPairs.put("/protocol/study-sites",
				"/protocol/study-sites");
		modificationXPathPairs.put("/protocol/sites",
				"/protocol/sites");
		modificationXPathPairs.put("/protocol/responsible-department",
				"/protocol/responsible-department");
		modificationXPathPairs.put("/protocol/funding", "/protocol/funding");
		modificationXPathPairs.put("/protocol/staffs", "/protocol/staffs");
		modificationXPathPairs.put("/protocol/drugs", "/protocol/drugs");
		modificationXPathPairs.put("/protocol/devices", "/protocol/devices");
		modificationXPathPairs.put("/protocol/phases", "/protocol/phases");
		modificationXPathPairs.put("/protocol/extra/has-budget-or-not",
				"/protocol/extra/has-budget-or-not");
		modificationXPathPairs.put("/protocol/extra/prmc-related-or-not",
				"/protocol/extra/prmc-related-or-not");
		modificationXPathPairs.put("/protocol/extra/has-contract-or-not",
				"/protocol/extra/has-contract-or-not");
		modificationXPathPairs.put("/protocol/committee-review",
				"/protocol/committee-review");
		modificationXPathPairs.put("/protocol/modification/to-modify-section/conduct-under-uams",
				"/protocol/modification/to-modify-section/conduct-under-uams");
		modificationXPathPairs.put("/protocol/migrated",
				"/protocol/migrated");
//		modificationXPathPairs.put("/protocol/most-recent-study",
//				"/protocol/most-recent-study");
//		modificationXPathPairs.put("/protocol/approval-status",
//				"/protocol/approval-status");
		modificationXPathPairs.put("/protocol/accrual-goal",
				"/protocol/accrual-goal");
		modificationXPathPairs.put("/protocol/study-nature",
				"/protocol/study-nature");
		modificationXPathPairs.put("/protocol/hud/treatment-location",
				"/protocol/hud/device-desc");
		modificationXPathPairs.put("/protocol/summary/drugs-and-devices/ind",
				"/protocol/summary/drugs-and-devices/ind");
		modificationXPathPairs.put("/protocol/summary/drugs-and-devices/ide",
				"/protocol/summary/drugs-and-devices/ide");
		modificationXPathPairs.put("/protocol/summary/irb-determination/review-period",
				"/protocol/summary/irb-determination/review-period");
		modificationXPathPairs.put("/protocol/summary/irb-determination/fda",
				"/protocol/summary/irb-determination/fda");
		modificationXPathPairs.put("/protocol/summary/irb-determination/adult-risk",
				"/protocol/summary/irb-determination/adult-risk");
		modificationXPathPairs.put("/protocol/summary/irb-determination/ped-risk",
				"/protocol/summary/irb-determination/ped-risk");
		modificationXPathPairs.put("/protocol/summary/irb-determination/consent-waived",
				"/protocol/summary/irb-determination/consent-waived");
		modificationXPathPairs.put("/protocol/summary/irb-determination/consent-document-waived",
				"/protocol/summary/irb-determination/consent-document-waived");
		modificationXPathPairs.put("/protocol/summary/irb-determination/hipaa-applicable",
				"/protocol/summary/irb-determination/hipaa-applicable");
		modificationXPathPairs.put("/protocol/summary/irb-determination/hipaa-waived",
				"/protocol/summary/irb-determination/hipaa-waived");
		modificationXPathPairs.put("/protocol/summary/irb-determination/suggested-next-review-type",
				"/protocol/summary/irb-determination/suggested-next-review-type");
		modificationXPathPairs.put("/protocol/summary/irb-determination/suggested-type",
				"/protocol/summary/irb-determination/suggested-type");
		modificationXPathPairs.put("/protocol/summary/irb-determination/finding",
				"/protocol/summary/irb-determination/finding");
		modificationXPathPairs.put("/protocol/summary/irb-determination/finding-other",
				"/protocol/summary/irb-determination/finding-other");
		modificationXPathPairs.put("/protocol/summary/irb-determination/reportable",
				"/protocol/summary/irb-determination/reportable");
		modificationXPathPairs.put("/protocol/summary/irb-determination/irb",
				"/protocol/summary/irb-determination/irb");
		modificationXPathPairs.put("/protocol/summary/irb-determination/hipaa",
				"/protocol/summary/irb-determination/hipaa");
		modificationXPathPairs.put("/protocol/summary/irb-determination/audit-type",
				"/protocol/summary/irb-determination/audit-type");
		modificationXPathPairs.put("/protocol/summary/irb-determination/audit-other",
				"/protocol/summary/irb-determination/audit-other");
		modificationXPathPairs.put("/protocol/summary/irb-determination/hipaa-finding",
				"/protocol/summary/irb-determination/hipaa-finding");
		modificationXPathPairs.put("/protocol/summary/irb-determination/irb-finding",
				"/protocol/summary/irb-determination/irb-finding");
		modificationXPathPairs.put("/protocol/summary/irb-determination/agenda-date",
				"/protocol/summary/irb-determination/agenda-date");
		modificationXPathPairs.put("/protocol/summary/irb-determination/recent-motion",
				"/protocol/summary/irb-determination/recent-motion");
		modificationXPathPairs.put("/protocol/summary/irb-determination/non-compliance-assessment",
				"/protocol/summary/irb-determination/non-compliance-assessment");
		modificationXPathPairs.put("/protocol/summary/irb-determination/reportable-to-ohrp",
				"/protocol/summary/irb-determination/reportable-to-ohrp");
		modificationXPathPairs.put("/protocol/summary/irb-determination/hipaa-waived-date",
				"/protocol/summary/irb-determination/hipaa-waived-date");
		modificationXPathPairs.put("/protocol/summary/coverage-determination/medicare-benefit",
				"/protocol/summary/coverage-determination/medicare-benefit");
		modificationXPathPairs.put("/protocol/summary/coverage-determination/theraputic-intent",
				"/protocol/summary/coverage-determination/theraputic-intent");
		modificationXPathPairs.put("/protocol/summary/coverage-determination/enrolled-diagnosed",
				"/protocol/summary/coverage-determination/enrolled-diagnosed");
		modificationXPathPairs.put("/protocol/summary/coverage-determination/trial-category",
				"/protocol/summary/coverage-determination/trial-category");
		modificationXPathPairs.put("/protocol/summary/budget-determination/approval-date",
				"/protocol/summary/budget-determination/approval-date");
		modificationXPathPairs.put("/protocol/accural-goal-local",
				"/protocol/accural-goal-local");
		modificationXPathPairs.put("/protocol/accrual-goal",
				"/protocol/accrual-goal");
		modificationXPathPairs.put("/protocol/epic",
				"/protocol/epic");
		modificationXPathPairs.put("/protocol/diseases",
				"/protocol/diseases");
		modificationXPathPairs.put("/protocol/crimson/has-budget",
				"/protocol/crimson/has-budget");
		modificationXPathPairs.put("/protocol/budget-created",
				"/protocol/budget-created");
		modificationXPathPairs.put("/protocol/can-notify-regulatory",
				"/protocol/can-notify-regulatory");
		modificationXPathPairs.put("/protocol/subjects/age-ranges/age-range",
				"/protocol/subjects/age-ranges/age-range");
		modificationXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/inclusion",
				"/protocol/inclusion-criteria");
		modificationXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/exclusion",
				"/protocol/exclusion-criteria");
		modificationXPathPairs.put("/protocol/summary/clinical-trials-determinations/nct-number",
				"/protocol/summary/clinical-trials-determinations/nct-number");
		
		xPathPairMap.put(ProtocolFormXmlDataType.MODIFICATION,
				modificationXPathPairs);

		Map<String, String> studyClosureXPathPairs = new HashMap<String, String>();
//		studyClosureXPathPairs.put("/study-closure/approval-status",
//				"/protocol/approval-status");
		xPathPairMap.put(ProtocolFormXmlDataType.STUDY_CLOSURE,
				studyClosureXPathPairs);

		Map<String, String> emergencyUseXPathPairs = new HashMap<String, String>();
		emergencyUseXPathPairs.put("/emergency-use/title", "/protocol/title");
		emergencyUseXPathPairs.put("/emergency-use/staffs", "/protocol/staffs");
		emergencyUseXPathPairs.put("/emergency-use/committee-review",
				"/protocol/committee-review");
		emergencyUseXPathPairs.put("/emergency-use/treatment-location",
				"/protocol/treatment-location");
		emergencyUseXPathPairs.put("/emergency-use/committee-review", "/protocol/committee-review");

		xPathPairMap.put(ProtocolFormXmlDataType.EMERGENCY_USE,
				emergencyUseXPathPairs);
		/* no longer exist
		Map<String, String> hudXPathPairs = new HashMap<String, String>();
		hudXPathPairs.put("/hud/title", "/protocol/title");
		hudXPathPairs.put("/hud/device-name", "/protocol/device-name");
		hudXPathPairs.put("/hud/device-desc", "/protocol/device-desc");
		hudXPathPairs.put("/hud/treatment-location",
				"/protocol/treatment-location");
		hudXPathPairs.put("/hud/staffs", "/protocol/staffs");
		hudXPathPairs
				.put("/hud/committee-review", "/protocol/committee-review");

		xPathPairMap.put(ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE,
				hudXPathPairs);
				*/

		Map<String, String> hudrenewalXPathPairs = new HashMap<String, String>();
		hudrenewalXPathPairs.put("/hud-renewal/title", "/protocol/title");
		hudrenewalXPathPairs.put("/hud-renewal/device-name",
				"/protocol/device-name");
		hudrenewalXPathPairs.put("/hud-renewal/device-desc",
				"/protocol/device-desc");
		hudrenewalXPathPairs.put("/hud-renewal/treatment-location",
				"/protocol/treatment-location");
		hudrenewalXPathPairs.put("/hud-renewal/staffs", "/protocol/staffs");
		hudrenewalXPathPairs.put("/hud-renewal/committee-review",
				"/protocol/committee-review");
		hudrenewalXPathPairs.put("/hud-renewal/summary/irb-determination",
				"/protocol/summary/irb-determination");

		xPathPairMap.put(
				ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE_RENEWAL,
				hudrenewalXPathPairs);
		
		Map<String, String> auditXPathPairs = new HashMap<String, String>();
		auditXPathPairs.put("/audit/basic-details/audit-receipt-date", "/protocol/audit/audit-receipt-date");
		auditXPathPairs.put("/audit/basic-details/audit-type",
				"/protocol/audit/audit-type");
		auditXPathPairs.put("/audit/summary/irb-determination/finding",
				"/protocol/summary/irb-determination/finding");
		auditXPathPairs.put("/audit/summary/irb-determination/finding-other",
				"/protocol/summary/irb-determination/finding-other");
		auditXPathPairs.put("/audit/summary/irb-determination/reportable",
				"/protocol/summary/irb-determination/reportable");
		auditXPathPairs.put("/audit/summary/irb-determination/irb",
				"/protocol/summary/irb-determination/irb");
		auditXPathPairs.put("/audit/summary/irb-determination/hipaa",
				"/protocol/summary/irb-determination/hipaa");
		auditXPathPairs.put("/audit/summary/irb-determination/hipaa-finding",
				"/protocol/summary/irb-determination/hipaa-finding");
		auditXPathPairs.put("/audit/summary/irb-determination/irb-finding",
				"/protocol/summary/irb-determination/irb-finding");

		xPathPairMap.put(
				ProtocolFormXmlDataType.AUDIT,
				auditXPathPairs);
		
		Map<String, String> staffXPathPairs = new HashMap<String, String>();
		staffXPathPairs.put("/staff/staffs", "/protocol/staffs");
		staffXPathPairs.put("/staff/summary/irb-determination/finding",
				"/protocol/summary/irb-determination/finding");
		staffXPathPairs.put("/staff/summary/irb-determination/finding-other",
				"/protocol/summary/irb-determination/finding-other");
		staffXPathPairs.put("/staff/summary/irb-determination/reportable",
				"/protocol/summary/irb-determination/reportable");
		staffXPathPairs.put("/staff/summary/irb-determination/irb",
				"/protocol/summary/irb-determination/irb");
		staffXPathPairs.put("/staff/summary/irb-determination/hipaa",
				"/protocol/summary/irb-determination/hipaa");
		staffXPathPairs.put("/staff/summary/irb-determination/hipaa-finding",
				"/protocol/summary/irb-determination/hipaa-finding");
		staffXPathPairs.put("/staff/summary/irb-determination/irb-finding",
				"/protocol/summary/irb-determination/irb-finding");

		xPathPairMap.put(
				ProtocolFormXmlDataType.STAFF,
				staffXPathPairs);
	}

	private Map<ProtocolFormXmlDataType, Map<String, String>> protocolFormXPathPairMap = new EnumMap<ProtocolFormXmlDataType, Map<String, String>>(
			ProtocolFormXmlDataType.class);
	{
		Map<String, String> newSubmissionXPathPairs = new HashMap<String, String>();
		// newSubmissionXPathPairs.put("/protocol/submission-type",
		// "/protocol/submission-type");
		newSubmissionXPathPairs.put("/protocol/title", "/protocol/title");
		newSubmissionXPathPairs.put("/protocol/study-type",
				"/protocol/study-type");
		newSubmissionXPathPairs.put("/protocol/site-responsible",
				"/protocol/site-responsible");
		newSubmissionXPathPairs.put("/protocol/lay-summary",
				"/protocol/lay-summary");
		newSubmissionXPathPairs.put("/protocol/responsible-department",
				"/protocol/responsible-department");
		// newSubmissionXPathPairs.put("/protocol/staffs/staff/user[roles/role='Principal Investigator']",
		// "/protocol/staffs/staff/user");
		newSubmissionXPathPairs.put("/protocol/study-sites",
				"/protocol/study-sites");
		newSubmissionXPathPairs.put("/protocol/sites",
				"/protocol/sites");
		newSubmissionXPathPairs.put("/protocol/staffs", "/protocol/staffs");
		newSubmissionXPathPairs.put("/protocol/drugs", "/protocol/drugs");
		newSubmissionXPathPairs.put("/protocol/devices", "/protocol/devices");
		newSubmissionXPathPairs.put("/protocol/phases", "/protocol/phases");
		newSubmissionXPathPairs.put("/protocol/funding", "/protocol/funding");
		newSubmissionXPathPairs.put("/protocol/budget/potentially-billed",
				"/protocol/extra/has-budget-or-not");
		newSubmissionXPathPairs.put("/protocol/contract/have-new-contract",
				"/protocol/extra/has-contract-or-not");
		newSubmissionXPathPairs.put("/protocol/extra/biosafety-related-or-not",
				"/protocol/extra/biosafety-related-or-not");
		newSubmissionXPathPairs
				.put("/protocol/risks/radiation-safety/involve-the-use-of-radiation/y/exceed-standard-of-care",
						"/protocol/extra/radiation-related-or-not");
		newSubmissionXPathPairs.put("/protocol/misc/is-cancer-study",
				"/protocol/extra/prmc-related-or-not");
		newSubmissionXPathPairs.put("/protocol/subjects/multiple-sites/y/total-accrual-goal-for-all-sites",
				"/protocol/accrual-goal");
		newSubmissionXPathPairs.put("/protocol/subjects/vulnerable-populations/included",
				"/protocol/included-vulnerable-populations");
		newSubmissionXPathPairs.put("/protocol/study-nature",
				"/protocol/study-nature");
		newSubmissionXPathPairs.put("/protocol/device-desc",
				"/protocol/hud/device-desc");
		newSubmissionXPathPairs.put("/protocol/subjects/total-number",
				"/protocol/accural-goal-local");
		newSubmissionXPathPairs.put("/protocol/epic",
				"/protocol/epic");
		newSubmissionXPathPairs.put("/protocol/diseases",
				"/protocol/diseases");
		newSubmissionXPathPairs.put("/protocol/budget-created",
				"/protocol/budget-created");
		newSubmissionXPathPairs.put("/protocol/can-notify-regulatory",
				"/protocol/can-notify-regulatory");
		newSubmissionXPathPairs.put("/protocol/subjects/age-ranges/age-range",
				"/protocol/subjects/age-ranges/age-range");
		newSubmissionXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/inclusion",
				"/protocol/misc/inclusion-exclusion-criteria-for-this-study/inclusion");
		newSubmissionXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/exclusion",
				"/protocol/misc/inclusion-exclusion-criteria-for-this-study/exclusion");
		newSubmissionXPathPairs.put("/protocol/contract/require-contract-before-irb",
				"/protocol/contract/require-contract-before-irb");
		newSubmissionXPathPairs.put("/protocol/misc/is-registered-at-clinical-trials/nct-number",
				"/protocol/summary/clinical-trials-determinations/nct-number");
		
		//newSubmissionXPathPairs.put("/protocol/related-contract",
				//"/protocol/related-contract");

		protocolFormXPathPairMap.put(ProtocolFormXmlDataType.PROTOCOL,
				newSubmissionXPathPairs);

		Map<String, String> hrsdXPathPairs = new HashMap<String, String>();
		hrsdXPathPairs.put("/hsrd/title", "/hsrd/title");
		hrsdXPathPairs.put("/hsrd/staffs", "/hsrd/staffs");

		protocolFormXPathPairMap.put(
				ProtocolFormXmlDataType.HUMAN_SUBJECT_RESEARCH_DETERMINATION,
				hrsdXPathPairs);

		Map<String, String> continuingReviewXPathPairs = new HashMap<String, String>();
		continuingReviewXPathPairs.put("/continuing-review/staffs", "/continuing-review/staffs");
		continuingReviewXPathPairs.put("/continuing-review/subject-accrual/first-subject-enrolled-date", "/continuing-review/summary/irb-determination/first-subject-enrolled-date");
		continuingReviewXPathPairs.put("/continuing-review/subject-accrual/enrollment/local/since-approval", "/continuing-review/summary/irb-determination/subject-accrual/enrollment/local/since-approval");
		protocolFormXPathPairMap.put(ProtocolFormXmlDataType.CONTINUING_REVIEW,
				continuingReviewXPathPairs);

		Map<String, String> reportableNewInformationXPathPairs = new HashMap<String, String>();
		reportableNewInformationXPathPairs.put("/reportable-new-information/staffs",
				"/reportable-new-information/staffs");
		protocolFormXPathPairMap.put(
				ProtocolFormXmlDataType.REPORTABLE_NEW_INFORMATION,
				reportableNewInformationXPathPairs);

		Map<String, String> modificationXPathPairs = new HashMap<String, String>();
		modificationXPathPairs.put("/protocol/title", "/protocol/title");
		modificationXPathPairs.put("/protocol/study-type",
				"/protocol/study-type");
		modificationXPathPairs.put("/protocol/site-responsible",
				"/protocol/site-responsible");
		modificationXPathPairs.put("/protocol/lay-summary",
				"/protocol/lay-summary");
		modificationXPathPairs.put("/protocol/study-sites",
				"/protocol/study-sites");
		modificationXPathPairs.put("/protocol/sites",
				"/protocol/sites");
		modificationXPathPairs.put("/protocol/staffs", "/protocol/staffs");
		modificationXPathPairs.put("/protocol/drugs", "/protocol/drugs");
		modificationXPathPairs.put("/protocol/devices", "/protocol/devices");
		modificationXPathPairs.put("/protocol/phases", "/protocol/phases");
		modificationXPathPairs.put("/protocol/funding", "/protocol/funding");
		modificationXPathPairs.put("/protocol/responsible-department",
				"/protocol/responsible-department");
		modificationXPathPairs.put("/protocol/budget/potentially-billed",
				"/protocol/extra/has-budget-or-not");
		modificationXPathPairs.put("/protocol/contract/have-new-contract",
				"/protocol/extra/has-contract-or-not");
		modificationXPathPairs.put("/protocol/extra/biosafety-related-or-not",
				"/protocol/extra/biosafety-related-or-not");
		modificationXPathPairs
				.put("/protocol/risks/radiation-safety/involve-the-use-of-radiation/y/exceed-standard-of-care",
						"/protocol/extra/radiation-related-or-not");
		modificationXPathPairs.put("/protocol/misc/is-cancer-study",
				"/protocol/extra/prmc-related-or-not");
		modificationXPathPairs.put("/protocol/subjects/multiple-sites/y/total-accrual-goal-for-all-sites",
				"/protocol/accrual-goal");
		modificationXPathPairs.put("/protocol/subjects/vulnerable-populations/included",
				"/protocol/included-vulnerable-populations");
		modificationXPathPairs.put("/protocol/study-nature",
				"/protocol/study-nature");
		modificationXPathPairs.put("/protocol/device-desc",
				"/protocol/hud/device-desc");
		modificationXPathPairs.put("/protocol/modification/to-modify-section",
				"/protocol/modification/to-modify-section");
		modificationXPathPairs.put("/protocol/migrated",
				"/protocol/migrated");
		modificationXPathPairs.put("/protocol/subjects/total-number",
				"/protocol/accural-goal-local");
		modificationXPathPairs.put("/protocol/subjects/multiple-sites/y/total-accrual-goal-for-all-sites",
				"/protocol/accrual-goal");
		modificationXPathPairs.put("/protocol/epic",
				"/protocol/epic");
		modificationXPathPairs.put("/protocol/diseases",
				"/protocol/diseases");
		modificationXPathPairs.put("/protocol/crimson/has-budget",
				"/protocol/crimson/has-budget");
		modificationXPathPairs.put("/protocol/budget-created",
				"/protocol/budget-created");
		modificationXPathPairs.put("/protocol/can-notify-regulatory",
				"/protocol/can-notify-regulatory");
		modificationXPathPairs.put("/protocol/subjects/age-ranges/age-range",
				"/protocol/subjects/age-ranges/age-range");
		modificationXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/inclusion",
				"/protocol/misc/inclusion-exclusion-criteria-for-this-study/inclusion");
		modificationXPathPairs.put("/protocol/misc/inclusion-exclusion-criteria-for-this-study/exclusion",
				"/protocol/misc/inclusion-exclusion-criteria-for-this-study/exclusion");
		modificationXPathPairs.put("/protocol/contract/require-contract-before-irb",
				"/protocol/contract/require-contract-before-irb");
		modificationXPathPairs.put("/protocol/initial-mod", "/protocol/initial-mod");
		modificationXPathPairs.put("/protocol/misc/is-registered-at-clinical-trials/nct-number",
				"/protocol/summary/clinical-trials-determinations/nct-number");
		modificationXPathPairs.put("/protocol/compliance-approved","/protocol/compliance-approved");
		//modificationXPathPairs.put("/protocol/related-contract",
				//"/protocol/related-contract");
		
		protocolFormXPathPairMap.put(ProtocolFormXmlDataType.MODIFICATION,
				modificationXPathPairs);

		Map<String, String> studyClosureXPathPairs = new HashMap<String, String>();
		studyClosureXPathPairs.put("/study-closure/staffs",
				"/study-closure/staffs");
		protocolFormXPathPairMap.put(ProtocolFormXmlDataType.STUDY_CLOSURE,
				studyClosureXPathPairs);

		Map<String, String> emergencyUseXPathPairs = new HashMap<String, String>();
		emergencyUseXPathPairs.put("/emergency-use/basic-details/eu-title",
				"/emergency-use/title");
		emergencyUseXPathPairs.put("/emergency-use/staffs",
				"/emergency-use/staffs");
		emergencyUseXPathPairs.put("/emergency-use/basic-details/ieu-or-eu",
				"/emergency-use/ieu-or-eu");
		emergencyUseXPathPairs.put(
				"/emergency-use/basic-details/treatment-location",
				"/emergency-use/treatment-location");

		protocolFormXPathPairMap.put(ProtocolFormXmlDataType.EMERGENCY_USE,
				emergencyUseXPathPairs);

		Map<String, String> hudXPathPairs = new HashMap<String, String>();
		hudXPathPairs.put("/hud/title", "/hud/title");
		hudXPathPairs.put("/hud/device-name", "/hud/device-name");
		hudXPathPairs.put("/hud/device-desc", "/hud/device-desc");
		hudXPathPairs.put("/hud/treatment-location", "/hud/treatment-location");
		hudXPathPairs.put("/hud/staffs", "/hud/staffs");

		protocolFormXPathPairMap.put(
				ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE, hudXPathPairs);

		Map<String, String> hudrenewalXPathPairs = new HashMap<String, String>();
		hudrenewalXPathPairs.put("/hud-renewal/title", "/hud-renewal/title");
		hudrenewalXPathPairs.put("/hud-renewal/device-name",
				"/hud-renewal/device-name");
		hudrenewalXPathPairs.put("/hud-renewal/device-desc",
				"/hud-renewal/device-desc");
		hudrenewalXPathPairs.put("/hud-renewal/treatment-location",
				"/hud-renewal/treatment-location");
		hudrenewalXPathPairs.put("/hud-renewal/staffs", "/hud-renewal/staffs");

		protocolFormXPathPairMap.put(
				ProtocolFormXmlDataType.HUMANITARIAN_USE_DEVICE_RENEWAL,
				hudrenewalXPathPairs);
		
		Map<String, String> auditXPathPairs = new HashMap<String, String>();
		auditXPathPairs.put("/audit/basic-details/audit-receipt-date", "/audit/basic-details/audit-receipt-date");
		auditXPathPairs.put("/audit/basic-details/audit-type",
				"/audit/basic-details/audit-type");
		auditXPathPairs.put("/audit/staffs", "/audit/staffs");

		protocolFormXPathPairMap.put(
				ProtocolFormXmlDataType.AUDIT,
				auditXPathPairs);
		
		Map<String, String> staffXPathPairs = new HashMap<String, String>();
		staffXPathPairs.put("/staff/staffs", "/staff/staffs");

		protocolFormXPathPairMap.put(
				ProtocolFormXmlDataType.STAFF,
				staffXPathPairs);
	}

	@Override
	public ProtocolForm updateProtocolFormMetaDataXml(
			ProtocolFormXmlData protocolFormXmlData, String extraDataXml) {
		Assert.notNull(protocolFormXmlData);
		Assert.notNull(protocolFormXmlData.getProtocolForm());
		Assert.notNull(protocolFormXmlData.getProtocolForm().getProtocol());

		ProtocolFormXmlDataType protocolFormXmlDataType = protocolFormXmlData
				.getProtocolFormXmlDataType();
		ProtocolForm pf = protocolFormXmlData.getProtocolForm();

		if (protocolFormXPathPairMap.get(protocolFormXmlDataType) == null) {
			logger.debug("no entry needs to be updated!");
			return pf;
		}

		String protocolFormMetaDataXml = pf.getMetaDataXml();

		try {
			ProtocolFormStatus protocolFormStatus = protocolFormStatusDao
					.getLatestProtocolFormStatusByFormId(pf.getId());

			logger.debug("protocolFormStatus: "
					+ protocolFormStatus.getProtocolFormStatus()
							.getDescription());
			protocolFormMetaDataXml = xmlProcessor
					.replaceOrAddNodeValueByPath("/"
							+ pf.getProtocolFormType().getBaseTag()
							+ "/status", protocolFormMetaDataXml,
							org.apache.commons.lang.StringEscapeUtils
									.escapeXml(protocolFormStatus
											.getProtocolFormStatus()
											.getDescription()));
			
			pf.setMetaDataXml(protocolFormMetaDataXml);
			pf = procotolFormDao.saveOrUpdate(pf);
			
			if (extraDataXml == null || extraDataXml.isEmpty()) {
				logger.debug("before mergeByXPaths -> protocolForm.metadataxml: "
						+ protocolFormMetaDataXml);
				protocolFormMetaDataXml = xmlProcessor.mergeByXPaths(
						pf.getMetaDataXml(), protocolFormXmlData.getXmlData(),
						XmlProcessor.Operation.UPDATE_IF_EXIST,
						protocolFormXPathPairMap.get(protocolFormXmlDataType));
				logger.debug("after mergeByXPaths -> protocolForm.metadataxml: "
						+ protocolFormMetaDataXml);
			} else {
				protocolFormMetaDataXml = parseExtraDataXml(pf, extraDataXml);

				Map<String, Object> resultMap = null;
//
				Document extraDataXmlDoc = xmlProcessor
						.loadXmlStringToDOM(extraDataXml);
//				Document currentProtocolMetaDataXmlDoc = xmlProcessor
//						.loadXmlStringToDOM(protocolFormMetaDataXml);
				XPath xpath = xmlProcessor.getXPathInstance();
//				// ***@TODO need to talk about this...this is mainly for
//				// assigned reviewer?
//				/*
//				 * Element committeeEl =
//				 * (Element)xpath.evaluate("/committee-review/committee",
//				 * extraDataXmlDoc,XPathConstants.NODE);
//				 * 
//				 * if (committeeEl != null){ logger.debug("committee-element: "
//				 * + DomUtils.elementToString(committeeEl)); resultMap =
//				 * xmlProcessor.addSubElementToElementIdentifiedByXPath(
//				 * "/protocol/committee-review", protocolFormMetaDataXml,
//				 * DomUtils.elementToString(committeeEl), false);
//				 * 
//				 * protocolFormMetaDataXml =
//				 * resultMap.get("finalXml").toString(); }
//				 */
//				Element committeeReviewEl = (Element) xpath.evaluate("/"
//						+ pf.getProtocolFormType().getBaseTag()
//						+ "/committee-review", currentProtocolMetaDataXmlDoc,
//						XPathConstants.NODE);
//
//				Element committeeReviewInExtraDataEl = (Element) xpath
//						.evaluate("/committee-review", extraDataXmlDoc,
//								XPathConstants.NODE);
//
//				Map<String, Object> orgResultMap = null;
//				if (committeeReviewEl != null) {
//					Element committeeReviewFirstChildInExtraDataEl = null;
//					if (committeeReviewInExtraDataEl != null
//							&& committeeReviewInExtraDataEl.hasChildNodes()) {
//						committeeReviewFirstChildInExtraDataEl = (Element) committeeReviewInExtraDataEl
//								.getFirstChild();
//
//						logger.debug("committee review first child: "
//								+ DomUtils
//										.elementToString(committeeReviewFirstChildInExtraDataEl));
//						orgResultMap = xmlProcessor
//								.addSubElementToElementIdentifiedByXPath(
//										"/"
//												+ pf.getProtocolFormType()
//														.getBaseTag()
//												+ "/committee-review",
//										protocolFormMetaDataXml,
//										DomUtils.elementToString(committeeReviewFirstChildInExtraDataEl),
//										false);
//					}
//				} else {
//					orgResultMap = xmlProcessor.addElementByPath("/"
//							+ pf.getProtocolFormType().getBaseTag()
//							+ "/committee-review", protocolFormMetaDataXml,
//							extraDataXml, false);
//				}
//
//				protocolFormMetaDataXml = orgResultMap != null ? orgResultMap
//						.get("finalXml").toString() : protocolFormMetaDataXml;
				
				Document protocolFormMetaDataXmlDoc = xmlProcessor
						.loadXmlStringToDOM(protocolFormMetaDataXml);

				Element formStatusTrackEl = (Element) xpath.evaluate(
						"//revisition-requested-status-track", extraDataXmlDoc,
						XPathConstants.NODE);

				if (formStatusTrackEl != null) {
					logger.debug("form-status-track: "
							+ DomUtils.elementToString(formStatusTrackEl));
					

					Element formStatusTrackElInMetaData = (Element) xpath
							.evaluate(
									"/"
											+ pf.getProtocolFormType()
													.getBaseTag()
											+ "/committee-review/revisition-requested-status-track",
									protocolFormMetaDataXmlDoc,
									XPathConstants.NODE);

					if (formStatusTrackElInMetaData != null) {
						formStatusTrackElInMetaData.setAttribute(
								"original-object-status", formStatusTrackEl
										.getAttribute("original-object-status"));
						formStatusTrackElInMetaData.setAttribute(
								"original-form-status", formStatusTrackEl
										.getAttribute("original-form-status"));
						formStatusTrackElInMetaData
								.setAttribute(
										"original-form-committee-status",
										formStatusTrackEl
												.getAttribute("original-form-committee-status"));
						formStatusTrackElInMetaData.setAttribute(
								"requested-committee", formStatusTrackEl
										.getAttribute("requested-committee"));
						protocolFormMetaDataXml = DomUtils
								.elementToString(protocolFormMetaDataXmlDoc);
					} else {
						resultMap = xmlProcessor
								.addSubElementToElementIdentifiedByXPath(
										"/"
												+ pf.getProtocolFormType()
														.getBaseTag()
												+ "/committee-review",
										protocolFormMetaDataXml,
										DomUtils.elementToString(formStatusTrackEl),
										false);
						protocolFormMetaDataXml = resultMap.get("finalXml")
								.toString();
					}
				}
				
				xpath.reset();
							
				//workflowcontrol update
				Element workflowControlEl = (Element) (xpath.evaluate(
						"/committee-review/workflow-control", extraDataXmlDoc,
						XPathConstants.NODE));

				if (workflowControlEl != null) {					
					Element recordedWorkflowPathEl = (Element) (xpath.evaluate(
							"/committee-review/workflow-control", protocolFormMetaDataXmlDoc,
							XPathConstants.NODE));
					
					if (recordedWorkflowPathEl != null) {
						recordedWorkflowPathEl.getParentNode().removeChild(recordedWorkflowPathEl);
					}
					
					protocolFormMetaDataXmlDoc.getFirstChild().appendChild(protocolFormMetaDataXmlDoc.importNode(workflowControlEl, true));
					
					protocolFormMetaDataXml = DomUtils
							.elementToString(protocolFormMetaDataXmlDoc);
				}
				
			}
			
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", protocolFormStatus.getProtocolFormStatus().getPriorityLevel());

			protocolFormMetaDataXml = xmlProcessor.addAttributesByPath(
					"/"+ pf.getProtocolFormType().getBaseTag() +"/status", protocolFormMetaDataXml, attributes);

			logger.debug("protocolFormMetaDataXml->finaXml: "
					+ protocolFormMetaDataXml);

			pf.setMetaDataXml(protocolFormMetaDataXml);
			pf = procotolFormDao.saveOrUpdate(pf);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return pf;
	}
	
	private String parseExtraDataXml(ProtocolForm protocolForm, String extraDataXml){
		Map<String, String> resultMap = new HashMap<String, String>();
		String protocolFormMetaDataXml = protocolForm.getMetaDataXml();
		
		Date now = new Date();
		
		Map<String, String> pairMap = new HashMap<String, String>();
		pairMap.put("/committee-review/committee/extra-content/ind", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/drugs-and-devices/ind");
		pairMap.put("/committee-review/committee/extra-content/ide", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/drugs-and-devices/ide");
		
		pairMap.put("/committee-review/committee/extra-content/fda", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/fda");
		pairMap.put("/committee-review/committee/extra-content/suggested-type", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/suggested-type");
		pairMap.put("/committee-review/committee/extra-content/expedited/category", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/expedited-category");
		pairMap.put("/committee-review/committee/extra-content/expedited/consent-waived", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/consent-waived");
		pairMap.put("/committee-review/committee/extra-content/expedited/consent-documentation-waived", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/consent-document-waived");
		//pairMap.put("/committee-review/committee/extra-content/expedited/adult-risk", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/committee-review/irb-determination/adult-risk");
		pairMap.put("/committee-review/committee/extra-content/expedited/hipaa-waived", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-waived");
		pairMap.put("/committee-review/committee/extra-content/expedited/adult-risk", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/adult-risk");
		pairMap.put("/committee-review/committee/extra-content/expedited/ped-risk", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/ped-risk");
		pairMap.put("/committee-review/committee/extra-content/exempt/category", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/exempt-category");
		pairMap.put("/committee-review/committee/extra-content/exempt/consent-waived", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/consent-waived");
		pairMap.put("/committee-review/committee/extra-content/exempt/consent-documentation-waived", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/consent-document-waived");
		pairMap.put("/committee-review/committee/extra-content/exempt/hipaa-waived", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-waived");
		pairMap.put("/committee-review/committee/extra-content/finding", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/finding");
		pairMap.put("/committee-review/committee/extra-content/finding-other", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/finding-other");
		pairMap.put("/committee-review/committee/extra-content/reportable", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/reportable");
		pairMap.put("/committee-review/committee/extra-content/irb", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/irb");
		pairMap.put("/committee-review/committee/extra-content/hipaa", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa");
		pairMap.put("/committee-review/committee/extra-content/audit-type", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/audit-type");
		pairMap.put("/committee-review/committee/extra-content/audit-other", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/audit-other");
		pairMap.put("/committee-review/committee/extra-content/hipaa-finding", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-finding");
		pairMap.put("/committee-review/committee/extra-content/irb-finding", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/irb-finding");
		
		pairMap.put("/committee-review/committee/extra-content/medicare-benefit", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/coverage-determination/medicare-benefit");
		pairMap.put("/committee-review/committee/extra-content/theraputic-intent", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/coverage-determination/theraputic-intent");
		pairMap.put("/committee-review/committee/extra-content/enrolled-diagnosed", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/coverage-determination/enrolled-diagnosed");
		pairMap.put("/committee-review/committee/extra-content/trial-category", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/coverage-determination/trial-category");
		
		pairMap.put("/committee-review/committee/extra-content/nct-number", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/clinical-trials-determinations/nct-number");
		
		pairMap.put("/committee-review/committee/cancel-reason", "/"+ protocolForm.getProtocolFormType().getBaseTag() +"/cancel-reason");
		
		List<String> values = null;
		try{			
			for (Map.Entry<String, String> entry : pairMap.entrySet()){
				values = xmlProcessor.listElementStringValuesByPath(entry.getKey(), extraDataXml);
				
				if (values!=null&&values.size()>0){
					resultMap.put(entry.getValue(), values.get(0));
					
					String hipaaWaitedDate = "";
					if (entry.getKey().contains("hipaa-waived") && values.get(0).toLowerCase().equals("yes")){
						hipaaWaitedDate = DateFormatUtil.formateDateToMDY(now);
					}
					
					resultMap.put("/"+ protocolForm.getProtocolFormType().getBaseTag() +"/summary/irb-determination/hipaa-waived-date", hipaaWaitedDate);
				}
			}

			if (resultMap != null){
				for (Map.Entry<String, String> entry : resultMap.entrySet()){
					logger.debug("key: " + entry.getKey());
					logger.debug("value: " + entry.getValue());
					protocolFormMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath(entry.getKey(), protocolFormMetaDataXml, entry.getValue());
				}
			} 
			
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return protocolFormMetaDataXml;
	}

	@Override
	public Protocol updateProtocolMetaDataXml(ProtocolForm protocolForm) {

		Assert.notNull(protocolForm);
		Assert.notNull(protocolForm.getProtocol());

		ProtocolFormXmlDataType protocolFormXmlDataType = protocolForm
				.getProtocolFormType().getDefaultProtocolFormXmlDataType();

		Protocol p = protocolForm.getProtocol();

		if (xPathPairMap.get(protocolFormXmlDataType) == null) {
			logger.debug("no entry needs to be updated!");
			return p;
		}
		try {
			String protocolMetaDataXml = xmlProcessor.mergeByXPaths(
					p.getMetaDataXml(), protocolForm.getMetaDataXml(),
					XmlProcessor.Operation.UPDATE_IF_EXIST,
					xPathPairMap.get(protocolFormXmlDataType));
			logger.debug("after mergeByXPaths -> protocol.metadataxml: "
					+ protocolMetaDataXml);

			ProtocolStatus protocolStatus = protocolDao
					.getLatestProtocolStatusByProtocolId(p.getId());
			logger.debug("protocolStatus: "
					+ protocolStatus.getProtocolStatus().getDescription());
			protocolMetaDataXml = xmlProcessor.replaceOrAddNodeValueByPath(
					"/protocol/status", protocolMetaDataXml,
					org.apache.commons.lang.StringEscapeUtils
							.escapeXml(protocolStatus.getProtocolStatus()
									.getDescription()));

			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("priority", protocolStatus.getProtocolStatus().getPriorityLevel());

			protocolMetaDataXml = xmlProcessor.addAttributesByPath(
					"/protocol/status", protocolMetaDataXml, attributes);

			logger.debug("final protocolMetaDataXml: " + protocolMetaDataXml);

			p.setMetaDataXml(protocolMetaDataXml);
			p = protocolDao.saveOrUpdate(p);

		} catch (Exception ex) {
			logger.error(ex.getMessage());
			ex.printStackTrace();
		}

		return p;
	}

	// @Override
	// public Protocol updateProtocolMetaDataXml(
	// ProtocolForm protocolForm,
	// ProtocolFormXmlDataType protocolFormXmlDataType) {
	//
	// Assert.notNull(protocolForm);
	//
	// Assert.notNull(protocolFormXmlDataType);
	//
	// ProtocolFormXmlData protocolFormXmlData =
	// protocolForm.getTypedProtocolFormXmlDatas().get(protocolFormXmlDataType);
	//
	// Protocol p = updateProtocolMetaDataXml(protocolFormXmlData);
	//
	//
	// return p;
	//
	//
	// }

	/*
	 * @Override public Protocol updateProtocolStatus(Protocol protocol) {
	 * 
	 * //get current protocolstatus and put it into the protocol.metaDataXml
	 * try{
	 * 
	 * ProtocolStatus protocolStatus =
	 * protocolDao.getLatestProtocolStatusByProtocolId(protocol.getId());
	 * 
	 * logger.debug("protocolStatus: " +
	 * protocolStatus.getProtocolStatus().getDescription()); String
	 * thisProtocolMetaDataXml =
	 * xmlProcessor.replaceOrAddNodeValueByPath("/protocol/status",
	 * protocol.getMetaDataXml(),
	 * org.apache.commons.lang.StringEscapeUtils.escapeXml
	 * (protocolStatus.getProtocolStatus().getDescription()));
	 * 
	 * logger.debug("metaDataXml: " + thisProtocolMetaDataXml);
	 * 
	 * protocol.setMetaDataXml(thisProtocolMetaDataXml);
	 * 
	 * protocol = protocolDao.saveOrUpdate(protocol);
	 * 
	 * }catch(Exception ex) { ex.printStackTrace();
	 * logger.error("missing status for protocol: " + protocol.getId()); }
	 * 
	 * return protocol;
	 * 
	 * }
	 */
	// @Override
	// public ProtocolForm updateProtocolFormStatus(ProtocolForm protocolForm) {
	//
	// //get current protocolformstatus and put it into the
	// protocolform.metaDataXml
	// try{
	// ProtocolFormStatus protocolFormStatus =
	// procotolFormDao.getLatestProtocolFormStatusByProtocolFormId(protocolForm.getId());
	//
	// logger.debug("protocolFormStatus: " +
	// protocolFormStatus.getProtocolFormStatus().getDescription());
	// String thisProtocolFormMetaDataXml =
	// xmlProcessor.replaceOrAddNodeValueByPath("/protocol/status",
	// protocolForm.getMetaDataXml(),
	// org.apache.commons.lang.StringEscapeUtils.escapeXml(protocolFormStatus.getProtocolFormStatus().getDescription()));
	//
	// logger.debug("metaDataXml: " + thisProtocolFormMetaDataXml);
	// protocolForm.setMetaDataXml(thisProtocolFormMetaDataXml);
	//
	// protocolForm = procotolFormDao.saveOrUpdate(protocolForm);
	//
	// }catch(Exception ex)
	// {
	// logger.error("missing status for protocolForm: " + protocolForm.getId());
	// }
	//
	// return protocolForm;
	//
	// }
	private List<String> ignorePathList = Lists.newArrayList();{
		ignorePathList.add("/protocol/staffs/staff/user/roles/role");
		ignorePathList.add("/protocol/modification/to-modify-section/involve-change-in/budget-modified");
		ignorePathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/procedure");
		ignorePathList.add("/protocol/modification/to-modify-section/involve-addition-deletion-of/pharmacy");
	}

	@Override
	public ProtocolFormXmlData consolidateProtocolFormXmlData(
			ProtocolFormXmlData protocolFormXmlData,
			ProtocolFormXmlDataType protocolFormXmlDataType)
			throws IOException, SAXException, XPathExpressionException {
		String lookupPath = validationXmlPath;

		switch (protocolFormXmlDataType) {
		case PROTOCOL:
			lookupPath = validationXmlPath + "/newSubmissionValidation.xml";
			break;
		case EMERGENCY_USE:
			lookupPath = validationXmlPath + "/emergencyuseValidation.xml";
			break;
		case HUMAN_SUBJECT_RESEARCH_DETERMINATION:
			lookupPath = validationXmlPath + "/hsrdValidation.xml";
			break;
		case HUMANITARIAN_USE_DEVICE:
			lookupPath = validationXmlPath + "/hudValidation.xml";
			break;
		case HUMANITARIAN_USE_DEVICE_RENEWAL:
			lookupPath = validationXmlPath + "/hudRenewalValidation.xml";
			break;
		case CONTINUING_REVIEW:
			lookupPath = validationXmlPath + "/continuingReviewValidation.xml";
			break;
		case STUDY_CLOSURE:
			lookupPath = validationXmlPath + "/studyClosureValidation.xml";
			break;
		case REPORTABLE_NEW_INFORMATION:
			lookupPath = validationXmlPath + "/reportableNewInfoValidation.xml";
			break;
		case MODIFICATION:
			lookupPath = validationXmlPath + "/modificationValidation.xml";
			break;
		case AUDIT:
			lookupPath = validationXmlPath + "/auditValidation.xml";
			break;
		case STAFF:
			lookupPath = validationXmlPath + "/staffValidation.xml";
			break;
		}

		 //String testPath =
		 //"file:src/test/java/edu/uams/clara/webapp/protocol/service/newSubmissionValidation.xml";

		XPath xPath = xmlProcessor.getXPathInstance();

		//Document validationXmlDocument = xmlProcessor.loadXmlFileToDOM(testPath);
		Document validationXmlDocument = xmlProcessor
				.loadXmlFileToDOM(resourceLoader.getResource(lookupPath)
						.getFile());

		String prerequisiteRuleXpath = "/rules/rule/prerequisites/rule";

		NodeList prerequisiteRulesLst = (NodeList) xPath.evaluate(
				prerequisiteRuleXpath, validationXmlDocument,
				XPathConstants.NODESET);

		logger.debug("nodelist length: " + prerequisiteRulesLst.getLength());

		for (int i = 0; i < prerequisiteRulesLst.getLength(); i++) {
			// Element prerequisiteRuleEl = (Element)
			// xPath.evaluate(prerequisiteRuleXpath,
			// validationXmlDocument, XPathConstants.NODE);
			if (prerequisiteRulesLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element prerequisiteRuleEl = (Element) prerequisiteRulesLst
						.item(i);

				List<String> lst = new ArrayList<String>(0);
				lst = xmlProcessor.listElementStringValuesByPath(
						prerequisiteRuleEl.getAttribute("path"),
						protocolFormXmlData.getXmlData());

				Element prerequisiteRuleConstraintEl = (Element) prerequisiteRuleEl
						.getElementsByTagName("constraint").item(0);

				Map<String, Object> resultMap = new HashMap<String, Object>();

				Element el = (Element) prerequisiteRuleEl.getParentNode()
						.getParentNode();
				
				if (needRemoved(lst,
						prerequisiteRuleConstraintEl.getAttribute("data"), prerequisiteRuleConstraintEl.getAttribute("type"))) {
					Element ruleEl = (Element) prerequisiteRuleConstraintEl
							.getParentNode().getParentNode().getParentNode();
					
					if (!ignorePathList.contains(ruleEl.getAttribute("path"))){
						resultMap = xmlProcessor.deleteElementByPath(
								ruleEl.getAttribute("path"),
								protocolFormXmlData.getXmlData());
						
						//logger.debug("path: " + ruleEl.getAttribute("path")
								//+ " removed!");
					}
					
					
					protocolFormXmlData.setXmlData((resultMap!=null&&!resultMap.isEmpty())?resultMap.get("finalXml")
							.toString():protocolFormXmlData.getXmlData());
					protocolFormXmlData = protocolFormXmlDataDao
							.saveOrUpdate(protocolFormXmlData);
							
				}
			}

		}

		Document currentProtocolXmlDataDoc = xmlProcessor
				.loadXmlStringToDOM(protocolFormXmlData.getXmlData());
		NodeList list = currentProtocolXmlDataDoc.getElementsByTagName("*");

		logger.debug("totoal node list length: " + list.getLength());

		for (int i = 0; i < list.getLength(); i++) {
			Node nd = list.item(i);

			if (!nd.hasChildNodes() && !nd.hasAttributes()
					&& nd.getTextContent().isEmpty()) {
				Node parentNode = nd.getParentNode();
				parentNode.removeChild(nd);
			}

			for (int j = 0; j < nd.getChildNodes().getLength(); j++) {
				if (!nd.getChildNodes().item(j).hasChildNodes()
						&& !nd.getChildNodes().item(j).hasAttributes()
						&& nd.getChildNodes().item(j).getTextContent()
								.isEmpty()) {
					Node pnd = nd.getChildNodes().item(j).getParentNode();
					pnd.removeChild(nd.getChildNodes().item(j));
				}
			}
		}

		String finalXml = DomUtils.elementToString(currentProtocolXmlDataDoc);
		protocolFormXmlData.setXmlData(finalXml);
		protocolFormXmlData = protocolFormXmlDataDao
				.saveOrUpdate(protocolFormXmlData);

		return protocolFormXmlData;
	}

	private boolean needRemoved(List<String> lst, String dataValue, String type) {
		boolean needRemoved = false;

		String value = null;
		List<String> valueLst = null;

		if (lst.size() > 0) {
			if (dataValue.contains(",")) {
				valueLst = Arrays.asList(dataValue.split(","));
				
				if (type.equals("NOINTERSECT")){
					if (Collections.disjoint(valueLst, lst)){
						needRemoved = false;
					} else {
						needRemoved = true;
					}
				} else {
					//Collections.disjoint Returns true if the two specified collections have no elements in common
					if (Collections.disjoint(valueLst, lst)){
						needRemoved = true;
					} else {
						needRemoved = false;
					}
				}
			} else {
				value = dataValue;
				if (type.contains("NOT")){
					if (!lst.contains(value)) {
						needRemoved = false;
					} else {
						needRemoved = true;
					}
				} else {
					if (lst.contains(value)) {
						needRemoved = false;
					} else {
						needRemoved = true;
					}
				}
			}
		}
		logger.debug("lst: " + lst + " dataValue: " + dataValue + " type: " + type + " needRemoved: " + needRemoved);
		return needRemoved;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
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
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	public ProtocolFormDao getProcotolFormDao() {
		return procotolFormDao;
	}

	@Autowired(required = true)
	public void setProcotolFormDao(ProtocolFormDao procotolFormDao) {
		this.procotolFormDao = procotolFormDao;
	}

	public String getValidationXmlPath() {
		return validationXmlPath;
	}

	public void setValidationXmlPath(String validationXmlPath) {
		this.validationXmlPath = validationXmlPath;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}

	@Autowired(required = true)
	public void setProtocolFormStatusDao(
			ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolAndFormStatusService getProtocolAndFormStatusService() {
		return protocolAndFormStatusService;
	}

	@Autowired(required = true)
	public void setProtocolAndFormStatusService(
			ProtocolAndFormStatusService protocolAndFormStatusService) {
		this.protocolAndFormStatusService = protocolAndFormStatusService;
	}
}
