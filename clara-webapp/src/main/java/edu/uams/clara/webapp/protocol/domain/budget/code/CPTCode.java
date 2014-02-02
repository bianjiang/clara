package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.core.domain.AbstractDomainEntity;

@Entity
@Table(name = "cpt_code")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class CPTCode extends AbstractDomainEntity {

	private static final long serialVersionUID = 1871604313539366061L;
	
	@Column(name="code")
	private String code;
	
	@Column(name="short_description")
	private String shortDescription;
	
	@Column(name="medium_description")
	private String mediumDescription;
	
	@Column(name="long_description", length = 8000)
	private String longDescription;
	
	@ManyToOne
	@JoinColumn(name="category_code_id")
	private CategoryCode categoryCode;
	
	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setMediumDescription(String mediumDescription) {
		this.mediumDescription = mediumDescription;
	}

	public String getMediumDescription() {
		return mediumDescription;
	}

	public void setCategoryCode(CategoryCode categoryCode) {
		this.categoryCode = categoryCode;
	}

	public CategoryCode getCategoryCode() {
		return categoryCode;
	}

	
	

}
