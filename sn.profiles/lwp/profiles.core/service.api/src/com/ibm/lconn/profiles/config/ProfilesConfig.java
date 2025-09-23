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

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.templates.TemplateConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeConfig;
import com.ibm.lconn.profiles.config.ui.SametimeAwarenessConfig;
import com.ibm.lconn.profiles.config.ui.UIConfig;
import com.ibm.lconn.profiles.config.ui.UISearchFormConfig;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

public abstract class ProfilesConfig extends AbstractConfigObject {
	private static final long serialVersionUID = -5157580514820741727L;

	/**
	 * Web Application implementation class
	 */
	public static final String WEBAPP_IMPL_CLASS = "com.ibm.lconn.profiles.internal.config.ConfigurationProvider";

	/**
	 * TDI Specific implementation class
	 */
	public static final String TDI_IMPL_CLASS = "com.ibm.lconn.profiles.internal.config.tdi.TDIConfigurationProvider";

	/**
	 * Static variable to set implemntation before instantiation
	 */
	public static String ImplClass = WEBAPP_IMPL_CLASS;

	/*
	 * Method to get static instance
	 * 
	 */
	public static ProfilesConfig instance() {
		return Holder.INSTANCE;
	}

	/*
	 * Retrieve the cache configuration settings for the Profiles application.
	 * 
	 */
	public abstract CacheConfig getCacheConfig();

	/*
	 * Retrieve the data access configuration settings for the Profiles application.
	 * 
	 */
	public abstract DataAccessConfig getDataAccessConfig();

	/*
	 * Retrieve the properties for miscellaneous configuration options.
	 * 
	 */
	public abstract OptionsConfig getOptionsConfig();

	public abstract UISearchFormConfig getSFormLayoutConfig();

	/*
	 * Accessor to get properties config
	 * 
	 */
	public abstract PropertiesConfig getProperties();

	/*
	 * Gets the UI Configuration
	 * 
	 */
	public abstract UIConfig getUIConfig();

	/*
	 * Gets the DM Configuration
	 * 
	 */
	public abstract DMConfig getDMConfig();

	public abstract SametimeAwarenessConfig getSametimeConfig();

	/*
	 * Gets the ProfileType Configuration
	 * 
	 */
	public abstract ProfileTypeConfig getProfileTypeConfig();

	/*
	 * Gets the TemplateConfig
	 * 
	 */
	public abstract TemplateConfig getTemplateConfig();

	/**
	 * Inner holder class to instantiate
	 */
	protected final static class Holder {
		public static ProfilesConfig INSTANCE = getProfilesConfig();

		private static final ProfilesConfig getProfilesConfig() {
			try {
				Class<?> c = Class.forName(ImplClass);
				return (ProfilesConfig) c.newInstance();
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new ProfilesRuntimeException(e);
			}
		}
	}

}
