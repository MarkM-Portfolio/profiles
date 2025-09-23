/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import javax.activation.MimeType;

/*
 * SyncResponse is used to retain the HTTP response from Abdera to be passed back to the caller.
 * The SC Sync code closes the stream when done using Abdera; so the content would not available to the caller.
 */
public class SyncResponse
{
	private int      responseCode    = 0;
	private Long     responseStatus  = 0L;
	private String   responseMessage = null;
	private String   responseBody    = null;
	private MimeType responseContentType = null;
	private String   responseErrorMsg = null;

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int statusCode)	{
		this.responseCode = statusCode;
	}

	public long getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(Long status) {
		this.responseStatus = status;
	}

	public MimeType getResponseContentType() {
		return responseContentType;
	}

	public void setResponseContentType(MimeType mimeContentType) {
		this.responseContentType = mimeContentType;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String message) {
		this.responseMessage = message;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String respBody){
		this.responseBody  = respBody;		
	}

	public String getResponseErrorMsg() {
		return responseErrorMsg;
	}

	public void setResponseErrorMsg(String responseErrorMsg) {
		this.responseErrorMsg = responseErrorMsg;
	}

}
