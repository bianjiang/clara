package edu.uams.clara.core.util.xml.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.XMLTemplateField;
import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;

/**
 * Attempt to rewrite the XmlProcessor 1) Better performance 2) Thread-safety;
 * Lightweight, so it can be created in separate threads 3) Unified and cleaner
 * interfaces 4) all public functions need to be exception free
 * 
 * @author bianjiang
 * 
 */
public class XmlHandlerImpl implements XmlHandler {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlHandlerImpl.class);

	// global lock
	private final static Object glock = new Object();

	private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			.newInstance();

	private static DocumentBuilder documentBuilder;
	static {
		try {
			// dbFactory isn't thread-safe
			synchronized (glock) {
				documentBuilder = dbFactory.newDocumentBuilder();
			}
		} catch (ParserConfigurationException e) {
			logger.error("error in creating DocumentBuilder", e);
		}
	}

	private static XPathFactory xpathFactory;
	static {
		try {
			synchronized (glock) {
				xpathFactory = XPathFactory
						.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI);
			}
		} catch (XPathFactoryConfigurationException e) {
			logger.error("error in creating XPathFactory instance", e);
		}
	}

	public XmlHandlerImpl() {

	}
	
	private synchronized Element createElementStructureByPath(final Element docRoot,
			String path) {

		Document doc = docRoot.getOwnerDocument();
		List<String> nodeList = getNodeList(path);

		logger.trace("nodeList: " + nodeList + " =? ");

		Assert.isTrue(nodeList.size() > 0);

		// remove first one, should be docRoot.getNodeName()...
		nodeList.remove(0);

		// remove last one
		nodeList.remove(nodeList.size() - 1);

		Element currentNode = docRoot;
		int c = 0;
		for (String n : nodeList) {
			NodeList cur = currentNode.getElementsByTagName(n);
			String curName = currentNode.getNodeName();
			c = cur.getLength();

			if (c > 1) {
				throw new RuntimeException("illeagl xml structure; find " + c
						+ " elements with name " + n);
			}

			if (c == 0) {
				logger.debug("empty node...; " + n + " doesn't exist under "
						+ curName);

				Element newN = doc.createElement(n);
				currentNode.appendChild(newN);

				currentNode = newN;
				continue;
			}

			currentNode = (Element) cur.item(0);

		}
		return currentNode;

	}
	
	private List<String> getNodeList(final String path) {
		List<String> nodeList = new ArrayList<String>(0);
		String[] p = path.split("/");
		for (String t : p) {
			if (StringUtils.hasText(t)) {
				nodeList.add(t);
			}
		}

		return nodeList;
	}

	@Override
	public Document newDocument() {
		synchronized (glock) {
			documentBuilder.reset();
			return documentBuilder.newDocument();
		}
	}

	@Override
	public Document newDocument(String rootTag) {
		synchronized (glock) {
			documentBuilder.reset();

			Document doc = documentBuilder.newDocument();

			if (rootTag != null && !rootTag.isEmpty()) {
				doc.appendChild(doc.createElement(rootTag));
			}
			return doc;
		}
	}

	@Override
	public XPath newXPathInstance() {
		try {
			synchronized (glock) {
				XPath xpathInstance = xpathFactory.newXPath();
				return xpathInstance;
			}

		} catch (Exception ex) {
			logger.error("failed to create xpathInstance", ex);
			throw ex;
		}
	}

	@Override
	public Document loadXmlFileToDOM(File xmlFile) throws IOException,
			SAXException {
		InputSource documentInputSource = new InputSource(
				new InputStreamReader(new FileInputStream(xmlFile)));
		return parse(documentInputSource);
	}

	private Document parse(final InputSource source) throws SAXException,
			IOException {
		// only one documentBuilder instance, so synchronized on glock.
		synchronized (glock) {
			documentBuilder.reset();
			return documentBuilder.parse(source);
		}
	}

	@Override
	public Document parse(final String inXmlString) throws SAXException,
			IOException {
		try {
			InputSource documentInputSource = new InputSource(new StringReader(
					inXmlString));
			return parse(documentInputSource);
		} catch (Exception ex) {
			logger.error("Exception when parsing: " + inXmlString, ex);
			throw ex;
		}
	}
	
	
	@Override
	public String replaceOrAddNodeValueByPath(final String path, final String xmlData,
			final String nodeValue) throws SAXException, IOException,
			XPathExpressionException {

		Document finalDom = parse(xmlData);

		XPath xPathInstance = newXPathInstance();
		NodeList nodeList = (NodeList) (xPathInstance.evaluate(path, finalDom,
				XPathConstants.NODESET));

		int l = nodeList.getLength();

		if (l == 0) {
			Element elementStructure = createElementStructureByPath(
					(Element) finalDom.getFirstChild(), path);

			List<String> nodeNameList = getNodeList(path);

			String lastNodeName = nodeNameList.get(nodeNameList.size() - 1);
			Element lastElement = finalDom.createElement(lastNodeName);
			lastElement.setTextContent(nodeValue);

			elementStructure.appendChild(lastElement);

		} else {
			Element currentElement = null;
			for (int i = 0; i < l; i++) {
				if (nodeList.item(i) == null)
					continue;
				currentElement = (Element) nodeList.item(i);

				currentElement.setTextContent(nodeValue);
			}
		}

		return DomUtils.elementToString(finalDom.getFirstChild());
	}

	@Override
	public Source replaceTemplateFields(final String templateXml,
			final List<XMLTemplateField> xmlTemplateFields)
			throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {
		Document templateDoc = parse(templateXml);
		return DomUtils.toSource(replaceTemplateFields(templateDoc,
				xmlTemplateFields));
	}

	@Override
	public Source replaceTemplateFields(final File xmlFile,
			final List<XMLTemplateField> xmlTemplateFields)
			throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {
		Document templateDoc = loadXmlFileToDOM(xmlFile);
		return DomUtils.toSource(replaceTemplateFields(templateDoc,
				xmlTemplateFields));
	}

	private Document replaceTemplateFields(Document templateDoc,
			final List<XMLTemplateField> xmlTemplateFields)
			throws SAXException, IOException, XPathExpressionException, ParserConfigurationException {

		XPath xPathInstance = newXPathInstance();
		
		XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
		
		for (XMLTemplateField xmlTemplateField : xmlTemplateFields) {
			xPathInstance.reset();

			NodeList nodeList = (NodeList) (xPathInstance.evaluate(
					xmlTemplateField.getNodeXPath(), templateDoc,
					XPathConstants.NODESET));

			int length = nodeList.getLength();

			for (int i = 0; i < length; i++) {
				Node node = nodeList.item(i);

				if (Node.ELEMENT_NODE != node.getNodeType()) {
					continue;
				}

				Element element = (Element) node;
				switch (xmlTemplateField.getFieldType()) {
				case ELEMENT_NODE:
					if(xmlTemplateField.isAppend()) {
						//if we do append, it needs to appended to the position pointed by the xpath, 
						//the value string needs to be converted Element object first
						//Element el = xmlHandler.parse(xmlTemplateField.getValue()).getDocumentElement();
						
						NodeList subNodeList = xmlHandler.parse(xmlTemplateField.getValue()).getDocumentElement().getChildNodes();
						
						for (int j = 0; j < subNodeList.getLength(); j++) {
							Element el = (Element) subNodeList.item(j);
							
							element.appendChild(templateDoc.importNode(el, true));
						}
						
						
					} else{ 
						element.setTextContent(xmlTemplateField.getValue());
					}
					break;
				case ATTRIBUTE:
					element.setAttribute(xmlTemplateField.getAttributeName(),
							xmlTemplateField.getValue());
					break;
				default:
					break;
				}

			}
		}

		return templateDoc;
	}

	/***
	 * Exception safe...
	 * 
	 * @param xml
	 * @param xPath
	 * @return
	 */
	@Override
	public String getSingleStringValueByXPath(final String xml,
			final String xPathExpression) {

		try {
			Document doc = parse(xml);
			
			Element rootElement = doc.getDocumentElement();

			return getSingleStringValueByXPath(rootElement, xPathExpression);

		} catch (Exception ex) {
			logger.error("getSingleStringValueByXPath() failed; wih: " + xml
					+ "; and " + xPathExpression, ex);
			return "";
		}

	}
	
	private static String getFirstLevelTextContent(Node node) {
	    NodeList list = node.getChildNodes();

	    StringBuilder textContent = new StringBuilder();
	    
	    if (list.getLength() > 0) {
	    	 for (int i = 0; i < list.getLength(); ++i) {
	 	        Node child = list.item(i);
	 	        if (child.getNodeType() == Node.TEXT_NODE)
	 	            textContent.append(child.getTextContent());
	 	    }
	    } else {
	    	return node.getTextContent();
	    }
	   
	    return textContent.toString();
	}

	private String getSingleStringValueByXPath(final Node parentNode,
			final String xPathExpression) throws XPathExpressionException {

		XPath xPathInstance = newXPathInstance();
		Node node = (Node) (xPathInstance.evaluate(xPathExpression, parentNode,
				XPathConstants.NODE));
		if (node == null) {
			return "";
		}

		return getFirstLevelTextContent(node);

	}

	@Override
	public List<Element> listElementsByXPath(final String xml, String elementPath){

		List<Element> results = Lists.newArrayList();
		try {
			Document doc = parse(xml);
				
			XPath xPathInstance = newXPathInstance();
			NodeList nodeList = (NodeList) (xPathInstance.evaluate(elementPath, doc,
						XPathConstants.NODESET));

			for (int i = 0; i< nodeList.getLength(); i++){

				try{
					Element element = (Element)nodeList.item(i);
					results.add(element);
				} catch (Exception ex) {
					logger.error("listElementsByXPath() failed", ex);
				}
			}

		} catch (Exception ex) {
			logger.error("listElementsByXPath() failed", ex);
		}


		return results;

	}
	

		
	/***
	 * Return the first value of the xpath starting at certain element identified by xpath
	 * Basically, it find the element at elementPath, and using xPathExpressions releative to the element Path
	 * This is useful, to find sub elements within a parent, e.g., /protocol/staffs/staff (and need to have retrieve the values for one staff).
	 * for retreiving a list of staff info see
	 * 
	 * @param xml
	 * @param xPathExpressions
	 * @return
	 */
	@Override
	public Map<String, String> getFirstStringValuesByXPathsAtNode(final String xml, String elementPath,
			Set<String> xPathExpressions) {
		Map<String, String> results = Maps.newHashMap();

		try {
			Document doc = parse(xml);
			
			XPath xPathInstance = newXPathInstance();
			Node node = (Node) (xPathInstance.evaluate(elementPath, doc,
					XPathConstants.NODE));
			
			// can't even find the element.... so just return...
			if(node == null){
				return results;
			}
			
			for (String xpath : xPathExpressions) {
				try{
					results.put(xpath, getSingleStringValueByXPath(node, xpath));
				}catch(XPathExpressionException e){
					logger.error("getFirstStringValuesByXPaths() xpath=" + xpath + "failed", e);
				}				
			}

		} catch (Exception ex) {
			logger.error("getSingleStringValuesByXPaths() failed; wih: " + xml);
		}

		return results;
	}

	@Override
	public List<Map<String, String>> getListOfMappedElementValues(final String xml, String elementPath,
			Set<String> xPathExpressions) {
		List<Map<String, String>> results = Lists.newArrayList();

		try {
			Document doc = parse(xml);
			
			XPath xPathInstance = newXPathInstance();
			NodeList nodeList = (NodeList) (xPathInstance.evaluate(elementPath, doc,
					XPathConstants.NODESET));
			
			for (int i = 0; i< nodeList.getLength(); i++){
				Node node = nodeList.item(i);
				Map<String, String> elementResults = Maps.newHashMap();
				for (String xpath : xPathExpressions) {
					try{
						elementResults.put(xpath, getSingleStringValueByXPath(node, xpath));
					}catch(XPathExpressionException e){
						logger.error("getFirstStringValuesByXPaths() xpath=" + xpath + "failed", e);
					}				
				}
				results.add(elementResults);
			}			

		} catch (Exception ex) {
			logger.error("getSingleStringValuesByXPaths() failed; wih: " + xml);
		}

		return results;
	}
	
	
	/***
	 * Return the first value of the xpath
	 * 
	 * @param xml
	 * @param xPathExpressions
	 * @return
	 */
	@Override
	public Map<String, String> getFirstStringValuesByXPaths(final String xml,
			Set<String> xPathExpressions) {
		Map<String, String> results = Maps.newHashMap();

		try {
			Document doc = parse(xml);
			
			for (String xpath : xPathExpressions) {
				try{
					results.put(xpath, getSingleStringValueByXPath(doc, xpath));
				}catch(XPathExpressionException e){
					logger.error("getFirstStringValuesByXPaths() xpath=" + xpath + "failed", e);
				}				
			}

		} catch (Exception ex) {
			logger.error("getSingleStringValuesByXPaths() failed; wih: " + xml);
		}

		return results;
	}
	
	/***
	 * Return the first value of the xpath
	 * 
	 * @param xml
	 * @param xPathExpressions
	 * @return
	 */
	@Override
	public Map<String, List<String>> getStringValuesByXPaths(final String xml,
			Set<String> xPathExpressions) {
		Map<String, List<String>> results = Maps.newHashMap();
		
		try {
			Document doc = parse(xml);
			
			for (String xpath : xPathExpressions) {
				try{
					results.put(xpath, getStringValuesByXPath(doc, xpath));
				}catch(XPathExpressionException e){
					logger.error("getStringValuesByXPaths() xpath=" + xpath + "failed", e);
				}
			}

		} catch (Exception ex) {
			logger.error("getStringValuesByXPaths() failed; wih: " + xml);
		}

		return results;
	}
	
	@Override
	public List<String> getStringValuesByXPath(final String xml,
			final String xPathExpression) {

		try {
			Document doc = parse(xml);
			
			return getStringValuesByXPath(doc, xPathExpression);

		} catch (Exception ex) {
			logger.error("getStringValuesByXPaths() failed; wih: " + xml);
			// return empty list
			return Lists.newArrayList();
		}

	}
	
	private List<String> getStringValuesByXPath(final Node parentNode,
			final String xPathExpression) throws XPathExpressionException {
		List<String> results = Lists.newArrayList();

		XPath xPathInstance = newXPathInstance();
		NodeList nodes = (NodeList) (xPathInstance.evaluate(xPathExpression,
				parentNode, XPathConstants.NODESET));

		if (nodes != null) {
			for (int i = 0; i < nodes.getLength(); i++) {
				results.add(nodes.item(i).getTextContent());
			}
		}

		return results;

	}
	
	@Override
	public String getAttributeValueByPathAndAttributeName(String path,
			String originalXml, String attributeName)
			throws XPathExpressionException, SAXException, IOException {
		Document document = parse(originalXml);
		
		XPath xPath = newXPathInstance();
		
		Element element = (Element) (xPath.evaluate(
				path, document,
				XPathConstants.NODE));
		
		String attributeValue = "";
		
		if (element != null){
			if (element.getAttribute(attributeName) != null){
				attributeValue = element.getAttribute(attributeName);
			}
		}
		
		return attributeValue;
	}
}
