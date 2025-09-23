/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;

/**
 * Utility class to add 'assertion' capabilities to classes. Throws unchecked
 * (rumtime) exception if assertion fails. Generally this is only handled in the
 * UI / API layers; unchecked exception type makes this simpler to implement. In
 * addition, as this is a runtime exception, this class integrates well with
 * Spring transactions.
 * 
 * @author ahernm@us.ibm.com
 */
public abstract class AssertionUtils 
{
	/**
	 * Checks if an 'assertion' is true.  Throws 'precondition' failure if fails.
	 * 
	 * @param assertion_p
	 * @throws AssertionException
	 */
	public static final void assertTrue(boolean assertion_p) throws AssertionException
	{
		assertTrue(assertion_p, AssertionType.PRECONDITION);
	}
	
	/**
	 * Checks if an 'assertion' is true.  Throws assertion failure with specified cause.
	 * 
	 * @param assertion_p
	 * @param type
	 * @throws AssertionException
	 */
	public static final void assertTrue(boolean assertion_p, AssertionType type) throws AssertionException
	{
		assertTrue(assertion_p, type, null);
	}
	
	/**
	 * Check if the 'assertion' is true.  Throw an assertion failure with the specified cause.  If a message is specified, use it.
	 * @param assertion_p
	 * @param type
	 * @param message
	 * @throws AssertionException
	 */
	public static final void assertTrue(boolean assertion_p, AssertionType type, String message) throws AssertionException
	{
		if (!assertion_p)
		{
			AssertionType _resolvedType = getTypeEnum(type, AssertionType.PRECONDITION);
			if (StringUtils.isEmpty(message)) {
				throw new AssertionException(_resolvedType);
			} else {
				throw new AssertionException(_resolvedType, message);
			}
		}
	}
	
	/**
	 * Simple assertion for null-ness
	 * 
	 * @param obj
	 * @throws AssertionException
	 */
	public static final void assertNotNull(Object obj) throws AssertionException
	{
		assertNotNull(obj, AssertionType.PRECONDITION);
	}
	
	/**
	 * Simple assertion for null-ness with result type
	 * 
	 * @param obj
	 * @param type
	 * @throws AssertionException
	 */
	public static final void assertNotNull(Object obj, AssertionType type) throws AssertionException
	{
		assertTrue(obj != null, type);
	}
	
	/**
	 * Simple assertion for null-ness
	 * 
	 * @param str
	 * @throws AssertionException
	 */
	public static final void assertNotEmpty(String str) throws AssertionException
	{
		assertNotEmpty(str, AssertionType.PRECONDITION);
	}
	
	/**
	 * Simple assertion for null-ness
	 * 
	 * @param str
	 * @param type
	 * @throws AssertionException
	 */
	public static final void assertNotEmpty(String str, AssertionType type) throws AssertionException
	{
		assertTrue(nonEmptyString(str), type);
	}
	
	/**
	 * Simple assertion for to verify message is not null
	 * 
	 * @param message
	 * @param type
	 * @throws AssertionException
	 */
	public static final void assertNotEmpty(String str, AssertionType type, String message) throws AssertionException
	{	
		assertTrue(nonEmptyString(str), type, message);
	}
	
	/**
	 * Convenience method to assert string equality
	 * 
	 * @param s1
	 * @param s2
	 * @throws AssertionException
	 */
	public static final void assertEquals(String s1, String s2) throws AssertionException
	{
		assertTrue(stringEquals(s1,s2));
	}
	
	/**
	 * Convenience method to assert string equality
	 * 
	 * @param s1
	 * @param s2
	 * @param type
	 * @throws AssertionException
	 */
	public static final void assertEquals(String s1, String s2, AssertionType type) throws AssertionException
	{
		assertTrue(stringEquals(s1,s2), type);
	}

	/**
	 * Utility method that checks if string is non-null and length is greater than 0.
	 * 
	 * @param str
	 * @return
	 */
	public static final boolean nonEmptyString(String str)
	{
		if (str != null && str.length() > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Utility method to check string equality in null safe way
	 * 
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static final boolean stringEquals(String s1, String s2)
	{
		if (s1 == null)
		{
			return (s2 == null);
		}
		else
		{
			return s1.equals(s2);
		}
	}
	
	/**
	 * 
	 * @param cause
	 * @param defaultCause
	 * @return
	 */
	private static final AssertionType getTypeEnum(AssertionType cause, AssertionType defaultCause)
	{
		if (cause == null)
		{
			return defaultCause;
		}
		else
		{
			return cause;
		}
	}

	
}
