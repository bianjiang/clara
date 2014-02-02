package edu.uams.clara.webapp.fileserver.service;

import java.io.IOException;
import java.io.InputStream;

import edu.uams.clara.core.domain.AbstractDomainEntity;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;

public interface FileGenerateAndSaveService {

	UploadedFile processFileGenerateAndSave(AbstractDomainEntity object,
			String filename, InputStream fileIn, String ext, String contentType)
			throws IOException;

	UploadedFile processFileGenerateAndSaveLocal(String filename,
			InputStream fileIn, String ext, String contentType)
			throws IOException;
}
