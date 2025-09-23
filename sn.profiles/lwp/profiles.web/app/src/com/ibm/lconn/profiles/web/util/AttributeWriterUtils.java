/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.util;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.peoplepages.data.Employee;

/**
 * Decomposition to put basic writing methods in base class. All pure utility
 * methods should go here.
 * 
 *
 */
public abstract class AttributeWriterUtils {

	protected static final String BUNDLE = "com.ibm.lconn.profiles.strings.uilabels";
	
	protected final HttpServletRequest request;
	protected final ServletContext context;
	protected final Writer writer;
	protected final boolean allowEmailInReturn = LCConfig.instance().isEmailReturned();
	
	protected final String lang;
	
	protected Employee profile = null; 
	
	protected AttributeWriterUtils(
			final ServletContext context, 
			final HttpServletRequest request, 
			final Writer writer) 
	{
		this.request = request;
		this.context = context;
		this.writer = writer;
		
		Locale locale = request.getLocale();
		if (locale == null) this.lang = "en";
		else this.lang = locale.toString().toLowerCase();
	}
	
	/**
	 * Writes a value HTML escaped
	 * @param str
	 * @return
	 * @throws IOException
	 */
	protected final AttributeWriterUtils writeEscHtml(final String str) throws IOException {
		if (str != null)
			StringEscapeUtils.escapeHtml(writer, str);
		return this;
	}
	
	/**
	 * Writes the value JavaScript escaped
	 * @param str
	 * @return
	 * @throws IOException
	 */
	protected final AttributeWriterUtils writeEscJS(final String str) throws IOException {
		if (str != null)
			StringEscapeUtils.escapeJavaScript(writer, str);
		return this;
	}	
	
	/**
	 * Writes a value escaped
	 * @param str
	 * @return
	 * @throws IOException
	 */
	protected final AttributeWriterUtils writeEscUrl(final String str) throws IOException {
		if (str != null)
			writer.write(java.net.URLEncoder.encode(str, "UTF-8"));
		return this;
	}

	/**
	 * writes the value skipping nulls
	 * 
	 * @param params
	 * @param str
	 * @throws IOException
	 */
	protected final AttributeWriterUtils write(final String str) throws IOException {
		if (str != null) 
			writer.write(str);
		return this;
	}
	
	/**
	 * Utility to get label from default bundle for key
	 * @param key
	 * @return
	 */
	protected final String label(String key) {
		return label(key, null);
	}
	
	/**
	 * Utility to get label given 
	 * @param key
	 * @param label
	 * @return
	 */
	protected final String label(String key, UILabelConfig label) {
		final String labelKey = label == null ? key : label.getKey();

		final ResourceBundle rb = isNotDefaultBundle(label) ?
				LocalizationHelper.getResourceBundle(
						context, request, BUNDLE, label) :
				defaultBundle();
						
		try {
			return rb.getString(labelKey);
		} catch (MissingResourceException e) {
			return "???" + labelKey +"???";
		}
	}

	
	/**
	 * Utility method to get the default bundle
	 */
	private ResourceBundle _defaultBundle = null;
	private final ResourceBundle defaultBundle() {
		if (_defaultBundle == null) {
			_defaultBundle = LocalizationHelper.getResourceBundle(
					context, request, BUNDLE, null);
		}
		return _defaultBundle;
	}
	
	/**
	 * Utility method to test if a label does not use the default bundle
	 * @param label
	 * @return
	 */
	private final boolean isNotDefaultBundle(final UILabelConfig label) {
		return (label != null && StringUtils.isNotEmpty(label.getBidref()));
	}
	
	/**
	 * Returns the context root
	 * @return
	 */
	protected final String contextRoot() {
		return request.getContextPath();
	}
	
	/**
	 * Retrieves the email attribute value
	 * @param attr
	 * @return
	 */
	protected final String emailValue(final UIAttributeConfig attr) {
		return (String) profile.get(attr.getEmail());
	}
	
	/**
	 * Retrieves the uid attribute value
	 * @param attr
	 * @return
	 */
	protected final String uidValue(final UIAttributeConfig attr) {
		return (String) profile.get(attr.getUid());
	}
	
	/**
	 * Retrieves the userid attribute value
	 * @param attr
	 * @return
	 */
	protected final String useridValue(final UIAttributeConfig attr) {
		return (String) profile.get(attr.getUserid());
	}
	
	/**
	 * Utility method to retrieve the attribue value
	 * @param profile
	 * @param attr
	 * @return
	 */
	protected final String getAttrValue(final UIAttributeConfig attr) {
		if (attr.isExtensionAttribute()) {
			ProfileExtension pe = (ProfileExtension) profile.get(attr.getAttributeId());
			if (pe != null)
				return pe.getStringValue();
			return null;
		} else if (attr.getAttributeId().startsWith("workLocation.")) {
			WorkLocation wl = profile.getWorkLocation();
			if (wl != null)
				return wl.getByAttributeId(attr.getAttributeId());
			return null;			
		} else {
			return (String) profile.get(attr.getAttributeId());			
		}
	}
	
	/**
	 * @param profile the profile to set
	 */
	public final void setProfile(Employee profile) {
		this.profile = profile;
	}
}
