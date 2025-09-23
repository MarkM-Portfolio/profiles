/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2008, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.lconn.core.appext.annotations.SNAXTransactionManager;
import com.ibm.lconn.core.appext.api.SNAXContextVariable;
import com.ibm.lconn.core.appext.spi.SNAXAppContextAccess.ContextScope;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.GraphEnum;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.MessageAclEnum;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.NodeOfCreator;
import com.ibm.lconn.profiles.config.dm.ConnectionTypeConfig.WorkflowEnum;
import com.ibm.lconn.profiles.config.dm.DMConfig;

import com.ibm.lconn.profiles.policy.Acl;
import com.ibm.lconn.profiles.policy.Feature;

import com.ibm.lconn.profiles.internal.policy.PolicyHelper;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.exception.ConnectionExistsException;
import com.ibm.lconn.profiles.internal.exception.DataAccessCreateException;
import com.ibm.lconn.profiles.internal.exception.DataAccessException;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.exception.ProfilesRuntimeException;

import com.ibm.lconn.profiles.internal.service.cache.MethodCallCache;
import com.ibm.lconn.profiles.internal.service.store.interfaces.ConnectionDao;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.EventLogHelper;
import com.ibm.lconn.profiles.internal.util.SocialContactsHelper;
import com.ibm.lconn.profiles.internal.util.SocialContactsHelper.SocialContactsAction;
import com.ibm.lconn.profiles.resources.SvcApiRes;

import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.EventLogEntry;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.internal.service.notifications.NotificationUtil;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/**
 * Implements business logic for the management of Connection objects per the configuration.
 */
@Service(ConnectionService.SVCNAME)
public class ConnectionServiceImpl extends AbstractProfilesService implements ConnectionService
{
  private static final Logger logger = Logger.getLogger(ConnectionServiceImpl.class.getName(), SvcApiRes.BUNDLE);

	private static final List<Connection> EMPTY_CONN_LIST = Collections.emptyList();

	private SNAXContextVariable<Map<String, Object>> connCache = 
			new SNAXContextVariable<Map<String, Object>>(ContextScope.REQUEST) {
				protected Map<String, Object> initialize() { 
							return new HashMap<String,Object>(6);
		}
	};

	@Autowired private PeoplePagesService pps;
	@Autowired private ConnectionDao connectionDao;
	@Autowired private ProfileServiceBase profSvc;

	@Autowired
	public ConnectionServiceImpl(@SNAXTransactionManager PlatformTransactionManager txManager)
	{
		super(txManager);
	}

