package edu.uams.clara.webapp.report.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "report_criteria")
@JsonIgnoreProperties({"reportTemplate", "criteria"})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ReportCriteria extends AbstractDomainEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1942785194662102898L;
	
	@ManyToOne
	@JoinColumn(name="report_id")
	private ReportTemplate reportTemplate;
	
	@Column(name="criteria")
	private String criteria;

	public ReportTemplate getReportTemplate() {
		return reportTemplate;
	}

	public void setReportTemplate(ReportTemplate reportTemplate) {
		this.reportTemplate = reportTemplate;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	@Transient
	@JsonProperty("reportCriteria")
	public ReportFieldTemplate getCriteriaObject(){
		ObjectMapper objectMapper = new ObjectMapper();
		ReportFieldTemplate reportFieldTemplate = null;
		try {
			if (this.getCriteria() != null && !this.getCriteria().isEmpty()) {
				reportFieldTemplate = objectMapper.readValue(this.getCriteria(), ReportFieldTemplate.class);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportFieldTemplate;
	}

}
