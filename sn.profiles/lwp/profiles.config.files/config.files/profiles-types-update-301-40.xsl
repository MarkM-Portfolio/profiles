<?xml version="1.0" encoding="UTF-8"?>
<!-- ***************************************************************** -->
<!-- -->
<!-- IBM Confidential -->
<!-- -->
<!-- OCO Source Materials -->
<!-- -->
<!-- Copyright IBM Corp. 2001, 2012 -->
<!-- -->
<!-- The source code for this program is not published or otherwise -->
<!-- divested of its trade secrets, irrespective of what has been -->
<!-- deposited with the U.S. Copyright Office. -->
<!-- -->
<!-- ***************************************************************** -->

<!-- 5724_S68 -->
<xsl:stylesheet version="1.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:tns="http://www.ibm.com/profiles-config" xsi:schemaLocation="http://www.ibm.com/profiles-config profiles-config.xsd"
	xmlns:pt="http://www.ibm.com/profiles-types">
	<xsl:output method="xml" indent="yes" />
	<xsl:strip-space elements="*"/>
	
	<!-- the set of standard fields allowed -->
	<xsl:variable name="standardAttributes">
		alternateLastname
		bldgId
		blogUrl
		calendarUrl
		countryCode
		courtesyTitle
		deptNumber
		description
		displayName
		distinguishedName
		email
		employeeNumber
		employeeTypeCode
		experience
		faxNumber
		floor
		freeBusyUrl
		givenName
		groupwareEmail
		guid
		ipTelephoneNumber
		isManager
		jobResp
		key
		lastUpdate
		loginId
		managerUid
		mobileNumber
		nativeFirstName
		nativeLastName
		officeName
		orgId
		pagerId
		pagerNumber
		pagerServiceProvider
		pagerType
		preferredFirstName
		preferredLanguage
		preferredLastName
		profileType
		secretaryUid
		shift
		sourceUrl
		surname
		telephoneNumber
		tenantKey
		timezone
		title
		uid
		workLocationCode
	</xsl:variable>
	
	<!-- find the global extension elements -->
	<xsl:variable name="extensionAttributes" select="/tns:config/tns:profileDataModels/tns:profileExtensionAttributes/*"/>
	<!-- find the global extension attribute ids -->
	<xsl:variable name="extensionAttributesIds" select="$extensionAttributes/@extensionId"/>				
	<!-- find the total list of profile types defined in api model -->
	<xsl:variable name="apiProfileTypes" select="/tns:config/tns:apiConfiguration/tns:apiModel"/>
	<!-- find the total list of profile type ids in api model -->
	<xsl:variable name="apiProfileTypesIds" select="$apiProfileTypes/@profileType"/>
	<!-- find the total list of profile types defined in the layout model -->
	<xsl:variable name="layoutProfileTypes" select="/tns:config/tns:layoutConfiguration/tns:profileLayout"/>
	<!-- find the total list of profile types ids defined in the layout model -->
	<xsl:variable name="layoutProfileTypesIds" select="$layoutProfileTypes/@profileType"/>
	
	<xsl:template name="migrateApiProfileType">
		<xsl:param name="apiProfileType"/>
		<xsl:variable name="profileTypeId" select="@profileType"/>
		<xsl:variable name="layoutProfileType" select="$layoutProfileTypes[@profileType=$profileTypeId]"/>
		<xsl:variable name="hasLayout" select="boolean($layoutProfileType)"/>
		<type xmlns="http://www.ibm.com/profiles-types">
			<parentId>snx:person</parentId>
			<id><xsl:value-of select="$profileTypeId"/></id>
			<xsl:for-each select="*">
				<xsl:variable name="isExtensionAttribute" select="local-name() = 'editableApiExtensionAttribute' or local-name() = 'hiddenApiExtensionAttribute'"/>
				<xsl:variable name="isReadwrite" select="local-name() = 'editableApiAttribute' or local-name() = 'editableApiExtensionAttribute'"/>
				<xsl:variable name="isHidden" select="local-name() = 'hiddenApiAttribute' or local-name() = 'hiddenApiExtensionAttribute'"/>
				<xsl:variable name="ref">
					<xsl:choose>
						<xsl:when test="$isExtensionAttribute">
							<xsl:value-of select="@extensionIdRef"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<xsl:variable name="updatability">
					<xsl:choose>
						<xsl:when test="$isReadwrite">
							<xsl:text>readwrite</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>read</xsl:text>
						</xsl:otherwise>
					</xsl:choose>				
				</xsl:variable>
				<xsl:variable name="isRichText">
					<xsl:choose>
						<xsl:when test="$hasLayout">
							<xsl:value-of select="boolean($layoutProfileType/*/*[@richtext='true' and .=$ref])"/>
						</xsl:when>
						<xsl:otherwise>
						false
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>								
				<property xmlns="http://www.ibm.com/profiles-types">
					<ref><xsl:value-of select="$ref"/></ref>
					<updatability><xsl:value-of select="$updatability"/></updatability>
					<hidden><xsl:value-of select="string($isHidden)"/></hidden>
					<xsl:if test="$isRichText='true'">
					<richText>true</richText>
					</xsl:if>
				</property>
			</xsl:for-each>	
			<xsl:if test="$hasLayout">
				<xsl:call-template name="migrateLayout">
					<xsl:with-param name="layoutProfileType" select="$layoutProfileType"/>
				</xsl:call-template>
			</xsl:if>
		</type>					
	</xsl:template>
	
	<xsl:template name="migrateLayout">
		<xsl:param name="layoutProfileType"/>		
		<xsl:variable name="jobInformation" select="$layoutProfileType/tns:jobInformation"/>
		<xsl:variable name="contactInformation" select="$layoutProfileType/tns:contactInformation"/>
		<xsl:variable name="associatedInformation" select="$layoutProfileType/tns:associatedInformation"/>
		<xsl:for-each select="$jobInformation/*">
			<xsl:variable name="index" select="position()"/>			
			<xsl:call-template name="migrateLayoutAttribute">
				<xsl:with-param name="attribute" select="."/>
				<xsl:with-param name="duplicates" select="$apiProfileTypes[@profileType=$layoutProfileType/@profileType]/tns:editableApiAttribute/. | $apiProfileTypes[@profileType=$layoutProfileType/@profileType]/tns:editableApiExtensionAttribute/@extensionIdRef | $jobInformation/*[position() &lt; $index]"/>
			</xsl:call-template>			
		</xsl:for-each>	
		<xsl:for-each select="$contactInformation/*">
			<xsl:variable name="index" select="position()"/>
			<xsl:call-template name="migrateLayoutAttribute">
				<xsl:with-param name="attribute" select="."/>
				<xsl:with-param name="duplicates" select="$apiProfileTypes[@profileType=$layoutProfileType/@profileType]/tns:editableApiAttribute/. | $apiProfileTypes[@profileType=$layoutProfileType/@profileType]/tns:editableApiExtensionAttribute/@extensionIdRef | $jobInformation/tns:attribute/. | $jobInformation/tns:editableAttribute/. | $jobInformation/tns:extensionAttribute/@extensionIdRef | $contactInformation/*[position() &lt; $index]"/>
			</xsl:call-template>			
		</xsl:for-each>		
		<xsl:for-each select="$associatedInformation/*">
			<xsl:variable name="index" select="position()"/>
			<xsl:call-template name="migrateLayoutAttribute">
				<xsl:with-param name="attribute" select="."/>
				<xsl:with-param name="duplicates" select="$apiProfileTypes[@profileType=$layoutProfileType/@profileType]/tns:editableApiAttribute/. | $apiProfileTypes[@profileType=$layoutProfileType/@profileType]/tns:editableApiExtensionAttribute/@extensionIdRef | $jobInformation/tns:attribute/. | $jobInformation/tns:editableAttribute/. | $jobInformation/tns:extensionAttribute/@extensionIdRef | $contactInformation/tns:attribute/. | $contactInformation/tns:editableAttribute/. | $contactInformation/tns:extensionAttribute/@extensionIdRef | $associatedInformation/*[position() &lt; $index]"/>
			</xsl:call-template>			
		</xsl:for-each>	
	</xsl:template>
	
	<xsl:template name="migrateLayoutAttribute">
		<xsl:param name="attribute"/>
		<xsl:param name="duplicates"/>
		<xsl:variable name="isExtensionAttribute" select="local-name() = 'extensionAttribute'"/>
		<xsl:variable name="isReadwrite" select="(local-name() = 'editableAttribute' and string(@disabled) !='true') or ($isExtensionAttribute and @editable='true')"/>
		<xsl:variable name="isRichText" select="@richtext='true'"/>
		<xsl:variable name="isCode">
			<xsl:choose>
				<xsl:when test=". = 'secretaryName'">true</xsl:when>
				<xsl:when test=". = 'secretaryEmail'">true</xsl:when>
				<xsl:when test=". = 'secretaryKey'">true</xsl:when>
				<xsl:when test=". = 'employeeTypeDesc'">true</xsl:when>
				<xsl:when test=". = 'workLocation.address1'">true</xsl:when>
				<xsl:when test=". = 'workLocation.address2'">true</xsl:when>
				<xsl:when test=". = 'workLocation.city'">true</xsl:when>
				<xsl:when test=". = 'workLocation.state'">true</xsl:when>
				<xsl:when test=". = 'workLocation.postalCode'">true</xsl:when>
				<xsl:when test=". = 'countryDisplayValue'">true</xsl:when>
				<xsl:when test=". = 'departmentTitle'">true</xsl:when>
				<xsl:when test=". = 'organizationTitle'">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>			
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="codeValue">
			<xsl:choose>
				<xsl:when test="$isCode = 'true'"><xsl:value-of select="."/></xsl:when>
				<xsl:otherwise>ignoreMe</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="ref">
   			<xsl:choose>
				<xsl:when test="$isExtensionAttribute"><xsl:value-of select="@extensionIdRef"/></xsl:when>
				<xsl:when test=". = 'secretaryName'">secretaryUid</xsl:when>
				<xsl:when test=". = 'secretaryEmail'">secretaryUid</xsl:when>
				<xsl:when test=". = 'secretaryKey'">secretaryUid</xsl:when>
				<xsl:when test=". = 'employeeTypeDesc'">employeeTypeCode</xsl:when>
				<xsl:when test=". = 'workLocation.address1'">workLocationCode</xsl:when>
				<xsl:when test=". = 'workLocation.address2'">workLocationCode</xsl:when>
				<xsl:when test=". = 'workLocation.city'">workLocationCode</xsl:when>
				<xsl:when test=". = 'workLocation.state'">workLocationCode</xsl:when>
				<xsl:when test=". = 'workLocation.postalCode'">workLocationCode</xsl:when>
				<xsl:when test=". = 'countryDisplayValue'">countryCode</xsl:when>
				<xsl:when test=". = 'departmentTitle'">deptNumber</xsl:when>
				<xsl:when test=". = 'organizationTitle'">orgId</xsl:when>
				<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>    			
   			</xsl:choose>    			
		</xsl:variable>
		<xsl:variable name="isDuplicate">
			<xsl:choose>
				<xsl:when test="$isCode = 'true'">
					<xsl:choose>
						<xsl:when test="$codeValue = 'secretaryName' or $codeValue = 'secretaryEmail' or $codeValue='secretaryKey'">
							<xsl:value-of select="boolean($duplicates[.='secretaryUid']) or boolean($duplicates[.='secretaryName']) or boolean($duplicates[.='secretaryEmail']) or boolean($duplicates[.='secretaryKey'])"/>
						</xsl:when>
						<xsl:when test="$codeValue = 'employeeTypeDesc'">
							<xsl:value-of select=" boolean($duplicates[.='employeeTypeCode']) or boolean($duplicates[.='employeeTypeDesc'])"/>
						</xsl:when>
						<xsl:when test="$codeValue = 'workLocation.address1' or $codeValue = 'workLocation.address2' or $codeValue = 'workLocation.city' or $codeValue = 'workLocation.state' or $codeValue = 'workLocation.postalCode'">
							<xsl:value-of select=" boolean($duplicates[.='workLocationCode']) or boolean($duplicates[.='workLocation.address1']) or boolean($duplicates[.='workLocation.address2']) or boolean($duplicates[.='workLocation.city']) or boolean($duplicates[.='workLocation.state']) or boolean($duplicates[.='workLocation.postalCode'])"/>
						</xsl:when>
						<xsl:when test="$codeValue = 'countryDisplayValue'">
							<xsl:value-of select=" boolean($duplicates[.='countryCode']) or boolean($duplicates[.='countryDisplayValue'])"/>
						</xsl:when>
						<xsl:when test="$codeValue = 'departmentTitle'">
							<xsl:value-of select=" boolean($duplicates[.='deptNumber']) or boolean($duplicates[.='departmentTitle'])"/>
						</xsl:when>
						<xsl:when test="$codeValue = 'organizationTitle'">
							<xsl:value-of select=" boolean($duplicates[.='orgId']) or boolean($duplicates[.='organizationTitle'])"/>
						</xsl:when>
						<xsl:otherwise>false</xsl:otherwise>
					</xsl:choose>
				</xsl:when>				
				<xsl:otherwise>
					<xsl:value-of select="boolean($duplicates[.=$ref])"/>
				</xsl:otherwise>
			</xsl:choose>				
		</xsl:variable>
		<xsl:variable name="updatability">
			<xsl:choose>
				<xsl:when test="$isReadwrite">
					<xsl:text>readwrite</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>read</xsl:text>
				</xsl:otherwise>
			</xsl:choose>				
		</xsl:variable>
		<xsl:variable name="isValidAttribute" select="(contains($standardAttributes, $ref) or ($isExtensionAttribute and boolean($extensionAttributes[@extensionId=$ref])))"/>							
			<xsl:if test="$isDuplicate != 'true' and $isValidAttribute">
	<property xmlns="http://www.ibm.com/profiles-types">
		<ref><xsl:value-of select="$ref"/></ref>
		<updatability><xsl:value-of select="$updatability"/></updatability>
		<hidden>false</hidden>
			<xsl:if test="$isRichText='true'">
		<richText>true</richText>
			</xsl:if>										
		</property>					
		</xsl:if>		
	</xsl:template>
	
	<xsl:template name="migrateLayoutProfileType">
		<xsl:param name="layoutProfileType"/>
		<xsl:variable name="profileTypeId" select="@profileType"/>
		<xsl:variable name="apiProfileType" select="$apiProfileTypes[@profileType=$profileTypeId]"/>
		<xsl:variable name="hasApiProfileType" select="boolean($apiProfileType)"/>
		<xsl:if test="$hasApiProfileType != 'true'">
			<type xmlns="http://www.ibm.com/profiles-types">
				<parentId>snx:person</parentId>
				<id><xsl:value-of select="$profileTypeId"/></id>
				<xsl:call-template name="migrateLayout">
					<xsl:with-param name="layoutProfileType" select="$layoutProfileType"/>
				</xsl:call-template>
			</type>		
		</xsl:if>					
	</xsl:template>
		
	<xsl:template match="/">
		<xsl:call-template name="copyrightText"/>		
		<config id="profiles-types" xmlns="http://www.ibm.com/profiles-types"
			xmlns:pt="http://www.ibm.com/profiles-types" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://www.ibm.com/profiles-types profiles-types.xsd">
			<xsl:for-each select="$apiProfileTypes">
				<xsl:variable name="index" select="position()"/>
				<xsl:call-template name="migrateApiProfileType">
					<xsl:with-param name="apiProfileType" select="$apiProfileTypes[$index]"/>
				</xsl:call-template>
			</xsl:for-each>
			<xsl:for-each select="$layoutProfileTypes">				
				<xsl:variable name="index" select="position()"/>
				<xsl:call-template name="migrateLayoutProfileType">
					<xsl:with-param name="layoutProfileType" select="$layoutProfileTypes[$index]"/>
				</xsl:call-template>				
			</xsl:for-each>
		</config>
	</xsl:template>

    <xsl:template name="copyrightText">
		<xsl:comment>Copyright IBM Corp. 2011  All Rights Reserved.</xsl:comment>    
    </xsl:template>    

</xsl:stylesheet>