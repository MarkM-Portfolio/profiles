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

dojo.provide("lconn.profiles.Following");
dojo.require("lconn.profiles.ProfilesCore");
dojo.require("lconn.profiles.ProfilesXSL");
dojo.require("lconn.core.globalization.bidiUtil");
dojo.require("lconn.core.formutilities");
dojo.require("lconn.core.util.html");

lconn.profiles.Following.instance = function() {

	this.onLoad = function() {
        var widgetId = "_"+this.iContext.widgetId+"_";
		var attributesItemSet = this.iContext.getiWidgetAttributes();
		this.resourceId = attributesItemSet.getItemValue("resourceId");
		var displayedUserKey = attributesItemSet.getItemValue("profileDisplayedUserKey");
		var mode = this.iContext.getiDescriptor().getItemValue("mode");
		var widgetViewMode = "normal"; 
		if (mode == "fullpage") {
			widgetViewMode = "maximize";
			lconn.profiles.Following.currentViewDomNode = "following_widgetId_fullpage_container";
		}
		else {
			lconn.profiles.Following.currentViewDomNode = "following_widgetId_container";
		}
		
		lconn.profiles.Following.xslName = "view-all-following.xsl";
		lconn.profiles.Following.followResourcesKeyArray = [];
		
		generalrs.label_inactive_user_msg = generalrs["label.inactive.user.msg"]; //make sure we get the inactive user message
		for (rName in generalrs) {
			if (rName && generalrs[rName] && rName.indexOf(".") == -1 && rName.indexOf("/") == -1) {
				lconn.profiles.Following.followResourcesKeyArray.push(rName);
			}
		}		
		
		lconn.profiles.ProfilesCore.showProgressMsg(lconn.profiles.Following.currentViewDomNode, generalrs.follow_Loading);
        lconn.profiles.Following.loadFollow(displayedUserKey, lconn.profiles.Following.currentViewDomNode);
	}	
}

lconn.profiles.Following.loadFollow = function( key, container, currentPage, sortBy, itemsPerPage) {
	if( typeof(currentPage) == "undefined" ) currentPage = 0;
	if( typeof(sortBy) == "undefined" ) sortBy = 0;
	if( typeof(itemsPerPage) == "undefined" ) itemsPerPage = "";
	
	var followedLoaded = "";
	var action =  lconn.profiles.ProfilesCore.getParam("action");
	var callback = null;
	if(action == null || action == "in") {
		if(ui && ui._check_ui_enabled()) {
			callback = function(htmlFragment) {
				var count = 0;
				try {
					var parser = new DOMParser();
					var htmlFragment = parser.parseFromString(htmlFragment, 'text/html');
					count = +htmlFragment.getElementById("followsThirdLevel").getAttribute("data-total-count") || 0;
				} catch (ignore) {}
				lconn.profiles.Following.AddPageSubTitle("follow_title_Following", false, count);
			};
		} else {
	    	lconn.profiles.Following.AddPageSubTitle("follow_title_ContactsFollowedBy", true);
		}
	    followLoaded = lconn.profiles.Following.loadFollowing( key, container, currentPage, sortBy, itemsPerPage, callback);
	}
	else if(action == "out") {
	    lconn.profiles.Following.AddPageSubTitle("follow_title_ContactsFollowing", true);
	    followLoaded = lconn.profiles.Following.loadFollowers( key, container, currentPage, sortBy, itemsPerPage);
	}
	
	if( followLoaded )
    	profiles_AddLiveNameSupport( container );
	else
    	lconn.profiles.ProfilesCore.showInfoMsg( "divNetworkFollowInfo", "error", generalrs.follow_LoadingError );
	
	if(followLoaded) {
		lconn.core.utilities.processUntilElementIsFound(
			"follow_mainContentTable", 
			function(){
				lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId("follow_mainContentTable"));
			},
			null, null, false
		);		
	}
}

