package edu.uams.clara.webapp.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateFormatUtil {

	private final static DateFormat df = new SimpleDateFormat("MM/dd/yyyy"); 
	//private final static DateFormat ymddf = new SimpleDateFormat("yyyy-MM-dd"); 
	private final static DateTimeFormatter fmt = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
	
	/**
	 * Using joda-time's DateTimeFormatter... Java's SimpleDateFormat is not thread-safe
	 * 
	 * @param date
	 * @return
	 */
	public static String formateDate(Date date){		
		if(date == null){
			return "";
		}
		DateTime dateTime = date==null?null:new DateTime(date);	
		
		//DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		return fmt.print(dateTime);
	}
	
	public static String formateDateToMDY(Date date){
		if(date == null){
			return "";
		}
		
		return df.format(date);
	}
	
	public static Date toDate(String dateString) {
		Date date = null;

		try {
			date = df.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return date;
	}
}
