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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.appext.util.SNAXDbInfo;
import com.ibm.lconn.core.appext.util.ibatis.PagingInfo;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

@Repository(ProfileDraftDao.REPOSNAME)
public class ProfileDraftSqlMapDao extends AbstractSqlMapDao implements ProfileDraftDao
{
	@Autowired private SNAXDbInfo dbInfo;
    private final TransactionTemplate deleteTxTemp;
	
	@Autowired
	public ProfileDraftSqlMapDao(@SNAXTransactionManager PlatformTransactionManager txManager) 
	{
		DefaultTransactionDefinition reqNewTd = new DefaultTransactionDefinition();
		reqNewTd.setPropagationBehavior(SNAXConstants.DEBUG_MODE ? TransactionDefinition.PROPAGATION_REQUIRED : TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		deleteTxTemp = new TransactionTemplate(txManager, reqNewTd);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.sqlmapdao.ProfileDraftDao#purgeTable(java.util.Date)
	 */
	public void purgeTable(Date olderThan){
		final Map<String,Object> map = getMapForRUD(3);
		map.put("endDate", olderThan);
		
		int purgeBatchSize = PropertiesConfig.getInt(ConfigProperty.EVENT_LOG_PURGE_BATCH_SIZE);
		PagingInfo pagingInfo = new PagingInfo(dbInfo.getDbType(), purgeBatchSize);
		map.put("pagingInfo", pagingInfo);
		
		int lastPurge = 0;
		do {
			lastPurge = (Integer) deleteTxTemp.execute(new TransactionCallback(){
				public Object doInTransaction(TransactionStatus arg0) {
					return getSqlMapClientTemplate().delete("ProfileDraft.purgeDraftTable", map);
				}				
			});
		} while (lastPurge > 0);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.store.sqlmapdao.ProfileDraftDao#recordDraftValues(java.util.Map)
	 */
	public void recordDraftValues(Map<String, Object> draftValues) {
		setTenantKeyForC(draftValues);
		getSqlMapClientTemplate().insert("ProfileDraft.insertDraftEmployee", draftValues);
	}
}
