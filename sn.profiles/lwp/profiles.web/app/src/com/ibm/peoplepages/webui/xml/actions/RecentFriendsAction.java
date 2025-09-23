/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamException;

import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.RetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class RecentFriendsAction implements RestAction
{
	private static final String LAST_MODIFIED = "Last-Modified";
	private static final String HEAD_CACHE_CONTROL = "Cache-Control";
	private static final String HEAD_EXPIRES = "Expires";

	public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws DataAccessException, IOException, XMLStreamException
	{		
		String uid = request.getParameter("uid");
		String userKey = request.getParameter("key");

		if(userKey == null)
		{
			PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
			userKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, ProfileLookupKey.forUid(uid), false);
		}
		int pageSize   = 12;
		int pageNumber = 0;
		int orderBy    = RetrievalOptions.OrderByType.MOST_RECENT;

		String pageSizeStr = request.getParameter("pageSize");
		if ( pageSizeStr != null && pageSizeStr.length() > 0 ) {
			try {
				pageSize = Integer.parseInt( pageSizeStr );
			}
			catch (NumberFormatException ex ) {
				// Not to log anything for now
			}
		}
		// write out recent friends
//		XMLStreamWriter writer = RestServletUtil.getXMLWriter2(response, true);
//		String serviceLink = ServiceReferenceUtil.getServiceLink("profiles", request.isSecure());

//		ViewAllFriendsAction.performFriends(userKey, pageSize, pageNumber, writer, null, false, orderBy, serviceLink);
		ViewAllFriendsAction.performFriendsWrite(request, response, userKey, pageSize, pageNumber, null, null, false, orderBy);
	}
}
