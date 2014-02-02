package edu.uams.clara.webapp.xml.processor.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.webapp.xml.processor.BudgetXmlDifferService;
import edu.uams.clara.webapp.xml.processor.BudgetXmlTransformService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class BudgetXmlDifferServiceImpl implements BudgetXmlDifferService {
	private final static Logger logger = LoggerFactory
			.getLogger(BudgetXmlTransformService.class);
	private XmlProcessor xmlProcessor;
	
	private void checkExpense(NodeList oldExpList,NodeList newExpList,Element oldExps,Document oldBudget){
		String newExpId;
		String oldExpId;
		List<String> newExpIdList = new ArrayList<String>();
		List<String> oldExpIdList = new ArrayList<String>();
		
		for (int newExpLen = 0; newExpLen < newExpList.getLength(); newExpLen++) {

			Element newExpense = (Element) newExpList.item(newExpLen);
			newExpId = newExpense.getAttribute("id");
			newExpIdList.add(newExpId);

		}
		
		for (int oldExpLen = 0; oldExpLen < oldExpList.getLength(); oldExpLen++) {

			Element oldExpense = (Element) oldExpList.item(oldExpLen);
			oldExpId = oldExpense.getAttribute("id");
			oldExpIdList.add(oldExpId);

		}
		
		for (int i = 0; i < newExpIdList.size(); i++) {
			if (!oldExpIdList.contains(newExpIdList.get(i))) {
				Element newExpense = (Element) newExpList.item(i);
				Element addExpense = oldBudget.createElement("expense");
				oldExps.appendChild(addExpense);

				addExpense.setAttribute("diff", "A");

				NamedNodeMap newExpAttrs = newExpense.getAttributes();
				for (int k = 0; k < newExpAttrs.getLength(); k++) {
					addExpense.setAttribute(newExpAttrs.item(k).getNodeName(),
							newExpense.getAttribute(newExpAttrs.item(k)
									.getNodeName()));
				}

				oldExpIdList.add(addExpense.getAttribute("id"));
			} else
			{

				int ExpIndex = oldExpIdList.indexOf(newExpIdList.get(i));

				Element oldExpense = (Element) oldExpList.item(ExpIndex);

				Element newExpense = (Element) newExpList.item(i);

				compExpense(oldExpense, newExpense);

			}
		}
		
		for (int i = 0; i < oldExpIdList.size(); i++) {
			if (!newExpIdList.contains(oldExpIdList.get(i))) {
				// delete
				Element oldExpense = (Element) oldExpList.item(i);
				oldExpense.setAttribute("diff", "D");

			}

		}
		
		
	}

	private void checkEpoch(NodeList oldEpochList, NodeList newEpochList,
			Document oldBudget, Element oldEpochs) {
		String newEpochId;
		String oldEpochId;
		List<String> newEpIdList = new ArrayList<String>();
		List<String> oldEpIdList = new ArrayList<String>();

		// create epoch list for new budget
		for (int newEpochLen = 0; newEpochLen < newEpochList.getLength(); newEpochLen++) {

			Element newEpoch = (Element) newEpochList.item(newEpochLen);
			newEpochId = newEpoch.getAttribute("id");
			newEpIdList.add(newEpochId);

		}

		// create epoch list for old budget
		for (int oldEpochLen = 0; oldEpochLen < oldEpochList.getLength(); oldEpochLen++) {

			Element oldEpoch = (Element) oldEpochList.item(oldEpochLen);
			oldEpochId = oldEpoch.getAttribute("id");
			oldEpIdList.add(oldEpochId);
		}

		// check added or modified epochs
		for (int i = 0; i < newEpIdList.size(); i++) {
			// check added epoch
			if (!oldEpIdList.contains(newEpIdList.get(i))) {
				Element newEpoch = (Element) newEpochList.item(i);
				Element addEpoch = oldBudget.createElement("epoch");
				oldEpochs.appendChild(addEpoch);

				addEpoch.setAttribute("diff", "A");

				NamedNodeMap newEpochAttrs = newEpoch.getAttributes();
				for (int k = 0; k < newEpochAttrs.getLength(); k++) {
					addEpoch.setAttribute(newEpochAttrs.item(k).getNodeName(),
							newEpoch.getAttribute(newEpochAttrs.item(k)
									.getNodeName()));
				}

				// update epoch list in old budget
				oldEpIdList.add(addEpoch.getAttribute("id"));
				// compare epochs
				compEpoch(addEpoch, newEpoch);

				// begin to check cycles
				checkArm(addEpoch, newEpoch, oldBudget);
			} else{

				int EpochIndex = oldEpIdList.indexOf(newEpIdList.get(i));

				Element oldEpoch = (Element) oldEpochList.item(EpochIndex);

				Element newEpoch = (Element) newEpochList.item(i);

				// compare epochs
				compEpoch(oldEpoch, newEpoch);

				// begin to check cycles
				checkArm(oldEpoch, newEpoch, oldBudget);
			}
		}

		// check deleted epochs in new budget
		for (int i = 0; i < oldEpIdList.size(); i++) {
			if (!newEpIdList.contains(oldEpIdList.get(i))) {
				// delete
				Element oldEpoch = (Element) oldEpochList.item(i);
				oldEpoch.setAttribute("diff", "D");

			}

		}

	}
    
	private void checkArm(Element oldEpoch, Element newEpoch, Document oldBudget) {
		String newArmId;
		String oldArmId;
		List<String> newArmIdList = new ArrayList<String>();
		List<String> oldArmIdList = new ArrayList<String>();
		
		NodeList oldArmList = oldEpoch.getElementsByTagName("arm");
		NodeList newArmList = newEpoch.getElementsByTagName("arm");
		
		NodeList ArmsList = oldEpoch.getElementsByTagName("arms");
		
		Element Arms = (Element) ArmsList.item(0);
		if(ArmsList.getLength()==0){
			Element addArms = oldBudget.createElement("arms");
			oldEpoch.appendChild(addArms);
			ArmsList = oldEpoch.getElementsByTagName("arms");
			Arms = (Element) ArmsList.item(0);
		}
		
		
		// create arms list for epochs in new budget
		for (int newArmLen = 0; newArmLen < newArmList.getLength(); newArmLen++) {

			Element newArm = (Element) newArmList.item(newArmLen);
			newArmId = newArm.getAttribute("id");
			newArmIdList.add(newArmId);

		}

		// create arms list for epochs in old budget
		for (int oldArmLen = 0; oldArmLen < oldArmList.getLength(); oldArmLen++) {

			Element oldArm = (Element) oldArmList.item(oldArmLen);
			oldArmId = oldArm.getAttribute("id");
			oldArmIdList.add(oldArmId);

		}
		// check added or modified arms
				for (int i = 0; i < newArmIdList.size(); i++) {
					// check added arm
					if (!oldArmIdList.contains(newArmIdList.get(i))) {
						
						Element newArm = (Element) newArmList.item(i);
						Element addArm = oldBudget.createElement("arm");
						Element ntoes = oldBudget.createElement("notes");

						Arms.appendChild(addArm);
						Arms.appendChild(ntoes);

						addArm.setAttribute("diff", "A");
						NamedNodeMap newArmAttrs = newArm.getAttributes();
						for (int k = 0; k < newArmAttrs.getLength(); k++) {
							addArm.setAttribute(newArmAttrs.item(k).getNodeName(),
									newArm.getAttribute(newArmAttrs.item(k)
											.getNodeName()));
						}

						// update arm list in old budget
						oldArmIdList.add(addArm.getAttribute("id"));
						compArm(addArm, newArm);
						checkCycle(addArm, newArm, oldBudget);
					} 
					else{
					// begin to check modified arm with same id
						int ArmIndex = oldArmIdList.indexOf(newArmIdList.get(i));
						Element oldArm = (Element) oldArmList.item(ArmIndex);

						Element newArm = (Element) newArmList.item(i);

						// compare arms
						compArm(oldArm, newArm);

						// begin to check cycles
						checkCycle(oldArm, newArm, oldBudget);
					}
				}
				// delete cycle
				for (int i = 0; i < oldArmIdList.size(); i++) {
					if (!newArmIdList.contains(oldArmIdList.get(i))) {
						// delete
						Element oldArm = (Element) oldArmList.item(i);
						oldArm.setAttribute("diff", "D");
						delCycleForArm(oldArm);
					}

				}
		
	}

	private void checkCycle(Element oldArm, Element newArm,
			Document oldBudget) {
		String newCycleId;
		String oldCycleId;
		List<String> newCylIdList = new ArrayList<String>();
		List<String> oldCylIdList = new ArrayList<String>();
		NodeList oldCycleList = oldArm.getElementsByTagName("cycle");
		NodeList newCycleList = newArm.getElementsByTagName("cycle");
		
		NodeList cyclesList = oldArm.getElementsByTagName("cycles");
		Element cycles = (Element) cyclesList.item(0);

		if(cyclesList.getLength()==0){
			Element addCycless = oldBudget.createElement("cycles");
			oldArm.appendChild(addCycless);
			cyclesList = oldArm.getElementsByTagName("cycles");
			cycles = (Element) cyclesList.item(0);
		}
		
		// create cycle list for epochs in new budget
		for (int newCycleLen = 0; newCycleLen < newCycleList.getLength(); newCycleLen++) {

			Element newCycle = (Element) newCycleList.item(newCycleLen);
			newCycleId = newCycle.getAttribute("id");
			newCylIdList.add(newCycleId);

		}

		// create cycle list for epochs in old budget
		for (int oldCycleLen = 0; oldCycleLen < oldCycleList.getLength(); oldCycleLen++) {

			Element oldCycle = (Element) oldCycleList.item(oldCycleLen);
			oldCycleId = oldCycle.getAttribute("id");
			oldCylIdList.add(oldCycleId);

		}
		//logger.debug(oldCylIdList.get(0)+" "+newCylIdList.get(0));
		// check added or modified cycles
		for (int i = 0; i < newCylIdList.size(); i++) {

			// check added cycle
			if (!oldCylIdList.contains(newCylIdList.get(i))) {
				
				Element newCycle = (Element) newCycleList.item(i);
				Element addCycle = oldBudget.createElement("cycle");

				cycles.appendChild(addCycle);

				addCycle.setAttribute("diff", "A");

				NamedNodeMap newCycleAttrs = newCycle.getAttributes();
				for (int k = 0; k < newCycleAttrs.getLength(); k++) {
					addCycle.setAttribute(newCycleAttrs.item(k).getNodeName(),
							newCycle.getAttribute(newCycleAttrs.item(k)
									.getNodeName()));
				}

				// update cycle list in old budget
				oldCylIdList.add(addCycle.getAttribute("id"));

				addVisitForCycle(addCycle, newCycle, oldBudget);
				// compare cycle
				compCycle(addCycle, newCycle);
				// begin to check cycles
				checkVisit(addCycle, newCycle, oldBudget);
			} 

			// begin to check modified cycles with same id
			else{
				int CycleIndex = oldCylIdList.indexOf(newCylIdList.get(i));
				Element oldCycle = (Element) oldCycleList.item(CycleIndex);

				Element newCycle = (Element) newCycleList.item(i);
				// compare cycle
				compCycle(oldCycle, newCycle);
				// begin to check cycles
				checkVisit(oldCycle, newCycle, oldBudget);
			}
		}

		// delete cycle
		for (int i = 0; i < oldCylIdList.size(); i++) {
			if (!newCylIdList.contains(oldCylIdList.get(i))) {
				// delete
				logger.debug(oldCylIdList.size()+"@@@@@");
				Element oldCycle = (Element) oldCycleList.item(i);
				oldCycle.setAttribute("diff", "D");
				// begin to delete Visit for cycle
				delVisitForCycle(oldCycle);

			}

		}

	}

	private void checkVisit(Element oldCycle, Element newCycle,
			Document oldBudget) {
		String newVisitId;
		String oldVisitId;

		NodeList oldVisitList = oldCycle.getElementsByTagName("visit");
		NodeList newVisitList = newCycle.getElementsByTagName("visit");
		List<String> newVtIdList = new ArrayList<String>();
		List<String> oldVtIdList = new ArrayList<String>();
		
		NodeList VisitsList = oldCycle.getElementsByTagName("visits");
		Element Visits = (Element) VisitsList.item(0);
		
		if(VisitsList.getLength()==0){
			Element addVisits = oldBudget.createElement("visits");
			oldCycle.appendChild(addVisits);
			VisitsList = oldCycle.getElementsByTagName("visits");
			Visits = (Element) VisitsList.item(0);
		}

		// create visit list for cycles in the new budget
		for (int newVlLen = 0; newVlLen < newVisitList.getLength(); newVlLen++) {

			Element newVisit = (Element) newVisitList.item(newVlLen);
			newVisitId = newVisit.getAttribute("id");
			newVtIdList.add(newVisitId);

		}

		// create visit list for cycles in the old budget
		for (int oldVlLen = 0; oldVlLen < oldVisitList.getLength(); oldVlLen++) {
			Element oldVisit = (Element) oldVisitList.item(oldVlLen);
			oldVisitId = oldVisit.getAttribute("id");
			oldVtIdList.add(oldVisitId);

		}

		// check added or modified visit
		for (int i = 0; i < newVtIdList.size(); i++) {
			if (!oldVtIdList.contains(newVtIdList.get(i))) {

				Element newVisit = (Element) newVisitList.item(i);
				Element addVisit = oldBudget.createElement("visit");
				Visits.appendChild(addVisit);

				addVisit.setAttribute("diff", "A");

				NamedNodeMap newVisitAttrs = newVisit.getAttributes();
				for (int k = 0; k < newVisitAttrs.getLength(); k++) {
					addVisit.setAttribute(newVisitAttrs.item(k).getNodeName(),
							newVisit.getAttribute(newVisitAttrs.item(k)
									.getNodeName()));
				}

				// update visit list in old budget
				oldVtIdList.add(addVisit.getAttribute("id"));

				addVpForVisit(addVisit, newVisit, oldBudget);
				checkVp(addVisit, newVisit, oldBudget);
			}
			// check modified visit with the same id
			else{
				int visitIndex = oldVtIdList.indexOf(newVtIdList.get(i));
				Element oldVisit = (Element) oldVisitList.item(visitIndex);

				Element newVisit = (Element) newVisitList.item(i);

				checkVp(oldVisit, newVisit, oldBudget);
			}

		}

		// check deleted visit
		for (int i = 0; i < oldVtIdList.size(); i++) {
			if (!newVtIdList.contains(oldVtIdList.get(i))) {
				// delete visit
				Element oldVisit = (Element) oldVisitList.item(i);
				oldVisit.setAttribute("diff", "D");

				// begin to delete vp
				delVpForVisit(oldVisit);

			}

		}

	}

	private void compExpense(Element oldExpense, Element newExpense){
		if (!oldExpense.getAttribute("fa")
				.equals(newExpense.getAttribute("fa"))) {
			oldExpense.setAttribute("fa", newExpense.getAttribute("fa"));
			oldExpense.setAttribute("diff", "M");
		}
		
		if (!oldExpense.getAttribute("count")
				.equals(newExpense.getAttribute("count"))) {
			oldExpense.setAttribute("count", newExpense.getAttribute("count"));
			oldExpense.setAttribute("diff", "M");
		}
		
		if (!oldExpense.getAttribute("cost")
				.equals(newExpense.getAttribute("cost"))) {
			oldExpense.setAttribute("cost", newExpense.getAttribute("cost"));
			oldExpense.setAttribute("diff", "M");
		}
		
		if (!oldExpense.getAttribute("type")
				.equals(newExpense.getAttribute("type"))) {
			oldExpense.setAttribute("type", newExpense.getAttribute("type"));
			oldExpense.setAttribute("diff", "M");
		}
		
		if (!oldExpense.getAttribute("subtype")
				.equals(newExpense.getAttribute("subtype"))) {
			oldExpense.setAttribute("subtype", newExpense.getAttribute("subtype"));
			oldExpense.setAttribute("diff", "M");
		}
		
		if (!oldExpense.getAttribute("description")
				.equals(newExpense.getAttribute("description"))) {
			oldExpense.setAttribute("description", newExpense.getAttribute("description"));
			oldExpense.setAttribute("diff", "M");
		}
		
		if (!oldExpense.getAttribute("notes")
				.equals(newExpense.getAttribute("notes"))) {
			oldExpense.setAttribute("notes", newExpense.getAttribute("notes"));
			oldExpense.setAttribute("diff", "M");
		}
		
		
	}
	private void compEpoch(Element oldEpoch, Element newEpoch) {
		if (!oldEpoch.getAttribute("name")
				.equals(newEpoch.getAttribute("name"))) {
			oldEpoch.setAttribute("name", newEpoch.getAttribute("name"));
			oldEpoch.setAttribute("diff", "M");
		}
	}
	
	private void compArm(Element oldArm, Element newArm){
		if (!oldArm.getAttribute("name")
				.equals(newArm.getAttribute("name"))) {
			oldArm.setAttribute("name", newArm.getAttribute("name"));
			oldArm.setAttribute("diff", "M");
		}
	}

	private void compCycle(Element oldCycle, Element newCycle) {
		if (!oldCycle.getAttribute("name")
				.equals(newCycle.getAttribute("name"))) {
			oldCycle.setAttribute("name", newCycle.getAttribute("name"));
			oldCycle.setAttribute("diff", "M");
		}

		if (!oldCycle.getAttribute("repetitions").equals(
				newCycle.getAttribute("repetitions"))) {
			oldCycle.setAttribute("repetitions",
					newCycle.getAttribute("repetitions"));
			oldCycle.setAttribute("diff", "M");
		}

		if (!oldCycle.getAttribute("startday").equals(
				newCycle.getAttribute("startday"))) {
			oldCycle.setAttribute("startday", newCycle.getAttribute("startday"));
			oldCycle.setAttribute("diff", "M");
		}

		if (!oldCycle.getAttribute("endday").equals(
				newCycle.getAttribute("endday"))) {
			oldCycle.setAttribute("endday", newCycle.getAttribute("endday"));
			oldCycle.setAttribute("diff", "M");
		}

		if (!oldCycle.getAttribute("durationunit").equals(
				newCycle.getAttribute("durationunit"))) {
			oldCycle.setAttribute("durationunit",
					newCycle.getAttribute("durationunit"));
			oldCycle.setAttribute("diff", "M");
		}
	}

	private void addVpForVisit(Element addVisit, Element newVisit,
			Document oldBudget) {
		Element addvisitProcedures = oldBudget.createElement("visitprocedures");
		addVisit.appendChild(addvisitProcedures);
		Element addNotes = oldBudget.createElement("notes");
		addVisit.appendChild(addNotes);

		NodeList newVpList = newVisit.getElementsByTagName("vp");
		
		for (int i = 0; i < newVpList.getLength(); i++) {
			Element newVp = (Element) newVpList.item(i);
			Element addVp = oldBudget.createElement("vp");
			addvisitProcedures.appendChild(addVp);
			addVp.setAttribute("diff", "A");
			addVp.setAttribute("pid", newVp.getAttribute("pid"));
			addVp.setAttribute("t", newVp.getAttribute("t"));
			addVp.setAttribute("r", newVp.getAttribute("r"));
		}
	}

	private void addVisitForCycle(Element addCycle, Element newCycle,
			Document oldBudget) {
		Element addVisits = oldBudget.createElement("visits");
		Element addNotes = oldBudget.createElement("notes");
		addCycle.appendChild(addNotes);
		addCycle.appendChild(addVisits);

		NodeList newVisitList = newCycle.getElementsByTagName("visit");

		for (int i = 0; i < newVisitList.getLength(); i++) {
			Element newVisit = (Element) newVisitList.item(i);
			Element addVisit = oldBudget.createElement("visit");
			addVisits.appendChild(addVisit);
			addVisit.setAttribute("diff", "A");
			addVisit.setAttribute("id", newVisit.getAttribute("id"));
			addVisit.setAttribute("name", newVisit.getAttribute("name"));
			addVisit.setAttribute("cycleindex",
					newVisit.getAttribute("cycleindex"));
			addVisit.setAttribute("unit", newVisit.getAttribute("unit"));
			addVisit.setAttribute("unitvalue",
					newVisit.getAttribute("unitvalue"));
			// add vp for visit
			addVpForVisit(addVisit, newVisit, oldBudget);
		}

	}

	private void delVpForVisit(Element oldVisit) {
		NodeList delVpList = oldVisit.getElementsByTagName("vp");
		for (int i = 0; i < delVpList.getLength(); i++) {
			Element delVp = (Element) delVpList.item(i);
			delVp.setAttribute("diff", "D");
		}
	}

	private void delVisitForCycle(Element oldCycle) {
		NodeList delVisitList = oldCycle.getElementsByTagName("visit");
		for (int i = 0; i < delVisitList.getLength(); i++) {
			Element delVisit = (Element) delVisitList.item(i);
			delVisit.setAttribute("diff", "D");
			delVpForVisit(delVisit);
		}
	}
	
	private void delCycleForArm(Element oldArm) {
		NodeList delCycleList = oldArm.getElementsByTagName("cycle");
		for (int i = 0; i < delCycleList.getLength(); i++) {
			Element delCycle = (Element) delCycleList.item(i);
			delCycle.setAttribute("diff", "D");
			delVisitForCycle(delCycle);
		}
	}

	private void checkVp(Element oldVisit, Element newVisit, Document oldBudget) {

		NodeList oldVpList = oldVisit.getElementsByTagName("vp");
		NodeList newVpList = newVisit.getElementsByTagName("vp");
		NodeList oldVisitProcedureList = oldVisit
				.getElementsByTagName("visitprocedures");
		Element addVisitProcedure = (Element) oldVisitProcedureList.item(0);

		String newVpId;
		String oldVpId;
		List<String> newVpIdList = new ArrayList<String>();
		List<String> oldVpIdList = new ArrayList<String>();
		// generate list for vpid in new list
		for (int newVpLen = 0; newVpLen < newVpList.getLength(); newVpLen++) {
			Element newVp = (Element) newVpList.item(newVpLen);
			newVpId = newVp.getAttribute("pid");
			newVpIdList.add(newVpId);

		}
		// generate list for vpid in old list
		for (int oldVpLen = 0; oldVpLen < oldVpList.getLength(); oldVpLen++) {
			Element oldVp = (Element) oldVpList.item(oldVpLen);
			oldVpId = oldVp.getAttribute("pid");
			oldVpIdList.add(oldVpId);

		}

		// add vp
		for (int i = 0; i < newVpIdList.size(); i++) {
			if (!oldVpIdList.contains(newVpIdList.get(i))) {
				Element newVp = (Element) newVpList.item(i);
				Element addVp = oldBudget.createElement("vp");
				addVisitProcedure.appendChild(addVp);
				addVp.setAttribute("diff", "A");
				addVp.setAttribute("pid", newVp.getAttribute("pid"));

				addVp.setAttribute("t", newVp.getAttribute("t"));
				addVp.setAttribute("r", newVp.getAttribute("r"));
			} else {
				Element newVp = (Element) newVpList.item(i);
				int vpIndex = oldVpIdList.indexOf(newVpIdList.get(i));
				Element oldVp = (Element) oldVpList.item(vpIndex);
				compVp(oldVp, newVp);
			}

		}

		// delete vp
		for (int i = 0; i < oldVpIdList.size(); i++) {
			if (!newVpIdList.contains(oldVpIdList.get(i))) {
				// delete
				Element oldVp = (Element) oldVpList.item(i);
				oldVp.setAttribute("diff", "D");
				// logger.debug(oldVp.getAttribute("diff"));

			}

		}

	}

	private void compVp(Element oldVp, Element newVp) {
		if (!oldVp.getAttribute("t").equals(newVp.getAttribute("t"))) {
			oldVp.setAttribute("t", newVp.getAttribute("t"));
			oldVp.setAttribute("diff", "M");
		}
		if (!oldVp.getAttribute("r").equals(newVp.getAttribute("r"))) {
			oldVp.setAttribute("r", newVp.getAttribute("r"));
			oldVp.setAttribute("diff", "M");
		}

	}

	private void checkProcedure(NodeList oldEpochList, NodeList newEpochList,
			Document oldBudget) {
		
		for (int newEpochLen = 0; newEpochLen < newEpochList.getLength(); newEpochLen++) {
			Element newEpoch = (Element) newEpochList.item(newEpochLen);
			Element oldEpoch = (Element) oldEpochList.item(newEpochLen);
			
			List<String> newProIdList = new ArrayList<String>();
			List<String> oldProIdList = new ArrayList<String>();
			
			Element newProcedures = (Element) newEpoch.getElementsByTagName(
					"procedures").item(0);
			Element oldProcedures = (Element) oldEpoch.getElementsByTagName(
					"procedures").item(0);
			
			if(oldEpoch.getElementsByTagName(
					"procedures").getLength()==0){
				Element addPros = oldBudget.createElement("procedures");
				oldEpoch.appendChild(addPros);
				oldProcedures = (Element) oldEpoch.getElementsByTagName(
						"procedures").item(0);
			}
			
			try{
				
				NodeList oldProcedureList = oldProcedures
						.getElementsByTagName("procedure");
				for (int oldProLen = 0; oldProLen < oldProcedureList.getLength(); oldProLen++) {
					Element oldProcedure = (Element) oldProcedureList
							.item(oldProLen);
					oldProIdList.add(oldProcedure.getAttribute("id"));
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			try{
				NodeList newProcedureList = newProcedures
						.getElementsByTagName("procedure");
				for (int newProLen = 0; newProLen < newProcedureList.getLength(); newProLen++) {
					Element newProcedure = (Element) newProcedureList
							.item(newProLen);
					newProIdList.add(newProcedure.getAttribute("id"));
				}
				for (int i = 0; i < newProIdList.size(); i++) {
					if (!oldProIdList.contains(newProIdList.get(i))) {
						// delete

						Element newPro = (Element) newProcedureList.item(i);
						Element addPro = oldBudget.createElement("procedure");

						oldProcedures.appendChild(addPro);
						addPro.setAttribute("diff", "A");

						NamedNodeMap newProAttrs = newPro.getAttributes();
						for (int k = 0; k < newProAttrs.getLength(); k++) {
							addPro.setAttribute(newProAttrs.item(k).getNodeName(),
									newPro.getAttribute(newProAttrs.item(k)
											.getNodeName()));

							// logger.debug(newProChildAttrs.item(k).getNodeName());

						}

						NodeList newProChildList = newPro.getChildNodes();
						for (int newProChildLen = 0; newProChildLen < newProChildList
								.getLength(); newProChildLen++) {
							// logger.debug(newProChildList.item(newProChildLen)
							// .getNodeName());
							Element addProChild = oldBudget
									.createElement(newProChildList.item(
											newProChildLen).getNodeName());
							Element newProChild = (Element) newProChildList
									.item(newProChildLen);
							addPro.appendChild(addProChild);

							NamedNodeMap newProChildAttrs = newProChild
									.getAttributes();
							for (int k = 0; k < newProChildAttrs.getLength(); k++) {
								addProChild.setAttribute(newProChildAttrs.item(k)
										.getNodeName(), newProChild
										.getAttribute(newProChildAttrs.item(k)
												.getNodeName()));

								// logger.debug(newProChildAttrs.item(k).getNodeName());

							}
							NodeList subChildList = newProChild.getChildNodes();
							if (subChildList.getLength() > 0) {
								for (int subChildLen = 0; subChildLen < subChildList
										.getLength(); subChildLen++) {
									if(subChildList.item(
											subChildLen).getNodeName().contains("#")){
										continue;
									}
									Element addChildSub = oldBudget
											.createElement(subChildList.item(
													subChildLen).getNodeName());
									addProChild.appendChild(addChildSub);
									Element newChildSub = (Element) subChildList
											.item(subChildLen);
									addChildSub.setTextContent(newChildSub
											.getTextContent());

								}

							}

						}

					}

				}
			}catch(Exception e){
				
			}
			
			try{
				Element initialOldProcedures = (Element) oldEpoch.getElementsByTagName(
						"procedures").item(0);
				NodeList oldProcedureList = initialOldProcedures
						.getElementsByTagName("procedure");
				for (int i = 0; i < oldProIdList.size(); i++) {
					if (!newProIdList.contains(oldProIdList.get(i))) {
						// delete
						Element oldPro = (Element) oldProcedureList.item(i);
						oldPro.setAttribute("diff", "D");
					}
				}
			}catch(Exception e){
				
			}
	
		}
	}

	public String differBudgetXml(String oldBudgetXml, String newBudgetXml) {
		Document oldBudget = null;
		Document newBudget = null;
		try {

			oldBudget = xmlProcessor.loadXmlStringToDOM(oldBudgetXml);

			newBudget = xmlProcessor.loadXmlStringToDOM(newBudgetXml);

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		NodeList oldEpochList = oldBudget.getElementsByTagName("epoch");
		NodeList newEpochList = newBudget.getElementsByTagName("epoch");
		NodeList epochsList = oldBudget.getElementsByTagName("epochs");
		NodeList expensesList = oldBudget.getElementsByTagName("expenses");
		
		NodeList oldExpList = oldBudget.getElementsByTagName("expense");
		NodeList newExpList = newBudget.getElementsByTagName("expense");
		if(oldExpList.getLength()>0||newExpList.getLength()>0){
			Element oldExps =(Element) expensesList.item(0);
			checkExpense( oldExpList, newExpList, oldExps,oldBudget);
		}
		
		Element oldEpochs =(Element) epochsList.item(0);
		checkEpoch(oldEpochList, newEpochList, oldBudget,oldEpochs);

		oldEpochList = oldBudget.getElementsByTagName("epoch");
		checkProcedure(oldEpochList, newEpochList, oldBudget);

		oldBudgetXml = DomUtils.elementToString(oldBudget, false,
				Encoding.UTF16);

		return oldBudgetXml;

	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

}
