package edu.uams.clara.webapp.xml.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public interface XmlProcessor {

	public enum Operation{
		ADD, REPLACE, UPDATE_IF_EXIST, REPLACE_BY_XPATH_LIST;
	}
	
	/**
	 * based on our use case, it only check the children of the root node...
	 */
	String merge(final String originalXml, final String modifiedXml) throws SAXException, IOException;
	
	
	/**
	 * replace elements in originalXml with elements in modifiedXml according to xPathPairs
	 * if the elements exist in modifiedXml, but not in originalXml, it will add the element
	 * if the elements exist in originalXml, but not in modifiedXml (element value is null/empty, or the nodelist is null), it will remove the element in the originalXml 
	 * @param originalXml
	 * @param modifiedXml
	 * @param xmlMergeOperation
	 * @param xPathPairs
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	String mergeByXPaths(final String originalXml, final String modifiedXml, final Operation xmlMergeOperation, final Map<String, String> xPathPairs) throws SAXException, IOException, XPathExpressionException;

	
	/**
	 * the listPath uniquely identify a list section of the xml document, for example /protocol/drugs/drug means add (<code>elementXml</code>) a drug element
	 * into the drugs sections, and it should generate an id to uniquely identify the drug locally...
	 * <protocol>
	 * 	<drugs>
	 * 		<drug id=""></drug>
	 * 	</drugs>
	 * </protocol>
	 * if drugs doesn't exist, it will create drugs first and then add the drug in...
	 * @param listPath
	 * @param originalXml
	 * @param elementXml
	 * @param generateId if this is true, an id will be generated, otherwise, no...
	 * @return Map<String, String> which should have finalXml, elementXml with elementId in it, and elementId as keys...
	 * need to find a better way to return multiple values...
	 * @throws IOException
	 * @throws SAXException
	 */
	Map<String, Object> addElementByPath(String path, String originalXml, String elementXml, boolean generateId) throws SAXException, IOException;

	/**
	 *
	 * @param listPath
	 * @param originalXml
	 * @param elementId
	 * @return true if successful, otherwiest false...
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException 
	 */

	Map<String, Object> deleteElementByPathById(String path,
			String originalXml, String elementId) throws SAXException,
			IOException, XPathExpressionException;
	
	/**
	 *
	 * @param listPath
	 * @param originalXml
	 * @return true if successful, otherwiest false...
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException 
	 */

	Map<String, Object> deleteElementByPath(String path,
			String originalXml) throws SAXException,
			IOException, XPathExpressionException;

	/**
	 * doesn't seem like this should be here... but no better place now...
	 * @param listPath
	 * @param originalXml
	 * @param elementId
	 * @return
	 * @throws XPathExpressionException
	 * @throws IOException
	 * @throws SAXException
	 */
	String getElementByPathById(String path, String originalXml, String elementId) throws XPathExpressionException, SAXException, IOException;
	
	Map<String, Object> updateElementByPathById(String listPath,
			String originalXml, String elementId, String elementXml)
			throws SAXException, IOException;
	
	/**
	 * 
	 * @param path
	 * @param xmlData
	 * @param asList if true, the result will be wrapped inside a <list></list>
	 * @return
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	String listElementsByPath(String path, String originalXml, boolean asList)
			throws XPathExpressionException, SAXException, IOException;
	
	String getAttributeValueByPathAndAttributeName(String path, String originalXml, String attributeName)
			throws XPathExpressionException, SAXException, IOException;
	
	List<String> getAttributeValuesByPathAndAttributeName(String path, String originalXml, String attributeName)
			throws XPathExpressionException, SAXException, IOException;
	
	String listElementsByPaths(Set<String> paths, String originalXml, boolean inList)
	throws XPathExpressionException, SAXException, IOException;

	Map<String, Object> listElementValuesByPaths(Set<String> paths, Map<String, Class<?>> dataTypes, String xmlData)
			throws SAXException, IOException, XPathExpressionException;

	Map<String, List<String>> listElementStringValuesByPaths(Set<String> paths, String xmlData)
	throws SAXException, IOException, XPathExpressionException;

	List<String> listElementStringValuesByPath(String path, String xmlData)
	throws SAXException, IOException, XPathExpressionException;

	String addAttributesByPath(String path, String xmlData, Map<String, String> attributes) throws SAXException, IOException, XPathExpressionException;
	
	String newElementIdByPath(String path, String xmlData) throws SAXException,
	IOException, XPathExpressionException;
	
	String replaceAttributeValueByPathAndAttributeName(String path, String attributeName, String xmlData, String value) throws SAXException, IOException, XPathExpressionException;
	
	String deleteAttributeByPathAndAttributeName(String path, String attributeName, String xmlData) throws SAXException, IOException, XPathExpressionException;
	
	String loadXmlFile(File xmlFile) throws FileNotFoundException, IOException;
	
	//String replaceNodeValueByPath(String path, String xmlData, String value) throws SAXException, IOException, XPathExpressionException;

	String replaceOrAddNodeValueByPath(String path, String xmlData, String value) throws SAXException, IOException, XPathExpressionException;

	Map<String, Object> addSubElementToElementIdentifiedByXPath(String xPath,
			String originalXml, String elementXml, boolean generateId)
			throws SAXException, IOException, XPathExpressionException;

	String loadXmlFile(String xmlFilePath) throws IOException;

	String listElementsByPath(String path, String originalXml, boolean inList,
			boolean includeChildren) throws XPathExpressionException,
			SAXException, IOException;

	String listElementsByPaths(Set<String> paths, String xmlData,
			boolean inList, boolean includeChildren) throws SAXException,
			IOException, XPathExpressionException;

	List<Element> listDomElementsByPaths(Set<String> paths, String xmlData)
			throws SAXException, IOException, XPathExpressionException;

	String replaceRootTagWith(final String xmlData, final String rootTag) throws SAXException, IOException;

	Document loadXmlFileToDOM(final File xmlFile) throws IOException, SAXException;

	Document loadXmlFileToDOM(final String xmlFilePath) throws IOException,
			SAXException;

	Document loadXmlStringToDOM(final String xmlData) throws IOException,
			SAXException;

	XPath getXPathInstance();

	String escapeText(final String inXml) throws SAXException, IOException;
	
	Document newDocument();

	//output the current jaxp implementation
	void OutputJaxpImplementationInfo();


	//Document parse(InputSource in) throws SAXException, IOException;


	DocumentBuilder getDocumentBuilder() throws ParserConfigurationException;


	Document parse(String inXmlString) throws SAXException, IOException;

	List<String> listElementDomStringsByPaths(Set<String> paths,
			String xmlData, boolean includeChildren)
			throws SAXException, IOException, XPathExpressionException;
}
