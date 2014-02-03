package edu.uams.clara.webapp.common.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/service/EmailServiceTest-context.xml" })
public class PatternMatchTest {
	private final static Logger logger = LoggerFactory
			.getLogger(PatternMatchTest.class);

	private Pattern formXmlDataPattern = Pattern
			.compile("(?:ajax)?\\/(protocols|contracts)\\/(\\d+)\\/((protocol|contract)\\-forms)\\/(\\d+)(?:\\/.*?)?\\/((protocol|contract)-form-xml-datas)\\/(\\d+)\\/");

	@Test
	public void testPatternMatcher(){
		String url = "http://localhost:8080/clara-webapp/ajax/protocols/201467/protocol-forms/1489/protocol-form-xml-datas/1917/update";
		String url1 = "http://localhost:8080/clara-webapp/protocols/201467/protocol-forms/1489/new-submission/protocol-form-xml-datas/1917/basic-details";

		Matcher m = formXmlDataPattern.matcher(url);
		Matcher m1 = formXmlDataPattern.matcher(url1);

		logger.debug("url match or not: " + m.find());
		logger.debug("url1 match or not: " + m1.find());

		long formId = Long.valueOf(m1.group(5));
		String object = m1.group(1);

		logger.debug("formId: " + formId + ", object: " + object);

	}
}
