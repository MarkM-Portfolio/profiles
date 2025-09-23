/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.connectors.Util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.ibm.di.server.SearchCriteria;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDICriteriaOperator;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

public class TDIConnectorHelper {
	
	/**
	 * convert InputStream to byte[]
	 */
	public static byte[] inputStreamToByteArray(InputStream stream)
	{
		byte[] image = null;
		
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		try
		{
			while((ch = stream.read()) != -1)
				bytestream.write(ch);
			
			image = bytestream.toByteArray();
			bytestream.close();					
		}catch (IOException e){
			e.printStackTrace();
		}
		return image;
	}
	
	public static String inputStreamtoString(InputStream is){
		   BufferedReader in = new BufferedReader(new InputStreamReader(is));
		   StringBuffer buffer = new StringBuffer();
		   String line = "";
		   try {
			while ((line = in.readLine()) != null){
			     buffer.append(line);
			   }
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   return buffer.toString();
		}
	
	public static ProfileDescriptor getEmployeeByCriterial(TDIProfileService tdiProfileSvc, 
			TDIProfileSearchCriteria.TDIProfileAttribute attribute, 
			String value)
	{
		ProfileDescriptor searchForProfileDescriptor = null;
		
		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());
		TDIProfileSearchCriteria searchCriteria = new TDIProfileSearchCriteria();
		searchCriteria.setAttribute(attribute);
		searchCriteria.setOperator(TDICriteriaOperator.EQUALS);
		searchCriteria.setValue(value);
		options.getSearchCriteria().add( searchCriteria);
		TDIProfileCollection profileDescCollection = tdiProfileSvc.getProfileCollection(options);
		
		if((profileDescCollection != null) && (profileDescCollection.getProfiles().size()>0))
			searchForProfileDescriptor = profileDescCollection.getProfiles().get(0);
		
		return searchForProfileDescriptor;
	}
	
	// This method should be used if the criteria can return muliple values
	// note plural - Employees, not Employee - in the method name
	public static TDIProfileCollection getEmployeesByCriterial(TDIProfileService tdiProfileSvc, 
			TDIProfileSearchCriteria.TDIProfileAttribute attribute, 
			String value)
	{
		TDIProfileSearchOptions options = new TDIProfileSearchOptions();
		options.setSearchCriteria(new ArrayList<TDIProfileSearchCriteria>());
		TDIProfileSearchCriteria searchCriteria = new TDIProfileSearchCriteria();
		searchCriteria.setAttribute(attribute);
		searchCriteria.setOperator(TDICriteriaOperator.EQUALS);
		searchCriteria.setValue(value);
		options.getSearchCriteria().add( searchCriteria);
		TDIProfileCollection profileDescCollection = tdiProfileSvc.getProfileCollection(options);
		
		return profileDescCollection;
	}

	public static SearchPair parseSearchScript(SearchCriteria search) throws TDIException
	{
		SearchPair searchPair = new SearchPair();
		String scriptFilter = search.getScriptFilter();
		//parse searchValue and key
		String[] splitScript = scriptFilter.split(" LIKE ");
		if(splitScript.length <=1)
			throw new TDIException("no valid whereClauses in linkCriteria scripts");

		searchPair.set_searchKey(splitScript[0]);
		String[] splitSearchValue = splitScript[1].split("'");
		if(splitSearchValue.length>1)
			searchPair.set_searchValue(splitSearchValue[1]);
		else
			searchPair.set_searchValue(splitSearchValue[0]);
		return searchPair;
	}
	
	public static boolean checkExceptionRecoverable(RuntimeException e)
	{
		boolean recoverable = true;
		if ((e instanceof org.springframework.dao.PermissionDeniedDataAccessException) ||
				(e instanceof org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException) ||
				(e instanceof org.springframework.dao.NonTransientDataAccessResourceException)){
			recoverable = false;
		}
		return recoverable;
	}
}
