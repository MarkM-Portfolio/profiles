/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.appext.msgvector.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Required by Spring config in infra
 */
public class EmptyBoardConfigProps {

	private static final HashMap<String,Object> _empty = new HashMap<String,Object>(1);

	public static Map<String,Object> getBoardProps() {
		return _empty;
	}	
}
