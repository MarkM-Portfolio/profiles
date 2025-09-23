/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.GivenName;
import com.ibm.lconn.profiles.internal.service.store.interfaces.GivenNameDao;

/**
 *
 *
 */
@Repository(GivenNameDao.REPOSNAME)
public class GivenNameSqlMapDao extends AbstractNameSqlMapDao<GivenName> 
	implements GivenNameDao
{
	public GivenNameSqlMapDao() {
		super("GivenName");
	}
}
