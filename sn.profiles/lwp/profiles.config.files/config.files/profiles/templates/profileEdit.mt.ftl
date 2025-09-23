<#-- ***************************************************************** --> 
<#--                                                                   -->
<#-- Licensed Materials - Property of IBM                              -->
<#--                                                                   -->
<#-- 5724-S68                                                          -->                                                          
<#--                                                                   -->
<#-- Copyright IBM Corp. 2013  All Rights Reserved.                    -->
<#--                                                                   -->
<#-- US Government Users Restricted Rights - Use, duplication or       -->
<#-- disclosure restricted by GSA ADP Schedule Contract with           -->
<#-- IBM Corp.                                                         -->                             
<#--                                                                   -->
<#-- ***************************************************************** -->
		
<#-- ***************************************************************** --> 
<#-- Template: profileEdit.ftl                                         -->
<#--                                                                   -->
<#-- This template is used to render a profile edit form.              -->    
<#--                                                                   -->
<#-- Each form area is identified by the following section labels:     -->
<#-- 	"contactInformation" = the contact information tab             -->
<#-- 	"associatedInformation" = the about me information tab         -->   
<#-- Validation rules for an attribute are defined in                  -->
<#-- WEB-INF/validation.xml. For validation rules to be picked up the  -->
<#-- sectionLabel specified here must correspond to a subEditForm      -->
<#-- value in validation.xml                                           -->                                                   
<#-- ***************************************************************** -->   	
   	
		    	
<#import "commonUtil.ftl" as util>
<#compress>   
		
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName" isBidiTextDir=true/>
	<@util.renderFormControl ref="employeeNumber" isDisabled=true singleColumnLayout=false nlsKey="label.employeeNumber" isBidiTextDir=true/>
	
    <@util.renderFormControl ref="courtesyTitle" singleColumnLayout=false nlsKey="label.courtesyTitle" isBidiTextDir=true/>
	<@util.renderFormControl ref="givenName" singleColumnLayout=false nlsKey="label.givenName" isBidiTextDir=true/>
	<@util.renderFormControl ref="surname" singleColumnLayout=false nlsKey="label.surname" isBidiTextDir=true/>
	<@util.renderFormControl ref="preferredFirstName" singleColumnLayout=false nlsKey="label.preferredFirstName" isBidiTextDir=true/>
	<@util.renderFormControl ref="preferredLastName" singleColumnLayout=false nlsKey="label.preferredLastName" isBidiTextDir=true/>

	
	<@util.renderFormControl ref="officeName" singleColumnLayout=false nlsKey="label.officeName" isBidiTextDir=true/>
    
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>

	<@util.renderBidiEnforce />	
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp" isBidiTextDir=true/>
	
	<@util.renderFormControl ref="deptNumber" singleColumnLayout=false nlsKey="label.deptNumber" isBidiTextDir=true/>
    
    <@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

	<@util.renderBidiEnforce />	
	    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description" isBidiTextDir=true/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience" isBidiTextDir=true/>

	<@util.renderBidiEnforce />	
    		
</@util.renderSection>   	

</#compress>							        