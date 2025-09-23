<%@ page contentType="text/javascript;charset=UTF-8" %><%@ 
    taglib prefix="c" 			uri="http://java.sun.com/jsp/jstl/core" %><%@
    taglib prefix="fn"			uri="http://java.sun.com/jsp/jstl/functions" %><%@
    taglib prefix="cache"		uri="http://www.ibm.com/connections/core/cache" %><%@
    taglib prefix="core"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %><%@
    taglib prefix="bidi"		uri="http://www.ibm.com/lconn/tags/bidiutil" %><%@
    taglib prefix="lc-ui"		uri="http://www.ibm.com/lconn/tags/coreuiutil" %><%@
    taglib prefix="profiles"	uri="/WEB-INF/tlds/profiles.tld" %>

<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2010, 2016                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<core:serviceLink serviceName="profiles" var="profilesSvcHref"/>
<core:serviceLink serviceName="communities" var="communitiesSvcHref"/>
<core:serviceLink serviceName="webresources" var="webresourcesSvcHref"/>
<cache:versionStamp var="bizCardAcs" />
<core:PluginConfig outputScriptTag="false" />

<%-- generic debug printer --%>
<%
	// in case the referer specified debug
	boolean isDebug = (request.getHeader("Referer") != null && request.getHeader("Referer").indexOf("debug=true") != -1) || (request.getParameter("debug") != null && request.getParameter("debug").equals("true")); 
	if (isDebug) request.setAttribute("isDebug", isDebug);
%>

