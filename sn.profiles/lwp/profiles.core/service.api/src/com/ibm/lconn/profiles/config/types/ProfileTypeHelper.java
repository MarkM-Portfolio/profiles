/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2018                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config.types;

import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.abdera.Abdera;
import org.apache.abdera.writer.StreamWriter;
import org.apache.abdera.writer.WriterFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// currently Profiles does not support JSON pay-loads
//import com.ibm.json.java.JSONArray;
//import com.ibm.json.java.JSONObject;

import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;

import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.types.Updatability;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.util.CacheDelegate;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * A helper class used to extract tenant information from the current thread of execution
 * and issue a tenant specific request to the cache instance.
 */
public class ProfileTypeHelper
{
	private static final Log LOG = LogFactory.getLog(ProfileTypeHelper.class);

	private static ResourceBundleHelper RESOURCE = new ResourceBundleHelper(CacheDelegate.class);

	private final static String CLASS_NAME = ProfileTypeHelper.class.getSimpleName();

	private final static boolean IS_MT_ENV;

	static {
		IS_MT_ENV = LCConfig.instance().isMTEnvironment();
	}

	// prohibit changing ProfileType meta-data of these fields
	private static String [] prohibitedProperties =
	{
		// key fields
		PropertyEnum.KEY.getValue(),
		PropertyEnum.UID.getValue(),
		PropertyEnum.GUID.getValue(),
		PropertyEnum.LOGIN_ID.getValue(),
		PropertyEnum.USER_ID.getValue(),
		PropertyEnum.TENANT_KEY.getValue(),
		PropertyEnum.HOME_TENANT_KEY.getValue(),
		PropertyEnum.PROFILE_TYPE.getValue(),

		// BSS fields
		PropertyEnum.DISPLAY_NAME.getValue(),
		PropertyEnum.EMAIL.getValue(),
		PropertyEnum.GIVEN_NAME.getValue(),
		PropertyEnum.SURNAME.getValue(),
		PropertyEnum.TIME_ZONE.getValue(),
		PropertyEnum.USER_STATE.getValue(),

		// others
		PropertyEnum.ORG_ID.getValue(),
		PropertyEnum.MANAGER_UID.getValue(),
		PropertyEnum.GROUPWARE_EMAIL.getValue(),
		PropertyEnum.USER_MODE.getValue(),
		PropertyEnum.EMPLOYEE_TYPE_CODE.getValue(),
		PropertyEnum.LAST_UPDATE.getValue()
	};

	// Cisco 2018: LLIS skip fields. Profiles Admin APIs can not update these fields, identical
	// to the restricted fields that LLIS can not update. Keep in sync with the coding logic 
	// in method: doExecute() in the class:
	// com.ibm.lconn.profiles.internal.bss.commands.LLISProfileUpdateCommand.java
	// Ideally we should use this same fields in the above class too. Keep it separate to reduce
	// code changes for now.
	private static String [] llisProtectedFields = {
		PropertyEnum.PROFILE_TYPE.getValue(), 
		PropertyEnum.TENANT_KEY.getValue(), 
		PropertyEnum.KEY.getValue(), 
		PropertyEnum.UID.getValue(), 
		PropertyEnum.GUID.getValue(), 
		PropertyEnum.USER_ID.getValue(), 
		PropertyEnum.LOGIN_ID.getValue(), 
	
		// BSS fields
		PropertyEnum.EMAIL.getValue(), 
		PropertyEnum.USER_STATE.getValue(), 
		PropertyEnum.DISTINGUISHED_NAME.getValue(), 
		PropertyEnum.DISPLAY_NAME.getValue(), 
		PropertyEnum.GIVEN_NAME.getValue(), 
		PropertyEnum.SURNAME.getValue(), 
		PropertyEnum.TIME_ZONE.getValue(),
		// see rtc 150249 || attributeId.equals( PropertyEnum.JOB_RESP.getValue(),
		
		// other fields to skip/protect
		PropertyEnum.ORG_ID.getValue(), 
		PropertyEnum.GROUPWARE_EMAIL.getValue(), 
		PropertyEnum.USER_MODE.getValue(), 
		PropertyEnum.LAST_UPDATE.getValue(), 
		PropertyEnum.EMPLOYEE_TYPE_CODE.getValue() 
	};
										
