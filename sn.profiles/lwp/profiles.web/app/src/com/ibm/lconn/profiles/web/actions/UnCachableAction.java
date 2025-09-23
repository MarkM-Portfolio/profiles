/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.actions;

import javax.servlet.http.HttpServletRequest;

/**
 * @author user
 *
 */
public abstract class UnCachableAction extends BaseAction {

	public UnCachableAction() {
		this.isPublic = false;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.web.actions.BaseAction#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected final long getLastModified(HttpServletRequest request) throws Exception {
		return UNDEF_LASTMOD;
	}
	
	@Override
	protected boolean doCache(){
		return false;
	}
}
