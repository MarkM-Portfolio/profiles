<?xml version="1.0" encoding="UTF-8"?>
<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2001, 2012                                    -->
<!--                                                                   -->
<!-- The source code for this program is not published or otherwise    -->
<!-- divested of its trade secrets, irrespective of what has been      -->
<!-- deposited with the U.S. Copyright Office.                         -->
<!--                                                                   -->
<!-- ***************************************************************** -->

<!-- 5724_S68                                                          -->
<xsl:stylesheet version="1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:tns="http://www.ibm.com/profiles-config" xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
	<xsl:output method="text" />        
    <xsl:template match="/">
		<xsl:variable name="profileLayoutsToHandleFirst" select="tns:config/tns:layoutConfiguration/tns:businessCardLayout[@profileType != 'default']"/>
		<xsl:variable name="profileLayoutsToHandleLast" select="tns:config/tns:layoutConfiguration/tns:businessCardLayout[@profileType = 'default']"/>
		<xsl:call-template name="copyrightText"/> 
		<xsl:call-template name="templateDescription"/>		          
&lt;#import "commonUtil.ftl" as util&gt;  
&lt;#compress&gt;   
		<xsl:for-each select="$profileLayoutsToHandleFirst">
			<xsl:choose>
				<xsl:when test="position() = 1">
&lt;#if profile.profileType == "<xsl:value-of select="@profileType"/>"&gt;				
				</xsl:when>
				<xsl:otherwise>
&lt;#elseif profile.profileType == "<xsl:value-of select="@profileType"/>"&gt;				
				</xsl:otherwise>
			</xsl:choose>
    		<xsl:for-each select="tns:attributes/*">   	
	    		<xsl:call-template name="transformAttributes"/>	
			</xsl:for-each>    
			<xsl:choose>
				<xsl:when test="last() = position() and count($profileLayoutsToHandleLast) > 0">
&lt;#else&gt;			
				</xsl:when>
				<xsl:otherwise>			
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		<xsl:for-each select="$profileLayoutsToHandleLast">
    		<xsl:for-each select="tns:attributes/*">   	
	    		<xsl:call-template name="transformAttributes"/>	
			</xsl:for-each>    
		</xsl:for-each>
		<xsl:if test="count($profileLayoutsToHandleFirst) > 0">
&lt;/#if&gt;
		</xsl:if>
