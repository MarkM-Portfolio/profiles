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
import com.ibm.lconn.profiles.data.MVConnectionOptions;

/**
 *
 *
 */
public class ProfilesRelatedEntryMessageCollectionAdapter extends
		ProfilesBaseEntryMessageCollectionAdapter 
{
	public static final String MODEL_RELATED_OPTIONS = "relatedOptions";

	private static final Logger LOGGER = Logger.getLogger(ProfilesRelatedEntryMessageCollectionAdapter.class.getName());
	
	private static final String CLASS_NAME = ProfilesRelatedEntryMessageCollectionAdapter.class.getSimpleName();
	
	public ProfilesRelatedEntryMessageCollectionAdapter(MessageVectorService service, MessageVectorServiceConfig config, MessageVectorProviderConfig providerConfig) {
		super(service, config, providerConfig);
	}
	
	@CollectionActionMapping(methodAction=CollectionAction.GetFeed)
	public ResponseContext doGetEntries(
			LCRequestContext requestContext,
			@ModelAttribute(name=MODEL_TITLE) @ValueRequired String title,
			@ModelAttribute(name=MODEL_VECTOR_TYPE) @ValueRequired String vectorType,
			@ModelAttribute(name=MODEL_ENTRY_OPTIONS) @ValueRequired EntryMessageRetrievalOptions options,
			@ModelAttribute(name=MODEL_RESOURCE_IDS) List<String> resourceIds,
			@ModelAttribute(name=MODEL_RELATED_OPTIONS) @ValueRequired Object relatedOptions)
		throws Exception 
	{
		if(LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "vector type="+vectorType+", user external id="+resourceIds.get(0)+", pageSize="+options.getPageSize()+", sinceEntryId="+
				options.getSinceEntryId()+", since="+options.getSince()+", sortMethod="+options.getSortMethod()+", sortOrder="+options.getSortOrder());
		
		MVConnectionOptions relatedopts = (MVConnectionOptions) relatedOptions;
		relatedopts.setSourceKey(resourceIds.get(0));
		// defect 73752. possible serialization issue with MVConnectionsOptions. use a Map.
		EntryMessageResultsCollection results = service.getEntryMessagesForRelated(vectorType, options, relatedopts.toMap());
		EntryMessageCollectionEntityProvider collectionProvider = new EntryMessageCollectionEntityProvider(requestContext, options, results, title);
		EntityProviderResponseContext response = new AFEntityProviderResponseContext(collectionProvider, requestContext.getAbdera(), LCAtomConstants.CHARENC_UTF8);	
		response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
		response.setStatus(LCAtomConstants.SC_OK);
		return response;
	}
}
