package edu.uams.clara.webapp.protocol.service.history;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.history.BusinessObjectTrackService;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.ProtocolFormStatusEnum;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

public class ProtocolTrackService extends BusinessObjectTrackService<Protocol> {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolTrackService.class);
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;

	@Override
	public String getFormStatus(Map<String, Object> attributeRawValues) {
		ProtocolFormStatusEnum protocolFormStatus = null;
		
		if(attributeRawValues.get("FORM_STATUS") != null && !attributeRawValues.get("FORM_STATUS").toString().isEmpty() && !attributeRawValues.get("FORM_STATUS").equals("REMOVE_CURRENT_FORM_STATUS")){
			protocolFormStatus = ProtocolFormStatusEnum.valueOf(attributeRawValues.get("FORM_STATUS").toString());
		}
		
		return (protocolFormStatus!=null)?protocolFormStatus.getDescription():null;
	}

	@Override
	public String getOldFormStatus(Map<String, Object> attributeRawValues) {
		ProtocolFormStatusEnum oldProtocolFormStatus = null;
		if(attributeRawValues.get("OLD_FORM_STATUS") != null && !attributeRawValues.get("OLD_FORM_STATUS").toString().isEmpty()){
			oldProtocolFormStatus = ProtocolFormStatusEnum.valueOf(attributeRawValues.get("OLD_FORM_STATUS").toString());
		}
		
		return (oldProtocolFormStatus!=null)?oldProtocolFormStatus.getDescription():null;
	}

	@Override
	public Track getOrCreateTrackFromChildObject(Form form) {
		ProtocolForm protocolForm = (ProtocolForm) form;
		Track track = null;
		try{
			track = getOrCreateTrack("PROTOCOL", protocolForm.getProtocol().getId());
		} catch (Exception e){
			e.printStackTrace();
		}
	
		return track;
	}
	
	/*
	@Override
	public String getCommitteeNote(Form form, Committee committee) {
		ProtocolForm protocolForm = (ProtocolForm) form;
		
		ProtocolFormCommitteeStatus pfcss = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(committee, protocolForm.getId());
		
		return (pfcss!=null)?pfcss.getNote():"";
	}
	*/
	
	@Override
	public Map<String, String> getFormCommitteeStatusAttributeValues(Form form, Committee committee, 
			Map<String, String> attributeValues) {
		ProtocolForm protocolForm = (ProtocolForm) form; 
		
		try {
			ProtocolFormCommitteeStatus pfcss = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(committee, protocolForm.getId());
			
			attributeValues.put("{FORM_COMMITTEE_STATUS_ID}", String.valueOf(pfcss.getId()));
		} catch (Exception e) {
			//don't care
		}
		
		return attributeValues;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

}
