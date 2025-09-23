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
		<xsl:variable name="profileLayoutsToHandleFirst" select="tns:config/tns:layoutConfiguration/tns:profileLayout[@profileType != 'default']"/>
		<xsl:variable name="profileLayoutsToHandleLast" select="tns:config/tns:layoutConfiguration/tns:profileLayout[@profileType = 'default']"/>        
    	<xsl:call-template name="copyrightText"/>
		<xsl:call-template name="templateDescription"/>			    	    	
&lt;#import "commonUtil.ftl" as util&gt;
&lt;#compress&gt;   
		<xsl:for-each select="$profileLayoutsToHandleFirst">
&lt;#if profile.profileType == "<xsl:value-of select="@profileType"/>"&gt;
			<xsl:for-each select="*">
				<xsl:call-template name="transformSection"/>
			</xsl:for-each>			
			<xsl:choose>
				<xsl:when test="$profileLayoutsToHandleFirst[last()] = . and count($profileLayoutsToHandleLast) > 0">
&lt;#else&gt;			
				</xsl:when>
				<xsl:otherwise>
&lt;/#if&gt;			
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>		
		<xsl:for-each select="$profileLayoutsToHandleLast">
			<xsl:call-template name="transformSection">
				<xsl:with-param name="sectionName">contactInformation</xsl:with-param>
				<xsl:with-param name="editableAttributes" select="./tns:contactInformation//tns:editableAttribute | ./tns:contactInformation//tns:extensionAttribute[@editable='true']"/>
				<xsl:with-param name="singleColumnLayout">false</xsl:with-param>				
			</xsl:call-template>
			<xsl:call-template name="transformSection">
				<xsl:with-param name="sectionName">contactInformation</xsl:with-param>
				<xsl:with-param name="editableAttributes" select="./tns:jobInformation//tns:editableAttribute | ./tns:jobInformation//tns:extensionAttribute[@editable='true']"/>
				<xsl:with-param name="singleColumnLayout">false</xsl:with-param>
			</xsl:call-template>
&lt;@util.renderSection sectionLabel="contactInformation"&gt;

	&lt;@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/&gt;

&lt;/@util.renderSection&gt; 						
			<xsl:call-template name="transformSection">
				<xsl:with-param name="sectionName">aboutMe</xsl:with-param>
				<xsl:with-param name="editableAttributes" select="./tns:associatedInformation//tns:editableAttribute | ./tns:associatedInformation//tns:extensionAttribute[@editable='true']"/>
				<xsl:with-param name="singleColumnLayout">true</xsl:with-param>				
			</xsl:call-template>
		</xsl:for-each>		
		<xsl:if test="count($profileLayoutsToHandleFirst) > 0">
&lt;/#if&gt;
		</xsl:if>
&lt;/#compress&gt;					
    </xsl:template>
    <xsl:template name="transformSection">
    	<xsl:param name="sectionName"/>    	
    	<xsl:param name="editableAttributes"/>
    	<xsl:param name="singleColumnLayout"/>
&lt;@util.renderSection sectionLabel="<xsl:value-of select="$sectionName"></xsl:value-of>"&gt;
		<xsl:for-each select="$editableAttributes">
			<xsl:call-template name="transformAttribute">
				<xsl:with-param name="attribute" select="."/>
				<xsl:with-param name="singleColumnLayout" select="$singleColumnLayout"/>
			</xsl:call-template>			
		</xsl:for-each>
&lt;/@util.renderSection&gt;   							    
    </xsl:template>    
    <xsl:template name="transformAttribute">
    	<xsl:param name="attribute"/>
    	<xsl:param name="singleColumnLayout"/>
    	<xsl:variable name="isExtension" select="local-name() = 'extensionAttribute'"/>
		<xsl:variable name="nlsBundle">
			<xsl:choose>
				<xsl:when test="string-length(@bundleIdRef) = 0"></xsl:when>
				<xsl:otherwise><xsl:value-of select="@bundleIdRef"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>			
		<xsl:variable name="nlsKey">
			<xsl:choose>
				<xsl:when test="string-length(@labelKey) = 0">label.<xsl:value-of select="."/></xsl:when>
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
    	<xsl:variable name="isDisabled" select="@disabled = 'true'"/>
    	<xsl:variable name="isMultiline" select="@multiline = 'true'"/>
    	<xsl:variable name="isPerson" select="@hcard = 'true'"/>
    	<xsl:choose>
    		<xsl:when test="$isDisabled">
    &lt;@util.renderFormControl ref="<xsl:value-of select="$ref"/>" isDisabled=true singleColumnLayout=<xsl:value-of select="$singleColumnLayout"/><xsl:if test="$nlsBundle != ''"> nlsBundle="<xsl:value-of select="$nlsBundle"/>"</xsl:if> nlsKey="<xsl:value-of select="$nlsKey"/>"/&gt;
    		</xsl:when>
    		<xsl:when test="$isMultiline">
    &lt;@util.renderFormControl ref="<xsl:value-of select="$ref"/>" isMultiline=true singleColumnLayout=<xsl:value-of select="$singleColumnLayout"/><xsl:if test="$nlsBundle != ''"> nlsBundle="<xsl:value-of select="$nlsBundle"/>"</xsl:if> nlsKey="<xsl:value-of select="$nlsKey"/>"/&gt;
    		</xsl:when>
    		<xsl:when test="$isPerson">
	&lt;@util.renderFormControl ref="<xsl:value-of select="$ref"/>" dataId="<xsl:value-of select="$dataId"/>" dataKey="<xsl:value-of select="$dataKey"/>" isPerson=true singleColumnLayout=<xsl:value-of select="$singleColumnLayout"/><xsl:if test="$nlsBundle != ''"> nlsBundle="<xsl:value-of select="$nlsBundle"/>"</xsl:if> nlsKey="<xsl:value-of select="$nlsKey"/>"/&gt;    		
    		</xsl:when>
    		<xsl:otherwise>
    &lt;@util.renderFormControl ref="<xsl:value-of select="$ref"/>" singleColumnLayout=<xsl:value-of select="$singleColumnLayout"/><xsl:if test="$nlsBundle != ''"> nlsBundle="<xsl:value-of select="$nlsBundle"/>"</xsl:if> nlsKey="<xsl:value-of select="$nlsKey"/>"/&gt;
    		</xsl:otherwise>
    	</xsl:choose>    	    	    	
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
&lt;#-- Template: profileEdit.ftl                                         --&gt;
&lt;#--                                                                   --&gt;
&lt;#-- This template is used to render a profile edit form.		      --&gt;    
&lt;#--                                                                   --&gt;
&lt;#-- Each form area is identified by the following section labels:     --&gt;
&lt;#-- 	"contactInformation" = the contact information tab			  --&gt;
&lt;#-- 	"associatedInformation" = the about me information tab        --&gt;                                                      
&lt;#-- ***************************************************************** --&gt;   	
    	</xsl:text>
    </xsl:template>    
</xsl:stylesheet>