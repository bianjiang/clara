package edu.uams.clara.webapp.webservice.psc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.uams.clara.webapp.protocol.web.ajax.ProtocolDashboardAjaxController;

public class PSCStudyImporter {
	private String _ServiceURL = "";
	private String _Username = "";
	private String _Password = "";
	private Boolean _Auth = false;
	private Boolean _SSL = false;
	private String _SubAction = "POST";
	private String _Cookies = "";
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolDashboardAjaxController.class);
	
	public String get_SubAction() {
		return _SubAction;
	}

	public void set_SubAction(String _SubAction) {
		this._SubAction = _SubAction;
	}

	public String get_Cookies() {
		return _Cookies;
	}

	public void set_Cookies(String _Cookies) {
		this._Cookies = _Cookies;
	}

	public Boolean get_SSL() {
		return _SSL;
	}

	public void set_SSL(Boolean _SSL) {
		this._SSL = _SSL;
	}

	public String get_ServiceURL() {
		return _ServiceURL;
	}

	public void set_ServiceURL(String _ServiceURL) {
		this._ServiceURL = _ServiceURL;
	}

	public String get_Username() {
		return _Username;
	}

	public void set_Username(String _Username) {
		this._Username = _Username;
	}

	public String get_Password() {
		return _Password;
	}

	public void set_Password(String _Password) {
		this._Password = _Password;
	}

	public Boolean get_Auth() {
		return _Auth;
	}

	public void set_Auth(Boolean _Auth) {
		this._Auth = _Auth;
	}
	
	public String GetStringFromXMLFile(String aFileName){
		logger.info(aFileName);
		String retStr = "";
		FileInputStream fis;
		try {
			fis = new FileInputStream(aFileName);
			int Len;
			try {
				Len = fis.available();
				byte Buff[]= new byte[Len];
				fis.read(Buff);
				retStr = new String(Buff);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retStr;
	}
	
	
	
	public AuthObjectTest getSSOSessionID(){
		String URLStr = _ServiceURL; //"https://psc.uams.edu:8443/psc/api/v1/studies";
		String JSessID = "";
		String ltValue = "";
		try {
			URL TestURL = new URL(URLStr);
			try {
			    HttpURLConnection Conn = (HttpURLConnection) TestURL.openConnection();
				Conn.setRequestMethod(_SubAction);
				
				Conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=\"utf8\"");
				Conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				
				int returnCode = Conn.getResponseCode();

				if (returnCode == 200){
					
					String cookie = Conn.getHeaderField("Set-Cookie");
					
				     if (cookie != null){
				    	 logger.info("cookie: " + cookie);
				    	 if (cookie.contains("JSESSIONID")){
				    		 cookie = cookie.substring(cookie.indexOf("JSESSIONID"));
				    		 JSessID = cookie.substring(cookie.indexOf('=') + 1, cookie.indexOf(';'));
				    	 }
				     }
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getInputStream()));
					String line;
					while ((line = in.readLine()) != null){
						if (line.contains("name=\"lt\"")){
							ltValue = line.substring(line.indexOf("value"));
							ltValue = ltValue.substring(ltValue.indexOf("\"") + 1);
							ltValue = ltValue.substring(0, ltValue.indexOf("\""));
						}						
					}
				}else{
					logger.error("error code: " + returnCode);
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getErrorStream()));
					String line;
					while ((line = in.readLine()) != null){
						logger.info(line);
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new AuthObjectTest(JSessID, ltValue);
	}
	
	public String OperatePSCStudySnapshot(String aXMLBody, String aOperate){
		String retStr = "";
		String URLStr = _ServiceURL; //"https://psc.uams.edu:8443/psc/api/v1/studies";
		try {
			URL TestURL = new URL(URLStr);
			try {
				HttpURLConnection Conn = (HttpURLConnection) TestURL.openConnection();
				Conn.setRequestMethod(_SubAction);	
				
				
				Conn.setRequestProperty("Content-Type", "text/xml; charset=\"utf8\"");
				Conn.setRequestProperty("Accept", "text/xml; charset=\"utf8\"");
				Conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				Conn.setRequestProperty("Cookie", _Cookies);
				Conn.setRequestMethod(aOperate);
				Conn.setDoInput(true);
				Conn.setDoOutput(true);
				OutputStream output = null;			

				System.out.println(Conn.getURL());
				
				try{
					output = Conn.getOutputStream();
					output.write(aXMLBody.getBytes());
				
				}finally{
					if (output != null)try{
						output.close();
					}catch (IOException logOrIgnore){
						
					}
				}
				int returnCode = Conn.getResponseCode();
				if (returnCode == 200 || returnCode == 201 || returnCode == 505){
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getInputStream()));
					String line;
					
					while ((line = in.readLine()) != null){
						retStr += line + "<br/>";
						logger.info(line);			
					}
				}else if(Conn.getErrorStream() != null){
					logger.error("error code: " + returnCode);
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getErrorStream()));
					String line;
					while ((line = in.readLine()) != null){
						retStr += line + "<br/>";
						logger.info(line);
					}
				}else {
					logger.error("error code: " + returnCode);
					retStr += "error code: " + returnCode;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retStr;
	}
	
	
	public String getAllPSCStudies(){
		String retStr = "";
		String URLStr = _ServiceURL; //"https://psc.uams.edu:8443/psc/api/v1/studies";
		try {
			URL TestURL = new URL(URLStr);
			try {
				HttpURLConnection Conn = (HttpURLConnection) TestURL.openConnection();
				Conn.setRequestMethod(_SubAction);
				
				Conn.setRequestProperty("Content-Type", "text/plain; charset=\"utf8\"");
				Conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				Conn.setRequestProperty("Cookie", _Cookies);
				Conn.setRequestMethod(_SubAction);
				Conn.setDoInput(true);
				Conn.setDoOutput(false);
				int returnCode = Conn.getResponseCode();
				if (returnCode == 200){
					
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getInputStream()));
					String line;
					while ((line = in.readLine()) != null){
						logger.info(line);			
					}
				}else{
					logger.error("error code: " + returnCode);
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getErrorStream()));
					String line;
					while ((line = in.readLine()) != null){
						logger.info(line);
					}
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retStr;
	}
	
	public String GetPSCSessID(AuthObjectTest aAuthObj){
		String URLStr = _ServiceURL; //"https://psc.uams.edu:8443/psc/api/v1/studies";
		String UserName = aAuthObj.get_UserName();
		String Password = aAuthObj.get_Password();
		String tempStr = "40298A5F0D1CA766E9C86556C400CBDC";	
		int StdLen = tempStr.length();
		String retStr = "";
		Boolean bFound = false;
		try {
			URL TestURL = new URL(URLStr);
			try {
				
				HttpURLConnection Conn = (HttpURLConnection) TestURL.openConnection();
				
			
				
				Conn.setDoInput(true);
				Conn.setDoOutput(true);
				Conn.setRequestMethod(_SubAction);
				String query = "username=" + UserName + "&password=" + Password + 
				  "&lt=" + aAuthObj.get_ltValue() + "&_eventId=submit";
				
				_Cookies = "JSESSIONID=" + aAuthObj.get_SSOSessionID();
//				_Cookies = "JSESSIONID=7667B91C2FBC28D80BB5B35A2419CDEC";
				Conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=\"utf8\"");
				Conn.setRequestProperty("Cookie", _Cookies);
				Conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				OutputStream output = null;
				try{
					output = Conn.getOutputStream();
					output.write(query.getBytes("UTF-8"));

				}finally{
					if (output != null)try{
						output.close();
					}catch (IOException logOrIgnore){
						
					}
				}
//				Conn.setRequestMethod(_SubAction);
				System.out.println(Conn.getURL() );
				int returnCode = Conn.getResponseCode();
				
				if (returnCode == 200){
					
					
					String cookie = Conn.getHeaderField("Set-Cookie");					
				     if (cookie != null){
				    	 logger.info("cookie: " + cookie); 
				    
				     }
				     System.out.println(Conn.getURL() );
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getInputStream()));
					String line;
					
					while (((line = in.readLine()) != null) && !bFound){
//						logger.info(line);
						if (line.contains("jsessionid=")){
							//jsessionid=40298A5F0D1CA766E9C86556C400CBDC
							retStr = line.substring(line.indexOf("jsessionid="));
							if (retStr.contains("\"")){
								retStr = retStr.substring(retStr.indexOf("=") + 1, retStr.indexOf("\""));
//								logger.debug(retStr + "len:" + retStr.length() + " std:" + StdLen);	
								if (retStr.length() == StdLen){
									bFound = true;
	
								}
							}
						}
					}
				}else{
				
					System.out.println(Conn.getURL() );
					logger.error("error code: " + returnCode);
					
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getErrorStream()));
					String line;
					while ((line = in.readLine()) != null){
						logger.info(line);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return retStr;
	}
	
	
	public String getTicket(AuthObjectTest aAuthObj){
		String URLStr = _ServiceURL; //"https://psc.uams.edu:8443/psc/api/v1/studies";
		String UserName = aAuthObj.get_UserName();
		String Password = aAuthObj.get_Password();
		String retStr = "";
		try {
			URL TestURL = new URL(URLStr);
			try {
			
				HttpURLConnection Conn = (HttpURLConnection) TestURL.openConnection();
						
				Conn.setDoInput(true);
				Conn.setDoOutput(true);
				Conn.setRequestMethod(_SubAction);
				String query = "username=" + UserName + "&password=" + Password + 
				  "&lt=" + aAuthObj.get_ltValue() + "&_eventId=submit";
				
				_Cookies = "JSESSIONID=" + aAuthObj.get_SSOSessionID();
				Conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=\"utf8\"");
				Conn.setRequestProperty("Cookie", _Cookies);
				Conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
				OutputStream output = null;
				try{
					output = Conn.getOutputStream();
					output.write(query.getBytes("UTF-8"));

				}finally{
					if (output != null)try{
						output.close();
					}catch (IOException logOrIgnore){
						
					}
				}
//				Conn.setRequestMethod(_SubAction);
			
				int returnCode = Conn.getResponseCode();
			
				if (returnCode == 302){
						
					String cookie = Conn.getHeaderField("Set-Cookie");					
				     if (cookie != null){
				    	 logger.info("cookie: " + cookie); 	    
				     }

					retStr=Conn.getHeaderField("location");
				}else{
					logger.error("error code: " + returnCode);
					BufferedReader in = new BufferedReader(new InputStreamReader(Conn.getErrorStream()));
					String line;
					while ((line = in.readLine()) != null){
						logger.info(line);
					}			
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retStr;
	}

}
