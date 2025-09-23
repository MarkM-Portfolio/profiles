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
package com.ibm.lconn.profiles.config.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.ibm.lconn.core.web.util.resourcebundle.ResourcesConfig;
import com.ibm.lconn.profiles.config.AbstractConfigObject;
import com.ibm.lconn.profiles.config.ProfilesConfig;

public class UIConfig extends AbstractConfigObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7852670462934290043L;
	
	private final VCardExportConfig vcardExportConfig;
	private final Map<String, UIBusinessCardConfig> businessCardConfigs;
	
	/**
	 * CTOR for WebApp
	 * 
	 * @param profilesConfig
	 */
	public UIConfig(HierarchicalConfiguration profilesConfig) {
		HierarchicalConfiguration layoutConfig = (HierarchicalConfiguration) profilesConfig.subset("layoutConfiguration");
		this.vcardExportConfig = new VCardExportConfig((HierarchicalConfiguration) layoutConfig.subset("vcardExport"));
		this.businessCardConfigs = Collections.unmodifiableMap(initBizCardConfigs(layoutConfig));
	}
	
	/**
	 * CTOR for TDI
	 *
	 */
	public UIConfig() {
		this.vcardExportConfig = new VCardExportConfig();
		this.businessCardConfigs = Collections.emptyMap();
	}

	private final Map<String, UIBusinessCardConfig> initBizCardConfigs(HierarchicalConfiguration layoutConfig) {
		Map<String, UIBusinessCardConfig> m = new HashMap<String, UIBusinessCardConfig>();
		int maxIndex = layoutConfig.getMaxIndex("businessCardLayout");
		
		for (int i = 0; i <= maxIndex; i++) {
			UIBusinessCardConfig c = new UIBusinessCardConfig(
					(HierarchicalConfiguration)layoutConfig.subset("businessCardLayout(" + i + ")"));
			m.put(c.getProfileType(), c);
		}
		
		return m;
	}

	/**
	 * Convienence Method to retrieve UI config
	 * @return
	 */
	public static final UIConfig instance() {
		return ProfilesConfig.instance().getUIConfig();
	}

	/**
	 * @return the resourcesConfig
	 */
	public final ResourcesConfig getResourcesConfig() {
		return ResourcesConfig.instance();
	}

	/**
	 * @return the vcardExportConfig
	 */
	public final VCardExportConfig getVCardExportConfig() {
		return vcardExportConfig;
	}
	
	/**
	 * @return the businessCardConfigs
	 */
	public final Map<String, UIBusinessCardConfig> getBusinessCardConfigs() {
		return businessCardConfigs;
	}
	
	/**
	 * Utility method to get a business card config by profile type
	 * @param profileType
	 * @return
	 */
	public final UIBusinessCardConfig getBusinessCardConfig(String profileType) {
		UIBusinessCardConfig c = businessCardConfigs.get(profileType);
		if (c == null) c = businessCardConfigs.get("default");
		if (c == null && businessCardConfigs.size() > 0) 
			c = businessCardConfigs.get(businessCardConfigs.keySet().iterator().next());
		return c;
	}
}
