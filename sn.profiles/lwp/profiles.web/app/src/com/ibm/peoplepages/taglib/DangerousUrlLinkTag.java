/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.taglib;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspFragment;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.ibm.lconn.core.web.secutil.DangerousUrlHelper;

public class DangerousUrlLinkTag extends SimpleTagSupport {

    private String _url;
    private String _var;
    private String _scope;
    
    /*
     * @see com.ibm.workplace.util.portal.taglib.OpenActivitiesTag#doStartTagImpl()
     */
    public void doTag() throws JspException {
	try {
	    
	    PageContext pageContext = (PageContext)getJspContext();
	    
	    String protectedUrl = generateProtectedURL(_url,DangerousUrlHelper.getNonce(pageContext));
	    
	    saveObjectInVar(protectedUrl);
	    
	} 
	catch (Exception e) {
	    throw new JspException(e);
	}
    }

    private String generateProtectedURL(String url,String nonce){
	if(url == null)
	    return url;
	
	StringBuffer sbuffer = new StringBuffer(url);
	if(url.indexOf("?") == -1)
	    sbuffer.append("?");
	else
	    sbuffer.append("&");
	
	sbuffer.append(DangerousUrlHelper.DANGEROUS_NONCE);
	sbuffer.append("=");
	sbuffer.append(nonce);
	
	return sbuffer.toString();	
    }
    
    
    /**
     * @return Returns the url.
     */
    public String getUrl() {
	return _url;
    }
    
    /**
     * @param url
     *            The url to set.
     */
    public void setUrl(String url) {
	this._url = url;
    }

    /**
     * Set the input object to the scope and var defined by the "var" and "scope" tag attributes. This method is named
     * saveObjectInVar() instead of the more consistent setObjectInVar to avoid exposing "objectInVar" as a tag
     * attribute.
     *
     * @param o
     *            the Object to be saved in an attribute
     */
    public void saveObjectInVar( Object o) throws IllegalArgumentException
    {
        if( getVar( ) == null) {
            throw new IllegalArgumentException( "var == null");
        }

        String toScope = getScope( );
        // Expose this value as a scripting variable
        int inScope = PageContext.PAGE_SCOPE;

        if( toScope == null || "page".equals( toScope)) {
            inScope = PageContext.PAGE_SCOPE;
        }
        else if( "request".equals( toScope)) {
            inScope = PageContext.REQUEST_SCOPE;
        }
        else if( "session".equals( toScope)) {
            inScope = PageContext.SESSION_SCOPE;
        }
        else if( "application".equals( toScope)) {
            inScope = PageContext.APPLICATION_SCOPE;
        }

        getJspContext( ).setAttribute( getVar( ), o, inScope);
    }

    /**
     * Get the scope within which to set specified object.
     */
    public String getScope( )
    {
        return ( _scope);
    }

    /**
     * Set the scope within which to set specified object.
     *
     * @param scope
     *            the scope of the attribute to save the bean to
     */
    public void setScope( String scope)
    {
        _scope = scope;
    }

    /**
     * Get the name mapped to the specified object. If the attribute contains an EL expression, the expression is
     * evaluated first. If an error is thrown in EL evaluation, the value of the attribute is returned.
     */
    public String getVar( )
    {
        return _var;
    }

    /**
     * Set the scope within which to retrieve or save the specified object.
     *
     * @param var
     *            the name of the attribute to save the bean to
     */
    public void setVar( String var)
    {
        _var = var;
    }
}
