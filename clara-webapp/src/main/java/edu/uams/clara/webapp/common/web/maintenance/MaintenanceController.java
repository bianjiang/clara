package edu.uams.clara.webapp.common.web.maintenance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.uams.clara.webapp.common.dao.history.TrackDao;
import edu.uams.clara.webapp.common.domain.history.Track;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormCommitteeStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolFormStatusDao;
import edu.uams.clara.webapp.protocol.dao.businesslogicobject.ProtocolStatusDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolStatus;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;

@Controller
public class MaintenanceController {
	private ProtocolDao protocolDao;
	
	private ProtocolFormDao protocolFormDao;
	
	private ProtocolStatusDao protocolStatusDao;
	
	private ProtocolFormStatusDao protocolFormStatusDao;
	
	private ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao;
	
	private TrackDao trackDao;
	
	/**
	 * @author YuFan
	 * This page is used to show the reset form page for admin.
	 * 
	 */
	@RequestMapping(value = "/admin/super/reset-form", method = RequestMethod.GET)
	public String resetForm(
			@RequestParam("formId") long formId,
			ModelMap modelMap) {
		ProtocolForm protocolForm = protocolFormDao.findById(formId);
		
		Protocol protocol = protocolForm.getProtocol();
		
		Track track = trackDao.getTrackByTypeAndRefObjectID("PROTOCOL", protocol.getId());
		
		List<ProtocolStatus> protocolStatusLst = protocolStatusDao.listProtocolStatusesByProtocolId(protocol.getId());
		
		List<ProtocolFormStatus> protocolFormStatusLst = protocolFormStatusDao.getAllProtocolFormStatusByParentFormId(formId);
		
		List<ProtocolFormCommitteeStatus> protocolFormCommitteeStatusLst = protocolFormCommitteeStatusDao.listAllByProtocolFormId(formId);
		
		modelMap.put("protocolMetaData", protocol.getMetaDataXml());
		modelMap.put("protocolFormMetaData", protocolForm.getMetaDataXml());
		modelMap.put("trackXmlData", track.getXmlData());
		modelMap.put("protocolStatusLst", protocolStatusLst);
		modelMap.put("protocolFormStatusLst", protocolFormStatusLst);
		modelMap.put("protocolFormCommitteeStatusLst", protocolFormCommitteeStatusLst);
		
		return "super/resetform";
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

	public ProtocolStatusDao getProtocolStatusDao() {
		return protocolStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolStatusDao(ProtocolStatusDao protocolStatusDao) {
		this.protocolStatusDao = protocolStatusDao;
	}

	public ProtocolFormStatusDao getProtocolFormStatusDao() {
		return protocolFormStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormStatusDao(ProtocolFormStatusDao protocolFormStatusDao) {
		this.protocolFormStatusDao = protocolFormStatusDao;
	}

	public ProtocolFormCommitteeStatusDao getProtocolFormCommitteeStatusDao() {
		return protocolFormCommitteeStatusDao;
	}
	
	@Autowired(required = true)
	public void setProtocolFormCommitteeStatusDao(
			ProtocolFormCommitteeStatusDao protocolFormCommitteeStatusDao) {
		this.protocolFormCommitteeStatusDao = protocolFormCommitteeStatusDao;
	}

	public TrackDao getTrackDao() {
		return trackDao;
	}
	
	@Autowired(required = true)
	public void setTrackDao(TrackDao trackDao) {
		this.trackDao = trackDao;
	}
}
