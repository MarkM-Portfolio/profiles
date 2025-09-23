/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.internal.service.store.interfaces.GivenNameDao;

@Service(GivenNameService.SVCNAME)
public class GivenNameServiceImpl extends AbstractNameService<GivenName,GivenNameDao> 
	implements GivenNameService 
{

	@Autowired
	public GivenNameServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager, GivenNameDao giveNameDao) {
		super(txManager, giveNameDao, MINNAMES);
	}

}
