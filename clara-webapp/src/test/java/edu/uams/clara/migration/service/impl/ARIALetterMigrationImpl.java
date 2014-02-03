package edu.uams.clara.migration.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;

import edu.uams.clara.core.util.xml.DomUtils;
import edu.uams.clara.core.util.xml.DomUtils.Encoding;
import edu.uams.clara.integration.incoming.aria.domain.AriaLetter;
import edu.uams.clara.migration.service.ARIALetterMigrationService;
import edu.uams.clara.webapp.fileserver.service.SFTPService;
import edu.uams.clara.webapp.protocol.dao.ProtocolDao;
import edu.uams.clara.webapp.protocol.dao.protocolform.ProtocolFormXmlDataDao;
import edu.uams.clara.webapp.protocol.domain.protocolform.ProtocolFormXmlData;
import edu.uams.clara.webapp.protocol.domain.protocolform.enums.ProtocolFormXmlDataType;
import edu.uams.clara.webapp.xml.processor.XmlProcessor;

public class ARIALetterMigrationImpl implements ARIALetterMigrationService {
	private final static Logger logger = LoggerFactory
			.getLogger(ARIALetterMigrationImpl.class);

	private ProtocolFormXmlDataDao protocolFormXmlDataDao;
	private XmlProcessor xmlProcessor;
	private SFTPService sFTPService;
	private ProtocolDao protocolDao;

	private String localDirectory;
	private String fileServerHost;

	private MessageDigest messageDigest = null;
	private XPathFactory xpathFactory = XPathFactory.newInstance();

	private final static String[] letterMappings = new String[] {
			"approvedDate", "approvedName", "approvedTime", "irbNum",
			"letterType","messageBody", "messageCC", "messageFrom",
			"messageTo", "messageToAddress", "messageToCity",
			"messageToMailslot", "messageToState", "messageToZip", "piName",
			"prnNumber", "protocolSponsor", "protocolTitle", "reviewDate","letterCategory"};

	@Override
	public XPath getXPathInstance() {
		return xpathFactory.newXPath();
	}

	private String deleteLettersElement(String xmlData) {
		Map<String, Object> resultMap = new HashMap<String, Object>(0);
		try {
			resultMap = xmlProcessor.deleteElementByPath("/protocol/letters",
					xmlData);
			xmlData = resultMap.get("finalXml").toString();
		} catch (Exception e) {
			logger.info("element does not exist.");
		}
		return xmlData;
	}

