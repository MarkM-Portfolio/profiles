/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;

/**
 * Data-access bean for DB-based search queries
 * 
 *
 */
@Repository
public interface SearchDao {

	public static final String REPOSNAME = "com.ibm.lconn.profiles.internal.service.store.interfaces.SearchDao";
	
	/**
	 * Find profiles given search criteria
	 * 
	 * @param searchValues
	 * @param options
	 * @return
	 */
	public List<String> findProfileKeys(Map<String,Object> searchValues, ProfileSetRetrievalOptions options);
	
}
