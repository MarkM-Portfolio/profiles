/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.config.ui;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.config.AbstractConfigObject;
import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.internal.util.UrlSubstituter;

/**
 *
 */
public class UIBusinessCardActionConfig extends AbstractConfigObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2386728469309757756L;
	
	private static final Logger logger = Logger.getLogger(
			UIBusinessCardActionConfig.class.getName(),
			"com.ibm.lconn.profiles.resources.svcapi");

	private final String urlPattern;
	private final String jsonUrlPattern;
	private final boolean emailEnabledRequired;
	private final String liClass;
	private final UILabelConfig label;
	private final UIIconConfig icon;
	
	private final Acl requireAcl;
	private final boolean hideIfAnyConnection;  // TODO: slightly hacktastic solution to problem for 3.0.  It will work though...
	
	/**
	 * @param configuration
	 */
	public UIBusinessCardActionConfig(HierarchicalConfiguration config) {
		this.emailEnabledRequired = config.getBoolean("[@emailEnabledRequired]", false);
		this.urlPattern = config.getString("[@urlPattern]");
		this.jsonUrlPattern = StringEscapeUtils.escapeJavaScript(this.urlPattern);
		this.liClass = config.getString("[@liClass]");
		this.label = new UILabelConfig(config.subset("label"));
		
		if (config.getMaxIndex("icon") == 0) {
			this.icon = new UIIconConfig((HierarchicalConfiguration)config.subset("icon"));
		} else {
			this.icon = null;
		}
		
		// setup feature/acl
		String requireAclName = config.getString("[@requireAcl]", "").trim();
		Acl requireAclObj = null;
		boolean hideIfAnyConnection = config.getBoolean("[@hideIfAnyConnection]", false);
		
		if (StringUtils.isEmpty(requireAclName)) {
			logger.log(Level.FINER, "debug.config.noAclString", new Object[]{label.getKey(), requireAclName});			
		} else if (requireAclName.matches("\\w+(\\.\\w+)*\\$\\w+(\\.\\w+)*")) {
			logger.log(Level.FINER, "debug.config.validAclString", new Object[]{label.getKey(), requireAclName});
			
			String[] tmp = requireAclName.split("\\$");
			requireAclObj = Acl.getByName(tmp[1]);
			
			if (requireAclObj == null) {
				logger.log(Level.WARNING, "warning.config.unknownAclSetting", new Object[]{label.getKey(), requireAclName});
			} 
			// special case for colleagues; use different default
			else if (Acl.COLLEAGUE_CONNECT.equals(requireAclObj)) {
				hideIfAnyConnection = config.getBoolean("[@hideIfAnyConnection]", true);
			}
			
		} else {
			logger.log(Level.WARNING, "warning.config.invalidAclString", new Object[]{label.getKey(), requireAclName});
		}
		this.requireAcl = requireAclObj;
		this.hideIfAnyConnection = hideIfAnyConnection;
	}
	
	/**
	 * Utility method to clone this for 
	 * @param toClone
	 * @param profile
	 * @param secure
	 */
	UIBusinessCardActionConfig(UIBusinessCardActionConfig toClone, Map<String,String> profileSubMap, boolean secure) {
		this.emailEnabledRequired = toClone.isEmailEnabledRequired();
		this.urlPattern = UrlSubstituter.resolve(toClone.getUrlPattern(), profileSubMap, secure);
		this.jsonUrlPattern = StringEscapeUtils.escapeJavaScript(this.urlPattern);
		this.liClass = toClone.getLiClass();
		this.label = toClone.getLabel();
		this.icon = toClone.getIcon() == null ? null : new UIIconConfig(toClone.getIcon(), secure);
		this.requireAcl = toClone.requireAcl;
		this.hideIfAnyConnection = toClone.hideIfAnyConnection;
	}

	/**
	 * @return the emailEnabledRequired
	 */
	public final boolean isEmailEnabledRequired() {
		return emailEnabledRequired;
	}

	/**
	 * @return the label
	 */
	public final UILabelConfig getLabel() {
		return label;
	}

	/**
	 * @return the urlPattern
	 */
	public final String getUrlPattern() {
		return urlPattern;
	}
	
	/**
	 * Gets the urlPattern string encoded for JSON
	 * @return jsonUrlPattern
	 */
	public final String getJsonUrlPattern() {
		return jsonUrlPattern;
	}

	/**
	 * @return the icon
	 */
	public final UIIconConfig getIcon() {
		return icon;
	}

	/**
	 * @return the liClass
	 */
	public final String getLiClass() {
		return liClass;
	}

	/**
	 * @return the requireAcl
	 */
	public final Acl getRequireAcl() {
		return requireAcl;
	}

	/**
	 * @return the hideIfAnyConnection
	 */
	public final boolean isHideIfAnyConnection() {
		return hideIfAnyConnection;
	}

}
