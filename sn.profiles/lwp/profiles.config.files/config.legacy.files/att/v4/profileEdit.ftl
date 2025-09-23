
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
		
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="displayName" isDisabled=true singleColumnLayout=false nlsKey="label.displayName"/>
    		
    <@util.renderFormControl ref="blogUrl" singleColumnLayout=false nlsKey="label.blogUrl"/>
    		
    <@util.renderFormControl ref="reachMe" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.contactInformation.extattr.reachMe"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">
		
    <@util.renderFormControl ref="skills" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.skills"/>
    		
    <@util.renderFormControl ref="orgs" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.orgs"/>
    		
    <@util.renderFormControl ref="patents" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.patents"/>
    		
    <@util.renderFormControl ref="languages" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.languages"/>
    		
    <@util.renderFormControl ref="projects" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.projects"/>
    		
    <@util.renderFormControl ref="degrees" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.degrees"/>
    		
    <@util.renderFormControl ref="certifications" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.certifications"/>
    		
    <@util.renderFormControl ref="colleges" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.colleges"/>
    		
    <@util.renderFormControl ref="gradSchool" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.gradSchool"/>
    		
    <@util.renderFormControl ref="highSchool" singleColumnLayout=false nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.highSchool"/>
    		
    <@util.renderFormControl ref="experience" singleColumnLayout=false nlsKey="label.experience"/>
    		
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">

	<@util.renderFormControl ref="timezone" singleColumnLayout=false nlsKey="label.timezone" isSelect=true options=util.availableTimezones()/>

</@util.renderSection> 						
			
<@util.renderSection sectionLabel="associatedInformation">
		
    <@util.renderFormControl ref="interests" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.interests"/>
    		
    <@util.renderFormControl ref="favoriteMovies" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteMovies"/>
    		
    <@util.renderFormControl ref="favoriteMusic" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteMusic"/>
    		
    <@util.renderFormControl ref="favoriteQuote" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteQuote"/>
    		
    <@util.renderFormControl ref="favoriteSports" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteSports"/>
    		
    <@util.renderFormControl ref="activities" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.activities"/>
    		
    <@util.renderFormControl ref="favoriteTVShows" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteTVShows"/>
    		
    <@util.renderFormControl ref="favoriteBooks" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteBooks"/>
    		
    <@util.renderFormControl ref="twitterId" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.twitterId"/>
    		
    <@util.renderFormControl ref="linkedInId" singleColumnLayout=true nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.linkedInId"/>
    		
    <@util.renderFormControl ref="description" singleColumnLayout=true nlsKey="label.description"/>
    		
</@util.renderSection>   							    
    
</#compress>					
    