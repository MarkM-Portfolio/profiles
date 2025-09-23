/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import org.springframework.stereotype.Service;

import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.PronunciationCollection;
import com.ibm.lconn.profiles.data.PronunciationRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

/**
 *
 */
@Service
public interface PronunciationService 
{
	/**
	 * A method for paging over all of the photos in the system. This is meant
	 * for administrative use only.
	 * 
	 * @param options
	 * @return
	 * 
	 * @throws ProfilesRuntimeException
	 */
	public PronunciationCollection getAll(PronunciationRetrievalOptions options);
	
    /**
     * takes a map of field names to values
     * one key value pair must be uid, ${uid}
     * @param pronunciation object
     * @throws DataAccessException
     */
    public void update(Pronunciation pronunciation);

    /**
     * Check for the existance of a Pronunciation
     * 
     * @param key
     * @return
     * @throws DataAccessRetrieveException
     */
	public boolean existByKey(String key) throws DataAccessRetrieveException;
    
	/**
	 * Delete the Pronunciation
	 * @param key
	 * @throws DataAccessDeleteException
	 */
    public void delete(String key) throws DataAccessDeleteException;

    /**
     * Get by key
     * @param key
     * @return
     * @throws DataAccessRetrieveException
     */
    public Pronunciation getByKey(String key) throws DataAccessRetrieveException;
    
    /**
     * For statistics reporting purposes
     * @return
     */
    public int countUsersWith();
}
