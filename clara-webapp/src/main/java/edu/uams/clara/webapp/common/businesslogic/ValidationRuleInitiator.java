package edu.uams.clara.webapp.common.businesslogic;

import java.util.List;

import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;

public interface ValidationRuleInitiator {
	List<Rule> initValidationRules();
}
