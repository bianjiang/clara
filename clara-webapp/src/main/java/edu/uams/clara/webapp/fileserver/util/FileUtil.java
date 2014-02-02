package edu.uams.clara.webapp.fileserver.util;

public class FileUtil {

	public static String getExtension(String filename){
		return (filename.lastIndexOf(".")==-1)?"":filename.substring(filename.lastIndexOf(".")+1,filename.length());
	}
}
