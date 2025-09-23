/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.api.messageboard.adapters;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.EntityProviderResponseContext;
import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.EntryMessageCollectionEntityProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProviderConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorServiceConfig;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessageResultsCollection;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessageRetrievalOptions;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.annotation.CollectionAction;
import com.ibm.lconn.core.web.atom.annotation.CollectionActionMapping;
import com.ibm.lconn.core.web.atom.annotation.ModelAttribute;
import com.ibm.lconn.core.web.atom.annotation.ValueRequired;
import com.ibm.lconn.core.web.atom.util.AFEntityProviderResponseContext;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;

/**
 *
 *
 */
public class ProfilesSystemEntryMessageCollectionAdapter extends
		ProfilesBaseEntryMessageCollectionAdapter 
{	
	private static final Logger LOGGER = Logger.getLogger(ProfilesSystemEntryMessageCollectionAdapter.class.getName());
	
	private static final String CLASS_NAME = ProfilesSystemEntryMessageCollectionAdapter.class.getSimpleName();
	
	public ProfilesSystemEntryMessageCollectionAdapter(MessageVectorService service, MessageVectorServiceConfig config, MessageVectorProviderConfig providerConfig) {
		super(service, config, providerConfig);
	}
	
	@CollectionActionMapping(methodAction={CollectionAction.GetFeed})
	public ResponseContext doGetSystemEntries(
			LCRequestContext request,
			@ModelAttribute(name=MODEL_TITLE) @ValueRequired String title,
			@ModelAttribute(name=MODEL_RESOURCE_IDS) List<String> resourceIds,
			@ModelAttribute(name=MODEL_VECTOR_TYPE) @ValueRequired String vectorType,
			@ModelAttribute(name=MODEL_ENTRY_OPTIONS) @ValueRequired EntryMessageRetrievalOptions options)
	{
		if(LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doGetSystemEntries", "vector type="+vectorType+", pageSize="+options.getPageSize()+", sinceEntryId="+
				options.getSinceEntryId()+", since="+options.getSince()+", sortMethod="+options.getSortMethod()+", sortOrder="+options.getSortOrder());
	
		if (resourceIds != null && resourceIds.size() > 0)
			return doGetMulti(request, resourceIds, title, vectorType, options);
		
		EntryMessageResultsCollection results = service.getEntryMessagesForSystem(vectorType, options);
		EntryMessageCollectionEntityProvider collectionProvider = new EntryMessageCollectionEntityProvider(request, options, results, title);
		EntityProviderResponseContext response = new AFEntityProviderResponseContext(collectionProvider, request.getAbdera(), LCAtomConstants.CHARENC_UTF8);	
		response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
		response.setStatus(LCAtomConstants.SC_OK);
		return response;
	}
	
	@CollectionActionMapping(methodAction={CollectionAction.PostEntry})
	public ResponseContext doGetMulti(
			LCRequestContext request,
			@ModelAttribute(name=MODEL_RESOURCE_IDS) @ValueRequired List<String> resourceIds,
			@ModelAttribute(name=MODEL_TITLE) @ValueRequired String title,
			@ModelAttribute(name=MODEL_VECTOR_TYPE) @ValueRequired String vectorType,
			@ModelAttribute(name=MODEL_ENTRY_OPTIONS) @ValueRequired EntryMessageRetrievalOptions options)
	{		
		StringBuilder userIdsString = new StringBuilder();
		//TODO NEWS message vector EJB needs users external ids. Instead of changing it in Adapters, convert user's internal ids to external ids in Filter (ProfileLookupKeySetLCFilter.java)
		for(String id : resourceIds){
			userIdsString.append(id);
			userIdsString.append("|");
		}
		
		if(LOGGER.isLoggable(Level.FINER))
			LOGGER.logp(Level.FINER, CLASS_NAME, "doGetSystemEntries", "vector type="+vectorType+", user external ids="+userIdsString.toString()+", pageSize="+options.getPageSize()+", sinceEntryId="+
				options.getSinceEntryId()+", since="+options.getSince()+", sortMethod="+options.getSortMethod()+", sortOrder="+options.getSortOrder());
		
		EntryMessageResultsCollection results = service.getEntryMessages(resourceIds, vectorType, options);
		EntryMessageCollectionEntityProvider collectionProvider = new EntryMessageCollectionEntityProvider(request, options, results, title);
		EntityProviderResponseContext response = new AFEntityProviderResponseContext(collectionProvider, request.getAbdera(), LCAtomConstants.CHARENC_UTF8);	
		response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
		response.setStatus(LCAtomConstants.SC_OK);
		return response;
	}

}
