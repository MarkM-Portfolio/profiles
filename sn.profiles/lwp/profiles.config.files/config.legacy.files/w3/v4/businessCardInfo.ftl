
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
<#-- Template: businessCardInfo.ftl                                    -->
<#--                                                                   -->
<#-- This template is used to render fields in the business card.      -->                                                          
<#-- ***************************************************************** -->   	
    			          
<#import "commonUtil.ftl" as util>  
<#compress>   
		
	<@util.renderProperty ref="displayName" nlsKey="label.displayName" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<h2><span id='${util.getDisplayNameId()}' class='fn'>				
		<@util.renderValue ref=ref/>																															
			</span></h2>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="jobResp" nlsKey="label.jobResp" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<p class='title'>				
		<@util.renderValue ref=ref/>																															
			</p>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="title" nlsKey="label.title" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<p class='role'>				
		<@util.renderValue ref=ref/>																															
			</p>						
	</@util.renderProperty>					    							    
    	<div class='adr'>
	<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<span class='locality'>				
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>																															
			</span>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			,&nbsp;<span class='region'>				
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>																															
			</span>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			&nbsp;<span class='country-name'>				
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>																															
			</span>						
	</@util.renderProperty>					    							    
    	<p class='extended-address'>
	<@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<@util.renderNls nlsKey=nlsKey/> 				
		<@util.renderValue ref=ref/>																															
			&nbsp;|&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<@util.renderNls nlsKey=nlsKey/> 				
		<@util.renderValue ref=ref/>																															
			&nbsp;|&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<@util.renderNls nlsKey=nlsKey/> 				
		<@util.renderValue ref=ref/>																															
									
	</@util.renderProperty>					    							    
    	</p></div>
	<@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<p class='tel'>				
		<@util.renderValue ref=ref/>																															
			</p>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<p>				
		<@util.renderValue ref=ref renderAs="email"/>																															
			</p>						
	</@util.renderProperty>					    							    
    	
</#compress>			
    