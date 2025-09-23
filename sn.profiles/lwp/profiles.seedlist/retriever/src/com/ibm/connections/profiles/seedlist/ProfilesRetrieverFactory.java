/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.connections.profiles.seedlist;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.ilel.seedlist.SeedlistException;
import com.ibm.ilel.seedlist.imp.AbstractRetrieverFactory;
import com.ibm.ilel.seedlist.imp.RetrieverRequestImp;
import com.ibm.ilel.seedlist.retriever.RetrieverRequest;
import com.ibm.ilel.seedlist.retriever.RetrieverService;
import com.ibm.ilel.seedlist.retriever.lconn.LConnRetrieverFactory;

public class ProfilesRetrieverFactory extends AbstractRetrieverFactory
		implements LConnRetrieverFactory {

	private static final String CLASSNAME = ProfilesRetrieverFactory.class.getName();
	private static Logger logger = Logger.getLogger(CLASSNAME);

	public RetrieverService getRetrieverService(Properties prop,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws SeedlistException {
		String method = "getRetrieverService";

		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, method, prop);
		}
		
		boolean isPublic = false;
		ProfilesRetrieverService retVal = new ProfilesRetrieverService(isPublic, prop, servletRequest);

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, method, retVal);
		}

		return retVal;
	}
	
	public RetrieverService getPublicRetrieverService(Properties prop,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws SeedlistException {
		String method = "getPublicRetrieverService";

		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, method, prop);
		}

		boolean isPublic = true;
		ProfilesRetrieverService retVal = new ProfilesRetrieverService(isPublic, prop, servletRequest);

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, method, retVal);
		}

		return retVal;
	}

	public RetrieverRequest createRequest(String seedlistId) throws SeedlistException {
		String method = "createRequest";

		if (logger.isLoggable(Level.FINER)) {
			logger.entering(CLASSNAME, method, seedlistId);
		}

		RetrieverRequestImp retVal = new RetrieverRequestImp(seedlistId);

		if (logger.isLoggable(Level.FINER)) {
			logger.exiting(CLASSNAME, method, retVal);
		}

		return retVal;
	}

	public String getVersion() {
		return "2.5";
	}
}
