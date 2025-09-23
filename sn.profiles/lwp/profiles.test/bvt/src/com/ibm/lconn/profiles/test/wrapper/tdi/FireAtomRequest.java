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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.Response.ResponseType;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

public class FireAtomRequest {
	public static void main(String[] args){
		
		Abdera abdera = new Abdera();
		AbderaClient client = new AbderaClient(abdera);
		try {
			client.addCredentials("http://localhost:80/", "IBM Connections Profiles", "BASIC", 
					new UsernamePasswordCredentials("RestAdmin", "password"));
			client.setAuthenticationSchemeDefaults();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream is = FireAtomRequest.class.getResourceAsStream("test.xml");
		ClientResponse resp = client.post("http://localhost:80/profile",is);
		
		if (resp.getType() == ResponseType.SUCCESS) {
			System.out.println("success");
			System.out.println(resp.getContentLength());
			  //Document<Feed> doc = resp.getDocument();
		} else {
			System.out.println("error");
			  // there was an error
		}

//		Entry entry = abdera.newEntry();
////		 ...

		
		
//		URL url = new URL("http://hostname:80/createUser");
//        URLConnection conn = url.openConnection();
//        conn.setDoOutput(true);
//        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
//        wr.write(data);
//        wr.flush();
	}
}
