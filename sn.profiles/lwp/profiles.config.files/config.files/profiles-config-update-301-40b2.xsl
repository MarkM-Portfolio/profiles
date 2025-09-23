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

    <xsl:output method="xml" omit-xml-declaration="no" indent="yes" />

    <xsl:attribute-set name="DbCleanupTask">
        <xsl:attribute name="name">DbCleanupTask</xsl:attribute>
        <xsl:attribute name="enabled">true</xsl:attribute>
        <xsl:attribute name="interval">0 0 0 * * ?</xsl:attribute>
        <xsl:attribute name="type">internal</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="ProcessLifeCycleEventsTask">
        <xsl:attribute name="name">ProcessLifeCycleEventsTask</xsl:attribute>
        <xsl:attribute name="enabled">true</xsl:attribute>
        <xsl:attribute name="interval">0 */2 * * * ?</xsl:attribute>
        <xsl:attribute name="type">internal</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="ProcessTDIEventsTask">
        <xsl:attribute name="name">ProcessTDIEventsTask</xsl:attribute>
        <xsl:attribute name="enabled">true</xsl:attribute>
        <xsl:attribute name="interval">0 */2 * * * ?</xsl:attribute>
        <xsl:attribute name="type">internal</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="StatsCollectorTask">
        <xsl:attribute name="name">StatsCollectorTask</xsl:attribute>
        <xsl:attribute name="enabled">
            <xsl:value-of select="tns:statistics/@enabled"/>
        </xsl:attribute>
        <xsl:attribute name="interval">0 0 1 * * ?</xsl:attribute>
        <xsl:attribute name="type">internal</xsl:attribute>
        <xsl:attribute name="scope">local</xsl:attribute>
    </xsl:attribute-set>
    <xsl:attribute-set name="RefreshSystemObjectsTask">
	    <xsl:attribute name="name">RefreshSystemObjectsTask</xsl:attribute>
	    <xsl:attribute name="enabled">true</xsl:attribute>
	    <xsl:attribute name="interval">0 */2 * * * ?</xsl:attribute>
	    <xsl:attribute name="type">internal</xsl:attribute>
    </xsl:attribute-set>

    <xsl:template match="/tns:config">

        <xsl:comment>        
*****************************************************************
                                                                 
 Licensed Materials - Property of IBM                            
                                                                 
 5724-S68                                                        
                                                                 
 Copyright IBM Corp. 2001, 2011  All Rights Reserved.            
                                                                 
 US Government Users Restricted Rights - Use, duplication or     
 disclosure restricted by GSA ADP Schedule Contract with         
 IBM Corp.                                                       
                                                                 
