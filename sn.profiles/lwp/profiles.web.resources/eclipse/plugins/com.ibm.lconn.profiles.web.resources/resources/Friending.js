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

dojo.provide("lconn.profiles.Friending");

dojo.require("lconn.profiles.ProfilesCore");
dojo.require("lconn.profiles.ProfilesXSL");
dojo.require("lconn.profiles.PersonTag");
dojo.require("lconn.core.globalization.bidiUtil");
dojo.require("lconn.core.formutilities");
dojo.require("lconn.core.util.html");

(function() {
var profiles_viewAllFriendsXSLT = 	"view-all-friends.xsl";
var profiles_recentFriendsXSLT = "recent-friends.xsl";
var profiles_acceptInvXSLT = 	"accept-invitations.xsl";
var profiles_sendRequestXSLT = 	"send-request.xsl";


var profiles_XSLTResourcesKeyArray = [];


lconn.profiles.Friending.instance = function()
{
	this.nDisplayRecentFriends = 12; // todo enh: these parameters should be part of the widgets-config.xml instance definition
	
	this.onLoad = function()
	{
		var attributesItemSet = this.iContext.getiWidgetAttributes();
		this.resourceId = attributesItemSet.getItemValue("resourceId");
		
		generalrs.label_inactive_user_msg = generalrs["label.inactive.user.msg"]; //make sure we get the inactive user message
		for (rName in generalrs) {
			if (rName && generalrs[rName] && rName.indexOf(".") == -1 && rName.indexOf("/") == -1) {
				profiles_XSLTResourcesKeyArray.push(rName);
			}
		}		
		
		var mode = this.iContext.getiDescriptor().getItemValue("mode");
		var widgetViewMode = "normal"; 
		if (mode == "fullpage")
		{
			widgetViewMode = "maximize";
			lconn.profiles.Friending.currentViewDomNode = "recent_colleagues_widgetId_fullpage_container";
		}
		else
			lconn.profiles.Friending.currentViewDomNode = "recent_colleagues_widgetId_container";
		
         document.getElementById(lconn.profiles.Friending.currentViewDomNode ).innerHTML += generalrs.friendsLoading;
         var widgetId = "_"+this.iContext.widgetId+"_";
         var attributesItemSet = this.iContext.getiWidgetAttributes();
         var displayedUserKey = attributesItemSet.getItemValue("profileDisplayedUserKey");
         lconn.profiles.Friending.loadWidgetContent(widgetViewMode, displayedUserKey, this.iContext);
	};
}

lconn.profiles.Friending.AddPageSubTitle = function(resourceBundleId, inviteAction)
{
	var titleNode = document.getElementById("pageSubTitle");
	
	if (titleNode) {
		var temp = generalrs[resourceBundleId];
		if(!inviteAction){
			temp = lconn.profiles.ProfilesCore.replacePlaceHolders(temp, ["<span class='vcard'><a href='javascript:void(0);' onclick='lconn.core.WidgetPlacement.reloadOverviewPage()' class='fn url'>"+lconn.core.globalization.bidiUtil.enforceTextDirection(lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName))+"</a><span class='x-lconn-userid' style='display: none;'>"+profilesData.displayedUser.userid+"</span></span>"]);
		}else{
			temp = lconn.profiles.ProfilesCore.replacePlaceHolders(temp, ["<span class='vcard'><a href='" + applicationContext + "/html/profileView.do?key="+profilesData.displayedUser.key+"' class='fn url'>"+lconn.core.globalization.bidiUtil.enforceTextDirection(lconn.core.util.html.encodeHtml(profilesData.displayedUser.displayName))+"</a><span class='x-lconn-userid' style='display: none;'>"+profilesData.displayedUser.userid+"</span></span>"]);
		}
		titleNode.innerHTML = temp;
		profiles_AddLiveNameSupport("pageSubTitle");
	}
	
		setTimeout(function() {
			if (dojo.byId("pageItemsMainRegion")) {
				dojo.attr(dojo.byId("pageItemsMainRegion"), "aria-label", lconn.profiles.ProfilesCore.replacePlaceHolders(generalrs[resourceBundleId], [lconn.core.globalization.bidiUtil.enforceTextDirection(profilesData.displayedUser.displayName)]));
			}
		}, 2000);	
}
		
lconn.profiles.Friending.setItemsPerPage = function(controlObj,itemsPerPage)
{
	var formObj = lconn.core.formutilities.findParentForm(controlObj);
	var displayedUserKey = formObj.elements["displayedUserKey"].value;
	var currentPage = 0; // formObj.elements["current-page"].value; Reset current page to 0 when setting the items per page. SPR#JMGE7JJR65
	var sortBy = formObj.elements["sortBy"].value;
	
	//profiles_clearExistingNodes();
	
	lconn.profiles.Friending.loadFullFriends(displayedUserKey, currentPage, sortBy, itemsPerPage);
}
	
lconn.profiles.Friending.pageTo = function(controlObj, pageNumber)
{
	var formObj = lconn.core.formutilities.findParentForm(controlObj);
	var displayedUserKey = formObj.elements["displayedUserKey"].value;
	var itemsPerPage = formObj.elements["items-per-page"].value;
	var sortBy = formObj.elements["sortBy"].value;
	var maxPages = formObj.elements["total-pages"].value;
	
	// pageNumber is 0,1,..(N-1); maxPages is 1,2..N
	var pNum = Math.max(0, Math.min(pageNumber, maxPages-1));
	lconn.profiles.Friending.loadFullFriends(displayedUserKey, (isNaN(pNum)?0:pNum), sortBy, itemsPerPage);
	
	return false;
}

lconn.profiles.Friending.sortFriends = function(formControl, sortByValue)
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
	
	lconn.profiles.Friending.loadFullFriends(displayedUserKey, currentPage, sortBy, itemsPerPage);
}