	/**
	 * Main method for connection creation...
	 * 
	 * @param connection
	 * @return
	 * @throws DataAccessException
	 * @throws ConnectionExistsException
	 * @throws EmailException 
	 * @throws ProfilesRuntimeException
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public String createConnection(Connection connection)
			throws DataAccessException, ConnectionExistsException, ProfilesRuntimeException, EmailException
	{
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "enter createConnection(connection)", connection);
		}

		// the two users that we eventually link together
		Employee sourceUser = null;
		Employee targetUser = null;

		// the user who is initiating the connection and who is on the other end
		Employee initiatingUser = null;
		Employee receivingUser = null;

		// validate we have proper input at this point before proceeding further
		AssertionUtils.assertNotNull(connection);
		AssertionUtils.assertNotEmpty(connection.getType());
		AssertionUtils.assertNotEmpty(connection.getSourceKey());
		AssertionUtils.assertNotEmpty(connection.getTargetKey());

		// get the connection type configuration information and ensure the user is attempting to create a type that is supported
		ConnectionTypeConfig connectionTypeConfig = ProfilesConfig.instance().getDMConfig().getConnectionTypeConfigs().get(connection.getType());
		AssertionUtils.assertNotNull(connectionTypeConfig);

		// get the user and ensure they are authenticated
		Employee currentUser = AppContextAccess.getCurrentUserProfile();		
		AssertionUtils.assertNotNull(currentUser, AssertionType.UNAUTHORIZED_ACTION);

		// if the user is an admin, then that user can basically do anything
//		boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
		boolean isAdmin = AppContextAccess.isUserAnAdmin();

		// validate that the connection is not linking the same user on both ends
		AssertionUtils.assertTrue(!connection.getSourceKey().equals(connection.getTargetKey()));

		// validate the creator is making themself a proper node in the connection link
		NodeOfCreator nodeOfCreator = connectionTypeConfig.getNodeOfCreator();
		if (!isAdmin) {
			if (NodeOfCreator.SOURCE.equals(nodeOfCreator)) {
				AssertionUtils.assertEquals(currentUser.getKey(), connection.getSourceKey());
				sourceUser = currentUser;
			} else if (NodeOfCreator.TARGET.equals(nodeOfCreator)) {
				AssertionUtils.assertEquals(currentUser.getKey(), connection.getTargetKey());
				targetUser = currentUser;
			}
		}

		ProfileRetrievalOptions pro = new ProfileRetrievalOptions(ProfileRetrievalOptions.Verbosity.LITE);
		if (sourceUser == null) {
			sourceUser = pps.getProfile(ProfileLookupKey.forKey(connection.getSourceKey()), pro);
		}
		if (targetUser == null) {
			targetUser = pps.getProfile(ProfileLookupKey.forKey(connection.getTargetKey()), pro);
		}

		if (NodeOfCreator.SOURCE.equals(nodeOfCreator)) {
			initiatingUser = sourceUser;
			receivingUser  = targetUser;
		} else {
			initiatingUser = targetUser;
			receivingUser  = sourceUser;
		}

		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "initiating user - " + initiatingUser.getDisplayName());
			logger.log(Level.FINEST, "receiving user - " + receivingUser.getDisplayName());
		}

		AssertionUtils.assertNotNull(sourceUser);
		AssertionUtils.assertNotNull(targetUser);
		AssertionUtils.assertNotNull(initiatingUser);
		AssertionUtils.assertNotNull(receivingUser);

		// acl check
		PolicyHelper.assertAcl(Acl.CONNECTION_VIEW, initiatingUser.getKey());
		WorkflowEnum workflow = connectionTypeConfig.getWorkflow();
		if (WorkflowEnum.CONFIRMED.equals(workflow)) {
			PolicyHelper.assertAcl(Acl.COLLEAGUE_CONNECT, receivingUser.getKey(), initiatingUser.getKey());    	
		}   

		// If the connection already exists abort
		if (_doesConnectionExist(connection))
		{
			throw new ConnectionExistsException();
		}

		// if the connection is bidirectional, we need 2 objects
		Connection otherConnection = null;
		GraphEnum graph = connectionTypeConfig.getGraph();
		if (GraphEnum.BIDIRECTIONAL.equals(graph))
		{
			otherConnection = new Connection();
			otherConnection.setSourceKey(connection.getTargetKey());
			otherConnection.setTargetKey(connection.getSourceKey());
			otherConnection.setType(connection.getType());
			otherConnection.setStatus(Connection.StatusType.ACCEPTED);
		}

		// handle work-flow
		if (WorkflowEnum.CONFIRMED.equals(workflow)) {

			// only an admin can create circumvent workflow
			if (!isAdmin) {
				AssertionUtils.assertTrue(connection.getStatus() == Connection.StatusType.PENDING);
			}

			// set the status on the link in the reverse direction to unconfirmed, unless the admin has override to auto-accept, then just set as accept as well
			if (otherConnection != null) {
				int statusOther = connection.getStatus() == Connection.StatusType.PENDING ? Connection.StatusType.UNCONFIRMED : Connection.StatusType.ACCEPTED;
				otherConnection.setStatus(statusOther);
			}
		}

		// clear the cache
		clearConnCache();

		// rtc 176130 - connection table has its own lastupdate column.
		// on cloud, connections are shwon through contacts. for performance, we will not
		// update the profile lastupdate and force a re-index connection creation is not
		// relevant to seedlist data. we do use this update on-prem for the my network
		// views
		if ( LCConfig.instance().isLotusLive() == false){
			profSvc.touchProfile(connection.getSourceKey());
			profSvc.touchProfile(connection.getTargetKey());
		}

		// create the connections
		Date now = new Date();
		String connectionId = _createConnectionObj(connection, initiatingUser, now);
		String otherConnectionId = null;
		if (otherConnection != null) {
			otherConnectionId = _createConnectionObj(otherConnection, initiatingUser, now);
		}

		if (SocialContactsHelper.isServiceAvailable()) {
			// Attempt to notify Social Contacts
			SocialContactsAction scAction = SocialContactsAction.REQUEST;
			// save the connection in case we need to restore it
			String savedConnectionId = connectionId;
			try {
				// save target user ID so we can sync with Social Contacts
				String targetID  = receivingUser.getUserid();
				String userEmail = initiatingUser.getEmail();
				SocialContactsHelper.syncWithSocialContacts(connectionId, targetID, userEmail, scAction);
			}
			catch (Exception ex) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.SEVERE, "Sync with Social Contacts : createConnection("+connectionId+") FAILED");
				}
				// reset the state in Profile db
// TODO			deleteConnection(savedConnectionId, false); // delete without sending compliance events
				// make a list of ids we need to remove
				List<String> idsToDelete = new ArrayList<String>(2);
				idsToDelete.add(connectionId);
				if (otherConnectionId != null) {
					idsToDelete.add(otherConnectionId);
				}
				String[] finalList = new String[idsToDelete.size()];
				idsToDelete.toArray(finalList);			
				connectionDao.deleteById(finalList);
				throw new DataAccessCreateException( ex );
			}
		}

		// Attempt to send notifications
		if (NotificationUtil.isEnabled()) {
			String notificationType = connectionTypeConfig.getNotificationType();
			if (notificationType != null && notificationType.length() > 0) {

				if (logger.isLoggable(Level.FINEST)) {
					logger.log(Level.FINEST, "create connection notification will be constructed and sent to notificationType=" + notificationType);
				}

				// who sends, who receives
				try {
					NotificationUtil.sendMessage(initiatingUser, receivingUser, notificationType, connection.getMessage(), AppContextAccess.getContext().getCurrentUserLocale());
				}
				catch (Exception e) {
					// RTC 87576 at the very least log exceptions thrown by
					// notifications
					if (logger.isLoggable(Level.FINEST)) {
						logger.log(Level.FINEST, "", e);
					}
					else if (logger.isLoggable(Level.SEVERE)) {
						logger.log(Level.SEVERE, "", e);
					}    			
				}
			}    	
		}

		// Attempt to send events
		if (!connectionTypeConfig.isExtension()) {
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, initiatingUser.getKey(), receivingUser.getKey(), EventLogEntry.Event.CONNECTION_CREATED );
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.BRIEF_DESC, connection.getMessage());
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.CONNECTION_ID, connectionId);
			eventLogSvc.insert( eventLogEntry );
		}

		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "exit createConnection(connection)", connectionId);
		}

		return connectionId;
	}

	/**
	 * Internal method for creating the Connection object.
	 * 
	 * @param connection
	 * @param currentUser
	 * @param now
	 * @return
	 * @throws ProfilesRuntimeException
	 */
	private final String _createConnectionObj(Connection connection, Employee currentUser, Date now) throws ProfilesRuntimeException
	{
		String connectionId = java.util.UUID.randomUUID().toString();

		connection.setConnectionId(connectionId);
		connection.setCreated(now);
		connection.setLastMod(now);
		connection.setCreatedByKey(currentUser.getKey());
		connection.setLastModByKey(currentUser.getKey());
		connection.setTenantKey(currentUser.getTenantKey());

		connectionDao.create(connection);

		return connectionId;
	}

