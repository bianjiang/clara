package edu.uams.clara.webapp.common.businesslogic.form.validator;

import java.util.List;
import java.util.Map;

import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;

public interface ValidationRuleHandler {

	List<ValidationResponse> validate(List<Rule> validationRules, Map<String, List<String>> values);

}
