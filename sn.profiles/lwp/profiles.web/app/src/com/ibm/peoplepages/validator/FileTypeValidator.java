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
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.Resources;

/**
 * @author sberajaw
 */
public class FileTypeValidator implements Serializable 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5948438647768411251L;

	public static boolean validateFileType(Object bean, ValidatorAction va, 
		Field field, ActionMessages errors, HttpServletRequest request) throws Exception 
	{
		// if (not) required return;
		if (!ValidWhenHelper.setVarsAndCheckIfRequired(bean, field, request))
			return true;

		// Due to the dojo.iframe.send() is used to upload photo, there
		// is no way to catch any validation error. So bypass the validation
		// and let UploadPhotoAction handles it
		if ("photo".equalsIgnoreCase( field.getKey() ) ) {
		    // by pass the validation so that UploadPhotoAction will catch this
		    return true;
		}

		StringTokenizer tokenizer = new StringTokenizer(field.getVarValue("allowedmimetypes"), ",");
		String[] allowedMimeTypes = new String[tokenizer.countTokens()];
		int i = 0;
		while(tokenizer.hasMoreTokens()) {
			allowedMimeTypes[i++] = tokenizer.nextToken().trim();
		}
		
		FormFile file = (FormFile) PropertyUtils.getSimpleProperty(bean, field.getProperty());
		if (file != null && file.getFileData().length > 0) {
			String contentType = file.getContentType();
			for (i = 0; i < allowedMimeTypes.length; i++) {
				if (contentType.equalsIgnoreCase(allowedMimeTypes[i])) {
					return true;
				}
			}
			
			errors.add(field.getKey(), Resources.getActionMessage(request, va, field));
			return false;
		}
		
		return true;
	}
}
