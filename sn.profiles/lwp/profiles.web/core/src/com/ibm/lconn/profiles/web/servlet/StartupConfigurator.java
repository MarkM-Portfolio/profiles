/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.servlet;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.util.Build;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.OptionsConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.config.types.ProfilesTypesCache;

import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfilesAppService;
import com.ibm.lconn.profiles.internal.service.cache.ProfileCache;
import com.ibm.lconn.profiles.internal.service.cache.ProfilesTagCloudCache;
import com.ibm.lconn.profiles.internal.util.ProfilesHighway;
import com.ibm.lconn.profiles.internal.util.SchemaVersionInfo;

import com.ibm.lconn.scheduler.exception.SchedulerException;
import com.ibm.lconn.scheduler.service.Scheduler;

import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;

/**
 * 
 */
public class StartupConfigurator implements ServletContextListener {
	
	private Log LOG = LogFactory.getLog(StartupConfigurator.class);

	// this system env. var. MUST be set on the OS env of the SC server
    public static final String CLOUD_DATA_FS_KEY = "DataFS";

	public void contextInitialized(ServletContextEvent cse) {

		// set build info via the Build class.		
		readBuildNumber(cse.getServletContext());

		// init spring context
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] {
						SNAXConstants.LC_APPEXT_CORE_CONTEXT,
						ProfilesServiceConstants.LC_PROFILES_MSGVECTOR_CONTEXT,
						ProfilesServiceConstants.LC_PROFILES_CORE_SERVICE_CONTEXT,
						"classpath:com/ibm/lconn/profiles/web/context/was-spring-svc-context.xml" });
		AppServiceContextAccess.setContext(applicationContext);

		LCConfig.instance();
		
		// force jndi lookup so it doesn't happen in another thread
		final ProfilesAppService appSvc = AppServiceContextAccess.getContextObject(ProfilesAppService.class);
		
		boolean sametimeAwareness = OptionsConfig.instance().isSametimeAwarenessEnabled();
		String sametimeUnSecureHref = null;
		String sametimeSecureHref = null;
		
		LOG.info("sametimeAwareness: " + sametimeAwareness);
		if (sametimeAwareness) {
			sametimeUnSecureHref = ProfilesConfig.instance()
					.getSametimeConfig().getSametimeUnsecureHref();
			sametimeSecureHref = ProfilesConfig.instance().getSametimeConfig()
					.getSametimeSecureHref();
		}

		String sametimeInputType = ProfilesConfig.instance()
				.getSametimeConfig().getSametimeInputType();

		// set Sametime values
		cse.getServletContext().setAttribute("sametimeAwareness",
				sametimeAwareness);
		cse.getServletContext().setAttribute("sametimeUnSecureHref",
				sametimeUnSecureHref);
		cse.getServletContext().setAttribute("sametimeSecureHref",
				sametimeSecureHref);
		cse.getServletContext().setAttribute("sametimeInputType",
				sametimeInputType);

		// setup profiles JS
		cse.getServletContext().setAttribute(
				"debugProfilesJS",
				ProfilesConfig.instance().getProperties().getBooleanValue(
						ConfigProperty.JS_DEBUGGING_ENABLED));

		// Put configuration on the application context
		cse.getServletContext().setAttribute("profilesConfig", ProfilesConfig.instance());

		// start the scheduler service
		initScheduledTasks();

		// initialize profile caches. done here so that it is not done on first read of profile.
		// also make sure they are clear.
		ProfileCache.instance().clear();

		// initialize profiles tag cloud dyna-cache.
		ProfilesTagCloudCache.getInstance().initialize();

		// initialize profiles types dyna-cache.
		ProfilesTypesCache.getInstance().initialize();
		
		// this will initialize the policy configuration and the cache
		PolicyHolder.instance().initialize();

		// initialize Highway with the default org's ProfileType & Policy string "null"
		ProfilesHighway.instance().initDefaultOrgHighwayData();

		// OCS:149395 LLC1(S33): IC Profiles: An error is shown on organization tag widget
		// moved db schema validation below the cache initialization since throwing an exception from a
		// schema mis-match does not prevent the Profiles app from starting.  that results on a NPE on
		// first access to the cache(s) since they were never initialized.
		try {
			// assert schema version
			appSvc.setSchemaVersion();
			if (SchemaVersionInfo.instance().meetsMinimumSupported() == false){
				String error = "Error: Profiles database schema is out of sync; Some features of Profiles will not be available";
				LOG.fatal(error);
			}
		}
		catch (Throwable e) {
			String error = "Error: Profiles database schema is out of sync; Some features of Profiles will not be available";
			LOG.fatal(error, e);
			// throwing an exception here has no useful effect; only this servlet fails and other parts of Profiles load and run
			// this leads to a NPE being thrown when any cache is used since it has not been initialized.
			// eg. the tag cloud shows an error message but, at that point, there is no way to determine why. 
			//throw new RuntimeException(new UnavailableException(error));
		}
		
		boolean isLotusLive = LCConfig.instance().isLotusLive();
		if (isLotusLive) // process photo sync only if on Cloud
		{
			// the root directory for saving Cloud photos for sync
			String cloudRoot = System.getenv(CLOUD_DATA_FS_KEY);
			// this system env. var. MUST be set on Cloud deployment
			if (StringUtils.isBlank(cloudRoot)) {
				LOG.error(ResourceManager.format(ResourceManager.WORKER_BUNDLE, "error.profile.sync.failed.to.init", new Object[]{CLOUD_DATA_FS_KEY, cloudRoot}) );
				// service.impl\src\com\ibm\peoplepages\internal\resources\Worker_en.properties
				// error.profile.sync.failed.to.init=CLFRN1363E: Error initializing ProfileSyncTask. This error will prevent
				// the Profiles application from synchronizing profile updates. The ''{0}'' environment variable is ''{1}''.
			}
		}
		// rtc 104328
		// Profiles is to report that it is not enabled on Cloud while it runs concurrently with Cloud Profiles
		// Print out the status for reference. We should see that profiles is not enabled on Cloud
		// but is in other MT environments (GAD). this ridiculousness should be removed after
		// profiles fully replaces cloud profiles.
		try {
			boolean isEnabledVC = VenturaConfigurationProvider.Factory.getInstance().isServiceEnabled("profiles");
			boolean isEnabledSRU = ServiceReferenceUtil.isServiceEnabled("profiles");
			LOG.info("Profiles enabled report VenturaConfiguration: " + isEnabledVC + " ServiceReferenceUtil: " + isEnabledSRU);
		}
		catch (VenturaConfigException e) {
			// now what?
			LOG.error("Error: checking enablement configuration: " + e.getMessage());
		}
	}

	public void contextDestroyed(ServletContextEvent cse) {

		// terminate & free the profiles tag cloud dyna-cache.
	    ProfilesTagCloudCache.getInstance().terminate();

	    // terminate & free the profiles types dyna-cache.
		ProfilesTypesCache.getInstance().terminate();
		
		// terminate org policy cache.
		PolicyHolder.instance().terminate();

	    // do not terminate the scheduler service - see rtc 170536
		// termScheduledTasks();
	}
	
	private void readBuildNumber(ServletContext servletContext) {
		// Build class looks for the file /com/ibm/lconn/core/util/build.properties which is created
		// at build time via build.xml instruction
		// <property name="build.properties.name" value="com/ibm/lconn/core/util/appbuild.properties"/>
		// i found classloader needs the file to be in an ear level jar.
		// this output is from a sample run
		// Build.getRelease(): 4.5.0.0
		// Build.getStream(): IC10.0_Profiles
		// Build.getBuild(): 20130521.1310D
		// Build.getCoreStream: IC10.0_Infra
		// Build.getCoreBuild: 20130522-1034
		String release = Build.getRelease(); // e.g. 4.5.0.0 or 10.0
		String stream = Build.getStream(); // e.g. IC10.0_Profiles
		String build = Build.getBuild(); // e.g. 20130521.1310D
		String buildNumber = stream+"_"+build;
		//
		servletContext.setAttribute("versionNumber", release);
		servletContext.setAttribute("buildNumber", buildNumber);
		String appStartupDate = new SimpleDateFormat("yyyyMMddHHmmSSS").format(new Date());
		servletContext.setAttribute("appStartupDate", appStartupDate);
		LOG.info("profiles release: "+release+" buildNumber: "+buildNumber+" appStartupDate: "+appStartupDate);
	}
	
	private void initScheduledTasks(){
		try{
		    Scheduler.init();
		    if (Scheduler.isAvailable() == false){
		    	LOG.error("Error: Profiles scheduled tasks were not initialized; scheduler not available");
		    }
		}
		catch(SchedulerException se){
			LOG.error("Error initializing profiles scheduled tasks", se);
		}
	}
	// see rtc 170536
	//private void termScheduledTasks(){
	//	try{
	//	    Scheduler.remove();
	//	    if (Scheduler.isAvailable() == true){
	//	    	LOG.error("Error: Profiles scheduled tasks were not terminated; scheduler is still available");
	//	    }
	//	}
	//	catch(SchedulerException se){
	//		LOG.error("Error terminating profiles scheduled tasks", se);
	//	}
	//}
}