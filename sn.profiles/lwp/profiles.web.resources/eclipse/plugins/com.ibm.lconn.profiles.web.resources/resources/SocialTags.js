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

dojo.provide("lconn.profiles.SocialTags");

dojo.require("dojo.parser");
dojo.require("dojo.cookie");
dojo.require("dojo._base.event");
dojo.require("lconn.profiles.ProfilesCore");
dojo.require("lconn.profiles.ProfilesXSL");
dojo.require("lconn.core.globalization.bidiUtil");
dojo.require("lconn.core.formutilities");

var profiles_tagsXSLT = "tags.xsl";

var socialTagsIContext = null;

function profiles_loadTags(displayedUserKey, container, iContext, focusElement)
{
	var tempTypeAhead = dijit.byId("socialTagName");
	if(tempTypeAhead != null) tempTypeAhead.destroy();	
	
	socialTagsIContext = iContext;

	var dataUrl = applicationContext + "/atom/forms/profileTags.do?targetKey=" + displayedUserKey +"&format=full";
	
	if( lconn.profiles.ProfilesCore.isUserLoggedIn()) dataUrl += "&flagByKey=" + lconn.profiles.ProfilesCore.getLoggedInUserKey();
	if( typeof(profilesData.config.appChkSum) != "undefined" ) dataUrl += ("&acs=" + profilesData.config.appChkSum ); // add param for deleted users

	lconn.core.widgetUtils.handleRefresh( dataUrl, iContext);

  	var resourceIdArray = ["socialTagsAddTags",
  						   "socialTagsNoTags", 
  						   "socialTagsNoTagsCantAdd", 
  						   "socialTagsYouTagged" , 
  						   "socialTagsViewAs" , 
  						   "socialTagsList" , 
  						   "socialTagsTagCloud", 
  						   "socialTagsAddTagsAltText", 
  						   "socialTagsRemoveTagsAltText",
						   "socialTagsListAltText",
     					   "socialTagsTagCloudAltText",
						   "socialTagsPeople",
						   "socialTagsPerson",						 
						   "socialTagsTaggedBy",
						   "socialTagsWhoTagged",
						   "socialTagsWhoTaggedMulti", 
						   "socialTagsAddedBy"];

  	// default tag view to cloud view (unless a cookie was set with list)
	var defaultView = new Array();
	defaultView.push(["defaultView", ((dojo.cookie("profiles.socialTags.view") == "list")? "list": "cloud")]);
	var additionalParamsMap = defaultView;
	
	lconn.profiles.ProfilesXSL.loadContent(
		dataUrl, 
		profiles_tagsXSLT, 
		container, 
		resourceIdArray, 
		additionalParamsMap,
		displayedUserKey, 
		true // append lastMod 
  	);
	
	if(lconn.profiles.ProfilesCore.isUserLoggedIn()) {
		lconn.core.utilities.processUntilElementIsFound(
			"add-tag-view", 
			function(){
				var dijitTagName = dijit.byId("socialTagName");
				if (dijitTagName == null) {
					dojo.parser.parse(dojo.byId("add-tag-view"));
					dijitTagName = dijit.byId("socialTagName");
				}

				dijitTagName.hintText = generalrs.socialTagsAddTagsAltText;
				dijitTagName.updateHintText();
				try {
					if (typeof focusElement === "string") focusElement = dojo.byId(focusElement);
					focusElement.focus();
				} catch (e) {}
				
				profiles_checkSearchTags();
			}, 
			null,
			null,
			false /* do not throw error if not found */
		);
	}

	// lconn.core.utilities.processUntilElementIsFound parameters: 
	// (elementId, callback, iContext, params, pThrowIfExhausted, pWaitBetweenTries, pMaxTries)
	lconn.core.utilities.processUntilElementIsFound(
		"tagCloud", 
		function(){
			var tagSliderSection = dojo.byId("tagCloud");
			if(typeof(tagSliderSection) != "undefined" && tagSliderSection != null){
				lconn.core.TagSlider.showTagVis(0, 'tagCloud');
			}
			//bidi text direction support
			lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId("socialTags"));
			
			profiles_checkSearchTags();
		}, 
		null,
		null,
		false /* do not throw error if not found */
	);
}

