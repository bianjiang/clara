package edu.uams.clara.integration.outgoing.ctms.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.uams.clara.integration.common.domain.AbstractExternalDomainEntity;

@Entity
@Table(name = "clara_protocoldisease", schema="dbo", catalog="crissql.[ctms_integration]")
public class ClaraProtocolDisease extends AbstractExternalDomainEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = -232987286310271058L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", insertable = true, updatable = false, nullable = false, unique = true)
	private long id;
	
	@Column(name="clara_protocol_id")
	private long claraProtocolId;
	
	@Column(name="do_id")
	private String doID;
	
	@Column(name="description")
	private String description;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	protected long getInternalId() {
		return this.id;
	}

	@Override
	protected void setInternalId(long id) {
		this.id = id;		
	}

	public long getClaraProtocolId() {
		return claraProtocolId;
	}

	public void setClaraProtocolId(long claraProtocolId) {
		this.claraProtocolId = claraProtocolId;
	}

	public String getDoID() {
		return doID;
	}

	public void setDoID(String doID) {
		this.doID = doID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
