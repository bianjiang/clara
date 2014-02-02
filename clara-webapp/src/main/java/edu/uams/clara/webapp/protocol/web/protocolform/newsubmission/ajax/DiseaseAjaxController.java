package edu.uams.clara.webapp.protocol.web.protocolform.newsubmission.ajax;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * this contraller eventually needs to be moved to the terminology server
 */
@Controller
public class DiseaseAjaxController {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory
			.getLogger(DrugAjaxController.class);
	/**
	 * return json result from php via proxy
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/diseases/list", method = RequestMethod.GET)
	public @ResponseBody
		String listById(@RequestParam("node") String doid) {
		String str = "";
		String outStr = "";
		try {
		    // Create a URL for the proxy page
		    URL serviceUrl = new URL("http://irbdev.uams.edu/diseaseOntology/claraJSON_browse.php?id="+doid);

		    // Read all the text returned by the server
		    BufferedReader in = new BufferedReader(new InputStreamReader(serviceUrl.openStream()));
		    
		    while ((str = in.readLine()) != null) {
		        outStr += str;
		    }
		    in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		
		return outStr;
	}
	
	/**
	 * return json result from php via proxy
	 * @return
	 */
	@RequestMapping(value = "/ajax/protocols/protocol-forms/new-submission/diseases/search", method = RequestMethod.GET)
	public @ResponseBody
		String searchByKeyword(@RequestParam("text") String text) {
		String str = "";
		String outStr = "";
		try {
		    // Create a URL for the proxy page
		    URL serviceUrl = new URL("http://irbdev.uams.edu/diseaseOntology/claraJSON_search.php?text="+text);

		    // Read all the text returned by the server
		    BufferedReader in = new BufferedReader(new InputStreamReader(serviceUrl.openStream()));
		    
		    while ((str = in.readLine()) != null) {
		        outStr += str;
		    }
		    in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		
		return outStr;
	}

	
	
}
