package edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums;


public enum ProtocolFormCommitteeStatusEnum{
	ANY("Any Status (placeholder)", "NORMAL", ""),
	LIMBO("Limbo Status (Program error occurred)", "ERROR", ""),
	TO_BE_DETERMINED("TO BE DETERMINED (placeholder)", "NORMAL", ""),
	//IN_PREVIEW("In Preview", "INFO"),
	IN_REVIEW("In Review", "INFO", "START"),
	IN_REVIEW_REQUESTED("Pharmacy Review Requested", "INFO", "START"),
	//IN_BUDGET_DEVELOP("Under Budget Development", "INFO"),
	REJECTED("Rejected", "ERROR", "END"),
	APPROVED("Approved", "INFO", "END"),
	EXPEDITED_APPROVED("Expedited Approved", "INFO", "END"),
	EXEMPT_APPROVED("Exempt Approved", "INFO", "END"),
	COMPLETED("Completed", "INFO", "END"),
	ACKNOWLEDGED("Acknowledged", "INFO", "END"),
	PENDING_REVIEWER_ASSIGNMENT("Pending Reviewer Assignment", "WARN", "START"),
	REVIEWER_ASSIGNED("Reviewer Assigned", "INFO", "END"),
	REVISION_REQUESTED("Revision Requested", "WARN", "START"),
	PENDING_IRB_REVIEW_ASSIGNMENT("Pending IRB Review Assignment", "WARN", "START"),
	PENDING_IRB_REVIEW_RE_ASSIGNMENT("Pending IRB Review Re-assignment", "WARN", "START"),
	IRB_AGENDA_ASSIGNED("Assigned to an IRB Agenda", "INFO", "END"),
	IRB_AGENDA_APPROVED("IRB Agenda Approved", "INFO", "END"),
	EXPEDITED_IRB_REVIEW("Expedited IRB Review", "INFO", "START"),
	EXEMPT_IRB_REVIEW("Exempt IRB Review", "INFO", "START"),
	PENDING_EXPEDITED_IRB_REVIEW("Pending Expedited IRB Review", "WARN", "START"),
	PENDING_EXEMPT_IRB_REVIEW("Pending Exempt IRB Review", "WARN", "START"),
	NOT_APPLICABLE_TO_EXPEDITED_IRB_REVIEW("Not Applicable to Expedited IRB Review", "INFO", "END"),
	NOT_APPLICABLE_TO_EXEMPT_IRB_REVIEW("Not Applicable to Exempt IRB Review", "INFO", "END"),
	//RECEIVED("Received", "INFO"),
	NOTIFIED("Notified", "INFO", "START_END"),
	COMMITTEE_REVIEW_NOT_APPLICABLE("Committee Review Not Applicable", "INFO", "END"),
	REMOVED_FROM_IRB_AGENDA("Removed from the IRB Agenda", "WARN", "END"),
	IN_PTL_DEVELOP("Under PTL Development", "INFO", "START"),
	PENDING_PTL_DEVELOP("Pending PTL Development", "WARN", "END"),
	DECLINED("Declined", "ERROR", "END"),
	TABLED("Tabled", "INFO", "END"),
	EXEMPT_FROM_IND_IDE_REGULATIONS("Exempt from IND/IDE Reguations", "INFO", "END"),
	PENDING_IND_IDE_ASSIGNMENT("Pending IND/IDE Assignment", "WARN", "END"),
	POTENTIAL_NON_COMPLIANCE_IN_REVIEW("Potential Noncompliance Pending IRB Review Assignment", "WARN", "END"),
	PENDING_LEGAL_REVIEW("Pending Legal Review", "WARN", "END"),
	PENDING_SPONSOR_RESPONSE("Pending Sponsor Response", "WARN", "END"),
	DEFERRED("Deferred", "INFO", "END"),
	DEFERRED_WITH_MINOR_CONTINGENCIES("IRB Deferred With Minor Contingencies", "INFO", "END"),
	DEFERRED_WITH_MAJOR_CONTINGENCIES("IRB Deferred With Major Contingencies", "INFO", "END"),
	SUSPENDED_FOR_CAUSE("Suspended for Cause", "INFO", "END"),
	TERMINATED_FOR_CAUSE("Terminated for Cause", "INFO", "END"),
	PENDING_CONSENT_LEGAL_REVIEW("Pending Consent Legal Review", "WARN", "END"),
	IN_WAIVER_REQUESTED("Pharmacy Waiver Requested", "INFO", "START"),
	WAIVER_REQUEST_APPROVED("Pharamcy Waiver Request Approved", "INFO", "END"),
	PENDING_PHARMACY_REVIEW("Pending Pharmacy Review", "INFO", "END"),
	PENDING_ASSESSMENT("Pending Assessment", "INFO", "START"),
	PENDING_PI_RESPONSE("Pending PI Response", "INFO", "END"),
	APPROVE_WITH_CONTINGENCIES("Approved with Contingencies", "WARN", "END"),
	IN_LEGAL_REVIEW("In Legal Review", "INFO", "START"),
	IN_REVIEW_FOR_BUDGET("In Review", "INFO", "START"),
	IRB_REVIEW_NOT_NEEDED("IRB Review Not Needed", "INFO", "END"),
	PENDING_IRB_DETERMINATION("Pending IRB Determination", "INFO", "END"),
	REGULATORY_AFFAIRS_INVOLVEMENT_NOT_REQUIRED("Regulatory Affairs Involvement not required", "INFO", "END"),
	CONTRACT_REQUIRED("Contract is Required", "INFO", "END"),
	CONTRACT_NOT_REQUIRED("Contract is NOT Required", "INFO", "END"),
	BUDGET_NOT_REVIEWED("Budget Not Reviewed", "INFO", "END");
	
	private String description;
	private String priorityLevel;
	private String statusType;
	
	
	private ProtocolFormCommitteeStatusEnum(String description, String priorityLevel, String statusType){
		this.description = description;
		this.priorityLevel = priorityLevel;
		this.statusType = statusType;
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

	public String getStatusType() {
		return statusType;
	}

	public void setStatusType(String statusType) {
		this.statusType = statusType;
	}
}
