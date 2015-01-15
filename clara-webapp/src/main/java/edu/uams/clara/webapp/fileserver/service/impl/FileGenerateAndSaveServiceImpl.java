package edu.uams.clara.webapp.fileserver.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.common.service.audit.AuditService;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.FileGenerateAndSaveService;
import edu.uams.clara.webapp.fileserver.service.SFTPService;

public class FileGenerateAndSaveServiceImpl implements FileGenerateAndSaveService{
	private final static Logger logger = LoggerFactory
			.getLogger(FileGenerateAndSaveServiceImpl.class);
	
	private UploadedFileDao uploadedFileDao;

	private MessageDigest messageDigest = null;

	private AuditService auditService;
	
	private SFTPService sFTPService;

	@Value("${fileserver.local.dir.path}")
	private String uploadDirResourcePath;
	
	public FileGenerateAndSaveServiceImpl() throws NoSuchAlgorithmException {

		messageDigest = MessageDigest.getInstance("SHA-256",
				new org.bouncycastle.jce.provider.BouncyCastleProvider());

		logger.debug("" + messageDigest.getProvider().getInfo());
	}

	@Override
	public UploadedFile processFileGenerateAndSave(AbstractDomainEntity object, String filename, InputStream fileIn, String ext, String contentType) throws IOException {
		
		
		if (fileIn != null) {
			
			byte[] bytes = IOUtils.toByteArray(fileIn);

			messageDigest.update(bytes);
			
			String identifier = new String(Hex.encode(messageDigest.digest()));
			
			String fullpath = uploadDirResourcePath + "/" + identifier + "."
					+ ext;
			
			logger.debug("fullpath: " + fullpath);

			UploadedFile uploadedFile = new UploadedFile();
			
			uploadedFile.setFilename(filename);
			uploadedFile.setContentType(contentType);
			uploadedFile.setSize(bytes.length);
			uploadedFile.setIdentifier(identifier);
			uploadedFile.setCreated(new Date());
			// this is set after sftp
			uploadedFile.setPath(uploadDirResourcePath); // use local and gets reset to remote after sftp
			//uploadedFile.setPath("/" + object.getClass().getSimpleName().toLowerCase() + "/" + object.getId() + "/");
			uploadedFile.setExtension(ext);
			
			//@OPT this is really an actual call as the sftp actually will update, but who cares...
			uploadedFile = uploadedFileDao.saveOrUpdate(uploadedFile);
			
			logger.debug("file id: " + uploadedFile.getId());
			
			File outputFile = new File(fullpath);
			if (outputFile.exists()) {
				logger.debug("file already exist...don't move the file");
				auditService.auditEvent("FILE_UPLOADED_EXIST", "fileId: "
						+ uploadedFile.getFilename() + "; already exist at: "
						+ fullpath);
				UploadedFile existUploadedFile = uploadedFileDao.getUploadedFileByIdentifier(identifier);
				
				uploadedFile.setPath(existUploadedFile.getPath());

			} else {
				InputStream inputStream = new ByteArrayInputStream(bytes);

				OutputStream outputStream = new FileOutputStream(fullpath);

				int readBytes = 0;
				byte[] buffer = new byte[10000];
				while ((readBytes = inputStream.read(buffer, 0, 10000)) != -1) {

					outputStream.write(buffer, 0, readBytes);
				}
				logger.debug("file saved ...");
				outputStream.close();
				inputStream.close();
				
				try{
					sFTPService.uploadLocalUploadedFileToRemote(object, uploadedFile);
				}
				catch(Exception ex){
					logger.error("failed to sftp the file to file server; uploadedFile.id: " + uploadedFile.getId() + "; object id: " + object.getId(), ex);

				}
			}
			
			return uploadedFile;
		}
		
		return null;
		
	}
	
