package edu.uams.clara.webapp.protocol.domain.irb;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.protocol.dao.irb.IRBReviewerDao;

@NamedNativeQuery(
	    name="listByAgendaItemId",
	    query="SELECT agenda_item_reviewer.* FROM agenda_item_reviewer "
	    		+ " INNER JOIN agenda_item ON agenda_item_reviewer.agenda_item_id = agenda_item.id "
				+ " WHERE agenda_item_reviewer.retired = :retired AND agenda_item.retired = :retired AND agenda_item.id = :agendaItemId", 
	    resultSetMapping = "AgendaItemReviewerWrapper")

@SqlResultSetMapping(name="AgendaItemReviewerWrapper",
entities={
    @EntityResult(
    		entityClass = AgendaItemReviewerWrapper.class, fields={
        @FieldResult(name="id", column="id"),
        @FieldResult(name="concurrentVersion", column="concurrent_version"),
        @FieldResult(name="retired", column="retired"),
        @FieldResult(name="agendaItemId", column="agenda_item_id"),
        @FieldResult(name="irbReviewerId", column="irb_reviewer_id"),
       })}
)
@Entity
@Configurable(dependencyCheck=true)
@Table(name = "agenda_item_reviewer")
public class AgendaItemReviewerWrapper extends AbstractDomainEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6526363948802470917L;
	
	@Column(name="agenda_item_id")
	private long agendaItemId;
	
	@Column(name="irb_reviewer_id")
	private long irbReviewerId;
	
	@Transient
	private IRBReviewerDao irbReviewerDao;
	
	@Transient
	public IRBReviewer getIRBReviewer() {
		if (this.irbReviewerId != 0) {
			return irbReviewerDao.findById(this.irbReviewerId);
		}
		
		return null;
	}
	
	@Transient
	public void setIRBReviewer(IRBReviewer irbReviewer) {
		if(irbReviewer == null){
			this.irbReviewerId = 0;
		}else{
			this.irbReviewerId = irbReviewer.getId();
		}
	}

	public long getAgendaItemId() {
		return agendaItemId;
	}

	public void setAgendaItemId(long agendaItemId) {
		this.agendaItemId = agendaItemId;
	}

	public long getIrbReviewerId() {
		return irbReviewerId;
	}

	public void setIrbReviewerId(long irbReviewerId) {
		this.irbReviewerId = irbReviewerId;
	}

	public IRBReviewerDao getIrbReviewerDao() {
		return irbReviewerDao;
	}

	@Autowired(required = true)
	public void setIrbReviewerDao(IRBReviewerDao irbReviewerDao) {
		this.irbReviewerDao = irbReviewerDao;
	}

}
