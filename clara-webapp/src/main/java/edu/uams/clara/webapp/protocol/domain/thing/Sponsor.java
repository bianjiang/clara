package edu.uams.clara.webapp.protocol.domain.thing;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import edu.uams.clara.core.domain.thing.AbstractThing;

@Entity
@DiscriminatorValue("SPONSOR")
public class Sponsor extends AbstractThing {

	private static final long serialVersionUID = -4996069715363709381L;
	
	public Sponsor(){
		super();
	}
}
