package edu.uams.clara.integration.outgoing.ctms.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.uams.clara.integration.common.domain.AbstractExternalDomainEntity;

@Entity
@Table(name = "clara_funding", schema="dbo", catalog="crissql.[ctms_integration]")
public class ClaraFunding extends AbstractExternalDomainEntity{

	private static final long serialVersionUID = -6257140674752800583L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", insertable = true, updatable = false, nullable = false, unique = true)
	private long id;


	@Column(name="irbnumber")
	private long irbnumber;
	
	@Column(name="sponsor_name")
	private String sponsorName;

	@Column(name="type")
	private String type;
	
	@Column(name="external_id")
	private String externalID;

	@Override
	protected long getInternalId() {
		return this.id;
	}

	@Override
	protected void setInternalId(long id) {
		this.id = id;
		
	}


	public String getSponsorName() {
		return sponsorName;
	}

	public void setSponsorName(String sponsorName) {
		this.sponsorName = sponsorName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIrbnumber() {
		return irbnumber;
	}

	public void setIrbnumber(long irbnumber) {
		this.irbnumber = irbnumber;
	}

	public String getExternalID() {
		return externalID;
	}

	public void setExternalID(String externalID) {
		this.externalID = externalID;
	}


}
