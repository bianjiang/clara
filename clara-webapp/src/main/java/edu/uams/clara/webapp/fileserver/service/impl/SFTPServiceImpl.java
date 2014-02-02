package edu.uams.clara.webapp.fileserver.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;

public class SFTPServiceImpl implements SFTPService {

	private final static Logger logger = LoggerFactory
			.getLogger(SFTPService.class);

	private String localDirectory;

	private String remoteDirectory;

	private String knownHostsFilename;

	private String user;

	private String password;

	private String host;

	private int port = 22;

	private ChannelSftp openSFTPChannel() {
		ChannelSftp sftp = null;
		try {
			JSch jsch = new JSch();
			jsch.setKnownHosts(knownHostsFilename);
			Session session = jsch.getSession(user, host, port);

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", 
	                  "publickey,keyboard-interactive,password");
			
			session.setConfig(config);

			session.setPassword(password);
			session.connect();
			sftp = (ChannelSftp) session.openChannel("sftp");
			//logger.debug("connecting...");
			sftp.connect();
			sftp.cd("/");
		//	start with root
		} catch (JSchException | SftpException e) {
			throw new RuntimeException(String.format(
					"%s@%s:%d: Error connecting to sftp.", user, host, port), e);
		}
		return sftp;
	}

	@Override
	public synchronized void uploadLocalFileToRemote(String fileName)
			throws JSchException {
		
		logger.debug("fileName: " + fileName);
		
		
		ChannelSftp sftpChannel = null;
		try {		

			String destPath = remoteDirectory + "/" + fileName;

			sftpChannel = openSFTPChannel();
			//start with root
			

			// create folder if the path is not exist
			String[] folders = destPath.split("/");
			for ( int i = 0; i < folders.length - 1; i++) {
				
				String folder = folders[i];
				//the first / causes a empty string on split... 
				if(!folder.trim().isEmpty()) {
					logger.debug("folder: " + folder);
					
					try {
						sftpChannel.cd(folder);
					} catch (SftpException e) {
						
						sftpChannel.mkdir(folder);
						sftpChannel.cd(folder);
					}
					logger.debug("pwd: " + sftpChannel.pwd());
				}				
			}

			String srcPath = localDirectory + "/" + folders[folders.length - 1]; // the last part is always the file name
			logger.debug(srcPath);
			logger.debug(destPath);
			sftpChannel.put(srcPath, destPath, ChannelSftp.OVERWRITE); //destPath is absolute path on the remote server
			File file =  new File(srcPath);
			file.delete();
			// sftpChannel.exit();
		} catch (SftpException e) {
			logger.error("SftpException with filename: " + fileName,e);
		} finally {
			if (sftpChannel != null) {
				sftpChannel.disconnect();
				if (sftpChannel.getSession() != null) {
					sftpChannel.getSession().disconnect();
				}
				//logger.debug("disconnecting the sftpChannel...");
			}
		}
	}
	
	@Override
	public String downloadFileFromRemoteAndConvertToXml(String fileName)
			throws JSchException {
		logger.debug("fileName: " + fileName);
		
		ChannelSftp sftpChannel = null;
		
		try {
			String destPath = remoteDirectory + "/" + fileName;

			sftpChannel = openSFTPChannel();
			
			String[] folders = destPath.split("/");
			for ( int i = 0; i < folders.length - 1; i++) {
				String folder = folders[i];
				
				if(!folder.trim().isEmpty()) {
					try {
						sftpChannel.cd(folder);
					} catch (Exception e) {
						logger.error("Folder: " + folder + " does not exist!");
					}
				}
			}
			
			String srcPath = localDirectory + "/" + folders[folders.length - 1];
			
			logger.debug(destPath);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			sftpChannel.get(destPath, baos);
			
			//logger.debug("final xml: " + baos.toString());
			return baos.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sftpChannel != null) {
				sftpChannel.disconnect();
				if (sftpChannel.getSession() != null) {
					sftpChannel.getSession().disconnect();
				}
				//logger.debug("disconnecting the sftpChannel...");
			}
		}
		
		return null;
	}
	
	private UploadedFileDao uploadedFileDao;

	@Override
	public void uploadLocalUploadedFileToRemote(AbstractDomainEntity object,
			UploadedFile uploadedFile) throws JSchException {
		
		String fileServerRelateivePath = "/" + object.getClass().getSimpleName().toLowerCase()
				+ "/" + object.getId() + "/";
		uploadLocalFileToRemote(fileServerRelateivePath + uploadedFile.getIdentifier()
				+ "." + uploadedFile.getExtension());
		uploadedFile.setPath(fileServerRelateivePath);
		uploadedFileDao.saveOrUpdate(uploadedFile);

	}

	public String getLocalDirectory() {
		return localDirectory;
	}

	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	public String getRemoteDirectory() {
		return remoteDirectory;
	}

	public void setRemoteDirectory(String remoteDirectory) {
		this.remoteDirectory = remoteDirectory;
	}

	public String getKnownHostsFilename() {
		return knownHostsFilename;
	}

	public void setKnownHostsFilename(String knownHostsFilename) {
		this.knownHostsFilename = knownHostsFilename;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}

	@Autowired(required=true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}

}
