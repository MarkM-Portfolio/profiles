/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service.store.sqlmapdao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.data.AbstractDataObject;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * Marker interface to extend
 */
public abstract class AbstractSqlMapDao extends SqlMapClientDaoSupport {

	private final static String CLASS_NAME = AbstractSqlMapDao.class.getName();
	private static Logger logger = Logger.getLogger(CLASS_NAME,
			ResourceManager.BUNDLE_NAME);

	public final static String ORACLE = "oracle";
	public final static String DERBY = "derby";
	public final static String DB2 = "db2";
	public final static String SQLSERVER = "sqlserver";
	public final static String AS400 = "as400";
	private final static String MICROSOFT = "microsoft";
	
	private static String applyMT = "true";
	private static boolean isApplyMT = true;
	static{
		boolean val = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_APPLY_TENANT_CONSTRAINT);
		if (val == false){
			applyMT = "false";
			isApplyMT = false;
		}
	}

	private static String dbVendor = null;

	//private static Boolean applyMTConstraint = PropertiesConfig.getBoolean(ConfigProperty.PROFILE_APPLY_TENANT_CONSTRAINT);

	public String getDbVendor() {
		if (logger.isLoggable(FINER))
			logger.entering(CLASS_NAME, "getDbVendor");

		if (dbVendor == null) {
			try {
				Connection conn = getSqlMapClientTemplate().getDataSource().getConnection();

				DatabaseMetaData meta = conn.getMetaData();
				String driverName = meta.getDriverName();
				String productName = meta.getDatabaseProductName();
				String productVersion = meta.getDatabaseProductVersion();
				String driverVersion = meta.getDriverVersion();
				String url = meta.getURL();

				if (logger.isLoggable(FINER)) {
					logger.logp(FINER, CLASS_NAME, "getDbVendor",
							"This is the driver name used to run jdbc driverName: "
									+ driverName);
					logger.logp(FINER, CLASS_NAME, "getDbVendor",
							"This is the productName name used to run jdbc productName: "
									+ productName);
					logger.logp(FINER, CLASS_NAME, "getDbVendor",
							"This is the productVersion name used to run jdbc productVersion: "
									+ productVersion);
					logger.logp(FINER, CLASS_NAME, "getDbVendor",
							"This is the driverVersion name used to run jdbc driverVersion: "
									+ driverVersion);
					logger.logp(FINER, CLASS_NAME, "getDbVendor",
									"This is the url name used to run jdbc url: "
											+ url);
				}
				// align check more with lc.appext/core/api/com.ibm.lconn.core.appext.util.SNAXDbInfo
				// logic here is a bit less restrictive with startsWith vs. contains.
				driverName = driverName.toLowerCase();
				productName = productName.toLowerCase();
				if (driverName.contains(DERBY)) {
					dbVendor = DERBY;
				}
				else if ((driverName.contains(DB2) || productName.contains(DB2))){
					if (driverName.contains("as/400")){
						dbVendor = AS400;
					}
					else{
						dbVendor = DB2;
					}
				}
				else if (driverName.contains(ORACLE) || productName.contains(ORACLE)){
					dbVendor = ORACLE;
				}
				else if (driverName.contains(SQLSERVER) || driverName.contains(MICROSOFT) || productName.contains(MICROSOFT)){
					dbVendor = SQLSERVER;
				}
				//add by IBM i team -- begin
				else if(driverName.toLowerCase().contains("as/400")) {
					dbVendor = AS400;
				}
				//add by IBM i team -- end
				conn.close();
			}
			catch (Exception e) {
				logger.logp(SEVERE, CLASS_NAME, "getDbVendor", "error.dbvendor", e);
				throw new ProfilesRuntimeException(e);
			}
		}
		if (logger.isLoggable(FINER))
			logger.exiting(CLASS_NAME, "getDbVendor", dbVendor);

		return dbVendor;
	}

	protected void setTenantKeyForC(AbstractDataObject o){
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		String tk = ctx.getTenantKey();
		if (Tenant.SINGLETENANT_KEY.equals(tk)) tk = Tenant.DB_SINGLETENANT_KEY;
		// if this is not the global admin, we must use the app context.
		if (ctx.isAdmin() == false){
			o.setDbTenantKey(tk);
		}
		else{
			// we'll allow admin to set the tenant key on the object/map
			if ( StringUtils.isEmpty(o.getTenantKey())){
				o.setDbTenantKey(tk);
			}
			else{
				tk = o.getTenantKey();
				if (Tenant.SINGLETENANT_KEY.equals(tk)){
					tk = Tenant.DB_SINGLETENANT_KEY;
				}
				o.setDbTenantKey(tk);
			}
		}
		// cannot insert 'ignore' tenant. if we drop FKs, do we check if tenant key exists
		AssertionUtils.assertTrue((StringUtils.equals(tk,Tenant.IGNORE_TENANT_KEY)==false));
		AssertionUtils.assertTrue((StringUtils.equals(tk,Tenant.SINGLETENANT_KEY)==false));
	}

	protected void setTenantKeyForC(Map<String, Object> m) {
		AppContextAccess.Context ctx = AppContextAccess.getContext();
		String tk = ctx.getTenantKey();
		if (Tenant.SINGLETENANT_KEY.equals(tk)){
			tk = Tenant.DB_SINGLETENANT_KEY;
		}
		// if this is not the global admin, we must use the app context.
		if (ctx.isAdmin() == false){
			m.put("dbTenantKey", tk);
		}
		else{
			// we'll allow admin to set the tenant key on the object/map
			if (StringUtils.isEmpty((String)m.get("tenantKey"))){
				m.put("dbTenantKey", tk);
			}
			else{
				tk = (String)m.get("tenantKey");
				if (Tenant.SINGLETENANT_KEY.equals(tk)){
					tk = Tenant.DB_SINGLETENANT_KEY;
				}
				m.put("dbTenantKey", tk);
			}
		}
		// cannot insert 'ignore' tenant. if we drop FKs, do we check if tenant key exists
		AssertionUtils.assertTrue((StringUtils.equals(tk,Tenant.IGNORE_TENANT_KEY)==false));
		AssertionUtils.assertTrue((StringUtils.equals(tk,Tenant.SINGLETENANT_KEY)==false));
	}

	protected Map<String,Object> getMapForC(int size) {
		HashMap<String,Object> m = new HashMap<String,Object>(size+1);
		setTenantKeyForC(m);
		return m;
	}

	protected Map<String,Object> getMapForRUD(int size){
		HashMap<String,Object> m = new HashMap<String,Object>(size+2);
		String tenantKey = getTenantKeyForRUD();
		if (Tenant.SINGLETENANT_KEY.equals(tenantKey))
			tenantKey = Tenant.DB_SINGLETENANT_KEY;
		m.put("dbTenantKey",tenantKey);       
		if (isApplyMT && !Tenant.IGNORE_TENANT_KEY.equals(tenantKey))
			m.put("applyMT",applyMT);
		if (logger.isLoggable(FINEST))
			logger.logp(FINEST, CLASS_NAME, "getMapForR", getMapAsString(m));
		return m;
	}

	@SuppressWarnings("unchecked")
	protected void augmentMapForRUD(Map m){
		String tenantKey = getTenantKeyForRUD();
		if (Tenant.SINGLETENANT_KEY.equals(tenantKey))
			tenantKey = Tenant.DB_SINGLETENANT_KEY;
		m.put("dbTenantKey",tenantKey);		
		if (isApplyMT && !Tenant.IGNORE_TENANT_KEY.equals(tenantKey))
			m.put("applyMT",applyMT);
	}
	
	// special method for employee search where we use a special object in place of a map to send in search values
	@SuppressWarnings("unchecked")
	protected void augmentObjectForRUD(EmployeeSearchObject o){
		String tenantKey = getTenantKeyForRUD();
		if (Tenant.SINGLETENANT_KEY.equals(tenantKey)) tenantKey = Tenant.DB_SINGLETENANT_KEY;
		o.setDbTenantKey(tenantKey);		
		if (isApplyMT && !Tenant.IGNORE_TENANT_KEY.equals(tenantKey)) o.setApplyMT(applyMT);
	}
	
	// method is private as concrete classes retrieve maps that have both a tenantKey
	// as well as an indicator to apply the tenant constraint (applyMT). if a concrete
	// class only sets a tenantKey, it may miss the applyMT setting.
	private String getTenantKeyForRUD(){
		String tenantKey = AppContextAccess.getContext().getTenantKey();
		if (logger.isLoggable(FINEST))
			logger.logp(FINEST, CLASS_NAME, "getTenantKeyForR",
					"The TenantKey is : " + tenantKey);
		AssertionUtils.assertNotNull(tenantKey);
		return tenantKey;
	}

	private String getMapAsString(HashMap<String, Object> map) {
		StringBuilder sb = new StringBuilder("The map contains : " + map.size() + " items\n");
		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Entry<String, Object> pairs = (Map.Entry<String, Object>)it.next();
			String   key = (String) pairs.getKey();
			Object value = pairs.getValue();
			String   val = (String) value.toString();
			if (i >0)
				sb.append("\n");
			sb.append("[" + i + "] " + key + " : " + val);
			i++;
		}
		return sb.toString();
	}

}