package edu.uams.clara.webapp.xml.processor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolForm;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.BudgetXmlExportService.BudgetDocumentType;
import edu.uams.clara.webapp.xml.processor.impl.BudgetXmlExportServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/xml/processor/budgetXmlExportServiceTest-context.xml" })
public class budgetXmlExportServiceTest {

	private final static Logger logger = LoggerFactory
			.getLogger(budgetXmlExportServiceTest.class);
	private BudgetXmlExportServiceImpl budgetExport;

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private ProtocolFormDao protocolFormDao;

	@Test
	public void test() {
		long protocolFormId= 16148;
		
		
		ProtocolFormXmlData protocolFormXmlData =protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.BUDGET);
		ProtocolFormXmlData budgetXmlData = getProtocolFormXmlDataDao()
				.findById(protocolFormXmlData.getId());
		String budgetXml = budgetXmlData.getXmlData();
	
		budgetExport.generateBudgetExcelDocument(budgetXml, BudgetDocumentType.FULL,protocolFormId);
		logger.debug("protocolid "+budgetXmlData.getProtocolForm().getProtocol().getId());
		
	 }
	 
	 
	// @Test
	 public void multipleTest(){
		 logger.debug("begin");
		 List<ProtocolForm> protocolFormList = protocolFormDao.findAll();
		 List<Long> exceptionList = new ArrayList<Long>();
		 for(int i=0;i<protocolFormList.size();i++){
/*			 if(protocolFormList.get(i).getId()==9739){
			 continue;}*/
			 try{
				
				 long protocolFormId =protocolFormList.get(i).getId();
				 if(protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.BUDGET)!=null){
					 logger.debug("processing "+protocolFormId);
				 ProtocolFormXmlData protocolFormXmlData =protocolFormXmlDataDao.getLastProtocolFormXmlDataByProtocolFormIdAndType(protocolFormId, ProtocolFormXmlDataType.BUDGET);
					ProtocolFormXmlData budgetXmlData = getProtocolFormXmlDataDao()
							.findById(protocolFormXmlData.getId());
					String budgetXml = budgetXmlData.getXmlData();
				
					budgetExport.generateBudgetExcelDocument(budgetXml, BudgetDocumentType.FULL,protocolFormId);
				 }
			 }catch(Exception e){
				 logger.debug(""+protocolFormList.get(i).getId());
				 exceptionList.add(protocolFormList.get(i).getId());
				 e.printStackTrace();
			 }
		 }
		 
		 for(int i=0;i<exceptionList.size();i++){
			 logger.debug("error "+exceptionList.get(i));
		 }
		 
			
	 }

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	public BudgetXmlExportServiceImpl getBudgetExport() {
		return budgetExport;
	}

	@Autowired(required = true)
	public void setBudgetExport(BudgetXmlExportServiceImpl budgetExport) {
		this.budgetExport = budgetExport;
	}


	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}

	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

}
