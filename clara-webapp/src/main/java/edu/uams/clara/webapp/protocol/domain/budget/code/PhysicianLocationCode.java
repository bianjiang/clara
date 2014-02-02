package edu.uams.clara.webapp.protocol.domain.budget.code;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity

@DiscriminatorValue("PHYSICIAN_LOCATION_CODE")
public class PhysicianLocationCode extends AbstractLocationCode {

	private static final long serialVersionUID = -2754080954360146388L;

	public PhysicianLocationCode(){
		super();
	}
}
