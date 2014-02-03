package edu.uams.clara.migration.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public interface ARIALetterMigrationService {
	void migrateLetter() throws FileNotFoundException, IOException;
	void deleteErrorLetter() throws IOException, XPathExpressionException;
	XPath getXPathInstance();

	XPathFactory getXpathFactory();
}
