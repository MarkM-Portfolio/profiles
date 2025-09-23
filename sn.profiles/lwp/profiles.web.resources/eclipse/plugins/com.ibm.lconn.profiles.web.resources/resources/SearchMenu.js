/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/* author: Tony Estrada                                              */

dojo.provide("lconn.profiles.SearchMenu");

dojo.require("dijit.Menu");

lconn.profiles.SearchMenu.init = function() {
	var dojoMenuWidget = null;
	var params = { id: "searchPopup", style:"display:none"};
	dojoMenuWidget = new dijit.Menu( params, dojo.byId('searchCentral') );
	
	var searchByStdName		= new dijit.MenuItem({ label: generalrs.label_header_searchby_stdname,		
												   title: generalrs.label_header_searchby_stdname_description, 
												   value: "name",	iconClass:"dijitMenuItemIcon lotusCheckmark"} );
												   
	var searchByKanjiName	= new dijit.MenuItem({ label: generalrs.label_header_searchby_kanjiname,	
												   title: generalrs.label_header_searchby_kanjiname_description, 
												   value: "kanjiName"});
												   
	var searchByDepartment	= new dijit.MenuItem({ label: generalrs.label_header_searchby_company,		
												   title: generalrs.label_header_searchby_company_description, 
												   value: "department"});
												    
	var searchByPhoneNumber	= new dijit.MenuItem({ label: generalrs.label_header_searchby_phone,		
												   title: generalrs.label_header_searchby_phone_description, 
												   value: "phoneNumber"});
												    
	var searchByJobTitle	= new dijit.MenuItem({ label: generalrs.label_header_searchby_jobtitle,		
												   title: generalrs.label_header_searchby_jobtitle_description, 
												   value: "jobTitle"});
											
	var searchByEmail		= new dijit.MenuItem({ label: generalrs.label_header_searchby_email,		
												   title: generalrs.label_header_searchby_email_description, 
												   value: "email"});
												   
	var searchByTags		= new dijit.MenuItem({ label: generalrs.label_header_searchby_profiletags,	
												   title: generalrs.label_header_searchby_profiletags_description, 
												   value: "profileTags"});
												    
	var searchByCommunity	= new dijit.MenuItem({ label: generalrs.label_header_searchby_community,	
												   title: generalrs.label_header_searchby_community_description, 
												   value: "community"});

	dojoMenuWidget.addChild(searchByStdName);
	dojoMenuWidget.addChild(searchByKanjiName);
	dojoMenuWidget.addChild(searchByDepartment);
	dojoMenuWidget.addChild(searchByPhoneNumber);
	dojoMenuWidget.addChild(searchByJobTitle);
	if( typeof(bShowEmail) == "undefined" || bShowEmail ) dojoMenuWidget.addChild(searchByEmail);
	dojoMenuWidget.addChild(searchByTags);
	dojoMenuWidget.addChild(searchByCommunity);

	dojo.connect(dojoMenuWidget, 'onItemClick', 'lconn_profiles_SearchMenu_setSearchSelection');
}

lconn_profiles_SearchMenu_setSearchSelection = function( selected ) {

	var menu = dijit.byId("searchPopup");
	
	// set class for all menu items as checked/unchecked
	var items = menu.getChildren();
   	for(var i=0; i < items.length; i++)
   	{
   		var item = items[i];
   		if( dojo.string.trim(item.value) == dojo.string.trim(selected.value) ) {
			dojo.addClass(item.iconNode, "dijitMenuItemIcon lotusCheckmark");
			        								
		} else { //reset the classes of others
			dojo.removeClass(item.iconNode, "dijitMenuItemIcon lotusCheckmark");
		}
   	}
   	
   	// set the text field to show the selected menu choice
	var searchFor = document.getElementById("searchFor");
	if( searchFor ) {
		searchFor.className = "lotusText lotusInactive"; 
		searchFor.title = selected.title; 
		searchFor.value = selected.label;
	}

	// set the searchby url arg 
	var searchBy = document.getElementById("searchBy");
	if( searchBy ) {
		searchBy.value = selected.value;
	}
	
	var foo=dijit.popup.close( menu );
}