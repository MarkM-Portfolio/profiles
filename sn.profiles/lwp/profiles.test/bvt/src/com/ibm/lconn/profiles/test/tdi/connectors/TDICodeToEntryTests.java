/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.tdi.connectors;

import java.util.HashMap;
import java.util.Map;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.Department;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.data.codes.AbstractCode.CodeField;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class TDICodeToEntryTests extends BaseTestCase {

	public void testCountryToEntry() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Country.TABLENAME));
		Converter converter = new Converter(Country.TABLENAME);
		//
		String code = "au";
		String displayVal = "Australia";
		Map<String, String> values = new HashMap<String, String>(1);
		values.put(Country.F_DISPLAY_VALUE.getName(),displayVal);
		Country country = new Country(code, Tenant.SINGLETENANT_KEY, values);
		//
		//Entry e = cc.codeToEntry(country);
		Entry e = converter.codeToEntry(country);
		//
		assert(code.equals(e.getAttribute(Country.F_COUNTRY_CODE).getName()));
		assert(displayVal.equals(e.getAttribute(Country.F_DISPLAY_VALUE).getName()));
	}

	public void testDepartmentToEntry() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Department.TABLENAME));
		Converter converter = new Converter(Department.TABLENAME);
		// set up code id and value
		String deptCode = "acct";
		String deptTitle = "Accounting";
		Map<String, String> values = new HashMap<String, String>(1);
		values.put(Department.F_DEPARTMENT_TITLE.getName(),deptTitle);
		Department dept = new Department(deptCode, Tenant.SINGLETENANT_KEY, values);
		//
		//Entry e = cc.codeToEntry(country);
		Entry e = converter.codeToEntry(dept);
		//
		assert (deptCode.equals(e.getAttribute(Department.F_DEPARTMENT_CODE.getName())));
		assert (deptTitle.equals(e.getAttribute(Department.F_DEPARTMENT_TITLE.getName())));
	}

	public void testEmployeeTypeToEntry() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(EmployeeType.TABLENAME));
		Converter converter = new Converter(EmployeeType.TABLENAME);
		// set up code and values
		String emplType = "btmfdr";
		String emplDesc = "Bottom Feeder";
		Map<String, String> values = new HashMap<String, String>(1);
		values.put(EmployeeType.F_EMPLOYEE_DESCRIPTION.getName(),emplDesc);
		EmployeeType emplyType = new EmployeeType(emplType, Tenant.SINGLETENANT_KEY, values);
		//
		//Entry e = cc.codeToEntry(country);
		Entry e = converter.codeToEntry(emplyType);
		//
		assert (emplType.equals(e.getAttribute(EmployeeType.F_EMPLOYEE_TYPE.getName())));
		assert (emplDesc.equals(e.getAttribute(EmployeeType.F_EMPLOYEE_DESCRIPTION.getName())));
	}

	public void testOrganizationToEntry() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Organization.TABLENAME));
		Converter converter = new Converter(Organization.TABLENAME);
		// set up code and values
		String orgCode = "dev";
		String orgTitle = "Development";
		Map<String, String> values = new HashMap<String, String>(1);
		values.put(Organization.F_ORG_TITLE.getName(),orgTitle);
		Organization org = new Organization(orgCode, Tenant.SINGLETENANT_KEY, values);
		//
		//Entry e = cc.codeToEntry(country);
		Entry e = converter.codeToEntry(org);
		//
		assert (orgCode.equals(e.getAttribute(Organization.F_ORG_CODE.getName())));
		assert (orgTitle.equals(e.getAttribute(Organization.F_ORG_TITLE.getName())));
	}

	public void testWorkLocationToEntry() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Organization.TABLENAME));
		Converter converter = new Converter(Organization.TABLENAME);
		// set up code id and values
		String addr1 = "600 N. Charles Street";
		String city = "Baltimore";
		String postalCode = "21201";
		String state ="MD";
		String locCode ="Walters Art Museum";
		Map<String, String> values = new HashMap<String, String>(1);
		values.put(WorkLocation.F_ADDRESS1.getName(),addr1);
		values.put(WorkLocation.F_ADDRESS2.getName(),null);
		values.put(WorkLocation.F_CITY.getName(), city);
		values.put(WorkLocation.F_POSTALCODE.getName(), postalCode);
		values.put(WorkLocation.F_STATE.getName(), state);
		WorkLocation wl = new WorkLocation(locCode, Tenant.SINGLETENANT_KEY, values);
		//
		//Entry e = cc.codeToEntry(country);
		Entry e = converter.codeToEntry(wl);
		//
		assert (locCode.equals(e.getAttribute(WorkLocation.F_WORK_LOCATION_CODE.getName())));
		assert (addr1.equals(e.getAttribute(WorkLocation.F_ADDRESS1.getName())));
		assertNull (e.getAttribute(WorkLocation.F_ADDRESS2.getName()));
		assert (city.equals(e.getAttribute(WorkLocation.F_CITY.getName())));
		assert (postalCode.equals(e.getAttribute(WorkLocation.F_POSTALCODE.getName())));
		assert (state.equals(e.getAttribute(WorkLocation.F_STATE.getName())));
		
	}
	
	// IMPORTANT: This code is a replica of CodesConnector.codeToEntry. Need to remove this
	// when log4j, slf4j stackoverflow issue is resolved.
	public class Converter{
		private String _tableName;

		public Converter(String tableName){
			_tableName = tableName;
		}

		public Entry codeToEntry(AbstractCode<?> item){
			Entry rtnVal = new Entry();
			// get the fields for this code.
			for (CodeField cf : item.getFieldDefs()) {
				String name = cf.getName();
				Object value = item.getFieldValue(cf);
				if ( value != null ) value = value.toString();
				rtnVal.addAttributeValue(name,value);
			}
			return rtnVal;
		}
	}
}
