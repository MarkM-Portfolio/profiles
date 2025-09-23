/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.policy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.exception.PolicyException;
import com.ibm.lconn.profiles.policy.Feature;

public class PolicyParser {
	protected final static String CLASS_NAME = PolicyParser.class.getName();
	protected static Logger LOGGER = Logger.getLogger(CLASS_NAME);
	
	// try validator - http://stackoverflow.com/questions/28770257/commons-xmlconfiguration-and-schema-validation
	public static void parsePolicy(URL fileURL, String orgId, boolean modifiable, OrgPolicy orgPolicy) throws PolicyException {
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		if (isDebug){
			LOGGER.log(Level.INFO,"PolicyParser.parsePolicy fileURL ; "+debugString(fileURL,orgId,modifiable,orgPolicy).toString());
		}
		InputStream is = null;
		try {
			if (fileURL != null) {
				XMLConfiguration xmlconfig = null;
				is = fileURL.openStream();
				if (is != null) {
					// we got a reference to the xml file stream, load the features and permissions
					xmlconfig = new XMLConfiguration();
					xmlconfig.setDocumentBuilder(getValidatingDocBuilder());
					xmlconfig.setValidating(true);
					xmlconfig.load(new java.io.InputStreamReader(is));
					// we got a reference to the xml file stream, go ahead and load the features and permissions
					// since this is the internal definition of the policies, set the modifiable flag to "false"
					parse(xmlconfig, orgId, modifiable, orgPolicy);
					if (isDebug) {
						LOGGER.log(Level.INFO, "PolicyParser parsed " + debugString(fileURL, orgId, modifiable, orgPolicy).toString());
					}
				}
			}
		}
		catch (Exception ex){
			StringBuffer sb = debugString(fileURL, orgId, modifiable, orgPolicy);
			LOGGER.log(Level.SEVERE, "PolicyParser error opening xmlconfig " + sb.toString());
			throw new PolicyException(sb.toString(),ex);
		}
		finally{
			if (is != null){try{is.close();}catch(IOException ignore){}}
		}
	}
	
	public static void parsePolicy(String xmlString, String orgId, boolean modifiable, OrgPolicy orgPolicy) throws PolicyException {
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		if (isDebug){
			LOGGER.log(Level.INFO,"PolicyParser.parsePolicy xmlString ; "+debugString(xmlString,orgId,modifiable,orgPolicy).toString());
		}
		String trimXmlString = StringUtils.trimToEmpty(xmlString);
		InputStream is = null;
		try {
			XMLConfiguration xmlconfig = null;
			is = new ByteArrayInputStream(trimXmlString.getBytes(Charset.forName("UTF-8")));
			if (is != null) {
				// we got a reference to the xml file stream, load the features and permissions...
				xmlconfig = new XMLConfiguration();
				xmlconfig.setDocumentBuilder(getValidatingDocBuilder());
				xmlconfig.setValidating(true);
				xmlconfig.load(new java.io.InputStreamReader(is));
				// we got a reference to the xml file stream, go ahead and load the features and permissions...
				// since this is the internal definition of the policies, set the modifiable flag to "false"
				parse(xmlconfig,orgId,modifiable,orgPolicy);
				if (isDebug) {
					LOGGER.log(Level.INFO, "PolicyParser parsed " + debugString(xmlString, orgId, modifiable, orgPolicy).toString());
				}
			}
		}
		catch (ConfigurationException cEx) {
			StringBuffer sb = new StringBuffer("Parser configuration exception:\n").append(cEx.getMessage());
			LOGGER.log(Level.SEVERE, "PolicyParser error opening xmlconfig " + sb.toString());
			throw new PolicyException(sb.toString(),cEx);
		}
		catch (Exception ex){
			StringBuffer sb = new StringBuffer("Parser general error:\n").append(ex.getMessage()).append("\n").append(ex.getStackTrace());
			LOGGER.log(Level.SEVERE, "PolicyParser error opening xmlconfig " + sb.toString());
			throw new PolicyException(sb.toString(),ex);
		}
		finally{
			if (is != null){try{is.close();}catch(IOException ignore){}}
		}
	}
		
	public static void parsePolicy(HierarchicalConfiguration config, String orgId, boolean modifiable, OrgPolicy orgPolicy) throws PolicyException {
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		if (isDebug) {
			LOGGER.log(Level.INFO, "PolicyParser.parsePolicy fileURL ; " + debugString(config, orgId, modifiable, orgPolicy).toString());
		}
		if (config != null) {
			parse(config, orgId, modifiable, orgPolicy);
			if (isDebug) {
				LOGGER.log(Level.INFO, "PolicyParser parsed " + debugString(config, orgId, modifiable, orgPolicy).toString());
			}
		}
	}
	
