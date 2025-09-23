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

package com.ibm.peoplepages.validator;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.util.ValidatorUtils;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.validator.Resources;

/**
 * @author sberajaw
 */
public class MaxByteLengthValidator implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -68617862446774595L;

	public static boolean validateMaxByteLength(Object bean, ValidatorAction va, 
		Field field, ActionMessages errors, HttpServletRequest request) throws Exception 
	{
		// if (not) required return;
		if (!ValidWhenHelper.setVarsAndCheckIfRequired(bean, field, request))
			return true;
		
		int maxByteLength = Integer.parseInt(field.getVarValue("maxbytelength"));
		
		String inputValue = ValidatorUtils.getValueAsString(bean, field.getProperty());
		
		int bytesNeeded = 0;
		if (inputValue != null) {
			for (int i = 0; i < inputValue.length(); i++) {
				int ch = inputValue.codePointAt(i);
				if (ch < 0x80) {
					bytesNeeded++;
				}
				else if (ch < 0x0800) {
					bytesNeeded += 2;
				}
				else if (ch < 0x10000) {
					bytesNeeded += 3;
				}
				else {
					bytesNeeded += 4;
				}
			}
			if (bytesNeeded <= maxByteLength) {
				return true;
			}
			
			int avgBytesPerChar = bytesNeeded/inputValue.length();
			int maxChars = (int) Math.floor(maxByteLength/avgBytesPerChar);
			
			Arg[] args = field.getArgs("maxbytelength");
			for (int i = 0; i < args.length; i++) {
				Arg arg = args[i];
				if (arg != null) {
					String key = arg.getKey();
					if (key != null && key.equalsIgnoreCase("(maxbytelength)")) {
						arg.setKey(Integer.toString(maxChars));
					}
				}
			}
			
			errors.add(field.getKey(), Resources.getActionMessage(request, va, field));
			return false;
		}
		
		return true;
	}
}
