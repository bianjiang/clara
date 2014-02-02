package edu.uams.clara.webapp.report.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Where;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@Entity
@Table(name = "report_template")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ReportTemplate extends AbstractDomainEntity {
	
	private static final long serialVersionUID = -7678286384762916065L;
	
	public enum Status{
		READY("Ready"),
		NOT_READY("Not Ready");
		
		private String description;
		
		private Status(String description){
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	public enum GlobalOperator{
		AND, OR;
	}
	
	public enum ScheduleType{
		NONE, DAILY, WEEKLY, MONTHLY;
	}
	
	@Column(name="type_description")
	private String typeDescription;
	
	@Column(name="description")
	private String description;
	
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	
	@Column(name="parameters", nullable=true)
	private String parameters;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name="global_operator")
	@Enumerated(EnumType.STRING)
	private GlobalOperator globalOperator;
	
	//@Column(name="cron_expression", nullable = true)
	//private String cronExpression;
	
	@Column(name="schedule_type", nullable = true)
	@Enumerated(EnumType.STRING)
	private ScheduleType scheduleType;
	
	@OneToMany(mappedBy="reportTemplate", fetch=FetchType.EAGER)
	@Where(clause="retired = 0")
	private List<ReportCriteria> reportCriterias;

	public String getTypeDescription() {
		return typeDescription;
	}

	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public GlobalOperator getGlobalOperator() {
		return globalOperator;
	}

	public void setGlobalOperator(GlobalOperator globalOperator) {
		this.globalOperator = globalOperator;
	}

	public List<ReportCriteria> getReportCriterias() {
		return reportCriterias;
	}

	public void setReportCriterias(List<ReportCriteria> reportCriterias) {
		this.reportCriterias = reportCriterias;
	}

	public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}
}
