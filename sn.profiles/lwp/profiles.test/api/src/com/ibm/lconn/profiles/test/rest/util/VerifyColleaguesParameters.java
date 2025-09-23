/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;


public class VerifyColleaguesParameters extends AbstractParameters {

	public VerifyColleaguesParameters(){
		put("connectionType","colleague");
	}
	
	public void setSourceEmail(String value){
		put("sourceEmail",  value);
	}
	public void setTargetEmail(String value){
		put("targetEmail",value);
	}
	
}
