
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
		
<#if profile.profileType == "JEL">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_COMPYNAME" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_COMPYNAME" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "IFP">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "TGN">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "ATT">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "LVO">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "NONIBMEMP">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "CAB">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "GOD">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "TSB">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "SWO">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "ITN">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "BPI">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "CCS">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#elseif profile.profileType == "BWI">				
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="preferredLanguage" nlsKey="label.preferredLanguage" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJBROLE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJBROLE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
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
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<#else>			
				
<@util.renderSection sectionLabel="jobInformation">  
        	
    <@util.renderProperty ref="nativeLastName" nlsKey="label.nativeLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="nativeFirstName" nlsKey="label.nativeFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_COMPYNAME" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_COMPYNAME" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRIJRSS" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRIJRSS" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong>
		<@util.renderValue ref=ref/>			
			</strong><br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="address1" nlsKey="label.workLocation.address1" ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			, 
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			&nbsp;|
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref renderAs="email"/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderNls nlsKey=nlsKey/> 
		<@util.renderValue ref=ref renderAs="hcard" dataId=dataId dataKey=dataKey userId=(data.secretary.secretaryUserid)!"" userEmail=(data.secretary.secretaryEmail)!""/>			
			<br/>
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="contactInformation">  
    
	<div class="lotusSectionBody">
		<table class="lotusVertTable">
			<tbody>	
	    	
    <@util.renderProperty ref="IBMEXT_IMPCNTINF" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_IMPCNTINF" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
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
        	
    <@util.renderProperty ref="courtesyTitle" nlsKey="label.courtesyTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="alternateLastname" nlsKey="label.alternateLastname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="isManager" nlsKey="label.isManager" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_JOBCATGRY" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_JOBCATGRY" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="shift" nlsKey="label.shift" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_TIELINE" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_TIELINE" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="ipTelephoneNumber" nlsKey="label.ipTelephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="mobileNumber" nlsKey="label.mobileNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="faxNumber" nlsKey="label.faxNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerNumber" nlsKey="label.pagerNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerId" nlsKey="label.pagerId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerType" nlsKey="label.pagerType" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="pagerServiceProvider" nlsKey="label.pagerServiceProvider" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="groupwareEmail" nlsKey="label.groupwareEmail" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref renderAs="email"/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="IBMEXT_PRFBUSADR" nlsBundle="blueplabel" nlsKey="label.contactInformation.extattr.IBMEXT_PRFBUSADR" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsBundle=nlsBundle nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
			<br/>							
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="deptNumber" dataId="department" dataKey="departmentTitle" nlsKey="label.departmentTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
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
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p>
	
		<@util.renderValue ref=ref/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
    
</@util.renderSection>   							    
    
</#if>
			
</#compress>			
    