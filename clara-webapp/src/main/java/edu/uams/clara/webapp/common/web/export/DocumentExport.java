package edu.uams.clara.webapp.common.web.export;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.uams.clara.webapp.common.web.view.PdfFormView;

@Controller
public class DocumentExport{
	
	@RequestMapping(value="/export/generate", method = RequestMethod.POST)
	public ModelAndView generatePdfView(@RequestParam(value="htmlString", required = true) String htmlString, ModelMap modelMap){
		
		modelMap.put("htmlString", htmlString);

		return new ModelAndView(new PdfFormView(), modelMap) ;
		//return "ajax/export";	
	}
	/*
	@RequestMapping(value = "/ajax/export.pdf", method = RequestMethod.POST)
	public View generatePdfView(HttpServletRequest request, HttpServletResponse response){
		return new PdfFormView();
	}
	*/
}