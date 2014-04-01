package edu.uams.clara.webapp.webservice.cas.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import edu.uams.clara.webapp.webservice.cas.service.CASService;

public class CASServiceImpl implements CASService {

	private String casServerUrl;

	private String casLoginUrl;

	private String casServiceUrl;

	private String casUsername;

	private String casPassword;

	private TicketValidator ticketValidator;

	private HttpClient httpClient;

	private final static Logger logger = LoggerFactory
			.getLogger(CASService.class);

	@Override
	public boolean validateTicket(String ticket) {

		try {
			ticketValidator.validate(ticket, casServiceUrl);

		} catch (TicketValidationException e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean validateTicket(String ticket, String casServiceUrl) {

		try {
			ticketValidator.validate(ticket, casServiceUrl);

		} catch (TicketValidationException e) {
			logger.error(e.getMessage());
			// e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public CASAuthentication getTicket() {
		return getTicket(casUsername, casPassword, casServiceUrl);
	}

	@Override
	public CASAuthentication getTicket(String casServiceUrl) {
		return getTicket(casUsername, casPassword, casServiceUrl);
	}
	
	private Pattern casTicketRegexPattern = Pattern.compile(".*ticket=(.*)");
	

	private CASAuthentication getCASAuthenticationFromContext(
			HttpContext context) {
		CASAuthentication casAuthentication = null;
		String ticket = null;
		HttpUriRequest currentReq = (HttpUriRequest) context
				.getAttribute(ExecutionContext.HTTP_REQUEST);
		HttpHost currentHost = (HttpHost) context
				.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
		String currentUrl = (currentReq.getURI().isAbsolute()) ? currentReq
				.getURI().toString() : (currentHost.toURI() + currentReq
				.getURI());
		Matcher m = casTicketRegexPattern.matcher(currentUrl);

		if (m.find()) {
			ticket = m.group(1);
		}
		
		casAuthentication = new CASAuthentication(ticket);

		return casAuthentication;
	}

	@Override
	public CASAuthentication getTicket(String username, String password,
			String casServiceUrl) {

		CASAuthentication casAuthentication = null;

		String casGetTicketUrl = casLoginUrl + "?service=" + casServiceUrl;
		logger.debug("casGetTicketUrl: " + casGetTicketUrl);
		HttpContext context = new BasicHttpContext();

		HttpGet httpGet = new HttpGet(casGetTicketUrl);
		HttpEntity entity = null;
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpGet, context);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException(response.getStatusLine().toString());

			entity = response.getEntity();

			// System.out.println("Login form get: " +
			// response.getStatusLine());
			String ltValue = "";
			String executionValue = "";
			if (entity != null) {

				InputStream is = entity.getContent();

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				String str = "";

				while ((str = br.readLine()) != null) {
					// System.out.println(""+str);
					if (str.contains("name=\"lt\"")) {
						ltValue = str.substring(str.indexOf("value"));
						ltValue = ltValue.substring(ltValue.indexOf("\"") + 1);
						ltValue = ltValue.substring(0, ltValue.indexOf("\""));
					}
					if (str.contains("name=\"execution\"")) {
						executionValue = str.substring(str.indexOf("value"));
						executionValue = executionValue.substring(executionValue.indexOf("\"") + 1);
						executionValue = executionValue.substring(0, executionValue.indexOf("\""));
					}

				}
			}

			// I might already logged in, don't need to repost
			if (ltValue.isEmpty()) {
				casAuthentication = getCASAuthenticationFromContext(context);
				casAuthentication.setHttpClient((AbstractHttpClient) httpClient);
				return casAuthentication;
			}

			logger.debug("ltValue: " + ltValue);
			logger.debug("executionValue: " + executionValue);

			HttpPost httPost = new HttpPost(casGetTicketUrl);
			logger.debug(casGetTicketUrl);
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			nvps.add(new BasicNameValuePair("username", username));
			nvps.add(new BasicNameValuePair("password", password));
			nvps.add(new BasicNameValuePair("lt", ltValue));
			
			//since training server use crissso, need this value.
			if(!executionValue.isEmpty()){
				nvps.add(new BasicNameValuePair("execution", executionValue));
			}
			nvps.add(new BasicNameValuePair("_eventId", "submit"));

			httPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

			response = httpClient.execute(httPost, context);
			entity = response.getEntity();
			EntityUtils.consume(entity);

			
			httpGet = new HttpGet(casServiceUrl);

			response = httpClient.execute(httpGet, context);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				throw new IOException(response.getStatusLine().toString());

			entity = response.getEntity();
			

			EntityUtils.consume(entity);
			
			casAuthentication = ((CASService.CASRedirectStrategy)((AbstractHttpClient) httpClient).getRedirectStrategy()).getCasAuthentication();//getCASAuthenticationFromContext(context);
			logger.debug(casAuthentication +" "+httpClient+ ""+((CASService.CASRedirectStrategy)((AbstractHttpClient) httpClient).getRedirectStrategy()).getCasAuthentication() );
			casAuthentication.setHttpClient((AbstractHttpClient) httpClient);
			return casAuthentication;

		} catch (Exception e) {
			e.printStackTrace();

		}

		return casAuthentication;

	}

	public String getCasServerUrl() {
		return casServerUrl;
	}

	public void setCasServerUrl(String casServerUrl) {
		this.casServerUrl = casServerUrl;
	}

	public TicketValidator getTicketValidator() {
		return ticketValidator;
	}

	public void setTicketValidator(TicketValidator ticketValidator) {
		this.ticketValidator = ticketValidator;
	}

	@Autowired(required = true)
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public String getCasServiceUrl() {
		return casServiceUrl;
	}

	public void setCasServiceUrl(String casServiceUrl) {
		this.casServiceUrl = casServiceUrl;
	}

	public String getCasUsername() {
		return casUsername;
	}

	public void setCasUsername(String casUsername) {
		this.casUsername = casUsername;
	}

	public String getCasPassword() {
		return casPassword;
	}

	public void setCasPassword(String casPassword) {
		this.casPassword = casPassword;
	}

	public String getCasLoginUrl() {
		return casLoginUrl;
	}

	public void setCasLoginUrl(String casLoginUrl) {
		this.casLoginUrl = casLoginUrl;
	}

}
