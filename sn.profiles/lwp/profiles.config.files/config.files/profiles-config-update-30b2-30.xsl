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
                                                                 
 Copyright IBM Corp. 2001, 2010  All Rights Reserved.            
                                                                 
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
			<xsl:for-each select="tns:layoutConfiguration">
				<xsl:copy>
					<xsl:copy-of select="tns:vcardExport" />
					<xsl:copy-of select="tns:searchLayout" />
					<xsl:copy-of select="tns:searchResultsLayout" />
						
					<xsl:for-each select="tns:businessCardLayout">
						<xsl:copy>
							<xsl:attribute name="profileType"><xsl:value-of select="@profileType"/></xsl:attribute>
							<xsl:copy-of select="tns:attributes" />
							<xsl:for-each select="tns:actions">
								<xsl:copy>
									<xsl:for-each select="tns:action">
										<xsl:choose>
											<xsl:when test="current()/tns:label/@labelKey='personCardAddAsColleagues'">
												<xsl:comment>Do nothing; skip element</xsl:comment>
											</xsl:when>
											<xsl:otherwise>
												<xsl:copy-of select="current()"/>
											</xsl:otherwise>
										</xsl:choose>									
									</xsl:for-each>
									<action urlPattern="javascript:lconn.profiles.bizCard.bizCardUI.openNetworkInviteDialog('{{key}}', window.X_loggedInUserKey, ((window.dojo &amp;&amp; typeof(dojo.getObject) == 'function' &amp;&amp; dojo.getObject('lconn.profiles.Friending.currentViewDomNode')) || (document.getElementById('lotusSearchResultsContent') || new Object()).id || (document.getElementById('contentArea') || new Object()).id || (document.getElementById('lotusMain') || new Object()).id || null))" liClass="lotusMenuSeparator"
										    requireAcl="profile.colleague$profile.colleague.connect">
										<label labelKey="personCardAddAsColleagues"/>
									</action>
								</xsl:copy>
							</xsl:for-each>
						</xsl:copy>
					</xsl:for-each>
					
					<xsl:copy-of select="tns:profileLayout" />
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> END LAYOUT CONFIGURATION SECTION </xsl:comment>

			<xsl:comment> START API CONFIGURATION SECTION </xsl:comment>
			<xsl:copy-of select="tns:apiConfiguration" />
			<xsl:comment> END API CONFIGURATION SECTION </xsl:comment>
			
			<xsl:comment> CACHE CONFIG START </xsl:comment>
			<xsl:for-each select="tns:caches">
				<xsl:copy>
					<xsl:for-each select="tns:fullReportsToChainCache">
						<xsl:copy>
							<xsl:copy-of select="tns:enabled" />
							<xsl:copy-of select="tns:size" />
							<xsl:copy-of select="tns:refreshTime" />
							<xsl:element name="refreshInterval">20</xsl:element>
							<xsl:copy-of select="tns:startDelay" />
							<xsl:copy-of select="tns:ceouid" />
						</xsl:copy>
					</xsl:for-each>
					<xsl:for-each select="tns:profileObjectCache">
						<xsl:copy>
							<xsl:copy-of select="tns:refreshTime"/>
							<xsl:element name="refreshInterval">15</xsl:element>
							<xsl:copy-of select="tns:startDelay"/>
						</xsl:copy>
					</xsl:for-each>
				</xsl:copy>	
			</xsl:for-each>
			<xsl:comment> CACHE CONFIG END </xsl:comment>
			
			<xsl:comment> DATA ACCESS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:dataAccess"/>
			<xsl:comment> DATA ACCESS CONFIG END </xsl:comment>

			<xsl:comment> STATISTICS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:statistics" />
			<xsl:comment> STATISTICS CONFIG END </xsl:comment>

			<xsl:copy-of select="tns:acf" />
			<xsl:copy-of select="tns:sametimeAwareness"/>
			<xsl:copy-of select="tns:javelinGWMailSearch" />

			<xsl:element name="properties">
				<xsl:copy-of select="tns:properties/tns:property"/>
			</xsl:element>			
			
			<xsl:comment> BOARD UI ENABLEMENT </xsl:comment>
			<xsl:copy-of select="tns:board" />
		</config>
	</xsl:template>
</xsl:stylesheet>