	// no one should ever need to instantiate this class
	private ProfileTypeHelper(){
	}

	// Get the array of LLIS protected fields
	public static String [] getLLISProtectedFields() {
		return llisProtectedFields;
	}

	// not entirely sure why this one has to be different, but there y'are. Previous callers now directly call the innards of this
//	public static ProfileType getBaseProfileType() {
//		ProfileType rtn = null;
//		rtn = ProfilesConfig.instance().getProfileTypeConfig().getBaseProfileType();
//		return rtn;
//	}

	public static final ProfileType getProfileType(String typeId, boolean ifNotFoundReturnAbstractPerson){
		ProfileType rtn = null;
		String tenantKey = getTK();
		rtn = ProfilesConfig.instance().getProfileTypeConfig().getProfileType(typeId, tenantKey, ifNotFoundReturnAbstractPerson);
		return rtn;
	}

	// this API is called from all over the code base - looking for a PT based on typeId
	public static final ProfileType getProfileType(String typeId){
		return getProfileType(typeId, null);
	}

	public static final ProfileType getProfileType(String typeId, String orgId){
		ProfileType rtn = null;
		String tenantKey = null;
		// if we have an orgId, the item may be in the cache
		tenantKey = ((null != orgId) ? orgId : getTK());
		rtn = ProfilesConfig.instance().getProfileTypeConfig().getProfileType(typeId, tenantKey, true);
		return rtn;
	}

	public static final Map<String, ProfileTypeImpl> getGlobalProfileTypes(){
		Map<String, ProfileTypeImpl> rtn = null;
		rtn = ProfilesConfig.instance().getProfileTypeConfig().getGlobalProfileTypes();
		return rtn;
	}

	private static final String getTK(){
		if (IS_MT_ENV){
			AppContextAccess.Context ctx = AppContextAccess.getContext();
			String tenantKey = ctx.getTenantKey();
			return tenantKey;
		}
		else{
			// shortcut. if this is not MT no need to look up the app context.
			return Tenant.SINGLETENANT_KEY;
		}
	}

	public static ProfileType getProfileTypeFromXMLString(String payload, String tenantId, boolean isValidationNeeded)
	{
		String methodName = "getProfileTypeFromXMLString";

		ProfileType profileType = null;

		// parse the extended attributes pay-load into a complete ProfileType (including the base ProfileType for that orgId)
		boolean isValidInputXML = true;
		try {
			// get a base ProfileType (default base data set)
			ProfileType basePT = ProfilesConfig.instance().getProfileTypeConfig().getBaseProfileType(); // base class (this call is cumbersome)
			// save the PT id so we can find the right PT after the parse
			String basePTId = basePT.getId();
			// SC5.0 : Note hard-coding this to 'default' for SC 5.0 - in future, we'll need to scan the xml string & extract the id tag value 
			basePTId = ProfileTypeConfig.DEFAULT_TYPE_ID;
			// parse the input - we need the base profile types (already parsed on start up) and the extension attributes. the parser code
			// will merge the input definition with the base definitions. we require that an input definition extends the current base definition.
			Map<String, ProfileTypeImpl> ptTypes = ProfileTypeHelper.parseProfileType(payload, basePT, tenantId);
			if (null != ptTypes) {
				profileType = ProfileTypeHelper.validatePTForCloud50(tenantId, basePT, ptTypes, isValidationNeeded);
				if (null == profileType) {
					isValidInputXML = false;
				}
			}
			else {
				isValidInputXML = false;
			}
		}
		catch (Exception ex) {
			isValidInputXML = false;
			String msg = ex.getLocalizedMessage();
			LOG.error(CLASS_NAME + "." + methodName + " : Internal error. " + ((StringUtils.isEmpty(msg)) ? ex.getMessage() : msg));
			if (LOG.isDebugEnabled())
				ex.printStackTrace();
		}
		if (! isValidInputXML) {
			profileType = null;
			LOG.error(RESOURCE.getString("err.invalid.profiletype.xml", tenantId, payload));
		}
		return profileType;
	}

