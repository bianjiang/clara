package edu.uams.clara.webapp.protocol.web.queue.ajax;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.queue.web.ajax.IRBOfficeReviewQueueAjaxController;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/queue/ajax/IRBOfficeReviewQueueAjaxControllerTest-context.xml"})
public class IRBOfficeReviewQueueAjaxControllerTest {
	
	private final static Logger logger = LoggerFactory
	.getLogger(IRBOfficeReviewQueueAjaxControllerTest.class);
	
	
	private IRBOfficeReviewQueueAjaxController irbOfficeReviewQueueAjaxController;


	@Test
	public void textProcessReviewItem(){
		//test Expedited item
		
		//irbOfficeReviewQueueAjaxController.processReviewItem(81l, IRBOfficeReviewAction.EXPEDITED, 1l, 1l);
	}

	@Autowired(required=true)
	public void setIrbOfficeReviewQueueAjaxController(
			IRBOfficeReviewQueueAjaxController irbOfficeReviewQueueAjaxController) {
		this.irbOfficeReviewQueueAjaxController = irbOfficeReviewQueueAjaxController;
	}


	public IRBOfficeReviewQueueAjaxController getIrbOfficeReviewQueueAjaxController() {
		return irbOfficeReviewQueueAjaxController;
	}
	
	
}
