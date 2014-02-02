package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint;

import java.util.List;
import java.util.Map;

public interface ConstraintHandler {

	boolean evaluateConstraint(Object value, Constraint constraint,
			Map<String, List<String>> values, Map<String, Object> cachedValues);

}
