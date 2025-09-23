/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import com.ibm.connections.highway.common.api.HighwaySettingNames;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.util.RoleTestHelper;
import com.ibm.peoplepages.data.Employee;

public class ProfilesRoleServiceTest extends BaseTransactionalTestCase {

	private TDIProfileService service = null;
	private RoleDao roleDao = null;
	private Employee employeeA;
	private Employee employeeB;
	private Employee employeeExternal;
	List<EmployeeRole> autoRoles;

	protected void onSetUpBeforeTransactionDelegate() {
		if (service == null) service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		if (roleDao == null) roleDao = AppServiceContextAccess.getContextObject(RoleDao.class);
		// run as admin below
	}

	@Override
	protected void onSetUpInTransaction() {
		// we run as admin with employeeA
		employeeA = CreateUserUtil.createProfile("employeeA", "employeeAemail", null);
		runAs(employeeA, true);
		employeeB = CreateUserUtil.createProfile("employeeB", "employeeBemail", null);
		employeeExternal = CreateUserUtil.createExternalProfile();
		autoRoles = service.getRoles(employeeB.getKey());
	}

	@Override
	public void onTearDownAfterTransaction() throws Exception {
	}

	public void testInvalidRoles() throws Exception {
		// EmployeeRole must be ascii and no whitespace.
		List<String> invalidSet = createInvalid();
		String s;
		for (int i = 0; i < invalidSet.size(); i++) {
			s = invalidSet.get(i);
			// System.out.println(s+" : "+EmployeeRole.isValid(s));
			assertFalse("role id should be invalid: " + s + " bytes: ", EmployeeRole.isValid(s));
		}
	}

// addRoles not exposed in 5.0
//	public void testCreateRoles() {
//		String[] roleIds = new String[] { "role1", "role2", "role3" };
//		List<EmployeeRole> roles = RoleTestHelper.createRoles(employeeB, roleIds);
//		List<EmployeeRole> mergedRoles = RoleTestHelper.AunionB(roles,autoRoles);
//		service.addRoles(employeeB.getKey(), roles);
//		// retrieve roles
//		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
//		assertTrue(RoleTestHelper.rolesMatch(mergedRoles, rolesDB));
//	}
	
	public void testSetRoles(){
		String[] roleIdsOne = new String[] { "role1", "role2", "role3" };
		String[] roleIdsTwo = new String[] { "role2", "role3", "role4" };
		List<EmployeeRole> rolesOne= RoleTestHelper.createRoles(employeeB, roleIdsOne);
		List<EmployeeRole> rolesTwo = RoleTestHelper.createRoles(employeeB, roleIdsTwo);
		// setRoles should replace the existing roles with the input set
		service.setRoles(employeeB.getKey(), rolesOne); 
		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
		assertTrue(rolesDB.size() == rolesOne.size());
		assertTrue(RoleTestHelper.rolesMatch(rolesOne, rolesDB));
		// reset the roles
		service.setRoles(employeeB.getKey(), rolesTwo);
		rolesDB = service.getRoles(employeeB.getKey());
		assertTrue(rolesDB.size() == rolesTwo.size());
		assertTrue(RoleTestHelper.rolesMatch(rolesTwo, rolesDB));
	}
	
	public void testMissingRole(){
		// if a role is missing in the database, we are to back fill it with a bootstrap role as set
		// during account creation. this state should be an anomoly and we'll test by creating a user
		// who should get the bootstrap role. then delete the role using the dao class, then retrieve
		// the user.
		// step 1: reset roles for employee user and make sure we see the update
		String[] roleIdsOne = new String[] { "role1", "role2", "role3" };
		List<EmployeeRole> rolesOne= RoleTestHelper.createRoles(employeeB, roleIdsOne);
		service.setRoles(employeeB.getKey(), rolesOne);
		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
		assertTrue(RoleTestHelper.rolesMatch(rolesOne, rolesDB));
		// step2: now delete these roles - we can't check for empty as the DB layer itself back fills the role
		roleDao.deleteRoles(employeeB.getKey());
		// step 3: retrieve the roles. we shold get the bootstrap role
		rolesDB = service.getRoles(employeeB.getKey());
		assertTrue(RoleTestHelper.checkBootstrapRole(employeeB,rolesDB));
		
		// repeat for the external user
		// step 1:
		rolesOne= RoleTestHelper.createRoles(employeeExternal, roleIdsOne);
		service.setRoles(employeeExternal.getKey(), rolesOne);
		rolesDB = service.getRoles(employeeExternal.getKey());
		assertTrue(RoleTestHelper.rolesMatch(rolesOne, rolesDB));
		// step2:
		roleDao.deleteRoles(employeeExternal.getKey());
		// step 3:
		rolesDB = service.getRoles(employeeExternal.getKey());
		assertTrue(RoleTestHelper.checkBootstrapRole(employeeExternal,rolesDB));
	}

