package edu.uams.clara.webapp.report.domain;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormCommitteeStatusEnum;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;

public class CommitteeActions {
	
	private  Map<Committee, List<ProtocolFormCommitteeStatusEnum>> startCommitteeStatusMapForHSR = Maps
			.newHashMap();
	{
		// Expedited
		List<ProtocolFormCommitteeStatusEnum> expeditedList = Lists
				.newArrayList();
		expeditedList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_EXPEDITED_IRB_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.IRB_EXPEDITED_REVIEWER,
				expeditedList);

		// Exempt
		List<ProtocolFormCommitteeStatusEnum> exemptList = Lists.newArrayList();
		exemptList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_EXEMPT_IRB_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.IRB_EXEMPT_REVIEWER, exemptList);

		// irbReview
		List<ProtocolFormCommitteeStatusEnum> irbReviewList = Lists
				.newArrayList();
		irbReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.IRB_REVIEWER, irbReviewList);

		// irbOffice
		List<ProtocolFormCommitteeStatusEnum> irbOfficeList = Lists
				.newArrayList();
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_ASSIGNMENT);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
		//irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.IRB_OFFICE, irbOfficeList);

		// irbPrereview
		List<ProtocolFormCommitteeStatusEnum> irbPrereviewList = Lists
				.newArrayList();
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.IRB_PREREVIEW, irbPrereviewList);

		// irbConsentReview
		List<ProtocolFormCommitteeStatusEnum> irbConsentReviewList = Lists
				.newArrayList();
		irbConsentReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.IRB_CONSENT_REVIEWER,
				irbConsentReviewList);

		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		startCommitteeStatusMapForHSR.put(Committee.IRB_ASSIGNER, irbAssignerList);

		// colleageDean
		List<ProtocolFormCommitteeStatusEnum> colleageDeanList = Lists
				.newArrayList();
		colleageDeanList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.COLLEGE_DEAN, colleageDeanList);

		// departmentChair
		List<ProtocolFormCommitteeStatusEnum> departmentChairList = Lists
				.newArrayList();
		departmentChairList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMapForHSR.put(Committee.DEPARTMENT_CHAIR,
				departmentChairList);
	}
	
	private  Map<Committee, List<ProtocolFormCommitteeStatusEnum>> endCommitteeStatusMapForHSR = Maps
			.newHashMap();
	{
		List<ProtocolFormCommitteeStatusEnum> expeditedList = Lists
				.newArrayList();
		expeditedList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		expeditedList
				.add(ProtocolFormCommitteeStatusEnum.NOT_APPLICABLE_TO_EXPEDITED_IRB_REVIEW);
		expeditedList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMapForHSR.put(Committee.IRB_EXPEDITED_REVIEWER,
				expeditedList);

		// exempt
		List<ProtocolFormCommitteeStatusEnum> exemptList = Lists.newArrayList();
		exemptList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		exemptList
				.add(ProtocolFormCommitteeStatusEnum.NOT_APPLICABLE_TO_EXEMPT_IRB_REVIEW);
		exemptList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		exemptList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMapForHSR.put(Committee.IRB_EXEMPT_REVIEWER, exemptList);

		// irbReview
		List<ProtocolFormCommitteeStatusEnum> irbReviewList = Lists
				.newArrayList();
		irbReviewList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbReviewList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
		irbReviewList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
		irbReviewList
				.add(ProtocolFormCommitteeStatusEnum.REMOVED_FROM_IRB_AGENDA);
		endCommitteeStatusMapForHSR.put(Committee.IRB_REVIEWER, irbReviewList);

		// irbOffice
		List<ProtocolFormCommitteeStatusEnum> irbOfficeList = Lists
				.newArrayList();
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.EXEMPT_IRB_REVIEW);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.EXPEDITED_IRB_REVIEW);
/*		
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);*/
		endCommitteeStatusMapForHSR.put(Committee.IRB_OFFICE, irbOfficeList);

		// irbPrereview
		List<ProtocolFormCommitteeStatusEnum> irbPrereviewList = Lists
				.newArrayList();
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbPrereviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMapForHSR.put(Committee.IRB_PREREVIEW, irbPrereviewList);

		// irbConsentReview
		List<ProtocolFormCommitteeStatusEnum> irbConsentReviewList = Lists
				.newArrayList();
		irbConsentReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMapForHSR.put(Committee.IRB_CONSENT_REVIEWER,
				irbConsentReviewList);

		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		endCommitteeStatusMapForHSR.put(Committee.IRB_ASSIGNER, irbAssignerList);

		

		// colleageDean
		List<ProtocolFormCommitteeStatusEnum> colleageDeanList = Lists
				.newArrayList();
		colleageDeanList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMapForHSR.put(Committee.COLLEGE_DEAN, colleageDeanList);

		// departmentChair
		List<ProtocolFormCommitteeStatusEnum> departmentChairList = Lists
				.newArrayList();
		departmentChairList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMapForHSR.put(Committee.DEPARTMENT_CHAIR,
				departmentChairList);
	}
	
	private  Map<Committee, List<ProtocolFormCommitteeStatusEnum>> startCommitteeStatusMap = Maps
			.newHashMap();
	{
		// Expedited
		List<ProtocolFormCommitteeStatusEnum> expeditedList = Lists
				.newArrayList();
		expeditedList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_EXPEDITED_IRB_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_EXPEDITED_REVIEWER,
				expeditedList);

		// Exempt
		List<ProtocolFormCommitteeStatusEnum> exemptList = Lists.newArrayList();
		exemptList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_EXEMPT_IRB_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_EXEMPT_REVIEWER, exemptList);

		// irbReview
		List<ProtocolFormCommitteeStatusEnum> irbReviewList = Lists
				.newArrayList();
		irbReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_REVIEWER, irbReviewList);

		// irbOffice
		List<ProtocolFormCommitteeStatusEnum> irbOfficeList = Lists
				.newArrayList();
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_ASSIGNMENT);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
		//irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_OFFICE, irbOfficeList);

		// irbPrereview
		List<ProtocolFormCommitteeStatusEnum> irbPrereviewList = Lists
				.newArrayList();
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_PREREVIEW, irbPrereviewList);

		// irbConsentReview
		List<ProtocolFormCommitteeStatusEnum> irbConsentReviewList = Lists
				.newArrayList();
		irbConsentReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.IRB_CONSENT_REVIEWER,
				irbConsentReviewList);

		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		startCommitteeStatusMap.put(Committee.IRB_ASSIGNER, irbAssignerList);

		// budgetReview
		List<ProtocolFormCommitteeStatusEnum> budgetReviewList = Lists
				.newArrayList();
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.BUDGET_REVIEW, budgetReviewList);

		// budgetManager
		List<ProtocolFormCommitteeStatusEnum> budgetManagerList = Lists
				.newArrayList();
		budgetManagerList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		/*budgetManagerList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);*/
		startCommitteeStatusMap
				.put(Committee.BUDGET_MANAGER, budgetManagerList);

		// coverageReview
		List<ProtocolFormCommitteeStatusEnum> coverageReviewList = Lists
				.newArrayList();
		coverageReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.COVERAGE_REVIEW,
				coverageReviewList);

		// pharmacyReview
		List<ProtocolFormCommitteeStatusEnum> pharmacyReviewList = Lists
				.newArrayList();
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED);
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.IN_WAIVER_REQUESTED);
		startCommitteeStatusMap.put(Committee.PHARMACY_REVIEW,
				pharmacyReviewList);

		// hospitalReview
		List<ProtocolFormCommitteeStatusEnum> hospitalReviewList = Lists
				.newArrayList();
		hospitalReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.HOSPITAL_SERVICES,
				hospitalReviewList);

		// colleageDean
		List<ProtocolFormCommitteeStatusEnum> colleageDeanList = Lists
				.newArrayList();
		colleageDeanList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.COLLEGE_DEAN, colleageDeanList);

		// departmentChair
		List<ProtocolFormCommitteeStatusEnum> departmentChairList = Lists
				.newArrayList();
		departmentChairList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.DEPARTMENT_CHAIR,
				departmentChairList);

		// gatekeeper
		List<ProtocolFormCommitteeStatusEnum> gatekeeperList = Lists
				.newArrayList();
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.GATEKEEPER, gatekeeperList);

		// COMPLIANCE_REVIEW
		List<ProtocolFormCommitteeStatusEnum> complianceList = Lists
				.newArrayList();
		complianceList
				.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW_FOR_BUDGET);
		complianceList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap
				.put(Committee.COMPLIANCE_REVIEW, complianceList);

		// achri
		List<ProtocolFormCommitteeStatusEnum> achriList = Lists.newArrayList();
		achriList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		startCommitteeStatusMap.put(Committee.ACHRI, achriList);
		
		// PROTOCOL_LEGAL_REVIEW
		List<ProtocolFormCommitteeStatusEnum> protocolLegalReviewList = Lists.newArrayList();
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.IN_LEGAL_REVIEW);
		startCommitteeStatusMap.put(Committee.PROTOCOL_LEGAL_REVIEW, protocolLegalReviewList);
		
		// BIOSAFETY
		List<ProtocolFormCommitteeStatusEnum> biosafetyList = Lists.newArrayList();
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		startCommitteeStatusMap.put(Committee.BIOSAFETY, biosafetyList);
		
		// PRMC
		List<ProtocolFormCommitteeStatusEnum> prmcList = Lists.newArrayList();
		prmcList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		startCommitteeStatusMap.put(Committee.PRMC, prmcList);		
		
		// RADIATION_SAFETY
		List<ProtocolFormCommitteeStatusEnum> radiationSafetyList = Lists.newArrayList();
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		startCommitteeStatusMap.put(Committee.RADIATION_SAFETY, radiationSafetyList);	
		
		// COI
		List<ProtocolFormCommitteeStatusEnum> coiList = Lists.newArrayList();
		coiList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		startCommitteeStatusMap.put(Committee.COI, coiList);	
		
		//MONITORING_REGULATORY_QA
		List<ProtocolFormCommitteeStatusEnum> monitoringList = Lists.newArrayList();
		monitoringList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		startCommitteeStatusMap.put(Committee.MONITORING_REGULATORY_QA, monitoringList);	
		
		//CLINICAL_TRIALS_REVIEW
		List<ProtocolFormCommitteeStatusEnum> clinicalTrialsList = Lists.newArrayList();
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		startCommitteeStatusMap.put(Committee.CLINICAL_TRIALS_REVIEW, clinicalTrialsList);	
	}
	private  Map<Committee, List<ProtocolFormCommitteeStatusEnum>> endCommitteeStatusMap = Maps
			.newHashMap();
	{
		List<ProtocolFormCommitteeStatusEnum> expeditedList = Lists
				.newArrayList();
		expeditedList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		expeditedList
				.add(ProtocolFormCommitteeStatusEnum.NOT_APPLICABLE_TO_EXPEDITED_IRB_REVIEW);
		expeditedList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMap.put(Committee.IRB_EXPEDITED_REVIEWER,
				expeditedList);

		// exempt
		List<ProtocolFormCommitteeStatusEnum> exemptList = Lists.newArrayList();
		exemptList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		exemptList
				.add(ProtocolFormCommitteeStatusEnum.NOT_APPLICABLE_TO_EXEMPT_IRB_REVIEW);
		exemptList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		exemptList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMap.put(Committee.IRB_EXEMPT_REVIEWER, exemptList);

		// irbReview
		List<ProtocolFormCommitteeStatusEnum> irbReviewList = Lists
				.newArrayList();
		irbReviewList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbReviewList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
		irbReviewList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
		irbReviewList
				.add(ProtocolFormCommitteeStatusEnum.REMOVED_FROM_IRB_AGENDA);
		endCommitteeStatusMap.put(Committee.IRB_REVIEWER, irbReviewList);

		// irbOffice
		List<ProtocolFormCommitteeStatusEnum> irbOfficeList = Lists
				.newArrayList();
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.IRB_AGENDA_ASSIGNED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.EXEMPT_IRB_REVIEW);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.EXPEDITED_IRB_REVIEW);
/*		
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MINOR_CONTINGENCIES);
		irbOfficeList
				.add(ProtocolFormCommitteeStatusEnum.DEFERRED_WITH_MAJOR_CONTINGENCIES);
		irbOfficeList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);*/
		endCommitteeStatusMap.put(Committee.IRB_OFFICE, irbOfficeList);

		// irbPrereview
		List<ProtocolFormCommitteeStatusEnum> irbPrereviewList = Lists
				.newArrayList();
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		irbPrereviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		irbPrereviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMap.put(Committee.IRB_PREREVIEW, irbPrereviewList);

		// irbConsentReview
		List<ProtocolFormCommitteeStatusEnum> irbConsentReviewList = Lists
				.newArrayList();
		irbConsentReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.IRB_CONSENT_REVIEWER,
				irbConsentReviewList);

		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		endCommitteeStatusMap.put(Committee.IRB_ASSIGNER, irbAssignerList);

		// budgetReview
		List<ProtocolFormCommitteeStatusEnum> budgetReviewList = Lists
				.newArrayList();
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		budgetReviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMap.put(Committee.BUDGET_REVIEW, budgetReviewList);

		// budgetManager
		List<ProtocolFormCommitteeStatusEnum> budgetManagerList = Lists
				.newArrayList();
		budgetManagerList
				.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		budgetManagerList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		endCommitteeStatusMap.put(Committee.BUDGET_MANAGER, budgetManagerList);

		// coverageReview
		List<ProtocolFormCommitteeStatusEnum> coverageReviewList = Lists
				.newArrayList();
		coverageReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		coverageReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		coverageReviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		coverageReviewList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_CONSENT_LEGAL_REVIEW);
		endCommitteeStatusMap
				.put(Committee.COVERAGE_REVIEW, coverageReviewList);

		// pharmacyReview
		List<ProtocolFormCommitteeStatusEnum> pharmacyReviewList = Lists
				.newArrayList();
		pharmacyReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.WAIVER_REQUEST_APPROVED);
		endCommitteeStatusMap
				.put(Committee.PHARMACY_REVIEW, pharmacyReviewList);

		// hospitalReview
		List<ProtocolFormCommitteeStatusEnum> hospitalReviewList = Lists
				.newArrayList();
		hospitalReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.HOSPITAL_SERVICES,
				hospitalReviewList);

		// colleageDean
		List<ProtocolFormCommitteeStatusEnum> colleageDeanList = Lists
				.newArrayList();
		colleageDeanList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.COLLEGE_DEAN, colleageDeanList);

		// departmentChair
		List<ProtocolFormCommitteeStatusEnum> departmentChairList = Lists
				.newArrayList();
		departmentChairList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.DEPARTMENT_CHAIR,
				departmentChairList);

		// gatekeeper
		List<ProtocolFormCommitteeStatusEnum> gatekeeperList = Lists
				.newArrayList();
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.PENDING_PTL_DEVELOP);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		endCommitteeStatusMap.put(Committee.GATEKEEPER, gatekeeperList);

		// COMPLIANCE_REVIEW
		List<ProtocolFormCommitteeStatusEnum> complianceList = Lists
				.newArrayList();
		complianceList
				.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		complianceList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		complianceList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.COMPLIANCE_REVIEW, complianceList);

		// achri
		List<ProtocolFormCommitteeStatusEnum> achriList = Lists.newArrayList();
		achriList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		achriList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		endCommitteeStatusMap.put(Committee.ACHRI, achriList);
		
		// PROTOCOL_LEGAL_REVIEW
		List<ProtocolFormCommitteeStatusEnum> protocolLegalReviewList = Lists.newArrayList();
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.CONTRACT_REQUIRED);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.CONTRACT_NOT_REQUIRED);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_DETERMINATION);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.PENDING_SPONSOR_RESPONSE);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		endCommitteeStatusMap.put(Committee.PROTOCOL_LEGAL_REVIEW, protocolLegalReviewList);
		
		// BIOSAFETY
		List<ProtocolFormCommitteeStatusEnum> biosafetyList = Lists.newArrayList();
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		endCommitteeStatusMap.put(Committee.BIOSAFETY, biosafetyList);
		
		// PRMC
		List<ProtocolFormCommitteeStatusEnum> prmcList = Lists.newArrayList();
		prmcList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		prmcList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		prmcList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		prmcList.add(ProtocolFormCommitteeStatusEnum.APPROVE_WITH_CONTINGENCIES);
		prmcList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		endCommitteeStatusMap.put(Committee.PRMC, prmcList);
		
		// RADIATION_SAFETY
		List<ProtocolFormCommitteeStatusEnum> radiationSafetyList = Lists.newArrayList();
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		endCommitteeStatusMap.put(Committee.RADIATION_SAFETY, radiationSafetyList);	

		// COI
		List<ProtocolFormCommitteeStatusEnum> coiList = Lists.newArrayList();
		coiList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		coiList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		coiList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		endCommitteeStatusMap.put(Committee.COI, coiList);
		
		//MONITORING_REGULATORY_QA
		List<ProtocolFormCommitteeStatusEnum> monitoringList = Lists.newArrayList();
		monitoringList.add(ProtocolFormCommitteeStatusEnum.EXEMPT_FROM_IND_IDE_REGULATIONS);
		monitoringList.add(ProtocolFormCommitteeStatusEnum.REGULATORY_AFFAIRS_INVOLVEMENT_NOT_REQUIRED);
		monitoringList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		
		endCommitteeStatusMap.put(Committee.MONITORING_REGULATORY_QA, monitoringList);	
		
		//CLINICAL_TRIALS_REVIEW
		List<ProtocolFormCommitteeStatusEnum> clinicalTrialsList = Lists.newArrayList();
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		endCommitteeStatusMap.put(Committee.CLINICAL_TRIALS_REVIEW, clinicalTrialsList);	
	}
	
	private  Map<Committee, List<ProtocolFormCommitteeStatusEnum>> gateKeeperAssignedstartCommitteeStatusMap = Maps
			.newHashMap();
	{
	
		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
				.newArrayList();
		irbAssignerList
				.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.IRB_ASSIGNER, irbAssignerList);
		
		// budgetManager
		List<ProtocolFormCommitteeStatusEnum> budgetManagerList = Lists
						.newArrayList();
		budgetManagerList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		budgetManagerList.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		gateKeeperAssignedstartCommitteeStatusMap
						.put(Committee.BUDGET_MANAGER, budgetManagerList);
		
		
		
		// budgetReview
		List<ProtocolFormCommitteeStatusEnum> budgetReviewList = Lists
				.newArrayList();
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.BUDGET_REVIEW, budgetReviewList);

		

		// pharmacyReview
		List<ProtocolFormCommitteeStatusEnum> pharmacyReviewList = Lists
				.newArrayList();
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW_REQUESTED);
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.IN_WAIVER_REQUESTED);
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.PHARMACY_REVIEW,
				pharmacyReviewList);

		// gatekeeper
		List<ProtocolFormCommitteeStatusEnum> gatekeeperList = Lists
				.newArrayList();
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.GATEKEEPER, gatekeeperList);

		

		// achri
		List<ProtocolFormCommitteeStatusEnum> achriList = Lists.newArrayList();
		achriList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.ACHRI, achriList);
		
		// PROTOCOL_LEGAL_REVIEW
		List<ProtocolFormCommitteeStatusEnum> protocolLegalReviewList = Lists.newArrayList();
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.IN_LEGAL_REVIEW);
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.PROTOCOL_LEGAL_REVIEW, protocolLegalReviewList);
		
		// BIOSAFETY
		List<ProtocolFormCommitteeStatusEnum> biosafetyList = Lists.newArrayList();
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.BIOSAFETY, biosafetyList);
		
		// PRMC
		List<ProtocolFormCommitteeStatusEnum> prmcList = Lists.newArrayList();
		prmcList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.PRMC, prmcList);		
		
		// RADIATION_SAFETY
		List<ProtocolFormCommitteeStatusEnum> radiationSafetyList = Lists.newArrayList();
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.RADIATION_SAFETY, radiationSafetyList);	
		
		// COI
		List<ProtocolFormCommitteeStatusEnum> coiList = Lists.newArrayList();
		coiList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.COI, coiList);	
		
		//MONITORING_REGULATORY_QA
		List<ProtocolFormCommitteeStatusEnum> monitoringList = Lists.newArrayList();
		monitoringList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.MONITORING_REGULATORY_QA, monitoringList);	
		
		//CLINICAL_TRIALS_REVIEW
		List<ProtocolFormCommitteeStatusEnum> clinicalTrialsList = Lists.newArrayList();
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.IN_REVIEW);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.CLINICAL_TRIALS_REVIEW, clinicalTrialsList);	
		
		//REGULATORY_MANAGER
		List<ProtocolFormCommitteeStatusEnum> regulatoryManagerTrialsList = Lists.newArrayList();
		regulatoryManagerTrialsList.add(ProtocolFormCommitteeStatusEnum.PENDING_REVIEWER_ASSIGNMENT);
		
		gateKeeperAssignedstartCommitteeStatusMap.put(Committee.REGULATORY_MANAGER, regulatoryManagerTrialsList);	
		
	}
	private  Map<Committee, List<ProtocolFormCommitteeStatusEnum>> gateKeeperAssignedendCommitteeStatusMap = Maps
			.newHashMap();
	{
		// irbAssigner
		List<ProtocolFormCommitteeStatusEnum> irbAssignerList = Lists
						.newArrayList();
		irbAssignerList.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.IRB_ASSIGNER, irbAssignerList);		
		
		// budgetManager
		List<ProtocolFormCommitteeStatusEnum> budgetManagerList = Lists
						.newArrayList();
		budgetManagerList
						.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		budgetManagerList
						.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.BUDGET_MANAGER, budgetManagerList);

		// budgetReview
		List<ProtocolFormCommitteeStatusEnum> budgetReviewList = Lists
				.newArrayList();
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		budgetReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		budgetReviewList
				.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.BUDGET_REVIEW, budgetReviewList);

	

		// pharmacyReview
		List<ProtocolFormCommitteeStatusEnum> pharmacyReviewList = Lists
				.newArrayList();
		pharmacyReviewList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		pharmacyReviewList
				.add(ProtocolFormCommitteeStatusEnum.WAIVER_REQUEST_APPROVED);
		gateKeeperAssignedendCommitteeStatusMap
				.put(Committee.PHARMACY_REVIEW, pharmacyReviewList);

	

		// gatekeeper
		List<ProtocolFormCommitteeStatusEnum> gatekeeperList = Lists
				.newArrayList();
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.PENDING_PTL_DEVELOP);
		gatekeeperList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.GATEKEEPER, gatekeeperList);

	

		// achri
		List<ProtocolFormCommitteeStatusEnum> achriList = Lists.newArrayList();
		achriList.add(ProtocolFormCommitteeStatusEnum.REVISION_REQUESTED);
		achriList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.ACHRI, achriList);
		
		// PROTOCOL_LEGAL_REVIEW
		List<ProtocolFormCommitteeStatusEnum> protocolLegalReviewList = Lists.newArrayList();
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.CONTRACT_REQUIRED);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.CONTRACT_NOT_REQUIRED);
		//protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.PENDING_IRB_DETERMINATION);
		//protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.PENDING_SPONSOR_RESPONSE);
		protocolLegalReviewList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.PROTOCOL_LEGAL_REVIEW, protocolLegalReviewList);
		
		// BIOSAFETY
		List<ProtocolFormCommitteeStatusEnum> biosafetyList = Lists.newArrayList();
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		biosafetyList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.BIOSAFETY, biosafetyList);
		
		// PRMC
		List<ProtocolFormCommitteeStatusEnum> prmcList = Lists.newArrayList();
		prmcList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		prmcList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		prmcList.add(ProtocolFormCommitteeStatusEnum.ACKNOWLEDGED);
		prmcList.add(ProtocolFormCommitteeStatusEnum.APPROVE_WITH_CONTINGENCIES);
		prmcList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.PRMC, prmcList);
		
		// RADIATION_SAFETY
		List<ProtocolFormCommitteeStatusEnum> radiationSafetyList = Lists.newArrayList();
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		radiationSafetyList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.RADIATION_SAFETY, radiationSafetyList);	

		// COI
		List<ProtocolFormCommitteeStatusEnum> coiList = Lists.newArrayList();
		coiList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		coiList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		coiList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.COI, coiList);
		
		//MONITORING_REGULATORY_QA
		List<ProtocolFormCommitteeStatusEnum> monitoringList = Lists.newArrayList();
		monitoringList.add(ProtocolFormCommitteeStatusEnum.EXEMPT_FROM_IND_IDE_REGULATIONS);
		monitoringList.add(ProtocolFormCommitteeStatusEnum.REGULATORY_AFFAIRS_INVOLVEMENT_NOT_REQUIRED);
		monitoringList.add(ProtocolFormCommitteeStatusEnum.COMPLETED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.MONITORING_REGULATORY_QA, monitoringList);	
		
		//CLINICAL_TRIALS_REVIEW
		List<ProtocolFormCommitteeStatusEnum> clinicalTrialsList = Lists.newArrayList();
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.COMMITTEE_REVIEW_NOT_APPLICABLE);
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.APPROVED);
		clinicalTrialsList.add(ProtocolFormCommitteeStatusEnum.REJECTED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.CLINICAL_TRIALS_REVIEW, clinicalTrialsList);	
		
		//REGULATORY_MANAGER
		List<ProtocolFormCommitteeStatusEnum> regulatoryManagerTrialsList = Lists.newArrayList();
		regulatoryManagerTrialsList.add(ProtocolFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		
		gateKeeperAssignedendCommitteeStatusMap.put(Committee.REGULATORY_MANAGER, regulatoryManagerTrialsList);	
	}
	
	private  List<ProtocolFormStatusEnum> draftFormStatus = Lists.newArrayList();
	{
		draftFormStatus.add(ProtocolFormStatusEnum.DRAFT);
		draftFormStatus.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		draftFormStatus.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		draftFormStatus.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
	}
	
	private  List<ProtocolFormStatusEnum> revisionRequestedStatus = Lists.newArrayList();
	{
		revisionRequestedStatus.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		revisionRequestedStatus.add(ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MINOR_CONTINGENCIES);
		revisionRequestedStatus.add(ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES);
	}
	
	private  List<ProtocolFormStatusEnum> irbSubmissionStatus = Lists.newArrayList();
	{
		irbSubmissionStatus.add(ProtocolFormStatusEnum.UNDER_IRB_PREREVIEW);
		irbSubmissionStatus.add(ProtocolFormStatusEnum.PENDING_IRB_REVIEW_RE_ASSIGNMENT);
	}
	
	private  List<ProtocolFormStatusEnum> piStartActions = Lists.newArrayList();{
		piStartActions.add(ProtocolFormStatusEnum.DRAFT);
		piStartActions.add(ProtocolFormStatusEnum.UNDER_REVISION);
		piStartActions.add(ProtocolFormStatusEnum.REVISION_REQUESTED);
		piStartActions.add(ProtocolFormStatusEnum.REVISION_PENDING_PI_ENDORSEMENT);
		piStartActions.add(ProtocolFormStatusEnum.PENDING_PI_ENDORSEMENT);
		piStartActions.add(ProtocolFormStatusEnum.PENDING_PI_SIGN_OFF);
		piStartActions.add(ProtocolFormStatusEnum.PENDING_PL_ENDORSEMENT);
		piStartActions.add(ProtocolFormStatusEnum.PENDING_TP_ENDORSEMENT);
		piStartActions.add(ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES);
		piStartActions.add(ProtocolFormStatusEnum.IRB_DEFERRED_WITH_MINOR_CONTINGENCIES);
		piStartActions.add(ProtocolFormStatusEnum.UNDER_REVISION_MAJOR_CONTINGENCIES);
		piStartActions.add(ProtocolFormStatusEnum.UNDER_REVISION_MINOR_CONTINGENCIES);
		piStartActions.add(ProtocolFormStatusEnum.REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT);
		piStartActions.add(ProtocolFormStatusEnum.REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT);
	}
	
	private  List<ProtocolFormStatusEnum> reviewTypeBeforeIRB = Lists.newArrayList();
	{
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_PREREVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_ACH_PREREVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_BUDGET_MANAGER_REVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_BUDGET_REVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_COVERAGE_REVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_HOSPITAL_SERVICES_REVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_DEPARTMENT_REVIEW);
		reviewTypeBeforeIRB.add(ProtocolFormStatusEnum.UNDER_COLLEGE_REVIEW);
		
		
	}
	
	private  List<ProtocolFormStatusEnum> completeFormStatus = Lists.newArrayList();
	{
		completeFormStatus.add(ProtocolFormStatusEnum.ACKNOWLEDGED);
		completeFormStatus.add(ProtocolFormStatusEnum.APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.COMPLETED);
		completeFormStatus.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_ACKNOWLEDGED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_APPROVED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_DECLINED);
		completeFormStatus.add(ProtocolFormStatusEnum.IRB_REVIEW_NOT_NEEDED);
		completeFormStatus.add(ProtocolFormStatusEnum.DETERMINED_HUMAN_SUBJECT_RESEARCH);
		completeFormStatus.add(ProtocolFormStatusEnum.DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH);
		completeFormStatus.add(ProtocolFormStatusEnum.DETERMINED_NOT_REPORTABLE_NEW_INFORMATION);
	}
	
	
	private  List<ProtocolFormStatusEnum> irbApprovalNSFFormStatus = Lists.newArrayList();
	{
		irbApprovalNSFFormStatus.add(ProtocolFormStatusEnum.EXEMPT_APPROVED);
		irbApprovalNSFFormStatus.add(ProtocolFormStatusEnum.EXPEDITED_APPROVED);
		irbApprovalNSFFormStatus.add(ProtocolFormStatusEnum.IRB_APPROVED);
	}
	
	
	private Map<Committee, List<ContractFormCommitteeStatusEnum>> startContractCommitteeStatusMap = Maps.newHashMap();{
		List<ContractFormCommitteeStatusEnum> contractManagerList = Lists.newArrayList();
		contractManagerList.add(ContractFormCommitteeStatusEnum.IN_REVIEW);
		startContractCommitteeStatusMap.put(Committee.CONTRACT_MANAGER, contractManagerList);
		
		List<ContractFormCommitteeStatusEnum> contractAdminList = Lists.newArrayList();
		contractAdminList.add(ContractFormCommitteeStatusEnum.IN_REVIEW);
		startContractCommitteeStatusMap.put(Committee.CONTRACT_ADMIN, contractAdminList);
		
		List<ContractFormCommitteeStatusEnum> contractLegalList = Lists.newArrayList();
		contractLegalList.add(ContractFormCommitteeStatusEnum.IN_REVIEW);
		startContractCommitteeStatusMap.put(Committee.CONTRACT_LEGAL_REVIEW, contractLegalList);
	}
	
	private Map<Committee, List<ContractFormCommitteeStatusEnum>> endContractCommitteeStatusMap = Maps.newHashMap();{
		List<ContractFormCommitteeStatusEnum> contractManagerList = Lists.newArrayList();
		contractManagerList.add(ContractFormCommitteeStatusEnum.REVIEWER_ASSIGNED);
		contractManagerList.add(ContractFormCommitteeStatusEnum.REVISION_REQUESTED);
		endContractCommitteeStatusMap.put(Committee.CONTRACT_MANAGER, contractManagerList);
		
		List<ContractFormCommitteeStatusEnum> contractAdminList = Lists.newArrayList();
		contractAdminList.add(ContractFormCommitteeStatusEnum.APPROVED);
		contractAdminList.add(ContractFormCommitteeStatusEnum.REVISION_REQUESTED);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_SPONSOR_RESPONSE);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_PI);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_BUDGET);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_COVERAGE);
		contractAdminList.add(ContractFormCommitteeStatusEnum.PENDING_IRB);
		contractAdminList.add(ContractFormCommitteeStatusEnum.OUTSIDE_IRB);
		endContractCommitteeStatusMap.put(Committee.CONTRACT_ADMIN, contractAdminList);
		
		List<ContractFormCommitteeStatusEnum> contractLegalList = Lists.newArrayList();
		contractLegalList.add(ContractFormCommitteeStatusEnum.COMPLETED);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_SPONSOR_RESPONSE);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_PI);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_BUDGET);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_COVERAGE);
		contractLegalList.add(ContractFormCommitteeStatusEnum.PENDING_IRB);
		contractLegalList.add(ContractFormCommitteeStatusEnum.OUTSIDE_IRB);
		contractLegalList.add(ContractFormCommitteeStatusEnum.APPROVED);
		endContractCommitteeStatusMap.put(Committee.CONTRACT_LEGAL_REVIEW, contractLegalList);
		
		
		
	}
	
	public Map<Committee, List<ProtocolFormCommitteeStatusEnum>> getStartCommitteeStatusMap() {
		return startCommitteeStatusMap;
	}

	public Map<Committee, List<ProtocolFormCommitteeStatusEnum>> getEndCommitteeStatusMap() {
		return endCommitteeStatusMap;
	}

	public List<ProtocolFormStatusEnum> getDraftFormStatus() {
		return draftFormStatus;
	}

	public List<ProtocolFormStatusEnum> getCompleteFormStatus() {
		return completeFormStatus;
	}

	public List<ProtocolFormStatusEnum> getPiStartActions() {
		return piStartActions;
	}

	public List<ProtocolFormStatusEnum> getIrbApprovalNSFFormStatus() {
		return irbApprovalNSFFormStatus;
	}

	public Map<Committee, List<ProtocolFormCommitteeStatusEnum>> getStartCommitteeStatusMapForHSR() {
		return startCommitteeStatusMapForHSR;
	}

	public Map<Committee, List<ProtocolFormCommitteeStatusEnum>> getEndCommitteeStatusMapForHSR() {
		return endCommitteeStatusMapForHSR;
	}

	public List<ProtocolFormStatusEnum> getRevisionRequestedStatus() {
		return revisionRequestedStatus;
	}

	public List<ProtocolFormStatusEnum> getIrbSubmissionStatus() {
		return irbSubmissionStatus;
	}

	public List<ProtocolFormStatusEnum> getReviewTypeBeforeIRB() {
		return reviewTypeBeforeIRB;
	}

	public Map<Committee, List<ContractFormCommitteeStatusEnum>> getEndContractCommitteeStatusMap() {
		return endContractCommitteeStatusMap;
	}

	public Map<Committee, List<ContractFormCommitteeStatusEnum>> getStartContractCommitteeStatusMap() {
		return startContractCommitteeStatusMap;
	}

	public Map<Committee, List<ProtocolFormCommitteeStatusEnum>> getGateKeeperAssignedendCommitteeStatusMap() {
		return gateKeeperAssignedendCommitteeStatusMap;
	}

	public void setGateKeeperAssignedendCommitteeStatusMap(
			Map<Committee, List<ProtocolFormCommitteeStatusEnum>> gateKeeperAssignedendCommitteeStatusMap) {
		this.gateKeeperAssignedendCommitteeStatusMap = gateKeeperAssignedendCommitteeStatusMap;
	}

	public Map<Committee, List<ProtocolFormCommitteeStatusEnum>> getGateKeeperAssignedstartCommitteeStatusMap() {
		return gateKeeperAssignedstartCommitteeStatusMap;
	}

	public void setGateKeeperAssignedstartCommitteeStatusMap(
			Map<Committee, List<ProtocolFormCommitteeStatusEnum>> gateKeeperAssignedstartCommitteeStatusMap) {
		this.gateKeeperAssignedstartCommitteeStatusMap = gateKeeperAssignedstartCommitteeStatusMap;
	}

}
