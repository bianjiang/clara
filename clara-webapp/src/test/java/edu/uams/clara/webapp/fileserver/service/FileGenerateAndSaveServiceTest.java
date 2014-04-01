package edu.uams.clara.webapp.fileserver.service;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.domain.Protocol;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "file:src/test/java/edu/uams/clara/webapp/fileserver/service/FileGenerateAndSaveServiceTest-context.xml" })
public class FileGenerateAndSaveServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(FileGenerateAndSaveServiceTest.class);
	
	private FileGenerateAndSaveService fileGenerateAndSaveService;
	
	private ProtocolDao protocolDao;
	
	@Test
	public void testProcessFileGenerateAndSave() throws Exception{
		Protocol protocol = protocolDao.findById(201793l);
		/*
		String htmlString = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=us-ascii\"></head>" 
				+ "<body><div class=\"email-template\"><div class=\"uamslogo\" style=\"width:100px;height:57px;\"><img style=\"width:100px;height:57px;\" src=\"http://clara.uams.edu/clara-webapp/static/images/uams-logo-medium.png\"></div>"
				+ "<strong>Institutional Review Board</strong><br>4301 West Markham, #636<br>Little Rock, AR 72205-7199<br>501-686-5667<br>501-686-7265 (fax)<br>"
				+ "<a href=\"http://www.uams.edu/irb/irb.asp\" target=\"_blank\">www.uams.edu/irb/irb.asp</a>"
				+ "<br><br>FWA00001119<br><br>04/30/2013<br><br><strong>PI Name:</strong>  Pilgreen, George<br><strong>PI Department:</strong> CHP Health Prof Student Success"
				+ "<br><strong>Project Title:</strong> College of Health Professions Cultural Climate Study.  Will take place through an on-line survery."
				+ "<br><br>NOT HUMAN SUBJECT RESEARCH DETERMINATION"
				+ "<br><br>The Institutional Review Board Director or Designee reviewed your material and determined that this project is NOT human subject research as defined in 45 CFR 46.102, and therefore it does not fall under the jurisdiction of the IRB review process."
				+ "<br><br><strong>Committee Notes/Comments:</strong><br>"
				+ "The rationale for this determination is that the project's primary aim is to evaluate current perceptions/conditions within the UAMS community and the findings are primarily intended for internal UAMS use.<br><br>Please keep the IRB advised of any changes that may require the project to be re-classified as human subject research.  <br><br>If you have any questions, please contact an IRB administrator at 501-686-5667.<br>"
				+ "<a href=\"https://clara.uams.edu/clara-webapp/protocols/201807/dashboard\">Click here to access study.</a>"
				+ "<br><br><img src=\"http://clara.uams.edu/clara-webapp/static/images/signatures/Paal.png\"></div></body></html>";
			*/
		String htmlString = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=us-ascii\"></head>" 
				+ "<body><div class=\"email-template\"><div class=\"uamslogo\" style=\"width:100px;height:57px;\"><img style=\"width:100px;height:57px;\" src=\"http://clara.uams.edu/clara-webapp/static/images/uams-logo-medium.png\"></div>"
				+ "<br><br><strong>Research Study Number IRB#:</strong>  201793<br><strong>Title:</strong> Measuring Listening Time in an Elementary School Classroom"
				+ "<br><br><strong>PI Name:</strong> Smiley,Donna"
				+ "<br><br><strong>Committee Notes/Comments:</strong><br>"
				+ "<a href=\"https://clara.uams.edu/clara-webapp/protocols/201793/dashboard\">Click here to access study.</a>"
				+ "</div></body></html>";
				
		UploadedFile uploadedFile = fileGenerateAndSaveService
				.processFileGenerateAndSave(protocol,
						"Ach revision requested letter",
						IOUtils.toInputStream(htmlString),
						"html",
						"text/html");
	}

	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}
	
	@Autowired(required = true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}
	
	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

}
