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

dojo.provide("lconn.profiles.aria.Toolbar");

dojo.require("lconn.core.aria.Toolbar");

dojo.requireLocalization("lconn.profiles", "ui");

(function() {

	var SELECTED_CLASS = "lotusSelected";
	
	dojo.declare("lconn.profiles.aria.Toolbar", [lconn.core.aria.Toolbar], {
	
		strings: {},
		showSelect: true,
		
		stringKeys: {
			unselected: "label.a11y.button.unselected",
			selected: "label.a11y.button.selected"
		},
	
		constructor: function() {
			this.strings = window.generalrs || this.strings;
			
			//load our strings...
			var defaultStrings = dojo.i18n.getLocalization("lconn.profiles", "ui");
			if (typeof defaultStrings !== "undefined") {
				this.strings = dojo.mixin(
					defaultStrings,
					this.strings
				);
			}

			if (this.selIdx > -1 && this.showSelect) {
				dojo.forEach(this.allItems, dojo.hitch(this, function(item, idx) {
					this._setSelected(item, (item == this.allItems[this.selIdx]));
				}));
			}
			if(window.location.pathname.includes('profileView') && window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()){
				dojo.forEach(this.allItems, dojo.hitch(this, function(item, idx) {
					this._setSelected(item, (item == this.allItems[1]));
				}));
			}
		},
		
		_setSelected: function(item, yn) {
			try {	
				var pNode = item;
				while (item && pNode && pNode.nodeName && pNode.nodeName.toUpperCase() != "LI") {
					pNode = pNode.parentNode;
				}
				if (item && pNode) {
					var txt = dojo.trim(item.innerText || item.textContent || "");
					var str = "{0}";
					if (yn) {
						dojo.addClass(pNode, SELECTED_CLASS);
						str = this.strings[this.stringKeys.selected] || str;
					} else {
						dojo.removeClass(pNode, SELECTED_CLASS);
						str = this.strings[this.stringKeys.unselected] || str;
					}
					
					dojo.attr(item, "aria-label", str.replace("{0}", txt));
				}
			} catch (e) {}
		}



	});



})();