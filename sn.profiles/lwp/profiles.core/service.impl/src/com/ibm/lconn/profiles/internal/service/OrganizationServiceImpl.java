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

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.internal.service.store.interfaces.OrganizationDao;

@Service(OrganizationService.SVCNAME)
public class OrganizationServiceImpl extends AbstractCodesService<Organization, OrganizationDao> implements OrganizationService {

	public OrganizationServiceImpl(TransactionTemplate transactionTemplate,
			OrganizationDao codesDao) {
		super(transactionTemplate, codesDao, Organization.class);
	}

}
