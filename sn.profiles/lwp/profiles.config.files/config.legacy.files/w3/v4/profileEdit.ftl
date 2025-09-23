
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
<#-- Template: profileEdit.ftl                                         -->
<#--                                                                   -->
<#-- This template is used to render a profile edit form.		      -->    
<#--                                                                   -->
<#-- Each form area is identified by the following section labels:     -->
<#-- 	"contactInformation" = the contact information tab			  -->
<#-- 	"associatedInformation" = the about me information tab        -->                                                      
<#-- ***************************************************************** -->   	
    				    	    	
<#import "commonUtil.ftl" as util>
<#compress>   
		
<#if profile.profileType == "JEL">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
    <@util.renderFormControl ref="IBMEXT_PRIJRSS" singleColumnLayout=false nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "IFP">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "TGN">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "ATT">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "LVO">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "NONIBMEMP">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "CAB">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "GOD">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "TSB">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "SWO">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "ITN">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "BPI">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "CCS">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#elseif profile.profileType == "BWI">				
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<#else>			
				
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="telephoneNumber" singleColumnLayout=false nlsKey="label.telephoneNumber"/>
    		
    <@util.renderFormControl ref="ipTelephoneNumber" singleColumnLayout=false nlsKey="label.ipTelephoneNumber"/>
    		
    <@util.renderFormControl ref="mobileNumber" singleColumnLayout=false nlsKey="label.mobileNumber"/>
    		
    <@util.renderFormControl ref="faxNumber" singleColumnLayout=false nlsKey="label.faxNumber"/>
    		
    <@util.renderFormControl ref="pagerNumber" singleColumnLayout=false nlsKey="label.pagerNumber"/>
    		
    <@util.renderFormControl ref="pagerId" singleColumnLayout=false nlsKey="label.pagerId"/>
    		
    <@util.renderFormControl ref="pagerType" singleColumnLayout=false nlsKey="label.pagerType"/>
    		
    <@util.renderFormControl ref="pagerServiceProvider" singleColumnLayout=false nlsKey="label.pagerServiceProvider"/>
    		
    <@util.renderFormControl ref="groupwareEmail" singleColumnLayout=false nlsKey="label.groupwareEmail"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="jobResp" singleColumnLayout=false nlsKey="label.jobResp"/>
    		
    <@util.renderFormControl ref="IBMEXT_PRIJRSS" singleColumnLayout=false nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS"/>
    		
	<@util.renderFormControl ref="secretaryUid" dataId="secretary" dataKey="secretaryName" isPerson=true singleColumnLayout=false nlsKey="label.secretaryName"/>    		
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=true nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
</#if>
		
</#compress>					
    