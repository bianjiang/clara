package edu.uams.clara.webapp.xml.processor.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.CharMatcher;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.webapp.xml.processor.ProtocolFormXmlDifferService;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ProtocolFormXmlDifferServiceImpl implements
		ProtocolFormXmlDifferService {
	private final static Logger logger = LoggerFactory
			.getLogger(ProtocolFormXmlDifferServiceImpl.class);

	private XmlProcessor xmlProcessor;

	private XPathFactory xpathFactory = XPathFactory.newInstance();

	@Override
	public XPath getXPathInstance() {
		return xpathFactory.newXPath();
	}

	public String differProtocolFormXml(String baseTag, String oldXml,
			String newXml) throws XPathExpressionException {
		Document oldDoc = null;
		Document newDoc = null;
		try {

			oldDoc = xmlProcessor.loadXmlStringToDOM(oldXml);

			newDoc = xmlProcessor.loadXmlStringToDOM(newXml);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		oldDoc = differXml(baseTag, oldDoc, newDoc);

		String result = "";
		result = DomUtils.elementToString(oldDoc, false, Encoding.UTF16);

		return result;
	}
	
	private String stringTrim(String data){
		/*data = data.replace("", " ");
		data = data.replace("", " ");
		data = data.replaceAll("[^A-Za-z0-9()\\[\\]]", " ");*/
		data =CharMatcher.JAVA_ISO_CONTROL.removeFrom(data);
		data = data.replace(" ", "");
		data=data.trim();
		return data;
	}

	private Document differXml(String baseTag, Document oldDoc, Document newdoc)
			throws XPathExpressionException {

		// get the xpath of each node in the oldxml
		List<String> oldPathList = new ArrayList<String>();
		NodeList oldNodeList = oldDoc.getElementsByTagName(baseTag);
		String oldpathPrefix = "";

		oldPathList = buildPathForOldXml(oldPathList, oldNodeList,
				oldpathPrefix);

		// get the xpath of each node in the newxml
		List<String> newPathList = new ArrayList<String>();
		NodeList newNodeList = newdoc.getElementsByTagName(baseTag);
		String newpathPrefix = "";
		newPathList = buildPathForOldXml(newPathList, newNodeList,
				newpathPrefix);

		// build a list for recognized by id
		List<String> specPathListById = new ArrayList<String>();
		specPathListById = createSpecPathListById(specPathListById, baseTag);

		// replace the path for special case without id
		// begin to deal with oldxml
		oldPathList = speListForCategory(oldDoc, oldPathList);
		oldPathList = speListForStaff(oldDoc, oldPathList);
		oldPathList = speListForStudySite(oldDoc, oldPathList);
		oldPathList = speListForFundingSource(oldDoc, oldPathList);
		oldPathList = speListById(oldDoc, oldPathList, specPathListById);

		// begin to deal with newxml
		newPathList = speListForCategory(newdoc, newPathList);
		newPathList = speListForStaff(newdoc, newPathList);
		newPathList = speListForStudySite(newdoc, newPathList);
		newPathList = speListForFundingSource(newdoc, newPathList);
		newPathList = speListById(newdoc, newPathList, specPathListById);

		oldDoc = compareModifyAndDelNodes(oldDoc, newdoc, oldPathList);
		oldDoc = compareAddedNodes(oldDoc, newdoc, newPathList);

		return oldDoc;
	}

	private List<String> createSpecPathListById(List<String> specPathListById,
			String baseTag) {
		specPathListById.add(baseTag);
		return specPathListById;
	}

	/*
	 * private List<String> createSpecailTags(){ List<String> specialTags = new
	 * ArrayList<String>(); specialTags.add("category");
	 * specialTags.add("staff"); specialTags.add("site");
	 * specialTags.add("funding-source"); return specialTags;
	 * 
	 * }
	 */

	private Document compareAddedNodes(Document oldDoc, Document newdoc,
			List<String> newPathList) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();

		// this element is added for the node that whose father node is also new
		// added.
		Element fatherNodeForLstRound = null;

		for (int i = 0; i < newPathList.size(); i++) {

			xPathExpression = xPath.compile(newPathList.get(i));
			NodeList oldnodes = (NodeList) xPathExpression.evaluate(oldDoc,
					XPathConstants.NODESET);
			NodeList newnodes = (NodeList) xPathExpression.evaluate(newdoc,
					XPathConstants.NODESET);

			// GetTextContext
			XPathExpression exprForText = xPath.compile(newPathList.get(i)
					+ "/text()");
			Object newTextSearchresult = exprForText.evaluate(newdoc,
					XPathConstants.NODESET);
			Object oldTextSearchresult = exprForText.evaluate(oldDoc,
					XPathConstants.NODESET);
			NodeList oldTextnodes = (NodeList) oldTextSearchresult;
			NodeList newTextnodes = (NodeList) newTextSearchresult;

			if (newnodes.getLength() > 0) {

				for (int j = 0; j < newnodes.getLength(); j++) {
					Element tempAddEle = oldDoc.createElement(newnodes.item(j)
							.getNodeName());

					int tagForCheckBox = 0;
					int addtag = 0;
					if (oldnodes.getLength() > 0) {

						// for check box type check box cannot be null
						if (oldTextnodes.getLength() > 1
								|| newTextnodes.getLength() > 1) {

							for (int m = 0; m < oldnodes.getLength(); m++) {
								/*
								 * if(newTextnodes
								 * .item(j)!=null&&oldTextnodes.item(m)!=null){
								 */
								if (newTextnodes
										.item(j)
										.getNodeValue()
										.equals(oldTextnodes.item(m)
												.getNodeValue())) {
									tagForCheckBox = 1;
									break;
								}
								if (m == oldnodes.getLength() - 1) {
									tempAddEle.setAttribute("diff", "A");
									tagForCheckBox = 2;
									addtag = 1;
								}

							}

						}
					}

					// }
					// in new, not in old not in here should build a list for
					// New
					if (oldnodes.getLength() == 0 && tagForCheckBox == 0) {
						NamedNodeMap newAttr = newnodes.item(j).getAttributes();
						for (int k = 0; k < newAttr.getLength(); k++) {
							Element temNewEle = (Element) newnodes.item(j);
							tempAddEle.setAttribute(newAttr.item(k)
									.getNodeName(),
									temNewEle.getAttribute(newAttr.item(k)
											.getNodeName()));
						}
						tempAddEle.setAttribute("diff", "A");
						addtag = 1;
					}
					if (addtag == 1) {
						String[] tempSplit = newPathList.get(i).split("/");
						String fatherPath = "";
						for (int n = 1; n < tempSplit.length - 1; n++) {
							fatherPath = fatherPath + "/" + tempSplit[n];
						}
						// if the node is not the root node, append it to its
						// father
						// node
						if (!fatherPath.isEmpty()) {
							// logger.debug(fatherPath);
							xPathExpression = xPath.compile(fatherPath);
							NodeList addNodes = (NodeList) xPathExpression
									.evaluate(oldDoc, XPathConstants.NODESET);

							Element fatherNode = (Element) addNodes.item(0);
							if (fatherNode == null)
								fatherNode = fatherNodeForLstRound;

							fatherNode.appendChild(tempAddEle);
							fatherNodeForLstRound = tempAddEle;
						}

						if (newTextnodes.item(j) != null)
							tempAddEle.setTextContent(newTextnodes.item(j)
									.getNodeValue());
					}
				}

			}
		}

		return oldDoc;

	}

	private Document compareModifyAndDelNodes(Document oldDoc, Document newdoc,
			List<String> oldPathList) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();

		// for those do not have id
		for (int i = 0; i < oldPathList.size(); i++) {

			XPathExpression expr = xPath.compile(oldPathList.get(i));
			Object oldSearchresult = expr.evaluate(oldDoc,
					XPathConstants.NODESET);
			Object newSearchresult = expr.evaluate(newdoc,
					XPathConstants.NODESET);
			NodeList oldnodes = (NodeList) oldSearchresult;
			NodeList newnodes = (NodeList) newSearchresult;

			// GetTextContext
			xPathExpression = xPath.compile(oldPathList.get(i) + "/text()");
			NodeList oldTextnodes = (NodeList) xPathExpression.evaluate(oldDoc,
					XPathConstants.NODESET);
			NodeList newTextnodes = (NodeList) xPathExpression.evaluate(newdoc,
					XPathConstants.NODESET);

			if (oldnodes.getLength() > 0 && newnodes.getLength() == 0) {
				for (int j = 0; j < oldnodes.getLength(); j++) {
					Element tempOldEle = (Element) oldnodes.item(j);
					tempOldEle.setAttribute("diff", "D");
				}
			}

			// old and new has the same length and no id
			// if (newnodes.getLength() == oldnodes.getLength()) {
			if (newnodes.getLength() > 0 && oldnodes.getLength() > 0) {
				for (int j = 0; j < oldnodes.getLength(); j++) {
					Element tempNewEle = (Element) newnodes.item(j);
					Element tempOldEle = (Element) oldnodes.item(j);

					NamedNodeMap oldAttr = tempOldEle.getAttributes();
					// for check box type check box cannot be null
					int tagForCheckBox = 0;
					if (oldTextnodes.getLength() > 1
							|| newTextnodes.getLength() > 1) {

						for (int m = 0; m < newnodes.getLength(); m++) {
							/*
							 * if(oldTextnodes
							 * .item(j)!=null&&newTextnodes.item(m)!=null){
							 */
							// logger.debug(""+j+"size is"+oldTextnodes.getLength()+" "+oldnodes.item(0).getNodeName()
							// + ""+ oldPathList.get(i));

							if (oldTextnodes.getLength() == 0)

								if (oldTextnodes
										.item(j)
										.getNodeValue()
										.equals(newTextnodes.item(m)
												.getNodeValue())) {
									tagForCheckBox = 1;
									break;
								}

							if (m == newnodes.getLength() - 1) {
								tempOldEle.setAttribute("diff", "D");
								tagForCheckBox = 2;
							}

						}
					}
					// tagForCheckBox>0, means this tag is a tagForCheckBox
					if (tagForCheckBox > 0)
						continue;

					for (int k = 0; k < oldAttr.getLength(); k++) {
						String oldStr= tempOldEle.getAttribute(
								oldAttr.item(k).getNodeName());
						String newStr = tempNewEle.getAttribute(oldAttr.item(k)
								.getNodeName());
						
						if(oldStr!=null){
							oldStr=stringTrim(oldStr);
						}
						if(newStr!=null){
							newStr=stringTrim(newStr);
						}
						
						if (!oldStr.equals(newStr)) {
							tempOldEle.setAttribute(oldAttr.item(k)
									.getNodeName(),
									tempNewEle.getAttribute(oldAttr.item(k)
											.getNodeName()));
							tempOldEle.setAttribute("diff", "M");

						}

					}
					if (oldTextnodes.getLength() > j) {
						if (newTextnodes.item(j) == null) {
							oldTextnodes.item(j).setNodeValue("");
							tempOldEle.setAttribute("diff", "M");
						} else{
							String oldStr = oldTextnodes.item(j).getNodeValue();
							String newStr = newTextnodes.item(j).getNodeValue();
							oldStr =StringEscapeUtils.unescapeHtml(oldStr);
							newStr =StringEscapeUtils.unescapeHtml(newStr);
							if(oldStr!=null){
								oldStr=stringTrim(oldStr);
							}
							if(newStr!=null){
								newStr=stringTrim(newStr);
							}
							if (!oldStr.equals(newStr)) {
							tempOldEle.setAttribute("diff", "M");
							oldTextnodes.item(j).setNodeValue(
									newTextnodes.item(j).getNodeValue());

							}
						}
					}
				}
			}
		}
		return oldDoc;
	}

	private List<String> buildPathForOldXml(List<String> oldPathList,
			NodeList oldNodeList, String pathPrefix) {
		for (int i = 0; i < oldNodeList.getLength(); i++) {
			String path = "";
			if (oldNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				path = pathPrefix + "/" + oldNodeList.item(i).getNodeName();
				oldPathList.add(path);
			}

			if (oldNodeList.item(i).getChildNodes().getLength() > 0) {
				buildPathForOldXml(oldPathList, oldNodeList.item(i)
						.getChildNodes(), path);
			}

		}

		return oldPathList;
	}

	private List<String> speListForCategory(Document oldDoc,
			List<String> oldPathList) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();
		String path = "/protocol/irb-fees/category";
		xPathExpression = xPath.compile(path + "/name/text()");
		NodeList speTextnodes = (NodeList) xPathExpression.evaluate(oldDoc,
				XPathConstants.NODESET);

		/*
		 * for(int i=0; i < speTextnodes.getLength(); i ++){ Element el =
		 * (Element)speTextnodes.item(i); //String name = el.get }
		 */

		int count = -1;
		for (int j = 0; j < oldPathList.size(); j++) {
			if (oldPathList.get(j).contains(path)) {
				String tempPath = oldPathList.get(j);
				String tempPath2 = "";
				if (oldPathList.get(j).equals(path))
					count++;
				tempPath2 = path + "[name='"
						+ speTextnodes.item(count).getNodeValue() + "']";
				String replacePath = tempPath.replace(path, tempPath2);
				oldPathList.set(j, replacePath);

			}
		}
		return oldPathList;
	}

	private List<String> speListForStaff(Document oldDoc,
			List<String> oldPathList) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();
		String path = "/protocol/staffs/staff";
		xPathExpression = xPath.compile(path + "/user");
		NodeList speTextnodes = (NodeList) xPathExpression.evaluate(oldDoc,
				XPathConstants.NODESET);

		int count = -1;
		for (int j = 0; j < oldPathList.size(); j++) {
			if (oldPathList.get(j).contains(path)) {
				String tempPath = oldPathList.get(j);
				String tempPath2 = "";
				if (oldPathList.get(j).equals(path))
					count++;
				Element tempEle = (Element) speTextnodes.item(count);
				tempPath2 = path + "[user[@id='" + tempEle.getAttribute("id")
						+ "']]";
				String replacePath = tempPath.replace(path, tempPath2);
				oldPathList.set(j, replacePath);

			}
		}
		return oldPathList;
	}

	private List<String> speListForStudySite(Document oldDoc,
			List<String> oldPathList) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();
		String path = "/protocol/study-sites/site";
		xPathExpression = xPath.compile(path);
		NodeList speTextnodes = (NodeList) xPathExpression.evaluate(oldDoc,
				XPathConstants.NODESET);

		int count = -1;
		for (int j = 0; j < oldPathList.size(); j++) {
			if (oldPathList.get(j).contains(path)) {
				String tempPath = oldPathList.get(j);
				String tempPath2 = "";
				if (oldPathList.get(j).equals(path))
					count++;
				Element tempEle = (Element) speTextnodes.item(count);
				tempPath2 = path + "[@site-id='"
						+ tempEle.getAttribute("site-id") + "']";
				String replacePath = tempPath.replace(path, tempPath2);
				oldPathList.set(j, replacePath);

			}
		}
		return oldPathList;
	}

	private List<String> speListForFundingSource(Document oldDoc,
			List<String> oldPathList) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();
		String path = "/protocol/funding/funding-source";

		xPathExpression = xPath.compile(path);
		NodeList speTextnodes = (NodeList) xPathExpression.evaluate(oldDoc,
				XPathConstants.NODESET);

		int count = -1;
		for (int j = 0; j < oldPathList.size(); j++) {
			if (oldPathList.get(j).contains(path)) {
				String tempPath = oldPathList.get(j);
				String tempPath2 = "";
				if (oldPathList.get(j).equals(path))
					count++;
				Element tempEle = (Element) speTextnodes.item(count);
				tempPath2 = path + "[@entityid='"
						+ tempEle.getAttribute("entityid") + "']";
				String replacePath = tempPath.replace(path, tempPath2);
				oldPathList.set(j, replacePath);

			}
		}
		return oldPathList;
	}

	private List<String> speListById(Document oldDoc, List<String> oldPathList,
			List<String> specPathListById) throws XPathExpressionException {
		XPathExpression xPathExpression = null;

		XPath xPath = getXPathInstance();
		for (int i = 0; i < specPathListById.size(); i++) {
			String path = specPathListById.get(i);
			xPathExpression = xPath.compile(path);
			NodeList speTextnodes = (NodeList) xPathExpression.evaluate(oldDoc,
					XPathConstants.NODESET);

			int count = -1;
			for (int j = 0; j < oldPathList.size(); j++) {
				if (oldPathList.get(j).contains(path)) {
					String tempPath = oldPathList.get(j);
					String tempPath2 = "";
					if (oldPathList.get(j).equals(path)) {
						count++;
						Element tempEle = (Element) speTextnodes.item(count);
						tempPath2 = path + "[@id='"
								+ tempEle.getAttribute("id") + "']";
						String replacePath = tempPath.replace(path, tempPath2);
						oldPathList.set(j, replacePath);
					}

				}
			}
		}
		return oldPathList;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	@Override
	public XPathFactory getXpathFactory() {
		return xpathFactory;
	}

	public void setXpathFactory(XPathFactory xpathFactory) {
		this.xpathFactory = xpathFactory;
	}
}
