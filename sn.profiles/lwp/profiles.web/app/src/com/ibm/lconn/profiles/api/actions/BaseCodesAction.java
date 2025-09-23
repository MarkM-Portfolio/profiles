/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2015                                    */
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

import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.codes.AbstractCode;

import com.ibm.lconn.profiles.internal.service.BaseCodesService;

public abstract class BaseCodesAction extends APIAction
{
	protected BaseCodesService<?> service = null;

	// /admin/atom/codes/*   com.ibm.lconn.profiles.api.actions.{1}Action  is blocked on Cloud environment
	private static boolean _isOnCloud;
	static {
		_isOnCloud = (LCConfig.instance().isLotusLive() || LCConfig.instance().isMTEnvironment()); 
	}

	protected static final class Bean
	{
		SearchResultsPage<?> resultsPage = null;
		String searchType = null;
		String outputType = PeoplePagesServiceConstants.MIME_TEXT_XML;
		boolean isEntryOnly = false;
		public Bean() {}
	}

	protected Object instantiateActionBean(HttpServletRequest request) throws Exception
	{
		Bean   bean   = new Bean();
		String codeId = getCodeId(request);

		//
		// Setup output format
		//
		bean.outputType = PeoplePagesServiceConstants.MIME_TEXT_XML;
		bean.searchType = getSearchType();

		service = getService();
		if (codeId == null) {
			bean.resultsPage = createSRP(service.getAll());
			bean.isEntryOnly = false;
		} else {
			bean.resultsPage = createSRP(service.getById(codeId));
			bean.isEntryOnly = true;
		}
		return bean;
	}

	protected long getLastModified(HttpServletRequest request) throws Exception 
	{
		return new Date().getTime();
	}

	protected String getCodeId(HttpServletRequest request)
	{
		return request.getParameter("codeId");
	}

	protected <T> SearchResultsPage<T> createSRP(T result)
	{
		SearchResultsPage<T> retVal = null;
		if (result == null) 
		{
			List<T> emptyList = Collections.emptyList();
			retVal = new SearchResultsPage<T>(emptyList, 0, 0, 0); 
		}
		else {
			retVal = new SearchResultsPage<T>(Collections.singletonList(result), 1, 1, 1);
		}
		return retVal;
	}

	protected final ActionForward doExecuteGET(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{
		if (_isOnCloud) {
			throw new APIException(ECause.INVALID_OPERATION);
		}

		Bean bean = getAndStoreActionBean(request, Bean.class);

		response.setCharacterEncoding(AtomConstants.XML_ENCODING);
		response.setContentType(AtomConstants.ATOM_CONTENT_TYPE);

		AtomGenerator2 atomGenerator = new AtomGenerator2(request, response.getWriter(), false, bean.outputType);
		atomGenerator.transform(bean.resultsPage, bean.searchType, bean.isEntryOnly);

		return null;	
	}

	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception 
	{	  
		if (_isOnCloud) {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		String codeId = getCodeId(request);

		getService().delete(codeId);
		
		return null;
	}

	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		if (_isOnCloud) {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		AtomParser3 atomParser = new AtomParser3();

		List<?> aclist = atomParser.parseCodes(request.getInputStream());
		List <AbstractCode<?>> codes = (List <AbstractCode<?>>) aclist;
		for (AbstractCode<?> code : codes)
		{
			doCreate(code);
		}

		return null;
	}

	protected ActionForward doExecutePUT(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		if (_isOnCloud) {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		AtomParser3 atomParser = new AtomParser3();
		String   codeId = getCodeId(request);
		AbstractCode<?> code = null;

		if (codeId == null) {
			List<?> aclist = atomParser.parseCodes(request.getInputStream());
			code = (AbstractCode<?>) aclist.get(0);			
			doCreate(code);
		} else {
			List<?> aclist = atomParser.parseCodes(request.getInputStream());
			code = (AbstractCode<?>) aclist.get(0);			
			doUpdate(code);
		}

		return null;
	}

	protected abstract BaseCodesService<?> getService();
	protected abstract String              getSearchType();
	protected abstract void                doCreate(AbstractCode<?> code);
	protected abstract void                doUpdate(AbstractCode<?> code);
	
}
