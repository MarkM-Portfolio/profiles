/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.data;

/**
 * Class for org structure / report to paging This class only supports ordering by displayname.
 * 
 */
public class ReportToRetrievalOptions extends ProfileSetRetrievalOptions {

	private static final long serialVersionUID = -4516796995664156234L;

	public ReportToRetrievalOptions() {
		super();
		super.setOrderBy(OrderBy.DISPLAY_NAME);
	}

	public void setOrderBy(OrderBy orderBy) {
		if (orderBy == null) {
			orderBy = OrderBy.DISPLAY_NAME;
		}
		if (orderBy.equals(OrderBy.DISPLAY_NAME) == false) {
			throw new IllegalArgumentException("ReportToRetrieval.setOrderBy orderBy must be by DISPLAY_NAME");
		}
		super.setOrderBy(orderBy);
	}
}
