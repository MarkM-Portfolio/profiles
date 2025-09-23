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

dojo.provide("lconn.profiles.widgets._Base");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("lconn.core.globalization.bidiUtil");

dojo.requireLocalization("lconn.profiles", "ui");

(function() {

	dojo.declare(
		"lconn.profiles.widgets._Base",
		[dijit._Widget, dijit._Templated],
		{
			templatePath: null,
			
			profilesSvcLocation: "/profiles",
						
			widgetId: null,
			
			_data: null,
			
			blankGif: this._blankGif || dojo.config.blankGif || dijit._Widget.prototype._blankGif,
			strings: {},
			
			hiddenClassName: "lotusHidden",
			
			postMixInProperties: function() {

				this.inherited(arguments);
				
				//load our strings...
				var defaultStrings = dojo.i18n.getLocalization("lconn.profiles", "ui");
				if (typeof defaultStrings !== "undefined") {
					this.strings = dojo.mixin(
						defaultStrings,
						this.strings
					);
				} else {
					this.strings = dojo.mixin(
						window.generalrs || {}, //deprecated, but if all else fails, try to use it.
						this.strings
					);			
				}
				
				//any strings with a period in the name need to be replaced with underscores so 
				//the dojo templated engine can read them
				for (str in dojo.clone(this.strings)) {
					if (str.indexOf(".") > -1) {
						this.strings[str.replace(/\./g, "_")] = this.strings[str];
					}
				}				
				
				//map to the profiles service
				if (dojo.exists("lconn.core.url") && dojo.exists("lconn.core.config.services.profiles")) {
					this.profilesSvcLocation = lconn.core.url.getServiceUrl(lconn.core.config.services.profiles);
				}
								
			},
			
			postCreate: function() {

				this.inherited(arguments);
				
				this.enforceBidi();
				
			},
			
			//method shows/hides the loading div
			showLoading: function(yn) {
				if (typeof yn == "undefined") yn = true;
				//show/hide the loading div
				if (this.loadingNode) this.showNode(this.loadingNode, yn);
				if (this.nonLoadingNode) this.showNode(this.nonLoadingNode, !yn);

			},
			
			enforceBidi: function(node) {
				if (typeof node === "string") node = dojo.byId(node);
				if (!node) node = this.domNode;				
				
				lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(node);
			},
			
			showNode: function(node, yn) {
				if (typeof yn == "undefined") yn = true;
				dojo[((yn) ? "removeClass" :"addClass")](node, this.hiddenClassName);
			},
						
			showMessage: function(msg, stype) {
				if (!stype) stype = "confirmation";
				var el = dojo.byId("profileInfoMsgDiv");
				if (el && dojo.exists("lconn.profiles.ProfilesCore.showInfoMsg")) {
					lconn.profiles.ProfilesCore.showInfoMsg(el, stype, msg);
				} else {
					if (console && console[stype]) {
						console[stype](msg);
					}
				}
			},
			
			showWaitCursor: function(yn) {
				if (typeof yn == "undefined") yn = true;
				dojo.style(dojo.body(), "cursor", ((yn) ? "wait" :"default"));
			},
			
			onError: function(response, ioArgs) {
				this.showMessage(this.strings["errorDefaultMsg"], "error");
				if (console) {
					console.error(response, ioArgs);
				}

			},
			
			_connects: []
		}
	);
	
})();
