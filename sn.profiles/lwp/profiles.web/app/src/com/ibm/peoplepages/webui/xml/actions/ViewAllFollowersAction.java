/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.web.util.CachingHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.webui.servlet.RestServlet.RestAction;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class ViewAllFollowersAction implements RestAction
{
    public void actionPerformed(HttpServletRequest request, HttpServletResponse response) throws IOException, DataAccessException, XMLStreamException
    {    
    	CachingHelper.disableCaching(response);
    	
	String userKey = request.getParameter("key");
	String uid = request.getParameter("uid");
	
	int pageSize = 10;
	String pageSizeS = request.getParameter("pageSize");
	if(pageSizeS != null && !pageSizeS.equals(""))
	    pageSize = Integer.parseInt(pageSizeS);
	
	String pageNumberS = request.getParameter("pageNumber");
	int pageNumber = 0;
	if(pageNumberS != null && !pageNumberS.equals(""))
	    pageNumber = Integer.parseInt(pageNumberS);
	
	if(userKey == null) {
	    PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	    userKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, ProfileLookupKey.forUid(uid), false);
	}
	
	FollowingService fs = AppServiceContextAccess.getContextObject(FollowingService.class);
	List<Employee> users = fs.getProfileFollowers(ProfileLookupKey.forKey(userKey), pageSize, pageNumber);
	int totalCount = fs.getProfileFollowersCount(ProfileLookupKey.forKey(userKey));
	
	WriteXMLDocUtil.writeXMLDoc(request, response, users, "followers", totalCount);
    }
}
