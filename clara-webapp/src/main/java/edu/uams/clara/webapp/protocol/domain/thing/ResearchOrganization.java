package edu.uams.clara.webapp.protocol.domain.thing;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import edu.uams.clara.core.domain.thing.AbstractThing;

/**
 * CRO (Clinical Research Organization) / SMO (Site Management Organization)
 * @author bianjiang
 *
 */
@Entity
@DiscriminatorValue("RESEARCH_ORGANIZATION")
public class ResearchOrganization extends AbstractThing{

	private static final long serialVersionUID = -8238289588485378568L;

	public ResearchOrganization(){
		super();
	}
}

