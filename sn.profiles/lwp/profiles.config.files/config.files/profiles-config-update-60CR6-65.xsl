<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2016                                          -->
<!--                                                                   -->
<!-- The source code for this program is not published or otherwise    -->
<!-- divested of its trade secrets, irrespective of what has been      -->
<!-- deposited with the U.S. Copyright Office.                         -->
<!--                                                                   -->
<!-- ***************************************************************** -->

<xsl:stylesheet version="1.0" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:tns="http://www.ibm.com/profiles-config" xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />

	<xsl:attribute-set name="recommendedTags">
		<xsl:attribute name="extensionId">recommendedTags</xsl:attribute>
		<xsl:attribute name="length">256</xsl:attribute>
		<xsl:attribute name="sourceKey">recommendedTags</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="departmentKey">
		<xsl:attribute name="extensionId">departmentKey</xsl:attribute>
		<xsl:attribute name="length">256</xsl:attribute>
		<xsl:attribute name="sourceKey">departmentKey</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="privacyAndGuidelines">
		<xsl:attribute name="extensionId">privacyAndGuidelines</xsl:attribute>
		<xsl:attribute name="length">256</xsl:attribute>
		<xsl:attribute name="sourceKey">privacyAndGuidelines</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="touchpointState">
		<xsl:attribute name="extensionId">touchpointState</xsl:attribute>
		<xsl:attribute name="length">256</xsl:attribute>
		<xsl:attribute name="sourceKey">touchpointState</xsl:attribute>
	</xsl:attribute-set>

	<xsl:attribute-set name="touchpointSession">
		<xsl:attribute name="extensionId">touchpointSession</xsl:attribute>
		<xsl:attribute name="maxBytes">1000000</xsl:attribute>
		<xsl:attribute name="sourceKey">touchpointSession</xsl:attribute>
	</xsl:attribute-set>

	<!-- XSLT identity transform -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/tns:config/tns:profileDataModels/tns:profileExtensionAttributes">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <simpleAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="recommendedTags"></simpleAttribute>
            <simpleAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="departmentKey"></simpleAttribute>
            <simpleAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="privacyAndGuidelines"></simpleAttribute>
            <simpleAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="touchpointState"></simpleAttribute>
            <richtextAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="touchpointSession"></richtextAttribute>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
