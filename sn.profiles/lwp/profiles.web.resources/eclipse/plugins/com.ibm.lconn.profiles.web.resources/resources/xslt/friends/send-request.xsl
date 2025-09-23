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
<xsl:stylesheet version="1.0" 
  xmlns="http://www.w3.org/1999/xhtml" 
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="xsl html">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>
  
  <xsl:param name="friendsInitialMsgForInv"/>
  <xsl:param name="friendsIncludeMsgForInv"/>
  <xsl:param name="friendsSendInvAction"/>
  <xsl:param name="friendsCancelInvAction"/>
    
  <xsl:template match="/">
    <form id="invitation" class="lotusForm">
        <br/>
          <fieldset>
		  <legend><label for="invitation_text"><xsl:value-of select="$friendsIncludeMsgForInv"/></label></legend><%-- a11y --%>
          <br />
          <textarea cols="40" rows="8" name="invitation_text" id="invitation_text" class="bidiAware"><xsl:if test="/xml-root/@showInviteUI = 'disabled'">
              <xsl:attribute name="disabled">true</xsl:attribute>
            </xsl:if><xsl:value-of select="$friendsInitialMsgForInv"/></textarea>
          <br />
          </fieldset>
        <div class="lotusBtnContainer">
          <input type="button" value="{$friendsSendInvAction}" class="lotusBtn lotusBtnSpecial lotusLeft"
            onclick="lconn.profiles.Friending.sendFriendRequest(this,'{xml-root/@targetKey}', '', 'back')">
            <xsl:if test="/xml-root/@showInviteUI = 'false'">
              <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
          </input>
          <span style="padding-left: 5px;">
            <a href="javascript:void(0);" onclick="profiles_goBack();" class="lotusAction"><xsl:value-of select="$friendsCancelInvAction"/></a>
          </span>
        </div>
    </form>
  </xsl:template>
</xsl:stylesheet>
