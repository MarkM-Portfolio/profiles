/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;


/*
 *
 */
public class CacheConfig extends AbstractConfigObject
{
	private static final long serialVersionUID = -4812866780463028073L;
	
	private final ObjectCacheConfig fullReportsToChainConfig;
	private final ObjectCacheConfig profileObjectCache;
	
	/*
	 * CTOR for WebApp
	 * @param config
	 */
	public CacheConfig(HierarchicalConfiguration config) 
	{
		this.fullReportsToChainConfig = new ObjectCacheConfig(config,"fullReportsToChainCache");
		this.profileObjectCache = new ObjectCacheConfig(config,"profileObjectCache");
	}
	
	/*
	 * CTOR for TDI
	 * @param config
	 */
	public CacheConfig() 
	{
		this.fullReportsToChainConfig = new ObjectCacheConfig("fullReportsToChainCache");
		this.profileObjectCache = new ObjectCacheConfig("profileObjectCache");
	}
	

	/*
	 * Gets full-reports-to-chain cache config
	 * @return
	 */
	public ObjectCacheConfig getFullReportsToChainConfig() {
		return this.fullReportsToChainConfig;
	}


	/*
	 * Gets object cache configuration
	 * @return
	 */
	public ObjectCacheConfig getProfileObjectCache() {
		return this.profileObjectCache;
	}
	
	/*
	 * Syntax sugar
	 * @return
	 */
	public static CacheConfig instance() {
		return ProfilesConfig.instance().getCacheConfig();
	}

	public static class ObjectCacheConfig extends AbstractConfigObject {
		private static final long serialVersionUID = 3336631191013229795L;
		
		private static final boolean DEFAULT_ENABLEMENT = true;
		
		private String ceoUid = null;
		private String filePath = null;
		private int refreshInterval = Integer.MAX_VALUE;
		private String refreshTime = "23:00";
		private int size = -1;
		private int startDelay = Integer.MAX_VALUE;
		private boolean enabled = false;		
		
		/*
		 * CTOR for WebApp
		 */
		protected ObjectCacheConfig(Configuration config, String cacheName) {
			String prefix = "caches." + cacheName + ".";
			
			this.ceoUid = config.getString(prefix+"ceouid");
			this.filePath = config.getString(prefix+"filePath");
			this.refreshInterval =  config.getInt(prefix+"refreshInterval");
			this.refreshTime = config.getString(prefix+"refreshTime");
			this.size = config.getInt(prefix+"size",this.size);
			this.startDelay = config.getInt(prefix+"startDelay",this.startDelay);
			this.enabled = config.getBoolean(prefix+"enabled",DEFAULT_ENABLEMENT);
		}
		
		/*
		 * CTOR for TDI
		 */
		protected ObjectCacheConfig(String cacheName) {}

		public String getCEOUid() {
			return ceoUid;
		}

		public String getFilePath() {
			return filePath;
		}

		public int getRefreshInterval() {
			return refreshInterval;
		}

		public String getRefreshTime() {
			return refreshTime;
		}

		public int getSize() {
			return size;
		}

		public int getStartDelay() {
			return startDelay;
		}

		public boolean isEnabled() {
			return enabled;
		}		
	}
}
