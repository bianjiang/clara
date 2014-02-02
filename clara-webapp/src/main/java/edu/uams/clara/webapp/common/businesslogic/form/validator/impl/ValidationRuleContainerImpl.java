package edu.uams.clara.webapp.common.businesslogic.form.validator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import edu.uams.clara.webapp.common.businesslogic.ValidationRuleInitiator;
import edu.uams.clara.webapp.common.businesslogic.form.validator.ValidationRuleContainer;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.UserValue;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.ValidationRule;

public class ValidationRuleContainerImpl implements ValidationRuleContainer {
	private final static Logger logger = LoggerFactory
			.getLogger(ValidationRuleContainer.class);
	
	@PostConstruct
	public void init(){
		if(valudationRuleInitiators != null){
			for(Entry<String, ValidationRuleInitiator> entry:valudationRuleInitiators.entrySet()){
				this.addValidationRules(entry.getKey(), entry.getValue().initValidationRules());
			}
		}
	}

	private Map<String, ValidationRuleInitiator> valudationRuleInitiators;

	private Map<String, List<Rule>> ruleContainer = new HashMap<String, List<Rule>>(0);

	private Map<String, Set<String>> cachedValueKeys = new HashMap<String, Set<String>>(0);

	private void addCachedValueKey(String key, String valueKey){
		Assert.hasText(valueKey);

		Set<String> valueKeys = cachedValueKeys.get(key);

		if(valueKeys == null){
			valueKeys = new HashSet<String>(0);
			cachedValueKeys.put(key, valueKeys);
		}

		valueKeys.add(valueKey);
	}
	@Override
	public List<Rule> getValidationRules(String key){
		return ruleContainer.get(key);
	}

	@Override
	public void addValidationRules(String key, List<Rule> validationRules){
		Assert.notNull(validationRules);

		List<Rule> rules = ruleContainer.get(key);
		if(rules == null){
			rules = new ArrayList<Rule>(0);
			ruleContainer.put(key, rules);
		}

		for(Rule vr:validationRules){
			addValidationRule(key, vr);

		}

	}

	@Override
	public void addValidationRule(String key, Rule validationRule){
		List<Rule> rules = ruleContainer.get(key);
		if(rules == null){
			rules = new ArrayList<Rule>(0);
			ruleContainer.put(key, rules);
		}

		addCachedValueKey(key, validationRule);		

		rules.add(validationRule);
	}
	
	private void addCachedValueKey(String key, Rule rule){
		addCachedValueKey(key, rule.getValueKey());
		List<Constraint> constraints = rule.getConstraints();
		if(constraints != null && !constraints.isEmpty()){
			for(Constraint c:constraints){
				if(c.getParams() != null){
					for(Entry<String, Object> paramEntry:c.getParams().entrySet()){
						if(paramEntry.getValue() instanceof UserValue){
							addCachedValueKey(key, ((UserValue)paramEntry.getValue()).getValueKey());
						}
					}
				}
			}
		}

		if(rule.getPrerequisiteRules() != null && !rule.getPrerequisiteRules().isEmpty()){
			for(Rule pr:rule.getPrerequisiteRules()){
				addCachedValueKey(key, pr);
			}
		}
		/*
		if(rule instanceof ValidationRule){
			ValidationRule validationRule = (ValidationRule)rule;
			if(validationRule.getPrerequisiteRules() != null && !validationRule.getPrerequisiteRules().isEmpty()){
				for(Rule pr:validationRule.getPrerequisiteRules()){
					addCachedValueKey(key, pr);
				}
			}
		}*/
	}

	@Override
	public Set<String> getCachedValueKeys(String key) {
		return cachedValueKeys.get(key);
	}
	public void setValudationRuleInitiators(Map<String, ValidationRuleInitiator> valudationRuleInitiators) {
		this.valudationRuleInitiators = valudationRuleInitiators;
	}
	public Map<String, ValidationRuleInitiator> getValudationRuleInitiators() {
		return valudationRuleInitiators;
	}
	

}
