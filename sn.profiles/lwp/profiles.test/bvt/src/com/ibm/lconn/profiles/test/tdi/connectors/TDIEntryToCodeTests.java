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
import java.util.List;
import java.util.Map;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.api.tdi.connectors.CodesConnector;
import com.ibm.lconn.profiles.api.tdi.connectors.Util.TDICodeBlockException;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.data.codes.CodeType;
import com.ibm.lconn.profiles.data.codes.Country;
import com.ibm.lconn.profiles.data.codes.Department;
import com.ibm.lconn.profiles.data.codes.EmployeeType;
import com.ibm.lconn.profiles.data.codes.Organization;
import com.ibm.lconn.profiles.data.codes.WorkLocation;
import com.ibm.lconn.profiles.data.codes.AbstractCode.CodeField;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class TDIEntryToCodeTests extends BaseTestCase {

	public void testEntryToCountry() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Country.TABLENAME));
		Converter converter = new Converter(Country.TABLENAME);
		// set up code id and value
		String countryCode = "au";
		String displayValue = "Australia";
		// create entry
		Entry entry = new Entry();
		entry.addAttributeValue(Country.F_COUNTRY_CODE.getName(), countryCode);
		entry.addAttributeValue(Country.F_DISPLAY_VALUE.getName(),displayValue);
		// convert to Code
		//AbstractCode code = cc.entryToAbstractCode(entry);
		AbstractCode code = converter.entryToAbstractCode(entry);
		// check results
		assert (code instanceof Country);
		// method not visible: assert (code.getCodeType().equals(CodeType.countryCode));
		assert (code.getCodeId().equals(countryCode));
		assert (code.getFieldValue(Country.F_DISPLAY_VALUE).equals(displayValue));
		
		//TODO put in erroneous values
		boolean caughtException = false;
		try{
			Entry entry2 = new Entry();
			entry.addAttributeValue(Department.F_DEPARTMENT_CODE.getName(), countryCode);
			entry.addAttributeValue(Country.F_DISPLAY_VALUE.getName(),displayValue);
			// convert to Code
			//AbstractCode code = cc.entryToAbstractCode(entry);
			code = converter.entryToAbstractCode(entry2);
		}
		catch(TDICodeBlockException ex){
			caughtException = true;
		}
		assertTrue(caughtException);
	}

	public void testEntryToDepartment() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Department.TABLENAME));
		Converter converter = new Converter(Department.TABLENAME);
		// set up code id and value
		String deptCode = "acct";
		String deptTitle = "Accounting";
		// create entry
		Entry entry = new Entry();
		entry.addAttributeValue(Department.F_DEPARTMENT_CODE.getName(), deptCode);
		entry.addAttributeValue(Department.F_DEPARTMENT_TITLE.getName(),deptTitle);
		// convert to Code
		//AbstractCode code = cc.entryToAbstractCode(entry);
		AbstractCode code = converter.entryToAbstractCode(entry);
		// check results
		assert (code instanceof Department);
		// method not visible: assert (code.getCodeType().equals(CodeType.departmentCode));
		assert (code.getCodeId().equals(deptCode));
		assert (code.getFieldValue(Country.F_DISPLAY_VALUE).equals(deptTitle));
	}

	public void testEntryToEmployeeType() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(EmployeeType.TABLENAME));
		Converter converter = new Converter(EmployeeType.TABLENAME);
		// set up code id and value
		String emplType = "btmfdr";
		String emplDesc = "Bottom Feeder";
		// create entry
		Entry entry = new Entry();
		entry.addAttributeValue(EmployeeType.F_EMPLOYEE_TYPE.getName(), emplType);
		entry.addAttributeValue(EmployeeType.F_EMPLOYEE_DESCRIPTION.getName(),emplDesc);
		// convert to Code
		//AbstractCode code = cc.entryToAbstractCode(entry);
		AbstractCode code = converter.entryToAbstractCode(entry);
		// check results
		assert (code instanceof EmployeeType);
		// method not visible: assert (code.getCodeType().equals(CodeType.employeeTypeCode));
		assert (code.getCodeId().equals(emplType));
		assert (code.getFieldValue(EmployeeType.F_EMPLOYEE_DESCRIPTION).equals(emplDesc));
	}

	public void testEntryToOrganization() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(Organization.TABLENAME));
		Converter converter = new Converter(Organization.TABLENAME);
		// set up code id and value
		String orgCode = "dev";
		String orgTitle = "Development";
		// create entry
		Entry entry = new Entry();
		entry.addAttributeValue(Organization.F_ORG_CODE.getName(), orgCode);
		entry.addAttributeValue(Organization.F_ORG_TITLE.getName(),orgTitle);
		// convert to Code
		//AbstractCode code = cc.entryToAbstractCode(entry);
		AbstractCode code = converter.entryToAbstractCode(entry);
		// check results
		assert (code instanceof Organization);
		// method not visible: assert (code.getCodeType().equals(CodeType.orgId));
		assert (code.getCodeId().equals(orgCode));
		assert (code.getFieldValue(Organization.F_ORG_TITLE).equals(orgTitle));
	}


	public void testEntryToWorkLocation() throws Exception{
		// init the connector
		//CodesConnector cc = new CodesConnector();
		//cc.initialize(new MockConnector(WorkLocation.TABLENAME));
		Converter converter = new Converter(WorkLocation.TABLENAME);
		// set up code id and value
		String addr1 = "600 N. Charles Street";
		String city = "Baltimore";
		String postalCode = "21201";
		String state ="MD";
		String locCode ="Walters Art Museum";
		
		// create entry
		Entry entry = new Entry();
		entry.addAttributeValue(WorkLocation.F_ADDRESS1.getName(),addr1);
		entry.addAttributeValue(WorkLocation.F_ADDRESS2.getName(),null);
		entry.addAttributeValue(WorkLocation.F_CITY.getName(),city);
		entry.addAttributeValue(WorkLocation.F_POSTALCODE.getName(),postalCode);
		entry.addAttributeValue(WorkLocation.F_STATE.getName(),state);
		entry.addAttributeValue(WorkLocation.F_WORK_LOCATION_CODE.getName(),locCode);
		// convert to Code
		//AbstractCode code = cc.entryToAbstractCode(entry);
		AbstractCode code = converter.entryToAbstractCode(entry);
		// check results
		assert (code instanceof WorkLocation);
		// method not visible: assert (code.getCodeType().equals(CodeType.workLocationCode));
		assert (code.getCodeId().equals(locCode));
		assert (code.getFieldValue(WorkLocation.F_ADDRESS1).equals(addr1));
		assert (code.getFieldValue(WorkLocation.F_ADDRESS2)== null);
		assert (code.getFieldValue(WorkLocation.F_CITY).equals(city));
		assert (code.getFieldValue(WorkLocation.F_STATE).equals(state));
		assert (code.getFieldValue(WorkLocation.F_POSTALCODE).equals(postalCode));
	}

	// mock connector with enough to get through these tests
	public class MockConnector extends com.ibm.di.connector.Connector {
		
		private String _tableName;
		
		public String getVersion(){
			return ("LC 4.0 Mock Connector");
		}
		
		public MockConnector(String tableName){
			_tableName = tableName;			
		}
		
		public String getParam(String param){
			String rtnVal = null;
			if ("tableName".equals(param)){
				rtnVal = _tableName;
			}
			return rtnVal;
		}
	}

	// IMPORTANT: This code is a replica of CodesConnector.codeToEntry. Need to remove this
	// when log4j, slf4j stackoverflow issue is resolved.
	public class Converter{
		private String _tableName;
		
		public Converter(String tableName){
			_tableName = tableName;
		}

		public AbstractCode entryToAbstractCode(Entry entry)
		{
			// get the code class for the input tableName
			Class<? extends AbstractCode<?>> codeClass = AbstractCode.getCodeClassByName(_tableName);
			if (codeClass == null){
				throw new TDICodeBlockException("err_unSupport_tableName");
			}
			// get the id CodeField for this code (e.g. 'countryCode')
			CodeField codeIdField =AbstractCode.getCodeIdField(codeClass);
			// get the code id value from the entry (e.g. 'us')
			String codeId = entry.getString(codeIdField.getName());
			if ( codeId == null ){
				throw new TDICodeBlockException("err_codeIdInEntry_null");
			}
			// get the fieldValue mappings from the entry (e.g. <'displayValue','United States'>)
			Map<String, String> codeFieldValueMap = new HashMap<String, String>();
			codeFieldValueMap = this.getCodeFieldValueMap(entry, codeClass);
			// now create the Abstract Code
			AbstractCode rtnVal = createCodeItem(codeClass,codeId,codeFieldValueMap,Tenant.SINGLETENANT_KEY);
			return rtnVal;
		}
		// TODO we should have a CodeFactory in the code package
		private AbstractCode createCodeItem(
				Class<? extends AbstractCode<?>> codeClass,
						String codeId,
						Map<String, String> codeFieldValueMap,
						String tenantId){
			AbstractCode rtnVal = null;
			if (codeClass.isAssignableFrom(Country.class)){
				rtnVal = new Country(codeId,tenantId,codeFieldValueMap);
			}
			else if (codeClass.isAssignableFrom(Organization.class)){
				rtnVal = new Organization(codeId,tenantId,codeFieldValueMap);
			}
			else if (codeClass.isAssignableFrom(WorkLocation.class)){
				rtnVal = new WorkLocation(codeId,tenantId,codeFieldValueMap);
			}
			else if (codeClass.isAssignableFrom(EmployeeType.class)){
				rtnVal = new EmployeeType(codeId,tenantId,codeFieldValueMap);
			}
			else if (codeClass.isAssignableFrom(Department.class)){
				rtnVal = new Department(codeId,tenantId,codeFieldValueMap);
			}
			else{
				throw new TDICodeBlockException("err_unSupport_tableName");
			}
			return rtnVal;
		}

		// create a mapping of code fields to values from an input entry. use the registered
		// field mappings in AbstractCode class to extract values.
		private Map<String, String> 
		getCodeFieldValueMap(Entry tdiEntry, Class<? extends AbstractCode<?>> codeClass) {
			// get the CodeFields relevant to this code type
			List<CodeField> fields = AbstractCode.getFieldDefs(codeClass);		
			Map<String, String> codeFieldValueMap = new HashMap<String,String>( fields.size() * 2 );
			for (CodeField field : fields) {
				codeFieldValueMap.put( field.getName(),  tdiEntry.getString(field.getName()));
			}
			return codeFieldValueMap;
		}
	}
}
