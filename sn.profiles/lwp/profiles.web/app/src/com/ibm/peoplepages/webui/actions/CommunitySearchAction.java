// DELETE_COMMUNITYSEARCH
///* ***************************************************************** */
///*                                                                   */
///* IBM Confidential                                                  */
///*                                                                   */
///* OCO Source Materials                                              */
///*                                                                   */
///* Copyright IBM Corp. 2009, 2013                                    */
///*                                                                   */
///* The source code for this program is not published or otherwise    */
///* divested of its trade secrets, irrespective of what has been      */
///* deposited with the U.S. Copyright Office.                         */
///*                                                                   */
///* ***************************************************************** */
//
//package com.ibm.peoplepages.webui.actions;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.struts.action.ActionForm;
//import org.apache.struts.action.ActionForward;
//import org.apache.struts.action.ActionMapping;
//import org.apache.struts.action.DynaActionForm;
//
//import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
//import com.ibm.lconn.profiles.web.actions.BaseAction;
//import com.ibm.peoplepages.data.SearchResultsPage;
//import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
//import com.ibm.peoplepages.service.PeoplePagesService;
//
//public class CommunitySearchAction extends BaseAction {
//
//	public ActionForward doExecute(ActionMapping mapping, ActionForm form, 
//		HttpServletRequest request,HttpServletResponse response) throws Exception {
//		
//		PeoplePagesService service = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
//
//		DynaActionForm simpleSearchForm = (DynaActionForm) form;
//		String searchBy = "";
//		String searchFor = "";
//		Map searchMap = simpleSearchForm.getMap();
//		Iterator iterator = searchMap.keySet().iterator();
//		while (iterator.hasNext()) {
//			String searchFormKey = (String) iterator.next();
//			String searchFormValue = (String) searchMap.get(searchFormKey);
//			if (!searchFormValue.equals("")) {
//				searchBy = searchFormKey;
//				searchFor = searchFormValue;
//			}
//		}
//		
//		String searchType = request.getParameter("searchType");
//		if (searchType != null && !searchType.equals("")) {
//			request.setAttribute("searchType", searchType);
//		}
//		else if (searchBy.equals(PeoplePagesServiceConstants.KEYWORD)) {
//			request.setAttribute("searchType", "keywordSearch");
//		}
//		else {
//			request.setAttribute("searchType", "simpleSearch");
//		}
//    
//		String pageStr = request.getParameter("page");
//		
//		if (searchBy.equals(PeoplePagesServiceConstants.KEYWORD) || searchBy.equals(PeoplePagesServiceConstants.COMMUNITY)) {
//			SearchResultsPage communityResults = null;
//			if (searchFor != null && searchFor.length() > 0) {
//				Map map = new HashMap();
//				searchFor = searchFor.toLowerCase();
//				searchFor = searchFor.replace('*', '%');
//				map.put(searchBy, searchFor);
//				int pageNumber = 1;
//				if (pageStr != null && !pageStr.equals("")) {
//					pageNumber = new Integer(pageStr).intValue();
//				}
//				communityResults = null; //service.searchForCommunities(map, pageNumber);
//				if (communityResults != null) {
//					request.setAttribute("searchResultsPage", communityResults);
//				}
//			}
//			else {
//				request.setAttribute("searchResultsPage", new SearchResultsPage());
//			}
//		}
//		else if ((searchFor = request.getParameter("profileTags")) != null && searchFor.length() > 0) {
//			Map<String,String> m = new HashMap<String,String>();
//			m.put(PeoplePagesServiceConstants.KEYWORD, searchFor);
//			int pageNumber = 1;
//			if (pageStr != null && !pageStr.equals("")) {
//				pageNumber = new Integer(pageStr).intValue();
//			}
//			SearchResultsPage communityResults = null;//service.searchForCommunities(m, pageNumber);
//			if (communityResults != null) {
//				request.setAttribute("searchResultsPage", communityResults);
//			}
//		}
//		else {
//			request.setAttribute("searchResultsPage", new SearchResultsPage());
//		}
//		return mapping.findForward("communitySearchResults");
//	}
//
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
//	 */
//	@Override
//	protected long getLastModified(HttpServletRequest request) throws Exception {
//		return UNDEF_LASTMOD;
//	}
//}
