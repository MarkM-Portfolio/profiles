/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.web.rpfilter;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 * Caches resource 'indefininately' so long as it contains a valid "lastMod=XXX"
 * parameter. This parameter is used to set the last modified time for the
 * resource.
 * 
 * @author mahern
 * 
 */
public final class RPFilterIndefiniteRSIVD implements RPFilterResponseSetter, RPFilterInvalidator 
{
	private String lastModParameter;
	private int time2live;
	
	public void init(Properties config) 
	{
		lastModParameter = config.getProperty("lastModParameter");
		
		try { 
			time2live = Integer.parseInt(config.getProperty("time2live","36000"));
		} catch (NumberFormatException e) {
			time2live = 36000;
			throw new IllegalArgumentException(RPFilterUtil.getString("info.RPFilter.indef.invalidTime2Live",new Object[]{config.getProperty("time2Live")}));
		}
	}

	public void setResponseHeaders(HttpServletRequest request, RPFilterResponse response, String resource) 
	{
		try 
		{
			long lastMod = Long.parseLong(request.getParameter(lastModParameter));
			
			if (lastMod >= 0)
			{
				response.setLastModified(lastMod);
			}
			
			RPFilterCacheControl rpfcc = response.getCacheControl();
			rpfcc.setMaxAge(time2live);
			rpfcc.setProxyMaxAge(time2live);
			rpfcc.setPublic(true);
			
			response.applyCacheControl();
			
		}
		catch (NumberFormatException e)
		{
			// ignore
		}
	}

	public boolean isValid(HttpServletRequest request, String resource, long lastModifiedTime) 
	{
		try 
		{
			long lastModParam = Long.parseLong(request.getParameter(lastModParameter));
			return (lastModParam >= 0);
		}
		catch (NumberFormatException e)
		{
			// ignore
		}
		
		return false;
	}

}
