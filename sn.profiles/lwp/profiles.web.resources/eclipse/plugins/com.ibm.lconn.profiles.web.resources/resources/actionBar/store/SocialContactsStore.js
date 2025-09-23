/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.actionBar.store.SocialContactsStore");

dojo.require("lconn.profiles.actionBar.store.BaseStore");

dojo.declare(
	"lconn.profiles.actionBar.store.SocialContactsStore", 
	[lconn.profiles.actionBar.store.BaseStore],
	{
		
		_searchUrl: null, //social contacts search API
		_homeUrl: null, //social contacts Home application
		_viewUrl: null, //social contacts View contact
		_addUrl: null, //social contacts Add contact
		
		_entryId: null, //if the contact is found, this holds the id
		
		_retrieveContactId: function(callback) {

			// build the url to the social contacts api to get the user					
			var url = this._searchUrl.toString();
			if (url.indexOf("count=") == -1) {
				url += "&count={count}";
			}
			if (url.indexOf("fields=") == -1) {
				url += "&fields={fields}";
			}
			
			var args = {
				"count": "1",
				"fields": "id",
				"userId": encodeURIComponent(this.profile.userid)
			};
			
			this._entryId = null;
			
			lconn.profiles.xhrGet({
				url: dojo.replace(url, args),
				handleAs: "json",
				load: dojo.hitch(this, function(data) {
					if (data && data.entry && data.entry[0] && data.entry[0].id && data.entry[0].id !== "") { //found a contact record
						this._entryId = data.entry[0].id;
					}
					dojo.hitch(this, callback)();
				}),
				error: dojo.hitch(this, function(err) {
					this.onError(err);
					dojo.hitch(this, callback)();
				}),
				sync: false
			});			
		},
		
		loadData: function(callback) {
			if (dojo.exists("profilesData.config") && !profilesData.config.isLotusLive) {
				callback();
				return;
			}

			if (this.isLoggedIn && !this.isMyProfile && this.checkAcl("profile.search$profile.search.contacts.view") && dojo.exists("gllConnectionsData.srvUrls")) {

				this._homeUrl = gllConnectionsData.srvUrls["sc-contacts-home"];
				this._viewUrl = gllConnectionsData.srvUrls["sc-contacts-view"];
				this._addUrl = gllConnectionsData.srvUrls["sc-contacts-create"] || this._homeUrl + "#/contact/new?userId={userId}&orgId={orgId}&email={email}&name={name}";
				
				var apiBaseUrl = gllConnectionsData.srvUrls["sc-contacts-search"];
				if (apiBaseUrl) {
					apiBaseUrl = apiBaseUrl.toString().split("?")[0];
					this._searchUrl = apiBaseUrl + "?connectToId={userId}";
				}

				
				if (this._searchUrl && this._homeUrl && this._viewUrl && this._addUrl) {
					
					this._retrieveContactId(dojo.hitch(this, function() {
						if (this._entryId) {
							this.addDataItem({
								id: this.ACTION_ID_PREFIX + "personCardViewContactRecord",
								menu_text: this.strings["personCardViewContactRecord"],
								onClick: dojo.hitch(this, this._viewContact)
							});	
						} else {
							this.addDataItem({
								id: this.ACTION_ID_PREFIX + "personCardAddContactRecord",
								menu_text: this.strings["personCardAddContactRecord"],
								onClick: dojo.hitch(this, this._addContact)
							});
						}
						
						callback();
					}));
					
				} else {
					callback();					
				}				
		
			} else {
				callback();
			}
			
		},
		
		
		_viewContact: function() {
			if (this._viewUrl && this._entryId) {
				// build the url to the social contacts api to get the user			
				location.href = dojo.replace(
					this._viewUrl.toString(),
					{
						"contactId": encodeURIComponent(this._entryId)
					}
				);
			}
		},
				
		
		_addContact: function() {
			if (this._addUrl && !this._entryId) {
				if (!this.profile.email) {
					/*NEEDS TO BE REWRITTEN TO BE A BACKEND S2S REQUEST.  THIS IS MAKING USE OF A JAVLIN EMAIL ADDRESS*/
					try {
						if (dojo.exists("gllConnectionsData.srvUrls.contacts")) {
							lconn.profiles.xhrGet({
								url: gllConnectionsData.srvUrls.contacts.toString() + "/javlin/get?uid=" + this.profile.userid + "&token=" + dojo.cookie("token"),
								handleAs: "text",
								load: dojo.hitch(this, function(data) {
									try {
										/*HACKHACKHACK.  I feel icky.*/
										if (typeof window.SemTagPerson == "undefined") window.SemTagPerson = {};
										var data = dojo.trim(((data.split("<body>")[1]).split("</body>")[0]));
										dojo.eval(data);
										this.profile.email = SemTagPerson.tmp.email.internet;
										if (this.profile.email) {
											dojo.hitch(this, this._addContact)();
										} else {
											this.onError({});
										}
									} catch (e) {
										this.onError(e);
									}
								}),
								error: dojo.hitch(this, function(err) {
									this.onError(err);
								}),
								sync: false
							});
						} else {
							this.showMessage("errorDefaultMsg2", [this.profile], null, "error");
							this.reloadActionBar({});						
						}
					} catch (ee) {
						this.onError(err);
					}
					
				} else {
					// build the url to the social contacts ui to create the user			
					location.href = dojo.replace(
						this._addUrl.toString(),
						{
							"userId": encodeURIComponent(this.profile.userid),
							"name": encodeURIComponent(this.profile.displayName),
							"email": encodeURIComponent(this.profile.email),
							"orgId": encodeURIComponent(this.profile.tenantKey)
						}
					);

				}
			}		
		}
		
	}
);
