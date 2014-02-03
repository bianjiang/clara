package edu.uams.clara.webapp.xml.processor;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.budget.code.HospitalChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianChargeProcedureDao;
import edu.uams.clara.webapp.protocol.dao.budget.code.PhysicianLocationCodeDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.xml.processor.impl.BudgetXmlCostUpdateImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/xml/processor/budgetXmlCostUpdateTest-context.xml" })
public class budgetXmlCostUpdateTest {

	private final static Logger logger = LoggerFactory
			.getLogger(budgetXmlCostUpdateTest.class);

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private BudgetXmlCostUpdateImpl budgetXmlCostUpdateImpl;


	@Test
	public void costUpdate() {

		ProtocolFormXmlData BudgetXmlData = getProtocolFormXmlDataDao().findById(1114);

		String BudgetXml = BudgetXmlData.getXmlData();

		BudgetXml=budgetXmlCostUpdateImpl.updateCost(BudgetXml);
	    logger.debug(BudgetXml);

	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public BudgetXmlCostUpdateImpl getBudgetXmlCostUpdateImpl() {
		return budgetXmlCostUpdateImpl;
	}

	@Autowired(required = true)
	public void setBudgetXmlCostUpdateImpl(BudgetXmlCostUpdateImpl budgetXmlCostUpdateImpl) {
		this.budgetXmlCostUpdateImpl = budgetXmlCostUpdateImpl;
	}


}
