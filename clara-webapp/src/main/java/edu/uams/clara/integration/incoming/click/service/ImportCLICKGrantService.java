package edu.uams.clara.integration.incoming.click.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uams.clara.integration.incoming.click.dao.ClickGrantDao;
import edu.uams.clara.webapp.protocol.dao.thing.GrantDao;
import edu.uams.clara.webapp.protocol.domain.thing.Grant;

@Service
public class ImportCLICKGrantService {
	private final static Logger logger = LoggerFactory
			.getLogger(ImportCLICKGrantService.class);
	
	private GrantDao grantDao;
	
	private ClickGrantDao clickGrantDao;
	
	public void importProjectToClickGrant(){
		List<Object[]> clickProjects = clickGrantDao.listAllClickProjects();
		for(Object[] clickProject:clickProjects){
			if(clickProject==null){
				continue;
			}
			
			try{
			
			String agency = (String) clickProject[0];
			String title = (String) clickProject[1];
			String piId = (String) clickProject[2];
			String piName = (String) clickProject[3];
			String prnfull = (String) clickProject[4];
			String status = (String) clickProject[5];
			String startdateStr = (String) clickProject[6];
			String enddateStr = (String) clickProject[7];
			
			String prn = prnfull.substring(5, 10);
			//logger.debug(prn);
			
			//check to see if valid project
			if(prn.isEmpty()||piId.isEmpty()){
				continue;
			}
			
			Grant grant = new Grant();
			try{
				grant = grantDao.findGrantByFullPRN(prnfull);
			}catch(Exception ex){
				//not found don't care
			}
			
			grant.setCreated(new Date());
			grant.setFundingAgency(agency);
			grant.setPiId(piId);
			grant.setPiName(piName);
			grant.setPrn(prn);
			grant.setFullprn(prnfull);
			grant.setRetired(false);
			grant.setStatus(status);
			grant.setGrantTitle(title);
			
			try{
				Date startDate = new SimpleDateFormat("MM-dd-yyyy").parse(startdateStr);
				grant.setStartDate(startDate);
			}catch(Exception e){
				//
			}
			
			try{
				Date endDate = new SimpleDateFormat("MM-dd-yyyy").parse(enddateStr);
				grant.setEndDate(endDate);
			}catch(Exception e){
				//
			}
			
			grant = grantDao.saveOrUpdate(grant);			
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		
	}

	public GrantDao getGrantDao() {
		return grantDao;
	}

	@Autowired(required=true)
	public void setGrantDao(GrantDao grantDao) {
		this.grantDao = grantDao;
	}

	public ClickGrantDao getClickGrantDao() {
		return clickGrantDao;
	}
	@Autowired(required=true)
	public void setClickGrantDao(ClickGrantDao clickGrantDao) {
		this.clickGrantDao = clickGrantDao;
	}
}
