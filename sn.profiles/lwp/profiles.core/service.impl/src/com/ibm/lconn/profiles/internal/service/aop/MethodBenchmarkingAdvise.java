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

import com.ibm.peoplepages.util.statistics.StopWatch;

/**
 *
 *
 */
public final class MethodBenchmarkingAdvise implements MethodInterceptor {

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public final Object invoke(MethodInvocation invoke) throws Throwable {
		Object result = invoke.proceed();
		return result;
	}

	/**
	 * Gets the key for a benchmark
	 * 
	 * @param invoke
	 * @return
	 */
	private final String getKey(MethodInvocation invoke) {
		Object obj = invoke.getThis();
		if (obj != null) 
			return obj.getClass().getName() + "." + invoke.getMethod().getName();
		return invoke.getMethod().getName();
	}

}
