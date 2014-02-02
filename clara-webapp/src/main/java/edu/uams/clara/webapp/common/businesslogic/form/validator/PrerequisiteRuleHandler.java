package edu.uams.clara.webapp.common.businesslogic.form.validator;

import java.util.List;
import java.util.Map;

import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;


public interface PrerequisiteRuleHandler {

	/**
	 * validate all the prerequisite rules, the raw values are stored in values HashMap, while the cachedValues is holding the type safe values
	 * this cachedValues can be shared between PrerequisiteRuleHandler and the ValidationRuleHandler
	 * there is one trap point that technically you can define the same value as a different type, for example,
	 * you can have list of rules for the same valueKey, lets say /protocol/title, but you give it a different dataType class
	 * In this situation, whichever gets resolved first will be used, unless we turn the cache off explicitly...
	 * However, this situation should be avoid, since you datatype should be consistent and unique for each valueKey
	 * @param prerequisiteRules
	 * @param values
	 * @param cachedValues
	 * @return
	 */
	boolean validate(List<Rule> prerequisiteRules,
			Map<String, List<String>> values, Map<String, Object> cachedValues);

	boolean validate(List<Rule> prerequisiteRules,
			Map<String, List<String>> values);

}
