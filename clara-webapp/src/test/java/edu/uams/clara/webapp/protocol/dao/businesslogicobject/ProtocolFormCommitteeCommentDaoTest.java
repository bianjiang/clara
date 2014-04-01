package edu.uams.clara.webapp.protocol.dao.businesslogicobject;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.uams.clara.webapp.protocol.domain.businesslogicobject.ProtocolFormCommitteeComment;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/businesslogicobject/ProtocolFormCommitteeCommentDaoTest-context.xml" })
public class ProtocolFormCommitteeCommentDaoTest {
	
	private final static Logger logger = LoggerFactory
	.getLogger(ProtocolFormCommitteeCommentDaoTest.class);

	private ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao;
	
	@Test
	public void testListAllParentsByProtocolFormId() throws JsonGenerationException, JsonMappingException, IOException{
		List<ProtocolFormCommitteeComment> protocolFormCommitteeComents = protocolFormCommitteeCommentDao.listAllParentsByProtocolFormId(569);
		
		for(ProtocolFormCommitteeComment protocolFormCommitteeComment:protocolFormCommitteeComents){
			logger.debug("parent: " + protocolFormCommitteeComment.getId());
			if(protocolFormCommitteeComment.getReplies() != null){
				logger.debug("size: " + protocolFormCommitteeComment.getReplies().size());
				for(int i = 0; i < protocolFormCommitteeComment.getReplies().size(); i++){
					ProtocolFormCommitteeComment reply = protocolFormCommitteeComment.getReplies().get(i);
					
				}
			}
			
		}
		
		 ObjectMapper mapper = new ObjectMapper();
		 assertTrue(mapper.canSerialize(protocolFormCommitteeComents.getClass()));
		 ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
	     writer.writeValue(System.out, protocolFormCommitteeComents);
	}
	
	//@Test
	public void commentsByProtocolFormId(){
		List<ProtocolFormCommitteeComment> protocolFormCommitteeComents = protocolFormCommitteeCommentDao.listAllCommentsByProtocolFormId(60);
	}

	@Autowired(required=true)
	public void setProtocolFormCommitteeCommentDao(
			ProtocolFormCommitteeCommentDao protocolFormCommitteeCommentDao) {
		this.protocolFormCommitteeCommentDao = protocolFormCommitteeCommentDao;
	}

	public ProtocolFormCommitteeCommentDao getProtocolFormCommitteeCommentDao() {
		return protocolFormCommitteeCommentDao;
	}	
}
