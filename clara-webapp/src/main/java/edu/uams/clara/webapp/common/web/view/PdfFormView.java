package edu.uams.clara.webapp.common.web.view;

import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

import edu.uams.clara.core.util.web.view.document.AbstractPdfView;

public class PdfFormView extends AbstractPdfView{
	
	@Override
	protected void buildPdfDocument(Map<String, Object> model,
			Document document, PdfWriter writer, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		document.setPageSize(PageSize.LETTER);
		response.setHeader("Content-Disposition", "attachment; filename=\"whatever.pdf\"");
		HTMLWorker htmlWorker = new HTMLWorker(document);
		htmlWorker.parse(new StringReader((String)model.get("htmlString")));
	}
	
}