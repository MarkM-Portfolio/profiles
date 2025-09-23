/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
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
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.Pronunciation;
import com.ibm.lconn.profiles.data.PronunciationCollection;
import com.ibm.lconn.profiles.data.PronunciationRetrievalOptions;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.PronunciationService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class PronunciationConnector extends Connector{

	private static String classname = PronunciationConnector.class.getName();
	private static final Log LOG = LogFactory.getLog(classname);
	private ResourceBundleHelper _mlp;

	private TDIProfileService _tdiProfileSvc;
	private PronunciationService _pronunciationSvc;

	Pronunciation _pronunciationItem;
	private PronunciationRetrievalOptions _nextSet;
	private Iterator<Pronunciation> _pronunciationCollection;

	/**
	 * Constructor
	 */
	public PronunciationConnector()
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
		
		//
		// Allow TDICRUDService handle initialization of Profiles TDI AppContext and avoid code duplication
		//
		ProfilesTDICRUDServiceImpl.getInstance();	
		if (_tdiProfileSvc == null)
			_tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);	
		if (_pronunciationSvc == null)
			_pronunciationSvc = AppServiceContextAccess.getContextObject(PronunciationService.class);
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
			LOG.trace("Begin-PronunciationConnector.selectEntries");
		}
		_nextSet = new PronunciationRetrievalOptions(); // reset next set
		loadNextSet();	// load next set explicitly to reset state
	}

	/**
	 * Simple method to load next page assuming there is a set to load
	 */
	private void loadNextSet() {
		if (_nextSet != null) {
			PronunciationCollection pronunciations = _pronunciationSvc.getAll(_nextSet);
			_pronunciationCollection = pronunciations.getResults().iterator();
			_nextSet = pronunciations.getNextSet();
		}		
	}

	/**
	 * Used by the iterator mode
	 * Return the next entry.
	 *
	 * @return The next entry.
	 */
	public Entry getNextEntry()
	{	
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PronunciationConnector.getNextEntry");
		}
		// if have next entry ready, return
		if (_pronunciationCollection != null && _pronunciationCollection.hasNext()) {
			return pronunciationToEntry(_pronunciationCollection.next());
		}
		// else if have a 
		else if (_nextSet != null) {
			loadNextSet();
			return getNextEntry();
		}	
		else {
			_pronunciationCollection = null;
			return null;
		}
	}
	
	/**
	 * Used by the lookup/delete mode
	 * Return the pronunciation entry matched with the SearchCriteria.
	 *
	 * @return The pronunciation entry.
	 */
	public Entry findEntry(SearchCriteria search) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PronunciationConnector.findEntry");
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
		return TDICodeRunner.run(new PronunciationCodeBlock<Entry>("err_method_findEntry", searchValue)
		{
			public Entry run() throws RuntimeException 
			{
				Entry entry;
				// Clear list of multiple entries found
				clearFindEntries();
				try{			
					if((PeoplePagesServiceConstants.UID).equals(searchKey))
					{
						String profileUid = searchValue;
						entry = getPronunciationByUid(profileUid);
						if(entry == null)
						{
							if (LOG.isErrorEnabled()) {
								LOG.trace(_mlp.getString("err_noEntry_with_searchValue", profileUid));
							}
							return null;
						}
						addFindEntry(entry);
					}
					else if((PeoplePagesServiceConstants.KEY).equals(searchKey))
					{
						String profileUid = searchValue;
						entry = getPronunciationByKey(profileUid);
						if(entry == null)
						{
							if (LOG.isErrorEnabled()) {
								LOG.trace(_mlp.getString("err_noEntry_with_searchValue", profileUid));
							}
							return null;
						}
						addFindEntry(entry);
					}
					else
						throw new TDICodeBlockException(_mlp.getString("err_unSupport_searchCriteria", searchKey));
					// If only one entry is found we return that one. Otherwise, we return null to signal
					// that zero or more than one was found (caller uses getFindEntryCount() to get actual number).
					if (getFindEntryCount() == 1) 
						return getFirstFindEntry();
					else
						return null;
				}catch(Exception e) {
					e.printStackTrace();
				}	
			return null;
			}		
		});
	}
	
	/**
	 * Used by the delete mode
	 * delete the pronunciation entry matched with the SearchCriteria.
	 *@param entry: the entry result get from findEntry.
	 */
	public void deleteEntry (Entry entry, SearchCriteria search) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PronunciationConnector.deleteEntry");
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

		TDICodeRunner.run(new PronunciationCodeBlock<Object>("err_method_deleteItem", searchValue) 
		{	
			public Object run() throws RuntimeException 
			{
				if((PeoplePagesServiceConstants.KEY).equals(searchKey))
				{
					LOG.trace("deletePronunciation_key " + searchValue);
					ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc,
							TDIProfileAttribute.KEY, searchValue);
					if(searchFor == null)
						throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", searchValue));
					
					_pronunciationSvc.delete(searchValue);
				}
				else if((PeoplePagesServiceConstants.UID).equals(searchKey))
				{
					ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc, 
							TDIProfileAttribute.UID, searchValue);
					if(searchFor == null)
						throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", searchValue));

					String key = searchFor.getProfile().getKey();
					LOG.trace("deletePronunciation_uid " + searchValue);
					_pronunciationSvc.delete(key);
				}			
				return null;
			}	
		});
	}
	
	private void updatePronunciation(Entry prEntry)throws TDIException{
		String pronunciationUid = prEntry.getString(PeoplePagesServiceConstants.UID);
		inputValidation(prEntry,_mlp.getString("err_nullSearchValue"));
		inputValidation(pronunciationUid,_mlp.getString("err_nullSearchValue"));

		final Entry entry = prEntry;

		TDICodeRunner.run(new PronunciationCodeBlock<Entry>("err_method_queryReply",pronunciationUid)
		{
			public Entry run() throws RuntimeException,TDICodeBlockException
			{
				InputStream linkStream = null;
				byte[] pronunciationContent = null;
				String uid = entry.getString(PeoplePagesServiceConstants.UID);
				Attribute streamAttr= entry.getAttribute("linkStream");
				Attribute contentAttr= entry.getAttribute("audioFile");
				try {
						if(streamAttr != null)
						{
							linkStream = (InputStream)streamAttr.getValue(0);
							if(linkStream != null)
								updatePronounceBylinkStream(uid, linkStream);
							else 
								throw new TDICodeBlockException(_mlp.getString("err_no_updateFile_detail"));
						}
						else if(contentAttr != null)
						{
							pronunciationContent = (byte[])contentAttr.getValue(0);
							if(pronunciationContent != null)
								updatePronounceByMediaContent(uid, pronunciationContent);
							else
								throw new TDICodeBlockException(_mlp.getString("err_no_updateFile_detail"));
						}
						else
							throw new TDICodeBlockException(_mlp.getString("err_unSupport_update_method"));					
					} catch (TDIException e) {
						e.printStackTrace();
					}			
					return null;
			}
		});
	}
	
	
	/**
	 * Used by the addonly/update(when no entry matched in findEntry function) mode
	 * add this new entry.
	 * @param entry: the entry to be added
	 * @throws TDIException 
	 */
	public void putEntry (Entry entry) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PronunciationConnector.putEntry entry=" + entry);
		}
		updatePronunciation(entry);
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

	public void modEntry(Entry prEntry, SearchCriteria search, Entry old)throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PronuncationConnector.modEntry");
		}

		inputValidation(prEntry, _mlp.getString("err_required_null", "entry"));
		inputValidation(search, _mlp.getString("err_nullSearchValue"));
		inputValidation(old, _mlp.getString("err_required_null", "entry"));
		
		updatePronunciation(prEntry);
	}
	
	/**
	 * Used by the updateToDB mode
	 * update the pronunciation entry into DB.
	 *
	 *@return The pronunciation entry.
	 * @throws TDIException 
	 */
	public Entry queryReply(Entry theEntry) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-PronuncationConnector.queryReply");
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
			LOG.trace("Begin-PronunicationConnector.updateToDB entry=" + entry);
		}
		updatePronunciation(entry);
		return getPronunciationByUid(entry.getString(PeoplePagesServiceConstants.UID));
	}
	
	private void  updatePronounceBylinkStream(String uid, InputStream linkStream) throws TDIException 
	{
		final String pronounceUID = uid;
		final InputStream link = linkStream;				
		TDICodeRunner.run(new PronunciationCodeBlock<Object>("err_method_updatePronounceBylinkStream",uid) 
		{	
			public Object run() throws RuntimeException {
				AssertionUtils.assertNotNull(pronounceUID, AssertionType.PRECONDITION);
				ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc, TDIProfileAttribute.UID, pronounceUID);
				if(searchFor == null)
					throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", pronounceUID));
				String key = searchFor.getProfile().getKey();		
				String fileURL = TDIConnectorHelper.inputStreamtoString(link);
				byte[] fileContent = TDIServiceHelper.getURLContent(fileURL);
				Pronunciation pronunciation = new Pronunciation();
				pronunciation.setAudioFile(fileContent);
				pronunciation.setKey(key);
				_pronunciationSvc.update(pronunciation);
								
				return null;
			}	
		});		
	}
	
	private void updatePronounceByMediaContent(String uid, byte[] fileContent) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("updatePronunciation_uid: " + uid);
		}
		
		final String pronounceUID = uid;
		final byte[] mediaContent = fileContent;
		
		TDICodeRunner.run(new PronunciationCodeBlock<Object>("err_method_updatePronounceByMediaContent", uid)
		{	
			public Object run() throws RuntimeException,TDICodeBlockException {
				ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc, TDIProfileAttribute.UID, pronounceUID);				
				if(searchFor == null)
					throw new TDICodeBlockException(_mlp.getString("err_noEntry_with_searchValue", pronounceUID));

				String key = searchFor.getProfile().getKey();
				Pronunciation pronunciation = new Pronunciation();
				pronunciation.setAudioFile(mediaContent);
				pronunciation.setKey(key);
				_pronunciationSvc.update(pronunciation);
										
				return null;
			}
		});
	}
	
	private Entry getPronunciationByUid(String uid) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("getPronunciationByUid | uid : " + uid);
		}
		inputValidation(uid,_mlp.getString("err_nullSearchValue"));
		final String uidValue = uid;
		return TDICodeRunner.run(new PronunciationCodeBlock<Entry>("err_method_getItemByUid", uid)
		{
			public Entry run() throws RuntimeException {
				ProfileDescriptor searchFor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc,
						TDIProfileAttribute.UID, uidValue);
				if(searchFor == null)
					return null;
				String key = searchFor.getProfile().getKey();
				LOG.trace("getPronunciation_key " + key);
				
				Pronunciation pronunciation = _pronunciationSvc.getByKey(key);
				if(pronunciation == null)
					return null;
				Entry resEntry = pronunciationToEntry(pronunciation);					
				return resEntry;
			}
		});
	}
	
	private Entry getPronunciationByKey(String key) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("getPronunciationByKey | key : " + key);
		}
		inputValidation(key,_mlp.getString("err_nullSearchValue"));
		final String keyValue = key;
		return TDICodeRunner.run(new PronunciationCodeBlock<Entry>("err_method_getItemByKey", key)
		{
			public Entry run() throws RuntimeException {
				Pronunciation pronunciation = _pronunciationSvc.getByKey(keyValue);
				if(pronunciation == null)
					return null;
				Entry resEntry = pronunciationToEntry(pronunciation);
				return resEntry;
			}		
		});
	}
	
	private Entry pronunciationToEntry(Pronunciation pronunciationResource)
	{
		Entry entry = new Entry();		
		entry.addAttributeValue("key", pronunciationResource.getKey());
		entry.addAttributeValue("fileType",pronunciationResource.getFileType());
		entry.addAttributeValue("audioFile",pronunciationResource.getAudioFile());
		entry.addAttributeValue("updated",pronunciationResource.getUpdated());
		
		return entry;
	}
	
	/**
	 * Reusable code block
	 */
	private abstract class PronunciationCodeBlock<T> implements TDICodeBlock<T> {
		private String errMsg;
		private Object[] errMsgParams;
		
		public PronunciationCodeBlock(String errMsg, Object...errMsgParams) {
			this.errMsg = errMsg;
			this.errMsgParams = errMsgParams;
		}		
		
		public T handleTDICodeBlockException(TDICodeBlockException e) throws TDIException
		{
			String formattedMsg = _mlp.getString(errMsg, errMsgParams);
			String errorMsg = formattedMsg + " | " + e.getMessage();
			LOG.error(formattedMsg);
			if (getLog() != null) {
		        getLog().logerror(errorMsg, e);
		      }
			throw new TDIException(e.getMessage(), e);
		}
		
		public T handleRecoverable(RuntimeException e)throws TDIException 
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
	
	public void terminate()
	{
		
	}
	
	public String getVersion() {
		return "PronunciationConnector_4.0.0.0";
	}
}

