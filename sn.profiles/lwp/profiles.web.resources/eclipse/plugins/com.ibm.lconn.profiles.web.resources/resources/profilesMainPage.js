/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * This class handles all of the javascript interaction on the Main profiles page.  
 * NOTE: Most of this code was migrated directly from the businessCardInfo.jsp.
 */

dojo.provide("lconn.profiles.profilesMainPage");

dojo.require("lconn.profiles.ProfilesCore");

dojo.require("lconn.profiles.actionBar.ActionBar");

dojo.require("lconn.profiles.actionBar.store.MainPageStore");
dojo.require("lconn.profiles.actionBar.store.OrgExtensionStore");
dojo.require("lconn.profiles.actionBar.store.SocialContactsStore");

dojo.require("lconn.profiles.integrations.store.AppRegStore");
dojo.require("lconn.core.util.html");
dojo.require("lconn.core.util.services");

dojo.requireLocalization("lconn.profiles", "ui");

(function() {
	var _actionBar = null;
	var isDebug1 = false;
	
	lconn.profiles.profilesMainPage = {
	
		strings: {},
		
		profile: {
			displayName: "",
			key: "",
			userid: "",
			email: "",
			tenantKey: "",
			showEmail: true,
			isActive: true
		},
		
		enabledPermissions: [],
		
		isMyProfile: false,
		isLoggedIn: false,
		
		actionBarNode: null,
		
		connection: {
			id: null,
			status: null,
			canFriend: false,
			canUnfriend: false,
			canFollow: false,
			canUnfollow: false,
			TYPES: {
				PENDING: "0",
				CONNECTED: "1",
				INVITED: "2"			
			}
		},
		
		appContext: "/profiles",
		
		
		//load up what we need for the main page
		init: function init_$0(args) {
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
			this.connection = dojo.mixin(dojo.clone(this.connection), dojo.clone(args.connection || {}));
			this.profile = dojo.mixin(dojo.clone(this.profile), dojo.clone(args.profile || {}));
			
			this.enabledPermissions = args.enabledPermissions;
			
			this.isLoggedIn = (dojo.exists("profilesData.loggedInUser") && profilesData.loggedInUser.isLoggedIn);
			
			// initialize AppRegStore singleton instance
			if (dojo.exists("lconn.profiles.integrations.store.AppRegStore")) {
				var appRegStore = lconn.profiles.integrations.store.AppRegStore.getInstance();
				appRegStore.loadData(function(err){
					if (err) {
						console.error("Error loading AppRegStore: " + err);
					} else {
						var bizcard = dojo.byId("businessCardDetails");
						if (bizcard) {
						lconn.profiles.ProfilesCore.augmentPhoneCallLinks(bizcard);
						}
					}
				});
			}
			
			//put a slight delay to make sure all is loaded.
			setTimeout(dojo.hitch(this, function(){
				this.isMyProfile = (lconn.profiles.ProfilesCore.getLoggedInUserKey() == this.profile.key);
				
				this._setInactive();
				
				this._loadSametimeStatus();
				
				this._loadActionBar();
				
				this._loadConfirmationMessages();
				
			}),100);
				
				
		},
		
		// Method to wait until window.navbarData is available.
		// window.navbarData contains data and method for user entitlement checking,
		// which is required for AppReg extensions.
		// @param callback - a callback to be executed with window.navbarData as parameter when data is available 
		_waitForNavbarData: function(callback) {
      var MAX_TIMEOUT = 10000;
      var TIME_OFFSET = 500;
      var count = 0;
      var intervalId = setInterval(function() {
        if (!window.navbarData && count < MAX_TIMEOUT) {
          count += TIME_OFFSET;
        } else {
          clearInterval(intervalId);
          if (window.navbarData) {
            window.navbarData.isCreated(function(){
              callback(window.navbarData);
            });
          } else {
            callback();
          }
        }
      }, TIME_OFFSET);
		},
		
		_setInactive: function _setInactive_$1() {
			//dim the screen if the profile isn't active
			if (!this.profile.isActive) {
				dojo.addOnLoad(dojo.hitch(this, function(){
					try {
						dojo.addClass( dojo.byId("profilePaneLeft"), "lotusDim");
						
						/* lotusDim fails contrast for a11y 
						dojo.addClass( dojo.byId("businessCardContent"), "lotusDim");	
						dojo.addClass( dojo.byId("profilePaneRight"), "lotusDim");
						dojo.addClass( dojo.byId("centerWidgetContainer"), "lotusDim");
						*/
						this.showMessage("label.inactive.user.msg", [], "inactiveUserMsgDiv", "info");
						
					} catch (e) {
						if (window.console) console.warn("Unable to dim all screen elements." , e);
					}
				}));
			}		
		},
		
		showMessage: function showMessage_$2(key, args, node, typ) {
			if (!args) {
				args = [this.profile.displayName];
				
			} else if (typeof args === "string") {
				args = [args];
				
			}
			
			if (typeof node !== "string") {
				node = "profileInfoMsgDiv";
			}
			
			if (typeof typ !== "string") {
				typ = "confirmation";
			}
			
			var msg = key;
			if (this.strings[key]) {
				msg = this.strings[key];
			}
			for (var i = 0; i < args.length; i++) {
				msg = msg.replace("{" + i + "}", lconn.core.util.html.encodeHtml(args[i]));
			}

			
			if (dojo.byId(node)) {
				lconn.profiles.ProfilesCore.showInfoMsg( node, typ, msg);
			}
		},
		
		_loadConfirmationMessages: function _loadConfirmationMessages_$3() {
			
			dojo.addOnLoad(
				dojo.hitch(this, function() {
					var skey = null;
					switch( window.location.hash ) {
						case "#inputProfileActionInvited":						
							skey = "personCardInvitedContact"; 
							break;
						case "#inputProfileActionAcceptedInvite":						
							skey = "personCardAddedContact";
							break;
						case "#inputProfileActionRemovedFromNetwork":
							skey = "personCardRemovedContact";
							break;
						case "#inputProfileActionFollowing":
							dijit.focus(dojo.byId('inputProfileActionFollowing'));
						default:
							break;
					}
					
					if (skey) {
						this.showMessage(skey);
					}
					
					if (dojo.exists("lconn.core.globalization.bidiUtil")) {
						lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage();
					}
				})
			);
			
		},
		
		//reload the action bar with updated connection info.
		reloadActionBar: function reloadActionBar_$4(args) {
			this.connection = dojo.mixin(dojo.clone(this.connection), dojo.clone(args.connection || {}));
			
			//if we are reloading the actionBar, we want the focus to be on last clicked item.
			if (_actionBar && typeof _actionBar.getLastActionId == "function") {
				var focusIdx = -1;
				var id = _actionBar.getLastActionId();
				dojo.query("#businessCardActions>.lotusActionBar button").some(function(btn, idx) {
					if (btn.id === id) {
						focusIdx = idx;
						return true;
					}
				});
			}
			
			setTimeout(dojo.hitch(this, this._loadActionBar, focusIdx), 0);
			
		},
		
		//refresh the specified iwidgets on the page
		refreshWidgets: function refreshWidgets_$5(arr) {
			arr = arr || [];
			if (dojo.exists("lconn.core.WidgetPlacement")) {
				for (var i = 0; i < arr.length; i++) {
					try {
						lconn.core.WidgetPlacement.refresh(arr[i], null, false);
					} catch (e1) {}
				}
			}
		},

		_loadActionBar: function _loadActionBar_$6(idx) {
				
			//if we can unfollow, then we can't follow.  And vice versa.
			if (this.connection.canUnfollow) this.connection.canFollow = false;
			if (this.connection.canFollow) this.connection.canUnfollow = false;
			
			//set the bits according to out current status
			if (this.connection.status === this.connection.TYPES.PENDING || this.connection.status === this.connection.TYPES.INVITED) {
				this.connection.canFriend = false;
				this.connection.canUnfriend = false;
			} else if (this.connection.status === this.connection.TYPES.CONNECTED) {
				this.connection.canFriend = false;
				this.connection.canUnfriend = true;
			} else {
				this.connection.canFriend = true;
				this.connection.canUnfriend = false;			
			}
			
			//if this is my own profile, we can't do anything.
			if (this.isMyProfile) {
				this.connection.canFriend = false;
				this.connection.canUnfriend = false;
				this.connection.canFollow = false;
				this.connection.canUnfollow = false;
			}
				
			//find the actionBarNode...
			dojo.query("#businessCardActions>.lotusActionBar").some(dojo.hitch(this, function(node) {
				this.actionBarNode = node;
				return node;
			}));				

			
			if (this.actionBarNode) {

				var tempNode = dojo.create("span");
				dojo.place(tempNode, this.actionBarNode, "first");
				
				var abArgs = {};
				if (idx > -1) {
					abArgs.focusIdx = idx;
				}
				
				if (_actionBar) {
					_actionBar.reset();
					if (abArgs.focusIdx) _actionBar.focusIdx = abArgs.focusIdx;
				} else {
					_actionBar = new lconn.profiles.actionBar.ActionBar(abArgs, tempNode);
				}
			
	
				var storeArgs = {
					profile: this.profile,
					connection: this.connection,
					strings: this.strings,
					isLoggedIn: this.isLoggedIn,
					enabledPermissions: this.enabledPermissions,
					appContext: this.appContext,
					isMyProfile: this.isMyProfile,
					caller: this
				};


		if (isDebug1) {
			var userInfo = lconn.core.auth.getUser();

			console.debug("userInfo.id: " + userInfo.id); 
			console.debug("userInfo._native.tenantKey: " + userInfo._native.tenantKey); 

			console.debug("profilesData.loggedInUser.userid: " + profilesData.loggedInUser.userid); 
			console.debug("profilesData.loggedInUser.tenantKey: " + profilesData.loggedInUser.tenantKey); 
		}

		var that = this;
		
		lconn.core.util.services.isEnabled("files", profilesData.loggedInUser.userid,  profilesData.loggedInUser.tenantKey).then(function(enabled) {
			storeArgs.enabledFiles = enabled;

			if(isDebug1) {
				console.debug("storeArgs.enabledFiles: " + storeArgs.enabledFiles);
			}

			//add the core actions for the main profile page, loading the contacts store as the callback
				_actionBar.addStore(
					new lconn.profiles.actionBar.store.MainPageStore(dojo.clone(storeArgs)),
					dojo.hitch(that, that.loadSupplementaleStores, dojo.clone(storeArgs))
				);
		});
				
			}

		},
		
		loadSupplementaleStores: function loadSupplementaleStores_$7(storeArgs) {

			var loadExtenstionsStore_ = dojo.hitch(this, function() {
				_actionBar.addStore(
					new lconn.profiles.actionBar.store.OrgExtensionStore(dojo.clone(storeArgs))
				);						
			});
			
			//load the contacts store, loading the extensions store as the callback
			_actionBar.addStore(
				new lconn.profiles.actionBar.store.SocialContactsStore(dojo.clone(storeArgs)),
				loadExtenstionsStore_
			);

		},	
		
		_loadSametimeStatus: function _loadSametimeStatus_$8() {
			try {
				if (dojo.byId("awarenessArea")) { //only show sametime status if the dom is there...
					var pc = lconn.profiles.ProfilesCore;
					if (pc.isSametimeEnabled()) { // use awareness defined in profiles-config							
						profilesData.displayedUser.loadAwarenessInto = "IMcontent";
						lconn.profiles.sametime.sametimeAwareness.initIMService(sametimeAwarenessConfig);
						lconn.profiles.sametime.sametimeAwareness.loadProfilesIMStatus(applicationContext, profilesData.displayedUser);
						dojo.addOnLoad(function(){
							lconn.core.utilities.show("awarenessArea"); 
						});
						
					} else if (pc.isSametimeProxyEnabled() || pc.isSametimeCloudProxyEnabled()) {
					
						if (!dojo.exists("profilesData.loggedInUser") || !profilesData.loggedInUser.isLoggedIn) {
							dojo.addOnLoad(dojo.hitch(this, function(){ 
								var el = dojo.byId("IMcontent");
								if (el) {
									lconn.core.utilities.show("awarenessArea");
									el.innerHTML = this.strings["label.profile.im.signin"];
								}
							}));
							
						} else {
							dojo.addOnLoad(function(){ 
								lconn.core.utilities.show("awarenessArea"); 
							});
							setTimeout(dojo.hitch(this, function(){
								if(!dojo.hasClass(dojo.byId("StatusIMAwarenessDisplayedUser"),"hasSTStatus"))
									dojo.byId("IMcontent").innerHTML = this.strings.noStatuAvailable;
							}), 15000);

							if (pc.isSametimeCloudProxyEnabled()) {
								pc.sametimeCloudProxyImpl.scanPage();
							}
						}
					}
				}
			} catch (e) {
				if (console) {
					console.warn("Unabled to load sametime status.", e);
				}
			}
		},
 
		
		
		onError: function onError_$9(err) {
			if (console) { //TODO - better error handling?
				console.error(err);
			}
		}
		
		
			
	};
	
	
})();
