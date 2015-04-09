package edu.uams.clara.webapp.xml.processor.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.webapp.xml.processor.BudgetXmlDifferService;
import edu.uams.clara.webapp.xml.processor.BudgetXmlTransformService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;


public class BudgetXmlDifferServiceImpl implements BudgetXmlDifferService{
	private final static Logger logger = LoggerFactory
			.getLogger(BudgetXmlTransformService.class);
	private XmlProcessor xmlProcessor;
	
	
	private List<String> epochAttributes = Lists.newArrayList();{
		epochAttributes.add("simple");
		epochAttributes.add("conditional");
		epochAttributes.add("name");
	}
	
	private List<String> armAttributes = Lists.newArrayList();{
		armAttributes.add("hidden");
		armAttributes.add("name");
	}
	
	private List<String> cycleAttributes = Lists.newArrayList();{
		cycleAttributes.add("repetitions");
		cycleAttributes.add("name");
		cycleAttributes.add("simple");
		cycleAttributes.add("repeatforever");
		cycleAttributes.add("duration");
		cycleAttributes.add("durationunit");
		cycleAttributes.add("startday");
		cycleAttributes.add("endday");
		
	}
	
	private List<String> visitAttributes = Lists.newArrayList();{
		visitAttributes.add("subj");
		visitAttributes.add("name");
		visitAttributes.add("cycleindex");
		visitAttributes.add("unit");
		visitAttributes.add("unitvalue");
	}
	
	private List<String> vpAttributes = Lists.newArrayList();{
		vpAttributes.add("r");
		vpAttributes.add("t");
	}
	
	private List<String> procecureAttributes = Lists.newArrayList();{
		procecureAttributes.add("cptcode");
		procecureAttributes.add("description");
		procecureAttributes.add("alternative");
		procecureAttributes.add("conditional");
		procecureAttributes.add("type");
		procecureAttributes.add("expensecategory");
	}
	
	private List<String> expenseAttributes = Lists.newArrayList();{
		expenseAttributes.add("cost");
		expenseAttributes.add("count");
		expenseAttributes.add("description");
		expenseAttributes.add("external");
		expenseAttributes.add("fa");
		expenseAttributes.add("faenabled");
		expenseAttributes.add("notes");
		expenseAttributes.add("subtype");
		expenseAttributes.add("type");
	}
	
	
	private Map<String,Element> getIndexElementMap(NodeList nodeList, List<String> indexes){
		Map<String,Element> elementMap = Maps.newHashMap();
		for(int i=0;i<nodeList.getLength();i++){
			Element element = (Element) nodeList.item(i);
			String key = "";
			for(String index:indexes){
				key+= element.getAttribute(index);
			}
			if(element.getNodeName().equals("procedure")){
				Element miscCost = (Element) element.getElementsByTagName("misc").item(0);
				key += miscCost.getTextContent();
				logger.debug(key);
			}
			elementMap.put(key, element);
		}
		return elementMap;
	}
	
	private Element copyElement(Element source, Element target,boolean modificationCopy){
		NamedNodeMap srcAttrs = target.getAttributes();
		for (int i = 0; i < srcAttrs.getLength(); i++) {
			String attributeName = srcAttrs.item(i).getNodeName();
			if(attributeName.equals("t")&&modificationCopy){
				source.setAttribute(attributeName,
						source.getAttribute(attributeName)+"->"+target.getAttribute(attributeName));
			}else{
				source.setAttribute(attributeName,
						target.getAttribute(attributeName));
			}
		}
		return source;
	}
		
	private void compareElements(Element srcEle, Element targetEle, List<String> attributes){
		String srcInfo = "";
		String targetInfo = "";
		for(String attribute: attributes){
			srcInfo += srcEle.getAttribute(attribute);
			targetInfo += targetEle.getAttribute(attribute);
		}
		
		if(!srcInfo.equals(targetInfo)){
			srcEle = copyElement(srcEle,targetEle,true);
			srcEle.setAttribute("diff", "M");
		}
	}
	
	private void deleteElementCheck(Set<String> compIndexes, Set<String> currIndexes, Map<String,Element> compEleMap ){
		for(String index : compIndexes){
			if(!currIndexes.contains(index)){
				//mark as delete
				Element compEle = compEleMap.get(index);
				compEle.setAttribute("diff", "D");
			}
		}
	}
	
