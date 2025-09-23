<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2001, 2013                                    -->
<!--                                                                   -->
<!-- The source code for this program is not published or otherwise    -->
<!-- divested of its trade secrets, irrespective of what has been      -->
<!-- deposited with the U.S. Copyright Office.                         -->
<!--                                                                   -->
<!-- ***************************************************************** -->
<!-- 5724_S68 -->

<xsl:stylesheet version="1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tns="http://www.ibm.com/profiles-config" xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
	
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
	    
	<xsl:template match="/tns:config">
	<xsl:comment>		
*****************************************************************
                                                                 
 Licensed Materials - Property of IBM                            
                                                                 
 5724-S68                                                        
                                                                 
 Copyright IBM Corp. 2001, 2010  All Rights Reserved.            
                                                                 
 US Government Users Restricted Rights - Use, duplication or     
 disclosure restricted by GSA ADP Schedule Contract with         
 IBM Corp.                                                       
                                                                 
*****************************************************************
	</xsl:comment>

	<config id="profiles"
			xmlns="http://www.ibm.com/profiles-config"
			xmlns:tns="http://www.ibm.com/profiles-config"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
			
		<xsl:comment> START PROFILES DATA MODELS SECTION </xsl:comment>
		<xsl:copy-of select="tns:profileDataModels"/>
		<xsl:comment> END PROFILES DATA MODELS SECTION </xsl:comment>
	
		<xsl:comment> START LAYOUT CONFIGURATION SECTION </xsl:comment>
		<xsl:for-each select="tns:layoutConfiguration">
			<xsl:copy>
				<xsl:comment> UI Template rendering configuration information </xsl:comment>
				<xsl:copy-of select="tns:templateConfiguration"/>
			
				<xsl:comment> UI configuration for vcard export feature. The first charset defined is the default. </xsl:comment>
				<xsl:copy-of select="tns:vcardExport"/>
			
				<xsl:for-each select="tns:searchLayout">
					<xsl:copy>
						<xsl:for-each select="*">
							<xsl:choose>
								<xsl:when test="self::node()[text()='background']">
									<attribute><xsl:attribute name="showLabel"><xsl:value-of select="@showLabel" /></xsl:attribute>description</attribute>
								</xsl:when>
								<xsl:otherwise>
									<xsl:copy-of select="current()"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:for-each>
					</xsl:copy>
				</xsl:for-each>
			
				<xsl:copy-of select="tns:businessCardLayout"/>
			
				<xsl:copy-of select="tns:profileLayout"/>
			</xsl:copy>
			</xsl:for-each><xsl:comment> END LAYOUT CONFIGURATION SECTION </xsl:comment>
				
			<xsl:comment> CACHE CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:caches"/>
			<xsl:comment> CACHE CONFIG END </xsl:comment>
			
			<xsl:comment> DATA ACCESS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:dataAccess"/>
			<xsl:comment> DATA ACCESS CONFIG END </xsl:comment>
			
			<xsl:copy-of select="tns:acf"/>
			<xsl:copy-of select="tns:sametimeAwareness"/>
			<xsl:copy-of select="tns:javelinGWMailSearch"/>
			
			<xsl:comment> Additional config settings START </xsl:comment>
			<xsl:copy-of select="tns:properties"/>
			<xsl:comment> Additional config settings END </xsl:comment>
			
			<xsl:comment> START SCHEDULED TASKS CONFIGURATION </xsl:comment>
			<xsl:copy-of select="tns:scheduledTasks"/>
			<xsl:comment> END SCHEDULED TASKS CONFIGURATION </xsl:comment>
		</config>
	</xsl:template>
</xsl:stylesheet>