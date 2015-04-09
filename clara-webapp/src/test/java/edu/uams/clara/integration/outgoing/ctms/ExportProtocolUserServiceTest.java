package edu.uams.clara.integration.outgoing.ctms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.outgoing.ctms.dao.ClaraFundingDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolDiseaseDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraProtocolUserDao;
import edu.uams.clara.integration.outgoing.ctms.dao.ClaraUserDao;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraProtocol;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraUser;
import edu.uams.clara.integration.outgoing.ctms.domain.ClaraUser.UserType;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/outgoing/ctms/ExportProtocolUserServiceTest-context.xml" })
public class ExportProtocolUserServiceTest {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ExportProtocolUserServiceTest.class);
	
	private ExportProtocolsToCTMSService exportProtocolUserToCTMSService;

	private UserDao userDao;

	private ClaraUserDao claraUserDao;

	private ClaraProtocolUserDao claraProtocolUserDao;
	
	private ClaraProtocolDao claraProtocolDao;

	private ProtocolDao protocolDao;
	
	private ClaraFundingDao claraFundingDao;
	private ClaraProtocolDiseaseDao claraProtocolDiseaseDao;
	
	/*
	public ClaraUser mockClaraUser(){
		User user = userDao.findById(1l);

		Person person = user.getPerson();

		String email = person.getEmail();
		String firstName = person.getFirstname();
		String lastName = person.getLastname();
		String middleName = person.getMiddlename();
		String phone = person.getWorkphone();

		ClaraUser claraUser = new ClaraUser();
		claraUser.setEmail(email);
		claraUser.setFirstName(firstName);
		claraUser.setLastName(lastName);
		claraUser.setMiddleName(middleName);
		claraUser.setPhone(phone);
		claraUser.setUserId(user.getId());
		claraUser.setUserType(UserType.CLARA);
		
		return claraUser;
	}*/
	
	//@Test
	public void testLinkedServer(){
		ClaraProtocol cp = claraProtocolDao.findByIRBNumber("99075");
		
		logger.debug("id: " + cp.getId() + " " + cp.getTitle());
	}

	//@Test
	public void testBuildQuery() {
		
		//ClaraUser claraUser = mockClaraUser();
		
		//claraUser = claraUserDao.insert(claraUser);
		
		//logger.debug("insert: " +  claraUser.getId());
		
		ClaraUser claraUser = claraUserDao.findById(1l);
		
		claraUser.setFirstName("Whatever");
		
		claraUser = claraUserDao.update(claraUser);
		logger.debug("updated: " +  claraUser.getId());
		
		logger.debug("after change: " + claraUser.getFirstName());		
	}
	
	private boolean shouldRun = false;
	
	@Test
	public void testUpdateIntegration() throws Exception {

		//logger.error(LogUtils.email(), "send a email");
		//logger.debug("about to run export to CTMS; but shouldRun? : " + this.isShouldRun());
		
		//ClaraProtocol claraProtocol = claraProtocolDao.findByIRBNumber("18");
		//logger.debug("id: " + claraProtocol.getId());
		long startTime = System.nanoTime();
		exportProtocolUserToCTMSService.updateCTMSIntegration("NOT_CLOSED");
		long endTime = System.nanoTime();
		
		logger.debug("Export to CTMS finished and it took: " + toSeconds(endTime - startTime) + " seconds");
	}
	
	
	//@Test
	public void test(){
		ClaraProtocol claraProtocol = new ClaraProtocol();
		claraProtocol =claraProtocolDao.findById(1);
		/*claraProtocol.setMetaDataXml(protocolDao.findById(3).getMetaDataXml());
		claraProtocolDao.saveOrUpdate(claraProtocol);
*/	}

	private double toSeconds(long nanoSeconds){
		return ((double) nanoSeconds) / 1000000000;  
	}

	public ClaraUserDao getClaraUserDao() {
		return claraUserDao;
	}

	@Autowired(required = true)
	public void setClaraUserDao(ClaraUserDao claraUserDao) {
		this.claraUserDao = claraUserDao;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required = true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}


	public ExportProtocolsToCTMSService getExportProtocolUserToCTMSService() {
		return exportProtocolUserToCTMSService;
	}

	@Autowired(required = true)
	public void setExportProtocolUserToCTMSService(
			ExportProtocolsToCTMSService exportProtocolUserToCTMSService) {
		this.exportProtocolUserToCTMSService = exportProtocolUserToCTMSService;
	}

	public ClaraProtocolDao getClaraProtocolDao() {
		return claraProtocolDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolDao(ClaraProtocolDao claraProtocolDao) {
		this.claraProtocolDao = claraProtocolDao;
	}

	public ClaraProtocolUserDao getClaraProtocolUserDao() {
		return claraProtocolUserDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolUserDao(ClaraProtocolUserDao claraProtocolUserDao) {
		this.claraProtocolUserDao = claraProtocolUserDao;
	}

	public boolean isShouldRun() {
		return shouldRun;
	}

	@Value("${scheduler.task.outgoing.should.run}")
	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public ClaraFundingDao getClaraFundingDao() {
		return claraFundingDao;
	}

	@Autowired(required = true)
	public void setClaraFundingDao(ClaraFundingDao claraFundingDao) {
		this.claraFundingDao = claraFundingDao;
	}

	public ClaraProtocolDiseaseDao getClaraProtocolDiseaseDao() {
		return claraProtocolDiseaseDao;
	}

	@Autowired(required = true)
	public void setClaraProtocolDiseaseDao(ClaraProtocolDiseaseDao claraProtocolDiseaseDao) {
		this.claraProtocolDiseaseDao = claraProtocolDiseaseDao;
	}


}
