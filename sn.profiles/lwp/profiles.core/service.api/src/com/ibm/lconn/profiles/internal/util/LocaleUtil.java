/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2015, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;


import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.lconn.core.web.util.lang.LangUtil;

public class LocaleUtil
{
	private static final Class<LocaleUtil> CLAZZ = LocaleUtil.class;
	private static final String CLASS_NAME = CLAZZ.getSimpleName();
	private static final Log    LOG        = LogFactory.getLog(CLAZZ);

	/**
	 * store all the ISO countries and languages in two sets
	 */
	private static Set<String> countries;
	private static Set<String> languages;

	private static LocaleUtil instance = new LocaleUtil();

	public static LocaleUtil instance()
	{
		return instance;
	}

	private LocaleUtil()
	{
		countries = getISOCountries();
		languages = getISOLanguages();
		if (LOG.isTraceEnabled()) {
			LOG.trace("LocaleUtil loaded: languages( " + languages.size() + ") countries( " + countries.size() + ") ");
		}
	}

	public static Set<String> getCountries() {
		return countries;
	}
	public static Set<String> getLanguages() {
		return languages;
	}

	private static Set<String> getISOCountries()
	{
		String[] isoCountries = Locale.getISOCountries();

		Set<String> countries = new HashSet<String>(isoCountries.length);
		for (int i = 0; i < isoCountries.length; i++) {
			countries.add(isoCountries[i]);
		}
		return countries;
	}

	private static Set<String> getISOLanguages()
	{
		String[] isoLanguages = Locale.getISOLanguages();

		Set<String> languages = new HashSet<String>(isoLanguages.length);
		for (int i = 0; i < isoLanguages.length; i++) {
			languages.add(isoLanguages[i]);
		}
		return languages;
	}

	public static Locale getDefaultLocale()
	{
		Locale locale = Locale.getDefault(); // fall back to server JVM locale
		return locale;
	}

