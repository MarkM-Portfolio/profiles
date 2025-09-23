/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.List;
import java.util.Locale;

import org.apache.abdera.writer.StreamWriter;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.peoplepages.data.Employee;

public abstract class BaseCardGenerator
{
	protected final StreamWriter sw;
	protected final String profilesURL;
	protected Employee emp = null;
	protected boolean isLite = true;

	private boolean inclLabels = false;
	private String  lang   = null;
	private Locale  locale = null;

	protected final boolean hideExtensionProfiName = PropertiesConfig.getBoolean(ConfigProperty.HIDE_EXTENSION_PROF_NAME);
	
	public BaseCardGenerator(StreamWriter sw, String url)
	{
		this.profilesURL = url;
		this.sw = sw;
	}

	public boolean isInclLabels() {
		return inclLabels;
	}

	public void setInclLabels(boolean includeLabels) {
		this.inclLabels = includeLabels;
	}

	public String getLanguage() {
		return lang;
	}

	public void setLanguage(String language) {
		this.lang = language;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	protected String buildCommaDelimitedTagList(List<Tag> tags)
	{
		StringBuilder buffer = new StringBuilder();

		if ( tags != null ) {
		    for (int i = 0; i < tags.size(); i++)
		    {
			if (i > 0)
			{
				buffer.append(",");
			}
			Tag tag = tags.get(i);
			buffer.append(tag.getTag());
		    }
		}
		return buffer.toString();
	}

}

