/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.directory.SearchBoxWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.form.TextBox");
dojo.require("lconn.core.widgetUtils");

dojo.require("lconn.profiles.directory.Localization");

dojo.declare(
    "lconn.profiles.directory.SearchBoxWidget",
    [dijit._Widget, dijit._Templated, lconn.profiles.directory.Localization],
{
	templatePath: dojo.moduleUrl("lconn.profiles", "directory/templates/SearchBoxWidget.html"),
	widgetsInTemplate: true,
	blankGif: "",
	_keyPressed: false,
	
	constructor: function() {
		this.inherited(arguments);
		
		this.blankGif = lconn.core.widgetUtils.addVersionNumber(djConfig.blankGif.substr(0, djConfig.blankGif.indexOf("?")));
	},
	
	postCreate: function() {
		this.connect(this.searchBox.textbox, "onkeypress", "keyPress");
		if(!dojo.isIE) {
			this.connect(this.searchBox.textbox, "oninput", "_onInput");
		}
	},
	    	
	getQuery: function() {
		return this.searchBox.textbox.value;
	},
	
	keyPress: function(evt) {
		//setTimeout to let browser apply changes from this event
		setTimeout(dojo.hitch(this, this.onQueryChanged), 1);
		this._keyPressed = true;
	},
	
	_onInput: function(evt) {
		if(!this._keyPressed){
			this.keyPress(evt);
		}
		this._keyPressed = false;
	},
	
	onQueryChanged: function() {
	}
});