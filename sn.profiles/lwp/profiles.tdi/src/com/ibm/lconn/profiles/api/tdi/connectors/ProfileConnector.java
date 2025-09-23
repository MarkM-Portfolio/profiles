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

package com.ibm.lconn.profiles.api.tdi.connectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.SearchCriteria;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.AbstractProfilesConnector;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.ConnectorModes;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.SearchPair;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlock;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlockException;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeRunner;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDIConnectorHelper;
import com.ibm.lconn.profiles.api.tdi.service.TDIException;
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.config.dm.DMConfig;
import com.ibm.lconn.profiles.config.dm.ExtensionAttributeConfig;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.ProfileAttributes;
import com.ibm.lconn.profiles.data.ProfileAttributes.Attribute;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.data.ProfileExtension;
import com.ibm.lconn.profiles.data.TDICriteriaOperator;
import com.ibm.lconn.profiles.data.TDIProfileCollection;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria;
import com.ibm.lconn.profiles.data.TDIProfileSearchCriteria.TDIProfileAttribute;
import com.ibm.lconn.profiles.data.TDIProfileSearchOptions;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.NameHelper;
import com.ibm.lconn.profiles.internal.util.ProfileHelper;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

public class ProfileConnector extends AbstractProfilesConnector {

	private static String classname = ProfileConnector.class.getName();
	private static final Log LOG = LogFactory.getLog(classname);
	
	private static final int IS_MANAGER_IX = 0;
	private static final int UID_IX        = 1;
	private static final int DIST_NAME_IX  = 2;
	private static final int GUID_IX       = 3;
	private static final int EMAIL_IX      = 4;
	private static final int DISP_NAME_IX  = 5;

	protected TDIProfileSearchOptions _nextSet;
	protected Iterator<ProfileDescriptor> _profileCollection;
	
	/**
	 * Constructor
	 */
	public ProfileConnector()
	{
		super();
		// Set the supported modes
		setModes(new String[]{
				ConnectorConfig.ITERATOR_MODE,
				ConnectorConfig.LOOKUP_MODE,
				ConnectorConfig.DELETE_MODE,
				ConnectorConfig.ADDONLY_MODE,
				ConnectorConfig.UPDATE_MODE,
				ConnectorModes.INACTIVATE,
				//ConnectorModes.REVOKE,
				ConnectorModes.ACTIVATE
				});
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
			LOG.trace("Begin-selectEntries");
		}
		_profileCollection =  new Iterator<ProfileDescriptor>()
		{
			TDIProfileService service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
			TDIProfileSearchOptions nextSet = new TDIProfileSearchOptions();
			Iterator<ProfileDescriptor> currSet = null;
			
			public boolean hasNext() {
				if (currSet != null && currSet.hasNext()) {
					return true;
				}
				else if (nextSet != null) {
					// setProfileOnly says to get only the user data in MEMBERPROFILE table, i.e., don't read
					// extensions, given names, surnames, or login names tables.

					if (_iterator_return_key_data_only) {
						nextSet.setProfileOnly(true);
					}
					TDIProfileCollection coll = service.getProfileCollection(nextSet);
					currSet = coll.getProfiles().iterator();
					nextSet = coll.getNextPage();
					return hasNext();  // recurse to handle case where page is empty
				}
				return false;
			}

			public ProfileDescriptor next() {
				// ensures that their is an element to return
				if (!hasNext())
					throw new NoSuchElementException();				
				return currSet.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Used by the iterator mode
	 * Return the next profile entry.
	 *
	 * @return The the next profile entry.
	 */
	public Entry getNextEntry() throws TDIException
	{	
		return TDICodeRunner.run(new ProfileCodeBlock<Entry>("err_method_findEntry") {
			public Entry run() throws RuntimeException, TDICodeBlockException {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Begin-getNextEntry");
				}
				if(_profileCollection.hasNext())
				{
					ProfileDescriptor currentDescriptor = _profileCollection.next();
					Entry retEntry = TDIServiceHelper.descriptorToEntry(currentDescriptor);
					Employee currentProfile = currentDescriptor.getProfile();
					retEntry.setAttribute("sys_usrState", currentProfile.getState().getName());
					retEntry.setAttribute("sys_usrMode", currentProfile.getMode().getName());

					if (LOG.isTraceEnabled()) {
						LOG.trace("End-getNextEntry: mode: " + currentProfile.getMode().getName());
					}

					return retEntry;
				}
				else
					return null;
			}
		});
			
	}
	
	/**
	 * Used by the lookup/delete/update mode	 * Return the profile entry matched with the SearchCriteria.
	 *
	 * @return The profile entry.(entry in EMPLOYEE)
	 */
	public Entry findEntry(SearchCriteria search) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-findEntry");
		}
		inputValidation(search, _mlp.getString("err_nullSearchValue"));
		String searchValue = "";
		String searchKey = "";
		if (search.getScriptFilter() != null)
		{
			SearchPair searchPair = TDIConnectorHelper.parseSearchScript(search);
			searchKey = searchPair.get_searchKey();
			searchValue = searchPair.get_searchValue();
		}
		else
		{
		    Vector<?> criteria = search.getCriteria();
		    if (criteria.size() == 0) {
		    	throwTDIException("no searchCriteria");
		    }
		    searchKey = search.getCriteria(0).name;
		    searchValue = String.valueOf(search.getCriteria(0).value.toString());
		}
		inputValidation(searchValue,_mlp.getString("err_nullSearchValue"));
		
