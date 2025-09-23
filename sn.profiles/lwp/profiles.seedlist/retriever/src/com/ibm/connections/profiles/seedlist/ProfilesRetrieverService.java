/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.connections.profiles.seedlist;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import com.ibm.ilel.seedlist.SeedlistException;
import com.ibm.ilel.seedlist.common.ApplicationInfo;
import com.ibm.ilel.seedlist.common.EntrySet;
import com.ibm.ilel.seedlist.retriever.RetrieverRequest;
import com.ibm.ilel.seedlist.retriever.RetrieverService;
import com.ibm.ilel.seedlist.retriever.connections.profiles.ProfileState;
import com.ibm.lconn.profiles.data.IndexerProfileCollection;
import com.ibm.lconn.profiles.data.IndexerProfileDescriptor;
import com.ibm.lconn.profiles.data.IndexerSearchOptions;
import com.ibm.lconn.profiles.internal.constants.ProfilesIndexConstants;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ProfileService;
import com.ibm.peoplepages.data.Employee;

public class ProfilesRetrieverService implements RetrieverService {

	private static String seedlistIdRoot = ProfilesIndexConstants.PROFILES_SEEDLIST_ID_ROOT;
	private static final String CLASSNAME = ProfilesRetrieverService.class.getName();
	private static Logger logger = Logger.getLogger(CLASSNAME);

	private ProfileService pService = AppServiceContextAccess.getContextObject(ProfileService.class);

	private HttpServletRequest servletRequest;
	private boolean isPublic;

	public ProfilesRetrieverService(boolean isPublic, Properties properties, HttpServletRequest servletRequest) {
		final String method = "ProfilesRetrieverService";
		final boolean FINER = logger.isLoggable(Level.FINER);

		if (FINER) logger.entering(CLASSNAME, method);

		this.isPublic = isPublic;
		this.servletRequest = servletRequest;

		if (FINER)
			logger.finer("ProfilesRetriverService: httpServletRequestURL =  " + servletRequest.getRequestURL() + "?" + servletRequest.getQueryString());

		if (FINER) logger.exiting(CLASSNAME, method);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.ibm.lotus.search.providers.content.seedlist.retriever.RetrieverService#getNumberOfDocuments(com.ibm.lotus.search.providers.content
	 * .seedlist.common.ApplicationInfo, com.ibm.lotus.search.providers.content.seedlist.retriever.Request)
	 */
	public int getNumberOfDocuments(ApplicationInfo appInfo, RetrieverRequest request) throws SeedlistException
	{
		final String method = "getNumberOfDocument";
		final boolean FINER = logger.isLoggable(Level.FINER);
		final boolean FINEST= logger.isLoggable(Level.FINEST);

		int numberOfDocuments = 0;
		if (FINER) logger.entering(CLASSNAME, method);

		try {
			if (FINER) logger.exiting(CLASSNAME, method);
			IndexerSearchOptions options = getSearchOptions(appInfo, request);
			if (FINEST) logger.finest("Seedlist options : " + options.toString());

			numberOfDocuments = pService.countForIndexing(options);
		}
		catch (DataAccessRetrieveException e) {
			if (FINER) {
				logger.throwing(CLASSNAME, method, e);
			}
			throw new SeedlistException(SeedlistException.TYPE_ENTRY_DATA_ERROR, e.getMessage(), e);
		}
		if (FINER) logger.exiting(CLASSNAME, method, numberOfDocuments);
		return numberOfDocuments;
	}

