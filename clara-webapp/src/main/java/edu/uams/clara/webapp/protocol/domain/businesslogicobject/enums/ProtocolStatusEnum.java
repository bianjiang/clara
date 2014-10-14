package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;


public enum ProtocolStatusEnum {
	ANY("Any Status (placeholder)", "NORMAL"),
	ARCHIVED("Archived from ARIA/Crimson", "INFO"),
	ARCHIVED_OPEN("Archived from ARIA/Crimson - Open", "INFO"),
	LIMBO("Limbo Status (Program error occurred)", "ERROR"),
	TO_BE_DETERMINED("TO BE DETERMINED (placeholder)", "INFO"),
	DRAFT("Draft", "NORMAL"),
	PENDING_PI_ENDORSEMENT("Pending PI Endorsement", "WARN"),
	UNDER_IRB_OFFICE_REVIEW("Under IRB Office Review", "INFO"),
	UNDER_IRB_PREREVIEW("Under IRB Office Prereview", "INFO"),
	UNDER_PREREVIEW("Under Pre-review", "INFO"),
	REVISION_REQUESTED("Revision Requested", "ERROR"),
	NEW_SUBMISSION_REVISED("Protocol has been revised", "INFO"),
	PENDING_IRB_REVIEW_ASSIGNMENT("Pending IRB Review Assignment", "WARN"),
	PENDING_IRB_REVIEW_RE_ASSIGNMENT("Pending IRB Review Re-assignment", "WARN"),
	IRB_AGENDA_ASSIGNED("Assigned to an IRB Agenda", "INFO"),
	IRB_REVIEWER_ASSIGNED("IRB Reviewer Assigned", "INFO"),
	UNDER_COMMITTEE_REVIEW("Under Committee Review", "INFO"),
	IRB_APPROVED("IRB Approved", "INFO"),
	IRB_DECLINED("IRB Declined", "WARN"),
	IRB_DEFERRED("IRB Deferred", "WARN"),
	IRB_DEFERRED_WITH_MINOR_CONTINGENCIES("IRB Deferred With Minor Contingencies", "WARN"),
	IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES("IRB Deferred With Major Contingencies", "WARN"),
	IRB_TABLED("IRB Tabled", "INFO"),
	IRB_ACKNOWLEDGED("IRB Acknowledged", "INFO"),
	IRB_SUSPENDED("IRB Suspended for Cause", "INFO"),
	IRB_TERMINATED("IRB Terminated for Cause", "INFO"),
	IRB_CLOSED("IRB Closed", "INFO"),
	PENDING_CONTRACT("Pending Contract", "WARN"), 
	PENDING_IND_IDE("Pending IND/IDE", "WARN"), 
	OPEN_FOR_ENROLLMENT("Open For Enrollment", "INFO"), 
	ON_CLINICAL_HOLD("On Clinical Hold", "INFO"), 
	ON_HOLD("On Hold", "WARN"),
	CLOSE("Closed", "WARN"),
	CANCELLED("Study Cancelled", "WARN"),
	HSRD_ACKNOWLEDGED("IRB Office Acknowledged HSRD", "INFO"),
	HSRD_REVISION_REQUESTED("IRB Office Request Revisions on HSRD", "ERROR"),
	HSRD_REVISED("HSRD Revised", "INFO"),
	EXPEDITED_IRB_REVIEW("Expedited IRB Review", "INFO"),
	EXEMPT_IRB_REVIEW("Exempt IRB Review", "INFO"),
	PENDING_COMMITTEE_PREVIEW("Pending Committee Preview", "WARN"),
	PENDING_COMMITTEE_REVIEW("Pending Committee Review", "WARN"),
	UNDER_PHARMACY_REVIEW("Under Pharmacy Review", "INFO"),
	UNDER_BUDGET_REVIEW("Under Budget Review", "INFO"),
	UNDER_BUDGET_DEVELOP("Under Budget Development", "INFO"),
	UNDER_COVERAGE_REVIEW("Under Coverage Review", "INFO"),
	UNDER_HOSPITAL_SERVICES_REVIEW("Pending Hospital Services Review", "INFO"),
	UNDER_COLLEGE_REVIEW("Under College Administration Review", "INFO"),
	UNDER_REVISION("Under Revision", "INFO"),
	UNDER_REVISION_MAJOR_CONTINGENCIES("Under Revision With Major Contingencies", "INFO"),
	UNDER_REVISION_MINOR_CONTINGENCIES("Under Revision With Minor Contigencies", "INFO"),
	REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT("Revision With Major Contingencies Pending PI Endoresment", "WARN"),
	REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT("Revision With Minor Contingencies Pending PI Endoresment", "WARN"),
	UNDER_REVIEW("Under Review", "INFO"),
	UNDER_PTL_DEVELOP("Under PTL Develop", "INFO"),
	REVISION_PENDING_PI_ENDORSEMENT("Revision Pending PI Endorsement", "WARN"),
	EXPEDITED_APPROVED("Expedited Approved", "INFO"),
	EXPEDITED_DECLINED("Expedited Declined", "WARN"),
	EXEMPT_APPROVED("Exempt Approved", "INFO"),
	EXEMPT_DECLINED("Exempt Declined", "WARN"),
	DETERMINED_HUMAN_SUBJECT_RESEARCH("Determined - Human Subject Research", "INFO"),
	DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH("Determined - Not Human Subject Research", "WARN"),
	POTENTIAL_NON_COMPLIANCE("Potential Noncompliance Pending IRB Review Assignment", "ERROR"),
	PENDING_LEGAL_REVIEW("Pending Legal Review", "WARN"),
	PENDING_SPONSOR_RESPONSE("Pending Sponsor Response", "WARN"),
	EMERGENCY_USE_INTENDED_IRB_ACKNOWLEDGED("Intended Emergency Use IRB Acknowledged", "INFO"),
	EMERGENCY_USE_FOLLOW_UP_IRB_ACKNOWLEDGED("Follow-up Emergency Use IRB Acknowledged", "INFO"),
	EMERGENCY_USE_INTENDED_UNDER_IRB_OFFICE_REVIEW("Intended Emergency Use Under IRB office review", "INFO"),
	EMERGENCY_USE_FOLLOW_UP_UNDER_IRB_OFFICE_REVIEW("Follow-up Emergency Use Under IRB office review", "INFO"),
	DETERMINED_NOT_REPORTABLE_NEW_INFORMATION("Determined - Not Reportable New Information", "WARN"),
	UNDER_ACH_PREREVIEW("Under ACH Pre-review", "INFO"),
	UNDER_DEPARTMENT_REVIEW("Under Department Review", "INFO"),
	UNDER_LEGAL_REVIEW("Under Legal Review", "INFO"),
	PENDING_PI_SIGN_OFF("Pending PI Sign Off", "WARN"),
	UNDER_CONSENT_LEGAL_REVIEW("Under Consent Legal Review", "INFO"),
	BUDGET_REVIEW_COMPLETED("Budget Review Completed", "INFO"),
	UNDER_BUDGET_MANAGER_REVIEW("Under Budget Manager Review", "INFO"),
	PENDING_PL_ENDORSEMENT("Pending Project Leader Endorsement", "WARN"),
	PENDING_TP_ENDORSEMENT("Pending Treating Physician Endorsement", "WARN"),
	PENDING_REVIEWER_ASSIGNMENT("Pending Reviewer Assignment", "WARN"),
	PENDING_ASSESSMENT("Pending Assessment", "WARN"),
	OPEN("Open", "INFO"),
	DECLINED("Declined", "WARN"),
	SUSPENDED("Suspended", "INFO"),
	TERMINATED("Terminated", "INFO"),
	DETERMINED("Determined", "INFO"),
	CLOSED("Closed", "WARN"),
	EXPIRED("Expired", "WARN"),
	TABLED("Tabled", "INFO"),
	ACKNOWLEDGED("Acknowledged", "INFO"),
	UNDER_REVISION_RESPONSE_TO_TABLED("Under Revision Response to Tabled", "INFO"),
	RESPONSE_TO_TABLED_PENDING_PI_ENDORSEMENT("Response to Tabled Pending PI Endorsement", "WARN");
	
	private String description;
	private String priorityLevel;
	
	private ProtocolStatusEnum(String description, String priorityLevel){
		this.description = description;
		this.priorityLevel = priorityLevel;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getPriorityLevel() {
		return priorityLevel;
	}

	public void setPriorityLevel(String priorityLevel) {
		this.priorityLevel = priorityLevel;
	}
}
