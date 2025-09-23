/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * This class handles all of the javascript interaction on the Network profiles page.  
 * NOTE: Most of this code was migrated directly from the networkView.jsp.
 */

dojo.provide("lconn.profiles.profilesNetworkPage");

dojo.require("lconn.profiles.ProfilesCore");


dojo.requireLocalization("lconn.profiles", "ui");

(function(win) {
	
	lconn.profiles.profilesNetworkPage = {
	
		strings: {},
		
		profile: {
			displayName: "",
			key: "",
			userId: "",
			email: "",
			showEmail: true,
			isActive: true
		},
		
		
		isMyProfile: false,
		isLoggedIn: false,
		
		//this is the default
		urlObject: {
			path: "/profiles/html/networkView.do",
			queryParameters: {
				widgetId: "friends",
				action: "rc",
				requireAuth: true
			}		
		},
		
		selIdx: -1,
		
		appContext: "/profiles",
		
		ids: {
			navMenu: "profilesNavMenuUL"
		},

		
		
		//load up what we need for the main page
		init: function(args) {
			args = args || {};
			
			this.strings = args.strings || window.generalrs || this.strings;
			
			//load our strings...
			var defaultStrings = dojo.i18n.getLocalization("lconn.profiles", "ui");
			if (typeof defaultStrings !== "undefined") {
				this.strings = dojo.mixin(
					defaultStrings,
					this.strings
				);
			}

			//make sure our app context is correct
			this.appContext = args.appContext || applicationContext || this.appContext;

			//mixin our key objects
			this.profile = dojo.mixin(dojo.clone(this.profile), dojo.clone(args.profile || {}));
			
			this.ids = dojo.mixin(dojo.clone(this.ids), dojo.clone(args.ids || {}));
						
			this.isLoggedIn = (dojo.exists("profilesData.loggedInUser") && profilesData.loggedInUser.isLoggedIn);
			
			this.urlObject = lconn.core.url.parse(win.location.href);
			
			var loadToolbarControl_ = dojo.hitch(this, function() {
				dojo.addOnLoad(dojo.hitch(this, "_loadToolbar"));
			});
				
			// ie9+ has an issue where dojo addOnLoad is firing before the page is actually all loaded
			// so we need to hang the function off of the window load event.		
			if (dojo.isIE && dojo.isIE >= 9) {
				dojo.connect(window, "load", loadToolbarControl_);
			} else {
				loadToolbarControl_();
			}	

				
		},
		
		_toolbar: null,
		_loadToolbar: function() {
			this.isMyProfile = (lconn.profiles.ProfilesCore.getLoggedInUserKey() == this.profile.key);

			var widgetId = this.urlObject.queryParameters.widgetId;
			var action = this.urlObject.queryParameters.action;
			
			if (widgetId == "follow") {
				if (action == "out") { //friend request
					this.selIdx = 3;
				} else {
					this.selIdx = 2;
				}
				
			} else { //friends (default)			
				if (action == "in") { //show invites
					this.selIdx = 1;
				} else if (action == "fr") { //friend request
					this.selIdx = -1;
				} else { //show colleages (default)
					this.selIdx = 0;
				}
				
			}
			this._toolbar = new lconn.profiles.aria.Toolbar(
				this.ids.navMenu, 
				{
					selIdx: this.selIdx,
					stringKeys: {
						unselected: "label.a11y.button.unselected",
						selected: "label.a11y.button.selected"
					}					
				}
			); // accessibility

		},
		
		menuSelect: function( urlParams ) {
			var selectNode = urlParams.selectNode;
			delete urlParams.selectNode;
			
			//if the select node isn't passed in, set the focus to the main area.
			// taken out with 153856.  May be re-introduced with 149676.
			//if (!selectNode) {
			//	selectNode = "*[role='main']";
			//}
			
			this.urlObject.queryParameters = dojo.mixin(this.urlObject.queryParameters, (urlParams || {}));
			
			var widgetId = this.urlObject.queryParameters.widgetId;
			
			if (widgetId == "friends") {
				if (this.isMyProfile) {
					this.urlObject.queryParameters.requireAuth = true;
				} else {
					delete this.urlObject.queryParameters.requireAuth;
				}
			}
			
			profiles_goto(lconn.core.url.write(this.urlObject), true, selectNode);
		}
		
		
			
	};
	
	//backwards compat
	win.profilesNetwork_menuSelect = lconn.profiles.profilesNetworkPage.menuSelect;
	
	
})(window);

