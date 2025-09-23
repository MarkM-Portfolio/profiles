<#-- ***************************************************************** --> 
<#--                                                                   -->
<#-- Licensed Materials - Property of IBM                              -->
<#--                                                                   -->
<#-- 5724-S68                                                          -->                                                          
<#--                                                                   -->
<#-- Copyright IBM Corp. 2011, 2014  All Rights Reserved.              -->
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
	
	<#-- Start SmartCloud Fields -->
	<@util.renderFormControl ref="address1" isMultiline=true/>
	<@util.renderFormControl ref="address2" isMultiline=true/>
	<@util.renderFormControl ref="address3" isMultiline=true/>		
	<@util.renderFormControl ref="address4" isMultiline=true/>
	<#-- End SmartCloud Fields -->
    		
    <@util.renderFormControl ref="bldgId" singleColumnLayout=false nlsKey="label.bldgId" isBidiTextDir=true/>
    		
    <@util.renderFormControl ref="floor" singleColumnLayout=false nlsKey="label.floor"/>
    		
    <@util.renderFormControl ref="officeName" singleColumnLayout=false nlsKey="label.officeName" isBidiTextDir=true/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
	<#-- Start SmartCloud Fields -->
	<#assign phoneLabelOptions=["label.phone.other","label.phone.home","label.phone.work","label.phone.cell","label.phone.fax"]/>
	<@util.renderFormControl ref="phone1" renderLabelAs="select" labelOptions=phoneLabelOptions labelDefault="label.phone.other"/>
	<@util.renderFormControl ref="phone2" renderLabelAs="select" labelOptions=phoneLabelOptions labelDefault="label.phone.other"/>	
    <@util.renderFormControl ref="phone3" renderLabelAs="select" labelOptions=phoneLabelOptions labelDefault="label.phone.other"/>
	<#-- End SmartCloud Fields -->
	
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
    <@util.renderFormControl ref="blogUrl" singleColumnLayout=false nlsKey="label.blogUrl"/>

	<@util.renderBidiEnforce />	
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp" isBidiTextDir=true/>
    		
	<#if profile.permissions?has_content && profile.permissions['profile.typeAhead$profile.typeAhead.view']?has_content && profile.permissions['profile.typeAhead$profile.typeAhead.view'] == 'true'>
		<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
	</#if>

	<#-- SmartCloud Disabled
	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>
	-->
	
	<#-- Start SmartCloud Fields -->
	<@util.renderFormControl ref="item1" isMultiline=true/>
	<@util.renderFormControl ref="item2" isMultiline=true/>
	<@util.renderFormControl ref="item3" isMultiline=true/>
	<@util.renderFormControl ref="item4" isMultiline=true/>
	<@util.renderFormControl ref="item5" isMultiline=true/>
	<@util.renderFormControl ref="item6" isMultiline=true/>
	<@util.renderFormControl ref="item7" isMultiline=true/>
	<@util.renderFormControl ref="item8" isMultiline=true/>
	<@util.renderFormControl ref="item9" isMultiline=true/>	
	<@util.renderFormControl ref="item10" isMultiline=true/>
	<#-- End SmartCloud Fields -->	

	<@util.renderBidiEnforce />	
	    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description" isBidiTextDir=true/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience" isBidiTextDir=true/>

	<@util.renderBidiEnforce />	
    		
</@util.renderSection>   	

</#compress>							        