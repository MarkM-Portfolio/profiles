/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/* author: Ronny A. Pena                                             */

//dojo.//provide("lconn.core.api"); // FIXME: Provisionally remove, don't contribute global namespace
dojo.provide("lconn.profiles.api");

//API Facade
/* FIXME: This should not contribute to the global namespace, no references found externally
lconn.core.api.DojoXhrErrorHandler = function(dojoXhrResponseObj, ioArgs)
{
	lconn.profiles.ProfilesCore.DefaultXHRErrorHandler(dojoXhrResponseObj, ioArgs)
}

lconn.core.api.ErrorHandler = function(functionName, exception, htmlContainerElemId)
{
	var xsltArgs = {htmlContainerElemId: htmlContainerElemId};
	lconn.profiles.ProfilesCore.DefaultErrorHandler(functionName, exception, xsltArgs);
}

lconn.core.api.getProxifiedURL = function(inputURL)
{
 return lconn.profiles.ProfilesCore.getProxifiedURL(inputURL);
}
*/

// TODO: Determine appropriate use of these methods.  Candidate for removal? 

lconn.profiles.api.isUserLoggedIn = function()
{
	return lconn.profiles.ProfilesCore.isUserLoggedIn();
}

lconn.profiles.api.getLoggedInUserUid = function()
{
	return lconn.profiles.ProfilesCore.getLoggedInUserUid();
}

lconn.profiles.api.getDisplayedUserInfo = function()
{
	profilesData.displayedUser.profileLastModDate = profilesData.config.profileLastMod;
	return profilesData.user;
}

lconn.profiles.api.getCurrentPageId = function()
{
	return profilesData.config.pageId;
}

lconn.profiles.api.loadWidgetFullPage = function(widgetId, additionalParameters)
{
	alert("* replace with widget framework *");
	lconn.core.WidgetPlacement.loadFullpageView(widgetId);
}

lconn.profiles.api.semanticTag = function()
{
	this.parse = function(rootNodeId)
	{	
		profiles_AddLiveNameSupport(rootNodeId);	
	};
}
