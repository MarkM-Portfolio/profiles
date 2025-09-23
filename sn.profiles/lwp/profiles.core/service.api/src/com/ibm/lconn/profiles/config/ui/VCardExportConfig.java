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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.CharEncoding;

import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.config.AbstractConfigObject;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.resources.SvcApiRes;

/**
 *
 *
 */
public class VCardExportConfig extends AbstractConfigObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2558512418774096661L;

	public static class CharsetConfig {
		private final String name;
		private final UILabelConfig label;
		
		protected CharsetConfig(HierarchicalConfiguration csConfig) {
			this.name = csConfig.getString("[@name]");
			this.label = new UILabelConfig(csConfig.subset("label"));
			
			if (!CharEncoding.isSupported(this.name))
				throw new ProfilesRuntimeException(
					new ResourceBundleHelper(
						SvcApiRes.BUNDLE).getString("error.unsupported.char.encoding"));
		}

		/**
		 * @return the label
		 */
		public final UILabelConfig getLabel() {
			return label;
		}

		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}
	}
	
	private final List<CharsetConfig> charsets;
	
	/**
	 * CTor for WebApp
	 * @param vcExportConfig
	 */
	public VCardExportConfig(HierarchicalConfiguration vcExportConfig) {
		int maxCs = vcExportConfig.getMaxIndex("charset");
		List<CharsetConfig> charsets = new ArrayList<CharsetConfig>(maxCs+1);
		
		for (int i = 0; i <= maxCs; i++) {
			CharsetConfig csConfig = new CharsetConfig(
					(HierarchicalConfiguration) vcExportConfig.subset("charset(" + i + ")"));
			charsets.add(csConfig);
		}
		
		this.charsets = Collections.unmodifiableList(charsets);
	}
	
	/**
	 * CTor for TDI
	 */
	public VCardExportConfig() {
		this.charsets = Collections.emptyList();
	}

	/**
	 * @return the charsets
	 */
	public final List<CharsetConfig> getCharsets() {
		return charsets;
	}
	
	/**
	 * Convience method
	 * @return
	 */
	public static final VCardExportConfig instance() {
		return UIConfig.instance().getVCardExportConfig();
	}

}
