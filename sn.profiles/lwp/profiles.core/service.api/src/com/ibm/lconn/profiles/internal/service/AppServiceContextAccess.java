/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2001, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import org.springframework.context.ApplicationContext;


/**
 * Access point for non IoC managed beans to gain access to Profiles Objects
 * 
 */
public final class AppServiceContextAccess {
	
	private static ApplicationContext context;

	private AppServiceContextAccess() { }
	
	/**
	 * @return the context
	 */
	public static ApplicationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public static void setContext(ApplicationContext context) {
		AppServiceContextAccess.context = context;
	}

	/**
	 * Utility method to gain access to Spring managed application objects
	 * assuming that the name is the same as the Class.
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 */
	public static <T> T getContextObject(Class<T> type) {
		return getContextObject(type, type.getName());
	}
	
	/**
	 * Utility method to gain access to Spring managed application objects
	 * passing in the object name and a cast-to type.
	 * 
	 * @param <T>
	 * @param type
	 * @param objectName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getContextObject(Class<T> type, String objectName) {
		return (T) context.getBean(objectName, type);
	}
}
