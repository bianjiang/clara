package edu.uams.clara.webapp.common.businesslogic.form.validator.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.businesslogic.form.validator.PrerequisiteRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.ConstraintHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.util.ValueObjectHelper;

public class PrerequisiteRuleHandlerImpl implements PrerequisiteRuleHandler{

	private final static Logger logger = LoggerFactory
	.getLogger(PrerequisiteRuleHandler.class);

	private ConstraintHandler constraintHandler;

	@Override
	public boolean validate(List<Rule> prerequisiteRules,
			Map<String, List<String>> values) {

		return validate(prerequisiteRules, values, null);
	}


	@Override
	public boolean validate(List<Rule> prerequisiteRules,
			Map<String, List<String>> values, Map<String, Object> cachedValues) {

		String valueKey = null;
		Object value = null;

		for (Rule pr : prerequisiteRules) {

			valueKey = pr.getValueKey();

			//logger.debug("validating PrerequisiteRule on: " + valueKey);

			value = ValueObjectHelper.getTypeSafeValueObject(valueKey, pr.getValueType(), values, cachedValues);


			for (Constraint c : pr.getConstraints()) {
				if (!constraintHandler.evaluateConstraint(value, c, values, cachedValues)) {
					//logger.debug("coonstraint " + c.getConstraintType() + " doesn't meet! return false!");
					return false;
				}
			}

		}


		return true;
	}


	@Autowired(required=true)
	public void setConstraintHandler(ConstraintHandler constraintHandler) {
		this.constraintHandler = constraintHandler;
	}


	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}

}