	public void deleteErrorLetter() throws IOException,
			XPathExpressionException {
		String rawdata = null;
		BufferedReader reader = new BufferedReader(new FileReader(
				"C:\\DOCUME~1\\yuanjiawei\\Desktop\\letter66.csv"));

		int index = 1;
		while ((rawdata = reader.readLine()) != null) {
			if (index == 1) {
				index++;
				continue;
			}

			String[] item = rawdata.split(",");
			try {
				Integer.valueOf(item[6].replace("-comma-", ","));
			} catch (Exception e) {
				continue;
			}
			long IRBNum = Integer.valueOf(item[6].replace("-comma-", ","));
			//logger.debug(IRBNum + "");
			ProtocolFormXmlData protocolFormXmlData = null;
			try {
				protocolFormXmlData = protocolFormXmlDataDao
						.getLastProtocolFormXmlDataByProtocolIdAndType(IRBNum,
								ProtocolFormXmlDataType.ARCHIVE);
			} catch (Exception e) {

			}
			if (protocolFormXmlData == null) {
				continue;
			}

			// if Letters ele does exist, create one
			Document doc = null;

			try {
				doc = xmlProcessor.loadXmlStringToDOM(protocolFormXmlData
						.getXmlData());
			} catch (SAXException e) {
				e.printStackTrace();
			}
			NodeList lettersList = doc.getElementsByTagName("letters");
			for (int i = 0; i < lettersList.getLength(); i++) {
				Node letterNode = lettersList.item(i);
				letterNode.getParentNode().removeChild(letterNode);
			}
			String newContractFormXmldata = DomUtils.elementToString(doc,
					false, Encoding.UTF16);
			protocolFormXmlData.setXmlData(newContractFormXmldata);
			protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);
			if (IRBNum == 5374) {
				break;
			}

		}

	}

	private String fillForm(String iniData, AriaLetter letter,
			List<String> signatureList, Map<String, String> mapForType) {

		String letterType = letter.getLetterType().replace("", "");
		if (mapForType.containsKey(letterType)) {
			letterType = mapForType.get(letterType);
		}else{
			letterType = letter.getLetterCategory().replace("", "");
		}
		iniData = iniData.replace("${LetterType}", letterType);

		iniData = iniData.replace("${ApprovedDate}", letter.getApprovedDate()
				.replace("", ""));

		iniData = iniData.replace("${Review Date}", letter.getReviewDate()
				.replace("", ""));

		iniData = iniData.replace("${msg_to}",
				letter.getMessageTo().replace("", ""));

		iniData = iniData.replace("${msg_to_address}", letter
				.getMessageToAddress().replace("", ""));

		iniData = iniData.replace("${msg_to_mailslot}", letter
				.getMessageToMailslot().replace("", ""));

		iniData = iniData.replace("${msg_to_city}", letter.getMessageToCity()
				.replace("", ""));

		iniData = iniData.replace("${msg_to_state}", letter.getMessageToState()
				.replace("", ""));

		iniData = iniData.replace("${msg_to_zip}", letter.getMessageToZip()
				.replace("", ""));

		iniData = iniData.replace("${Protocol Title}", letter
				.getProtocolTitle().replace("", ""));

		iniData = iniData.replace("${HRACID}",
				letter.getIrbNum().replace("", ""));

		iniData = iniData.replace("${pi_name}",
				letter.getPiName().replace("", ""));

		iniData = iniData.replace("${PRN}",
				letter.getPrnNumber().replace("", ""));

		iniData = iniData.replace("${Protocol Sponsor}", letter
				.getProtocolSponsor().replace("", ""));

		iniData = iniData.replace("${msg_body}", letter.getMessageBody()
				.replace("", ""));

		iniData = iniData.replace("${ApprovedName}", letter.getApprovedName()
				.replace("", ""));

		iniData = iniData.replace("${msg_from}", letter.getMessageFrom()
				.replace("", ""));

		iniData = iniData.replace("${ApprovedTime}", letter.getApprovedTime()
				.replace("", ""));

		iniData = iniData.replace("${msg_cc}",
				letter.getMessageCC().replace("", ""));

		// signature img
		String signatureImg = "";
		if (letter.getApprovedName() != null
				&& !letter.getApprovedName().isEmpty()) {
			String signature[] = letter.getApprovedName().split(" ");
			if (signature.length > 1) {
				signatureImg = signature[1].trim();
			}
		}
		if (signatureList.contains(signatureImg)) {
			iniData = iniData.replace("${SignatureName}", signatureImg);
		} else {
			iniData = iniData.replace("${SignatureName}", "blank");
		}
		iniData = iniData.replace("\"\"", "\"");
		return iniData;

	}

	private String uploadDocumenttoFileServer(String path, String ext,
			String irbNum) {

		File fileDir = new File(localDirectory);
		if (!fileDir.exists())
			fileDir.mkdir();

		// copy the file from aria server to local
		String fileName = path + "\\" + irbNum + "." + ext;
		String hashFileName = "";
		String uploadfilename = "";
		try {
			messageDigest = MessageDigest.getInstance("SHA-256",
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			InputStream fileContent = new FileInputStream(fileName);

			byte[] bytes = IOUtils.toByteArray(fileContent);

			messageDigest.update(bytes);

			// get the hash of file content
			hashFileName = new String(Hex.encode(messageDigest.digest()));

			// write the file to local

			uploadfilename = localDirectory + "/" + hashFileName + "." + ext;

			FileOutputStream fout = new FileOutputStream(uploadfilename);
			fout.write(bytes);
			fout.flush();
			fout.close();

			// upload file to the server
			int trySFTP = 1;
			while (trySFTP > 0) {
				try {
					sFTPService.uploadLocalFileToRemote("protocol/" + irbNum
							+ "/" + hashFileName + "." + ext);
					trySFTP = 0;
				} catch (Exception e) {
					e.printStackTrace();
					if (trySFTP < 5) {
						trySFTP++;
					}
					if (trySFTP == 5) {
						trySFTP = 0;
						BufferedReader input = new BufferedReader(
								new FileReader("C:\\Data\\SFTPMissedList.txt"));
						String existData = "";
						String newData = "";
						while ((existData = input.readLine()) != null) {
							newData += existData + "\n";
						}
						input.close();
						newData += fileName;
						BufferedWriter output = new BufferedWriter(
								new FileWriter("C:\\Data\\SFTPMissedList.txt"));
						output.write(newData);
						output.close();
					}

				}
			}
			// delete the file after uploading...
			File uploadedFile = new File(uploadfilename);
			uploadedFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// remove template dir
		fileDir.delete();

		// production

		return "https://" + fileServerHost + "/files/protocol/" + irbNum + "/"
				+ hashFileName + "." + ext;

	}

	@Override
	public void migrateLetter() throws IOException {
		// get letter type mapp
		List<Long> lettersEleDeletedList = Lists.newArrayList();
		BufferedReader readerForType = new BufferedReader(
				new FileReader(
						"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\letterTypeMap.csv"));
		String letterType = "";
		Map<String, String> mapForType = new HashMap<String, String>();
		while ((letterType = readerForType.readLine()) != null) {
			String[] letterTypeMap = letterType.split(",");
			mapForType.put(letterTypeMap[0], letterTypeMap[1]);
		}

		List<String> signatureList = new ArrayList<String>();
		signatureList.add("Eisenach");
		signatureList.add("Evans");
		signatureList.add("Gubbins");
		signatureList.add("Lawson");
		signatureList.add("Mahadevan");
		signatureList.add("Oliveto");
		signatureList.add("Valentine");
		signatureList.add("Wohlleb");

		String htmlTemplate = "";

		FileInputStream fstream;
		fstream = new FileInputStream(
				"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\LetterTemplate.html");
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = "";

		while ((strLine = br.readLine()) != null) {
			htmlTemplate += strLine + "\n";
		}

		List<AriaLetter> ariaLetters = getLetterMapping();

		for (AriaLetter letter : ariaLetters) {
			try {
				String iniLetterData = htmlTemplate;
				long irbNum;
				try {
					irbNum = Long.valueOf(letter.getIrbNum());
				} catch (Exception e) {
					// irbNum is empty
					continue;
				}
				//logger.debug(irbNum + "");

				ProtocolFormXmlData protocolFormXmlData = null;
				try {
					protocolFormXmlData = protocolFormXmlDataDao
							.getLastProtocolFormXmlDataByProtocolIdAndType(
									irbNum, ProtocolFormXmlDataType.ARCHIVE);
				} catch (Exception e) {
					continue;
				}
				if (protocolFormXmlData == null) {
					continue;
				}

				// fill in data
				iniLetterData = fillForm(iniLetterData, letter, signatureList,
						mapForType);

				// begin to add letter element

				String xmlData = protocolFormXmlData.getXmlData();
				// delete letters first time
				if (!lettersEleDeletedList.contains(irbNum)) {
					xmlData = deleteLettersElement(xmlData);
					lettersEleDeletedList.add(irbNum);
					/*BufferedReader input = new BufferedReader(
							new FileReader(
									"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\processedIrbList.txt"));
					String existData = "";
					String newData = "";
					while ((existData = input.readLine()) != null) {
						newData += existData + "\n";
					}
					input.close();
					newData += irbNum;
					BufferedWriter output = new BufferedWriter(
							new FileWriter(
									"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\processedIrbList.txt"));
					output.write(newData);
					output.close();*/
				}

				Document doc = null;
				try {
					doc = xmlProcessor.loadXmlStringToDOM(xmlData);
				} catch (SAXException e) {
					continue;
				}

				XPath xPath = getXPathInstance();
				XPathExpression xPathExpression = null;

				xPathExpression = xPath.compile("//protocol");
				NodeList protocolNodes = (NodeList) xPathExpression.evaluate(
						doc, XPathConstants.NODESET);
				Element protocolEle = (Element) protocolNodes.item(0);

				xPathExpression = xPath.compile("//protocol/letters");
				NodeList lettersNodesList = (NodeList) xPathExpression
						.evaluate(doc, XPathConstants.NODESET);
				Element lettersEle = doc.createElement("letters");
				if (lettersNodesList.getLength() > 0) {
					lettersEle = (Element) lettersNodesList.item(0);
				} else {
					protocolEle.appendChild(lettersEle);
				}

				Element letterEle = doc.createElement("letter");

				// create a letter
				letterEle.setAttribute("date", letter.getApprovedDate()
						.replace("", ""));
				letterEle.setAttribute("from",
						letter.getMessageFrom().replace("", ""));
				letterEle.setAttribute("to",
						letter.getMessageTo().replace("", ""));
				String actualType = letter.getLetterType().replace("", "");
				if (mapForType.containsKey(actualType)) {
					actualType = mapForType.get(actualType);
				}else{
					actualType = letter.getLetterCategory().replace("", "");
				}

				letterEle.setAttribute("type", actualType);

				File fileDir = new File(localDirectory);
				if (!fileDir.exists()) {
					fileDir.mkdir();
				}
				String filename = localDirectory + "/" + String.valueOf(irbNum)
						+ ".html";
				File letterFile = new File(filename);
				BufferedWriter output = new BufferedWriter(new FileWriter(
						filename));
				output.write(iniLetterData);
				output.close();

				String letterPath = "";
				letterPath = uploadDocumenttoFileServer(localDirectory, "html",
						String.valueOf(irbNum));

				// if letter exists, skip
				xPathExpression = xPath
						.compile("//protocol/letters/letter[@path=\""
								+ letterPath + "\" and @from = \""
								+ letter.getMessageFrom().replace("", "")
								+ "\" and @to = \""
								+ letter.getMessageTo().replace("", "")
								+ "\" ]");
				NodeList existedlettersNodesList = (NodeList) xPathExpression
						.evaluate(doc, XPathConstants.NODESET);
				if (existedlettersNodesList.getLength() > 0) {
					continue;
				}
				letterEle.setAttribute("path", letterPath);
				lettersEle.appendChild(letterEle);
				letterFile.delete();

				String newFormXmldata = DomUtils.elementToString(doc, false,
						Encoding.UTF16);
				protocolFormXmlData.setXmlData(newFormXmldata);
				protocolFormXmlDataDao.saveOrUpdate(protocolFormXmlData);

				fileDir.delete();
			} catch (Exception e) {
				logger.debug("error: "+letter.getIrbNum()+" "+letter.getApprovedDate());
			}

		}

		//only for firsttime, latter, move read to the top and write for each new add
		BufferedReader input = new BufferedReader(
				new FileReader(
						"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\processedIrbList.txt"));
		String existData = "";
		String newData = "";
		while ((existData = input.readLine()) != null) {
			newData += existData + "\n";
		}
		input.close();
		for(Long irbProcessed: lettersEleDeletedList){
			newData += irbProcessed + "\n";
		}
		BufferedWriter output = new BufferedWriter(
				new FileWriter(
						"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\processedIrbList.txt"));
		output.write(newData);
		output.close();
	}

	private List<AriaLetter> getLetterMapping() {
		List<AriaLetter> ariaLetters = Lists.newArrayList();
		try {
			CSVReader csvReader = new CSVReader(
					new FileReader(
							"C:\\DOCUME~1\\yuanjiawei\\Desktop\\clara doc\\aria-letter-migration\\138725.csv"));
			int numberOfColumns = letterMappings.length;
			//csvReader.readNext(); // no header
			String[] nextLine;

			while ((nextLine = csvReader.readNext()) != null) {
				AriaLetter ariaLetter = new AriaLetter();
				Map<String, Object> properties;
				properties = BeanUtils.describe(ariaLetter);
				for (int i = 0; i < numberOfColumns; i++) {
					properties.put(letterMappings[i], nextLine[i].trim());
				}
				BeanUtils.populate(ariaLetter, properties);
				ariaLetters.add(ariaLetter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ariaLetters;
	}

	public ProtocolFormXmlDataDao getProtocolFormXmlDataDao() {
		return protocolFormXmlDataDao;
	}

	@Autowired(required = true)
	public void setProtocolFormXmlDataDao(
			ProtocolFormXmlDataDao protocolFormXmlDataDao) {
		this.protocolFormXmlDataDao = protocolFormXmlDataDao;
	}

	@Override
	public XPathFactory getXpathFactory() {
		return xpathFactory;
	}

	public XmlProcessor getXmlProcessor() {
		return xmlProcessor;
	}

	@Autowired(required = true)
	public void setXmlProcessor(XmlProcessor xmlProcessor) {
		this.xmlProcessor = xmlProcessor;
	}

	public SFTPService getsFTPService() {
		return sFTPService;
	}

	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}

	public ProtocolDao getProtocolDao() {
		return protocolDao;
	}

	@Autowired(required = true)
	public void setProtocolDao(ProtocolDao protocolDao) {
		this.protocolDao = protocolDao;
	}

	public String getLocalDirectory() {
		return localDirectory;
	}

	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	public String getFileServerHost() {
		return fileServerHost;
	}

	public void setFileServerHost(String fileServerHost) {
		this.fileServerHost = fileServerHost;
	}

}
