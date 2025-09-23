/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

// TO DO:
// Cleanup this file.  Most of the functions in this file have been
// replaced by the common widget framework.  Need to cleanup redundancies here.

dojo.require("dijit.Dialog");
dojo.require("lconn.core.utilities");
dojo.require("lconn.core.url");
dojo.require("lconn.core.auth");
dojo.require("lconn.profiles.formBasedUtility");
dojo.require("lconn.profiles.LocalCache");

dojo.require("lconn.core.util.html");
dojo.require("com.ibm.oneui.controls.MessageBox");

dojo.require("lconn.profiles.integrations.store.AppRegStore");
dojo.provide("lconn.profiles.ProfilesCore");

lconn.profiles.ProfilesCore.showAlert = function(arg) {
	var lotusAlertDialog = dijit.byId('lotusAlertDialog');
	if(lotusAlertDialog) {
		dojo.byId("lotusAlertDialogContent").innerHTML = arg;
		dojo.attr(dojo.byId("lotusAlertDialog"), "aria-describedby", "lotusAlertDialogContent"); /*a11y - jaws will not announce if this is not here*/
		
		lotusAlertDialog.show();
		dojo.style(lotusAlertDialog.domNode, "zIndex", 20000);  //make sure dialog is on top of any other dialogs.
	} else {
		alert( arg );
	}
	return;
};

// Override and wrap dojo's dijit.findWidets:
// Dojo1.4.3 findWidgets is finding *ANY* node element of type 1 which has an attribute of widgetId
// This is turn is conflicting because mum widgets also have an attribute of widgetId
// As a result, dojo's widget destroy is failing
// FIXME: Investigate changing mum to not use "widgetId" as an attribute, possibly in CRE
var dojoOriginalFindWidgets = dijit.findWidgets;
dijit.findWidgets = function() { return dojo.filter(dojoOriginalFindWidgets.apply(this, arguments), function(widget) { return !!widget; }); };

var blankImg = dojo.config.blankGif || dijit._Widget.prototype._blankGif;
var versionStamp = ibmConfig.versionStamp;
var proxyURL = ibmConfig.proxyURL;
lconn.profiles.ProfilesCore._proxyHelper = new lconn.core.url.ProxyUrlHelper(proxyURL);

lconn.profiles.ProfilesCore.getProxifiedURL = function(inputURL)
{
	return lconn.profiles.ProfilesCore._proxyHelper.getProxifiedURL(inputURL);
}

lconn.profiles.ProfilesCore.confirm = function confirm(msg, callback) {

	var confirmCallback = function(confirmed) {
		if (confirmed) {
			callback();
		}
	};
	
	if (!msg) {
		confirmCallback(true);

	} else if (dojo.exists("lconn.core.DialogUtil")) {
		lconn.core.DialogUtil.prompt(generalrs["information_confirmation_alt"], msg, generalrs["ok"], generalrs["cancel"], confirmCallback);

	} else {
		confirmCallback(confirm(msg));

	}

};