	public void testDuplicateRoles() {
		String[] roleIdsWithDupe = new String[] { "ROle1", "ROLE1", "role2" };
		String[] roleIdsNoDupe = new String[] { "role1", "role2" };
		List<EmployeeRole> rolesWithDupe = RoleTestHelper.createRoles(employeeB, roleIdsWithDupe);
		List<EmployeeRole> rolesNoDupe = RoleTestHelper.createRoles(employeeB, roleIdsNoDupe);
		//service.addRoles(employeeB.getKey(), rolesWithDupe);
		service.setRoles(employeeB.getKey(), rolesWithDupe);
		// retrieve roles
		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
		assertTrue(rolesDB.size() == rolesNoDupe.size());
		assertTrue(RoleTestHelper.rolesMatch(rolesNoDupe, rolesDB));
	}

// addRoles not exposed in 5.0
//	public void testAddRoles() {
//		String[] roleIdsOne = new String[] { "rolE1", "rOLE2", "ROle3", "role4" };
//		List<EmployeeRole> rolesOne = RoleTestHelper.createRoles(employeeB, roleIdsOne);
//		String[] roleIdsOneLower = RoleTestHelper.toLower(roleIdsOne);
//		List<EmployeeRole> rolesOneLower = RoleTestHelper.createRoles(employeeB, roleIdsOneLower);
//		String[] roleIdsTwo = new String[] { "ROLE3", "rOLE4", "role5", "ROLe6" };
//		List<EmployeeRole> rolesTwo = RoleTestHelper.createRoles(employeeB, roleIdsTwo);
//		String[] roleIdsAll = new String[] { "role1", "role2", "role3", "role4", "role5", "role6" };
//		List<EmployeeRole> rolesAll = RoleTestHelper.createRoles(employeeB, roleIdsAll);
//		rolesAll = RoleTestHelper.AunionB(rolesAll,autoRoles);
//		// add and check set one
//		service.addRoles(employeeB.getKey(), rolesOne);
//		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
//		assertTrue(rolesDB.size() == (rolesOne.size()+autoRoles.size()));
//		RoleTestHelper.rolesMatch(RoleTestHelper.AunionB(rolesOneLower,autoRoles), rolesDB);
//		// add set two
//		service.addRoles(employeeB.getKey(), rolesTwo);
//		rolesDB = service.getRoles(employeeB.getKey());
//		assertTrue(rolesDB.size() == roleIdsAll.length+autoRoles.size());
//		assertTrue(RoleTestHelper.rolesMatch(rolesAll, rolesDB));
//	}

//addRoles, deleteRoles not exposed in 5.0
//	public void testDeleteEmployeeRoles() {
//		String[] roleIds = new String[] { "role1", "role2", "role3" };
//		List<EmployeeRole> roles = RoleTestHelper.createRoles(employeeB, roleIds);
//		service.addRoles(employeeB.getKey(), roles);
//		// delete all roles
//		service.deleteRoles(employeeB.getKey());
//		// retrieve roles
//		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
//		// we pick up the default based on the user mode
//		//assertTrue(rolesDB.size() == 0);
//		assertTrue(RoleTestHelper.checkBootstrapRole(employeeB,rolesDB));
//		
//		// do the same for an external employee
//		service.addRoles(employeeExternal.getKey(), roles);
//		// delete all roles
//		service.deleteRoles(employeeExternal.getKey());
//		rolesDB = service.getRoles(employeeExternal.getKey());
//		RoleTestHelper.checkBootstrapRole(employeeExternal,rolesDB);
//	}

// addRoles, deleteRoles not exposed in 5.0
//	public void testDeleteSomeEmployeeRoles() {
//		String[] roleIds = new String[] { "rolE1", "rOLE2", "ROle3", "role4" };
//		List<EmployeeRole> roles = RoleTestHelper.createRoles(employeeB, roleIds);
//		service.addRoles(employeeB.getKey(), roles);
//		// delete some roles
//		String[] deleteRoleIds = new String[] { "rolE3", "Role4" };
//		String[] remainingRoleIds = new String[] { "role1", "role2" };
//		List<EmployeeRole >remainingRoles =  RoleTestHelper.createRoles(employeeB, remainingRoleIds);
//		remainingRoles = RoleTestHelper.AunionB(remainingRoles,autoRoles);
//		List<String> deleteIds = Arrays.asList(deleteRoleIds);
//		service.deleteRoles(employeeB.getKey(), deleteIds);
//		// retrieve roles
//		List<EmployeeRole> rolesDB = service.getRoles(employeeB.getKey());
//		assertTrue(RoleTestHelper.rolesMatch(remainingRoles, rolesDB));
//	}

