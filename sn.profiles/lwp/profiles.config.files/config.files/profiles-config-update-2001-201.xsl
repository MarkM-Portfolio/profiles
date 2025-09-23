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

		<config xmlns="http://www.ibm.com/profiles-config" xmlns:tns="http://www.ibm.com/profiles-config"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="profiles"
			xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
			<xsl:comment> START PROFILES DATA MODELS SECTION </xsl:comment>
			<xsl:copy-of select="tns:profileDataModels" />
			<xsl:comment> END PROFILES DATA MODELS SECTION </xsl:comment>

			<xsl:comment> START LAYOUT CONFIGURATION SECTION </xsl:comment>
			<xsl:copy-of select="tns:layoutConfiguration" />
			<xsl:comment> END LAYOUT CONFIGURATION SECTION </xsl:comment>

			<xsl:copy-of select="tns:apiConfiguration" />
			
			<xsl:comment> START FULL-TEXT CONFIG </xsl:comment>
			<xsl:copy-of select="tns:IndexingTask" />
			<xsl:comment> END FULL-TEXT CONFIG </xsl:comment>
			
			<xsl:comment> CACHE CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:caches" />
			<xsl:comment> CACHE CONFIG END </xsl:comment>
			
			<xsl:comment> DATA ACCESS CONFIG START </xsl:comment>
			<xsl:for-each select="tns:dataAccess" >
				<xsl:copy>
					<xsl:copy-of select="tns:organizationalStructureEnabled" />
					<xsl:copy-of select="tns:resolvedCodes" />
					<xsl:copy-of select="tns:search" />
					<xsl:call-template name="add-directory" />
					<xsl:call-template name="add-includeExtensionsInJavelinJS" />
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> DATA ACCESS CONFIG END </xsl:comment>

			<xsl:comment> STATISTICS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:statistics" />
			<xsl:comment> STATISTICS CONFIG END </xsl:comment>
			
			<xsl:copy-of select="tns:acf" />
			<xsl:copy-of select="tns:sametimeAwareness" />
			<xsl:copy-of select="tns:structuredTags" />
			<xsl:copy-of select="tns:tagOthers" />
			<xsl:copy-of select="tns:javelinGWMailSearch" />
		</config>
	</xsl:template>
	<xsl:template name="add-directory" >
        <xsl:element name="directory" namespace="http://www.ibm.com/profiles-config">
			<lconnUserIdField xmlns="http://www.ibm.com/profiles-config">guid</lconnUserIdField>
			<loginAttributes xmlns="http://www.ibm.com/profiles-config">
				<loginAttribute>uid</loginAttribute>
				<loginAttribute>email</loginAttribute>
				<loginAttribute>loginId</loginAttribute>
			</loginAttributes>
			<deferLoginResolution enabled="false" xmlns="http://www.ibm.com/profiles-config"/>
		</xsl:element>
	</xsl:template>
	<xsl:template name="add-includeExtensionsInJavelinJS" >
		<xsl:element name="includeExtensionsInJavelinJS" namespace="http://www.ibm.com/profiles-config">
			<xsl:attribute name="enabled">false</xsl:attribute>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
