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
                                                                 
 Copyright IBM Corp. 2001, 2009  All Rights Reserved.            
                                                                 
 US Government Users Restricted Rights - Use, duplication or     
 disclosure restricted by GSA ADP Schedule Contract with         
 IBM Corp.                                                       
                                                                 
*****************************************************************
		</xsl:comment>

		<config xmlns="http://www.ibm.com/profiles-config" xmlns:tns="http://www.ibm.com/profiles-config"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="profiles"
			xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd">
			<xsl:comment> START PROFILES DATA MODELS SECTION </xsl:comment>
			<xsl:for-each select="tns:profileDataModels" >
				<xsl:copy>
					<xsl:for-each select="tns:profileExtensionAttributes" >
						<xsl:copy>
						<xsl:comment> This extension attribute is required by the 'MyLinks' profile widget </xsl:comment>
						<xsl:element name="xmlFileAttribute">
							<xsl:attribute name="extensionId">profileLinks</xsl:attribute>
							<xsl:attribute name="schemaFile">profile-links.xsd</xsl:attribute>
							<xsl:attribute name="indexBindingExpr">/linkroll/link/@name | /linkroll/link/@url</xsl:attribute>
							<xsl:element name="indexFields">
								<xsl:element name="indexField">
									<xsl:attribute name="fieldName">linkName</xsl:attribute>
									<xsl:attribute name="fieldExpr">/linkroll/link/@name</xsl:attribute>
								</xsl:element>
							</xsl:element>
						</xsl:element>
						<xsl:copy-of select="tns:simpleAttribute"/>
						<xsl:copy-of select="tns:richtextAttribute"/>
						</xsl:copy>
					</xsl:for-each>
					<xsl:for-each select="tns:profileDataModel" >
						<xsl:copy>
						<xsl:comment> =================================================================================== </xsl:comment>
						<xsl:comment> This section is used to define attributes that are updated via the drafting process </xsl:comment>
						<xsl:comment>   In most deployments you should never edit the config for this section.			 </xsl:comment>
						<xsl:comment> Example: &lt;draftableAttribute&gt;displayName&lt;draftableAttribute/&gt;                  	 </xsl:comment>
						<xsl:comment> Example: Example: &lt;draftableExtensionAttribute extensionIdRef="tieline"/&gt;			 </xsl:comment>
						<xsl:comment> =================================================================================== </xsl:comment>
						</xsl:copy>
					</xsl:for-each>
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> END PROFILES DATA MODELS SECTION </xsl:comment>

			<xsl:comment> START LAYOUT CONFIGURATION SECTION </xsl:comment>
			<xsl:for-each select="tns:layoutConfiguration" >
				<xsl:copy>
				<xsl:comment>
		            UI configuration for vcard export feature.
		 			
		            The first charset defined is the default.
				</xsl:comment>
					<xsl:element name="vcardExport">
						<xsl:element name="charset">
							<xsl:attribute name="name">UTF-8</xsl:attribute>
							<xsl:element name="label">
								<xsl:attribute name="labelKey">label.vcard.encoding.utf8</xsl:attribute>
							</xsl:element>
						</xsl:element>
						<xsl:element name="charset">
							<xsl:attribute name="name">ISO-8859-1</xsl:attribute>
							<xsl:element name="label">
								<xsl:attribute name="labelKey">label.vcard.encoding.iso88591</xsl:attribute>
							</xsl:element>
						</xsl:element>
						<xsl:element name="charset">
							<xsl:attribute name="name">Cp943c</xsl:attribute>
							<xsl:element name="label">
								<xsl:attribute name="labelKey">label.vcard.encoding.cp943c</xsl:attribute>
							</xsl:element>
						</xsl:element>
					</xsl:element>
					<xsl:element name="searchLayout">
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">true</xsl:attribute>
							<xsl:text>displayName</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>preferredFirstName</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>preferredLastName</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">true</xsl:attribute>
							<xsl:text>profileTags</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">true</xsl:attribute>
							<xsl:text>jobResp</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>experience</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>background</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">true</xsl:attribute>
							<xsl:text>organizationTitle</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>workLocation.city</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>workLocation.state</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>countryDisplayValue</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>email</xsl:text>
						</xsl:element>
						<xsl:element name="attribute">
							<xsl:attribute name="showLabel">false</xsl:attribute>
							<xsl:text>telephoneNumber</xsl:text>
						</xsl:element>
					</xsl:element>
					
					<xsl:element name="searchResultsLayout">
						<xsl:attribute name="profileType">default</xsl:attribute>
						<xsl:element name="column">
							<xsl:element name="attribute">
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="photo">true</xsl:attribute>
								<xsl:attribute name="uid">key</xsl:attribute>
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:text>profilePicture</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:element name="column">
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="hcard">true</xsl:attribute>
								<xsl:attribute name="uid">key</xsl:attribute>
								<xsl:attribute name="userid">userid</xsl:attribute>
								<xsl:text>displayName</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml"></xsl:attribute>
								<xsl:attribute name="appendHtml"></xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>jobResp</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml"></xsl:attribute>
								<xsl:attribute name="appendHtml"></xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>organizationTitle</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml"></xsl:attribute>
								<xsl:attribute name="appendHtml"></xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>workLocation.city</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml"></xsl:attribute>
								<xsl:attribute name="appendHtml"></xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>workLocation.state</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml"></xsl:attribute>
								<xsl:attribute name="appendHtml"></xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>countryDisplayValue</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:element name="column">
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;strong&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/strong&gt;</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>telephoneNumber</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;strong&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/strong&gt;</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="email">true</xsl:attribute>
								<xsl:text>email</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;strong&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/strong&gt;</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="email">true</xsl:attribute>
								<xsl:text>groupwareEmail</xsl:text>
							</xsl:element>
						</xsl:element>
					</xsl:element>

					<xsl:element name="businessCardLayout">
						<xsl:attribute name="profileType">default</xsl:attribute>
						<xsl:element name="attributes">
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;h2 id='%displayNameId%'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/h2&gt;</xsl:attribute>
								<xsl:text>displayName</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;p class='title'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/p&gt;</xsl:attribute>
								<xsl:text>jobResp</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;p class='role'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/p&gt;</xsl:attribute>
								<xsl:text>title</xsl:text>
							</xsl:element>

							<xsl:element name="html">
								<xsl:attribute name="prependHtml">&lt;div class='adr'&gt;</xsl:attribute>
							</xsl:element>

							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;span class='locality'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/span&gt;</xsl:attribute>
								<xsl:text>workLocation.city</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="prependHtml">,&amp;nbsp;&lt;span class='region'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/span&gt;</xsl:attribute>
								<xsl:text>workLocation.state</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&amp;nbsp;&lt;span class='country-name'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/span&gt;</xsl:attribute>
								<xsl:text>countryDisplayValue</xsl:text>
							</xsl:element>
								
							<xsl:element name="html">
								<xsl:attribute name="prependHtml">&lt;p class='extended-address'&gt;</xsl:attribute>
							</xsl:element>

							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&amp;nbsp;|&amp;nbsp;</xsl:attribute>
								<xsl:attribute name="labelKey">label.contactInformation.bldgId</xsl:attribute>
								<xsl:text>bldgId</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&amp;nbsp;|&amp;nbsp;</xsl:attribute>
								<xsl:attribute name="labelKey">label.contactInformation.floor</xsl:attribute>
								<xsl:text>floor</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="labelKey">label.contactInformation.officeName</xsl:attribute>
								<xsl:text>officeName</xsl:text>
							</xsl:element>
								
							<xsl:element name="html">
								<xsl:attribute name="prependHtml">&lt;/p&gt;</xsl:attribute>
							</xsl:element>
							<xsl:element name="html">
								<xsl:attribute name="prependHtml">&lt;/div&gt;</xsl:attribute>
							</xsl:element>

							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;p class='tel'&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/p&gt;</xsl:attribute>
								<xsl:text>telephoneNumber</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;p&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/p&gt;</xsl:attribute>
								<xsl:attribute name="email">true</xsl:attribute>
								<xsl:text>email</xsl:text>
							</xsl:element>															
						</xsl:element>

						<xsl:element name="actions">
							<xsl:element name="action">
								<xsl:attribute name="urlPattern">mailto:{email}</xsl:attribute>
								<xsl:attribute name="emailEnabledRequired">true</xsl:attribute>
								<xsl:attribute name="liClass">lotusSendMail</xsl:attribute>
								<xsl:element name="label">
									<xsl:attribute name="labelKey">personCardSendMail</xsl:attribute>
								</xsl:element>
								<xsl:element name="icon">
									<xsl:attribute name="href">{profilesSvcRef}/nav/common/styles/images/iconSendMail.gif</xsl:attribute>
									<xsl:element name="alt">
										<xsl:attribute name="labelKey">personCardSendMail</xsl:attribute>
									</xsl:element>
								</xsl:element>
							</xsl:element>
							<xsl:element name="action">
								<xsl:attribute name="urlPattern">{profilesSvcRef}/html/wc.do?action=fr&amp;requireAuth=true&amp;widgetId=friends&amp;targetKey={key}</xsl:attribute>
								<xsl:attribute name="liClass">lotusMenuSeparator</xsl:attribute>
								<xsl:element name="label">
									<xsl:attribute name="labelKey">personCardAddAsColleagues</xsl:attribute>
								</xsl:element>
							</xsl:element>
							<xsl:element name="action">
								<xsl:attribute name="urlPattern">{profilesSvcRef}/vcard/profile.do?key={key}</xsl:attribute>
								<xsl:attribute name="liClass">lotusMenuSeparator</xsl:attribute>
								<xsl:element name="label">
									<xsl:attribute name="labelKey">personCardDownloadVCard</xsl:attribute>
								</xsl:element>
							</xsl:element>
						</xsl:element>
					</xsl:element>
					<xsl:element name="profileLayout">
						<xsl:attribute name="profileType">default</xsl:attribute>
						<xsl:element name="jobInformation">
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;strong&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/strong&gt;&lt;br/&gt;</xsl:attribute>
								<xsl:text>jobResp</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;br/&gt;</xsl:attribute>
								<xsl:text>employeeTypeDesc</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;br/&gt;</xsl:attribute>
								<xsl:text>organizationTitle</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">, </xsl:attribute>
								<xsl:text>workLocation.city</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">, </xsl:attribute>
								<xsl:text>workLocation.state</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;br/&gt;</xsl:attribute>
								<xsl:text>countryDisplayValue</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;br/&gt;</xsl:attribute>
								<xsl:text>telephoneNumber</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="email">true</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;br/&gt;</xsl:attribute>
								<xsl:text>email</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="hcard">true</xsl:attribute>
								<xsl:attribute name="email">secretaryEmail</xsl:attribute>
								<xsl:attribute name="uid">secretaryUid</xsl:attribute>
								<xsl:attribute name="userid">secretaryUserid</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;br/&gt;</xsl:attribute>
								<xsl:text>secretaryName</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:element name="contactInformation">
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="disabled">true</xsl:attribute>
								<xsl:attribute name="prependHtml">&lt;strong&gt;</xsl:attribute>
								<xsl:attribute name="appendHtml">&lt;/strong&gt;&lt;br/&gt;</xsl:attribute>
								<xsl:text>displayName</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>bldgId</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>floor</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>officeName</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>telephoneNumber</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>ipTelephoneNumber</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>mobileNumber</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>pagerNumber</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>faxNumber</xsl:text>
							</xsl:element>
							<xsl:element name="attribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="email">true</xsl:attribute>
								<xsl:text>email</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="email">true</xsl:attribute>
								<xsl:text>groupwareEmail</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:attribute name="blogUrl">true</xsl:attribute>
								<xsl:text>blogUrl</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:element name="associatedInformation">
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">false</xsl:attribute>
								<xsl:attribute name="richtext">true</xsl:attribute>
								<xsl:text>description</xsl:text>
							</xsl:element>
							<xsl:element name="editableAttribute">
								<xsl:attribute name="showLabel">true</xsl:attribute>
								<xsl:attribute name="richtext">true</xsl:attribute>
								<xsl:attribute name="hideIfEmpty">true</xsl:attribute>
								<xsl:text>experience</xsl:text>
							</xsl:element>
						</xsl:element>
					</xsl:element>
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> END LAYOUT CONFIGURATION SECTION </xsl:comment>

			<xsl:comment> START API CONFIGURATION SECTION </xsl:comment>
			<xsl:copy-of select="tns:apiConfiguration" />
			<xsl:comment> END API CONFIGURATION SECTION </xsl:comment>
			
			<xsl:comment> CACHE CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:caches" />
			<xsl:comment> CACHE CONFIG END </xsl:comment>
			
			<xsl:comment> DATA ACCESS CONFIG START </xsl:comment>
			<xsl:for-each select="tns:dataAccess">
				<xsl:copy>
					<xsl:comment>
						indicates if organizational data is available (i.e. managerUid)
					</xsl:comment>
					<xsl:element name="organizationalStructureEnabled">
						<xsl:text>true</xsl:text>
					</xsl:element>
					<xsl:comment>
						indicates which fields need to be resolved before being displayed
					</xsl:comment>
					<xsl:comment>
						WARNING: in most deployments the resolvedCodes section should not be
						modified
					</xsl:comment>
					<xsl:element name="resolvedCodes">
						<xsl:element name="resolvedCode">
							<xsl:text>workLocationCode</xsl:text>
						</xsl:element>
						<xsl:element name="resolvedCode">
							<xsl:text>orgId</xsl:text>
						</xsl:element>
						<xsl:element name="resolvedCode">
							<xsl:text>employeeTypeCode</xsl:text>
						</xsl:element>
						<xsl:element name="resolvedCode">
							<xsl:text>countryCode</xsl:text>
						</xsl:element>
						<xsl:element name="resolvedCode">
							<xsl:text>departmentCode</xsl:text>
						</xsl:element>
					</xsl:element>
					<xsl:comment>
						search preferences
					</xsl:comment>
					<xsl:comment>
						WARNING: in most deployments the departmentSearchOnCode,
						departmentSearchOnCode,
					</xsl:comment>
					<xsl:comment>
						and locationSearchOnCode should not be modified
					</xsl:comment>
					<xsl:element name="search">
						<xsl:element name="departmentSearchOnCode">
							<xsl:text>false</xsl:text>
						</xsl:element>
						<xsl:element name="departmentField">
							<xsl:text>organization</xsl:text>
						</xsl:element>
						<xsl:element name="locationSearchOnCode">
							<xsl:text>false</xsl:text>
						</xsl:element>
						<xsl:element name="maxRowsToReturn">
							<xsl:text>250</xsl:text>
						</xsl:element>
						<xsl:element name="pageSize">
							<xsl:text>10</xsl:text>
						</xsl:element>
						<xsl:element name="firstNameSearch">
							<xsl:attribute name="enabled">false</xsl:attribute>
						</xsl:element>
						<xsl:element name="kanjiNameSearch">
							<xsl:attribute name="enabled">true</xsl:attribute>
							<xsl:attribute name="default">false</xsl:attribute>
						</xsl:element>
					</xsl:element>
					<xsl:comment> Directory integration configuration </xsl:comment>
					<xsl:element name="directory">
						<xsl:comment>
							Specifies the profiles field that is used to resolve person records
							via WALTZ / Javelin
						</xsl:comment>
						<xsl:element name="lconnUserIdField">
							<xsl:text>guid</xsl:text>
						</xsl:element>
						<xsl:comment>
							Lists fields that will be used to resolve logins against
						</xsl:comment>
						<xsl:element name="loginAttributes">
							<xsl:element name="loginAttribute">
								<xsl:text>uid</xsl:text>
							</xsl:element>
							<xsl:element name="loginAttribute">
								<xsl:text>email</xsl:text>
							</xsl:element>
							<xsl:element name="loginAttribute">
								<xsl:text>loginId</xsl:text>
							</xsl:element>
						</xsl:element>
						<xsl:comment>
							Should not be changed; added to handle boundary case
						</xsl:comment>
						<xsl:element name="deferLoginResolution">
							<xsl:attribute name="enabled">false</xsl:attribute>
						</xsl:element>
					</xsl:element>
					<xsl:comment>
						Optional security setting for Profiles javelin card.  This setting is to disallow JSONP security.  
						Older 3rd party software may will not work with this setting unless they include a reverse proxy.  
						All of the Connections application will work with JSONP disabled.
					</xsl:comment>
					<xsl:element name="allowJsonpJavelin">
						<xsl:attribute name="enabled">true</xsl:attribute>
					</xsl:element>
					<xsl:comment>
						Untested/non-'supported' feature that adds 'simple' extension
						attributes to javelin JS for implementors that manually edit javelin
						card JSP.
					</xsl:comment>
					<xsl:element name="includeExtensionsInJavelinJS">
						<xsl:attribute name="enabled">false</xsl:attribute>
					</xsl:element>
				</xsl:copy>
			</xsl:for-each>
			<xsl:comment> DATA ACCESS CONFIG END </xsl:comment>

			<xsl:comment> STATISTICS CONFIG START </xsl:comment>
			<xsl:copy-of select="tns:statistics" />
			<xsl:comment> STATISTICS CONFIG END </xsl:comment>

			<xsl:copy-of select="tns:acf" />
			<xsl:copy-of select="tns:sametimeAwareness" />
			
			<xsl:copy-of select="tns:tagOthers" />
			<xsl:copy-of select="tns:javelinGWMailSearch" />
			
			<xsl:comment> BOARD UI ENABLEMENT </xsl:comment>
			<xsl:element name="board">
				<xsl:attribute name="enabled">true</xsl:attribute>
			</xsl:element>
			<xsl:element name="boardInProfileHome">
				<xsl:attribute name="enabled">true</xsl:attribute>
			</xsl:element>
			<xsl:element name="boardInProfileView">
				<xsl:attribute name="enabled">true</xsl:attribute>
			</xsl:element>
			<xsl:element name="statusUpdatesInProfileView">
				<xsl:attribute name="enabled">true</xsl:attribute>
			</xsl:element>
			<xsl:element name="boardNetworkACL">
				<xsl:attribute name="enabled">false</xsl:attribute>
			</xsl:element>
		</config>
	</xsl:template>
</xsl:stylesheet>
