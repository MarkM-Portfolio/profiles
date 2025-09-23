/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;

public class ProfilesAction extends ProfileAPIAction
{
	protected static class Bean extends BaseBean
	{
		ProfileLookupKeySet plkSet;
	}

	protected Bean instantiateActionBean_delegate(HttpServletRequest request)
			throws Exception 
	{
		Bean bean = new Bean();
		bean.searchType = "employee";
		bean.allowOverrideIsLite = true;
		bean.plkSet = getProfileLookupKeySet(request);
		
		AssertionUtils.assertNotNull(bean.plkSet);
		AssertionUtils.assertNotNull(bean.plkSet.getValues().length > 0);
		
		bean.searchType =  bean.plkSet.getType().toString().toLowerCase(Locale.US);
		bean.pageSize = bean.plkSet.getValues().length;
		
		return bean;
	}  
	
	protected void instantiateActionBean_postInit(BaseBean baseBean, HttpServletRequest request)
		throws Exception 
	{
		Bean bean = (Bean) baseBean;
		
		ProfileRetrievalOptions options = bean.isLite ? ProfileRetrievalOptions.LITE : ProfileRetrievalOptions.EVERYTHING;
		List<Employee> profiles = pps.getProfiles(bean.plkSet, options);

		// SPR: #RPAS7JZHWG In 2.0, we return an empty ArrayList when resource not found.
		// For backward compatibility, reversing the behavior to not asserting null
		// AssertionUtils.assertNotNull(profile, ProfilesAssertionException.Type.RESOURCE_NOT_FOUND);
		if ( profiles.size() > 0 )
		    bean.resultsPage = new SearchResultsPage<Employee>(profiles, profiles.size(), 1, bean.pageSize);
		else 
		    bean.resultsPage = new SearchResultsPage<Employee>(profiles, 0, 1, bean.pageSize);
	}
}
