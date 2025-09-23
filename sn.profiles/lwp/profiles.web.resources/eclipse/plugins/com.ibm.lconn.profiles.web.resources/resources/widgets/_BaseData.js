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

dojo.provide("lconn.profiles.widgets._BaseData");

dojo.require("lconn.core.auth");
dojo.require("lconn.profiles.widgets._Base");

(function() {

	dojo.declare(
		"lconn.profiles.widgets._BaseData",
		[lconn.profiles.widgets._Base],
		{
			userKey: null,
			lastMod: null,
			
			postMixInProperties: function() {

				this.inherited(arguments);		
				
				//get the userKey and lastMod from our global object if it isn't passed in.
				if (!this.userKey && dojo.exists("profilesData.displayedUser.key")) {
					this.userKey = profilesData.displayedUser.key;
				}
				
				//try pulling it from the url
				if (!this.userKey) {
					try {
						this.userKey = dojo.queryToObject(window.location.search.substring(1))["key"];
					} catch (e) {}
				}
				
				if (!this.userKey && dojo.exists("profilesData.loggedInUser.loggedInUserKey")) {
					this.userKey = profilesData.loggedInUser.loggedInUserKey;
				}
				

				
				if (!this.lastMod) {
					if (dojo.exists("profilesData.config.profileLastMod")) {
						this.lastMod = profilesData.config.profileLastMod;
					} else {
						this.lastMod = new Date().getTime();
					}
				}

								
			},
			
			postCreate: function() {

				this.inherited(arguments);
				
			},
			
			xhr: function(method, args) {
				if (dojo.getObject("com.ibm.ajax.auth")) {
					com.ibm.ajax.auth.prepareSecure(args);
				}
				return dojo.xhr(method, args);			
			}
			
		}
	);
	
})();
