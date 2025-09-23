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
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.HashMap;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileExtensionDraftDao;

@Repository(ProfileExtensionDraftDao.REPOSNAME)
public class ProfileExtensionDraftSqlMapDao extends AbstractSqlMapDao implements ProfileExtensionDraftDao {
	public ProfileExtension insertProfileExtension(ProfileExtension values) throws DataAccessCreateException {
		setTenantKeyForC(values);
		return (ProfileExtension) getSqlMapClientTemplate().insert("ProfileExtensionDraft.insertProfileExtensionDraft", values);
	}

	public ProfileExtension getProfileExtensionDraft(HashMap<String, String> map) {
		augmentMapForRUD(map);
		return (ProfileExtension) getSqlMapClientTemplate().queryForObject("ProfileExtensionDraft.SelectProfileExtensionDraftByKeyId", map);
	}
}
