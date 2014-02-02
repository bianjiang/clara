package edu.uams.clara.webapp.protocol.web.irb.agenda.ajax;

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
import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;

@Controller
public class IRBAgendaXmlAjaxController {

	private final static Logger logger = LoggerFactory
	.getLogger(IRBAgendaXmlAjaxController.class);

	private AgendaDao agendaDao;

	@RequestMapping(value = "/ajax/agendas/{agendaId}/save-xml-data", method = RequestMethod.POST)
	public @ResponseBody
	String saveAgendaXmlData(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("xmlData") String xmlData) {
		
		if (xmlData == null || xmlData.isEmpty()){
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		try{
			Agenda agenda = agendaDao.findById(agendaId);
			agenda.setXmlData(xmlData);
			
			agenda = agendaDao.saveOrUpdate(agenda);
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to save agenda xmlData");
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/load-xml-data", method = RequestMethod.GET)
	public @ResponseBody
	String loadAgendaXmlData(
			@PathVariable("agendaId") long agendaId) {
		
		try{
			Agenda agenda = agendaDao.findById(agendaId);
			
			return agenda.getXmlData();
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to load agenda xmlData");
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/save-meeting-xml-data", method = RequestMethod.POST)
	public @ResponseBody
	String saveAgendaMeetingXmlData(
			@PathVariable("agendaId") long agendaId,
			@RequestParam("xmlData") String xmlData) {
		
		try{
			Agenda agenda = agendaDao.findById(agendaId);
			agenda.setMeetingXmlData(xmlData);
			
			agenda = agendaDao.saveOrUpdate(agenda);
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to save agenda xmlData");
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/load-meeting-xml-data", method = RequestMethod.GET)
	public @ResponseBody
	String loadAgendaMeetingXmlData(
			@PathVariable("agendaId") long agendaId) {
		
		try{
			Agenda agenda = agendaDao.findById(agendaId);
			
			return agenda.getMeetingXmlData();
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to load agenda xmlData");
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		
	}

	@Autowired(required = true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}
}
