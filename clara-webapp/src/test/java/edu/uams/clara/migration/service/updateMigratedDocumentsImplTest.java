package edu.uams.clara.migration.service;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.migration.service.impl.UpdateMigratedDocumentsImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/migration/service/updateMigratedDocumentsImplTest-context.xml" })
public class updateMigratedDocumentsImplTest {
	private UpdateMigratedDocumentsImpl updateMigratedDocumentsImpl;
	private final static Logger logger = LoggerFactory
			.getLogger(updateMigratedDocumentsImplTest.class);

	@Test
	public void updateMigratedDocs() throws SAXException, IOException, ParserConfigurationException{
		FileInputStream fstream;
		try {
			fstream = new FileInputStream("C:\\Data\\8-5error list.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				logger.debug("processing: "+strLine);
				DocumentBuilderFactory domFactory = DocumentBuilderFactory
						.newInstance();
				domFactory.setNamespaceAware(true);
				try{
				DocumentBuilder builder = domFactory.newDocumentBuilder();
				Document doc = builder.parse("C:\\Data\\AriaXmls-8-5\\update\\"+strLine+".xml");
				String ariaXmlData = DomUtils.elementToString(doc, false);
				updateMigratedDocumentsImpl.updateMigratedDocumentsService(ariaXmlData);
				}catch(Exception e){
					logger.debug(strLine);
				}
			}

		}catch(Exception e){

			}


	}
	public UpdateMigratedDocumentsImpl getUpdateMigratedDocumentsImpl() {
		return updateMigratedDocumentsImpl;
	}
	@Autowired(required=true)
	public void setUpdateMigratedDocumentsImpl(
			UpdateMigratedDocumentsImpl updateMigratedDocumentsImpl) {
		this.updateMigratedDocumentsImpl = updateMigratedDocumentsImpl;
	}
}