	@Override
	public void processFileGenerateAndSave(String filename,
			InputStream fileIn) throws IOException {
		if (fileIn != null) {
			
			byte[] bytes = IOUtils.toByteArray(fileIn);

			String[] folders = filename.split("/");
			
			String fullpath = uploadDirResourcePath + "/" + folders[folders.length - 1];
			
			logger.debug("fullpath: " + fullpath);
			
			InputStream inputStream = new ByteArrayInputStream(bytes);

			OutputStream outputStream = new FileOutputStream(fullpath);

			int readBytes = 0;
			byte[] buffer = new byte[10000];
			while ((readBytes = inputStream.read(buffer, 0, 10000)) != -1) {

				outputStream.write(buffer, 0, readBytes);
			}
			logger.debug("file saved ...");
			outputStream.close();
			inputStream.close();
			
			try{
				sFTPService.uploadLocalUploadedFileToRemote(filename);
			}
			catch(Exception ex){
				ex.printStackTrace();

			}
		}
	}
	
	/**
	 * This function is used to process uploaded files
	 * We currently have this part separated from the ajax controller that creates a link between protocol/contract forms to a specific file.
	 * 
	 */
	@Override
	public UploadedFile processFileGenerateAndSaveLocal(String filename, InputStream fileIn, String ext, String contentType) throws IOException {
		
		
		if (fileIn != null) {
			
			byte[] bytes = IOUtils.toByteArray(fileIn);

			messageDigest.update(bytes);
			
			String identifier = new String(Hex.encode(messageDigest.digest()));
			
			String fullpath = uploadDirResourcePath + "/" + identifier + "."
					+ ext;
			
			logger.debug("fullpath: " + fullpath);

			UploadedFile uploadedFile = new UploadedFile();
			
			uploadedFile.setFilename(filename);
			uploadedFile.setContentType(contentType);
			uploadedFile.setSize(bytes.length);
			uploadedFile.setIdentifier(identifier);
			uploadedFile.setCreated(new Date());
			uploadedFile.setPath(uploadDirResourcePath); // uploaded files, set to local for now, and the controller will update the paths, and upload the file the remove file server through sftp
			uploadedFile.setExtension(ext);
			
			uploadedFile = uploadedFileDao.saveOrUpdate(uploadedFile);
			
			logger.debug("file id: " + uploadedFile.getId());
			
			File outputFile = new File(fullpath);
			logger.debug("full absolute path: " + outputFile.getAbsolutePath());
			if (outputFile.exists()) {
				logger.debug("file already exist...don't move the file");
				auditService.auditEvent("FILE_UPLOADED_EXIST", "fileId: "
						+ uploadedFile.getId() + "; already exist at: "
						+ fullpath, uploadedFile);

			} else {
				InputStream inputStream = new ByteArrayInputStream(bytes);

				OutputStream outputStream = new FileOutputStream(fullpath);

				int readBytes = 0;
				byte[] buffer = new byte[10000];
				while ((readBytes = inputStream.read(buffer, 0, 10000)) != -1) {

					outputStream.write(buffer, 0, readBytes);
				}
				logger.debug("file saved ...");
				outputStream.close();
				inputStream.close();
				
				/*
				try{
					sFTPService.uploadLocalUploadedFileToRemote(object, uploadedFile);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				*/
			}
			
			return uploadedFile;
		}
		
		return null;
		
	}
	
	@Override
	public void copyFileToSharedFolder(String fromFilePath, String toFilePath, String toShareFolderUserName, String toShareFolderUserPassword, String fileName) {
		try
		{
			File outputFile = new File(fromFilePath);
			InputStream in = new FileInputStream(outputFile);
			
			NtlmPasswordAuthentication toAuth = new NtlmPasswordAuthentication("ad.uams.edu", toShareFolderUserName, toShareFolderUserPassword);
			
			//String fromPath = fromFilePath + fileName;
			
			String toPath = toFilePath + fileName;
			
			//SmbFile fromFile = new SmbFile(fromPath);
			
			SmbFile toFile = new SmbFile(toPath, toAuth);
			
			SmbFileOutputStream sfos = new SmbFileOutputStream(toFile);
            sfos.write(IOUtils.toByteArray(in));
			
			//fromFile.copyTo(toFile);
			
        } catch (Exception e) {
            e.printStackTrace();
        }
		
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

	public SFTPService getsFTPService() {
		return sFTPService;
	}

	@Autowired(required = true)
	public void setsFTPService(SFTPService sFTPService) {
		this.sFTPService = sFTPService;
	}
	
}
