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
  <xsl:param name="blankImg"/>
  <xsl:param name="etag"/>
  <xsl:param name="loggedIn"/>
  <xsl:param name="loggedInUserUid"/>
  <xsl:param name="loggedInUserKey" />
  <xsl:param name="displayedUserKey" />
  
  <xsl:variable name="isCurrentUser" select="$loggedIn = 'true' and $loggedInUserKey = $displayedUserKey" />
  
  <xsl:variable name="total-items-count" select="atom:feed/@snx:total-friends" />
  <xsl:variable name="items-per-page" select="atom:feed/@snx:items-per-page" />
  <xsl:variable name="sortBy" select="atom:feed/@snx:sort-by" />
  <xsl:variable name="current-page" select="atom:feed/@snx:current-page" />
  <xsl:variable name="js-app-prefix" select="'lconn.profiles.Friending'" />
  
  <xsl:param name="friendsNoFriends" select="'No network contacts are associated with this profile'"/>
  <xsl:param name="friendsFullPageTitle"/>
  <xsl:param name="friendsInvitations"/>
  <xsl:param name="friendsLoadingInv"/>
  <xsl:param name="label_inactive_user_msg"/>
  <xsl:param name="showEmail"/>

  <xsl:template match="/">
      <xsl:choose>
         <xsl:when test="contains(atom:feed/@snx:ui-level,'second')">
          <div>
          <!-- 		
            <div class="lotusTabContainer">
              <ul class="lotusTabs lotusHidden">
                <li id="friendTab" class="lotusSelected">
                  <a href="javascript:void(0);" onclick="lconn.profiles.Friending.showColleagues('{$displayedUserKey}')"><xsl:value-of select="$friendsFullPageTitle" /></a>
                </li>
                <xsl:if test="$isCurrentUser">
                  <li id="invitationsTab">
                    <a href="javascript:void(0);" onclick="lconn.profiles.Friending.showInvitations()"><xsl:value-of select="$friendsInvitations" /></a>
                  </li>
                </xsl:if>
              </ul>
            </div>
 		   -->
            <div id="friendsTabContent">
              <form onsubmit="return {$js-app-prefix}.lconn.profiles.Friending.RemoveFriends(this, '{$displayedUserKey}');">
				<input type="submit" style="display:none;"/>			  
                <xsl:call-template name="third-level-content" />
              </form>
            </div>
            <div id="invitationsTabContent" style="display: none" empty="true">
              <img class="lotusLoading" src="{$blankImg}"></img>
              <xsl:value-of select="$friendsLoadingInv" />
            </div>
          </div>
         </xsl:when>
         <xsl:otherwise>
             <xsl:call-template name="third-level-content" />
         </xsl:otherwise>
      </xsl:choose>
  </xsl:template>
  
  <xsl:param name="tablePagingSortBy"/>
  <xsl:param name="friendsSortByDisplayName"/>
  <xsl:param name="friendsSortByRecent"/>
  <xsl:param name="friendsRemovedSelected"/>
  <xsl:param name="friendsTelephone"/>
  <xsl:param name="friendsEmail"/>
  <xsl:param name="friendsAltEmail"/> 

  <xsl:param name="friends_tableheading_selection"/>
  <xsl:param name="friends_tableheading_photo"/>
  <xsl:param name="friends_tableheading_employeeinfo"/>  
  <xsl:param name="friends_tableheading_contactinfo"/>  
  
  <xsl:template name="third-level-content">
    <div id="friendsThirdLevel" class="lotusChunk">
      <xsl:choose>
        <xsl:when test="$total-items-count > 0">
            <xsl:if test="$isCurrentUser">
              <div id="frinds_removeFriendsDiv" class="lotusChunk lotusBtnContainer">
                <span class="lotusBtn lotusBtnAction lotusLeft lotusBtnNetwork">
                  <a href="javascript:void(0);" role="button"
                    onclick="lconn.profiles.Friending.RemoveFriends(this, '{$displayedUserKey}')">
                    <xsl:value-of select="$friendsRemovedSelected"/>
                  </a>
                </span>
                <br/><br/>
              </div>
            </xsl:if>

            <xsl:call-template name="table-paging-header"/>
            <!-- ****************** -->
            <!-- The sort area is not using final styling - will deliver when available -->
            <!-- ****************** -->
            <div id="friends_sortDiv" class="lotusChunk lotusSort">
              <ul class="lotusInlinelist" role="list">
                <li class="lotusFirst" role="listitem"><xsl:value-of select="$tablePagingSortBy"/></li>
                <li class="lotusFirst" role="listitem">
                  <a role="button" onclick="lconn.profiles.Friending.sortFriends(this, 0)" 
                    title="{$tablePagingSortBy} {$friendsSortByDisplayName}" href="javascript:void(0);">
                     <xsl:if test="$sortBy='0'">
                        <xsl:attribute name="class">lotusActiveSort lotusAscending</xsl:attribute>
						<span class="lotusAltText">&#9650;</span>
                     </xsl:if>
                    <xsl:value-of select="$friendsSortByDisplayName"/>
                  </a>
                </li>
                <li role="listitem">
                  <a role="button" onclick="lconn.profiles.Friending.sortFriends(this, 3)" 
                    title="{$tablePagingSortBy} {$friendsSortByRecent}" href="javascript:void(0);" class="lotusActiveSort">
                     <xsl:if test="$sortBy='3'">
                        <xsl:attribute name="class">lotusActiveSort lotusDescending</xsl:attribute>
						<span class="lotusAltText">&#9660;</span>
                     </xsl:if>
                    <xsl:value-of select="$friendsSortByRecent"/>
                  </a>
                </li>
              </ul>
            </div>
            <!-- 
            <div class="lotusSort">
              <xsl:value-of select="$tablePagingSortBy"/>
              <select onchange="lconn.profiles.Friending.sortFriends(this, '{$displayedUserKey}')">
                <xsl:choose>
                   <xsl:when test="$sortBy='0'">
                      <option value="0" selected="selected"><xsl:value-of select="$friendsSortByDisplayName"/></option>
                   </xsl:when>
                   <xsl:otherwise>
                      <option value="0"><xsl:value-of select="$friendsSortByDisplayName"/></option>
                   </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                   <xsl:when test="$sortBy='3'">
                      <option value="3" selected="selected"><xsl:value-of select="$friendsSortByRecent"/></option>
                   </xsl:when>
                   <xsl:otherwise>
                      <option value="3"><xsl:value-of select="$friendsSortByRecent"/></option>
                   </xsl:otherwise>
                </xsl:choose>
              </select>
            </div>
             -->
			 <div id="pageItemsMainRegion" aria-label="" role="region">
            <table id="friends_mainContentTable" class="lotusTable" border="0" cellspacing="0" width="100%">
                <tbody>
					<tr class="lotusHidden"><!-- a11y -->
						<th><xsl:value-of select="$friends_tableheading_selection"/></th>
						<th><xsl:value-of select="$friends_tableheading_photo"/></th>
						<th><xsl:value-of select="$friends_tableheading_employeeinfo"/></th>
						<th><xsl:value-of select="$friends_tableheading_contactinfo"/></th>
					</tr>			
                  <xsl:for-each select="atom:feed/atom:entry">
                    <xsl:variable name="pos" select="position()" />  
                    <tr>  
                      <xsl:choose>
                        <xsl:when test="position() = 1">
                          <xsl:attribute name="class">lotusFirstRow</xsl:attribute>
                        </xsl:when> 
                        <xsl:when test="position() mod 2 = 0">
                          <xsl:attribute name="class">lotusAltRow</xsl:attribute>
                        </xsl:when> 
                        <xsl:otherwise>
                          <xsl:attribute name="class"></xsl:attribute>
                        </xsl:otherwise>     
                      </xsl:choose>
                      <xsl:if test="$isCurrentUser">
                        <td style="width:25px" class="lotusFirstCell">
                          <div>                        
                            <input type="checkbox" value="{atom:id/text()}" id="select_friend_{$pos}" aria-label="{atom:title/text()}"/>
                          </div>
                        </td>
                      </xsl:if>
                      <td style="width:70px">
                        <xsl:choose>
                          <xsl:when test="$isCurrentUser">
                            <xsl:attribute name="class"></xsl:attribute>
                          </xsl:when> 
                          <xsl:otherwise>
                            <xsl:attribute name="class">lotusFirstCell</xsl:attribute>
                          </xsl:otherwise>     
                        </xsl:choose>
                        <div>                        
                          <img src="{@snx:imageUrl}" width="55" height="55" alt="{atom:title/text()}" />
                          <xsl:if test="$sortBy='0'">
                            <span style="padding-right: 5px;">&#160;</span>
                          </xsl:if>
                        </div>
                      </td>
                      <td>
                        <div>                        
                          <div role="article" aria-label="{atom:title/text()}"><span class="vcard">
                            <span class="x-lconn-userid" style="display: none;"><xsl:value-of select="@snx:userid"/></span>
                            
                              <a href="{$applicationContext}/html/profileView.do?key={@snx:key}" class="fn url lotusPerson bidiAware lotusBold">
                                <xsl:value-of select="atom:title/text()"/>
                              </a>
                            
                          </span></div>
                          <div>                             
                              <xsl:value-of select="@snx:title" />                             
                          </div>
                          <xsl:if test="@snx:org != '' and @snx:org != 'null'">
                            <div><xsl:value-of select="@snx:org" /></div>
                          </xsl:if>
                          <xsl:if test="@snx:location != '' and @snx:location != 'null'">
                             <div><xsl:value-of select="@snx:location" /></div>
                          </xsl:if>
						<xsl:if test="@snx:isActive = 'false'">
						  <div><xsl:value-of select="$label_inactive_user_msg" /></div>
						</xsl:if>						  
                        </div>
                      </td>
                      <td class="lotusLastCell">
                        <div>                        
                          <xsl:if test="@snx:tel != ''">
                          <div><xsl:value-of select="$friendsTelephone"/>&#160;<xsl:value-of select="@snx:tel" /></div>
                          </xsl:if>
                          <xsl:if test="$showEmail = 'true' ">
                          	<xsl:if test="@snx:email != ''">
                            <div><xsl:value-of select="$friendsEmail"/>&#160;<strong><a href="mailto:{@snx:email}"><xsl:value-of select="@snx:email" /></a></strong></div>
                            </xsl:if>
                            <xsl:if test="@snx:groupware-mail != ''">
                            <div><xsl:value-of select="$friendsAltEmail"/>&#160;<strong><a href="mailto:{@snx:groupware-mail}"><xsl:value-of select="@snx:groupware-mail" /></a></strong></div>
                            </xsl:if>
                          </xsl:if>
                        </div>
                      </td>
                    </tr>
                 </xsl:for-each>
                </tbody>
              </table>
			  </div>
              <img src="{$blankImg}" width='650' height="0" alt="" role="presentation"/>

            <xsl:call-template name="table-paging-footer"/>
            <span id="friends_count"/>
          </xsl:when>
          <xsl:otherwise>
              <span id="friends_count"><p><xsl:value-of select="$friendsNoFriends" /></p></span>
         </xsl:otherwise>
       </xsl:choose>
    </div>
  </xsl:template>  

  <!-- COPIED CODE FROM ../table-paging.xsl. TABLE-PAGING.XSL IS THE ORIGINAL FILE -->
	<xsl:param name="tablePagingJumpToPage" select="'Jump to page'"/>
	<xsl:param name="tablePagingPage" select="'Page'"/>
	<xsl:param name="tablePagingPageLinkAlt" select="'Go to page {0} of items'"/>
	<xsl:param name="tablePagingItermsPerPage" select="'items per page'"/>
	<xsl:param name="tablePagingShow" select="'Show:'"/>
	<xsl:param name="tablePagingShowLinkAlt" select="'Show {0} items per page'"/>
	<xsl:param name="tablePagingPrevious" select="'Previous'"/>
	<xsl:param name="tablePagingNext" select="'Next'"/>
	<xsl:param name="tablePagingPreviousAlt" select="'Go to the previous page of items'"/>
	<xsl:param name="tablePagingNextAlt" select="'Go to the next page of items  '"/>
	<xsl:param name="tablePagingOf" select="'of'"/>
 
	<xsl:param name="tablePagingLabel" select="'Paging controls.'"/>  
	<xsl:param name="tableBottomPagingLabel" select="'Bottom paging controls.'"/> 
	<xsl:param name="tablePagingInfo" select="'{0} - {1} of {2}'"/>
	<xsl:param name="tablePagingInfoAlt" select="'Showing items {0} through {1} of {2}'"/>
	<xsl:param name="tablePagingJumpToPageControl" select="'Jump to page {0} of {1}'"/>
	<xsl:param name="tablePagingJumpToPageDesc" select="'By changing the value in this control, the page will automatically reload showing the items for that page.'"/>
  

	<xsl:variable name="first-item-number" select="($items-per-page * $current-page + 1)" />
	<xsl:variable name="last-item-number">
		<xsl:choose>         
			<xsl:when test="$total-items-count &lt; (($items-per-page * $current-page) + $items-per-page)">
				<xsl:value-of select="$total-items-count"/>
			</xsl:when>
			<xsl:otherwise>   
				<xsl:value-of select="(($items-per-page * $current-page) + $items-per-page)"/>              
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:variable>
	<xsl:variable name="total-pages" select="ceiling(($total-items-count) div ($items-per-page))" />

	<xsl:variable name="paging-info">
		<xsl:call-template name="replacePlaceHolders">
			<xsl:with-param name="searchFor" select="'{2}'" />
			<xsl:with-param name="value" select="$total-items-count" />
			<xsl:with-param name="inputString">
				<xsl:call-template name="replacePlaceHolders">
					<xsl:with-param name="searchFor" select="'{1}'" />
					<xsl:with-param name="value" select="$last-item-number" />
					<xsl:with-param name="inputString">
						<xsl:call-template name="replacePlaceHolders">
							<xsl:with-param name="inputString" select="$tablePagingInfo" />
							<xsl:with-param name="searchFor" select="'{0}'" />
							<xsl:with-param name="value" select="$first-item-number" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>			
			</xsl:with-param>
		</xsl:call-template>	
	</xsl:variable>
		
	
	<xsl:variable name="paging-info-alt">
		<xsl:call-template name="replacePlaceHolders">
			<xsl:with-param name="searchFor" select="'{2}'" />
			<xsl:with-param name="value" select="$total-items-count" />
			<xsl:with-param name="inputString">
				<xsl:call-template name="replacePlaceHolders">
					<xsl:with-param name="searchFor" select="'{1}'" />
					<xsl:with-param name="value" select="$last-item-number" />
					<xsl:with-param name="inputString">
						<xsl:call-template name="replacePlaceHolders">
							<xsl:with-param name="inputString" select="$tablePagingInfoAlt"/>
							<xsl:with-param name="searchFor" select="'{0}'" />
							<xsl:with-param name="value" select="$first-item-number" />
						</xsl:call-template>
					</xsl:with-param>
				</xsl:call-template>			
			</xsl:with-param>
		</xsl:call-template>
	</xsl:variable>	

	    
	<xsl:template name="table-paging-header">
		<div class="lotusPaging" role="region" aria-label="{$tablePagingLabel} {$paging-info-alt}">
			<div class="lotusLeft" aria-hidden="true">
				<xsl:call-template name="display-items-info"/>
			</div>
			<ul class="lotusRight lotusInlinelist" role="list">
				<xsl:call-template name="next-previous-links"/>
			</ul>                    
			<xsl:if test="$total-pages &gt; 1">
				<xsl:call-template name="display-pages"/>
			</xsl:if>
		</div>    
	</xsl:template>
	
	<xsl:template name="pagecount-alttext">
		<xsl:param name="count">5</xsl:param>
		<xsl:call-template name="replacePlaceHolders">
			<xsl:with-param name="inputString" select="$tablePagingShowLinkAlt"/>
			<xsl:with-param name="value" select="$count" />
		</xsl:call-template>		
	</xsl:template>
	
	<xsl:template name="items-per-page-link">
		<xsl:param name="count">5</xsl:param>
		<xsl:param name="class"></xsl:param>

		<xsl:choose>
			<xsl:when test="$items-per-page = $count">
				<li class="{$class}" role="listitem" aria-disabled="true"><xsl:value-of select="$count"/></li>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="alt_itemsperpage">
					<xsl:call-template name="replacePlaceHolders">
						<xsl:with-param name="inputString" select="$tablePagingShowLinkAlt"/>
						<xsl:with-param name="value" select="$count" />
					</xsl:call-template>
				</xsl:variable>
				<li class="{$class}" role="listitem">
					<a href="javascript:void(0);" onclick="{$js-app-prefix}.setItemsPerPage(this,{$count})" title="{$alt_itemsperpage}" aria-label="{$alt_itemsperpage}"><xsl:value-of select="$count"/></a>
				</li>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>	

	<xsl:template name="table-paging-footer">
		<!-- Bottom paging -->

		<div class="lotusPaging" role="region" aria-label="{$tableBottomPagingLabel} {$paging-info-alt}">
			<xsl:call-template name="output-hidden-fields"/>
			
			<!-- items per page links -->
			<div class="lotusLeft">
				<span aria-hidden="true"><xsl:value-of select="$tablePagingShow"/>&#160;</span>			
				<ul class="lotusInlinelist" role="list">
					
					<xsl:call-template name="items-per-page-link">
						<xsl:with-param name="count">5</xsl:with-param>
						<xsl:with-param name="class">lotusFirst</xsl:with-param>
					</xsl:call-template>
					
					<xsl:call-template name="items-per-page-link">
						<xsl:with-param name="count">10</xsl:with-param>
					</xsl:call-template>
					
					<xsl:call-template name="items-per-page-link">
						<xsl:with-param name="count">50</xsl:with-param>
					</xsl:call-template>
					
					<xsl:call-template name="items-per-page-link">
						<xsl:with-param name="count">100</xsl:with-param>
					</xsl:call-template>				
										   
				</ul>
				<span aria-hidden="true"><xsl:value-of select="$tablePagingItermsPerPage"/></span> 
			</div>

			<ul class="lotusRight lotusInlinelist" role="list">
				<xsl:call-template name="next-previous-links"/>
			</ul>
			
			<xsl:if test="$total-pages &gt; 1">
				<span class="lotusInlinelist">
					<label for="jumpToPageNumber" class="lotusHidden"><xsl:value-of select="$tablePagingJumpToPage"/></label>
					<span id="jumpToPageNumberDesc" class="lotusHidden"><xsl:value-of select="$tablePagingJumpToPageDesc"/></span>
					
					<xsl:variable name="mainJumpText">
						<xsl:call-template name="replacePlaceHolders">
							<xsl:with-param name="searchFor" select="'{1}'" />
							<xsl:with-param name="value" select="$total-pages" />
							<xsl:with-param name="inputString" select="$tablePagingJumpToPageControl"/>
						</xsl:call-template>
					</xsl:variable>
					
					<xsl:value-of select="substring-before($mainJumpText,'{0}')" />
					<input 
						id="jumpToPageNumber"
						aria-describedby="jumpToPageNumberDesc"
						type="text" 
						name="pageNumber" 
						value="{$current-page + 1}" 
						onblur="{$js-app-prefix}.pageTo(this,(this.value - 1))"
						onkeypress="{$js-app-prefix}.handlePageToEnterKey(event, this,(this.value - 1))"
					/>					
					<xsl:value-of select="substring-after($mainJumpText,'{0}')" />

				</span>

			</xsl:if>
		</div>   
		
	<!--end bottom paging-->     
	</xsl:template>
  
	<xsl:template name="output-hidden-fields">
		<input type="hidden" name="key"  id="displayedUserKey" value="{$displayedUserKey}"/>
		<input type="hidden" name="items-per-page"  id="items-per-page" value="{$items-per-page}"/>
		<input type="hidden" name="sortBy"          id="sortBy"         value="{$sortBy}"/>
		<input type="hidden" name="current-page"    id="current-page"   value="{$current-page}"/>
		<input type="hidden" name="total-pages"     id="total-pages"    value="{$total-pages}"/>
	</xsl:template>
        
	<xsl:template name="next-previous-links">
		<xsl:choose>         
			<xsl:when test="$current-page = 0">
				<li class="lotusFirst" role="listitem" aria-disabled="true"><xsl:value-of select="$tablePagingPrevious"/></li>
			</xsl:when>             
			<xsl:otherwise>
			<li class="lotusFirst" role="listitem">
				<a href="javascript:void(0);" onclick="{$js-app-prefix}.pageTo(this,{$current-page - 1})" title="{$tablePagingPreviousAlt}" aria-label="{$tablePagingPreviousAlt}">
					<xsl:value-of select="$tablePagingPrevious"/>
				</a>
			</li>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>         
			<xsl:when test="$current-page = $total-pages - 1">
				<li role="listitem" aria-disabled="true"><xsl:value-of select="$tablePagingNext"/></li>
			</xsl:when>             
			<xsl:otherwise>
				<li role="listitem">
					<a href="javascript:void(0);" onclick="{$js-app-prefix}.pageTo(this,{$current-page + 1})" title="{$tablePagingNextAlt}" aria-label="{$tablePagingNextAlt}">
						<xsl:value-of select="$tablePagingNext"/>
					</a>
				</li>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
  
	<xsl:template name="display-items-info">
		<xsl:value-of select="$paging-info"/>
	</xsl:template>

	<xsl:template name="render-list-item">
		<xsl:param name="linkPage">-1</xsl:param>
		<xsl:param name="className"></xsl:param>
		<xsl:param name="prefix"></xsl:param>
		
		<xsl:if test="$linkPage &gt;= 0">
			<xsl:variable name="realLinkPage" select="$linkPage + 1"/>
			<xsl:variable name="listItemLinkAlt">
				<xsl:call-template name="replacePlaceHolders">
					<xsl:with-param name="inputString" select="$tablePagingPageLinkAlt" />
					<xsl:with-param name="value" select="$realLinkPage" />
				</xsl:call-template>	
			</xsl:variable>
			<xsl:if test="$prefix != ''"><span aria-hidden="true"><xsl:value-of select="$prefix"/></span></xsl:if>
			<li role="listitem" class="{$className}">
				<xsl:choose>
					<xsl:when test="$current-page = $linkPage">
						<xsl:attribute name="aria-disabled">true</xsl:attribute>
						<xsl:value-of select="$realLinkPage"/>
					</xsl:when>
					<xsl:otherwise>
						<a href="javascript:void(0);" onclick="{$js-app-prefix}.pageTo(this,{$linkPage})" title="{$listItemLinkAlt}" aria-label="{$listItemLinkAlt}">
							<xsl:value-of select="$realLinkPage"/>
						</a>
					</xsl:otherwise>
				</xsl:choose>			
			</li>
		</xsl:if>		

	</xsl:template>  
   
	<xsl:template name="render-list-loop">
		<xsl:param name="linkPage">-1</xsl:param>
		<xsl:param name="className"></xsl:param>
		<xsl:param name="ubound">-1</xsl:param>

		<xsl:if test="$linkPage &lt;= $ubound">
			<!-- render the item -->
			<xsl:call-template name="render-list-item">
				<xsl:with-param name="linkPage" select="$linkPage"/>
				<xsl:with-param name="className" select="$className"/>
			</xsl:call-template>

			<!-- loop to the next item -->
			<xsl:call-template name="render-list-loop">
				<xsl:with-param name="linkPage" select="$linkPage + 1"/>
				<xsl:with-param name="ubound" select="$ubound"/>
			</xsl:call-template>
		</xsl:if>

	</xsl:template>

	<xsl:template name="display-pages">
		<xsl:variable name="cpage" select="$current-page" />
		<xsl:variable name="tpage" select="$total-pages - 1" />
		
		<xsl:variable name="lbound1">
			<xsl:choose>
				<xsl:when test="$cpage = 0 or $cpage = 1">1</xsl:when>
				<xsl:when test="$cpage = $tpage or $cpage = $tpage - 1"><xsl:value-of select="$tpage - 2"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$cpage - 1"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="lbound">
			<xsl:choose>
				<xsl:when test="$lbound1 &lt;= 0">1</xsl:when>
				<xsl:otherwise><xsl:value-of select="$lbound1"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>			
		
		
		<xsl:variable name="ubound1">
			<xsl:choose>
				<xsl:when test="$cpage = 0 or $cpage = 1">2</xsl:when>
				<xsl:when test="$cpage = $tpage or $cpage = $tpage - 1"><xsl:value-of select="$tpage - 1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$cpage + 1"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="ubound">
			<xsl:choose>
				<xsl:when test="$ubound1 &gt;= $tpage"><xsl:value-of select="$tpage - 1"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="$ubound1"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>			
		

		<!--first page link -->
		<nobr>
			<span aria-hidden="true"><xsl:value-of select="$tablePagingPage"/>&#160;</span>
			
			<ul class="lotusInlinelist" role="list">
				<xsl:call-template name="render-list-item">
					<xsl:with-param name="className">lotusFirst</xsl:with-param>
					<xsl:with-param name="linkPage" select="0"/>
				</xsl:call-template>

				<!--first elipses if needed --> 
				<xsl:if test="$lbound &gt; 1">
					<li aria-disabled="true" role="listitem">...</li>
				</xsl:if>
				
				<!-- loop through lbound and ubound to render page links -->
				<xsl:call-template name="render-list-loop">
					<xsl:with-param name="linkPage" select="$lbound"/>
					<xsl:with-param name="ubound" select="$ubound"/>
				</xsl:call-template>
				
				<!--last elipses if needed -->
				<xsl:if test="$ubound &lt; $tpage - 1">
					<li aria-disabled="true" role="listitem">...</li>
				</xsl:if>		
				
				<!--last page link -->
				<xsl:call-template name="render-list-item">
					<xsl:with-param name="linkPage" select="$tpage"/>
				</xsl:call-template>
			</ul>
		</nobr>
	
	</xsl:template>  
  <!-- END OF COPIED CODED -->
  
  <!-- copied from replace.xsl. DO NOT EDIT THE CODE BELOW -->
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
