package edu.uams.clara.webapp.common.util;

public class HTMLHelper {

	public static String convertLinebreaks(String text){
		return text.replace("\n", "<br/>\n");
	}
}