lconn.profiles.Following.loadFollowing = function( key, container, currentPage, sortBy, itemsPerPage, callback) {
	var dataUrl = applicationContext + 
					"/atom2/forms/viewfollowedprofiles.xml" + 
					"?key=" + key + 
					"&pageSize=" + itemsPerPage + 
					"&pageNumber=" + currentPage + 
					"&sortBy=" + sortBy + 
					"&ui-level=second" +
					((bShowEmail)?"&showEmail":"") +
					(profilesData.config.appChkSum != null ? "&acs=" + profilesData.config.appChkSum: "");
	
	var xslFileName = lconn.profiles.Following.xslName;
	var resources = lconn.profiles.Following.followResourcesKeyArray;
	var additionalParamsMap = new Array();
	additionalParamsMap.push(["showEmail", ((bShowEmail)?"true":"false")]);
	additionalParamsMap.push(["action","in"]);
	return lconn.profiles.ProfilesXSL.loadContent( dataUrl, xslFileName, container, resources, additionalParamsMap, key, true /*append lastMod*/, callback );
}

lconn.profiles.Following.loadFollowers = function( key, container, currentPage, sortBy, itemsPerPage) {
	var dataUrl = applicationContext + 
					"/atom2/forms/viewallfollowers.xml" + 
					"?key=" + key + 
					"&pageSize=" + itemsPerPage + 
					"&pageNumber=" + currentPage + 
					"&sortBy=" + sortBy + 
					"&ui-level=second" +
					((bShowEmail)?"&showEmail":"") +
					(profilesData.config.appChkSum != null ? "&acs=" + profilesData.config.appChkSum: "");
		
	var xslFileName = lconn.profiles.Following.xslName;
	var resources = lconn.profiles.Following.followResourcesKeyArray;
	var additionalParamsMap = new Array();
	additionalParamsMap.push(["showEmail", ((bShowEmail)?"true":"false")]);
	additionalParamsMap.push(["action","out"]);
	return lconn.profiles.ProfilesXSL.loadContent( dataUrl, xslFileName, container, resources, null, key, true /*append lastMod*/ );
}

lconn.profiles.Following.removeContacts = function(formControl)
{

	if (formControl && formControl.tagName.toLowerCase() !== "form") {
		formControl = lconn.core.formutilities.findParentForm(formControl);
	}
	
	var authedUserKey = lconn.profiles.ProfilesCore.getLoggedInUserKey();
	
	if ( authedUserKey ) {
		
		var values = [];
		dojo.query("input[id^='select_contact_']:checked", formControl).forEach(
			function(node) {
				if (node && node.value && node.value.length > 0) {
					values.push(node.value);
				}
			}
		);
	
		if (values.length == 0) {
			lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "info", generalrs.follow_selectForRemoval, true /* show alert if element does not exist */ );
			return;
		}
		
		var needsReload = false;
		dojo.forEach(
			values, 
			function(val) {
				if (val) {
					needsReload = true;
					var unfollowUrl = applicationContext + "/html/following.do"+"?targetKey=" + val + "&sourceKey=" + authedUserKey +"&action=unfollow";
					lconn.profiles.Following.xhrPost( unfollowUrl, null,
						// the widget framework's data caching mechanism is interfering with any subsequent requests to reload the data through the widget framework
						// thus the 2-n stop follow requests do not reload the updated xml data through the loadFollow() path.  
						// Furthermore, there is no mechanism to cleanly tell the widget to refresh the xml data on the next load, 
						// thus we are better off reloading the page on the last request of a bulk unfollow request set
						function(){ return; }, // do not waste time processing returned data
						true // sync
					);
				}
			}
		);
	
		if ( needsReload ) {
			window.location.reload(); 
		}
	}
}

lconn.profiles.Following.removeContact = function(val)
{
	var authedUserKey = lconn.profiles.ProfilesCore.getLoggedInUserKey();
	
	if ( authedUserKey ) {
		var unfollowUrl = applicationContext + "/html/following.do"+"?targetKey=" + val.id + "&sourceKey=" + authedUserKey +"&action=unfollow";
		lconn.profiles.Following.xhrPost( unfollowUrl, null,
			// the widget framework's data caching mechanism is interfering with any subsequent requests to reload the data through the widget framework 
			// Furthermore, there is no mechanism to cleanly tell the widget to refresh the xml data on the next load, 
			// thus we are better off reloading the page on the last request of a bulk unfollow request set
			function(){ return; }, // do not waste time processing returned data
			true // sync
		);
		
		window.location.reload(); 
	}
}