	@Override
	public String differBudgetXml(String compreadBudgetXml, String currentBudgetXml) {
		
		//load budget
		Document compDoc = null;
		Document currDoc = null;
		
		try {
			compDoc = xmlProcessor.loadXmlStringToDOM(compreadBudgetXml);
			currDoc = xmlProcessor.loadXmlStringToDOM(currentBudgetXml);
		} catch (Exception e) {
			logger.debug("Warning: Load Budget Failed!!!");
			e.printStackTrace();
		}
		//get epochlists and sort them by index
		List<String> indexes = Lists.newArrayList();
		indexes.add("id");
		Map<String,Element> compEpochs = getIndexElementMap(compDoc.getElementsByTagName("epoch"),indexes);
		Map<String,Element> currEpochs = getIndexElementMap(currDoc.getElementsByTagName("epoch"),indexes);
		//comapre epochs
		compareEpoch(compEpochs, currEpochs, compDoc);

		//compare expenses
		Element compExpenses = (Element) compDoc.getElementsByTagName("expenses").item(0);
		Element currExpenses = (Element) currDoc.getElementsByTagName("expenses").item(0);
		
		compareExpenses(compExpenses,currExpenses,compDoc);
		
		
		compreadBudgetXml = DomUtils.elementToString(compDoc, false,
				Encoding.UTF16);
		logger.debug(compreadBudgetXml);
		return compreadBudgetXml;
	}
	
	private void compareExpenses(Element compExpenses, Element currExpenses, Document compDoc){
		List<String> indexes = Lists.newArrayList();
		indexes.add("description");
		indexes.add("type");
		indexes.add("subtype");
		Map<String,Element> compExps = getIndexElementMap(compExpenses.getElementsByTagName("expense"),indexes);
		Map<String,Element> currExps = getIndexElementMap(currExpenses.getElementsByTagName("expense"),indexes);
		
		Set<String> compExpIndexes = compExps.keySet();
		Set<String> currExpIndexes = currExps.keySet();
		// check added exps
		for (String expIndex : currExpIndexes) {
			try {
				Element currExpEle = compExps.get(expIndex);

				if (!compExpIndexes.contains(expIndex)) {
					// adding the new exp element
					Element addingExpEle = compDoc.createElement("expense");

					addingExpEle = copyElement(addingExpEle,
							currExps.get(expIndex),false);
					addingExpEle.setAttribute("diff", "A");

					compExpenses.appendChild(addingExpEle);
				} else {
					// check modified exps
					Element compExpEle = compExps.get(expIndex);
					compareElements(compExpEle, currExpEle, procecureAttributes);

				}
			} catch(Exception e){
						logger.debug("Error: Adding Exps Failed!!!");
					}
		}
		
		//check delete pro...
		deleteElementCheck(compExpIndexes,currExpIndexes,compExps);
	}
	
	private void compareEpoch(Map<String,Element> compEpochs, Map<String,Element> currEpochs, Document compDoc){
		Element epochsEle = (Element) compDoc.getElementsByTagName("epochs").item(0);
		
		Set<String> compEpochIndexes = compEpochs.keySet();
		Set<String> currEpochIndexes = currEpochs.keySet();
		//check added epochs
		for(String epochIndex : currEpochIndexes){
			try{
				Element currEpochEle = currEpochs.get(epochIndex);
				if(!compEpochIndexes.contains(epochIndex)){
					
					//adding the new epoch element
					Element addingEpochEle = compDoc.createElement("epoch");
					Element addingNotesEle = compDoc.createElement("notes");
					addingNotesEle.setTextContent(currEpochs.get(epochIndex).getElementsByTagName("notes").item(0).getTextContent());
					addingEpochEle = copyElement(addingEpochEle,currEpochs.get(epochIndex),false);
					addingEpochEle.setAttribute("diff", "A");
					addingEpochEle.appendChild(addingNotesEle);
					epochsEle.appendChild(addingEpochEle);
					//add arms
					comapreArms(addingEpochEle, currEpochEle, compDoc);
					//add procedures
					compareProcedures(addingEpochEle, currEpochEle, compDoc);
				}else{
					//epoch exist, check if it is modified
					Element compEpochEle = compEpochs.get(epochIndex);
					compareElements(compEpochEle,currEpochEle,epochAttributes);
					
					//check arms.......
					comapreArms(compEpochEle, currEpochEle, compDoc);
					//check procedures
					compareProcedures(compEpochEle, currEpochEle, compDoc);
				}
				
			}catch(Exception e){
				e.printStackTrace();
				logger.debug("Error: Adding Epoch Failed!!!");
			}
		}
		
		//check deleted epochs
		deleteElementCheck(compEpochIndexes,currEpochIndexes,compEpochs);
		
	}
	