	private static DocumentBuilder getValidatingDocBuilder() throws SAXException, ParserConfigurationException {
		Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(PolicyConfig.instance().getPolicyXsdUrl());
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true);
		docBuilderFactory.setSchema(schema);
		
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		//if you want an exception to be thrown when there is invalid xml document,
		//you need to set your own ErrorHandler because the default
		//behavior is to just print an error message.
		docBuilder.setErrorHandler(
			new ErrorHandler() {
			//@Override
			public void warning(SAXParseException exception) throws SAXException {
				throw exception;
			}

			//@Override
			public void error(SAXParseException exception) throws SAXException {
				throw exception;
			}

			//@Override
			public void fatalError(SAXParseException exception)  throws SAXException {
				throw exception;
			}  
		});
		return docBuilder;
	}
	
	private static void parse(HierarchicalConfiguration config, String orgId, boolean modifiable, OrgPolicy orgPolicy) { //throws PolicyException {
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		/*
		xml is in this format we need to traverse...
		------------
		<features orgId="[ORGID]">
			<feature name="profile.board">
				<profileType type="advisor" actorType="default" identity="freemium" actorIdentity="freemium" enabled="true">
					<acl name="profile.board.write.message" scope="colleagues_and_self" />
					<acl name="profile.board.write.comment" scope="colleagues_and_self" />
				</profileType>
				...
			</feature>
			...
		</features>
		...
		 */		
		// Here is where we start drilling down through the config objects to pull out the info we need to
		// build our composite key that'll eventually populate the Permission object
		//
		//<features>
		int maxFeatures = config.getMaxIndex(PolicyConstants.NODE_FEATURES.getLocalPart());
		for (int i = 0; i <= maxFeatures; i++) {
			HierarchicalConfiguration featuresConfig = (HierarchicalConfiguration) config.subset(PolicyConstants.NODE_FEATURES.getLocalPart()+"(" + i + ")");
			String featuresOrgId = featuresConfig.getString("[@"+PolicyConstants.ATTR_ORGID.getLocalPart()+"]", PolicyConstants.DEFAULT_ORG);
			if (orgId.equals(featuresOrgId)){
				if (isDebug) {
					LOGGER.log(Level.INFO,"PolicyParser.parse: located features for orgId and will continue parsing: " + orgId);
				}		
				//<feature>
				int maxFeature = featuresConfig.getMaxIndex(PolicyConstants.NODE_FEATURE.getLocalPart());
				for (int j = 0; j <= maxFeature; j++) {
					HierarchicalConfiguration featureConfig = (HierarchicalConfiguration) featuresConfig.subset(PolicyConstants.NODE_FEATURE.getLocalPart()+"(" + j + ")");
					String featureName = featureConfig.getString("[@"+PolicyConstants.ATTR_NAME.getLocalPart()+"]");
					//make sure the feature is legit
					if (!Feature.isValid(featureName)){
						throw new PolicyException("PolicyParser.parse invalid feature name: "+featureName);
					}
					if (!isValidName(featureName)){
						throwInvalidNameException(featureName);
					}
					//<profileType>
					int maxprofileType = featureConfig.getMaxIndex(PolicyConstants.NODE_PROFILETYPE.getLocalPart());
					for (int k = 0; k <= maxprofileType; k++) {
						HierarchicalConfiguration profileTypeConfig = (HierarchicalConfiguration) featureConfig.subset(PolicyConstants.NODE_PROFILETYPE.getLocalPart()+"(" + k + ")");
						String targetIdentity = profileTypeConfig.getString("[@"+PolicyConstants.ATTR_IDENTITY.getLocalPart()+"]", PolicyConstants.DEFAULT_IDENTITY);
						if (!isValidName(targetIdentity)) throwInvalidNameException(targetIdentity);
						String actorIdentity = profileTypeConfig.getString("[@"+PolicyConstants.ATTR_ACTORIDENTITY.getLocalPart()+"]", PolicyConstants.DEFAULT_IDENTITY);
						if (!isValidName(actorIdentity)) throwInvalidNameException(targetIdentity);
						String targetMode = profileTypeConfig.getString("[@"+PolicyConstants.ATTR_MODE.getLocalPart()+"]", PolicyConstants.DEFAULT_MODE);
						if (!isValidMode(targetMode)) throwInvalidModeException(targetIdentity);
						String actorMode = profileTypeConfig.getString("[@"+PolicyConstants.ATTR_ACTORMODE.getLocalPart()+"]", PolicyConstants.DEFAULT_MODE);
						if (!isValidMode(actorMode)) throwInvalidModeException(targetIdentity);
						String targetType = profileTypeConfig.getString("[@"+PolicyConstants.ATTR_TYPE.getLocalPart()+"]", PolicyConstants.DEFAULT_TYPE);
						if (!isValidName(targetType)) throwInvalidNameException(targetIdentity);
						String actorType = profileTypeConfig.getString("[@"+PolicyConstants.ATTR_ACTORTYPE.getLocalPart()+"]", PolicyConstants.DEFAULT_TYPE);
						if (!isValidName(actorType)) throwInvalidNameException(targetIdentity);
						boolean enabled = profileTypeConfig.getBoolean("[@"+PolicyConstants.ATTR_ENABLED.getLocalPart()+"]", true);					
						//set the permission of the overall feature
						try {
							FeatureLookupKey featureLookupKey = new FeatureLookupKey(
								featureName, 
								targetIdentity, actorIdentity,
								targetMode, actorMode,
								targetType, actorType
							);
							orgPolicy.setIsFeatureEnabled(orgId, featureLookupKey, enabled, modifiable); //TODO - don't need tenant key unless it is used for validation that we are updating the correct OrgPolicy
						}
						catch (PolicyException ufEx) {
							LOGGER.log(Level.WARNING,"PolicyParser.parse - unabled to set feature: " +featureName+": " + ufEx.getMessage());
							throw ufEx;
						}
						if (enabled) {
							//the feature itself is enabled, iterate through the acls
							//<acl>
							int maxAcl = profileTypeConfig.getMaxIndex(PolicyConstants.NODE_ACL.getLocalPart());
							for (int l = 0; l <= maxAcl; l++) {
								HierarchicalConfiguration aclConfig = (HierarchicalConfiguration) profileTypeConfig.subset(PolicyConstants.NODE_ACL.getLocalPart()+"(" + l + ")");
								String aclName = aclConfig.getString("[@"+PolicyConstants.ATTR_NAME.getLocalPart()+"]");
								String scopeName = aclConfig.getString("[@"+PolicyConstants.ATTR_SCOPE.getLocalPart()+"]");
								boolean dissallowNonAdminIfInactive = aclConfig.getBoolean("[@"+PolicyConstants.ATTR_DISALLOW.getLocalPart()+"]", false);
								//we've now got all our information, set the Permission
								try {
									PermissionLookupKey permissionLookupKey = new PermissionLookupKey(
										aclName, featureName, 
										targetIdentity, actorIdentity,
										targetMode, actorMode,
										targetType, actorType
									);
									if (isDebug) {
										LOGGER.log(Level.INFO,"PolicyParser.parse: adding scope [" + scopeName + "] for tenant " + orgId + " with lookupKey: " + permissionLookupKey );
									}
									orgPolicy.setPermission(orgId, permissionLookupKey, scopeName, dissallowNonAdminIfInactive, modifiable);
								}
								catch (PolicyException pEx) {
									LOGGER.log(Level.WARNING,"PolicyParser.parse - unable to set permission: " + pEx.getMessage());
									throw pEx;
								}
							}
						}
					}
				}
			}
			if (isDebug) {
				LOGGER.log(Level.INFO,"PolicyParser.parse: parsed policy config for org = " + orgPolicy.dumpPolicy() );
			}			
		}
	}
	
	private static boolean isValidName(String val){
		boolean rtn = (    val != null
						&& StringUtils.contains(val,LookupKey.delim)    == false
						&& StringUtils.contains(val,LookupKey.dbldelim) == false );
		return rtn;
	}
	
	private static boolean isValidMode(String val){
		boolean rtn = (    val != null
						&& StringUtils.equals(val,UserMode.EXTERNAL.getName()) == true
						|| StringUtils.equals(val,UserMode.INTERNAL.getName()) == true );
		return rtn;
	}
	
	private static void throwInvalidNameException(String name) throws PolicyException{
		StringBuffer sb = new StringBuffer("PolicyParser: policy definition an entry contains a reserved delimeter: '")
							.append(LookupKey.delim).append("' or '").append(LookupKey.dbldelim).append("'");
		throw new PolicyException(sb.toString());
	}
	
	private static void throwInvalidModeException(String name) throws PolicyException{
		StringBuffer sb = new StringBuffer("PolicyParser: policy definition an entry contains an invalid mode: "+name);
		throw new PolicyException(sb.toString());
	}
	
	private static StringBuffer debugString(URL fileURL, String orgId, boolean modifiable, OrgPolicy orgPolicy) {
		StringBuffer rtn = new StringBuffer("fileURL: ");
		rtn.append((fileURL != null) ? (rtn.append(fileURL.toExternalForm())) : (rtn.append("null")));
		rtn.append(" orgId").append(orgId).append(" modifiable: ").append(modifiable);
		return rtn;
	}
	
	private static StringBuffer debugString(String string, String orgId, boolean modifiable, OrgPolicy orgPolicy) {
		StringBuffer rtn = new StringBuffer("inputString: ");
		rtn.append((string != null) ? (rtn.append(string)) : (rtn.append("null")));
		rtn.append(" orgId").append(orgId).append(" modifiable: ").append(modifiable);
		return rtn;
	}
	
	private static StringBuffer debugString(HierarchicalConfiguration config, String orgId, boolean modifiable, OrgPolicy orgPolicy) {
		StringBuffer rtn = new StringBuffer("HierarchicalConfiguration: ");
		rtn.append((config != null) ? (rtn.append("...")) : (rtn.append("null")));
		rtn.append(" orgId").append(orgId).append(" modifiable: ").append(modifiable);
		return rtn;
	}
	
	//private static StringBuffer debugString(InputStream is, String orgId, boolean modifiable, OrgPolicy orgPolicy) {
	//	StringBuffer rtn = new StringBuffer("HierarchicalConfiguration: ");
	//	rtn.append((is != null) ? (rtn.append("...")) : (rtn.append("null")));
	//	rtn.append(" orgId").append(orgId).append(" modifiable: ").append(modifiable);
	//	return rtn;
	//}
}