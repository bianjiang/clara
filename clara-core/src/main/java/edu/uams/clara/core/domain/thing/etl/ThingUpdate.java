package edu.uams.clara.core.domain.thing.etl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING, name="type")
@Table(name="thing_update",schema="dbo")
public class ThingUpdate extends AbstractDomainEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9163944404700379474L;

	@Column(name="value",length = 5000)
	private String value;
	
	@Column(name="description",length = 5000)
	private String description;
	
	@Column(name="type")
	private String type;
	
	@Column(name="is_approved")
	private boolean isApproved;
	
	@Column(name="identifier", nullable=true)
	private String identifier;
	
	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setApproved(boolean isApproved) {
		this.isApproved = isApproved;
	}

	public boolean isApproved() {
		return isApproved;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
