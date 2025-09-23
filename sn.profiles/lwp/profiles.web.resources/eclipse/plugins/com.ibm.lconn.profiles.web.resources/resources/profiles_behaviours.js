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

dojo.provide("lconn.profiles.profiles_behaviours");


lconn.profiles.profiles_behaviours = {
	// dojo animations
	animImgResize: function profiles_animImgResize(id,width1,width2,height1,height2,d,r) {
		var c2;
		
		if (typeof arguments[0] == "string") { //old api
			c2 = {
				"id": id,
				"w": width2,
				"h": height2,
				"delay": d,
				"rate": r
			};
		} else {
			c2 = arguments[0];
			c2.h = c2.h || c2.height;
			c2.w = c2.w || c2.width;
		}
		
		if (!c2.id) return;
		var el = dojo.byId(c2.id);
		if (!el) return;
		
		
		var c1 = dojo.coords(el);
		c2 = dojo.mixin(dojo.clone(c1), dojo.clone(c2));
		
		var diff = {
			h: (c2.h - c1.h) / 2,
			w: (c2.w - c1.w) / 2
		};
		
		//center the shrink/grow
		c2.t = Math.round(c2.t - diff.h);
		c2.l = Math.round(c2.l - diff.w);
		
		dojo.animateProperty({
			node: el,
			duration: (c2.delay || 500),
			rate: (c2.rate || 10),
			properties: {
				height: { start: c1.h, end: c2.h },
				width: { start: c1.w, end: c2.w },
				top: { start: c1.t, end: c2.t },
				left: { start: c1.l, end: c2.l }
			}
		}).play();
	},
	
	fadeIn: function profiles_fadeIn(id) {
		return dojo.fadeIn({
			node: id,
			duration: 500,
			 beforeBegin: function() {
				var node = dojo.byId(id);
				dojo.style(node, "opacity", 0);
				dojo.style(node, "display", "block");
			}
		});
	},
	
	fadeOut: function profiles_fadeOut(id) {
		return dojo.fadeOut({
			node: id,
			duration: 500,
			beforeBegin: function() {
				var node = dojo.byId(id);
				dojo.style(node, "display", "none");
			}
		});
	},

	// function to coordinate toggling divs
	toggleDiv: function toggleDiv(id) {
		if (dojo.byId(id).style.display != "none") {
			var fx = lconn.profiles.profiles_behaviours.fadeOut(id);
		} else {
			var fx = lconn.profiles.profiles_behaviours.fadeIn(id);
		}
		fx.play();
	},
	
	// show/hide the slim card
	showSlimCard: function showSlimCard(userid,s) {
		var e = dojo.byId(s);
		if (e) { 
			SemTagPerson.renderMiniBizCard(userid,null,e);
			e.style.display = "block";
		}
	},
	
	hideSlimCard: function hideSlimCard(s) {
		var e = dojo.byId(s);
		if (e) {
			e.style.display = "none";
		}
	},
	
	_toolbar: null,
	
	setProfilesToolbarControl: function() {
		var toolbarId = "lotusHeaderNavigation_UL";
		
		if (dojo.byId(toolbarId)) {
			var selIdx = -1;
		
			// highlight selected tab based on current URL
			var suffix = "";
			var href = window.location.href;
			var isMyProfile = ( profilesData.loggedInUser.loggedInUserKey == profilesData.displayedUser.key );
			
			if ( href.indexOf("/editMyProfileView.do") != -1 || 
				href.indexOf("/myProfileView.do") != -1 ||
				(isMyProfile &&	(href.indexOf("/profileView.do") != -1 ||
								 href.indexOf("/reportingStructureView.do") != -1)) ) {
				suffix = "MyProfile";
			}
			
			else if ( isMyProfile && href.indexOf("/networkView.do") != -1 ) {
				suffix = "MyNetwork";
			} 
			
			else if ( href.indexOf("/searchProfiles.do") != -1 ||
					href.indexOf("/simpleSearch.do") != -1 ||
					href.indexOf("/simpleSearchView.do") != -1 ||
					href.indexOf("/advancedSearch.do") != -1 ||
					href.indexOf("/advancedSearchView.do") != -1 ) { 
				suffix = "Directory";
			}
			
			dojo.query("#" + toolbarId + " li").forEach(function(liNode, idx) {
				if (liNode.id == "liProfileHeader_" + suffix) {
					selIdx = idx;
				}
			});
			
			this._toolbar = new lconn.profiles.aria.Toolbar(toolbarId, {"selIdx": selIdx, "showSelect": (selIdx > -1)});
		}
	},
	
	init: function() {
		
		var loadToolbarControl_ = function() {
			dojo.addOnLoad(lconn.profiles.profiles_behaviours.setProfilesToolbarControl);
		};
		
		// ie9+ has an issue where dojo addOnLoad is firing before the page is actually all loaded
		// so we need to hang the function off of the window load event.		
		if (dojo.isIE && dojo.isIE >= 9) {
			dojo.connect(window, "load", loadToolbarControl_);
		} else {
			loadToolbarControl_();
		}		
	}
};


//TODO - migrate and remove references to these global functions
var profiles_animImgResize = lconn.profiles.profiles_behaviours.animImgResize;
var toggleDiv = lconn.profiles.profiles_behaviours.toggleDiv;
var profiles_fadeIn = lconn.profiles.profiles_behaviours.fadeIn;
var profiles_fadeOut = lconn.profiles.profiles_behaviours.fadeOut;
var showSlimCard = lconn.profiles.profiles_behaviours.showSlimCard;
var hideSlimCard = lconn.profiles.profiles_behaviours.hideSlimCard;


lconn.profiles.profiles_behaviours.init();