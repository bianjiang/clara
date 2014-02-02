package edu.uams.clara.webapp.common.businesslogic.form.validator;

import java.util.Map;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;

public class ValidationResponse {

	public ValidationResponse(Constraint constraint, Map<String, Object> additionalData){
		this.constraint = constraint;
		this.additionalData = additionalData;
	}
	
	private Constraint constraint;
	
	private Map<String, Object> additionalData;

	public void setAdditionalData(Map<String, Object> additionalData) {
		this.additionalData = additionalData;
	}

	public Map<String, Object> getAdditionalData() {
		return additionalData;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public Constraint getConstraint() {
		return constraint;
	}

}
