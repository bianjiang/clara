package edu.uams.clara.webapp.common.domain.usercontext.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum Committee {
	PI("Principal Investigator", Permission.ROLE_PI, "pi", false), //PI is not a real committee, it's a place holder for anyone who is on the protocol's staff list.. we might need more security rules on this.
	GATEKEEPER("Gatekeeper", Permission.ROLE_GATEKEEPER, "gatekeeper", true),
	IRB_PREREVIEW("IRB Prereview", Permission.ROLE_IRB_PREREVIEW, "irb-prereview", true),
	IRB_OFFICE("IRB Office", Permission.ROLE_IRB_OFFICE, "irb-office", true),
	IRB_CHAIR("IRB Chair", Permission.ROLE_IRB_CHAIR, "irb-chair", false),
	BUDGET_MANAGER("Budget Manager", Permission.ROLE_BUDGET_MANAGER, "budget-manager", true),
	BUDGET_REVIEW("Budget Review", Permission.ROLE_BUDGET_REVIEWER, "budget-review", true),
	BUDGET_DEVELOP("Budget Develop", Permission.ROLE_BUDGET_DEVELOPER, "budget-develop", true),
	COVERAGE_REVIEW("Coverage Review", Permission.ROLE_COVERAGE_REVIEWER, "coverage-review", true),
	COVERAGE_MANAGER("Coverage Manager", Permission.ROLE_COVERAGE_MANAGER, "coverage-manager", true),
	SUB_DEPARTMENT_CHIEF("Sub-Department Chief", Permission.ROLE_SUB_DEPARTMENT_CHIEF, "sub-department-chief", true),
	DEPARTMENT_CHAIR("Department Chair", Permission.ROLE_DEPARTMENT_CHAIR, "department-chair", true),
	COLLEGE_DEAN("Dean of College", Permission.ROLE_COLLEGE_DEAN, "college-dean", true),
	IRB_REVIEWER("IRB Reviewer", Permission.ROLE_IRB_REVIEWER, "irb-reviewer", true),
	PHARMACY_REVIEW("Pharmacy Review", Permission.ROLE_PHARMACY_REVIEW, "pharmacy-review", true),
	IRB_CONSENT_REVIEWER("IRB Office Consent Reviewer", Permission.ROLE_IRB_CONSENT_REVIEWER, "irb-consent-reviewer", true),
	IRB_PROTOCOL_REVIEWER("IRB Office Protocol Reviewer", Permission.ROLE_IRB_PROTOCOL_REVIEWER, "irb-protocol-reviewer", true),
	IRB_EXPEDITED_REVIEWER("IRB Expedited Reviewer", Permission.ROLE_IRB_EXPEDITED_REVIEWER, "irb-expedited-reviewer", true),
	IRB_EXEMPT_REVIEWER("IRB Exempt Reviewer", Permission.ROLE_IRB_EXEMPT_REVIEWER, "irb-exempt-reviewer", true),
	HOSPITAL_SERVICES("Hospital Services Reviewer", Permission.ROLE_HOSPITAL_SERVICES_REVIEWER, "hospital-services-reviewer", true),
	//COLLEGE_DEPT_SUB_DEPT("College/Dept/Sub Dept Reviewer", "college-dept-sub-dept-reviewer", true),
	CONTRACT_LEGAL_REVIEW("Contract Legal Reviewer", Permission.ROLE_CONTRACT_LEGAL_REVIEW, "contract-legal-reviewer", true),
	PROTOCOL_LEGAL_REVIEW("Protocol Legal Reviewer", Permission.ROLE_PROTOCOL_LEGAL_REVIEW, "protocol-legal-reviewer", true),
	BIOSAFETY("Biosafety Reviewer", Permission.ROLE_BIOSAFETY_REVIEWER, "biosafety-reviewer", true),
	PRMC("PRMC Reviewer", Permission.ROLE_PRMC_REVIEWER, "prmc-reviewer", true),
	RADIATION_SAFETY("Radiation Safety Reviewer", Permission.ROLE_RADIATION_SAFETY_REVIEWER,  "raditation-safety-reviewer", true),
	COI("COI Reviewer", Permission.ROLE_COI_REVIEWER, "coi-reviewer", true),
	MONITORING_REGULATORY_QA("Monitoring/Regulatory/QA reviewer", Permission.ROLE_MONITORING_REGULATORY_QA_REVIEWER, "monitoring-regulatory-qa-reviewer", true),
	ACHRI("ACHRI Reviewer", Permission.ROLE_ACHRI_REVIEWER, "achri-reviewer", true),
	PTL("Project Team Leader", Permission.ROLE_PTL, "ptl", true),
	REGULATORY_MANAGER("Regulatory Manager", Permission.ROLE_REGULATORY_MANAGER, "regulatory-manager", true),
	//LEGAL_REVIEW("Legal Review (Contract)", Permission.ROLE_LEGAL_REVIEWER, "legal-review", true),
	CONTRACT_MANAGER("Contract Manager", Permission.ROLE_CONTRACT_MANAGER, "contract-manager", true),
	CONTRACT_ADMIN("Contract Admin", Permission.ROLE_CONTRACT_ADMIN, "contract-admin", true),
	COMPLIANCE_REVIEW("Hospital Compliance", Permission.ROLE_COMPLIANCE_REVIEW, "compliance-review", true),
	RESEARCH_COMPLIANCE("Research Compliance", Permission.ROLE_RESEARCH_COMPLIANCE, "research-compliance", true),
	CLINICAL_TRIALS_REVIEW("ClinicalTrials.gov", Permission.ROLE_CLINICAL_TRIALS_REVIEW, "clinical-trials-review", true),
	IRB_ASSIGNER("IRB Assigner", Permission.ROLE_IRB_ASSIGNER, "irb-assigner", true),
	IRB_MEETING_OPERATOR("IRB Meeting Operator", Permission.ROLE_IRB_MEETING_OPERATOR, "irb-meeting-operator", true),
	ACH_PHARMACY_REVIEWER("ACH Pharmacy Reviewer", Permission.ROLE_ACH_PHARMACY_REVIEWER, "ach-pharmacy-reviewer", true),
	CCTO("Cancer Clinical Trials Office", Permission.ROLE_CCTO, "ccto", false),
	PBS("Patient Business Services", Permission.ROLE_PBS, "pbs", false),
	BEACON_TEAM("Beacon Team", Permission.ROLE_BEACON_TEAM, "beacon-team", false),
	WILLOW_TEAM("Willow Team", Permission.ROLE_WILLOW_TEAM, "willow-team", false);
	
	private String description;
	
	private String tagName;
	
	private Permission rolePermissionIdentifier;
	
	private boolean assignable;
	
	private Committee(String description, Permission rolePermissionIdentifier, String tagName, boolean assignable){
		this.rolePermissionIdentifier = rolePermissionIdentifier;
		this.description = description;
		this.tagName = tagName;
		this.assignable = assignable;
	}
	
	private static final Map<Permission, Committee> lookUpByPermission = new HashMap<Permission, Committee>();
	
	static {
		for (Committee c : EnumSet.allOf(Committee.class)){
			lookUpByPermission.put(c.getRolePermissionIdentifier(), c);
		}
	}
	
	public static Committee getCommitteeByRolePermissionIdentifier(Permission rolePermissionIdentifier){
		return lookUpByPermission.get(rolePermissionIdentifier);
	}

	public String getDescription() {
		return description;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getTagName() {
		return tagName;
	}

	public void setAssignable(boolean assignable) {
		this.assignable = assignable;
	}

	public boolean isAssignable() {
		return assignable;
	}

	public Permission getRolePermissionIdentifier() {
		return rolePermissionIdentifier;
	}

	public void setRolePermissionIdentifier(Permission rolePermissionIdentifier) {
		this.rolePermissionIdentifier = rolePermissionIdentifier;
	}
}
