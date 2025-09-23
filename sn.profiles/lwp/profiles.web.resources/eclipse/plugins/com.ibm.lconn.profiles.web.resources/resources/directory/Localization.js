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

dojo.provide("lconn.profiles.directory.Localization");

dojo.require("dojo.i18n");
dojo.requireLocalization("lconn.profiles.directory", "common");

dojo.declare(
		"lconn.profiles.directory.Localization",
		null,
{
		commonStrings: null,
		
		postMixInProperties: function(){
			this.commonStrings = dojo.i18n.getLocalization("lconn.profiles.directory", "common");
	        dojo.mixin(this, this.commonStrings);
	        this.inherited(arguments);
		},
		
		getString: function(stringId, params) {
			return dojo.string.substitute(this.commonStrings[stringId], params);
		}
});