	/*
	 *  parse the input - we need the base profile types (already parsed on start up) and the extension attributes. the parser code
	 *  will merge the input definition with the base definitions. we require that an input definition extends the current base definition.
	 */
	public static Map<String, ProfileTypeImpl> parseProfileType(String inputExtensionString, ProfileType basePT, String tenantKey) throws Exception
	{
		// seed the type-hierarchy scope with this abstract base type, and then parse our global/static
		// profiles-types.xml definition from which we will pick up snx:mtperson
		Map<String, ExtensionType> extensionProperties = DMConfig.instance().getExtensionPropertiesToType(); // extension properties
		Map<String, ProfileTypeImpl> scope = null;
//		scope = new HashMap<String, ProfileTypeImpl>(1);
//		scope.put(basePT.getId(), (ProfileTypeImpl)basePT);
		scope = getGlobalProfileTypes();
		Map<String, ProfileTypeImpl> types = ProfileTypeParser.parseTypes(inputExtensionString, extensionProperties, scope, tenantKey);
		return types;
	}

	public static ProfileType validatePTForCloud50(String tenantId, ProfileType basePT,  Map<String, ProfileTypeImpl> ptTypes, boolean isValidationNeeded)
	{
		String methodName = "validateForCloud50";

		ProfileType profileType = null;

		if (LOG.isDebugEnabled())
			LOG.debug(CLASS_NAME + "." + methodName + "(" + tenantId  + ", " + ptTypes.toString() + ")");

		String requestPTId = basePT.getId();
		// SC5.0 : Note hard-coding this to 'default' for SC 5.0 - in future, we'll need to scan the xml string & extract the id tag value 
		requestPTId = ProfileTypeConfig.DEFAULT_TYPE_ID;
		// pick out newly defined ProfileType and validate it against caller's expectations
//		Map<String, ProfileTypeImpl> origTypes =  getGlobalProfileTypes();
//		Map<String, ProfileTypeImpl> newType   =  getNewestProfileType(ptTypes, origTypes);
		ProfileTypeImpl newTypePT = ptTypes.get(requestPTId);
		//caller is asking for a PT that has a specified tenant ID - verify that this one matches
		if (null != newTypePT) { // BP here for testing on ST - orgId = IGNORE_TENANTKEY
			if (tenantId.equalsIgnoreCase(newTypePT.getOrgId())) {
				profileType = newTypePT;
				// if the definition came from SC via the Admin API, validate the object; if it came from our cache, then assume it was already valid
				String error = null;
				if (null != profileType) {
					if (isValidationNeeded) { 
						error = validateInputProfileType(profileType, basePT, tenantId);
						if ( error != null){
							profileType = null;
							LOG.warn(CLASS_NAME + "." + methodName + " invalid ProfileType payload detected with error: " + error);
						}
					}
				}
			}
		}
		return profileType;
	}

