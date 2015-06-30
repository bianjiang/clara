package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;


public enum ProtocolFormStatusEnum {
	ANY("Any Status (placeholder)", "NORMAL", false),
	ARCHIVED("Archived from ARIA/Crimson", "INFO", false),
	ARCHIVED_OPEN("Archived from ARIA/Crimson - Open", "INFO", false),
	LIMBO("Limbo Status (Program error occurred)", "ERROR", false),
	TO_BE_DETERMINED("TO BE DETERMINED (placeholder)", "INFO", false),
	DRAFT("Draft","NORMAL", false),
	PENDING_PI_ENDORSEMENT("Pending PI Endorsement", "WARN", true),
	UNDER_PREREVIEW("Under Pre-review", "INFO", false),
	UNDER_IRB_OFFICE_REVIEW("Under IRB Office Review", "INFO", false),
	UNDER_IRB_PREREVIEW("Under IRB Office Prereview", "INFO", false),
	UNDER_COMMITTEE_REVIEW("Under Committee(s) Review", "INFO", false),
	PENDING_IRB_REVIEW_ASSIGNMENT("Pending IRB Review Assignment", "WARN", false),
	PENDING_IRB_REVIEW_RE_ASSIGNMENT("Pending IRB Review Re-assignment", "WARN", false),
	IRB_AGENDA_ASSIGNED("Assigned to an IRB Agenda", "INFO", false),
	IRB_REVIEWER_ASSIGNED("IRB Reviewer Assigned", "INFO", false),
	PENDING_REVIEWER_ASSIGNMENT("Pending Reviewer Assignment", "INFO", false),
	APPROVED("Approved", "INFO", false),
	IRB_APPROVED("Approved by IRB", "INFO", false),
	IRB_DECLINED("IRB Declined", "WARN", false),
	IRB_DEFERRED("IRB Deferred", "WARN", false),
	IRB_DEFERRED_WITH_MINOR_CONTINGENCIES("IRB Deferred With Minor Contingencies", "WARN", true),
	IRB_DEFERRED_WITH_MAJOR_CONTINGENCIES("IRB Deferred With Major Contingencies", "WARN", true),
	IRB_TABLED("IRB Tabled", "INFO", true),
	IRB_ACKNOWLEDGED("IRB Acknowledged", "INFO", false),
	IRB_SUSPENDED("IRB Suspended for Cause", "INFO", false),
	IRB_TERMINATED("IRB Terminated for Cause", "INFO", false),
	IRB_CLOSED("IRB Closed", "INFO", false),
	COMPLETED("Completed", "INFO", false),
	ACKNOWLEDGED("Acknowledged", "INFO", false),
	REVISION_REQUESTED("Revision Requested", "ERROR", false),
	REVISED("Revised", "INFO", false),
	PENDING_EXPEDITED_IRB_REVIEW("Pending Expedited IRB Review", "WARN", false),
	PENDING_EXEMPT_IRB_REVIEW("Pending Exempt IRB Review", "WARN", false),
	RECEIVED("Received", "INFO", false),
	UNDER_PHARMACY_REVIEW("Pending Pharmacy Review", "INFO", false),
	UNDER_BUDGET_REVIEW("Pending Budget Review", "INFO", false),
	UNDER_BUDGET_DEVELOP("Pending Budget Development", "INFO", false),
	UNDER_COVERAGE_REVIEW("Pending Coverage Review", "INFO", false),
	UNDER_COLLEGE_REVIEW("Under College Administration Review", "INFO", false),
	UNDER_HOSPITAL_SERVICES_REVIEW("Pending Hospital Services Review", "INFO", false),
	UNDER_REVISION("Under Revision", "INFO", false),
	UNDER_REVISION_MAJOR_CONTINGENCIES("Under Revision With Major Contingencies", "WARN", false),
	UNDER_REVISION_MINOR_CONTINGENCIES("Under Revision With Minor Contingencies", "WARN", false),
	REVISION_WITH_MAJOR_PENDING_PI_ENDORSEMENT("Revision With Major Contingencies Pending PI Endorsement", "WARN", true),
	REVISION_WITH_MINOR_PENDING_PI_ENDORSEMENT("Revision With Minor Contingencies Pending PI Endorsement", "WARN", true),
	UNDER_REVIEW("Under Review", "WARN", false),
	UNDER_PTL_DEVELOP("Under PTL Develop", "WARN", false),
	REVISION_PENDING_PI_ENDORSEMENT("Revision Pending PI Endorsement", "WARN", true),
	EXPEDITED_APPROVED("Expedited Approved", "INFO", false),
	EXPEDITED_DECLINED("Expedited Declined", "WARN", false),
	EXEMPT_APPROVED("Exempt Approved", "INFO", false),
	EXEMPT_DECLINED("Exempt Declined", "WARN", false),
	DETERMINED_HUMAN_SUBJECT_RESEARCH("Determined - Human Subject Research", "INFO", false),
	DETERMINED_NOT_HUMAN_SUBJECT_RESEARCH("Determined - Not Human Subject Research", "WARN", false),
	POTENTIAL_NON_COMPLIANCE("Potential Noncompliance Pending IRB Review Assignment", "ERROR", false),
	PENDING_LEGAL_REVIEW("Pending Legal Review", "WARN", false),
	PENDING_SPONSOR_RESPONSE("Pending Sponsor Response", "WARN", false),
	EMERGENCY_USE_INTENDED_IRB_ACKNOWLEDGED("Intended Emergency Use IRB Acknowledged", "INFO", false),
	EMERGENCY_USE_FOLLOW_UP_IRB_ACKNOWLEDGED("Follow-up Emergency Use IRB Acknowledged", "INFO", false),
	EMERGENCY_USE_INTENDED_UNDER_IRB_OFFICE_REVIEW("Intended Emergency Use Under IRB office review", "INFO", false),
	EMERGENCY_USE_FOLLOW_UP_UNDER_IRB_OFFICE_REVIEW("Follow-up Emergency Use Under IRB office review", "INFO", false),
	DETERMINED_NOT_REPORTABLE_NEW_INFORMATION("Determined - Not Reportable New Information", "WARN", false),
	UNDER_ACH_PREREVIEW("Under ACH Pre-review", "INFO", false),
	UNDER_DEPARTMENT_REVIEW("Under Department Review", "INFO", false),
	UNDER_LEGAL_REVIEW("Under Legal Review", "INFO", false),
	PENDING_PI_SIGN_OFF("Pending PI Sign Off", "WARN", true),
	UNDER_CONSENT_LEGAL_REVIEW("Under Consent Legal Review", "INFO", false),
	BUDGET_REVIEW_COMPLETED("Budget Review Completed", "INFO", false),
	UNDER_BUDGET_MANAGER_REVIEW("Under Budget Manager Review", "INFO", false),
	PENDING_PL_ENDORSEMENT("Pending Project Leader Endorsement", "WARN", true),
	PENDING_TP_ENDORSEMENT("Pending Treating Physician Endorsement", "WARN", true),
	PENDING_ASSESSMENT("Pending Assessment", "WARN", false),
	CANCELLED("Cancelled", "WARN", false),
	UNDER_COMPLIANCE_REVIEW("Under Hospital Compliance Review", "INFO", false),
	COMPLIANCE_APPROVED("Hospital Compliance Approved", "INFO", false),
	COMPLIANCE_REJECTED("Hospital Compliance Rejected", "ERROR", false),
	ON_HOLD("On Hold", "WARN", false),
	IRB_REVIEW_NOT_NEEDED("IRB Review Not Needed", "INFO", false),
	BUDGET_NOT_REVIEWED("Budget Not Reviewed", "INFO", false),
	UNDER_REVISION_RESPONSE_TO_TABLED("Under Revision Response to Tabled", "INFO", false),
	RESPONSE_TO_TABLED_PENDING_PI_ENDORSEMENT("Response to Tabled Pending PI Endorsement", "WARN", true),
	RETURN_FOR_BUDGET_NEGOTIATIONS("Return for Budget Negotiations", "WARN", false),
	PENDING_BUDGET_NEGOTIATIONS("Pending Budget Negotiations", "WARN", false),
	PENDING_BUDGET_NEGOTIATIONS_PENDING_PI_ENDORSEMENT("Pending Budget Negotiations Pending PI Endorsement", "WARN", true),
	UNDER_PRECOVERAGE_REVIEW("Pending Pre-Coverage Review", "INFO", false);
	
	private String description;
	private String priorityLevel;
	private boolean pendingPIAction;
	
	private ProtocolFormStatusEnum(String description, String priorityLevel, Boolean pendingPIAction){
		this.description = description;
		this.priorityLevel = priorityLevel;
		this.pendingPIAction = pendingPIAction;
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

	public boolean isPendingPIAction() {
		return pendingPIAction;
	}

	public void setPendingPIAction(boolean pendingPIAction) {
		this.pendingPIAction = pendingPIAction;
	}
}
