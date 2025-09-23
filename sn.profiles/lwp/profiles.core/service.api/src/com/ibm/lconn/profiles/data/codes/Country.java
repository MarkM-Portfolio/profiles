/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.data.codes;

import java.util.Map;

import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;





public class Country extends AbstractCode<Country>
{
	private static final long serialVersionUID = -7724290433924382185L;

	public static final String TABLENAME = "Country";
	
	public static final CodeField F_COUNTRY_CODE = new CodeField(Country.class, "countryCode", String.class, true);
	public static final CodeField F_DISPLAY_VALUE = new CodeField(Country.class, "displayValue", String.class);

	static {
		AbstractCode.finalizeFieldList(Country.class);
		putNameToCodeMap(TABLENAME,Country.class);
	}

	public Country(String codeId, Map<String, ? extends Object> values) {
		super(codeId, values, "country", PeoplePagesServiceConstants.CCODE);
	}

	public Country(String codeId, String tenantKey, Map<String, ? extends Object> values) {
		super(codeId, tenantKey, values, "country", PeoplePagesServiceConstants.CCODE);
	}
	
	public String getRecordSummary() {
		return "Information for country code " + getCodeId();
	}
	
	public String getRecordTitle() {
		return getDisplayValue();
	}
	
	public String getCountryCode() {
		return getCodeId();
	}

	public String getDisplayValue() {
		return getFieldValue(F_DISPLAY_VALUE);
	}

	// used to make sure this, i.e., the Country, class is loaded so that the static 
	// initializer has registered the service context vai AbstractCode.putNameToCodeMap(...)
	public static String makeSureServiceContextIsRegistered() {
		return null;
	}

}
