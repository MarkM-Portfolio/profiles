<?xml version="1.0" encoding="UTF-8"?>
<!--
/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
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
  xmlns:atom="http://www.w3.org/2005/Atom" 
  xmlns:snx="http://www.ibm.com/xmlns/prod/sn"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="snx xsl html atom">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>
    
  <xsl:param name="loggedIn"/>
  <xsl:param name="loggedInUserUid"/>
  <xsl:param name="loggedInUserKey" />
  <xsl:param name="displayedUserKey" />
  <xsl:param name="bidiIsRTL" select="false()"/>
  
  <xsl:param name="numberOfNameToDisplay" select="5" />
  <xsl:param name="containerId" select="'report-chain'" />
  
  <xsl:variable name="isManager"  select="/atom:feed/atom:entry[1]/atom:content/html:div/html:span/html:div[@class='x-is-manager']/text()"/>
  <xsl:variable name="profileKey" select="/atom:feed/atom:entry[1]/atom:content/html:div/html:span/html:div[@class='x-profile-key']/text()"/>
  <xsl:variable name="managerKey" select="/atom:feed/atom:entry[2]/atom:content/html:div/html:span/html:div[@class='x-profile-key']/text()"/>

  <xsl:param name="label_profile_otherviews_reportingstructure"/>
  <xsl:param name="label_profile_otherviews_samemanager"/>
  <xsl:param name="label_profile_otherviews_peoplemanaged"/>
  
  <xsl:template match="/atom:feed">
    <div id="{$containerId}-sub">
      <xsl:variable name="isManager" select="false" />
      <div class="lotusChunk">
	    <xsl:choose>
	      <xsl:when test="system-property('xsl:vendor')='Microsoft'">
			  <xsl:attribute name="style">margin-top: 5px; padding-bottom: 15px; overflow-x: auto; overflow-y: hidden;</xsl:attribute>		  
	      </xsl:when>
	      <xsl:otherwise>
			  <xsl:attribute name="style">margin-top: 5px; overflow-x: auto; overflow-y: hidden;</xsl:attribute>		  
	      </xsl:otherwise>
	    </xsl:choose>
	      <ul class="lotusTree" role="list">
              <xsl:choose>
				<xsl:when test="count(atom:entry) &lt;= $numberOfNameToDisplay">
				  <xsl:call-template name="entry">
				  	<xsl:with-param name="node" select="atom:entry[count(/atom:feed/atom:entry)]" />
				    <xsl:with-param name="relativePosition" select="0" />
				    <xsl:with-param name="nodePosition" select="count(/atom:feed/atom:entry)" />
				  </xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
				  <xsl:call-template name="entry">
				  	<xsl:with-param name="node" select="atom:entry[($numberOfNameToDisplay+1-1)]" /><!-- SPR#MMOE7TKPAX; safari has issues with node params -->
				  	<xsl:with-param name="relativePosition" select="0" />
				    <xsl:with-param name="nodePosition" select="$numberOfNameToDisplay" />
				  </xsl:call-template>
				</xsl:otherwise>
              </xsl:choose>
	      </ul>
      </div>
      <div class="lotusChunk lotusLast">
        <a class="lotusAction" href="{$applicationContext}/html/reportingStructureView.do?key={$profileKey}">
          <xsl:value-of select="$label_profile_otherviews_reportingstructure" />
        </a>
        <xsl:if test="string-length($managerKey) > 0">
          <div class="{$containerId}-sub-sameManager">
            <a class="lotusAction" href="{$applicationContext}/html/reportingStructureView.do?key={$profileKey}&amp;subAction=sameManager&amp;managerKey={$managerKey}&amp;2">
              <xsl:value-of select="$label_profile_otherviews_samemanager" />
            </a>
          </div>
        </xsl:if>
        <xsl:if test="/atom:feed/atom:entry[1]/atom:content/html:div/html:span/html:div[@class='x-is-manager']/text() = 'Y'">
          <div class="{$containerId}-sub-peopleManaged">
            <a class="lotusAction" href="{$applicationContext}/html/reportingStructureView.do?key={$profileKey}&amp;subAction=peopleManaged">
              <xsl:value-of select="$label_profile_otherviews_peoplemanaged" />
            </a>
          </div>
        </xsl:if>
      </div>
    </div>
  </xsl:template>
  
  <xsl:template name="entry">
    <xsl:param name="node"/>
    <xsl:param name="relativePosition"/>
    <xsl:param name="nodePosition"/>
    <xsl:variable name="marginSize" select="$relativePosition*11+2" />
	<!-- for debugging 
	relaPos: <xsl:value-of select="$relativePosition" /><br/>
	nodePos: <xsl:value-of select="$nodePosition" /><br/>
	counter: <xsl:value-of select="count(/atom:feed/atom:entry[$nodePosition - 1 ])" /><br/>    
    name: <xsl:value-of select="$node/atom:contributor/atom:name" /><br/>
    -->
   	<li role="listitem">
        <xsl:attribute name="aria-level"><xsl:value-of select="$relativePosition" /></xsl:attribute>
	    <xsl:choose>
	      <xsl:when test="$nodePosition = 1">
	        <div class="lotusOrgChartBottom lotusBottom bidiAware" style="padding-top: 0px;">
	          <xsl:choose>
	            <xsl:when test="$bidiIsRTL = 'true'">
	              <xsl:attribute name="style">margin-right: <xsl:value-of select="$marginSize" />px; padding-right:20px;</xsl:attribute>
	            </xsl:when>
	            <xsl:otherwise>
	              <xsl:attribute name="style">margin-left: <xsl:value-of select="$marginSize" />px; padding-left:20px;</xsl:attribute>
	            </xsl:otherwise>
	          </xsl:choose>
	         <xsl:value-of select="$node/atom:contributor/atom:name" />
	        </div>
	      </xsl:when>
	      <xsl:when test="$relativePosition = 0">
	        <div class="lotusOrgChartTop">
	          <xsl:choose>
	            <xsl:when test="$bidiIsRTL = 'true'">
	              <xsl:attribute name="style">margin-right:<xsl:value-of select="$marginSize" />px; padding-right:20px;</xsl:attribute>
	            </xsl:when>
	            <xsl:otherwise>
	              <xsl:attribute name="style">margin-left:<xsl:value-of select="$marginSize" />px; padding-left:20px;</xsl:attribute>
	            </xsl:otherwise>
	          </xsl:choose>
	          <xsl:call-template name="renderPerson">
	            <xsl:with-param name="node" select="$node" />
	            <xsl:with-param name="additionalClassName" select="'lotusTop'" />
	          </xsl:call-template>
	        </div>
	      </xsl:when>
	      <xsl:otherwise>
	        <div class="lotusOrgChartMiddle">
	          <xsl:choose>
	            <xsl:when test="$bidiIsRTL = 'true'">
	              <xsl:attribute name="style">margin-right: <xsl:value-of select="$marginSize" />px; padding-right:20px;</xsl:attribute>
	            </xsl:when>
	            <xsl:otherwise>
	              <xsl:attribute name="style">margin-left: <xsl:value-of select="$marginSize" />px; padding-left:20px;</xsl:attribute>
	            </xsl:otherwise>
	          </xsl:choose>
	          <xsl:call-template name="renderPerson">
	            <xsl:with-param name="node" select="$node" />
	          </xsl:call-template>
	        </div>
	      </xsl:otherwise>
	    </xsl:choose>
	 </li>
    <xsl:if test="$nodePosition &gt; 0 and count(/atom:feed/atom:entry[$nodePosition - 1 ]) = 1">
      <xsl:call-template name="entry">
        <xsl:with-param name="node" select="/atom:feed/atom:entry[($nodePosition - 1)]"/>
        <xsl:with-param name="relativePosition" select="$relativePosition + 1"/>
        <xsl:with-param name="nodePosition" select="$nodePosition - 1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="renderPerson">
    <xsl:param name="node"/>
    <xsl:param name="additionalClassName"/>
    <xsl:variable name="contributor" select="$node/atom:contributor" />
    <xsl:variable name="profileUserId" select="$node/atom:content/html:div/html:span/html:div[@class='x-lconn-userid']/text()" />
    
    <div id="profilesReportChain_{$profileUserId}">
		<xsl:choose>
		  <xsl:when test="$contributor/snx:userState = 'inactive'">
		    <xsl:attribute name="class">lotusDim</xsl:attribute>
		  </xsl:when> 
		  <xsl:otherwise>
		    <xsl:attribute name="class"></xsl:attribute>
		  </xsl:otherwise>     
		</xsl:choose>
	    <span class="vcard">
	      <a href="{$node/atom:link[@rel='related' and @type='text/html']/@href}" class="fn lotusPerson {$additionalClassName} bidiAware">
	        <xsl:value-of select="$contributor/atom:name" />
	      </a>
	      <span class="x-lconn-userid" style="display: none;">
	        <xsl:value-of select="$profileUserId"/>
	      </span>
	    </span>
    </div>
  </xsl:template>
  
</xsl:stylesheet>