	/**
	 * Main deletion method.
	 * 
	 * DIRECTIONAL: user must be source or admin to delete
	 * BIDIRECTIONAL: user must be source or admin on one of the edges to delete
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteConnection(String connectionId) throws DataAccessException 
	{
		deleteConnection(connectionId, true); // 
	}
	@Transactional(propagation=Propagation.REQUIRED)
	public void deleteConnection(String connectionId, boolean isRealDelete) throws DataAccessException 
	{
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "enter deleteConnection(connectionId)", connectionId);
		}

		// get the user and ensure they are authenticated
		Employee currentUser = AppContextAccess.getCurrentUserProfile();		
		AssertionUtils.assertNotNull(currentUser, AssertionType.UNAUTHORIZED_ACTION);

		// keep a list of ids we need to remove
		List<String> idsToDelete = new ArrayList<String>(2);
		idsToDelete.add(connectionId);

		// get the connection
		Connection connection = getConnection(connectionId, false, false);  // DO - enforce full read rights to connection
		AssertionUtils.assertNotNull(connection);

		// find out the nature of the connection
		String sourceKey = connection.getSourceKey();
		String targetKey = connection.getTargetKey();

		ConnectionTypeConfig ctc = _getConnectionTypeConfig(connection);
		Connection otherConnection = null;
		if (GraphEnum.BIDIRECTIONAL.equals(ctc.getGraph())) {
			// Skip read access
			otherConnection = connectionDao.getBySourceTargetType(
					targetKey, sourceKey, connection.getType(), false);
			AssertionUtils.assertNotNull(otherConnection);

			idsToDelete.add(otherConnection.getConnectionId());
		}

		// assert that the user can actually remove the connection
//		boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
		boolean isAdmin = AppContextAccess.isUserAnAdmin();

		boolean canDeleteConnection = false;
		if (isAdmin) {
			canDeleteConnection = true;
		}
		if (currentUser.getKey().equals(connection.getSourceKey())) {
			canDeleteConnection = true;
		}
		if (GraphEnum.BIDIRECTIONAL.equals(ctc.getGraph())) {
			if (otherConnection.getSourceKey().equals(currentUser.getKey())) {
				canDeleteConnection = true;
			}
		}

		AssertionUtils.assertTrue (canDeleteConnection, AssertionType.UNAUTHORIZED_ACTION);

		profSvc.touchProfile(sourceKey);
		profSvc.touchProfile(targetKey);

		// save source / target user ID before record is deleted so we can sync with Social Contacts
		Employee targetUser = pps.getProfile(ProfileLookupKey.forKey(targetKey), ProfileRetrievalOptions.MINIMUM);
		String   targetID   = targetUser.getUserid();
		Employee sourceUser = pps.getProfile(ProfileLookupKey.forKey(sourceKey), ProfileRetrievalOptions.MINIMUM);
		String   sourceID   = sourceUser.getUserid();
		// save the connection in case we need to restore it
		Connection savedConnection = getConnection(connectionId, true, true);

		String[] finalList = new String[idsToDelete.size()];
		idsToDelete.toArray(finalList);			
		connectionDao.deleteById(finalList);

		// is this a "real" delete operation as opposed to an "undo" after failing to notify Social Contacts during createConnection
		// if "real", then advise Social Contacts about the delete;
		// if not, then no need to tell Social Contacts or to send compliance events about the "undo" delete
		if (isRealDelete) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, currentUser.getUid() + " is removing connection " + connectionId + " between " + sourceID + " and " + targetID);
			}
			if (SocialContactsHelper.isServiceAvailable()) {
				//
				// Attempt to notify Social Contacts
				//
				SocialContactsAction scAction = SocialContactsAction.REMOVE;
				String syncID = sourceID;
				String userEmail = targetUser.getEmail();
				// Fix up ID & 'onBehalfOf' email, SC Contacts will not allow you to remove a connection to yourself (HTTP 405)
				if (currentUser.getKey().equals(connection.getSourceKey())) {
					syncID = targetID;
					userEmail = sourceUser.getEmail();
				}
				try {
					SocialContactsHelper.syncWithSocialContacts(connectionId, syncID, userEmail, scAction);
				}
				catch (Exception ex) {
					if (logger.isLoggable(Level.INFO)) {
						logger.log(Level.SEVERE, "Sync with Social Contacts : deleteConnection(" + connectionId + ") FAILED");
					}
					// reset the state in Profile db
// TODO				createConnection(savedConnection);
					restoreConnections(savedConnection, otherConnection);
					throw new DataAccessCreateException( ex );
				}
			}

			//
			// Attempt to send events
			// 
			if (!ctc.isExtension()) {
				EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
				EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, connection.getSourceKey(), connection.getTargetKey(), EventLogEntry.Event.CONNECTION_REJECTED );
				eventLogEntry.setProperty(EventLogEntry.PROPERTY.CONNECTION_ID, connectionId);				
				eventLogSvc.insert( eventLogEntry );
			}
		}
		// clear cache
		clearConnCache();

		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "exit deleteConnection(connectionId)", connectionId);
		}
	}

	private void restoreConnections(Connection savedConnection, Connection otherConnection)
	{
		// If the connection already exists abort
		if ((_doesConnectionExist(savedConnection)) || (_doesConnectionExist(otherConnection)))
		{
			throw new ConnectionExistsException();
		}

		// clear the cache
		clearConnCache();

		// create the connections
		connectionDao.create(savedConnection);
		// if the connection is bidirectional, we need to recreate both objects
		if (null != otherConnection) {
			connectionDao.create(otherConnection);
		}
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Connection getConnection(String connectionId, boolean inclMessage, boolean inclProfiles) throws DataAccessException 
	{
		Connection connection = connectionDao.getById(connectionId, inclMessage);
		_assertConnectionAccess(connection, inclMessage);
		_addProfilesToConnection(connection, inclProfiles);
		return connection;
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Connection getConnection(
			final String sourceKey, 
			final String targetKey,
			final String type,
			final boolean inclMessage,
			final boolean inclProfiles) throws DataAccessException 
	{
		return _getConnFromCache(new ConnectionResult() {
			public Connection run() {
				Connection connection = connectionDao.getBySourceTargetType(sourceKey, targetKey, type, inclMessage);
				_assertConnectionAccess(connection, inclMessage);
				_addProfilesToConnection(connection, inclProfiles);
				return connection;
			}
		},  new Object[]{sourceKey, targetKey, type, inclMessage, inclProfiles});
	}

	/**
	 * Utility interface for caching
	 */
	private interface ConnectionResult {
		public Connection run();
	}

	/**
	 * 
	 * @param connectionResult
	 * @param arguments
	 * @return
	 */
	private Connection _getConnFromCache(
				ConnectionResult call,
				Object[] arguments) 
	{
		String argKey = MethodCallCache.getArgumentKey(arguments);
		Object res = connCache.get().get(argKey);

		// cache if null
		if (res == null) {
			res = call.run();
			if (res == null) {
				res = ObjectUtils.NULL;
			}
			connCache.get().put(argKey, res);
		}

		// convert null to null
		if (res == ObjectUtils.NULL) {
			return null;
		} else {
			return (Connection) res;
		}
	}

