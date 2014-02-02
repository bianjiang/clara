package edu.uams.clara.webapp.contract.domain.search;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import edu.uams.clara.webapp.common.domain.search.AbstractSearchBookmark;

@Entity
@DiscriminatorValue("CONTRACT")
@Cache(usage=CacheConcurrencyStrategy.READ_WRITE)
public class ContractSearchBookmark extends AbstractSearchBookmark {

	private static final long serialVersionUID = 918839634898357425L;

	public ContractSearchBookmark(){
		super();
	}
	
}
