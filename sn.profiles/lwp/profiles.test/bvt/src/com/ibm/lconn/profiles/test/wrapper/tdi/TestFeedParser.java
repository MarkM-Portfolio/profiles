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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.di.entry.Entry;
import com.ibm.lconn.profiles.api.tdi.service.impl.ProfilesTDICRUDServiceImpl;
import com.ibm.lconn.profiles.api.tdi.util.TDIServiceHelper;
import com.ibm.lconn.profiles.data.ProfileDescriptor;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.ProfileExtensionService;
import com.ibm.lconn.profiles.internal.service.ProfileLoginService;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.TestAppContext;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.service.PeoplePagesService;

import junit.framework.TestCase;

public class TestFeedParser extends BaseTransactionalTestCase{
	private TDIProfileService service = null;
	static {
		Logger.getLogger("org.springframework.orm.ibatis.SqlMapClientTemplate").setLevel(Level.FINEST);
	}
	protected void onSetUpBeforeTransactionDelegate() {
		if (service == null) {
			service = AppServiceContextAccess.getContextObject(TDIProfileService.class);
		}
		
		runAsAdmin(Boolean.TRUE);
	}
//	public void testVcardParser() throws Exception {
//		ProfileDescriptor descriptor = null;
//		InputStream is = TestFeedParser.class.getResourceAsStream("test.xml");
//		String atom = convertStreamToString(is);
//		descriptor = TDIServiceHelper.feedsToDescriptor(atom);
//		System.out.println("Descriptor: " + descriptor);
//		String key = service.create(descriptor);
//		System.out.println(key);
//	}
	
//	public void testVcardGen() throws Exception{
//		ProfileDescriptor descriptor = null;
//		InputStream is = TestFeedParser.class.getResourceAsStream("test.xml");
//		String atom = convertStreamToString(is);
//		descriptor = TDIServiceHelper.feedsToDescriptor(atom);
//		String feeds = TDIServiceHelper.descriptorToFeeds(descriptor, "localhost");
//		System.out.println(feeds);
//	}
	
	public void testDelete() throws Exception{
		service.delete("123");
	}
	public String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            e.getMessage();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
	

}
