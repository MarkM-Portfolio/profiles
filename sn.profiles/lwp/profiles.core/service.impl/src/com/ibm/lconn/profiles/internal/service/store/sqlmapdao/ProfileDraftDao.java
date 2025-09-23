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

package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Repository;


/**
 * @author colleen
 */
@Repository(ProfileDraftDao.REPOSNAME)
public interface ProfileDraftDao
{
	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.sqlmapdao.ProfileDraftDao";
	
	/**
	 * Write values into the draft table
	 * 
	 * @param draftValues
	 */
	void recordDraftValues(Map<String,Object> draftValues);
	
	/**
	 * Purge the draft table older than a certain date
	 * 
	 * @param olderThan
	 */
	void purgeTable(Date olderThan);    
}
