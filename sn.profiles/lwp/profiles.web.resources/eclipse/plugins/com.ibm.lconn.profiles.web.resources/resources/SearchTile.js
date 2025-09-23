/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/* author: Robert Barber                                             */

/*
 * This class displays the individual search tiles on the Directory 
 * search page.
 */
 
 
dojo.provide("lconn.profiles.SearchTile");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated"); 

dojo.require("lconn.core.HTMLUtil");


dojo.declare(
    "lconn.profiles.SearchTile",
    [dijit._Widget, dijit._Templated],
    {
	
		templatePath: dojo.moduleUrl("lconn.profiles", "templates/SearchTile.html"),
		
		messages: {},
		
		_tileConnects: [], //holds any dojo.connects so we can disconnect on destroy
		
		profilesSvcLocation: "/profiles",
		person: {},
		
		_inactive: false,
		
		showEmail: true,
		expandThumbnails: true,
		lastMod: "",
		
		postMixInProperties: function() {
			this.inherited(arguments);
			
			// set all the necessary field data
			this.person.encoded_userid = encodeURIComponent(this.person.userid);
			
			this.person.escape_userid = lconn.core.HTMLUtil.escapeText(this.person.userid);
			
			this.person.member = this.person.member || "";
			this.person.escape_member = lconn.core.HTMLUtil.escapeText(this.person.member);
			
			this.person.ext.escape_jobResp = ((!this.person.ext.jobResp)?"&nbsp;":lconn.core.HTMLUtil.escapeText(this.person.ext.jobResp));
			
			
			if (this.person.ext && this.person.ext.state === "INACTIVE") {
				this._inactive = true;
			}
			if (this.person.ext.lastUpdate) {
				this.lastMod = encodeURIComponent(this.person.ext.lastUpdate);
			}
		
		},
		
		postCreate: function() {
			this.inherited(arguments);
			
			if (this._inactive || (!this.showEmail)) {
				dojo.addClass(this.emailNode, "lotusHidden");
			}
			
			if (this._inactive) {
				dojo.attr(this.linkNode, "aria-label", this.messages.inactiveText);
				
				dojo.addClass(this.jobRespNode, "lotusHidden");
				dojo.removeClass(this.inactiveNode, "lotusHidden");
			}

			// only zoom the picture if expandThumbnails is set
			if (this.expandThumbnails) {
				this._tileConnects.push(dojo.connect(this.photoNode, "mouseover", this, "_growPhoto"));
				this._tileConnects.push(dojo.connect(this.photoNode, "mouseout", this, "_resetPhoto"));
			}
			
			
		},
		
		destroy: function() {
			// disconnect any connects
			while (this._tileConnects.length > 0) {
				dojo.disconnect(this._tileConnects.pop());
			}
			
			this.inherited(arguments);
		},
		
		// zoom in
		_growTimeout: "",
		_growPhoto: function() {
			if (!this.expandThumbnails) return;
			
			this._growTimeout = setTimeout(dojo.hitch(this, function() {
				if (dojo.exists("lconn.profiles.profiles_behaviours")) {
					dojo.style(this.photoNode, "zIndex", "100000");
					lconn.profiles.profiles_behaviours.animImgResize({
						id: this.photoNode.id,
						width: 155,
						height: 155,
						delay: 100
					});
				}
			}), 500); 
			return;
		},

		_resetPhoto: function() {
			if (!this.expandThumbnails) return;
			
			clearTimeout(this._growTimeout);
			if (this.photoNode.style.width != "55px") {
				if (dojo.exists("lconn.profiles.profiles_behaviours")) {
					dojo.style(this.photoNode, "zIndex", "auto");
					lconn.profiles.profiles_behaviours.animImgResize({
						id: this.photoNode.id,
						width: 55,
						height: 55,
						delay: 1
					});
				}
			}
		}	
		
    }
);
    
