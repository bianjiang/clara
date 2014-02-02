package edu.uams.clara.core.domain;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;


@MappedSuperclass
@Cacheable(true)
public abstract class AbstractDomainEntity implements Serializable {

	private static final long serialVersionUID = 7418769162300520148L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(insertable = true, updatable = false, nullable = false, unique = true)
    private long id;

    @Version
    @Column(name="concurrent_version")
    private int concurrentVersion;

    @Column(name="retired", nullable=false)
    private boolean retired;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
    
	public void setRetired(boolean retired) {
		this.retired = retired;
	}

	public boolean isRetired() {
		return retired;
	}
	
	/**
	 * Version number OPTLOCK is automatically set by hibernate, shouldn't allow set by other parts of the application
	 * http://docs.jboss.org/hibernate/orm/4.1/devguide/en-US/html/ch05.html
	 * @param concurrentVersion
	 */
	private void setConcurrentVersion(int concurrentVersion) {
		this.concurrentVersion = concurrentVersion;
	}

	public int getConcurrentVersion() {
		return concurrentVersion;
	}
	
	@Override
	public int hashCode()
    {
		int hash = 17;
		hash = (int) (31 * hash + this.id);
		hash = 31 * hash + this.getClass().getSimpleName().hashCode();
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
	    final AbstractDomainEntity other = (AbstractDomainEntity)obj;
	    
	    if(this.getId() != other.getId()){
	    	return false;
	    }
	    return true;
	}
    
}
