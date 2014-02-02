package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "location_code")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING, name="type")
public abstract class AbstractLocationCode extends AbstractDomainEntity implements
		Comparable<AbstractLocationCode> {

	private static final long serialVersionUID = 426525421892776800L;

	@Column(name="code")
	private String code;
	
	@Column(name="description", length = 8000)
	private String description;	

	@Override
	public int compareTo(AbstractLocationCode o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		if (o == null)
			return AFTER;
		if (this.getId() == o.getId())
			return EQUAL;
		if (this.getId() > o.getId())
			return AFTER;
		else
			return BEFORE;

	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash = (int) (31 * hash + this.getId());
		return hash;
	}

	@Override
	public boolean equals(Object aThat) {
		if (this == aThat)
			return true;
		if (!(aThat instanceof AbstractLocationCode))
			return false;

		AbstractLocationCode that = (AbstractLocationCode) aThat;
		return (this.getId() == that.getId());

	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