		final String value = searchValue;
		final String SearchField = searchKey;		

		if (LOG.isTraceEnabled()) {
			LOG.trace("ProfileConnector: findEntry: SearchField: " + SearchField);
			LOG.trace("ProfileConnector: findEntry: value: " + value);
		}

		return TDICodeRunner.run(new ProfileCodeBlock<Entry>("err_method_findEntry",searchValue)
		{
			public Entry run() throws RuntimeException, TDICodeBlockException
			{
				Entry entry;
				clearFindEntries();
				try 
				{
					TDIProfileAttribute SEARCH_CRITERIA = TDIProfileAttribute.resolveCriteria(SearchField);
					if(SEARCH_CRITERIA == null)
						throw new TDICodeBlockException("cannot create search_criteria according to the searchField: " + SearchField);

					if("".equals(value))
						throw new TDICodeBlockException("search value of this searchField is an empty string: " + SearchField);

					// Note that the original code for search fields, e.g, key, uid, ..., other than managerUid (which is the only field that
					// returns multiple values) has been 100% preserved, i.e., the code could have be rewritten using 
					// just getEmployeesByCriterial()  (note the 's' in Employees), and then check for a single employee.  However,
					// the more conservative approach of using the original code as id was adopted.	The original appraoch only
					// worked for managerUid if a manager has one report.
					if ( ! (SearchField.equals("managerUid")))
					{
						if (LOG.isTraceEnabled()) {
							LOG.trace("ProfileConnector: findEntry: SearchField is NOT managerUid : " + SearchField);
						}

						ProfileDescriptor searchForProfileDescriptor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc, SEARCH_CRITERIA, value);

						if(searchForProfileDescriptor!=null)
						{
							entry = TDIServiceHelper.descriptorToEntry(searchForProfileDescriptor);
							Employee currentProfile = searchForProfileDescriptor.getProfile();
							entry.addAttributeValue("usrState",currentProfile.getState().getName());
							addFindEntry(entry);
						}
						else
						{
							if (LOG.isTraceEnabled()) {
								LOG.trace("can not find the entry with the searchValue: " + value);
							}
							return null;
						}
						//TODO If we get more than one result, how to deal with?
						if (getFindEntryCount() == 1) {
							return getFirstFindEntry();
						} else {
							return null;
						}
					}
					else
					{
						if (LOG.isTraceEnabled()) {
							LOG.trace("ProfileConnector: findEntry: SearchField is managerUid : " + SearchField);
						}

						// search field is managerUid, which can result in multiple entries
						TDIProfileCollection profileDescCollection = TDIConnectorHelper.getEmployeesByCriterial(_tdiProfileSvc, SEARCH_CRITERIA, value);
						if (profileDescCollection != null)
						{
							int profilesCount = profileDescCollection.getProfiles().size();

							if (LOG.isTraceEnabled()) {
								LOG.trace("ProfileConnector: findEntry: profilesCount: " + profilesCount);
							}

							if ((profilesCount == 0))
								return null;
							else
							if ((profilesCount == 1))
							{
								ProfileDescriptor searchForProfileDescriptor = profileDescCollection.getProfiles().get(0);

								entry = TDIServiceHelper.descriptorToEntry(searchForProfileDescriptor);
								Employee currentProfile = searchForProfileDescriptor.getProfile();
								entry.addAttributeValue("usrState",currentProfile.getState().getName());
								addFindEntry(entry);

								return getFirstFindEntry();
							}
							else // assume > 1
							{
								ProfileDescriptor searchForProfileDescriptor = null;

								for (int ix = 0; ix < profilesCount; ix++)
								{
									if (LOG.isTraceEnabled()) {
										LOG.trace("ProfileConnector: findEntry: ix: " + ix);
									}

									searchForProfileDescriptor = profileDescCollection.getProfiles().get(ix);

									if (LOG.isTraceEnabled()) {
										LOG.trace("ProfileConnector: findEntry: searchForProfileDescriptor.getProfile().getDisplayName(): " + searchForProfileDescriptor.getProfile().getDisplayName());
									}

									entry = TDIServiceHelper.descriptorToEntry(searchForProfileDescriptor);
									Employee currentProfile = searchForProfileDescriptor.getProfile();
									entry.addAttributeValue("usrState",currentProfile.getState().getName());
									addFindEntry(entry);
								}

								// tdi doc says to call getFindEntryCount() at this point.
								int entryCount = getFindEntryCount(); 
								if (LOG.isTraceEnabled()) {
									LOG.trace("ProfileConnector: findEntry: getFindEntryCount(): " + entryCount);
								}

								// following the rules of "Developing a Connector", return null except when count is 1.
								return null;
							}
						}
						else
						{
							if (LOG.isTraceEnabled()) {
								LOG.trace("can not find the entry with the searchValue: " + value);
							}
							return null;
						}
					}
				}
				catch (Exception e) {
					String errorMsg = e.getMessage();
					LOG.error(errorMsg);
					if (getLog() != null) 
						getLog().logerror(errorMsg, e);
			}	
			return null;
		}
		});
	}
	
	/**
	 * Used by the delete mode (will call findEntry() function first)
	 * delete the profile entry matched with the SearchCriteria.
	 *@param entry: the entry result get from findEntry. (entry in EMPLOYEE table)
	 */
	public void deleteEntry (Entry entry, SearchCriteria search) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-deleteEntry");
		}
		inputValidation(search, _mlp.getString("err_nullSearchValue"));
		final Entry profileEntry = entry;
		String searchValueTemp = "";
		if (search.getScriptFilter() != null) 
		{
			SearchPair searchPair = TDIConnectorHelper.parseSearchScript(search);
			searchValueTemp = searchPair.get_searchValue();
		}
		else
			searchValueTemp = String.valueOf(search.getCriteria(0).value.toString());

		final String searchValue = searchValueTemp;
		inputValidation(searchValue, _mlp.getString("err_nullSearchValue"));
		TDICodeRunner.run(new ProfileCodeBlock<Object>("err_method_deleteItem", searchValue) 
		{
			public Object run() throws RuntimeException, TDICodeBlockException {
				String key = profileEntry.getString("key");
				try {
					if (_state == STATE_INACTIVATE) {
						_tdiProfileSvc.inactivateProfile(profileEntry.getString(PeoplePagesServiceConstants.KEY));
					}
					else {
						deleteProfile(key);
					}
				} catch (TDIException e) {
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
	 *@param entry: the entry need to be added into EMPLOYEE. (entry in EMPLOYEE table)
	 * @throws TDIException 
	 */
	public void putEntry (Entry entry) throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-putEntry");
		}
		inputValidation(entry, _mlp.getString("err_required_null", "entry"));
		String key = createProfile(entry);
		entry.addAttributeValue("__key__", key);

	}
	
	/**
	 * Used by the update(when the entry matched) mode
	 * add this new profile entry.
	 *@param entry: the output mapped conn Entry, ready to be written to the data source. (entry in EMPLOYEE table)
	 *@param search: the SearchCriteria to be used to make the modify call to the underlying system. 
	 * @throws TDIException 
	 */
	public void modEntry (Entry ldapEntry, SearchCriteria search) throws TDIException
	{
		modEntry(ldapEntry, search, null);
	}	

	@SuppressWarnings("deprecation")
	public void modEntry(Entry ldapEntry, SearchCriteria search, Entry old)throws TDIException
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-ProfileConnector.modEntry");
			LOG.trace("Begin-ProfileConnector.modEntry0: ldapEntry: " + ldapEntry);
			LOG.trace("Begin-ProfileConnector.modEntry1: old: " + old);
		}

		ProfileDescriptor descriptorToUpdate = null;
		Entry entryWithUpdates = null;
		boolean doUpdate = false;
		
		inputValidation(ldapEntry, _mlp.getString("err_required_null", "ldapEntry"));
		inputValidation(search, _mlp.getString("err_nullSearchValue"));

		if (LOG.isDebugEnabled()) {
			LOG.debug("mark managers = " + _mark_manager  + ", state = " + _state);
		}

		// new options on the connector to set state and manager field				
		if (_mark_manager) {

			String keyVal = ldapEntry.getString("key");

			String isManagerVal = ldapEntry.getString("isManager");
			String uidVal = ldapEntry.getString("uid");
			String dnVal = ldapEntry.getString("distinguishedName");
			String guidVal = ldapEntry.getString("guid");
			String emailVal = ldapEntry.getString("email");
			String displayNameVal = ldapEntry.getString("displayName");

			if (LOG.isTraceEnabled()) {
				LOG.trace("Begin-ProfileConnector.modEntry5: isManagerVal: " + isManagerVal);
			}

			// test for not set yet, e.g., first mark manager run
			if (isManagerVal == null) {
				isManagerVal = "X";  // set bogus value so update will be done,
									 // can't use null, that means using mark manager mode (does it work?) 
			}

			String[] valsFromIterator = new String[6];
			valsFromIterator[IS_MANAGER_IX] = isManagerVal;
			valsFromIterator[UID_IX] = uidVal;	   //  qqq need constant
			valsFromIterator[DIST_NAME_IX] = dnVal;
			valsFromIterator[GUID_IX] = guidVal;
			valsFromIterator[EMAIL_IX] = emailVal;
			valsFromIterator[DISP_NAME_IX] = displayNameVal;

			updateManagerField( keyVal, valsFromIterator);

			if (LOG.isTraceEnabled()) {
				LOG.trace("Begin-ProfileConnector.modEntry5: isManager return: ");
			}

			return;
		}

		// the profile connector supports the "skip lookup" option which means did not
		// previously lookup the user and compare to work.  In this case, we will bypass
		// comparisons to the db and just do an update.
		if (old != null) {
			descriptorToUpdate = TDIServiceHelper.entryToDescriptor(old);
			entryWithUpdates = old.clone(old);
			entryWithUpdates.merge(ldapEntry);
		}
		// old is null
		else {
			descriptorToUpdate = TDIServiceHelper.entryToDescriptor(ldapEntry);
			entryWithUpdates = ldapEntry.clone(ldapEntry);
			doUpdate = true;
		}

		if (descriptorToUpdate != null) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("in mod entry, entryWithUdates = " + entryWithUpdates
						+ ", descToUpdate = " + descriptorToUpdate);
			}
			// update all,include source URL
			String sourceLDAPURL = null;
			String dbUrl = descriptorToUpdate.getProfile().getSourceUrl();

			com.ibm.di.entry.Attribute attr = entryWithUpdates.getAttribute(PeoplePagesServiceConstants.SOURCE_URL);
			if (attr != null)
				sourceLDAPURL = (String) attr.getValue();

			if ((dbUrl == null) || (!dbUrl.equals(sourceLDAPURL)))
				doUpdate = true;

			if (!doUpdate) {
				ProfileDescriptor descriporFromLDAP = TDIServiceHelper
						.entryToDescriptor(entryWithUpdates);
				if (LOG.isTraceEnabled()) {
					LOG.trace("in mod entry, descriporFromLDAP = " + descriporFromLDAP);
				}

				boolean match = compareTwoProfile(descriporFromLDAP,
						descriptorToUpdate);
				doUpdate = !match;
				}
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-ProfileConnector.modEntry2: doUpdate: " + doUpdate);
		}

		if(doUpdate)
		{
			String crtKey = descriptorToUpdate.getProfile().getKey();
			if ((crtKey != null) && entryWithUpdates.getAttribute("key").getValue() == "") {
				entryWithUpdates.addAttributeValue("key", crtKey);
			}
			updateProfile(entryWithUpdates);
		}				
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-ProfileConnector.modEntry3: post updateProfile: " + doUpdate);
		}

		// new options on the connector to set state and manager field				
		if (_mark_manager) {
			updateManagerField(entryWithUpdates.getString(PeoplePagesServiceConstants.KEY), null);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-ProfileConnector.modEntry4: post mark manager: " + doUpdate);
		}

		if (_state == STATE_ACTIVATE) {
			_tdiProfileSvc.activateProfile(TDIServiceHelper.entryToDescriptor(entryWithUpdates));
		}
		else if (_state == STATE_INACTIVATE) {
			_tdiProfileSvc.inactivateProfile(entryWithUpdates.getString(PeoplePagesServiceConstants.KEY));
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-ProfileConnector.modEntry5: return: " + doUpdate);
		}

	}
	
	/**
	 * Used by the customize mode - MarkManager, Inactivate, Revoke
	 * update the photo entry into DB.
	 *
	 *@param The profile entry.
	 */
	//TODO how to deliver a profileEntry to this method?
	public Entry queryReply(Entry profileEntry) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-queryReply Method");
		}
		String connectorMode = ((ConnectorConfig)getConfiguration()).getMode();
		if((ConnectorModes.INACTIVATE).equals(connectorMode)) {
			logmsg(_mlp.getString("err_mode_deprecated", ConnectorModes.INACTIVATE));
			inactivateProfile(profileEntry);
		}
		
		else if((ConnectorModes.ACTIVATE).equals(connectorMode)) {
			logmsg(_mlp.getString("err_mode_deprecated", ConnectorModes.ACTIVATE));
			activateProfile(profileEntry);
		}
		
		return profileEntry;
	}
	
	/**
	 * Mark the is_manager field of this profile based on other users in db
	 * this mode has been replaced by a checkbox used with update mode
	 * 
	 * @param profileEntry
	 * @deprecated
	 */

	private void markManager(Entry profileEntry) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-markManager Method");
		}
		inputValidation(profileEntry,_mlp.getString("err_required_null", "entry"));
		String key = profileEntry.getString(PeoplePagesServiceConstants.KEY);
		updateManagerField(key, null);
	}
	
	/**
	 * Inactivate the profile
	 * this mode has been replaced by a checkbox used with update mode
	 * 
	 * @param profileEntry
	 * @deprecated
	 */

	private void inactivateProfile(Entry profileEntry) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-inactivateProfile Method, profileEntry=" + profileEntry);
		}
		inputValidation(profileEntry,_mlp.getString("err_required_null", "entry"));
		String profileKey =  profileEntry.getString(PeoplePagesServiceConstants.KEY);
		_tdiProfileSvc.inactivateProfile(profileKey);
	}

	/**
	 * Activate the profile
	 * this mode has been replaced by a checkbox used with update mode
	 * 
	 * @param profileEntry
	 * @deprecated
	 */

	private void activateProfile(Entry profileEntry) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-activateProfile Method");
		}
		inputValidation(profileEntry, _mlp.getString("err_required_null", "entry"));		
		_tdiProfileSvc.activateProfile(TDIServiceHelper.entryToDescriptor(profileEntry));
	}
	
	private Entry getProfileByKey(String key) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-getProfileForKey Method key=" + key);
		}
		final String profileKey = key;
		return TDICodeRunner.run(new ProfileCodeBlock<Entry>("err_method_findEntry",key)
		{
			public Entry run() throws RuntimeException, TDICodeBlockException 
			{
				Entry entry = null;
				AssertionUtils.assertNotNull(profileKey, AssertionType.PRECONDITION);
				AssertionUtils.assertNotEmpty(profileKey, AssertionType.PRECONDITION);
				ProfileDescriptor searchForProfileDescriptor = TDIConnectorHelper.getEmployeeByCriterial(_tdiProfileSvc, TDIProfileAttribute.KEY, profileKey);
				if(searchForProfileDescriptor!=null)
				{
					entry = TDIServiceHelper.descriptorToEntry(searchForProfileDescriptor);
				}
				return entry;
			}	
		});
		
		
	}
	
	private String createProfile(Entry profileEntry)throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-createProfile Method profileEntry=" +profileEntry);
		}
		String profileUID =  profileEntry.getString(PeoplePagesServiceConstants.UID);
		inputValidation(profileUID, _mlp.getString("err_required_null", "uid"));
		final Entry entryInDB = profileEntry;
		return TDICodeRunner.run(new ProfileCodeBlock<String>("err_method_findEntry",profileUID)
		{

			public String run() throws RuntimeException, TDICodeBlockException {
				String key = null;
				String dn = null;
				dn = entryInDB.getString(PeoplePagesServiceConstants.DN);
				if(dn == null)
					throw new TDICodeBlockException("dn is null.");
				key = _tdiProfileSvc.create(TDIServiceHelper.entryToDescriptor(entryInDB));
				return key;
			}	
		});
	}
	
	private void deleteProfile(String key) throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-deleteProfile Method key= " + key);
		}
		inputValidation(key, _mlp.getString("err_required_null", "key"));
		final String profileKey = key;
		TDICodeRunner.run(new ProfileCodeBlock<Object>("err_method_findEntry",key)
		{
			public Object run() throws RuntimeException, TDICodeBlockException 
			{
				_tdiProfileSvc.delete(profileKey);
				return null;
			}			
		});
	}
	
	private void updateProfile(Entry profileEntry)throws TDIException{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-updateProfile Method profileEntry=" + profileEntry);
		}	
		
		String dn = profileEntry.getString(PeoplePagesServiceConstants.DN);
		inputValidation(dn, _mlp.getString("err_required_null", "dn"));
		final Entry entryInDB = profileEntry;
		TDICodeRunner.run(new ProfileCodeBlock<Object>("err_method_updateProfile",dn)
		{
			public Object run() throws RuntimeException, TDICodeBlockException 
			{			
				_tdiProfileSvc.update(TDIServiceHelper.entryToDescriptor(entryInDB));
				return null;
			}
		});

	}
	
	private boolean updateManagerField(String key, final String[] valsFromIterator) throws TDIException 
	{
		if (LOG.isTraceEnabled()) {
			LOG.trace("Begin-updateManagerField Method key=" + key);
		}
		
		
		boolean isOldMarkManagerMode = false;  // note mark manager mode isn't used by config file.  should be depricated

		if (valsFromIterator == null)
			isOldMarkManagerMode = true;

		final boolean isMarkManagerConfigSetting = ! isOldMarkManagerMode;

		inputValidation(key, _mlp.getString("err_required_null", "key"));
		final String profileKey = key;

		return TDICodeRunner.run(new ProfileCodeBlock<Boolean>("err_method_updateProfile",key)
		{
			public Boolean run() throws RuntimeException,TDICodeBlockException 
			{
				boolean isManager = false;
				
				Entry currentEntry;
				try 
				{
					if (isMarkManagerConfigSetting)
					{
						// using newer appoach mode where has been replaced by a checkbox used with update mode
						TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
						c.setAttribute(TDIProfileSearchCriteria.TDIProfileAttribute.MANAGER_UID);
						c.setOperator(TDICriteriaOperator.EQUALS);
						c.setValue(valsFromIterator[UID_IX]);
						TDIProfileSearchOptions options = new TDIProfileSearchOptions();
						options.setSearchCriteria(Collections.singletonList(c));
						isManager = _tdiProfileSvc.count(options) > 0;
						
						String isManager_Current;
						if(isManager){
							isManager_Current="Y";
						}else{
							isManager_Current="N";
						}
						
						String isManager_old = valsFromIterator[0];
						boolean updateNeeded = false;
						if(isManager_old != null){
							if(!isManager_Current.equals(isManager_old)){
								updateNeeded = true;
							}
						}else{
							updateNeeded = true;
						}
						
						if(updateNeeded) {
							// we are creating a minimal entry with just what we need to be updated
							// because of what customers expect to see in the eventlog entry, 
							// more fields than necessary are included here
							Entry entry = new Entry();
							entry.addAttributeValue(PeoplePagesServiceConstants.IS_MANAGER, isManager_Current);
							entry.addAttributeValue(PeoplePagesServiceConstants.KEY, profileKey);
							entry.addAttributeValue(PeoplePagesServiceConstants.UID, valsFromIterator[UID_IX]);
							entry.addAttributeValue(PeoplePagesServiceConstants.DN, valsFromIterator[DIST_NAME_IX]);
							entry.addAttributeValue(PeoplePagesServiceConstants.GUID, valsFromIterator[GUID_IX]);
							entry.addAttributeValue(PeoplePagesServiceConstants.EMAIL, valsFromIterator[EMAIL_IX]);
							entry.addAttributeValue(PeoplePagesServiceConstants.DISPLAY_NAME, valsFromIterator[DISP_NAME_IX]);
							updateProfile(entry);
						}
					}
					else
					{
						// keep old code for mark manager mode, which is not used by us,
						currentEntry = getProfileByKey(profileKey);
						TDIProfileSearchCriteria c = new TDIProfileSearchCriteria();
						c.setAttribute(TDIProfileSearchCriteria.TDIProfileAttribute.MANAGER_UID);
						c.setOperator(TDICriteriaOperator.EQUALS);
						c.setValue(currentEntry.getString(PeoplePagesServiceConstants.UID));
						TDIProfileSearchOptions options = new TDIProfileSearchOptions();
						options.setSearchCriteria(Collections.singletonList(c));
						isManager = _tdiProfileSvc.count(options) > 0;
						
						String isManager_Current;
						if(isManager){
							isManager_Current="Y";
						}else{
							isManager_Current="N";
						}
						
						String isManager_old = currentEntry.getString(PeoplePagesServiceConstants.IS_MANAGER);
						boolean updateNeeded = false;
						if(isManager_old!=null){
							if(!isManager_Current.equals(isManager_old)){
								currentEntry.setAttribute(PeoplePagesServiceConstants.IS_MANAGER, isManager_Current);
								updateNeeded = true;
							}
						}else{
							currentEntry.addAttributeValue(PeoplePagesServiceConstants.IS_MANAGER, isManager_Current);
							updateNeeded = true;
						}
						
						if(updateNeeded) {
							updateProfile(currentEntry);
							// we are creating a minimal entry with just what we need to be updated
							// because of what customers expect to see in the eventlog entry, 
							// more fields than necessary are included here
							Entry entry = new Entry();
							entry.addAttributeValue(PeoplePagesServiceConstants.IS_MANAGER, isManager_Current);
							entry.addAttributeValue(PeoplePagesServiceConstants.KEY, profileKey);
							entry.addAttributeValue(PeoplePagesServiceConstants.UID, currentEntry.getString(PeoplePagesServiceConstants.UID));
							entry.addAttributeValue(PeoplePagesServiceConstants.DN, currentEntry.getString(PeoplePagesServiceConstants.DN));
							entry.addAttributeValue(PeoplePagesServiceConstants.GUID, currentEntry.getString(PeoplePagesServiceConstants.GUID));
							entry.addAttributeValue(PeoplePagesServiceConstants.EMAIL, currentEntry.getString(PeoplePagesServiceConstants.EMAIL));
							entry.addAttributeValue(PeoplePagesServiceConstants.DISPLAY_NAME, currentEntry.getString(PeoplePagesServiceConstants.DISPLAY_NAME));
							updateProfile(entry);
						}
					}
				}catch (TDIException e) {
					String errorMsg = e.getMessage();
					LOG.error(errorMsg);
					if (getLog() != null) {
						getLog().logerror(errorMsg, e);
					}
				}
				if (LOG.isTraceEnabled()) {
					LOG.trace("end-updateManagerField Method key=");
				}
				return isManager;
			}	
		});
	}

	private static final Set<String> IGNORE_KEYS = Collections.unmodifiableSet(new HashSet<String>(
			Arrays.asList(new String[]{"key", "lastUpdate", "sourceUrl"})
		));

	private boolean compareTwoProfile(ProfileDescriptor ldapDescriptor, ProfileDescriptor dbDescriptor) {
		final boolean FINEST = LOG.isTraceEnabled();
		
		boolean match = true;
		Employee ldapEmployee = ldapDescriptor.getProfile();
		Employee dbEmployee = dbDescriptor.getProfile();
		
		if (FINEST) {
			LOG.trace("COMPARE START (ldap/db) RECORDS: " + ldapEmployee.getUid() + "/" + dbEmployee.getUid()); 
		}
		
		/**
		 * First compare the Employee Obj
		 */
		for(ProfileAttributes.Attribute attr : ProfileAttributes.getAll()){
			final String key = attr.getAttrId();
			
			if (!IGNORE_KEYS.contains(key)) {
				Object ldVal = getNormAttrValue(attr, ldapEmployee, true);
				Object dbVal = getNormAttrValue(attr, dbEmployee, false);				
				
				if (ldVal == null) {
					/*
					 * in this case, there is no mapping for the LDAP attribute.
					 * We should ignore it for the purposes of matching
					 */
					continue;
				} else if (!ldVal.equals(dbVal)) {
					if (FINEST) {
						LOG.trace("COMPARE DIFF (ldap/db) {" + key + "}: " + ldVal + "/" + dbVal); 
					}
					
					match = false;
				}
			}		
			// Break from loop if no match
			if (!match) {
				break;
			}
		}
		
		//
		// Only perform comparison if no match
		//   checks givenNames, surnames & logins
		//
		if (match) {	
	
			// compares list and logs message
			match = compareNameLists(FINEST, "givenNames",
						NameHelper.getNamesForSource(ldapDescriptor.getGivenNames(), NameSource.SourceRepository),
						NameHelper.getNamesForSource(dbDescriptor.getGivenNames(), NameSource.SourceRepository));
			
			if (match /* givenNames */) {
				match = compareNameLists(FINEST, "surnames",
							NameHelper.getNamesForSource(ldapDescriptor.getSurnames(), NameSource.SourceRepository),
							NameHelper.getNamesForSource(dbDescriptor.getSurnames(), NameSource.SourceRepository));
				
				if (match /* surnames */) {
					match = compareNameLists(FINEST, "logins",
								ldapDescriptor.getLogins(),
								dbDescriptor.getLogins());
				}
			}
		}
		
		if (FINEST) {
			LOG.trace("COMPARE END [" + (match ? "MATCH" : "NO-MATCH") + "] (ldap/db) RECORDS: " + ldapEmployee.getUid() + "/" + dbEmployee.getUid()); 
		}
		
		return match;
	}
		
	/**
	 * Utility method to get the attribute value in a normalized form of the
	 * attribute value. All <null>'s are converted to "". In the case of an LDAP
	 * user, <null> is left <null> in order to indicate that the attribute is
	 * not mapped.
	 * 
	 * @param attr
	 * @param dbEmployee
	 * @param ldapUser
	 * @return
	 */
	private Object getNormAttrValue(Attribute attr, Employee userObj, boolean ldapUser) {
		Object value = null;
		
		if (attr.isExtension()) {
			ProfileExtension pe = (ProfileExtension) userObj.getProfileExtension(Employee.getExtensionIdForAttributeId(attr.getAttrId()), false);
	
			if (pe != null) {
				value = pe.getStringValue();
				
				// special case for LDAP user - PE is only non-null when there *is* an extension mapping
				if (ldapUser && value == null) {
					value = "";
				}
			}
		}
		else {
			value = userObj.get(attr.getAttrId());
		}
		
		if (value == null && !ldapUser)
			return "";
		
		return value;
	}
	
	/**
	 * Internal helper method with tracing to compare a list of names.
	 * Internally it will do a case-free comparison of the LDAP names.
	 * 
	 * @param FINEST
	 * @param listName
	 * @param ldapNameList
	 * @param dbNameList
	 * @return
	 */
	private final boolean compareNameLists(
			final boolean FINEST, 
			final String listName, 
			final List<String> ldapNameList, 
			final List<String> dbNameList) 
	{
		final boolean match = compareTwoNameArrays(dbNameList, ldapNameList);
		
		if (!match && FINEST) {
			LOG.trace("COMPARE DIFF (ldap/db) {" + listName + "}: " + ldapNameList + "/" + dbNameList);
		}
		
		return match;
	}

	/**
	 * Performs mechanics of name comparison
	 * 
	 * @param dbNameString
	 * @param ldapNameString
	 * @return
	 */
	private final boolean compareTwoNameArrays(List<String> dbNameString, List<String> ldapNameString) {
		boolean match = true;
		
		if(dbNameString.size()!=ldapNameString.size()){
			return false;
		}
		
		for(String name : ldapNameString){
			if(name!=null){
				name = name.toLowerCase();
				
				if(!dbNameString.contains(name)){
					return false;
				}
			}
		}
		
		return match;
	}


	public ExtensionAttributeConfig[] getExtensionArray(){
		Map<String,? extends ExtensionAttributeConfig> extensions = 
		    DMConfig.instance().getExtensionAttributeConfig();
		Object[] obj = extensions.values().toArray();
		ExtensionAttributeConfig[] extensArray = new ExtensionAttributeConfig[obj.length];
		for(int i=0;i<obj.length;i++){
			extensArray[i] = (ExtensionAttributeConfig) obj[i];
		}
		
		return extensArray;
	}	
	
	/**
	 * Reusable code block
	 */
	private abstract class ProfileCodeBlock<T> implements TDICodeBlock<T> {
		private String errMsg;
		private Object[] errMsgParams;
		
		public ProfileCodeBlock(String errMsg, Object...errMsgParams) {
			this.errMsg = errMsg;
			this.errMsgParams = errMsgParams;
		}		
		
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
		
		public T handleRecoverable(RuntimeException e)
				throws TDIException 
		{
			String formattedMsg = _mlp.getString(errMsg, errMsgParams);
			LOG.error(formattedMsg);
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
		return "ProfileConnector_4.0.0.0";
	}
	
	public void throwTDIException(String errorMsg) throws TDIException
	{
			TDIException e = new TDIException(errorMsg);
			if (getLog() != null) {
		        getLog().logerror(errorMsg, e);
		      }
			throw e;
	}
}
