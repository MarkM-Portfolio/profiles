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
package com.ibm.lconn.profiles.test.service.codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.internal.service.OrganizationService;

/*
 *
 */
public class OrganizationServiceTest extends BaseCodesServiceTest<Organization, OrganizationService> {

	public OrganizationServiceTest() {
		super(Organization.class, OrganizationService.class);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.test.service.codes.BaseCodesServiceTest#codesToAdd()
	 */
	@Override
	protected List<Organization> codesToAdd() {
		List<Organization> l = new ArrayList<Organization>();
		l.add(newOrganization("sales", "Sales Organization"));
		l.add(newOrganization("marketing", "Marketing Organization"));
		l.add(newOrganization("devel", "Software Development"));
		return l;
	}

	@Override
	protected Organization newValue(String codeId, Map<String, ? extends Object> values) {
		return new Organization(codeId, values);
	}

	private Organization newOrganization(String code, String orgTitle) {
		return new Organization(code, Collections.singletonMap(Organization.F_ORG_TITLE.getName(), orgTitle));		
	}

}
