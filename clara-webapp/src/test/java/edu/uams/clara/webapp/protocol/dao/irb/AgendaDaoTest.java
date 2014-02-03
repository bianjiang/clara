package edu.uams.clara.webapp.protocol.dao.irb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.protocol.dao.businesslogicobject.AgendaStatusDao;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.AgendaStatus;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.enums.AgendaStatusEnum;
import edu.uams.clara.webapp.protocol.domain.irb.Agenda;
import edu.uams.clara.webapp.protocol.domain.irb.AgendaItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/irb/AgendaDaoTest-context.xml" })
public class AgendaDaoTest {

	private final static Logger logger = LoggerFactory
	.getLogger(AgendaDaoTest.class);

	private AgendaDao agendaDao;

	private AgendaItemDao agendaItemDao;

	private AgendaStatusDao agendaStatusDao;

	//@Test
	public void testListAgendasByStatuses() throws JsonGenerationException, JsonMappingException, IOException{
		List<AgendaStatusEnum> availableAgendaStatuses = new ArrayList<AgendaStatusEnum>(0);

		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_INCOMPLETE);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_PENDING_CHAIR_APPROVAL);
		availableAgendaStatuses.add(AgendaStatusEnum.AGENDA_APPROVED);

		List<Agenda> agendas = agendaDao.listAgendasByStatuses(availableAgendaStatuses);

		ObjectMapper objectMapper = new ObjectMapper();

		logger.debug(objectMapper.writeValueAsString(agendas));
	}

	//@Test
	public void testAgendaItem(){
		AgendaItem agendaItem = agendaItemDao.findById(107l);

		logger.debug("agendaItemId: " + agendaItem.getId() + "; xmlData: " + agendaItem.getXmlData());
	}

	@Test
	public void testAgendaStatusDao(){
		AgendaStatus agendaStatus = agendaStatusDao.getAgendaStatusByAgendaId(35l);

		logger.debug("status: " + agendaStatus.getAgendaStatus());
	}

	@Autowired(required=true)
	public void setAgendaDao(AgendaDao agendaDao) {
		this.agendaDao = agendaDao;
	}

	public AgendaDao getAgendaDao() {
		return agendaDao;
	}

	public AgendaItemDao getAgendaItemDao() {
		return agendaItemDao;
	}

	@Autowired(required=true)
	public void setAgendaItemDao(AgendaItemDao agendaItemDao) {
		this.agendaItemDao = agendaItemDao;
	}

	public AgendaStatusDao getAgendaStatusDao() {
		return agendaStatusDao;
	}

	@Autowired(required=true)
	public void setAgendaStatusDao(AgendaStatusDao agendaStatusDao) {
		this.agendaStatusDao = agendaStatusDao;
	}
}
