/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.connectors.Util;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.api.tdi.service.impl.ProfilesTDICRUDServiceImpl;
import com.ibm.lconn.profiles.api.tdi.util.DBConnectionsHelper;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

/**
 *
 */
public abstract class AbstractProfilesConnector extends Connector {

	protected ResourceBundleHelper _mlp;
	protected TDIProfileService _tdiProfileSvc;
	
	protected Boolean _mark_manager = false;
	protected Boolean _iterator_return_key_data_only = false;
	protected int _state = 0;

	protected static int STATE_NOCHANGE = 0;
	protected static int STATE_ACTIVATE = 1;
	protected static int STATE_INACTIVATE = 2;
	
	
	/**
	 * 
	 */
	public final Object querySchema(Object obj) throws Exception
	{
		initializeInternal();
		
		return super.querySchema(obj);
	}
	
	
	/**
	 * Consolidated initialize logic.
	 * 
	 * Handles init of TDICRUD service and other objects
	 */
	public final void initialize (Object config) throws Exception
	{
		super.initialize(config);
		
		initializeInternal();
	}
	
	private boolean __init = false;
	protected final void initializeInternal() throws Exception {
		if (!__init) {
		
			// handle headless TDI case
			if("true".equals(DBConnectionsHelper.getHeadlessTDIScripts()))
			{
				System.setProperty("java.awt.headless", "true"); 
			}
			
			// init messages
			if (_mlp == null) {
				_mlp = new ResourceBundleHelper("profiles_messages");
			}
			
			Boolean bParam = getBoolean("update_mark_manager");
			Boolean bParam1 = getBoolean("iterator_return_key_data_only");
			String sParam = getParam("stateName");

		    if (bParam != null) {
				_mark_manager = bParam;
			}

		    if (bParam1 != null) {
				_iterator_return_key_data_only = bParam1;
			}

		    if (sParam != null) {
		      sParam = sParam.trim();
		      if (sParam.length() > 0) {
		    	  if (sParam.equals("li.state_nochange")) {
		    		  _state = STATE_NOCHANGE;
		    	  }
		    	  else if (sParam.equals("li.state_activate")) {
		    		  _state = STATE_ACTIVATE;
		    	  }
		    	  else if (sParam.equals("li.state_inactivate")) {
		    		  _state = STATE_INACTIVATE;
		    	  }
		      }
		    }
	    	
		    // init password
			DBConnectionsHelper.setPassword(
					(String)((ConnectorConfig)getConfiguration()).getMetamergeConfig().getTDIProperties().getProperty("profiles",
							DBConnectionsHelper.PASSWORD));
			
			// init svc
			ProfilesTDICRUDServiceImpl.getInstance();	
			if (_tdiProfileSvc == null) {
				_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			}
		
			__init = true;
		}		
	}
	
	/**
	 * Method sub objects can override to setup environemtn
	 * @param config
	 * @throws Exception
	 */
	protected void initLcConnector(Object config) throws Exception { }
	
}
