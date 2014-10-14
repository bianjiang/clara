package edu.uams.clara.webapp.fileserver.web.ajax;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jcraft.jsch.JSchException;

import edu.uams.clara.webapp.fileserver.dao.UploadedFileDao;
import edu.uams.clara.webapp.fileserver.domain.UploadedFile;
import edu.uams.clara.webapp.fileserver.service.SFTPService;

@Controller
public class FileServerAjaxController {
	
	private SFTPService sftpService;
	
	private UploadedFileDao uploadedFileDao;
	
	@RequestMapping(value = "/ajax/documents/download")
	public @ResponseBody String getUrl(@RequestParam("docId") List<String> uploadedFileIds){
		Map<String, String> fileNamesMap = Maps.newHashMap();
		
		for (String uploadedFileIdStr : uploadedFileIds) {
			long uploadedFileId = Long.parseLong(uploadedFileIdStr);
					
			UploadedFile uploadedFile = uploadedFileDao.findById(uploadedFileId);
			
			String fileName = uploadedFile.getPath() + "/" + uploadedFile.getIdentifier() + "." + uploadedFile.getExtension();
			
			fileNamesMap.put(fileName, uploadedFile.getFilename());
		}
		
		String zipUrl = "";
		
		try {
			zipUrl = sftpService.downloadMultipleFiles(fileNamesMap);
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "{\"url\":\""+zipUrl+"\"}";
	}

	public SFTPService getSftpService() {
		return sftpService;
	}
	
	@Autowired(required = true)
	public void setSftpService(SFTPService sftpService) {
		this.sftpService = sftpService;
	}

	public UploadedFileDao getUploadedFileDao() {
		return uploadedFileDao;
	}
	
	@Autowired(required = true)
	public void setUploadedFileDao(UploadedFileDao uploadedFileDao) {
		this.uploadedFileDao = uploadedFileDao;
	}
}
