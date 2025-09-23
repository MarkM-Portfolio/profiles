/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.config;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.HierarchicalConfiguration;
import com.ibm.lconn.profiles.internal.policy.PolicyConfig;
import com.ibm.lconn.core.util.EnvironmentType;
import com.ibm.lconn.profiles.config.CacheConfig;
import com.ibm.lconn.profiles.config.DataAccessConfig;
import com.ibm.lconn.profiles.config.LCConfig;
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
import com.ibm.ventura.internal.service.admin.was.WASAdminService;

/**
 * A singleton class used to hold globally accessible application properties loaded in the running web application.
 */
public class ConfigurationProvider extends ProfilesConfig {

	private final LCConfig lotusConnectionsConfig;
	
	private final UISearchFormConfig sFormLayoutConfig;

	private final CacheConfig cacheConfig;;

	private final DataAccessConfig dataAccessConfig;

	private final OptionsConfig optionsConfig;

	private final SametimeAwarenessConfig sametimeConfig;

	private final PropertiesConfig propertiesConfig;

	private final UIConfig uiConfig;

	private final DMConfig dmConfig;

	private final ProfileTypeConfig profileTypeConfig;

	private final TemplateConfig templateConfig;

	private static final Logger logger = Logger.getLogger(ConfigurationProvider.class.getName());

	private static final long serialVersionUID = 1L;

	private static final String PROFILES = "profiles";

	private static final String CONFIGURATION_FILE_PATH;
	static {
		EnvironmentType envType = EnvironmentType.getType();
		if (EnvironmentType.WEBSPHERE == envType) {
			String installRoot = System.getProperty("user.install.root");
			String cellName = WASAdminService.getCellName();
			CONFIGURATION_FILE_PATH = installRoot + File.separatorChar + "config" + File.separatorChar + "cells" + File.separatorChar
					+ cellName + File.separatorChar + "LotusConnections-config" + File.separatorChar + "profiles";
		}
		else {
			CONFIGURATION_FILE_PATH = System.getProperty("test.config.files")
				+ File.separatorChar + "profiles";		
		}
	}

	public ConfigurationProvider() throws ProfilesRuntimeException {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "ConfigurationProvider", new Object[] {});
		}

		try {
			// info from LotusConnections-config.xml
			lotusConnectionsConfig = LCConfig.instance();
			lotusConnectionsConfig.verify(PROFILES);

			// load the profiles-config.xml
			HierarchicalConfiguration profilesConfig = (HierarchicalConfiguration) VenturaConfigurationProvider.Factory.getInstance()
					.getConfiguration("profiles");

			// load the data access configuration from profiles-config
			HierarchicalConfiguration dataAccessConfiguration = (HierarchicalConfiguration) profilesConfig.subset("dataAccess");
			
			cacheConfig = new CacheConfig(profilesConfig);
			dataAccessConfig = new DataAccessConfig(dataAccessConfiguration);
			optionsConfig = new OptionsConfig(profilesConfig, dataAccessConfiguration);
			sametimeConfig = new SametimeAwarenessConfig(profilesConfig);
			propertiesConfig = new PropertiesConfig((HierarchicalConfiguration) profilesConfig.subset("properties"));
			uiConfig = new UIConfig(profilesConfig);
			dmConfig = new DMConfig(profilesConfig, false);

			//
			// Initialize Search Form Config
			//
			HierarchicalConfiguration sFormConfig = (HierarchicalConfiguration) profilesConfig.subset("layoutConfiguration.searchLayout");

			sFormLayoutConfig = new UISearchFormConfig(sFormConfig);

			// On initialization of Profiles, we need to ensure that the PolicyConfig is initialized.
			// This is a shared component with Homepage/News, so it is its own singleton
			PolicyConfig.instance();
			
			boolean isMultitenant = (lotusConnectionsConfig.isLotusLive() || lotusConnectionsConfig.isMTEnvironment());
			profileTypeConfig = new ProfileTypeConfig(isMultitenant, dmConfig.getExtensionPropertiesToType(), true);

			templateConfig = new TemplateConfig(CONFIGURATION_FILE_PATH, profilesConfig);
		}
		catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
				logger.warning(e.getLocalizedMessage());
			}
			throw new ProfilesRuntimeException(e);
		}
		finally {
			if (logger.isLoggable(Level.FINER)) {
				logger.exiting(getClass().getName(), "ConfigurationProvider");
			}
		}
	}

	public DataAccessConfig getDataAccessConfig() {
		return dataAccessConfig;
	}

	public UISearchFormConfig getSFormLayoutConfig() {
		return sFormLayoutConfig;
	}

	/**
	 * @return
	 * @see com.ibm.lconn.core.web.util.config.ConfigurationProvider#getCacheConfiguration()
	 */
	public CacheConfig getCacheConfig() {
		return cacheConfig;
	}

	/**
	 * Returns the options config
	 * 
	 * @return
	 */
	public OptionsConfig getOptionsConfig() {
		return optionsConfig;
	}

	public PropertiesConfig getProperties() {
		return propertiesConfig;
	}

	public UIConfig getUIConfig() {
		return uiConfig;
	}

	public DMConfig getDMConfig() {
		return dmConfig;
	}

	public SametimeAwarenessConfig getSametimeConfig() {
		return sametimeConfig;
	}

	public ProfileTypeConfig getProfileTypeConfig() {
		return profileTypeConfig;
	}

	public TemplateConfig getTemplateConfig() {
		return templateConfig;
	}

	public LCConfig getLotusConnectionConfig(){
		return lotusConnectionsConfig;
	}

}
