/* *************************************************************** */
/*                                                                 */
/* HCL Confidential                                                */
/*                                                                 */
/* OCO Source Materials                                            */
/*                                                                 */
/* Copyright HCL Technologies Limited 2014, 2022                   */
/*                                                                 */
/* The source code for this program is not published or otherwise  */
/* divested of its trade secrets, irrespective of what has been    */
/* deposited with the U.S. Copyright Office.                       */
/*                                                                 */
/* *************************************************************** */

dojo.provide("lconn.profiles.directory.DirectoryController");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dojo.hash");
dojo.require("dojox.encoding.digests.MD5");
dojo.require("lconn.core.widgetUtils");

dojo.require("lconn.profiles.directory.ResultsWidget");
dojo.require("lconn.profiles.directory.DataReader");
dojo.require("lconn.profiles.directory.SearchBoxWidget");
dojo.require("lconn.profiles.directory.Localization");

dojo.declare(
    "lconn.profiles.directory.DirectoryController",
    [dijit._Widget, dijit._Templated, lconn.profiles.directory.Localization],
{
	templatePath: dojo.moduleUrl("lconn.profiles", "directory/templates/DirectoryController.html"),
	widgetsInTemplate: true,
	
	dataReader: null,
	_queryRunning: false,
	_queryTimeout: 150,
	advancedSeachSection: null,
	footer: null,
	blankGif: "",
	cookieName: "lconn.profiles.directory",
	scrollHandler: null,
	allowAdvancedSearch: true,
	
	constructor: function() {
		this.inherited(arguments);
		
		this.blankGif = lconn.core.widgetUtils.addVersionNumber(djConfig.blankGif);
	},
	
	postCreate: function() {
		this.advancedSeachSection = dojo.byId("divProfilesAdvancedSearch");
		this.containerNode.removeChild(this.advancedSeachSection);
		this.content.appendChild(this.advancedSeachSection);
		if(window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()) {
			dojo.style(this.content, "border", "none");
		}
		lconn.profiles.profilesSearchPage.showSimpleUI = dojo.hitch(this, "showSimpleUI");
		dojo.addOnUnload(dojo.hitch(this, "updateCookie"));
		
		this.inherited(arguments);
	},
	
	startup: function() {
		this.inherited(arguments);
		
		this.dataReader = new lconn.profiles.directory.DataReader();
		this.dataReader.startup();
		
		this.connect(window, "onscroll", "_checkTopBarFixed");
		dojo.subscribe("/dojo/hashchange", this, "hashChanged");
		
		this.searchResultsNode.setCountNodes(this.resultCountNode, this.totalResults, this.allResults);
		this.searchResultsNode.setTopFixedElement(this.searchNode);

		var queryParam = this._splitHash(dojo.hash()).q;
		if(queryParam) {
			this.searchTextNode.searchBox.setValue(queryParam);
		}

		var showAdvanced = false; //special flag that'll determine whether we show the advanced search ui.
		var advancedSearchString = null;  //variable to be populated and passed into the showAdvancedUI method
		
		if(this.allowAdvancedSearch) {
			//First, we need to check for the #advancedSearch.  This is what THIS control uses to 
			//show/hide the adv ui
			showAdvanced = (typeof this._splitHash(dojo.hash()).advancedSearch != "undefined");
		
			if(!showAdvanced) {
				//Next, we need to check for a specially crafted url: ?showfulladv=true&displayName=xxx
				//This url is crafted in the Profiles' legacy Directory search page when there are more 
				//than 250 results
				var uri = window.location.href;
				if (uri.indexOf("?")) {
					var queryObject = dojo.queryToObject(uri.substring(uri.indexOf("?") + 1));
					if (queryObject["showfulladv"] == "true") {
						showAdvanced = true;
						advancedSearchString = queryObject["displayName"] || "";
					}
				}
			}
		}
		
		if(showAdvanced) {
			this.showAdvancedUI(advancedSearchString);
			return;
		} else {
			var parameters = this._splitHash(dojo.hash());
			parameters.advancedSearch = undefined;
			parameters.simpleSearch = "";
			dojo.hash(this._writeHash(parameters), true);
		}
		
		var scroll = this.getCookieScroll();
		if(scroll > 0) {
			this.scrollHandler = this.connect(this, "_showContent", dojo.hitch(this, "_scrollTo", scroll));
		}
		
		this.searchTextNode.searchBox.focus();
		
		if(queryParam) {
			this.searchTextNode.keyPress();
		}
		
		if (!this.allowAdvancedSearch) {
			dojo.addClass(this.advancedSearchLinkNode, "lotusHidden");
		}
		
	},
	
	uninitialize: function() {
		if(this.dataReader) {
			this.dataReader.destroyRecursive();
		}
		
		this.inherited(arguments);
	},
	
	_startQuery: function() {
		if(!this._queryRunning && this._checkQueryChanged()) {
			this._queryRunning = true;
			dojo.removeClass(this.loadingIcon, "lotusHidden");
			setTimeout(dojo.hitch(this, function() {
				var query = this.searchTextNode.getQuery();
				query = dojo.string.trim(query);
				this._queryRunning = false;
				this._updateHash(query);
				
				if(!query) {
					this.searchResultsNode._cleanAll();
					this.dataReader.resetLastQuery();
					this._hideContent();
					return;
				}
				
				if(query === this.dataReader.lastQuery.query) {
					dojo.addClass(this.loadingIcon, "lotusHidden");
					return;
				}
				
				this.dataReader.executeQuery(/*query*/query, /*successCallback*/dojo.hitch(this.searchResultsNode, "setResults"), /*errorCallback*/null);
			}), (this._shouldDoubleTimeout() ? (this._queryTimeout * 2) : this._queryTimeout));
		}
	},
	
	_requestNextQueryPage: function() {
		if(typeof this._splitHash(dojo.hash()).advancedSearch != "undefined") {
			return;
		}
		
		this.dataReader.getNextPage(/*successCallback*/dojo.hitch(this.searchResultsNode, "appendResults"), /*errorCallback*/null);
	},
	
	_shouldDoubleTimeout: function() {
		return (this.searchTextNode.getQuery().length <= 2 );
	},
	
	_checkQueryChanged: function() {
		var q1 = this.searchTextNode.getQuery();
		var q2 = this.dataReader.lastQuery.query;
		
		return !(q1 === q2);
	},
	
	_tagClicked: function(tag) {
		if(tag) {
			this.searchTextNode.searchBox.setValue(tag);
			this.searchTextNode.keyPress();
		}
	},
	
	_showContent: function() {
		dojo.addClass(this.loadingIcon, "lotusHidden");
		dojo.removeClass(this.content, "lotusHidden");
	},
	
	_hideContent: function() {
		dojo.addClass(this.loadingIcon, "lotusHidden");
		dojo.addClass(this.content, "lotusHidden");
		dojo.addClass(this.resultCountNode, "lotusHidden");
	},
	
	_checkTopBarFixed: function() {
		if((this.domNode.offsetWidth + "px") != this.searchNode.style.maxWidth) {
			this.searchNode.style.maxWidth = this.domNode.offsetWidth + "px";
			this.searchPlaceholder.style.height = this.searchNode.offsetHeight /* padding-top of fixed class */ + 15 + "px";
		}
		
		if(typeof this._splitHash(dojo.hash()).advancedSearch != "undefined") {
			return;
		}
		
		var de = dojo.doc.documentElement;
        var scrollTop = window.pageYOffset || de.scrollTop;
		var searchNodePos = dojo.position(this.searchNode, true);
		if(!dojo.hasClass(this.searchNode, "fixed")) {
			searchNodePos.y += -15; //padding-top of fixed class;
		}
		var pos = !dojo.hasClass(this.searchPlaceholder, "lotusHidden") ? dojo.position(this.searchPlaceholder, true) : searchNodePos;
		
		if(scrollTop > pos.y) {
			dojo.removeClass(this.searchPlaceholder, "lotusHidden");
			dojo.addClass(this.searchNode, "fixed");
			dojo.addClass(this.searchNode, "scrolling");
		} else {
			dojo.addClass(this.searchPlaceholder, "lotusHidden");
			dojo.removeClass(this.searchNode, "fixed");
			dojo.removeClass(this.searchNode, "scrolling");
		}
	},
	
	showSimpleUI: function() {
		this._showContent();
		dojo.addClass(this.advancedSeachSection, "lotusHidden");
		dojo.removeClass(this.searchResultsNode.domNode, "lotusHidden");
		dojo.removeClass(this.searchNode, "lotusHidden");
		//document.location.hash = "#simpleSearch";
		var parameters = this._splitHash(dojo.hash());
		parameters.advancedSearch = undefined;
		parameters.simpleSearch = "";
		dojo.hash(this._writeHash(parameters), true);
		
		var query = dojo.byId('advancedSearchForm').displayName.value;
		if(query) {
			this.searchTextNode.searchBox.setValue(query);
			if(this.searchResultsNode.resultCount > 0) {
				dojo.removeClass(this.resultCountNode, "lotusHidden");
			}
			this.searchTextNode.keyPress();
		} else {
			this.searchTextNode.searchBox.setValue("");
			this._hideContent();
		}
		
		setTimeout(dojo.hitch(this, function() {
			this.searchTextNode.searchBox.focus();
		}), 1);
	},
	
	showAdvancedUI: function(searchVal) {
		if (!this.allowAdvancedSearch) {
			this.showSimpleUI();
			return;
		}
		
		dojo.removeClass(this.content, "lotusHidden");
		dojo.addClass(this.resultCountNode, "lotusHidden");
		dojo.addClass(this.searchResultsNode.domNode, "lotusHidden");
		dojo.addClass(this.searchNode, "lotusHidden");
		dojo.removeClass(this.advancedSeachSection, "lotusHidden");
		//document.location.hash = "#advancedSearch";
		var parameters = this._splitHash(dojo.hash());
		parameters.advancedSearch = "";
		parameters.simpleSearch = undefined;
		dojo.hash(this._writeHash(parameters), true);
		
		//if the simple search box has a value, use that to populate 
		//the advanced search field.  Otherwise, we'll use the value
		//passed into the function.
		if (this.searchTextNode.searchBox.textbox.value !== "") {
			searchVal = this.searchTextNode.searchBox.textbox.value;
		};
		if (searchVal && typeof searchVal === "string") {
			dojo.byId('advancedSearchForm').displayName.value = searchVal;
		}

		setTimeout(function() {
			dojo.byId('advancedSearchForm').keyword.focus();
		}, 1);
	},
	
	_splitHash: function(hash) {
		var params = {};
		if(!hash) {
			return params;
		}
		if(hash.charAt(0) == "#") {
			hash = query.substring(1);
		}
		
		var args = hash.split("&");
		for(var i=0; i < args.length; i++) {
			var keyValue = args[i].split("=");
			var key = decodeURIComponent(keyValue[0]);
			var existing = params[key];
			var value = "";
			
			if(keyValue.length > 1) {
				value = decodeURIComponent(keyValue[1]);
			}
			
			if (dojo.isArray(existing)) {
				existing.push(value);
			} else if (existing) {
				params[key] = [existing, value];
			} else {
				params[key] = value;
			}
		}
		return params;
	},
	
	_writeHash: function(map) {
		var out = [];
		for(var key in map) {
			var values = map[key];
			key = encodeURIComponent(key);
			if(values) {
				out.push(key + "=" + encodeURIComponent(values));
			} else if(values !== undefined) {
				out.push(key);
			}
		}
		return out.join("&");
	},
	
	_updateHash: function(query) {
		var parameters = this._splitHash(dojo.hash());
		parameters.q = query ? query : undefined;
		dojo.hash(this._writeHash(parameters), true);
	},
	
	getCookieScroll: function() {
		var scroll = 0;
		var user = lconn.core.auth.getUser();
		var id = dojox.encoding.digests.MD5(window.location.href + (user ? user.id : "anonymous"));
		var cookie = JSON.parse(dojo.cookie(this.cookieName) || null);
		
		if(cookie && cookie.id === id) {
			scroll = cookie.scrollTop;
			dojo.cookie(this.cookieName, null, {expires: -1});
		}
		
		return scroll;
	},
	
	updateCookie: function() {
		var scrollTop = dojo.isChrome ? document.body.scrollTop : document.documentElement.scrollTop;
		if(scrollTop <= 0) {
			return;
		}
		var user = lconn.core.auth.getUser();
		var cookie = {
			id: dojox.encoding.digests.MD5(window.location.href + (user ? user.id : "anonymous")),
			scrollTop: scrollTop
		}
		dojo.cookie(this.cookieName, JSON.stringify(cookie));
	},
	
	_scrollTo: function(value) {
		if(this.scrollHandler) {
			this.scrollHandler.remove();
			this.scrollHandler = null;
		}
		
		var scrollTop = dojo.isChrome ? document.body.scrollTop : document.documentElement.scrollTop;
		if(scrollTop < value) {
			window.scrollTo(0, value);
			setTimeout(dojo.hitch(this, "_scrollTo", value), 1);
		}
	},
	
	hashChanged: function(newHash) {
		var queryParm = this._splitHash(newHash).q;
		var currentQuery = dojo.string.trim(this.searchTextNode.getQuery());	
		//changes for PRB0043117 if no values from query default them to empty string
		queryParm = (typeof queryParm === "undefined") ? "" : queryParm;
		queryParam = dojo.string.trim(queryParm);

		if(queryParam != currentQuery) {
			this.searchTextNode.searchBox.setValue(queryParam);
			this.searchTextNode.keyPress();
		}
	}
});
