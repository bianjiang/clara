package edu.uams.clara.webapp.common.dao.usercontext;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.domain.usercontext.CitiMember;
import edu.uams.clara.webapp.common.domain.usercontext.User;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

@Repository
public class CitiMemberDao extends AbstractDomainDao<CitiMember> {

	private final static Logger logger = LoggerFactory
			.getLogger(CitiMemberDao.class);
	
	private static final long serialVersionUID = 1670172255298783886L;

	
	@Transactional(readOnly = true)
	public CitiMember findCitiTrainingRecordByCompletionReportNumber(String completionReportNumber){
		TypedQuery<CitiMember> query = getEntityManager()
				.createQuery(
						"SELECT cm FROM CitiMember cm WHERE cm.retired = :retired AND cm.completionReportNumber = :completionReportNumber ",
						CitiMember.class).setParameter("retired", Boolean.FALSE)
		.setParameter("completionReportNumber", completionReportNumber);
		query.setHint("org.hibernate.cacheable", true);
		
		return query.getSingleResult();
	}
	
	/*
	@Transactional(readOnly = true)
	public List<CitiMember> listByUserName(String userName) {

		TypedQuery<CitiMember> query = getEntityManager()
				.createQuery(
						"SELECT cm FROM CitiMember cm WHERE cm.retired = :retired AND cm.username = :username",
						CitiMember.class).setParameter("retired", Boolean.FALSE)
				.setParameter("username", userName);

		query.setHint("org.hibernate.cacheable", true);

		return query.getResultList();
	}
	*/
	/*
	@Transactional(readOnly = true)
	public CitiMember getByUserName(String userName) {

		TypedQuery<CitiMember> query = getEntityManager()
				.createQuery(
						"SELECT cm FROM CitiMember cm WHERE cm.retired = :retired AND cm.username = :username ORDER BY cm.id DESC",
						CitiMember.class).setParameter("retired", Boolean.FALSE)
				.setParameter("username", userName);

		query.setHint("org.hibernate.cacheable", true);
		query.setFirstResult(0);
		query.setMaxResults(1);

		return query.getSingleResult();
	}*/
	
	@Transactional(readOnly = true)
	public List<CitiMember> listCitiMemberByUser(User user){
		List<CitiMember> citiMembers = Lists.newArrayList();
		
		String citiId = null;
		
		try {
			List<String> citiIds = xmlProcessor.listElementStringValuesByPath("/metadata/citi-id", user.getProfile());

			if(citiIds.size()>0){
				citiId = citiIds.get(0);
			}
		} catch (Exception ex){
			//don't care...
		}
		
		String queryString = "SELECT cm FROM CitiMember cm WHERE cm.retired = :retired AND ("
				+ ((citiId != null)?" cm.memberID = :memberID OR":"")
				+ ((user.getUsername() != null)?" cm.username = :username OR":"")
				+ ((user.getPerson() != null && user.getPerson().getSap() != null && !user.getPerson().getSap().isEmpty())?" cm.employeeNumber = :employeeNumber OR":"")
				+ ((user.getPerson() != null && user.getPerson().getEmail() != null && !user.getPerson().getEmail().isEmpty())?" cm.emailAddress = :emailAddress OR":"");
		queryString = queryString.substring(0, queryString.length() - 2) + ")";
		
		logger.debug("query:" + queryString);
				
		TypedQuery<CitiMember> query = getEntityManager()
				.createQuery(
						queryString,
						CitiMember.class).setParameter("retired", Boolean.FALSE);
		if(citiId != null){
			query.setParameter("memberID", citiId);
		}
		if(user.getUsername() != null){
			query.setParameter("username", user.getUsername());
		}
		if(user.getPerson() != null && user.getPerson().getSap() != null && !user.getPerson().getSap().isEmpty()){
			query.setParameter("employeeNumber", user.getPerson().getSap());
		}
		if(user.getPerson() != null && user.getPerson().getEmail() != null && !user.getPerson().getEmail().isEmpty()){
			query.setParameter("emailAddress", user.getPerson().getEmail());
		}			
		
			

		query.setHint("org.hibernate.cacheable", true);
		
		try{
			citiMembers.addAll(query.getResultList());
		}catch(Exception ex){
			//don't care... just don't find any..
		}

		return citiMembers;
		
	}
	
	/**
	 * @TODO: this needs to be out of this, this needs to go into the user service
	 * @return
	 */
	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required=true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	private XmlProcessor xmlProcessor;
	

}