&lt;/#compress&gt;			
    </xsl:template>

	<!-- Template that migrates the businessCardLayout/attributes/* children -->
    <xsl:template name="transformAttributes">
    	<!-- if its html, we just migrate prepend html, and then recurse on the children -->
    	<xsl:if test="local-name() = 'html'">
    		<!--  migrate the prepend html -->
    		<xsl:value-of select="@prependHtml"/>
    		<!-- recurse on the children -->
    		<xsl:for-each select="*">
    			<xsl:call-template name="transformAttributes"/>
    		</xsl:for-each>
    		<!-- migrate the append html -->
    		<xsl:value-of select="@appendHtml"/>
    	</xsl:if>
    	<!-- otherwise, we just migrate all fields -->
    	<xsl:if test="local-name() != 'html'">
			<xsl:variable name="isExtension" select="local-name() = 'extensionAttribute'"/>    	
			<xsl:variable name="hideIfEmpty">
				<xsl:choose>
					<xsl:when test="@hideIfEmpty='true'">true</xsl:when>
					<xsl:otherwise>false</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="nlsBundle">
				<xsl:choose>
					<xsl:when test="string-length(@bundleIdRef) = 0"></xsl:when>
					<xsl:otherwise><xsl:value-of select="@bundleIdRef"/></xsl:otherwise>
				</xsl:choose>
			</xsl:variable>			
			<xsl:variable name="nlsKey">
				<xsl:choose>
					<xsl:when test="string-length(@labelKey) = 0 or contains(@labelKey, 'label.contactInformation.')">label.<xsl:value-of select="."/></xsl:when>
					<xsl:otherwise><xsl:value-of select="@labelKey"/></xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
    		<xsl:variable name="ref">
    			<xsl:choose>
					<xsl:when test="$isExtension"><xsl:value-of select="@extensionIdRef"/></xsl:when>
					<xsl:when test=". = 'secretaryName'">secretaryUid</xsl:when>
					<xsl:when test=". = 'secretaryEmail'">secretaryUid</xsl:when>
					<xsl:when test=". = 'secretaryKey'">secretaryUid</xsl:when>
					<xsl:when test=". = 'employeeTypeDesc'">employeeTypeCode</xsl:when>
					<xsl:when test=". = 'workLocation.address1'">workLocationCode</xsl:when>
					<xsl:when test=". = 'workLocation.address2'">workLocationCode</xsl:when>
					<xsl:when test=". = 'workLocation.city'">workLocationCode</xsl:when>
					<xsl:when test=". = 'workLocation.state'">workLocationCode</xsl:when>
					<xsl:when test=". = 'workLocation.postalCode'">workLocationCode</xsl:when>
					<xsl:when test=". = 'countryDisplayValue'">countryCode</xsl:when>
					<xsl:when test=". = 'departmentTitle'">deptNumber</xsl:when>
					<xsl:when test=". = 'organizationTitle'">orgId</xsl:when>
					<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>    			
    			</xsl:choose>    			
    		</xsl:variable>
    		<xsl:variable name="dataKey">
    			<xsl:choose>
					<xsl:when test=". = 'secretaryName'">secretaryName</xsl:when>
					<xsl:when test=". = 'secretaryEmail'">secretaryEmail</xsl:when>
					<xsl:when test=". = 'secretaryKey'">secretaryKey</xsl:when>
					<xsl:when test=". = 'employeeTypeDesc'">employeeTypeDesc</xsl:when>
					<xsl:when test=". = 'workLocation.address1'">address1</xsl:when>
					<xsl:when test=". = 'workLocation.address2'">address2</xsl:when>
					<xsl:when test=". = 'workLocation.city'">city</xsl:when>
					<xsl:when test=". = 'workLocation.state'">state</xsl:when>
					<xsl:when test=". = 'workLocation.postalCode'">postalCode</xsl:when>
					<xsl:when test=". = 'countryDisplayValue'">countryDisplayValue</xsl:when>
					<xsl:when test=". = 'departmentTitle'">departmentTitle</xsl:when>
					<xsl:when test=". = 'organizationTitle'">organizationTitle</xsl:when>
					<xsl:otherwise></xsl:otherwise>    			
    			</xsl:choose>
    		</xsl:variable>
    		<xsl:variable name="dataId">
    			<xsl:choose>
					<xsl:when test=". = 'secretaryName'">secretary</xsl:when>
					<xsl:when test=". = 'secretaryEmail'">secretary</xsl:when>
					<xsl:when test=". = 'secretaryKey'">secretary</xsl:when>
					<xsl:when test=". = 'employeeTypeDesc'">employeeType</xsl:when>    		
					<xsl:when test=". = 'workLocation.address1'">workLocation</xsl:when>
					<xsl:when test=". = 'workLocation.address2'">workLocation</xsl:when>
					<xsl:when test=". = 'workLocation.city'">workLocation</xsl:when>
					<xsl:when test=". = 'workLocation.state'">workLocation</xsl:when>
					<xsl:when test=". = 'workLocation.postalCode'">workLocation</xsl:when>
					<xsl:when test=". = 'countryDisplayValue'">country</xsl:when>
					<xsl:when test=". = 'departmentTitle'">department</xsl:when>
					<xsl:when test=". = 'organizationTitle'">organization</xsl:when>				
					<xsl:otherwise></xsl:otherwise>    			
    			</xsl:choose>    	
    		</xsl:variable>    
    		<xsl:variable name="renderAs">
    			<xsl:choose>
    				<xsl:when test="@email='true'">email</xsl:when>
    				<xsl:when test="@hcard='true'">hcard</xsl:when>
    				<xsl:when test="@blogUrl='true'">blogUrl</xsl:when>
    				<xsl:when test="@link='true'">link</xsl:when>
    				<xsl:otherwise></xsl:otherwise>
    			</xsl:choose>
    		</xsl:variable>
    		<xsl:variable name="userId">
    			<xsl:choose>
    				<xsl:when test="@userid='secretaryUserid'">(data.secretary.secretaryUserid)!""</xsl:when>
    				<xsl:when test="string-length(@userid) &gt; 0">(profile.<xsl:value-of select="@userid"/>)!""</xsl:when>
    				<xsl:otherwise></xsl:otherwise>    			
	    		</xsl:choose>
    		</xsl:variable>
    		<xsl:variable name="userEmail">
    			<xsl:choose>
    				<xsl:when test="@email='secretaryEmail'">(data.secretary.secretaryEmail)!""</xsl:when>
    				<xsl:when test="string-length(@email) &gt; 0">(profile.<xsl:value-of select="@email"/>)!""</xsl:when>
    				<xsl:otherwise></xsl:otherwise>   			
    			</xsl:choose>
    		</xsl:variable>    	    		
    		<xsl:variable name="prependHtml">
    			<xsl:choose>
    				<xsl:when test="contains(@prependHtml, '%displayNameId%')"><xsl:value-of select="substring-before(@prependHtml, '%displayNameId%')"/>${util.getDisplayNameId()}<xsl:value-of select="substring-after(@prependHtml, '%displayNameId%')"/></xsl:when>
    				<xsl:otherwise><xsl:value-of select="@prependHtml"/></xsl:otherwise>
    			</xsl:choose>
    		</xsl:variable>
	&lt;@util.renderProperty ref="<xsl:value-of select="$ref"/>"<xsl:if test="$dataId != ''"> dataId="<xsl:value-of select="$dataId"/>"</xsl:if><xsl:if test="$dataKey != ''"> dataKey="<xsl:value-of select="$dataKey"/>"</xsl:if><xsl:if test="$nlsBundle != ''"> nlsBundle="<xsl:value-of select="$nlsBundle"/>"</xsl:if> nlsKey="<xsl:value-of select="$nlsKey"/>"<xsl:if test="$hideIfEmpty = 'true'"> hideIfEmpty=true</xsl:if> ;  ref, dataId, dataKey, nlsKey, nlsBundle&gt;    		
			<xsl:value-of select="$prependHtml"/>		
			<xsl:if test="@showLabel = 'true'">&lt;@util.renderNls<xsl:if test="$nlsBundle != ''"> nlsBundle=nlsBundle</xsl:if> nlsKey=nlsKey/&gt;<xsl:text> </xsl:text></xsl:if>				
		&lt;@util.renderValue ref=ref<xsl:if test="$renderAs != ''"> renderAs="<xsl:value-of select="$renderAs"/>"</xsl:if><xsl:if test="$dataId != ''"> dataId=dataId</xsl:if><xsl:if test="$dataKey != ''"> dataKey=dataKey</xsl:if><xsl:if test="$renderAs='hcard'"><xsl:if test="$userId != ''"> userId=<xsl:value-of select="$userId"/></xsl:if><xsl:if test="$userEmail != ''"> userEmail=<xsl:value-of select="$userEmail"/></xsl:if></xsl:if>/&gt;																															
			<xsl:value-of select="@appendHtml"/>						
	&lt;/@util.renderProperty&gt;					    							    
    	</xsl:if>				
    </xsl:template>
    
    <xsl:template name="copyrightText">
		<xsl:text>
&lt;#-- ***************************************************************** --&gt; 
&lt;#--                                                                   --&gt;
&lt;#-- Licensed Materials - Property of IBM                              --&gt;
&lt;#--                                                                   --&gt;
&lt;#-- 5724-S68                                                          --&gt;                                                          
&lt;#--                                                                   --&gt;
&lt;#-- Copyright IBM Corp. 2011  All Rights Reserved.                    --&gt;
&lt;#--                                                                   --&gt;
&lt;#-- US Government Users Restricted Rights - Use, duplication or       --&gt;
&lt;#-- disclosure restricted by GSA ADP Schedule Contract with           --&gt;
&lt;#-- IBM Corp.                                                         --&gt;                             
&lt;#--                                                                   --&gt;
&lt;#-- ***************************************************************** --&gt;
		</xsl:text>    
    </xsl:template> 
    <xsl:template name="templateDescription">
    	<xsl:text>
&lt;#-- ***************************************************************** --&gt; 
&lt;#-- Template: businessCardInfo.ftl                                    --&gt;
&lt;#--                                                                   --&gt;
&lt;#-- This template is used to render fields in the business card.      --&gt;                                                          
&lt;#-- ***************************************************************** --&gt;   	
    	</xsl:text>
    </xsl:template>       
</xsl:stylesheet>