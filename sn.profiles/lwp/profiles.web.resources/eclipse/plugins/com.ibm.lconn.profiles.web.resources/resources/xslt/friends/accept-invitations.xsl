<?xml version="1.0" encoding="UTF-8"?>
<!--
/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2014                                    */
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
  exclude-result-prefixes="snx xsl">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>
  <xsl:param name="blankImg"/>
  <xsl:param name="etag"/>	
  <xsl:param name="loggedIn" />
  <xsl:param name="loggedInUserUid" />
  <xsl:param name="loggedInUserKey" />
  <xsl:param name="isNewUI" />

  <xsl:variable name="userLoggedIn" select="$loggedIn = 'true'" />
  <xsl:variable name="newUI" select="$isNewUI = 'true'" />
  
  <xsl:param name="friendsFullPageTitle"/>
  <xsl:param name="friendsInvitations"/>
  <xsl:param name="friendsNewInv"/>
  <xsl:param name="friendsNewInvs"/>
  <xsl:param name="friendsNewInvcnx8"/>
  <xsl:param name="friendsNewInvscnx8"/>
  <xsl:param name="friendsInCommonSingle"/>
  <xsl:param name="friendsInCommonMulti"/>
  <xsl:param name="friendsDate"/>
  <xsl:param name="friendsAcceptAction"/>
  <xsl:param name="friendsIgnoreAction"/>
  <xsl:param name="friendsRejectAction"/>
  <xsl:param name="friendsNoInv"/>
  <xsl:param name="friendsLoading"/>  
  <xsl:param name="friendsShowAllCommonFriends"/>
  
  <xsl:template match="/">
    <xsl:choose>
     <xsl:when test="contains(snx:invitations/@ui-level,'second')">
        <div>
        <!-- 
          <div class="lotusTabContainer">
            <ul class="lotusTabs">
              <li id="friendTab">
                <a href="javascript:void(0);" onclick="lconn.profiles.Friending.showColleagues('{$loggedInUserKey}')"><xsl:value-of select="$friendsFullPageTitle" /></a>
              </li>
              <xsl:if test="$userLoggedIn">
                <li id="invitationsTab"  class="lotusSelected">
                  <a href="javascript:void(0);" onclick="lconn.profiles.Friending.showInvitations()"><xsl:value-of select="$friendsInvitations" /></a>
                </li>
              </xsl:if>
            </ul>
          </div>
           -->

          <div id="friendsTabContent" style="display: none" empty="true">
          	<img class="lotusLoading" src="{$blankImg}"></img>
            <xsl:value-of select="$friendsLoading" />
          </div>

          <div id="invitationsTabContent">
              <xsl:call-template name="third-level-content" />
          </div>
        </div>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="third-level-content" />
     </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="third-level-content">
    <xsl:variable name="activeInvites" select="count(snx:invitations/snx:invitation)" />
    <div id="accept-invitations-section" class="lotusChunk">
      <xsl:choose>
        <xsl:when test="$newUI">
      <span id="friends_count">
        <xsl:choose>
         <xsl:when test="$activeInvites=1">
           <h3>
             <xsl:call-template name="replacePlaceHolders">
               <xsl:with-param name="inputString" select="$friendsNewInvcnx8" />
               <xsl:with-param name="value" select="$activeInvites" />
             </xsl:call-template>
           </h3>
         </xsl:when>
         <xsl:when test="$activeInvites &gt; 1">
           <h3>
             <xsl:call-template name="replacePlaceHolders">
               <xsl:with-param name="inputString" select="$friendsNewInvscnx8" />
               <xsl:with-param name="value" select="$activeInvites" />
             </xsl:call-template>
           </h3>
         </xsl:when>
	     <xsl:otherwise>
           <h3>
             <xsl:call-template name="replacePlaceHolders">
               <xsl:with-param name="inputString" select="$friendsNewInvscnx8" />
               <xsl:with-param name="value" select="$activeInvites" />
             </xsl:call-template>
           </h3>
	     </xsl:otherwise>
        </xsl:choose>
      </span>
      <table class="lotusTable cnx8Invitetbl" role="presentation">
        <xsl:choose>
          <xsl:when test="$activeInvites &gt; 0">
            <xsl:for-each select="snx:invitations/snx:invitation">
              <xsl:variable name="pos" select="position()" />	
			  <xsl:variable name="inviterKey" select="@key" />
              <tr>
                <xsl:choose>
                  <xsl:when test="position() = 1">
                    <xsl:attribute name="class">lotusResultRow lotusFirst</xsl:attribute>
                  </xsl:when> 
                  <xsl:when test="position() mod 2 = 0">
                    <xsl:attribute name="class">lotusResultRow lotusAltRow</xsl:attribute>
                  </xsl:when> 
                  <xsl:otherwise>
                    <xsl:attribute name="class">lotusResultRow</xsl:attribute>
                  </xsl:otherwise>     
                </xsl:choose> 
                <td class="lotusFirst">
                 <div>                        
                   <xsl:if test="@isActive = 'false'">
                     <xsl:attribute name="class">lotusDim</xsl:attribute>
                   </xsl:if>
                   <img src="{$applicationContext}/photo.do?key={@key}&amp;lastMod={@lastMod}" width="45" length="45" alt="{@inviter-name}" />
                 </div>
                </td>
                <td style="width: 100%">
                 <div>                        
                   <xsl:if test="@isActive = 'false'">
                     <xsl:attribute name="class">lotusDim</xsl:attribute>
                   </xsl:if>
                  <h4>
                      <span class="vcard">
                        <a href="{$applicationContext}/html/profileView.do?key={@key}" class="fn lotusPerson bidiAware"><xsl:value-of select="@inviter-name" /></a>
                        <span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@userid"/></span>
                      </span>
                  </h4>

                  <xsl:if test="not(@msg = '')">
                    <div class="bidiAware">
                      <xsl:call-template name="valueOfUnescapeXML">
                        <xsl:with-param name="inputString" select="@msg"/>
                      </xsl:call-template>
                    </div>
                  </xsl:if>
          
                  <xsl:if test="@common-friends=1">
                    <div style="padding-top:10px;">
                        <xsl:call-template name="replacePlaceHolders">
                          <xsl:with-param name="inputString" select="$friendsInCommonSingle" />
                          <xsl:with-param name="value" select="@common-friends" />
                        </xsl:call-template>
                    </div>
                  </xsl:if>
                  
                  <xsl:if test="not(@common-friends=1)">
                    <div style="padding-top:10px;">
                        <xsl:call-template name="replacePlaceHolders">
                          <xsl:with-param name="inputString" select="$friendsInCommonMulti" />
                          <xsl:with-param name="value" select="@common-friends" />
                        </xsl:call-template>
                    </div>
                  </xsl:if>

				  <xsl:if test="not(@common-friends=0)">
					<div id="friendsInCommon_div_{$inviterKey}" class="">
						<xsl:for-each select="snx:common-friend">
		                  	<xsl:choose>
								<xsl:when test="position() &lt; 10">
									<div class="lotusLeft lotusNetworkPerson" >
										<span class="vcard">
											<a href="{$applicationContext}/html/profileView.do?key={@key}">
												<img src="{$applicationContext}/photo.do?key={@key}&amp;lastMod={@lastMod}" width="32" length="32" alt="{@displayName}" title="{@displayName}" class="fn lotusPerson" />
												<span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@userid" /></span>
											</a>
										</span>
									</div>
								</xsl:when>
								<xsl:when test="position() &gt;= 10">
									<div class="lotusLeft lotusNetworkPerson lotusHidden" >
										<span class="vcard">
											<a href="{$applicationContext}/html/profileView.do?key={@key}">
												<img src="{$applicationContext}/photo.do?key={@key}&amp;lastMod={@lastMod}" width="32" length="32" alt="{@displayName}" title="{@displayName}"  class="fn lotusPerson" />
												<span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@userid" /></span>
											</a>
										</span>
									</div>
								</xsl:when>
							</xsl:choose>
							<xsl:if test="position() = 10">
								<div id="frindsInCommonShowAll_div_{$inviterKey}" class="lotusLeft lotusNetworkPerson">
									<a href="javascript:;" title="{$friendsShowAllCommonFriends}" onclick="lconn.profiles.Friending.showAllCommonFriends('{$inviterKey}');">
										<br/><xsl:value-of select="$friendsShowAllCommonFriends" />
									</a>
								</div>
							</xsl:if>
						</xsl:for-each>
					</div>
				  </xsl:if>
                  <!--
                  <div class="lotusMeta" dataType="LCDate"><xsl:value-of select="$friendsDate" /><xsl:value-of select="@date" /></div>
                   -->
                  </div>
						<div style="padding-top:10px;">
		                   <xsl:if test="@isActive = 'false'">
		                     <xsl:attribute name="class">lotusDim</xsl:attribute>
		                   </xsl:if>
							<ul class="lotusInlinelist lotusLinks" role="list">
								<li class="lotusFirst" role="listitem">
									<a id="accept_link_{$pos}" href="javascript:;" aria-label="{$friendsAcceptAction} {@inviter-name}" title="{$friendsAcceptAction} {@inviter-name}" onclick="lconn.profiles.Friending.acceptFriendRequest('{@connectionId}', '{$loggedInUserKey}') "><xsl:value-of select="$friendsAcceptAction" /></a>
								</li>
								<li role="listitem">
									<a id="reject_link_{$pos}" href="javascript:;" aria-label="{$friendsIgnoreAction} {@inviter-name}" title="{$friendsIgnoreAction} {@inviter-name}" onclick="lconn.profiles.Friending.rejectFriendRequest('{@connectionId}', '{$loggedInUserKey}')"><xsl:value-of select="$friendsRejectAction" /></a>
								</li>
							</ul>
						</div><!-- End btnContainer -->
						<img src="{$blankImg}" width='650' height="0" alt=""/>
					</td>
				</tr>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <!-- <tr><td><xsl:value-of select="$friendsNoInv" /> <img src="{$blankImg}" width='650' height="0"/></td></tr> -->
          </xsl:otherwise>
        </xsl:choose>
      </table>
        </xsl:when>
        <xsl:otherwise>
        <span id="friends_count">
        <xsl:choose>
         <xsl:when test="$activeInvites=1">
           <h3>
             <xsl:call-template name="replacePlaceHolders">
               <xsl:with-param name="inputString" select="$friendsNewInv" />
               <xsl:with-param name="value" select="$activeInvites" />
             </xsl:call-template>
           </h3>
         </xsl:when>
         <xsl:when test="$activeInvites &gt; 1">
           <h3>
             <xsl:call-template name="replacePlaceHolders">
               <xsl:with-param name="inputString" select="$friendsNewInvs" />
               <xsl:with-param name="value" select="$activeInvites" />
             </xsl:call-template>
           </h3>
         </xsl:when>
	     <xsl:otherwise>
           <p>
             <xsl:call-template name="replacePlaceHolders">
               <xsl:with-param name="inputString" select="$friendsNewInvs" />
               <xsl:with-param name="value" select="$activeInvites" />
             </xsl:call-template>
           </p>
	     </xsl:otherwise>
        </xsl:choose>
      </span>
      <table class="lotusTable" role="presentation">
        <xsl:choose>
          <xsl:when test="$activeInvites &gt; 0">
            <xsl:for-each select="snx:invitations/snx:invitation">
              <xsl:variable name="pos" select="position()" />	
			  <xsl:variable name="inviterKey" select="@key" />
              <tr>
                <xsl:choose>
                  <xsl:when test="position() = 1">
                    <xsl:attribute name="class">lotusResultRow lotusFirst</xsl:attribute>
                  </xsl:when> 
                  <xsl:when test="position() mod 2 = 0">
                    <xsl:attribute name="class">lotusResultRow lotusAltRow</xsl:attribute>
                  </xsl:when> 
                  <xsl:otherwise>
                    <xsl:attribute name="class">lotusResultRow</xsl:attribute>
                  </xsl:otherwise>     
                </xsl:choose> 
                <td class="lotusFirst">
                 <div>                        
                   <xsl:if test="@isActive = 'false'">
                     <xsl:attribute name="class">lotusDim</xsl:attribute>
                   </xsl:if>
                   <img src="{$applicationContext}/photo.do?key={@key}&amp;lastMod={@lastMod}" width="45" length="45" alt="{@inviter-name}" />
                 </div>
                </td>
                <td style="width: 100%">
                 <div>                        
                   <xsl:if test="@isActive = 'false'">
                     <xsl:attribute name="class">lotusDim</xsl:attribute>
                   </xsl:if>
                  <h4>
                      <span class="vcard">
                        <a href="{$applicationContext}/html/profileView.do?key={@key}" class="fn lotusPerson bidiAware"><xsl:value-of select="@inviter-name" /></a>
                        <span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@userid"/></span>
                      </span>
                  </h4>

                  <xsl:if test="not(@msg = '')">
                    <div class="bidiAware">
                      <xsl:call-template name="valueOfUnescapeXML">
                        <xsl:with-param name="inputString" select="@msg"/>
                      </xsl:call-template>
                    </div>
                  </xsl:if>
          
                  <xsl:if test="@common-friends=1">
                    <div style="padding-top:10px;">
                        <xsl:call-template name="replacePlaceHolders">
                          <xsl:with-param name="inputString" select="$friendsInCommonSingle" />
                          <xsl:with-param name="value" select="@common-friends" />
                        </xsl:call-template>
                    </div>
                  </xsl:if>
                  
                  <xsl:if test="not(@common-friends=1)">
                    <div style="padding-top:10px;">
                        <xsl:call-template name="replacePlaceHolders">
                          <xsl:with-param name="inputString" select="$friendsInCommonMulti" />
                          <xsl:with-param name="value" select="@common-friends" />
                        </xsl:call-template>
                    </div>
                  </xsl:if>

				  <xsl:if test="not(@common-friends=0)">
					<div id="friendsInCommon_div_{$inviterKey}" class="">
						<xsl:for-each select="snx:common-friend">
		                  	<xsl:choose>
								<xsl:when test="position() &lt; 10">
									<div class="lotusLeft lotusNetworkPerson" >
										<span class="vcard">
											<a href="{$applicationContext}/html/profileView.do?key={@key}">
												<img src="{$applicationContext}/photo.do?key={@key}&amp;lastMod={@lastMod}" width="32" length="32" alt="{@displayName}" title="{@displayName}" class="fn lotusPerson" />
												<span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@userid" /></span>
											</a>
										</span>
									</div>
								</xsl:when>
								<xsl:when test="position() &gt;= 10">
									<div class="lotusLeft lotusNetworkPerson lotusHidden" >
										<span class="vcard">
											<a href="{$applicationContext}/html/profileView.do?key={@key}">
												<img src="{$applicationContext}/photo.do?key={@key}&amp;lastMod={@lastMod}" width="32" length="32" alt="{@displayName}" title="{@displayName}"  class="fn lotusPerson" />
												<span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@userid" /></span>
											</a>
										</span>
									</div>
								</xsl:when>
							</xsl:choose>
							<xsl:if test="position() = 10">
								<div id="frindsInCommonShowAll_div_{$inviterKey}" class="lotusLeft lotusNetworkPerson">
									<a href="javascript:;" title="{$friendsShowAllCommonFriends}" onclick="lconn.profiles.Friending.showAllCommonFriends('{$inviterKey}');">
										<br/><xsl:value-of select="$friendsShowAllCommonFriends" />
									</a>
								</div>
							</xsl:if>
						</xsl:for-each>
					</div>
				  </xsl:if>
                  <!--
                  <div class="lotusMeta" dataType="LCDate"><xsl:value-of select="$friendsDate" /><xsl:value-of select="@date" /></div>
                   -->
                  </div>
                </td>
               </tr>
               <tr class="lotusDetails">
					<td></td>
					<td>
						<div style="padding-top:10px;">
		                   <xsl:if test="@isActive = 'false'">
		                     <xsl:attribute name="class">lotusDim</xsl:attribute>
		                   </xsl:if>
							<ul class="lotusInlinelist lotusLinks" role="list">
								<li class="lotusFirst" role="listitem">
									<a id="accept_link_{$pos}" href="javascript:;" aria-label="{$friendsAcceptAction} {@inviter-name}" title="{$friendsAcceptAction} {@inviter-name}" onclick="lconn.profiles.Friending.acceptFriendRequest('{@connectionId}', '{$loggedInUserKey}') "><xsl:value-of select="$friendsAcceptAction" /></a>
								</li>
								<li role="listitem">
									<a id="reject_link_{$pos}" href="javascript:;" aria-label="{$friendsIgnoreAction} {@inviter-name}" title="{$friendsIgnoreAction} {@inviter-name}" onclick="lconn.profiles.Friending.rejectFriendRequest('{@connectionId}', '{$loggedInUserKey}')"><xsl:value-of select="$friendsIgnoreAction" /></a>
								</li>
							</ul>
						</div><!-- End btnContainer -->
						<img src="{$blankImg}" width='650' height="0" alt=""/>
					</td>
				</tr>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <!-- <tr><td><xsl:value-of select="$friendsNoInv" /> <img src="{$blankImg}" width='650' height="0"/></td></tr> -->
          </xsl:otherwise>
        </xsl:choose>
      </table>
        </xsl:otherwise>
      </xsl:choose>
      <img src="{$blankImg}" width='650' height="0" alt=""/>
    </div><!--end section-->
  </xsl:template>

  <xsl:template name="valueOfUnescapeXML">
    <xsl:param name="inputString" />
    <xsl:param name="searchFor" select="'&lt;br/&gt;'" />
    <xsl:choose>
      <xsl:when test="contains($inputString, $searchFor)">
        <xsl:value-of select="substring-before($inputString,$searchFor)" />
        <br/>
        <xsl:call-template name="valueOfUnescapeXML">
          <xsl:with-param name="inputString" select="substring-after($inputString,$searchFor)" />
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$inputString" />
      </xsl:otherwise>
    </xsl:choose>    
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