lconn.profiles.ProfilesCore.replacePlaceHolders = function(inputString, arrayOfValues)
{
	inputString = inputString.replace(/\'\'/g, "'");

	if(arrayOfValues.length > 0)
		inputString = inputString.replace(/\{0\}/, arrayOfValues[0]);
	if(arrayOfValues.length > 1)
		inputString = inputString.replace(/\{1\}/, arrayOfValues[1]);
	if(arrayOfValues.length > 2)
		inputString = inputString.replace(/\{2\}/, arrayOfValues[2]);

	return inputString;
}

lconn.profiles.ProfilesCore.isUserLoggedIn = function()
{
	return profilesData.loggedInUser.isLoggedIn;
}

lconn.profiles.ProfilesCore.getLoggedInUserKey = function()
{
	return profilesData.loggedInUser.loggedInUserKey;
}
lconn.profiles.ProfilesCore.getLoggedInUserUid = function()
{
	return profilesData.loggedInUser.loggedInUserUID;
}

lconn.profiles.ProfilesCore.getXMLDoc = function(userDataUrl)
{
	var xmlDoc = null;
	var callback = function(xmlDocument){xmlDoc = xmlDocument;};
	lconn.profiles.xhrGet({
		checkAuthHeader: true,
		url: userDataUrl,
		error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
		handleAs: "xml",
		load: callback,
		sync: true }
	);
	return xmlDoc;
}


lconn.profiles.ProfilesCore.DefaultXHRErrorHandler = function(response, ioArgs)
{
	var temp = null;
	var msg = null;

	if(response.status == 404)
	{
	 	if (ioArgs.args != null && ioArgs.args.url != null)
	 	{
	 		if (ioArgs.args.url.indexOf("roller-ui/feed") != -1 /* blogs */ ||
	 		 	ioArgs.args.url.indexOf("files/basic/anonymous/api/userlibrary") != -1 /* files SPR#AONO7TJHYY */ ||
	 		 	ioArgs.args.url.indexOf("wikis/basic/anonymous/api/userlibrary") != -1 /* wikis PMR 80291,999,744 */ )
	 		{
					msg = generalrs.multiFeedReaderNoFeeds;
					lconn.profiles.ProfilesCore.displayError(ioArgs, msg, response);
					return;
			}
		}
	}
	if(response.status == 400) /* Handle Bad Request Response from Communities PMR 80291,999,744 */
	{
	 	if (ioArgs.args != null && ioArgs.args.url != null)
	 	{
	 		if (ioArgs.args.url.indexOf("service/atom/communities") != -1) /* communities */
	 		{
					msg = generalrs.multiFeedReaderNoFeeds;
					lconn.profiles.ProfilesCore.displayError(ioArgs, msg, response);
					return;
			}
		}
	}


	if(response.documentElement != null)
	{
		if(response.documentElement.nodeName == "error" || response.documentElement.nodeName == "parsererror")
		{
			var xmlString = (dojox.data.dom.innerXML(response.documentElement));
			msg = lconn.profiles.ProfilesCore._getErrorMessages(null, null, null, null, xmlString);
		}
	}
	else
	{
		var temp2 = generalrs.errorUnableToConnect;
		temp2 = lconn.profiles.ProfilesCore.replacePlaceHolders(temp2, [ioArgs.args.url]);

		if(response.message != null && response.name != null) // is an Error
			return lconn.profiles.ProfilesCore.DefaultErrorHandler(temp2, response, ioArgs.args.xsltArgs);
		else //if(typeof(response) == "string")
			msg = lconn.profiles.ProfilesCore._getErrorMessages(temp2, null, null, null, response);
	}

	if(msg == null)
		msg = lconn.profiles.ProfilesCore._getErrorMessages(null, null, null, null, response);

	lconn.profiles.ProfilesCore.displayError(ioArgs, msg, response);
}

lconn.profiles.ProfilesCore.displayError = function(ioArgs, msg, response)
{
	if (ioArgs && ioArgs.args && ioArgs.args.xsltArgs && ioArgs.args.xsltArgs.htmlContainerElemId)
		lconn.profiles.ProfilesCore.showInfoMsg(ioArgs.args.xsltArgs.htmlContainerElemId, "error", msg);

	else if (ioArgs && ioArgs.args && ioArgs.args.htmlContainerElemId)
		lconn.profiles.ProfilesCore.showInfoMsg(ioArgs.args.htmlContainerElemId, "error", msg);

	if (window.console) {
		console.error(msg);
		console.error(
			(typeof(ioArgs) != "object"? "Arguments: ["+ioArgs+"]"+"\n": "")+
			(response? "Response: ["+response +"]" :"")
		);
	}

}

lconn.profiles.ProfilesCore.DefaultErrorHandler = function(functionName, exception, xsltArgs)
{
	var common_getFunctionName = function(functionObject)
	{
		//firefox
		var name = functionObject.name;

		//my custom function names
		if(functionObject.tempName != null && functionObject.tempName != "")
	    	name = functionObject.tempName;

		//ie and others
		if(name == null || name == "")
		{
		    var functionObject_toString = functionObject.toString();
		    var result = functionObject_toString.match(/function (\w*)/);
		    if(result != null)
			    name = result[1];

		   //if (name == null || name == "")
			    //name = functionObject.constructor;

		    //if (name == null || name == "")
			    //name = functionObject.prototype.name;

		    //if (name == null || name == "")
		    	//name = functionObject.toString();

		    if (name == null || name == "")
		    	name = "anonymous-function";
		}

		return name;
	};


	var common_getStacktrace = function()
	{
		var functionObject = common_getStacktrace.caller;
		var errorObject = new Error();

		if(errorObject.stack != null)
			return errorObject.stack;
		else
		{
			var stackTrace = "";
			var STACK_MAX_DEPTH = 500;
			var i = 0;
			while (functionObject != null && ++i < STACK_MAX_DEPTH)
			{
			  stackTrace += "\n" + common_getFunctionName(functionObject);

			  var tempFunctionObject = functionObject.caller;
			  if(tempFunctionObject == null && functionObject.arguments != null && functionObject.arguments.caller != null)
			  	tempFunctionObject = functionObject.arguments.caller;

			  functionObject = tempFunctionObject;
			}
			return stackTrace;
		}
	};

	var log = function(errorMsg,exception, htmlContainerElemId)
	{
		var msg = "";
		if(exception instanceof(Error))
		{
			var line = null;
			if(exception.lineNumber != null)
				line = exception.lineNumber;

			if(line != null)
				msg += generalrs.errorLine + " " + line + "<br/>";

			var stack = common_getStacktrace();

			msg = lconn.profiles.ProfilesCore._getErrorMessages(errorMsg, exception.message, exception.name, line, stack);
		}
		else
		{
			var stack = common_getStacktrace();
			msg = lconn.profiles.ProfilesCore._getErrorMessages(errorMsg, exception, null, null, stack);
		}

		var ioArgs = {args: {xsltArgs: {htmlContainerElemId: (htmlContainerElemId? htmlContainerElemId : null)}}};
		lconn.profiles.ProfilesCore.displayError(ioArgs, msg);
	};

	log(functionName, exception, (typeof(xsltArgs) == "object" && xsltArgs.htmlContainerElemId? xsltArgs.htmlContainerElemId : null) );
}

lconn.profiles.ProfilesCore._getErrorMessages = function(errorName, errorMsg, errorType, errorLine, errorStackTrace) {
	var msg = "";
	if(errorName != null)
		msg += generalrs.errorName + " " + errorName + "<br/>";
	if(errorMsg != null)
		msg += generalrs.errorMsg + " " + errorMsg + "<br/>";
	if(errorType != null)
		msg += generalrs.errorType + " " + errorType + "<br/>";
	if(errorLine != null)
		msg += generalrs.errorLine + " " + errorLine + "<br/>";
	if(errorStackTrace != null)
		msg += generalrs.errorStackTrace  + "<br/><pre>" + errorStackTrace + "</pre><br/>";	
		
	return [generalrs.errorDefaultMsg2, msg];
}


lconn.profiles.ProfilesCore.getErrorHTML = function(errorName, errorMsg, errorType, errorLine, errorStackTrace)
{
	var time = new Date().getTime();
	var msg = "";
	msg += "<span style='font-size: x-small;vertical-align: top;'>";
	msg += "	<img class='iconsMessages16 iconsMessages16-msgError16' src='" + blankImg + "'/><b>"+generalrs.errorDefaultMsg+"</b><br/>";
	msg += "	"+generalrs.errorDefaultMsg2+"<br/><br/>";
	msg += "	<a href='javascript:void(0);' onclick=\"lconn.profiles.ProfilesCore.toggleVisibility('trace_"+time+"');\">"+generalrs.errorDefaultMsg3+"</a>";
	msg += "	<div id='trace_" + time + "' style='visibility: hidden; display: none;'>";
	msg += "<br/>";
	if(errorName != null)
		msg += generalrs.errorName + " " + errorName + "<br/>";
	if(errorMsg != null)
		msg += generalrs.errorMsg + " " + errorMsg + "<br/>";
	if(errorType != null)
		msg += generalrs.errorType + "" + errorType + "<br/>";
	if(errorLine != null)
		msg += generalrs.errorLine + " " + errorLine + "<br/>";
	if(errorStackTrace != null)
		msg += generalrs.errorStackTrace  + "<br/><pre>" + errorStackTrace + "</pre><br/>";
	msg += "	</div>";
	msg += " </span>";

	return msg;
}

lconn.profiles.ProfilesCore.loadResourceStrings = function(params, resourceBundleKeys)
{
	dojo.deprecated("lconn.profiles.ProfilesCore.loadResourceStrings", "Replace with direct references to dojo message bundles", "3.5");
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

lconn.profiles.ProfilesCore.loadContent = function(dataUrl, XSLViewURL, container, resourceBundleKeys, additionalParamsMap, displayedUserKey, appendLastMod )
{
	if( !dataUrl ) return false;

	// append lastMod to the URL if requested and it is not already in the URL
	if( appendLastMod && dataUrl.indexOf("lastMod=") == -1) {
		dataUrl += ((dataUrl.indexOf("?") == -1) ? "?lastMod=" : "&lastMod=") + profilesData.config.profileLastMod;
	}

	var xsltArgs = {
		xmlDocUrl: dataUrl
	};
	return lconn.profiles.ProfilesCore._loadContent(xsltArgs, XSLViewURL, container, resourceBundleKeys, additionalParamsMap, displayedUserKey);
}

lconn.profiles.ProfilesCore.loadContentObj = function(dataDoc, XSLViewURL, container, resourceBundleKeys, additionalParamsMap, displayedUserKey, appendLastMod )
{
	// append lastMod to the URL if requested and it is not already in the URL
	if( appendLastMod && dataUrl.indexOf("lastMod=") == -1) {
		dataUrl += ((dataUrl.indexOf("?") == -1) ? "?lastMod=" : "&lastMod=") + profilesData.config.profileLastMod;
	}

	var xsltArgs = {
		xmlDoc: dataDoc
	};

	return lconn.profiles.ProfilesCore._loadContent(xsltArgs, XSLViewURL, container, resourceBundleKeys, additionalParamsMap, displayedUserKey);
}

/**
 * Internal method to allow use of either:
 * 	xmlDoc
 *  xmlDocUrl
 * In the xsltArg object
 */
lconn.profiles.ProfilesCore._loadContent = function(xsltArgs, XSLViewURL, container, resourceBundleKeys, additionalParamsMap, displayedUserKey )
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

		params = lconn.profiles.ProfilesCore.loadResourceStrings(params, resourceBundleKeys);
		return params;
	};
	/** end of internal function */

	var params = getGeneralXslParams(resourceBundleKeys, additionalParamsMap, displayedUserKey);

	if(xsltArgs) {
		try {
			var errHandle = function(ioArgs, msg, response) {
				lconn.profiles.ProfilesCore.displayError(ioArgs, "Unable to load XSL: " + XSLViewURL , response);
			};
			/* Complete setup of XSLT args ; exepects */
			xsltArgs.xsltUrl = XSLViewURL;
			xsltArgs.htmlContainerElemId = container;
			xsltArgs.aXslParams = params;
			xsltArgs.dojoErrorHandler = xsltArgs.exceptionHandler = errHandle;
			lconn.core.xslt.transformAndRender(xsltArgs);
			returnCode = true;
		}
		catch(e) {
			var sMsg = e.message;
			throw new Error("lconn.profiles.ProfilesCore._loadContent:\n" + "exception: "+sMsg + "\n" + "for XSL: ["+XSLViewURL+"]");
		}
	}
	else {
		throw new Error("lconn.profiles.ProfilesCore._loadContent: xsltArgs is null for XSL: ["+XSLViewURL+"]");
	}

	return returnCode;
}

