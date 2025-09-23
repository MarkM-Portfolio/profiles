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
  xmlns:atom="http://www.w3.org/2005/Atom"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:td="urn:ibm.com/td"
  exclude-result-prefixes="snx xsl atom td html">
  
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>
  
  <xsl:param name="loggedIn" />
  <xsl:param name="loggedInUserUid" />
  <xsl:param name="loggedInUserKey" />
  
  <xsl:param name="containerId" select="'temp'"/>
  <xsl:param name="blankGif" select="''"/>  
  <xsl:param name="alternateUrl" select="''"/>  
  
  <xsl:param name="multiFeedReaderSeeAllFeeds" select="'See all'"/>
  <xsl:param name="multiFeedReaderNoFeeds" select="'No Feeds'"/>
  <xsl:param name="multiFeedReaderUpdatedBy" select="'Updated By'"/>
  <xsl:param name="multiFeedReaderCreatedBy" select="'Created By'"/>
      
  <xsl:template match="/atom:feed">
    <div class="lotusChunk">
      <table id="{$containerId}FeedTableContainer" class="lotusTable" cellspacing="0" role="presentation">
          <!-- <td><xsl:value-of select="atom:author/atom:name/text()" /></td> -->
          <xsl:variable name="hasIcoImages">
            <xsl:if test="count(atom:entry/atom:link[@type='image/x-icon']/@href) > 0">
  	          <xsl:value-of select="true()" />                  
            </xsl:if>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="count(atom:entry) > 0">
              <xsl:for-each select="atom:entry">
                <tr>
                  <xsl:if test="position() = 1">
                    <xsl:attribute name="class">lotusFirst</xsl:attribute>
                  </xsl:if>
				  <xsl:choose>
					  <xsl:when test="$hasIcoImages = 'true'">
						 <td width="20">
						   <xsl:if test="count(atom:link[@type='image/x-icon']/@href) > 0">
							 <img src="{atom:link[@type='image/x-icon']/@href}?logDownload=false" style="max-width: 20px; max-height: 20px;" role="presentation" alt="" />
						   </xsl:if>				   
						</td>
					  </xsl:when>				  
					  <xsl:otherwise>
						 <td width="35">
							<xsl:choose>
								<xsl:when test="atom:link[@rel='thumbnail']/@href != '' and contains(atom:link[@rel='enclosure']/@type, 'image/')">
									<img src="{atom:link[@rel='thumbnail']/@href}" style="max-width: 32px; max-height: 32px;" role="presentation" alt="" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="extension">
										<xsl:call-template name="getExtension">
											<xsl:with-param name="filename" select="atom:title/text()"/>
										</xsl:call-template>
									</xsl:variable>
									<img src="{$blankGif}" class="lconn-ftype32 lconn-ftype32-{$extension}" role="presentation" alt="" />
								</xsl:otherwise>
							</xsl:choose>
						</td>
					  </xsl:otherwise>
				  </xsl:choose>
                  <td>
                    <h4>
                    <xsl:choose>
                      <xsl:when test="count(atom:link[@rel='alternate' and @type='text/html']/@href) > 0">
                        <a href="{atom:link[@rel='alternate' and @type='text/html']/@href}"><xsl:value-of select="atom:title/text()" /></a>
                      </xsl:when>
                      <xsl:otherwise>
                        <a href="{atom:link/@href}"><xsl:value-of select="atom:title/text()" /></a>
                      </xsl:otherwise>                        
                    </xsl:choose>
                    </h4>
                    <div class="lotusMeta" role="list">
                        <xsl:if test="count(atom:author/atom:name) > 0">
                          <xsl:call-template name="renderUserInfo">
                            <xsl:with-param name="stringContent" select="$multiFeedReaderCreatedBy" />
                            <xsl:with-param name="userInfoNode" select="atom:author" />
                          </xsl:call-template>
                          <span class="lotusDivider" aria-hidden="true" role="img">|</span>
                          <span lcNodeType="AtomFeedDate" role="listitem"><xsl:value-of select="atom:published/text()" /></span>
                          <span class="lotusDivider">&#160;</span>
                        </xsl:if>                     
                        <xsl:if test="count(atom:contributor/atom:name) > 0">
                          <xsl:call-template name="renderUserInfo">
                            <xsl:with-param name="stringContent" select="$multiFeedReaderUpdatedBy" />
                            <xsl:with-param name="userInfoNode" select="atom:contributor" />
                          </xsl:call-template>
                          <span class="lotusDivider" aria-hidden="true" role="img">|</span>
                          <span lcNodeType="AtomFeedDate" role="listitem"><xsl:value-of select="atom:updated/text()" /></span>
                          <span class="lotusDivider">&#160;</span>
                        </xsl:if>
                        <xsl:if test="count(td:modifier/atom:name) > 0">
                          <xsl:call-template name="renderUserInfo">
                            <xsl:with-param name="stringContent" select="$multiFeedReaderUpdatedBy" />
                            <xsl:with-param name="userInfoNode" select="td:modifier" />
                          </xsl:call-template>
                          <span class="lotusDivider" aria-hidden="true" role="img">|</span>
                          <span lcNodeType="AtomFeedDate" role="listitem"><xsl:value-of select="atom:updated/text()" /></span>
                        </xsl:if>
                    </div>                    
                  </td>
                </tr>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <tr class="lotusFirst">
                <td><xsl:value-of select="$multiFeedReaderNoFeeds"/></td>
             </tr>
            </xsl:otherwise>
          </xsl:choose>
      </table>
		 <xsl:choose>
			 <xsl:when test="count(atom:link[@rel='alternate']/@href) > 0">
				<a href="{atom:link[@rel='alternate']/@href}" class="lotusAction"><xsl:value-of select="$multiFeedReaderSeeAllFeeds"/></a>
			 </xsl:when>
			 <xsl:when test="$alternateUrl != ''">
				<a href="{$alternateUrl}" class="lotusAction"><xsl:value-of select="$multiFeedReaderSeeAllFeeds"/></a>
			 </xsl:when>                        
		 </xsl:choose>
    </div>
  </xsl:template>
  
  <xsl:template name="renderUserInfo">
    <xsl:param name="stringContent" />
    <xsl:param name="userInfoNode" />
    <span role="listitem"><xsl:value-of select="$stringContent" /></span>
    <span role="listitem">
      <xsl:choose>
        <xsl:when test="count($userInfoNode/snx:userid) > 0">
          <xsl:attribute name="class">vcard</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="count($userInfoNode/atom:email) > 0">
            <xsl:attribute name="class">vcard</xsl:attribute>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>&#160;</xsl:text>       
      <a class="fn lotusPerson">
        <xsl:choose>
          <xsl:when test="count($userInfoNode/atom:uri) > 0">
            <xsl:attribute name="href"><xsl:value-of select="$userInfoNode/atom:uri/text()"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="count($userInfoNode/*[local-name()='userid']/text()) > 0">
            <xsl:variable name="profileLink">profileView.do?userid=<xsl:value-of select="$userInfoNode/*[local-name()='userid']/text()"/></xsl:variable>  
            <xsl:attribute name="href"><xsl:value-of select="$profileLink"/></xsl:attribute>          
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="href">javascript:void(0);</xsl:attribute>
          </xsl:otherwise>                      
        </xsl:choose>
        <xsl:value-of select="$userInfoNode/atom:name/text()"/>
      </a>
      <xsl:choose>
        <xsl:when test="count($userInfoNode/*[local-name()='userid']/text()) > 0">
          <span class="x-lconn-userid" style="display: none;"><xsl:value-of select="$userInfoNode/*[local-name()='userid']/text()"/></span>
        </xsl:when>
        <xsl:otherwise>
          <xsl:if test="count($userInfoNode/atom:email) > 0">
            <span class="email" style="display: none;"><xsl:value-of select="$userInfoNode/atom:email/text()"/></span>
          </xsl:if>
        </xsl:otherwise>
      </xsl:choose>
    </span>  
  </xsl:template>
  
	<xsl:template name="getExtension">
		<xsl:param name="filename"/>

		<xsl:choose>
			<xsl:when test="contains($filename, '.')">
				<xsl:call-template name="getExtension">
					<xsl:with-param name="filename" select="substring-after($filename, '.')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$filename"/>
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