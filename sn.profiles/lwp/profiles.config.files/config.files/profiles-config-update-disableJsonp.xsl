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
<!-- 5724_S68 -->

<xsl:stylesheet version="1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tns="http://www.ibm.com/profiles-config" 
	xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
	
	<!-- XSLT identity transform -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<!-- update the allowJsonpJavelin to disabled by default -->
	<xsl:template match="/tns:config/tns:dataAccess/tns:allowJsonpJavelin">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="enabled">false</xsl:attribute>			
			<xsl:for-each select="*">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>


</xsl:stylesheet>