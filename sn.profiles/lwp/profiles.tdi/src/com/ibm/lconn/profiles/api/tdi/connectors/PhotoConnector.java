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

package com.ibm.lconn.profiles.api.tdi.connectors;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.Connector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.ibm.lconn.core.util.ResourceBundleHelper;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.ConnectorModes;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.SearchPair;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlock;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlockException;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeRunner;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDIConnectorHelper;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;
import com.ibm.lconn.profiles.api.tdi.service.impl.ProfilesTDICRUDServiceImpl;
import com.ibm.lconn.profiles.api.tdi.util.DBConnectionsHelper;
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.data.Photo;
import com.ibm.lconn.profiles.data.PhotoCollection;
import com.ibm.lconn.profiles.data.PhotoCrop;
import com.ibm.lconn.profiles.data.PhotoRetrievalOptions;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PhotoService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class PhotoConnector extends Connector{
	
	private static String classname = ProfileConnector.class.getName();
	private static final Log LOG = LogFactory.getLog(classname);
	
	private ResourceBundleHelper _mlp;
	
	private TDIProfileService _tdiProfileSvc;
	private PhotoService _photoSvc;
	
	private PhotoRetrievalOptions _nextSet;
	private Iterator<Photo> _photoCollection;
	  
	/**
	 * Constructor
	 */
	public PhotoConnector()
	{
		super();
		// Set the supported modes
		setModes(new String[]{
				ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.DELETE_MODE,
				ConnectorConfig.UPDATE_MODE,
				ConnectorModes.UPDATE_TO_DB,
				});
	}

	/**
	 * Initialize connector
	 *
	 * @param  o              ConnectorMode
	 * @exception  Exception  Description of the Exception
	 */	
	public void initialize (Object object) throws Exception
	{
		_mlp = new ResourceBundleHelper("profiles_messages");
		// Allow TDICRUDService handle initialization of Profiles TDI AppContext and avoid code duplication
		ProfilesTDICRUDServiceImpl.getInstance();		
		if (_tdiProfileSvc == null)
			_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		if (_photoSvc == null)
			_photoSvc = AppServiceContextAccess.getContextObject(PhotoService.class);
		
		//set environment when the 'headless' value is true
		if("true".equals(DBConnectionsHelper.getHeadlessTDIScripts()))
		{
			System.setProperty("java.awt.headless", "true"); 
		}
	}
	
	/**
	 * Used by the iterator mode
	 * selectEntries
	 *
	 * @exception  Exception  Description of the Exception
	 */
	public void selectEntries() throws Exception 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.selectEntries");
		}
		_nextSet = new PhotoRetrievalOptions(); // reset next set
		loadNextSet();	// load next set explicitly to reset state
	}

	/**
	 * Simple method to load next page assuming there is a set to load
	 */
	private void loadNextSet() {
		if (_nextSet != null) {
			PhotoCollection photos = _photoSvc.getAllPhotosForTDI(_nextSet);
			_photoCollection = photos.getResults().iterator();
			_nextSet = photos.getNextSet();
		}		
	}

	/**
	 * Used by the iterator mode
	 * Return the next photo entry.
	 *
	 * @return The the next photo entry.
	 */
	public Entry getNextEntry()
	{	
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-getNextEntry");
		}
		// if have next entry ready, return
		if (_photoCollection != null && _photoCollection.hasNext()) {
			return photoToEntry(_photoCollection.next());
		}
		// else if have a 
		else if (_nextSet != null) {
			loadNextSet();
			return getNextEntry();
		}	
		else {
			_photoCollection = null;
			return null;
		}
	}
	
	/**
	 * Used by the lookup/delete mode
	 * Return the photo entry matched with the SearchCriteria.
	 *
	 * @return The photo entry.
	 */
	public Entry findEntry(SearchCriteria search) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.findEntry");
		}
		inputValidation(search,_mlp.getString("err_nullSearchValue"));
		String searchValueTemp = "";
		String searchKeyTemp = "";
		if (search.getScriptFilter() != null)
		{
			SearchPair searchPair = TDIConnectorHelper.parseSearchScript(search);
			searchKeyTemp = searchPair.get_searchKey();
			searchValueTemp = searchPair.get_searchValue();
		}
		else
		{
			searchKeyTemp = search.getCriteria(0).name.toString();
			searchValueTemp = String.valueOf(search.getCriteria(0).value.toString());
		}
		final String searchValue = searchValueTemp;
		final String searchKey = searchKeyTemp;
		
		return TDICodeRunner.run(new PhotoCodeBlock<Entry>("err_method_findEntry", searchValueTemp)
		{
			public Entry run() throws RuntimeException/*BEGIN*/, TDICodeBlockException /*END*/{				
				Entry entry;
				// Clear list of multiple entries found
				clearFindEntries();
				try 
				{
					// a search criteria of UID is not possible if using the connector with
					// update mode, but was possible with the old (now deprecated) updateDB mode.  
					// When the old mode is removed, this code can also be removed.					
					if((PeoplePagesServiceConstants.UID).equals(searchKey))
					{
						String profileUid = searchValue;
						entry = getPhotoByUid(profileUid);	
						if(entry == null)
						{
							if (LOG.isErrorEnabled()) {
								LOG.error(_mlp.getString("err_noEntry_with_searchValue", profileUid));
							}
							return null;
						}
						addFindEntry(entry);
					}
					else if((PeoplePagesServiceConstants.KEY).equals(searchKey))
					{
						String profileKey = searchValue;
						entry = getPhotoByKey(profileKey);
						if(entry == null)
						{
							if (LOG.isErrorEnabled()) {
								LOG.error(_mlp.getString("err_noEntry_with_searchValue", profileKey));
							}
							return null;
						}
						addFindEntry(entry);
					}
					else
						throw new TDICodeBlockException(_mlp.getString("err_unSupport_searchCriteria", searchKey));		
					// If only one entry is found we return that one. Otherwise, we return null to signal
					// that zero or more than one was found (caller uses getFindEntryCount() to get actual number).
					if (getFindEntryCount() == 1) {
						return getFirstFindEntry();
					} else {
						return null;
					}
				}
				catch (Exception e) {
					String errorMsg = e.getMessage();
					LOG.error(errorMsg);
					if (getLog() != null) {
				        getLog().logerror(errorMsg, e);
				      }
				}	
				return null;
			}
		});
	}
	
	/**
	 * Used by the delete mode
	 * delete the photo entry matched with the SearchCriteria.
	 *@param entry: the entry result get from findEntry.
	 */
	public void deleteEntry (Entry entry, SearchCriteria search) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.deleteEntry");
		}
		inputValidation(search,_mlp.getString("err_nullSearchValue"));
	
		String searchValueTemp = "";
		String searchKeyTemp = "";
		if (search.getScriptFilter() != null)
		{
			SearchPair searchPair = TDIConnectorHelper.parseSearchScript(search);
			searchKeyTemp = searchPair.get_searchKey();
			searchValueTemp = searchPair.get_searchValue();
		}
		else
		{
			searchKeyTemp = search.getCriteria(0).name.toString();
			searchValueTemp = String.valueOf(search.getCriteria(0).value.toString());
		}
		final String searchValue = searchValueTemp;
		final String searchKey = searchKeyTemp;
		
		TDICodeRunner.run(new PhotoCodeBlock<Object>("err_method_deleteItem", searchValue) 
		{	
			/* main method body */
			public Object run() throws RuntimeException,TDICodeBlockException {
				if((PeoplePagesServiceConstants.KEY).equals(searchKey))
				{
					LOG.trace("deletePhoto_key " + searchValue);
					ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc,
							TDIProfileAttribute.KEY, searchValue);
					if(searchFor == null)
						throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", searchValue));
					
					_photoSvc.deletePhotoByKeyForTDI(searchValue);
				}
				else if((PeoplePagesServiceConstants.UID).equals(searchKey))
				{
					// a search criteria of UID is not possible if using the connector with
					// update mode, but was possible with the old (now deprecated) updateDB mode.  
					// When the old mode is removed, this code can also be removed.
					ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc,
							TDIProfileAttribute.UID, searchValue);
					if(searchFor == null)
					{
						throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", searchValue));
					}
					String key = searchFor.getProfile().getKey();
					if (LOG.isTraceEnabled()) {
						LOG.trace("deletePhoto_uid " + searchValue);
					}
					_photoSvc.deletePhotoByKeyForTDI(key);
				}
				return null;
			}	
		});
	}
	
	private void updatePhoto(Entry photoEntry)throws TDIException{

		String photoUid = photoEntry.getString(PeoplePagesServiceConstants.UID);
		inputValidation(photoUid, _mlp.getString("err_required_null", "uid"));

		final Entry entry = photoEntry;
		final String uid = photoUid;
		
		TDICodeRunner.run(new PhotoCodeBlock<Entry>("err_method_queryReply", photoUid)
		{
			public Entry run() throws RuntimeException {
				byte[] photoContent = null;
				InputStream linkStream = null;
				Attribute streamAttr= entry.getAttribute("linkStream");
				Attribute imageAttr= entry.getAttribute("image");
				try {
					if(streamAttr != null)
					{
						linkStream = (InputStream)streamAttr.getValue(0);
						if(linkStream != null)
							updatePhotoBylinkStream(uid, linkStream);
						else 
							throw new TDICodeBlockException(_mlp.getString("err_no_updateFile_detail"));
					}
					else if(imageAttr != null)
					{
						photoContent = (byte[])imageAttr.getValue(0);
						if(photoContent != null)
							updatePhotoByImageContent(uid, photoContent);
						else
							throw new TDICodeBlockException(_mlp.getString("err_no_updateFile_detail"));
					}
					else
						throw new TDICodeBlockException(_mlp.getString("err_unSupport_update_method"));	
				} 
				catch (TDIException e) {
					String errorMsg = e.getMessage();
					LOG.error(errorMsg);
					if (getLog() != null) {
				        getLog().logerror(errorMsg, e);
				      }
				}
				return null;
			}		
		});
	}
	
	
	/**
	 * Used by the addonly/update(when no entry matched in findEntry function) mode
	 * add this new profile entry.
	 * @param entry: the photo entry to be added
	 * @throws TDIException 
	 */
	public void putEntry (Entry entry) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.putEntry entry=" + entry);
		}
		updatePhoto(entry);
	}

	/**
	 * Used by the update(when the entry matched) mode
	 * add this new profile entry.
	 *@param entry: the output mapped conn Entry, ready to be written to the data source. (entry in EMPLOYEE table)
	 *@param search: the SearchCriteria to be used to make the modify call to the underlying system. 
	 * @throws TDIException 
	 */
	public void modEntry (Entry entry, SearchCriteria search) throws TDIException
	{
		modEntry(entry, search, findEntry(search));
	}	

	public void modEntry(Entry photoEntry, SearchCriteria search, Entry old)throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.modEntry");
		}

		inputValidation(photoEntry, _mlp.getString("err_required_null", "entry"));
		inputValidation(search, _mlp.getString("err_nullSearchValue"));
		inputValidation(old, _mlp.getString("err_required_null", "entry"));
		
		updatePhoto(photoEntry);
	}

		/**
	 * Used by the updateToDB mode
	 * update the photo entry into DB.
	 *
	 *@return The photo entry.
	 */
	public Entry queryReply(Entry theEntry) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.queryReply");
		}
		String connectorMode = ((ConnectorConfig)getConfiguration()).getMode();
		if((ConnectorModes.UPDATE_TO_DB).equals(connectorMode))
		{
			logmsg(_mlp.getString("err_mode_deprecated", ConnectorModes.UPDATE_TO_DB));
			theEntry = updateToDB(theEntry);
		}
		return theEntry;
	}
	
	/**
	 * This mode was supposed to act as update?  It is now deprecated in favor of the standard mode
	 * 
	 * @param entry
	 * @deprecated
	 */
	private Entry updateToDB(Entry entry) throws TDIException
	{		
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PhotoConnector.updateToDB entry=" + entry);
		}
		updatePhoto(entry);
		return getPhotoByUid(entry.getString(PeoplePagesServiceConstants.UID));
	}	
	
	private void updatePhotoBylinkStream(String uid, InputStream linkStream) throws TDIException {
		final String photoUID = uid;
		final InputStream link = linkStream;

		TDICodeRunner.run(new PhotoCodeBlock<Object>("err_method_updateItemBylinkStream",uid) 
		{	
			public Object run() throws RuntimeException {
				ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc, 
						TDIProfileAttribute.UID, photoUID);
				if(searchFor == null)
					throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", photoUID));
				
				String key = searchFor.getProfile().getKey();
				//convert InputStream to byte[]
				String imageURL = TDIConnectorHelper.inputStreamtoString(link);
				byte[] fileContent = TDIServiceHelper.getURLContent(imageURL);
				PhotoCrop photo = new PhotoCrop();
				photo.setKey(key);
				photo.setImage(fileContent);	
				_tdiProfileSvc.updatePhotoForTDI(photo);
				return null;
			}	
		});
	}
	
	private void updatePhotoByImageContent(String uid, byte[] image) throws TDIException {
		if (LOG.isTraceEnabled()) {
			LOG.trace("updatePhotoByImageContent with value" + uid);
		}
		final String photoUID = uid;
		final byte[] imageContent = image;
		
		TDICodeRunner.run(new PhotoCodeBlock<Object>("err_method_updateItemByImageContent", uid) 
		{	
			public Object run() throws RuntimeException {
			ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc,
					TDIProfileAttribute.UID, photoUID);
			if(searchFor == null)
				throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", photoUID));

			String key = searchFor.getProfile().getKey();
			
			PhotoCrop photo = new PhotoCrop();
			photo.setKey(key);
			photo.setImage(imageContent);
			_tdiProfileSvc.updatePhotoForTDI(photo);
			
			return null;
			}
		});
	}
	
	private Entry getPhotoByUid(String uid) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("getPhotoByUid " + uid);
		}
		inputValidation(uid,_mlp.getString("err_required_null", "uid"));
		final String uidValue = uid;
		return TDICodeRunner.run(new PhotoCodeBlock<Entry>("err_method_getItemByUid", uid)
		{
			public Entry run() throws RuntimeException {	
				ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc,
						TDIProfileAttribute.UID, uidValue);
				if(searchFor == null)
					return null;
				String key = searchFor.getProfile().getKey();
				Photo photo = _photoSvc.getPhotoForTDI(ProfileLookupKey.forKey(key));
				if(photo == null)
					return null;
				
				Entry resEntry = photoToEntry(photo);
				return resEntry;
			}
			
		});
	}
	
	private Entry getPhotoByKey(String key) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("getPhotoByKey | key : " + key);
		}
		inputValidation(key, _mlp.getString("err_required_null", "key"));
		final String keyValue = key;
		return TDICodeRunner.run(new PhotoCodeBlock<Entry>("err_method_getItemByKey", key)
		{
			public Entry run() throws RuntimeException 
			{
				Photo photo = _photoSvc.getPhotoForTDI(ProfileLookupKey.forKey(keyValue));
				if(photo == null)
					return null;
				Entry resEntry = photoToEntry(photo);
				return resEntry;
			}			
		});
	}

	private Entry photoToEntry(Photo photoResource)
	{
		Entry entry = new Entry();
		
		entry.addAttributeValue("key", photoResource.getKey());
		entry.addAttributeValue("fileType",photoResource.getFileType());
		entry.addAttributeValue("image",photoResource.getImage());
		entry.addAttributeValue("updated",photoResource.getUpdated());
		
		return entry;
	}
	
	/**
	 * Reusable code block
	 */
	private abstract class PhotoCodeBlock<T> implements TDICodeBlock<T> {
		private String errMsg;
		private Object[] errMsgParams;
		
		public PhotoCodeBlock(String errMsg, Object...errMsgParams) {
			this.errMsg = errMsg;
			this.errMsgParams = errMsgParams;
		}	
		
		// BEGIN
		public T handleTDICodeBlockException(TDICodeBlockException e) throws TDIException
		{
			String formattedMsg = _mlp.getString(errMsg, errMsgParams);
			String errorMsg = formattedMsg + " | " + e.getMessage();
			LOG.error(errorMsg);
			if (getLog() != null) {
		        getLog().logerror(errorMsg, e);
		      }
			throw new TDIException(e.getMessage(), e);
		}
		// END
		
		public T handleRecoverable(RuntimeException e)
				throws TDIException 
		{
			String formattedMsg = _mlp.getString(errMsg, errMsgParams);
			LOG.error(formattedMsg);
			if (getLog() != null) {
		        getLog().logerror(formattedMsg, e);
		      }
			throw new TDIException(formattedMsg, e);
		}
		
		public Log getLogger() {
			return LOG;
		}
	}
	
	private void inputValidation(Object input, String errorMsg) throws TDIException
	{
		if(input == null)
		{
			logmsg(errorMsg);
			throw new TDIException(errorMsg);
		}
	}
	
	public void terminate(){
	}
	
	public String getVersion() {
		return "PhotoConnector_4.0.0.0";
	}
}
