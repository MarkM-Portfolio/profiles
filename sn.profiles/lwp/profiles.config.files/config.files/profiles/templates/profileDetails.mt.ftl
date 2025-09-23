<#-- ***************************************************************** --> 
<#--                                                                   -->
<#-- Licensed Materials - Property of IBM                              -->
<#--                                                                   -->
<#-- 5724-S68                                                          -->                                                          
<#--                                                                   -->
<#-- Copyright IBM Corp. 2013, 2014  All Rights Reserved.              -->
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
<#-- 	"jobInformation" = main profile page data section              -->
<#-- 	"contactInformation" = Contact Information widget data         -->
<#-- 	"associatedInformation" = Background widget data               -->
<#--                                                                   -->                             
<#-- If a customer wants to support an additional template based       -->
<#-- widget, see InfoCenter for more information on required steps.    -->
<#--                                                                   -->
<#-- ***************************************************************** -->   	  
		    	
<#import "commonUtil.ftl" as util>
<#compress>
		
<@util.renderSection sectionLabel="jobInformation">

	<@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<strong class="bidiAware">
		<@util.renderValue ref=ref/>
		</strong><br/>
	</@util.renderProperty>
        	
	<@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
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
						<strong  class="bidiAware">
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
						<p class="bidiAware">
						<@util.renderValue ref=ref/>				
						</p>
					</td>
				</tr>	
				</@util.renderProperty>	
				    				    
        		<@util.renderProperty ref="givenName" nlsKey="label.givenName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>		
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>				
						</p>
					</td>
				</tr>
				</@util.renderProperty>	
				    
        		<@util.renderProperty ref="surname" nlsKey="label.surname" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>		
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>				
						</p>
					</td>
				</tr>
				</@util.renderProperty>
				    
        		<@util.renderProperty ref="preferredFirstName" nlsKey="label.preferredFirstName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>		
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>				
						</p>
					</td>
				</tr>
				</@util.renderProperty>	
				    
        		<@util.renderProperty ref="preferredLastName" nlsKey="label.preferredLastName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>		
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>				
						</p>
					</td>
				</tr>
				</@util.renderProperty>	
				    
        		<@util.renderProperty ref="employeeNumber" nlsKey="label.employeeNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>		
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>				
						</p>
					</td>
				</tr>
				</@util.renderProperty>

				
				    
        		<@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>		
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
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
        	
				<@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>					
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>										
						</p>
					</td>
				</tr>
				</@util.renderProperty>
        	
				<@util.renderProperty ref="deptNumber" nlsKey="label.deptNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>					
				<tr>
					<th scope="row">
					<@util.renderNls nlsKey=nlsKey/> 			
					</th>
					<td>
						<p class="bidiAware">
						<@util.renderValue ref=ref/>										
						</p>
					</td>
				</tr>
				</@util.renderProperty>
				
			</tbody>
		</table>
	</div> 
	
</@util.renderSection>   							    
    
<@util.renderSection sectionLabel="associatedInformation">  

    <#if !(profile["description"])?has_content && !(profile["experience"])?has_content>
		<@util.renderNls nlsKey="noinformation"/>			
	</#if>
	        	
    <@util.renderProperty ref="description" nlsKey="label.description" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
	<tr>
		<th scope="row">
			<@util.renderNls nlsKey=nlsKey/>
		</th>
		<td>
			<p class="bidiAware">
				<@util.renderValue ref=ref/>			
			</p>
		</td>
	</tr>					
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="experience" nlsKey="label.experience" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
	<tr>
		<th scope="row">
			<#if (profile["description"])?has_content>
				<br/><@util.renderNls nlsKey=nlsKey/>
			</#if>
		</th>
		<td>
			<p class="bidiAware">
				<@util.renderValue ref=ref/>			
			</p>
		</td>
	</tr>					
	</@util.renderProperty>					    
    
</@util.renderSection>

</#compress>    