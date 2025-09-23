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

package com.ibm.peoplepages.webui.xml.actions;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.lconn.core.web.util.RestServletUtil;

import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;

import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.FollowingService;
import com.ibm.lconn.profiles.internal.service.EmailException;
import com.ibm.lconn.profiles.internal.util.SocialContactsHelper.SocialContactsAction;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.service.PeoplePagesService;

public class SendFriendRequestAction extends RestFriendAction
{
	private final static Class<SendFriendRequestAction> CLAZZ = SendFriendRequestAction.class;

	public SendFriendRequestAction() {
		super(CLAZZ);
	}

 	public void actionPerformed(HttpServletRequest request, HttpServletResponse response)
 			throws DataAccessException, IOException, Exception
 	{
		super.actionPerformed(request, response);
	}

	@Override
	protected SocialContactsAction getActionType()
	{
		return SocialContactsAction.REQUEST;
	}

	@Override
	protected String getActionTypeName()
	{
		return getActionType().getValue();
	}

	@Override
	protected boolean doWork(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
 		String sourceKey    = request.getParameter("targetKey");	// TODO - refactor UI to match
 		String sourceUserId = request.getParameter("targetUserId");	// TODO - refactor UI to match
 		String sourceUserid = request.getParameter("targetUserid");	// TODO - refactor UI to match

 		if (sourceUserId == null || sourceUserId.equals(""))
 			sourceUserId = sourceUserid;

 		// String msg = request.getParameter("msg");
 		String msg = getRequestDataAsUTF8String( request );

 		// get key from userId if key was not sent in SPR#JMGE85RL39
 		if (sourceKey == null) {
 			if(sourceUserId != null) {
 				PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
 				sourceKey = pps.getLookupForPLK(ProfileLookupKey.Type.KEY, ProfileLookupKey.forUserid(sourceUserId), false);
 				LOG.debug(CLASS_NAME + ": Profile key was null.  Used userid ["+sourceUserId+"] to get profile key ["+ sourceKey +"]");
 			}
 			else {
 				LOG.error(CLASS_NAME + ": both key and userid are null for invitation request sent by user with key ["+GetUserInfoAction.getKeyFromLoggedInUser(request)+"]. Cannot send invitation request.");
 			}
 		}

 		Connection conn = new Connection();
 		conn.setSourceKey(sourceKey);
 		conn.setTargetKey(GetUserInfoAction.getKeyFromLoggedInUser(request));

 		if (msg != null && !msg.equals("")) {
 			conn.setMessage(msg);	    
 			// conn.setMessage(XMLUtil.escapeXML(msg));
 		}
//		FollowingService followingSvc = AppServiceContextAccess.getContextObject(FollowingService.class);
		try {
		    // remember the connection ID
			String connectionId = connService.createConnection(conn);
		    // TODO: This is a test code. Remove or modify when done
		    // Test code begins
		    /* Leave the code here when there is a check-box to follow the user
		    LOG.info("SendFriendRequestAction: calling following service...");

		    if ( !followingSvc.isUserFollowedByKey(GetUserInfoAction.getKeyFromLoggedInUser(request), sourceKey) ) {
				followingSvc.followUserByKey(GetUserInfoAction.getKeyFromLoggedInUser(request), sourceKey);
		    }
		    */
		    // Test code ends
		    RestServletUtil.printSuccess(response);
		} 
		catch (ConnectionExistsException e)
		{
			RestServletUtil.printError(response, "connection-exist");
		}
		catch (EmailException e)
		{
		    // RestServletUtil.printError(response, "Failed to send e-mail");
		    LOG.error(CLASS_NAME + "Failed to send e-mail notification. e = " + e );
		    
		    RestServletUtil.printError(response, "notification-error");
		}
		return true;
	}

	public static String getRequestDataAsUTF8String(HttpServletRequest request) throws IOException
    {
    	ByteArrayOutputStream data = new ByteArrayOutputStream();
    	InputStream    inputStream = request.getInputStream();

    	int b = 0;
    	while ((b = inputStream.read()) > -1) {
    		data.write(b);
    	}
    	return (new String(data.toByteArray(), "UTF-8"));
    }

}
