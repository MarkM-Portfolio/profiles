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

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfileFullReportToChainAction extends ProfileAPIAction
{
	private OrgStructureService orgStructSvc;
	
	public ProfileFullReportToChainAction() {
		this.orgStructSvc = AppServiceContextAccess.getContextObject(OrgStructureService.class);
	}
	
	private static final class Bean extends BaseBean
	{
		public Bean() {}
		public ProfileLookupKey plk;
	}
	
	protected BaseBean instantiateActionBean_delegate(HttpServletRequest request) 
			throws Exception 
	{
		Bean bean = new Bean();
		bean.searchType = PeoplePagesServiceConstants.REPORTING_CHAIN;
		bean.pageSize = resolvePageSize(request, -1);
		bean.allowOverrideIsLite = false;
		bean.lastMod = new Date().getTime();
		
		bean.plk = getProfileLookupKey(request);
		AssertionUtils.assertNotNull(bean.plk);
		
		List<Employee> reports = orgStructSvc.getReportToChain(bean.plk, ProfileRetrievalOptions.LITE, true, bean.pageSize);
		bean.resultsPage = new SearchResultsPage<Employee>(reports, reports.size(), 1, reports.size());
		
		return bean;
	}
}
