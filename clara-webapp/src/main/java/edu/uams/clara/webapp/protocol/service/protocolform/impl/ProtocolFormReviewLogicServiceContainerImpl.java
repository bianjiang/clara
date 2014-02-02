package edu.uams.clara.webapp.protocol.service.protocolform.impl;

import java.util.HashMap;
import java.util.Map;

import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicService;
import edu.uams.clara.webapp.protocol.service.protocolform.ProtocolFormReviewLogicServiceContainer;

public class ProtocolFormReviewLogicServiceContainerImpl implements
		ProtocolFormReviewLogicServiceContainer {
	private Map<String, ProtocolFormReviewLogicService> protocolFormReviewLogicServices = new HashMap<String, ProtocolFormReviewLogicService>();
	
	@Override
	public ProtocolFormReviewLogicService getProtocolFormReviewLogicService(
			String name) {
		// TODO Auto-generated method stub
		return protocolFormReviewLogicServices.get(name);
	}

	public Map<String, ProtocolFormReviewLogicService> getProtocolFormReviewLogicServices() {
		return protocolFormReviewLogicServices;
	}

	public void setProtocolFormReviewLogicServices(
			Map<String, ProtocolFormReviewLogicService> protocolFormReviewLogicServices) {
		this.protocolFormReviewLogicServices = protocolFormReviewLogicServices;
	}

}
