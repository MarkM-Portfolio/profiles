/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.cache;

import java.util.Calendar;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.CacheConfig.ObjectCacheConfig;
import com.ibm.peoplepages.internal.resources.ResourceManager;

public class CacheService
{
	private Log LOG = LogFactory.getLog(CacheService.class);

	private static CacheService cacheService = null;

	private Timer fullTimer = new Timer(true);
	private Timer objectTimer = new Timer(true);

	private boolean fullTimerStarted = false;
	private boolean objectTimerStarted = false;

    private boolean fullCacheEnabled = false;

	public static CacheService getInstance()
	{
		return getInstance(true);
	}
	
	public static CacheService getInstance(boolean initOrgStruct)
	{
		if (cacheService == null)
		{
			cacheService = new CacheService(initOrgStruct);
		}
		return cacheService;
	}

	/**
	 * This creation method does a simple init of a hashtable with 500 elements.
	 */
	private CacheService(boolean initOrgStruct)
	{
		DataAccessConfig config = DataAccessConfig.instance();
		if (initOrgStruct && config.isOrgStructureEnabled())
		{
			LOG.info(ResourceManager.getString("info.orgStructureEnabled"));
			initFullReportsToChainCache();
		}
		else
		{
			LOG.info(ResourceManager.getString("info.orgStructureDisabled"));
		}
		
		initProfilesObjectCache();
	}

	private void initFullReportsToChainCache()
	{
		ObjectCacheConfig occ = ProfilesConfig.instance().getCacheConfig().getFullReportsToChainConfig();
		if (occ.isEnabled())
		{
			LOG.info(ResourceManager.getString("info.fullReportsToEnabled"));
			int _startDelay = occ.getStartDelay();
			int _taskInterval = occ.getRefreshInterval();
			String refreshTimeOfDay = occ.getRefreshTime();
			enableFullCache(_startDelay, _taskInterval, refreshTimeOfDay);
		}
		else
		{
			LOG.info(ResourceManager.getString("info.fullReportsToDisabled"));
		}
	}

	public void enableFullCache(int startDelay, int refreshInterval, String refreshTimeOfDay)
	{
		// TODO: need to make sure that the full cache is enabled.
		Calendar refreshTime = getRefreshTimeCalendar(refreshTimeOfDay);
		Object[] tokens = {refreshTime.getTime()};
		LOG.info(ResourceManager.format("info.fullReportsToScheduled", tokens));

		/*    
		if (fullTimerStarted == false)
		{
			//JK rescheduling a canceled thread causes an exception
			fullTimer = new Timer(true);
		}
		*/
		// We need to cancel any existing task first. Otherwise we will end up
		// with multiple tasks running
		if ( fullTimerStarted ) {

		    LOG.info("Canceling full timer...");
		    fullTimer.cancel();
		    fullTimer = null;
		}
		
		fullTimer = new Timer(true);
		fullTimerStarted = true;
		fullTimer.schedule(new FullReportChainCacheTask(), 
				getMinutesInMilliseconds(startDelay), 
				getMinutesInMilliseconds(refreshInterval));
		
		fullCacheEnabled = true;
	}

	public void disableFullCache()
	{
		cancelFullTimer();
		FullRprtToChainCache.getInstance().resetCache();
		fullCacheEnabled = false;
	}

	public void reloadFullCache()
	{
	    /*
	    ObjectCacheConfig occ = PeoplePagesConfig.INSTANCE.getCacheConfig().getFullReportsToChainConfig();
	    if (occ.isEnabled())
	    */
	    // We need to check whether fullCache has been enabled through the command or not
	    // Not just from the config file
	    if (fullCacheEnabled)
		{
			FullRprtToChainCache.getInstance().loadCache();
		}
		else
		{
			LOG.info(ResourceManager.getString("info.fullReportsToDisabled"));
		}
	}
	
	private void initProfilesObjectCache()
	{
		ObjectCacheConfig occ = ProfilesConfig.instance().getCacheConfig().getProfileObjectCache();
		if (occ.isEnabled())
		{
			LOG.info(ResourceManager.getString("info.profilesObjectCacheEnabled"));
			int _startDelay = occ.getStartDelay();
			int _taskInterval = occ.getRefreshInterval();
			String refreshTimeOfDay = occ.getRefreshTime();
			enableObjectCache(_startDelay, _taskInterval, refreshTimeOfDay);
		}
		else
		{
			LOG.info(ResourceManager.getString("info.profilesObjectCacheDisabled"));
		}
	}

	public void enableObjectCache(int startDelay, int refreshInterval, String refreshTimeOfDay)
	{
		ProfilesObjectCache.getInstance(); // initialize
		
		Calendar refreshTime = getRefreshTimeCalendar(refreshTimeOfDay);
		if (objectTimerStarted == false)
		{
			//JK  rescheduling a canceled thread causes an exception
			objectTimer = new Timer(true);
		}	
		objectTimer = new Timer(true);
		objectTimerStarted = true;
		objectTimer.schedule(new ProfilesObjectCacheTask(), 
				getMinutesInMilliseconds(startDelay), 
				getMinutesInMilliseconds(refreshInterval));

		Object[] tokens = {refreshTime.getTime()};
		LOG.info(ResourceManager.format("info.profilesObjectCacheScheduled", tokens));
	}
	
	public void cancelFullTimer()
	{
		if (fullTimerStarted)
		{
			fullTimer.cancel();
			fullTimerStarted = false;
		}
	}
	
	public void cancelObjectTimer()
	{
		if (objectTimerStarted)
		{
			objectTimer.cancel();
			objectTimerStarted = false;
		}
	}
	private long getMinutesInMilliseconds(int minutes)
	{
		return 60000 * minutes;
	}

	private Calendar getRefreshTimeCalendar(String timeOfDay)
	{
		Calendar date = Calendar.getInstance();
		if (timeOfDay.indexOf(':') == -1)
		{
			date.set(Calendar.HOUR, new Integer(timeOfDay).intValue());
		}
		else
		{
			String hourString = timeOfDay.substring(0, timeOfDay.indexOf(':'));
			String minutesString = timeOfDay.substring(timeOfDay.indexOf(':') + 1);
			date.set(Calendar.HOUR_OF_DAY, new Integer(hourString).intValue());
			date.set(Calendar.MINUTE, new Integer(minutesString).intValue());
		}
		return date;
	}

}
