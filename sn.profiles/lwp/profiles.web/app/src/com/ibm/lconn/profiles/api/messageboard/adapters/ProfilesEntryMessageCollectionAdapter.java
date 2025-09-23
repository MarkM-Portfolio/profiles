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
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.EntityProviderResponseContext;
import com.ibm.lconn.core.appext.msgvector.api.MessageVectorService;
import com.ibm.lconn.core.appext.msgvector.atom.api.EntryMessageCollectionEntityProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.EntryMessageEntityProvider;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageParser;
import com.ibm.lconn.core.appext.msgvector.atom.api.MessageVectorProviderConfig;
import com.ibm.lconn.core.appext.msgvector.config.MessageVectorServiceConfig;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessage;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessageResultsCollection;
import com.ibm.lconn.core.appext.msgvector.data.EntryMessageRetrievalOptions;
import com.ibm.lconn.core.appext.msgvector.data.MessageVector;
import com.ibm.lconn.core.web.atom.LCRequestContext;
import com.ibm.lconn.core.web.atom.annotation.CollectionAction;
import com.ibm.lconn.core.web.atom.annotation.CollectionActionMapping;
import com.ibm.lconn.core.web.atom.annotation.ModelAttribute;
import com.ibm.lconn.core.web.atom.annotation.RequestParameter;
import com.ibm.lconn.core.web.atom.annotation.ValueRequired;
import com.ibm.lconn.core.web.atom.util.AFEntityProviderResponseContext;
import com.ibm.lconn.core.web.atom.util.LCAtomConstants;
import com.ibm.lconn.core.web.atom.util.LCResponseUtils;

/**
 *
 * 
 */
public class ProfilesEntryMessageCollectionAdapter extends ProfilesBaseEntryMessageCollectionAdapter {

	public static final String PARAM_ENTRY_ID = "entryId";

	public static final String MODEL_ENTRY = "entryMessage";

	private static final Logger LOGGER = Logger.getLogger(ProfilesEntryMessageCollectionAdapter.class.getName());

	private static final String CLASS_NAME = ProfilesEntryMessageCollectionAdapter.class.getSimpleName();

	public ProfilesEntryMessageCollectionAdapter(MessageVectorService service, MessageVectorServiceConfig config,
			MessageVectorProviderConfig providerConfig) {
		super(service, config, providerConfig);
	}

	/**
	 * Gets a feed
	 * 
	 * @param requestContext
	 * @param assocResourceId
	 * @param vectorType
	 * @param options
	 * @return
	 * @throws Exception
	 */
	@CollectionActionMapping(methodAction = CollectionAction.GetFeed)
	public ResponseContext doGetEntries(LCRequestContext requestContext, @ModelAttribute(name = MODEL_TITLE) @ValueRequired String title,
			@ModelAttribute(name = MODEL_RESOURCE_ID) @ValueRequired String assocResourceId,
			@ModelAttribute(name = MODEL_RESOURCE_IDS) List<String> resourceIds,
			@ModelAttribute(name = MODEL_VECTOR_TYPE) @ValueRequired String vectorType,
			@ModelAttribute(name = MODEL_ENTRY_OPTIONS) @ValueRequired EntryMessageRetrievalOptions options) throws Exception {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doGetEntries", "title=" + title + ", assocResourceId=" + assocResourceId + ", resourceIds="
					+ resourceIds + ", vector type=" + vectorType);

		String resourceIdentifier = null;
		if (null != resourceIds && null != resourceIds.get(0)) {
			resourceIdentifier = resourceIds.get(0);
		}
		else {
			// RTC 73856 : current user's GUID, passed via assocResourceId
			resourceIdentifier = assocResourceId;
		}

		if (LOGGER.isLoggable(Level.FINER)) LOGGER.log(Level.FINER, "resourceIdentifier=" + resourceIdentifier);

