package edu.uams.clara.integration.outgoing.ctms.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.uams.clara.integration.common.domain.AbstractExternalDomainEntity;

@Entity
@Table(name = "clara_protocol", schema="dbo", catalog="crissql.[ctms_integration]")
public class ClaraProtocol extends AbstractExternalDomainEntity {

	private static final long serialVersionUID = -89964205004820806L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", insertable = true, updatable = false, nullable = false, unique = true)
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@Override
	protected long getInternalId() {
		// TODO Auto-generated method stub
		return this.id;
	}

	@Override
	protected void setInternalId(long id) {
		this.id = id;		
	}
	
	@Column(name="irb_number")
	private String irbNumber;
	
	@Column(name="title")
	private String title;
	
	@Column(name="clara_created_date")
	private String claraCreatedDate;
	
	@Column(name="budget_approval_date")
	private String budgetApprovalDate;
	
	@Column(name="irb_submitted_date")
	private String irbSubmittedDate;
	
	@Column(name="irb_approval_date")
	private String irbApprovalDate;
	
	@Column(name="last_cr_date")
	private String lastCRApprovalDate;
	
	@Column(name="next_cr_date")
	private String nextCRApprovalDate;
	
	@Column(name="lay_summary")
	private String laySummary;
	
	@Column(name="inclusion_criteria")
	private String inclusionCriteria;
	
	@Column(name="exclusion_criteria")
	private String exclusionCriteria;
	
	@Column(name="phases")
	private String phases;
	
	@Column(name="local_accrual_gaol")
	private String localAccuralGaol;
	
	@Column(name="irb_status")
	private String irbStatus;
	
	@Column(name="budget_review_assigned")
	private Boolean budgetReviewAssigned;
	
	@Column(name="study_type")
	private String studyType;
	
	@Column(name="cancer_related")
	private String cancerRelated;
	
	
	public static class Builder extends AbstractExternalDomainEntity.Builder<ClaraProtocol>{

		public Builder() throws InstantiationException, IllegalAccessException {
			super();
		}
		
		public Builder(long id) throws InstantiationException, IllegalAccessException{
			super(id);
		}
		
	}
	

	public String getIrbApprovalDate() {
		return irbApprovalDate;
	}

	public void setIrbApprovalDate(String irbApprovalDate) {
		this.irbApprovalDate = irbApprovalDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPhases() {
		return phases;
	}

	public void setPhases(String phases) {
		this.phases = phases;
	}

	public String getLocalAccuralGaol() {
		return localAccuralGaol;
	}

	public void setLocalAccuralGaol(String localAccuralGaol) {
		this.localAccuralGaol = localAccuralGaol;
	}

	public String getIrbStatus() {
		return irbStatus;
	}

	public void setIrbStatus(String irbStatus) {
		this.irbStatus = irbStatus;
	}

	public String getIrbNumber() {
		return irbNumber;
	}

	public void setIrbNumber(String irbNumber) {
		this.irbNumber = irbNumber;
	}

	public String getClaraCreatedDate() {
		return claraCreatedDate;
	}

	public void setClaraCreatedDate(String claraCreatedDate) {
		this.claraCreatedDate = claraCreatedDate;
	}

	public String getBudgetApprovalDate() {
		return budgetApprovalDate;
	}

	public void setBudgetApprovalDate(String budgetApprovalDate) {
		this.budgetApprovalDate = budgetApprovalDate;
	}

	public String getIrbSubmittedDate() {
		return irbSubmittedDate;
	}

	public void setIrbSubmittedDate(String irbSubmittedDate) {
		this.irbSubmittedDate = irbSubmittedDate;
	}

	public String getLastCRApprovalDate() {
		return lastCRApprovalDate;
	}

	public void setLastCRApprovalDate(String lastCRApprovalDate) {
		this.lastCRApprovalDate = lastCRApprovalDate;
	}

	public String getNextCRApprovalDate() {
		return nextCRApprovalDate;
	}

	public void setNextCRApprovalDate(String nextCRApprovalDate) {
		this.nextCRApprovalDate = nextCRApprovalDate;
	}

	public String getLaySummary() {
		return laySummary;
	}

	public void setLaySummary(String laySummary) {
		this.laySummary = laySummary;
	}

	public Boolean getBudgetReviewAssigned() {
		return budgetReviewAssigned;
	}

	public void setBudgetReviewAssigned(Boolean budgetReviewAssigned) {
		this.budgetReviewAssigned = budgetReviewAssigned;
	}

	public String getInclusionCriteria() {
		return inclusionCriteria;
	}

	public void setInclusionCriteria(String inclusionCriteria) {
		this.inclusionCriteria = inclusionCriteria;
	}

	public String getExclusionCriteria() {
		return exclusionCriteria;
	}

	public void setExclusionCriteria(String exclusionCriteria) {
		this.exclusionCriteria = exclusionCriteria;
	}

	public String getStudyType() {
		return studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}

	public String getCancerRelated() {
		return cancerRelated;
	}

	public void setCancerRelated(String cancerRelated) {
		this.cancerRelated = cancerRelated;
	}

	

}
