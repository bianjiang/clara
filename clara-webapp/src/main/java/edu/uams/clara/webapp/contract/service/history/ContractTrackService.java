package edu.uams.clara.webapp.contract.service.history;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.common.domain.form.Form;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.common.service.history.BusinessObjectTrackService;
import edu.uams.clara.webapp.contract.dao.businesslogicobject.ContractFormCommitteeStatusDao;
import edu.uams.clara.webapp.contract.domain.Contract;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.ContractFormCommitteeStatus;
import edu.uams.clara.webapp.contract.domain.businesslogicobject.enums.ContractFormStatusEnum;
import edu.uams.clara.webapp.contract.domain.contractform.ContractForm;

public class ContractTrackService extends BusinessObjectTrackService<Contract> {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ContractTrackService.class);
	
	private ContractFormCommitteeStatusDao contractFormCommitteeStatusDao;

	@Override
	public String getFormStatus(Map<String, Object> attributeRawValues) {
		ContractFormStatusEnum contractFormStatus = null;
		
		if(attributeRawValues.get("FORM_STATUS") != null && !attributeRawValues.get("FORM_STATUS").toString().isEmpty()){
			contractFormStatus = ContractFormStatusEnum.valueOf(attributeRawValues.get("FORM_STATUS").toString());
		}
		
		return (contractFormStatus!=null)?contractFormStatus.getDescription():null;
	}

	@Override
	public String getOldFormStatus(Map<String, Object> attributeRawValues) {
		ContractFormStatusEnum oldContractFormStatus = null;
		if(attributeRawValues.get("OLD_FORM_STATUS") != null && !attributeRawValues.get("OLD_FORM_STATUS").toString().isEmpty()){
			oldContractFormStatus = ContractFormStatusEnum.valueOf(attributeRawValues.get("OLD_FORM_STATUS").toString());
		}
		
		return (oldContractFormStatus!=null)?oldContractFormStatus.getDescription():null;
	}

	@Override
	public Track getOrCreateTrackFromChildObject(Form form) {
		ContractForm contractForm = (ContractForm) form;
		Track track = null;
		try{
			track = getOrCreateTrack("CONTRACT", contractForm.getContract()
					.getId());
		} catch (Exception e){
			e.printStackTrace();
		}
	
		return track;
	}
	
	/*
	@Override
	public String getCommitteeNote(Form form, Committee committee) {
		ContractForm contractForm = (ContractForm) form;
		
		ContractFormCommitteeStatus cfscs = contractFormCommitteeStatusDao.getLatestByCommitteeAndContractFormId(committee, contractForm.getId());
		
		return (cfscs!=null)?cfscs.getNote():"";
	}
	*/
	
	@Override
	public Map<String, String> getFormCommitteeStatusAttributeValues(Form form,
			Committee committee, Map<String, String> attributeValues) {
		ContractForm contractForm = (ContractForm) form;

		try {
			ContractFormCommitteeStatus cfscs = contractFormCommitteeStatusDao.getLatestByCommitteeAndContractFormId(committee, contractForm.getId());

			attributeValues.put("{FORM_COMMITTEE_STATUS_ID}", String.valueOf(cfscs.getId()));
		} catch (Exception e) {
			//don't care
		}
		
		return attributeValues;
	}

	public ContractFormCommitteeStatusDao getContractFormCommitteeStatusDao() {
		return contractFormCommitteeStatusDao;
	}

	@Autowired(required = true)
	public void setContractFormCommitteeStatusDao(
			ContractFormCommitteeStatusDao contractFormCommitteeStatusDao) {
		this.contractFormCommitteeStatusDao = contractFormCommitteeStatusDao;
	}

}
