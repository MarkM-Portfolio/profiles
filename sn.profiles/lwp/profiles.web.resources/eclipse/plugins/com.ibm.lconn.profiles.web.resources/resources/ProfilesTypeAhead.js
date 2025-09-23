/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.ProfilesTypeAhead");

dojo.require("lconn.core.TypeAhead");

dojo.declare(
    "lconn.profiles.ProfilesTypeAhead",
    [lconn.core.TypeAhead],
    {  
		hideEmptyResults: true,
		submitFormOnKey: true,
		templatePath: dojo.moduleUrl("lconn.profiles", "templates/typeAhead.html"),
		
		postMixInProperties: function() {

			this.inherited(arguments);
			
			//make sure this exists since the template attaches to it.
			if (typeof this.compositionend !== "function") {
				this.compositionend = function() {};
			}
			
		},
		postCreate: function() {

			this.inherited(arguments);
			
			//make sure the main field is visible
			if (dojo.style(this.focusNode, "display") == "none") {
				dojo.style(this.focusNode, "display", "inline");
			}
			
		}
    }
);
