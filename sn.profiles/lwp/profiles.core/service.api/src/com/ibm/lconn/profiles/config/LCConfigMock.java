/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.config;

import java.util.Properties;
import com.ibm.lconn.core.gatekeeper.LCSupportedFeature;
import com.ibm.lconn.profiles.internal.policy.PolicyConfig;

/**
 * Mock LCConfig object used in (stand alone) unit tests to inject configuration
 * settings indicating if the environment is on-prem or mt.
 */
public class LCConfigMock extends LCConfig
{
	private static final long serialVersionUID = 2L;

	ConfigSnapshot snapshot = null;

	Properties gkProps = null; // for holding GK and other config overrides

	LCConfigMock() {
		super();
		snapshot = new ConfigSnapshot(this.isLotusLive, this.isMTEnvironment);
	}

	public void inject(boolean isLL, boolean isMT) {
		if (isLL) isMT = true;
		snapshot = new ConfigSnapshot(this.isLotusLive, this.isMTEnvironment);
		this.isLotusLive = isLL;
		this.isMTEnvironment = isMT;
		// need to reset policy files - i.e. since we changed the config on-prem/cloud, we
		// need to read in the corresponding policy files
		PolicyConfig.instance().initialize();
	}

	public void revert() {
		this.isLotusLive = snapshot.isLL;
		this.isMTEnvironment = snapshot.isMT;
		instance = null;
		super.initMock();
		// need to reset policy files
		PolicyConfig.instance().initialize();
	}

	class ConfigSnapshot {
		boolean isLL;
		boolean isMT;

		ConfigSnapshot(boolean isLL, boolean isMT) {
			this.isLL = isLL;
			this.isMT = isMT;
		}
	}

	public void setEnabled(String gatekeeperSettingName, boolean propertyValue)
	{
		if (gatekeeperSettingName != null) {
			if (gkProps == null)
				gkProps = new Properties();
			gkProps.put(gatekeeperSettingName, propertyValue);
		}
	}
	public boolean isEnabled(LCSupportedFeature gatekeeperSetting, String gatekeeperSettingName, boolean propertyDefaultValue)
	{
		return isEnabled(gatekeeperSettingName, propertyDefaultValue);
	}
	public boolean isEnabled(String gatekeeperSettingName, boolean propertyDefaultValue)
	{
		boolean gkSetting = propertyDefaultValue;
		if (gatekeeperSettingName != null) {
			try {
				if (null != gkProps) {
					Object gkSettingValue = gkProps.get(gatekeeperSettingName);
					if (null != gkSettingValue) {
						if (gkSettingValue instanceof Boolean)
							gkSetting = (Boolean) gkSettingValue;
						else
							gkSetting = Boolean.parseBoolean((String) gkSettingValue);
					}
				}
			}
			catch (IllegalArgumentException iae) {
				gkSetting = propertyDefaultValue;
			}
		}
		return gkSetting;
	}

}
