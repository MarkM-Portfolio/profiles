/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.actions.bssjson;


/**
 * Despite the large 'theoretical' data model supported by the API. The current Servlet only pays attention to the
 * subscriber id that is passed, the organisation id and the locale 
 * @author blooby
 *
 */
public class BSSSubscriber extends BSSObject {
	
	public static final String SUBSCRIBER_ID = "SubscriberId";
	
	public BSSSubscriber(String organizationId, String subscriberId, String locale) {
		super(organizationId, locale);
		setSubscriberId(subscriberId);
	}
	
	public String getSubscriberId() {
		return jsonObject.get(SUBSCRIBER_ID).toString();
	}
	
	public void setSubscriberId(String subscriberId) {
		if (subscriberId != null)
			jsonObject.put(SUBSCRIBER_ID, subscriberId);
	}
	
};
 
