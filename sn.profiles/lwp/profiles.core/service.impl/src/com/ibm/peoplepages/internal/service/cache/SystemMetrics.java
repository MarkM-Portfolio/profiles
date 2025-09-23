/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.peoplepages.data.ProfileTag;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
//import com.ibm.lconn.core.compint.news.microblog.api.INewsProfilesMicrobloggingMetricsService;
//import com.ibm.lconn.core.compint.news.microblog.impl.NewsMicrobloggingMetricsRemote;

/**
 * SystemMetrics 
 *
 * @author rmelanson
 */
public class SystemMetrics {

	private static final Log LOG = LogFactory.getLog(SystemMetrics.class);

	private static long lastGetTime = 0;
	//    private static long getTimeoutInterval = 3600000;  // 1 hour
	private static long getTimeoutInterval = 100;  // 1 hour

	private static HashMap<String,String> metricCache = new HashMap<String,String>();

	static Object METRIC_CACHE_LOCK = new Object();
	public static final long BAD_VAL = -1L;

	private static String metricKeyNames[] = {
		"profiles.metric.employee.count",
		"profiles.metric.employee.active.count",
		"profiles.metric.employee.inactive.count",
		"profiles.metric.employee.external.count",
		"profiles.metric.employee.internal.count",

		"profiles.metric.background.count",
		"profiles.metric.picture.count",
		"profiles.metric.pronunciation.count",
		"profiles.metric.authenticated.today",
		"profiles.metric.authenticated.last.7.days",
		"profiles.metric.authenticated.last.30.days",
		"profiles.metric.tag.profile.count",
		"profiles.metric.tag.unique.count",
		"profiles.metric.tag.total.count",
		"profiles.metric.tag.top.5",
/*
		"profiles.metric.user.board.count",
		"profiles.metric.board.count.today",
		"profiles.metric.board.total.count",
		"profiles.metric.board.comment.count",
*/
		"profiles.metric.colleage.total.count",
		"profiles.metric.colleage.count.user",
		"profiles.metric.colleage.count.accepted.user",
		"profiles.metric.colleage.count.pending.user",
		"profiles.metric.links.count"
	};

	final static int metricCount = metricKeyNames.length;

	final public static int METRIC_PEOPLE_COUNT_IX          = 0;
	final public static int METRIC_PEOPLE_COUNT_ACTIVE_IX   = 1;
	final public static int METRIC_PEOPLE_COUNT_INACTIVE_IX = 2;
	final public static int METRIC_PEOPLE_COUNT_EXTERNAL_IX = 3;
	final public static int METRIC_PEOPLE_COUNT_INTERNAL_IX = 4;

	final public static int METRIC_BACKGROUND_COUNT_IX      = 5;
	final public static int METRIC_PICTURES_COUNT_IX        = 6;
	final public static int METRIC_PRONOUNCE_COUNT_IX       = 7;
	final public static int METRIC_LOGIN1_COUNT_IX          = 8;
	final public static int METRIC_LOGIN7_COUNT_IX          = 9;
	final public static int METRIC_LOGIN30_COUNT_IX         = 10;
	final public static int METRIC_TAGPROFILE_COUNT_IX      = 11;
	final public static int METRIC_TAGUNIQUE_COUNT_IX       = 12;
	final public static int METRIC_TAGTOTAL_COUNT_IX        = 13;
	final public static int METRIC_TAGTOP5_IX               = 14;
	final public static int METRIC_BOARD_COUNT = 4;
/*
	// we really need to deprecate these 'board' metrics
	final public static int METRIC_BOARD_COUNT_IX           = 15;
	final public static int METRIC_BOARDTODAY_COUNT_IX      = 16;
	final public static int METRIC_BOARDTOTAL_COUNT_IX      = 17;
	final public static int METRIC_BOARDCMT_COUNT_IX        = 18;
*/
	final public static int METRIC_COLLEAGE_TOTAL_IX        = 19 - METRIC_BOARD_COUNT;
	final public static int METRIC_COLLEAGUE_COUNT_IX       = 20 - METRIC_BOARD_COUNT;
	final public static int METRIC_COLLEAGUE_COUNT_ACCEPTED_IX = 21 - METRIC_BOARD_COUNT; // number of accepted colleague invites STATUS=1
	final public static int METRIC_COLLEAGUE_COUNT_PENDING_IX  = 22 - METRIC_BOARD_COUNT; // number of pending  colleague invites STATUS=2
	final public static int METRIC_LINKS_COUNT_IX           = 23 - METRIC_BOARD_COUNT;

