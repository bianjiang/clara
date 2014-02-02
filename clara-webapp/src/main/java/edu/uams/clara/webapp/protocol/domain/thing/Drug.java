package edu.uams.clara.webapp.protocol.domain.thing;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import edu.uams.clara.core.domain.thing.AbstractThing;


@Entity
@DiscriminatorValue("DRUG")
public class Drug extends AbstractThing {

	private static final long serialVersionUID = -406035006961139540L;

	public Drug(){
		super();
	}

}
