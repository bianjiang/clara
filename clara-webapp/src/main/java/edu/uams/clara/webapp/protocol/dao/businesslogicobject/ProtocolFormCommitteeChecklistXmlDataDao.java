package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.enums.Committee;
import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeChecklistXmlData;

@Repository
public class ProtocolFormCommitteeChecklistXmlDataDao extends AbstractDomainDao<ProtocolFormCommitteeChecklistXmlData> {

	private static final long serialVersionUID = -4171688254132203400L;

	/**
	 *  
	 * @param formId
	 * @param committee
	 * @return 
	 */
	public ProtocolFormCommitteeChecklistXmlData findLatestByProtocolFormIdAndCommittee(long protocolFormId, Committee committee){
		String query = "SELECT fccxd FROM ProtocolFormCommitteeChecklistXmlData fccxd "
				+ " WHERE fccxd.protocolForm.id = :protocolFormId AND fccxd.retired = :retired "
				+ " AND fccxd.committee = :committee "
				+ " ORDER BY fccxd.created DESC";

		TypedQuery<ProtocolFormCommitteeChecklistXmlData> q = getEntityManager().createQuery(query,
				ProtocolFormCommitteeChecklistXmlData.class);

		q.setHint("org.hibernate.cacheable", true);
		q.setFirstResult(0);
		q.setMaxResults(1);
		
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolFormId", protocolFormId);
		q.setParameter("committee", committee);	
			
		return q.getSingleResult();

	}

}
