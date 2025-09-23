/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.web.ui.extensions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.exception.DefaultExceptionHandler;

import com.ibm.lconn.profiles.internal.exception.AssertionException;
import com.ibm.lconn.profiles.internal.exception.AssertionType;

public class ExceptionHandler extends DefaultExceptionHandler {

  private static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
  private static final String PRAGMA_AUTHENTICATE_XHR = "XHR";
  
  public Resolution handleAssertionException(AssertionException exception, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
    AssertionType type = exception.getType();
    switch (type) {
      case UNAUTHORIZED_ACTION:
        return handleAuthorizationError(request, response);
        
      case USER_NOT_FOUND: 
        return handleUserNotFoundError(request, response);
        
      case RESOURCE_NOT_FOUND:
        return handleResourceNotFoundError(request, response);
    }
    
    return new ErrorResolution(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
  }
  
  protected Resolution handleAuthorizationError(HttpServletRequest request, HttpServletResponse response) {
    if (request.getRemoteUser() == null) {
      response.setHeader(HEADER_WWW_AUTHENTICATE, PRAGMA_AUTHENTICATE_XHR);
      return new ErrorResolution(HttpServletResponse.SC_UNAUTHORIZED); 
    }
    else {
      return new ErrorResolution(HttpServletResponse.SC_FORBIDDEN);
    }
  }
  
  protected Resolution handleUserNotFoundError(HttpServletRequest request, HttpServletResponse response) {
    return new ErrorResolution(HttpServletResponse.SC_BAD_REQUEST);
  }

  protected Resolution handleResourceNotFoundError(HttpServletRequest request, HttpServletResponse response) {
    return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND);
  }
  
}
