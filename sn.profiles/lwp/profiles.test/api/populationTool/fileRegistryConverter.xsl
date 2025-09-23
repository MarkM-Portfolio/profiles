<?xml version="1.0" encoding="UTF-8"?>
<!-- ***************************************************************** -->
<!--                                                                   -->
<!-- IBM Confidential                                                  -->
<!--                                                                   -->
<!-- OCO Source Materials                                              -->
<!--                                                                   -->
<!-- Copyright IBM Corp. 2012                                          -->
<!--                                                                   -->
<!-- The source code for this program is not published or otherwise    -->
<!-- divested of its trade secrets, irrespective of what has been      -->
<!-- deposited with the U.S. Copyright Office.                         -->
<!--                                                                   -->
<!-- ***************************************************************** -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:ic="http://www.ibm.com/ibm/connections" xmlns:sdo="commonj.sdo"
	xmlns:wim="http://www.ibm.com/websphere/wim" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<xsl:output method="xml" />
	<xsl:template match="/">
		<personAccountPool>
			<xsl:for-each
				select="/sdo:datagraph/wim:Root/wim:entities[@xsi:type='wim:PersonAccount']">
				<personAccount>
					<xsl:attribute name="uid">
   					<xsl:value-of select="wim:uid" />
   				</xsl:attribute>
					<xsl:attribute name="cn">
   					<xsl:value-of select="wim:cn" />
   				</xsl:attribute>
					<xsl:attribute name="sn">
   					<xsl:value-of select="wim:sn" />
   				</xsl:attribute>
				</personAccount>
			</xsl:for-each>
		</personAccountPool>
	</xsl:template>
</xsl:stylesheet>