function profiles_showTagCloud()
{
	lconn.profiles.ProfilesCore.hide("tagsList");
	lconn.profiles.ProfilesCore.hide("tagsYouAddedView");
	lconn.profiles.ProfilesCore.show("tagCloud");

	lconn.core.utilities.processUntilElementIsFound(
		"tagCloudActionBt_disabled", 
		function(){
			try {
				dojo.cookie("profiles.socialTags.view","cloud",{expires:9999});
				dojo.byId("tagCloudActionBt_disabled").focus();
			} catch (e) {}
		}, 
		null,
		null,
		false /* do not throw error if not found */
	);	

}

function profiles_showTagList()
{
	lconn.profiles.ProfilesCore.hide("tagCloud");
	lconn.profiles.ProfilesCore.show("tagsYouAddedView");
	lconn.profiles.ProfilesCore.show("tagsList");
	
	lconn.core.utilities.processUntilElementIsFound(
		"tagListActionBt_disabled", 
		function(){
			try {
				dojo.cookie("profiles.socialTags.view","list",{expires:9999});
				dojo.byId("tagListActionBt_disabled").focus();
			} catch (e) {}
		}, 
		null,
		null,
		false /* do not throw error if not found */
	);	

}

lconn.profiles.SocialTags.saveNewTag = function(userKey, formControl) 
{
	var tagName = lconn.core.formutilities.findParentForm(formControl).elements["socialTagName"].value;
	if(tagName != null && tagName != "" && tagName != generalrs.socialTagsAddTagsAltText)
	{
		//clean new tag only
		tagName = profiles_cleanupTag(tagName);
		profiles_putTags( userKey, profiles_getCurrentTagsCSV( tagName));
		document.getElementById("social_tags").innerHTML = generalrs.socialTagsLoading;
		dojo.attr("social_tags", { "role": "alert" });
	}
}

function profiles_removeTag( userKey, tagName)
{	
	tagName = profiles_cleanupTag(tagName);
	// remove tag submits a new list of tags to be updated minus the tag to be removed
	if(tagName != null && tagName != "")
	{
		profiles_putTags( userKey, profiles_getCurrentTagsCSV( "", tagName));
		document.getElementById("social_tags").innerHTML = generalrs.socialTagsLoading;
		dojo.attr("social_tags", { "role": "alert" });
	}
}

function profiles_deleteInstancesOfTagForSelf( userKey, tagName)
{	
	tagName = encodeURIComponent(tagName);
	if(tagName != null && tagName != "" && userKey == lconn.profiles.ProfilesCore.getLoggedInUserKey())
	{
		var atomUrl = applicationContext + "/atom/forms/profileTags.do?targetKey=" + userKey + "&tag=" + tagName;
		lconn.profiles.xhrDelete({
		   	url: atomUrl,
	       	load: function(res, ioArgs) {
		       	profilesData.config.profileLastMod = new Date().getTime();
	           	profiles_loadTags(userKey, "socialTags_widgetId_container", socialTagsIContext);
	     		return res;
	   	    },
	   		checkAuthHeader: true
		});
		document.getElementById("social_tags").innerHTML = generalrs.socialTagsLoading;
	}
}


// builds and returns a CSV list of tags currently displayed by the widget
// and includes and/or omits the tags referenced by arguments 
function profiles_getCurrentTagsCSV( includeTag, excludeTag)
{
	if( typeof(includeTag) == "undefined") includeTag = "";
	if( typeof(excludeTag) == "undefined") excludeTag = "";

	// build list of tags from screen	 
	var tagList = new Array;
	var ul=document.getElementById('tagsList');
	if(ul != null)
	{
		var anchor=ul.getElementsByTagName('a');
		if(typeof(anchor.length) != "undefined")
		{
		 	for(var i=0; i<anchor.length; ++i)
		 	{
		 		if(anchor[i].className.indexOf("profileTag") != -1 && anchor[i].getAttribute("flagged") == "true")
		 		{
			 		var t=anchor[i].innerText; //innerHTML encodes < and > 
			 		//support FF
					if(t == undefined){
					   t = anchor[i].textContent;
						
					}
					//clean the tag just in case
					t = profiles_cleanupTag( t );
			 		if( excludeTag!="" && t==excludeTag) 
			 			continue; // if excluding tag, skip it
			 		tagList.push(t);
		 		}
		 	}
		}
	}
 
 	// append the tag to include if any is supplied  
 	var tags = includeTag.split(new RegExp( "[, \u3000]{1}", "g" )); 
	tagList = tagList.concat(tags);	
	return tagList;
}