lconn.profiles.Friending.loadWidgetContent = function(widgetViewMode, displayedUserKey, iContext)
{
	/** internal functions */
	var _loadPendingInv = function()
	{
		var dataUrl = applicationContext + "/atom2/forms/invitations.xml?ui-level=second";
		lconn.profiles.ProfilesXSL.loadContent(dataUrl, profiles_acceptInvXSLT, lconn.profiles.Friending.currentViewDomNode, profiles_XSLTResourcesKeyArray, null, null, true /* append lastMod */);
		profiles_AddLiveNameSupport("accept-invitations-section");
		//profiles_AddLiveNameSupport("friendsInCommon_div");
	};
	
	var _loadSendFriendInv = function(targetKey)
	{	
		var LoggedInUserUid = lconn.profiles.ProfilesCore.getLoggedInUserKey();
		var showInviteUI = (LoggedInUserUid != targetKey);
  		var params = lconn.profiles.ProfilesCore.loadResourceStrings(null,
  			['friendsInitialMsgForInv', 'friendsIncludeMsgForInv', 'friendsSendInvAction', 'friendsCancelInvAction']);
		params.push(["applicationContext", applicationContext]);
		var xmlDoc = dojox.data.dom.createDocument("<xml-root ui-level='second' targetKey='"+targetKey+"' showInviteUI='"+showInviteUI+"'/>");
		lconn.core.xslt.transformAndRender({
			xmlDoc: xmlDoc, 
			xslDoc: lconn.profiles.ProfilesXSL.getCachedXSL(profiles_sendRequestXSLT),
			htmlContainerElemId: lconn.profiles.Friending.currentViewDomNode, 
			aXslParams: params
		});

		if(LoggedInUserUid == targetKey && targetKey != null)
			lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "error", generalrs.friendsCannotAddYourself, true /* show alert if element does not exist */ );
	};
	
	var _loadRecentFriends = function(displayedUserKey, container, iContext)
	{
		var dataUrl = applicationContext + "/atom2/forms/recentfriends.xml?key=" + displayedUserKey;
		var widgetScope = iContext.iScope();
		dataUrl += ((widgetScope && widgetScope.nDisplayRecentFriends)? "&pageSize=" + widgetScope.nDisplayRecentFriends: "");
		
		try
		{  
			lconn.profiles.ProfilesXSL.loadContent(dataUrl, profiles_recentFriendsXSLT, container, 
				["friendsViewAllFriends","friendsNoFriends","friendsNewInv","friendsNewInvs"], null, displayedUserKey, true /* append lastMod */);
			profiles_AddLiveNameSupport(lconn.profiles.Friending.currentViewDomNode );
		}
		catch(exception)
		{
			lconn.profiles.ProfilesCore.DefaultErrorHandler("_loadRecentFriends", exception,container);
		}
	}
	/** end of internal functions */
	
	var friendDivContainer = lconn.profiles.Friending.currentViewDomNode ;
	
	if(widgetViewMode == "maximize")
	{
	  var action =  lconn.profiles.ProfilesCore.getParam("action");
	  if(action == null || action == "rc")
	  {
	    lconn.profiles.Friending.AddPageSubTitle("friendsColleaguesFor", true);
	    lconn.profiles.Friending.loadFullFriends(displayedUserKey);
	  }
	  else if(action == "in")
	  {
	    lconn.profiles.Friending.AddPageSubTitle("friendsColleaguesFor", true);
	    _loadPendingInv();
	  }
	  else if(action == "fr")
	  {
	   lconn.profiles.Friending.AddPageSubTitle("friendsColleaguesInvite", true);
	    var targetKey =  lconn.profiles.ProfilesCore.getParam("targetKey");
	    _loadSendFriendInv(displayedUserKey);
	  }
	}
	else
	  _loadRecentFriends(displayedUserKey, friendDivContainer, iContext);

}

