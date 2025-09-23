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

package com.ibm.lconn.profiles.population.tool;

import java.util.Set;

import com.ibm.lconn.profiles.test.rest.model.ExtensionField;
import com.ibm.lconn.profiles.test.rest.model.Field;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

/**
 * Populates W3 specific field values
 */
public class W3PopulationTask extends Task {
	
	public static class ExtensionFields {
		// primary job responsibility skill set
		public static final String IBMEXT_PRIJRSS = "IBMEXT_PRIJRSS";
		// ibm sub company name
		public static final String IBMEXT_COMPYNAME = "IBMEXT_COMPYNAME";
		public static final String IBMEXT_PRFBUSADR = "IBMEXT_PRFBUSADR";
		// important contact information
		public static final String IBMEXT_IMPCNTINF = "IBMEXT_IMPCNTINF";
		// your tie-line
		public static final String IBMEXT_TIELINE = "IBMEXT_TIELINE";
		// what you are known for
		public static final String WHAT_I_AM_KNOWN_FOR = "whatIAmKnownFor";
	}
	
	private static final String[] PRIJSS = {
		"Team Leader", "Sales Leader", "Individual Contributor", "Chef", "Dancer"
	};
		
	private static final String[] COMPYNAME = {
		"IBM USA", "IBM United Kingdom"
	};
	
	private static final String[] IMPCNTINF = {
		"Contact me via ST", "I am available via postal mail", "I am best reached at home"
	};

	private static final String[] WHAT_KNOWN = {
		"I am known for being awesome", "I am known for not very much", ""
	};
	
	private static final String[] ASSISTANT = {
		"wasadmin", "", "", "", "sdaryn"
	};
	
	private static final String[] BUILDING = {
		"A",
		"B"
	};
	
	private static final String[] FLOOR = {
		"1", "2", "3", "4"
	};
	
	private static final String[] OFFICE = {
		"Oval", "Square"
	};
			
	private int assistantIndex = 0;
	private int buildingIndex = 0;
	private int floorIndex = 0;
	private int officeIndex = 0;
	private int prijssIndex = 0;
	private int companyNameIndex = 0;
	private int contactInfoIndex = 0;
	private int whatKnownIndex = 0;
		
	public W3PopulationTask() throws Exception {
		super();
	}
	
	public int incrementIndex(int curValue, String[] listItems) {
		int newValue = curValue + 1;
		if (newValue >= listItems.length) {
			newValue = 0;
		}
		return newValue;
	}
	
	@Override
	public void doTask(ProfileEntry profileEntry) throws Exception {
		if (profileEntry == null)
			return;

		// now update the profile entry		
		String uid = (String) profileEntry.getProfileFields().get(Field.UID);
		ProfileEntry serverVersion = getProfileByUid(uid);
		if (serverVersion != null) {
			
			// update building, floor, office
			String buildingId = BUILDING[buildingIndex];
			String floorId = FLOOR[floorIndex];
			String officeId = OFFICE[officeIndex];
			String primaryJobSS = PRIJSS[prijssIndex];
			String compyName = COMPYNAME[companyNameIndex];
			String contactInfo = IMPCNTINF[contactInfoIndex];
			String whatIamKnown = WHAT_KNOWN[whatKnownIndex];
			String assistant = ASSISTANT[assistantIndex];
			
			assistantIndex = incrementIndex(assistantIndex, ASSISTANT);
			buildingIndex = incrementIndex(buildingIndex, BUILDING);
			floorIndex = incrementIndex(floorIndex, FLOOR);
			officeIndex = incrementIndex(officeIndex, OFFICE);
			prijssIndex = incrementIndex(prijssIndex, PRIJSS);
			companyNameIndex = incrementIndex(companyNameIndex, COMPYNAME);
			contactInfoIndex = incrementIndex(contactInfoIndex, IMPCNTINF);
			whatKnownIndex = incrementIndex(whatKnownIndex, WHAT_KNOWN);
			
			serverVersion.getProfileFields().put(Field.BLDG_ID, buildingId);
			serverVersion.getProfileFields().put(Field.FLOOR, floorId);
			serverVersion.getProfileFields().put(Field.OFFICE_NAME, officeId);
			serverVersion.getProfileFields().put(Field.SECRETARY_UID, assistant);
			// w3 uses preferred language to actually holding the Contractor Company Name
			serverVersion.getProfileFields().put(Field.PREFERRED_LANGUAGE, "CONTRACTOR");
			serverVersion.getProfileFields().put(Field.JOB_RESP, "Software Engineer");
			serverVersion.getProfileFields().put(Field.TELEPHONE_NUMBER, "1-555-555-5555");
			serverVersion.getProfileFields().put(Field.MOBILE_NUMBER, "1-212-212-2121");
			if (profileEntry.getEmail() != null && profileEntry.getEmail().length() > 0) {
				serverVersion.getProfileFields().put(Field.EMAIL, profileEntry.getEmail());
			}
			// clear out existing extension fields
			Set<ExtensionField> extensionFields = serverVersion.getProfileExtensionFields();
			extensionFields.clear();
			
			extensionFields.add(new ExtensionField(ExtensionFields.IBMEXT_COMPYNAME, compyName));
			extensionFields.add(new ExtensionField(ExtensionFields.IBMEXT_PRIJRSS, primaryJobSS));
			extensionFields.add(new ExtensionField(ExtensionFields.IBMEXT_IMPCNTINF, contactInfo));
			extensionFields.add(new ExtensionField(ExtensionFields.WHAT_I_AM_KNOWN_FOR, whatIamKnown));			
			String url = serverVersion.getLinkHref(ApiConstants.Atom.REL_EDIT);
			adminTransport.doAtomPut(null, url, serverVersion.toEntryXml(),NO_HEADERS, HTTPResponseValidator.OK);
		}
	}
		
}
