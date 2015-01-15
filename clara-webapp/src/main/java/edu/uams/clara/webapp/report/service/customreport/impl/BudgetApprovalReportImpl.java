package edu.uams.clara.webapp.report.service.customreport.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import edu.uams.clara.webapp.common.util.DateFormatUtil;
import edu.uams.clara.webapp.report.domain.ReportTemplate;
import edu.uams.clara.webapp.report.service.customreport.CustomReportService;

public class BudgetApprovalReportImpl extends CustomReportService {
	private final static Logger logger = LoggerFactory.getLogger(BudgetApprovalReportImpl.class);

	private EntityManager em;
	
	@Override
	public String generateReportResult(ReportTemplate reportTemplate) {
		String finalResultXml = "<report-results>";
		Map<String, String> queryCriteriasValueMap = Maps.newHashMap();
		queryCriteriasValueMap.put("Budget Approval Date", "6/1/2014 to "+DateFormatUtil.formateDateToMDY(new Date()));
		finalResultXml = finalResultXml+generateSummaryCriteriaTable(reportTemplate,
				queryCriteriasValueMap);
		
		finalResultXml += "<report-result id=\""+ reportTemplate.getTypeDescription() +"\"  created=\""+ DateFormatUtil.formateDateToMDY(new Date()) +"\">";
		finalResultXml += "<title>"+ reportTemplate.getDescription() +"</title>";
		
		finalResultXml += "<fields>";
		finalResultXml += "<field id=\"protocolid\" desc=\"IRB #\" hidden=\"false\" />";
		finalResultXml += "<field id=\"title\" desc=\"Title\" hidden=\"false\" />";
		finalResultXml += "<field id=\"budgetapprovaldate\" desc=\"Budget Approval Date\" hidden=\"false\" />";
		finalResultXml += "<field id=\"involvecancer\" desc=\"Involve Cancer Drug Treatment \" hidden=\"false\" />";
		finalResultXml += "</fields>";
		finalResultXml += "<report-items>";
		
		List<Object[]> results = generateQueryResult();
		
		for(Object[] result : results){
			try{
				BigInteger pid = (BigInteger)result[0];
				String title = (String)result[1];
				String budgetApprovalDate = (String)result[2];
				String involveCancer = (String)result[3];
				
				if(involveCancer.equals("n")){
					involveCancer = "No";
				}else if(involveCancer.equals("y")){
					involveCancer = "Yes";
				}
				
				
				finalResultXml += "<report-item>";
				finalResultXml += "<field id=\"" + "protocolid" + "\">";
				finalResultXml +="<![CDATA[<a target=\"_blank\" href=\""+this.getAppHost()+"/clara-webapp/protocols/"+pid+"/dashboard\">"+pid+"</a>]]>";
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"" + "title" + "\">";
				finalResultXml += title;
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"" + "budgetapprovaldate" + "\">";
				finalResultXml += budgetApprovalDate;
				finalResultXml += "</field>";
				
				finalResultXml += "<field id=\"" + "involvecancer" + "\">";
				finalResultXml += involveCancer;
				finalResultXml += "</field>";
				finalResultXml += "</report-item>";
				
			}catch(Exception e){
				logger.debug((BigInteger)result[0]+"");
				e.printStackTrace();
			}
		}
		
		
		finalResultXml += "</report-items>";
		finalResultXml += "</report-result>";
		finalResultXml += "</report-results>";
		
		finalResultXml = finalResultXml.replaceAll("&", "&amp;");
		finalResultXml =finalResultXml.replace("<![CDATA[null]]>", "");
		finalResultXml =finalResultXml.replace("&gt;null", "&gt;");
		finalResultXml =finalResultXml.replace("null&lt;br&gt;", "");
		
		logger.debug(finalResultXml);
		return finalResultXml;
	}
	
	private List<Object[]> generateQueryResult(){
		String queryStatement  = "select id,  meta_data_xml.value('(/protocol/title/text())[1]','varchar(max)') as title,  meta_data_xml.value('(/protocol/summary/budget-determination/approval-date/text())[1]','varchar(100)') as involvecancertreat, meta_data_xml.value('(/protocol/epic/involve-chemotherapy/text())[1]','varchar(100)') as budgetapprovaldate from protocol where retired = 0 and meta_data_xml.value('(/protocol/summary/budget-determination/approval-date/text())[1]','varchar(100)')  is not null and Datediff(Day, meta_data_xml.value('(/protocol/summary/budget-determination/approval-date/text())[1]','varchar(100)'), '2014-06-01')<0";
	
		Query query = em.createNativeQuery(queryStatement);
	
		List<Object[]> results = (List<Object[]>) query.getResultList();
		
		return results;
	
	
	}

	public EntityManager getEm() {
		return em;
	}

	@PersistenceContext(unitName = "defaultPersistenceUnit")
	public void setEm(EntityManager em) {
		this.em = em;
	}
}
