<?xml version="1.0" encoding="UTF-8"?>
<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2001, 2010                                    -->
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
			<xsl:for-each select="tns:layoutConfiguration" >
				<xsl:copy>
					<xsl:call-template
						name="copy-layoutConfiguration" />
				</xsl:copy>
			</xsl:for-each>
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
					<xsl:for-each select="tns:search">
						<xsl:copy>
							<xsl:call-template name="copy-search-with-more" />
						</xsl:copy>
					</xsl:for-each>
					<xsl:copy-of select="tns:directory" />
					<xsl:copy-of select="tns:includeExtensionsInJavelinJS" />
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
    <xsl:template
        name="copy-profileExtensionAttributes-with-simpleAttribute">
        <xsl:copy-of select="node()" />
        <xsl:element name="simpleAttribute"
            namespace="http://www.ibm.com/profiles-config"
            use-attribute-sets="simpleAttribute">
        </xsl:element>
    </xsl:template>
    <xsl:attribute-set name="simpleAttribute">
        <xsl:attribute name="extensionId">spokenLanguages</xsl:attribute>
        <xsl:attribute name="length">64</xsl:attribute>
    </xsl:attribute-set>
    <xsl:template name="copy-layoutConfiguration">
		<xsl:element name="searchLayout" namespace="http://www.ibm.com/profiles-config">
			<attribute showLabel="true" xmlns="http://www.ibm.com/profiles-config">displayName</attribute>
			<attribute showLabel="false" xmlns="http://www.ibm.com/profiles-config">preferredFirstName</attribute>
			<attribute showLabel="false" xmlns="http://www.ibm.com/profiles-config">preferredLastName</attribute>
			<attribute showLabel="true" xmlns="http://www.ibm.com/profiles-config">jobResp</attribute>
			<attribute showLabel="true" xmlns="http://www.ibm.com/profiles-config">organizationTitle</attribute>
			<attribute showLabel="false" xmlns="http://www.ibm.com/profiles-config">countryDisplayValue</attribute>
			<attribute showLabel="false" xmlns="http://www.ibm.com/profiles-config">email</attribute>
			<attribute showLabel="false" xmlns="http://www.ibm.com/profiles-config">telephoneNumber</attribute>
		</xsl:element>
		<searchResultsLayout profileType="default" xmlns="http://www.ibm.com/profiles-config">
			<column>
				<attribute hideIfEmpty="true" photo="true" uid="key"
					showLabel="false">profilePicture</attribute>
			</column>
			<column>
				<editableAttribute showLabel="false" prependHtml="&lt;strong&gt;"
					appendHtml="&lt;/strong&gt;" hideIfEmpty="true" hcard="true" uid="key"
					userid="key">displayName</editableAttribute>
				<attribute showLabel="false" prependHtml="" appendHtml=""
					hideIfEmpty="true">jobResp</attribute>
				<attribute showLabel="false" prependHtml="" appendHtml=""
					hideIfEmpty="true">organizationTitle</attribute>
				<attribute showLabel="false" prependHtml="" appendHtml=""
					hideIfEmpty="true">workLocation.city</attribute>
				<attribute showLabel="false" prependHtml="" appendHtml=""
					hideIfEmpty="true">workLocation.state</attribute>
				<attribute showLabel="false" prependHtml="" appendHtml=""
					hideIfEmpty="true">countryDisplayValue</attribute>
			</column>
			<column>
				<attribute showLabel="true" prependHtml="&lt;strong&gt;"
					appendHtml="&lt;/strong&gt;" hideIfEmpty="true">telephoneNumber</attribute>
				<attribute showLabel="true" prependHtml="&lt;strong&gt;"
					appendHtml="&lt;/strong&gt;" hideIfEmpty="true" email="true">email</attribute>
				<attribute showLabel="true" prependHtml="&lt;strong&gt;"
					appendHtml="&lt;/strong&gt;" hideIfEmpty="true" email="true">groupwareEmail</attribute>
			</column>
		</searchResultsLayout>
		<xsl:copy-of select="tns:profileLayout" />
	</xsl:template>
	<xsl:template name="copy-search-with-more" >
        <xsl:copy-of select="node()" />
        <xsl:element name="server" namespace="http://www.ibm.com/profiles-config"></xsl:element>
        <xsl:element name="port" namespace="http://www.ibm.com/profiles-config"></xsl:element>
	</xsl:template>
</xsl:stylesheet>
