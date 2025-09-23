/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;

import com.ibm.lconn.profiles.internal.util.CacheDelegate;
import com.ibm.lconn.profiles.internal.util.CacheDelegateFactory;

public abstract class AbstractProfilesCache
{
	protected static final Log LOG = LogFactory.getLog(AbstractProfilesCache.class);

	private static final ResourceBundleHelper _rbhComm = new ResourceBundleHelper("com.ibm.peoplepages.internal.resources.messages", AbstractProfilesCache.class.getClassLoader());

	// per-tenant cache JNDI name (lookup)
	private  String CACHE_JNDI_NAME = null; // getCacheJNDIName(); // eg ConfigProperty.TAGCLOUD_CACHE_LOOKUP_S

	// per-tenant cache time-to-live (seconds)
	private int    CACHE_TTL       = 0; // getCacheTTLValue(); // eg ConfigProperty.ALL_TAGS_CLOUD_REFRESH_IVAL);
	
	// cache implementation specific abstract methods
	protected abstract void           initialize();
	protected abstract void           terminate();

	protected abstract String         getCacheJNDIName();
	protected abstract int            getCacheTTLValue();
	protected abstract ConfigProperty getRefreshValue();
	
	// per-tenant cache
	protected CacheDelegate _perTenantCache = null;
	private boolean _initialized   = false;

	protected boolean isInitialized() {
		return _initialized;
	}

	protected void setInitialized(boolean inited) {
		_initialized = inited;
	}

	protected void initialize(String jndiName, ConfigProperty refreshProp) {
		if (! isInitialized()) {
			boolean initialized = initCache(jndiName, refreshProp);
			setInitialized(initialized);
			if (LOG.isDebugEnabled()) {
				LOG.debug("AbstractProfilesCache.initialize(" + jndiName + "...) : " + (initialized ? "succeeded" : "failed"));
			}
		}
	}

	protected void terminate(String jndiName) {
		if (isInitialized()) {
			boolean terminated = terminateCache(jndiName);
			setInitialized(! terminated);
			if (LOG.isDebugEnabled()) {
				LOG.debug("AbstractProfilesCache.terminate(" + jndiName + ") : " + (terminated ? "succeeded" : "failed"));
			}
			_perTenantCache = null;
		}
	}

	/**
	 * Empty the cache.
	 */
	protected void reset(String jndiName) {
		if (_perTenantCache != null) {
			if (isInitialized()) {
				boolean reset = resetCache(jndiName);
				if (LOG.isDebugEnabled()) {
					LOG.debug("AbstractProfilesCache.reset(" + jndiName + ") : " + (reset ? "succeeded" : "failed"));
				}
			}
			_initialized = true;
		}
	}

	private boolean initCache(String cacheJNDIName, ConfigProperty cacheTTLValue) {
		boolean success = false;
		// Retrieve per-tenant dyna-cache specific settings from config

		// per-tenant cache JNDI name (lookup)
		CACHE_JNDI_NAME = cacheJNDIName; // PropertiesConfig.getString(ConfigProperty.TAGCLOUD_CACHE_LOOKUP_STRING);

		// per-tenant cache time-to-live (seconds)
		ConfigProperty cacheRefresh = cacheTTLValue;
		int configTTL = PropertiesConfig.getInt(cacheRefresh);
		// allow an over-ride values to be set in profiles-config.xml <properties> ...
		// eg.  <property name="com.ibm.lconn.profiles.config.AllTagsRefreshInterval" value="400"/>

		// if an over-ride value was specified, then use it instead of the default (10 minutes / 600 seconds)
		if (configTTL > 0)
			CACHE_TTL = configTTL;

		// used for object caching of tag cloud per tenant
		HashMap<String, Object> args = new HashMap<String, Object>(3);
		args.put(CacheDelegate.CACHE_JNDI_NAME, CACHE_JNDI_NAME);
		args.put(CacheDelegate.CACHE_TTL_VALUE, CACHE_TTL);
		_perTenantCache = CacheDelegateFactory.INSTANCE.getCacheDelegate(args);
		success = (null != _perTenantCache);
		return success;
	}

	private boolean terminateCache(String cacheJNDIName) {
		return CacheDelegateFactory.INSTANCE.removeCacheDelegate(cacheJNDIName);
	}

	private boolean resetCache(String cacheJNDIName) {
		return CacheDelegateFactory.INSTANCE.resetCacheDelegate(cacheJNDIName);
	}
}
