package org.spontaneous.activities.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

	public static final String EMPTY_STRING = "";
	public static final String DATE_PATTERN = "dd.MM.yy HH:mm";
	
	public final static long ONE_SECOND = 1000;
	public final static long SECONDS = 60;

	public final static long ONE_MINUTE = ONE_SECOND * 60;
	public final static long MINUTES = 60;

	public final static long ONE_HOUR = ONE_MINUTE * 60;
	public final static long HOURS = 24;

	public final static long ONE_DAY = ONE_HOUR * 24;
	  
	public static String printDate(Long timeInMillis) {
		
		if (timeInMillis != null) {
			Date date = new Date(timeInMillis);
			SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN);
			return format.format(date);
		}
		return EMPTY_STRING;
	}
	
	/**
	   * Converts time (in milliseconds) to human-readable format
	   *  "<dd:>hh:mm:ss"
	   */
	public static String millisToShortDHMS(long duration) {
		String res = "";
	    duration /= ONE_SECOND;
	    int seconds = (int) (duration % SECONDS);
	    duration /= SECONDS;
	    int minutes = (int) (duration % MINUTES);
	    duration /= MINUTES;
	    int hours = (int) (duration % HOURS);
	    int days = (int) (duration / HOURS);
	    if (days == 0 && hours == 0) {
	    	res = String.format("%02d:%02d", minutes, seconds);
	    } else if (days == 0) {
	    	res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	    } else {
	      res = String.format("%dd%02d:%02d:%02d", days, hours, minutes, seconds);
	    }
	    return res;
	  }
}
