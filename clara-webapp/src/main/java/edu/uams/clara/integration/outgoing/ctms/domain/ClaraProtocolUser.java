package edu.uams.clara.integration.outgoing.ctms.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.uams.clara.integration.common.domain.AbstractExternalDomainEntity;

@Entity
@Table(name = "clara_protocoluser", schema="dbo", catalog="crissql.[ctms_integration]")
public class ClaraProtocolUser extends AbstractExternalDomainEntity {

	private static final long serialVersionUID = -8037561787537449228L;
	
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
	
	@Column(name="clara_user_id")
	private long claraUserId;
	
	@Column(name="clara_protocol_id")
	private long claraProtocolId;
	
	@Column(name="clara_protocoluser_role")
	private String claraProtocolUserRole;
	
	public long getClaraUserId() {
		return claraUserId;
	}

	public void setClaraUserId(long claraUserId) {
		this.claraUserId = claraUserId;
	}

	public long getClaraProtocolId() {
		return claraProtocolId;
	}

	public void setClaraProtocolId(long claraProtocolId) {
		this.claraProtocolId = claraProtocolId;
	}

	public String getClaraProtocolUserRole() {
		return claraProtocolUserRole;
	}

	public void setClaraProtocolUserRole(String claraProtocolUserRole) {
		this.claraProtocolUserRole = claraProtocolUserRole;
	}


}
