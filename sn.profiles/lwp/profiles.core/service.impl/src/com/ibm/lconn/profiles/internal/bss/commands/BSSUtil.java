/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.bss.commands;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;
import com.ibm.lconn.commands.IPlatformCommandConstants;
import com.ibm.lconn.commands.IPlatformCommandRecord;
import com.ibm.lconn.commands.PlatformCommandResponse;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClient;

public class BSSUtil {

	// command must be a subscriber command
	public static  DSObject lookupSubscriber(WaltzClient waltzclient, IPlatformCommandRecord command) throws DSException {
		String subscriberId = (String) command.getProperties().get(IPlatformCommandConstants.DIRECTORYID);
		String orgId = (String) command.getProperties().get(IPlatformCommandConstants.CUSTOMER_ID);
		DSObject rtnVal = waltzclient.exactUserIDMatch(subscriberId,orgId);
		return rtnVal;
	}
	
	public static boolean isSuccess(PlatformCommandResponse response){
		String val = response.getResponseCode();
		return (StringUtils.isEmpty(val) || val.equals(IPlatformCommandConstants.SUCCESS));
	}
	
	public static String getSummary(IPlatformCommandRecord command) {
		String value = null;
		if ( command == null){
			return value;
		}
		StringBuilder ret = new StringBuilder("Profiles received BSS command: ");
		if (command != null) {
			Map<String,Object> props = command.getProperties();
			if ( props != null){
				ret.append("name:").append(command.getCommandName()).append(" ");
				ret.append(IPlatformCommandConstants.DIRECTORYID).append(":").append(props.get(IPlatformCommandConstants.DIRECTORYID))
				   .append(" ");
				ret.append(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID).append(":").append(props.get(IPlatformCommandConstants.LOTUSLIVE_CUSTOMER_ID))
				   .append(" ");;
				ret.append(IPlatformCommandConstants.EMAIL).append(":").append(props.get(IPlatformCommandConstants.EMAIL));
			}
		}
		return ret.toString();
	}
	
	/**
	 * Get string representation suited for output to logs.
	 */
	public static String getPrintString(IPlatformCommandRecord command) {
		String value = null;
		if ( command == null){
			return value;
		}
		StringBuilder result = new StringBuilder();
		value = command.getCommandName();
		if (StringUtils.isNotEmpty(value)) {
			result.append("\nName         : " + value);
		}
		else{
			result.append("\nName         : empty ");
		}
		value = command.getCommandRecordId();
		if (StringUtils.isNotEmpty(value)) {
			result.append("\nRecordId     : " + value);
		}
		else{
			result.append("\nRecordId     : empty ");
		}
		value = command.getSource();
		if (StringUtils.isNotEmpty(value)) {
			result.append("\nSource       : " + value);
		}
		else{
			result.append("\nSource       : empty ");
		}
		Locale locale = command.getLocale();
		if (locale != null) {
			value = locale.toString();
			if (StringUtils.isNotEmpty(value)) result.append("\nLocale       : " + value);
		}
		else{
			result.append("\nLocale       : empty ");
		}
		Map<String, Object> properties = command.getProperties();
		result.append("\nCommand has  : " + properties.size() + " properties");
		for (String key : properties.keySet()) {
			result.append("\nProperty key : " + key + " value : " + properties.get(key));
		}
		return result.toString();
	}
	
	/**
	 * Get single line string representation suited for inclusion in BSS responses
	 */
	public static String getString(IPlatformCommandRecord command) {
		String value = null;
		if ( command == null){
			return value;
		}
		StringBuilder result = new StringBuilder();
		value = command.getCommandName();
		if (StringUtils.isNotEmpty(value)) {
			result.append("Name : " + value);
		}
		else{
			result.append("Name : empty ");
		}
		value = command.getCommandRecordId();
		if (StringUtils.isNotEmpty(value)) {
			result.append(" ; RecordId : " + value);
		}
		else{
			result.append(" ; RecordId : empty");
		}
		value = command.getSource();
		if (StringUtils.isNotEmpty(value)) {
			result.append(" ; Source : " + value);
		}
		else{
			result.append(" ; Source : empty");
		}
		Locale locale = command.getLocale();
		if (locale != null) {
			value = locale.toString();
			if (StringUtils.isNotEmpty(value)) result.append(" ; Locale : " + value);
		}
		else{
			result.append(" ; Locale : null");
		}
		Map<String, Object> properties = command.getProperties();
		result.append(" ; Command has  : " + properties.size() + " properties");
		for (String key : properties.keySet()) {
			result.append(" ; key : " + key + " value : " + properties.get(key));
		}
		return result.toString();
	}
	
	public static StringBuffer throwableString(Throwable t){
		StringBuffer msg = new StringBuffer();
		if (t != null){
			// use the the throwable message if available. ow use the stack trace for a hint
			if (StringUtils.isEmpty(t.getMessage())==false){
				msg.append(t.getMessage());
			}
			else{
				//see if there is a cause and use that
				Throwable thr = t.getCause();
				if (thr == null || thr.getStackTrace().length == 0){
					// else use the original throwable
					thr = t;
				}
				//StackTraceElement[] elements = Thread.currentThread().getStackTrace();
				StackTraceElement[] elements = thr.getStackTrace();
				int number = Math.min(elements.length,30);  // 30 is arbitrary number in an attempt to limit the return
				StackTraceElement s;
				for (int i = 0; i < number; i++) {
					s = elements[i];
					msg.append(s.getClassName()).append(".").append(s.getMethodName()).append(".")
					   .append(s.getFileName()).append(":").append(s.getLineNumber())
					   .append(System.getProperty("line.separator"));
				}
			}
		}
		return msg;
	}
	
	public static void logError(Logger LOGGER, IPlatformCommandRecord command, String msg){
		StringBuffer sb = new StringBuffer().append("Profiles error processing command: ")
				.append(BSSUtil.getPrintString(command)).append(" : response message : ").append(msg);
		LOGGER.log(Level.WARNING,sb.toString());
	}
}
