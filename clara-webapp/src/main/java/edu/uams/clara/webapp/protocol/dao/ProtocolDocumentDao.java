package edu.uams.clara.webapp.protocol.dao;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;

import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDocumentDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocument;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlDataDocumentWrapper;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;

@Repository
public class ProtocolDocumentDao extends AbstractDomainDao<ProtocolFormXmlDataDocument> {

	private static final long serialVersionUID = -53397841844865362L;

	private ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao;
	
	/**
	 * List all categories as List<String> for all formXmlData link to the same protocol
	 * @param formXmlData
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<String> listProtocolFormXmlDataDocumentCategories(Protocol protocol) {
		String query = "SELECT DISTINCT pfd.category FROM ProtocolFormXmlDataDocument pfd "
				+ " WHERE pfd.retired = :retired"
				+ " AND pfd.protocolFormXmlData.protocolForm.protocol = :protocol"
				+ " AND pfd.protocolFormXmlData.protocolFormXmlDataType = :protocolFormXmlDataType ";

		TypedQuery<String> q = getEntityManager().createQuery(query,
				String.class);
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocol", protocol);
		q.setParameter("protocolFormXmlDataType", ProtocolFormXmlDataType.PROTOCOL);

		return q.getResultList();
	}
	
	/**
	 * List all the latest files for the entire protocol
	 */
	@Transactional(readOnly = true)
	public List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormXmlDataDocumentsByProtocolId(long protocolId) {

		Query q = (TypedQuery<ProtocolFormXmlDataDocumentWrapper>) getEntityManager().createNamedQuery("listProtocolFormXmlDataDocumentsByProtocolId");
		q.setHint("org.hibernate.cacheable", false);
		q.setParameter("retired", Boolean.FALSE);
		q.setParameter("protocolId", protocolId);
		//q.setParameter("protocolFormXmlDataType", ProtocolFormXmlDataType.PROTOCOL);
		
		return q.getResultList();

	}

	/**
	 * List all revisions for the specified FormXmlDataDocument,
	 * relay to the formXmlDataDocumentDao to do the work...
	 */
	public List<ProtocolFormXmlDataDocumentWrapper> listProtocolFormXmlDataDocumentRevisionsByParentId(
			long parentId) {
		return protocolFormXmlDataDocumentDao.listProtocolFormXmlDataDocumentRevisionsByParentId(parentId);
	}

	@Autowired(required=true)
	public void setProtocolFormXmlDataDocumentDao(
			ProtocolFormXmlDataDocumentDao protocolFormXmlDataDocumentDao) {
		this.protocolFormXmlDataDocumentDao = protocolFormXmlDataDocumentDao;
	}

	public ProtocolFormXmlDataDocumentDao getProtocolFormXmlDataDocumentDao() {
		return protocolFormXmlDataDocumentDao;
	}
}
