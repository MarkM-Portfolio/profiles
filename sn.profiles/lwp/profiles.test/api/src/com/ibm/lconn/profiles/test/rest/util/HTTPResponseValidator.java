/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2010, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

public class HTTPResponseValidator
{
  public static final Map<String, String> HEADERS_CREATED = new HashMap<String, String>(1);
  static
  {
    // we only want to validate existence of these headers on create per atom
    HEADERS_CREATED.put("Location", "");
    // HEADERS_CREATED.put("Content-Length", "");
    HEADERS_CREATED.put("Content-Type", "");
    // HEADERS_CREATED.put("Location", "");
    // TODO Work with live to know how we should use ETag
  }
  
  public static final HTTPResponseValidator IGNORE = new HTTPResponseValidator(-1);
  
  public static final HTTPResponseValidator OK = new HTTPResponseValidator(200);

  public static final HTTPResponseValidator CREATED = new HTTPResponseValidator(201, HEADERS_CREATED);
  
  public static final HTTPResponseValidator CREATED_NO_HEADER = new HTTPResponseValidator(201);

  public static final HTTPResponseValidator NO_CONTENT = new HTTPResponseValidator(204);

  public static final HTTPResponseValidator BAD_REQUEST = new HTTPResponseValidator(400);

  public static final HTTPResponseValidator UNAUTHORIZED = new HTTPResponseValidator(401);

  public static final HTTPResponseValidator FORBIDDEN = new HTTPResponseValidator(403);

  public static final HTTPResponseValidator NOT_FOUND = new HTTPResponseValidator(404);

  public static final HTTPResponseValidator METHOD_NOT_ALLOWED = new HTTPResponseValidator(405);

  public static final HTTPResponseValidator CONFLICT = new HTTPResponseValidator(409);
  
  public static final HTTPResponseValidator NOT_SUPPORTED = new HTTPResponseValidator(501);

  private final int expectedStatusCode;

  private final Map<String, String> expectedHeaders;

  public HTTPResponseValidator(int expectedStatusCode)
  {
    this(expectedStatusCode, new HashMap<String, String>(0));
  }

  public HTTPResponseValidator(int expectedStatusCode, Map<String, String> expectedHeaders)
  {
    this.expectedStatusCode = expectedStatusCode;
    this.expectedHeaders = expectedHeaders;
  }
    
  public boolean isErrorExpected()
  {
	return expectedStatusCode >= 400;
  }

  public void validate(ClientResponse response)
  {
	  Assert.assertNotNull(response);
	  if (expectedStatusCode > 0)
		  Assert.assertEquals(expectedStatusCode, response.getStatus());

	  // verify that the response has the headers we need it to have, if no value is provided for a header, we only check that is specified
	  List<String> responseHeaders = Arrays.asList(response.getHeaderNames());
//	  System.out.println("Response Headers");
//	  for (Iterator iterator = responseHeaders.iterator(); iterator.hasNext(); )
//	  {
//		  String header = (String) iterator.next();
//		  System.out.println(header + " : " + response.getHeader(header) + " : " + response.getHeader(header.toLowerCase()));
//	  }
	  for (String headerToValidate : expectedHeaders.keySet())
	  {
		  // cloud lower-cases response header key names !
		  String lowerCaseHeader = headerToValidate.toLowerCase(); // 'cos on SC we get it as  lower case !!
//	      Assert.assertEquals(headerToValidate, true, responseHeaders.contains(headerToValidate));
		  Assert.assertEquals(headerToValidate, true, 
				  (responseHeaders.contains(headerToValidate) || responseHeaders.contains(lowerCaseHeader)));
		  String value = response.getHeader(headerToValidate);
		  String expected = expectedHeaders.get(headerToValidate);
		  if (expected != null && expected.length() > 0)
		  {
			  Assert.assertEquals(value, expected);
		  }
	  }
  }

  public void validate(HttpMethodBase methodBase)
  {
    Assert.assertNotNull(methodBase);
    Assert.assertEquals(expectedStatusCode, methodBase.getStatusCode());
    // verify that the response has the headers we need it to have, if no value is provided for a header, we only check that is specified
    Header[] headers = methodBase.getResponseHeaders();
    List<String> responseHeaders = new ArrayList<String>(headers.length);
    for (Header h : headers)
    {
      responseHeaders.add(h.getName());
    }

    for (String headerToValidate : expectedHeaders.keySet())
    {
      Assert.assertEquals(headerToValidate, true, responseHeaders.contains(headerToValidate));
      Header header = methodBase.getResponseHeader(headerToValidate);
      String value = header.getValue();
      String expected = expectedHeaders.get(headerToValidate);
      if (expected != null && expected.length() > 0)
      {
        Assert.assertEquals(value, expected);
      }
    }
  }

  public HTTPResponseValidator from(Map<String, String> newHeaders)
  {
    newHeaders.putAll(expectedHeaders);
    return new HTTPResponseValidator(this.expectedStatusCode, newHeaders);
  }
}
