/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import com.ibm.lconn.profiles.data.codes.AbstractCode;

import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.BaseCodesService;
import com.ibm.lconn.profiles.internal.service.EmployeeTypeService;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class EmployeeTypeAction extends BaseCodesAction // APIAction
{
	private final static String searchType = PeoplePagesServiceConstants.ECODE;

	@Override
	protected BaseCodesService<?> getService()
	{
		if (null == service)
			service = AppServiceContextAccess.getContextObject(EmployeeTypeService.class);
		return service;
	}

	@Override
	protected String getSearchType()
	{
		return searchType;
	}

	@Override
	protected void doCreate(AbstractCode<?> code) {
	    Object tmp = code;
	    if (tmp instanceof EmployeeType)
	    {
	    	EmployeeType ccode = (EmployeeType)tmp;
			((EmployeeTypeService)getService()).create(ccode);
//			((EmployeeTypeService)getService()).create((EmployeeType) code);
	    }
	}

	@Override
	protected void doUpdate(AbstractCode<?> code) {
	    Object tmp = code;
	    if (tmp instanceof EmployeeType)
	    {
	    	EmployeeType ccode = (EmployeeType)tmp;
			((EmployeeTypeService)getService()).update(ccode);
//			((EmployeeTypeService)getService()).update((EmployeeType) code);
	    }
	}

}
