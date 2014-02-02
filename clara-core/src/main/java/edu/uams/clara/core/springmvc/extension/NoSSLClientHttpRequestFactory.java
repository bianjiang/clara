package edu.uams.clara.core.springmvc.extension;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;



public class NoSSLClientHttpRequestFactory extends SimpleClientHttpRequestFactory{
		
	private final static Logger logger = LoggerFactory
			.getLogger(NoSSLClientHttpRequestFactory.class);
	
	@Override
	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
		logger.info("herere");
		return super.createRequest(uri, httpMethod);
		
	}
	 @Override
	   protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {

	      if (connection instanceof HttpsURLConnection) {
	    	 
	    	 
	         ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
	             public boolean verify(String hostname, SSLSession session) {
	            	 logger.info("hostname: " + hostname);
	            	 try {
						logger.info("chain: " + session.getPeerCertificateChain().length);
					} catch (SSLPeerUnverifiedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                 return true;
	             }});
	      }
	      super.prepareConnection(connection, httpMethod);
	   } 
}
