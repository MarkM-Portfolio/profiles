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

dojo.provide("lconn.profiles.backgroundInfo");
dojo.require("dojo.cookie");

lconn.profiles.backgroundInfo.instance = function() {
	
	this.onLoad = function() {
		var widgetId = "_"+this.iContext.widgetId+"_";
		var mode = this.iContext.getiDescriptor().getItemValue("mode");
		var widgetViewMode = (mode == "fullpage") ? "maximize" : "normal"; 
		var currentViewDomNode = (mode == "fullpage") ? "backgroundInfo_widgetId_fullpage_container" : "backgroundInfo_widgetId_container";
		var el = document.getElementById(currentViewDomNode);
		if(el) el.innerHTML = generalrs.backgroundInfoLoading;

		var dataUrl = applicationContext + "/html/profileDetails.do?key="+ profilesData.displayedUser.key +"&lastMod=" + profilesData.config.profileLastMod + "&section=associatedInformation";		
        var langCookie = dojo.cookie(profilesData.config.langCookieName);
        if (typeof langCookie != 'undefined') {
            dataUrl += ("&lang=" +langCookie);
        }
		lconn.profiles.xhrGet({
		   	url: dataUrl,
			load: 
				function(response, ioArgs){
					try {
						var el = document.getElementById(currentViewDomNode);
						if(el)el.innerHTML = response;
					}
					catch(exception) {
						lconn.profiles.ProfilesCore.DefaultErrorHandler(widgetId, exception, currentViewDomNode );
					}
				},
	       	error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
		   	checkAuthHeader: true
		});
	};
}
