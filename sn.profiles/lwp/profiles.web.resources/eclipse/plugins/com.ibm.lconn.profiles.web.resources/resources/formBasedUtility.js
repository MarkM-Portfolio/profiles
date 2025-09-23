/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.require("com.ibm.ajax.auth");
dojo.require("lconn.core.auth.whiteListHelper");
dojo.provide("lconn.profiles.formBasedUtility");

// TODO: Move this to a global Connections level class

dojo.declare(
	// class
	"lconn.profiles.formBasedUtility",	
	// superclass
	null,
	{	
		// summary: Utility encapsulating the logic for detecting the form based auth challenge on Ajax requests
   			
		// _contextRoot: String
		_contextRoot: null,
		
		// REDIRECT_PATH: String
		REDIRECT_PATH: null,
		
		// COOKIE_NAME: String
		COOKIE_NAME: "ProfilesReqURL",
		
		constructor: function(contextRoot){
			if (!lconn.profiles.formBasedUtility.prototype._init){
				lconn.profiles.formBasedUtility.prototype._init = true;
				this._contextRoot = contextRoot;
				this.REDIRECT_PATH = "/auth/loginRedirect.do?loginReturnPage=" + profilesData.config.loginReturnPageEnc;
				this._overrideXhrCalls();
			} 
			else {
				throw new Error("lconn.profiles.formBasedUtility is a singleton. It cannot be instantiated twice");				
			}
		},
		
		_checkHeaders : function(auth, response, ioArgs) {
			var xlconnAuth = ioArgs.xhr.getResponseHeader("X-LConn-Auth");
			var hasHeader = ("true" == xlconnAuth || "false" == xlconnAuth);
			
			if (hasHeader) {
				return false;
			} else if (ioArgs.args.checkAuthHeader) {
				return true;
			}

			return false;
		},
		
		_check302 : function(auth, response, ioArgs) {
			if (!ioArgs.args._profilesRequest) {  // indicates that this is Profiles app request
				return false;
			}
		
			if (typeof ioArgs.xhr.status == "unknown") {  // seen on IE6
				return true;
			}
				
			var returnStatus = ioArgs.xhr.status;
		    if (returnStatus == 302 || 
		            (dojo.isIE && (returnStatus == 0 || returnStatus == 12150))) {
		         return true;
		    }
		    
		    return false;
		},
		
		_check401 : function(auth, response, ioArgs) {
			if (!ioArgs.args._profilesRequest) {  // indicates that this is Profiles app request
				return false;
			}

		    return (ioArgs.xhr.status == 401);
		},		
		
		
		_overrideXhrCalls: function(){
			// summary: Dynamically override the lconn.profiles.xhr* calls to invoke the form based auth mechanism
			
			var auth = com.ibm.ajax.auth;
			
			// add method to check for X-LConn-Auth
			auth.addAuthenticationCheck(this._checkHeaders);
			// always check for 302 response
			auth.addAuthenticationCheck(this._check302);
			// always check for 401 response
			auth.addAuthenticationCheck(this._check401);
			
			var url = this._contextRoot + this.REDIRECT_PATH;
			var that = this;
			
			var handler = {
					// just put the context root, which is form based auth protected
			        url: url,
			        authenticationRequired: function(response, ioArgs, onauthenticated) {
							if (ioArgs.args[1].noLoginRedirect && ioArgs.args[1]._profilesRequest) {
								lconn.profiles.ProfilesCore.showInfoMsg("profileInfoMsgDiv", "error", generalrs["error.sessionTimeout"]);
							} else {
								var racp = window.location.href.replace(/,/g,"%2C");
								//dojo.cookie(that.COOKIE_NAME, racp, {path: "/"});
								// dojo.cookie seems to be encoding the url, causing a redirect error
								document.cookie = that.COOKIE_NAME+"="+ racp +"; path=" + that._contextRoot;
								location.href = this.url;
							}
			            },
			        onSuccess: function(response, ioArgs) {
			            	console.log("Successfully loaded");
			        }
			};
			
			// Some xhrGet requests have handleAs text for IE even though they are trying to fetch xml
			// So if we don't get xml back for an atom request, assume it is a form based login
			var isFormLoginResponse = function(response,ioArgs) {
				ioArgs.args._profilesRequest = true;
				return this._check302(null, response, ioArgs);
			};

			var isUnauthenticatedResponseFunction = isFormLoginResponse;
			
	        if (typeof(CUSTOM_AUTH_JS_CLASS) !== "undefined"){
	            var customAuthJSObject = eval("new " + CUSTOM_AUTH_JS_CLASS + "()");
	            if (typeof(customAuthJSObject.isAuthenticationRequired) !== "undefined"){
	                auth.setDefaultAuthenticationTests(true, false, true);
	                isUnauthenticatedResponseFunction = customAuthJSObject.isAuthenticationRequired;
	            if (typeof(customAuthJSObject.handler) !== "undefined")
	                auth.setAuthenticationHandler(dojo.hitch(customAuthJSObject.handler, customAuthJSObject.handler.authenticationRequired));
	            else
	                auth.setAuthenticationHandler(dojo.hitch(handler, handler.authenticationRequired)); 
	            }
	        }
	        else{
	            auth.setDefaultAuthenticationTests(true, false, true);
	            auth.setAuthenticationHandler(dojo.hitch(handler, handler.authenticationRequired)); 
	        }
			
			    
     		var whiteListHelper = new lconn.core.auth.whiteListHelper(profilesGlobalServices, ibmConfig.proxyURL); //setting proxy url to null for now
			originaldojoxhr = dojo.xhr;
			dojo.xhr = function() {  
				// only handle FBA for whitelisted urls
				var ioArgs = arguments[1];
				
				var checkAuthHeader = false;
				var profilesRequest = false;
			  	if (ioArgs.checkAuthHeader) {
			  		checkAuthHeader = ioArgs.checkAuthHeader;
			  		profilesRequest = true;
			  	}
				
				// We have our own check for X-LConn-Auth
				// TODO - move to use the built-in check in com.ibm.ajax.auth, but there are some logical 
				// differences between profiles' checks and the core check which will need to be resolved.
				auth.checkByXLConnAuth = false;
				
				if (whiteListHelper.isWhiteListedURL(ioArgs.url)) {
				
						// set handleAs to "text" (default) due to a bug in com.ibm.ajax.auth 
						// assuming that handleAs is set
						// handleAs is optional according to dojo doc and set to "text" when not set
						
						if (typeof ioArgs.handleAs == "undefined"){
							ioArgs.handleAs = "text";
						}
				
				
						try{
							ioArgs[1] = auth.prepareSecure(ioArgs); 
							ioArgs[1]._checkAuthHeader = checkAuthHeader; // ahernm - this is needed to make TAM forms auth work with The Board
							ioArgs[1]._profilesRequest = profilesRequest;
					    } catch(e){
					    	console.log("exception in overriden lconn.profiles.xhr (form-based auth");
					        console.log(e);
					    }    
				   }    
			    return originaldojoxhr(arguments[0], arguments[1], arguments[2]);
			};
		}		
	}
);

lconn.profiles.formBasedUtility.prototype._init = false;