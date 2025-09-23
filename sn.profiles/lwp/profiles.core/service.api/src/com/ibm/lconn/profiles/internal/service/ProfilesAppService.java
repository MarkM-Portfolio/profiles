/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

/**
 * General service for containing general system administration services
 */
@Service(ProfilesAppService.SVCNAME)
public interface ProfilesAppService {
	public static final String SVCNAME = "com.ibm.lconn.profiles.internal.service.ProfilesAppService";
	public static final String STATE_INCOMPLETE = "incomplete";
	public static final String STATE_COMPLETE = "complete";
	
	public enum TaskRunStatus {
		/**
		 * There is a process currently rebuilding the index
		 */
		RUNNING,
		/**
		 * A rebuild is required and a process has been started
		 */
		STARTED,
		/**
		 * No index rebuild is required
		 */
		NOTREQUIRED
	}
		
	/**
     * Checks that the schema matches the expected schema version
     * 
     * @throws DataAccessException
     */
    public void setSchemaVersion();

	/**
	 * Gets an application property
	 * @param key
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	public String getAppProp(String key) throws ProfilesRuntimeException;
	
	/**
	 * Sets an application property
	 * @param key
	 * @param value
	 * @throws ProfilesRuntimeException
	 */
	public void setAppProp(String key, String value) throws ProfilesRuntimeException;

    /**
     * Enumerate the set of distinct profile type references in the Profiles DB.
     * @return
     */
    public List<String> findDistinctProfileTypeReferences();
    
}
