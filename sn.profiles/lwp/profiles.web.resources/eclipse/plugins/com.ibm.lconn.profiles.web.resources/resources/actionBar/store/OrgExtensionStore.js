/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2017                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.actionBar.store.OrgExtensionStore");

dojo.require("lconn.profiles.actionBar.store.BaseStore");

dojo.require("lconn.core.config.services");
dojo.require("lconn.core.url");

dojo.declare(
	"lconn.profiles.actionBar.store.OrgExtensionStore", 
	[lconn.profiles.actionBar.store.BaseStore],
	{
		loadData: function(callback) {
			if (dojo.exists("profilesData.config") && !profilesData.config.isLotusLive) {
				callback();
				return;
			}

			var bssUrl = lconn.core.url.getServiceUrl(lconn.core.config.services.bss); // "/manage";
			if (bssUrl) {

				// build the url to the bss api to get the action extensions
				var query = {};
				// RTC 192258 [OCS 212625] [EH] CSRF_Token in URL via profiles
//				if (dojo.cookie) {
//					query["csrfToken"] = dojo.cookie("token");
//				}
				if (dojo.exists("lconn.share.config.services.maxAge")) {
					query["max-age"] = dojo.getObject("lconn.share.config.services.maxAge");
				}
				var sQuery = dojo.objectToQuery(query);
				if (sQuery.length > 0) sQuery = "?" + sQuery;

				dojo.xhrGet({
					url: bssUrl.toString() + "/extensions/getExtensions/byExtensionPoint/person_component" + sQuery,
					handleAs: "json",
					load: dojo.hitch(this, function(items) {

						//these parameters from the bss action extension can be processed for replacement 
						//of info from the displayed user					
						var processParams = ["url", "menu_text", "tooltip"];

						//this object is used as a search replace parameter map for the url link and menu text
						//currently only user_id is supported.
						var dispUser = this.profile;
						dispUser.user_id = dispUser.userid;

						//iterate through the actions and add the menuitems
						dojo.forEach(items, dojo.hitch(this, function(item, idx) {

							if (item.type == "action") {

								//replace the parameters of the action items with the map of the displayed user
								dojo.forEach(processParams, dojo.hitch(this, function(param) {
									if (item[param]) {
										for (var prop in dispUser) {
											if (dispUser.hasOwnProperty(prop)) {
												var cleanParam = dojox.html.entities.encode(item[param].replace(new RegExp("\\$\\{" + prop + "\\}", "g"), dispUser[prop]));
												item[param] = ( !(cleanParam.substring(0,4) === "http") && param === "url") ? "": cleanParam; 
											} 
										}
									}
								}));

								this.addDataItem(item);								
							}
						}));

						callback();
					}),
					// RTC 192258 [OCS 212625] [EH] CSRF_Token in URL via profiles
					headers: {
//						'X-Csrf-Token': $rootScope.token
						'X-Csrf-Token': dojo.cookie("token")
					},
					error: dojo.hitch(this, function(err) {
						this.onError(err);
						callback();
					}),
					sync: false
				});

			} else {
				callback();
			}
		}
	}
);
