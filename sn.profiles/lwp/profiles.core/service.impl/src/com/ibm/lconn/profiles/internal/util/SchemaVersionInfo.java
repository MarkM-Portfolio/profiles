/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2016                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.util;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SchemaVersionDao;
import com.ibm.peoplepages.internal.resources.ResourceManager;

/**
 * This class is an initial attempt to encapsulate schema information and
 * what functionality is supported as per schema versions. Schema version
 * information is held in the table SNPROF_SCHEMA
 * 
 * This class is open to any type of change change as we understand how
 * the schema version is used in our code. According to architects (Miki)
 * we are to backwards support a feature for two iterations.
 * 
 * Jay sent the refresh snippet, which is used in gatekeeper code. His
 * recommendation is keep this simple until we have a better idea how
 * this schema detection evolves.
 */
public class SchemaVersionInfo {
	
	private static final String CLASSNAME = SchemaVersionInfo.class.getName();
	private static final Logger logger = Logger.getLogger(CLASSNAME,"com.ibm.peoplepages.internal.resources.messages");
	
	public enum Feature {
//:(		CLOUD_USE_PHOTO_GUID_TABLE_FOR_CUD,
//:(		CLOUD_PHOTOGUID_USE_PHOTO_GUID_TABLE_ONLY
	}
	
	private static SchemaVersionInfo instance = new SchemaVersionInfo(); // singleton instance
	
	private static long lastRefresh = 0;
	private static long ONE_HOUR = TimeUnit.HOURS.toMillis(1);  // one hour in millis
	
	private Float dbSchemaVer = new Float(-1.0);
	//private String  preSchemaVer; don't support 'pre'
	private Float postSchemaVer = new Float(-1.0);
	
	private HashSet<Feature> supportedFeatures = new HashSet<Feature>(8); // arbitrary number hoping we don't go much beyond 2 or 4
	
	// minimum supported versions
	public static Float MIN_DBVER    = new Float(55);
	public static Float MIN_POSTVER  = new Float(0.0); // see fixup55.sql - we added POSTSCHEMAVER and left it at 0
	// currently in play schema versions
	private static Float post_cloudPhotoGuidTableExists = new Float(55.1);      // see postfixup55s.1.sql
	private static Float post_cloudPhotoGuidBackfillComplete = new Float(55.2); // see postfixup55s.2.sql
	
	private SchemaVersionInfo(){
	}
	
	public static SchemaVersionInfo instance(){
		return instance;
	}
	
	public Float getDbSchemaVer(){
		return dbSchemaVer;
	}
	
	public Float getPostSchemaVer(){
		return postSchemaVer;
	}
	
	// used to make a backup before a refresh so we can compensate for unexpected errors
	@SuppressWarnings("unchecked")
	public SchemaVersionInfo clone(){
		SchemaVersionInfo newVI = new SchemaVersionInfo();
		newVI.dbSchemaVer       = this.dbSchemaVer;
		newVI.postSchemaVer     = this.postSchemaVer;
		newVI.supportedFeatures = (HashSet<Feature>)(this.supportedFeatures.clone());
		return newVI;
	}
	
	// used to restore a backup in case we have an error during refresh.
	private void copy(SchemaVersionInfo vi){
		this.dbSchemaVer       = vi.dbSchemaVer;
		this.postSchemaVer     = vi.postSchemaVer;
		this.supportedFeatures = vi.supportedFeatures;
	}
	
	public void setSchemaVersions(Integer dbSchemaVer, String postSchemaVer){
		this.dbSchemaVer = new Float(dbSchemaVer);
		this.postSchemaVer = Float.parseFloat(postSchemaVer); // implicit conversion
	}
	
	public void init() {
		refresh();
		if (instance.meetsMinimumSupported() == true) {
			StringBuffer sb = new StringBuffer("(dbSchemaVersion,postSchemaVersion) = (");
			sb.append(SchemaVersionInfo.MIN_DBVER).append(",").append(SchemaVersionInfo.MIN_POSTVER).append(")");
			sb.append(" found (").append(SchemaVersionInfo.instance().getDbSchemaVer()).append(",");
			sb.append(SchemaVersionInfo.instance().getPostSchemaVer()).append(")");
			logger.log(Level.INFO, "info.schema.version", sb.toString());
		}
		else {
			StringBuffer sb1 = new StringBuffer("(dbSchemaVersion,postSchemaVersion) = (");
			sb1.append(SchemaVersionInfo.MIN_DBVER).append(",").append(SchemaVersionInfo.MIN_POSTVER).append(")");
			StringBuffer sb2 = new StringBuffer("(").append(SchemaVersionInfo.instance().getDbSchemaVer()).append(",");
			sb2.append(SchemaVersionInfo.instance().getPostSchemaVer()).append(")");
			String errMsg = ResourceManager.format("error.schema.version", new Object[] { sb1, sb2 });
			logger.severe(errMsg);
		}
	}
	
