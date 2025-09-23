/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.atomxslt;

import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import com.ibm.lconn.profiles.internal.util.XMLUtil;

/**
 * @author <a href="mailto:rapena@us.ibm.com">Ronny A. Pena</a>
 */
public class RecentFriendTestCase extends TestCase
{
	String serverURL = "http://rapena.dyn.webahead.ibm.com";
	
	public void testBaseTest() throws Exception
	{
		System.out.println("gettting atom feed");
		InputStream xmlDocInputStream = null;
		xmlDocInputStream = XSLTUtil.getInputStream( serverURL + "/profiles/atom2/recentfriends.xml?uid=688635897");
		
//			xmlDocInputStream = new FileInputStream(
//					"C:/Documents and Settings/Administrator/clearcase/rpena_LC2_0_sn_profiles/socnet.05/sn.profiles/lwp/peoplepages.web/WebContent/tools/reporttochain.xml");

		FileInputStream xslTemplateInputStream = new FileInputStream(
				"../peoplepages.web/WebContent/xslt/friends/recent-friends.xsl");

		Document parseXmlFile = XSLTUtil.JAXParseXmlFile(xmlDocInputStream, xslTemplateInputStream, null);
		
//		int length = parseXmlFile.selectNodes("/html:div/html:div/html:ul/html:li").getLength();
		
//		System.out.println("number of nodes: " + length + "\n");
//		assertEquals(2, length);
//		
		XMLUtil.serialize(parseXmlFile, System.out);
		
		xmlDocInputStream.close();
		xslTemplateInputStream.close();
	}
}