lconn.profiles.Following.AddPageSubTitle = function(resourceBundleId, action, count)
{
	var temp = generalrs[resourceBundleId];
	if(temp) {
		if(!action){
			if(resourceBundleId === 'follow_title_Following') {
				temp = lconn.profiles.ProfilesCore.replacePlaceHolders(temp, [count || 0]);
			} else {
				temp = lconn.profiles.ProfilesCore.replacePlaceHolders(temp, ["<span class='vcard'><a href='javascript:void(0);' onclick='lconn.core.WidgetPlacement.reloadOverviewPage()' class='fn url'>"+lconn.core.globalization.bidiUtil.enforceTextDirection(lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName))+"</a><span class='x-lconn-userid' style='display: none;'>"+profilesData.displayedUser.userid+"</span></span>"]);
			}
		}else{
			temp = lconn.profiles.ProfilesCore.replacePlaceHolders(temp, ["<span class='vcard'><a href='" + applicationContext + "/html/profileView.do?key="+profilesData.displayedUser.key+"' class='fn url'>"+lconn.core.globalization.bidiUtil.enforceTextDirection(lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName))+"</a><span class='x-lconn-userid' style='display: none;'>"+profilesData.displayedUser.userid+"</span></span>"]);
		}
		
		if (dojo.byId("pageSubTitle")) {
			dojo.byId("pageSubTitle").innerHTML = temp;
			profiles_AddLiveNameSupport("pageSubTitle");
		}	
		setTimeout(function() {
			if (dojo.byId("pageItemsMainRegion")) {
				dojo.attr(dojo.byId("pageItemsMainRegion"), "aria-label", lconn.profiles.ProfilesCore.replacePlaceHolders(generalrs[resourceBundleId], [lconn.core.globalization.bidiUtil.enforceTextDirection(profilesData.displayedUser.displayName)]));
			}
		}, 2000);
	}


}

lconn.profiles.Following.sort = function(formControl, sortByValue)
{
	var formObj = lconn.core.formutilities.findParentForm(formControl);
	var displayedUserKey = formObj.elements["displayedUserKey"].value;
	var itemsPerPage = formObj.elements["items-per-page"].value;
	var currentPage = formObj.elements["current-page"].value;
	var sortBy = null;
	if(sortByValue == null)
		sortBy = lconn.core.formutilities.getValue(formControl);
	else
		sortBy = sortByValue;
	
	lconn.profiles.Following.loadFollow(displayedUserKey, lconn.profiles.Following.currentViewDomNode, currentPage, sortBy, itemsPerPage);
}

lconn.profiles.Following.setItemsPerPage = function(controlObj,itemsPerPage)
{
	var formObj = lconn.core.formutilities.findParentForm(controlObj);
	var displayedUserKey = formObj.elements["displayedUserKey"].value;
	var currentPage = 0; // formObj.elements["current-page"].value; Reset current page to 0 when setting the items per page. SPR#JMGE7JJR65
	var sortBy = formObj.elements["sortBy"].value;
	lconn.profiles.Following.loadFollow(displayedUserKey, lconn.profiles.Following.currentViewDomNode, currentPage, sortBy, itemsPerPage);
}

lconn.profiles.Following.pageTo = function(controlObj, pageNumber)
{
	var formObj = lconn.core.formutilities.findParentForm(controlObj);
	var displayedUserKey = formObj.elements["displayedUserKey"].value;
	var itemsPerPage = formObj.elements["items-per-page"].value;
	var sortBy = formObj.elements["sortBy"].value;
	var maxPages = formObj.elements["total-pages"].value;

	// pageNumber is 0,1,..(N-1); maxPages is 1,2..N
	var pNum = Math.max(0, Math.min(pageNumber, maxPages-1));
	lconn.profiles.Following.loadFollow(displayedUserKey, lconn.profiles.Following.currentViewDomNode, (isNaN(pNum)?0:pNum), sortBy, itemsPerPage);
	
	return false;
}

