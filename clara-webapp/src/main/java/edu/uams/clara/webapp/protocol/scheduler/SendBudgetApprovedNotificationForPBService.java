package edu.uams.clara.webapp.protocol.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.uams.clara.core.util.xml.XmlHandler;
import edu.uams.clara.core.util.xml.XmlHandlerFactory;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.service.email.ProtocolEmailService;

@Service
public class SendBudgetApprovedNotificationForPBService {

	private final static Logger logger = LoggerFactory
			.getLogger(SendBudgetApprovedNotificationForPBService.class);
	
	private ProtocolEmailService protocolEmailService;
	
	private boolean shouldRun = false;
	
	public void sendBudgetApprovedNotification(Protocol p) {
		// If run on test, comment out this part.
		
		if(!this.isShouldRun()) return;
		
		boolean budgetExist =true;
		try {
			XmlHandler xmlHandler = XmlHandlerFactory.newXmlHandler();
			String budgetCreated = xmlHandler.getSingleStringValueByXPath(p.getMetaDataXml(), "/protocol/budget-created");
			if(!budgetCreated.equals("y")){
				budgetExist=false;
			}
		} catch (Exception e) {
		}
		
		if(budgetExist){
		try {
			protocolEmailService.sendProtocolNotification(p, null, null,
					null, "BUDGET_APPROVED_NOTIFICATION_FOR_PB", "", null, null,
					null, "", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		}

	}

	public ProtocolEmailService getProtocolEmailService() {
		return protocolEmailService;
	}

	@Autowired(required=true)
	public void setProtocolEmailService(ProtocolEmailService protocolEmailService) {
		this.protocolEmailService = protocolEmailService;
	}
	
	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}
}