	private static List<String> createInvalid() throws Exception {
		ArrayList<String> rtn = new ArrayList<String>();
		// null is invalid
		rtn.add(null);
		// dec 00-32, hex 00-40 are whitespace
		for (int i = 0; i < 32; i++) {
			StringBuffer sb = new StringBuffer("a");
			char c = (char) i;
			sb.append(c);
			sb.append("c");
			rtn.add(sb.toString());
			// System.out.println(sb.toString());
		}
		// dec 127 (DEL) is also whitespace
		StringBuffer sb = new StringBuffer("a");
		char c = (char) 127;
		sb.append(c);
		sb.append("c");
		rtn.add(sb.toString());
		// add non-ascii char
		sb = new StringBuffer("z");
		byte[] bad = new byte[] { (byte) 0xc3, (byte) 0xa1 };// c3 a1 (acute a - html &aacute;)
		String badString = new String(bad, "UTF-8");
		// printBinary(badString);
		sb.append(badString);
		rtn.add(sb.toString());
		// e2 80 8b is an null space
		sb = new StringBuffer();
		byte a = 0x61;
		byte b = 0x62;		
		byte signedByte = -1;
		int unsigned_e2 = signedByte & (0xe2);
		System.out.println(unsigned_e2);
		byte bytee2 = (byte) (unsigned_e2 & 0xff);
		int unsigned_80 = signedByte & (0x80);
		byte byte80 = (byte) (unsigned_80 & 0xff);
		int unsigned_8b = signedByte & (0x8b);
		byte byte8b = (byte) (unsigned_8b & 0xff);
		bad = new byte[] { a, bytee2, byte80, byte8b, b };
		// printByte(bad);
		badString = new String(bad, "UTF-8");
		rtn.add(badString);
		//
		return rtn;
	}

	private static void printBinary(String s) {
		byte[] bytes = s.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes) {
			int val = b;
			for (int i = 0; i < 8; i++) {
				binary.append((val & 128) == 0 ? 0 : 1);
				val <<= 1;
			}
			binary.append(' ');
		}
		System.out.println("'" + s + "' to binary: " + binary);
	}

	// this needs checking. not sure it is accurate
	// private static void printByte(byte[] bytes) {
	// StringBuilder binary = new StringBuilder();
	// for (byte b : bytes) {
	// int val = b;
	// for (int i = 0; i < 8; i++) {
	// binary.append((val & 128) == 0 ? 0 : 1);
	// val <<= 1;
	// }
	// binary.append(' ');
	// }
	// System.out.println("bytes to binary: " + binary);
	// }
	
	public static void main(String[] args){
		StringBuffer sb = new StringBuffer();
		byte a = 0x61;
		byte b = 0x62;		
		byte signedByte = -1;
		System.out.println(signedByte);
		int unsigned_e2 = signedByte & (0xe2);
		System.out.println(unsigned_e2);
		byte bytee2 = (byte) (unsigned_e2 & 0xff);
		int unsigned_80 = signedByte & (0x80);
		byte byte80 = (byte) (unsigned_80 & 0xff);
		int unsigned_8b = signedByte & (0x8b);
		byte byte8b = (byte) (unsigned_8b & 0xff);
		byte[] bad = new byte[] { a, bytee2, byte80, byte8b, b };
		// printByte(bad);
		try{
		String badString = new String(bad, "UTF-8");
		System.out.println(badString);
		}
		catch(Exception e){
			System.out.println(e);
		}
		}
}
