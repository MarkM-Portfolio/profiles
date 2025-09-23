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
package com.ibm.lconn.profiles.config.ui.rules;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Joseph Lu
 */
public class LangDisplayRule implements BaseDisplayRule
{

    public LangDisplayRule() {}

    /**
     *  Extension class should overwrite this method
     *
     */
    public boolean checkRule(HttpServletRequest request, Map<String,String> attrs) {
	boolean retval = true;
	String supportedLocales = attrs.get("supportedLocales");

	if ( supportedLocales != null ) {
	    String currentLang = request.getLocale().getLanguage();
	    String currentLangCountry = request.getLocale().getCountry();
	    String fullName = currentLang;
	    
	    supportedLocales = supportedLocales.toLowerCase();
	    if ( currentLangCountry != null && currentLangCountry.length() > 0 ) 
		fullName = currentLang +"_" +currentLangCountry;

	    if ( (supportedLocales.indexOf( currentLang.toLowerCase() ) == -1) && 
		 (supportedLocales.indexOf( fullName.toLowerCase() ) == -1 ) ) {
		retval = false;
	    }
	}

	return retval;
    }
}
