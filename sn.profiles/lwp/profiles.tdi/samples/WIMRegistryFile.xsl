<?xml version="1.0" ?>
<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2012, 2013                                    -->
<!--                                                                   -->
<!-- The source code for this program is not published or otherwise    -->
<!-- divested of its trade secrets, irrespective of what has been      -->
<!-- deposited with the U.S. Copyright Office.                         -->
<!--                                                                   -->
<!-- ***************************************************************** -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sdo="commonj.sdo" xmlns:wim="http://www.ibm.com/websphere/wim" xmlns:ic="http://www.ibm.com/ibm/connections"	 version="1.0">

<xsl:output method="xml" indent="yes" />
	<xsl:template match="sdo:datagraph/wim:Root">
		<DocRoot>
			<xsl:for-each select="wim:entities">
				<Entry>
					<xsl:if test="wim:identifier/@externalName">
						<Attribute name="$dn">
							<Value>
								<xsl:value-of select="wim:identifier/@externalName" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:manager/@externalName">
						<Attribute name="manager">
							<Value>
								<xsl:value-of select="wim:manager/@externalName" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:secretary/@externalName">
						<Attribute name="secretary">
							<Value>
								<xsl:value-of select="wim:secretary/@externalName" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:identifier/@externalName">
						<Attribute name="externalName">
							<Value>
								<xsl:value-of select="wim:identifier/@externalName" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:identifier/@externalId">
						<Attribute name="externalId">
							<Value>
								<xsl:value-of select="wim:identifier/@externalId" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:identifier/@externalId">
						<Attribute name="ibm-entryUuid">
							<Value>
								<xsl:value-of select="wim:identifier/@externalId" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:identifier/@uniqueId">
						<Attribute name="uniqueId">
							<Value>
								<xsl:value-of select="wim:identifier/@uniqueId" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:identifier/@uniqueName">
						<Attribute name="$dn">
							<Value>
								<xsl:value-of select="wim:identifier/@uniqueName" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="ic:ibm-saasOrgID">
						<Attribute name="tenantId">
							<Value>
								<xsl:value-of select="ic:ibm-saasOrgID" /> 
							</Value>
						</Attribute>
					</xsl:if>


					<xsl:if test="wim:uid">
						<Attribute name="uid">
							<Value>
								<xsl:value-of select="wim:uid" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:cn">
						<Attribute name="cn">
							<Value>
								<xsl:value-of select="wim:cn" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:sn">
						<Attribute name="sn">
							<Value>
								<xsl:value-of select="wim:sn" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:givenName">
						<Attribute name="givenName">
							<Value>
								<xsl:value-of select="wim:givenName" />
							</Value> 
					</Attribute>
					</xsl:if>

					<xsl:if test="wim:initials">
						<Attribute name="initials">
							<Value>
								<xsl:value-of select="wim:initials" />
							</Value> 
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:mail">
						<Attribute name="mail">
							<Value>
								<xsl:value-of select="wim:mail" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:displayName">
						<Attribute name="displayName">
							<Value>
								<xsl:value-of select="wim:displayName" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:preferredName">
						<Attribute name="preferredName">
							<Value>
								<xsl:value-of select="wim:preferredName" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:labeledURI">
						<Attribute name="labeledURI">
							<Value>
								<xsl:value-of select="wim:labeledURI" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:carLicense">
						<Attribute name="carLicense">
							<Value>
								<xsl:value-of select="wim:carLicense" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:telephoneNumber">
						<Attribute name="telephoneNumber">
							<Value>
								<xsl:value-of select="wim:telephoneNumber" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:facsimileTelephoneNumber">
						<Attribute name="facsimileTelephoneNumber">
							<Value>
								<xsl:value-of select="wim:facsimileTelephoneNumber" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:pager">
						<Attribute name="pager">
							<Value>
								<xsl:value-of select="wim:pager" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:mobile">
						<Attribute name="mobile">
							<Value>
								<xsl:value-of select="wim:mobile" /> 
								</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:homePostalAddress">
						<Attribute name="homePostalAddress">
							<Value>
								<xsl:value-of select="wim:homePostalAddress" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:postalAddress">
						<Attribute name="postalAddress">
							<Value>
								<xsl:value-of select="wim:postalAddress" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:roomNumber">
						<Attribute name="roomNumber">
							<Value>
								<xsl:value-of select="wim:roomNumber" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:l">
						<Attribute name="l">
							<Value>
								<xsl:value-of select="wim:l" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:localityName">
						<Attribute name="localityName">
							<Value>
								<xsl:value-of select="wim:localityName" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:st">
						<Attribute name="st">
							<Value>
								<xsl:value-of select="wim:st" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:stateOrProvinceName">
						<Attribute name="stateOrProvinceName">
							<Value>
								<xsl:value-of select="wim:stateOrProvinceName" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:street">
						<Attribute name="street">
							<Value>
								<xsl:value-of select="wim:street" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:postalCode">						
	 					<Attribute name="postalCode">
							<Value>
								<xsl:value-of select="wim:postalCode" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:employeeNumber">
						<Attribute name="employeeNumber">
							<Value>
								<xsl:value-of select="wim:employeeNumber" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:employeeType">
						<Attribute name="employeeType">
							<Value>
								<xsl:value-of select="wim:employeeType" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:businessCategory">
						<Attribute name="businessCategory">
							<Value>
								<xsl:value-of select="wim:businessCategory" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:departmentNumber">
						<Attribute name="departmentNumber">
							<Value>
								<xsl:value-of select="wim:departmentNumber" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:seeAlso">
						<Attribute name="seeAlso">
							<Value>
								<xsl:value-of select="wim:seeAlso" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:title">
						<Attribute name="title">
							<Value>
								<xsl:value-of select="wim:title" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:c">
						<Attribute name="c">
							<Value>
								<xsl:value-of select="wim:c" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:countryName">
						<Attribute name="countryName">
							<Value>
								<xsl:value-of select="wim:countryName" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:o">
						<Attribute name="o">
							<Value>
								<xsl:value-of select="wim:o" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:ou">
						<Attribute name="ou">
							<Value>
								<xsl:value-of select="wim:ou" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:dc">
						<Attribute name="dc">
							<Value>
								<xsl:value-of select="wim:dc" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:principalName">
						<Attribute name="principalname">
							<Value>
								<xsl:value-of select="wim:principalName" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:realm">
						<Attribute name="realm">
							<Value>
								<xsl:value-of select="wim:realm" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:city">
						<Attribute name="city">
							<Value>
								<xsl:value-of select="wim:city" /> 
							</Value>
						</Attribute>
					</xsl:if>

					<xsl:if test="wim:description">
						<Attribute name="description">
							<Value>
								<xsl:value-of select="wim:description" /> 
							</Value>
						</Attribute>
					</xsl:if>



				</Entry>
			</xsl:for-each>
		</DocRoot>
	</xsl:template>
</xsl:stylesheet>
