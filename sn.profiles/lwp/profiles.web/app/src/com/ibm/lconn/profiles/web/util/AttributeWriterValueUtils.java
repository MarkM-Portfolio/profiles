/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.web.util;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.peoplepages.functions.Functions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.util.CookieHelper;

/**
 * Provides logic on how to write the values for various UIAttributeConfig
 * types. This class only outputs the attribute value and not the prepend/append
 * html or the label
 * 
 *
 */
public class AttributeWriterValueUtils extends AttributeWriterUtils {
	
	public AttributeWriterValueUtils(
			final ServletContext context, 
			final HttpServletRequest request, 
			final Writer writer) 
	{
		super(context,request,writer);
	}
	
	/**
	 * Method for outputting an attribute value
	 * 
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	public final void writeUIAttrValue(final UIAttributeConfig attr, final boolean empty, final String value)
		throws IOException
	{
		// output vcard
		if (attr.getIsHcard()) {
			outputHCardAttrValue(attr, empty, value);
		} 
		
		// output blog
		else if (attr.getIsBlogUrl()) {
			outputBlogUrlAttrValue(attr, empty, value);
		}  
		
		// output link
		else if (attr.getIsLink()) {
			outputLinkAttrValue(attr, empty, value);
		} 
		
		// output sametime
		else if (attr.getIsSametimeLink()) {
			outputSametimeLinkAttrValue(attr, empty, value);
		} 
		
		// output email
		else if (attr.getIsEmail()) {
			outputEmailAttrValue(attr, empty, value);
		}
		
		// output normal attr including richtext
		else {
			outputRegularAttrValue(attr, empty, value);
		}
	}
	
	/**
	 * <span class="vcard">
	 *   <a undefined="1" href="/profiles/html/profileView.do?userid=ab41d440-773f-1029-9e32-882a644e52b4&amp;lang=en" class="fn url hasHover">MICHAEL I. AHERN</a>
	 * 		<span class="x-lconn-userid" style="display: none;">ab41d440-773f-1029-9e32-882a644e52b4</span>
	 * </span>
	 * 
	 * Special case for the Search results page looks like so:
	 * 
	 *	<span class="vcard">
     *  	<h3> <a undefined="2" href="/profiles/html/profileView.do?key=4617a711-d739-4263-82ec-b7ba049a7d41&amp;lang=en" class="fn url person hasHover">Ann M. Hayes</a></h3>
	 *		<span class="x-lconn-userid" style="display: none;">91ae7240-8f0a-1028-847b-db07163b51b2</span>
	 *	</span>
	 *
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	protected final void outputHCardAttrValue(final UIAttributeConfig attr, final boolean empty, final String value)
		throws IOException
	{
		write("<span class='vcard'>");
		{
			final String useridAttr = useridValue(attr);
			
			// special case for search
			final boolean searchResult = PeoplePagesServiceConstants.KEY.equals(attr.getUid());
			
			// writes <a href='...'>..</a>
			{
				if (searchResult) {
					write("<h3>");
					write("<a class='fn url person' href='").write(contextRoot()).write("/html/profileView.do?key=").writeEscUrl(uidValue(attr)).writeEscHtml("&lang=").write(lang).write("'>");
				}
				else {
					write("<a class='fn url' href='").write(contextRoot()).write("/html/profileView.do?userid=").writeEscUrl(useridAttr).writeEscHtml("&lang=").write(lang).write("'>");
				}
				
				writeAttrValueStr(attr, value);
				
				write("</a>");
				if (searchResult) {
					write("</h3>");
				}
			}			
			
			// <span class='...' />
			{
				if (StringUtils.isNotEmpty(useridAttr)) {
					write("<span class='x-lconn-userid' style='display: none;'>").writeEscHtml(useridAttr).write("</span>");
				}
				else {
					write("<span class='email' style='display: none;'>").writeEscHtml(emailValue(attr)).write("</span>");
				}
			}
		}		
		write("</span>");
	}
	
	/*
	<c:when test="${attributeType == 'sametimeLink'}">
		<c:if test="${sametimeLinksSvcLocation != null}">
			<c:choose>
				<c:when test="${cookie.LtpaToken == null}">
			        <c:if test="${prependHtml != null}">${prependHtml}</c:if>	
					<span id="imStatus">
						<html:link action="/auth/loginRedirect"> 
							<fmt:message key="label.profile.im.signin" />
						</html:link>
					</span>
					<c:if test="${appendHtml != null}">${appendHtml}</c:if>
				</c:when>
				<c:otherwise>
			        <c:if test="${prependHtml != null}">${prependHtml}</c:if>	
					<span class="imStatus ${emailAttribute}_status">
						<script type="text/javascript">
							writeSametimeLink('${emailAttribute}', '${attribute}', true, 'icon:no');
						</script>
					</span>
					<c:if test="${appendHtml != null}">${appendHtml}</c:if>
				</c:otherwise>
			</c:choose>
		</c:if>
	</c:when>
	 */
	protected final void outputSametimeLinkAttrValue(final UIAttributeConfig attr, final boolean empty, final String value)
		throws IOException
	{
		String ltpa = CookieHelper.getCookieValue(request, "LtpaToken");
		if (ltpa != null) {
			String returnUrl = (String) request.getAttribute("profilesOriginalLocation");
			write("<span id='imStatus'>");
			write("<a href='").write(contextRoot()).write("/auth/loginRedirect.do?loginReturnPage=").writeEscUrl(returnUrl).write("&lang=").write(lang).write("'/>");
				write(label("label.profile.im.signin"));
			write("</a>");
			write("</span>");
		} else if (allowEmailInReturn) {
			String email = emailValue(attr);
			
			write("<script type='text/javascript'>");
			write("writeSametimeLink('").writeEscJS(email).write("', ')").writeEscJS(value).write("', true, 'icon:no')");
			write("</script>");
		}
	}
	
