/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.metrics;

import java.util.HashMap;
import com.ibm.peoplepages.internal.service.cache.SystemMetrics;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;


public class SystemMetricsTest extends BaseTransactionalTestCase {

	public void onSetUpBeforeTransactionDelegate() throws Exception {
		CreateUserUtil.setTenantContext();
		runAsAdmin(Boolean.TRUE);
	}

	@Override
	protected void onSetUpInTransaction() {
	}

	public void onTearDownAfterTransaction() throws Exception {
	}

	@SuppressWarnings("rawtypes")
	public void testSystemMetrics() throws Exception {
		String[] keyNames =  SystemMetrics.getMetricKeyNameArray();
		// interfaces uses raw types - it is <String,String>
		HashMap sm = SystemMetrics.fetchMetrics();
		String val;
		String badVal = ""+SystemMetrics.BAD_VAL; // should be -1
		for (String key : keyNames){
			// ejb calls for board (activity stream) cannot work. these will return
			// BAD_VAL. otherwise we should get a count of at least 0 or a string
			// response not equal to BAD_VAL for the other metrics.
			if ( key.contains("board") == false ){
				val = (String)sm.get(key);
				assertNotNull(val);
				assertFalse("Incorrect metric "+key+" with value "+val,badVal.equals(val));
			}
		}
	}
}
