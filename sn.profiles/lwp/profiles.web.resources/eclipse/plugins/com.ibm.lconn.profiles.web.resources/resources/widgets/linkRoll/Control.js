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

/**
 * dijit that uses the Profiles linkroll api to view/add/remove links
 * to websites
 */
 
dojo.provide("lconn.profiles.widgets.linkRoll.Control");


dojo.require("dojox.xml.DomParser");
dojo.require("lconn.profiles.widgets._BaseData");
dojo.require("lconn.profiles.widgets.linkRoll.Item");

(function() {

	dojo.declare(
		"lconn.profiles.widgets.linkRoll.Control",
		[lconn.profiles.widgets._BaseData],
		{
			templatePath: dojo.moduleUrl("lconn.profiles", "widgets/linkRoll/templates/Control.html"),
			
			viewUrl: "{profilesSvcLocation}/atom2/forms/linkroll.xml?userKey={userKey}",
			addUrl: "{profilesSvcLocation}/atom2/forms/linkroll.xml?lastMod={lastMod}",
			removeUrl: "{profilesSvcLocation}/atom2/forms/linkroll.xml?action=delete&lastMod={lastMod}",
						
			canAddLinks: false,
			canRemoveLinks: false,			
			
			//this is the content used to PUT to linkroll api for add/delete of entries
			xmlPutTemplate: "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
					"<linkroll xmlns=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\" xmlns:snx=\"http://www.ibm.com/xmlns/prod/sn/profiles/ext/profile-links\">" +
						"<link name=\"{linkName}\" url=\"{linkUrl}\"/>" +
					"</linkroll>"
			,
			
			_data: [],
			
			postMixInProperties: function() {

				this.inherited(arguments);
				
				//TODO - add new permission to acl to check for this.  For now, we just check for the current user
				if (dojo.exists("profilesData.loggedInUser.loggedInUserKey")) {
					this.canAddLinks = this.canRemoveLinks = (this.userKey == profilesData.loggedInUser.loggedInUserKey);
				}
				
			},
			
			postCreate: function() {

				this.inherited(arguments);
				
				//show the "Add Link" button if we are allowed
				if (this.canAddLinks) {
					dojo.removeClass(this.addNode, this.hiddenClassName);
				}				
				
				//automated testing looks for these ids
				if (dojo.query("a[id='addLinkLinkRollButton']").length == 0) {
					dojo.attr(this.addLinkNode, {
						"id": "addLinkLinkRollButton"
					});
				}
				
				if (dojo.query("input[id='name']").length == 0) {
					dojo.attr(this.addLinkNameNode, {
						"id": "name"
					});
					dojo.attr(this.addLinkNameLabelNode, {
						"for": "name"
					});					
				}
				
				if (dojo.query("input[id='url']").length == 0) {
					dojo.attr(this.addLinkUrlNode, {
						"id": "url"
					});
					dojo.attr(this.addLinkUrlLabelNode, {
						"for": "url"
					});					
				}

				this.getLinks();
							
			},
			
			resetLinkData: function() {
				this._data = [];
			},
			
			addLinkData: function(name, url) {
				this._data.push({
					"name": name,
					"url": this._verifyUrlProtocol(url)
				});
			},
			
			isValidLink: function(linkName, linkUrl) {
				var isValid = true;
				
				if (this._data && this._data.length) {
					
					//we found existing links.  Check for dupes
					dojo.forEach(this._data, dojo.hitch(this, function(item) {
						if (dojo.trim(item["name"]) == dojo.trim(linkName)) {
							isValid = false;
							this.showMessage(this.strings.linkRollDuplicateName, "error");
						} else
						if (item["url"].toLowerCase() == this._verifyUrlProtocol(linkUrl).toLowerCase()) {
							isValid = 	false;
							this.showMessage(this.strings.linkRollDuplicateUrl, "error");							
						}
					}));
				}
				return isValid;
			},
			
			//makes the backend request to get the data
			getLinks: function() {
				if (!this.userKey) {
					this.showMessage("userKey cannot be null", "error");
					return;
				}
				
				//show the loading div
				this.showLoading(true);
				
				//load the data
				this.xhr("GET", {
					sync: false, 
					url: dojo.replace(this.viewUrl, this), 
					handleAs: "text", 
					expectedContentType: "xml",
					checkAuthHeader: true,
					load: dojo.hitch(this, function(response, ioArgs) {
						try {
							this.resetLinkData();
							//we are going to pull out the links from the respinding xml
							var arrData = dojox.xml.DomParser.parse(response).childrenByName("linkroll")[0].childrenByName("link");
							dojo.forEach(arrData, dojo.hitch(this, function(item) {
								this.addLinkData(item.getAttribute("name"), item.getAttribute("url"));
							}));
							
							
						} catch (e) {
							this.resetLinkData();
						}
						this.renderLinks();
					}),
					error: dojo.hitch(this, function(response, ioArgs) {
						if( ioArgs.xhr.status == 404 ) {
							this.resetLinkData();
						} else { 
							this.onError(response, ioArgs);
						}
						this.renderLinks();
					})
				});				
			},
			
			_links: [],
			
			_verifyUrlProtocol: function(url) {
				if (url && !url.match(/^[a-zA-Z0-9+.-]+:\/\//) && url.toLowerCase().indexOf("file:/") != 0 && url.toLowerCase().indexOf("mailto:") != 0) {
					return "http://" + url;
				} else {
					return url;
				}
			},
			
			//render whatever links are stored in the _data object
			renderLinks: function() {

				//destroy any existing links
				dojo.forEach(this._links, dojo.destroy); 
				this.linksNode.innerHTML = "";
				this._links.length = 0;
				
				
				if (this._data && this._data.length) {
					//we found links.  Go ahead and render them
					dojo.forEach(this._data, dojo.hitch(this, function(item) {
					
						var node = dojo.create("div");
						dojo.place(node, this.linksNode, "last");
												
						var props = {
							linkName: item["name"],
							linkUrl: item["url"],
							strings: this.strings,
							containerWidget: this
						};
						
						this._links.push(new lconn.profiles.widgets.linkRoll.Item(props, node));
					}));
					
					dojo.removeClass(this.linksNode, this.hiddenClassName);
					dojo.addClass(this.noLinksNode, this.hiddenClassName);
					
				} else {
					//no data, just show the "no links" message.
					dojo.addClass(this.linksNode, this.hiddenClassName);
					dojo.removeClass(this.noLinksNode, this.hiddenClassName);
					
				}
				
				//hide the loading div
				this.showLoading(false);
				
				
				this.enforceBidi();
				
				if(ui && ui._check_ui_enabled()) {
					dojo.query(".cnx8LinkNodes").style("margin", "0px");		
				}				
			},
			
			saveAddLink: function() {
				if (!this.canAddLinks) return false;
				
				var nameField = this.addLinkNameNode;
				var urlField = this.addLinkUrlNode;

				var invFields = [];
				
				dojo.forEach([nameField, urlField], function(f) {
					if (f && typeof f.value != "undefined" && (f.value == null || f.value == "")) {
						dojo.attr(f, "aria-invalid", "true");
						invFields.push(f);
					} else {
						dojo.removeAttr(f, "aria-invalid");
					}
				});
				
				if (invFields.length > 0) {
					try {
						invFields[0].focus();
					} catch (ee) {}

					this.showMessage(this.strings.linkRollNameOrLinkCannotBeEmpty, "error");

					return false;
				}
				
				try {
					lconn.core.globalization.bidiUtil.stripSpecialCharacters(this.addLinkFormNode);
				} catch (e) {}
				
				var linkName = dojo.trim(nameField.value).replace(/%22/g,"\"");
				var linkUrl = this._verifyUrlProtocol(urlField.value).replace(/%22/g,"\"");

				if (!this.isValidLink(linkName, linkUrl)) {
					return false;
				}


				//build this object to be used to replace the values in the xml put data
				var linkObj = {
					"linkName": xmlEncode_(linkName),
					"linkUrl": xmlEncode_(plusEncode_(encodeURI(linkUrl)))
				};
				
				this.xhr("PUT", {
					url: dojo.replace(this.addUrl, this),
					putData: dojo.replace(this.xmlPutTemplate, linkObj),
					checkAuthHeader: true,
					load: dojo.hitch(this, function(response, ioArgs) {

						if (response === "" || ioArgs.xhr.status !== 200) {
							this.showMessage(this.strings.errorDefaultMsg2, "error");
							
						} else {
							this.lastMod = profilesData.config.profileLastMod = new Date().getTime();
							this.showMessage(this.strings.linkRollLinkAdded);
							this.showAddForm(false);
							this.getLinks();
						}					

					}),
					error: dojo.hitch(this, function(response, ioArgs) {
						this.onError(response, ioArgs);
					})
				});
				
				return false;
			},
			
			cancelAddLink: function() {
				this.showAddForm(false);
			},
			
			removeLink: function(linkRollItem) {
				if (!this.canRemoveLinks) return;
				
				//build this object to be used to replace the values in the xml put data
				var linkObj = {
					"linkName": xmlEncode_(linkRollItem.linkName),
					"linkUrl": xmlEncode_(plusEncode_(encodeURI(linkRollItem.linkUrl)))
				};
				
				this.xhr("PUT", {
					url: dojo.replace(this.removeUrl, this),
					putData: dojo.replace(this.xmlPutTemplate, linkObj),
					checkAuthHeader: true,
					load: dojo.hitch(this, function(response, ioArgs) {
					
						if (response === "" || ioArgs.xhr.status !== 200) {
							this.showMessage(this.strings.errorDefaultMsg2, "error");
							
						} else {
							this.lastMod = profilesData.config.profileLastMod = new Date().getTime();
							this.showMessage(this.strings.linkRollLinkRemoved);
							this.getLinks();
							try {
								this.addLinkNode.focus();
							} catch (e) {}	
						}
					
					}),
					error: dojo.hitch(this, function(response, ioArgs) {
						this.onError(response, ioArgs);
					})
				});
			

			},
			
			showAddForm: function(yn) {
				yn = !!yn;
				
				if (yn) {
		
					this.addLinkNameNode.value = "";
					this.addLinkUrlNode.value = "";
					
					dojo.removeClass(this.addLinkFormNode, this.hiddenClassName);
					
					setTimeout(dojo.hitch(this, function() {
						this.addLinkNameNode.focus();
					}),0);
				} else {
					dojo.addClass(this.addLinkFormNode, this.hiddenClassName);
					
					try {
						this.addLinkNode.focus();
					} catch (e) {}
				}
	
			}
		}
	);

	//shortcut functions
	var xmlEncode_ = lconn.profiles.widgets.utils.XmlEncoder.encode;
	var plusEncode_ = lconn.profiles.widgets.utils.PlusEncoder.encode;
	
})();