function profiles_goBack()
{
	if(history.length > 1)
		history.back();
	else
		profiles_goto(applicationContext);
}

function profiles_setProfilesLastMod(document)
{
	if(document.documentElement.nodeName == "success")
		profilesData.config.profileLastMod = document.documentElement.getAttribute("time");
	//dojo.require("dojo.date.stamp");
	//var newDate = dojo.date.stamp.toISOString(new Date());
	//var temp = new Date();
	//var newDate = temp.getYear() +  "" + temp.getHours() + "" + temp.getMilliseconds();
	//alert("profilesData.config.profileLastMod: '" + profilesData.config.profileLastMod + "' current: '" + newDate + "'");
}

//This code will set/get the last element with the focus, which can be used to set the focus on load.  This
//will help keep the context for a11y.
(function() {
	var LAST_ELEMENT_KEY = "lastElement";
	lconn.profiles.ProfilesCore.setLastElement = function setLastElement(id) {
		if (dojo.exists("lconn.profiles.LocalCache") && typeof id === "string") {
			return lconn.profiles.LocalCache.set(LAST_ELEMENT_KEY, id);
		}
	};
	lconn.profiles.ProfilesCore.getLastElement = function getLastElement() {
		if (dojo.exists("lconn.profiles.LocalCache")) {
			return lconn.profiles.LocalCache.get(LAST_ELEMENT_KEY);
		} else {
			return null;
		}
	};
	lconn.profiles.ProfilesCore.clearLastElement = function getLastElement() {
		if (dojo.exists("lconn.profiles.LocalCache")) {
			return lconn.profiles.LocalCache.unset(LAST_ELEMENT_KEY);
		} else {
			return null;
		}
	};
})();

//FIXME: replace with global navigation functions (not yet available, should be in 3.5)
function profiles_goto(url, ignoreLastMod, currentElemId)
{
	if( url == "" || typeof(url) != "string") return;
	
	//document.cookie = 'ProfilesReqURL=' + document.location.href + '; path=/';
	//document.location.href = applicationContext + '/html/loginView.do?lang=' + appLang;

	if(ignoreLastMod == null)
	{
		if(url.indexOf("?") != -1)
			url += "&acs=" + profilesData.config.appChkSum + "&lastMod=" + profilesData.config.profileLastMod;
		else
			url += "?acs=" + profilesData.config.appChkSum + "&lastMod=" + profilesData.config.profileLastMod;
	}

	if (currentElemId) lconn.profiles.ProfilesCore.setLastElement(currentElemId);
	
	SideBar_RedirectUrl = url;
	setTimeout(function() {
		if(dojo.isIE) {
			try {
				//IE hack to work around bug showing "Unspecified Error" for onbeforeunload
				eval("window.location.href = SideBar_RedirectUrl;");
			} catch (e) {}
			
		} else {
			window.location.assign(SideBar_RedirectUrl);
		}
	}, 100 );
	
}

//FIXME: Remove all references
lconn.profiles.ProfilesCore.loadTime = function()
{
	/**
	Conditional load to save badwidth.
	By comparing minutes and using the clients time for output, we gaurentee that the time on page
	will always agree with the client's clock on each page refresh.  Ideally this should all be done
	in javascript rather than as a DOJO action but I wanted to avoid internationalization issues at
	this stage.

	TODO v2.0 make do all of this work in javascript
	*/
	// Tony E 5/28:  SPR#JMGE85DKJR - NO LONGER NEEDED - Cleanup after beta (some code is calling this function and I cannot find who it is)
	/*
	var pageMin = Math.floor(pageTime/60000);
	var clientTime = new Date().getTime();
	var clientMin = Math.floor(clientTime/60000);
	if (pageMin != clientMin || (Math.abs(pageTime-clientTime) >= 60000))
	{
		var clientTimeNorm = clientMin*60000;
		var dataUrl = applicationContext + "/html/profileLocalTime.do?timezoneId=" + encodeURIComponent(document.getElementById('timezoneId').value) + "&timeNow=" + clientTimeNorm + "&lang=" + appLang;
		lconn.profiles.xhrGet(
		{
	    	url: dataUrl,
			load: function(response, ioArgs)
			{
				document.getElementById('time').innerHTML += "<br/>" + response + "RELATIVE TO CLIENT TIME";
			},
	   		checkAuthHeader: false
		});
	}
	*/
}

lconn.profiles.ProfilesCore.showProgressMsg = function( elId, msg ) {
	var e = dojo.byId(elId);
	if(e) e.innerHTML = '<img class="lotusLoading" alt="'+msg+'" src="'+blankImg+'" />&nbsp;'+msg;
}

