/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.connectors.Util;


import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;

public class TDICodeRunner {
	
	private static final ResourceBundleHelper BUNDLE = new ResourceBundleHelper("profiles_messages");
	
	/**
	 * Utility method to handle boiler plate aspects of code checking
	 * @param <T>
	 * @param block
	 * @return
	 * @throws TDIException
	 */
	public static <T> T run(final TDICodeBlock<T> block) throws TDIException {
		try {
			return block.run();
		}catch (TDICodeBlockException e)
		{
			return block.handleTDICodeBlockException(e);
		}
		catch (RuntimeException e) {
			if (isRecoverableException(e)) {
				return block.handleRecoverable(e);
			} 
			else {
				String msg = BUNDLE.getString("err_unrecoverable");
				block.getLogger().fatal(msg);
				throw e;
			}
		}
		
	}
	
	public static boolean isRecoverableException(RuntimeException e) {
		if ((e instanceof org.springframework.dao.PermissionDeniedDataAccessException) ||
				(e instanceof org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException) ||
				(e instanceof org.springframework.dao.NonTransientDataAccessResourceException)){
			return false;
		}
		
		return true;
	}

}
