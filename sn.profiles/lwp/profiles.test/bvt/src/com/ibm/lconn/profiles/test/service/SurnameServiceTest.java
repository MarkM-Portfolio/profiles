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

import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SurnameDao;

/**
 *
 *
 */
public class SurnameServiceTest extends BaseNameServiceTest<Surname, SurnameService, SurnameDao> {

	@Override
	protected SurnameService initNameService() {
		return AppServiceContextAccess.getContextObject(SurnameService.class);
	}

	@Override
	protected SurnameDao initDao() {
		return AppServiceContextAccess.getContextObject(SurnameDao.class);
	}	

}
