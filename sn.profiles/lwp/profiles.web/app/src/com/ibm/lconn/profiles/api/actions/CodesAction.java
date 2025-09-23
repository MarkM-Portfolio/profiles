/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.CountryService;
import com.ibm.lconn.profiles.internal.service.DepartmentService;
import com.ibm.lconn.profiles.internal.service.EmployeeTypeService;
import com.ibm.lconn.profiles.internal.service.OrganizationService;
import com.ibm.lconn.profiles.internal.service.WorkLocationService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class CodesAction extends APIAction
{
	private final CountryService countrySvc = AppServiceContextAccess.getContextObject(CountryService.class);
	private final DepartmentService deptSvc = AppServiceContextAccess.getContextObject(DepartmentService.class);
	private final EmployeeTypeService empTypeSvc = AppServiceContextAccess.getContextObject(EmployeeTypeService.class);
	private final OrganizationService orgSvc = AppServiceContextAccess.getContextObject(OrganizationService.class);
	private final WorkLocationService workLocSvc = AppServiceContextAccess.getContextObject(WorkLocationService.class);
	
	private static final class Bean
	{
		SearchResultsPage<?> resultsPage;
		String searchType;
		String outputType = PeoplePagesServiceConstants.HCARD;
		boolean isLite = true;
		public Bean() {}
	}
	
	protected final ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		Bean bean = getAndStoreActionBean(request, Bean.class);

		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);
		
		AtomGenerator2 atomGenerator = new AtomGenerator2(request, response.getWriter(), bean.isLite, bean.outputType);
		atomGenerator.transform(bean.resultsPage, bean.searchType);
		
		return null;	
	}
	
	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		return new Date().getTime();
	}
	
	protected Object instantiateActionBean(HttpServletRequest request)
		throws Exception
	{
		Bean bean = new Bean();
		
		//
		// Setup output format
		//
		String outputType = request.getParameter(PeoplePagesServiceConstants.OUTPUT);
		if (PeoplePagesServiceConstants.VCARD.equals(outputType) || PeoplePagesServiceConstants.HCARD.equals(outputType))
		{
			bean.outputType = outputType;
		}
		
		//
		// Resolve codes
		//
		String paramValue = null;
		if (AssertionUtils.nonEmptyString(paramValue = request.getParameter(PeoplePagesServiceConstants.CCODE)))
		{
			bean.searchType = PeoplePagesServiceConstants.CCODE;
			bean.resultsPage = createSRP(countrySvc.getById(paramValue));
		}
		else if (AssertionUtils.nonEmptyString(paramValue = request.getParameter(PeoplePagesServiceConstants.DCODE)))
		{
			bean.searchType = PeoplePagesServiceConstants.DCODE;
			bean.resultsPage = createSRP(deptSvc.getById(paramValue));
		}
		else if (AssertionUtils.nonEmptyString(paramValue = request.getParameter(PeoplePagesServiceConstants.ECODE)))
		{
			bean.searchType = PeoplePagesServiceConstants.ECODE;
			bean.resultsPage = createSRP(empTypeSvc.getById(paramValue));
		}
		else if (AssertionUtils.nonEmptyString(paramValue = request.getParameter(PeoplePagesServiceConstants.OCODE)))
		{
			bean.searchType = PeoplePagesServiceConstants.OCODE;
			bean.resultsPage = createSRP(orgSvc.getById(paramValue));
		}
		else if (AssertionUtils.nonEmptyString(paramValue = request.getParameter(PeoplePagesServiceConstants.WORK_LOC_CODE)))
		{
			bean.searchType = PeoplePagesServiceConstants.WORK_LOC_CODE;
			bean.resultsPage = createSRP(workLocSvc.getById(paramValue));
		}
		else
		{
			AssertionUtils.assertTrue(false);
		}
		
		return bean;
	}

	private <T> SearchResultsPage<T> createSRP(T result)
	{
		if (result == null) 
		{
			List<T> emptyList = Collections.emptyList();
			return new SearchResultsPage<T>(emptyList, 0, 0, 0); 
		}
		
		return new SearchResultsPage<T>(Collections.singletonList(result), 1, 1, 1);
	}

}
