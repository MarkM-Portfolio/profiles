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

dojo.provide("lconn.profiles.actionBar.store.BaseStore");


dojo.require("dojo.data.ItemFileReadStore");


dojo.declare(
	"lconn.profiles.actionBar.store.BaseStore", 
	[dojo.data.ItemFileReadStore],
	{
		profile: {},
		connection: {},
		strings: {},
		
		caller: null,
		
		isLoggedIn: false,
		isMyProfile: false,
		
		enabledPermissions: [],
		
		dataObj: {
			label: "menu_text",
			identifier: "id",
			items: []
		},
		
		appContext: "/profiles",
		
		ACTION_ID_PREFIX: "actn__",
		
		constructor: function(args) {
			dojo.safeMixin(this,args);
		},
		
		checkAcl: function checkAcl(permission) {
			try {
				for (var i = 0; i < this.enabledPermissions.length; i++) {
					if (this.enabledPermissions[i] == permission) {
						return true;
					}
				}
			} catch (e) {
				if (console) {
					console.error("Unable to read permission information for this user.");
					console.info(["lconn.profiles.actionBar.store.BaseStore.enabledPermissions=", this.enabledPermissions]);
					console.info(["profilesData.enabledPermissions=", profilesData.enabledPermissions]);
				}
				this.onError(e);
			}
			return false;
		},
		
		//override
		loadData: function(callback) {
			callback();
		},
		
		fetch: function() {
			if (!this.data) {
				this.data = dojo.clone(this.dataObj);
			}
			this.inherited(arguments);
		},
		
		addDataItem: function(item) {
			if (!this.data) {
				this.data = dojo.clone(this.dataObj);
			}
			if (!this.data.items) {
				this.data.items = [];
			}
			
			if (!item.id || item.id == "") {
				item.id = "_" + Math.random();
			}
			
			this.data.items.push(item);
		},
		
		reloadActionBar: function(args) {
			if (this.caller && typeof this.caller.reloadActionBar === "function") {
				return this.caller.reloadActionBar(args);
			}
		},

		//refresh the specified iwidgets on the page
		refreshWidgets: function(args) {
			if (this.caller && typeof this.caller.refreshWidgets == "function") {
				return this.caller.refreshWidgets(args);
			}
		},
		
		showMessage: function(key, args, node, typ) {
			if (this.caller && typeof this.caller.showMessage == "function") {
				return this.caller.showMessage(key, args, node, typ);
			}
		},
 
		onError: function(err) {
			if (console) { //TODO - better error handling?
				console.error(err);
			}
		}
	}
);