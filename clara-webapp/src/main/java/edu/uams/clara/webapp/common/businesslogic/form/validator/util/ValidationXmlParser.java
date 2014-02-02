package edu.uams.clara.webapp.common.businesslogic.form.validator.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.Constraint;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintLevel;
import edu.uams.clara.webapp.common.businesslogic.form.validator.constraint.enums.ConstraintType;
import edu.uams.clara.webapp.common.businesslogic.form.validator.rule.Rule;

public class ValidationXmlParser {
	private final static Logger logger = LoggerFactory
			.getLogger(ValidationXmlParser.class);
	
	private DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			.newInstance();
	private DocumentBuilder dBuilder = null;

	public ValidationXmlParser() throws ParserConfigurationException {
		dBuilder = dbFactory.newDocumentBuilder();
	}

	public synchronized Document loadToDOM(File xmlFile) throws IOException,
			SAXException {

		return dBuilder.parse(xmlFile);
	}

	private String getConstraintParamKey(ConstraintType constraintType){
		String constraintParamKey = null;
		switch (constraintType){
			case CONTAINS:
				constraintParamKey = ConstraintType.Contains.ParamKeys.VALUE.toString();
				break;
			case NOTCONTAINSMULTIPLE:
				constraintParamKey = ConstraintType.NotContainsMultiple.ParamKeys.VALUE.toString();
				break;
			case DATE:
				constraintParamKey = ConstraintType.Date.ParamKeys.DATA_FORMAT.toString();
				break;
			case EQUAL:
				constraintParamKey = ConstraintType.Equal.ParamKeys.VALUE.toString();
				break;
			case NOTEQUAL:
				constraintParamKey = ConstraintType.NotEqual.ParamKeys.VALUE.toString();
				break;
			case MEMBEROF:
				constraintParamKey = ConstraintType.MemberOf.ParamKeys.VALUES.toString();
				break;
			case NOINTERSECT:
				constraintParamKey = ConstraintType.NonIntersect.ParamKeys.VALUES.toString();
				break;
			case INTERSECT:
				constraintParamKey = ConstraintType.Intersect.ParamKeys.VALUES.toString();
				break;
			case UNIQUE:
				constraintParamKey = ConstraintType.Unique.ParamKeys.VALUE.toString();
				break;
			case CONTAINSNA:
				constraintParamKey = ConstraintType.ContainsNA.ParamKeys.VALUE.toString();
				break;
			case NOTCONTAINS:
				constraintParamKey = ConstraintType.NotContains.ParamKeys.VALUE.toString();
				break;
			default:
				break;
		}
		return constraintParamKey;
		
	}
	
	public List<Rule> getRules(Document validationDoc) throws DOMException,
			ClassNotFoundException {

		List<Rule> rules = new ArrayList<Rule>();
		//List<Rule> prerequisiteRules = new ArrayList<Rule>();

		Element currentRuleElement = null;
		Element currentContraintOrPreElement = null;
		Element currentPrerequisiteRuleElement = null;
		Element currentPrerequisiteRuleConstrainElement = null;
		
		NodeList ruleList = validationDoc.getDocumentElement().getChildNodes();
		int rl = ruleList.getLength();

		for (int i = 0; i < rl; i++) {
			Rule rule = null;
			
			if (ruleList.item(i).getNodeType() == Node.ELEMENT_NODE){
				currentRuleElement = (Element) ruleList.item(i);

				if (currentRuleElement.getAttribute("value-type").isEmpty()){
					rule = new Rule(currentRuleElement.getAttribute("path"));
				}else{
					rule = new Rule(currentRuleElement.getAttribute("path"),
							Class.forName(currentRuleElement.getAttribute("value-type")));
				}

				//rule = new Rule(currentRuleElement.getAttribute("path"), String.class);
				rule.addAdditionalData("pagename", currentRuleElement.getAttribute("pagename"));
				rule.addAdditionalData("pageref", currentRuleElement.getAttribute("pageref"));

				NodeList constraintList = currentRuleElement.getChildNodes();

				int cpl = constraintList.getLength();

				for (int j = 0; j < cpl; j++) {				
					if (constraintList.item(j).getNodeType() == Node.ELEMENT_NODE){
						currentContraintOrPreElement = (Element) constraintList.item(j);

						if (currentContraintOrPreElement.getTagName().equals(
								"constraint")) {
							Constraint constraint = new Constraint(
									ConstraintType.valueOf(currentContraintOrPreElement
											.getAttribute("type")),
									ConstraintLevel
											.valueOf(currentContraintOrPreElement
													.getAttribute("level")),
									currentContraintOrPreElement
											.getAttribute("error-msg"));
							String constraintParamKey = getConstraintParamKey(ConstraintType.valueOf(currentContraintOrPreElement
									.getAttribute("type")));
							constraint.addParam(constraintParamKey, currentContraintOrPreElement.getAttribute("data"));
							rule.addConstraint(constraint);
						} else if (currentContraintOrPreElement.getTagName().equals(
								"prerequisites")) {
							NodeList prerequisiteRuleList = currentContraintOrPreElement
									.getChildNodes();
							int prl = prerequisiteRuleList.getLength();

							for (int k = 0; k < prl; k++) {
								Rule prerequisiteRule = null;
								
								if (prerequisiteRuleList.item(k).getNodeType() == Node.ELEMENT_NODE){
									currentPrerequisiteRuleElement = (Element) prerequisiteRuleList
											.item(k);
									
									if (currentPrerequisiteRuleElement.getAttribute("value-type").isEmpty()){
										prerequisiteRule = new Rule(currentPrerequisiteRuleElement.getAttribute("path"));
									}else{
										prerequisiteRule = new Rule(currentPrerequisiteRuleElement.getAttribute("path"),
												Class.forName(currentPrerequisiteRuleElement.getAttribute("value-type")));
									}

									//prerequisiteRule = new Rule(
											//currentPrerequisiteRuleElement
													//.getAttribute("path"), String.class);

									NodeList prerequisiteRuleConstraintList = currentPrerequisiteRuleElement
											.getChildNodes();
									int prcl = prerequisiteRuleConstraintList.getLength();

									for (int p = 0; p < prcl; p++) {
										if (prerequisiteRuleConstraintList.item(p).getNodeType() == Node.ELEMENT_NODE){
											currentPrerequisiteRuleConstrainElement = (Element) prerequisiteRuleConstraintList
													.item(p);

											Constraint prerequisiteRuleConstrain = new Constraint(
													ConstraintType.valueOf(currentPrerequisiteRuleConstrainElement
															.getAttribute("type")),
													ConstraintLevel
															.valueOf(currentPrerequisiteRuleConstrainElement
																	.getAttribute("level")),
													currentPrerequisiteRuleConstrainElement
															.getAttribute("error-msg"));
											String prerequisiteRuleConstraintParamKey = getConstraintParamKey(ConstraintType.valueOf(currentPrerequisiteRuleConstrainElement
															.getAttribute("type")));
											prerequisiteRuleConstrain.addParam(prerequisiteRuleConstraintParamKey, currentPrerequisiteRuleConstrainElement.getAttribute("data"));
											prerequisiteRule
													.addConstraint(prerequisiteRuleConstrain);
											
											rule.addPrerequisiteRule(prerequisiteRule);
										}
									}
								}
							}
						}
					}
					
				}

				rules.add(rule);
			}
				
		}

		return rules; 

	}
}
