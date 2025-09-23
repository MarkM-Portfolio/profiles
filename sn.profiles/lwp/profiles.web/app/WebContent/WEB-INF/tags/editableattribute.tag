<%-- ***************************************************************** --%>
<%--                                                                   --%>
<%-- IBM Confidential                                                  --%>
<%--                                                                   --%>
<%-- OCO Source Materials                                              --%>
<%--                                                                   --%>
<%-- Copyright IBM Corp. 2001, 2013                                    --%>
<%--                                                                   --%>
<%-- The source code for this program is not published or otherwise    --%>
<%-- divested of its trade secrets, irrespective of what has been      --%>
<%-- deposited with the U.S. Copyright Office.                         --%>
<%--                                                                   --%>
<%-- ***************************************************************** --%>

<%-- 5724-S68                                                          --%>
<%@ tag body-content="empty" %>

<%@ attribute name="attributeType"	required="true" 	rtexprvalue="true" %>
<%@ attribute name="attribute" 		required="true"		rtexprvalue="true" %>
<%@ attribute name="uidAttribute"	required="false"	rtexprvalue="true" %>
<%@ attribute name="useridAttribute"	required="false"	rtexprvalue="true" %>
<%@ attribute name="styleClasses"	required="false" 	rtexprvalue="false" %>

<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" 	uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" 		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" 	uri="http://jakarta.apache.org/struts/tags-html" %>
<%@ taglib prefix="tiles" 	uri="http://jakarta.apache.org/struts/tags-tiles" %>
<%@ taglib prefix="tags"	uri="/WEB-INF/tags" %>

<html:xhtml/>
<c:url var="profilesHtmlDir" value="/html"/>
	    	
<c:choose>
	<c:when test="${attributeType == 'multiline'}">
		<html:textarea property="attribute(${attribute})" styleClass="lotusText ${styleClasses}" style="resize: none;" styleId="${attribute}" rows="6" onchange="dataChange(this);" />
	</c:when>
	<c:when test="${attributeType == 'richtext'}">
		<div id="${attribute}_RTE" class="rte ${styleClasses}">
			<div id="${attribute}_RTE_loading" class="lotusSection lotusLoading"><fmt:message key="widgetLoading" /></div>
			<html:textarea rows="10" cols="90" property="attribute(${attribute})_Editor" styleId="${attribute}" styleClass="lotusHidden" />
			<html:hidden property="attribute(${attribute})"/>
	    	<script type="text/javascript">editPageEditorsIds.push("${attribute}");</script>
		</div>
	</c:when>
	<c:when test="${attributeType == 'name'}">
		<div class="nameTypeaheadControl">
			<html:text property="attribute(${attribute})" styleId="jobInformation.secretaryName" onchange="dataChange(this);" />
			<html:hidden property="attribute(${uidAttribute})" styleId="jobInformation.secretaryName_Uid" />
		</div>
		<script type="text/javascript">
			dojo.addOnLoad(
			    function() {
			    	// lang
			    	//create widget programmatically
			    	var lang = "<profiles:appLang />";
			    	var peopleTypeAheadStore = new lconn.core.PeopleDataStore({
			    		jsId:"peopleTypeAheadStore",
			    		queryParam:"name",
			    		url:"${pageContext.request.contextPath}/html/nameTypeahead.do"+ ((lang != "")?"?lang="+lang:"")
			    		});
			    	
			    	var args={
			    		minChars:3,
			    		store:peopleTypeAheadStore,
			    		searchDelay:400,
			    		hasDownArrow:false,
			    		autocomplete:false,
			    		multipleValues:false
			    	};

			    	var tempAssistant = dojo.byId("jobInformation.secretaryName").value; // save the current assistant text because the typeAhead dijit creation will blank out the field's value
			    	var peopleTypeAhead = new lconn.core.PeopleTypeAhead( args, dojo.byId("jobInformation.secretaryName") );
			    	var textField = dojo.byId("jobInformation.secretaryName");
			    	textField.value = tempAssistant;
			    	textField.onchange = function(){dataChange(this);};
			    	
			    	var uidIdList = dojo.query(".nameTypeaheadControl > input[type='hidden']");
			    	
			    	dojo.connect( 
						    peopleTypeAhead, 
						    "onSelect", 
				    		function(item) {
				    			if(!item) return false;
				    			if(item.userid) uidIdList[0].value = item.uid;
				    			textField.value = item.name;
							});
				});
		</script>
	</c:when>
	<c:when test="${attributeType == 'disabled'}">
		<div id="${attribute}_ro" class="disabled ${styleClasses}"></div>
		<html:hidden styleId="${attribute}" property="attribute(${attribute})" onchange="dataChange(this);" />
		<script type="text/javascript">dojo.html.set(dojo.byId("${attribute}_ro"), dojo.byId("${attribute}").value);</script>		
	</c:when>
	<c:otherwise>
		<html:text property="attribute(${attribute})" styleClass="lotusText ${styleClasses}" styleId="${attribute}" onchange="dataChange(this);" />
	</c:otherwise>
</c:choose>
