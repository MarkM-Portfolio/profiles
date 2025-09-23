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
<#-- 	"jobInformation" = main profile page data section			   -->
<#-- 	"contactInformation" = Contact Information widget data         -->
<#-- 	"associatedInformation" = Background widget data			   -->
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
        	
    <@util.renderProperty ref="employeeTypeCode" dataId="employeeType" dataKey="employeeTypeDesc" nlsKey="label.employeeTypeDesc" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
	</@util.renderProperty>					    
        	
    <@util.renderProperty ref="orgId" dataId="organization" dataKey="organizationTitle" nlsKey="label.organizationTitle" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
			<br/>
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
        	
    <@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
		
		<span data-phone-type="telephone"><@util.renderValue ref=ref/></span>
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
	
	<#-- Start SmartCloud Fields -->
	<@util.renderProperty ref="address1" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="address2" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="address3" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="address4" hideIfEmpty=true isMultiline=true/>
	<#-- End SmartCloud Fields -->
	
        	
    <@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
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
        	
    <@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
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
	
		<span data-phone-type="telephone"><@util.renderValue ref=ref/></span>
										
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
	
		<span data-phone-type="ip_telephone"><@util.renderValue ref=ref/></span>
										
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
	
		<span data-phone-type="mobile"><@util.renderValue ref=ref/></span>
										
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
	
		<span data-phone-type="pager"><@util.renderValue ref=ref/></span>
										
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
	
		<span data-phone-type="fax"><@util.renderValue ref=ref/></span>
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    
	
	<#-- Start SmartCloud Fields -->
	<@util.renderProperty ref="phone1" hideIfEmpty=true/>
	<@util.renderProperty ref="phone2" hideIfEmpty=true/>
	<@util.renderProperty ref="phone3" hideIfEmpty=true/>
    <#-- End SmartCloud Fields --> 
        	
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
        	
    <@util.renderProperty ref="blogUrl" nlsKey="label.blogUrl" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p class="bidiSTT_URL">
	
		<@util.renderValue ref=ref renderAs="blogUrl"/>			
										
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
        	
    <@util.renderProperty ref="secretaryUid" dataId="secretary" dataKey="secretaryName" nlsKey="label.secretaryName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
							
	<tr>
		<th scope="row">
		<@util.renderNls nlsKey=nlsKey/> 			
		</th>
		<td>
			<p class="bidiAware">
	
		<@util.renderValue ref=ref dataId=dataId dataKey=dataKey/>			
										
			</p>
		</td>
	</tr>					
	
	</@util.renderProperty>					    

	<#-- Start SmartCloud Fields -->
	<@util.renderProperty ref="item1" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item2" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item3" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item4" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item5" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item6" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item7" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item8" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item9" hideIfEmpty=true isMultiline=true/>
	<@util.renderProperty ref="item10" hideIfEmpty=true isMultiline=true/>
	<#-- End SmartCloud Fields -->	
    		
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
   		

<#-- miniCard section to be used in Contacts widget --> 
<@util.renderSection sectionLabel="miniCard">
	<div class="lotusWidget2">
		<table role="presentation">
			<tr>
				<td><@util.renderPhoto/></td>
				<td>
					<@util.renderProperty ref="displayName" nlsKey="label.displayName" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
						<div><strong class="bidiAware"><a href="${util.getProfileUrl()}"><@util.renderValue ref=ref/></a></strong></div>
					</@util.renderProperty>
					<@util.renderProperty ref="jobResp" nlsKey="label.jobResp" hideIfEmpty=true ;  ref, dataId, dataKey, nlsKey, nlsBundle>
						<div><@util.renderValue ref=ref/></div>
					</@util.renderProperty>
					<@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
						<div><@util.renderValue ref=ref renderAs="email"/></div>
					</@util.renderProperty>
				</td>
			</tr>
		</table>
		<#if util.isAuthenticated()=true && util.isMe()=false>
			<#if util.hasConnection()=false>
				<div class="lotusChunk">${util.bundleResource(nls["template"],"label.ProfileNotInNetwork",[profile.displayName])}</div>
				<input type="button" value="${util.bundleResource(nls["template"],"label.InviteToConnect",[])}" onclick="location.href = (&quot;${util.getProfileUrl()}&invite=true&quot;);" class="lotusBtn">
			<#else>
				<div class="lotusChunk">${util.bundleResource(nls["template"],"label.ProfileInNetwork",[profile.displayName])}</div>
			</#if>
		</#if>
	</div>

</@util.renderSection>	


<#-- network section to be used in Contacts widget --> 
<@util.renderSection sectionLabel="network">
	<#if totalFriends?has_content>
		<#if friends?has_content && (totalFriends > 0)>
			<div class="lotusChunk" role="list">
				<#list friends?keys as friendid>
					<#assign friend = friends[friendid]>
					<div role="listitem" class="lotusLeft lotusNetworkPerson">
						<span class="vcard">
							<span style="display: none;" class="x-lconn-userid">${friendid}</span>
							<a title="${friend.displayName}" class="fn lotusPerson url" href="${util.getProfileUrl(friend.key)}">
								<@util.renderPhoto width="32" height="32" key=friend.key displayName=friend.displayName />
							</a>
						</span>
					</div>
				</#list>
			</div>
		<#else>
			<div class="lotusChunk">
				${util.bundleResource(nls["template"],"label.NoNetworkContacts",[])}
			</div>
		</#if>
			
		<div class="lotusChunk">
			<a class="lotusAction" href="${util.getNetworkUrl()}">${util.bundleResource(nls["template"],"label.ViewAllInNetwork",[totalFriends])}</a>
		</div>
	</#if>

</@util.renderSection>	

</#compress>    