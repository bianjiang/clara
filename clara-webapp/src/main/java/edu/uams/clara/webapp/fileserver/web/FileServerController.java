package edu.uams.clara.webapp.fileserver.web;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.fileserver.util.FileUtil;

@Controller
public class FileServerController {

	private final static Logger logger = LoggerFactory
			.getLogger(FileServerController.class);

	private UploadedFileDao uploadedFileDao;

	private MessageDigest messageDigest = null;

	private AuditService auditService;
	
	private FileGenerateAndSaveService fileGenerateAndSaveService;

	@Value("${fileserver.local.dir.path}")
	private String uploadDirResourcePath;

	public FileServerController() throws NoSuchAlgorithmException {

		messageDigest = MessageDigest.getInstance("SHA-256",
				new org.bouncycastle.jce.provider.BouncyCastleProvider());

		logger.debug("" + messageDigest.getProvider().getInfo());
	}

	@RequestMapping("/fileserver/index")
	public String goIndex() {
		return "fileserver/index";
	}
	
	private ObjectMapper objectMapper = new ObjectMapper();

	@RequestMapping("/fileserver/fileUpload")
	public void processFileUpload(@RequestParam("file") MultipartFile file,
			HttpServletResponse response) throws IOException {
		logger.debug("" + messageDigest.getProvider().getInfo());
		logger.debug("uploadDirResourcePath: " + uploadDirResourcePath);
		if (file != null && !file.isEmpty() && file.getSize() > 0) {
			
			UploadedFile uploadedFile = fileGenerateAndSaveService.processFileGenerateAndSaveLocal(file.getOriginalFilename(), file.getInputStream(), FileUtil.getExtension(file.getOriginalFilename()), file.getContentType());

			AbstractHttpMessageConverter<String> stringHttpMessageConverter = new StringHttpMessageConverter();

			MediaType htmlMimeType = MediaType.TEXT_HTML;

			if (stringHttpMessageConverter.canWrite(String.class, htmlMimeType)) {

				try {

					stringHttpMessageConverter.write(objectMapper
							.writeValueAsString(uploadedFile), htmlMimeType,
							new ServletServerHttpResponse(response));

				} catch (IOException m_Ioe) {

				} catch (HttpMessageNotWritableException p_Nwe) {

				}

			}

		}

	}

	/*
	 * @RequestMapping("/fileUpload") public @ResponseBody UploadedFile
	 * processFileUpload(@RequestParam("file") MultipartFile file) throws
	 * IOException { logger.debug("" + messageDigest.getProvider().getInfo());
	 * logger.debug("uploadDirResourcePath: " + uploadDirResourcePath); if (file
	 * != null && !file.isEmpty() && file.getSize() > 0) {
	 * 
	 * messageDigest.update(file.getBytes());
	 * 
	 * String identifier = new String(Hex.encode(messageDigest.digest()));
	 * String filename = file.getOriginalFilename(); String ext =
	 * FileUtil.getExtension(filename);
	 * 
	 * logger.debug(file.getOriginalFilename() + "; " + file.getSize() + "; " +
	 * file.getContentType() + "; SHA256: " + identifier);
	 * 
	 * String fullpath = uploadDirResourcePath + "/" + identifier + "." + ext;
	 * 
	 * UploadedFile uploadedFile = new UploadedFile();
	 * 
	 * uploadedFile.setFilename(file.getOriginalFilename());
	 * uploadedFile.setContentType(file.getContentType());
	 * uploadedFile.setSize(file.getSize());
	 * uploadedFile.setIdentifier(identifier); uploadedFile.setCreated(new
	 * Date()); uploadedFile.setPath(uploadDirResourcePath);
	 * uploadedFile.setExtension(ext);
	 * 
	 * uploadedFile = uploadedFileDao.saveOrUpdate(uploadedFile);
	 * 
	 * logger.debug("file id: " + uploadedFile.getId());
	 * 
	 * File outputFile = new File(fullpath); if (outputFile.exists()) {
	 * logger.debug("file already exist...don't move the file");
	 * auditService.auditEvent("FILE_UPLOADED_EXIST", "fileId: " +
	 * uploadedFile.getId() + "; already exist at: " + fullpath, uploadedFile);
	 * 
	 * } else { InputStream inputStream = file.getInputStream();
	 * 
	 * OutputStream outputStream = new FileOutputStream(fullpath);
	 * 
	 * int readBytes = 0; byte[] buffer = new byte[10000]; while ((readBytes =
	 * inputStream.read(buffer, 0, 10000)) != -1) {
	 * 
	 * outputStream.write(buffer, 0, readBytes); } outputStream.close();
	 * inputStream.close(); }
	 * 
	 * auditService.auditEvent("FILE_UPLOADED", "fileId: " +
	 * uploadedFile.getId() + "; has been uploaded and stored in: " + fullpath,
	 * uploadedFile); return uploadedFile; } else { return null; }
	 * 
	 * 
	 * 
	 * }
	 */

	/**
	 * the file is stored on the disk as identifier + ".ext"
	 * 
	 * @param physicalfilename
	 * @return
	 */
	@RequestMapping(value = "/fileDownload/{identifier}.{extension}")
	public @ResponseBody
	Resource processFileDownload(@PathVariable("identifier") String identifier,
			@PathVariable("extension") String extension) {

		Resource fileResource = new FileSystemResource(
				uploadDirResourcePath
						+ "/"
						+ identifier
						+ ((extension != null && !extension.isEmpty()) ? ("." + extension)
								: ""));
		return fileResource;
	}

	@Autowired(required = true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}

	@Autowired(required = true)
	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public AuditService getAuditService() {
		return auditService;
	}


	public FileGenerateAndSaveService getFileGenerateAndSaveService() {
		return fileGenerateAndSaveService;
	}

	@Autowired(required = true)
	public void setFileGenerateAndSaveService(FileGenerateAndSaveService fileGenerateAndSaveService) {
		this.fileGenerateAndSaveService = fileGenerateAndSaveService;
	}
}
