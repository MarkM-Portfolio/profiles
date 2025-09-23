/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;

/**
 *
 *
 */
public class OrgStructureDisabledAdvise implements MethodInterceptor {

	private final boolean enabled = DataAccessConfig.instance().isOrgStructureEnabled();
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invoke) throws Throwable {
		if (!enabled)
			throw new AssertionException(AssertionType.UNAUTHORIZED_ACTION);
		
		return invoke.proceed();
	}

}
