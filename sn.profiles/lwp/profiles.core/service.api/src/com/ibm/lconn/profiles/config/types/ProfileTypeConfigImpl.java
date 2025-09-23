/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.types;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.lconn.profiles.data.Tenant;

abstract class ProfileTypeConfigImpl {
	// the logger
	private static final Logger logger = Logger.getLogger(ProfileTypeConfig.class.getName());

	// the set of types that are defined globally at the cell level via
	// Ventura-Config (as defined in LotusConnections-config/profiles-types.xml)
	protected Map<String, ProfileTypeImpl> GLOBAL_TYPES;
	
	// used by seedlist if variable indexing is off. an internal profile type that has a 'base' set of properties
	// with ootb indexing instructions. initialization specific to the impl is in the concrete classes.
	protected ProfileType seedlistdefault_type;

	// the list of identifiers for extension properties as defined in LotusConnections-config/profiles-config.xml
	protected Map<String, ExtensionType> extensionProperties;
	
	// method for parsing global profile-types.xml and seeding the GLOBAL_TYPES with the profile
	// type definitions defined in that file.
	protected abstract void setGlobalTypes(Map<String, ExtensionType> extensionProperties, boolean parseVentura) throws Exception;

	public Map<String, ProfileTypeImpl> getGlobalProfileTypes() {
		return GLOBAL_TYPES;
	}

	protected ProfileTypeConfigImpl(boolean multiTenantConfigEnabled, Map<String, ExtensionType> extensionProperties, boolean parseVentura)
			throws Exception {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "ProfileTypeConfig(multiTenantConfigEnabled, extensionProperties, parseVentura)",
					new Object[] { multiTenantConfigEnabled, extensionProperties, parseVentura });
		}
		// used to validate profile-type.xml definitions
		this.extensionProperties = extensionProperties;
		// set global types
		setGlobalTypes(extensionProperties, parseVentura);
	}

	/**
	 * Return the internally managed profile type that enumerates all properties used for default seedlist
	 * 
	 * @return
	 */
	public ProfileType getSeedlistDefaultType() {
		return seedlistdefault_type;
	}

	/**
	 * Return the internally managed profile type that enumerates properties of the common base person.
	 * By default, all users share these properties (unless overridden by derived types)
	 * 
	 * @return
	 */
	public abstract ProfileType getBaseProfileType();

	/**
	 * Retrieve a <code>ProfileType</code> definition or null, if undefined.
	 * 
	 * @param id
	 * @return
	 */
	public abstract ProfileType getProfileType(String id, String tenantKey);

	public abstract ProfileType getProfileType(String id, String tenantKey, boolean ifNotFoundReturnAbstractPerson);

	public abstract Set<String> getProfileTypeIds(String tenantKey);
	
	protected Map<String, ProfileTypeImpl> parseTypes(URL fileURL, Map<String, ProfileTypeImpl> scope) throws Exception {
		Map<String, ProfileTypeImpl> rtn = null;
		try {
			// register the global base types with 'no' tenant key. these definitions are global.
			rtn = ProfileTypeParser.parseTypes(fileURL,extensionProperties,scope,Tenant.IGNORE_TENANT_KEY);
		}
		catch (IOException e) {
			logger.log(Level.SEVERE,
					"Unable to initialize Profiles application.  Unable to read configuration file at: " + fileURL.toString());
			throw e;
		}
		catch (Exception e) {
			logger.log(Level.SEVERE,
					"Unable to initialize Profiles application.  Unable to read configuration file at: " + fileURL.toString());
			throw e;
		}
		return rtn;
	}
}
