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
package com.ibm.lconn.profiles.api.providers;

import java.util.Map;

import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.RequestContext.Scope;

import com.ibm.lconn.core.web.atom.LCFilter;
import com.ibm.lconn.core.web.atom.LCFilterChain;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.util.LCModelHelper;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.service.PeoplePagesService;

/**
 *
 *
 */
public class ProfileLookupKeyLCFilter implements LCFilter {

	private final PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	
	private final Map<String,ProfileLookupKey.Type> paramTypeMap;
	private final ProfileLookupKey.Type targetType;
	private final String modelAttribute;
	private final String saveAsRequestObject;
	
	
	/**
	 * Creates filter with no method to retrieve PLK object
	 * @param paramTypeMap
	 * @param targetType
	 * @param modelAttribute
	 */
	public ProfileLookupKeyLCFilter(
			final Map<String, Type> paramTypeMap, 
			final ProfileLookupKey.Type targetType,
			final String modelAttribute) 
	{
		this(paramTypeMap, targetType, modelAttribute, null);
	}
	
	/**
	 * Creates filter with method to retrieve PLK object
	 * @param paramTypeMap
	 * @param targetType
	 * @param modelAttribute
	 * @param saveAsRequestObject Request-Attribute in which PLK will be saved
	 */
	public ProfileLookupKeyLCFilter(
			final Map<String, Type> paramTypeMap, 
			final ProfileLookupKey.Type targetType,
			final String modelAttribute,
			final String saveAsRequestObject) 
	{		
		this.paramTypeMap = paramTypeMap;
		this.targetType = targetType;
		this.modelAttribute = modelAttribute;
		this.saveAsRequestObject = saveAsRequestObject;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.web.atom.LCFilter#doFilter(com.ibm.lconn.core.web.atom.LCRequestContext, com.ibm.lconn.core.web.atom.LCFilterChain)
	 */
	public ResponseContext doFilter(LCRequestContext request,
			LCFilterChain chain) throws Exception 
	{
		ProfileLookupKey plk = RequestContextUtils.getProfileLookupKey(request, paramTypeMap);
		AssertionUtils.assertNotNull(plk, AssertionType.BAD_REQUEST);
		
		String value = service.getLookupForPLK(targetType, plk, false);
		AssertionUtils.assertNotNull(value, AssertionType.BAD_REQUEST);
		
		LCModelHelper.setModelAttribute(request, modelAttribute, value);
		
		if (saveAsRequestObject != null)
			request.setAttribute(Scope.REQUEST, saveAsRequestObject, plk);
		
		return chain.next(request);
	}

}
