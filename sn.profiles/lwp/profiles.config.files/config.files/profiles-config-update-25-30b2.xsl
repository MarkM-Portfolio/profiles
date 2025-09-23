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
	<xsl:template match="/tns:config">

		<xsl:comment>		
*****************************************************************
                                                                 
 Licensed Materials - Property of IBM                            
                                                                 
 5724-S68                                                        
                                                                 
 Copyright IBM Corp. 2001, 2009  All Rights Reserved.            
                                                                 
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
			<xsl:copy-of select="tns:layoutConfiguration" />
			<xsl:comment> END LAYOUT CONFIGURATION SECTION </xsl:comment>

			<xsl:comment> START API CONFIGURATION SECTION </xsl:comment>
			<xsl:copy-of select="tns:apiConfiguration" />
			<xsl:comment> END API CONFIGURATION SECTION </xsl:comment>
			
			<xsl:comment> CACHE CONFIG START </xsl:comment>
			<xsl:for-each select="tns:caches">
				<xsl:copy>
					<xsl:copy-of select="tns:fullReportsToChainCache"/>
					<xsl:for-each select="tns:profileObjectCache">
						<xsl:copy>
							<xsl:copy-of select="tns:refreshTime"/>
							<xsl:element name="refreshInterval">15</xsl:element>
							<xsl:copy-of select="tns:startDelay"/>
						</xsl:copy>
					</xsl:for-each>
				</xsl:copy>	
			</xsl:for-each>
			<xsl:comment> CACHE CONFIG END </xsl:comment>
			
			<xsl:comment> DATA ACCESS CONFIG START </xsl:comment>
			<xsl:for-each select="tns:dataAccess">
				<xsl:copy>
					<xsl:copy-of select="tns:organizationalStructureEnabled"/>
					<xsl:copy-of select="tns:resolvedCodes" />
					<xsl:for-each select="tns:search">
						<xsl:copy>
							<xsl:copy-of select="tns:departmentSearchOnCode" />
							<xsl:copy-of select="tns:departmentField" />
							<xsl:copy-of select="tns:locationSearchOnCode" />
							<xsl:copy-of select="tns:maxRowsToReturn" />
							<xsl:copy-of select="tns:pageSize" />
							<xsl:copy-of select="tns:firstNameSearch" />
							<xsl:copy-of select="tns:kanjiNameSearch" />
							<xsl:element name="sortNameSearchResultsBy">
								<xsl:attribute name="default">displayName</xsl:attribute>
							</xsl:element>
							<xsl:element name="sortIndexSearchResultsBy">
								<xsl:attribute name="default">relevance</xsl:attribute>
							</xsl:element>		
						</xsl:copy>
					</xsl:for-each>
					<xsl:for-each select="tns:directory">
						<xsl:copy>
							<xsl:copy-of select="tns:lconnUserIdField"/>
							<xsl:copy-of select="tns:loginAttributes"/>
						</xsl:copy>
					</xsl:for-each>					
					<xsl:copy-of select="tns:allowJsonpJavelin" />
					<xsl:copy-of select="tns:includeExtensionsInJavelinJS" />
					<xsl:copy-of select="tns:nameOrdering"/>
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> DATA ACCESS CONFIG END </xsl:comment>

			<xsl:comment> STATISTICS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:statistics" />
			<xsl:comment> STATISTICS CONFIG END </xsl:comment>

			<xsl:copy-of select="tns:acf" />
			<xsl:element name="sametimeAwareness">
				<xsl:attribute name="enabled">
					<xsl:value-of select="tns:sametimeAwareness/@enabled"/>
				</xsl:attribute>
				<xsl:attribute name="href">http://localhost:59449/stwebapi/</xsl:attribute>
				<xsl:attribute name="ssl_href">http://localhost:59449/stwebapi/</xsl:attribute>
				<xsl:attribute name="sametimeInputType">email</xsl:attribute>
			</xsl:element>			

			<xsl:copy-of select="tns:javelinGWMailSearch" />

			<xsl:element name="properties">
				<xsl:copy-of select="tns:properties/tns:property"/>
			</xsl:element>			
			
			<xsl:comment> BOARD UI ENABLEMENT </xsl:comment>
			<xsl:copy-of select="tns:board" />
		</config>
	</xsl:template>
</xsl:stylesheet>