lconn.profiles.Following.handlePageToEnterKey = function(event, selfObj, pageNum)
{
	var evt = event || window.event;
	if (evt.keyCode == 13) {
		dojo.stopEvent(evt);
		lconn.profiles.Following.pageTo(selfObj, pageNum);
		return false;
	}
	return true;
}

//-----[ BEGIN FOLLOW AJAX CONTORLS ]

lconn.profiles.Following.xhrError = function( data ) {
	var errmsg = "";
	var responseText = "";
	var retcode = false;
	
	if( data ) {
		var returnStatus = data.status;
	    if (returnStatus == 302 || (dojo.isIE && (returnStatus == 0 || returnStatus == 12150))) { // redirect
			retcode = true;
			
		// eat xhr cancelled messages (such as when user clicks away before deferred xhrs are sent) SPR#XBJX7UL4X9
		} else if ( data.dojoType == "cancel" ) { 
			retcode = true; 
			
		} else { 
			if(data.responseText) {
				var rt = data.responseText;
				var mBeg = "<message>";
				var mEnd = "</message>";
			    var msg = rt.slice( rt.indexOf(mBeg)+mBeg.length, rt.indexOf(mEnd)-1);
			    var el = dojo.byId("profileInfoMsgDiv");
			    
			    if( el && msg ) lconn.profiles.ProfilesCore.showInfoMsg(el, "error", msg);
			}
			errmsg += ( data.name ) ? data.name + "\n" : "";
			errmsg += ( data.status ) ? data.status + "\n" : "";
			errmsg += ( data.message ) ? "\n" + data.message + "\n" : "";
			errmsg += ( data.fileName ) ? "\n" + data.fileName + "\n" : "";
			alert( errmsg );
		} 
	}
	
	return retcode;
}

lconn.profiles.Following.xhrGet = function( servlet, callback ) {
	if ( servlet ) {
		var kw = {
	        url: servlet,
	        load: callback,
	        error: function(data){ lconn.profiles.Following.xhrError(data) }, 
	        timeout: 30000,
	        checkAuthHeader: true
		};
		lconn.profiles.xhrGet(kw);
		return true;
	}	
	return false;
}

lconn.profiles.Following.xhrPost = function( postUrl, form, callback, sync ) {
	return lconn.profiles.Following._xhrPostDelete(postUrl, form, callback, true, sync);
}

lconn.profiles.Following.xhrDelete = function( postUrl, form, callback ) {
	return lconn.profiles.Following._xhrPostDelete(postUrl, form, callback, false);
}

lconn.profiles.Following._xhrPostDelete = function( postUrl, form, callback, isPost, isSync ) {
	if( typeof(isSync) == "undefined" || isSync == "") isSync = false;
		if ( postUrl ) {	
		var kw = {
	        url: postUrl,
			load: callback,
			error: function(data){ lconn.profiles.Following.xhrError(data) }, 
			timeout: 30000, 
			checkAuthHeader: true,
			sync: isSync
		};
		
		if (form != null && form != "") {
			var formObj = dojo.formToObject(form);
			if (isPost) {
				kw.headers = { "Content-Type": "text/json" };
				kw.postData = dojo.formToJson(form);
			} else {
				kw.form = form;
			}
		}
		
		if (isPost) {
			lconn.profiles.xhrPost(kw);
		} else {
			lconn.profiles.xhrDelete(kw);
		}
		
		return true;
	}	
	return false;
}

//-----[ END FOLLOW AJAX CONTORLS ]



//-----[ BEGIN:  FOLLOW ACTIONS ]

lconn.profiles.Following.followUser = function() {
	lconn.profiles.Following.followUserByKey( profilesData.displayedUser.key );
}

