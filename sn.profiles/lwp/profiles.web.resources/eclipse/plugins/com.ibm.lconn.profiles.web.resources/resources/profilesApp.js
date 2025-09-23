/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/**
 * This module is the global include for Profiles and forms the basis of
 * all script used within the application.  Do not directly require this
 * module from anything other than page level modules.
 */
dojo.provide("lconn.profiles.profilesApp");

// Pre-Dojo Javascript code, probably should remove soon
dojo.require("lconn.core.formutilities");
dojo.require("lconn.profiles.popup");

// Contributes global functions and behaviors to pages
dojo.require("lconn.profiles.profiles_help");
dojo.require("lconn.profiles.profiles_behaviours");

// "lconn.core.nls.strings",
dojo.require("com.ibm.ajax.auth");
dojo.require("lconn.core.auth.whiteListHelper");
dojo.require("lconn.core.TagSlider");
dojo.require("lconn.core.xpath");
dojo.require("lconn.core.xslt");
dojo.require("lconn.core.url");
dojo.require("lconn.core.widgetUtils");
dojo.require("lconn.core.HTMLUtil");
dojo.require("lconn.core.MenuUtility");
dojo.require("lconn.core.DateUtil");
dojo.require("lconn.core.errorhandling");
dojo.require("lconn.core.HelpLauncher");

//Accessibility
dojo.require("lconn.profiles.aria.Toolbar");
dojo.require("lconn.core.aria.TabPanel");

// Header related code
dojo.require("lconn.core.header");
dojo.require("lconn.core.LanguageSelector");

// Search Control
dojo.require("lconn.profiles.SearchBar");

// Typeahead
dojo.require("lconn.core.TypeAhead");
dojo.require("lconn.core.TypeAheadDataStore");
dojo.require("lconn.core.PeopleTypeAhead");
dojo.require("lconn.core.PeopleDataStore");

//iWidget framework  
dojo.require("lconn.core.WidgetPlacement");
dojo.require("lconn.core.mumOverride");

dojo.require("lconn.core.i18nOverrider"); // TODO: replace with new functions

dojo.require("lconn.profiles.api");
dojo.require("lconn.profiles.ProfilesCore");
dojo.require("lconn.profiles.Friending");
dojo.require("lconn.profiles.PersonTag");
dojo.require("lconn.profiles.ReportingStructure");
dojo.require("lconn.profiles.SocialTags");
dojo.require("lconn.profiles.MultiFeedReader");
dojo.require("lconn.profiles.PhotoCrop");
dojo.require("lconn.profiles.Following");
// "lconn.profiles.SearchMenu",
dojo.require("lconn.profiles.formBasedUtility");
dojo.require("lconn.profiles.ProfilesTypeAhead");
dojo.require("lconn.profiles.PeopleTypeAhead");
dojo.require("lconn.profiles.PeopleTypeAheadTiles");
dojo.require("lconn.profiles.contactInfo");
dojo.require("lconn.profiles.backgroundInfo");
dojo.require("lconn.profiles.profileDetails");
dojo.require("lconn.profiles.multiWidget");
dojo.require("lconn.profiles.ProfilesTagWidget"); // new class for profiles tagwidget version
dojo.require("lconn.profiles.directory.DirectoryController"); // new class for new directory design

//new iWidget Containers
dojo.require("lconn.profiles.widgets.linkRoll.Container");


//Page-specific code
dojo.require("lconn.profiles.profilesMainPage");
dojo.require("lconn.profiles.profilesNetworkPage");
dojo.require("lconn.profiles.profilesSearchPage");

// Common Tags
dojo.require("lconn.core.CommonTags.AjaxCall");
dojo.require("lconn.core.CommonTags.TagDialog");
dojo.require("lconn.core.CommonTags.FeedConverter");
dojo.require("lconn.core.CommonTags.TagWidget");

// the bizCards
dojo.require("lconn.core.people");
dojo.require("lconn.communities.bizCard.bizCard");

dojo.require("lconn.profiles.ckeditor");

//Sand JS					
//dojo.//require("lconn.sand.DYK"); //FIXME: Module no longer exists?  Remove if so
dojo.require("lconn.sand.DYKWrapped");
dojo.require("lconn.sand.socialPathWrapped");
dojo.require("lconn.sand.socialPath");
dojo.require("lconn.sand.sandSharedLC");
dojo.require("lconn.sand.sharedLC");

//bidi BTD support
dojo.require("lconn.core.globalization.bidiUtil");

dojo.requireLocalization("lconn.profiles", "ui");
dojo.requireLocalization("lconn.profiles", "attributes");

//FIXME: Use of generalrs is deprecated, all bundles should be accessed via dojo.i18n.getLocalization(...)
dojo.deprecated("generalrs", "generalrs is deprecated, all accesses to messages should be through dojo.i18n.getLocalization(...)", "3.5");
window['generalrs'] = window['lc_default'] = dojo.i18n.getLocalization("lconn.profiles", "ui");
