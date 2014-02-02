package edu.uams.clara.webapp.common.dao.history;

import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.ejb.HibernateEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.history.Track;

@Repository
public class TrackDao extends AbstractDomainDao<Track> {
	private final static Logger logger = LoggerFactory
			.getLogger(TrackDao.class);

	private static final long serialVersionUID = 6860070346923009850L;

	public Track getTrackByTypeAndRefObjectID(String type, long refObjectId) {
		TypedQuery<Track> query = getEntityManager()
				.createQuery(
						" SELECT tk FROM Track tk "
								+ " WHERE tk.type = :type AND tk.retired=:retired "
								+ " AND tk.refObjectId = :refObjectId ",
						Track.class).setParameter("retired", Boolean.FALSE)
				.setParameter("type", type)
				.setParameter("refObjectId", refObjectId);
		query.setHint("org.hibernate.cacheable", true);

		Track track = null;
		try {
			return query.getSingleResult();
		} catch (EmptyResultDataAccessException ex) {
			// ex.printStackTrace();
		}

		return track;
	}

	@Transactional(readOnly = true)
	public Track getTrackByTypeAndRefObjectClassAndId(String type,
			Class<?> refObjectClass, long refObjectId) {
		TypedQuery<Track> query = getEntityManager()
				.createQuery(
						" SELECT tk FROM Track tk "
								+ " WHERE tk.type = :type AND tk.retired=:retired AND tk.refObjectClass = :refObjectClass "
								+ " AND tk.refObjectId = :refObjectId ",
						Track.class).setParameter("retired", Boolean.FALSE)
				.setParameter("type", type)
				.setParameter("refObjectClass", refObjectClass)
				.setParameter("refObjectId", refObjectId);
		query.setHint("org.hibernate.cacheable", true);

		Track track = null;
		try {
			track = query.getSingleResult();
		} catch (EmptyResultDataAccessException ex) {
			// ex.printStackTrace();
		}

		return track;
	}

	@Transactional(readOnly = true)
	public List<Track> ListTracksByType(String type) {
		TypedQuery<Track> query = getEntityManager()
				.createQuery(
						" SELECT tk FROM Track tk "
								+ " WHERE tk.retired=:retired AND tk.type =:type",
						Track.class).setParameter("retired", Boolean.FALSE)
				.setParameter("type", type);
		query.setHint("org.hibernate.cacheable", true);

		List<Track> tracks = null;
		try {
			tracks = query.getResultList();
		} catch (EmptyResultDataAccessException ex) {
			// ex.printStackTrace();
		}

		return tracks;

	}

	@Transactional(readOnly = true)
	public List<Track> findAllByEmailTypeAndDate(String emailType, String date) {
		String xpath = "xml_data.exist('//log[@email-template-identifier=\""
				+ emailType + "\" and @date=\"" + date + "\"]')=1";

		TypedQuery<Track> query = getEntityManager().createQuery(
				" SELECT tk FROM Track tk "
						+ " WHERE tk.retired=:retired AND (" + xpath + ")",
				Track.class).setParameter("retired", Boolean.FALSE);
		query.setHint("org.hibernate.cacheable", true);

		List<Track> tracks = null;
		try {
			tracks = query.getResultList();
		} catch (EmptyResultDataAccessException ex) {
			// ex.printStackTrace();
		}

		return tracks;
	}
	
	@Transactional(readOnly = true)
	public List<String> getLastestLogByObjectIdAndRowNumber(String objectType, long objectId, int rowNumber) {
		String query  = "SELECT TOP "+ rowNumber +" CAST(c.query('.') AS VARCHAR(MAX)) AS log FROM track CROSS APPLY xml_data.nodes('/logs/log') AS t(c)"
					+ " WHERE ref_object_id = "+ objectId +" AND type = '"+ objectType +"'" 
					+ " ORDER BY CONVERT(DATETIME, c.value('(@date-time)','varchar(50)')) DESC";
		
		HibernateEntityManager hem = getEntityManager().unwrap(HibernateEntityManager.class);
		
		Session session = hem.getSession();
		
		org.hibernate.Query q = session.createSQLQuery(query);
		q.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);

		List resultLst = q.list();
		
		List<String> logList = Lists.newArrayList();
		
		for (Object result : resultLst) {
			Map row = (Map) result;
			
			logList.add(row.get("log").toString());
		}
		
		return logList;
	}
}
