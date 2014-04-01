package edu.uams.clara.test.Assert;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public final class XMLAssert {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlProcessor.class);

	private final static DocumentBuilderFactory dbFactory = DocumentBuilderFactory
			.newInstance();

	private synchronized static boolean isNodeEquals(Node nodeA, Node nodeB) {
		Assert.assertNotNull(nodeA);
		Assert.assertNotNull(nodeB);
		final int typeA = nodeA.getNodeType();
		final int typeB = nodeB.getNodeType();
		Assert.assertEquals(typeA, typeB);

		switch (typeA) {
		case Node.ELEMENT_NODE:
			return isElementEquals((Element) nodeA, (Element) nodeB);
		case Node.COMMENT_NODE:
			return true;
		case Node.CDATA_SECTION_NODE:
			return true;
		case Node.TEXT_NODE:
			return isCharacterDataEquals((CharacterData) nodeA,
					(CharacterData) nodeB);
		case Node.PROCESSING_INSTRUCTION_NODE:
			return isProcessingInstructionEquals((ProcessingInstruction) nodeA,
					(ProcessingInstruction) nodeB);
		default:
			logger.error("Unexpected node type: " + nodeA.toString());
			return true;
		}
	}

	private synchronized static boolean isCharacterDataEquals(CharacterData charDataA,
			CharacterData charDataB) {
		return charDataA.getData().equals(charDataB.getData());
	}

	private synchronized static boolean isProcessingInstructionEquals(
			ProcessingInstruction piA, ProcessingInstruction piB) {
		return piA.getData().equals(piB.getData());
	}

	private synchronized static boolean isNodeListEquals(NodeList listA, NodeList listB) {
		final int sizeA = listA.getLength();
		final int sizeB = listB.getLength();
		if (sizeA != sizeB) {
			return false;
		}

		for (int i = 0, n = sizeA; i < n; ++i) {

			boolean tEqual = false;
			for (int j = 0, m = sizeB; j < m; ++j) {
				if (isNodeEquals(listA.item(i), listB.item(j))) {
					tEqual = true;
					break;
				}
			}
			if (!tEqual) {
				return false;
			}
		}

		return true;
	}


	private synchronized static boolean isAttributeMapEquals(NamedNodeMap listA,
			NamedNodeMap listB) {
		// This is only for checking attribute maps.

		// Assert.assertEquals(listA.getLength(), listB.getLength());
		final int lengthA = listA.getLength();
		final int lengthB = listB.getLength();
		if (lengthA != lengthB) {
			return false;
		}

		
		for (int i = 0, n = listA.getLength(); i < n; ++i) {
			final Node itemA = listA.item(i);
			final Node itemB = listB.getNamedItem(itemA.getNodeName());

			if (itemB == null) {
				return false;
			}

			if (itemA.getNodeType() != itemB.getNodeType()) {
				return false;
			}

			if (!itemA.getNodeValue().equals(itemB.getNodeValue())) {
				return false;
			}
		}
		return true;
		/*
		 * 
		 * for (int i = 0, n = listA.getLength(); i < n; ++i) { final Node itemA
		 * = listA.item(i); final Node itemB =
		 * listB.getNamedItem(itemA.getNodeName());
		 * 
		 * 
		 * boolean tEqual = false; for (int j = 0, m = listB.getLength(); j < m;
		 * ++j) { if(isNodeEquals(listA.item(i), listB.item(j))){ tEqual = true;
		 * break; } } if(!tEqual){ return false; } }
		 * 
		 * return true;
		 */
	}

	private synchronized static boolean isElementEquals(Element elemA, Element elemB) {
		if (!elemA.getNodeName().equals(elemB.getNodeName())) {
			return false;
		}

		if (!isAttributeMapEquals(elemA.getAttributes(), elemB.getAttributes())) {
			return false;
		}

		return isNodeListEquals(elemA.getChildNodes(), elemB.getChildNodes());
	}

	private synchronized static void assertDocumentEquals(Document docA, Document docB) {
		if (!isNodeListEquals(docA.getChildNodes(), docB.getChildNodes())) {
			throw new AssertionError("not equal");
		}
	}

	public synchronized static void assertXMLEquals(String originalXml, String modifiedXml)
			throws AssertionError, ParserConfigurationException, SAXException,
			IOException {

		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		
		Document o = dBuilder.parse(new InputSource(new StringReader(
				originalXml)));
		
		
		Document m = dBuilder.parse(new InputSource(new StringReader(
				modifiedXml)));

		assertDocumentEquals(o, m);

	}
}
