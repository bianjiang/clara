package edu.uams.clara.webapp.protocol.domain.thing;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import edu.uams.clara.core.domain.thing.AbstractThing;


@Entity
@DiscriminatorValue("DEVICE")
public class Device extends AbstractThing {

	private static final long serialVersionUID = -406035006961139541L;

	public Device(){
		super();
	}

}