lconn.profiles.Friending.handlePageToEnterKey = function(event, selfObj, pageNum)
{
	var evt = event || window.event;
	if (evt.keyCode == 13) {
		dojo.stopEvent(evt);
		lconn.profiles.Friending.pageTo(selfObj, pageNum);
		return false;
	}
	return true;
}

lconn.profiles.Friending.loadFullFriends = function(displayedUserKey, pageNumber, sortBy, pageSize)
{
	var dataUrl = applicationContext + "/atom2/forms/viewallfriends.xml" + 
					"?key=" + displayedUserKey + 
					"&ui-level=second" +
					((pageNumber != null)?"&pageNumber=" + pageNumber:"") +
					((sortBy != null)?"&sortBy=" + sortBy:"") +
					((pageSize != null)?"&pageSize=" + pageSize:"") +
					((bShowEmail)?"&showEmail":"");

	var showEmailParam = new Array();
	showEmailParam.push(["showEmail", ((bShowEmail)?"true":"false")]);
	var additionalParamsMap = showEmailParam;
	
	lconn.profiles.ProfilesXSL.loadContent(dataUrl, profiles_viewAllFriendsXSLT, lconn.profiles.Friending.currentViewDomNode, profiles_XSLTResourcesKeyArray, additionalParamsMap, displayedUserKey, true /* append lastMod */);
	profiles_AddLiveNameSupport("friendsThirdLevel");
	lconn.core.utilities.processUntilElementIsFound(
		"friends_mainContentTable", 
		function(){
			lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId("friends_mainContentTable"));
		},
		null, null, false
	);		
}

lconn.profiles.Friending.viewAllColleagues = function(key)
{
	var LoggedInUserUid = lconn.profiles.ProfilesCore.getLoggedInUserKey();
	var self = (LoggedInUserUid == key);
	var url = applicationContext+"/html/networkView.do"+"?widgetId=friends"+"&key="+key+(self?"&requireAuth=true":"");
	profiles_goto(url, true);
}

lconn.profiles.Friending.viewAllInvitations = function()
{
	// var url = applicationContext + "/xsl/invitations.xml?xslt=" + profiles_acceptInvXSLT;
	var url = applicationContext + "/html/networkView.do"+"?widgetId=friends"+"&action=in"+(self?"&requireAuth=true":"");
	profiles_goto(url, true);
}

lconn.profiles.Friending.viewSendRequest = function(targetKey)
{
	if( dijit.byId("networkInviteDialog") ) // use the invite dojo dijit if available 
		lconn.profiles.Friending.showNetworkInvite(targetKey);
	else {
		// var url = applicationContext + "/xsl/view-send-request.xml?targetKey=" + targetKey + "&xslt=" + profiles_sendRequestXSLT;
		var url = applicationContext + "/html/wc.do?action=fr&requireAuth=true&widgetId=friends&targetKey=" + targetKey;
		profiles_goto(url);
	}
}

lconn.profiles.Friending.showNetworkInvite = function(key) {
 	var vDialog=dijit.byId("networkInviteDialog");
 	if( vDialog ) vDialog.show();
}

lconn.profiles.Friending.hideNetworkInvite = function() {
 	var vDialog=dijit.byId("networkInviteDialog");
 	if( vDialog ) {
 		vDialog.hide();
 	}
 	else if ( dojo.byId("networkInviteDialogWindow") ) { // in windowed mode
 		window.close();
 	}
	return false;
}

