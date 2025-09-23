/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* (C) Copyright IBM Corp. 2009, 2012                                */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.codes.AbstractCode;

/**
 *
 *
 */
@Repository
public interface BaseCodesDao<CT extends AbstractCode> {
	public CT get(String codeId);
	public List<CT> getAll();
	// used by cache reload. could introduce retrieval options. use cases not clear yet.
	public List<CT> getAllIgnoreTenant();
	public void create(CT code);	
	public void update(CT code);	
	public void delete(String codeId);
}
