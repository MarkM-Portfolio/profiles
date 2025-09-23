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

import java.util.Arrays;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import com.ibm.lconn.profiles.data.EmployeeRole;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.store.interfaces.RoleDao;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.lconn.profiles.test.util.RoleTestHelper;
import com.ibm.peoplepages.data.Employee;

public class RoleDaoTest extends BaseTransactionalTestCase {

	private Employee employee;
	private RoleDao roleDao;
	private List<EmployeeRole> autoRoles; // automatically assigned roles
	
	public void onSetUpBeforeTransactionDelegate() throws Exception {
		if (roleDao == null) roleDao = AppServiceContextAccess.getContextObject(RoleDao.class);	
	}
	
	@Override
	protected void onSetUpInTransaction() {
		employee = CreateUserUtil.createProfile();
		autoRoles = roleDao.getRoles(employee.getKey());
	}
	
	public void onTearDownAfterTransaction() throws Exception{
	}

	public void testCreateRoles() throws Exception{
		String[]roleIds = new String[]{"id1","id2","id3"};
		List<EmployeeRole> roles = RoleTestHelper.createRoles(employee,roleIds);
		persistRoles(employee,roles);
		List<EmployeeRole> rolesDB = roleDao.getRoles(employee.getKey());
		List<EmployeeRole> rolesMerged = RoleTestHelper.AunionB(autoRoles,roles);
		boolean ok = RoleTestHelper.rolesMatch(rolesMerged,rolesDB);
		assertTrue(ok);
	}
	
	public void testCreateDuplicate() throws Exception{
		String[]roleIds = new String[]{"id1","id1"};
		try{
			List<EmployeeRole> roles = RoleTestHelper.createRoles(employee,roleIds);
			persistRoles(employee,roles);
			assertTrue("duplicate roles not allowed",false);
		}
		catch (Exception e){
			assertTrue(e instanceof DataIntegrityViolationException);
		}
	}
	
	public void testDeleteByEmployee() throws Exception{
		String[]roleIds = new String[]{"id1","id2","id3"};
		List<EmployeeRole> roles = RoleTestHelper.createRoles(employee,roleIds);
		persistRoles(employee,roles);
		List<EmployeeRole> rolesDB = roleDao.getRoles(employee.getKey());
		assertTrue(rolesDB.size()==(roles.size()+autoRoles.size()));
		roleDao.deleteRoles(employee.getKey());
		rolesDB = roleDao.getRoles(employee.getKey());
		//assertTrue(rolesDB.size()==0);
		assertTrue(RoleTestHelper.checkBootstrapRole(employee,rolesDB));
	}
	
	public void testDeleteByEmpAndRoleIds() throws Exception{
		String[]roleIds = new String[]{"id1","id2","id3","id4","id5"};
		List<EmployeeRole> roles = RoleTestHelper.createRoles(employee,roleIds);
		persistRoles(employee,roles);
		List<EmployeeRole> rolesDB = roleDao.getRoles(employee.getKey());
		assertTrue(rolesDB.size()==(roles.size()+autoRoles.size()));
		// delete a couple roles for this user
		String[] deleteIds = new String[]{"id4","id5"};
		List<EmployeeRole> remainingRoles =  RoleTestHelper.createRoles(employee, new String[]{"id1","id2","id3"});
		remainingRoles = RoleTestHelper.AunionB(remainingRoles,autoRoles);
		roleDao.deleteRoles(employee.getKey(),Arrays.asList(deleteIds));
		rolesDB = roleDao.getRoles(employee.getKey());
		assertTrue(rolesDB.size()==remainingRoles.size());
		boolean ok = RoleTestHelper.rolesMatch(remainingRoles,rolesDB);
		assertTrue(ok);
	}
	
	private void persistRoles(Employee e, List<EmployeeRole> roles){
		if ( roles == null || roles.size() == 0){
			return;
		}
		roleDao.addRoles(e.getKey(),roles);
	}
}
