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
	xmlns:tns="http://www.ibm.com/profiles-config" 
	xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">

	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
	
	<xsl:attribute-set name="FieldDepartmentNumber">
		<xsl:attribute name="fieldId">FIELD_DEPARTMENT_NUMBER</xsl:attribute>
		<xsl:attribute name="fieldSearchable">true</xsl:attribute>
		<xsl:attribute name="exactMatchSupported">true</xsl:attribute>
	</xsl:attribute-set>

	<!-- XSLT identity transform -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- rtc 162733 make FIELD_DEPARTMENT_NUMBER exact match-->
	<!--  case: /config/profileDataModels exists, /searchAttributeConfig does not -->
	<xsl:template match="/tns:config/tns:profileDataModels[not(tns:searchAttributeConfig)]">
		<xsl:copy>
			<xsl:copy-of select="@*|node()"/>
			<searchAttributeConfig xmlns="http://www.ibm.com/profiles-config">
				 <searchAttribute fieldId="FIELD_DEPARTMENT_NUMBER" fieldSearchable="true" exactMatchSupported="true" />
	    	</searchAttributeConfig>
	    </xsl:copy>
	</xsl:template>
	<!--  case: /config/profileDataModels/searchAttributeConfig exists, entry for FIELD... does not -->
	<xsl:template match="/tns:config/tns:profileDataModels/tns:searchAttributeConfig[not(tns:searchAttribute[@fieldId='FIELD_DEPARTMENT_NUMBER'])]">
		<xsl:copy>
			<xsl:copy-of select="@*|node()"/>
			<searchAttribute  xmlns="http://www.ibm.com/profiles-config" xsl:use-attribute-sets="FieldDepartmentNumber"></searchAttribute>		
		</xsl:copy>
	</xsl:template>
	
	<!-- allowJsonJavelin default to change from true to false -->
	<!-- this step is done in profiles-config-update-55CR1-60.xsl -->
	<!-- <xsl:template match="/tns:config/tns:dataAccess/tns:allowJsonpJavelin[@enabled='true']"> -->
	<!--	<allowJsonpJavelin xmlns="http://www.ibm.com/profiles-config" enabled="false"/> -->
	<!-- </xsl:template> -->

	<!-- update the scheduled task PhotoSyncTask; change name to ProfileSyncTask and photoSyncBatchSize to profileSyncBatchSize-->
	<xsl:template match="/tns:config/tns:scheduledTasks/tns:task[@name='PhotoSyncTask']">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:attribute name="name">ProfileSyncTask</xsl:attribute>

			<xsl:for-each select="tns:args">
				<xsl:copy>
					<xsl:for-each select="tns:property">
						<xsl:copy>
							<xsl:for-each select="@*">
								<xsl:choose>
									<xsl:when test=".='photoSyncBatchSize'">
										<xsl:attribute name="name">profileSyncBatchSize</xsl:attribute>
									</xsl:when>
									<xsl:otherwise>
										<xsl:copy/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
							<xsl:value-of select="."/>
						</xsl:copy>
					</xsl:for-each>
				</xsl:copy>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>	

</xsl:stylesheet>