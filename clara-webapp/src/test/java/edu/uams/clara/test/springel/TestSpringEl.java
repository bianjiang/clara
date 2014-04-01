package edu.uams.clara.test.springel;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;


public class TestSpringEl {

	private final static Logger logger = LoggerFactory
			.getLogger(XmlProcessor.class);
	
	final static String LOG4J_CHECK_CLASS = "org.apache.log4j.Logger";
    final static String LOG4J2_CHECK_CLASS = "org.apache.logging.log4j.Logger";
	@Test
	public void testSpringEl() throws ClassNotFoundException{
		
		try {
			Class.forName( LOG4J_CHECK_CLASS );
		} catch (ClassNotFoundException e) {
			// don't care, test the second one
			try {
				Class.forName( LOG4J2_CHECK_CLASS );
			} catch (ClassNotFoundException ex) {
				throw ex;
			}
		}
		
		/*
		ExpressionParser parser = new SpelExpressionParser();
		
		String queueIdentifier = "QUEUE_BUDGET_REVIEWER";
		User user = new User();
		user.setId(1l);
		boolean showHistory = false;
		
		NamedScopeEvaluationContext context = new NamedScopeEvaluationContext();
		context.addContext("user", user);
		context.addContext("queueIdentifier", queueIdentifier);
		context.addContext("showHistory", showHistory);
		
		
		Expression exp = parser.parseExpression("T(edu.uams.clara.webapp.queue.service.QueueService).formInQueueByUserHashCode(queueIdentifier, user, showHistory)");
		String msg = exp.getValue(context, String.class);
		logger.debug(msg);
		*/
	}
}
