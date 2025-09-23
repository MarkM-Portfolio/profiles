/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2010                                      */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.validator;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.struts.action.ActionMessages;

/**
 * This is a generic class for calling standard struts validators with the constructs needed for lotus connections
 */
public class LCStrutsValidators {

	public static final String VAR_VALIDATOR_CLS = "lc_validatorCls";
	public static final String VAR_VALIDATOR_METHOD = "lc_validatorMethod";	
	
	private static final String STRUTS_FIELD_CHECKS = "org.apache.struts.validator.FieldChecks";
	private static final String STRUTS_FIELD_CHECKS_MASK = "validateMask";
	private static final String STRUTS_FIELD_CHECKS_REQUIED = "validateRequired";
	
	/**
	 * Custom impl of 'Required' validator
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static boolean validateRequired(Object bean, ValidatorAction va, 
			Field field, ActionMessages errors, HttpServletRequest request) throws Exception 
	{
		return callValidate(STRUTS_FIELD_CHECKS, STRUTS_FIELD_CHECKS_REQUIED, bean, va, field, errors, request);
	}
	
	/**
	 * Custom impl of 'mask' validator
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static boolean validateMask(Object bean, ValidatorAction va, 
			Field field, ActionMessages errors, HttpServletRequest request) throws Exception 
	{
		return callValidate(STRUTS_FIELD_CHECKS, STRUTS_FIELD_CHECKS_MASK, bean, va, field, errors, request);
	}
	
	/**
	 * Generic method for validators
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static boolean genericValidate(Object bean, ValidatorAction va, 
			Field field, ActionMessages errors, HttpServletRequest request) throws Exception 
	{
		String methodName = field.getVarValue(VAR_VALIDATOR_METHOD);
		if (StringUtils.isEmpty(methodName))
			throw new IllegalArgumentException("Must specify 'lc_validatorMethod' <var> to use this validator.");
		
		String clsName = field.getVarValue(VAR_VALIDATOR_CLS);
		if (StringUtils.isEmpty(clsName))
			throw new IllegalArgumentException("Must specify 'lc_validatorCls' <var> to use this validator");
		
		return LCStrutsValidators.callValidate(clsName, methodName, bean, va, field, errors, request);
	}
	
	/**
	 * Invokes the validator
	 * 
	 * @param clsName
	 * @param methodName
	 * @param bean
	 * @param va
	 * @param field
	 * @param errors
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private static boolean callValidate(
			String clsName, String methodName,
			Object bean, ValidatorAction va, 
			Field field, ActionMessages errors, HttpServletRequest request) throws Exception
	{
		// if (not) required return;
		if (!ValidWhenHelper.setVarsAndCheckIfRequired(bean, field, request))
			return true;
		
		Class<?> validatorCls = getValidatorCls(clsName);
		Method validatorMethod = getValidatorMethod(validatorCls, methodName);
		
		return (Boolean) validatorMethod.invoke(null, bean, va, field, errors, request);
	}

	/**
	 * Gets the validator method associated with this class
	 * @param validatorCls
	 * @param field
	 * @return
	 * @throws Exception 
	 */
	private static Method getValidatorMethod(Class<?> validatorCls, String methodName) throws Exception 
	{
		try {
			return validatorCls.getMethod(methodName, Object.class, ValidatorAction.class, Field.class, ActionMessages.class, HttpServletRequest.class);
		} catch (Exception e) {
			throw new Exception("Unable to find validator method: " + methodName + " in class: " + validatorCls.getName());
		}
	}

	/**
	 * Gets the validator
	 * @param clsName2
	 * @return
	 * @throws Exception 
	 */
	private static Class<?> getValidatorCls(String clsName) throws Exception 
	{
		try {
			return Class.forName(clsName);
		} catch (Exception e) {
			throw new Exception("Unable to find validator class: " + clsName, e);
		}
	}
	
}
