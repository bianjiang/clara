package edu.uams.clara.migration.service;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;

public interface MigrationService {
	List<String> saveMegrateDataAsProtocol(String metaXmlData,List<String> existedUserList) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException;

	XPath getXPathInstance();

	XPathFactory getXpathFactory();
}