	/**
	 * Outputs <a href="mailto:{attr}">{attr}</a>
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	protected final void outputEmailAttrValue(final UIAttributeConfig attr, final boolean empty, final String value) 
		throws IOException
	{
		if (allowEmailInReturn && !empty) {
			write("<a href=\"mailto:");
			write(Functions.escapeUnwiseURLChars(value));
			write("\">");
			writeAttrValueStr(attr, value);
			write("</a>");
		}
	}
	
	/**
	 * Outputs a regular attribute
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	protected final void outputRegularAttrValue(final UIAttributeConfig attr, final boolean empty, final String value)
		throws IOException
	{
		if (!empty) {
			writeAttrValueStr(attr, value);
		}			
	}
	
	/**
	 * Outputs <a href="${blogsLink}">label("label.profile.blogUrl")</a>
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	protected final void outputBlogUrlAttrValue(final UIAttributeConfig attr, final boolean empty, final String value) 
		throws IOException
	{
		String url = (value.startsWith("http")) ?
				Functions.escapeUnwiseURLChars(value) : "http:" + Functions.escapeUnwiseURLChars(value);
		
		write("<a href='").write(url).write("' target='_blank'>").write(label("label.profile.blogUrl")).write("</a>");
	}
	
	
	/**
	 * Write a link value: <a href="${url}">label("label.profile.link")</a>
	 * 
	 * <c:when test="${attributeType == 'link'}">
		<a href="${profiles:escapeUnwiseURLChars(attribute)}" target="_blank">
        	<c:if test="${prependHtml != null}">${prependHtml}</c:if><fmt:message key="label.profile.link" /><%--${attribute}--%><c:if test="${appendHtml != null}">${appendHtml}</c:if>
		</a>
	</c:when>
	 *
	 * @param attr
	 * @param empty
	 * @param value
	 * @throws IOException
	 */
	protected final void outputLinkAttrValue(final UIAttributeConfig attr, final boolean empty, final String value) 
		throws IOException
	{
		String escValue = Functions.escapeUnwiseURLChars(value);
		
		write("<a href='").write(escValue).write("' target='_blank'>").write(label("label.profile.link")).write("</a>");
	}
	
	
	/**
	 * Utility method to escape and write an attribute value string
	 * 
	 * @param attr
	 * @param sb
	 * @param value
	 * @return
	 */
	protected final void writeAttrValueStr(final UIAttributeConfig attr, final String value) 
		throws IOException 
	{
		if (value != null) {
			if (attr.isRichText()) writer.write(value); // output HTML				
			else StringEscapeUtils.escapeHtml(writer, value); // output text
		}
	}	
	
}
