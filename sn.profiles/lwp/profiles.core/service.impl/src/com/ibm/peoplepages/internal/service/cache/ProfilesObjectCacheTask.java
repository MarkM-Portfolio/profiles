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

import java.sql.Timestamp;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ahernm@us.ibm.com
 *
 */
public class ProfilesObjectCacheTask extends TimerTask 
{
	private static final Log LOG = LogFactory.getLog(ProfilesObjectCacheTask.class);


	public void run() 
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("ProfilesObjectCacheTask starting at " + new Timestamp(System.currentTimeMillis()));
		}

		ProfilesObjectCache pobjCache = ProfilesObjectCache.getInstance();
		pobjCache.reloadCache();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("ProfilesObjectCacheTask completed at " + new Timestamp(System.currentTimeMillis()));
		}
	}

}
