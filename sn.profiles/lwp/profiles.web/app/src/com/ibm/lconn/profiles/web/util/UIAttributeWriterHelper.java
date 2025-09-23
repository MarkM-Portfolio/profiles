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
package com.ibm.lconn.profiles.web.util;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;

/**
 * Base helper class for the web ui. This class is sub classed for search
 * results pages and the main attribute display page.
 * 
 *
 */
public final class UIAttributeWriterHelper extends
		AbstractAttributeWriterHelper 
{
	private final UIAttributeWriterConfig config;
	
	public UIAttributeWriterHelper(UIAttributeWriterConfig config, ServletContext context, HttpServletRequest request, Writer writer) {
		super(context, request, writer, true);
		this.config = config;
	}

	/**
	 * Does not write any value for HTML attributes.
	 */
	@Override
	protected final void writeHtmlAttr(UIAttributeConfig attr) throws IOException { }

	/**
	 * Write photo attr
	 * 
	 * @param params
	 * @throws IOException
	 */
	@Override
	protected void writePhotoAttr(UIAttributeConfig attr) throws IOException {
		// <img src='<c:url value="/photo.do?key=${uidAttribute}&lastMod=${lastUpdate}"/>' class="photo"  width="55" height="55" alt="${altValue}"/>
		write("<img src='").write(request.getContextPath()).write("/photo.do?key=").write(profile.getKey())
			.write("&lastMod=").write(String.valueOf(profile.getLastUpdate().getTime())).write("' class='photo'  width='55' height='55' alt='")
			.write(profile.getDisplayName()).write("'/>");
	}
	
	/**
	 * Writes a value if non-empty.
	 */
	@Override
	protected final void writeValueAttr(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		if (!empty || !attr.getIsHideIfEmpty()) {
			startAttribute(attr, empty, value);
			{
				// write the label
				startLabel(attr, empty, value);
				writeLabel(attr);
				endLabel(attr, empty, value);
				
				// write the value
				startValue(attr, empty, value);
				{
					writePrependHtml(attr);
					writeUIAttrValue(attr, empty, value);
					writeAppendHtml(attr);
				}
				endValue(attr, empty, value);
			}
			endAttribute(attr, empty, value);
		}
	}
	
	protected void startAttribute(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		write(config.getStartAttr());
	}
	
	protected void endAttribute(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		write(config.getEndAttr());
	}

	protected void startLabel(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		write(config.getStartLabel());
	}
	protected void endLabel(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		write(config.getEndLabel());
	}
	
	protected void startValue(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		write(config.getStartValue());
	}
	
	protected void endValue(final UIAttributeConfig attr, final boolean empty, final String value) throws IOException {
		write(config.getEndValue());
	}
}