	/**
	 * Utility method for consistent search options
	 * 
	 * @param appInfo
	 * @param request
	 * @return
	 */
	private final IndexerSearchOptions getSearchOptions(ApplicationInfo appInfo, RetrieverRequest request) {
		Timestamp lastModifiedDate = null;
		Timestamp until = null;
		String sinceKey = null;
		IndexerSearchOptions nextInfo = null;
		boolean initialIndex = true;

		if (request.getState() == null) {
			// start date
			if (request.getTimestamp() == null) {
				lastModifiedDate = new Timestamp(0);
				if (request.getDate() != null) {
					lastModifiedDate.setTime(request.getDate().getTime());
					initialIndex = false;
				}
			}
			else {
				lastModifiedDate = new Timestamp(ByteBuffer.wrap(request.getTimestamp().getStateData()).getLong());
				initialIndex = false;
			}
			// end date
			until = new Timestamp(System.currentTimeMillis());
		}
		else {
			ProfileState pState = new ProfileState(request.getState().asString(), request.getNumEntries());

			nextInfo = new IndexerSearchOptions(pState.getSince(), pState.getUntil(), pState.getSinceKey(), pState.getPageSize(),
					pState.isInitialIndex());

			until = nextInfo.getUntil();
			sinceKey = nextInfo.getSinceKey();
			lastModifiedDate = nextInfo.getSince(); // due to precision loss
		}

		return nextInfo == null ? new IndexerSearchOptions(lastModifiedDate, until, sinceKey, request.getNumEntries(), initialIndex)
				: nextInfo;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.ibm.lotus.search.providers.content.seedlist.retriever.RetrieverService#getDocuments(com.ibm.lotus.search.providers.content.seedlist
	 * .common.ApplicationInfo, com.ibm.lotus.search.providers.content.seedlist.retriever.Request)
	 */
	public EntrySet getDocuments(ApplicationInfo appInfo, RetrieverRequest request) throws SeedlistException {
		String method = "getDocuments";
		if (logger.isLoggable(Level.FINER))
			logger.entering(CLASSNAME, method);
		EntrySet children = getChildren(appInfo, request);
		if (logger.isLoggable(Level.FINER))
			logger.exiting(CLASSNAME, method);
		return children;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.ibm.lotus.search.providers.content.seedlist.retriever.RetrieverService#getChildren(com.ibm.lotus.search.providers.content.seedlist
	 * .common.ApplicationInfo, com.ibm.lotus.search.providers.content.seedlist.retriever.Request)
	 */
	public EntrySet getChildren(ApplicationInfo appInfo, RetrieverRequest request) throws SeedlistException {
		final String method = "getChildren";
		final boolean FINER = logger.isLoggable(Level.FINER);
		final boolean FINEST= logger.isLoggable(Level.FINEST);

		if (FINER) logger.entering(CLASSNAME, method);
		try {
			// set current locale
			Locale locale = request.getLocale();

			// portfolio documents
			EmployeeDocument[] documents = new EmployeeDocument[0];

			// seedlist id
			String seedlistId = request.getSeedlistId();

			// Search Options
			IndexerSearchOptions options = getSearchOptions(appInfo, request);
			if (FINEST) logger.finest("Seedlist options : " + options.toString());

			// State info for next request
			Map<String, IndexerSearchOptions> state = new HashMap<String, IndexerSearchOptions>(1); // holds state for next request

			// TODO: fix SeedlistId check
			if (seedlistId == null || seedlistId.length() == 0 || seedlistId.equals("SeedlistId")) {
				seedlistId = seedlistIdRoot;

				// Profiles documents
				// boolean hasCustom = hasCustomFields();
				documents = getProfilesDocuments(options, locale, request, state);

			}
			else {
				// Profiles document
				documents = getProfilesDocumentByUrl(seedlistId, locale);
				state.put("state", new IndexerSearchOptions(options.getUntil(), options.getUntil(), null, 0, options.isInitialIndex()));
			}

			if (FINER) logger.finer("ProfilesRetriverService: got documents size = " + documents.length);

			// entry set
			// defect 42463: seedlistIdRoot is used in the Profiles seedlist feed. It is always: PROFILES
			EmployeeEntrySet entrySet = new EmployeeEntrySet(Arrays.asList(documents), Collections.EMPTY_LIST, seedlistIdRoot,
					options.getUntil(), isPublic, request, servletRequest, state.get("state"));

			if (FINER) logger.exiting(CLASSNAME, method);

			return entrySet;
		}
		catch (Throwable t) {
			// invoked methods should be throwing SeedlidtException. if we get something else, it is an anomaly.
			// defect 52969
			if ((t instanceof SeedlistException) == false) {
				throw new SeedlistException(SeedlistException.TYPE_INTERNAL_ERROR, t.getMessage(), t);
			}
			else {
				throw (SeedlistException) t;
			}
		}
	}