	private void comapreArms(Element compEpochEle, Element currEpochEle, Document compDoc){
		List<String> indexes = Lists.newArrayList();
		indexes.add("id");
		Map<String,Element> compArms = getIndexElementMap(compEpochEle.getElementsByTagName("arm"),indexes);
		Map<String,Element> currArms = getIndexElementMap(currEpochEle.getElementsByTagName("arm"),indexes);
	
		Set<String> compArmIndexes = compArms.keySet();
		Set<String> currArmIndexes = currArms.keySet();
		
		Element armsEle = null;
		if(compEpochEle.getElementsByTagName("arms").getLength()>0){
			armsEle = (Element) compEpochEle.getElementsByTagName("arms").item(0);
		}else{
			armsEle = compDoc.createElement("arms");
			compEpochEle.appendChild(armsEle);
		}
		
		//create procedure lists for vp comparison
		Map<String,String> compProceduresMap = Maps.newHashMap();
		Map<String,String> currProceduresMap = Maps.newHashMap();
		NodeList compProcedures = compEpochEle.getElementsByTagName("procedure");
		NodeList currProcedures = currEpochEle.getElementsByTagName("procedure");
		for (int i = 0; i < compProcedures.getLength(); i++) {
					
			Element compProcedure = (Element) compProcedures.item(i);
			Element miscCost = (Element) compProcedure.getElementsByTagName("misc").item(0);
					
			compProceduresMap.put(compProcedure.getAttribute("id"), compProcedure.getAttribute("cptcode")+compProcedure.getAttribute("description")+miscCost.getTextContent());
		}
				
		for (int i = 0; i < currProcedures.getLength(); i++) {
					
			Element currProcedure = (Element) currProcedures.item(i);
			Element miscCost = (Element) currProcedure.getElementsByTagName("misc").item(0);
			currProceduresMap.put(currProcedure.getAttribute("id"), currProcedure.getAttribute("cptcode")+currProcedure.getAttribute("description")+miscCost.getTextContent());
		}		
		
		//check added arms
		for(String armIndex: currArmIndexes){
			try{
				Element currArmEle = currArms.get(armIndex);
				
				if(!compArmIndexes.contains(armIndex)){
					//adding the new arm element
					Element addingArmEle = compDoc.createElement("arm");
					Element addingNotesEle = compDoc.createElement("notes");
					addingNotesEle.setTextContent(currArms.get(armIndex).getElementsByTagName("notes").item(0).getTextContent());

					addingArmEle = copyElement(addingArmEle,currArms.get(armIndex),false);
					addingArmEle.appendChild(addingNotesEle);
					addingArmEle.setAttribute("diff", "A");
					
					armsEle.appendChild(addingArmEle);
					
					//add cycles...
					comapreCycles(addingArmEle, currArmEle, compDoc,compProceduresMap,currProceduresMap);
				}else{
					//check modified arms
					Element compArmEle = compArms.get(armIndex);
					
					compareElements(compArmEle,currArmEle,armAttributes);
					
					//check cycles...
					comapreCycles(compArmEle, currArmEle, compDoc,compProceduresMap,currProceduresMap);
				}
			}catch(Exception e){
				e.printStackTrace();
				logger.debug("Error: Adding Arm Failed!!!");
			}
		}
		
		//check delete arms...
		deleteElementCheck(compArmIndexes,currArmIndexes,compArms);
		
	}
	
