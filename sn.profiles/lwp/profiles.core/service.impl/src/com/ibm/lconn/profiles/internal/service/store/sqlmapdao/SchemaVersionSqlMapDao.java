/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.HashMap;
import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.internal.service.store.interfaces.SchemaVersionDao;
import com.ibm.lconn.profiles.internal.util.SchemaVersionInfo;

/**
 * 
 */
@Repository(SchemaVersionDao.REPOSNAME)
public class SchemaVersionSqlMapDao extends AbstractSqlMapDao implements SchemaVersionDao {

	private static final String APP_NAME = "Profiles";
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.SchemaVersionDao#getSchemaVersion()
	 */
	@SuppressWarnings("unchecked")
	public void setSchemaVersion(SchemaVersionInfo input)	{
		HashMap<String,Object> values;
		values = (HashMap<String,Object>) getSqlMapClientTemplate().queryForObject("Schema.readSchemaVersion", APP_NAME);
		Integer schemaVer = (Integer)values.get("dbSchemaVer");
		String  postVer = (String)values.get("postSchemaVer");
		input.setSchemaVersions(schemaVer,postVer);
	}
//	/* (non-Javadoc)
//	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.SchemaVersionDao#getSchemaVersion()
//	 */
//	public int getSchemaVersion()
//	{
//		return (Integer) getSqlMapClientTemplate().queryForObject("Schema.SelectSchemaVersion", APP_NAME);
//	}
}
