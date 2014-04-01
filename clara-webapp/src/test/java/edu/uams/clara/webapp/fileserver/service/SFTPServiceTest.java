package edu.uams.clara.webapp.fileserver.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jcraft.jsch.JSchException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/fileserver/service/SFTPServiceTest-context.xml" })
public class SFTPServiceTest {

	private final static Logger logger = LoggerFactory
			.getLogger(SFTPServiceTest.class);
	private MessageDigest messageDigest = null;
	private SFTPService sFTPService;

	// @Test
	public void loadTestUploadLocalFileToRemote() throws InterruptedException {
		Runnable r = new Runnable() {
			public void run() {
				String fileName = "ntuser.pol";
				try {
					sFTPService.uploadLocalFileToRemote(fileName);
				} catch (JSchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				logger.debug("uploaded");
			}
		};
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(r);
			thread.start();
			threads.add(thread);
			logger.debug("new uploading thread...");
		}

		logger.debug("waiting...");
		for (Thread thread : threads) {
			thread.join();
		}

	}

	@Test
	public void testUploadLocalFileToRemote() throws JSchException {
		String fileName = "protocol/133409/9ca9517f5d54fd232c2f93038d50701a334cdad7f9bc219587ebfb17b75f394e.pdf";

		sFTPService.uploadLocalFileToRemote(fileName);

	}

	//@Test
	public void testprocessAndUploadToRemote() throws JSchException,
			NoSuchAlgorithmException, IOException {

		// copy the file from server to local
		String FileName = "C://upload/ATD Consent Version 1.5 APPROVED.pdf";
		messageDigest = MessageDigest.getInstance("SHA-256",
				new org.bouncycastle.jce.provider.BouncyCastleProvider());
		InputStream fis = new FileInputStream(FileName);
		byte[] bytes = IOUtils.toByteArray(fis);
		messageDigest.update(bytes);
		// get the hash file name
		String identifier = new String(Hex.encode(messageDigest.digest()));

		String filename = "C://Data/upload/" + identifier + ".pdf";
		FileOutputStream fout = new FileOutputStream(filename);
		fout.write(bytes);
		fout.flush();
		fout.close();

		// upload file to the server
		sFTPService.uploadLocalFileToRemote("C://Data//upload//protocol//133409//"+identifier + ".pdf");
		
		// delete the file after uploading...
		File uploadedFile = new File(filename);
		uploadedFile.delete();

	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}

	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}
}