	private void comapreCycles(Element compArmEle, Element currArmEle, Document compDoc,Map<String,String> compProceduresMap,Map<String,String> currProceduresMap){
		List<String> indexes = Lists.newArrayList();
		indexes.add("id");
		Map<String,Element> compCycles = getIndexElementMap(compArmEle.getElementsByTagName("cycle"),indexes);
		Map<String,Element> currCycles = getIndexElementMap(currArmEle.getElementsByTagName("cycle"),indexes);
	
		Set<String> compCycleIndexes = compCycles.keySet();
		Set<String> currCycleIndexes = currCycles.keySet();
		
		Element cyclesEle = null;
		if(compArmEle.getElementsByTagName("cycles").getLength()>0){
			cyclesEle = (Element) compArmEle.getElementsByTagName("cycles").item(0);
		}else{
			cyclesEle = compDoc.createElement("cycles");
			compArmEle.appendChild(cyclesEle);
		}
		
		//check added cycles
		for(String cycleIndex: currCycleIndexes){
			try{
				Element currCycleEle = currCycles.get(cycleIndex);
				
				if(!compCycleIndexes.contains(cycleIndex)){
					//adding the new cycle element
					Element addingCycleEle = compDoc.createElement("cycle");
					Element addingNotesEle = compDoc.createElement("notes");
					addingNotesEle.setTextContent(currCycles.get(cycleIndex).getElementsByTagName("notes").item(0).getTextContent());

					addingCycleEle = copyElement(addingCycleEle,currCycles.get(cycleIndex),false);
					addingCycleEle.appendChild(addingNotesEle);
					addingCycleEle.setAttribute("diff", "A");
					
					cyclesEle.appendChild(addingCycleEle);
					
					//add visit...
					compareVisit(addingCycleEle, currCycleEle, compDoc,compProceduresMap,currProceduresMap);
				}else{
					//check modified cycles
					Element compCycleEle = compCycles.get(cycleIndex);
					
					compareElements(compCycleEle,currCycleEle,cycleAttributes);
					
					//check visit...
					compareVisit(compCycleEle, currCycleEle, compDoc,compProceduresMap,currProceduresMap);
				}
			}catch(Exception e){
				e.printStackTrace();
				logger.debug("Error: Adding Cycle Failed!!!");
			}
		}
		
		//check delete cycles...
		deleteElementCheck(compCycleIndexes,currCycleIndexes,compCycles);
		
	}
	
	private void compareVisit(Element compCycleEle, Element currCycleEle, Document compDoc,Map<String,String> compProceduresMap,Map<String,String> currProceduresMap){
		List<String> indexes = Lists.newArrayList();
		indexes.add("id");
		Map<String,Element> compVisits = getIndexElementMap(compCycleEle.getElementsByTagName("visit"),indexes);
		Map<String,Element> currVisits = getIndexElementMap(currCycleEle.getElementsByTagName("visit"),indexes);
	
		Set<String> compVisitIndexes = compVisits.keySet();
		Set<String> currVisitIndexes = currVisits.keySet();

		Element visitsEle = null;
		if(compCycleEle.getElementsByTagName("visits").getLength()>0){
			visitsEle = (Element) compCycleEle.getElementsByTagName("visits").item(0);
		}else{
			visitsEle = compDoc.createElement("visits");
			compCycleEle.appendChild(visitsEle);
		}
		
		
		//check added visits
		for(String visitIndex: currVisitIndexes){
			try{
				Element currVisitEle = currVisits.get(visitIndex);
				
				if(!compVisitIndexes.contains(visitIndex)){
					//adding the new visit element
					Element addingVisitEle = compDoc.createElement("visit");
					Element addingNotesEle = compDoc.createElement("notes");
					addingNotesEle.setTextContent(currVisits.get(visitIndex).getElementsByTagName("notes").item(0).getTextContent());
					
					addingVisitEle = copyElement(addingVisitEle,currVisits.get(visitIndex),false);
					addingVisitEle.appendChild(addingNotesEle);
					
					addingVisitEle.setAttribute("diff", "A");
					
					visitsEle.appendChild(addingVisitEle);
					
					//adding vp
					compareVp(addingVisitEle, currVisitEle, compDoc, compProceduresMap, currProceduresMap);
					
				}else{
					//check modified visits
					Element compVisitEle = compVisits.get(visitIndex);
					compareElements(compVisitEle,currVisitEle,visitAttributes);
					
					//check vp...
					compareVp(compVisitEle, currVisitEle, compDoc, compProceduresMap, currProceduresMap);
				}
			}catch(Exception e){
				e.printStackTrace();
				logger.debug("Error: Adding Visit Failed!!!");
			}
		}
		
		//check delete visits...
		deleteElementCheck(compVisitIndexes,currVisitIndexes,compVisits);
		
	}
	