	public static HashMap fetchMetrics()
	{
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: fetchMetrics: entry:");
		// out # of metrics
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: length: " + metricCount);

		// check to see if > an hour has elapsed since last get of metrics.
		// if an hour has elapsed since the last get, re-get the metrics
		if (hourTimeoutElapsed())
		{
			if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: hour timeout did elapse: ");

			getMetrics();

			// reset time of last fetch
			lastGetTime = System.currentTimeMillis();
			if (LOG.isTraceEnabled()){
				LOG.trace("SystemMetrics: lastGetTime: " + lastGetTime);
				LOG.trace("SystemMetrics: lastGetTime date: " + new Date(lastGetTime));
			}
		}

		if (LOG.isTraceEnabled())
		{
			LOG.trace("SystemMetrics: LinksCount: " + getEmployeeCount(false));
		}

		HashMap clonedMap = null;
		synchronized (METRIC_CACHE_LOCK) {
			clonedMap = (HashMap)metricCache.clone();
		}

		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: fetchMetrics: exit:");
		return clonedMap;
	}

	private static boolean hourTimeoutElapsed()
	{
		// if an hour has elapsed since the last get, re-get the metrics
		if ((System.currentTimeMillis() - lastGetTime) > getTimeoutInterval)
			return true;
		else
			return false;
	}


	// note that 'fetchMetrics()' is NOT called by this method, i.e., values are
	// not refreshed.  Also, it is assumed that there has been a prior call to 
	// fetchMetrics() somehow.
	public static String getMetricValueForKey( String key)
	{
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetricValueForKey: entry: key:" + key);
		String retStr = null;

		synchronized (METRIC_CACHE_LOCK) {
			retStr = ((String)(metricCache.get(key)));
		}

		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetricValueForKey: exit: retLong:" + retStr);
		return( retStr);
	}


	public static String getMetricDescriptionForKey( String key)
	{
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetricDescriptionForKey: entry: key:" + key);

		return( "not used in profiles");
	}

	public static long getEmployeeCount( boolean bDoTimeCheck)
	{
		if (bDoTimeCheck && hourTimeoutElapsed()) {
			fetchMetrics();
		}

		long tmplong = -1;
		// jtw - why is this synchronized versus any other getter?
		synchronized (METRIC_CACHE_LOCK) {
			try{
				tmplong = new Long(metricKeyNames[METRIC_PEOPLE_COUNT_IX]);
			}
			catch(Exception ignore){}
		}

		return tmplong;
	}

	public static String getMetricKeyName(int ix)
	{
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetricKeyName: entry: ix: " + ix);
		String retStr = null;

		if ((ix < 0)|| (ix >= metricCount))
			retStr = null;
		else
			retStr = metricKeyNames[ix];

		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetricKeyName: exit: retStr: " + retStr);
		return retStr;
	}

	public static String[] getMetricKeyNameArray()
	{	
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetricKeyNameArray: entry:");
		return metricKeyNames;
	}

	public static long getLastGetTime()	// or now if we haven't gotten metrics yet
	{	
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getLastGetTime: entry,exit: lastGetTime: " + lastGetTime);
		long retLastGetTime = lastGetTime;

		// if we haven't gotten metrics yet, return now assuming we are about to get them
		if (retLastGetTime == 0)
			retLastGetTime = System.currentTimeMillis();

		return retLastGetTime;
	}

