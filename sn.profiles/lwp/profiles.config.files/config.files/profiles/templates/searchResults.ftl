<#-- ***************************************************************** -->
<#--                                                                   -->
<#-- HCL Confidential					                               -->
<#--                                                                   -->
<#-- OCO Source Materials	                                           -->
<#--                                                                   -->
<#-- Copyright HCL Technologies Limited 2011, 2022                     -->
<#--                                                                   -->
<#-- The source code for this program is not published or otherwise    -->
<#-- divested of its trade secrets, irrespective of what has been      -->
<#-- deposited with the U.S. Copyright Office.  All Rights Reserved.   -->
<#--                                                                   -->
<#-- ***************************************************************** -->

<#-- ***************************************************************** --> 
<#-- Template: searchResults.ftl                                       -->
<#--                                                                   -->
<#-- This template is used to render lists of profile results in the   --> 
<#-- web application across multiple views that share a common look    -->
<#-- and feel.                                                         -->
<#--                                                                   -->
<#-- Each view is identified by one of the following section labels:   -->
<#-- 	"reportTo" = the full report-to-chain and same manager view    -->
<#-- 	"directory" = the profile directory search results			   -->
<#--                                                                   -->                             
<#-- If a customer wants special mark-up for either view, it is        -->
<#-- recommended that they leverage the renderSection macro.           -->
<#--                                                                   -->
<#-- For example, to render mark-up only when rendering directory      -->                                                          
<#-- results, the following macro should surround the mark-up		   -->
<#-- 	<@util.renderSection sectionLabel="directory">		           -->
<#-- 		... mark-up specific to directory view ...				   -->
<#-- 	</@util.renderSection>		  						           -->
<#-- ***************************************************************** -->
				    	
<#import "commonUtil.ftl" as util>
<#compress>

<#--  CNXSERV-12611 # Call to wrapper macro 'renderUI' which renders Cnx8 UI or pre-cnx8 UI -->
<#--   on basis of value of 'request.isCnx8UI' key (set in TemplateDataModel class) -->
<@renderUI>
</@renderUI>

<#--  CNXSERV-12611 # Declaration of wrapper macro 'renderUI' which renders Cnx8 UI or pre-cnx8 UI -->
<#--  on basis of value of 'request.isCnx8UI' key (set in TemplateDataModel class) -->
<#macro renderUI>
	<#assign isCnx8UI=request.isCnx8UI/>
	<#if isCnx8UI == true>		
		<@renderCnx8UI>
		</@renderCnx8UI>
	<#else>
		<@renderOldUI>
		</@renderOldUI>
	</#if>
	
</#macro>

<#--  CNXSERV-12611 # Declaration of macro 'renderCnx8UI' which renders Cnx8 UI -->
<#macro renderCnx8UI>
	<style>

	</style>
	<div class="cnx8ui-result-item-grid">
		<#--  <@util.renderPhoto displayName='${profile.displayName}'>
		</@util.renderPhoto>  -->
		<div>
			<@util.renderProperty ref="key" nlsKey="label.profilePicture" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
				
					<@util.renderValue ref=ref renderAs="photo"/>
				
			</@util.renderProperty>	
			<div class="cnx8ui-name-phone-email">	
				<@util.renderProperty ref="displayName" nlsKey="label.displayName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
					<div>
					
						<@util.renderValue ref=ref renderAs="hcard" userId=(profile.userid)!"" className="lotusBold cnx8ui-display-name"/>
					
					</div>
				</@util.renderProperty>					    
				<@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=false ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
					<div>
						<#--  <strong>In telephoneNumber</strong>  -->
						<@util.renderValueOrEmptyRow ref=ref/>
					</div>
				</@util.renderProperty>						
				<@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=false ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
					<div>
						<strong>
							<@util.renderValueOrEmptyRow ref=ref renderAs="email" className="cnx8ui-email"/>
						</strong>
					</div>
				</@util.renderProperty>
			
			</div>

			<@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=false ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
				<div class='bidiAware'>
					<@util.renderValueOrEmptyRow ref=ref dataId=dataId dataKey=dataKey/>
				
				</div>
			</@util.renderProperty>	
			<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=false ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
				<span>
				
					<@util.renderValueOrEmptyRow ref=ref dataId=dataId dataKey=dataKey/>
				
				</span>
			</@util.renderProperty>					    
						
			<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
				<span>
				
					<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
				
				</span>
			</@util.renderProperty>					    
						
			<@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
				<span class='bidiAware'>
				
					<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
				
				</span>
			</@util.renderProperty>				
			<#--  <span>${profileType.telephoneNumber}</span>  -->
		</div>
	</div>
</#macro>



<#--  CNXSERV-12611 # Declaration of macro 'renderOldUI' which renders Old UI before Cnx8-->
<#macro renderOldUI>

	<@util.renderSearchResultColumn isFirst=true isLast=false>
						
		<@util.renderProperty ref="key" nlsKey="label.profilePicture" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div>
			
				<@util.renderValue ref=ref renderAs="photo"/>
			
			</div>
		</@util.renderProperty>					    
		
	</@util.renderSearchResultColumn>
		
	<@util.renderSearchResultColumn isFirst=false isLast=false>
						
		<@util.renderProperty ref="displayName" nlsKey="label.displayName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div>
			
				<div role="article" aria-label="${profile.displayName?replace("\"", "&quot;")}">
			
				<@util.renderValue ref=ref renderAs="hcard" userId=(profile.userid)!"" className="lotusBold"/>
			
				</div>
			
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div class='bidiAware'>
			
				<@util.renderValue ref=ref/>
			
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div class='bidiAware'>
			
				<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
			
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div class='bidiAware'>
			
				<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
			
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div class='bidiAware'>
			
				<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
			
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div class='bidiAware'>
			
				<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
			
			</div>
		</@util.renderProperty>					    
		
	</@util.renderSearchResultColumn>
		
	<@util.renderSearchResultColumn isFirst=false isLast=true>
						
		<@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div>
			<@util.renderNls nlsKey=nlsKey/> <strong>
				<@util.renderValue ref=ref/>
			</strong>
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div>
			<@util.renderNls nlsKey=nlsKey/> <strong>
				<@util.renderValue ref=ref renderAs="email"/>
			</strong>
			</div>
		</@util.renderProperty>					    
					
		<@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
			<div>
			<@util.renderNls nlsKey=nlsKey/> <strong>
				<@util.renderValue ref=ref renderAs="email"/>
			</strong>
			</div>
		</@util.renderProperty>					    
		
	</@util.renderSearchResultColumn>
</#macro>

</#compress>