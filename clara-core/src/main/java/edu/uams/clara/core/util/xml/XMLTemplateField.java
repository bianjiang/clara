package edu.uams.clara.core.util.xml;

public class XMLTemplateField {
	
	public enum FieldType {
		ELEMENT_NODE, ATTRIBUTE;
	}
		
	private String nodeXPath;
	
	private FieldType fieldType;
	
	private String valueIdentifier;
	
	private String value;

	private boolean append;
	
	private String attributeName;
	

	public boolean isAppend() {
		return append;
	}

	public void setAppend(boolean append) {
		this.append = append;
	}

	public String getNodeXPath() {
		return nodeXPath;
	}

	public void setNodeXPath(String nodeXPath) {
		this.nodeXPath = nodeXPath;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getValueIdentifier() {
		return valueIdentifier;
	}

	public void setValueIdentifier(String valueIdentifier) {
		this.valueIdentifier = valueIdentifier;
	}

	
}
