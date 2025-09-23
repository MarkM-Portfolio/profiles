/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.internal.service;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.appext.api.SNAXConstants;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfileDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ProfilesConstantsDao;
import com.ibm.lconn.profiles.internal.service.store.interfaces.SchemaVersionDao;
import com.ibm.lconn.profiles.internal.util.SchemaVersionInfo;
import com.ibm.peoplepages.internal.resources.ResourceManager;

/**
 *
 */
public class ProfilesAppServiceImpl extends AbstractProfilesService implements ProfilesAppService 
{
	private static final Logger logger = 
		Logger.getLogger(ProfilesAppServiceImpl.class.getName(),
						 "com.ibm.peoplepages.internal.resources.messages");
	
	private static final ResourceBundleHelper rbh = new ResourceBundleHelper(logger.getResourceBundle());

//	private static final int CHECK_POINT_COUNT = 5000;
//	private static final int RES_PASS = 0;
//	private static final int RES_SUCCESS_CNT = 1;
//	private static final int RES_FAIL_CNT = 2;
	private final TransactionTemplate indexBuilderTT;
	
	@Autowired private ProfilesConstantsDao profileConstantsDao;
	
	@Autowired private SchemaVersionDao schemaVersionDao;
	@Autowired private ProfileDao profileDao;
	
	@Autowired
	public ProfilesAppServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager) {
		super(txManager);
		
		DefaultTransactionDefinition reqNewTd = new DefaultTransactionDefinition();
		reqNewTd.setPropagationBehavior(SNAXConstants.DEBUG_MODE ? TransactionDefinition.PROPAGATION_REQUIRED : TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		reqNewTd.setReadOnly(false);
				
		indexBuilderTT = new TransactionTemplate(txManager, reqNewTd);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfilesAppService#assertSchemaVersion()
	 */
	public void setSchemaVersion() throws DataAccessException {
		SchemaVersionInfo.instance().init();
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public String getAppProp(String key) throws ProfilesRuntimeException {
		return profileConstantsDao.getValue(key);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ProfilesConstantsService#setValue(java.lang.String, java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void setAppProp(String key, String value)
			throws ProfilesRuntimeException 
	{
		String prev = getAppProp(key);
		if (!StringUtils.equals(prev, value)) {
			profileConstantsDao.setValue(key, value);
		}
	}

	public List<String> findDistinctProfileTypeReferences()
	{
		List<String> profileTypes = profileDao.findDistinctProfileTypeReferences();
		return profileTypes;
	}
}
