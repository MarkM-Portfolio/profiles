/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * @author sberajaw
 */
public class HelpLangTag extends ValueWriterTag {
	
	private static final Map<Locale,Boolean> HAS_HELPLANGS = new ConcurrentHashMap<Locale,Boolean>(40);
	private static final Map<Locale,String> HELPLANGS = new ConcurrentHashMap<Locale,String>(40);
	
	@Override
	protected String getValue() throws JspException, IOException {
		PageContext pc = (PageContext) getJspContext();
		ServletContext sc = pc.getServletContext();
		ServletRequest req = pc.getRequest();
		return resolveHelpLang(sc, req);
	}

	public static final String resolveHelpLang(ServletContext sc, ServletRequest req) 
	{
		String helpLang = null;
		Enumeration<?> locales = req.getLocales();
		while (locales.hasMoreElements() && helpLang == null) {
			Locale locale = (Locale) locales.nextElement();
			Boolean hasHelpLang = HAS_HELPLANGS.get(locale);
			
			// Unresolved for this locale - resolve it
			if (hasHelpLang == null) {
				String localeName = locale.getCountry() != "" ? locale.getLanguage() + "_" + locale.getCountry() : locale.getLanguage();
				if (sc.getResourcePaths("/help/doc/" + localeName).size() == 0) {
					if (locale.getCountry() != "") {
						localeName = locale.getLanguage();
						helpLang = (sc.getResourcePaths("/help/doc/" + localeName).size() == 0) ? null : localeName;
					}	
				}
				else {
					helpLang = localeName;
				}
				
				// if sucess
				if (helpLang != null) {
					HAS_HELPLANGS.put(locale, Boolean.TRUE);
					HELPLANGS.put(locale, helpLang);
					// will fall out of loop
				}
				// if fail
				else {
					HAS_HELPLANGS.put(locale, Boolean.FALSE);
				}
			}
			else if (Boolean.TRUE.equals(hasHelpLang)) {
				return HELPLANGS.get(locale);
			}
			// else - continue search
		}
		
		if (helpLang == null) {
			helpLang = "en";
		}
		
		return helpLang;
	}
	
}
