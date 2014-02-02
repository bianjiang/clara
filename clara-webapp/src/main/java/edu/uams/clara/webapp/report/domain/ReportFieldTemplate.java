package edu.uams.clara.webapp.report.domain;

public class ReportFieldTemplate {
	
	public enum Operator {
		EQUALS("="), CONTAINS(""), BEFORE("<"), AFTER(">"),BETWEEN(">");
		
		private String realOperator;
		
		private Operator(String realOperator) {
			this.realOperator = realOperator;
		}

		public String getRealOperator() {
			return realOperator;
		}

		public void setRealOperator(String realOperator) {
			this.realOperator = realOperator;
		}
	}
		
	private String nodeXPath;
	
	private String reportableXPath;
	
	private String fieldXType;
	
	private String fieldIdentifier;
	
	private String fieldDisplayName;
	
	private String value;
	
	private String allowedOperators;
	
	private String displayValue;
	
	private Operator operator;
	
	private Object dataSourceObject;
	
	public String getNodeXPath() {
		return nodeXPath;
	}

	public void setNodeXPath(String nodeXPath) {
		this.nodeXPath = nodeXPath;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFieldIdentifier() {
		return fieldIdentifier;
	}

	public void setFieldIdentifier(String fieldIdentifier) {
		this.fieldIdentifier = fieldIdentifier;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public String getFieldDisplayName() {
		return fieldDisplayName;
	}

	public void setFieldDisplayName(String fieldDisplayName) {
		this.fieldDisplayName = fieldDisplayName;
	}

	public String getFieldXType() {
		return fieldXType;
	}

	public void setFieldXType(String fieldXType) {
		this.fieldXType = fieldXType;
	}

	public Object getDataSourceObject() {
		return dataSourceObject;
	}

	public void setDataSourceObject(Object dataSourceObject) {
		this.dataSourceObject = dataSourceObject;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public String getAllowedOperators() {
		return allowedOperators;
	}

	public void setAllowedOperators(String allowedOperators) {
		this.allowedOperators = allowedOperators;
	}

	public String getReportableXPath() {
		return reportableXPath;
	}

	public void setReportableXPath(String reportableXPath) {
		this.reportableXPath = reportableXPath;
	}
}
