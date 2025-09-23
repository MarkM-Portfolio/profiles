<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2014                                          -->
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

	<!-- update the scheduled task RefreshSystemObjectsTask -->
	<xsl:template match="/tns:config/tns:scheduledTasks/tns:task[@name='RefreshSystemObjectsTask']">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="interval">0 0 1 * * ?</xsl:attribute>
			<xsl:attribute name="enabled">true</xsl:attribute>
			<xsl:attribute name="type">internal</xsl:attribute>			
			<xsl:for-each select="*">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<!-- add the scheduled task PhotoSyncTask after the DbCleanupTask -->
	<xsl:template match="/tns:config/tns:scheduledTasks/tns:task[@name='DbCleanupTask']">
		<xsl:copy-of select="current()"/>
		<task xmlns="http://www.ibm.com/profiles-config">
			<xsl:attribute name="name">PhotoSyncTask</xsl:attribute>
			<xsl:attribute name="interval">0 */5 * * * ?</xsl:attribute>
			<xsl:attribute name="enabled">true</xsl:attribute>
			<xsl:attribute name="type">internal</xsl:attribute>	
			<args>
				<property name="photoSyncBatchSize">200</property>
			</args>
		</task>	
	</xsl:template>

	<!-- update any business card action to "download vcard" with an acl check -->
	<xsl:template match="/tns:config/tns:layoutConfiguration/tns:businessCardLayout/tns:actions/tns:action[contains(@urlPattern,'lconn.profiles.bizCard.bizCardUI.openVCardDialog')]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="requireAcl">profile.profile$profile.profile.view</xsl:attribute>			
			<xsl:for-each select="*">
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:copy>		
	</xsl:template>	

</xsl:stylesheet>