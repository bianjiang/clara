package edu.uams.clara.webapp.common.dao.post;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.uams.clara.core.dao.AbstractDomainDao;
import edu.uams.clara.webapp.common.dao.department.CollegeDao;
import edu.uams.clara.webapp.common.domain.department.College;
import edu.uams.clara.webapp.common.domain.post.Post;

@Repository
public class PostDao extends AbstractDomainDao<Post> {

	private static final long serialVersionUID = 5674515236612265654L;
	
	private final static Logger logger = LoggerFactory
	.getLogger(PostDao.class);
	
	@Transactional(readOnly=true)
	public List<Post> listAllOrderByDate(Boolean includeExpiredPosts){
		TypedQuery<Post> query;
		if (includeExpiredPosts){
			query = getEntityManager()
					.createQuery(
							"SELECT p FROM Post p WHERE p.retired = :retired ORDER BY p.created DESC", Post.class)
							.setParameter("retired", Boolean.FALSE);
		} else {
			query = getEntityManager()
					.createQuery(
							"SELECT p FROM Post p WHERE p.retired = :retired AND expire_date > GETDATE() ORDER BY p.created DESC", Post.class)
							.setParameter("retired", Boolean.FALSE);
		}
		query.setHint("org.hibernate.cacheable", true);
		return query.getResultList();
	}
	
	@Transactional(readOnly=true)
	public void detelePost(long id){
		Query query = getEntityManager().createNativeQuery("UPDATE [message_post] SET retired = 1 WHERE id="+id+"");
							
		query.executeUpdate();
	}


}
