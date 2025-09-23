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
			<xsl:copy-of select="tns:caches" />
			<xsl:comment> CACHE CONFIG END </xsl:comment>
			
			<xsl:comment> DATA ACCESS CONFIG START </xsl:comment>
			<xsl:for-each select="tns:dataAccess">
				<xsl:copy>
					<xsl:comment>
						indicates if organizational data is available (i.e. managerUid)
					</xsl:comment>
					<xsl:element name="organizationalStructureEnabled">
						<xsl:attribute name="enabled">true</xsl:attribute>
					</xsl:element>
					<xsl:comment>
						indicates which fields need to be resolved before being displayed
					</xsl:comment>
					<xsl:comment>
						WARNING: in most deployments the resolvedCodes section should not be
						modified
					</xsl:comment>
					<xsl:copy-of select="tns:resolvedCodes" />
					<xsl:comment>
						search preferences
					</xsl:comment>
					<xsl:comment>
						WARNING: in most deployments the departmentSearchOnCode,
						departmentSearchOnCode,
					</xsl:comment>
					<xsl:comment>
						and locationSearchOnCode should not be modified
					</xsl:comment>
					<xsl:copy-of select="tns:search" />
					<xsl:comment> Directory integration configuration </xsl:comment>
					<xsl:copy-of select="tns:directory" />
					<xsl:comment>
						Optional security setting for Profiles javelin card.  This setting is to disallow JSONP security.  
						Older 3rd party software may will not work with this setting unless they include a reverse proxy.  
						All of the Connections application will work with JSONP disabled.
					</xsl:comment>
					<xsl:copy-of select="tns:allowJsonpJavelin" />
					<xsl:comment>
						Untested/non-'supported' feature that adds 'simple' extension
						attributes to javelin JS for implementors that manually edit javelin
						card JSP.
					</xsl:comment>
					<xsl:copy-of select="tns:includeExtensionsInJavelinJS" />
					<xsl:comment>
						When name ordering is enabled, names must be entered as (FirstName LastName) or (LastName, FirstName).  
						This feature removes the ordering restriction but at a minor performance penalty.
					</xsl:comment>
					<xsl:element name="nameOrdering">
						<xsl:attribute name="enabled">false</xsl:attribute>
					</xsl:element>
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> DATA ACCESS CONFIG END </xsl:comment>

			<xsl:comment> STATISTICS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:statistics" />
			<xsl:comment> STATISTICS CONFIG END </xsl:comment>

			<xsl:copy-of select="tns:acf" />
			<xsl:copy-of select="tns:sametimeAwareness" />
			
			<xsl:copy-of select="tns:tagOthers" />
			<xsl:copy-of select="tns:javelinGWMailSearch" />
			
			<xsl:comment> BOARD UI ENABLEMENT </xsl:comment>
			<xsl:copy-of select="tns:board" />
			<xsl:copy-of select="tns:boardInProfileHome" />
			<xsl:copy-of select="tns:boardInProfileView" />
			<xsl:copy-of select="tns:statusUpdatesInProfileView" />
			<xsl:copy-of select="tns:boardNetworkACL" />
		</config>
	</xsl:template>
</xsl:stylesheet>
