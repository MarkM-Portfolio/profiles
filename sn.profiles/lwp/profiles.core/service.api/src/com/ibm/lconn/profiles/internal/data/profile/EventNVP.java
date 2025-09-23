/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.data.profile;

// helper class designed to hold the mapping between the infra-based public api for publishing
// events and the internal profiles representation. the one attribute causing an issue is 
// orgId (public) vs. tenantKey (internal).
public class EventNVP {
	public String prop;   // public property id (e.g. orgId)
	public String attr;   // internal attribute to retrieve (e.g. tenantKey)
	EventNVP(String prop, String attr){
		this.prop = prop;
		this.attr = attr;
	}
}
