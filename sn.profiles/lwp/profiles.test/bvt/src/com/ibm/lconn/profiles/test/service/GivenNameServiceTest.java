/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service;

import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.GivenNameDao;

/**
 *
 *
 */
public class GivenNameServiceTest extends BaseNameServiceTest<GivenName, GivenNameService, GivenNameDao> {
	
	public GivenNameServiceTest() {
		this.minNames = GivenNameService.MINNAMES;
	}
	
	@Override
	protected GivenNameService initNameService() {
		return AppServiceContextAccess.getContextObject(GivenNameService.class);
	}

	@Override
	protected GivenNameDao initDao() {
		return AppServiceContextAccess.getContextObject(GivenNameDao.class);
	}
	

}
