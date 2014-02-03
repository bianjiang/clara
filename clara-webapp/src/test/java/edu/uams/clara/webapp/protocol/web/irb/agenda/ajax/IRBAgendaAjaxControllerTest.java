package edu.uams.clara.webapp.protocol.web.irb.agenda.ajax;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.uams.clara.webapp.protocol.dao.irb.AgendaDao;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/irb/agenda/ajax/IRBAgendaAjaxControllerTest-context.xml"})
public class IRBAgendaAjaxControllerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(IRBAgendaAjaxControllerTest.class);

	private IRBAgendaAjaxController irbAgendaAjaxController;

	private AgendaDao agendaDao;

	private IRBReviewerDao irbReviewerDao;

	@Test
	public void testListAgendaIRBReviewers() throws JsonGenerationException, JsonMappingException, IOException{

		/*
		logger.debug("start");
		Agenda agenda = agendaDao.findById(1l);

		logger.debug("" + agenda.getIrbRoster());

		List<IRBReviewer> irbReviewers = irbReviewerDao.listIRBReviewersByIRBRoster(agenda.getIrbRoster());
		*/
		/*
		List<IRBReviewer> irbReviewers = irbAgendaAjaxController.listAgendaIRBReviewers(1l);

		ObjectMapper objectMapper = new ObjectMapper();

		logger.debug(objectMapper.writeValueAsString(irbReviewers));
		*/
	}

	@Autowired(required=true)
	public void setIrbAgendaAjaxController(IRBAgendaAjaxController irbAgendaAjaxController) {
		this.irbAgendaAjaxController = irbAgendaAjaxController;
	}

	public IRBAgendaAjaxController getIrbAgendaAjaxController() {
		return irbAgendaAjaxController;
	}

	@Autowired(required=true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	@Autowired(required=true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}



}
