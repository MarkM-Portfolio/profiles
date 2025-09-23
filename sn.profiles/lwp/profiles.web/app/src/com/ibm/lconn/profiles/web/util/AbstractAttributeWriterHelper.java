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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * Utility methods for outputting attributes. This contains the logic to handle
 * pre/post-pending HTML. Methods for working with labels, etc. Extenders must
 * specify the ofering of outputting regular attributes and HTML attributes
 * 
 *
 */
public abstract class AbstractAttributeWriterHelper extends AttributeWriterValueUtils {
	
	protected final boolean wrapLabelWithLabel;
	
	protected AbstractAttributeWriterHelper(
			final ServletContext context, 
			final HttpServletRequest request, 
			final Writer writer,
			final boolean wrapLabelWithLabel) 
	{
		super(context, request, writer);
		this.wrapLabelWithLabel = wrapLabelWithLabel;
	}

	/**
	 * Utility method to write attribute
	 * 
	 * @param attr
	 * @throws IOException
	 */
	public final void writeAttr(final UIAttributeConfig attr) 
		throws IOException
	{
		if (!canDisplay(attr))
			return;
		
		// Is an '<html>' - config attribute
		if (attr.isHtml()) {
			writeHtmlAttr(attr);
		} 
		
		// Is '<attribute>photo</attribute>'
		else if (attr.getIsPhoto()) {
			writePhotoAttr(attr);
		}
		
		// Is a regular attribute
		else {
			String value = getAttrValue(attr);
			boolean empty = StringUtils.isEmpty(value);
			
			writeValueAttr(attr, empty, value);
		}
	}
	
	/**
	 * Basic method for writing a value attribute
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	protected abstract void writeValueAttr(final UIAttributeConfig attr, final boolean empty, final String value) 
		throws IOException;
	
	/**
	 * Utility method to write HTML attributes.
	 * 
	 * @param attr
	 */
	protected abstract void writeHtmlAttr(final UIAttributeConfig attr) 
		throws IOException;	

	/**
	 * Utility to write photo attributes. 
	 * @param attr
	 * @throws IOException
	 */
	protected abstract void writePhotoAttr(final UIAttributeConfig attr) 
		throws IOException;
	
	/**
	 * Write append html
	 * 
	 * @param attr
	 * @throws IOException
	 */
	protected final void writeAppendHtml(final UIAttributeConfig attr) throws IOException {
		write(attr.getAppendHTML());
	}

	/**
	 * Utility method to write the prependHtml.  This handles 'displayName' special case for javelin.
	 * 
	 * @param attr
	 * @throws IOException
	 */
	protected final void writePrependHtml(final UIAttributeConfig attr) 
		throws IOException
	{
		String prependHtml = attr.getPrependHTML();
		if (prependHtml != null && "displayName".equals(attr.getAttributeId())) 
			prependHtml = prependHtml.replace("%displayNameId%", getDisplayNameId(attr));
		write(prependHtml);
	}

	/**
	 * Utility method to get the display name id for a user.  Special case for javelin.
	 * @param attr
	 * @return
	 */
	private final String getDisplayNameId(final UIAttributeConfig attr) {
		final String email = profile.getEmail();
		
		if(ProfilesConfig.instance().getSametimeConfig().getSametimeInputType().equalsIgnoreCase("uid")){
			return profile.getUid() + "vcardNameElem";
		}
		else if (email == null || !LCConfig.instance().isEmailReturned()) {
			return profile.getUserid() + "vcardNameElem";
		}
		else {
			return email + "vcardNameElem";
		}
	}

	/**
	 * Check the display rules and skip if the attribute should be skipped
	 * 
	 * @param attr
	 * @return
	 */
	private final boolean canDisplay(final UIAttributeConfig attr) {
		
		if (!attr.isHtml())
			return (allowEmailInReturn || !PeoplePagesServiceConstants.EMAIL.equals(attr.getAttributeId()));
			
		return true;
	}

	/**
	 * Writes a label with a space after it
	 * 
	 * @param attr
	 * @throws IOException
	 */
	protected void writeLabel(final UIAttributeConfig attr) throws IOException {
		if (attr.getIsShowLabel()) {
			if (wrapLabelWithLabel) {
				write("<label for='").write(attr.getAttributeId()).write("'>");
			}
			writer.write(getLabel(attr));
			if (wrapLabelWithLabel) {
				write("</label>");
			}
			writer.write(" ");
		}
	}
	
	/**
	 * Utility method to get the label for the attribute
	 * 
	 * @param attr
	 * @return
	 */
	protected final String getLabel(final UIAttributeConfig attr) {
		final UILabelConfig label = attr.getLabel();
		if (label == null)
			return "!empty-label-key!";
			
		return label(null, label);
	}

}