	public static Locale getLocale(String language)
	{
		Locale locale = null;

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + ".getLocale("+language+")");
		}

		// ensure that the language / country parts have correct case 		
		// if 2-part locale [pt_BR, zh_TW etc] - ensure that the separator is _ and NOT -

		if (isValidLanguage(language)) {
			String fixLanguage = LangUtil.normalize(language);
			locale = LocaleUtil.parseAndValidateISOCode(fixLanguage);
		}

		// defensive code. locale should not be null or invalid.
		if (locale == null || !isValidLocale(locale)) {
			locale = getDefaultLocale(); // fall-back - use the server JVM locale
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + " : use default locale " + locale);
			}
		}
		// really, really defensive code. locale should not be null.
		if (locale == null) {
			locale = Locale.ENGLISH; // fall-back - speak loudly and assume everyone speaks English
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + " : use ENGLISH locale " + locale);
			}
		}

		// either way, we need to be coming out of here with a valid supported locale
		String country = locale.getCountry();
		String msg = "LocaleUtil.getLocale requested : '" + language
							+ "', got '" + locale.getLanguage() + (StringUtils.isEmpty(country) ? "" : (" : " + country)) + "'";
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + msg);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " exiting " + "getLocale " + locale);
		}
		return locale;
	}

	/**
	 * Check if the input string is valid locale and if so, return a Locale object.
	 * A valid locale string is of the form langCode or langCode_countryCode 
	 * or langCode_countryCode_Variant, where langCode and countryCode are two-letter
	 * strings. Also, in order to simplify UI code, we allow the separator "-", not just
	 * "_".
	 * 
	 * @param name A string representation of a locale
	 * @return For valid locale string - the locale object. Otherwise, null
	 */
	public static Locale parseAndValidateISOCode(String name)
	{
		Locale retLocale  = null;

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " entering parseAndValidateISOCode(" + name + ")");
		}

		if ((null != name) && (name.length() >1) ) {

			// get the language from the input string
			// if valid language [fr, de, zh  etc] - ensure that the language code has proper case
			String languageCode = name.substring(0, 2).toLowerCase(Locale.ENGLISH);

			if ((null == languages) || languages.isEmpty()) {
				languages = getISOLanguages();
			}

			if (languages.contains(languageCode)) {

				String localeName = null;
				// If the string is longer than 2 then it must include a country code
				if (name.length() > 2) {
					String countryCode = "";

					if (name.charAt(2) == '_' || name.charAt(2) == '-') {
						if (name.contains("-")) {
							name = name.replaceAll("-", "_");
						}

						// if 2-part locale [pt_BR, zh_TW etc] - ensure that the language / country parts have proper case 		
						if (name.length() > 4) {
							if (name.charAt(2) == '_') {
								// split language & country parts; fix the case and re-combine as the locale name
								// case conversion should be done in English - to avoid "i" conversion issues in Turkish
								// language / country code is always in Latin chars, so locale-sensitive conversion is not required
								countryCode = name.substring(3, 5).toUpperCase(Locale.ENGLISH);
								if ((null == countries) || countries.isEmpty()) {
									countries = getISOCountries();
								}
								if (countries.contains(countryCode)) {
									localeName = languageCode + "_" + countryCode;
									retLocale = new Locale(languageCode, countryCode);
								}

								// We disregard the variant's value, if present.
//								if (name.length() > 5) {
//									// The variant must be at least a single character
//									if (name.length() > 6) {
//										if (name.charAt(5) == '_') {
//											// split variant off
//											String variant = name.substring(6, 7).toUpperCase(Locale.ENGLISH);
//											localeName = localeName + "_" + variant;
//											retLocale = new Locale(languageCode, countryCode, variant);
//										}
//									}
//								}
							}
						}
					}
				}
				else {
					// if 1-part locale [fr, de etc] - ensure that the language has proper case
					localeName = languageCode;
					retLocale = new Locale(languageCode);
				}
			}
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " exiting parseAndValidateISOCode " + retLocale);
		}
		return retLocale;
	}

	public static boolean isValidLanguage(String language)
	{
		boolean isValid = false;

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " entering isValidLanguage(" + language + ")");
		}
		if (language != null)
		{
			String fixLanguage = LangUtil.normalize(language);
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "  isValidLanguage : normalized language: [" + fixLanguage + "]");
			}
			String localeName = getLocaleName(fixLanguage);
			isValid = (null != localeName);
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "  isValidLanguage : language: [" + fixLanguage + "] is "
						+ (isValid ? "" : "NOT ") + "valid");
			}
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " exiting isValidLanguage(" + language + ") : " + isValid );
		}
		return isValid;
	}

	public static boolean isValidLocale(Locale locale)
	{
		boolean isValid = false;

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " entering isValidLocale(" + locale + ")");
		}
		if (locale != null) {
			try {
				isValid = ( StringUtils.isNotEmpty(locale.getLanguage())
						&&  LocaleUtils.isAvailableLocale(locale));
			}
			catch (MissingResourceException e) {
				if (LOG.isTraceEnabled()) {
					LOG.trace(CLASS_NAME + "  isValidLocale : locale: [" + locale + "] is "
							+ (isValid ? "" : "NOT ") + "valid");
				}
			}
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + " exiting " +"isValidLocale(" + locale + ") : " + isValid );
		}
		return isValid;
	}

	private static String getLocaleName(String name)
	{
		String localeName = null;

		if ((null != name) && (name.length() >1) ) {

			// get the language from the input string
			// if valid language [fr, de, zh  etc] - ensure that the language code has proper case
			String languageCode = name.substring(0, 2).toLowerCase(Locale.ENGLISH);

			// verify that the language code is a valid ISO code
			if ((null == languages) || languages.isEmpty()) {
				languages = getISOLanguages();
			}
			if (languages.contains(languageCode)) {

				// If the string is longer than 2 then it must include a country code
				if (name.length() > 2) {
					String countryCode = "";

					if (name.contains("-")) {
						name = name.replaceAll("-", "_");
					}
					// if 2-part locale [pt_BR, zh_TW etc] - ensure that the language / country parts have proper case 		
					if (name.length() >4) {
						if (name.charAt(2) == '_') {
							// split language & country parts; fix the case and re-combine as the locale name
							// case conversion should be done in English - to avoid "i" conversion issues in Turkish
							// language / country code is always in Latin chars, so locale-sensitive conversion is not required
							countryCode = name.substring(3, 5).toUpperCase(Locale.ENGLISH);
							// verify that the country code is a valid ISO code
							if ((null == countries) || countries.isEmpty()) {
								countries = getISOCountries();
							}
							if (countries.contains(countryCode)) {
								localeName = languageCode + "_" + countryCode;
							}
						}
					}
				}
				else {
					// if 1-part locale [fr, de etc] - ensure that the language has proper case
					localeName = languageCode;
				}
			}
		}
		return localeName;
	}

	// disabled for now since we cannot trust the sn.infra Locales.ALL_SUPPORTED list to be accurate / up-to-date
