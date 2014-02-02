package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "category_code")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class CategoryCode extends AbstractDomainEntity {

	private static final long serialVersionUID = -7599608041743943984L;

	@Column(name="code", length=4)
	private String code;
	
	@Column(name="description")
	private String description;

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
