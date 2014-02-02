package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintLevel;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintType;

@JsonIgnoreProperties({"params"})
public class Constraint implements Serializable  {

	private static final long serialVersionUID = -5061387870626187750L;

	public Constraint(){

	}

	public Constraint(ConstraintType constraintType){
		this.constraintType = constraintType;
	}

	public Constraint(ConstraintType constraintType, ConstraintLevel constraintLevel, String errorMessage){
		this.constraintLevel = constraintLevel;
		this.constraintType = constraintType;
		this.errorMessage = errorMessage;
	}

	private ConstraintType constraintType;

	private ConstraintLevel constraintLevel;

	private String errorMessage;

	private Map<String, Object> params;

	private Map<String, Object> results;

	public void addResult(String key, Object value){
		if(results == null){
			results = new HashMap<String, Object>(0);
		}
		results.put(key, value);
	}


	public void addParam(String key, Object value){
		if(params == null){
			params = new HashMap<String, Object>(0);
		}
		params.put(key, value);
	}

	public void finalizeErrorMessage(){
		String resultMessage = getErrorMessage();
		for(Entry<String, Object> entry:results.entrySet()){
			resultMessage = resultMessage.replaceAll("\\$\\{" + entry.getKey() + "\\}", entry.getValue() != null?entry.getValue().toString():"REPLACEMENT VALUE NOT FOUND");
		}

		this.errorMessage = resultMessage;
	}

	public void setConstraintType(ConstraintType constraintType) {
		this.constraintType = constraintType;
	}

	public ConstraintType getConstraintType() {
		return constraintType;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(constraintType.toString() + ": [");

		for(Entry<String, Object> entry:params.entrySet()){
			sb.append("{" + entry.getKey() + "=" + entry.getValue().toString() + "}");
		}

		sb.append("]");

		return sb.toString();
	}


	public void setConstraintLevel(ConstraintLevel constraintLevel) {
		this.constraintLevel = constraintLevel;
	}


	public ConstraintLevel getConstraintLevel() {
		return constraintLevel;
	}


	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public String getErrorMessage() {
		return errorMessage;
	}

	public void setResults(Map<String, Object> results) {
		this.results = results;
	}

	public Map<String, Object> getResults() {
		return results;
	}

}
