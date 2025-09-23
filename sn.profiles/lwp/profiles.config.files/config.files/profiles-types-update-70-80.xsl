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
	xmlns:tns="http://www.ibm.com/profiles-config"
    xmlns:pt="http://www.ibm.com/profiles-types"
    xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
    
	<!-- XSLT identity transform -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="/pt:config/pt:type">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
			<property xmlns="http://www.ibm.com/profiles-types">
				<ref>chatIdentity</ref>
				<updatability>readwrite</updatability>
				<hidden>true</hidden>
				<fullTextIndexed>false</fullTextIndexed>
			</property>
			<property xmlns="http://www.ibm.com/profiles-types">
				<ref>meetingIdentity</ref>
				<updatability>readwrite</updatability>
				<hidden>true</hidden>
				<fullTextIndexed>false</fullTextIndexed>
			</property>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
