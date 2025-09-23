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
package com.ibm.lconn.profiles.internal.service.aop;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;

import com.ibm.lconn.profiles.internal.service.OrgStructureService;

/**
 *
 *
 */
public class OrgStructureMatcher extends ProfilesServiceObjectMatcher implements Pointcut, ClassFilter {
	
	private final ClassFilter clsFilter = super.getClassFilter();
	
	/* (non-Javadoc)
	 * @see org.springframework.aop.Pointcut#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.springframework.aop.ClassFilter#matches(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean matches(Class cls) {
		return clsFilter.matches(cls) && OrgStructureService.class.isAssignableFrom(cls);
	}
	
}
