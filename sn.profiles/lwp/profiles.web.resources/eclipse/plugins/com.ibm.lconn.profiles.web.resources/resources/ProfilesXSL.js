/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
dojo.provide("lconn.profiles.ProfilesXSL");
dojo.require("lconn.core.util._XSLCache");
dojo.require("lconn.profiles.ProfilesCore");

lconn.profiles.ProfilesXSL.cachedXSLs = new (dojo.declare("", [lconn.core.util._XSLCache], {
	xslStrings: {
		"report-chain.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/reportChain/report-chain.xsl")},
		"tags.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/tags/tags.xsl")},
		"view-all-following.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/follow/view-all-following.xsl")},
		"view-all-friends.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/friends/view-all-friends.xsl")},
		"accept-invitations.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/friends/accept-invitations.xsl")},
		"send-request.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/friends/send-request.xsl")},
		"recent-friends.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/friends/recent-friends.xsl")},
		"feedreader.xsl": {templatePath : dojo.moduleUrl("lconn.profiles","xslt/multifeedreader/feedreader.xsl")}
	} 		
}));
lconn.profiles.ProfilesXSL.getCachedXSL = function(xslName)
{
	return lconn.profiles.ProfilesXSL.cachedXSLs.getXslDoc(xslName);
}
lconn.profiles.ProfilesXSL.loadResourceStrings = function(params, resourceBundleKeys)
{
	dojo.deprecated("lconn.profiles.ProfilesXSL.loadResourceStrings", "Replace with direct references to dojo message bundles", "3.5");
	if(params == null)
		params = new Array;

	for (var i = 0; resourceBundleKeys != null && i < resourceBundleKeys.length; i++) {
		var rawStr = generalrs[resourceBundleKeys[i]];
		if(rawStr) {
			var noDups = rawStr.replace(/\'\'/g, "'");
			if(noDups == null || noDups == "")
				params.push([resourceBundleKeys[i], resourceBundleKeys[i] + " resourceKey not found"]);
			else
				params.push([resourceBundleKeys[i], noDups]);
		}
	}

	return params;
}

lconn.profiles.ProfilesXSL.loadContent = function(dataUrl,xslName, container, resourceBundleKeys, additionalParamsMap, displayedUserKey, appendLastMod, callback )
{
	if( !dataUrl ) return false;

	// append lastMod to the URL if requested and it is not already in the URL
	if( appendLastMod && dataUrl.indexOf("lastMod=") == -1) {
		dataUrl += ((dataUrl.indexOf("?") == -1) ? "?lastMod=" : "&lastMod=") + profilesData.config.profileLastMod;
	}

	var xsltArgs = {
		xmlDocUrl: dataUrl, 
		callback: callback
	};
	return lconn.profiles.ProfilesXSL._loadContent(xsltArgs,xslName, container, resourceBundleKeys, additionalParamsMap, displayedUserKey);
}

lconn.profiles.ProfilesXSL.loadContentObj = function(dataDoc, xslName, container, resourceBundleKeys, additionalParamsMap, displayedUserKey, appendLastMod )
{

	var xsltArgs = {
		xmlDoc: dataDoc
	};

	return lconn.profiles.ProfilesXSL._loadContent(xsltArgs, xslName, container, resourceBundleKeys, additionalParamsMap, displayedUserKey);
}

lconn.profiles.ProfilesXSL._loadContent = function(xsltArgs, xslName, container, resourceBundleKeys, additionalParamsMap, displayedUserKey )
{
	var returnCode = false;
	/** internal function */
	var getGeneralXslParams = function(resourceBundleKeys, params, displayedUserKey)
	{
		if(params == null) params = new Array;
		params.push(["etag",versionStamp]);
		params.push(["blankImg",blankImg]);
		params.push(["appLang",(appLang?appLang:"")]);
		
		if(lconn.profiles.ProfilesCore.isUserLoggedIn())
		{
			params.push(["loggedIn", "true"]);
			params.push(["loggedInUserUid", lconn.profiles.ProfilesCore.getLoggedInUserUid()]);
			params.push(["loggedInUserKey", lconn.profiles.ProfilesCore.getLoggedInUserKey()]);
		}
		else
			params.push(["loggedIn", "false"]);
		if(displayedUserKey != null) params.push(["displayedUserKey", displayedUserKey]);
		params.push(["applicationContext", applicationContext]);
		params.push(["profImageDir", applicationContext + "/static/images/" + profilesData.config.appChkSum]);
		params.push(["isNewUI", ui && ui._check_ui_enabled()]);
		params = lconn.profiles.ProfilesCore.loadResourceStrings(params, resourceBundleKeys);
		return params;
	};
	/** end of internal function */

	var params = getGeneralXslParams(resourceBundleKeys, additionalParamsMap, displayedUserKey);

	if(xsltArgs) {
		try {
			/* Complete setup of XSLT args ; exepects */
			xsltArgs.htmlContainerElemId = container;
			xsltArgs.aXslParams = params;
			xsltArgs.dojoErrorHandler = lconn.profiles.ProfilesCore.DefaultXHRErrorHandler;
			xsltArgs.exceptionHandler = lconn.profiles.ProfilesCore.DefaultErrorHandler;
			if(lconn.profiles.ProfilesXSL.cachedXSLs.getXslDoc(xslName)){
				xsltArgs.xslDoc = lconn.profiles.ProfilesXSL.cachedXSLs.getXslDoc(xslName); 
			}
			lconn.core.xslt.transformAndRender(xsltArgs);
			returnCode = true;
		}
		catch(e) {
			var sMsg = e.message;
			throw new Error("lconn.profiles.ProfilesXSL._loadContent:\n" + "exception: "+sMsg + "\n" + "for XSL: ["+XSLViewURL+"]");
		}
	}
	else {
		throw new Error("lconn.profiles.ProfilesXSL._loadContent: xsltArgs is null for XSL: ["+XSLViewURL+"]");
	}

	return returnCode;
}