		EntryMessageResultsCollection results = service.getEntryMessages(resourceIdentifier, vectorType, options);
		EntryMessageCollectionEntityProvider collectionProvider = new EntryMessageCollectionEntityProvider(requestContext, options,
				results, title);
		EntityProviderResponseContext response = new AFEntityProviderResponseContext(collectionProvider, requestContext.getAbdera(),
				LCAtomConstants.CHARENC_UTF8);
		response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
		response.setStatus(LCAtomConstants.SC_OK);
		return response;
	}

	/**
	 * Gets an entry
	 * 
	 * @param requestContext
	 * @param entry
	 * @return
	 */
	@CollectionActionMapping(methodAction = CollectionAction.GetEntry)
	// rtc defect 99834: if we get no value (null) send a 404.
	public ResponseContext doGetEntry(LCRequestContext requestContext, @ModelAttribute(name = MODEL_ENTRY) EntryMessage entry) {
		ResponseContext response;
		if (entry != null) {
			EntryMessageEntityProvider entryEntity = new EntryMessageEntityProvider(requestContext, entry);
			entryEntity.setWriteStartEndDoc(true);
			response = new AFEntityProviderResponseContext(entryEntity, requestContext.getAbdera(), LCAtomConstants.CHARENC_UTF8);
			response.setContentType(LCAtomConstants.MIME_ATOM, LCAtomConstants.CHARENC_UTF8);
			response.setStatus(LCAtomConstants.SC_OK);
		}
		else {
			response = LCResponseUtils.emptyAtomResponse(LCAtomConstants.SC_NOT_FOUND);
		}
		return response;
	}

	/**
	 * Deletes an entry
	 * 
	 * @param entryId
	 * @return
	 * @throws Exception
	 */
	@CollectionActionMapping(methodAction = CollectionAction.DeleteEntry)
	public ResponseContext doDeleteEntry(@RequestParameter(name = PARAM_ENTRY_ID) @ValueRequired String entryId) {
		if (LOGGER.isLoggable(Level.FINER)) LOGGER.entering(CLASS_NAME, "doDeleteEntry", "entry id=" + entryId);
		service.deleteEntryMessage(entryId);
		return LCResponseUtils.emptyAtomResponse();
	}

	/**
	 * 
	 * @param requestContext
	 * @param messageVector
	 * @return
	 * @throws Exception
	 */
	@CollectionActionMapping(methodAction = CollectionAction.PostEntry)
	public ResponseContext doCreateEntry(LCRequestContext requestContext, @ModelAttribute(name = MODEL_VECTOR) MessageVector messageVector)
			throws Exception {
		if (LOGGER.isLoggable(Level.FINER))
			LOGGER.entering(CLASS_NAME, "doCreateEntry", "message vector id=" + messageVector.getVectorId());
		Document<Entry> entryDoc = requestContext.getDocument();
		EntryMessage entryMsg = MessageParser.parseEntryMessage(entryDoc.getRoot());
		entryMsg.setVectorId(messageVector.getVectorId());
		entryMsg.setEntryId(service.createEntryMessage(entryMsg));
		ResponseContext response = LCResponseUtils.emptyAtomResponse(LCAtomConstants.SC_CREATED);
		response.setHeader(LCAtomConstants.HEADER_LOCATION, requestContext.getProvider().urlFor(requestContext, entryMsg, null));
		return response;
	}

	/**
	 * 
	 * @param requestContext
	 * @param oldEntry
	 * @return
	 * @throws Exception
	 */
	@CollectionActionMapping(methodAction = CollectionAction.PutEntry)
	public ResponseContext putEntry(LCRequestContext requestContext,
			@ModelAttribute(name = MODEL_ENTRY) @ValueRequired EntryMessage oldEntry) throws Exception {
		if (LOGGER.isLoggable(Level.FINER)) LOGGER.entering(CLASS_NAME, "putEntry", "entry id=" + oldEntry.getEntryId());
		Document<Entry> entryDoc = requestContext.getDocument();
		EntryMessage entryMsg = MessageParser.parseEntryMessage(entryDoc.getRoot());
		entryMsg.setEntryId(oldEntry.getEntryId());
		service.updateEntryMessage(entryMsg);
		return LCResponseUtils.emptyAtomResponse();
	}

	@ModelAttribute(name = MODEL_ENTRY)
	public EntryMessage modelEntryMessage(@RequestParameter(name = PARAM_ENTRY_ID) @ValueRequired String entryId) {
		if (LOGGER.isLoggable(Level.FINER)) LOGGER.entering(CLASS_NAME, "modelEntryMessage", "entry id=" + entryId);
		EntryMessage rtnVal = service.getEntryMessage(entryId);
		return rtnVal;
	}

}
