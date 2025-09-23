/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A configuration object that manages the <code>ProfileType</code> hierarchy in a given Profiles deployment.
 * 
 * If instructed, this object can support a <code>ProfileType</code> system in the global and tenant scopes.
 * 
 */
public class ProfileTypeConfig {

	// logger
	private static final Logger logger = Logger.getLogger(ProfileTypeConfig.class.getName());
	// impl class
	ProfileTypeConfigImpl impl;

	public static final String BASE_TYPE_ID = "snx:person";
	public static final String MT_BASE_TYPE_ID = "snx:mtperson";
	public static final String DEFAULT_TYPE_ID = "default";

	public ProfileTypeConfig(boolean multiTenantConfigEnabled, Map<String, ExtensionType> extensionProperties, boolean parseVentura)
			throws Exception {
		if (logger.isLoggable(Level.FINER)) {
			logger.entering(getClass().getName(), "ProfileTypeConfig(multiTenantConfigEnabled, extensionProperties, parseVentura)",
					new Object[] { multiTenantConfigEnabled, extensionProperties, parseVentura });
		}
		if (multiTenantConfigEnabled) {
			impl = new ProfileTypeConfigImplMT(extensionProperties, parseVentura);
			logger.info("ProfileTypesConfig instantiated multitenant implementation");
		}
		else {
			impl = new ProfileTypeConfigImplST(extensionProperties, parseVentura);
			logger.info("ProfileTypesConfig instantiated single tenant implementation");
		}
		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(getClass().getName(), "ProfileTypeConfig(multiTenantConfigEnabled, extensionProperties, parseVentura)");
		}
	}

	/**
	 * Return the internally managed profile type that enumerates all potential properties
	 * 
	 * @return
	 */
	public ProfileType getSeedlistDefaultType() {
		return impl.getSeedlistDefaultType();
	}

	public ProfileType getBaseProfileType() {
		return impl.getBaseProfileType();
	}

	public Map<String, ProfileTypeImpl> getGlobalProfileTypes() {
		return impl.getGlobalProfileTypes();
	}

	/**
	 * Retrieve a <code>ProfileType</code> definition or null, if undefined.
	 * 
	 * @param id
	 * @return
	 */
	public ProfileType getProfileType(String id, String tenantKey) {
		return impl.getProfileType(id, tenantKey, true);
	}

	public ProfileType getProfileType(String id, String tenantKey, boolean ifNotFoundReturnAbstractPerson) {
		ProfileType rtnVal = impl.getProfileType(id, tenantKey, ifNotFoundReturnAbstractPerson);
		return rtnVal;
	}

	public Set<String> getProfileTypeIds(String tenantKey) {
		Set<String> rtnVal = impl.getProfileTypeIds(tenantKey);
		return rtnVal;
	}
	// this is the previous on-prem implementation, maintained for quick reference or
	// to quickly revert back until this goes through a build/test cycle
	// /**
	// * A configuration object that manages the <code>ProfileType</code> hierarchy in a given Profiles deployment.
	// *
	// * If instructed, this object can support a <code>ProfileType</code> system in the global and tenant scopes.
	// *
	// */
	// public class ProfileTypeConfig {
	//
	// // the logger
	// private static final Logger logger = Logger.getLogger(ProfileTypeConfig.class.getName());
	//
	// // the set of types that are defined globally at the cell level via Ventura-Config
	// private Map<String, ProfileTypeImpl> GLOBAL_TYPES;
	//
	// // a flag to instruct if we need to look for tenant specific types
	// private boolean multiTenantConfigEnabled;
	//
	// // the list of identifiers for extension properties
	// private Map<String, ExtensionType> extensionProperties;
	//
	// public static final String BASE_TYPE_ID = "snx:person";
	//
	// // an internal profile type that has ALL properties known by the system declared on it
	// private final ProfileType ALL;
	//
	// public ProfileTypeConfig(boolean multiTenantConfigEnabled, Map<String, ExtensionType> extensionProperties, boolean parseVentura)
	// throws Exception {
	// if (logger.isLoggable(Level.FINER)) {
	// logger.entering(getClass().getName(), "ProfileTypeConfig(multiTenantConfigEnabled, extensionProperties, parseVentura)",
	// new Object[] { multiTenantConfigEnabled, extensionProperties, parseVentura });
	// }
	//
	// // if we will later support lazy resolution of types at the tenant level
	// this.multiTenantConfigEnabled = multiTenantConfigEnabled;
	//
	// // used to validate profile-type.xml definitions
	// this.extensionProperties = extensionProperties;
	//
	// // the type hierarchy for profiles mandates all type definitions extend the abstract base type 'snx:person' that has the following
	// // fields.
	//
	// // display name
	// PropertyImpl displayName = new PropertyImpl();
	// displayName.setExtension(false);
	// displayName.setHidden(false);
	// displayName.setFullTextIndexed(PropertyEnum.DISPLAY_NAME.fullTextIndexed);
	// displayName.setInherited(false);
	// displayName.setRef(PropertyEnum.DISPLAY_NAME.getValue());
	// displayName.setRichText(false);
	// displayName.setUpdatability(Updatability.READ);
	//
	// // surname
	// PropertyImpl surname = new PropertyImpl();
	// surname.setExtension(false);
	// surname.setHidden(false);
	// surname.setFullTextIndexed(PropertyEnum.SURNAME.fullTextIndexed);
	// surname.setInherited(false);
	// surname.setRef(PropertyEnum.SURNAME.getValue());
	// surname.setRichText(false);
	// surname.setUpdatability(Updatability.READ);
	//
	// // guid
	// PropertyImpl guid = new PropertyImpl();
	// guid.setExtension(false);
	// guid.setHidden(false);
	// guid.setFullTextIndexed(true);
	// guid.setInherited(PropertyEnum.GUID.fullTextIndexed);
	// guid.setRef(PropertyEnum.GUID.getValue());
	// guid.setRichText(false);
	// guid.setUpdatability(Updatability.READ);
	//
	// // key
	// PropertyImpl key = new PropertyImpl();
	// key.setExtension(false);
	// key.setHidden(false);
	// key.setFullTextIndexed(PropertyEnum.KEY.fullTextIndexed);
	// key.setInherited(false);
	// key.setRef(PropertyEnum.KEY.getValue());
	// key.setRichText(false);
	// key.setUpdatability(Updatability.READ);
	//
	// // tenant key
	// PropertyImpl tenantKey = new PropertyImpl();
	// tenantKey.setExtension(false);
	// tenantKey.setHidden(false);
	// tenantKey.setFullTextIndexed(PropertyEnum.TENANT_KEY.fullTextIndexed);
	// tenantKey.setInherited(false);
	// tenantKey.setRef(PropertyEnum.TENANT_KEY.getValue());
	// tenantKey.setRichText(false);
	// tenantKey.setUpdatability(Updatability.READ);
	//
	// // uid
	// PropertyImpl uid = new PropertyImpl();
	// uid.setExtension(false);
	// uid.setHidden(false);
	// uid.setFullTextIndexed(PropertyEnum.UID.fullTextIndexed);
	// uid.setInherited(false);
	// uid.setRef(PropertyEnum.UID.getValue());
	// uid.setRichText(false);
	// uid.setUpdatability(Updatability.READ);
	//
	// // profile type
	// PropertyImpl profileType = new PropertyImpl();
	// profileType.setExtension(false);
	// profileType.setHidden(false);
	// profileType.setFullTextIndexed(PropertyEnum.PROFILE_TYPE.fullTextIndexed);
	// profileType.setInherited(false);
	// profileType.setRef(PropertyEnum.PROFILE_TYPE.getValue());
	// profileType.setRichText(false);
	// profileType.setUpdatability(Updatability.READ);
	//
	// // userid
	// PropertyImpl userId = new PropertyImpl();
	// userId.setExtension(false);
	// userId.setHidden(false);
	// userId.setFullTextIndexed(PropertyEnum.USER_ID.fullTextIndexed);
	// userId.setInherited(false);
	// userId.setRef(PropertyEnum.USER_ID.getValue());
	// userId.setRichText(false);
	// userId.setUpdatability(Updatability.READ);
	//
	// // sourceurl
	// PropertyImpl sourceUrl = new PropertyImpl();
	// sourceUrl.setExtension(false);
	// sourceUrl.setHidden(false);
	// sourceUrl.setFullTextIndexed(PropertyEnum.SOURCE_URL.fullTextIndexed);
	// sourceUrl.setInherited(false);
	// sourceUrl.setRef(PropertyEnum.SOURCE_URL.getValue());
	// sourceUrl.setRichText(false);
	// sourceUrl.setUpdatability(Updatability.READ);
	//
	// // distinguishedName
	// PropertyImpl distinguishedName = new PropertyImpl();
	// distinguishedName.setExtension(false);
	// distinguishedName.setHidden(false);
	// distinguishedName.setFullTextIndexed(PropertyEnum.DISTINGUISHED_NAME.fullTextIndexed);
	// distinguishedName.setInherited(false);
	// distinguishedName.setRef(PropertyEnum.DISTINGUISHED_NAME.getValue());
	// distinguishedName.setRichText(false);
	// distinguishedName.setUpdatability(Updatability.READ);
	//
	// // givenName
	// PropertyImpl givenName = new PropertyImpl();
	// givenName.setExtension(false);
	// givenName.setHidden(false);
	// givenName.setFullTextIndexed(PropertyEnum.GIVEN_NAME.fullTextIndexed);
	// givenName.setInherited(false);
	// givenName.setRef(PropertyEnum.GIVEN_NAME.getValue());
	// givenName.setRichText(false);
	// givenName.setUpdatability(Updatability.READ);
	//
	// // managerUid
	// PropertyImpl managerUid = new PropertyImpl();
	// managerUid.setExtension(false);
	// managerUid.setHidden(false);
	// managerUid.setFullTextIndexed(PropertyEnum.MANAGER_UID.fullTextIndexed);
	// managerUid.setInherited(false);
	// managerUid.setRef(PropertyEnum.MANAGER_UID.getValue());
	// managerUid.setRichText(false);
	// managerUid.setUpdatability(Updatability.READ);
	//
	// // isManager
	// PropertyImpl isManager = new PropertyImpl();
	// isManager.setExtension(false);
	// isManager.setHidden(false);
	// isManager.setFullTextIndexed(PropertyEnum.IS_MANAGER.fullTextIndexed);
	// isManager.setInherited(false);
	// isManager.setRef(PropertyEnum.IS_MANAGER.getValue());
	// isManager.setRichText(false);
	// isManager.setUpdatability(Updatability.READ);
	//
	// // loginId
	// PropertyImpl loginId = new PropertyImpl();
	// loginId.setExtension(false);
	// loginId.setHidden(false);
	// loginId.setFullTextIndexed(PropertyEnum.LOGIN_ID.fullTextIndexed);
	// loginId.setInherited(false);
	// loginId.setRef(PropertyEnum.LOGIN_ID.getValue());
	// loginId.setRichText(false);
	// loginId.setUpdatability(Updatability.READ);
	//
	// // userState
	// PropertyImpl userState = new PropertyImpl();
	// userState.setExtension(false);
	// userState.setHidden(false);
	// userState.setFullTextIndexed(PropertyEnum.USER_STATE.fullTextIndexed);
	// userState.setInherited(false);
	// userState.setRef(PropertyEnum.USER_STATE.getValue());
	// userState.setRichText(false);
	// userState.setUpdatability(Updatability.READ);
	//
	// // base properties
	// List<PropertyImpl> properties = new ArrayList<PropertyImpl>(14);
	// properties.add(displayName);
	// properties.add(guid);
	// properties.add(key);
	// properties.add(surname);
	// properties.add(tenantKey);
	// properties.add(uid);
	// properties.add(profileType);
	// properties.add(userId);
	// properties.add(sourceUrl);
	// properties.add(distinguishedName);
	// properties.add(givenName);
	// properties.add(managerUid);
	// properties.add(isManager);
	// properties.add(loginId);
	// properties.add(userState);
	//
	// // base profile type
	// ProfileTypeImpl baseType = new ProfileTypeImpl();
	// baseType.setId(BASE_TYPE_ID);
	// baseType.setParentId("");
	// baseType.setProperties(properties);
	//
	// // universal profile type that has all properties declared (used in search seedlist if variable indexing is disabled)
	// List<PropertyEnum> RICH_TEXT = new ArrayList<PropertyEnum>(2);
	// RICH_TEXT.add(PropertyEnum.DESCRIPTION);
	// RICH_TEXT.add(PropertyEnum.EXPERIENCE);
	// List<PropertyImpl> allProperties = new ArrayList<PropertyImpl>();
	// for (PropertyEnum standardProperty : PropertyEnum.values())
	// {
	// PropertyImpl propertyImpl = new PropertyImpl();
	// propertyImpl.setExtension(false);
	// propertyImpl.setFullTextIndexed(standardProperty.fullTextIndexed);
	// propertyImpl.setHidden(false);
	// propertyImpl.setInherited(false);
	// propertyImpl.setRef(standardProperty.getValue());
	// propertyImpl.setRichText(RICH_TEXT.contains(standardProperty));
	// propertyImpl.setUpdatability(Updatability.READ);
	// allProperties.add(propertyImpl);
	// }
	// for (String extensionKey : extensionProperties.keySet())
	// {
	// ExtensionType extensionType = extensionProperties.get(extensionKey);
	// PropertyImpl propertyImpl = new PropertyImpl();
	// propertyImpl.setExtension(true);
	// propertyImpl.setExtensionType(extensionType);
	// propertyImpl.setFullTextIndexed(true);
	// propertyImpl.setHidden(false);
	// propertyImpl.setInherited(false);
	// propertyImpl.setRef(extensionKey);
	// propertyImpl.setRichText(ExtensionType.RICHTEXT.equals(extensionType));
	// propertyImpl.setUpdatability(Updatability.READ);
	// allProperties.add(propertyImpl);
	// }
	// ProfileTypeImpl allType = new ProfileTypeImpl();
	// allType.setId("snx:all");
	// allType.setParentId("");
	// allType.setProperties(allProperties);
	// ALL = allType;
	//
	// if (logger.isLoggable(Level.FINER)) {
	// logger.log(Level.FINER, "Profile Type: ALL - properties:" + allProperties);
	// }
	//
	// // seed the type-hierarchy scope with this abstract base type, and then attempt to parse the global type definitions from cell
	// Map<String, ProfileTypeImpl> scope = new HashMap<String, ProfileTypeImpl>(1);
	// scope.put(baseType.getId(), baseType);
	// try {
	// if (parseVentura) {
	// VenturaConfigurationProvider configProvider = VenturaConfigurationProvider.Factory.getInstance();
	// URL fileURL = configProvider.getURLForXMLConfig("profiles-types.xml");
	// logger.log(Level.INFO, "Profiles Type Configuration profiles-types.xml loading");
	// GLOBAL_TYPES = Collections.unmodifiableMap(parseTypes(fileURL, scope));
	// logger.log(Level.INFO, "Profiles Type Configuration profiles-types.xml completed");
	// }
	// else {
	// GLOBAL_TYPES = Collections.unmodifiableMap(scope);
	// }
	// }
	// catch (VenturaConfigException e) {
	// throw e;
	// }
	// catch (Exception e) {
	// throw e;
	// }
	// finally {
	// if (logger.isLoggable(Level.FINER)) {
	// logger.exiting(getClass().getName(), "ConfigurationProvider");
	// }
	// }
	// }
	//
	// /**
	// * Return the internally managed profile type that enumerates all potential properties
	// * @return
	// */
	// public ProfileType getAll()
	// {
	// if (multiTenantConfigEnabled)
	// throw new UnsupportedOperationException("not yet implemented pending a cache deployed in install");
	//
	// return ALL;
	// }
	//
	// /**
	// * Retrieve a <code>ProfileType</code> definition or null, if undefined.
	// *
	// * Note: if multitenancy is enabled, this object will retrieve a type-hierarchy for a given tenant from dynacache before falling back
	// on
	// * the static hieararchy.
	// *
	// * @param id
	// * @return
	// */
	// public ProfileType getProfileType(String id) {
	// if (multiTenantConfigEnabled)
	// throw new UnsupportedOperationException("not yet implemented pending a cache deployed in install");
	//
	// return getProfileType(id, true);
	// }
	//
	// public ProfileType getProfileType(String id, boolean ifNotFoundReturnAbstractPerson) {
	// if (multiTenantConfigEnabled)
	// throw new UnsupportedOperationException("not yet implemented pending a cache deployed in install");
	//
	// // 3M reported having <space> characters in population, this just ensures that we fall back on default if <space> is populated
	// if (id != null)
	// {
	// id = id.trim();
	// }
	//
	// // preserve fallback on the 'default' pending the sql migration script to turn all NULL into "default"
	// if (id == null || "".equals(id)) {
	// id = "default";
	// }
	//
	// ProfileType result = GLOBAL_TYPES.get(id);
	//
	// // if a value is not found, this call can NEVER return null, as a result, we return the least descriptive valid type definition
	// if (result == null && ifNotFoundReturnAbstractPerson) {
	// result = GLOBAL_TYPES.get(BASE_TYPE_ID);
	// }
	//
	// return result;
	// }
	//
	// public Set<String> getProfileTypeIds() {
	// if (multiTenantConfigEnabled)
	// throw new UnsupportedOperationException("not yet implemented pending a cache deployed in install");
	//
	// return Collections.unmodifiableSet(GLOBAL_TYPES.keySet());
	// }
	//
	// /**
	// * Parse the type-definition file at the specified location seeding it with the list of existing types in scope.
	// *
	// * @param fileURL
	// * location of the profile-types definition to parse
	// * @param scope
	// * types in scope
	// */
	// private Map<String, ProfileTypeImpl> parseTypes(URL fileURL, Map<String, ProfileTypeImpl> scope) throws Exception {
	// SAXParser parser = null;
	// ProfileTypeHandler profileTypeHandler = new ProfileTypeHandler(scope, true, extensionProperties);
	//
	// try {
	// parser = ParserUtil.borrowParser();
	// InputStream is = fileURL.openStream();
	// parser.parse(is, profileTypeHandler);
	// if (logger.isLoggable(Level.FINEST)) {
	// logger.logp(Level.FINEST, getClass().getName(), "parseTypes", "Loaded file at:" + fileURL.toString());
	// }
	// }
	// catch (IOException e) {
	// logger.log(Level.SEVERE,
	// "Unable to initialize Profiles application.  Unable to read configuration file at: " + fileURL.toString());
	// throw e;
	// }
	// catch (Exception e) {
	// logger.log(Level.SEVERE,
	// "Unable to initialize Profiles application.  Unable to read configuration file at: " + fileURL.toString());
	// throw e;
	// }
	// finally {
	// ParserUtil.returnParser(parser);
	// }
	//
	// return profileTypeHandler.getProfileTypes();
	// }
	//
}
