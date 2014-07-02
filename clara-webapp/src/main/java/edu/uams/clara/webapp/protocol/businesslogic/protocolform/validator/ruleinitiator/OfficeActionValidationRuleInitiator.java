package edu.uams.clara.webapp.protocol.businesslogic.protocolform.validator.ruleinitiator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;

import edu.uams.clara.webapp.common.businesslogic.ValidationRuleInitiator;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;
import edu.uams.clara.webapp.common.businesslogic.form.validator.util.ValidationXmlParser;

public class OfficeActionValidationRuleInitiator implements ValidationRuleInitiator {
private ValidationXmlParser validationXmlParser;
	
	private ResourceLoader resourceLoader;
	
	@Value("${validationXmlPath}")
	private String validationXmlPath;

	public List<Rule> initValidationRules(){		
		
		List<Rule> protocolValidationRules = null;
		String fullPath = validationXmlPath + "/" + "officeActionValidation.xml";
		
		Resource  validationXmlFileResource = resourceLoader.getResource(fullPath);
		try{
			
			Document validationDoc = validationXmlParser.loadToDOM(validationXmlFileResource.getFile());
			protocolValidationRules = validationXmlParser.getRules(validationDoc);
		} catch (Exception e){
			e.printStackTrace();
		}

		return protocolValidationRules;
	}


	public ValidationXmlParser getValidationXmlParser() {
		return validationXmlParser;
	}

	@Autowired(required=true)
	public void setValidationXmlParser(ValidationXmlParser validationXmlParser) {
		this.validationXmlParser = validationXmlParser;
	}
	
	@Autowired(required=true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
}
