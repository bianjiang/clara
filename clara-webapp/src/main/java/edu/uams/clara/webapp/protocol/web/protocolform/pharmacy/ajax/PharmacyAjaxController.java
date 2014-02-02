package edu.uams.clara.webapp.protocol.web.protocolform.pharmacy.ajax;

import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.uams.clara.webapp.common.util.XMLResponseHelper;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

@Controller
public class PharmacyAjaxController {
	
	private final static Logger logger = LoggerFactory
			.getLogger(PharmacyAjaxController.class);

	private ProtocolFormDao protocolFormDao;
	
	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	
	//private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	/**
	 * @ToDo security, and handle result not found exception...
	 * @param formXmlDataId
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/pharmacy/get", method = RequestMethod.GET)
	public @ResponseBody
	String getPharmacyXmlData(@PathVariable("protocolFormId") long protocolFormId) {
				
		ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);				
		ProtocolFormXmlData pfxd = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.PHARMACY);
		try{
			return pfxd.getXmlData();
		}catch(Exception ex){
			//ex.printStackTrace();
			logger.warn("no pharmacy form created for protocolFormId: " + protocolFormId);
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
	}
	
	
	@RequestMapping(value = "/ajax/protocols/{protocolId}/protocol-forms/{protocolFormId}/pharmacy/save", method = RequestMethod.POST)
	public @ResponseBody Source savePharmacyXmlData(@PathVariable("protocolFormId") long protocolFormId, @RequestParam("xmlData") String xmlData) {
		if (xmlData == null || xmlData.isEmpty()){
			return XMLResponseHelper.newErrorResponseStub("Error when saving the pharmacy!");
		}
		
		/* allow pharmacy reviewer to update pharmacy form after approval
		ProtocolFormCommitteeStatus pfcs = protocolFormCommitteeStatusDao.getLatestByCommitteeAndProtocolFormId(Committee.PHARMACY_REVIEW, protocolFormId);
		
		
		if (pfcs.getProtocolFormCommitteeStatus().equals(ProtocolFormCommitteeStatusEnum.APPROVED)) {
			return XMLResponseHelper.newErrorResponseStub("Since the pharmacy form has been approved, you cannot edit this form now!");
		}
		*/
		
		try{
			ProtocolForm protocolForm = protocolFormDao.findById(protocolFormId);
			ProtocolFormXmlData pharmacyXmlData = protocolForm.getTypedProtocolFormXmlDatas().get(ProtocolFormXmlDataType.PHARMACY);
					
			pharmacyXmlData.setXmlData(xmlData);
			
			if(pharmacyXmlData.getParent() == null){
				pharmacyXmlData.setParent(pharmacyXmlData);
			}
			
			pharmacyXmlData = protocolFormXmlDataDao.saveOrUpdate(pharmacyXmlData);
			return XMLResponseHelper.newSuccessResponseStube(Boolean.TRUE.toString());
		} catch (Exception ex){
				ex.printStackTrace();
				
				return XMLResponseHelper.newErrorResponseStub("Error when saving the pharmacy!");
		}
		
		
	}

	@Autowired(required=true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}


	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}


	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	/*
	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}
	*/
}
