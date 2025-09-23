
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
			<h2 id='${util.getDisplayNameId()}' class='fn'>				
		<@util.renderValue ref=ref/>																															
									
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="uid" nlsKey="label.uid" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<span class='attuid'> (				
		<@util.renderValue ref=ref/>																															
			)</span></h2>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<div class='empstat'>				
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>																															
									
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="ConsCompany" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			/				
		<@util.renderValue ref=ref/>																															
									
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="jobResp" nlsKey="label.jobResp" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			</div><div class='jobResp'><b>				
		<@util.renderValue ref=ref/>																															
			</b></div>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="BusGroupId" nlsKey="label." ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<div class='ext-BusGroupId'><b>				
		<@util.renderValue ref=ref/>																															
			</b></div>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="BusUnitName" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<div class='ext-BusUnitName'><b>				
		<@util.renderValue ref=ref/>																															
			</b></div>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="Address" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<div class='extended-address'>				
		<@util.renderValue ref=ref/>																															
			,&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
							
		<@util.renderValue ref=ref/>																															
			,&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="City" nlsKey="label." ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
							
		<@util.renderValue ref=ref/>																															
			,&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="State" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
							
		<@util.renderValue ref=ref/>																															
			&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="PostalCode" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
							
		<@util.renderValue ref=ref/>																															
			&nbsp;						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="countryCode" nlsKey="label.countryCode" ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
							
		<@util.renderValue ref=ref/>																															
			</div><div class='tel'>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="reachMe" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<div class='reachme'>Reach Me:&nbsp;				
		<@util.renderValue ref=ref/>																															
			</div>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<span class='office'> O:				
		<@util.renderValue ref=ref/>																															
			</span>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<span class='cell'> C:				
		<@util.renderValue ref=ref/>																															
			</span>						
	</@util.renderProperty>					    							    
    	<div id='qbcinfo'
	<@util.renderProperty ref="uid" nlsKey="label.uid" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			 attuid='				
		<@util.renderValue ref=ref/>																															
			'>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="uid" nlsKey="label.uid" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<a href='qto://talk/				
		<@util.renderValue ref=ref/>																															
			'>						
	</@util.renderProperty>					    							    
    	
	<@util.renderProperty ref="uid" nlsKey="label.uid" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>    		
			<img src='http://presence.q.att.com/PresenceService/status?qid=				
		<@util.renderValue ref=ref/>																															
			'><span>Q Me!</span></a></div>						
	</@util.renderProperty>					    							    
    	</div>
</#compress>			
    