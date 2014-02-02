package edu.uams.clara.webapp.common.domain.usercontext;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Permission;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;

@Entity
@Configurable(dependencyCheck=true)
@Table(name = "user_account")
@JsonIgnoreProperties({"authorities", "userRoles"})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class User extends AbstractDomainEntity implements Serializable, UserDetails {
	
	private static final long serialVersionUID = 6431944420000917961L;

	public enum UserType{
		LDAP_USER, DATABASE_USER;
	};
	
	@Column(name="user_type")
	@Enumerated(EnumType.STRING)
	private UserType userType;
	
	@Column(name="username")
	private String username;	
	
	@Column(name="password", length=32, nullable=true)
	private String password;
	
	@OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="person_id", nullable=true)
	private Person person;
	
	@Column(name="is_trained")
	private boolean trained = Boolean.FALSE;
	
	@Column(name="profile")
	@Type(type="edu.uams.clara.core.jpa.hibernate.usertype.SQLXMLUserType")
	private String profile;

	@OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
	private Set<UserRole> userRoles = new HashSet<UserRole>(0);
	
	@OneToOne
	@JoinColumn(name="cv_file_id")
	private UploadedFile uploadedFile;
	
	@Column(name="digital_signature_path")
	private String signaturePath;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(
			name="user_permission",
			joinColumns=@JoinColumn(name="user_id"))
	@Column(name="permission")
	@Enumerated(EnumType.STRING)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	private Set<Permission> userPermissions = new HashSet<Permission>(0); 

	//* wrap extra data into user to send to the client as json
	@Transient
	private Object data;
	
	@Transient
	public Object getData(){
		return this.data;
	}
	
	@Transient
	public void setData(Object data){
		this.data = data;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
	
	@Override
	@Transient
	public Collection<GrantedAuthority> getAuthorities() {		
		Collection<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>(0);
		for(UserRole ur:userRoles){
			//if(ur.isRetired()) continue;
			if (!ur.isRetired()){
				//add in role default rights
				grantedAuthorities.addAll(ur.getRole().getDefaultPermissions());
			}
			
		}
		
		if (this.getUserPermissions() != null){
			grantedAuthorities.addAll(this.getUserPermissions());
		}
		return grantedAuthorities;
	}
	
	@Transient
	public String getAlternateEmail() {
		String altEmail = ""; 
		
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			
			if (profile != null && !profile.isEmpty()) {
				altEmail = xmlHandler.getSingleStringValueByXPath(profile, "/metadata/alternate-email");
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			altEmail = "";
		}
		
		return altEmail;
	}
	
	@Transient
	public void addUserRole(Set<UserRole> userRoles) {	
		this.userRoles = userRoles;
	}

	@Column(name="account_nonexpired")
	private boolean accountNonExpired;
	
	@Column(name="account_nonlocked")
	private boolean accountNonLocked;
	
	@Column(name="credentials_nonexpired")
	private boolean credentialsNonExpired;
	
	@Column(name="enabled")
	private boolean enabled;
	
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public UserType getUserType() {
		return userType;
	}

	public void setUserRoles(Set<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public Set<UserRole> getUserRoles() {
		Set<UserRole> activeUserRoles = new HashSet<UserRole>();
		
		for (UserRole ur : userRoles){
			if (!ur.isRetired()){
				activeUserRoles.add(ur);
			}
		}
		return activeUserRoles;
	}

	public void setTrained(boolean trained) {
		this.trained = trained;
	}

	public boolean isTrained() {
		return trained;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public Set<Permission> getUserPermissions() {
		return userPermissions;
	}

	public void setUserPermissions(Set<Permission> userPermissions) {
		this.userPermissions = userPermissions;
	}

	public String getSignaturePath() {
		return signaturePath;
	}

	public void setSignaturePath(String signaturePath) {
		this.signaturePath = signaturePath;
	}

	@Override
	public int hashCode()
    {
		int hash = 17;
		hash = (int) (31 * hash + this.getId());
		hash = 31 * hash + User.class.getSimpleName().hashCode();
		return hash;
    }
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final User other = (User)obj;
	    
	    if(this.getId() != other.getId()){
	    	return false;
	    }
	    return true;
	}

}
