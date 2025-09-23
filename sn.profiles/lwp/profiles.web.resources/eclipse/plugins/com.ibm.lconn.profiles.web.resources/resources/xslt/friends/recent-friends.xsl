<?xml version="1.0" encoding="UTF-8"?>
<!--
/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2015                                    */
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
  xmlns:atom="http://www.w3.org/2005/Atom"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="snx xsl html atom">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>
  
  <xsl:param name="loggedIn"/>
  <xsl:param name="loggedInUserUid"/>
  <xsl:param name="loggedInUserKey" />
  <xsl:param name="displayedUserKey" />
  
  <xsl:variable name="isCurrentUser" select="$loggedIn = 'true' and $loggedInUserKey = $displayedUserKey" />
  
  <xsl:param name="friendsViewAllFriends" select="'See All'"/>
  <xsl:param name="friendsNoFriends" select="'No network contacts are associated with this profile'"/>
  <xsl:param name="friendsNewInv"/>
  <xsl:param name="friendsNewInvs"/>
    
  <xsl:template match="/">
    <div id="recent-friends-container">   
      <xsl:if test="atom:feed/@snx:new-invitations-count > 0 and $isCurrentUser">
        <div class="lotusChunk">
          <p><b>
            <a href="javascript:void(0);" onclick="lconn.profiles.Friending.viewAllInvitations()">
              <xsl:if test="atom:feed/@snx:new-invitations-count = 1">
                  <xsl:call-template name="replacePlaceHolders">
                    <xsl:with-param name="inputString" select="$friendsNewInv" />
                    <xsl:with-param name="value" select="atom:feed/@snx:new-invitations-count" />
                  </xsl:call-template>
              </xsl:if>
              <xsl:if test="not( atom:feed/@snx:new-invitations-count = 1 )">
                  <xsl:call-template name="replacePlaceHolders">
                    <xsl:with-param name="inputString" select="$friendsNewInvs" />
                    <xsl:with-param name="value" select="atom:feed/@snx:new-invitations-count" />
                  </xsl:call-template>
              </xsl:if>
            </a>
           </b></p>
        </div>
      </xsl:if>

      
        <div class="lotusChunk" role="list">
          <xsl:choose>
            <xsl:when test="count(atom:feed/atom:entry) > 0">
              <xsl:for-each select="atom:feed/atom:entry">

				<div class="lotusLeft lotusNetworkPerson" role="listitem">
				<span class="vcard">
					<a href="{atom:link/@href}" class="fn lotusPerson hasHover url" title="{atom:title/text()}" aria-label="{atom:title/text()}">						
							<img class="usersRadius" height="32" width="32" alt="{atom:title/text()}" src="{@snx:imageUrl}" tabindex="-1"/>
							<span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@snx:userid"/></span>
							<span class="lotusAltText"><xsl:value-of select="atom:title/text()"/></span>
					</a>
				</span>   
				</div>

              </xsl:for-each>
            </xsl:when>

            <xsl:otherwise>
              <div class="lotusChunk" role="listitem" tabindex="-1"> 
                <xsl:value-of select="$friendsNoFriends"/>
             </div>
            </xsl:otherwise>

          </xsl:choose>            
        </div>
        <div class="lotusClear"></div>
      <!-- 
      <xsl:if test="$displayedUserKey != $displayedUserKey or atom:feed/@snx:logged-in = 'false'">
        <div class="lotusChunk tiny">
          <p><a class="action" href="javascript:void(0);" onclick="lconn.profiles.Friending.viewSendRequest('{$displayedUserKey}')"><xsl:value-of select="$rs/@send_friend_request"/></a></p>
        </div>
      </xsl:if>
       -->
    </div><!--end section-->


      <div class="lotusChunk" style="padding-top:5px;">
        <p>
          <a class="lotusAction" href="javascript:void(0);" id="recent-friends-viewall">
            <xsl:attribute name="onclick">
            	lconn.profiles.Friending.viewAllColleagues('<xsl:value-of select="$displayedUserKey"/>');
            </xsl:attribute>
            <xsl:call-template name="replacePlaceHolders">
              <xsl:with-param name="inputString" select="$friendsViewAllFriends" />
              <xsl:with-param name="value" select="atom:feed/@snx:total-friends" />
            </xsl:call-template>
          </a>
        </p>
     </div>

  </xsl:template>
  
  
  <!-- copied from replace.xsl. DO NOT EDIT THE CODE BELOW -->
  <xsl:template name="replacePlaceHolders">
    <xsl:param name="inputString" />
    <xsl:param name="value" />
    <xsl:call-template name="replace">
      <xsl:with-param name="inputString" select="$inputString" />
      <xsl:with-param name="searchFor" select="'{0}'" />
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
