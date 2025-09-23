<?xml version="1.0" encoding="UTF-8"?>
<!--
/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
-->
<xsl:stylesheet version="1.0" xmlns="http://www.w3.org/1999/xhtml" xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl html">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  <xsl:param name="friendsViewAllFriends" select="'see all ({0})'"/>
  <xsl:param name="number" select="13"/>
  
  <xsl:template match="/">
    <xsl:value-of select="'first: '"/>
    <xsl:value-of select="translate($friendsViewAllFriends, '{0}', string($number))"/>
    <xsl:value-of select="' second: '"/>    
    <xsl:call-template name="replacePlaceHolders">
      <xsl:with-param name="inputString" select="$friendsViewAllFriends" />
      <xsl:with-param name="value" select="string($number)" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="replacePlaceHolders">
      <xsl:param name="inputString" />
      <xsl:param name="value" />
	  <xsl:param name="searchFor" select="'{0}'" />
      <xsl:call-template name="replace">
          <xsl:with-param name="inputString" select="$inputString" />
          <xsl:with-param name="searchFor" select="$searchFor" />
          <xsl:with-param name="replaceText" select="$value" />
      </xsl:call-template>
  </xsl:template>  
  <xsl:template name="replace">
    <xsl:param name="inputString" />
    <xsl:param name="searchFor" />
    <xsl:param name="replaceText" />
    <xsl:choose>
      <xsl:when test="contains($inputString,$searchFor)">
        <xsl:value-of select="substring-before($inputString,$searchFor)" />
        <xsl:value-of select="$replaceText" />
        <xsl:call-template name="replace">
          <xsl:with-param name="inputString" select="substring-after($inputString,$searchFor)" />
          <xsl:with-param name="searchFor" select="$searchFor" />
          <xsl:with-param name="replaceText" select="$replaceText" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$inputString" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>