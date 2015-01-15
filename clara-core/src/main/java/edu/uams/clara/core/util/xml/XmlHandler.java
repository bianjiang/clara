package edu.uams.clara.core.util.xml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public interface XmlHandler {

	Document parse(final String inXmlString) throws SAXException, IOException;

	Document loadXmlFileToDOM(final File xmlFile) throws IOException, SAXException;

	XPath newXPathInstance();

	Source replaceTemplateFields(final String templateXml,
			List<XMLTemplateField> xmlTemplateFields) throws SAXException,
			IOException, XPathExpressionException, ParserConfigurationException;
	
	Source replaceTemplateFields(final File xmlFile,
			List<XMLTemplateField> xmlTemplateFields) throws SAXException,
			IOException, XPathExpressionException, ParserConfigurationException;


	String getSingleStringValueByXPath(String xml, String xPath);

	Map<String, String> getFirstStringValuesByXPaths(String xml,
			Set<String> xPathExpressions);

	Map<String, List<String>> getStringValuesByXPaths(String xml,
			Set<String> xPathExpressions);

	List<String> getStringValuesByXPath(String xml, String xPathExpression);

	List<Map<String, String>> getListOfMappedElementValues(String xml,
			String elementPath, Set<String> xPathExpressions);

	Map<String, String> getFirstStringValuesByXPathsAtNode(String xml,
			String elementPath, Set<String> xPathExpressions);

	List<Element> listElementsByXPath(final String xml, String elementPath);

	Document newDocument(String rootTag);

	Document newDocument();

	String replaceOrAddNodeValueByPath(String path, String xmlData, String value) throws SAXException, IOException, XPathExpressionException;
	
	String getAttributeValueByPathAndAttributeName(String path,
			final String originalXml, String attributeName)
			throws XPathExpressionException, SAXException, IOException;
}
