package edu.uams.clara.webapp.protocol.web.ajax;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/ajax/ProtocolDashboardAjaxControllerTest-context.xml"})
public class ProtocolDashboardAjaxControllerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolDashboardAjaxControllerTest.class);

	private ProtocolDashboardAjaxController protocolDashboardAjaxController;

	@Test
	public void testListProtocolForms() {
		//String forms = protocolDashboardAjaxController.listProtocolForms(1);

		//logger.debug(forms);
	}

	@Autowired(required=true)
	public void setProtocolDashboardAjaxController(
			ProtocolDashboardAjaxController protocolDashboardAjaxController) {
		this.protocolDashboardAjaxController = protocolDashboardAjaxController;
	}


	public ProtocolDashboardAjaxController getProtocolDashboardAjaxController() {
		return protocolDashboardAjaxController;
	}
}
