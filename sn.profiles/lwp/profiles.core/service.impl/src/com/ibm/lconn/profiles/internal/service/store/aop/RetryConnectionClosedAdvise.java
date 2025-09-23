/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.aop;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.jdbc.BadSqlGrammarException;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

/**
 * AOP Advice to retry DB operations if the connection is lost.
 * 
 *
 */
public class RetryConnectionClosedAdvise implements MethodInterceptor {

	private static final Logger logger = Logger.getLogger(RetryConnectionClosedAdvise.class.getName());
	private static final Logger sqlLogger = Logger.getLogger("com.ibm.lconn.profiles.internal.service.store.sqlmapdao.sql");
	
	private final int maxAttempts;
	private final long interval;
	private final Class<?> staleConnEx;
		
	public RetryConnectionClosedAdvise() {
		this.maxAttempts = PropertiesConfig.instance().getIntValue(ConfigProperty.RETRY_DAO_CONNECTIONS_ATTEMPTS);
		this.interval = PropertiesConfig.instance().getIntValue(ConfigProperty.RETRY_DAO_CONNECTION_INTERVAL);
		this.staleConnEx = getStaleConnEx();
	}
	
	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invoke) throws Throwable {
		boolean FINEST = logger.isLoggable(Level.FINEST);
		
		if (FINEST) {
			logger.finest("RetryConnectionClosedAdvise invoked on: " + 
					invoke.getThis().getClass() + "." + invoke.getMethod().getName());
		}
		
		if (!(invoke instanceof ReflectiveMethodInvocation))
			throw new IllegalArgumentException("RetryConnectionClosedAdvise only works when using Spring reflective AOP");
		
		// 
		if (sqlLogger.isLoggable(Level.FINEST)) {
			
		}
		
		ReflectiveMethodInvocation rInvoke = (ReflectiveMethodInvocation) invoke;
		int attempt = 1;
		
		do {	
			try {	
				return rInvoke.invocableClone().proceed();
			} 
			catch (BadSqlGrammarException badGrammarEx) { 
				Object[] args = invoke.getArguments();
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < args.length; i++)
					sb.append("ARG[" + i + "]: ").append(args[i]).append(i != args.length-1 ? ", " : "");
				
				logger.log(Level.WARNING, "Bad grammer exception " + invoke.getMethod() + "(" + sb.toString() + ") " + badGrammarEx.getMessage(), badGrammarEx);
			
				throw badGrammarEx;
			} 
			catch (Exception e) {
				if (FINEST) logger.log(Level.FINEST, "Exception caught during attempt", e);
				
				boolean doContinue = false;
				
				if (staleConnEx != null && attempt < maxAttempts) {	
					Throwable cause = e;
					do {
						if (staleConnEx.isInstance(cause)) {
							doContinue = true;
							break;
						}
					} while ((cause = cause.getCause()) != null);
				}
				
				if (FINEST) logger.finest("Continuing: " + doContinue + " / attempt: " + attempt);
				
				if (!doContinue)
					throw e;
				
				// else
				attempt++;
				Thread.sleep(interval);
			}
		} while (attempt <= maxAttempts);
		
		return null; // unreachable block.. in theory
	}
	
	private Class<?> getStaleConnEx() {
		try {
			return Class.forName("com.ibm.websphere.ce.cm.StaleConnectionException");
		} catch (Throwable e) {
			return null;
		}
	}

}
