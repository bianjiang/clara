package edu.uams.clara.webapp.webservice.cas.service;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import edu.uams.clara.webapp.webservice.cas.service.CASService.CASAuthentication;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/webservice/cas/service/CASServiceTest-context.xml"})
public class CASServiceTest {
	private final static Logger logger = LoggerFactory
			.getLogger(CASServiceTest.class);

	private CASService casService;

	//@Test
	public void testRestCAS(){
		RestTemplate template = new RestTemplate();

        HttpMessageConverter httpMessageConverter = new FormHttpMessageConverter();
        HttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        List<HttpMessageConverter< ? >> list = new LinkedList<HttpMessageConverter< ? >>();

        list.add(httpMessageConverter);
        list.add(stringHttpMessageConverter);

        template.setMessageConverters(list);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("username", "bianjiang");
        map.add("password", "shmily212521!");
        String result = template.postForObject("https://sso.uams.edu:8443/cas/v1/tickets", map, String.class);
        logger.debug("result: " + result);


	}
	@Test
	public void getPSCTicket(){
		String pscServerUrl = "https://psctrain.ad.uams.edu:8443/psc/auth/cas_security_check";
		//CASAuthentication casAuthentication = casService.getTicket();
		//logger.debug("ticket: " + casAuthentication.getTicket());

		CASAuthentication casAuthentication = casService.getTicket(pscServerUrl);
		logger.debug("ticket: " + casAuthentication.getTicket());
		logger.debug("validate: " + casService.validateTicket(casAuthentication.getTicket(), pscServerUrl));
		logger.debug("JSESSIONID: " + casAuthentication.getCookieValueByNameAndDomain("JSESSIONID", "psctrain.ad.uams.edu"));


		//casService.getTicket(pscServerUrl);
	}
	//@Test
	public void getTicket(){
		//String ticket = casService.getTicket("bianjiang", "shmily212521!");
		CASAuthentication casAuthentication = casService.getTicket();
		logger.debug("ticket: " + casAuthentication.getTicket());
		logger.debug("validate: " + casService.validateTicket(casAuthentication.getTicket()));
		logger.debug("JSESSIONID: " + casAuthentication.getCookieValueByNameAndDomain("JSESSIONID", "irbdev.uams.edu"));
		casAuthentication = casService.getTicket();
		logger.debug("ticket: " + casAuthentication.getTicket());
		logger.debug("validate: " + casService.validateTicket(casAuthentication.getTicket()));
		logger.debug("JSESSIONID: " + casAuthentication.getCookieValueByNameAndDomain("JSESSIONID", "irbdev.uams.edu"));

	}



	public CASService getCasService() {
		return casService;
	}

	@Autowired(required=true)
	public void setCasService(CASService casService) {
		this.casService = casService;
	}









}
