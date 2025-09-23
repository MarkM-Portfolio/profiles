<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- HCL Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright HCL Technologies Limited 2021                           -->
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

	<xsl:attribute-set name="chatIdentity">
		<xsl:attribute name="extensionId">chatIdentity</xsl:attribute>
		<xsl:attribute name="length">256</xsl:attribute>
		<xsl:attribute name="sourceKey">chatIdentity</xsl:attribute>
	</xsl:attribute-set>
	
	<xsl:attribute-set name="meetingIdentity">
		<xsl:attribute name="extensionId">meetingIdentity</xsl:attribute>
		<xsl:attribute name="length">256</xsl:attribute>
		<xsl:attribute name="sourceKey">meetingIdentity</xsl:attribute>
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
            <simpleAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="chatIdentity"></simpleAttribute>
            <simpleAttribute xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="meetingIdentity"></simpleAttribute>
        </xsl:copy>
    </xsl:template>
    
    

</xsl:stylesheet>
