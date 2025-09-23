<?xml version="1.0" encoding="UTF-8"?>
<!--
/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2001, 2022                     */
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
  xmlns:app="http://www.w3.org/2007/app"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:snx="http://www.ibm.com/xmlns/prod/sn"
  exclude-result-prefixes="xsl atom app snx">
  <xsl:output method="html" omit-xml-declaration="yes" indent="no" />
  
  <xsl:param name="applicationContext" select="'/profiles'"/>

  <xsl:param name="blankImg"/>
  <xsl:param name="loggedIn" />
  <xsl:param name="loggedInUserUid"/>
  <xsl:param name="loggedInUserKey" />
  <xsl:param name="isNewUI" />
  <xsl:param name="displayedUserKey" />
  <xsl:param name="defaultView" />
  
  <xsl:variable name="isCurrentUser" select="$loggedIn = 'true' and $loggedInUserKey = $displayedUserKey" />
  <xsl:variable name="canAddTag" select="/app:categories/@snx:canAddTag = 'true'"/>
  <xsl:variable name="tagOthersEnabled" select="/app:categories/@snx:tagOthersEnabled = 'true'"/>
  <xsl:variable name="numOfContributors" select="/app:categories/@snx:numberOfContributors" />  
  <xsl:variable name="newUI" select="$isNewUI = 'true'" />  
    
  <xsl:param name="socialTagsAddTags" select="'[Add profile tags]'"/>
  <xsl:param name="socialTagsNoTags" select="'No tags'"/>
  <xsl:param name="socialTagsNoTagsCantAdd" select="'No tags'"/>
  <xsl:param name="socialTagsYouTagged" select="'My tags for this profile'"/>
  <xsl:param name="socialTagsViewAs" select="'View as '"/>
  <xsl:param name="socialTagsList" select="'List'"/>
  <xsl:param name="socialTagsTagCloud" select="'Cloud'"/>
  <xsl:param name="socialTagsAddTagsAltText" select="'Add tag(s) to this profile'"/>
  <xsl:param name="socialTagsRemoveTagsAltText" select="'Remove tag {0} from this profile'"/>
  <xsl:param name="socialTagsListAltText" select="'List tags as a sequential list of tags'"/>
  <xsl:param name="socialTagsTagCloudAltText" select="'List tags as a tag cloud'"/>
  <xsl:param name="socialTagsTaggedBy" select="'Tagged by '"/>
  <xsl:param name="socialTagsPeople" select="' people '"/>
  <xsl:param name="socialTagsPerson" select="' person '"/>
  <xsl:param name="socialTagsAddedBy" select="'Tag added by '"/>
  <xsl:param name="socialTagsWhoTagged" select="'Tag {0} was tagged by 1 person.  See who added this tag '"/>
  <xsl:param name="socialTagsWhoTaggedMulti" select="'Tag {0} was tagged {1} people.  See who added this tag '"/>

	<xsl:template match="/">
		<div id="social_tags" aria-live="polite">
			<!--
				any logged in user can add tags to a profile. only owners of the
				profile can remove a tag from their profile
			-->
			<xsl:if test="$loggedIn = 'true'">
				<xsl:if test="$canAddTag">
					<div id="add-tag-view" class="lotusChunk lotusSearch">
						<form id="tagInputForm"
							onsubmit="lconn.profiles.SocialTags.saveNewTag( '{$displayedUserKey}', this); return false;">
							<span dojoType="lconn.core.TypeAheadDataStore" jsId="tagTypeAheadStore"
								queryParam="tag"
								url="{$applicationContext}/html/tagTypeahead.do?useJson=true">
							</span>
							<input style="width:80%;" dojoType="lconn.profiles.ProfilesTypeAhead" minChars="2" maxChars="255" searchDelay="400" title="{$socialTagsAddTagsAltText}"
              						multipleValues="true" token=" " store="tagTypeAheadStore" hasDownArrow="false" autoComplete="false"
              						name="socialTagName" id="socialTagName" class="tagTypeAhead"> </input>
									<span class="lotusBtnImg" style="padding: 4px 1px;">
	          							<a id="addTagButtonId" role="button" class="lotusAdd" href="javascript:;" title="{$socialTagsAddTagsAltText}"  
	            							 onclick="lconn.profiles.SocialTags.saveNewTag( '{$displayedUserKey}', this); return false;">
											<img alt="{$socialTagsAddTagsAltText}" src="{$blankImg}"/>
											<span class="lotusAltText">&#43;</span>
            							</a>
          					        </span>
						</form>
					</div>
				</xsl:if>
			</xsl:if>
			<xsl:if
				test="$loggedIn = 'true' and $tagOthersEnabled and count(/app:categories/atom:category[@snx:flagged='true']) > 0">
				<div id="tagsYouAddedView">
				    <xsl:if test="not($defaultView = 'list')">
						<xsl:attribute name="style">display: none;</xsl:attribute>
					</xsl:if>
					<div id="tagsYouAddedListHeader_div" class="lotusChunk">
						<h3><xsl:value-of select="$socialTagsYouTagged" /></h3>
					</div>
					<div id="tagsYouAddedList_div">
						<ul class="lotusList lotusEditable lotusTags lotusMeta">
							<xsl:for-each select="/app:categories/atom:category[@snx:flagged='true']">
								<xsl:if test="not(@scheme)">
									<xsl:variable name="noQuote1">
										<xsl:call-template name="valueOfEscape">
											<xsl:with-param name="inputString" select="@term" />
										</xsl:call-template>
									</xsl:variable>
									<xsl:variable name="tagRemoveTitle">
										<xsl:call-template name="replacePlaceHolders">
											<xsl:with-param name="inputString" select="$socialTagsRemoveTagsAltText"/>
											<xsl:with-param name="value" select="@term"/>
										</xsl:call-template>
									</xsl:variable>
									<li class="lotusAlignLeft">
										<span class="lotusRight">
											<xsl:choose>
												<xsl:when test="$canAddTag = 'true'">
													
														<a role="button" href="javascript:void(0);" class="lotusDelete lconnTagCount"
															onclick="profiles_removeTag('{$displayedUserKey}','{$noQuote1}')"
															title="{$tagRemoveTitle}">
															<img width="12" height="12"
																src="{$blankImg}"
																alt="{$tagRemoveTitle}">
															</img>
															<span class="lotusAltText">x</span>
														</a>
												</xsl:when>
												<xsl:otherwise>
													<img width="12" height="12"
														src="{$blankImg}"
														alt="" role="presentation">
													</img>												
												</xsl:otherwise>
											</xsl:choose>
										</span>										
										<a class="profileTag lotusLeft bidiAware" title="{@term}" href="javascript:void(0);" onclick="profiles_searchTag('{$noQuote1}')">
											<xsl:value-of select="@term" />
										</a>
									</li>
								</xsl:if>
							</xsl:for-each>
						</ul>
					</div>
				</div>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="count(/app:categories/atom:category) > 0">
					<div id="tagsList">
					    <xsl:if test="not($defaultView = 'list')">
							<xsl:attribute name="style">display: none;</xsl:attribute>
						</xsl:if>
					    <xsl:if test="$tagOthersEnabled">
							<div id="tagsListHeader_div" class="lotusChunk">
								<h3>
									<xsl:call-template name="replacePlaceHolders">
										<xsl:with-param name="inputString" select="$socialTagsTaggedBy"/>
										<xsl:with-param name="value" select="$numOfContributors"/>
									</xsl:call-template>
	<!-- 
									<xsl:value-of select="$socialTagsTaggedBy" />
									<xsl:text>&#x20;</xsl:text>
									<xsl:value-of select="$numOfContributors" />
	 -->
									<xsl:text>&#x20;</xsl:text>
									<xsl:choose>
										<xsl:when test="$numOfContributors > 1">
											<xsl:value-of select="$socialTagsPeople" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$socialTagsPerson" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>:</xsl:text>
								</h3>
							</div>
					    </xsl:if>
						<div id="tagsListTags_div">
							<ul class="lotusList lotusEditable lotusTags lotusMeta">
								<xsl:for-each select="/app:categories/atom:category">
									<xsl:variable name="noQuote">
										<xsl:call-template name="valueOfEscape">
											<xsl:with-param name="inputString" select="@term" />
										</xsl:call-template>
									</xsl:variable>
									<xsl:if test="not(@scheme)">
										<li class="lotusAlignLeft">
											<xsl:variable name="tagRemoveTitle">
												<xsl:call-template name="replacePlaceHolders">
													<xsl:with-param name="inputString" select="$socialTagsRemoveTagsAltText"/>
													<xsl:with-param name="value" select="@term"/>
												</xsl:call-template>
											</xsl:variable>
											<xsl:choose>
												<xsl:when test="$tagOthersEnabled">
													<span class="lotusRight">
													
														<xsl:variable name="_tagCountTitle">
															<xsl:choose>
																<xsl:when test="@snx:frequency != 1">
																	<xsl:call-template name="replace">
																		<xsl:with-param name="inputString" select="$socialTagsWhoTaggedMulti" />
																		<xsl:with-param name="searchFor" select="'{1}'" />
																		<xsl:with-param name="replaceText" select="@snx:frequency" />
																	</xsl:call-template>																
																</xsl:when>
																<xsl:otherwise>
																	<xsl:value-of select="$socialTagsWhoTagged" />
																</xsl:otherwise>
															</xsl:choose>
														</xsl:variable>
														
														<xsl:variable name="tagCountTitle">
															<xsl:call-template name="replacePlaceHolders">
																<xsl:with-param name="inputString" select="$_tagCountTitle"/>
																<xsl:with-param name="value" select="@term"/>
															</xsl:call-template>														
														</xsl:variable>
														
														<a href="javascript:void(0);" class="lconnTagCount" onclick="toggleDiv('SocialTaggerList{$noQuote}');"
															title="{$tagCountTitle}">
															<xsl:value-of select="@snx:frequency" />
														</a>
														<xsl:choose>
															<xsl:when test="$isCurrentUser and $canAddTag">
																<a role="button" href="javascript:void(0);" class="lotusDelete lconnTagCount"
																	onclick="profiles_deleteInstancesOfTagForSelf('{$displayedUserKey}','{$noQuote}')"
																	title="{$tagRemoveTitle}">
																	<img width="12" height="12"
																		src="{$blankImg}"
																		alt="{$tagRemoveTitle}">
																	</img>
																	<span class="lotusAltText">x</span>
																</a>
															</xsl:when>
															<xsl:otherwise>
																<img width="12" height="12"
																	src="{$blankImg}"
																	alt="" role="presentation">
																</img>															
															</xsl:otherwise>
														</xsl:choose>
													</span>
												</xsl:when>
												<xsl:when test="$canAddTag and $isCurrentUser">
													<span class="lotusRight">
														<a role="button" href="javascript:void(0);" class="lotusDelete lconnTagCount"
															onclick="profiles_removeTag('{$displayedUserKey}','{$noQuote}')"
															title="{$tagRemoveTitle}">
															<img width="12" height="12"
																src="{$blankImg}"
																alt="{$tagRemoveTitle}">
															</img>
															<span class="lotusAltText">x</span>
														</a>
													</span>
												</xsl:when>
												<xsl:otherwise>
													<span class="lotusRight">
														<img width="12" height="12"
															src="{$blankImg}"
															alt="" role="presentation">
														</img>
													</span>
												</xsl:otherwise>												
											</xsl:choose>
											<a class="profileTag lotusLeft bidiAware" href="javascript:void(0);" title="{@term}">
												<xsl:attribute name="onclick">profiles_searchTag('<xsl:call-template
													name="valueOfEscape"><xsl:with-param name="inputString"
													select="@term" /></xsl:call-template>')</xsl:attribute>
												<xsl:if test="@snx:flagged='true'">
													<xsl:attribute name="flagged">true</xsl:attribute>
												</xsl:if>
												<xsl:value-of select="@term" />
											</a>											
											<div class="lotusCommentBubble lotusClear" id="SocialTaggerList{@term}">
												<xsl:attribute name="style">display: none;</xsl:attribute>
												<div class="lotusCommentBubbleBody lotusLeft">
													<div class="lotusMeta lotusTiny">
														<xsl:call-template name="replacePlaceHolders">
															<xsl:with-param name="inputString" select="$socialTagsAddedBy"/>
															<xsl:with-param name="value" select="@snx:frequency"/>
														</xsl:call-template>

<!-- 
														<xsl:value-of select="$socialTagsAddedBy" />
														&#xA0;
														<xsl:value-of select="@snx:frequency" />
-->
 														&#xA0;
														<xsl:choose>
															<xsl:when test="@snx:frequency > 1">
																<xsl:value-of select="$socialTagsPeople" />
															</xsl:when>
															<xsl:otherwise>
																<xsl:value-of select="$socialTagsPerson" />
															</xsl:otherwise>
														</xsl:choose>
													</div>
													<ul class="lotusList lotusEditable lotusTags lotusMeta">
														<xsl:for-each select="atom:contributor">
															<li>
																<a href="javascript:void(0);"
																	onclick="profiles_goToProfile('{@snx:profileKey}');">
																	<xsl:value-of select="atom:name" />
																</a>
															</li>
														</xsl:for-each>
													</ul>
												</div>
											</div>
										</li>
									</xsl:if>
								</xsl:for-each>
							</ul>
						</div>
						<div class="lotusChunk10 lotusTiny">
							<ul class="lotusInlinelist" role="list">
								<li class="lotusFirst" role="listitem">
									<a id="tagCloudActionBt" class="lotusAction"
									     role="button" title="{$socialTagsTagCloudAltText}" href="javascript:void(0);"
									     onclick="profiles_showTagCloud()" >
									     <xsl:value-of select="$socialTagsTagCloud" />
									</a>
 								</li>
								<li role="listitem">
									<span id="tagListActionBt_disabled" tabindex="0" aria-pressed="true" aria-disabled="true"
									    role="button" class="lotusBold">
									    <xsl:value-of select="$socialTagsList"/>
									</span>
								</li>
							</ul>
						</div>
					</div>
					<div id="tagCloud">
					    <xsl:if test="not($defaultView = 'cloud')">
							<xsl:attribute name="style">display: none;</xsl:attribute>
						</xsl:if>
						<div class="lotusTagCloud lotusChunk">
							<ul role="list">
								<xsl:for-each select="/app:categories/atom:category">
									<xsl:if test="not(@scheme)">
										<li style="padding-right: 2px;" role="listitem">
											<xsl:attribute name="class">f<xsl:value-of
												select="position()" />-<xsl:value-of select="@snx:visibilityBin" /></xsl:attribute>
											<a href="javascript:void(0);">
												<xsl:attribute name="onclick">profiles_searchTag('<xsl:call-template
													name="valueOfEscape"><xsl:with-param name="inputString"
													select="@term" /></xsl:call-template>')</xsl:attribute>
												<xsl:attribute name="class">bidiAware lotusF<xsl:value-of
													select="@snx:intensityBin" /></xsl:attribute>
												<xsl:attribute name="title"><xsl:value-of
													select="@snx:frequency" /></xsl:attribute>
												<xsl:value-of select="@term" />
												<xsl:text> </xsl:text>
											</a>
										</li>
									</xsl:if>
								</xsl:for-each>
							</ul>
						</div>
						<div class="lotusChunk10 lotusTiny">							
							<ul class="lotusInlinelist" role="list">
								<li class="lotusFirst" role="listitem">
									<span id="tagCloudActionBt_disabled" tabindex="0" aria-pressed="true" aria-disabled="true"
									   	role="button" class="lotusBold">
										<xsl:value-of select="$socialTagsTagCloud"/>
									</span>
								</li>
								<li role="listitem">
									<a id="tagListActionBt" onclick="profiles_showTagList()" class="lotusAction"
									   role="button" title="{$socialTagsListAltText}" href="javascript:void(0);"><xsl:value-of select="$socialTagsList" />
									</a>
								</li>
							</ul>
						</div>
					</div><!-- End Tag Cloud Div -->
				</xsl:when>
				<xsl:otherwise>
					<div id="tagCloud"></div>
					<div class="lotusChunk">
						<xsl:choose>
							<xsl:when test="$canAddTag">
								<xsl:value-of select="$socialTagsNoTags" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$socialTagsNoTagsCantAdd" />
							</xsl:otherwise>
						</xsl:choose>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>

	<xsl:template name="valueOfEscape">
		<xsl:param name="inputString" />
		
		<xsl:variable name="noQuote_ves1">
			<xsl:call-template name="valueOfBackslash">
				<xsl:with-param name="inputString" select="$inputString" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="noQuote_ves2">
			<xsl:call-template name="valueOfEscapeApos">
				<xsl:with-param name="inputString" select="$noQuote_ves1" />
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:value-of select="$noQuote_ves2" />
	</xsl:template>

	<xsl:template name="valueOfEscapeApos">
		<xsl:param name="inputString" />
		<xsl:param name="searchFor" select='"&apos;"' />
		<xsl:choose>
			<xsl:when test="contains($inputString, $searchFor)">
				<xsl:value-of select="substring-before($inputString,$searchFor)" />
				<xsl:text>\\\'</xsl:text>
				<xsl:call-template name="valueOfEscapeApos">
					<xsl:with-param name="inputString"
						select="substring-after($inputString,$searchFor)" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$inputString" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="valueOfBackslash">
		<xsl:param name="inputString" />
		<xsl:param name="searchFor" select='"\\"' />
		<xsl:choose>
			<xsl:when test="contains($inputString, $searchFor)">
				<xsl:value-of select="substring-before($inputString,$searchFor)" />
				<xsl:text>\\\\</xsl:text>
				<xsl:call-template name="valueOfBackslash">
					<xsl:with-param name="inputString"
						select="substring-after($inputString,$searchFor)" />
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

    <!-- replaces string with variable on the link -->
    <xsl:template name="replacePlaceHoldersOnLink">
        <xsl:param name="inputString" />
        <xsl:param name="title" />
        <xsl:param name="value" />
        
	    <xsl:call-template name="replaceOnLink">
	        <xsl:with-param name="inputString" select="$inputString" />
	        <xsl:with-param name="searchFor" select="'{0}'" />
            <xsl:with-param name="linkTitle" select="$title" />
            <xsl:with-param name="linkValue" select="$value" />
        </xsl:call-template>
    </xsl:template>
	<xsl:template name="replaceOnLink">
	     <xsl:param name="inputString" />
	     <xsl:param name="searchFor" />
	     <xsl:param name="linkTitle" />
	     <xsl:param name="linkValue" />
		 <xsl:choose>
	     <xsl:when test="contains($inputString,$searchFor)">
	            <xsl:value-of select="substring-before($inputString,$searchFor)" />
				<a id="tagListActionBt" class="lotusAction" href="javascript:void(0);" title="{$linkTitle}"
					onclick="profiles_showTagCloud()">
					<xsl:value-of select="$linkValue" />
<!-- 					<xsl:attribute name="title"><xsl:value-of select="$linkTitle"/></xsl:attribute> -->
				</a>
 	            <xsl:call-template name="replaceOnLink">
	                <xsl:with-param name="inputString" select="substring-after($inputString,$searchFor)" />
	                <xsl:with-param name="searchFor" select="$searchFor" />
	                <xsl:with-param name="linkTitle" select="$linkTitle" />
	                <xsl:with-param name="linkValue" select="$linkValue" />
	            </xsl:call-template>
 	         </xsl:when>
	         <xsl:otherwise>
	             <xsl:value-of select="$inputString" />
	         </xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>