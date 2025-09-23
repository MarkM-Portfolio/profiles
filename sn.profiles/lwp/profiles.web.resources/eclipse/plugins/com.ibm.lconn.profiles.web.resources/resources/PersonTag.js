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

/* author: Ronny A. Pena                                             */

dojo.provide("lconn.profiles.PersonTag");
dojo.require("lconn.core.globalization.bidiUtil");

// @deprecated; use profiles_AddLiveNameSupport
function profiles_AddVCard(elemId)
{
	profiles_AddLiveNameSupport(elemId);
}

function profiles_AddLiveNameSupport(elemId)
{
	var temp = function(){
		//SemTagSvc.parseDom(null, elemId);
		setTimeout("SemTagSvc.parseDom(null, '" + elemId + "')", 1500 );
		//SemTagPerson.processHcards(SemTagSvc.getNodes('hcard'));
		//SemTagPerson.processMailtos(SemTagSvc.getNodes('mailto'));
		lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage(dojo.byId(elemId));
	};
	processUntilElementIsFound(elemId, temp);
} 
