/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/* author: Ronny A. Pena                                             */

dojo.provide("lconn.profiles.ReportingStructure");

dojo.require("lconn.profiles.ProfilesCore");
dojo.require("lconn.profiles.ProfilesXSL");
dojo.require("lconn.profiles.PersonTag");
dojo.require("lconn.core.globalization.bidiUtil");

var profiles_reportChainXSLT = "report-chain.xsl";
var bidiUtil = lconn.core.globalization.bidiUtil;

function profiles_loadReportChain(key, container, numberOfNameToDisplay, widgetId)
{
	var dataUrl = applicationContext + "/atom/forms/reportingChain.do?key=" + key;
	var tempArray = new Array;
	if(typeof(numberOfNameToDisplay) == "string")
		numberOfNameToDisplay = parseInt(numberOfNameToDisplay);
		
	tempArray.push(["numberOfNameToDisplay", numberOfNameToDisplay]);
	tempArray.push(["bidiIsRTL", profiles_isBidiRTL]);
	tempArray.push(["containerId", container]);
	
	lconn.profiles.ProfilesXSL.loadContent(
		dataUrl, profiles_reportChainXSLT, container, 
		["label_profile_otherviews_reportingstructure", "label_profile_otherviews_samemanager", "label_profile_otherviews_peoplemanaged"], 
		tempArray, null, true /* append lastMod */);
		
	profiles_AddLiveNameSupport(container + '-sub');
	
	
	/*a11y - set the aria-label of the manager to his/her subordinate and unhide if hidden*/
	lconn.core.utilities.processUntilAvailable(
		function() {
			try {
				var getNameFromNode = function(node) {
					var ret = "";
					var subNodes = dojo.query("a", node);
					if (subNodes.length == 0) subNodes = dojo.query("div", node);
					
					if (subNodes.length > 0) {
						ret = bidiUtil.enforceTextDirection(subNodes[0].innerHTML);
					}
					return ret;
				};
				
				dojo.query("li", container + '-sub').forEach(function(liNode, idx, liNodes) {
					//this is the code to unhide the widget if we get a hierarchy or the one result
					//returned is a manager
					try {
						if (idx == 0) { //only do this on the first pass.
							var unhideWidget = false;
							if (liNodes.length == 1) {
								if (dojo.query('.' + container + '-sub-peopleManaged', container).length > 0) {//if we only have one result, check to see if manager.
									unhideWidget = true;
								}
							} else {
								unhideWidget = true;
							}
              
							//check to see if we are allowed to see people managed
							try {
								if (window.profilesData && profilesData.enabledPermissions && profilesData.enabledPermissions.indexOf('profile.peopleManaged') == -1) {
									dojo.query('.' + container + '-sub-peopleManaged', container).addClass('lotusHidden');
									dojo.query('.' + container + '-sub-sameManager', container).addClass('lotusHidden');
								}
							} catch (ex) {
								if (window.console) console.error(ex);
							}
              
							if (unhideWidget) {
								lconn.core.WidgetPlacement.unhideWidget(widgetId);
							}
						}
					} catch (e) {
						if (window.console) console.error(e);
					}

					if (idx > liNodes.length - 2) return;

					var str = generalrs["reportStructureNameLabel"] || "${0}, manager of ${1}";
					var dir = profiles_isBidiRTL ? "rtl" : "ltr"; //whole string text direction depends on page orientation/translation
					str = bidiUtil.enforceTextDirection(dojo.string.substitute(
						str, 
						[
							getNameFromNode(liNode), 
							getNameFromNode(liNodes[idx + 1])
						]
					), dir);

					dojo.query("a", liNode).attr(
						{
							"aria-label": str,
							"title": str
						}
					);

				});
				bidiUtil.enforceTextDirectionOnPage("report-chain-sub");

			} catch (ee) {
				console.error(ee);
			}
		},
		'dojo.query("li", "' + container + '-sub").length > 0', 
		null, 
		false /* do not throw if test clause not found */
	);

}

