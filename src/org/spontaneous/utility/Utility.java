package org.spontaneous.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Class which has Utility methods
 *
 */
public class Utility {
	
    private static Pattern pattern;
    private static Matcher matcher;
    
    //Email Pattern
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 
    /**
     * Validate Email with regular expression
     *
     * @param email
     * @return true for Valid Email and false for Invalid Email
     */
    public static boolean validate(String email) {
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
 
    }
    /**
     * Checks for Null String object
     *
     * @param txt
     * @return true for not null and false for null String object
     */
    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }
    
    /**
     * Escapes "\" and beginning and ending '"' in the given string.
     * @param s
     * @return
     */
    public static String escape(String s) {
    	 String s1 = s.replaceAll("\\\\", "");
	 	 String s2 = s1.substring(1, s1.length() - 1);
	 	 return s2;
    }
}
