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
package com.ibm.lconn.profiles.internal.service.store.aop;

import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.stereotype.Repository;

/**
 * Pointcut to get advice applied to all methods of DAO
 * 
 *
 */
public class ProfilesDaoObjectMatcher extends AnnotationMatchingPointcut {

	/**
	 */
	public ProfilesDaoObjectMatcher() {
		super(Repository.class, true);
	}
}
