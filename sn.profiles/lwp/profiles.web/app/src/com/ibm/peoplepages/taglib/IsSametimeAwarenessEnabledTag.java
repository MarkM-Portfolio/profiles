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

package com.ibm.peoplepages.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;

import com.ibm.peoplepages.data.Employee;
import com.ibm.lconn.profiles.config.ui.UIBusinessCardConfig;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

import org.apache.commons.configuration.Configuration;
import com.ibm.lconn.core.web.util.services.ServiceReferenceUtil;
import com.ibm.ventura.internal.config.api.VenturaConfigurationProvider;
import com.ibm.ventura.internal.config.exception.ConfigurationNotFoundException;

public class IsSametimeAwarenessEnabledTag extends ValueWriterTag {

	private static final String CONNECT_ENABLED_PROPERTY = "sametimeAwareness[@enabled]";

	@Override
	protected String getValue() throws JspException, IOException {

		boolean isSametimeEnabled = false; //default is off
		
		//first check to see if the overall feature is enabled
		try {
			VenturaConfigurationProvider provider = VenturaConfigurationProvider.Factory.getInstance();
			try	{
				Configuration config = provider.getConfiguration(ServiceReferenceUtil.Service.PROFILES);
				isSametimeEnabled = config.getBoolean(CONNECT_ENABLED_PROPERTY);
			} catch (ConfigurationNotFoundException e) {}
		} catch (Exception e) {}
		
		//next check to see if the current user enabled
		if (isSametimeEnabled) {
			try {
				UIBusinessCardConfig cardConfig;
				Employee profile = AppContextAccess.getCurrentUserProfile();
				if (profile != null) {
					cardConfig = UIBusinessCardConfig.instance(profile.getProfileType());
				} else {
					cardConfig = UIBusinessCardConfig.instance();
				}
				isSametimeEnabled = cardConfig.isEnableSametimeAwareness();
			} catch (Exception e) {}
		}
		
		return String.valueOf(isSametimeEnabled);
	}
}
