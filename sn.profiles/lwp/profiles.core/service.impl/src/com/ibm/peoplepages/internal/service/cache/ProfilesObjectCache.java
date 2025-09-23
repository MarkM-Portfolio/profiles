/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.internal.service.cache;

import java.util.ArrayList;
import java.util.List;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.BaseCodesService;
import com.ibm.lconn.profiles.internal.service.CountryService;
import com.ibm.lconn.profiles.internal.service.DepartmentService;
import com.ibm.lconn.profiles.internal.service.EmployeeTypeService;
import com.ibm.lconn.profiles.internal.service.OrganizationService;
import com.ibm.lconn.profiles.internal.service.WorkLocationService;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfilesObjectCache 
{
	private final List<BaseCodesService<? extends AbstractCode>> codeServices;
	
	private static class Holder {
		protected static final ProfilesObjectCache instance = new ProfilesObjectCache();
	}
	
	public static ProfilesObjectCache getInstance()
	{
		return Holder.instance;
	}
	
	public static boolean isEnabled()
	{
		return ProfilesConfig.instance().getCacheConfig().getProfileObjectCache().isEnabled();
	}
	
	public ProfilesObjectCache()
	{
		//
		// init code service list
		//
		codeServices = new ArrayList<BaseCodesService<? extends AbstractCode>>();
		codeServices.add(AppServiceContextAccess.getContextObject(CountryService.class));
		codeServices.add(AppServiceContextAccess.getContextObject(DepartmentService.class));
		codeServices.add(AppServiceContextAccess.getContextObject(EmployeeTypeService.class));
		codeServices.add(AppServiceContextAccess.getContextObject(OrganizationService.class));
		codeServices.add(AppServiceContextAccess.getContextObject(WorkLocationService.class));
	}
	
	public void reloadCache()
	{		
		for (BaseCodesService<? extends AbstractCode> cs : codeServices) {
			cs.codeCache().reload();
		}
	}
	
}