	private static String validateInputProfileType(ProfileType profileType, ProfileType basePT, String orgId )
	{
		String methodName = "validateInputProfileType";

		String retMsg = null;
		boolean foundError = false;
		try {
			String verifyOrgId    = profileType.getOrgId();
			String verifyParentId = profileType.getParentId();
			if (StringUtils.equals(verifyOrgId, orgId) == false){
				foundError = true;
				retMsg = "orgId of payload: " + verifyOrgId + " does not match request orgId: " + orgId;
			}
			// require that parent id is equal to snx:mtperson?
			if (!foundError) { // don't waste time if we already have an error
				if (StringUtils.equals(verifyParentId, ProfileTypeConfig.MT_BASE_TYPE_ID) == false){
					foundError = true;
					retMsg = "parentId of payload: " + profileType.getParentId() + " is not " + ProfileTypeConfig.MT_BASE_TYPE_ID;
				}
			}
			if (!foundError) { // don't waste time if we already have an error

				// can check that all property type ref ids are valid, etc?
				// the parser should have filtered any bad XML that did not match the allowable
				// extended property ref IDs (from profiled-config.xml)
				List<Property> ptProperties = (List<Property>) profileType.getProperties();
				Iterator<Property> it = ptProperties.iterator();
				while( !foundError && it.hasNext() )
				{
					Property property = (Property)it.next();
					String   propName = property.getRef();
//					if (LOG.isDebugEnabled()) {
//						LOG.debug(CLASS_NAME + "." + methodName + " : validating " + propName);
//					}
					if (property.isExtension()) {
						// validate the extended property attributes
						retMsg = validateExtensionProperty(property, propName);
					}
					else {
						// validate the regular property attributes against prohibited set
						boolean isUpdateProhibited = isUpdateProhibited(property);
						if (isUpdateProhibited) {
							// potentially a disallowed change; but only if an update was attempted
							boolean isPropertyUpdated = isPropertyUpdated(property, basePT);
							if (isPropertyUpdated) {
								foundError = true;
								retMsg = "The attributes of property '" + propName + "' cannot be updated";
							}
							else {
								// no prohibited update was attempted - ok
							}
						}
						else {
							// an allowed update - anything we want to do here ?
//							boolean isValidUpdate = isValidPropertyUpdate(property, basePT);
//							if (! isValidUpdate) {
//								foundError = true;
//								retMsg = "Attempt to assign invalid value(s) to the attributes of property '" + propName;
//							}
						}
					}
					foundError = (retMsg != null);
//					if (LOG.isDebugEnabled()) {
//						LOG.debug(CLASS_NAME + "." + methodName + " : validating " + propName + " - processed OK");
//					}
				}
			}
		}
		catch (Exception ex) {
			String msg = ex.getLocalizedMessage();
			String errorMsg = ((StringUtils.isEmpty(msg)) ? ex.getMessage() : msg);
			errorMsg = ((StringUtils.isEmpty(errorMsg)) ? ex.toString() : errorMsg);
			retMsg = "validating Profile Type input for orgId: " + orgId + " failed : error " + errorMsg;
			LOG.error(CLASS_NAME + "." + methodName + " : Internal error. " + errorMsg);
			if (LOG.isTraceEnabled())
				ex.printStackTrace();
		}
		return retMsg;
	}

	private static boolean isUpdateProhibited(Property property)
	{
		boolean isProhibited = false;
		String  propName = property.getRef();
		int numProhibited = prohibitedProperties.length;
		int i = 0;
		while (! isProhibited && (i < numProhibited)) {
			if (propName.equalsIgnoreCase(prohibitedProperties[i])) {
				isProhibited = true;
			}
			i++;
		}
		return isProhibited;
	}

	private static boolean isPropertyUpdated(Property property, ProfileType basePT) {
		boolean isPropertyUpdated = false;
		// find this property in the base set
		String  propName = property.getRef();
		Property baseProperty = findProperty(propName, basePT);
		if (null != baseProperty)
			isPropertyUpdated = (isPropertyChanged(property, baseProperty));

		return isPropertyUpdated;
	}

	private static boolean isValidPropertyUpdate(Property property, ProfileType basePT) {
		boolean isValidPropertyUpdate = false;
		// find this property in the base set
		String  propName = property.getRef();
		Property baseProperty = findProperty(propName, basePT);
		if (null != baseProperty)
			isValidPropertyUpdate = true;
				// (isPropertyChangeValid(property, baseProperty));

		return isValidPropertyUpdate;
	}

	private static Property findProperty(String propName, ProfileType basePT) {
		boolean found = false;
		List<Property> ptProperties = (List<Property>) basePT.getProperties();
		Iterator<Property> it = ptProperties.iterator();
		Property baseProperty = null;
		while( !found && it.hasNext() )	{
			baseProperty = (Property)it.next();
			String   baseRef = baseProperty.getRef();
			if (propName.equalsIgnoreCase(baseRef)) {
				found = true;
			}
		}
		return baseProperty;
	}

