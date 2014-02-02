package edu.uams.clara.webapp.fileserver.service;

import com.jcraft.jsch.JSchException;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;

public interface SFTPService {

	void uploadLocalUploadedFileToRemote(AbstractDomainEntity object, UploadedFile uploadedFile) throws JSchException;

	void uploadLocalFileToRemote(String fileName) throws JSchException;
	
	String downloadFileFromRemoteAndConvertToXml(String fileName) throws JSchException;

}
