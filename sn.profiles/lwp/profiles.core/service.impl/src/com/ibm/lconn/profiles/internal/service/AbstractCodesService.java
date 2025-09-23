/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.internal.service.store.interfaces.BaseCodesDao;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;

import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 *
 *
 */
public class AbstractCodesService<CT extends AbstractCode, CDao extends BaseCodesDao<CT>> extends AbstractProfilesService implements BaseCodesService<CT>, InitializingBean {

	private static final Logger logger = Logger.getLogger(AbstractCodesService.class.getName());
	private static final String cacheClsname = AbstractCodesService.class.getName() + ".CodeCache";
	
	protected class CodeCacheImpl implements CodeCache<CT> {	
		private boolean enabled = ProfilesConfig.instance().getCacheConfig().getProfileObjectCache().isEnabled();
		private boolean init = false;
		private HashMap<String,HashMap<String,CT>> cacheSet = null;
		
		public void disable() {
			if (enabled) {
				enabled = false;
				init = false;
				cacheSet = null;
			}
		}

		public void enable() {
			if (!enabled) {
				enabled = true;
				forceReload();
			}
		}

		public CT get(String codeId){
			AppContextAccess.Context ctx = AppContextAccess.getContext();
			if (enabled && init && cacheSet != null){
				Map<String,CT> map = cacheSet.get(ctx.getTenantKey());
				if (map != null){
					return map.get(codeId);
				}
				else{
					return null;
				}
			}
			return null;
		}

		public CT get(String codeId, String tenantKey){
		        CT retval = null;
			final boolean FINER = logger.isLoggable(Level.FINER);

			if (FINER) logger.finer("AbstractCodesService, getting code for codeId = " +codeId +", tenantKey = " +tenantKey);

			if (enabled && init && cacheSet != null){
			    Map<String,CT> map = cacheSet.get( tenantKey );
			    
			    if (map != null){
				if (FINER) logger.finer("AbstractCodesService, found tenant in cacheSet...");
				retval = map.get(codeId);
			    }
			}
			
			return retval;
		}

		public void reload() {
			if (enabled) {
				forceReload();
			}
		}
		
		@SuppressWarnings("synthetic-access")
		private void forceReload() {
			// we eventually need an interface to load per tenant.
			final String METHOD = "forceReload";
			final boolean FINER = logger.isLoggable(Level.FINER);
			
			try {
				if (FINER) logger.entering(cacheClsname, METHOD);

				HashMap<String,HashMap<String,CT>> cacheSet = new HashMap<String,HashMap<String,CT>>();
				List<CT> allCodes = getAllIgnoreTenant();
				for (CT c : allCodes){
					HashMap<String,CT> map = cacheSet.get(c.getTenantKey());
					if (map == null){
						map = new HashMap<String,CT>();
					}
					if (FINER) logger.finer("forceReoad adding "+c);
					map.put(c.getCodeId(),c);
					cacheSet.put(c.getTenantKey(),map);
				}
				this.cacheSet = cacheSet;
				this.init = true;
				
				if (FINER) logger.exiting(cacheClsname, METHOD);
			}
			catch (Exception e) {
				if (FINER) {
					logger.throwing(cacheClsname, METHOD, e);
				}
				// log and continue
				logger.log(Level.WARNING, "Threw exception while initiailizing DB object cache", e);
			}
			finally{
				//TODO reset context tenantKey = currentTK;
			}
		}

		public boolean isEnabled() {
			return enabled;
		}

		public boolean isInit() {
			return init;
		}
		
	}
	
	protected final CodeCache<CT> codeCache;
	protected final CDao codesDao;
	protected final Class<CT> codeType;	
	
	/**
	 * @param transactionTemplate
	 * @param codesDao
	 */
	public AbstractCodesService(TransactionTemplate transactionTemplate, CDao codesDao, Class<CT> codeType) {
		super(transactionTemplate);
		this.codesDao = codesDao;
		this.codeType = codeType;
		this.codeCache = new CodeCacheImpl();
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#create(com.ibm.lconn.profiles.data.AbstractCode)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void create(CT code) {
		assertCurrentUserAdmin();
		codesDao.create(code);
		codeCache.reload();

		// Hookup with the event logging. Added since 3.0
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		EventLogEntry eventLogEntry = EventLogHelper.createAdminEventLogEntry(EventLogEntry.Event.PROFILE_CODE_CREATED );
		eventLogEntry.setProperty("codeId", code.getCodeId());
		eventLogEntry.setProperty("codeMap", code.toString());
		eventLogEntry.setProperty("codeType", code.getRecordType());
		// call to eventLogSvc.insert will set sysEvent value based on AppContext
		eventLogSvc.insert( eventLogEntry );
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#delete(java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void delete(String codeId) {
		assertCurrentUserAdmin();
		codesDao.delete(codeId);
		codeCache.reload();

		// Hookup with the event logging. Added since 3.0
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		EventLogEntry eventLogEntry = EventLogHelper.createAdminEventLogEntry(EventLogEntry.Event.PROFILE_CODE_DELETED );
		eventLogEntry.setProperty("codeId", codeId);
		eventLogSvc.insert( eventLogEntry );
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#get(java.lang.String)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public CT getById(String codeId) {
		if (codeCache.isEnabled() && codeCache.isInit())
			return codeCache.get(codeId);
		
		return codesDao.get(codeId);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#get(java.lang.String,java.lang.String)
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public CT getById(String codeId, String tenantKey) {

	    if (codeCache.isEnabled() && codeCache.isInit()) {
		return codeCache.get(codeId, tenantKey);
	    }
	    return codesDao.get(codeId);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#getAll()
	 */
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<CT> getAll() {		
		return codesDao.getAll();
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<CT> getAllIgnoreTenant() {		
		return codesDao.getAllIgnoreTenant();
	}
	
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#update(com.ibm.lconn.profiles.data.AbstractCode)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void update(CT code) {
		assertCurrentUserAdmin();
		codesDao.update(code);
		codeCache.reload();

		// Hookup with the event logging. Added since 3.0
		EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
		EventLogEntry eventLogEntry = EventLogHelper.createAdminEventLogEntry(EventLogEntry.Event.PROFILE_CODE_UPDATED );
		eventLogEntry.setProperty("codeId", code.getCodeId());
		eventLogEntry.setProperty("codeMap", code.toString());
		eventLogEntry.setProperty("codeType", code.getRecordType());

		eventLogSvc.insert( eventLogEntry );
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#codeType()
	 */
	public Class<CT> codeType() {
		return codeType;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.BaseCodesService#codeCache()
	 */
	public CodeCache<CT> codeCache() {
		return codeCache;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		codeCache.reload();
	}

}