*****************************************************************
        </xsl:comment>

        <config xmlns="http://www.ibm.com/profiles-config" xmlns:tns="http://www.ibm.com/profiles-config"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="profiles"
            xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
            <xsl:comment> START PROFILES DATA MODELS SECTION </xsl:comment>
            <xsl:copy-of select="tns:profileDataModels" />
            <xsl:comment> END PROFILES DATA MODELS SECTION </xsl:comment>

            <xsl:comment> START LAYOUT CONFIGURATION SECTION </xsl:comment>
            <xsl:variable name="bundleIdRef" select="//*/@bundleIdRef"/>
            <layoutConfiguration>
            	<xsl:comment>UI Template rendering configuration information</xsl:comment>
            	<templateConfiguration>
            		<xsl:comment>FTL template reloading to test customization, in production this value should be set to 0, but during customization set to value in seconds</xsl:comment>
            		<templateReloading>0</templateReloading>
            		<xsl:comment>Comma-delimited set of bundle identifiers custom resource bundles to make available to FTL templates</xsl:comment>
            		<templateNlsBundles><xsl:value-of select="$bundleIdRef"/></templateNlsBundles>
					<xsl:comment>Configuration for specific templates by name [businessCardInfo, searchResults]</xsl:comment>
					<xsl:variable name="businessCardAttributes" select="tns:layoutConfiguration/tns:businessCardLayout//*/tns:attribute"/>
					<xsl:variable name="bIncludeSecretary" select="boolean($businessCardAttributes[.='secretaryName']) or boolean($businessCardAttributes[.='secretaryEmail']) or boolean($businessCardAttributes[.='secretaryKey']) or boolean($businessCardAttributes[.='secretaryUserid'])"/>												
					<xsl:variable name="bIncludeManager" select="boolean($businessCardAttributes[.='managerName']) or boolean($businessCardAttributes[.='managerEmail']) or boolean($businessCardAttributes[.='managerKey']) or boolean($businessCardAttributes[.='managerUserid'])"/>
					<xsl:variable name="bIncludeExtensions" select="boolean(tns:layoutConfiguration/tns:businessCardLayout//*/tns:extensionAttribute)"/>
					<xsl:variable name="searchResultsAttributes" select="tns:layoutConfiguration/tns:searchResultsLayout//*/tns:attribute"/>					
					<xsl:variable name="sIncludeSecretary" select="boolean($searchResultsAttributes[.='secretaryName']) or boolean($searchResultsAttributes[.='secretaryEmail']) or boolean($searchResultsAttributes[.='secretaryKey']) or boolean($searchResultsAttributes[.='secretaryUserid'])"/>												
					<xsl:variable name="sIncludeManager" select="boolean($searchResultsAttributes[.='managerName']) or boolean($searchResultsAttributes[.='managerEmail']) or boolean($searchResultsAttributes[.='managerKey']) or boolean($searchResultsAttributes[.='managerUserid'])"/>
					<xsl:variable name="sIncludeExtensions" select="boolean(tns:layoutConfiguration/tns:searchResultsLayout//*/tns:extensionAttribute)"/>					
					<template name="businessCardInfo">
						<templateDataModel>
							<xsl:comment>include if you render associated data for workLocation, organization, or department</xsl:comment>
							<templateData>codes</templateData>
							<xsl:comment>include if you render profile extension fields in the template</xsl:comment>
							<xsl:choose>
								<xsl:when test="$bIncludeExtensions">
								<templateData>extensions</templateData>
								</xsl:when>
								<xsl:otherwise>
								<xsl:comment>&lt;templateData&gt;extensions&lt;/templateData%gt;</xsl:comment>								
								</xsl:otherwise>
							</xsl:choose>							
							<xsl:comment>include if you render secretary name or email in the template</xsl:comment>
							<xsl:choose>
								<xsl:when test="$bIncludeSecretary">							
							<templateData>secretary</templateData>								
								</xsl:when>
								<xsl:otherwise>														
							<xsl:comment>&lt;templateData&gt;secretary&lt;/templateData&gt;</xsl:comment>								
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>include if you render manager name or email in the template</xsl:comment>							
							<xsl:choose>
								<xsl:when test="$bIncludeManager">
									<templateData>manager</templateData>
								</xsl:when>
								<xsl:otherwise>
									<xsl:comment>&lt;templateData&gt;manager&lt;/templateData&gt;</xsl:comment>
								</xsl:otherwise>
							</xsl:choose>					
					</templateDataModel>
				</template>
				<template name="searchResults">
						<templateDataModel>
							<xsl:comment>include if you render associated data for workLocation, organization, or department</xsl:comment>
							<templateData>codes</templateData>
							<xsl:comment>include if you render profile extension fields in the template</xsl:comment>
							<xsl:choose>
								<xsl:when test="$sIncludeExtensions">
								<templateData>extensions</templateData>
								</xsl:when>
								<xsl:otherwise>
								<xsl:comment>&lt;templateData&gt;extensions&lt;/templateData%gt;</xsl:comment>								
								</xsl:otherwise>
							</xsl:choose>							
							<xsl:comment>include if you render secretary name or email in the template</xsl:comment>
							<xsl:choose>
								<xsl:when test="$sIncludeSecretary">							
							<templateData>secretary</templateData>								
								</xsl:when>
								<xsl:otherwise>														
							<xsl:comment>&lt;templateData&gt;secretary&lt;/templateData&gt;</xsl:comment>								
								</xsl:otherwise>
							</xsl:choose>
							<xsl:comment>include if you render manager name or email in the template</xsl:comment>							
							<xsl:choose>
								<xsl:when test="$sIncludeManager">
									<templateData>manager</templateData>
								</xsl:when>
								<xsl:otherwise>
									<xsl:comment>&lt;templateData&gt;manager&lt;/templateData&gt;</xsl:comment>
								</xsl:otherwise>
							</xsl:choose>					
						</templateDataModel>
					</template>
            	</templateConfiguration>            	
            	<xsl:copy-of select="tns:layoutConfiguration/tns:vcardExport"/>
            	<xsl:copy-of select="tns:layoutConfiguration/tns:searchLayout"/>
				<xsl:for-each select="tns:layoutConfiguration/tns:businessCardLayout">
				<businessCardLayout>
					<xsl:attribute name="profileType">
						<xsl:value-of select="@profileType"/>
					</xsl:attribute>
					<xsl:copy-of select="tns:actions"/>
				</businessCardLayout>
				</xsl:for-each>            	
            </layoutConfiguration>
            <!--  <xsl:copy-of select="tns:layoutConfiguration" /> -->
            <xsl:comment> END LAYOUT CONFIGURATION SECTION </xsl:comment>

            <xsl:comment> START CACHE CONFIGURATION SECTION </xsl:comment>
            <xsl:copy-of select="tns:caches" />
            <xsl:comment> END CACHE CONFIGURATION SECTION </xsl:comment>
            
            <xsl:comment> START DATA ACCESS CONFIGURATION </xsl:comment>
            <dataAccess>
            	<xsl:copy-of select="tns:dataAccess/tns:organizationalStructureEnabled"/>
            	<xsl:copy-of select="tns:dataAccess/tns:resolvedCodes"/>
            	<search>
            		<xsl:copy-of select="tns:dataAccess/tns:search/tns:maxRowsToReturn"/>
            		<xsl:copy-of select="tns:dataAccess/tns:search/tns:pageSize"/>
            		<xsl:copy-of select="tns:dataAccess/tns:search/tns:firstNameSearch"/>
            		<xsl:copy-of select="tns:dataAccess/tns:search/tns:kanjiNameSearch"/>
            		<xsl:copy-of select="tns:dataAccess/tns:search/tns:sortNameSearchResultsBy"/>
            		<xsl:copy-of select="tns:dataAccess/tns:search/tns:sortIndexSearchResultsBy"/>
            	</search>
            	<xsl:copy-of select="tns:dataAccess/tns:directory"/>
            	<xsl:copy-of select="tns:dataAccess/tns:allowJsonpJavelin"/>
            	<xsl:copy-of select="tns:dataAccess/tns:includeExtensionsInJavelinJS"/>
            	<xsl:copy-of select="tns:dataAccess/tns:nameOrdering"/>
            </dataAccess>
            <xsl:comment> END DATA ACCESS CONFIGURATION </xsl:comment>

            <xsl:copy-of select="tns:acf" />

            <xsl:copy-of select="tns:sametimeAwareness" />

            <xsl:copy-of select="tns:javelinGWMailSearch" />

            <xsl:comment> START ADDITIONAL CONFIGURATION SETTINGS </xsl:comment>
            <properties>
				<xsl:copy-of select="tns:properties/tns:property[substring(@name,30,10)!='.theboard.' and substring(@name,33,10)!='.theboard.']" />
            </properties>
            <xsl:comment> END ADDITIONAL CONFIGURATION SETTINGS </xsl:comment>
  
            <xsl:comment> START SCHEDULED TASKS CONFIGURATION </xsl:comment>
            <scheduledTasks>
                <task xsl:use-attribute-sets="DbCleanupTask">
                	<args>
					<property name="draftTrashRetentionInDays">30</property>
					<property name="eventLogTrashRetentionInDays">30</property>
					<property name="eventLogMaxBulkPurge">8000</property>
					</args>
				</task>
                <task xsl:use-attribute-sets="ProcessLifeCycleEventsTask">
                	<args>
					<property name="platformCommandBatchSize">2000</property>
					</args>
                </task>
                <task xsl:use-attribute-sets="ProcessTDIEventsTask">
                	<args>
					<property name="platformCommandBatchSize">2000</property>
					</args></task>
                <task xsl:use-attribute-sets="StatsCollectorTask">
                    <args>
                    <property name="filePath"><xsl:value-of select="tns:statistics/tns:statisticsFilePath" /></property>
                    <property name="fileName"><xsl:value-of select="tns:statistics/tns:statisticsFileName" /></property>
                    </args>
                </task>
                <task xsl:use-attribute-sets="RefreshSystemObjectsTask"></task>
            </scheduledTasks>
            <xsl:comment> END SCHEDULED TASKS CONFIGURATION </xsl:comment>

        </config>
    </xsl:template>

</xsl:stylesheet>
