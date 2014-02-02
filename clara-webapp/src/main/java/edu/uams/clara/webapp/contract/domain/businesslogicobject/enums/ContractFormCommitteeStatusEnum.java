package edu.uams.clara.webapp.contract.domain.businesslogicobject.enums;


public enum ContractFormCommitteeStatusEnum{
	ANY("Any Status (placeholder)", "NORMAL"),
	LIMBO("Limbo Status (Program error occurred)", "ERROR"),
	TO_BE_DETERMINED("TO BE DETERMINED (placeholder)", "NORMAL"),
	IN_PREVIEW("In Preview", "INFO"),
	IN_REVIEW("In Review", "INFO"),
	IN_BUDGET_DEVELOP("Under Budget Development", "INFO"),
	REJECTED("Rejected", "ERROR"),
	APPROVED("Approved", "INFO"),
	EXPEDITED_APPROVED("Expedited Approved", "INFO"),
	EXEMPT_APPROVED("Exempt Approved", "INFO"),
	COMPLETED("Completed", "INFO"),
	ACKNOWLEDGED("Acknowledged", "INFO"),
	PENDING_REVIEWER_ASSIGNMENT("Pending Reviewer Assignment", "WARN"),
	REVIEWER_ASSIGNED("Reviewer Assigned", "INFO"),
	REVISION_REQUESTED("Revision Requested", "WARN"),
	IRB_AGENDA_ASSIGNED("Assigned to an IRB Agenda", "INFO"),
	EXPEDITED_IRB_REVIEW("Expedited IRB Review", "INFO"),
	EXEMPT_IRB_REVIEW("Exempt IRB Review", "INFO"),
	PENDING_EXPEDITED_IRB_REVIEW("Pending Expedited IRB Review", "WARN"),
	PENDING_EXEMPT_IRB_REVIEW("Pending Exempt IRB Review", "WARN"),
	NOT_APPLICABLE_TO_EXPEDITED_IRB_REVIEW("Not Applicable to Expedited IRB Review", "INFO"),
	NOT_APPLICABLE_TO_EXEMPT_IRB_REVIEW("Not Applicable to Exempt IRB Review", "INFO"),
	RECEIVED("Received", "INFO"),
	NOTIFIED("Notified", "INFO"),
	COMMITTEE_REVIEW_NOT_APPLICABLE("Committee Review Not Applicable", "INFO"),
	REMOVED_FROM_IRB_AGENDA("Removed from the IRB Agenda", "WARN"),
	IN_PTL_DEVELOP("Under PTL Development", "INFO"),
	PENDING_PTL_DEVELOP("Pending PTL Development", "WARN"),
	DECLINED("Declined", "ERROR"),
	TABLED("Tabled", "INFO"),
	EXEMPT_FROM_IND_IDE_REGULATIONS("Exempt from IND/IDE Reguations", "INFO"),
	PENDING_IND_IDE_ASSIGNMENT("Pending IND/IDE Assignment", "WARN"),
	POTENTIAL_NON_COMPLIANCE_IN_REVIEW("Potential Non-compliance In Review", "WARN"),
	PENDING_LEGAL_REVIEW("Pending Legal Review", "WARN"),
	PENDING_SPONSOR_RESPONSE("Pending Sponsor Response", "WARN"),
	DEFERRED("Deferred", "INFO"),
	PENDING_SIGNATURE("Pending Signature", "WARN"),
	CONTRACT_EXECUTED_PENDING_DOCUMENTS("Contract Executed Pending Documents", "WARN"),
	NOT_APPLICABLE("Not Applicable", "INFO"),
	FINAL_LEGAL_APPROVAL("Final Legal Approval", "INFO"),
	PENDING_PI("Pending PI", "WARN"),
	PENDING_BUDGET("Pending Budget", "WARN"),
	PENDING_COVERAGE("Pending Coverage", "WARN"),
	PENDING_IRB("Pending IRB", "WARN"),
	OUTSIDE_IRB("Outside IRB", "WARN"),
	CANCELLED("Cancelled", "INFO");
	
	private String description;
	private String priorityLevel;
	
	private ContractFormCommitteeStatusEnum(String description, String priorityLevel){
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
