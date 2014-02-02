package edu.uams.clara.webapp.xml.processor;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public interface ProtocolFormXmlDifferService {
	String differProtocolFormXml(String baseTag, String oldXml, String newXml)
			throws XPathExpressionException;
	
	XPathFactory getXpathFactory();
	
	XPath getXPathInstance();

}