	private void compareVp(Element compVisitEle, Element currVisitEle, Document compDoc,Map<String,String> compProceduresMap,Map<String,String> currProceduresMap){
		List<String> indexes = Lists.newArrayList();
		indexes.add("pid");
		Map<String,Element> compVps = getIndexElementMap(compVisitEle.getElementsByTagName("vp"),indexes);
		Map<String,Element> currVps = getIndexElementMap(currVisitEle.getElementsByTagName("vp"),indexes);
		
		
		Map<String,Element> compVpsMapbyCpt = Maps.newHashMap();
		Map<String,Element> currVpsMapbyCpt = Maps.newHashMap();
		
		for(String pid :compVps.keySet()){
			compVpsMapbyCpt.put(compProceduresMap.get(pid), compVps.get(pid));
		}
		
		for(String pid :currVps.keySet()){
			currVpsMapbyCpt.put(currProceduresMap.get(pid), currVps.get(pid));
		}
		
		Set<String> compVpIndexes = compVps.keySet();
		Set<String> currVpIndexes = currVps.keySet();
		Element vpsEle = null;
		if(compVisitEle.getElementsByTagName("visitprocedures").getLength()>0){
			vpsEle = (Element) compVisitEle.getElementsByTagName("visitprocedures").item(0);
		}else{
			vpsEle = compDoc.createElement("visitprocedures");
			compVisitEle.appendChild(vpsEle);
		}
		
		for(String vpIndex : currVpIndexes){
			try{
				Element currVpEle = currVps.get(vpIndex);
				
				if(!compVpsMapbyCpt.keySet().contains(currProceduresMap.get(vpIndex))){
					//adding vp
					Element addingVpEle = compDoc.createElement("vp");
					addingVpEle = copyElement(addingVpEle,currVpEle,false);
					addingVpEle.setAttribute("diff", "A");
					vpsEle.appendChild(addingVpEle);
					
				}else{
					//check modified vp
					Element compVpEle = compVpsMapbyCpt.get(currProceduresMap.get(vpIndex));
					compareElements(compVpEle,currVpEle,vpAttributes);
					compVpEle.setAttribute("pid", currVpEle.getAttribute("pid"));
				}
				
			}catch(Exception e){
				e.printStackTrace();
				logger.debug("Error: Adding VP Failed!!!");
			}
		}
		
		//delete vp 
		for(String vpIndex : compVpIndexes){
				if(!currVpsMapbyCpt.keySet().contains(compProceduresMap.get(vpIndex))){
					//delete vp
					Element deletedVpEle = compVps.get(vpIndex);
					deletedVpEle.setAttribute("diff", "D");
				}
		}
	}
	
