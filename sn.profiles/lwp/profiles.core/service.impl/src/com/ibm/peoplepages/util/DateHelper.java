/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DateHelper
{

    private static Pattern ampm = Pattern.compile("([0-9])\\s*([p,P,a,A][m,M])");
    
    public static final int TwoDigitYearBalancePoint = 30;
    
    public static Date getCurrentTimestamp() {
        return new Date();
    }
    
    public static String cleanTimeString(String time) {
        time = time.trim();
        Matcher tm = ampm.matcher(time);
        time = tm.replaceFirst("$1 $2"); //space pm/am appropriately
        time = time.toUpperCase();
        return time;
    }
    
    //only checks against US formatted dates
    //TODO: need to import org.apache.commons.validator
    //to get a package that can validate against any give Locale
    public static boolean checkDateBounds(String str) {
        SimpleDateFormat dateFormat = getDateFormat();
        dateFormat.setLenient(false);
        try {dateFormat.parse(str);}
        catch (ParseException e) {return false;}
        
        return true;
    }
    
    public static boolean checkTimeBounds(String str) {
        boolean inbounds = false;
        SimpleDateFormat timeFormat = getTimeFormat();
        timeFormat.setLenient(false);
        try {
            //Date date;
        	timeFormat.parse(str); //can it be parsed?
            inbounds = true;
        }
        catch (ParseException e) {
            inbounds = false;
        }
        return inbounds;
    }
    
    public static Date combineDates( String dateStr, String timeStr) {
        Date returnDate = null;
        Date date = null, time = null;
        
        SimpleDateFormat dateFormat = getDateFormat();
        SimpleDateFormat timeFormat = getTimeFormat();
        
        //ignore the exception
        try {
            date = dateFormat.parse( dateStr);
        } catch (ParseException e) {}
            
        //ignore the exception
        try {
            time = timeFormat.parse( timeStr);
        } catch (ParseException e) {}
        
        if ( date != null || time != null) {
	        Calendar calendar = Calendar.getInstance();
	        
	        if ( date != null) {
	            calendar.setTime( date);
	            
	            int year =  calendar.get( Calendar.YEAR);
	            
                //if the user puts 00-29, assume he means 2000s
                //if the user puts 30-99, assume he means 1900s
	            if ( year < TwoDigitYearBalancePoint)
	                calendar.set( Calendar.YEAR, year + 2000);
	            else if ( year < 100) {
	                calendar.set( Calendar.YEAR, year + 1900);
	            }
	        }
	          
	        if ( time != null) {
	            Calendar tmpCal = Calendar.getInstance();
	            tmpCal.setTime( time);
	            
	            calendar.set(Calendar.HOUR_OF_DAY, tmpCal.get(Calendar.HOUR_OF_DAY));
	            calendar.set(Calendar.MINUTE, tmpCal.get(Calendar.MINUTE));
	        }
	        
	        returnDate = calendar.getTime();
        }
        
        return returnDate;
    }
    
    /**
     * http://findbugs.sourceforge.net/bugDescriptions.html#STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE
     * @return
     */
    private static SimpleDateFormat getDateFormat() {
    	return new SimpleDateFormat("MM/dd/yyyy");
    }
    
    /**
     * http://findbugs.sourceforge.net/bugDescriptions.html#STCAL_STATIC_SIMPLE_DATE_FORMAT_INSTANCE
     * @return
     */
    private static SimpleDateFormat getTimeFormat() {
    	return new SimpleDateFormat("hh:mm aaa");
    }
}
