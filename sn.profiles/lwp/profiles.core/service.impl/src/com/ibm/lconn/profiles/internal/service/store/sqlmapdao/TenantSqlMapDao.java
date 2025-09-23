/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

//import com.ibm.lconn.core.appext.util.SNAXDbInfo;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao;


@Repository(TenantDao.REPOSNAME)
public class TenantSqlMapDao extends AbstractSqlMapDao implements TenantDao {

	/**
	 * 
	 */
	public TenantSqlMapDao(){
	}

	public Tenant getTenant(String key){
		Tenant rtnVal = (Tenant)getSqlMapClientTemplate().queryForObject("Tenant.getTenant",key);
		return rtnVal;
	}

	public Tenant getTenantByExid(String exid){
		Tenant rtnVal = (Tenant)getSqlMapClientTemplate().queryForObject("Tenant.getTenantByExid",exid);
		return rtnVal;
	}

	public List<String> getTenantKeyList(){
		List<String> rtnVal = (List<String>) getSqlMapClientTemplate().queryForList("Tenant.getTenantKeyList");
		return rtnVal; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.lconn.profiles.internal.service.store.interfaces.TenantDao#
	 * createTenant(com.ibm.peoplepages.data.Tenant)
	 */
	public String createTenant(Tenant tenant) {
		String rtnVal = null;
		String exid = tenant.getExid();
		// see if the tenant exists - creating a tenant should be rarely called. and we have issues with bvt batch loads.
		Tenant dbTenant = (Tenant)getSqlMapClientTemplate().queryForObject("Tenant.getTenantByExid",exid);
		if (dbTenant != null){
			rtnVal = dbTenant.getTenantKey();
		}
		else{
			try{
				String tenantKey = tenant.getExid();
				//String key = java.util.UUID.randomUUID().toString();
				// as per MT directions, the exid is the shard key that is to propagate to all tables in all components
				// i.e. we will not have distinct 'internal' tenant ids.
				tenant.setTenantKey(tenantKey);
				// lower case name is set when name is set
				getSqlMapClientTemplate().insert("Tenant.createTenant",tenant);
				rtnVal = tenantKey;
			}
			catch (DataAccessException daex){
// leaving in temporarily
//System.out.println(">>>>Tenant.xml DataAccessException");
//System.out.println(">>>>"+daex.getStackTrace());
//System.out.println(">>>>"+daex.getCause());
				boolean isDupeKey = isDuplicateKeyException(daex);
				rtnVal = tenant.getExid();
				if (isDupeKey == false){
					throw daex;
				}
			}
		}
		return rtnVal;
	}
	
	private boolean isDuplicateKeyException(DataAccessException daex){
		boolean rtn = false;
		Throwable t = daex.getCause();
		try{
			if (Class.forName("com.ibm.websphere.ce.cm.DuplicateKeyException").equals(t)) {
//System.out.println(">>>>TenantSqlMapDao.isDuplicateKeyException: true");
	        	rtn = true;
			}
		}
		catch( ClassNotFoundException cnfe ){
    		// we aren't in WAS? just return?
			rtn = true;
//System.out.println(">>>>TenantSqlMapDao.isDuplicateKeyException: got ClassNotFoundException");
		}
//System.out.println(">>>>TenantSqlMapDao.isDuplicateKeyExceptio: return value: "+ rtn);
		return rtn;
	}

	//not allowed
	//public void updateTenantExid(Tenant tenant, String newExid){
	//  // TODO remove this operation
	//  throw new RuntimeException("Not supported, key and exid must be kept in synch");
//		HashMap<String,Object> map = new HashMap<String,Object>(3);
//		map.put("exid", newExid);
//		map.put("key",tenant.getKey());
//		map.put("lastUpdate",tenant.getLastUpdate());
//		getSqlMapClientTemplate().update("Tenant.updateTenantExId",map);
	//}

	public void updateTenantDescriptors(Tenant tenant) {
		getSqlMapClientTemplate().update("Tenant.updateTenantDescriptors", tenant);
	}

	public void deleteTenant(String key) {
		getSqlMapClientTemplate().delete("Tenant.deleteTenant", key);
	}

	public Integer countTenantProfiles(String key){
		Integer rtnVal = (Integer)getSqlMapClientTemplate().queryForObject(
							"Tenant.countProfilesInTenant",key);
		return rtnVal;
	}
}
