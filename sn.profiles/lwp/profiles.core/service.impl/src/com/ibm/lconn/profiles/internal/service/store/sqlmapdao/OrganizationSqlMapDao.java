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

import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.internal.service.store.interfaces.OrganizationDao;

/**
 *
 *
 */
@Repository(OrganizationDao.REPOSNAME)
public class OrganizationSqlMapDao extends AbstractCodesSqlMapDao<Organization> implements OrganizationDao {

	public OrganizationSqlMapDao() {
		super("Organization");
	}

	@Override
	protected Organization toCode(String codeId, String tenantKey, Map<String, ? extends Object> values) {
		return new Organization(codeId,tenantKey,values);
	}
	
}