lconn.profiles.Friending.showColleagues = function(displayedUserKey, forceReload)
{
	lconn.profiles.ProfilesCore.hide("invitationsTabContent");
	lconn.profiles.ProfilesCore.show("friendsTabContent");
	
	var invElm = dojo.byId("friendsTabContent");
	if( invElm ) { 
		var temp = invElm.getAttribute("empty");
		if(temp == "true" || forceReload)
		{
			invElm.innerHTML += "<img src='" + applicationContext + "/images/blank.gif' width='500' height='0'/>";
			var dataUrl = applicationContext + "/atom2/forms/viewallfriends.xml?key="+ displayedUserKey;
			lconn.profiles.ProfilesXSL.loadContent(dataUrl, profiles_viewAllFriendsXSLT, "friendsTabContent", profiles_XSLTResourcesKeyArray, null, displayedUserKey, true /* append lastMod */);
			invElm.setAttribute("empty", "false");
		}
		return true;
	}
	return false;
}

lconn.profiles.Friending.showInvitations = function( forceReload )
{
	lconn.profiles.ProfilesCore.hide("friendsTabContent");
	lconn.profiles.ProfilesCore.show("invitationsTabContent");
	
	var invElm = dojo.byId("invitationsTabContent");
	if( invElm ) { 
		var temp = invElm.getAttribute("empty");
		if(temp == "true" || forceReload)
		{
			invElm.innerHTML += "<img src='" + applicationContext + "/images/blank.gif' width='500' height='0'/>";
			var dataUrl = applicationContext + "/atom2/forms/invitations.xml" + "?lastMod=" + profilesData.config.profileLastMod;
			lconn.profiles.ProfilesXSL.loadContent(dataUrl, profiles_acceptInvXSLT, "invitationsTabContent", profiles_XSLTResourcesKeyArray);
			invElm.setAttribute("empty", "false");
		}
		window.setTimeout("lconn.profiles.Friending.setFocus('accept_link_1');lconn.profiles.Friending.updateLeftPanel();", 1000);
		return true;
	}
	return false;
}

lconn.profiles.Friending.updateLeftPanel = function() {
	document.getElementById("inivtesMenuA").getElementsByTagName("label")[0].innerText = '(' + document.getElementById("friends_count").getElementsByTagName("h3")[0].innerHTML.charAt(0) + ')';
}

lconn.profiles.Friending.RemoveFriends = function(formControl, displayedUserKey)
{
	if (formControl && formControl.tagName.toLowerCase() !== "form") {
		formControl = lconn.core.formutilities.findParentForm(formControl);
	}
	
	var stringContent = "";
	dojo.query("input[id^='select_friend_']:checked", formControl).forEach(
		function(node) {
			if (node && node.value && node.value.length > 0) {
				stringContent += node.value + ",";
			}
		}
	);

	if(stringContent == "")
	{
		lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "info", generalrs.friendsSelectFriendForRemoval, true /* show alert if element does not exist */ );
		return;
	}
	var dataUrl = applicationContext + "/atom2/forms/friends.xml?connectionIds=" + stringContent + "&lastMod=" + profilesData.config.profileLastMod;
	lconn.profiles.xhrDelete({
	   	url: dataUrl,
		handleAs: "xml",
		htmlContainerElemId: lconn.profiles.Friending.currentViewDomNode ,
       	error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
		load: function(response, ioArgs){
			profiles_setProfilesLastMod(response);
		    lconn.profiles.Friending.loadFullFriends(displayedUserKey);
			if(response.documentElement.nodeName == "success")
				lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "confirmation", generalrs.friendsRemoved);
	   	},
	   	checkAuthHeader: true
	});
	window.setTimeout("lconn.profiles.Friending.setFocus('select_friend_1');", 1000);
}