lconn.profiles.ProfilesCore.showInfoMsg = function (id, type, msg, showAlertIfNoEl, hideTimerSecs, showCloseLink) {
	if (typeof showCloseLink == "undefined") showCloseLink = true;
	var el = (typeof(id) == "string" ? document.getElementById(id) : ( typeof(id) == "object" ?  id : ""));
	if (el && type && msg) {
	
		//default is confirmation
		var imgAlt = generalrs.information_confirmation_alt;
		var imgCloseAlt = generalrs.information_confirmation_close_alt;
		var msgType = com.ibm.oneui.controls.MessageBox.TYPE.SUCCESS;
		
		switch (type) {
			case "error": // error
				imgAlt = generalrs.information_error_alt;
				imgCloseAlt = generalrs.information_error_close_alt;
				msgType = com.ibm.oneui.controls.MessageBox.TYPE.ERROR;
			break;
			case "warning": // warning
				imgAlt = generalrs.information_warning_alt;
				imgCloseAlt = generalrs.information_warning_close_alt;
				msgType = com.ibm.oneui.controls.MessageBox.TYPE.WARNING;
			break;
			case "info": // informational
				imgAlt = generalrs.information_information_alt;
				imgCloseAlt = generalrs.information_information_close_alt;
				msgType = com.ibm.oneui.controls.MessageBox.TYPE.INFO;
			break;
		}
		
		el.innerHTML = "";
		dojo.removeClass(el, "lotusHidden");
		
		var moreMsg = "";
		if (dojo.isArray(msg) && msg.length > 1) {
			moreMsg = msg[1];
			msg = msg[0];
		}
		
		
		var msgBox = new com.ibm.oneui.controls.MessageBox({
			_strings: {
				icon_alt: imgAlt,
				a11y_label: imgCloseAlt
			},
			type: msgType,
			msg: dojo.create("span", {innerHTML: msg }),
			msgMore: moreMsg,
			canClose: showCloseLink
		}, dojo.create("div", null, el, "last"));
		
		
		if (typeof hideTimerSecs == "number") {
			setTimeout(function() {
				msgBox.close();
			}, hideTimerSecs * 1000);
		}

		return true;

	} else if (showAlertIfNoEl && msg){
		alert(msg);
		return true;
	}

	return false;
	
}

//FIXME: Use generic message widget in 3.5
lconn.profiles.ProfilesCore.hideInfoMsg = function( id ) {
	var el = null;
	if(typeof(id) == "string")
		el = document.getElementById(id);
	else if(typeof(id) == "object")
		el = id;

	if(el) {
		el.className += " lotusHidden";
		return true;
	}

	return false;
}

lconn.profiles.ProfilesCore.toggleVisibility = function(divID, visibilityOnly)
{
	var element = null;
	if((typeof divID) == "string")
		element = document.getElementById(divID);
	else
		element = divID;

	if(element)
	{
		if(element.style.visibility == "hidden")
			lconn.profiles.ProfilesCore.show(element, visibilityOnly);
		else
			lconn.profiles.ProfilesCore.hide(element, visibilityOnly);

	}
	return false;
}

lconn.profiles.ProfilesCore.hide = function(divID, visibilityOnly)
{
	var element = null;
	if((typeof divID) == "string")
		element = document.getElementById(divID);
	else
		element = divID;
	if(element)
	{
		element.style.visibility = "hidden";
		if(!visibilityOnly)
			element.style.display = "none";
	}
	return false;
}

lconn.profiles.ProfilesCore.show = function(divID, visibilityOnly, retryUntilElementFound)
{
	var element = null;
	if((typeof divID) == "string")
		element = document.getElementById(divID);
	else
		element = divID;
	if(retryUntilElementFound)
	{
		var intervalId = "";
		intervalId = window.setInterval(function()
		{
			if(element)
			{
				show(element, visibilityOnly);
				window.clearInterval(intervalId);
			}
		},500);
	}
	else if(element)
	{
		element.style.visibility = "visible";
		if(!visibilityOnly)
			element.style.display = "block";
	}
	return false;
}

/**
 * gets a the query string param from a url.
 */
lconn.profiles.ProfilesCore.getParam = function(paramName) 
{
	dojo.deprecated("lconn.profiles.ProfilesCore.getParam", "Use lconn.core.url to parse URLs", "3.5");
	var obj = {};
	var loc = window.location.href.split("?");	
	if (loc.length > 1) 
		obj = dojo.queryToObject(loc[1].split("#")[0]);
		
	return (obj[paramName] || null);

}

/**
 * Adds a param to the query string and go to the url
 */
lconn.profiles.ProfilesCore.addParam = function(paramName, value)
{
	dojo.deprecated("lconn.profiles.ProfilesCore.addParam", "Use lconn.core.url to parse and rewrite URLs", "3.5");
	var obj = {};
	var loc = window.location.href.split("?");
	if (loc.length > 1) 
		obj = dojo.queryToObject(loc[1].split("#")[0]);

	obj[paramName] = value;
	
	retval = loc[0] + "?" + dojo.objectToQuery(obj);
	
	profiles_goto(retval, true);
}

lconn.profiles.ProfilesCore.handleKeyPress = function(controlObj, event)
{
	var keyCode = null;
	if (event)
		keyCode = event.keyCode;
	else if (window.event)
		keyCode = window.event.keyCode;

	if(keyCode == 13)
	{
		lconn.profiles.ProfilesCore.invoke_onclick(controlObj);
		//controlObj.click(); click function can not be invoked on an image
	}
}

//FIXME: overlap with dojo event handling?
lconn.profiles.ProfilesCore.invoke_onclick = function(controlObj)
{
	var evt = null;

       	if(document.createEvent)
         	evt = document.createEvent('MouseEvents');

       	if(evt && evt.initMouseEvent)
         	evt.initMouseEvent(
           		'click',
           		true,     // Click events bubble
           		true,     // and they can be cancelled
           		document.defaultView,  // Use the default view
           		1,        // Just a single click
           		0,        // Don't bother with co-ordinates
           		0,
           		0,
           		0,
           		false,    // Don't apply any key modifiers
           		false,
           		false,
           		false,
           		0,        // 0 - left, 1 - middle, 2 - right
           		null);    // Click events don't have any targets other than
                     // the recipient of the click
		controlObj.dispatchEvent(evt);
}



lconn.profiles.ProfilesCore.hasLatinChars = function(stringValue) {
	for (var i = 0; i < stringValue.length; i++) {
		if (lconn.core.globalization.bidiUtil.isLatinChar(stringValue.charCodeAt(i))) {
			return true;
		}
	}
	return false;
};

lconn.profiles.ProfilesCore.hasBidiChars = function(stringValue) {
	for (var i = 0; i < stringValue.length; i++) {
		if (lconn.core.globalization.bidiUtil.isBidiChar(stringValue.charCodeAt(i))) {
			return true;
		}
	}
	return false;
};


