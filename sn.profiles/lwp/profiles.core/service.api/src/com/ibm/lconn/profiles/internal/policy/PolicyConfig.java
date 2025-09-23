/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.policy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.core.util.ConfigHolder;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider.ConfigFileInfo;
import com.ibm.ventura.internal.config.exception.VenturaConfigException;

/**
 * This is the Singleton loading class for the initial Policy configuration(s).  It 
 * will load the internally specified policies as well as the externally defined
 * profiles-policy.xml.
 * 
 * These files are then available to the PolicyHolder class for initialization.
 */
public class PolicyConfig {
	
	private static final Logger LOGGER = Logger.getLogger(PolicyConfig.class.getName());
	private static final String POLICY_CONFIG_FILE_NAME = "profiles-policy";
	
	private URL internalPolicyUrl = null;;
	private HierarchicalConfiguration lccPolicyHc;
	private URL policyXsdUrl = null;
	
	private PolicyConfig() {
		initialize();
	}
	
	public URL getPolicyXsdUrl(){
		return policyXsdUrl;
	}
	
	public URL getInternalPolicy(){
		return internalPolicyUrl;
	}
	
	public HierarchicalConfiguration getLccPolicy(){
		return lccPolicyHc;
	}
	
	public void initialize(){
		boolean isDebug = LOGGER.isLoggable(Level.FINER);
		// reset policy settings
		
		internalPolicyUrl = lookupInternalBasePolicyUrl();
		if (isDebug) {LOGGER.finer("PolicyConfig.initialize set internal default policy: "+internalPolicyUrl);}
		lccPolicyHc = lookupLCCPolicyHc();
		if (isDebug) {LOGGER.finer("PolicyConfig.initialize set lcc HierarchicalConfiguration");}
		policyXsdUrl = lookupPolicyXsdUrl();
		if (isDebug) {LOGGER.finer("PolicyConfig.initialize set policy xsd file: "+policyXsdUrl);}
	}
	
	/**
	 * Utility method to ensure service works even if config file does not exist
	 * @return
	 */
	private final HierarchicalConfiguration lookupLCCPolicyHc() {
		final boolean isDebug = LOGGER.isLoggable(Level.FINER);
		HierarchicalConfiguration config = null;
		try {
			boolean havePolicyFile = false;
			for (Object configInfo :  VenturaConfigurationProvider.Factory.getInstance().getConfigFileInfo()) {
				ConfigFileInfo infoObj = (ConfigFileInfo) configInfo;
				if (isDebug) {
					LOGGER.finer("PolicyConfig - _getLCCPolicyObject: " + POLICY_CONFIG_FILE_NAME + " = " + infoObj.getId() + " ?");
				}
				if (POLICY_CONFIG_FILE_NAME.equals(infoObj.getId()) ) {
					havePolicyFile = true;
					break;
				}
			}
			if (havePolicyFile) {
				if (isDebug) {
					LOGGER.finer("PolicyConfig - _getLCCPolicyObject: policy config file found.");
				}
				config = (HierarchicalConfiguration) VenturaConfigurationProvider.Factory.getInstance().getConfiguration(POLICY_CONFIG_FILE_NAME);
			}
			else {
				if (isDebug) {
					LOGGER.finer("PolicyConfig - _getLCCPolicyObject: policy config file not found...");
				}
			}
		}
		catch (VenturaConfigException e) {
			throw new RuntimeException("PolicyConfig - _getLCCPolicyObject: Error with profiles-policy.xml file.", e);
		}
		// return an empty object rather than throwing an exception. jtw - why?
		// also ensure non-null-ness
		if (config == null) {
			config = new HierarchicalConfiguration();
		}
		return config;
	}
	
	private final URL lookupInternalBasePolicyUrl(){
		// the internal policy xml file is packaged in lc.profiles.core.service.api.jar. we do not place it in the
		// standard config directories as it is not available for public consumption. we have multiple environments
		// for locating the file: (1) runtime WAS, (2) mantis command line, (3) tdi, (4) eclipse runtime
		String internalXmlFileName = null;
		// this code will attempt to load the "internal" policy xml file from within the JAR. this should
		// work in cases (1), (2), (3).
		if (LCConfig.instance().isMTEnvironment()) {
			internalXmlFileName = PolicyConstants.DEFAULT_POLICY_OVERRIDE_MT;
		}
		else {
			internalXmlFileName = PolicyConstants.DEFAULT_POLICY_OVERRIDE_ST;
		}
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyConfig.constructor -  Internal xml config loading: " + internalXmlFileName);
		}
		URL rtn = this.getClass().getResource(internalXmlFileName);
		// if find no resource, look where it is expected for (4)
		if (rtn == null){
			String filePath = System.getProperty("test.config.files"); // constant for this?
			if (StringUtils.isNotEmpty(filePath)){
				filePath += internalXmlFileName;
				File internalFile = new File(filePath);
				if (internalFile.exists()){
					try{
						rtn = internalFile.toURI().toURL();
					}
					catch (MalformedURLException ex){
						rtn = null;
					}
				}
			}
		}
		return rtn;
	}
	
	// the internal policy xml file is packaged in lc.profiles.core.service.api.jar. we do not place it in the
	// standard config directories as it is not available for public consumption. we have multiple environments
	// for locating the file: (1) runtime WAS, (2) mantis command line, (3) tdi, (4) eclipse runtime
	// this code will attempt to load the internal policy xsd file from within the JAR. this should
	// work in cases (1), (2), (3).
	private final URL lookupPolicyXsdUrl(){
		//if (LCConfig.instance().isMTEnvironment()) {
			String internalXsdFileName = PolicyConstants.DEFAULT_POLICY_INTERNAL_XSD;
		//}
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("PolicyConfig.setPolicyXsdUrl -  Internal xml config loading: " + internalXsdFileName);
		}
		URL rtn = this.getClass().getResource(internalXsdFileName);
		// if find no resource, look where it is expected for (4)
		if (rtn == null){
			String filePath = System.getProperty("test.config.files"); // constant for this?
			if (StringUtils.isNotEmpty(filePath)){
				filePath += internalXsdFileName;
				File internalFile = new File(filePath);
				if (internalFile.exists()){
					try{
						rtn = internalFile.toURI().toURL();
					}
					catch (MalformedURLException ex){
						rtn = null;
					}
				}
			}
		}
		return rtn;
	}
	
	/**
	 * Accessor for configuration
	 * @return
	 */
	public static PolicyConfig instance() {
		return HOLDER.get();
	}

	/**
	 * Singleton holder instance
	 */
	private static final ConfigHolder<PolicyConfig> HOLDER = new ConfigHolder<PolicyConfig>() {
		@Override
		protected PolicyConfig newInstance() throws Exception {
			return new PolicyConfig();
		}
	};
}