	/**
	 * Utility method to add profiles to connection object
	 * 
	 * @param connection
	 * @param inclProfiles
	 * @throws DataAccessRetrieveException 
	 */
	private final void _addProfilesToConnection(
			Connection connection,
			boolean inclProfiles) throws DataAccessRetrieveException 
	{
		if (inclProfiles && connection != null)
		{
			Map<String,Boolean> keys = new HashMap<String,Boolean>();
			_addKeysForConnection(connection, keys, true);

			Map<String, Employee> profiles = pps.getProfilesMapByKeys(keys.keySet(), ProfileRetrievalOptions.MINIMUM);			
			_addProfilesToConnection(connection, profiles, true);
		}
	}

	/**
	 * Simple method to filter access control on message portion of connection.
	 * 
	 * @param connection
	 * @param inclMessage
	 * @throws DataAccessRetrieveException 
	 */
	private final void aclFilterConnectionMessage(Connection connection) throws DataAccessRetrieveException 
	{
		boolean canViewMessage = true;

		// Ensure we have a real connection
		if (connection == null) {
			return;			
		}

		// ensure that that connection is configured with the server
		ConnectionTypeConfig ctc = DMConfig.instance().getConnectionTypeConfigs().get(connection.getType());
		AssertionUtils.assertTrue( ctc != null, AssertionType.UNSUPPORTED_CONFIGURATION);

		// Get the current user and determine if admin
		Employee currentUser = AppContextAccess.getCurrentUserProfile();		
//		boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN)
		boolean isAdmin = AppContextAccess.isUserAnAdmin();

		// Admin users can see anything, so we only do additional checks if not an admin
		if (!isAdmin) {
			// assert access to the connection
			PolicyHelper.assertAcl(Acl.CONNECTION_VIEW, connection.getSourceKey());

			// if the message is requested, ensure the user has rights
			MessageAclEnum messageAcl = ctc.getMessageAcl();

			// a public message can be seen by anyone, so we only need to special check others
			if (!MessageAclEnum.PUBLIC.equals(messageAcl)) {

				// user must be authenticated
				canViewMessage = currentUser != null;

				if (canViewMessage) {
					String keyToCheck = currentUser.getKey();
					// new acl check based on cleaner configuration
					if (MessageAclEnum.SOURCE.equals(messageAcl)) {
						canViewMessage = connection.getSourceKey().equals(keyToCheck);				
					} else if (MessageAclEnum.TARGET.equals(messageAcl)) {
						canViewMessage = connection.getTargetKey().equals(keyToCheck);								
					} else if (MessageAclEnum.PRIVATE.equals(messageAcl)) {
						canViewMessage = connection.getTargetKey().equals(keyToCheck) || connection.getSourceKey().equals(keyToCheck);												
					}
				}				
			}				
		}

		// if the user cannot see the message, remove it from the bean
		if (!canViewMessage) {
			connection.setMessage("");
		}
	}

	/**
	 * Simple method to check access control on message portion of connection.
	 * 
	 * @param connection
	 * @param inclMessage
	 * @throws DataAccessRetrieveException 
	 */
	private final void _assertConnectionAccess(Connection connection, boolean inclMessage) throws DataAccessRetrieveException 
	{
		// Ensure we have a real connection
		if (connection == null) {
			return;			
		}

		// ensure that that connection is configured with the server
		ConnectionTypeConfig ctc = DMConfig.instance().getConnectionTypeConfigs().get(connection.getType());
		AssertionUtils.assertTrue( ctc != null, AssertionType.UNSUPPORTED_CONFIGURATION);

		// Get the current user and determine if admin
		Employee currentUser = AppContextAccess.getCurrentUserProfile();		
//		boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN)
		boolean isAdmin = AppContextAccess.isUserAnAdmin();

		// Admin users can see anything, so we only do additional checks if not an admin
		if (!isAdmin) {
			// assert access to the connection
			PolicyHelper.assertAcl(Acl.CONNECTION_VIEW, connection.getSourceKey());

			// if the message is requested, ensure the user has rights
			if (inclMessage) {
				MessageAclEnum messageAcl = ctc.getMessageAcl();

				// a public message can be seen by anyone, so we only need to special check others
				if (!MessageAclEnum.PUBLIC.equals(messageAcl)) {

					// user must be authenticated
					AssertionUtils.assertTrue(currentUser != null, AssertionType.UNAUTHORIZED_ACTION);
					String keyToCheck = currentUser.getKey();

					// new acl check based on cleaner configuration
					if (MessageAclEnum.SOURCE.equals(messageAcl)) {
						AssertionUtils.assertTrue( connection.getSourceKey().equals(keyToCheck), AssertionType.UNAUTHORIZED_ACTION);				
					} else if (MessageAclEnum.TARGET.equals(messageAcl)) {
						AssertionUtils.assertTrue( connection.getTargetKey().equals(keyToCheck), AssertionType.UNAUTHORIZED_ACTION);								
					} else if (MessageAclEnum.PRIVATE.equals(messageAcl)) {
						AssertionUtils.assertTrue( connection.getTargetKey().equals(keyToCheck) || connection.getSourceKey().equals(keyToCheck), AssertionType.UNAUTHORIZED_ACTION);												
					}

				}

			}			    	
		}
	}

