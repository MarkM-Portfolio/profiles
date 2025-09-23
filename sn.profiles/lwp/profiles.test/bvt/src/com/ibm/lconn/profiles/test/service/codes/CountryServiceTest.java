/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.internal.service.CountryService;

/*
 *
 */
public class CountryServiceTest extends BaseCodesServiceTest<Country, CountryService> {

	public CountryServiceTest() {
		super(Country.class, CountryService.class);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.test.service.codes.BaseCodesServiceTest#codesToAdd()
	 */
	@Override
	protected List<Country> codesToAdd() {
		List<Country> l = new ArrayList<Country>();
		l.add(newCountry("us", "United States"));
		l.add(newCountry("ca", "Canada"));
		l.add(newCountry("sk", "Slovak Republic"));
		l.add(newCountry("uk", "United Kingdom"));
		l.add(newCountry("jp", "Japan"));
		return l;
	}

	@Override
	protected Country newValue(String codeId, Map<String, ? extends Object> values) {
		return new Country(codeId, values);
	}

	private Country newCountry(String code, String displayValue) {
		return new Country(code, Collections.singletonMap("displayValue", displayValue));		
	}

}