	private static boolean isPropertyChanged(Property property, Property baseProperty) {
		boolean isPropertyChanged = false;
		Updatability propUpdate = property.getUpdatability();
		String    propUpdateVal = propUpdate.getValue().toLowerCase();
		Updatability baseUpdate = baseProperty.getUpdatability();
		String    baseUpdateVal = baseUpdate.getValue().toLowerCase();
		boolean isError = !(StringUtils.equals(propUpdateVal, Updatability.READ.value) || StringUtils.equals(propUpdateVal, Updatability.READWRITE.value));
		if (! isError) {
			if (! baseUpdateVal.equalsIgnoreCase(propUpdateVal))
				isPropertyChanged = true;
		}
		return isPropertyChanged;
	}

	private static String validateExtensionProperty(Property property, String ref) {
		String retMsg = null;
		boolean foundError = false;
		// let's validate what we can of the property 'item' fields
		/*
			<property>
				<ref>item1</ref>
				<updatability>readwrite</updatability>
				<label updatability="read"> <![CDATA[value1]]> </label>
			</property>
		*/
		// <ref>item1</ref>
		// verify that the item id is between 1 & 10 (inclusive)
		final String item = "item";
		if (StringUtils.startsWithIgnoreCase(ref, item)) {
			String refString = ref.split(item)[1].split("<")[0];
			try {
				Integer refVal = Integer.valueOf(refString);
				if ((0 > refVal) || (refVal > 10)) {
					foundError = true;
				}
			}
			catch (Exception e) {
				foundError = true;
			}
			if (foundError) {
				retMsg = "tag <ref> of payload contains a value [" + refString + "] that is outside of allowable range ( 1 - 10)";
			}
			else {
				// only validate the updatability & label properties for the 'item' extended attributes 
				// <updatability>readwrite</updatability>
				// verify that the updatability tag has a value and that it is either 'read' or 'readwrite'
				Updatability update = property.getUpdatability();
				String    updateVal = update.getValue().toLowerCase();
				foundError = !(StringUtils.equals(updateVal, Updatability.READ.value) || StringUtils.equals(updateVal, Updatability.READWRITE.value));
				if (foundError) {
					retMsg = "tag <updatability> " + " of payload contains a value [" + updateVal + "] that is not allowable ( read / readwrite )";
				}
				if (!foundError) { // don't waste time if we already have an error
					// <label updatability="read"> <![CDATA[value1]]> </label>
					// verify that the both the label and the updatability attribute have a value and that updatability is either 'read' or 'readwrite'
					Label label = property.getLabel();
					String labelVal = label.getLabel();
					update = label.getUpdatability();
					updateVal  = update.getValue().toLowerCase();
					foundError = ((StringUtils.isEmpty(labelVal) || StringUtils.isEmpty(updateVal))
							||  !((StringUtils.equals(updateVal, Updatability.READ.value) || StringUtils.equals(updateVal, Updatability.READWRITE.value))));
					if (foundError) {
						retMsg = "tag <label> " + " of payload contains incorrect value data [" + updateVal + "] that is not allowable ( read / readwrite )";
					}
				}
			}
		}
		return retMsg;
	}

	private static String validateRegularProperty(Property property, String ref) {
		String retMsg = null;
		boolean foundError = false;
		// let's validate what we can of the property 'item' fields
		/*
			<property>
				<ref>isManager</ref>
				<updatability>read</updatability>
				<hidden>false</hidden>
			</property>
		*/
		// <updatability>readwrite</updatability>
		// verify that the updatability tag has a value and that it is either 'read' or 'readwrite'
		Updatability update = property.getUpdatability();
		String    updateVal = update.getValue().toLowerCase();
		foundError = !(StringUtils.equals(updateVal, Updatability.READ.value) || StringUtils.equals(updateVal, Updatability.READWRITE.value));
		if (foundError) {
			retMsg = "tag <updatability> " + " of payload contains a value [" + updateVal + "] that is not allowable ( read / readwrite )";
		}
		return retMsg;
	}

