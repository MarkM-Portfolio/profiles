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

/* @author Ronny A. Pena                                             */

// JavaScript Document to determine location to place popup

dojo.provide("lconn.profiles.popup");
// Suggest removal of this code 
dojo.deprecated("lconn.profiles.popup", "Replace with dijit.Menu and lconn.core.MenuUtility", "3.5");

var gvMenu=false;
function showMenu(pMenu,e,bidir){

	hideMenu(e);//before we pop up this menu, hide any open menus
	var vSrc = (e.target) ? e.target : e.srcElement; /*gets event target, depending on browser*/
	var vMenu=document.getElementById(pMenu);
	//sets the menu position
	//normal position is a tad offset from the element that triggered the menu
	
	// to get the position for the drop down menu in case of RTL and LTR
	var menuLeftOffset;
	if( typeof( bidir) != "undefined" && bidir != null && bidir =='rtl') {
		menuLeftOffset = (vMenu.offsetWidth > 0)? vMenu.offsetWidth : 175; // assume default value for menu if offsetWidth is zero. 		
		menuLeftOffset = 24 - menuLeftOffset;
	}else{
		menuLeftOffset = vSrc.offsetWidth - 24	;
	}
	
	var vTop = (menuGetOffsetTop(e) + vSrc.offsetHeight - 10);	
	var vLeft = (menuGetOffsetLeft(e) + menuLeftOffset);
	
	
	//check to make sure position is not offscreen and adjust, if it is
	if (document.documentElement){ //IE 6.0+
		var vBody = document.documentElement;		
	}else{
		var vBody = document.body;
	}	
	if (window.innerHeight){
		var vHeight = window.innerHeight;
		var vWidth = window.innerWidth;
	}else{
		var vHeight = vBody.clientHeight;
		var vWidth = vBody.clientWidth;
	}
	if ((vTop + vMenu.offsetHeight) > vBody.offsetHeight) vTop -= vMenu.offsetHeight;	
	if ((vLeft + vMenu.offsetWidth) > vBody.offsetWidth) vLeft -= vMenu.offsetWidth;
	//check to make sure our adjustments didn't result in values less than 0 and reset to 0 if they did.
	if (vTop < 0) vTop=0;
	if (vLeft < 0) vLeft=0;
	
	//set the style
	vMenu.style.top = vTop + "px";
	vMenu.style.left = vLeft  + "px";
	vMenu.style.display="block";
	gvMenu=vMenu;
	e.cancelBubble=true;
   
}

function menuGetOffsetTop(event){
	if (event.pageY) {
		return event.pageY;
	}
	
	if (navigator.userAgent.indexOf('MSIE') != -1) {
		var yOffset = event.clientY;
		return document.body.scrollTop + yOffset;
	}
}

function menuGetOffsetLeft(event){
	if (event.pageX) {
		return event.pageX;
	}
	
	if (navigator.userAgent.indexOf('MSIE') != -1) {
		var xOffset = event.clientX;
		return document.body.scrollLeft + xOffset;
	}
}

function handleClick(event){
	hideMenu(event);
}

function hideMenu(e){
	if (!gvMenu) return;
	gvMenu.style.display="none";
	gvMenu=false;
}
