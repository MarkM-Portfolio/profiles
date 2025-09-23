/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import java.util.Set;

/**
 * A wrapper class for setting query parameters on a search profiles by criteria match request.
 * 
 */
public class SearchByCriteriaMatchParameters extends AbstractParameters
{

	public SearchByCriteriaMatchParameters()
	{
		super();
		setActiveUsersOnly(true);
		setFormat(Format.LITE);
		setPage(1);
		setPageSize(10);
	}

	public void setActiveUsersOnly(boolean activeUsersOnly)
	{
		put("activeUsersOnly", activeUsersOnly ? "true" : "false");
	}

	public void setCity(String value)
	{
		put("city", value);
	}

	public void setCountry(String value)
	{
		put("country", value);
	}

	public void setEmail(String value)
	{
		put("email", value);
	}

	public void setFormat(Format value)
	{
		put("format", value.getValue());
	}

	public void setJobTitle(String value)
	{
		put("jobTitle", value);
	}

	public void setName(String value)
	{
		put("name", value);
	}

	public void setOrganization(String value)
	{
		put("organization", value);
	}

	public void setPage(int page)
	{
		put("page", "" + page);
	}

	public void setPhoneNumber(String value)
	{
		put("phoneNumber", value);
	}

	public void setProfileTags(Set<String> tags)
	{
		put("profileTags", delimit(",", tags));
	}

	public void setProfileType(String value)
	{
		put("profileType", value);
	}

	public void setPageSize(int value)
	{
		put("ps", "" + value);
	}

	public void setSearch(String value)
	{
		put("search", value);
	}

	public void setState(String value)
	{
		put("state", value);
	}

	public void setMode(String value) {
		put("mode", value);
	}

	public void setUserId(String value) {
		put("userid", value);
	}

}
