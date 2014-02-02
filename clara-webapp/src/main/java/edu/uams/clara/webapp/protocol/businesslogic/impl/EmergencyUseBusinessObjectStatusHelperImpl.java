package edu.uams.clara.webapp.protocol.businesslogic.impl;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.businesslogic.ProtocolBusinessObjectStatusHelper;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.service.ProtocolFormService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class EmergencyUseBusinessObjectStatusHelperImpl  extends
ProtocolBusinessObjectStatusHelper {
	private final static Logger logger = LoggerFactory
			.getLogger(EmergencyUseBusinessObjectStatusHelperImpl.class);
	
	private ProtocolFormService protocolFormService;
	
	public EmergencyUseBusinessObjectStatusHelperImpl()
			throws ParserConfigurationException {
		super();
	}
	
	/*
	@Override
	public String checkCondition(Form form, Committee committee, User user,
			String action, String extraDataXml) {
		ProtocolForm protocolForm = (ProtocolForm) form;
		
		String formStatus = getFormStatus(form);
		
		if (formStatus.equals("")) return "";
		
		ProtocolFormStatusEnum protocolFormStatus = ProtocolFormStatusEnum
				.valueOf(formStatus);
		
		XmlProcessor xmlProcessor = getXmlProcessor();
		
		String condition = "";
		
		List<String> values = null;
		if (protocolFormStatus.equals(ProtocolFormStatusEnum.UNDER_IRB_OFFICE_REVIEW)){
			try{
				values = xmlProcessor.listElementStringValuesByPath("/emergency-use/ieu-or-eu", protocolForm.getMetaDataXml());
			} catch (Exception e){
				e.printStackTrace();
			}
			
			String formType = values!=null?values.get(0):"";
				
			if (formType.equals("intended-emergency-use")) condition = "INTENDED";
			if (formType.equals("emergency-use-follow-up-report")) condition = "FOLLOW-UP";
		}
		
		logger.debug("eu condition: " + condition);
		return condition;
	}*/
	
	@Override
	public String checkWorkflow(Form form, Committee committee, User user,
			String action, String extraDataXml){
		logger.debug("enter workflow...");
		String workflow = "";
		
		ProtocolForm protocolform = (ProtocolForm) form;
		
		ProtocolFormXmlData protocolXmlData = protocolform.getTypedProtocolFormXmlDatas().get(protocolform.getProtocolFormType().getDefaultProtocolFormXmlDataType());
		
		if (committee.equals(Committee.IRB_OFFICE) && action.equals("ACKNOWLEDGED")){
			workflow = protocolFormService.workFlowDetermination(protocolXmlData);
		}
		
		return workflow;
		
	}

	public ProtocolFormService getProtocolFormService() {
		return protocolFormService;
	}
	
	@Autowired(required=true)
	public void setProtocolFormService(ProtocolFormService protocolFormService) {
		this.protocolFormService = protocolFormService;
	}
}
