/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

function printpage() {
	parent.mainpage.print();  
}

function size() {
	var image = document.getElementById("imgsize").src;
	if(image.indexOf("max")>=0){
		parent.document.getElementById('myFrameset').setAttribute('cols', '0%,100%');
		document.getElementById('imgsize').src = "images/restore.gif";
		document.getElementById('imgsize').alt= "Restore";
		document.getElementById('Contents').innerHTML="";
	}else{
		parent.document.getElementById('myFrameset').setAttribute('cols', '30%,*');
		document.getElementById('imgsize').src = "images/maximize.gif";
		document.getElementById('imgsize').alt= "Maximize";
		document.getElementById('Contents').innerHTML="Contents";
	}
}

function bookmark() {

 title = parent.mainpage.document.title;
 url = parent.mainpage.location.href;

 if (window.sidebar) { // Mozilla Firefox Bookmark
   try {
         window.sidebar.addPanel(title, url,"");
      }catch (e) {
         alert('To bookmark this page, right click than select This Frame > Bookmark This Frame...');
      }
 } else if(document.all) { // IE Favorite
	try {
		window.external.AddFavorite( url, title);
      }catch (e) {
 		alert('To bookmark this page, right click than select Add to Favorites...')
	}
}
}


function getpage() {
	var page = window.parent.location.href;
	var query = page.indexOf("?");
	if(query>0){
		page = page.substring(query+1);
		parent.mainpage.location = page;
	}

}

function change_src(file, titleNew){
      var current = parent.location.href;
 	var url = current.indexOf("?");
	if(url>0){
		page = current.substring(0, url);
		parent.location = page +'?doc/' + file;
		
	}else{
		parent.location = current + "?doc/" + file;
	}
	var agt=navigator.userAgent.toLowerCase();
	if (agt.indexOf("safari") != -1){
		parent.mainpage.location = "doc/" + file;
	}
}