	private void compareSubProcedures(Element compParentPro,
			Element currParentPro, Document compDoc) {
		List<String> indexes = Lists.newArrayList();
		indexes.add("cptcode");
		indexes.add("description");
		Map<String, Element> compSubPros = getIndexElementMap(
				compParentPro.getElementsByTagName("procedure"), indexes);
		Map<String, Element> currSubPros = getIndexElementMap(
				currParentPro.getElementsByTagName("procedure"), indexes);
		Element subproceduresEle = null;
		if (compParentPro.getElementsByTagName("subprocedures").getLength() > 0) {
			subproceduresEle = (Element) compParentPro.getElementsByTagName(
					"subprocedures").item(0);
		} else {
			subproceduresEle = compDoc.createElement("subprocedures");
			compParentPro.appendChild(subproceduresEle);
		}
		Set<String> compSubProIndexes = compSubPros.keySet();
		Set<String> currSubProIndexes = currSubPros.keySet();
		// check added procedures
		for (String procedureIndex : currSubProIndexes) {
			try {
				Element currProceudreEle = currSubPros.get(procedureIndex);
				// check if this one is subprocedure
				if (!compSubProIndexes.contains(procedureIndex)) {

					// adding the new procedure element
					Element addingProceudreEle = compDoc
							.createElement("procedure");
					Element addingNotesEle = compDoc.createElement("notes");
					addingNotesEle.setTextContent(currSubPros
							.get(procedureIndex).getElementsByTagName("notes")
							.item(0).getTextContent());
					
					Element addingCovNotesEle = compDoc.createElement("coverage-notes");
					addingCovNotesEle.setTextContent(currSubPros
							.get(procedureIndex).getElementsByTagName("coverage-notes")
							.item(0).getTextContent());
					
					Element addingCliNotesEle = compDoc.createElement("clinical-notes");
					addingCliNotesEle.setTextContent(currSubPros
							.get(procedureIndex).getElementsByTagName("clinical-notes")
							.item(0).getTextContent());
					
					Element addingCostEle = compDoc.createElement("cost");
					Element targetCostEle = (Element) currSubPros
							.get(procedureIndex).getElementsByTagName("cost")
							.item(0);
					
					Element addingCostMiscEle = compDoc.createElement("misc");
					addingCostMiscEle.setTextContent(targetCostEle.getElementsByTagName("misc")
							.item(0).getTextContent());
					Element addingCostSponsorEle = compDoc.createElement("sponsor");
					addingCostSponsorEle.setTextContent(targetCostEle.getElementsByTagName("sponsor")
							.item(0).getTextContent());
					Element addingCostPriceEle = compDoc.createElement("price");
					addingCostPriceEle.setTextContent(targetCostEle.getElementsByTagName("price")
							.item(0).getTextContent());
					Element addingCostResidualEle = compDoc.createElement("residual");
					addingCostResidualEle.setTextContent(targetCostEle.getElementsByTagName("residual")
							.item(0).getTextContent());
					addingCostEle.appendChild(addingCostMiscEle);
					addingCostEle.appendChild(addingCostSponsorEle);
					addingCostEle.appendChild(addingCostPriceEle);
					addingCostEle.appendChild(addingCostResidualEle);
					
					
					Element addingHospEle = compDoc.createElement("hosp");
					Element addingPhysEle = compDoc.createElement("phys");
					
					addingHospEle = copyElement(addingHospEle,
							(Element)currSubPros.get(procedureIndex).getElementsByTagName("hosp").item(0), false);
					addingPhysEle = copyElement(addingPhysEle,
							(Element)currSubPros.get(procedureIndex).getElementsByTagName("phys").item(0), false);
					
					
					addingProceudreEle = copyElement(addingProceudreEle,
							currSubPros.get(procedureIndex), false);
					addingProceudreEle.appendChild(addingNotesEle);
					addingProceudreEle.appendChild(addingCliNotesEle);
					addingProceudreEle.appendChild(addingCostEle);
					addingProceudreEle.appendChild(addingCovNotesEle);
					addingProceudreEle.appendChild(addingHospEle);
					addingProceudreEle.appendChild(addingPhysEle);
					addingProceudreEle.setAttribute("diff", "A");

					subproceduresEle.appendChild(addingProceudreEle);

				} else {
					// check modified procedures
					Element compProceudreEle = compSubPros.get(procedureIndex);

					compareElements(compProceudreEle, currProceudreEle,
							procecureAttributes);
					compProceudreEle.setAttribute("id",
							currProceudreEle.getAttribute("id"));
				}

			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("Error: Adding SubProcedures Failed!!!");
			}
		}

	}

