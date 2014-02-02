package edu.uams.clara.webapp.common.businesslogic.form.validator;

import java.util.List;
import java.util.Set;

import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;

public interface ValidationRuleContainer {

	List<Rule> getValidationRules(String key);

	void addValidationRule(String key, Rule validationRule);

	void addValidationRules(String key, List<Rule> validationRules);

	Set<String> getCachedValueKeys(String key);

}
