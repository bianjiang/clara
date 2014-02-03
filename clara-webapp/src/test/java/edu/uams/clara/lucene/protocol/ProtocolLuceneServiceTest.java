package edu.uams.clara.lucene.protocol;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/lucene/protocol/ProtocolLuceneServiceTest-context.xml" })
public class ProtocolLuceneServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolLuceneServiceTest.class);

	private ProtocolLuceneService protocolLuceneService;

	private Jaxb2Marshaller jaxb2Marshaller;

	@Test
	public void testIndexer() throws ClassNotFoundException, NoSuchMethodException, SecurityException{
		//File xmlFile = new File("src/test/java/edu/uams/clara/lucene/common/mock-index-rules.xml");
		//Source source = new StreamSource(xmlFile);
		//IndexDocument indexDocument = (IndexDocument) jaxb2Marshaller.unmarshal(source);
		//logger.info("indexDocument: " + indexDocument.getIndexLocation());
	}

	public ProtocolLuceneService getProtocolLuceneService() {
		return protocolLuceneService;
	}

	@Autowired(required=true)
	public void setProtocolLuceneService(ProtocolLuceneService protocolLuceneService) {
		this.protocolLuceneService = protocolLuceneService;
	}

	public Jaxb2Marshaller getJaxb2Marshaller() {
		return jaxb2Marshaller;
	}

	@Autowired(required=true)
	public void setJaxb2Marshaller(Jaxb2Marshaller jaxb2Marshaller) {
		this.jaxb2Marshaller = jaxb2Marshaller;
	}
}
