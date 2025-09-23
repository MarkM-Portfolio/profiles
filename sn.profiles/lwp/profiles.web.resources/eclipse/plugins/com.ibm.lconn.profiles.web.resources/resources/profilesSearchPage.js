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

/*
 * This class handles all of the javascript interaction on the Directory
 * search page.  
 * NOTE: Most of this code was migrated directly from the jsp.
 */

dojo.provide("lconn.profiles.profilesSearchPage");

(function() {
	var _peopleTiles = null; //holder for typeahead widget
	
	lconn.profiles.profilesSearchPage = {
		// called from the profileSearch.jsp to load up the typeahead tiles
		loadSearchTilesControl: function(args) {
			
			//make sure we got a baseline for these parameters
			var baseArgs = {
				lang: "",
				lastMod: profilesData.config.profileLastMod,
				profilesSvcLocation: "/profiles",
				count: 21,
				minChars: 1,
				searchDelay: 250,
				liveNameSupport: true,
				expandThumbnails: true,
				showEmail: true,
				messages: {
					photoAltText: "",
					inactiveText: "",
					noResultsText: "",
					resultsHeadingText: ""
				}
			};

			//if we find the tiles class, use it.
			if (dojo.exists("lconn.profiles.PeopleTypeAheadTiles")) {
				_peopleTiles = new lconn.profiles.PeopleTypeAheadTiles(
					dojo.mixin(dojo.clone(baseArgs), dojo.clone(args || {})),
					dojo.byId("profilesNameSearchField")
				);
			}
			
			if (dojo.exists("lconn.core.globalization.bidiUtil")) {
				lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId('formProfilesSimpleSearch'));
			}			
		},
		
		//load up what we need for the search page
		init: function(args) {
			
			var showHideSections_ = function() {
				if ((args && args.showAdvanced == "true") || document.location.hash == "#advancedSearch") {
					dojo.addClass(dojo.byId("divProfilesTypeaheadSearch"),"lotusHidden");
					dojo.removeClass(dojo.byId("divProfilesAdvancedSearch"),"lotusHidden");
				} else if (document.location.hash == "#simpleSearch") {
					dojo.addClass(dojo.byId("divProfilesAdvancedSearch"),"lotusHidden");
					dojo.removeClass(dojo.byId("divProfilesTypeaheadSearch"),"lotusHidden");
				}		
			};
			
			if (dojo.isIE) { //93150 - IE native mode needs a little more time to render the UI before showing/hiding these areas
				setTimeout(function() {
					showHideSections_();
					
					//100374 - IE doesn't automatically submit the advanced form when prossing enter
					dojo.query("input[type='text']", "advancedSearchForm").connect("onkeypress", function(evt) {
						if ((evt || window.event).keyCode == 13) {
							dojo.byId("advancedSearchForm").submit();
						}
					});
					
				},0);
			} else {
				showHideSections_();
			}
			
			if (dojo.exists("lconn.core.globalization.bidiUtil")) {				
				dojo.query(".bidiAware").forEach( function(element) {
					var elementId = element.id ? element.id : "";
					if (elementId.indexOf("Number") > -1 || elementId.indexOf("email") > -1){
						dojo.removeClass(element, "bidiAware");
					}
				});
				lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage();
			}
	
		},
		
		
		submitSimpleSearch:function(frm) {
			if (typeof frm == "undefined") frm = dojo.byId('formProfilesSimpleSearch');
			if (frm) {
				frm.searchFor.value = _peopleTiles.getTextBoxValue();
				if(/\S/.test(frm.searchFor.value)){
					// string isn't empty and just whitespace
					document.location.href = frm.action + "?" + dojo.formToQuery(frm);
				}	
			}
			return false; //always return false to stop the browser from submitting the form since we are redirecting the browser.
		},
		
		submitAdvancedSearch: function(frm) {
			// unlike simple search, we are not validating that a non-empty entry exists. such a check has never been in place
			if (typeof frm == "undefined") frm = dojo.byId('advancedSearchForm');
			// add the lang parameter oherwise redirect logic will remove the first (keyword) parameter.
			if (frm) {
				document.location.href = frm.action + "/html/advancedSearch.do" + "?" + dojo.formToQuery(frm);
			}
			return false; //always return false to stop the browser from submitting the form since we are redirecting the browser.	
		},
		
		showSimpleUI: function() {

			dojo.addClass(dojo.byId('divProfilesAdvancedSearch'),'lotusHidden');
			dojo.removeClass(dojo.byId('divProfilesTypeaheadSearch'),'lotusHidden');
			window.location.hash = '#simpleSearch';
			dojo.byId('profilesNameSearchField').value = dojo.byId('advancedSearchForm').displayName.value;
			
			if (_peopleTiles) {
				_peopleTiles.reset();
				var newval = _peopleTiles.getTextBoxValue();
				if (newval.length > 0) {
					_peopleTiles._startSearchFromInput(newval);
				}
			}
			
			setTimeout(function(){
				dojo.byId('profilesNameSearchField').focus();
			}, 0);
			
		},

		showAdvancedUI: function() {

			dojo.addClass(dojo.byId('divProfilesTypeaheadSearch'),'lotusHidden');
			dojo.removeClass(dojo.byId('divProfilesAdvancedSearch'),'lotusHidden');
			window.location.hash = '#advancedSearch';

			if (_peopleTiles) {
				_peopleTiles.reset();
				dojo.byId('advancedSearchForm').displayName.value = _peopleTiles.getTextBoxValue();
			}

			setTimeout(function(){
				dojo.byId('advancedSearchForm').keyword.focus();
			}, 0);
		}		
	}
})();
