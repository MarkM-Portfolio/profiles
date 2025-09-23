/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.codes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.internal.service.WorkLocationService;

/*
 *
 */
public class WorkLocationServiceTest extends BaseCodesServiceTest<WorkLocation, WorkLocationService> {

	public WorkLocationServiceTest() {
		super(WorkLocation.class, WorkLocationService.class);
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.lconn.profiles.test.service.codes.BaseCodesServiceTest#codesToAdd()
	 */
	@Override
	protected List<WorkLocation> codesToAdd() {
		List<WorkLocation> l = new ArrayList<WorkLocation>();
		l.add(newWorkLocation("wtf4", "4 Technology Park Drive",null,"Westford","MA","01234"));
		l.add(newWorkLocation("wtf5", "5 Technology Park Drive",null,"Westford","MA","01234"));
		l.add(newWorkLocation("cam", "1 Rogers Street",null,"Cambridge","MA","56789"));
		return l;
	}

	@Override
	protected WorkLocation newValue(String codeId, Map<String, ? extends Object> values) {
		return new WorkLocation(codeId, values);
	}

	private WorkLocation newWorkLocation(String code, String address1, String address2, String city, String state, String zip) {
		Map<String,String> m = new HashMap<String,String>();
		m.put(WorkLocation.F_ADDRESS1.getName(), address1);
		m.put(WorkLocation.F_ADDRESS2.getName(), address2);
		m.put(WorkLocation.F_CITY.getName(), city);
		m.put(WorkLocation.F_STATE.getName(), state);
		m.put(WorkLocation.F_POSTALCODE.getName(), zip);
		return new WorkLocation(code, m);		
	}

}