lconn.profiles.ProfilesCore.getUploadFileName = function(elFile) {
	var newVal = "";
	if( dojo.isIE && elFile.value) { //IE9 and below does not have a files js API on the input file object
		// strip the C:\fakepath\ from value of input file field
		// IE's file input value is prepended with C:\fakepath\, thus we take everything after the last slash
		var fileFakePath = elFile.value;
		var lastSlashAt = fileFakePath.lastIndexOf("\\");
		var fileNameNoPath = fileFakePath.substring(lastSlashAt+1);
		newVal = fileNameNoPath;
	
	} else if( !dojo.isIE && elFile.files.length) {
		newVal = elFile.files[0].name;
	}

	try {		
		//if we are in a RTL language and the filename contains latin characters, then we need to enforce the LTR direction
		if (!dojo.isBodyLtr() && lconn.profiles.ProfilesCore.hasLatinChars(newVal)) {
			newVal = lconn.core.globalization.bidiUtil.enforceTextDirection(newVal, lconn.core.globalization.config.TEXT_DIRECTION.LEFT_TO_RIGHT);
		}
	} catch (e) {
		//if this errors out, set it to the original value.
	}
	
	return newVal;
};

dojo.addOnLoad(function() {
	if (typeof window.formbasedutility == "undefined" || window.formbasedutility == null) window.formbasedutility = new lconn.profiles.formBasedUtility(applicationContext);
});

//looks for an element until its loaded by the browser
//FIXME: replace with dojo topic subscription
var processUntilElementIsFound = function(elementId, callbackFunction, iContext, params, pThrowIfExhausted, pWaitBetweenTries, pMaxTries)
{
	lconn.core.utilities.processUntilElementIsFound(elementId, callbackFunction, iContext, params, pThrowIfExhausted, pWaitBetweenTries, pMaxTries);
}

// FIXME: replace with xhrintercept and different auth.js use 
lconn.profiles.xhr = function(method, args){
	if (dojo.getObject("com.ibm.ajax.auth"))
		com.ibm.ajax.auth.prepareSecure(args);
	return dojo.xhr(method, args);
}
lconn.profiles.xhrGet = function(args){
	return lconn.profiles.xhr("GET", args);
}
lconn.profiles.xhrPost = function(args){
	return lconn.profiles.xhr("POST", args);
}
lconn.profiles.xhrDelete = function(args){
	return lconn.profiles.xhr("DELETE", args);
}
lconn.profiles.xhrPut = function(args){
	return lconn.profiles.xhr("PUT", args);
}

lconn.profiles.xhrError = function( data, ioArgs ) {
	if(typeof(ioArgs) == "undefined") ioArgs = "";
	var errmsg = "";
	var retcode = false;

	if( data ) {
		if ( data.dojoType == "cancel" ) {

		} else {
			errmsg += ( data.name ) ? data.name + "\n" : "";
			errmsg += ( data.status ) ? data.status + "\n" : "";
			errmsg += ( data.message ) ? "\n" + data.message + "\n" : "";
			errmsg += ( data.fileName ) ? "\n" + data.fileName + "\n" : "";
			if (console) console.error( errmsg );
			lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["error.sessionTimeout"]);
		}
	}
}

lconn.profiles.ProfilesCore.getObject = function(nom) {
	if (typeof nom == "string") {
		var arr = nom.split(".");
		var obj = window;
		for (var n = 0; n < arr.length; n++) {
			if (typeof obj[arr[n]] == "undefined") {
				obj[arr[n]] = {};
			}
			obj = obj[arr[n]];
		}
		return obj;
	}
	
	return nom;
};