	/**
	 * 
	 * @param options
	 * @param locale
	 * @param request
	 * @param state
	 * @return
	 * @throws SeedlistException
	 */
	private final EmployeeDocument[] getProfilesDocuments(IndexerSearchOptions options, Locale locale, RetrieverRequest request,
			Map<String, IndexerSearchOptions> state) throws SeedlistException
	{
		final String method  = "getProfilesDocuments";
		final boolean FINER  = logger.isLoggable(Level.FINER);
		final boolean FINEST = logger.isLoggable(Level.FINEST);

		if (FINER) logger.entering(CLASSNAME, method);

		List<EmployeeDocument> documents = new ArrayList<EmployeeDocument>();

		IndexerProfileCollection results;

		if (FINER) logger.finer(" getProfilesDocuments: state = " + state.get("state") + ", pageSize = " + options.getPageSize());

		try {
			// If pagesize is set to 0, then bypass all the service calls and return
			// empty result. This is to handle 'Range=0' parameter in the seedlist URL

			if (options.getPageSize() == 0)
				results = getEmptyIndexerProfileCollection();
			else
				results = pService.getForIndexing(options);

			// We would try to get all the deleted record in the first seedlist call
			if (state.get("state") == null) {

				if (FINER) logger.finer(" getting deleted records...");
			}
		}
		catch (Exception e) {
			if (FINER) {
				logger.throwing(CLASSNAME, method, e);
			}
			throw new SeedlistException(SeedlistException.TYPE_INTERNAL_ERROR, e.getMessage(), e);
		}

		/*
		 * if (results.getProfiles().size() > 1){ Employee lastEntry = results.getProfiles().get(results.getProfiles().size() -
		 * 1).getProfile(); Employee sndlastEntry = results.getProfiles().get(results.getProfiles().size() - 2).getProfile(); Date lastDate
		 * = lastEntry.getRecordUpdated(); Date sndlastDate = sndlastEntry.getRecordUpdated(); // SimpleDateFormat sdf = new
		 * SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" ) ; // String last = sdf.format ( lastDate ) ; // String sndlast =
		 * sdf.format(sndlastDate); request.setStartIndex(0); }
		 */
		// setup paging
		if (results.getNext() != null) {
			state.put("state", results.getNext());
		}
		else {
			state.put("state", new IndexerSearchOptions(options.getUntil(), options.getUntil(), null, 0, options.isInitialIndex()));
		}

		List<IndexerProfileDescriptor> descs = null;
		try {
			descs = results.getProfiles();
			if (FINEST) {
				logger.logp(Level.FINEST, CLASSNAME, method, " : processing " + descs.size() + " records");
			}
			int i = 1;
			for (IndexerProfileDescriptor desc : descs) {
				Employee profile = desc.getProfile();
				String displayName = profile.getDisplayName();
				if (FINEST) {
					logger.logp(Level.FINEST, CLASSNAME, "EmployeeDocument", "Processing [" + i + "] : " + profile.getEmail() + " : " + displayName + " : " + profile.getGuid() + " : " + profile.getUid());
				}
				if (displayName != null) {
					documents.add(new EmployeeDocument(desc, locale));
					if (FINEST) {
						logger.logp(Level.FINEST, CLASSNAME, method, " added [" + i + "] " + displayName);
					}
				}
				else {
					String message = "DisplayName is not set for user with GUID " + profile.getGuid() + " and UID " + profile.getUid();
					logger.logp(Level.FINER, CLASSNAME, "EmployeeDocument", message);
				}
				i++;
			}
		}
		catch (Exception e) {
			if (FINER) {
				logger.throwing(CLASSNAME, method, e);
			}
			if ((e instanceof SeedlistException) == false) {
				throw new SeedlistException(SeedlistException.TYPE_INTERNAL_ERROR, e.getMessage(), e);
			}
			else {
				throw (SeedlistException) e;
			}
		}
		EmployeeDocument[] retVal = documents.toArray(new EmployeeDocument[0]);
		if (FINER) logger.exiting(CLASSNAME, method, retVal.length);
		return retVal;
	}