	private void compareProcedures(Element compEpoch, Element currEpoch, Document compDoc){
		List<String> indexes = Lists.newArrayList();
		indexes.add("cptcode");
		indexes.add("description");
		Map<String,Element> compPros = getIndexElementMap(compEpoch.getElementsByTagName("procedure"),indexes);
		Map<String,Element> currPros = getIndexElementMap(currEpoch.getElementsByTagName("procedure"),indexes);
		
		
		Set<String> compProIndexes = compPros.keySet();
		Set<String> currProIndexes = currPros.keySet();
		
		Element proceduresEle = null;
		if(compEpoch.getElementsByTagName("procedures").getLength()>0){
			proceduresEle = (Element) compEpoch.getElementsByTagName("procedures").item(0);
		}else{
			proceduresEle = compDoc.createElement("procedures");
			compEpoch.appendChild(proceduresEle);
		}
		
		
	
		// check added procedures
		for (String procedureIndex : currProIndexes) {
			try {
				Element currProceudreEle = currPros.get(procedureIndex);
				//check if this one is subprocedure
				if(currProceudreEle.getParentNode().getNodeName().equals("subprocedures")){
					//this is subprocedure, does not process here
					continue;
				}
				if (!compProIndexes.contains(procedureIndex)) {
					
					
					// adding the new procedure element
					Element addingProceudreEle = compDoc.createElement("procedure");
					Element addingNotesEle = compDoc.createElement("notes");
					addingNotesEle.setTextContent(currPros.get(procedureIndex)
							.getElementsByTagName("notes").item(0)
							.getTextContent());
					
					
					Element addingCliNotesEle = compDoc.createElement("clinical-notes");
					addingCliNotesEle.setTextContent(currPros
							.get(procedureIndex).getElementsByTagName("clinical-notes")
							.item(0).getTextContent());
					
					Element addingCostEle = compDoc.createElement("cost");
					Element targetCostEle = (Element) currPros
							.get(procedureIndex).getElementsByTagName("cost")
							.item(0);
					
					Element addingCostMiscEle = compDoc.createElement("misc");
					addingCostMiscEle.setTextContent(targetCostEle.getElementsByTagName("misc")
							.item(0).getTextContent());
					Element addingCostSponsorEle = compDoc.createElement("sponsor");
					addingCostSponsorEle.setTextContent(targetCostEle.getElementsByTagName("sponsor")
							.item(0).getTextContent());
					Element addingCostPriceEle = compDoc.createElement("price");
					addingCostPriceEle.setTextContent(targetCostEle.getElementsByTagName("price")
							.item(0).getTextContent());
					Element addingCostResidualEle = compDoc.createElement("residual");
					addingCostResidualEle.setTextContent(targetCostEle.getElementsByTagName("residual")
							.item(0).getTextContent());
					addingCostEle.appendChild(addingCostMiscEle);
					addingCostEle.appendChild(addingCostSponsorEle);
					addingCostEle.appendChild(addingCostPriceEle);
					addingCostEle.appendChild(addingCostResidualEle);
					
					Element addingCovNotesEle = compDoc.createElement("coverage-notes");
					addingCovNotesEle.setTextContent(currPros
							.get(procedureIndex).getElementsByTagName("coverage-notes")
							.item(0).getTextContent());
					
					Element addingHospEle = compDoc.createElement("hosp");
					Element addingPhysEle = compDoc.createElement("phys");
					
					addingHospEle = copyElement(addingHospEle,
							(Element)currPros.get(procedureIndex).getElementsByTagName("hosp").item(0), false);
					addingPhysEle = copyElement(addingPhysEle,
							(Element)currPros.get(procedureIndex).getElementsByTagName("phys").item(0), false);

					addingProceudreEle = copyElement(addingProceudreEle,
							currPros.get(procedureIndex),false);
					addingProceudreEle.appendChild(addingNotesEle);
					addingProceudreEle.appendChild(addingCliNotesEle);
					addingProceudreEle.appendChild(addingCovNotesEle);
					addingProceudreEle.appendChild(addingCostEle);
					addingProceudreEle.appendChild(addingHospEle);
					addingProceudreEle.appendChild(addingPhysEle);
					addingProceudreEle.setAttribute("diff", "A");

					proceduresEle.appendChild(addingProceudreEle);
					
					//check if this procedure has subprocedures
					if(currProceudreEle.getElementsByTagName("subprocedures").getLength()>0){
						compareSubProcedures(addingProceudreEle, currProceudreEle,compDoc);
					}
				} else {
					// check modified procedures
					Element compProceudreEle = compPros.get(procedureIndex);
					
					compareElements(compProceudreEle, currProceudreEle, procecureAttributes);
					compProceudreEle.setAttribute("id", currProceudreEle.getAttribute("id"));
					
					//check if this procedure has subprocedures
					if(currProceudreEle.getElementsByTagName("subprocedures").getLength()>0){
						compareSubProcedures(compProceudreEle, currProceudreEle,compDoc);
					}
					
				}
				
			} catch(Exception e){
				e.printStackTrace();
				logger.debug("Error: Adding Procedures Failed!!!");
			   }
		}
		//check delete procedure...
		deleteElementCheck(compProIndexes,currProIndexes,compPros);
	}


	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public List<String> getEpochAttributes() {
		return epochAttributes;
	}

	public List<String> getArmAttributes() {
		return armAttributes;
	}

	public List<String> getCycleAttributes() {
		return cycleAttributes;
	}

	public List<String> getVpAttributes() {
		return vpAttributes;
	}

	public List<String> getProcecureAttributes() {
		return procecureAttributes;
	}

	public List<String> getExpenseAttributes() {
		return expenseAttributes;
	}

}
