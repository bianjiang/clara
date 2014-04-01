package edu.uams.clara.integration.incoming.cititraining;

import java.io.IOException;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import edu.uams.clara.webapp.common.dao.usercontext.CitiMemberDao;
import edu.uams.clara.webapp.common.dao.usercontext.UserDao;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/cititraining/ImportCitiTrainingDataServiceTest-context.xml" })
public class ImportCitiTrainingDataServiceTest {
	
	private final static Logger logger = LoggerFactory
			.getLogger(ImportCitiTrainingDataServiceTest.class);
	
	private ImportCitiTrainingDataService importCitiTrainingDataService;
	
	private CitiMemberDao citiMemberDao;
	
	private UserDao userDao;
	
	private XmlProcessor xmlProcessor;
	
	
	//@Test
	public void testGetCitiMembersByUser() throws XPathExpressionException, SAXException, IOException{
		//String xmlData = "<metadata><is-trained>false</is-trained><citi-id>TopalogluBirgul</citi-id><nationality-changed>false</nationality-changed><citizenship-status /></metadata>";
		User user = userDao.findById(72l); // Bigural
		
		logger.debug(user.getProfile());
		
		List<CitiMember> citiMembers = citiMemberDao.listCitiMemberByUser(user);
		
		for(CitiMember citiMember:citiMembers){
			logger.debug("citiMember:" + citiMember.getCompletionReportNumber());
		}
		
		
	}
	
	@Test 
	public void testMatchedUserForImportedCitimember(){
		List<User> users = userDao.findAll();
		List<User> matchedUsers = Lists.newArrayList();
		int matchedUserCount =0;
		for(User user: users){
			List<CitiMember> citiMembers = citiMemberDao.listCitiMemberByUser(user);
			for(CitiMember citiMember :citiMembers){
				long citiMemberID = citiMember.getId();
				if(citiMemberID>15286){
					matchedUserCount++;
					if(!matchedUsers.contains(user)){
					matchedUsers.add(user);
					}
					break;
				}
			}
		}
		logger.debug("Matched Users: "+matchedUserCount);
		logger.debug("Matched Users: "+matchedUsers.size());
		/*for(User user:matchedUsers){
			logger.debug(user.getId()+"");
		}*/
	}
	
	//@Test
	public void testCitiDataService(){
		importCitiTrainingDataService.run();
	}
	

	public ImportCitiTrainingDataService getImportCitiTrainingDataService() {
		return importCitiTrainingDataService;
	}
	
	@Autowired(required=true)
	public void setImportCitiTrainingDataService(
			ImportCitiTrainingDataService importCitiTrainingDataService) {
		this.importCitiTrainingDataService = importCitiTrainingDataService;
	}


	public UserDao getUserDao() {
		return userDao;
	}

	@Autowired(required=true)
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public CitiMemberDao getCitiMemberDao() {
		return citiMemberDao;
	}

	@Autowired(required=true)
	public void setCitiMemberDao(CitiMemberDao citiMemberDao) {
		this.citiMemberDao = citiMemberDao;
	}
	
	
	
}
