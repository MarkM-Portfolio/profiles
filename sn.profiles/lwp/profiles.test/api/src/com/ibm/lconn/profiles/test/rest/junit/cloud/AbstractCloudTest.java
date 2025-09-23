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

package com.ibm.lconn.profiles.test.rest.junit.cloud;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Base;
import org.apache.abdera.writer.Writer;

import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.util.Transport;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

/**
 * A common abstract test case that all Profiles API tests on the cloud would extend
 * 
 */
public abstract class AbstractCloudTest extends TestCase {

	public static final Map<String, String> NO_HEADERS = new HashMap<String, String>(0);

	public static final Map<String, String> CACHE_CONTROL_PUBLIC = new HashMap<String, String>(1);

	public static final Map<String, String> CACHE_CONTROL_PRIVATE = new HashMap<String, String>(1);

	public static final Map<String, String> CONTENT_TYPE_SERVICE = new HashMap<String, String>(1);
	static {
		CACHE_CONTROL_PRIVATE.put("Cache-Control", "private,must-revalidate,max-age=0");
		CACHE_CONTROL_PUBLIC.put("Cache-Control", "public,must-revalidate,max-age=0");
		// CONTENT_TYPE_SERVICE.put("Content-Type", ApiConstants.Atom.MEDIA_TYPE_ATOM_SERVICE_DOCUMENT);
	}

	protected static URLBuilder urlBuilder;

	protected final int sleepTime = 100;

	protected Transport orgAUserATransport;

	protected Transport orgAUserBTransport;

	protected Transport orgBUserTransport;

	protected Transport guestUserTransport;

	protected static Abdera abdera = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		if (abdera == null) {
			abdera = new Abdera();
		}

		urlBuilder = new URLBuilder(CloudTestProperties.getInstance());
		
		// TODO in future we can pull a class from test properties

		orgAUserATransport = new Transport();
		orgAUserATransport.setup(urlBuilder.getServerURL(), CloudTestProperties.getInstance().getUserName("orgAUserA"), CloudTestProperties.getInstance()
				.getPassword("orgAUserA"));

		orgAUserBTransport = new Transport();
		orgAUserBTransport.setup(urlBuilder.getServerURL(), CloudTestProperties.getInstance().getUserName("orgAUserB"), CloudTestProperties.getInstance()
				.getPassword("orgAUserB"));
	
		orgBUserTransport = new Transport();
		orgBUserTransport.setup(urlBuilder.getServerURL(), CloudTestProperties.getInstance().getUserName("orgBUser"), CloudTestProperties.getInstance()
				.getPassword("orgBUser"));
		
		guestUserTransport = new Transport();
		guestUserTransport.setup(urlBuilder.getServerURL(), CloudTestProperties.getInstance().getUserName("guestUser"), CloudTestProperties.getInstance()
				.getPassword("guestUser"));

	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected static final Abdera ABDERA = new Abdera();

	public static Writer WRITER = ABDERA.getWriterFactory().getWriter("prettyxml");

	public static void prettyPrint(Base base) throws Exception {
		if (null != base) {
			WRITER.writeTo(base, System.out);
			System.out.println();
		}
		else {
			System.out.println("prettyPrint(): NULL");
		}
	}

	public void printProfileEntryFields(ProfileEntry pe, Collection<Field> fields) throws Exception {

		for (Field f : fields) {
			System.out.println("###--->>> " + f.name() + " : " + (String) pe.getProfileFields().get(f));
		}
		// ###--->>> DISPLAY_NAME: Monifa Shani
		// ###--->>> MANAGER_UID : null
		// ###--->>> KEY : 7ba39b9c-862a-4514-82fe-b553c58a3c43
		// ###--->>> UID : mshani
		// ###--->>> GUID : 72c91eda-4444-4c21-bffe-a71e74356c48
		// ###--->>> getUserId() : 72c91eda-4444-4c21-bffe-a71e74356c48
	}
}