<profiles:lcConfig var="isCardEnabled" property="com.ibm.lconn.personcard.enable" />
<c:if test="${isCardEnabled != 'off'}">
(function(win) {

	var _debugPrint = function( msg ) {
		if(_debug && win.console) 
			console.log( "semanticTagService: [" + msg + "]" );
	};

	<%-- creates a script node and appends it to the head element or body  --%>
	var _addScript = function( src, callback ) {
		var el=document.createElement('script');

		if (typeof callback == 'function') {
			el.onload = function() {
				_debugPrint("el.onload executed");
				el.onload = null;
				el.onreadystatechange = null;
				callback();
				el = null;
			};		
			el.onreadystatechange = function() {
				if (el.readyState == "loaded" || el.readyState == "complete"){
					_debugPrint("el.readyState:  " + el.readyState + ", el.src:  " + el.src);
					el.onload = null;
					el.onreadystatechange = null;
					callback();
					el = null;
				}
			};
		}
		
		el.charset = "UTF-8";
		if (src.indexOf("etag=") == -1) {
			src += ((src.indexOf("?") == -1)?"?":"&") + "etag=${bizCardAcs}";
		}		
		el.src = src;	

		var mainnode = document.getElementsByTagName('head');
		if (mainnode.length > 0) {
			mainnode[0].appendChild(el);
			_debugPrint("Done loading script to head tag: " + src);	
		} else {
			mainnode = document.getElementsByTagName('body');
			if (mainnode.length > 0) {
				mainnode[0].appendChild(el);
				_debugPrint("Done loading script to body tag: " + obj.src);	
			}
		}
		mainnode = null;
	};


	<%--  load the bizcard core code. --%>
	var _loadBizCard = function(callback) {
		
			
		var sSuffix = "&etag=${bizCardAcs}" + (_appLang? "&lang="+_appLang: "") + (_appCountry? "&country="+_appCountry: "") + (_debug? "&debug=true" :"");

		<%-- if dojo is already loaded on the page, we don't want ANY dojo core modules loaded from us --%>
		
			
		_addScript(
			SemTagSvcConfig.resourcesSvc + "/web/_js?include=com.ibm.lconn.personcard.external_prelim" + sSuffix + (!_inclDojo?"&exclude=com.ibm.lconn.personcard.external_allnativedojo":""),
			function() {

				_debugPrint("setting update dojo config to load connections bizcard...");
				if( typeof win.dojo == "object") {
				
					<%-- Make sure we've got a djConfig and dojo.config that we can use --%>
					win.djConfig = dojo.mixin(win.djConfig || {}, {
						localizationComplete: true,
						parseOnLoad: true
					});
					if (typeof win.djConfig.locale == "undefined") win.djConfig.locale = _appLocale;
					
					if (dojo.config) dojo.config.localizationComplete = true;
						
					
					<%--  There is code in the legacy looking for dojo.config.blankGif.  Make sure it's there. --%>
					var _blank = SemTagSvcConfig.resourcesSvc + "/web/com.ibm.oneui3.styles/css/images/blank.gif?etag=${bizCardAcs}";
					try {
						if (typeof dojo.config.blankGif == "undefined") {
							dojo.config.blankGif = _blank;
						}
					} catch (ee) {}
					
					try {
						win.djConfig = win.djConfig || {};
						if (typeof win.djConfig.blankGif == "undefined") {
							win.djConfig.blankGif = _blank;
						}
					} catch (ee) {}			
					
			
					if (win.dojo && typeof dojo._Url == "undefined") dojo._Url = function() {};
					
								

				} else {
					//throw an error - TODO
				}			
			
				<%-- as a defesive move, we are going to load the strings into the base lang --%>
				var copyLocale_ = function(component) {
					try {
						var _res = {};
						for (var obj in lconn[component].bizCard.nls.ui) {
							if (typeof lconn[component].bizCard.nls.ui[obj] == "object") {
								if (obj.indexOf("_") > -1) {
									var coreLang = obj.split("_")[0];
									if (typeof lconn[component].bizCard.nls.ui[coreLang] == "undefined") {
										_res[coreLang] = lconn[component].bizCard.nls.ui[obj];
									}
								}
								if (win.djConfig && djConfig.locale && typeof lconn[component].bizCard.nls.ui[djConfig.locale] == "undefined") {
									_res[djConfig.locale] = lconn[component].bizCard.nls.ui[obj];
								}
							}
						}
						dojo.mixin(lconn[component].bizCard.nls.ui, _res);
					} catch (e) {}
				};
				copyLocale_("profiles");
				copyLocale_("communities");
				
				
				dojo.require("dojo.i18n");
				if (typeof dojo.i18n.getLocalization == "function" && typeof dojo.i18n["getLocalization.bizcard.orig"] == "undefined") {
					dojo.i18n["getLocalization.bizcard.orig"] = dojo.i18n.getLocalization;
					dojo.i18n.getLocalization = dojo.hitch(dojo.i18n, function (packageName, bundleName, locale) {
						var objRes;
						
						<%-- only use this code to lconn packages --%>
						if (packageName && packageName.indexOf("lconn.") === 0) {
							_debugPrint("Load lconn resources: Starting...");
							try {

								
								_debugPrint("Load lconn resources: Locating resource - " + packageName + ".nls." + bundleName);
								
								var baseResource = dojo.getObject(packageName + ".nls." + bundleName);
								
								if (baseResource) {
								
									_debugPrint("Load lconn resources: Found resource - " + packageName + ".nls." + bundleName);
								
								
									<%--  make sure the locale is set --%>
									locale = locale || _appLocale;
									
									
									_debugPrint("Load lconn resources: Locating resource locale - " + locale);
									
									<%-- first let's try just with the locale --%>
									objRes = baseResource[locale];
									
									<%-- if not found and the locale contains lang and locale, get just the lang. --%>
									if (!objRes && locale.indexOf("_") > -1) objRes = baseResource[locale.split("_")[0]];
									
									<%-- as a last resort, just iterate through the object and get the string object manually --%>
									if (!objRes) {
										for (l in baseResource) {
											if (baseResource.hasOwnProperty(l) && typeof baseResource[l] === "object") {
												objRes = baseResource[l];
												break;
											}
										}
									}
									
									if (_debug) {
										if (!objRes) {
											_debugPrint("Load lconn resources: Unable to load resource - " + packageName + ".nls." + bundleName + "." + locale);
										} else {
											_debugPrint("Load lconn resources: Found resource.");
											if (win.console) console.log(objRes);
										}
									}
								}
							} catch (e) {
								if (win.console) {
									console.error("Unable to load lconn specific resources: " + packageName + " - " + bundleName);
									console.error(e);
								}
							}
						}
						
						if (!objRes) {
							try {
								objRes = dojo.hitch(dojo.i18n, dojo.i18n["getLocalization.bizcard.orig"])(packageName, bundleName, locale);
							} catch (e) {
								if (win.console) {
									console.error("Unable to load dojo i18n resources: " + packageName + " - " + bundleName);
									console.error(e);
								}
							}					
						}
						
						if (!objRes) {
							if (win.console) {
								console.error("Unable to load i18n resources: " + packageName + " - " + bundleName);
							}					
							objRes = {};
						}
						
						return objRes;

					});
				}
				

				var sExcludeDojoModules = "com.ibm.lconn.personcard.external_prelim~com.ibm.lconn.personcard.external_dojo";

				if (!_inclDojo) {
					//no-op this internal connections class
					define("jazz/inverted",[], function() {
						return {};
					});
					sExcludeDojoModules += "~com.ibm.lconn.personcard.external_allnativedojo~lconn.communities.bizCard.dialogs.deleteConfirmWidget";
				}
				
				_addScript( 
					"${webresourcesSvcHref}/web/_js?include=com.ibm.lconn.personcard.external" + sSuffix + "&exclude=" + sExcludeDojoModules,
					function() {
						if (_debug) {
							_debugPrint( "dojo version used for connections bizcard: " + win.dojo.version.toString());
						}
							
						if (typeof callback == "function") {
							callback();
						}
						
						dojo.publish("lconn/bizCard/init", [lconn]);

						SemTagSvc.init();
					}
				);
				
			}
		);	
		

		
		
	};

	<%--  loads the bizcard cross-domain version of dojo --%>
	var _loadDojo = function() {
		<%--  setup dojo loader configuration	--%>
		_debugPrint("Requested CARD WITH DOJO");

		<%-- dojo is provided by bizcard service, configure dojo and namespace it to connections specific namespace --%>
		<%-- note that dojo will make a copy of this config object and maintain its own copy --%>
		win.djConfig = {
			isDebug: _debug,
			baseUrl: SemTagSvcConfig.resourcesSvc + "/web/dojo/",
			blankGif: SemTagSvcConfig.resourcesSvc + "/web/com.ibm.oneui3.styles/css/images/blank.gif?etag=${bizCardAcs}",
			bindEncoding: "UTF-8",
			locale: _appLocale,
			localizationComplete: true,
			useXDomain: true,
			xdWaitSeconds: 20,
			parseOnLoad: false,
			afterOnLoad: true,
			<%-- Because we're loading dojo dynamically, tell dojo not to wait for a DOMContentLoaded see: --%>
			<%-- http://dojotoolkit.org/documentation/tutorials/1.6/dojo_config/ --%>			
			skipIeDomLoaded: true 
		};
		
		<%-- oneui proxy js requires djConfig.proxy be set before loading dojo --%>
		if (win.djConfig && win.SemTagSvcConfig && SemTagSvcConfig.proxyURL) 
			win.djConfig.proxy = SemTagSvcConfig.proxyURL;	
		
		<%-- load dojo, then load personcard.legacy on the callback (after dojo script loads) --%>
		_debugPrint("Loading dojo...");
		_addScript( 
			SemTagSvcConfig.resourcesSvc + "/web/_js?include=com.ibm.lconn.personcard.external_dojo&debug="+(_debug?"true":"false")+"&etag=${bizCardAcs}"+(_appLang? "&lang="+_appLang: ""),
			_loadBizCard
		);
	};



	var _debug;
	var _appLocale;
	var _appLang;
	var _appCountry;
	var _inclDojo;


	var _init = function() {
		_debug = ("${param.debug}" == "true" || "${isDebug}" == "true")? true :false;

		_debugPrint("Init bizcard");

	
		<%-- skip CRE token handling (see RTC#76599) --%>
		win.__iContainer_skip_init__ = true;

		<%-- Need to break out lang and country from locale and pass it in separately into the script resources. (RTC#89929) --%>
		_appLocale = ((${empty param.lang})?"<profiles:appLang />":"${param.lang}");
		if (_appLocale.indexOf("iw") == 0) _appLocale = _appLocale.replace(/iw/, "he");  <%-- RTC#89032 --%>


		if (_appLocale.indexOf("_") > -1) {
			_appCountry = _appLocale.split("_")[1];
			_appLang = _appLocale.split("_")[0];
		} else {
			_appLang = _appLocale;
			_appCountry = "";
		}
		
		<%--  setup semantic tag service configuration object --%>
		if(!win.SemTagSvcConfig) win.SemTagSvcConfig = {};
		SemTagSvcConfig.profilesSvc = SemTagSvcConfig.profilesSvc || "${profilesSvcHref}";
		SemTagSvcConfig.communitiesSvc = SemTagSvcConfig.communitiesSvc || "${communitiesSvcHref}";
		SemTagSvcConfig.resourcesSvc = SemTagSvcConfig.resourcesSvc || "${webresourcesSvcHref}";
		SemTagSvcConfig.appChksum = "${bizCardAcs}";
		SemTagSvcConfig.loadCssFiles = SemTagSvcConfig.loadCssFiles ||("${empty param.loadCssFiles || param.loadCssFiles == 'true'}" == "true"? true : false);
		SemTagSvcConfig.sametimeAwarenessEnabled = <profiles:isSametimeAwarenessEnabled />;
		if ("${empty param.cardBehavior}" == "false") SemTagSvcConfig.cardBehavior = "${param.cardBehavior}";
		
		if (!SemTagSvcConfig.cardBehavior) SemTagSvcConfig.cardBehavior = "hover";

		<%-- check for Portal --%>
		SemTagSvcConfig.isPortal = SemTagSvcConfig.isPortal || (typeof SemTagSvcPortal !== "undefined");
		
		<%-- check to see if jsonp is supported --%>
		SemTagSvcConfig.allowJsonp = ("true" === "${profiles:allowJsonp()}");
		
		<%--  setup additonal configs that the inner bizcard code may expect exist --%>
		if( !win.SemTagPerson) win.SemTagPerson = { services: [] }; <%-- Needed for xdomain --%>
		if( typeof(win.WidgetPlacementConfig) == "undefined") win.WidgetPlacementConfig = { applicationContext: '${profilesSvcHref}/' };

		<%-- sametime awareness needs this --%>
		try {
			var el = dojo.byId("usernameText");
			if (!el) {
				el = dojo.create(
					"div",
					{
						style: "display: none;",
						id: "usernameText", 
						innerHTML: "-"
					},
					dojo.body()
				);
			}
			
			<%-- hide the taskbar for external deployments --%>
			win.lconnAwaressHideActionBar = true;

		} catch (e) {
			_debugPrint( "unabled to place usernameText field on Dom Node for sametime awareness" );
		}


		_inclDojo = ( (${empty param.inclDojo}) ? (typeof win.dojo !== "object") : ${param.inclDojo != 'false' && param.inclDojo != '0'} );


		setTimeout((_inclDojo?_loadDojo:_loadBizCard), 1);


	};
	_init();
	
})(window);
</c:if>
