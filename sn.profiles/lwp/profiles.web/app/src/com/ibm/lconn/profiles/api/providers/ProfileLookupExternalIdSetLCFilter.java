/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.providers;

import java.util.List;
import java.util.Map;

import org.apache.abdera.protocol.server.ResponseContext;

import com.ibm.lconn.core.web.atom.LCFilter;
import com.ibm.lconn.core.web.atom.LCFilterChain;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.util.LCModelHelper;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;

/**
 *
 *
 */
public class ProfileLookupExternalIdSetLCFilter implements LCFilter {

	private final ProfileService service = AppServiceContextAccess.getContextObject(ProfileService.class);
	
	private final Map<String,ProfileLookupKey.Type> paramTypeMap;
	private final String modelAttribute;	
	
	/**
	 * Creates filter with method to retrieve PLK object
	 * @param paramTypeMap
	 * @param targetType
	 * @param modelAttribute
	 * @param saveAsRequestObject Request-Attribute in which PLK will be saved
	 */
	public ProfileLookupExternalIdSetLCFilter(
			final Map<String, Type> paramTypeMap, 
			final String modelAttribute) 
	{		
		this.paramTypeMap = paramTypeMap;
		this.modelAttribute = modelAttribute;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.core.web.atom.LCFilter#doFilter(com.ibm.lconn.core.web.atom.LCRequestContext, com.ibm.lconn.core.web.atom.LCFilterChain)
	 */
	public ResponseContext doFilter(LCRequestContext request,
			LCFilterChain chain) throws Exception 
	{
		ProfileLookupKeySet plkSet = RequestContextUtils.getProfileLookupKeySet(request, paramTypeMap);
		
		if (plkSet != null) {
			List<String> keys = service.getExternalIdsForSet(plkSet);
			AssertionUtils.assertTrue(keys.size() > 0, AssertionType.BAD_REQUEST);
		
			LCModelHelper.setModelAttribute(request, modelAttribute, keys);
		}
		
		return chain.next(request);
	}

}
