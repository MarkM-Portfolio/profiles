/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.messageboard.adapters;

import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProviderConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorServiceConfig;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.util.LCModelHelper;
import com.ibm.lconn.core.web.atom.util.LCRequestUtils;

/**
 *
 *
 */
public abstract class ProfilesBaseCollectionAdapter {
	
	public static final String MODEL_VECTOR_TYPE = "vectorType";
	
	protected static final String[] EMPTY_STRING_ARRAY = {};
	
	protected final MessageVectorService service;
	protected final MessageVectorServiceConfig config;
	protected final MessageVectorProviderConfig providerConfig;
	
	protected ProfilesBaseCollectionAdapter(
			MessageVectorService service,
			MessageVectorServiceConfig config,
			MessageVectorProviderConfig providerConfig) 
	{
		this.service = service;
		this.config = config;
		this.providerConfig = providerConfig;
	}
	
	protected int getPage(LCRequestContext request) {
		return LCRequestUtils.getPage(request);
	}
	
	protected int getPageSize(LCRequestContext request) {
		return LCRequestUtils.getPageSize(request, providerConfig.getDefaultPageSize());
	}
	
	/**
	 * Utility method to get MV config
	 * - TODO: error checking 
	 * 
	 * @param request
	 * @return
	 */
	protected MessageVectorConfig getMVConfig(LCRequestContext request) 
	{
		String vectorType = (String) LCModelHelper.getModelAttribute(request, MODEL_VECTOR_TYPE);
		return config.getMessageVectorConfigs().get(vectorType);
	}
}