	private final EmployeeDocument[] getProfilesDocumentByUrl(String seedlistId, Locale locale) throws SeedlistException
	{
		final String method = "getProfilesDocumentByUrl";
		final boolean FINER = logger.isLoggable(Level.FINER);
		final boolean FINEST = logger.isLoggable(Level.FINEST);

		if (FINER) logger.entering(CLASSNAME, method, seedlistId);
		List<EmployeeDocument> documents = new ArrayList<EmployeeDocument>();

		try {
			String uid = getUid(seedlistId);

			IndexerProfileDescriptor desc = null;

			desc = pService.getProfileForIndexing(uid);

			if (desc != null && desc.getProfile() != null) {
				documents.add(new EmployeeDocument(desc, locale));
				if (FINEST) {
					logger.logp(Level.FINEST, CLASSNAME, method, " added " + desc.getProfile().getDisplayName());
				}
			}

		}
		catch (Exception e) {
			if (FINER) {
				logger.throwing(CLASSNAME, method, e);
			}
			// We should allow invalid userId per instruction from search team.
			// If the userId is a bad one, we return empty seedlist entry
			// throw new SeedlistException(SeedlistException.TYPE_ENTRY_DATA_ERROR, e.getMessage(), e);
		}
		EmployeeDocument[] retVal = documents.toArray(new EmployeeDocument[0]);
		if (FINER) logger.exiting(CLASSNAME, method, retVal.length);
		return retVal;
	}

	private final Date getLastModifiedDate(RetrieverRequest request) throws SeedlistException
	{
		final String method = "getLastModifiedDate";
		final boolean FINER = logger.isLoggable(Level.FINER);

		if (FINER) logger.entering(CLASSNAME, method);
		Date date = request.getDate();
		if (date != null) {
			return request.getDate();
		}
		else { // if the date is not set
			if (FINER) logger.exiting(CLASSNAME, method);
			return new Date(0);
		}
	}

	private final String getUid(String url) throws SeedlistException {
		final String method = "getUid";
		final boolean FINER = logger.isLoggable(Level.FINER);

		if (FINER) logger.entering(CLASSNAME, method, url);

		try {

			// Since LC 3.5/4.0, we change/add the format like 'SeedlistId=92df9f40-8f0a-1028-8767-db07163b51b2'
			// In the old way, it is something like: SeedlistId=userid=92df9f40-8f0a-1028-8767-db07163b51b2
			// We support both now for backward compatible
			// Default to use the whole string of 'SeedlistId' as userId:
			String uid = url;

			if (url != null && url.indexOf('=') != -1) {
				StringTokenizer st = new StringTokenizer(url, "=");
				st.nextToken();
				uid = st.nextToken();
			}
			if (FINER) logger.exiting(CLASSNAME, method, uid);

			return uid;
		}
		catch (Exception e) {
			if (FINER) {
				logger.throwing(CLASSNAME, method, e);
			}

			throw new SeedlistException(SeedlistException.TYPE_PARAMETER_ERROR, e.getMessage(), e);
		}
	}

	/**
	 * A private method to return an IndexerProfileCollection to handle 'Range=0' parameter in the seedlist URL.
	 */
	private IndexerProfileCollection getEmptyIndexerProfileCollection() {
		IndexerProfileCollection res = new IndexerProfileCollection();
		List<IndexerProfileDescriptor> EMPTY_LIST = Collections.emptyList();
		res.setProfiles(EMPTY_LIST);
		res.setNext(null);

		return res;
	}
}
