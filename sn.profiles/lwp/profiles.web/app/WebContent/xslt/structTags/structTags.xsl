<?xml version="1.0" encoding="UTF-8"?>
<!--
/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
-->
<xsl:stylesheet version="1.0"
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:snx="http://www.ibm.com/xmlns/prod/sn/profiles"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="snx xsl html">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>
  
  <xsl:param name="structTagsNoTags" />
  
  <xsl:param name="loggedIn" />
  <xsl:param name="loggedInUserUid" />
  
  <xsl:variable name="profileUid" select="tags/@current-user-uid"/>
  <xsl:variable name="isCurrentUser" select="$loggedIn= 'true' and $loggedInUserUid = $profileUid" />
  
  <xsl:template match="/">
    <div class="lotusChunk">
      <ul class="lotusList lotusList lotusEditable lotusTags lotusMeta">
        <xsl:choose>
          <xsl:when test="count(tags/target-tags/tag) > 0">
            <xsl:for-each select="tags/target-tags/tag">
              <li>
                <!-- 
                <xsl:attribute name="style">white-space: nowrap;</xsl:attribute>
                 --> 
                <a class="lotusLeft" href="javascript:void(0);"><xsl:value-of select="@term" /></a>
                <xsl:if test="$isCurrentUser"> 
                  <span style="padding:2px"> </span>
                  <a href="javascript:void(0);" onclick="profiles_removeStrucTag('{$profileUid}','{@tag-id}')"><img src="{$applicationContext}/images/remove.gif"> </img></a>
                </xsl:if>
              </li>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise><li class="lotusLeft"><xsl:value-of select="$structTagsNoTags" /></li></xsl:otherwise>
        </xsl:choose>
      </ul>
      <!-- 
      <br/>
      Has tagged other:
      <ul class="lotusList">
        <xsl:choose>
          <xsl:when test="count(tags/source-tags/tag) > 0">
            <xsl:for-each select="tags/source-tags/tag">
              <li>
                <xsl:if test="$isCurrentUser"> 
                    <a href="javascript:void(0);" onclick="profiles_removeLink('{@name}', '{tags/@uid}')">
                      <img src="{$applicationContext}/images/remove.gif"> </img>
                    </a>
                </xsl:if> 
                <span style="padding:2px"> </span>
                <a href="javascript:void(0);" onclick="window.open('{@url}');"><xsl:value-of select="@term" /></a>
              </li>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>No Tags</xsl:otherwise>
        </xsl:choose>
      </ul>
       -->
    </div>
  </xsl:template>
  
</xsl:stylesheet>
