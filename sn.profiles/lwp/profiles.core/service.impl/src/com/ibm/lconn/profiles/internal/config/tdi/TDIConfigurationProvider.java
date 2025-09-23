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
package com.ibm.lconn.profiles.internal.config.tdi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.HierarchicalConfiguration;
import com.ibm.lconn.profiles.config.CacheConfig;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.OptionsConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeConfig;
import com.ibm.lconn.profiles.config.ui.SametimeAwarenessConfig;
import com.ibm.lconn.profiles.config.ui.UIConfig;
import com.ibm.lconn.profiles.config.ui.UISearchFormConfig;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;

/**
 * Profiles Configuration object that is used when running with TDI
 */
public class TDIConfigurationProvider extends ProfilesConfig {

	private static final Logger logger = Logger.getLogger(TDIConfigurationProvider.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = -2825629904962503138L;
	
	private static final CacheConfig cacheConfig = new CacheConfig();

	private static final DataAccessConfig dataAccess = new DataAccessConfig();

	private static final String[] profileTypes = { ProfilesConfig.DEFAULT };

	private static final OptionsConfig optionsConfig = new OptionsConfig();

	private HierarchicalConfiguration tdiProfilesConfig = initTDIProfilesConfig();

	private PropertiesConfig propertiesConfig = new PropertiesConfig((HierarchicalConfiguration) tdiProfilesConfig.subset("properties"));

	private static final UIConfig uiConfig = new UIConfig();

	private SametimeAwarenessConfig sametimeConfig = new SametimeAwarenessConfig();

	private final DMConfig dmConfig;

	private final ProfileTypeConfig profileTypeConfig;

	/**
	 * @throws VenturaConfigException
	 * 
	 */
	public TDIConfigurationProvider() throws VenturaConfigException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "TDIConfigurationProvider", new Object[] {});
		}

		try {
			this.dmConfig = new DMConfig(tdiProfilesConfig, true);
			// if TDI is MT enabled, will it do any special processing?
			// i believe the tdi directory has TDI-LotusConnections-config.xml
			//this.lotusConnectionsConfig =  LCConfig.instance();
			//boolean isMultitenant = lotusConnectionsConfig.isLotusLive();
			boolean isMultitenant = false;
			this.profileTypeConfig = new ProfileTypeConfig(isMultitenant, dmConfig.getExtensionPropertiesToType(), true);
		}
		catch (Exception e) {
			throw new VenturaConfigException(e);
		}
		finally {
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(getClass().getName(), "TDIConfigurationProvider");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getCacheConfig()
	 */
	@Override
	public CacheConfig getCacheConfig() {
		return cacheConfig;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getDMConfig()
	 */
	@Override
	public DMConfig getDMConfig() {
		return dmConfig;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getDataAccessConfig()
	 */
	@Override
	public DataAccessConfig getDataAccessConfig() {
		return dataAccess;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getOptionsConfig()
	 */
	@Override
	public OptionsConfig getOptionsConfig() {
		return optionsConfig;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getProperties()
	 */
	@Override
	public PropertiesConfig getProperties() {
		return propertiesConfig;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getSFormLayoutConfig()
	 */
	@Override
	public UISearchFormConfig getSFormLayoutConfig() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.ibm.lconn.profiles.config.ProfilesConfig#getStatisticsConfig()
	 */
	@Override
	public UIConfig getUIConfig() {
		return uiConfig;
	}

	public SametimeAwarenessConfig getSametimeConfig() {
		return sametimeConfig;
	}

	private HierarchicalConfiguration initTDIProfilesConfig() {
		try {
			return (HierarchicalConfiguration) VenturaConfigurationProvider.Factory.getInstance().getConfiguration("tdi-profiles");
		}
		catch (VenturaConfigException e) {
			throw new ProfilesRuntimeException(e);
		}
	}

	@Override
	public ProfileTypeConfig getProfileTypeConfig() {
		return profileTypeConfig;
	}

	public TemplateConfig getTemplateConfig() {
		return null;
	}

}
