package edu.uams.clara.webapp.common.businesslogic.form.validator.rule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;

public class Rule implements Serializable {

	private static final long serialVersionUID = 5141811952965216594L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(Rule.class);

	public Rule(){

	}

	public Rule(String valueKey){
		this.valueKey = valueKey;
	}

	public Rule(String valueKey, Class<?> valueType){
		this.valueKey = valueKey;
		this.valueType = valueType;
	}

	private String valueKey;

	private Class<?> valueType;

	private List<Constraint> constraints;
	
	private Map<String, Object> additionalData = null;

	private List<Rule> prerequisiteRules = null;

	public void addConstraint(Constraint constraint){
		if(constraints == null){
			constraints = new ArrayList<Constraint>(0);
		}
		constraints.add(constraint);
	}
	
	public void addPrerequisiteRule(Rule prerequisiteRule){
		if(prerequisiteRules == null){
			prerequisiteRules = new ArrayList<Rule>(0);
		}
		prerequisiteRules.add(prerequisiteRule);
	}

	public void addAdditionalData(String key, Object value){
		if(additionalData == null){
			additionalData = new HashMap<String, Object>(0);
		}
		additionalData.put(key, value);
	}


	public Object getadditionalData(String key){
		if(additionalData == null) return null;
		return additionalData.get(key);
	}
	

	public void setPrerequisiteRules(List<Rule> prerequisiteRules) {
		this.prerequisiteRules = prerequisiteRules;
	}

	public List<Rule> getPrerequisiteRules() {
		return prerequisiteRules;
	}

	public void setAdditionalData(Map<String, Object> additionalData) {
		this.additionalData = additionalData;
	}

	public Map<String, Object> getAdditionalData() {
		return additionalData;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public void setValueKey(String valueKey) {
		this.valueKey = valueKey;
	}

	public String getValueKey() {
		return valueKey;
	}

	public void setValueType(Class<?> valueType) {
		this.valueType = valueType;
	}

	public Class<?> getValueType() {
		return valueType;
	}

}