	public boolean supports (Feature feature){
		boolean isDebug = logger.isLoggable(Level.FINER);
		long diff = System.currentTimeMillis() - lastRefresh;
		if (isDebug){
			if (diff > ONE_HOUR){
				logger.log(Level.INFO, "schema version will be refreshed");
			}
			else{
				logger.log(Level.INFO, "no schema version refresh, diff = "+diff);
			}
		}
		if ( diff > ONE_HOUR ){
			refresh();  // this method will set lastRefresh
		}
		return supportedFeatures.contains(feature);
	}
	
	private synchronized void refresh(){
		boolean isDebug = logger.isLoggable(Level.FINER);
		if (isDebug) logger.entering(CLASSNAME,"refesh");
		//
		SchemaVersionDao svDao =  AppServiceContextAccess.getContextObject(SchemaVersionDao.class);
		SchemaVersionInfo bkup = this.clone();
		if (isDebug) logger.info("initial schema version info : "+bkup.toString());
		try{
			// retrieve the current version information - this is a db hit
			svDao.setSchemaVersion(this);
			// validate settings
			boolean rtn = meetsMinimumSupported();
			supportedFeatures.clear();
			if (rtn){
				calcSupportedFeatures();
			}
			else{
				StringBuffer sb1 = new StringBuffer("(dbSchemaVersion,postSchemaVersion) = (");
				sb1.append(SchemaVersionInfo.MIN_DBVER).append(",").append(SchemaVersionInfo.MIN_POSTVER).append(")");
				StringBuffer sb2 = new StringBuffer("(").append(SchemaVersionInfo.instance().getDbSchemaVer()).append(",");
				sb2.append(SchemaVersionInfo.instance().getPostSchemaVer()).append(")");
				String errMsg = ResourceManager.format("error.schema.version", new Object[] { sb1, sb2 });
				logger.severe(errMsg);
			}
		}
		catch (Throwable t){
			// log SEVERE error
			StringBuffer sb = new StringBuffer("unknown error reading schema version information");
			sb.append(System.getProperty("line.separator"));
			sb.append("throwable message: ").append(t.getMessage());
			logger.severe(sb.toString());
			this.copy(bkup);
		}
		lastRefresh = System.currentTimeMillis();
		if (isDebug){
			logger.info("refreshed schema version info : "+this.toString());
			if (isDebug) logger.exiting(CLASSNAME,"refesh");
		}
	}
	
	/**
	 * Determine if schema meets minimum versions.
	 */
	public boolean meetsMinimumSupported(){
		// not sure if there could be roundoff error. we increment dbVer by 1, postver by 0.1 increments
		boolean rtn = dbSchemaVer.compareTo(MIN_DBVER) >= -0.5;  // dbSchemaVer >= minVer
		rtn &= postSchemaVer.compareTo(MIN_POSTVER)    >= -0.05; // postSchemaVer >= minPost
		return rtn;
	}
	
	private void calcSupportedFeatures(){
		supportedFeatures.clear();
//:(		// postfixup55.1.sql introduces PHOTO_GUID for the cloud single photo table
//:(		boolean val = postSchemaVer.compareTo(post_cloudPhotoGuidTableExists) >= -0.05;
//:(		val &= LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_CLOUD_USE_PHOTO_GUID_TABLE_FOR_CUD,"PROFILES_CLOUD_USE_PHOTO_GUID_TABLE_FOR_CUD",true);
//:(		if (val) supportedFeatures.add(Feature.CLOUD_USE_PHOTO_GUID_TABLE_FOR_CUD);
//:(		// we need postSchemaVer 55.2 and a gatekeeper flag to indicate a transition to use the PHOTO_GUID table on cloud
//:(		val = LCConfig.instance().isEnabled(LCSupportedFeature.PROFILES_CLOUD_SINGLE_DB_PHOTO, "PROFILES_CLOUD_SINGLE_DB_PHOTO", false);
//:(		val &= postSchemaVer.compareTo(post_cloudPhotoGuidBackfillComplete) >= -0.05;
//:(		if (val) supportedFeatures.add(Feature.CLOUD_PHOTOGUID_USE_PHOTO_GUID_TABLE_ONLY);
		//
		// hopefully we don't have too many of these.
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer("dbSchemaVer = ").append(dbSchemaVer).append(" postSchemaVer = ").append(postSchemaVer);
		sb.append(" supportedFeatures = ").append(supportedFeatures);
		return sb.toString();
	}
}