function profiles_putTags( userKey, tagList )
{
	var xmlContent = "<app:categories xmlns:atom='http://www.w3.org/2005/Atom' xmlns:app='http://www.w3.org/2007/app' xmlns:snx='http://www.ibm.com/xmlns/prod/sn'>";
	for(var i=0; i<tagList.length; ++i){
		if(tagList[i] != null && tagList[i] != ""){
	 		xmlContent += "<atom:category term=\""+tagList[i]+"\"/>";
	 		}
	 		}
	xmlContent += "</app:categories>";
	
	var atomUrl = applicationContext + "/atom/forms/profileTags.do?targetKey=" + userKey + "&sourceKey=" + lconn.profiles.ProfilesCore.getLoggedInUserKey();
	lconn.profiles.xhrPut({
	   	url: atomUrl,
	   	putData: xmlContent,
       	load: function(res, ioArgs) {
	       	profilesData.config.profileLastMod = new Date().getTime();
           	profiles_loadTags(userKey, "socialTags_widgetId_container", socialTagsIContext, "addTagButtonId");
     			return res;
   	    },
	   	checkAuthHeader: true
	});

}

// method to encode characters using url encoding guidelines. This is to prevent breakage of xml parser in backend
function profiles_cleanupTag( tag )
{
	var retTag = tag;
	retTag = retTag.replace(/&/g,"&amp;"); // replace &	
	retTag = retTag.replace(/"/g,"&quot;"); // replace double quotes 
	retTag = retTag.replace(/'/g,"&apos;"); // replace single quotes 
	retTag = retTag.replace(/</g,"&lt;"); // replace less than braces
	retTag = retTag.replace(/>/g,"&gt;"); // replace greater than braces
	return retTag;
}

function profiles_canSearchTags()
{
	//if the current user has no rights to search, disable the links.
	var ok = false;
	try {
		if (dojo.exists("profilesData.loggedInUser.enabledPermissions")) {
			for (var ii = 0; ii < profilesData.loggedInUser.enabledPermissions.length; ii++) {
				if (profilesData.loggedInUser.enabledPermissions[ii] == "profile.search$profile.search.view") {
					ok = true;
					break;
				}
			}
		}
	} catch (e) {}
	
	return ok;
}

function profiles_checkSearchTags()
{
	try {
		var baseElement = dojo.byId("social_tags");
		if (!profiles_canSearchTags()) {
			dojo.query('a[onclick^="profiles_searchTag"]', dojo.byId("social_tags")).attr("aria-disabled", "true");
		}
	
		//we are going to insert wbr html nodes into every 25th character
		//of the tag display so it'll break properly if it is too long
		//for ie (ie11+ is trident7+) and msedge browsers
		if (dojo.isIE || dojo.isEdge || (dojo.isTrident && dojo.isTrident >= 7)) {
			//find all of the links in the tag cloud
			dojo.query('.lotusTagCloud a[onclick^="profiles_searchTag"]', baseElement).forEach(function(elTag) {
				var fullTag = elTag.innerText || elTag.textContent;
				if (fullTag.length > 25) {
					elTag.innerHTML = ""; //clear out the existing tag display
					var partsTag = fullTag.match(/[\s\S]{1,25}/g) || []; //this splits the text into blocks of 25 characters
					dojo.forEach(partsTag, function(part, idx) {
						if (idx > 0) {
							//insert a zero-width space to allow for word breaks
							elTag.appendChild(dojo.doc.createTextNode("\u200B"));
						}
						elTag.appendChild(dojo.doc.createTextNode(part));
					});
				}
			});			
		}	
	} catch (e) {
		if (window.console) console.error(e);
	}
}

function profiles_searchTag(tag)
{
	if (profiles_canSearchTags()) {
		var url = applicationContext + "/html/simpleSearch.do?profileTags="+ encodeURIComponent(tag) + "&isSimpleSearch=true";
		profiles_goto(url, true);
	}
}

function profiles_goToProfile(profile)
{
	var url = applicationContext + "/html/profileView.do?key="+ profile;
	profiles_goto(url);
}
