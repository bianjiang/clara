package edu.uams.clara.webapp.common.businesslogic.form.validator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.businesslogic.form.validator.PrerequisiteRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationResponse;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.ConstraintHandler;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.util.ValueObjectHelper;

public class ValidationRuleHandlerImpl implements ValidationRuleHandler {

	private final static Logger logger = LoggerFactory
			.getLogger(ValidationRuleHandler.class);

	public ValidationRuleHandlerImpl() {

	}

	private ConstraintHandler constraintHandler;

	private PrerequisiteRuleHandler prerequisiteRuleHandler;

	//private final Map<String, Object> cachedValues = new HashMap<String, Object>(
	//		0);

	@Override
	public List<ValidationResponse> validate(
			List<Rule> validationRules,
			Map<String, List<String>> values) {

		Assert.notNull(validationRules);

		List<ValidationResponse> validationResponses = new ArrayList<ValidationResponse>();

		String valueKey = null;
		Object value = null;
		//cachedValues.clear();
		Map<String, Object> cachedValues = Maps.newHashMap();
		for (Rule vr : validationRules) {

			valueKey = vr.getValueKey();

			logger.trace("validating ValidationRule on: " + valueKey);

			value = ValueObjectHelper.getTypeSafeValueObject(valueKey, vr.getValueType(), values, cachedValues);

			logger.trace("size of cachedValues: " + cachedValues.size());
			for(Entry<String, Object> entry:cachedValues.entrySet()){
				logger.trace(entry.getKey() + ":"  + entry.getValue().toString());
			}

			List<Rule> prerequisiteRules = vr.getPrerequisiteRules();

			//this validation rule has prerequisites
			if(prerequisiteRules != null && !prerequisiteRules.isEmpty()){

				logger.trace("this rule has prerequisites...");
				if(!prerequisiteRuleHandler.validate(prerequisiteRules, values, cachedValues)){
					logger.trace("the prerequisites for this rule [ " + valueKey + "] does not meet");
					continue;
				}
			}

			//the constraints of each rule should be ordered, the most important one should be appeared first
			for (Constraint c : vr.getConstraints()) {
				if (!constraintHandler.evaluateConstraint(value, c, values, cachedValues)) {
					validationResponses.add(new ValidationResponse(c, vr.getAdditionalData()));

					//the following constraints won't even get evaluated if a previous one failed.
					break;
				}
			}

		}
		return validationResponses;
	}

	@Autowired(required = true)
	public void setConstraintHandler(ConstraintHandler constraintHandler) {
		this.constraintHandler = constraintHandler;
	}

	public ConstraintHandler getConstraintHandler() {
		return constraintHandler;
	}


	@Autowired(required = true)
	public void setPrerequisiteRuleHandler(PrerequisiteRuleHandler prerequisiteRuleHandler) {
		this.prerequisiteRuleHandler = prerequisiteRuleHandler;
	}



	public PrerequisiteRuleHandler getPrerequisiteRuleHandler() {
		return prerequisiteRuleHandler;
	}

}
