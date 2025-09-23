/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2001, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.profileDetails");
dojo.require("dojo.cookie");

lconn.profiles.profileDetails.instance = function() {


	this.onLoad = function() {
		var widgetId = "_"+this.iContext.widgetId+"_";
		var mode = this.iContext.getiDescriptor().getItemValue("mode");
		var widgetViewMode = (mode == "fullpage") ? "maximize" : "normal"; 
		var currentViewDomNodeId = (mode == "fullpage") ? widgetId + "profileDetails_fullpage_container" : widgetId + "profileDetails_widget_container";
		var nlsKeyToRender = this.iContext.widgetId + "Loading";
		
		var titleNode = document.getElementById(widgetId + "pageSubTitle");
		var viewNode = document.getElementById(currentViewDomNodeId);
		if (viewNode) viewNode.innerHTML = generalrs[nlsKeyToRender] ? generalrs[nlsKeyToRender] : generalrs["widgetLoading"];		
	
		var attributesItemSet = this.iContext.getiWidgetAttributes();
		var section = attributesItemSet.getItemValue("section");
		
		var dataUrl = applicationContext + "/html/profileDetails.do?key="+ profilesData.displayedUser.key +"&lastMod=" + profilesData.config.profileLastMod + "&section=" + section;		
        var langCookie = dojo.cookie(profilesData.config.langCookieName);
        if (typeof langCookie != 'undefined') {
            dataUrl += ("&lang=" +langCookie);
        }
		lconn.profiles.xhrGet({
		   	url: dataUrl,
			load: 
				function(response, ioArgs){
					try {
						if (viewNode) {
							viewNode.innerHTML = response;
							dojo.style(viewNode, "overflow", "auto");
							lconn.profiles.ProfilesCore.waitForAppRegReady(function(err) {
							  if (!err) {
							    lconn.profiles.ProfilesCore.augmentPhoneCallLinks(viewNode);
							  }
							});
						}
						if (titleNode && titleNode.innerHTML.length == 0 && titleNode.parentNode) titleNode.parentNode.removeChild(titleNode);
					}
					catch(exception) {
						lconn.profiles.ProfilesCore.DefaultErrorHandler(widgetId, exception, currentViewDomNodeId );
					}
				},
	       	error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
		   	checkAuthHeader: true
		});
	};
}