	public void updateConnection(Connection connection) throws DataAccessException, ConnectionExistsException, ProfilesRuntimeException, EmailException
	{
		// get the user and ensure they are authenticated
		Employee currentUser = AppContextAccess.getCurrentUserProfile();
		AssertionUtils.assertNotNull(currentUser, AssertionType.UNAUTHORIZED_ACTION);

		// if the user is an admin, then that user can basically do anything
//		boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN)
		boolean isAdmin = AppContextAccess.isUserAnAdmin();

		// get the existing connection from the database
		Connection existingConnection = getConnection(connection.getConnectionId(), true, false);
		AssertionUtils.assertNotNull(existingConnection);

		// get the connection configuration so we know if we may need to process an accept operation
		ConnectionTypeConfig connectionTypeConfig = ProfilesConfig.instance().getDMConfig().getConnectionTypeConfigs().get(existingConnection.getType());

		// workflow is enabled for this type of connection, and the workflow status has changed on the incoming request to accepted so this is an accept operation 
		if (WorkflowEnum.CONFIRMED.equals(connectionTypeConfig.getWorkflow()) && connection.getStatus() == Connection.StatusType.ACCEPTED && existingConnection.getStatus() == Connection.StatusType.PENDING) {
			acceptConnection(connection.getConnectionId());
		} else {	
			String currentUserKey = currentUser.getKey();
			boolean userCanUpdate = false;			
			MessageAclEnum messageAcl = connectionTypeConfig.getMessageAcl();
			if (MessageAclEnum.PRIVATE.equals(messageAcl) || MessageAclEnum.PUBLIC.equals(messageAcl)) {
				// if the message is private to connection participants or public to all, either participant can update the message body
				userCanUpdate = currentUserKey.equals(existingConnection.getSourceKey()) || currentUserKey.equals(existingConnection.getTargetKey());
			} else if (MessageAclEnum.SOURCE.equals(messageAcl)) {
				// if the message is private to the source of the connection, only the source user can update it
				userCanUpdate = currentUserKey.equals(existingConnection.getSourceKey());				
			} else if (MessageAclEnum.TARGET.equals(messageAcl)) {
				// if the message is private to the target of the connection, only the target user can update it
				userCanUpdate = currentUserKey.equals(existingConnection.getTargetKey());				
			}		
			// but of course, the admin can do anything it pleases
			if (isAdmin) {
				userCanUpdate = true;
			}			
			AssertionUtils.assertTrue(userCanUpdate);				

			//
			// Perform action
			//
			profSvc.touchProfile(existingConnection.getSourceKey());
			profSvc.touchProfile(existingConnection.getTargetKey());		
			Date now = new Date();
			// update the message body
			existingConnection.setMessage(connection.getMessage());
			_updateConnection(existingConnection, currentUser, now);			
			clearConnCache();
		}
	}

	/*
	 * Implements logic to accept a confirmed connection type.
	 * 
	 * In order to accept a connection, the connection type definition must have a workflow of confirmed, and a status of pending.
	 * 
	 * In addition, the person that can approve must meet the following conditions depending on the nature of the graph and the nodeOfCreator attributes.
	 * 
	 * 	graph			nodeOfCreator		whoCanApprove
	 * 	directional		source				currentUser == connection.target
	 * 	directional		target				currentUser == connection.source
	 * 	directional		either				currentUser != connection.creator && (currentUser == connection.source || currentUser == connection.target)
	 * 	bidirectional	source				currentUser == connection.target
	 * 	bidirectional	target				currentUser == connection.source
	 * 	bidirectional	both				NOT SUPPORTED (errors in configuration start-up)
	 * 
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void acceptConnection(String connectionId) throws DataAccessException, ConnectionExistsException 
	{
		// get the user and ensure they are authenticated
		Employee currentUser = AppContextAccess.getCurrentUserProfile();		
		AssertionUtils.assertNotNull(currentUser, AssertionType.UNAUTHORIZED_ACTION);

		// get the pending connection to ensure it exists and the user can actually see it
		boolean retrieveProfiles = (SocialContactsHelper.isServiceAvailable()); // need these for Contacts sync
		Connection pendingConnection = getConnection(connectionId, true, retrieveProfiles);
		AssertionUtils.assertNotNull(pendingConnection);

		// get the connection configuration and ensure that it has a confirmed workflow and the connection is pending
		ConnectionTypeConfig connectionTypeConfig = ProfilesConfig.instance().getDMConfig().getConnectionTypeConfigs().get(pendingConnection.getType());
		AssertionUtils.assertTrue(WorkflowEnum.CONFIRMED.equals(connectionTypeConfig.getWorkflow()));
		AssertionUtils.assertTrue(pendingConnection.getStatus() == Connection.StatusType.PENDING);

		// implement the rules on approval based on the nature of the connection
		boolean userCanApprove = false;
		GraphEnum graph = connectionTypeConfig.getGraph();
		NodeOfCreator nodeOfCreator = connectionTypeConfig.getNodeOfCreator();
		String currentUserKey = currentUser.getKey();
		if (GraphEnum.DIRECTIONAL.equals(graph)) {
			if (NodeOfCreator.SOURCE.equals(nodeOfCreator)) {
				userCanApprove = currentUserKey.equals(pendingConnection.getTargetKey());
			}
			else if (NodeOfCreator.TARGET.equals(nodeOfCreator)) {
				userCanApprove = currentUserKey.equals(pendingConnection.getSourceKey());
			}
		} else if (GraphEnum.BIDIRECTIONAL.equals(graph)) {
			if (NodeOfCreator.SOURCE.equals(nodeOfCreator)) {
				userCanApprove = currentUserKey.equals(pendingConnection.getTargetKey());
			} else if (NodeOfCreator.TARGET.equals(nodeOfCreator)) {
				userCanApprove = currentUserKey.equals(pendingConnection.getSourceKey());
			}
		}
		boolean isAdmin = AppContextAccess.isUserAnAdmin();
		if (isAdmin) {
			userCanApprove = true;
		}			
		AssertionUtils.assertTrue(userCanApprove);

		// if the graph is bidi, then we will need to look up the other link in the connection, and ensure it is in a proper state
		Connection unconfirmedConnection = null;
		if (GraphEnum.BIDIRECTIONAL.equals(graph)) {
			unconfirmedConnection = connectionDao.getBySourceTargetType(pendingConnection.getTargetKey(), pendingConnection.getSourceKey(), pendingConnection.getType(), false);
			AssertionUtils.assertNotNull(unconfirmedConnection);
			AssertionUtils.assertTrue(unconfirmedConnection.getStatus() == Connection.StatusType.UNCONFIRMED);
		}

		//
		// Perform action
		//
		profSvc.touchProfile(pendingConnection.getSourceKey());
		profSvc.touchProfile(pendingConnection.getTargetKey());		
		Date now = new Date();
		pendingConnection.setStatus(Connection.StatusType.ACCEPTED);
		_updateConnection(pendingConnection, currentUser, now);
		if (GraphEnum.BIDIRECTIONAL.equals(graph)) {
			unconfirmedConnection.setStatus(Connection.StatusType.ACCEPTED);
			_updateConnection(unconfirmedConnection, currentUser, now);
		}		

		if (SocialContactsHelper.isServiceAvailable()) {
			//
			// Attempt to notify Social Contacts
			//
			SocialContactsAction scAction = SocialContactsAction.ACCEPT;
			try {
				Employee sourceUser = pendingConnection.getSourceProfile();
				Employee targetUser = pendingConnection.getTargetProfile();
				String userEmail = sourceUser.getEmail();
				String targetID  = targetUser.getUserid();
				SocialContactsHelper.syncWithSocialContacts(connectionId, targetID, userEmail, scAction);
			}
			catch (Exception ex) {
				if (logger.isLoggable(Level.INFO)) {
					logger.log(Level.SEVERE, "Sync with Social Contacts : acceptConnection("+connectionId+") FAILED");
				}
				// reset the state in Profile db
				pendingConnection.setStatus(Connection.StatusType.UNCONFIRMED);
				_updateConnection(pendingConnection, currentUser, now);
				if (GraphEnum.BIDIRECTIONAL.equals(graph)) {
					unconfirmedConnection.setStatus(Connection.StatusType.PENDING);
					_updateConnection(unconfirmedConnection, currentUser, now);
				}		
				throw new DataAccessCreateException( ex );
			}
		}

		//
		// Attempt to send events
		// 

		// the actor is always the current user, target is always the other user in pending connection
		String actorKey = currentUserKey;
		String targetKey = currentUserKey.equals(pendingConnection.getSourceKey()) ? pendingConnection.getTargetKey() : pendingConnection.getSourceKey();

		// we only send events for non-custom connection types
		if (!connectionTypeConfig.isExtension()) {
			EventLogService eventLogSvc = AppServiceContextAccess.getContextObject(EventLogService.class);
			EventLogEntry eventLogEntry = EventLogHelper.createEventLogEntry(pps, actorKey, targetKey, EventLogEntry.Event.CONNECTION_ACCEPTED );
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.BRIEF_DESC, pendingConnection.getMessage());
			eventLogEntry.setProperty(EventLogEntry.PROPERTY.CONNECTION_ID, connectionId);
			eventLogSvc.insert( eventLogEntry );			
		}

		clearConnCache();
	}

	/**
	 * Internal method for setting variables / data for connection update
	 * 
	 * @param connection
	 * @param currentUser
	 * @param now
	 * @throws ConnectionExistsException
	 */
	private final void _updateConnection(Connection connection, Employee currentUser, Date now) throws ConnectionExistsException
	{
		connection.setLastMod(now);
		connection.setCreatedByKey(currentUser.getKey());
		connection.setLastModByKey(currentUser.getKey());

		connectionDao.update(connection);
	}

