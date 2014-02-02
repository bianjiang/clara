package edu.uams.clara.webapp.common.dao.usercontext;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.SAPUser;

@Repository
public class SAPUserDao extends AbstractDomainDao<SAPUser> {

	/**
	 * 
	 */
	@Transactional(readOnly = true)
	public SAPUser getSapUserBysapID(String sap) {
		String query = "SELECT s FROM SAPUser s "
				+ " WHERE s.sap = :sap AND s.retired = :retired";

		TypedQuery<SAPUser> q = getEntityManager().createQuery(query,
				SAPUser.class);
		q.setFirstResult(0);
		q.setMaxResults(1);
		q.setHint("org.hibernate.cacheable", true);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("sap", sap);

		SAPUser sapUser = null;
		try{
			sapUser= q.getSingleResult();
		}catch(Exception e){
			
		}
		return sapUser;
		
	}
	
	private static final long serialVersionUID = 6499996834329494277L;

}
