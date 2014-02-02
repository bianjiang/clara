package edu.uams.clara.webapp.protocol.domain.thing;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import edu.uams.clara.core.domain.thing.AbstractThing;


@Entity
@DiscriminatorValue("TOXIN")
public class Toxin extends AbstractThing {

	

	private static final long serialVersionUID = -1602840326741939820L;

	public Toxin(){
		super();
	}

}
