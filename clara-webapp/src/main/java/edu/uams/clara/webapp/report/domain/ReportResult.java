package edu.uams.clara.webapp.report.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;

@Entity
@Table(name = "report_result")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ReportResult extends AbstractDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6978458199004749409L;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@ManyToOne
	@JoinColumn(name="template_id")
	private ReportTemplate reportTemplate;
	
	@ManyToOne
	@JoinColumn(name="uploaded_file_id")
	private UploadedFile uploadedFile;

	public ReportTemplate getReportTemplate() {
		return reportTemplate;
	}

	public void setReportTemplate(ReportTemplate reportTemplate) {
		this.reportTemplate = reportTemplate;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
}