(function() {
	var _availableUsers = [];
	
	//mixin the sametime support
	lconn.profiles.ProfilesCore = dojo.mixin(lconn.profiles.ProfilesCore, {
		
		getSametimeUserInfo: function(userid) {
			if (userid) return userid;
			try {
				if (this.isSametimeEnabled() && dojo.exists("profilesData.displayedUser")) {
					return (profilesData.displayedUser.email?profilesData.displayedUser.email:profilesData.displayedUser.uid);
				} else 
				if (this.isSametimeProxyEnabled() && dojo.exists("profilesData.displayedUser") && profilesData.displayedUser.dn) {
					return profilesData.displayedUser.dn;
				}
				if (this.isSametimeCloudProxyEnabled() && dojo.exists("profilesData.displayedUser") && profilesData.displayedUser.userid) {
					return profilesData.displayedUser.userid;
				}				
				
			} catch (e) {
				if (window.console) {
					console.error(e);
				}
			}
			return null;
		},
		// check to see which flavor of sametime (if any) is enabled.
		// this is either stlinks or the a local sametime service running on the client.
		isSametimeEnabled: function() {
			return dojo.exists("lconn.profiles.sametime.sametimeAwareness") && lconn.profiles.sametime.sametimeAwareness.isEnabled();
		},
		// this is the stproxy where the st server is running remotely.
		isSametimeProxyEnabled: function() {
			return (dojo.exists("lconn.profiles.sametime.sametimeProxyAwareness") && lconn.profiles.sametime.sametimeProxyAwareness.isEnabled());					
		},
		// this is the LL implementation of stproxy (which did not leverage the lconn.profiles.sametime.sametimeProxyAwareness)
		isSametimeCloudProxyEnabled: function() {
			try {
				return (
					dojo.exists("lotuslive.widgets.awareness") && 
					dojo.exists("gllConnectionsData") && 
					gllConnectionsData.entitlements.imenabled
				);
			} catch (e) {
				return false;
			}
		},
		
		// helper to see if any flavor sametime is enabled
		isAnySametimeServiceEnabled: function() {
			return (this.isSametimeEnabled() || this.isSametimeProxyEnabled() || this.isSametimeCloudProxyEnabled());
		},
		
		// if the service is enabled but not available, then the service is still loading.			
		isSametimeAvailable: function(userid) {
			return (this.isSametimeEnabled() && lconn.profiles.sametime.sametimeAwareness.isAvailable(lconn.profiles.ProfilesCore.getSametimeUserInfo(userid)));
		},
		isSametimeProxyAvailable: function(userid) {
			return (this.isSametimeProxyEnabled() && lconn.profiles.sametime.sametimeProxyAwareness.isAvailable(lconn.profiles.ProfilesCore.getSametimeUserInfo(userid)));
		},
		isSametimeCloudProxyAvailable: function(userid) {
			return (this.isSametimeCloudProxyEnabled() && dojo.exists("stproxy.dock.loggedin") && stproxy.dock.loggedin == "loggedin" && this.sametimeCloudProxyImpl.isAvailable(lconn.profiles.ProfilesCore.getSametimeUserInfo(userid)));
		},
		
		// helper to see if any flavor sametime is available
		isAnySametimeServiceAvailable: function(userid) {
			return (this.isSametimeAvailable(userid) || this.isSametimeProxyAvailable(userid) || this.isSametimeCloudProxyAvailable(userid));
		},
		
		

		//this was mostly copied from the sametimeProxyAwareness.js file.  Cloud has it's own version of sametime proxy.  TODO - Move to a separate file.  Or better yet, have a common connections api for sametime
		sametimeCloudProxyImpl: {
			isAvailable: function(userid) {
				var ret = (typeof stproxy == "object");
				
				if (ret && userid) {
					ret = !!(_availableUsers[userid]);
				}
				
				return ret;		
			},
			
			getHTMLContent: function (livename, includeStatusTxt) {
				var STATUS = stproxy.awareness;
				var ICONS = stproxy.uiControl.iconPaths;				
				
				var currentStatus = livename.model.status;
				var imagePath = "";
				if (currentStatus == STATUS.AVAILABLE)
					imagePath = ICONS.iconAvailable;
				else if (currentStatus == STATUS.AWAY
						|| currentStatus == STATUS.NOT_USING)
					imagePath = ICONS.iconAway;
				else if (currentStatus == STATUS.IN_MEETING)
					imagePath = ICONS.iconInMeeting;
				else if (currentStatus == STATUS.DND)
					imagePath = ICONS.iconDnd;
				else if (currentStatus == STATUS.AVAILABLE_MOBILE)
					imagePath = ICONS.iconAvailableMobile;
				else if (currentStatus == STATUS.AWAY_MOBILE)
					imagePath = ICONS.iconAwayMobile;
				else if (currentStatus == STATUS.IN_MEETING_MOBILE)
					imagePath = ICONS.iconInMeetingMobile;
				else if (currentStatus == STATUS.DND)
				 imagePath = ICONS.iconDndMobile;
				

				var htmlContent = "";
				if (imagePath != "") {
					htmlContent = '<img src="'
							+ imagePath
							+ '" alt=""  aria-label="'
							+ lconn.core.util.html.encodeHtmlAttribute(livename.model.statusMessage)
							+ '">'
							+ '<span class="lotusAltText">'
							+ lconn.core.util.html.encodeHtml(livename.model.statusMessage)
							+ '</span>'
							+ '<span class="lotusAccess" id="STIConDescriptionID">'
							+ stproxy.i18nStrings.openChat
							+ '</span>';
				}
				if (includeStatusTxt) {
					if (!dojo.isIE)
						htmlContent = "&nbsp;" + htmlContent;

					htmlContent += "&nbsp;" + lconn.core.util.html.encodeHtml(livename.model.statusMessage);
				} else
					htmlContent += "&nbsp;";

				return htmlContent;
			},			
			
			createSTStatusMsgLinkAction: function(livename, id, IMContentNode) {
			   if (dojo.config.isDebug && window.console)
				  console.log("createSTStatusMsgLinkAction called");
				
				var className = stproxy.uiControl.status[livename.model.status].iconClass;
				if (livename.model.status == stproxy.awareness.OFFLINE) {
					if (dojo.config.isDebug)
						console.log("createSTStatusMsgLinkAction displayed user not logged in. removing the loading msg");

					var span = document.createElement('span');
					span.innerHTML = "&nbsp;" + window.generalrs.noStatuAvailable;
					dojo.addClass(span, className);
					IMContentNode.innerHTML = "";
					IMContentNode.appendChild(span);
				} else {
					if (dojo.config.isDebug && window.console)
						console.log("createSTStatusMsgLinkAction setting up the status link for" + id);

					var a = document.createElement('a');

					dojo.attr(a, {
						href : 'javascript:;',
						title : livename.model.statusMessage,
						'aria-label' : livename.model.statusMessage,
						'aria-describedby' : "STIConDescriptionID",
						innerHTML : this.getHTMLContent(livename, true)
					});

					var temp = function(evt) {
						if (dojo.config.isDebug && window.console)
							console.log("createSTStatusMsgLinkAction openChat called for"	+ livename.model.id);
						stproxy.openChat(livename.model.id);
						dojo.stopEvent(evt);
						return false;
					};
					dojo.connect(a, "onclick", temp);

					IMContentNode.innerHTML = "";
					IMContentNode.appendChild(a);
					
				}
			},

			createSTIconLinkAction: function(livename, id, IMContentNode) {
				if (livename.model.status == stproxy.awareness.OFFLINE) {
					if (dojo.config.isDebug && window.console)
						console.log("createSTIconLinkAction displayed user not logged in. removing the loading msg");
					IMContentNode.innerHTML = "";
				} else {
					if (dojo.config.isDebug&& window.console)
						console.log("createSTIconLinkAction setting up the status link for " + id);

					var a = document.createElement('a');

					dojo.attr(a, {
						href : 'javascript:;',
						title : livename.model.statusMessage,
						'aria-label' : livename.model.statusMessage,
						style : "text-decoration: none !important; float: left",
						'aria-describedby' : "STIConDescriptionID",
						innerHTML : this.getHTMLContent(livename, false)
					});
					var temp = function(evt) {
					   if (dojo.config.isDebug && window.console)
						  console.log("createSTIconLinkAction openChat called for " + id);
						stproxy.openChat(id);
						dojo.stopEvent(evt);
						return false;
					};

					dojo.connect(a, "onclick", temp);

					IMContentNode.innerHTML = "";
					IMContentNode.appendChild(a);
				}
			},
			
			scanPage: function() {
			  if (dojo.exists("lconn.profiles.integrations.store.AppRegStore")) {
			    var appRegStore = lconn.profiles.integrations.store.AppRegStore.getInstance();
			    if (appRegStore.checkLoaded() && appRegStore.isSametimeChatDisabled === true) {
			      var node = dojo.byId('awarenessArea'); // hide IM status if ST is disabled
			      if (node) {
			        dojo.addClass(node, "lotusHidden");
			      }
			      return;
			    }
			  }
			  
				if (window.stproxy == null || !stproxy.isLoggedIn) {
					if (window.console) console.error("scanPage called but st library not load or user not logged in. waiting 1 sec");
					setTimeout(dojo.hitch(lconn.profiles.ProfilesCore.sametimeCloudProxyImpl, "scanPage"), 1000);
					return;
				}

				var nodes = dojo.query(".IMAwarenessDisplayedUser");
				for ( var i = 0; i < nodes.length; i++) {
					if (dojo.config.isDebug && window.console)
						console.log("scanPage looking for IMAwarenessDisplayedUser");

					var node = nodes[i];
					if (!dojo.hasClass(node, "hasSTStatus")) {
						if (dojo.config.isDebug && window.console)
							console.log("scanPage found an IMAwarenessDisplayedUser without st awareness set");

						if (!stproxy.isLoggedIn) {
							if (dojo.config.isDebug && window.console)
								console.log("scanPage lc/st user not logged in. removing any loading msg");

							var renderType = dojo.query(".renderType", node)[0].innerHTML;
							if (renderType == "Icon") {
								var IMContentNode = dojo.query(".IMContent", node)[0];
								IMContentNode.innerHTML = "";
							}
						} else {
							var id = profilesData.displayedUser.userid;
							try {
								id = dojo.query(".x-lconn-userid", node)[0].innerHTML;
							} catch (e) {}
							
							var renderType = dojo.query(".renderType", node)[0].innerHTML;
							var IMContentNode = dojo.query(".IMContent", node)[0];
							var livename = null;
							
							if (dojo.config.isDebug && window.console) {
								console.log("scanPage userid: " + id);
								console.log("scanPage renderType: " + renderType);
							}
							
							if (renderType == "StatusMsg") {
								livename = new sametime.LiveName({
									"userId" : id
								});
								livename.disableHoverBizCard = true;
							} else if (renderType == "Icon") {
								var userId = dojo.query(".uid", node)[0].innerHTML;
								livename = new sametime.LiveName({
									"userId" : id,
									"uid" : userId
								});
								livename.disableHoverBizCard = true;
							} else if (renderType == "Name") {
								var displayName = dojo.query(".fn", node)[0].innerHTML;
								var userId = dojo.query(".uid", node)[0].innerHTML;
								livename = new sametime.LiveName({
									"userId" : id,
									"displayName" : displayName,
									"uid" : userId
								});
								livename.disableClicks = true;
								livename.disableHoverBizCard = true;
							}
							
							var liveNameUpdate_ = dojo.hitch(this, function() {
								if (!livename.model) return;
								if (dojo.config.isDebug)
									console.log("liveNameUpdate_ setting st awanareness for: " + id + " [" + livename.model.status + "]");
									
								_availableUsers[id] = (livename.model.status != stproxy.awareness.OFFLINE);
								
								if (renderType == "StatusMsg")
									this.createSTStatusMsgLinkAction(livename, id, IMContentNode);
								if (renderType == "Icon")
									this.createSTIconLinkAction(livename, id, IMContentNode);
								else if (renderType == "Name") {
									if (livename.domNode || livename.domNode != "") {
										IMContentNode.innerHTML = "";
										IMContentNode.appendChild(livename.domNode);
									}
								}								

							});

							liveNameUpdate_();
							dojo.connect(livename.model, "onUpdate", liveNameUpdate_);

							dojo.addClass(node, "hasSTStatus");
							scanCompleted = true;
						}
					}
				}
				if (dojo.config.isDebug)
				   console.log("sametimeProxyAddLiveName ended");
			}

		},
			
		setA11yTabIndex: function() {
			// For a11y and JAWS, we need to set the tabindex of these roles so the browser
			// can jump to them
			var _rolesToSetTabIndex = [
				"banner",
				"complementary",
				"contentinfo",
				"form",
				"main",
				"navigation",
				"search",
				"application",
				"node",
				"article"
			];
			dojo.query("*[role]").forEach(function(node) {
				if (!dojo.hasAttr(node, "tabindex") && dojo.indexOf(_rolesToSetTabIndex, dojo.attr(node, "role")) > -1) {
					dojo.attr(node, "tabindex", "-1");
				}
			});	
		},
			
		setLastElementFocus: function() {
		
			// if the last element is set, try to set it's focus
			var lastElement = this.getLastElement();
			if (lastElement) {
				try {
					//first try to look it up as if it were an ID.
					var els = dojo.query("#" + lastElement);
					if (els && els[0]) {
						els[0].focus();
					}
					
					//if we can't find the id, just just doing a query for the element
					els = dojo.query(lastElement);
					if (els && els[0]) {
						els[0].focus();
					}
					
				} catch (e) {
					if (window.console) {
						console.log("Unable to set the focus to element: " + lastElement);
						console.log(e);
					}
				}
				this.clearLastElement(); //we only want to set the focus once.
			}				
		
		}

	});
})();

