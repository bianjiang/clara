package edu.uams.clara.webapp.common.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.common.security.impl.MutexLockServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/common/security/mutexLockTest-context.xml" })
public class mutexLockTest {
	
	private MutexLockServiceImpl mutexlockService;
	
	@Test
	public void unlockExpiredLocksTest(){
		mutexlockService.unlockExpiredMutexLock();
	}

	public MutexLockServiceImpl getMutexlockService() {
		return mutexlockService;
	}

	@Autowired(required= true)
	public void setMutexlockService(MutexLockServiceImpl mutexlockService) {
		this.mutexlockService = mutexlockService;
	}
}
