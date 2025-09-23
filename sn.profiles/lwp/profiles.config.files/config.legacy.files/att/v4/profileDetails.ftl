
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
<#-- Template: profileDetails.ftl                                      -->
<#--                                                                   -->
<#-- This template is used to render fields on the profile page.       -->                                                          
<#--                                                                   -->
<#-- Each view is identified by one of the following section labels:   -->
<#-- 	"jobInformation" = main profile page data section			  -->
<#-- 	"contactInformation" = Contact Information widget data        -->
<#-- 	"associatedInformation" = Background widget data			  -->
<#--                                                                   -->                             
<#-- If a customer wants to support an additional template based       -->
<#-- widget, see InfoCenter for more information on required steps.    -->
<#--                                                                   -->
<#-- ***************************************************************** -->   	  
    	
<#import "commonUtil.ftl" as util>
<#compress>
		
<@util.renderSection sectionLabel="jobInformation">  
        	
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
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="City" nlsKey="label." ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="State" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="PostalCode" nlsKey="label." hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
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
		<span class='office'> O:&nbsp;
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class='cell'> C:&nbsp;
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="uid" nlsKey="label.uid" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		</div><div id='qinfo'><a href='qto://talk/
		<@util.renderValue ref=ref/>			
			'>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="uid" nlsKey="label.uid" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<img src='http://presence.q.att.com/PresenceService/status?qid=
		<@util.renderValue ref=ref/>			
			'><span>Q Me!</span></a></div>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="skills" nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.skills" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="orgs" nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.orgs" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="patents" nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.patents" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="languages" nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.languages" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="projects" nlsBundle="tSpace" nlsKey="label.noBC.jobInformation.extattr.projects" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="degrees" nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.degrees" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="certifications" nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.certifications" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="colleges" nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.colleges" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="gradSchool" nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.gradSchool" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="highSchool" nlsBundle="tSpace" nlsKey="label.noBC.educationInformation.extattr.highSchool" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="displayName" nlsKey="label.displayName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="blogUrl" nlsKey="label.blogUrl" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="blogUrl"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="reachMe" nlsBundle="tSpace" nlsKey="label.contactInformation.extattr.reachMe" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    		
			</tbody>
		</table>
	</div> 
	
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="associatedInformation">  
    
    <#if !(profile["description"])?has_content>
		<#if !(profile["experience"])?has_content>
			<@util.renderNls nlsKey="noinformation"/>			
		</#if>
	</#if>	
	    	
    <@util.renderProperty ref="interests" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.interests" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="favoriteMovies" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteMovies" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="favoriteMusic" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteMusic" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="favoriteQuote" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteQuote" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="favoriteSports" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteSports" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="activities" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.activities" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="favoriteTVShows" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteTVShows" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="favoriteBooks" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.favoriteBooks" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="twitterId" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.twitterId" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="linkedInId" nlsBundle="tSpace" nlsKey="label.aboutMe.extattr.linkedInId" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	<span class="hidden-attribute">
		<@util.renderValue ref=ref/>			
			</span>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="description" nlsKey="label.description" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
					
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    	
</#compress>			
    