	private static void getMetrics()
	{
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: getMetrics: entry:");

		synchronized (METRIC_CACHE_LOCK) {
			metricCache.clear();

			if (LCConfig.instance().isMTEnvironment() == true) {
				if (LOG.isTraceEnabled()){
					LOG.trace("detected MT environment, no metrics");
				}
				return;
			} 
			else {
				ProfileDao profileDao = AppServiceContextAccess.getContextObject(ProfileDao.class);
				PhotoService photoService = AppServiceContextAccess.getContextObject(PhotoService.class);
				PronunciationService pronunciationService = AppServiceContextAccess.getContextObject(PronunciationService.class);
				ProfileLoginService loginSvc = AppServiceContextAccess.getContextObject(ProfileLoginService.class);
				ProfileTagService tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);
				ConnectionService connectSvc = AppServiceContextAccess.getContextObject(ConnectionService.class);
				ProfileExtensionService extSvc = AppServiceContextAccess.getContextObject(ProfileExtensionService.class);

				getMetricLong(profileDao, METRIC_PEOPLE_COUNT_IX);
				getMetricLong(profileDao, METRIC_PEOPLE_COUNT_ACTIVE_IX);
				getMetricLong(profileDao, METRIC_PEOPLE_COUNT_INACTIVE_IX);
				getMetricLong(profileDao, METRIC_PEOPLE_COUNT_EXTERNAL_IX);
				getMetricLong(profileDao, METRIC_PEOPLE_COUNT_INTERNAL_IX);

				getMetricLong(profileDao, METRIC_BACKGROUND_COUNT_IX);
				getMetricLong(photoService, METRIC_PICTURES_COUNT_IX);
				getMetricLong(pronunciationService, METRIC_PRONOUNCE_COUNT_IX);

				getMetricLong(loginSvc, METRIC_LOGIN1_COUNT_IX);
				getMetricLong(loginSvc, METRIC_LOGIN7_COUNT_IX);
				getMetricLong(loginSvc, METRIC_LOGIN30_COUNT_IX);

				getMetricLong(tagSvc, METRIC_TAGPROFILE_COUNT_IX);
				getMetricLong(tagSvc, METRIC_TAGUNIQUE_COUNT_IX);
				getMetricLong(tagSvc, METRIC_TAGTOTAL_COUNT_IX);
				getMetricString(tagSvc, METRIC_TAGTOP5_IX);

				getMetricLong(connectSvc, METRIC_COLLEAGE_TOTAL_IX);  // number of 'colleague' items in PROF_CONNECTIONS table
				getMetricLong(connectSvc, METRIC_COLLEAGUE_COUNT_IX); // number of profiles with colleagues

				getMetricLong(connectSvc, METRIC_COLLEAGUE_COUNT_ACCEPTED_IX); // number of accepted colleague invites STATUS=1
				getMetricLong(connectSvc, METRIC_COLLEAGUE_COUNT_PENDING_IX);  // number of pending  colleague invites STATUS=2

				getMetricLong(extSvc, METRIC_LINKS_COUNT_IX);

				/* march 16 2015 : no more Board metrics - they are owned by HomePage, NOT Profiles
				try {
					// guard the calls to new via ejb. one concern here is initialization. as of 4.0, there
					// is no const the call to setComponentName.
					NewsMicrobloggingMetricsRemote newsMetricsSvc = new NewsMicrobloggingMetricsRemote();
					NewsMicrobloggingMetricsRemote.setComponentName("profiles");
					// we must specify the class loader to the news service so it can be used when the
					// LCRemoteServiceFactory instantiates a service. This is necessary in the mbean context
					NewsMicrobloggingMetricsRemote.setClassLoader(INewsProfilesMicrobloggingMetricsService.class.getClassLoader());
					getMetricNews(newsMetricsSvc, METRIC_BOARD_COUNT_IX);
					getMetricNews(newsMetricsSvc, METRIC_BOARDTODAY_COUNT_IX);
					getMetricNews(newsMetricsSvc, METRIC_BOARDCMT_COUNT_IX);
					getMetricNews(newsMetricsSvc, METRIC_BOARDTOTAL_COUNT_IX);
				}						
				catch (Exception ex) {
					// rtc 109391 - seeing a 'null' message via an exception propagated back from homepage
					// print stack trace as well, which has info
					LOG.error("SystemMetrics: exception calling news metrics message: " + ex.getMessage());
					LOG.error("SystemMetrics: exception calling news metrics stack: " + ex.getStackTrace());
				}
				*/
			}
		}
		if (LOG.isTraceEnabled())
			LOG.trace("SystemMetrics: getMetrics: exit:");
		return;
	}


	public static boolean setCacheTimeoutMinutes(int newCacheTimeout)
	{
		if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: setCacheTimeoutMinutes: entry: newCacheTimeout: " + newCacheTimeout);

		// check cache timeout reset (by qe)
		// don't reset if timeout is < 0 or > 1 day
		if (newCacheTimeout >= 0 && newCacheTimeout <= 1440)
		{
			if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: setCacheTimeoutMinutes: entry: getTimeoutInterval: " + getTimeoutInterval);
			getTimeoutInterval = newCacheTimeout * 60000;
			if (LOG.isTraceEnabled()) LOG.trace("SystemMetrics: setCacheTimeoutMinutes: entry: getTimeoutInterval: " + getTimeoutInterval);
			return true;
		}

		return false;
	}


	private static void getMetricLong(Object svcInstance, int index)
	{
		long n = BAD_VAL;

		long now = System.currentTimeMillis();
		long day = 1000 * 60 * 60 * 24;
		long week = day * 7;
		long month = day * 30;

		String metricName = "unknown";
		try {
			metricName = metricKeyNames[index];
			if (LOG.isTraceEnabled()) {
				String tenantKey = AppContextAccess.getContext().getTenantKey();
				LOG.trace("\n\nSystemMetrics: getMetricLong: " + metricName + " using tenantKey " + tenantKey);
			}
			switch(index) {
			case METRIC_PEOPLE_COUNT_IX:
				n = ((ProfileDao)svcInstance).countProfiles();
				break;
			case METRIC_PEOPLE_COUNT_ACTIVE_IX:
				n = ((ProfileDao)svcInstance).countProfiles(METRIC_PEOPLE_COUNT_ACTIVE_IX);
				break;
			case METRIC_PEOPLE_COUNT_INACTIVE_IX:
				n = ((ProfileDao)svcInstance).countProfiles(METRIC_PEOPLE_COUNT_INACTIVE_IX);
				break;
			case METRIC_PEOPLE_COUNT_EXTERNAL_IX:
				n = ((ProfileDao)svcInstance).countProfiles(METRIC_PEOPLE_COUNT_EXTERNAL_IX);
				break;
			case METRIC_PEOPLE_COUNT_INTERNAL_IX:
				n = ((ProfileDao)svcInstance).countProfiles(METRIC_PEOPLE_COUNT_INTERNAL_IX);
				break;

			case METRIC_BACKGROUND_COUNT_IX:
				n = ((ProfileDao)svcInstance).countProfilesWithBackground();
				break;

			case METRIC_PICTURES_COUNT_IX:
				n = ((PhotoService)svcInstance).countProfilesWithPictures();
				break;

			case METRIC_PRONOUNCE_COUNT_IX:
				n = ((PronunciationService)svcInstance).countUsersWith();
				break;

			// the login counts are not correct, but are consistent with other services.
			// today should be time since midnight, and should user calendar class to do
			// deltas to account for dst boundaries
			case METRIC_LOGIN1_COUNT_IX:
				n = ((ProfileLoginService)svcInstance).count(new Date(now - day));
				break;

			case METRIC_LOGIN7_COUNT_IX:
				n = ((ProfileLoginService)svcInstance).count(new Date(now - week));
				break;

			case METRIC_LOGIN30_COUNT_IX:
				n = ((ProfileLoginService)svcInstance).count(new Date(now - month));
				break;

			case METRIC_TAGPROFILE_COUNT_IX:
				n = ((ProfileTagService)svcInstance).countEmployeesWithTags();
				break;

			case METRIC_TAGUNIQUE_COUNT_IX:
				n = ((ProfileTagService)svcInstance).countUniqueTags();
				break;

			case METRIC_TAGTOTAL_COUNT_IX:
				n = ((ProfileTagService)svcInstance).countTotalTags();
				break;

			case METRIC_COLLEAGE_TOTAL_IX:
				n = ((ConnectionService)svcInstance).countTotalCollegues();
				break;

			case METRIC_COLLEAGUE_COUNT_IX:
				n = ((ConnectionService)svcInstance).countProfilesWithCollegues();
				break;				

			case METRIC_COLLEAGUE_COUNT_ACCEPTED_IX: // number of accepted colleague invites STATUS=1
				n = ((ConnectionService)svcInstance).countProfilesWithCollegues(METRIC_COLLEAGUE_COUNT_ACCEPTED_IX);
				n = n / 2; // 
				break;				

			case METRIC_COLLEAGUE_COUNT_PENDING_IX:  // number of pending  colleague invites STATUS=2
				n = ((ConnectionService)svcInstance).countProfilesWithCollegues(METRIC_COLLEAGUE_COUNT_PENDING_IX);
				break;				

			case METRIC_LINKS_COUNT_IX:
				n = ((ProfileExtensionService)svcInstance).countProfilesWithLinks();
				break;				
			}
		}
		catch (Exception ex) {
			LOG.error("SystemMetrics: getMetricLong: error getting metric " + metricName);
			LOG.error("SystemMetrics: getMetricLong: general exception:   " + ex.getMessage());
			ex.printStackTrace();
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("SystemMetrics: getMetricLong: " + metricName + ", value = " + n);
		}
		metricCache.put(metricName, String.valueOf(n));
	}


	private static void getMetricString(Object svcInstance, int index) {
		String s = "";

		try {
			switch (index) {
				case METRIC_TAGTOP5_IX:
					StringBuilder tagTopFive = new StringBuilder(500);
					List<ProfileTag> tags = ((ProfileTagService)svcInstance).topFiveTags().getTags();
					int i = 0;
					int numTags = tags.size();
					if (numTags > 0) {
						for (i = 0; i < numTags; i++) {
							ProfileTag tag = tags.get(i);
							if (tag.getTag().length() > 0) {
								tagTopFive.append(tag.getTag());
								tagTopFive.append(" ");
								tagTopFive.append(tag.getFrequency());
								if (i != numTags - 1) {
									tagTopFive.append("; ");
								}
							}
						}
					}
					else {
						tagTopFive.append("-");
					}
					s = tagTopFive.toString();
					break;
			}

		} catch (Exception ex) {
			LOG.error("SystemMetrics: error getting metric " + metricKeyNames[index]);
			LOG.error("SystemMetrics: general exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("SystemMetrics: " + metricKeyNames[index] + ", value=" + s);
		}
		metricCache.put(metricKeyNames[index], s);
	}

//	private static void getMetricNews(
//			NewsMicrobloggingMetricsRemote newsMetricsSvc, int index) {
//		Long val;
//		long n = BAD_VAL;
//
//		try {
//			val = newsMetricsSvc.fetchMetric(metricKeyNames[index]);
//			n = (val != null) ? val : BAD_VAL;
//
//			switch (index) {
//				case METRIC_BOARD_COUNT_IX:
//					LOG.trace("SystemMetrics: employeesWithBoard: " + n);
//					break;
//
//				case METRIC_BOARDTODAY_COUNT_IX:
//					LOG.trace("SystemMetrics: boardTotalCount: " + n);
//					break;
//
//				case METRIC_BOARDTOTAL_COUNT_IX:
//					LOG.trace("SystemMetrics: boardToday: " + n);
//					break;
//
//				case METRIC_BOARDCMT_COUNT_IX:
//					LOG.trace("SystemMetrics: boardCommentCount: " + n);
//					break;
//			}
//
//		} catch (Exception ex) {
//			LOG.error("SystemMetrics: error getting metric " + metricKeyNames[index]);
//			LOG.error("SystemMetrics: general exception: " + ex.getMessage());
//			ex.printStackTrace();
//		}
//		if (LOG.isTraceEnabled()) {
//			LOG.trace("SystemMetrics: " + metricKeyNames[index] + ", value="+ n);
//		}
//		metricCache.put(metricKeyNames[index], String.valueOf(n));
//	}

}
