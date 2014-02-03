package edu.uams.clara.webapp.protocol.dao.protocolform;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/protocolform/ProtocolFormDaoTest-context.xml" })

public class ProtocolFormDaoTest {
private ProtocolFormDao protocolFormDao;


	public ProtocolFormDao getProtocolFormDao() {
		return protocolFormDao;
	}
	@Autowired(required = true)
	public void setProtocolFormDao(ProtocolFormDao protocolFormDao) {
		this.protocolFormDao = protocolFormDao;
	}

}
