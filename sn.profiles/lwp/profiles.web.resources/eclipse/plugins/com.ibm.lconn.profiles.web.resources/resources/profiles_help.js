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

dojo.provide("lconn.profiles.profiles_help");
dojo.require("lconn.core.help");


// encapsulate the js functions we don't need to expose publicly.
(function() {
	// function to hook up the help links
	var connectHelpLinkAction_ = function(query, topic) {
		dojo.query(query).forEach(function(helpNode) {
			dojo.style(helpNode, {"cursor": "pointer"});
			dojo.connect(helpNode, "onclick", function(e) {
				dojo.stopEvent(e);
				if (topic) {
					lconn.profiles.profiles_help.open(topic);
				} else {
					lconn.profiles.profiles_help.open();
				}
			});
		});
	};
	
	//if the url contains the on prem profiles plugin, just strip it out and let the help system find it.
	//this is needed for help to work in both cloud and onprem.
	var stripProfilesTopicPath_ = function(url) {
		if (typeof url == "string" && url.indexOf("/com.ibm.lotus.connections.profiles.help/pframe.html") > -1) {
			//default topic path.  return null and let the help system determine the default.
			return null;
		} else if (typeof url == "string" && url.indexOf("/com.ibm.lotus.connections.profiles.help/") > -1) {
			//onprem topic path is hardcoded.  remove it.
			return url.substring(url.lastIndexOf("/")+1);
		} else {
			return url;
		}
	};	
	
	//if there is a topic id that's changed between onprem and smartcloud
	var smartCloudTopicMap_ = {
		"t_pers_view_colleagues.html": "network_contacts.html",
		"t_pers_audiofile_upload.html": "t_people_edityourprofile.html",
		"t_pers_edit_profiles.html": "t_people_edityourprofile.html",
		"t_pers_change_bgd_info.htm": "t_people_edityourprofile.html",
		"c_pers_profiles.html": "pframe.html"
	};
	
	var getHelpTopicId_ = function(topicId) {
		var ret = stripProfilesTopicPath_(topicId);
		if (dojo.exists("profilesData.config") && profilesData.config.isLotusLive == true) {
			if (smartCloudTopicMap_[ret]) {
				ret = smartCloudTopicMap_[ret];
			}
		}
		
		return ret;
	};	
	
	var loadAllHelpActions_ = function() {
		dojo.addOnLoad(function() {
			connectHelpLinkAction_(".help_link");
			connectHelpLinkAction_(".edit_help_link", 't_pers_edit_profiles.html');
			connectHelpLinkAction_(".search_help_link", 't_pers_search_directory.html');
			connectHelpLinkAction_(".tags_help_link", 'c_pers_tags.html');
			connectHelpLinkAction_(".related_content_help_link", 'c_pers_profiles.html');
			connectHelpLinkAction_(".pronunciation_help_link", 't_pers_edit_profiles.html');
			connectHelpLinkAction_(".more_pronunciation_help_link", 't_pers_edit_profiles.html');
			
			//for our widgets, we need to strip out the profiles topic so the help system can find it on cloud as well as on prem
			if (dojo.exists("lconn.core.WidgetPlacement.openHelpWindow") && typeof lconn.core.WidgetPlacement.openHelpWindow == "function" && typeof lconn.core.WidgetPlacement["openHelpWindow.profilesOverride_"] == "undefined") {
				lconn.core.WidgetPlacement["openHelpWindow.profilesOverride_"] = lconn.core.WidgetPlacement.openHelpWindow;
				lconn.core.WidgetPlacement.openHelpWindow = function(url) {
					//Workaround for SmartCloud help issue.  Will be address in D42 and this change should be backed out once OCS 170158 is fixed.
					//lconn.core.WidgetPlacement["openHelpWindow.profilesOverride_"](getHelpTopicId_(url));
					lconn.profiles.profiles_help.open(url);
				}
			}
			
		});
	};
	

	
	lconn.profiles.profiles_help = {
		open: function(url) {
			lconn.core.help.launchHelp(getHelpTopicId_(url));
		},
		
		openDemo: function() {
			if (window.console) {
				console.warn("Demos are no longer available.  Opening help...");
			}
			this.open();		
		},
		
		init: function() {
			if (dojo.isIE && dojo.isIE >= 9) {
				dojo.connect(window, "load", loadAllHelpActions_);
			} else {
				loadAllHelpActions_();
			}
			
			//backwards compat...
			window.openHelpWindow = dojo.hitch(this, function(ctx/*not used*/, url) {
				this.open(url);
			});
			window.openDemoWindow = dojo.hitch(this, this.openDemo);
		},
		
		createWelcomeLineText: function(id, string, accessString, linkHref) {
			var QUOTESTRING = "&#034;";
			while (accessString.indexOf(QUOTESTRING) > -1) {
				accessString = accessString.replace(QUOTESTRING, '"');
			}
			
			var docElement = document.getElementById(id);
			if (docElement) {
				if (linkHref) {
					var linkText = string.replace('{0}', '<a href="javascript:;">');
					linkText = linkText.replace('{1}', '</a>');
					linkText = linkText.replace("'", "\'");
					docElement.innerHTML = linkText;

					var link = docElement.getElementsByTagName('a');
					if (link.length > 0) {
						link = link[0];
						link.onclick = function() {
							lconn.profiles.profiles_help.open(linkHref); 
							return false;
						};
					}
					
				} else {
					string = string.replace('{0}', "").replace('{1}', "");
					docElement.innerHTML = string;
				}
			}		
		}
	};
	
	lconn.profiles.profiles_help.init();

})();
