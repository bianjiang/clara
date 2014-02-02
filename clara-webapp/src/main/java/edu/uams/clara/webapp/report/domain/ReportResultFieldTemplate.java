package edu.uams.clara.webapp.report.domain;

public class ReportResultFieldTemplate {
	
	
	private String fieldIdentifier;
	
	private String fieldDisplayName;
	
	private String defaultDisplay;
	
	private String order;
	
	private String alias;
	
	private String value;
	

	public String getFieldIdentifier() {
		return fieldIdentifier;
	}

	public void setFieldIdentifier(String fieldIdentifier) {
		this.fieldIdentifier = fieldIdentifier;
	}

	public String getFieldDisplayName() {
		return fieldDisplayName;
	}

	public void setFieldDisplayName(String fieldDisplayName) {
		this.fieldDisplayName = fieldDisplayName;
	}

	public String getDefaultDisplay() {
		return defaultDisplay;
	}

	public void setDefaultDisplay(String defaultDisplay) {
		this.defaultDisplay = defaultDisplay;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}




}
