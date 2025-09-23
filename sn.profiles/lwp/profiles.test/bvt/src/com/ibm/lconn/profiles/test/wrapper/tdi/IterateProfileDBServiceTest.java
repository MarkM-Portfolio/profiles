/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.wrapper.tdi;

import junit.framework.TestCase;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.api.tdi.service.IterateProfileDBService;
import com.ibm.lconn.profiles.api.tdi.service.impl.IterateProfileDBServiceImpl;
/**
 * 
 * @author Liang Chen
 *
 */
public class IterateProfileDBServiceTest extends TestCase{
	IterateProfileDBService ipdbSvc;

	@Override
	protected void setUp() throws Exception {
		
		IterateProfileDBServiceImpl ipdbSvcImpl = new IterateProfileDBServiceImpl();
		ipdbSvc = (IterateProfileDBService) ipdbSvcImpl;
	}
    
	public void testNullSvc(){
		assertNotNull(ipdbSvc);
	}
	
//	public void testGetNextProfileEntry(){
//		Entry profileEntry = null;
//		int count = 0;
//		do{
//			profileEntry = ipdbSvc.getNextProfileEntry();
//			if(profileEntry!=null){
//				count++;
//			}
//		}while(profileEntry!=null);
//		System.out.println("Total number of entries: " + count);
//		assertTrue(count>0);
//	}
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
	
	

}
