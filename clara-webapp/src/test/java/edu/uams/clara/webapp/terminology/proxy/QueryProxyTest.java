package edu.uams.clara.webapp.terminology.proxy;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.terminology.proxy.domain.enums.CodeType;
import edu.uams.clara.webapp.terminology.proxy.service.TerminologyCodeQueryProxy;
import edu.uams.clara.webapp.terminology.proxy.service.TerminologyCodeQueryProxy.ResultFormat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/terminology/proxy/QueryProxyTest.xml"})
public class QueryProxyTest {

	private final static Logger logger = LoggerFactory
			.getLogger(QueryProxyTest.class);

	private TerminologyCodeQueryProxy terminologyCodeQueryProxy;

	//@Test
			public void CptCodeMap(){
				String result = terminologyCodeQueryProxy.cptAutoMap("82565");
				logger.debug(result);
			}
	//@Test
	public void queryByIdTest(){
		String result = terminologyCodeQueryProxy.queryByContent("163141003", CodeType.SNOMED_CT);
		logger.debug(result);
	}
	//@Test
	public void queryBuWordsTest(){
		String result = terminologyCodeQueryProxy.queryByContent("blood",CodeType.SNOMED_CT);
		logger.debug(result);
	}

	@Test
	public void listCodeByTypeAndIdentifier(){
		String result = terminologyCodeQueryProxy.listCodeByTypeAndIdentifier("DOID:104", CodeType.DISEASE_ONTOLOGY, ResultFormat.JSON_TREE);
		logger.debug(result);
	}

	//@Test
	public void listCodeByTypeAndName(){
		String result = terminologyCodeQueryProxy.listCodeByTypeAndName("Health", CodeType.CONDITION, ResultFormat.JSON_TREE);
		logger.debug(result);
	}

	//@Test
	public void listChildrenByTypeAndIdentifier(){
		String result = terminologyCodeQueryProxy.listChildrenByTypeAndIdentifier("ConID:0000027", CodeType.CONDITION, ResultFormat.JSON_TREE);
		logger.debug(result);
	}

	public TerminologyCodeQueryProxy getTerminologyCodeQueryProxy() {
		return terminologyCodeQueryProxy;
	}
	@Autowired(required=true)
	public void setTerminologyCodeQueryProxy(TerminologyCodeQueryProxy terminologyCodeQueryProxy) {
		this.terminologyCodeQueryProxy = terminologyCodeQueryProxy;
	}
}