(function() {
	  var appRegStore = null;
	  if (dojo.exists("lconn.profiles.integrations.store.AppRegStore")) {
	    // acquire a singleton instance of AppRegStore
	    appRegStore = lconn.profiles.integrations.store.AppRegStore.getInstance();
	  }
	  // mixin for AppReg extension chat services support (e.g. Jabber/Spark/Workspace)
	  lconn.profiles.ProfilesCore = dojo.mixin(lconn.profiles.ProfilesCore, {
	    isAnyChatServiceEnabled: function() {
	      if (appRegStore && !appRegStore.checkLoaded()) {
	        // returns true if AppReg loading is not complete yet
	        return true; 
	      }
	      var isSametimeChatDisabled = appRegStore && appRegStore.checkLoaded() && appRegStore.isSametimeChatDisabled === true;
	      return (appRegStore && appRegStore.isChatEnabled()) || (!isSametimeChatDisabled && this.isAnySametimeServiceEnabled());
	    },
	    isAnyChatServiceAvailable: function() {
	      return this.isExtensionChatAvailable() || this.isAnySametimeServiceAvailable();
	    },
	    isExtensionChatAvailable: function() {
	      return appRegStore && appRegStore.checkLoaded() && appRegStore.isChatEnabled();
	    },
		extractProfileAttribute: function(rawUrl,patten1) {
			var p = patten1.exec(rawUrl);
			if (p != null && p.length > 0) {
				return p[0].substring(2, p[0].length - 1).trim();
			}
			return null;
		},
	    openExtensionChat: function() {
			if (this.isExtensionChatAvailable()) {
				var rawUrl = appRegStore.getChatUrl();
			  	var patten1 = /\$\{\s*[A-z]*\s*\}/g;
			  	var profileAttr = this.extractProfileAttribute(rawUrl,patten1);
			  	var resolvedUrl = rawUrl;
				var result;  
			  	if(profileAttr!=null){
				  	if(profilesData.displayedUser[profileAttr]){
					  	resolvedUrl = rawUrl.replace(patten1, profilesData.displayedUser[profileAttr]);
					  	appRegStore.openChat(resolvedUrl);
				  	}
				  	else{
					  	dojo.xhrGet({
						  	url:"/profiles/atom/profileExtension.do?extensionId="+profileAttr+"&key="+profilesData.displayedUser.key,
						  	handleAs: "text",
						 	load: function(data,response){
								if(response.xhr.status===200){
									result = data;
								}
								else if(response.xhr.status===204){
									console.error("No content available for profile extension attribute "+ profileAttr);								
								}
								resolvedUrl = rawUrl.replace(patten1, result);
								appRegStore.openChat(resolvedUrl);
						  	},
						  	error:function(err){
							  	console.error(err.xhr.statusText + " : Error while retreiving profile extension attribute "+ profileAttr);
								resolvedUrl = rawUrl.replace(patten1, result);
								appRegStore.openChat(resolvedUrl);
						  	}
					  	});						
				  	}				
			  	}
				appRegStore.openChat(resolvedUrl);  
		  	}		
	  	},
	    getExtensionChatLabel: function() {
	      if (appRegStore && this.isExtensionChatAvailable()) {
	        return appRegStore.getChatLabel();
	      }
	    },

			isAnyWebmeetingServiceEnabled: function() {
        if (appRegStore && !appRegStore.checkLoaded()) {
          // returns true if AppReg loading is not complete yet
          return true; 
        }
        return (appRegStore && appRegStore.isWebmeetingEnabled());
      },

      isAnyWebmeetingServiceAvailable: function() {
	      return this.isExtensionWebmeetingAvailable();
	    },

      isExtensionWebmeetingAvailable: function() {
	      return appRegStore && appRegStore.checkLoaded() && appRegStore.isWebmeetingEnabled();
	    },

      getExtensionWebmeetingLabel: function() {
	      if (appRegStore && this.isExtensionWebmeetingAvailable()) {
	        return appRegStore.getWebmeetingLabel();
	      }
	    },

      openExtensionWebmeeting: function() {
        if (this.isExtensionWebmeetingAvailable()) {
          var rawUrl = appRegStore.getWebmeetingUrl();
          var patten1 = /\$\{\s*[A-z]*\s*\}/g;
          var profileAttr = this.extractProfileAttribute(rawUrl, patten1);
          if (profileAttr != null) {
            dojo.xhrGet({
              url:
                "/profiles/atom/profileExtension.do?extensionId=" +
                profileAttr +
                "&key=" +
                profilesData.displayedUser.key +
                "&email=" + profilesData.displayedUser.email,
              handleAs: "text",
              load: function (data, response) {
                if (response.xhr.status === 200) {
                  appRegStore.openWebmeeting(data);
                } else if (response.xhr.status === 204) {
                  console.error(
                    "No content available for profile extension attribute " +
                      profileAttr
                  );
                }
              },
              error: function (err) {
                console.error(
                  err.xhr.statusText +
                    " : Error while retreiving profile extension attribute " +
                    profileAttr
                );
              },
            });
          }
        }
	    },
			
	    isExtensionVideoCallAvailable: function() {
	      return appRegStore && appRegStore.checkLoaded() && appRegStore.isVideoCallEnabled();
	    },
	    openExtensionVideoCall: function() {
	      var email = profilesData && profilesData.displayedUser && profilesData.displayedUser.email;
	      if (this.isExtensionVideoCallAvailable() && email) {
	        appRegStore && appRegStore.openVideoCall(email);
	      }
	    },
	    getExtensionVideoCallLabel: function() {
	      if (appRegStore && this.isExtensionVideoCallAvailable()) {
	        return appRegStore.getVideoCallLabel();
	      }
	    },
	    isExtensionPhoneCallAvailable: function() {
	      return appRegStore && appRegStore.checkLoaded() && appRegStore.isPhoneCallEnabled();
	    },
	    waitForAppRegReady: function(callback) {
	      appRegStore.waitForLoaded(callback);
	    },
	    // This method is used to convert all phone numbers within a base node into click-to-call links.
	    // All children nodes will be searched, DOM elements that matches selector 'span[data-phone-type]' will be
	    // considered as phone numbers and will be converted into click-to-call link.
	    //
	    // This expects phone numbers to be rendered within SPAN tags with attribute 'data-phone-type', for example:
	    //   <span data-phone-type="telephone">+1123123123</span>
	    // Phone numbers found will be convert to a clickable link, for example:
	    //   <span data-phone-type="telephone"><a href="sip//+1123123123">+1123123123</a></span>
	    // 
	    // Fax numbers (i.e. data-phone-type='fax') will be ignored and not be converted into links.
	    // If invalid characters are found in phone number, it will not be converted into links.
	    //
	    // @param refNode {DOMNode} - A reference DOM node where this method will use as the base node to search for phone numbers.
	    augmentPhoneCallLinks: function(refNode) {
	      if (!appRegStore || !this.isExtensionPhoneCallAvailable() || !refNode) {
	        return;
	      }
	      var SELECTOR = "span[data-phone-type]";
	      dojo.query(SELECTOR, refNode).forEach(function(span) {
	        var phone = span.textContent.trim();
	        if (dojo.query('> a', span).length > 0 // SPAN already contains a link
	            || span.getAttribute('data-phone-type') === 'fax' // ignore fax numbers
	            || /[^\w\s\+\-]/.test(phone)) { // contains characters other than alphanumeric / space / plus / minus
	          return;
	        }
	        var a = document.createElement("a");
	        a.href = appRegStore.getPhoneCallUrl(phone);
	        a.innerText = phone;
	        span.innerHTML = "";
	        span.appendChild(a);
	      });
	    }
	  });
	})();