lconn.profiles.Following.unfollowUser = function() {
	lconn.profiles.Following.unfollowUserByKey( profilesData.displayedUser.key );
}

lconn.profiles.Following.followUserByKey = function( key, callback, obj ) {
	var authedUserKey = lconn.profiles.ProfilesCore.getLoggedInUserKey();
	if( authedUserKey && key ) {
	
		if (typeof obj !== "undefined") {
			dojo.style(obj ,"cursor", "wait");
		}
		var _callback = function(data) {
			if (typeof obj !== "undefined") {
				dojo.style(obj ,"cursor", "default");
			}
			if (typeof(callback) != "function") {
				callback = function(data){
					lconn.profiles.Following.userFollowed(data);
				}; 			
			}
			
			callback(data);
		}
		var followingUrl = applicationContext + "/html/following.do"+"?targetKey=" + key + "&sourceKey=" + authedUserKey +"&action=follow";
		lconn.profiles.Following.xhrPost( followingUrl, null, _callback);
	}
}
	
lconn.profiles.Following.unfollowUserByKey = function( key, callback, obj ) {
	var authedUserKey = lconn.profiles.ProfilesCore.getLoggedInUserKey();
	if( authedUserKey && key ) {
	
		if (typeof obj !== "undefined") {
			dojo.style(obj ,"cursor", "wait");
		}
		var _callback = function(data) {
			if (typeof obj !== "undefined") {
				dojo.style(obj ,"cursor", "default");
			}
			if (typeof(callback) != "function") {
				callback = function(data){
					lconn.profiles.Following.userUnfollowed(data);
				}; 			
			}
			
			callback(data);
		}
		var followingUrl = applicationContext + "/html/following.do"+"?targetKey=" + key + "&sourceKey=" + authedUserKey +"&action=unfollow";
		lconn.profiles.Following.xhrPost( followingUrl, null, _callback);
	}
}

lconn.profiles.Following.userFollowed = function(data) {
    var dataUrl = applicationContext + "/html/unfollow.do?key="+ profilesData.displayedUser.key +"&lastMod=" + profilesData.config.profileLastMod;
    lconn.profiles.Following.xhrGet(dataUrl, 
  		function(data){
    		lconn.profiles.Following.displayFollowedMessage();
	        var followButton = dojo.byId("liProfileActionFollowing");
	        if(followButton) followButton.innerHTML = data;
	        //SPR JMGE86XLPP
	        setTimeout(function () {var button = dojo.byId("inputProfileActionFollowing");if (button) {button.focus();}}, 1);
		}
    );
}
	
lconn.profiles.Following.userUnfollowed = function(data) {
    var dataUrl = applicationContext + "/html/follow.do?key="+ profilesData.displayedUser.key +"&lastMod=" + profilesData.config.profileLastMod;
    lconn.profiles.Following.xhrGet(dataUrl, 
  		function(data){
    		lconn.profiles.Following.displayUnfollowedMessage();
    		var followButton = dojo.byId("liProfileActionFollowing");
	        if(followButton) followButton.innerHTML = data;
	        //SPR JMGE86XLPP
	        setTimeout(function () {var button = dojo.byId("inputProfileActionFollowing");if (button) {button.focus();}}, 1);
		}
    );
}

lconn.profiles.Following.displayFollowedMessage = function() {
	var msg = lconn.profiles.ProfilesCore.replacePlaceHolders(generalrs.label_following_user_followed, [lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName)]);
	lconn.profiles.Following.displayMessage(msg);
}

lconn.profiles.Following.displayUnfollowedMessage = function() {
	var msg = lconn.profiles.ProfilesCore.replacePlaceHolders(generalrs.label_following_user_unfollowed, [lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName)]);
	lconn.profiles.Following.displayMessage(msg);
}

lconn.profiles.Following.displayMessage = function( msg ) {
	var el = dojo.byId("profileInfoMsgDiv");
	if( el ) {
		lconn.profiles.ProfilesCore.showInfoMsg(el, "confirmation", msg);
	} else {
		alert( msg );
	}
}

//-----[ END:  FOLLOW ACTIONS ]
