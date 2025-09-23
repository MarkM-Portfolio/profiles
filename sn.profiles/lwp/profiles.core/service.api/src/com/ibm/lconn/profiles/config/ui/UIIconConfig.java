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

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringEscapeUtils;

import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.data.AbstractDataObject;
import com.ibm.lconn.profiles.internal.util.UrlSubstituter;

/**
 *
 *
 */
public class UIIconConfig extends AbstractDataObject<UIIconConfig> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4356908151650953549L;
	
	private final UILabelConfig alt;
	private final String href;
	private final String jsonHref;
	
	public UIIconConfig(HierarchicalConfiguration iconConfig) {
		this.alt = new UILabelConfig(iconConfig.subset("alt"));
		this.href = iconConfig.getString("[@href]");
		this.jsonHref = StringEscapeUtils.escapeJavaScript(this.href);
	}
	
	UIIconConfig(UIIconConfig toCopy, boolean secure) {
		this.alt = toCopy.getAlt();
		this.href = UrlSubstituter.resolve(toCopy.getHref(), null, secure);
		this.jsonHref = StringEscapeUtils.escapeJavaScript(this.href);
	}

	/**
	 * @return the alt
	 */
	public final UILabelConfig getAlt() {
		return alt;
	}

	/**
	 * @return the href
	 */
	public final String getHref() {
		return href;
	}
	
	/**
	 * @return jsonHref
	 */
	public final String getJsonHref() {
		return jsonHref;
	}

}
