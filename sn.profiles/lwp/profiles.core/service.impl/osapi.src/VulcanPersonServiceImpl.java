/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package org.apache.shindig.vulcanext.person.service;

//import org.apache.commons.io.IOUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.inject.Inject;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.ConnectionService;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection;
import com.ibm.lconn.profiles.internal.util.AdminCodeSection.UncheckedCallableAdminBlock;
import com.ibm.peoplepages.data.Connection;
import com.ibm.peoplepages.data.ConnectionCollection;
import com.ibm.peoplepages.data.ConnectionRetrievalOptions;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKey.Type;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.service.PeoplePagesService;

public class VulcanPersonServiceImpl implements PersonService {
	//private ProfileService service = AppServiceContextAccess.getContextObject(ProfileService.class);
	private PeoplePagesService pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
	private ConnectionService cs = AppServiceContextAccess.getContextObject(ConnectionService.class);
	
	@Inject
	public VulcanPersonServiceImpl() {
	}

	/**
	 * Returns a list of persons that identified by user ids.
	 * 
	 * @param userIds  A set of user ids
	 * @param groupId  The group
	 * @param collectionOptions  filter, sort and paginate info
	 * @param fields   details to fetch. Empty set implies all
	 * @param token    The gadget token @return a list of people.
	 * @return Future that returns a RestfulCollection of Person
	 */
	public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId, 
			                                           CollectionOptions collectionOptions, Set<String> fields,
			                                           SecurityToken token) throws ProtocolException {
		System.out.println("VulcanPersonServiceImpl - ids =" + userIds + " fields=" + fields);
		int userIdsSize = userIds.size();
		List<Person> people = new ArrayList<Person>(userIdsSize);

		List<Employee> employees = null;
		final List<String> sanitizedIds = sanitizeUserIds(userIds, token);
		switch (groupId.getType()) {
		case all:
			// @all: select all contacts TODO: currently same as @friends
			// this does not include self
			// TODO: note missing break - so all and friends are alike for now
		case friends:
			// @friends: select all friends (subset of contacts)
			employees = new ArrayList<Employee>(userIdsSize);
			for (String id : sanitizedIds) {
				final String fid = id;
				System.out.println("about to get connections for id =" + id);
				// TODO: here is forGuid() method the correct one??
				ConnectionCollection cc = AdminCodeSection.doAsAdmin(new UncheckedCallableAdminBlock<ConnectionCollection>() {
					public ConnectionCollection call() {
						ConnectionRetrievalOptions cro = new ConnectionRetrievalOptions();
						// TODO: EVERYTHING, MINIMUM_W_STATE or LITE ??
						cro.setProfileOptions(ProfileRetrievalOptions.EVERYTHING);
						return cs.getConnections(ProfileLookupKey.forUserid(fid), cro);
					}
				});
				List<Connection> connections = cc.getResults();
				for (Connection connection : connections) {
					Employee emp = connection.getTargetProfile();
					System.out.println("converted over got emp =" + emp.getUserid());
					employees.add(emp);
				}
			}	
			break;
		case groupId:
			// {groupid}: select all members in a group
			// TODO: what here? - Profiles does not support groups
			throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "{groupid} currently not supported");
			//break;
		case deleted:
			// TODO: @deleted: what here???
			throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, "@deleted currently not supported");
			//break;
		case self:
			employees = AdminCodeSection.doAsAdmin(new UncheckedCallableAdminBlock<List<Employee>>() {
				public List<Employee> call() {
					ProfileLookupKeySet keySet = new ProfileLookupKeySet(Type.USERID, sanitizedIds);
					// TODO: EVERYTHING, MINIMUM_W_STATE or LITE ??
					return pps.getProfiles(keySet, ProfileRetrievalOptions.EVERYTHING);
				}
			});
			break;
		default:
			throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized");
		}
		/*
		 * do sort here on employee because sortBy field may not be in the "fields" parameter
		 * and not be set in the person object.
		 */
		VulcanPersonServiceHelper.doSort(employees, collectionOptions);

		Map<String,Employee> dups = new HashMap<String,Employee>(Math.max(6,employees.size()*2));
		for (Employee employee : employees) { 
			if (dups.put(employee.getUserid(), employee)==null) {  // ignore duplicates
				Person person = VulcanPersonServiceHelper.convertEmployee2Person(employee, fields, collectionOptions);
				if (person != null) {
					people.add(person);
				}
			}
		}
		/*
		 * set pagination stuff like start index, items per page, total found
		 */
		System.out.println("page index="+collectionOptions.getFirst()+" pageMax="+collectionOptions.getMax()+" peoples="+people.size());
	    int total = people.size();
	    int pageSize = collectionOptions.getMax();
	    int startIndex = collectionOptions.getFirst();
	    people = people.subList(startIndex, Math.min(startIndex+pageSize, total));
		RestfulCollection<Person> restCollection = new RestfulCollection<Person>(people, startIndex, total, pageSize);

		return ImmediateFuture.newInstance(restCollection);
	}

	/**
	 * Returns a person with specified fields identified by the person id.
	 * 
	 * @param userId  The id of the person to fetch.
	 * @param fields  The fields to fetch.
	 * @param token   The gadget token
	 * @return a single person.
	 */

	public Future<Person> getPerson(UserId userId, Set<String> fields, SecurityToken token) throws ProtocolException {
		final String id = userId.getUserId(token);
		System.out.println("VulcanPersonServiceImpl getPerson - id =" + id + " fields=" + fields);
		Employee employee = AdminCodeSection.doAsAdmin(new UncheckedCallableAdminBlock<Employee>() {
			public Employee call() {
				// TODO: EVERYTHING, MINIMUM_W_STATE or LITE ??
				return pps.getProfile(ProfileLookupKey.forUserid(id), ProfileRetrievalOptions.EVERYTHING);
			}
		});
		Person person = VulcanPersonServiceHelper.convertEmployee2Person(employee, fields, null);
		return ImmediateFuture.newInstance(person);
	}

	public Future<Person> updatePerson(UserId userId, GroupId groupId, String appId, Set<String> fields,
			                           Person person, SecurityToken token) throws ProtocolException {
		System.out.println("Vulcan PersonService - updatePerson stub Method");
		return null;
	}

	public Future<Person> createPerson(UserId userId, GroupId groupId, String appId, Set<String> fields, 
			                           Person person, SecurityToken token)	throws ProtocolException {
		System.out.println("Vulcan PersonService - createPerson Stub method");
		return null;
	}
	
	private static List<String> sanitizeUserIds(Set<UserId> userIds, SecurityToken token) {
		List<String> userIdsString = new ArrayList<String>(userIds.size());
		Map<String, String> ids = new HashMap<String, String>(userIds.size() * 2);
		for (UserId userId : userIds) {
			/*
			 * remove repeated ids and get valid ids
			 */
			String anId = userId.getUserId(token);
			if (anId != null) {
				if (ids.put(anId, anId) == null) {
					userIdsString.add(anId);
				}
			}
		}
		System.out.println("sanitized users="+userIdsString);
        return userIdsString;
	}	
}
