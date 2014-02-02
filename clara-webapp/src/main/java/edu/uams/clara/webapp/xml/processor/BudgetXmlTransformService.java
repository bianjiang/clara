package edu.uams.clara.webapp.xml.processor;

public interface BudgetXmlTransformService {

	String transformCLARABudgetToPSCTemplate(String budgetXml,String potoID);
	String outputCLARABudgetToPSCTemplate(String budgetXml,String potoID);
}
