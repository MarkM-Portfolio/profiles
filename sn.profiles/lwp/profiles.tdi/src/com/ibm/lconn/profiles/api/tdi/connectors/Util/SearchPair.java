/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.connectors.Util;

public class SearchPair {
	private String _searchKey = "";
	private String _searchValue = "";
	public String get_searchKey() {
		return _searchKey;
	}
	public void set_searchKey(String key) {
		_searchKey = key;
	}
	public String get_searchValue() {
		return _searchValue;
	}
	public void set_searchValue(String value) {
		_searchValue = value;
	}
}