//	private static boolean isLocalSupported(Locale reqestLocale)
//	{
//		if (null == lcSupportedLocales)
//			lcSupportedLocales = fixupLocales(Locales.ALL_SUPPORTED);
//
//		boolean isLocalSupported = false;
//	    Iterator<Locale> it = lcSupportedLocales.iterator();
//		boolean found = false;
//		// iterate over all Connections supported Locales to verify that the request locale is supported
//	    while (! found && it.hasNext())
//	    {
//	    	Locale aLocale = (Locale) it.next();
//	    	if (isSameLocale(aLocale, reqestLocale)) {
//	    		found = true;
//	    		isLocalSupported = true;
//	    	}
//		}
//	    return isLocalSupported;
//	}
//
//	private static boolean isSameLocale(Locale aLocale, Locale reqestLocale)
//	{
//		boolean isSameLocale = 
//				(  (aLocale.getLanguage().equalsIgnoreCase(reqestLocale.getLanguage()))
//				&& (aLocale.getCountry().equalsIgnoreCase(reqestLocale.getCountry())));
//		return isSameLocale;
//	}
//
//	private static List<Locale> fixupLocales(List<Locale> localeList)
//	{
//		List<Locale> fixSupported = new ArrayList<Locale>(localeList.size());
//		Iterator<Locale> it = localeList.iterator();
//		// iterate over all Connections supported Locales listing and construct a proper Locale for each language & country
//		while ( it.hasNext() )
//		{
//			Locale oldLocale = (Locale) it.next();
//			// if 2-part locale [pt_BR, zh_TW etc] - ensure that the language / country parts have proper case
//			String localeName = oldLocale.getLanguage();
//			if (localeName.contains("_"))
//			{
//				localeName = getLocaleName(localeName);
//			}
//			Locale newLocale = LocaleUtils.toLocale(localeName);
//			fixSupported.add(newLocale);
//		}
//		LocaleUtil.listLocales();
//
//		return fixSupported;
//	}
//
//	private static ResourceBundle templateBundle = null; 
//
//	public static void listLocales()
//	{
////		testLocales(oldSupportedLocales);
////		List<Locale> newSupportedLocales = fixupLocales(oldSupportedLocales);
////		testLocales(connectionsLocales);
//		Locale[] availableLocales = Locale.getAvailableLocales();
//		String[] connectionsLocaleNames = getLocaleNames(availableLocales);
//		listLocales(connectionsLocaleNames);
//	}
//
//	private static String[] getLocaleNames(Locale[] availableLocales)
//	{
//		List<String> available = new ArrayList<String>(availableLocales.length);
//		// iterate over all Connections supported Locales and extract the language name
//		for (int i = 0; i < availableLocales.length; i++)
//		{
//			Locale availableLocale = availableLocales[i];
//			String localeName = availableLocale.getLanguage();
//			// if 2-part locale [pt_BR, zh_TW etc] - ensure that the language / country parts have proper case
//			if (localeName.contains("_"))
//			{
//				localeName = getLocaleName(localeName);
//			}
//			available.add(localeName);
//		}
//		String[] availableNames = available.toArray(new String[0]);
//		return   availableNames;
//	}
//
//	private static void listLocales(String[] connectionsLocales)
//	{
//		// iterate over all Connections supported Locales listing language & country
//		for (int j = 0; j < connectionsLocales.length; j++)
//		{
//			StringBuilder sb = new StringBuilder();
//			String    locale = connectionsLocales[j];
//			sb.append(locale); 
//			sb.append(" ==>> "); 
//			try {
//				if (j >29) {
//					int k=0; // break point to look at double-barreled locales & invalid ones
//				}
//				Locale aLocale  = LocaleUtils.toLocale(locale);
//				if (null != aLocale) {
//					String language = aLocale.getLanguage();
//					String country  = aLocale.getCountry();
//					sb.append(language);
//					if (StringUtils.isNotEmpty(country)) {
//						sb.append("_");
//						sb.append(country);
//					}
//					templateBundle = ProfilesConfig.instance().getTemplateConfig().getBundle(aLocale);
//					if (null == templateBundle) {
//						sb.append(" Resource bundle was not found for this locale : " + locale);
//					}
//					else {
//						sb.append(" (got bundle) ");
//						String labelName = "custom3";
//						String useValue  = null;
//						String key = "label." + labelName;
//						try { 
//							useValue = templateBundle.getString(key);
//							sb.append(" got : " + useValue);
//						}
//						catch (MissingResourceException e) {
//							sb.append(" key " + key + " was not found in bundle");
//						}
//					}
//				}
//				else {
//					sb.append(" Locale was not found for : " + locale);
//				}
//			}
//			catch (Exception e) {
//				sb.append(" is invalid " + e.getMessage());
//			}
//			int i = j+1;
//			System.out.println("Locale [" + i + "] " + sb.toString());
//		}
//	}

}
