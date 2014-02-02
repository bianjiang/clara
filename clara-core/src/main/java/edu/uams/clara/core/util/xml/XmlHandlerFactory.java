package edu.uams.clara.core.util.xml;

import javax.xml.parsers.ParserConfigurationException;

import edu.uams.clara.core.util.xml.impl.XmlHandlerImpl;

public final class XmlHandlerFactory {

	public static XmlHandler newXmlHandler() throws ParserConfigurationException{
		return new XmlHandlerImpl();
	}
}
