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

dojo.provide("lconn.profiles.widgets.utils");

(function() {
	lconn.profiles.widgets.utils = {
		XmlEncoder: {
			encode: function encode(str) {
				return str.replace(/&/gm, "&amp;").replace(/</gm, "&lt;").replace(/>/gm, "&gt;").replace(/"/gm, "&quot;").replace(/'/gm, "&apos;");
			},
			
			decode: function decode(str) {
				return str.replace(/\&lt\;/gm, "<").replace(/\&gt\;/gm, ">").replace(/\&quot\;/gm, "\"").replace(/\&apos\;/gm, "'").replace(/\&amp\;/gm, "&");
			}
		},
		
		PlusEncoder: {
			encode: function encode(str) {
				return str.replace(/\+/gm, "&plus;");
			},
			
			decode: function decode(str) {
				return str.replace(/\&plus\;/gm, "+");
			}		
		},
		
		hasPermission: function(arg) {
			var ret = false;
			
			if (dojo.exists("profilesData.enabledPermissions")) {
				
				if (dojo.isArray(arg)) {
					arg = arg[0] + "$" + arg[1];
				}
				
				if (typeof arg === "string" && arg.indexOf("$") == -1 && arg.indexOf(".") > -1) {
					var tmp = arg.split(".");
					tmp.pop();
					arg = tmp.join(".") + "$" + arg;
				}
				
				ret = dojo.indexOf(profilesData.enabledPermissions, arg) > -1;
				
			}
			
			return ret;
		}
	}
	
})();
