package edu.uams.clara.integration.incoming.click.service;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import edu.uams.clara.integration.incoming.click.dao.ClickAgencyDao;
import edu.uams.clara.webapp.protocol.dao.thing.SponsorDao;
import edu.uams.clara.webapp.protocol.domain.thing.Sponsor;

@Service
public class ImportClickSponsorAgencyService {
	private final static Logger logger = LoggerFactory
			.getLogger(ImportClickSponsorAgencyService.class);


	private EntityManager entityManager;
	
	private ClickAgencyDao clickAgencyDao;

	private SponsorDao sponsorDao;

	public void updateSponsorList() {
		List<Sponsor> sponsors = sponsorDao.findAll(); 
		
		Set<String> existing =  Sets.newHashSet();
		
		for(Sponsor sponsor :sponsors){
			existing.add(sponsor.getDescription().trim());
		}

		Set<String> incoming = Sets.newHashSet();
		List<String> sponsorNames = clickAgencyDao.listAllClickAgencies();
		for (String sponsor : sponsorNames) {
			incoming.add(sponsor.trim().replace("GÇÖ", "'")); 
		}

		Set<String> newOnes = Sets.difference(incoming, existing); 

		for (String newOne : newOnes) {
			Sponsor s = new Sponsor();
			s.setRetired(false);
			s.setApproved(true);
			s.setDescription(newOne);
			s.setValue(newOne);
			s = sponsorDao.saveOrUpdate(s);
			logger.debug("added: " + s.getId());
		}
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public SponsorDao getSponsorDao() {
		return sponsorDao;
	}

	@Autowired(required = true)
	public void setSponsorDao(SponsorDao sponsorDao) {
		this.sponsorDao = sponsorDao;
	}

	public ClickAgencyDao getClickAgencyDao() {
		return clickAgencyDao;
	}

	@Autowired(required = true)
	public void setClickAgencyDao(ClickAgencyDao clickAgencyDao) {
		this.clickAgencyDao = clickAgencyDao;
	}
}
