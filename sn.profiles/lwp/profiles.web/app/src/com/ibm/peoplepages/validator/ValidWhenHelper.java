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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.Field;

import com.ibm.peoplepages.webui.forms.EditProfileForm;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ValidWhenHelper 
{
	public static boolean setVarsAndCheckIfRequired(
			Object bean, Field field, HttpServletRequest request)
	{
		if (bean instanceof EditProfileForm)
		{
			EditProfileForm form = (EditProfileForm) bean;
			String subEditForm = field.getVarValue("subEditForm");
			
			if (StringUtils.equals(subEditForm, form.getSubEditForm())) {
				request.setAttribute("tab", subEditForm); // for UI tabs
				request.setAttribute("fromValidationErr", Boolean.TRUE);
				return true;
			}
			else {
				return false;
			}
		}
		
		return true;
	}
}
