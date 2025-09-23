/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util.waltz;

import com.ibm.connections.directory.services.DSProviderFactory;

public class WaltzClientFactory {
	
	private static WaltzClientFactory INSTANCE;
	private static DSProviderFactory dspFactory;
	
	static {
		INSTANCE = new WaltzClientFactory();
	}
	
	private WaltzClientFactory(){
		dspFactory = DSProviderFactory.INSTANCE;
	}
	
	public final static WaltzClientFactory INSTANCE(){
		return INSTANCE;
	}
	
	public WaltzClient getWaltzClient(){
		WaltzClient rtn = new WaltzClient(dspFactory.getProfileProvider());
		return rtn;
	}
}
