package edu.uams.clara.core.util.xml;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.XMLTemplateField.FieldType;

public class XMLTemplateFieldFactory {
	
	public static XMLTemplateField newNodeTemplate(String valueIdentifier, String nodeXPath){
		
		return newNodeTemplate(valueIdentifier, nodeXPath, false);
	}

	public static XMLTemplateField newNodeTemplate(String valueIdentifier, String nodeXPath, boolean append){
		XMLTemplateField field = new XMLTemplateField();
		field.setValueIdentifier(valueIdentifier);
		field.setFieldType(FieldType.ELEMENT_NODE);
		field.setAppend(append);
		field.setNodeXPath(nodeXPath);
		field.setValue("");
		field.setAttributeName(null);
		return field;
	}
	
	public static XMLTemplateField newAttirbuteTemplate(String valueIdentifier, String nodeXPath, String name){
		XMLTemplateField field = new XMLTemplateField();
		field.setValueIdentifier(valueIdentifier);
		field.setFieldType(FieldType.ATTRIBUTE);
		field.setNodeXPath(nodeXPath);
		field.setAttributeName(name);
		field.setValue("");
		return field;
	}
	
	public static List<XMLTemplateField> setValues(List<XMLTemplateField> templateFields, Map<String, String> values){
		
		List<XMLTemplateField> newFields = Lists.newArrayList();
		for(XMLTemplateField templateField:templateFields){
			XMLTemplateField newField = new XMLTemplateField();
			newField.setValueIdentifier(templateField.getValueIdentifier());
			newField.setFieldType(templateField.getFieldType());
			newField.setNodeXPath(templateField.getNodeXPath());
			newField.setAttributeName(templateField.getAttributeName());
			newField.setAppend(templateField.isAppend());
			if(values.containsKey(templateField.getValueIdentifier())){
				newField.setValue(values.get(templateField.getValueIdentifier()));
			}else{
				newField.setValue(templateField.getValueIdentifier());
			}
			newFields.add(newField);
		}
		
		return newFields;
	}

	
}
