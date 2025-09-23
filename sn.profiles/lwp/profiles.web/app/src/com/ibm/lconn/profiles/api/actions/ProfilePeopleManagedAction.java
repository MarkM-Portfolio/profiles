/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfilePeopleManagedAction extends ProfileAPIAction
{
	private OrgStructureService orgStructSvc;

	private static final class Bean extends BaseBean
	{
		public ProfileLookupKey plk;
		
		public Bean() { }
	}
	
	public ProfilePeopleManagedAction() {
		this.orgStructSvc = AppServiceContextAccess.getContextObject(OrgStructureService.class);
	}
	
	protected BaseBean instantiateActionBean_delegate(HttpServletRequest request) 
			throws Exception 
	{
		Bean bean = new Bean();
		bean.searchType = PeoplePagesServiceConstants.PEOPLE_MANAGED;
		bean.pageNumber = resolvePageNumber(request);
		bean.pageSize = resolvePageSize(request, bean.pageSize);
		bean.allowOverrideIsLite = true; // infocenter says full is supported
		bean.lastMod = new Date().getTime();
		
		bean.plk = getProfileLookupKey(request);
		AssertionUtils.assertNotNull(bean.plk);

		// reject invalid params that are not handled predictably by orgStructSvc.getPeopleManaged() and
		// AtomGenerator2.writePagingInfo()
		AssertionUtils.assertTrue(bean.pageSize >= 1, AssertionType.BAD_REQUEST, PeoplePagesServiceConstants.PAGE_SIZE + "<1");
		AssertionUtils.assertTrue(bean.pageNumber >= 1, AssertionType.BAD_REQUEST, PeoplePagesServiceConstants.PAGE + "<1");
		
		ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
		setOptions.setProfileOptions(ProfileRetrievalOptions.LITE);
		setOptions.setIncludeCount(true);
		setOptions.setPageNumber(bean.pageNumber);
		setOptions.setPageSize(bean.pageSize);

		EmployeeCollection ecoll = orgStructSvc.getPeopleManaged(bean.plk,setOptions);

		bean.resultsPage = new SearchResultsPage<Employee>(ecoll.getResults(), ecoll.getTotalCount(), bean.pageNumber, bean.pageSize);

		return bean;
	}
}
