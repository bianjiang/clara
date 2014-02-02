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
@Table(name = "report_field")
@JsonIgnoreProperties({"reportTemplate", "field"})
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ReportField extends AbstractDomainEntity{
	
	private static final long serialVersionUID = -6949156278532258108L;

	@ManyToOne
	@JoinColumn(name="report_id")
	private ReportTemplate reportTemplate;
	
	@Column(name="field")
	private String field;
	
	@Transient
	@JsonProperty("reportField")
	public ReportResultFieldTemplate getFieldObject(){
		ObjectMapper objectMapper = new ObjectMapper();
		ReportResultFieldTemplate reportResultFieldTemplate = null;
		try {
			if (this.getField() != null && !this.getField().isEmpty()) {
				reportResultFieldTemplate = objectMapper.readValue(this.getField(), ReportResultFieldTemplate.class);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reportResultFieldTemplate;
	}

	public ReportTemplate getReportTemplate() {
		return reportTemplate;
	}

	public void setReportTemplate(ReportTemplate reportTemplate) {
		this.reportTemplate = reportTemplate;
	}

	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

}
