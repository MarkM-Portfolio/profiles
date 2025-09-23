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
public class Surname extends AbstractName<Surname> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9019549720321633862L;

	public Surname() {}
	
	@Override
	public final NameType getType() {
		return NameType.SURNAME;
	}	
	
}
