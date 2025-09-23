/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data;

/**
 *
 *
 */
public class GivenName extends AbstractName<GivenName> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2674528573685450484L;
	
	public GivenName() {}

	@Override
	public final NameType getType() {
		return NameType.GIVENNAME;
	}
}