lconn.profiles.init = function() {

	if(document.getElementById("searchResultsTable") != null)
		profiles_AddVCard('searchResultsTable');

	if(document.getElementById("rptStructTable") != null)
		profiles_AddVCard('rptStructTable');

	// edit profile unload checking (to make sure data is saved before navigating away)
	var form = document.getElementById("editProfile");
	if( form != null) window.onbeforeunload = onBeforeUnloadHandler;

	// FIXME: Stop using MenuPopup
	//  hide menus on click
	if( typeof(dojo) != "undefined") { // dojo based
		if(  typeof(MenuPopup) != "undefined") dojo.connect( document, "onclick", null, function () { MenuPopup.hideMenu(); } );

	}
	else { // no dojo, just use native event listener
		if(  typeof(MenuPopup) != "undefined") {
			if (document.addEventListener) document.addEventListener("click", function () { MenuPopup.hideMenu(); }, false);
			else if (document.attachEvent) document.attachEvent("onclick", function () { MenuPopup.hideMenu(); });
		}
	}

	//need to add lotusJapanese for special charaster and formatting stylesheet
	if (typeof(appLang) != "undefined") {
		try {
			//appLang will either be just the country or country_locale.
			//We need to make sure we just look at the country.
			var langCheck = (appLang + "_").split("_")[0].toLowerCase();
			if (langCheck === "ja") {
				dojo.addClass(dojo.body(), "lotusJapanese");
			}
		} catch (e) {
			if (window.console) {
				console.error("Unable to retrieve lang check parameter.");
				console.error(e);
			}
		}
	}

	// Register auth check function for profiles
	lconn.core.auth.setAuthCheck(
		dojo.partial(
			function(app) {
				return app.isUserLoggedIn();
			},
			lconn.profiles.ProfilesCore
		)
	);
	
	// A11y
	setTimeout(dojo.hitch(lconn.profiles.ProfilesCore, function() {
		// taken out with 153856.  May be re-introduced with 149676.
		//this.setA11yTabIndex();
		this.setLastElementFocus();	
	}), 100);

	

	
}

//init javascript code
if (typeof(dojo) != "undefined" && lconn.profiles.init) {
	dojo.addOnLoad(function() {
		if (dojo.isIE) { //IE 9+ needs a little more time to render the UI before showing/hiding these areas
			setTimeout(lconn.profiles.init, 0);
		} else {
			lconn.profiles.init();
		}
	});
}
