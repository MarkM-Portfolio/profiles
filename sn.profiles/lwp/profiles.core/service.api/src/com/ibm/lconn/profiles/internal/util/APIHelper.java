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

import org.apache.commons.lang.StringEscapeUtils;

import com.ibm.jse.util.xml.XMLUtil;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * @author ahernm@us.ibm.com
 * 
 */
public final class APIHelper
{
  private APIHelper()
  {
  }

  public static final void filterProfileAttrForAPI(Employee emp)
  {
    AssertionUtils.assertNotNull(emp);

    ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
    for (Property p : profileType.getProperties())
    {
      if (p.isHidden())
      {
        emp.remove(p.getRef());
      }
    }
  }

    /**
     *  A method to strip all invalid XML characters from all Profile fields. This is to prevent
     *  bad XML characters to screw up any ATOM feeds.
     * 
     *  @param emp The employee object to be cleaned.
     */
    public static void stripInvalidXMLCharacters(Employee emp) {
	
	ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
	
	for (Property p : profileType.getProperties()) {
	    
	    String attributeId = p.isExtension() ? Employee.getAttributeIdForExtensionId(p.getRef()) : p.getRef();
	    if (emp.containsKey(attributeId)) {
		if (p.isExtension()) {
		    ProfileExtension pe = emp.getProfileExtension(p.getRef(), true);
		    String source = pe.getStringValue();
		    if (source != null && source.length() > 0) {
			String filtered = XMLUtil.stripInvalidXmlChars( source );
			pe.setStringValue(filtered);
		    }
		}
		else {
		    String source = (String) emp.get(attributeId);
		    if (source != null && source.length() > 0) {
			emp.put(attributeId, XMLUtil.stripInvalidXmlChars(source));
		    }
		}
	    }
	}
    }

    /**
     *  A method to escape HTML from all Profile fields. This is used for generating JSON feed.
     * 
     *  @param emp The employee object to be cleaned.
     */
    public static void escapeHtml(Employee emp) {
	
	ProfileType profileType = ProfileTypeHelper.getProfileType(emp.getProfileType());
	
	for (Property p : profileType.getProperties()) {
	    
	    String attributeId = p.isExtension() ? Employee.getAttributeIdForExtensionId(p.getRef()) : p.getRef();
	    if (emp.containsKey(attributeId)) {
		if (p.isExtension()) {
		    ProfileExtension pe = emp.getProfileExtension(p.getRef(), true);
		    String source = pe.getStringValue();
		    if (source != null && source.length() > 0) {
			String filtered = StringEscapeUtils.escapeHtml( source );
			pe.setStringValue(filtered);
		    }
		}
		else {
		    String source = (String) emp.get(attributeId);
		    if (source != null && source.length() > 0) {
			emp.put(attributeId, StringEscapeUtils.escapeHtml(source));
		    }
		}
	    }
	}
    }

    /**
     *  A helper method to determine if the search type is for the Codes API.
     * 
     *  @param searchType - the type of search being performed
     *  @return true if one of the codes API types; false otherwise.
     */
	public static boolean isCodeSearchType(String searchType) {
		boolean retVal = false;
		// jtw: Not in 4.5. static analysis tools single out the string compare with ==. comment out in preparation
		// for testing and ultimately deletion
		// wja: removed in 5.0+ 
		// else if ((codeType == PeoplePagesServiceConstants.WORK_LOC_CODE) || (codeType == PeoplePagesServiceConstants.DEPTARTMENT_CODE)
		//		|| (codeType == PeoplePagesServiceConstants.CCODE) || (codeType == PeoplePagesServiceConstants.OCODE)
		//		|| (codeType == PeoplePagesServiceConstants.ECODE)) {

		if (	searchType.equals(PeoplePagesServiceConstants.WORK_LOC_CODE)
			||	searchType.equals(PeoplePagesServiceConstants.DCODE)
			||	searchType.equals(PeoplePagesServiceConstants.CCODE)
			||	searchType.equals(PeoplePagesServiceConstants.OCODE)
			||	searchType.equals(PeoplePagesServiceConstants.ECODE))
		{
			retVal = true;
		}
		return retVal;
	}

    /**
     *  Helper methods to get a limited depth stack trace rather than the 200 line monstrosity that is normally dumped in the log.
     *
     *  @param depth - the depth of stack that should be returned. Normally the stack is of limited use above 15 elements
     *                 (mostly Struts and filters) unless you are interested in those levels specifically.
     *  @return a string containing the stack of requested depth.
     */
	public static String getCallerStack(int depth)
	{
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String stackString = getCallerStack(stackTrace, depth, false);
		return stackString;
	}

	public static String getCallerStack(Exception ex, int depth)
	{
		StringBuffer    sb = new StringBuffer();
		String stackHeader = ex.getMessage();
		// if there is an exception message use it
		if (null == stackHeader) {
			stackHeader = ex.getClass().getSimpleName();
		}
		sb.append(stackHeader);
		// if this is an Assertion exception get the cause
		if (ex instanceof AssertionException) {
			AssertionException aex = (AssertionException) ex;
			String reason = aex.getType().name();
			sb.append(" : " + reason);
		}
		StackTraceElement[] stackTrace = ex.getStackTrace();
		String stackString = getCallerStack(stackTrace, depth, true);
		sb.append(stackString);
		return sb.toString();
	}

	private static String getCallerStack(StackTraceElement[] stackTrace, int depth, boolean isException)
	{
		StringBuffer sb = new StringBuffer();
		int peek = 3;
		if (isException) // if not in an Exception, the first 3 levels are not interesting
			peek = 0;
		try {
			for (int i = 0; i < depth; i++) {
				StackTraceElement callerElement = stackTrace[(peek + i)];
				String className = callerElement.getClassName();
				int    index     = className.lastIndexOf(".");
				String fileName  = className.substring(index+1, className.length()) + ".java";
				String callerMethod = className + "." + callerElement.getMethodName() + "(" + fileName + ":" + callerElement.getLineNumber() + ")";
				sb.append("\n  at " + callerMethod);
			}
		}
		catch (Exception ex) {
			// hmmm not much we can do; probably walked off the end of a shallow array; just return what we have accumulated
		}
		return sb.toString();
	}
}
