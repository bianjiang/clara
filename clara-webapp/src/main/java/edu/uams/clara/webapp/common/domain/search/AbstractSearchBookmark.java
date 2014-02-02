package edu.uams.clara.webapp.common.domain.search;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.domain.usercontext.User;

@Entity
@Table(name = "search_bookmark")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType=DiscriminatorType.STRING, name="object_class")
@JsonIgnoreProperties({"user"})
public abstract class AbstractSearchBookmark extends AbstractDomainEntity {

	private static final long serialVersionUID = -8478749940495788645L;
	
	@Column(name="name")
	private String name;
	
	@Column(name="object_class", insertable=false, updatable=false)
	private String objectClass;
	
	//@JSON encoded string
	@Column(name="search_criterias", length=8000)
	private String searchCriterias;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User user;
	
	
	@JsonProperty("userId")
	public long getUserId() {
		if (this.user == null) {
			return 0;
		}
		return this.user.getId();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSearchCriterias() {
		return searchCriterias;
	}

	public void setSearchCriterias(String searchCriterias) {
		this.searchCriterias = searchCriterias;
	}

	public String getObjectClass() {
		return objectClass;
	}

	public void setObjectClass(String objectClass) {
		this.objectClass = objectClass;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	

}
