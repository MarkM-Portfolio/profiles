/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test;

import junit.framework.Assert;

import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;
import com.ibm.peoplepages.util.appcntx.MockAdmin;

/**
 * Creates a random user to execute 
 */
public class CreateRandomUser extends BaseTransactionalTestCase
{
	public void testCreateRandomUser()
	{
		TestAppContext ctx = (TestAppContext)AppContextAccess.getContext();
		boolean  isAdmin  = ctx.isAdmin();
		Employee currUser = ctx.getCurrentUserProfile();
		try {
			// Compliance Events need 'actor' info
			ctx.setCurrUser(MockAdmin.INSTANCE,true);
			Employee actor = ctx.getCurrentUserProfile(); // check is Mock Admin

			Employee e = CreateUserUtil.createProfile();
			System.out.println("Created user with key: " + e.getKey());
			transactionManager.commit(transactionStatus);
			ctx.setCurrUser(currUser, isAdmin);
		}
		catch(Exception e){
			Assert.fail("Failed setting MockAdmin for CreateRandomUser: " + e.getMessage());
		}
		finally {
			ctx.setAdministrator(isAdmin);
		}
	}
}
