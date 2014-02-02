package edu.uams.clara.webapp.common.businesslogic.form.validator.constraint;

/**
 * some of the constraints might depends a user value which can't be resolved until runtime
 * @author jbian
 *
 */
public class UserValue {

	private String valueKey;

	public UserValue(String valueKey){
		this.valueKey = valueKey;
	}

	public void setValueKey(String valueKey) {
		this.valueKey = valueKey;
	}

	public String getValueKey() {
		return valueKey;
	}


}
