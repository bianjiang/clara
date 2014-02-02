package edu.uams.clara.webapp.xml.processor.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.webapp.xml.processor.DuplicateChildElementObject;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;
import edu.uams.clara.webapp.xml.processor.exception.XmlProcessorOperationNotYetImplementedException;

/**
 * current implementation has a dependency on
 * org.springframework.util.xml.DomUtils... doing DOM for all xml
 * manipulation... eventually probably want to move all these too xquery
 * update... but Saxon-EE is the only one supports it, and it cost 300.00 per
 * license...
 * this thing might have performance issues as we just did synchronized on everything to save the headache...
 * 
 * @author jbian
 * 
 */
public class DefaultXmlProcessorImpl implements XmlProcessor {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlProcessor.class);

	private final DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			.newInstance();
	private final DocumentBuilder documentBuilder;
	
	private final XPathFactory xpathFactory = XPathFactory.newInstance();
	
	public DefaultXmlProcessorImpl() throws ParserConfigurationException {
		documentBuilder = dbFactory.newDocumentBuilder();
	}
	
	private static String getJaxpImplementationInfo(String componentName, Class componentClass) {
	    CodeSource source = componentClass.getProtectionDomain().getCodeSource();
	    return MessageFormat.format(
	            "{0} implementation: {1} loaded from: {2}",
	            componentName,
	            componentClass.getName(),
	            source == null ? "Java Runtime" : source.getLocation());
	}
	
	@Override
	public void OutputJaxpImplementationInfo(){
		 logger.debug(getJaxpImplementationInfo("DocumentBuilderFactory", DocumentBuilderFactory.newInstance().getClass()));
		 logger.debug(getJaxpImplementationInfo("XPathFactory", XPathFactory.newInstance().getClass()));
		 logger.debug(getJaxpImplementationInfo("TransformerFactory", TransformerFactory.newInstance().getClass()));
		 logger.debug(getJaxpImplementationInfo("SAXParserFactory", SAXParserFactory.newInstance().getClass()));

	}

	private ResourceLoader resourceLoader;

	@Override
	public synchronized XPath getXPathInstance() {
		// XPathFactory is not thread-safe
		return xpathFactory.newXPath();
	}

	@Override
	public String merge(final String originalXml, final String modifiedXml) throws SAXException, IOException {

		if (modifiedXml == null
				|| !org.springframework.util.StringUtils.hasText(modifiedXml)) {
			logger.debug("modifiedXml is null!");
			return originalXml;
		}

		if (originalXml == null
				|| !org.springframework.util.StringUtils.hasText(originalXml)) {
			return modifiedXml;
		}

		logger.debug("originalXml: " + originalXml);
		logger.debug("modifiedXml: " + modifiedXml);

		Document merged = replace(parse(originalXml),
				parse(modifiedXml));

		return DomUtils.elementToString(merged);

	}
	
	/**
	 * based on our use case, it only check the children of the root node...
	 * 
	 * @param originalDom
	 * @param modifiedDom
	 * @return Document
	 */
	private Document replace(final Document originalDom,
			final Document modifiedDom) {

		/**
		 * the children nodes of a xml Document is the first level nodes in the
		 * xml doc, for example, for a protocol xml, the children nodes of the
		 * doc is <code><protocol></code>
		 */

		// it needs to find the latest identical node then start replacing
		// whatever under it...
		Element rootNode = (Element) modifiedDom.getFirstChild();

		Document finalDom = originalDom;

		Element finalDomRoot = (Element) finalDom.getFirstChild();

		NodeList modifiedNodes = rootNode.getChildNodes();

		int l = modifiedNodes.getLength();

		logger.trace("lenght: " + l);
		for (int i = 0; i < l; i++) {
			Node currentNode = modifiedNodes.item(i);

			logger.trace("currentNode: " + currentNode.getNodeName());

			NodeList matchedNodes = finalDomRoot
					.getElementsByTagName(currentNode.getNodeName());

			for (int j = 0; j < matchedNodes.getLength(); j++) {
				finalDomRoot.removeChild(matchedNodes.item(j));
			}

			finalDomRoot.appendChild(finalDom.importNode(currentNode, true));
		}

		logger.trace("finalDom: " + DomUtils.elementToString(finalDom));

		return finalDom;
	}

	@Override
	public String mergeByXPaths(final String originalXml,
			final String modifiedXml, Operation xmlMergeOperation,
			Map<String, String> xPathPairs) throws SAXException, IOException,
			XPathExpressionException {

		if (modifiedXml == null
				|| !org.springframework.util.StringUtils.hasText(modifiedXml)) {
			logger.debug("modifiedXml is null!");
			return originalXml;
		}

		if (originalXml == null
				|| !org.springframework.util.StringUtils.hasText(originalXml)) {
			return modifiedXml;
		}

		logger.debug("originalXml: " + originalXml);
		logger.debug("modifiedXml: " + modifiedXml);

		Document merged = mergeByXPaths(parse(originalXml),
				parse(modifiedXml), xmlMergeOperation, xPathPairs);

		return DomUtils.elementToString(merged);

	}
	
	/***
	 * Private, never gets called directly from outside
	 * @param originalDom
	 * @param modifiedDom
	 * @param xmlMergeOperation
	 * @param xPathPairs
	 * @return
	 * @throws XPathExpressionException
	 */
	private Document mergeByXPaths(final Document originalDom,
			final Document modifiedDom, Operation xmlMergeOperation,
			Map<String, String> xPathPairs) throws XPathExpressionException {
		Document finalDom = null;

		switch (xmlMergeOperation) {
		/**
		 * all elements in modifiedDom will replace the elements in originalDom,
		 * if it exists. it will add the elements that do not exist in the
		 * originalDom... according to the xPaths list provided
		 */
		case REPLACE_BY_XPATH_LIST:
			finalDom = replaceByXPaths(originalDom, modifiedDom, xPathPairs);

			break;
			/**
			 * it will use elements in modifiedDom to update the elements in the
			 * originalDom, if the id attribute match.. Do I need this?
			 */
		case UPDATE_IF_EXIST:
			finalDom = replaceIfExistingByXPaths(originalDom, modifiedDom, xPathPairs);
			//throw new XmlProcessorOperationNotYetImplementedException(
					//xmlMergeOperation.toString() + " not yet implemented!");
			break;
		default:
			throw new XmlProcessorOperationNotYetImplementedException(
					xmlMergeOperation.toString() + " not yet implemented!");
		}

		return finalDom;
	}
	
	private Document replaceIfExistingByXPaths(final Document originalDom,
			final Document modifiedDom, Map<String, String> xPathPairs)
			throws XPathExpressionException {

		Document finalDom = originalDom;
		Element finalDomRoot = (Element) finalDom.getFirstChild();
		
		//Element modifiedDomRoot = (Element) modifiedDom.getFirstChild();

		Element lastChild = null;

		for (Entry<String, String> xPathPair : xPathPairs.entrySet()) {

			/**
			 * basically, this is to copy the element specified in srcXPath, and
			 * replace/add it to the position pointed by destXPath...
			 */
			String srcXPath = xPathPair.getKey();

			logger.debug("srcXPath: " + srcXPath);

			String destXPath = xPathPair.getValue();

			logger.debug("destXPath: " + destXPath);

			XPath xPath = getXPathInstance();
			// find all the nodes specified by destXPath in the originalDom, and
			// delete them all
			NodeList existingNodeList = (NodeList) (xPath.evaluate(destXPath,
					finalDom, XPathConstants.NODESET));
			
			xPath.reset();
			// find all the nodes specified by srcXPath in the modifiedDom
			NodeList nodeList = (NodeList) (xPath.evaluate(srcXPath,
					modifiedDom, XPathConstants.NODESET));

			int el = existingNodeList.getLength();

			logger.debug("find '" + el + "' in originalDom using xPath: "
					+ destXPath);
			
			int l = nodeList.getLength();

			logger.debug("find '" + l + "' in modifiedXml using xPath: "
					+ srcXPath);

			for (int i = 0; i < el; i++) {
				Node c = existingNodeList.item(i);
				
				//xPathExpression = xPath.compile(srcXPath);
				//NodeList srcNodeLst = (NodeList) (xPathExpression.evaluate(
						//modifiedDom, XPathConstants.NODESET));
				//NodeList srcNodeLst = modifiedDomRoot.getElementsByTagName(c.getNodeName());

				if (l > 0){
					// remove this node from its parent...
					
					c.getParentNode().removeChild(c);
					logger.debug("Node:" + c.getNodeName() + " is removed!");
				}
				
			}

			// create the node structure first. and return the last child of the
			// path... the right most node...
			lastChild = createElementStructureByPath(finalDomRoot, destXPath);

			List<String> nodeNameList = getNodeList(destXPath);

			String lastNodeName = nodeNameList.get(nodeNameList.size() - 1);

			Node currentNode = null;
			for (int i = 0; i < l; i++) {
				currentNode = nodeList.item(i);

				// the name of the last node in srcXPath might not be the same
				// as the name of the last node in destXPath
				Element lastElement = finalDom.createElement(lastNodeName);

				// NodeList currentNodeChildNodes = currentNode.getChildNodes();
				// int s = currentNodeChildNodes.getLength();
				// for(int j = 0; j < s; j++){
				// lastElement.appendChild(finalDom.importNode(currentNodeChildNodes.item(j),
				// true));
				// }
				if (currentNode.hasAttributes()){
					NamedNodeMap attributes = currentNode.getAttributes();
					
					for (int j = 0; j < attributes.getLength(); j++) {
						String attribute_name = attributes.item(j).getNodeName();
						String attribute_value = attributes.item(j).getNodeValue();
						
						lastElement.setAttribute(attribute_name, attribute_value);
					}
				}
				
				while (currentNode.hasChildNodes()) {
					Node kid = currentNode.getFirstChild();
					currentNode.removeChild(kid);
					lastElement.appendChild(finalDom.importNode(kid, true));
				}

				lastChild.appendChild(lastElement);

			}

		}

		return finalDom;

	}

	/**
	 * replace elements in originalDom with modifiedDom according to listed
	 * xPaths, if the originalDom has elements not listed in the xPath, it will
	 * be kept untouched. in the HashMap<String, String> xPathPairs, the key is
	 * the path in the source xml, and the value is the xpath for the final
	 * note*: the if the xpath has attributes, it's not going to work... need to
	 * do a custom implementation when that use case happened...
	 * 
	 * @param originalDom
	 * @param modifiedDom
	 * @param xPaths
	 * @return
	 * @throws XPathExpressionException
	 */
	private Document replaceByXPaths(final Document originalDom,
			final Document modifiedDom, Map<String, String> xPathPairs)
			throws XPathExpressionException {

		Document finalDom = originalDom;
		Element finalDomRoot = (Element) finalDom.getFirstChild();

		Element lastChild = null;

		for (Entry<String, String> xPathPair : xPathPairs.entrySet()) {

			/**
			 * basically, this is to copy the element specified in srcXPath, and
			 * replace/add it to the position pointed by destXPath...
			 */
			String srcXPath = xPathPair.getKey();

			logger.debug("srcXPath: " + srcXPath);

			String destXPath = xPathPair.getValue();

			logger.debug("destXPath: " + destXPath);

			XPath xPath = getXPathInstance();
			// find all the nodes specified by destXPath in the originalDom, and
			// delete them all
			NodeList existingNodeList = (NodeList) (xPath.evaluate(destXPath,
					finalDom, XPathConstants.NODESET));

			int el = existingNodeList.getLength();

			logger.debug("find '" + el + "' in originalDom using xPath: "
					+ destXPath);

			for (int i = 0; i < el; i++) {
				Node c = existingNodeList.item(i);
				// remove this node from its parent...
				c.getParentNode().removeChild(c);
			}

			// create the node structure first. and return the last child of the
			// path... the right most node...
			lastChild = createElementStructureByPath(finalDomRoot, destXPath);

			List<String> nodeNameList = getNodeList(destXPath);

			String lastNodeName = nodeNameList.get(nodeNameList.size() - 1);

			xPath.reset();
			// find all the nodes specified by srcXPath in the modifiedDom
			NodeList nodeList = (NodeList) (xPath.evaluate(srcXPath,
					modifiedDom, XPathConstants.NODESET));

			int l = nodeList.getLength();

			logger.debug("find '" + l + "' in modifiedXml using xPath: "
					+ srcXPath);

			Node currentNode = null;
			for (int i = 0; i < l; i++) {
				currentNode = nodeList.item(i);

				// the name of the last node in srcXPath might not be the same
				// as the name of the last node in destXPath
				Element lastElement = finalDom.createElement(lastNodeName);

				// NodeList currentNodeChildNodes = currentNode.getChildNodes();
				// int s = currentNodeChildNodes.getLength();
				// for(int j = 0; j < s; j++){
				// lastElement.appendChild(finalDom.importNode(currentNodeChildNodes.item(j),
				// true));
				// }
				if (currentNode.hasAttributes()){
					NamedNodeMap attributes = currentNode.getAttributes();
					
					for (int j = 0; j < attributes.getLength(); j++) {
						String attribute_name = attributes.item(j).getNodeName();
						String attribute_value = attributes.item(j).getNodeValue();
						
						lastElement.setAttribute(attribute_name, attribute_value);
					}
				}
				
				while (currentNode.hasChildNodes()) {
					Node kid = currentNode.getFirstChild();
					currentNode.removeChild(kid);
					lastElement.appendChild(finalDom.importNode(kid, true));
				}

				lastChild.appendChild(lastElement);

			}

		}

		return finalDom;

	}

	

	@Override
	public synchronized Map<String, Object> addElementByPath(final String path,
			final String originalXml, final String elementXml, boolean generateId)
			throws SAXException, IOException {
		Assert.hasText(path);
		Assert.hasText(originalXml);
		Assert.hasText(elementXml);

		logger.debug(elementXml);

		Document originalDom = parse(originalXml);

		Document elementDom = parse(elementXml);

		Document finalDom = originalDom;

		Element finalDomRoot = (Element) finalDom.getFirstChild();

		Element elementRoot = (Element) elementDom.getFirstChild();

		List<String> nodeList = getNodeList(path);
		logger.trace("nodeList: " + nodeList + " =? ");

		Assert.isTrue(nodeList.size() > 0);

		// remove first one, should be protocol
		nodeList.remove(0);

		String newElementName = nodeList.get(nodeList.size() - 1);

		logger.trace("adding <" + newElementName + ">");
		// remove last one, should be <drug>, we are attaching the drug into
		// drugs... so we want the rightmost element to be drugs...
		nodeList.remove(nodeList.size() - 1);

		Element currentNode = finalDomRoot;
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

				Element newN = finalDom.createElement(n);
				currentNode.appendChild(newN);

				currentNode = newN;
				continue;
			}

			currentNode = (Element) cur.item(0);

		}

		logger.trace("rightmost element: " + currentNode.getNodeName());

		String id = "";

		if (generateId) {
			// using jdk UUID as uuid generator...
			id = UUID.randomUUID().toString();

			Assert.isTrue(
					newElementName.equals(elementRoot.getNodeName()),
					"the element you are adding does not match the rightmost element name in the path!");

			elementRoot.setAttribute("id", id);
		}
		currentNode.appendChild(finalDom.importNode(elementRoot, true));

		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put("finalXml", DomUtils.elementToString(finalDom));
		resultMap.put("elementXml", DomUtils.elementToString(elementDom));
		resultMap.put("elementId", id);
		return resultMap;
	}

	/**
	 * If the xpath identifies multiple elements, it will only add to the first
	 * element, if there is no such parent element, it will just add it...
	 */
	@Override
	public synchronized Map<String, Object> addSubElementToElementIdentifiedByXPath(
			final String parentElementXPath, final String originalXml, final String elementXml,
			boolean generateId) throws SAXException, IOException,
			XPathExpressionException {
		Assert.hasText(parentElementXPath);
		Assert.hasText(originalXml);
		Assert.hasText(elementXml);


		Document originalDom = parse(originalXml);

		Document finalDom = originalDom;

		Document elementDom = parse(elementXml);

		Element elementRoot = (Element) elementDom.getFirstChild();

		XPath xPath = getXPathInstance();

		// find all the nodes specified by xPathString in the finalDom, and
		// delete them all
		NodeList existingNodeList = (NodeList) (xPath.evaluate(parentElementXPath,
				finalDom, XPathConstants.NODESET));

		int el = existingNodeList.getLength();

		String id = "";

		Element currentNode = finalDom.getDocumentElement();
		if (el == 0) { // doesn't exist, create the parent...
			List<String> nodeList = getNodeList(parentElementXPath);

			// remove first one, should be protocol
			nodeList.remove(0);

			int c = 0;
			for (String n : nodeList) {
				NodeList cur = currentNode.getElementsByTagName(n);
				String curName = currentNode.getNodeName();
				c = cur.getLength();

				if (c > 1) {
					throw new RuntimeException("illeagl xml structure; find "
							+ c + " elements with name " + n);
				}

				if (c == 0) {
					logger.debug("empty node...; " + n
							+ " doesn't exist under " + curName);

					Element newN = finalDom.createElement(n);
					currentNode.appendChild(newN);

					currentNode = newN;
					continue;
				}

				currentNode = (Element) cur.item(0);

			}
		} else if (el > 0) {
			currentNode = (Element) existingNodeList.item(0); // only the first
																// one
		}

		if (generateId) {
			// using jdk UUID as uuid generator...
			id = UUID.randomUUID().toString();

			elementRoot.setAttribute("id", id);
		}

		currentNode.appendChild(finalDom.importNode(elementRoot, true));
		/*
		 * for(int i = 0; i < el; i++){ Node c = existingNodeList.item(i);
		 * 
		 * if (generateId) { // using jdk UUID as uuid generator... String id =
		 * UUID.randomUUID().toString();
		 * 
		 * elementRoot.setAttribute("id", id); }
		 * 
		 * c.appendChild(finalDom.importNode(elementRoot, true));
		 * 
		 * }
		 */
		logger.trace(DomUtils.elementToString(finalDom));

		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put("finalXml", DomUtils.elementToString(finalDom));
		resultMap.put("elementXml", DomUtils.elementToString(elementDom));
		resultMap.put("elementId", id);
		return resultMap;

	}

	@Override
	public synchronized Map<String, Object> deleteElementByPathById(
			String path, final String originalXml, String elementId)
			throws SAXException, IOException, XPathExpressionException {
		Assert.hasText(path);
		Assert.hasText(originalXml);
		Assert.hasText(elementId);

		Document originalDom = parse(originalXml);

		Document finalDom = originalDom;

		String xPathString = path + "[@id='" + elementId + "']";

		XPath xPath = getXPathInstance();

		// find all the nodes specified by xPathString in the finalDom, and
		// delete them all
		NodeList existingNodeList = (NodeList) (xPath.evaluate(xPathString,
				finalDom, XPathConstants.NODESET));

		int el = existingNodeList.getLength();

		logger.trace("find '" + el + "' in originalDom using xPath: "
				+ xPathString);

		for (int i = 0; i < el; i++) {
			Node c = existingNodeList.item(i);

			Node cp = c.getParentNode();
			// remove this node from its parent...
			cp.removeChild(c);

			logger.trace("node has child : " + cp.getChildNodes().getLength()
					+ ":" + cp.hasChildNodes());
		}

		logger.trace(DomUtils.elementToString(finalDom));

		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put("finalXml", DomUtils.elementToString(finalDom));
		resultMap.put("isDeleted", true);
		return resultMap;

	}

	/**
	 * Thread-safety tested
	 */
	@Override
	public Map<String, Object> deleteElementByPath(String path,
			final String originalXml) throws SAXException, IOException,
			XPathExpressionException {
		Assert.hasText(path);
		Assert.hasText(originalXml);

		Document originalDom = parse(originalXml);

		Document finalDom = originalDom;

		XPath xPath = getXPathInstance();

		// find all the nodes specified by xPathString in the finalDom, and
		// delete them all
		NodeList existingNodeList = (NodeList) (xPath.evaluate(path,
				finalDom, XPathConstants.NODESET));

		int el = existingNodeList.getLength();

		logger.trace("find '" + el + "' in originalDom using xPath: " + path);

		for (int i = 0; i < el; i++) {
			Node c = existingNodeList.item(i);

			Node cp = c.getParentNode();
			// remove this node from its parent...
			cp.removeChild(c);

			logger.trace("node has child : " + cp.getChildNodes().getLength()
					+ ":" + cp.hasChildNodes());
		}

		logger.trace(DomUtils.elementToString(finalDom));

		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put("finalXml", DomUtils.elementToString(finalDom));
		resultMap.put("isDeleted", true);
		return resultMap;

	}

	private synchronized boolean isEqualNode(final Node original, final Node patch) {
		if (patch == original) {
			return true;
		}
		if (patch.getNodeType() != original.getNodeType()) {
			return false;
		}

		if (original.getNodeName() == null) {
			if (patch.getNodeName() != null) {
				return false;
			}
		} else if (!original.getNodeName().equals(patch.getNodeName())) {
			return false;
		}

		if (original.getLocalName() == null) {
			if (patch.getLocalName() != null) {
				return false;
			}
		} else if (!original.getLocalName().equals(patch.getLocalName())) {
			return false;
		}

		if (original.getNamespaceURI() == null) {
			if (patch.getNamespaceURI() != null) {
				return false;
			}
		} else if (!original.getNamespaceURI().equals(patch.getNamespaceURI())) {
			return false;
		}

		if (original.getPrefix() == null) {
			if (patch.getPrefix() != null) {
				return false;
			}
		} else if (!original.getPrefix().equals(patch.getPrefix())) {
			return false;
		}

		if (original.getNodeValue() == null) {
			if (patch.getNodeValue() != null) {
				return false;
			}
		} else if (!original.getNodeValue().equals(patch.getNodeValue())) {
			return false;
		}
		
		if (original.getTextContent() == null) {
			if (patch.getTextContent() != null) {
				return false;
			}
		} else if (!original.getTextContent().equals(patch.getTextContent())) {
			return false;
		}
		
		return true;
	}

	private synchronized boolean hasEqualAttributes(final Node original, final Node patch) {

		NamedNodeMap map1 = original.getAttributes();
		NamedNodeMap map2 = patch.getAttributes();
		int len = map1.getLength();
		if (len != map2.getLength()) {
			return false;
		}

		for (int i = 0; i < len; i++) {
			Node n1 = map1.item(i);
			if (n1.getNodeName() != null) {
				Node n2 = map2.getNamedItem(n1.getNodeName());
				if (n2 == null) {
					return false;
				} else if (!n1.getNodeValue().equals(n2.getNodeValue())) {
					return false;
				}
			}
		}
		return true;
	}

	private synchronized DuplicateChildElementObject isChildElement(
			final Element origianlRootElement, final Element patchElement) {

		DuplicateChildElementObject childElementObject = new DuplicateChildElementObject();

		NodeList originalItems = origianlRootElement.getChildNodes();
		int item_number = originalItems.getLength();
		
		childElementObject.setNeedDuplicate(true);
		childElementObject.setElement(origianlRootElement);
		/*
		for (int i = 0; i < item_number; i++) {
			Element originalItem = null;
			//logger.debug("node name: " + DomUtils
					//.elementToString(originalItems.item(i)) + " node type: " + originalItems.item(i).getNodeType());
			if (originalItems.item(i).getNodeType() == Node.ELEMENT_NODE){
				originalItem = (Element) originalItems.item(i);
			}

			if (!originalItem.getNodeName().equals(patchElement.getNodeName())) {
				continue;
			}
			
			if (originalItem.isEqualNode(patchElement)) {
				childElementObject.setNeedDuplicate(false);
				childElementObject.setElement(originalItem);
				return childElementObject;
			}
		}
		*/
		
		for (int i = 0; i < item_number; i++) {
			Element originalItem = null;
			if (originalItems.item(i).getNodeType() == Node.ELEMENT_NODE){
				originalItem = (Element) originalItems.item(i);
			}

			if (!originalItem.getNodeName().equals(patchElement.getNodeName())) {
				continue;
			}
			
			if (isEqualNode(originalItem, patchElement)) {
				if (hasEqualAttributes(originalItem, patchElement)) {
					childElementObject.setNeedDuplicate(false);
					childElementObject.setElement(originalItem);
					return childElementObject;
				} else {
					childElementObject.setNeedDuplicate(true);
					childElementObject.setElement(origianlRootElement);
					return childElementObject;
				}
			}
		}

		return childElementObject;
	}

	private synchronized String duplicate(final Document originalDom,
			final Element originalRootElement, final Element patchElement) throws Exception {

		boolean isdone = false;
		Element parentElement = null;

		DuplicateChildElementObject childElementObject = isChildElement(
				originalRootElement, patchElement);
		if (!childElementObject.isNeedDuplicate()) {
			isdone = true;
			parentElement = childElementObject.getElement();
		} else if (childElementObject.getElement() != null) {
			parentElement = childElementObject.getElement();
		} else {
			parentElement = originalRootElement;
		}

		String son_name = patchElement.getNodeName();

		Element subITEM = null;
		if (!isdone) {
			subITEM = originalDom.createElement(son_name);

			if (patchElement.hasChildNodes()){
				if (patchElement.getFirstChild().getNodeType() == Node.TEXT_NODE){
					subITEM.setTextContent(patchElement.getTextContent());
					
				}
			}
			
			if (patchElement.hasAttributes()) {
				NamedNodeMap attributes = patchElement.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					String attribute_name = attributes.item(i).getNodeName();
					String attribute_value = attributes.item(i).getNodeValue();
					subITEM.setAttribute(attribute_name, attribute_value);
				}
			}
			parentElement.appendChild(subITEM);
		} else {
			subITEM = parentElement;
		}

		NodeList sub_messageItems = patchElement.getChildNodes();
		int sub_item_number = sub_messageItems.getLength();
		logger.debug("patchEl: " + DomUtils
							.elementToString(patchElement) + "length: " + sub_item_number);
		if (sub_item_number == 0) {
			isdone = true;
		} else {
			for (int j = 0; j < sub_item_number; j++) {
				if (sub_messageItems.item(j).getNodeType() == Node.ELEMENT_NODE){
					Element sub_messageItem = (Element) sub_messageItems.item(j);
					logger.debug("node name: " + DomUtils
							.elementToString(subITEM) + " node type: " + subITEM.getNodeType());
					duplicate(originalDom, subITEM, sub_messageItem);
				}
				
			}
		}

		return (parentElement != null) ? DomUtils
				.elementToString(parentElement) : "";
	}

	
	/**
	 * split path by "/" and remove empty node...
	 * 
	 * @param path
	 * @return
	 */
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
	public String getElementByPathById(String path,
			final String originalXml, String elementId)
			throws XPathExpressionException, SAXException, IOException {

		Assert.hasText(path);
		Assert.hasText(originalXml);
		Assert.hasText(elementId);

		Document originalDom = parse(originalXml);

		path = path + "[@id='" + elementId + "']";

		XPath xPath = getXPathInstance();

		Element element = (Element) (xPath.evaluate(path, originalDom,
				XPathConstants.NODE));
		Assert.notNull(element);

		return DomUtils.elementToString(element);
	}
	
	/**
	 * Thread-safety tested
	 */
	@Override
	public Map<String, Object> updateElementByPathById(
			String path, final String originalXml, String elementId, final String elementXml)
			throws SAXException, IOException {

		Assert.hasText(path);
		Assert.hasText(elementId);
		Assert.hasText(originalXml);
		Assert.hasText(elementId);


		Document originalDom = parse(originalXml);

		Document elementDom = parse(elementXml);

		Document finalDom = originalDom;

		Element finalDomRoot = (Element) finalDom.getFirstChild();

		Element elementRoot = (Element) elementDom.getFirstChild();

		List<String> nodeList = getNodeList(path);
		logger.trace("nodeList: " + nodeList + " =? ");

		// the root node of the path should be the same as the root node of
		// the originalXml
		Assert.isTrue(nodeList.size() > 0
				&& nodeList.get(0).equals(finalDomRoot.getNodeName()));

		// remove first one, should be protocol
		nodeList.remove(0);

		String elementToDeleteName = nodeList.get(nodeList.size() - 1);

		logger.trace("adding <" + elementToDeleteName + ">");
		// remove last one, should be <drug>, we are attaching the drug into
		// drugs... so we want the rightmost element to be drugs...
		nodeList.remove(nodeList.size() - 1);

		Element currentNode = finalDomRoot;
		int c = 0;
		for (String n : nodeList) {
			NodeList cur = currentNode.getElementsByTagName(n);

			c = cur.getLength();

			if (c > 1) {
				throw new RuntimeException("illeagl xml structure; find " + c
						+ " elements with name " + n);
			}

			if (c == 0) {
				throw new RuntimeException("illeagl xml structure; " + n
						+ " doesn't exist");
			}

			currentNode = (Element) cur.item(0);

		}

		logger.trace("rightmost element: " + currentNode.getNodeName());

		elementRoot.setAttribute("id", elementId);

		Node newChild = finalDom.importNode(elementRoot, true);
		NodeList nodes = currentNode.getChildNodes();

		int l = nodes.getLength();

		logger.trace("lenght: " + l);

		for (int i = 0; i < l; i++) {
			Element cc = (Element) nodes.item(i);

			if (cc.getAttribute("id").equals(elementId)) {
				currentNode.replaceChild(newChild, cc);
				break;
			}
		}

		Map<String, Object> resultMap = new HashMap<String, Object>(3);
		resultMap.put("finalXml", DomUtils.elementToString(finalDom));
		resultMap.put("elementXml", DomUtils.elementToString(elementDom));
		resultMap.put("elementId", elementId);
		return resultMap;
	}

	@Override
	public synchronized String listElementsByPath(String path,
			final String originalXml, boolean inList, boolean includeChildren)
			throws XPathExpressionException, SAXException, IOException {

		Assert.hasText(path);
		Assert.hasText(originalXml);

		Document originalDom = parse(originalXml);

		XPath xPath = getXPathInstance();

		logger.debug("xpath: " + path);
		NodeList nodeList = (NodeList) (xPath.evaluate(path, originalDom,
				XPathConstants.NODESET));
		Assert.notNull(nodeList);

		Document finalDom = documentBuilder.newDocument();
		Element root = null;
		if (inList) {
			root = finalDom.createElement("list");
		} else {
			root = finalDom.createElement(originalDom.getFirstChild()
					.getNodeName());
		}
		int l = nodeList.getLength();

		logger.trace("find: " + l);
		Node currentNode = null;
		for (int i = 0; i < l; i++) {
			currentNode = nodeList.item(i);
			logger.trace("find: " + currentNode.getNodeName());
			root.appendChild(finalDom.importNode(currentNode, includeChildren));
		}

		return DomUtils.elementToString(root);
	}

	@Override
	public synchronized String listElementsByPath(String path,
			final String originalXml, boolean inList)
			throws XPathExpressionException, SAXException, IOException {

		return listElementsByPath(path, originalXml, inList, true);

	}

	/**
	 * inList para force to create a <list></list> wrap the result
	 */
	@Override
	public synchronized String listElementsByPaths(Set<String> paths,
			final String xmlData, boolean inList, boolean includeChildren)
			throws SAXException, IOException, XPathExpressionException {
		Assert.hasText(xmlData);

		Document originalDom = parse(xmlData);

		Document finalDom = documentBuilder.newDocument();

		Element root = null;
		if (inList) {
			root = finalDom.createElement("list");
		} else {
			root = finalDom.createElement(originalDom.getFirstChild()
					.getNodeName());
		}

		// Element lastChild = null;

		for (String path : paths) {

			// create the node structure first. and return the last child of the
			// path... the right most node...
			// lastChild = createElementStructureByPath(root, path);
			XPath xPath = getXPathInstance();
			NodeList nodeList = (NodeList) (xPath.evaluate(path,
					originalDom, XPathConstants.NODESET));

			int l = nodeList.getLength();
			logger.trace("find: " + l);

			Node currentNode = null;
			for (int i = 0; i < l; i++) {
				currentNode = nodeList.item(i);

				root.appendChild(finalDom.importNode(currentNode,
						includeChildren));

			}

		}

		return DomUtils.elementToString(root);

	}

	@Override
	public String listElementsByPaths(Set<String> paths,
			final String xmlData, boolean inList) throws SAXException, IOException,
			XPathExpressionException {

		return listElementsByPaths(paths, xmlData, inList, true);

	}

	/**
	 * inList para force to create a <list></list> wrap the result
	 */
	@Override
	public List<Element> listDomElementsByPaths(Set<String> paths,
			final String xmlData) throws SAXException, IOException,
			XPathExpressionException {
		Assert.hasText(xmlData);

		Document originalDom = parse(xmlData);

		List<Element> elements = new ArrayList<Element>();

		for (String path : paths) {

			// create the node structure first. and return the last child of the
			// path... the right most node...
			// lastChild = createElementStructureByPath(root, path);
			XPath xPath = getXPathInstance();
			NodeList nodeList = (NodeList) (xPath.evaluate(path,
					originalDom, XPathConstants.NODESET));

			int l = nodeList.getLength();
			logger.trace("find: " + l);

			Node currentNode = null;
			for (int i = 0; i < l; i++) {
				currentNode = nodeList.item(i);

				elements.add((Element) currentNode);

			}

		}

		return elements;

	}

	@Override
	public Map<String, Object> listElementValuesByPaths(
			final Set<String> paths, final Map<String, Class<?>> dataTypes, final String xmlData)
			throws SAXException, IOException, XPathExpressionException {
		Document originalDom = parse(xmlData);

		Map<String, Object> results = new HashMap<String, Object>(0);
		for (String path : paths) {

			XPath xPath = getXPathInstance();
			NodeList nodeList = (NodeList) (xPath.evaluate(path,
					originalDom, XPathConstants.NODESET));

			int l = nodeList.getLength();
			logger.trace("find: " + l);

			Class<?> dataType = dataTypes.get(path);

			if (dataType.equals(List.class)) {
				List<String> valueList = new ArrayList<String>(0);
				Node currentNode = null;
				for (int i = 0; i < l; i++) {
					currentNode = nodeList.item(i);
					logger.trace(currentNode.getNodeName()
							+ " = "
							+ currentNode.getFirstChild().getNodeValue()
									.toString());
					valueList.add(currentNode.getFirstChild().getNodeValue()
							.toString());
				}
				results.put(path, valueList);
			} else {

				Node currentNode = nodeList.item(0);

				Object valueObject = null;

				if (currentNode != null && currentNode.getFirstChild() != null
						&& currentNode.getFirstChild().getNodeValue() != null) {
					valueObject = currentNode.getFirstChild().getNodeValue();

					if (valueObject != null) {

						try {
							valueObject = dataType.cast(currentNode
									.getFirstChild().getNodeValue());
						} catch (ClassCastException cce) {
							valueObject = valueObject.toString();
						}
						logger.trace(currentNode.getNodeName() + " = {"
								+ valueObject.toString() + "}");
					}

				}

				if (valueObject == null) {
					logger.trace(currentNode.getNodeName() + " = {"
							+ valueObject + "}");
				}

				results.put(path, valueObject);
			}

		}

		return results;
	}
	
	/**
	 * inList para force to create a <list></list> wrap the result
	 */
	@Override
	public List<String> listElementDomStringsByPaths(Set<String> paths,
			final String xmlData, boolean includeChildren)
			throws SAXException, IOException, XPathExpressionException {
		Assert.hasText(xmlData);

		Document originalDom = parse(xmlData);

		Document finalDom = documentBuilder.newDocument();
		
		
		List<String> nodes = Lists.newArrayList();
		// Element lastChild = null;

		for (String path : paths) {

			// create the node structure first. and return the last child of the
			// path... the right most node...
			// lastChild = createElementStructureByPath(root, path);
			XPath xPath = getXPathInstance();
			NodeList nodeList = (NodeList) (xPath.evaluate(path,
					originalDom, XPathConstants.NODESET));

			int l = nodeList.getLength();
			logger.trace("find: " + l);

			Node currentNode = null;
			for (int i = 0; i < l; i++) {
				currentNode = nodeList.item(i);

				Node importedNode = finalDom.importNode(currentNode,
						includeChildren);
				nodes.add(DomUtils.elementToString(importedNode));

			}

		}

		return nodes;

	}
	
	
	
	@Override
	public Map<String, List<String>> listElementStringValuesByPaths(
			final Set<String> paths, final String xmlData) throws SAXException,
			IOException, XPathExpressionException {
		Assert.hasText(xmlData);

		Document originalDom = parse(xmlData);

		Map<String, List<String>> results = new HashMap<String, List<String>>(0);
		for (String path : paths) {

			XPath xPath = getXPathInstance();
			NodeList nodeList = (NodeList) (xPath.evaluate(path,
					originalDom, XPathConstants.NODESET));

			int l = nodeList.getLength();
			logger.trace("find: " + l);

			List<String> valueList = new ArrayList<String>(0);

			Node currentNode = null;
			for (int i = 0; i < l; i++) {
				currentNode = nodeList.item(i);

				Object valueObject = null;

				if (currentNode != null && currentNode.getFirstChild() != null
						&& currentNode.getFirstChild().getNodeValue() != null) {
					valueObject = currentNode.getFirstChild().getNodeValue();

					if (valueObject != null) {
						valueList.add(valueObject.toString());

						logger.trace(currentNode.getNodeName() + " = "
								+ valueObject.toString());
					}
				}

			}
			results.put(path, valueList);

		}

		return results;
	}

	/***
	 * Read-only, no need to synchronized
	 * 
	 */
	@Override
	public synchronized List<String> listElementStringValuesByPath(String path,
			final String xmlData) throws SAXException, IOException,
			XPathExpressionException {
		Assert.hasText(xmlData);


		Document originalDom = parse(xmlData);

		XPath xPath = getXPathInstance();

		NodeList nodeList = (NodeList) (xPath.evaluate(path, originalDom,
				XPathConstants.NODESET));

		int l = nodeList.getLength();
		logger.trace("find: " + l);

		List<String> valueList = new ArrayList<String>(0);

		Node currentNode = null;
		for (int i = 0; i < l; i++) {
			currentNode = nodeList.item(i);

			Object valueObject = null;

			if (currentNode != null && currentNode.getFirstChild() != null
					&& currentNode.getFirstChild().getNodeValue() != null) {
				valueObject = currentNode.getFirstChild().getNodeValue();

				if (valueObject != null) {
					valueList.add(valueObject.toString());

					logger.trace(currentNode.getNodeName() + " = "
							+ valueObject.toString());
				}
			}

		}

		return valueList;

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

	@Override
	public String addAttributesByPath(String path, final String xmlData,
			final Map<String, String> attributes) throws SAXException, IOException, XPathExpressionException {

		Document finalDom = parse(xmlData);

		XPath xPath = getXPathInstance();
		NodeList nodeList = (NodeList) (xPath.evaluate(path, finalDom,
				XPathConstants.NODESET));

		int l = nodeList.getLength();

		Element currentElement = null;
		for (int i = 0; i < l; i++) {
			if (nodeList.item(i) == null)
				continue;
			currentElement = (Element) nodeList.item(i);

			for (Entry<String, String> entry : attributes.entrySet()) {
				currentElement.setAttribute(entry.getKey(), entry.getValue());
			}
		}

		return DomUtils.elementToString(finalDom.getFirstChild());
	}

	@Override
	public String newElementIdByPath(String path, final String xmlData)
			throws SAXException, IOException, XPathExpressionException {
		Assert.hasText(path);
		Assert.hasText(xmlData);


		Document finalDom = parse(xmlData);

		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();
		xPathExpression = xPath.compile(path);
		NodeList nodeList = (NodeList) (xPathExpression.evaluate(finalDom,
				XPathConstants.NODESET));

		int l = nodeList.getLength();

		Element currentElement = null;
		for (int i = 0; i < l; i++) {
			if (nodeList.item(i) == null)
				continue;
			String id = UUID.randomUUID().toString();
			currentElement = (Element) nodeList.item(i);

			currentElement.setAttribute("id", id);
		}

		return DomUtils.elementToString(finalDom.getFirstChild());
	}
	
	@Override
	public String replaceAttributeValueByPathAndAttributeName(String path, String attributeName, final String xmlData, String value)
			throws SAXException, IOException, XPathExpressionException {
		Assert.hasText(path);
		Assert.hasText(xmlData);

		Document finalDom = parse(xmlData);

		XPath xPath = getXPathInstance();
		NodeList nodeList = (NodeList) (xPath.evaluate(path,finalDom,
				XPathConstants.NODESET));

		int l = nodeList.getLength();

		Element currentElement = null;
		for (int i = 0; i < l; i++) {
			if (nodeList.item(i) == null)
				continue;
			currentElement = (Element) nodeList.item(i);
			currentElement.setAttribute(attributeName, value);
		}

		return DomUtils.elementToString(finalDom.getFirstChild());
	}
	
	@Override
	public String deleteAttributeByPathAndAttributeName(String path, String attributeName, final String xmlData)
			throws SAXException, IOException, XPathExpressionException {
		Assert.hasText(path);
		Assert.hasText(xmlData);

		Document finalDom = parse(xmlData);
		
		XPath xPath = getXPathInstance();
		
		NodeList nodeList = (NodeList) (xPath.evaluate(path, finalDom,
				XPathConstants.NODESET));

		int l = nodeList.getLength();

		Element currentElement = null;
		for (int i = 0; i < l; i++) {
			if (nodeList.item(i) == null)
				continue;
			currentElement = (Element) nodeList.item(i);
			currentElement.removeAttribute(attributeName);
		}

		return DomUtils.elementToString(finalDom.getFirstChild());
	}

	

	/*
	 * @Override public String replaceNodeValueByPath(String path, String
	 * xmlData, String nodeValue) throws SAXException, IOException,
	 * XPathExpressionException { InputSource documentInputSource = new
	 * InputSource(new StringReader( xmlData));
	 * 
	 * Document finalDom = parse(documentInputSource);
	 * 
	 * XPathExpression xPathExpression = null;
	 * 
	 * XPath xPath = getXPathInstance(); xPathExpression = xPath.compile(path);
	 * NodeList nodeList = (NodeList) (xPathExpression.evaluate(finalDom,
	 * XPathConstants.NODESET));
	 * 
	 * int l = nodeList.getLength();
	 * 
	 * Element currentElement = null; for (int i = 0; i < l; i++) { if
	 * (nodeList.item(i) == null) continue; currentElement = (Element)
	 * nodeList.item(i);
	 * 
	 * currentElement.setTextContent(nodeValue); }
	 * 
	 * return DomUtils.elementToString(finalDom.getFirstChild()); }
	 */

	@Override
	public String replaceOrAddNodeValueByPath(final String path, final String xmlData,
			final String nodeValue) throws SAXException, IOException,
			XPathExpressionException {

		Document finalDom = parse(xmlData);

		XPath xPath = getXPathInstance();
		NodeList nodeList = (NodeList) (xPath.evaluate(path, finalDom,
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

	/**
	 * thread-safety tested
	 */
	@Override
	public String replaceRootTagWith(final String xmlData, final String rootTag)
			throws SAXException, IOException {

		Document document = parse(xmlData);

		String resultXml = "";
		if (rootTag == null) { // remove which will lose the attirbutes on the
								// root element
			NodeList nodes = document.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				resultXml += DomUtils.elementToString((Element) n);
			}

		} else {
			document.renameNode(document.getFirstChild(), null, rootTag);

			resultXml = DomUtils.elementToString(document.getFirstChild());

		}

		return resultXml;

	}
	
	
	
	@Override
	public String getAttributeValueByPathAndAttributeName(String path,
			final String originalXml, String attributeName)
			throws XPathExpressionException, SAXException, IOException {

		Document document = parse(originalXml);
		
		XPath xPath = getXPathInstance();
		
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
	
	@Override
	public List<String> getAttributeValuesByPathAndAttributeName(String path,
			final String originalXml, String attributeName)
			throws XPathExpressionException, SAXException, IOException {

		Document document = parse(originalXml);
		
		XPath xPath = getXPathInstance();
		
		NodeList nodeList = (NodeList) (xPath.evaluate(
				path, document,
				XPathConstants.NODESET));

		List<String> attributeValues = new ArrayList<String>();
		
		if (nodeList != null && nodeList.getLength() > 0){
			for (int i = 0; i < nodeList.getLength(); i++){
				Element currentElement = (Element) nodeList.item(i);

				if (currentElement.getAttribute(attributeName) != null && !currentElement.getAttribute(attributeName).isEmpty()){
					attributeValues.add(currentElement.getAttribute(attributeName));
				}
			}
		}

		return attributeValues;
	}
	
	private void escapeNodeText(final NodeList nodes) {
		
		int l = nodes.getLength();
		
		if (nodes != null && l > 0){
			for (int i = 0; i < l; i++) {
				Node currentNode = nodes.item(i);
			
				//node value
				if (currentNode.getNodeType() == Node.TEXT_NODE) {
					currentNode.setNodeValue(StringEscapeUtils.escapeXml(currentNode.getNodeValue()));
				}
				//attributes
				NamedNodeMap attributes =  currentNode.getAttributes();
				
				if (attributes != null) {
					int len = attributes.getLength();
		
					for (int j = 0; j < len; j++) {
						Node attribute = attributes.item(j);
						attribute.setNodeValue(StringEscapeUtils.escapeXml(attribute.getNodeValue()));
					}				
				}
				
				
				if (currentNode.hasChildNodes()){
					escapeNodeText(currentNode.getChildNodes());
				}
				
				//logger.info("current: " + DomUtils.elementToString(currentNode, true, Encoding.ISO8859_1));
			}
		}
		
	}
	@Override
	public String escapeText(String inXml) throws SAXException, IOException{
		Document document = parse(inXml);
		Element rootNode = (Element) document.getFirstChild();
		
		escapeNodeText(rootNode.getChildNodes());
		
		return DomUtils.elementToString(document).replace("&amp;#", "&#");
		
	}
	
	/***
	 * DocumentBuilder is not thread-safe and needs to be reset before reuse
	 * @param in
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 */
	@Override
	public synchronized Document parse(final String inXmlString) throws SAXException, IOException {
		try{
			InputSource documentInputSource = new InputSource(new StringReader(
					inXmlString));
			documentBuilder.reset();
			return documentBuilder.parse(documentInputSource);
		}catch(Exception ex){
			logger.error("Exception when parsing: " + inXmlString, ex);
			throw ex;
		}
	}
	
	@Override 
	public synchronized DocumentBuilder getDocumentBuilder() throws ParserConfigurationException{
		return dbFactory.newDocumentBuilder();
	}
	
	@Override
	public synchronized Document newDocument() {
		documentBuilder.reset();
		return documentBuilder.newDocument();
	}
	
	@Override
	public synchronized String loadXmlFile(final File xmlFile) throws IOException {
		byte[] buffer = new byte[(int) xmlFile.length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(xmlFile));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);

	}

	@Override
	public synchronized String loadXmlFile(final String xmlFilePath)
			throws IOException {

		Resource xmlFileResource = resourceLoader.getResource(xmlFilePath);

		return loadXmlFile(xmlFileResource.getFile());
	}

	@Override
	public synchronized Document loadXmlFileToDOM(final File xmlFile)
			throws IOException, SAXException {
		
		return parse(loadXmlFile(xmlFile));
	}

	@Override
	public synchronized Document loadXmlFileToDOM(final String xmlFilePath)
			throws IOException, SAXException {
		return loadXmlFileToDOM(resourceLoader.getResource(xmlFilePath)
				.getFile());
	}

	@Override
	public synchronized Document loadXmlStringToDOM(final String xmlData)
			throws IOException, SAXException {

		return parse(xmlData);
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	

}