	public static ProfileType createBaseProfileType(String parentId, String typeId) {
		// get a base ProfileType (default base data set)
		ProfileType basePT = ProfilesConfig.instance().getProfileTypeConfig().getBaseProfileType();
		if (null != basePT) {
			// assign the parentId & typeId
			((ProfileTypeImpl)(basePT)).setParentId(parentId);
			((ProfileTypeImpl)(basePT)).setId(typeId);
		}
		return basePT;
	}
	
	public static void logProfileType(ProfileType profileType)
	{
		String methodName = "logProfileType";
		String msg = "";
		if (null != profileType) {
			msg = getSerializedProfileType(profileType);
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + "." + methodName + " : " + profileType.getId() + " is :\n" + msg);
			}
		}
	}

	public static String getSerializedProfileType(ProfileType pt) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		serializeProfileType(pt, getWriter(baos));
        byte[] bytes = baos.toByteArray();
        String msg = new String(bytes);
		return msg;
	}
	private static StreamWriter getWriter(ByteArrayOutputStream baos) {
		WriterFactory writerFactory = Abdera.getNewWriterFactory();
		StreamWriter sw = writerFactory.newStreamWriter();
        OutputStreamWriter writer = new OutputStreamWriter(baos);
		sw.setOutputStream( baos );
		sw.setWriter( writer );
		return sw;
	}

	public static void serializeProfileType(ProfileType pt, StreamWriter sw)
	{
		sw.startDocument( ); // AtomConstants.XML_ENCODING, AtomConstants.XML_VERSION);
//			sw.startElement(ProfileTypeConstants.CONFIG);
		sw.startElement(ProfileTypeConstants.CONFIG.getLocalPart());
		// <config id="profiles-types">
		sw.writeAttribute("id", "profiles-types");
		{
//			sw.startElement(ProfileTypeConstants.TYPE);
			sw.startElement(ProfileTypeConstants.TYPE.getLocalPart());
			{
				sw.startElement(ProfileTypeConstants.PARENT_ID.getLocalPart());
					sw.writeElementText(pt.getParentId());
				sw.endElement(); // ProfileTypeConstants.PARENT_ID

				sw.startElement(ProfileTypeConstants.ID.getLocalPart());
					sw.writeElementText(pt.getId());
				sw.endElement(); // ProfileTypeConstants.ID

				for (Property property : pt.getProperties())
				{
					sw.startElement(ProfileTypeConstants.PROPERTY.getLocalPart());
						sw.startElement(ProfileTypeConstants.REF.getLocalPart());
							sw.writeElementText(property.getRef());
						sw.endElement(); // ProfileTypeConstants.REF

						sw.startElement(ProfileTypeConstants.UPDATABILITY.getLocalPart());
							sw.writeElementText(property.getUpdatability().getValue());
						sw.endElement(); // ProfileTypeConstants.UPDATABILITY

						sw.startElement(ProfileTypeConstants.HIDDEN.getLocalPart());
							sw.writeElementText(Boolean.toString(property.isHidden()));
						sw.endElement(); // ProfileTypeConstants.HIDDEN

						Label label = property.getLabel();
						if (null !=label) {
							sw.startElement(ProfileTypeConstants.LABEL.getLocalPart());
								sw.writeAttribute(ProfileTypeConstants.UPDATABILITY.getLocalPart(), label.getUpdatability().getValue());
								sw.writeElementText(label.getLabel());
							sw.endElement(); // ProfileTypeConstants.LABEL
						}
					sw.endElement(); // ProfileTypeConstants.PROPERTY
				}
			}
			sw.endElement(); // ProfileTypeConstants.TYPE
		}
		sw.endElement(); // ProfileTypeConstants.CONFIG
		sw.endDocument();
	}


	// currently Profiles does not support JSON pay-loads
	public static ProfileType getProfileTypeFromJSON(String payload, String orgId) {
		// TODO enhancement - retrieve the tenant's profile types from the cache as a JSON string and reformulate the ProfileType object
		ProfileType retValProfileType = null;
//
//		// TODO enhancement -
//				// String profileType  = TENANT_PROFILE_TYPE.toJSONString();
//				// TENANT_PROFILE_TYPE = TENANT_PROFILE_TYPE.fromJSONString(profileType);
//				if (retValProfileType == null)
//				{
//					if (LOG.isDebugEnabled()) {
//						LOG.debug(CLASS_NAME +".getProfileType : no cache of ProfileType found, loading cache for tenant : " + orgId);
//					}
//
//					try {
//						// look up profile type for this tenant
//						retValProfileType = loadProfileType(orgId);
//					}
//					catch (Exception ex){
//		//TODO			// what exception might we get
//					}
//
//					// TODO enhancement -
//					// serialize the ProfileType (the tenant's profile types) object to a JSON string and put that string in the cache
//					// String profileType  = TENANT_PROFILE_TYPE.toJSONString();
//					// TENANT_PROFILE_TYPE = TENANT_PROFILE_TYPE.fromJSONString(profileType);
//		//TODO : fix
////					if (LOG.isDebugEnabled()) {
////						List<ProfileType> theProfileTypes = retValProfileType.getTags();
////						if (null != theProfileTypes) {
////							int numTags = theProfileTypes.size();
////							LOG.debug(CLASS_NAME +".getProfileType : putting cache for tenant : " + orgId + " (" + numTags + " tags)");
////							if (numTags >0) {
////								int i = 0;
////								for (ProfileType tag : theProfileTypes) {
////									LOG.debug(CLASS_NAME +".getProfileType : -- (" + i++ + ") " + tag.getTag());
////								}
////							}
////						}
////					}
//					String cacheOrgId = orgId;
//					if (LOG.isDebugEnabled()) {
//						_testingMT = false;
//						// HACK for testing - set BP here & flip _testingMT flag
//						if (_testingMT)
//						{
//							Employee currentUser = AppContextAccess.getCurrentUserProfile();
//							if (null != currentUser) 
//							{
//								cacheOrgId = getTestMTTenantKey(currentUser);
//							}
//						}
//					}
//					_perTenantCache.put( cacheOrgId, retValProfileType, PROFILES_TYPES_CACHE_TTL );
//				}
//				else if (LOG.isDebugEnabled()) {
//					LOG.debug("ProfilesTypesCache: Found in cache, return profile types...");
//				}
				return retValProfileType;
	}


	// ================ test code for MT ==================

	// for debug testing of MT on an on-Premise deployment
	protected static boolean _testingMT = false;

	public static String getTestOrgId() {
		String testOrgId = null;
		if (LOG.isDebugEnabled()) {
			// HACK for testing - set BP here & flip _testingMT flag
			if (_testingMT)
			{
				Employee currentUser = AppContextAccess.getCurrentUserProfile();
				if (null != currentUser) 
				{
					testOrgId = getTestMTTenantKey(currentUser);
				}
			}
		}
		return testOrgId;
	}

	protected static String getTestMTTenantKey(Employee currentUser)
	{
		String tenantKey = null;
		if (_testingMT) {
			if (null != currentUser)
			{
				String whichAmy = currentUser.getDisplayName();
				if (null != whichAmy)
				{
					String amyPrefix = "Amy Jones";
					String amy1 = amyPrefix + "1";
					String amy2 = amyPrefix + "2";
					String amy3 = amyPrefix + "3";
					if ( whichAmy.startsWith(amy1))
						tenantKey = "hack_tenantKey_1";
					else if ( whichAmy.startsWith(amy2))
						tenantKey = "hack_tenantKey_2";
					else if ( whichAmy.startsWith(amy3))
						tenantKey = "hack_tenantKey_3";
					else 
						tenantKey = currentUser.getTenantKey();
				}
			}
		}
		return tenantKey;
	}

}
