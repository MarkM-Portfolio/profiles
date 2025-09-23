/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.interfaces;

import org.springframework.stereotype.Repository;
import com.ibm.lconn.profiles.internal.util.SchemaVersionInfo;

/**
 *
 */
@Repository(SchemaVersionDao.REPOSNAME)
public interface SchemaVersionDao {
	public static final String REPOSNAME = "com.ibm.peoplepages.internal.service.store.interfaces.SchemaVersionDao";
	
	public void setSchemaVersion(SchemaVersionInfo toupdate);
}