var lastActionParams_ = {};
var showManagementAction = function(params, fnCallback)
{
	dojo.forEach(params, function(x) {
		params[x] = encodeURIComponent(params[x]);
	});
	
	var oldParams = dojo.queryToObject(((location.href + "?").split("?"))[1]);
	
	oldParams = dojo.mixin(oldParams, lastActionParams_);
	
	
	var newParams = dojo.mixin(oldParams, params);
	
	var nodeid;
	
	if (newParams.subAction == "sameManager") {
		nodeid = "rptChainSameMgr_li";
		newParams.appAction = "profileSameManager";
		
	} else 
	if (newParams.subAction == "peopleManaged") {
		nodeid = "rptChainPeopleMged_li"
		newParams.appAction = "profilePeopleManaged";
		
	} else {
		nodeid = "rptChain_li"
		newParams.appAction = "profileFullReportToChain";
		
	}
	
	lastActionParams_ = newParams;
	
	profilesRptStructure_menuSelect(nodeid);
	
	var dataURL = applicationContext + "/html/" + newParams.appAction + ".do?" + dojo.objectToQuery(newParams);
	
	var elementId = 'reportStructureArea';
	var tabContent = dojo.byId(elementId);
	tabContent.innerHTML  = generalrs.reportStructureLoading;
	setTimeout(function() {
		lconn.profiles.xhrGet({
			url: dataURL,
			htmlContainerElemId: "reportStructureArea",
			error: lconn.profiles.ProfilesCore.DefaultXHRErrorHandler,
			load: function(response, ioArgs){
				
				tabContent.innerHTML = response;
				
				//if there are any scripts in the the html, execut them
				dojo.query("script", tabContent).forEach(function(node) {
					try {
						if (node.innerHTML.length > 0) {
							dojo.eval(node.innerHTML);
						}
					} catch (ee) {
						console.error(ee);
					}
				});
				
				profiles_AddLiveNameSupport("rptStructTable");
				
				if (typeof fnCallback === "function") {
					fnCallback(lastActionParams_);
				}

			},
			checkAuthHeader: true
		});
	},10);
	
	return false;

}

/*
var activateRTCTab = function activateRTCTab(domItem, list) {
	console.debug("activateRTCTab ", arguments);
	
	if (domItem == null) return;

	currentNode = domItem;
	for (; currentNode.nodeName != 'LI'; ) {
		currentNode = currentNode.parentNode;
	}
	var domId = currentNode.id;

	var li = currentNode; 
	var ul = currentNode.parentNode;

	if (list == null || !dojo.isArray(list)) {
		list = [];
		for (var i=0; i<ul.childNodes.length; i++) {
			if (ul.childNodes[i].nodeName == 'LI') {
				list[list.length] = ul.childNodes[i].id;
			}
		}
	}

	for (var i=0; i<list.length; i++){
		var tab = dojo.byId(list[i]);
		if (tab) {
			if (list[i] == domId) {
				dojo.addClass(tab, "lotusSelected");
			} else {
				dojo.removeClass(tab, "lotusSelected");
			}
		}
	}
}

var showRptToChain = function showReportToChain(indicator, parameters) 
{
	var dataURL = applicationContext + "/html/profileFullReportToChain.do?key=" + encodeURIComponent(parameters["key"]) + "&managerKey=" + 
    		encodeURIComponent(parameters["managerKey"]) + "&isManager=" + parameters["isManager"] + "&lang=" + appLang;
	showManagementAction(dataURL);
}

var showSameManager = function showSameManager(indicator, parameters)
{
	var dataURL = applicationContext + "/html/profileSameManager.do?key=" + encodeURIComponent(parameters["key"]) + "&managerKey=" + 
    		encodeURIComponent(parameters["managerKey"]) + "&isManager=" + parameters["isManager"] + "&lang=" + appLang;
	showManagementAction(dataURL);
}

var showPeopleManaged = function showPeopleManaged(indicator, parameters) 
{
	var dataURL = applicationContext + "/html/profilePeopleManaged.do?key=" + encodeURIComponent(parameters["key"]) + "&managerKey=" + 
    		encodeURIComponent(parameters["managerKey"]) + "&isManager=" + parameters["isManager"] + "&lang=" + appLang;
	showManagementAction(dataURL);
}
*/