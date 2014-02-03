package edu.uams.clara.lucene.common;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Iterables;
import com.ibm.icu.util.BytesTrie.Iterator;

import edu.uams.clara.lucene.common.indexrules.IndexDocument;
import edu.uams.clara.lucene.common.indexrules.IndexRule;
import edu.uams.clara.lucene.common.indexrules.SourceField;
import edu.uams.clara.lucene.common.indexrules.SourceField.SourceFieldDataType;
import edu.uams.clara.webapp.protocol.domain.Protocol;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/lucene/common/XmlHandlerTest-context.xml" })
public class XmlHandlerTest {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlHandlerTest.class);

	/**
	 * Marshaller is implementation independent interface
	 */

	private Jaxb2Marshaller jaxb2Marshaller;

	private final SourceField mockSourceField(){
		final SourceField sourceField = new SourceField("/protocol/title", Protocol.class, "metaDataXml", SourceFieldDataType.XML, "/protocol/title/text()", String.class);
		return sourceField;
	}

	private final Set<IndexRule> mockIndexRules(){
		final SourceField title = new SourceField("/protocol/title", Protocol.class, "metaDataXml", SourceFieldDataType.XML, "/protocol/title/text()", String.class);
		final IndexRule titleIndexRule = new IndexRule("/protocol/title", TextField.class, title);

		final SourceField piUserId = new SourceField("/protocol/pi@userId", Protocol.class, "metaDataXml", SourceFieldDataType.XML, "/protocol/staffs/staff/user[roles/role=\"Principal Investigator\"]@id", Long.class);
		final IndexRule piUserIdIndexRule = new IndexRule("/protocol/pi/@userId", LongField.class, piUserId);

		final SourceField piFirst = new SourceField("/protocol/pi@firstname", Protocol.class, "metaDataXml", SourceFieldDataType.XML, "/protocol/staffs/staff/user[roles/role=\"Principal Investigator\"]/firstname", String.class);
		final SourceField piLast = new SourceField("/protocol/pi@lastname", Protocol.class, "metaDataXml", SourceFieldDataType.XML, "/protocol/staffs/staff/user[roles/role=\"Principal Investigator\"]/lastname", String.class);

		final IndexRule piNamesIndexRule = new IndexRule("/protocol/pi/@name", TextField.class, "{first} {last}", piFirst, piLast);

		final Set<IndexRule> indexRules = new HashSet<IndexRule>();
		indexRules.add(titleIndexRule);
		indexRules.add(piUserIdIndexRule);
		indexRules.add(piNamesIndexRule);

		return indexRules;
	}


	@XmlRootElement( name="root" )
	private static class XMLFakeRoot {

		private List<IndexRule> indexRules;

		@XmlElementWrapper(name="index-rules")
		@XmlElement(name="index-rule")
		public List<IndexRule> getIndexRules() {
			return indexRules;
		}

		public void setIndexRules(List<IndexRule> indexRules) {
			this.indexRules = indexRules;
		}

	}

	//@Test
	public void testJAXB2Marshalling(){


		//Writer outWriter = new StringWriter();
		//StreamResult result = new StreamResult( outWriter );
		File out = new File("src/test/java/edu/uams/clara/lucene/common/mock-index-rules.xml");
		logger.info(out.getAbsolutePath());
		StreamResult result = new StreamResult(out);
		try {
			IndexDocument indexDocument = new IndexDocument("~/lucene/test/", mockIndexRules());
			//xmlRoot.setIndexRules(mockIndexRules());
			jaxb2Marshaller.marshal(indexDocument, result);
		} catch (XmlMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testJAXB2UnMarshalling(){

		File xmlFile = new File("src/test/java/edu/uams/clara/lucene/common/mock-index-rules.xml");
		Source source = new StreamSource(xmlFile);
		IndexDocument indexDocument = (IndexDocument) jaxb2Marshaller.unmarshal(source);

		logger.info(Iterables.getLast(indexDocument.getIndexRules()).getIdentifier());
	}

	public Jaxb2Marshaller getJaxb2Marshaller() {
		return jaxb2Marshaller;
	}

	@Autowired(required=true)
	public void setJaxb2Marshaller(Jaxb2Marshaller jaxb2Marshaller) {
		this.jaxb2Marshaller = jaxb2Marshaller;
	}
}
