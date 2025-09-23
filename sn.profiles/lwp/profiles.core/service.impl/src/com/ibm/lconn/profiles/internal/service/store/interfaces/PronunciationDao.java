/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.PronunciationRetrievalOptions;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessDeleteException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.DataAccessUpdateException;

/**
 * @author colleen
 */
@Repository(PronunciationDao.REPOSNAME)
public interface PronunciationDao
{
	public static final String REPOSNAME = "com.ibm.peoplepages.internal.service.store.interfaces.PronunciationDao";
	
	Pronunciation getPronunciationWithoutFileByKey(String key) throws DataAccessRetrieveException;

	Pronunciation getPronunciationWithFileByKey(String key) throws DataAccessRetrieveException;

	int updatePronunciation(Pronunciation values) throws DataAccessUpdateException;

	void insertPronunciation(Pronunciation values) throws DataAccessCreateException;

	void deletePronunciationByKey(String key) throws DataAccessDeleteException;

	int countEmployeesWithPronunciation() throws DataAccessRetrieveException;

	List<Pronunciation> getAll(PronunciationRetrievalOptions options);
	
    // special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String key, String newTenantKey);
}
