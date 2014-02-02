package edu.uams.clara.webapp.common.businesslogic.form.validator.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationRule extends Rule{

	private static final long serialVersionUID = -7975611412093077794L;

	public ValidationRule(){

	}

	public ValidationRule(String valueKey){
		super(valueKey);
	}


	public ValidationRule(String valueKey, Class<?> dataType){
		super(valueKey, dataType);
	}

	private Map<String, Object> additionalData = null;

	private List<Rule> prerequisiteRules = null;

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

}
