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

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.PronunciationRetrievalOptions;
import com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao;

@Repository(PronunciationDao.REPOSNAME)
public class PronunciationSqlMapDao extends AbstractSqlMapDao implements PronunciationDao
{
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#getAll(com.ibm.lconn.profiles.data.PronunciationRetrievalOptions)
	 */
	@SuppressWarnings("unchecked")
	public List<Pronunciation> getAll(PronunciationRetrievalOptions options) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("options",options);
		return getSqlMapClientTemplate().queryForList("Pronunciation.getAll",m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#getPronunciationWithoutFileByKey(java.lang.String)
	 */
	public Pronunciation getPronunciationWithoutFileByKey(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		return (Pronunciation) getSqlMapClientTemplate().queryForObject("Pronunciation.GetPronunciationWithoutFileByKey", m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#getPronunciationWithFileByKey(java.lang.String)
	 */
	public Pronunciation getPronunciationWithFileByKey(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		return (Pronunciation) getSqlMapClientTemplate().queryForObject("Pronunciation.GetPronunciationWithFileByKey", m);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#updatePronunciation(com.ibm.lconn.profiles.data.Pronunciation)
	 */
	public int updatePronunciation(Pronunciation values) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("pronunciation", values);
		return getSqlMapClientTemplate().update("Pronunciation.updatePronunciation", m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#insertPronunciation(com.ibm.lconn.profiles.data.Pronunciation)
	 */
	public void insertPronunciation(Pronunciation values) {
		values.setUpdated(SNAXConstants.TX_TIMESTAMP.get());
		setTenantKeyForC(values);
		getSqlMapClientTemplate().insert("Pronunciation.insertPronunciation", values);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#deletePronunciationByKey(java.lang.String)
	 */
	public void deletePronunciationByKey(String key) {
		Map<String,Object> m = getMapForRUD(1);
		m.put("key",key);
		getSqlMapClientTemplate().delete("Pronunciation.deletePronunciationByKey", m);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.PronunciationDao#countEmployeesWithPronunciation()
	 */
	public int countEmployeesWithPronunciation() {
		Map<String,Object> m = getMapForRUD(0);
		return (Integer) getSqlMapClientTemplate().queryForObject("Pronunciation.countEmployeesWithPronunciation", m);
	}
	
    // special method used to switch user tenant key. probably obsolete in visitor model
	public void setTenantKey(String profileKey, String newTenantKey){
		Map<String,Object> m = getMapForRUD(2);
		m.put("key",profileKey);
		m.put("newTenantKey",newTenantKey);
		getSqlMapClientTemplate().update("Pronunciation.updateTenantKey",m);
	}
}