	private final boolean _doesConnectionExist(Connection connection) throws DataAccessException
	{
		return connectionDao.getBySourceTargetType(connection.getSourceKey(), connection.getTargetKey(), connection.getType(), false) != null;
	}

	private static boolean isConnectionFeatureEnabled(ConnectionTypeConfig ctc, Employee employee)
	{
		// valid connection type
		boolean result = ctc != null;
		if (employee != null) {			
			// ensure the employee is active
			result = result && employee.isActive();

			// if its a colleague connection, check that colleagues are enabled for the employee
			if (PeoplePagesServiceConstants.COLLEAGUE.equals(ctc.getType())) {
				result = result && PolicyHelper.isFeatureEnabled(Feature.COLLEAGUE, employee);
			}			
		}				
		return result;							
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ConnectionCollection getConnections(ProfileLookupKey plk, ConnectionRetrievalOptions cro) throws DataAccessException, ProfilesRuntimeException {
		return getConnections(plk, null, cro);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ConnectionCollection getConnections(ProfileLookupKey sourceKey, ProfileLookupKey targetKey, ConnectionRetrievalOptions cro)
				throws DataAccessException, ProfilesRuntimeException
	{
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "enter getConnections(sourceKey, targetKey, cro)", new Object[] {sourceKey, targetKey, cro});
		}

		// by default, the result is nothing
		ConnectionCollection result = new ConnectionCollection(EMPTY_CONN_LIST, 0, cro.calculatePageNumber(), cro.getMaxResultsPerPage());

		// validate the input arguments
		if (sourceKey == null) {
			AssertionUtils.assertNotNull(targetKey);
		}
		AssertionUtils.assertNotNull(cro);
		AssertionUtils.assertNotNull(cro.getProfileOptions());

		// validate that we have an actual connection type filter
		ConnectionTypeConfig ctc = _getConnectionTypeConfig(cro.getConnectionType());

		// Load the provided employees, and assert that they are valid
		Employee currentUser = AppContextAccess.getCurrentUserProfile();
		Employee sourceEmp = null;
		Employee targetEmp = null;		
		if (sourceKey != null) {
			sourceEmp = pps.getProfile(sourceKey, ProfileRetrievalOptions.MINIMUM);
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, "getConnections(sourceKey, ..., ...) " + sourceKey + ":" + sourceEmp);
			}
			AssertionUtils.assertNotNull(sourceEmp);
		}
		if (targetKey != null) {
			targetEmp = pps.getProfile(targetKey, ProfileRetrievalOptions.MINIMUM);
			if (logger.isLoggable(Level.FINEST)) {
				logger.log(Level.FINEST, "getConnections(..., targetKey, ...) " + targetKey + ":" + targetEmp);
			}
			AssertionUtils.assertNotNull(targetEmp);
		}

		boolean isAdmin = AppContextAccess.isUserAnAdmin();
		
		// validate that we should actually query the DB to attempt to find data
		boolean isFeatureEnabled = isConnectionFeatureEnabled(ctc, sourceEmp) && isConnectionFeatureEnabled(ctc, targetEmp);
		if (isFeatureEnabled) {
			// to view pending connections, the current user must be either the source or target user provided
			

			if (cro.getStatus() != Connection.StatusType.ACCEPTED || cro.isInclPendingCount()) {
				AssertionUtils.assertNotNull(currentUser, AssertionType.UNAUTHORIZED_ACTION);
				//A non-admin user should be either the source or the target if trying to create a connection
				if(!isAdmin){
					AssertionUtils.assertTrue(currentUser.matchesLookupKey(sourceKey) || currentUser.matchesLookupKey(targetKey));
				}
			}


			// flag if message should be retrieved from query (NOTE: we always will post-filter for acl purposes)
			cro.setInclMessage(cro.isInclMessage());			
			int[] connStatus = cro.isInclPendingCount() ? new int[]{cro.getStatus(), Connection.StatusType.PENDING} : new int[] {cro.getStatus()};			
			Map<Integer, Integer> counts = connectionDao.getConnectionsCountMap(sourceEmp != null ? sourceEmp.getKey() : null, targetEmp != null ? targetEmp.getKey() : null, cro, connStatus);

			// get connection list
			int total = counts.get(cro.getStatus());
			List<Connection> conns = (total > cro.getSkipResults()) ? _getConnectionsList(sourceEmp != null ? sourceEmp.getKey() : null, targetEmp != null ? targetEmp.getKey() : null, cro) : EMPTY_CONN_LIST;

			// post-filter access to the connection message based on connection acl rules
			for (Connection conn : conns) {
				aclFilterConnectionMessage(conn);
			}

			// setup object
			result = new ConnectionCollection(conns, total, cro.calculatePageNumber(), cro.getMaxResultsPerPage());
			if (cro.isInclPendingCount()) {
				result.setPendingInvitations(counts.get(Connection.StatusType.PENDING));
			}			
		}

		if (logger.isLoggable(Level.FINEST)) {
			if (null != result) {
				List<Connection> results = result.getResults();
				int numResults = 0;
				if (null != results) {
					numResults = results.size();
				}
				logger.log(Level.FINEST, "getConnections(sourceKey, targetKey, cro) returning : " + numResults + " results");
			}
			logger.log(Level.FINEST, "exit getConnections(sourceKey, targetKey, cro)", result);
		}
		return result;
	}
							
	/**
	 * Utility method to fetch the connections filtered by a sourceKey, targetKey, or both.
	 * 
	 * @param sourceKey
	 * @param targetKey
	 * @param cro
	 * @return
	 * @throws DataAccessRetrieveException
	 */
	private final List<Connection> _getConnectionsList(String sourceKey, String targetKey, ConnectionRetrievalOptions cro) throws DataAccessRetrieveException 
	{					
		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "enter _getConnectionsList(String sourceKey, String targetKey, ConnectionRetrievalOptions cro)", new Object[] {sourceKey, targetKey, cro});
		}

		List<Connection> result = Collections.emptyList();
		if (sourceKey == null || targetKey == null) {						
			List<Employee> profiles = Collections.emptyList();
			if (sourceKey != null && targetKey == null) {

				if (logger.isLoggable(Level.FINEST)) {
					logger.log(Level.FINEST, "fetch profiles by sourceKey-", sourceKey);
				}
				profiles = connectionDao.getProfilesConnectedTo(sourceKey, cro);	
			}
			else {
				if (logger.isLoggable(Level.FINEST)) {
					logger.log(Level.FINEST, "fetch profiles by targetKey-", targetKey);
				}
				profiles = connectionDao.getProfilesConnectedFrom(targetKey, cro);
			}
			result = new ArrayList<Connection>(profiles.size());
			for (Employee profile : profiles) {
				Connection c = new Connection();
				c.setSourceKey(sourceKey != null ? sourceKey : profile.getKey());
				c.setTargetKey(targetKey != null ? targetKey : profile.getKey());
				c.setType(cro.getConnectionType());
				c.setStatus(cro.getStatus());
				c.setConnectionId((String) profile.get(PeoplePagesServiceConstants.CONNECTION_ID));
				c.setCreated((Date) profile.get("connectionCreated"));
				c.setCreatedByKey((String) profile.get("connectionCreatedBy"));
				c.setLastMod((Date) profile.get("connectionLastmod"));
				c.setLastModByKey((String) profile.get("connectionLastmodBy"));
				c.setMessage((String) profile.get("connectionMessage"));
				if (sourceKey == null) {
					c.setSourceProfile(profile);
				}
				else {
					c.setTargetProfile(profile);
				}		
				result.add(c);
			}

			if (cro.isInclRelatedProfiles())
			{
				Map<String,Boolean> keys = new HashMap<String,Boolean>();
				for (Connection connection : result)
				{
					_addKeysForConnection(connection, keys, true);
				}

				Map<String,Employee> profileMap = pps.getProfilesMapByKeys(keys.keySet(), ProfileRetrievalOptions.MINIMUM);
				for (Connection connection : result)
				{
					_addProfilesToConnection(connection, profileMap, true);
				}

				// remove redundant users for resolving
				for (Employee p : profiles)	profileMap.remove(p.getKey());
				for (Employee p : profileMap.values()) profiles.add(p);
			}

			ProfileResolver2.resolveProfilesForListing(profiles, cro.getProfileOptions() == null ? ProfileRetrievalOptions.LITE : cro.getProfileOptions());
		}
		else {
			// optimization to call single result method where we know there can only be a list of 1
			Connection connection = getConnection(sourceKey, targetKey, cro.getConnectionType(), cro.isInclMessage(), true);
			if (connection != null) {
				result = Arrays.asList(connection);
			}			
		}

		if (logger.isLoggable(Level.FINEST)) {
			logger.log(Level.FINEST, "exit _getConnectionsList(String sourceKey, String targetKey, ConnectionRetrievalOptions cro)", result);
		}
		return result;				
	}

	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public ConnectionCollection getConnectionsInCommon(
			com.ibm.peoplepages.data.ProfileLookupKey.Type plkType,
			String[] plks, ConnectionRetrievalOptions cro) 
		throws DataAccessException, ProfilesRuntimeException 
	{
		return _getConnectionsInCommon(plkType, plks, cro, false);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public int getConnectionsInCommonCount(
			com.ibm.peoplepages.data.ProfileLookupKey.Type plkType,
			String[] plks, ConnectionRetrievalOptions cro)
			throws DataAccessException, ProfilesRuntimeException 
	{
		return _getConnectionsInCommon(plkType, plks, cro, true).getTotalResults();
	}
	
	private final ConnectionCollection _getConnectionsInCommon(
			com.ibm.peoplepages.data.ProfileLookupKey.Type plkType,
			String[] plks, ConnectionRetrievalOptions cro,
			boolean countOnly) 
		throws DataAccessException, ProfilesRuntimeException 
	{
		AssertionUtils.assertNotNull(plkType);
		AssertionUtils.assertNotNull(plks);
		AssertionUtils.assertNotNull(cro);
		AssertionUtils.assertNotNull(cro.getProfileOptions());

		AssertionUtils.assertTrue(!cro.isInclPendingCount());
		AssertionUtils.assertTrue(cro.getStatus() == Connection.StatusType.ACCEPTED);
		AssertionUtils.assertEquals(PeoplePagesServiceConstants.COLLEAGUE, cro.getConnectionType());

		if (plks.length < 2)
			return _getEmptyConnectionResults(cro, 0);

		//Assert ACL check for all plks
		for (String key : plks) {
			PolicyHelper.assertAcl(Acl.CONNECTION_VIEW, key);
		}

		String[] keys;
		switch (plkType)
		{
			case KEY:
				keys = plks;
				break;
			default:
				keys = new String[plks.length];
				int index = 0;
				for (String plk : plks)
				{
					Employee profile = pps.getProfile(new ProfileLookupKey(plkType, plk), ProfileRetrievalOptions.MINIMUM);
					if (profile == null)
						return _getEmptyConnectionResults(cro, 0);
					keys[index++] = profile.getKey();
				}
		}

		int total = connectionDao.countConnectionsInCommon(keys, cro);
		if (countOnly || total <= cro.getSkipResults())
			return _getEmptyConnectionResults(cro, total);

		List<Employee> profiles = connectionDao.getCommonProfilesConnectedTo(keys, cro);
		List<Connection> connections = new ArrayList<Connection>(profiles.size());
		for (Employee profile : profiles)
		{
			Connection connection = new Connection();
			connection.setTargetKey(profile.getKey());
			connection.setTargetProfile(profile);
			connection.setType(cro.getConnectionType());
			connection.setStatus(cro.getStatus());
			connection.setLastMod(profile.getRecordUpdated());
			connections.add(connection);
		}

		return new ConnectionCollection(connections, total, cro.calculatePageNumber(), cro.getMaxResultsPerPage());
	}

	private final ConnectionCollection _getEmptyConnectionResults(ConnectionRetrievalOptions cro, int total)
	{
		return new ConnectionCollection(EMPTY_CONN_LIST, total, 1, cro.getMaxResultsPerPage());
	}

	/**
	 * Utility method for retrieving ConnectionType configuration. 
	 * 
	 * @param connection
	 * @return
	 */
	private final ConnectionTypeConfig _getConnectionTypeConfig(Connection connection) 
	{
		AssertionUtils.assertNotNull(connection);

		return _getConnectionTypeConfig(connection.getType());
	}

	/**
	 * Utility method for retrieving ConnectionType configuration. 
	 *  
	 * @param connection
	 * @return
	 */
	private final ConnectionTypeConfig _getConnectionTypeConfig(String connectionType) 
	{
		AssertionUtils.assertNotEmpty(connectionType);

		ConnectionTypeConfig ctc = DMConfig.instance().getConnectionTypeConfigs().get(connectionType);
		AssertionUtils.assertNotNull(ctc);		
		return ctc;
	}

	/**
	 * Utility method to add all the relevant keys from a Connection for retrieval
	 * 
	 * @param keys
	 * @param connection
	 */
	private final void _addKeysForConnection(Connection connection, Map<String,Boolean> keys, boolean inclTarget)
	{
		keys.put(connection.getCreatedByKey(), Boolean.TRUE);
		keys.put(connection.getLastModByKey(), Boolean.TRUE);
		keys.put(connection.getSourceKey(), Boolean.TRUE);
		if (inclTarget) keys.put(connection.getTargetKey(), Boolean.TRUE);
	}

	/**
	 * Utility method to add related Profiles to the connection set.
	 * 
	 * @param connection
	 * @param profiles
	 */
	private final void _addProfilesToConnection(Connection connection, Map<String, Employee> profiles, boolean inclTarget)
	{
		connection.setCreatedByProfile(profiles.get(connection.getCreatedByKey()));
		connection.setLastModByProfile(profiles.get(connection.getLastModByKey()));
		connection.setSourceProfile(profiles.get(connection.getSourceKey()));
		if (inclTarget) connection.setTargetProfile(profiles.get(connection.getTargetKey()));
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ConnectionService#deleteAllForKey(java.lang.String)
	 */
	public void deleteAllForKey(String key) throws DataAccessException {
		assertCurrentUserAdmin();
		connectionDao.deleteByKey(key);
		clearConnCache();
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ConnectionService#getConnectedToKeys(java.util.List, com.ibm.peoplepages.data.ConnectionRetrievalOptions)
	 */
	public Map<String, List<Employee>> getConnectedProfilesForIndexer(List<String> forKeys, ConnectionRetrievalOptions cro)
			throws DataAccessException, ProfilesRuntimeException
	{
		// Access check
		assertCurrentUserSearchAdmin();

		if (forKeys.size() == 0) {
			Map<String,List<Employee>> r = Collections.emptyMap();
			return r;
		}

		List<Employee> allConns = connectionDao.getSourceTargetKeys(forKeys, cro);		

		//
		// Build a map of user/List<connections>
		// 
		int lf = (int) Math.min(allConns.size()/2, forKeys.size()*1.5);
		Map<String,List<Employee>> connsByUser = new HashMap<String,List<Employee>>(lf);

		for (Employee e : allConns) {
			String sourceKey = (String) e.get("sourceKey");
			List<Employee> targets = connsByUser.get(sourceKey);
			if (targets == null) {
				targets = new ArrayList<Employee>();
				connsByUser.put(sourceKey, targets);
			}
			targets.add(e);
		}
		return connsByUser;
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ConnectionService#countProfilesWithCollegues
	 */
	public int countProfilesWithCollegues() throws DataAccessException {
		ProfileRetrievalOptions options = new ProfileRetrievalOptions();
		return connectionDao.countProfilesWithCollegues(options);
	}


	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ConnectionService#countProfilesWithCollegues(int index)
	 */
	public int countProfilesWithCollegues(int connStatus) throws DataAccessException {
		ProfileRetrievalOptions options = new ProfileRetrievalOptions();
		return connectionDao.countProfilesWithCollegues(options, connStatus);
	}

	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.internal.service.ConnectionService#countTotalCollegues
	 */
	public int countTotalCollegues() throws DataAccessException {
		ProfileRetrievalOptions options = new ProfileRetrievalOptions();
		return connectionDao.countTotalCollegues(options);
	}

	/**
	 * Clear the connCache();
	 */
	private void clearConnCache() {
		connCache.get().clear();
	}

}
