package edu.uams.clara.core.util.log;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogUtils {

	public static final Marker EMAIL_MARKER = MarkerFactory.getMarker("EMAIL");
	
	public static Marker email(){
		return EMAIL_MARKER;
	}
}
