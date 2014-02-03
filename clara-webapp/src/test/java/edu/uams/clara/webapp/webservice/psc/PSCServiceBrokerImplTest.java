package edu.uams.clara.webapp.webservice.psc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"file:src/test/java/edu/uams/clara/webapp/webservice/psc/PSCServiceBrokerImplTest-context.xml"})
public class PSCServiceBrokerImplTest{


	private String trustFile;
	private RestTemplate restTemplate;
	private HttpGet httpGet;
	private HttpPost httpPost;
	private DefaultHttpClient httpClient;

	@Test
	public void testPSCPush(){
		String xmlData="";



	}
	//@Test
	public void runPost()
			throws ClientProtocolException, IOException {

		String xmlData="";

		HttpResponse httpResponse = httpClient.execute(httpGet);

		HttpEntity httpEntity = httpResponse.getEntity();

		List<Cookie> cookies = httpClient.getCookieStore().getCookies();

		String tok = cookies.get(1).toString();

		System.out.println("Login form get: " + httpResponse.getStatusLine());

		String[] ticketTemp;

		// logger.debug("ticket: "+ticket);
		ticketTemp = tok.split("value: ");

		String sTok = ticketTemp[1];

		ticketTemp = sTok.split("]");

		sTok = ticketTemp[0];

		EntityUtils.consume(httpEntity);



		httpPost.addHeader("Accept", "text/xml");

		httpPost.addHeader("Content-Type", "text/xml");

		StringEntity entity1 = new StringEntity(xmlData, "UTF-8");

		entity1.setContentType("text/xml");

		httpPost.setEntity(entity1);

		httpResponse = httpClient.execute(httpPost);

		httpEntity = httpResponse.getEntity();
		String result = "";
		if (httpEntity != null) {

			InputStream is = httpEntity.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String str = "";
			while ((str = br.readLine()) != null) {
				System.out.println("" + str);
				result += str;
			}
		}
		System.out.println(httpResponse.getStatusLine().getStatusCode());

		EntityUtils.consume(entity1);


	}


	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getTrustFile() {
		return trustFile;
	}

	public void setTrustFile(String trustFile) {
		this.trustFile = trustFile;
	}


	public HttpGet getHttpGet() {
		return httpGet;
	}


	public void setHttpGet(HttpGet httpGet) {
		this.httpGet = httpGet;
	}


	public HttpPost getHttpPost() {
		return httpPost;
	}


	public void setHttpPost(HttpPost httpPost) {
		this.httpPost = httpPost;
	}


	public DefaultHttpClient getHttpClient() {
		return httpClient;
	}


	public void setHttpClient(DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}



}
