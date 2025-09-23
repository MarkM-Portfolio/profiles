/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class APIException extends Exception 
{
	public static enum ECause
	{
		INVALID_REQUEST,
		FORBIDDEN,
		INVALID_XML_CONTENT,
		INVALID_OPERATION,
		INVALID_CONTENT_TYPE
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7536903789827072552L;

	private ECause eCause;
	
	/**
	 * 
	 */
	public APIException(ECause eCause) {
		this.eCause = eCause;
	}
	
	/**
	 * Returns the cause of the api exception
	 * @return
	 */
	public ECause getECause() {
		return eCause;
	}
	
}
