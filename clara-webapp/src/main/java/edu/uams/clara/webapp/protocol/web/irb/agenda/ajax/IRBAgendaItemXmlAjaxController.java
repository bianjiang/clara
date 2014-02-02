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
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemDao;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;

@Controller
public class IRBAgendaItemXmlAjaxController {

	private final static Logger logger = LoggerFactory
			.getLogger(IRBAgendaItemXmlAjaxController.class);

	private AgendaItemDao agendaItemDao;

	/**
	 * simple replace the xmlData with the new one... everything will be sent in at once, so 
	 * there is no need to do xml.merge...
	 * @param agendaId
	 * @param agendaItemId
	 * @param xmlData
	 * @return
	 */
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/save-xml-data", method = RequestMethod.POST)
	public @ResponseBody
	String saveAgendaItemXmlData(
			@PathVariable("agendaId") long agendaId,
			@PathVariable("agendaItemId") long agendaItemId,
			@RequestParam("xmlData") String xmlData) {
		
		try{
			AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
			agendaItem.setXmlData(xmlData);
			
			agendaItem = agendaItemDao.saveOrUpdate(agendaItem);
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to save agendaItem xmlData");
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		return XMLResponseHelper.xmlResult(Boolean.TRUE);
	}
	
	@RequestMapping(value = "/ajax/agendas/{agendaId}/agenda-items/{agendaItemId}/load-xml-data", method = RequestMethod.POST)
	public @ResponseBody
	String loadAgendaXmlData(
			@PathVariable("agendaId") long agendaId,
			@PathVariable("agendaItemId") long agendaItemId) {
		
		try{
			AgendaItem agendaItem = agendaItemDao.findById(agendaItemId);
			
			return agendaItem.getXmlData();
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to load agendaItem xmlData");
			return XMLResponseHelper.xmlResult(Boolean.FALSE);
		}
		
		
	}

	@Autowired(required = true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}


	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

}
