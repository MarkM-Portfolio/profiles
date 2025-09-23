
<#-- ***************************************************************** --> 
<#--                                                                   -->
<#-- Licensed Materials - Property of IBM                              -->
<#--                                                                   -->
<#-- 5724-S68                                                          -->                                                          
<#--                                                                   -->
<#-- Copyright IBM Corp. 2011  All Rights Reserved.                    -->
<#--                                                                   -->
<#-- US Government Users Restricted Rights - Use, duplication or       -->
<#-- disclosure restricted by GSA ADP Schedule Contract with           -->
<#-- IBM Corp.                                                         -->                             
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
<#-- 	"reportTo" = the full report-to-chain and same manager view   -->
<#-- 	"directory" = the profile directory search results            -->
<#--                                                                   -->                             
<#-- If a customer wants special mark-up for either view, it is        -->
<#-- recommended that they leverage the renderSection macro.           -->
<#--                                                                   -->
<#-- For example, to render mark-up only when rendering directory      -->                                                          
<#-- results, the following macro should surround the mark-up		  -->
<#-- 	<@util.renderSection sectionLabel="directory">		  -->
<#-- 		... mark-up specific to directory view ...				  -->
<#-- 	</@util.renderSection>		  						  -->
<#-- ***************************************************************** -->   	
    	
<#import "commonUtil.ftl" as util>
<#compress>
		
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
		
			<h3>
		
			<@util.renderValue ref=ref renderAs="hcard" userId=(profile.userid)!""/>
		
			</h3>
		
		</div>
	</@util.renderProperty>					    
    			 
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
		<div>
		
			<@util.renderValue ref=ref/>
		
		</div>
	</@util.renderProperty>					    
    			 
    <@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
		<div>
		
			<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
		
		</div>
	</@util.renderProperty>					    
    			 
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
		<div>
		
			<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
		
		</div>
	</@util.renderProperty>					    
    			 
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
		<div>
		
			<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>
		
		</div>
	</@util.renderProperty>					    
    			 
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    	  
		<div>
		
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
    	
</#compress>			
    