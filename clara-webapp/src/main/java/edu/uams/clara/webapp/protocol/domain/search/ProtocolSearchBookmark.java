package edu.uams.clara.webapp.protocol.domain.search;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.webapp.common.domain.search.AbstractSearchBookmark;

@Entity
@DiscriminatorValue("PROTOCOL")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ProtocolSearchBookmark extends AbstractSearchBookmark {

	private static final long serialVersionUID = 918839634898357395L;

	public ProtocolSearchBookmark(){
		super();
	}
	
}
