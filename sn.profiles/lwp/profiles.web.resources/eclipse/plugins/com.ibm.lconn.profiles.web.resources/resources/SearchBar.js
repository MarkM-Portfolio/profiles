/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * Extends lconn.core.SearchBar
 */

dojo.provide("lconn.profiles.SearchBar");

dojo.require("lconn.core.SearchBar");

dojo.requireLocalization("lconn.profiles", "ui");

dojo.declare(
    "lconn.profiles.SearchBar",
    [lconn.core.SearchBar],
    {
		
		//lconn.core.3earchBar looks for these
		thirdPartySearchEngines: [],
		globalOptions: [{}],
		strings: {},
		'class': 'lotusAlignLeft',
		
		queryValue: "",
		showIcons: true,
		
		_isLoading: false, // flag to stop the immediateAction option if that option is the default
		allowForceFocus: true, //flag to tell the control to not take control of the focus on loading for immediateAction options
		
		
		_forceFocusParam: "forceSearchBarFocus",
		
		// the base option with which the localOptions are mixed in.
		_baseOption: {
			label: '',
			scope: '',
			allowTypeahead: true,
			action: '',
			method: 'GET',
			immediateAction: false,
			valueSearchParam: 'searchFor',
			searchParams: {
				'searchBy': 'name',
				'searchFor' : ''
			},
			valueReplaceParam: null,
			defaultOption: false,
			'class': 'lotusAlignLeft',
			iconClass: 'baseIcon lconnSprite lconnSprite-iconProfiles16'

		},
		
		
		selectOption: function(item, focusTextbox) {  // we need to extend the selectOption to handle the immediateAction
			this.inherited(arguments);
			
			// if we're not in the process of loading and the selected option is flagged, go immediately to the action
			if (!this._isLoading && item && item.immediateAction) { 
				window.location.href = item.action;
			}
			return true;	
		},
		
		showDefaultOption: function() {

			if (!this.showIcons) {
				for (var i = 0; i < this.globalOptions.length; i++) {
					this.globalOptions[i].iconClass = ' ';
				}
			}
			
			this.inherited(arguments);
		},
		

		postMixInProperties: function() {
			this._isLoading = true; // flag us as loading...
			
			this.thirdPartySearchEngines = window.lconn_core_thirdPartySearchEngines || [],
			
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
					generalrs || {},
					this.strings
				);			
			}			
			

			var currLoc = (window.location.href + "?").split("?")[0];

			var defaultSet_ = -1;
			var currentDefault_ = 0;
			for (var i = 0; i < this.localOptions.length; i++) {
				// mixin the option with the _baseOption to make sure we have all the necessary info
				this.localOptions[i] = dojo.mixin(dojo.clone(this._baseOption), this.localOptions[i]);
				
				//if there are options in the query string, put them into the searchParams
				var searchQuery = (this.localOptions[i].action + "?").split("?")[1];
				var actionLoc = (this.localOptions[i].action + "?").split("?")[0];
				
				if (searchQuery.length > 0) {
					this.localOptions[i].searchParams = dojo.queryToObject(searchQuery);
					this.localOptions[i].action = actionLoc;
				}
				
				if (!this.showIcons) {
					this.localOptions[i].iconClass = ' ';
				}
				
				
				
				// if it's allowed and this is an immediateAction option, append 
				// on the parameter to set the focus back to this control when the
				// page reloads
				if (this.allowForceFocus && this.localOptions[i].immediateAction) {
					var queryObj = dojo.queryToObject((this.localOptions[i].action + "?").split("?")[1]);
					if (!queryObj[this._forceFocusParam]) {
						queryObj[this._forceFocusParam] = this.id;
						this.localOptions[i].action = actionLoc + "?" + dojo.objectToQuery(queryObj);
					}
				}
				
				// This code will inspect the action url and attempt to figure out
				// which option is the defaultOption based on the current url				
				if (defaultSet_ == -1 && currLoc.indexOf(actionLoc) > -1) {
					defaultSet_ = i;
				}
				
				if (this.localOptions[i].defaultOption == true) {
					currentDefault_ = i;
				}
			}

			this.localOptions[((defaultSet_ == -1)?currentDefault_:defaultSet_)].defaultOption = true;

			
			if (!this.showIcons) {
				for (x in this.featureIcons) {
					if (this.featureIcons[x]) {
						this.featureIcons[x] = " ";
					}
				}
			}
			
		},
		
		postCreate: function() {
			this.inherited(arguments);
			
			if (this.queryValue) {
				this.setValue(this.queryValue);
			}
			
			// a11y.  Need to set the aria info so screen readings know what to do with it.
			setTimeout(dojo.hitch(this, function() {

				if (this.allowForceFocus) {
					var loc = window.location.search;
					var queryObj = dojo.queryToObject((loc.indexOf("?") == 0)?loc.substring(1):loc);
					if (queryObj[this._forceFocusParam] == this.id) {
						if (this.textBox && typeof this.textBox.focus == "function") {
							this.textBox.focus();
						} else {
							this.scopeNode.focus();
						}
					}
				}
				
				if (dojo.attr(this.domNode, "role") == "search") {
					dojo.attr(this.domNode, "aria-label", this.strings["label.search.profiles.profilesearch.simple.heading"]);
				}
				
			}), 0);
			
			this._isLoading = false;
			
		},
		
		onSubmit: function() {
			var str = dojo.string.trim(this.getValue());
			if (this._isLoading || !str) return false; //we're loading or the value is blank.  Get outta here.

			var selectedOption = this.selectedOption;
			
			// Perform the global search if one of the features is selected
			if (selectedOption.feature) {
				return true;
				
			} else {

				if ((selectedOption.searchParams && selectedOption.valueSearchParam) || selectedOption.valueReplaceParam) {
					var searchUrl = selectedOption.action;
					
					if (selectedOption.valueSearchParam) {
						// pull the value out of the control and put it into the url to search.
						var opt = dojo.clone(selectedOption.searchParams);
						opt[selectedOption.valueSearchParam] = str;
					}
					
					if (selectedOption.valueReplaceParam) {
						searchUrl = searchUrl.replace(new RegExp("\{" + selectedOption.valueReplaceParam + "\}", "gi"), str);
					}
					
					
					
					// if it gets here, change to simpleSearch.do, which actually performs the action
					searchUrl = searchUrl.replace("searchProfiles.do", "simpleSearch.do");
					

					if (selectedOption.method.toLowerCase() == 'post') {
						if (this._formToSubmit) {
							this._formToSubmit.parentNode.removeChild(this._formToSubmit);
							this._formToSubmit = null;
						}
						
						this._formToSubmit = dojo.create(
							"form", 
							{
								"action": searchUrl,
								"method": "post",
								'class': "lotusHidden"
							}
						);
						
						for (x in opt) {
							if (opt[x]) {
								dojo.create(
									"input",
									{
										"type": "hidden",
										"name": x,
										"value": opt[x]
									},
									this._formToSubmit,
									"last"
								);
							}
						}
						
						
						dojo.place(this._formToSubmit, this.domNode, "last");

						this._formToSubmit.submit();

					} else {
						if (selectedOption.valueSearchParam) {
							searchUrl += ((searchUrl.indexOf("?") == -1) ? "?" : "&") + dojo.objectToQuery(opt);
						}
						
						window.location.href = searchUrl;
					}
					
				} else {
					// just build a generic url for this scope
					var encodedSearchStr = encodeURIComponent(str); //SPR#JHUG83E588
					switch( selectedOption.scope ){
						case "extkeyword":
							var searchUrl = selectedOption.action + encodedSearchStr;
							window.location.replace( searchUrl );
							break;
						default:
							return true;
							break;
					}						
				}

			}   
			
			return false; //Don't submit the form
		}
    }
);
    
