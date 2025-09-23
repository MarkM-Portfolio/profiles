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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.apache.struts.validator.Resources;

/**
 * @author sberajaw
 */
public class FileSizeValidator implements Serializable 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1714159142775304392L;

	public static boolean validateFileSize(Object bean, ValidatorAction va, 
		Field field, ActionMessages errors, HttpServletRequest request) throws Exception 
	{
		// if (not) required return;
		if (!ValidWhenHelper.setVarsAndCheckIfRequired(bean, field, request))
			return true;
		
		// size is in bytes
		int maxFileSize = Integer.parseInt(field.getVarValue("maxfilesize"));
		
		FormFile file = (FormFile) PropertyUtils.getSimpleProperty(bean, field.getProperty());
		if (file != null && file.getFileSize() > maxFileSize) {
			errors.add(field.getKey(), Resources.getActionMessage(request, va, field));
			return false;
		}
		
		return true;
	}
}
