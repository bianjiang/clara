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
import edu.uams.clara.webapp.protocol.dao.irb.AgendaItemReviewerDao;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItemReviewer;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/protocol/web/irb/agenda/ajax/IRBAgendaItemAjaxControllerTest-context.xml"})
public class IRBAgendaItemAjaxControllerTest {

	private final static Logger logger = LoggerFactory
	.getLogger(IRBAgendaItemAjaxControllerTest.class);

	//private IRBAgendaItemAjaxController irbAgendaItemAjaxController;

	private AgendaDao agendaDao;

	private IRBReviewerDao irbReviewerDao;

	private AgendaItemReviewerDao agendaItemReviewerDao;

	@Test
	public void testRemoveIRBReviewerFromAgendaItem() throws JsonGenerationException, JsonMappingException, IOException{

		AgendaItemReviewer agendaItemReviewer = agendaItemReviewerDao.findById(1l);

		agendaItemReviewerDao.remove(agendaItemReviewer);

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

	@Autowired(required=true)
	public void setAgendaItemReviewerDao(AgendaItemReviewerDao agendaItemReviewerDao) {
		this.agendaItemReviewerDao = agendaItemReviewerDao;
	}


	public AgendaItemReviewerDao getAgendaItemReviewerDao() {
		return agendaItemReviewerDao;
	}





}
