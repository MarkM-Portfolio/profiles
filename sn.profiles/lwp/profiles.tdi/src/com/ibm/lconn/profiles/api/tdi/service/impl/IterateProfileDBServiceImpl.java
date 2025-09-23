/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.tdi.service.impl;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.api.tdi.data.ProfileEntry;
import com.ibm.lconn.profiles.api.tdi.service.IterateProfileDBService;
import com.ibm.lconn.profiles.api.tdi.util.MessageLookup;
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;

/**
 * 
 * @author Liang Chen <mailto="chlcc@cn.ibm.com">
 *
 */
public class IterateProfileDBServiceImpl extends TDIWrapperSvc implements IterateProfileDBService{
	String classname = IterateProfileDBServiceImpl.class.getName();
//	Logger logger = Logger.getLogger(classname);
	private static final Log LOG = LogFactory.getLog(ProfilesTDICRUDServiceImpl.class);
	
	private int _index;
	private TDIProfileService _tdiProfileSvc;
	private TDIProfileSearchOptions _searchOptions;
	private TDIProfileCollection _profileCollection;
	private boolean _searchProfileOnly;
	
	static{
		TDIServiceHelper.setupEnvironment();
	}
	
	public IterateProfileDBServiceImpl(){
		super(TDIProfileService.class);
		_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);	
		_searchProfileOnly = false;
		init();
		
	}
	
	public IterateProfileDBServiceImpl(boolean onlyProfile){
		super(TDIProfileService.class);
		_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);	
		_searchProfileOnly = onlyProfile;
		init();
		
	}
	
	private void init(){
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering init");			
		}
//		logger.log(Level.FINEST, classname, "init");
		
		//init first paging
		_index = 0;
		_searchOptions = new TDIProfileSearchOptions();
		_searchOptions.setProfileOnly(_searchProfileOnly);
		_profileCollection = _tdiProfileSvc.getProfileCollection(_searchOptions);
		

	}
	
	public Entry getNextProfileEntry(){
		if (LOG.isTraceEnabled()) {
			LOG.trace("entering getNextProfileEntry");			
		}
//		logger.log(Level.FINEST, classname, "getNextProfileEntry");
		
		Entry entry = null;
		if(_index==_profileCollection.getProfiles().size()){
			_searchOptions = _profileCollection.getNextPage();
			
			if(_searchOptions==null){
				return null;
			}else{
				_profileCollection = _tdiProfileSvc.getProfileCollection(_searchOptions);
				_index=0;
			}
		}
		ProfileDescriptor profileDescript = _profileCollection.getProfiles().get(_index);
		ProfileEntry profileEntry = new ProfileEntry(profileDescript);
		entry = profileEntry.getEntry();
		_index++;
		return entry;
	}
		
	

}