lconn.profiles.Friending.sendFriendRequest = function(formControl, targetKey, targetUserId, doneUrl)
{
	if(typeof(doneUrl) == "undefined") var doneUrl = "";
	
	var dataUrl = applicationContext + 
					"/atom2/forms/friendrequest" + 
					(targetKey?"?targetKey=" + targetKey:(targetUserId?"?targetUserId=" + targetUserId:"?")) + 
					"&lastMod=" + profilesData.config.profileLastMod;
	
	var msg = formControl.form.elements["invitation_text"].value;
	
	var LoggedInUserKey = lconn.profiles.ProfilesCore.getLoggedInUserKey();
	if(targetKey && LoggedInUserKey == targetKey)
	{
		lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "error", generalrs.friendsCannotAddYourself, true /* show alert if element does not exist */ );
		return false;
	}		
			
	if(msg != null && msg != "");
	{
		// we generically limit the characters to 500, which is 1/4 what the database field limit is
		// so that international characters are covered properly
		if(msg.length > 500)
		{
			var temp = lconn.profiles.ProfilesCore.replacePlaceHolders(generalrs.friendsYouExceedTextLimit, [msg.length, '500']);
			lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "error", temp, true /* show alert if element does not exist */ );
			return false;
		}
		msg = msg.replace(new RegExp( "\\n", "g" ), "<br/>");
		// dataUrl += "&msg=" + encodeURIComponent(msg);
	}	

	dojo.rawXhrPut({
	   	url: dataUrl,
		handleAs: "xml",
		putData: msg,
		htmlContainerElemId: lconn.profiles.Friending.currentViewDomNode ,
       	error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
		load: function(response, ioArgs){
			try{
				if ( dojo.byId("networkInviteDialogWindow") ) window.close(); // windowed mode - TO DO: REVISE
				
				if(response.documentElement.nodeName == "error" && response.documentElement.getAttribute("code") == "connection-exist")
					lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "error", generalrs.friendsInvSent, true /* show alert if element does not exist */ );
				
				else if(response.documentElement.nodeName == "error" && response.documentElement.getAttribute("code") == "notification-error")
					lconn.profiles.ProfilesCore.showInfoMsg( "profileInfoMsgDiv", "error", generalrs.errorDefaultMsg2, true /* show alert if element does not exist */ );
				
				else if(response.documentElement.nodeName == "error" || response.documentElement.nodeName == "parsererror")
					lconn.profiles.ProfilesCore.DefaultXHRErrorHandler(response, ioArgs);
				
				else {
					profiles_setProfilesLastMod(response);

					if(doneUrl)	{
						if(doneUrl == 'back') {
							profiles_goBack();
						}
						else {
							profiles_goto( doneUrl);
						}
					}
				}
			}
			catch(exception)
			{
				lconn.profiles.ProfilesCore.DefaultErrorHandler("WidgetMgmt.loadWidgets", exception, "invitation");
			}
	   	}
	});
}

lconn.profiles.Friending.acceptFriendRequest = function(connectionId, displayedUserKey, callback)
{
	var dataUrl = applicationContext + "/atom2/forms/acceptrequest?connectionId=" + connectionId + "&lastMod=" + profilesData.config.profileLastMod;
	lconn.profiles.Friending.FriendRequestAction(dataUrl, connectionId, displayedUserKey, callback);
}

lconn.profiles.Friending.rejectFriendRequest = function(connectionId, displayedUserKey, callback)
{
	var dataUrl = applicationContext + "/atom2/forms/rejectrequest?connectionId=" + connectionId + "&lastMod=" + profilesData.config.profileLastMod;
	lconn.profiles.Friending.FriendRequestAction(dataUrl, connectionId, displayedUserKey, callback);
}

lconn.profiles.Friending.FriendRequestAction = function(dataUrl, connectionId, displayedUserKey, callback)
{	
	if( typeof(callback) == "function") // use the supplied callback if available
		_callback = callback;
	else {
		_callback = function(response, ioArgs){
			try
			{
				profiles_setProfilesLastMod(response);
				var invElm = document.getElementById("friendsTabContent");
				invElm.setAttribute("empty", "true");
				invElm = document.getElementById("invitationsTabContent");
				invElm.setAttribute("empty", "true");
				if(displayedUserKey != null) lconn.profiles.Friending.showInvitations();
				//profiles_goBack();
			}
			catch(exception)
			{
				lconn.profiles.ProfilesCore.DefaultErrorHandler("lconn.profiles.Friending.FriendRequestAction", exception, lconn.profiles.Friending.currentViewDomNode );
			}
		};
	}

	//alert("lconn.profiles.Friending.acceptFriendRequest: " + connectionId + " " + displayedUserKey);
	lconn.profiles.xhrPost({
	   	url: dataUrl, 
	   	handleAs: "xml",
		htmlContainerElemId: lconn.profiles.Friending.currentViewDomNode,
       	error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
		load: _callback,
	   	checkAuthHeader: true
	});
}

lconn.profiles.Friending.setFocus = function(elemId)
{
	var elem = document.getElementById(elemId);
	if(elem)
	{
		elem.focus();
	}
	else
	{
		elem = document.getElementById("friends_count");
		if(elem) elem.focus();
	}
}

lconn.profiles.Friending.showAllCommonFriends = function(inviterKey)
{
	dojo.query('.lotusHidden','friendsInCommon_div_' + inviterKey).forEach(
			function(node, index, arr) {
				dojo.removeClass( node, 'lotusHidden');
			}
	);
	dojo.addClass('frindsInCommonShowAll_div_' + inviterKey, 'lotusHidden');
}

})();
