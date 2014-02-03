package edu.uams.clara.integration.incoming.sap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.integration.incoming.sap.dao.SAPUserUpdateDao;
import edu.uams.clara.integration.incoming.sap.domain.SAPUserUpdate;
import edu.uams.clara.webapp.common.dao.usercontext.PersonDao;
import edu.uams.clara.webapp.common.dao.usercontext.SAPUserDao;
import edu.uams.clara.webapp.common.domain.usercontext.Person;
import edu.uams.clara.webapp.common.domain.usercontext.SAPUser;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/integration/incoming/sap/addSAPUserTest-context.xml" })
public class addSAPUserTest {

	private final static Logger logger = LoggerFactory
			.getLogger(addSAPUserTest.class);

	private SAPUserDao sapUserDao;
	private PersonDao personDao;
	private ResourceLoader resourceLoader;
	private SAPUserUpdateDao sapUserUpdateDao;

	// @Test
	public void findPersonBySSN() {
		Person person = personDao.getPersonBySSN("******");
		logger.debug(person.getEmail());
	}

	// @Test
	public void addSAPUser() {
		try {
			String rawdata = null;

			String csvFileName = "budgetcode/Security.csv";

			Resource csvFile = resourceLoader.getResource(csvFileName);

			InputStream in = csvFile.getInputStream();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(in));
			while ((rawdata = reader.readLine()) != null) {
				String item[] = rawdata.split(",");

				SAPUser sapUser = new SAPUser();
				sapUser.setSap(item[0]);
				sapUser.setSsn(item[1]);
				sapUser.setColledgeOrgNo(item[2]);
				sapUser.setDepartmentOrgNo(item[3]);
				sapUser.setPositionNo(item[4]);
				sapUser.setBirthDate(item[5]);

				sapUserDao.saveOrUpdate(sapUser);

			}

		} catch (Exception EX) {
			EX.printStackTrace();

		}
	}

	//update sapuser
	@Test
	public void newUpdateSapUser(){
		//update existing users
		List<SAPUser> sapUserList =sapUserDao.findAll();
		for(int i =0;i<sapUserList.size();i++){
			SAPUser sapUser = sapUserList.get(i);
			Object[] sapUserUpdate =null;
			try{
				sapUserUpdate=sapUserUpdateDao.findSapUserBySapFromLinkedServer(sapUser.getSap());

			}catch(Exception e){

			}
			/*if(sapUserUpdate==null){
				sapUser.setRetired(true);
				sapUserDao.saveOrUpdate(sapUser);
			}*/
			if(sapUserUpdate!=null){
				if(sapUserUpdate[1]!=null){
					String ssn = (String)sapUserUpdate[1];
					sapUser.setSsn(ssn);
				}
				if(sapUserUpdate[2]!=null){
					String colledgeNo = (String)sapUserUpdate[2];
					sapUser.setColledgeOrgNo(colledgeNo);
				}
				if(sapUserUpdate[3]!=null){
					String deptNo = (String)sapUserUpdate[3];
					sapUser.setDepartmentOrgNo(deptNo);
				}
				sapUserDao.saveOrUpdate(sapUser);
			}

		}//end for

		//insert new users
		List<Object[]> sapUserUpdateList =sapUserUpdateDao.findAllSapUserBySapFromLinkedServer();
		for(int i =0;i<sapUserUpdateList.size();i++){
			Object[] sapUserUpdate = sapUserUpdateList.get(i);
			SAPUser sapUser =null;
			try{
				if(sapUserUpdate[0]!=null)
				{String sapID = (String)sapUserUpdate[0];
				int sapInt = Integer.valueOf(sapID);
				sapID = ""+sapInt;
				sapUser=sapUserDao.getSapUserBysapID(sapID);
				}
			}catch(Exception e){

			}
			if(sapUser==null){
				sapUser = new SAPUser();
				sapUser.setRetired(false);
				if(sapUserUpdate[0]!=null){
					String sapID = (String)sapUserUpdate[0];
					int sapInt = Integer.valueOf(sapID);
					sapID = ""+sapInt;
					sapUser.setSap(sapID);
				}
				if(sapUserUpdate[1]!=null){
					String ssn = (String)sapUserUpdate[1];
					sapUser.setSsn(ssn);
				}
				if(sapUserUpdate[2]!=null){
					String colledgeNo = (String)sapUserUpdate[2];
					sapUser.setColledgeOrgNo(colledgeNo);
				}
				if(sapUserUpdate[3]!=null){
					String deptNo = (String)sapUserUpdate[3];
					sapUser.setDepartmentOrgNo(deptNo);
				}
				sapUserDao.saveOrUpdate(sapUser);
			}
		}//end for

	}



	//update sapuser, old version, update based on txt file
	//@Test
	public void oldUpdateSapUser(){

		//update existing users
		List<SAPUser> sapUserList =sapUserDao.findAll();
		for(int i =0;i<sapUserList.size();i++){
			SAPUser sapUser = sapUserList.get(i);
			SAPUserUpdate sapUserUpdate =null;
			try{
				sapUserUpdate=sapUserUpdateDao.getSapUserBysapID(sapUser.getSap());
			}catch(Exception e){

			}
			if(sapUserUpdate==null){
				sapUser.setRetired(true);
				sapUserDao.saveOrUpdate(sapUser);
			}
			else{
				sapUser.setBirthDate(sapUserUpdate.getBirthDate());
				sapUser.setColledgeOrgNo(sapUserUpdate.getColledgeOrgNo());
				sapUser.setDepartmentOrgNo(sapUserUpdate.getDepartmentOrgNo());
				sapUser.setPositionNo(sapUserUpdate.getPositionNo());
				sapUser.setSsn(sapUserUpdate.getSsn());
				sapUserDao.saveOrUpdate(sapUser);
			}
		}//end for

		//add new sap users
		List<SAPUserUpdate> sapUserUpdateList =sapUserUpdateDao.findAll();
		for(int i =0;i<sapUserUpdateList.size();i++){
			SAPUserUpdate sapUserUpdate = sapUserUpdateList.get(i);
			SAPUser sapUser =null;
			try{
				sapUser=sapUserDao.getSapUserBysapID(sapUserUpdate.getSap());
			}catch(Exception e){

			}
			if(sapUser==null){
				sapUser = new SAPUser();
				sapUser.setRetired(false);
				sapUser.setColledgeOrgNo(sapUserUpdate.getColledgeOrgNo());
				sapUser.setDepartmentOrgNo(sapUserUpdate.getDepartmentOrgNo());
				sapUser.setPositionNo(sapUserUpdate.getPositionNo());
				sapUser.setSsn(sapUserUpdate.getSsn());
				sapUser.setSap(sapUserUpdate.getSap());
				sapUser.setBirthDate(sapUserUpdate.getBirthDate());
				sapUserDao.saveOrUpdate(sapUser);
			}
		}//end for
	}

	public SAPUserDao getSapUserDao() {
		return sapUserDao;
	}

	@Autowired(required = true)
	public void setSapUserDao(SAPUserDao sapUserDao) {
		this.sapUserDao = sapUserDao;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public PersonDao getPersonDao() {
		return personDao;
	}

	@Autowired(required = true)
	public void setPersonDao(PersonDao personDao) {
		this.personDao = personDao;
	}

	public SAPUserUpdateDao getSapUserUpdateDao() {
		return sapUserUpdateDao;
	}

	@Autowired(required = true)
	public void setSapUserUpdateDao(SAPUserUpdateDao sapUserUpdateDao) {
		this.sapUserUpdateDao = sapUserUpdateDao;
	}

}
