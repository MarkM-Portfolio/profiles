<#-- *************************************************************** 
<#--                                                                  -->
<#-- HCL Confidential                                                 -->
<#--                                                                  -->
<#-- OCO Source Materials                                             -->
<#--                                                                  -->
<#-- Copyright HCL Technologies Limited 2022	                      -->
<#--                                                                  -->
<#-- The source code for this program is not published or otherwise   -->
<#-- divested of its trade secrets, irrespective of what has been     -->
<#-- deposited with the U.S. Copyright Office.                        -->
<#--                                                                  -->
<#-- ***************************************************************  -->

<#import "commonUtil.ftl" as util>
<#compress>
	<@util.renderProperty ref="displayName" nlsKey="label.displayName";  ref, dataId, dataKey, nlsKey, nlsBundle>
		<h2><span id='${util.getDisplayNameId()}' class='fn bidiAware'><@util.renderValue ref=ref/></span></h2>
	</@util.renderProperty>
	<@util.renderProperty ref="jobResp" nlsKey="label.jobResp";  ref, dataId, dataKey, nlsKey, nlsBundle>
		<p class='title bidiAware' title='<@util.renderValue ref=ref/>'><@util.renderValue ref=ref/></p>
	</@util.renderProperty>
	<@util.renderProperty ref="title" nlsKey="label.title";  ref, dataId, dataKey, nlsKey, nlsBundle>
		<p class='role bidiAware'><@util.renderValue ref=ref/></p>
	</@util.renderProperty>
	<div class='adr'>
		<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="city" nlsKey="label.workLocation.city" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
			<span class='locality bidiAware'><@util.renderValue ref=ref dataId=dataId dataKey=dataKey/></span>
		</@util.renderProperty>
		<@util.renderProperty ref="workLocationCode" dataId="workLocation" dataKey="state" nlsKey="label.workLocation.state" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
			,&nbsp;<span class='region bidiAware'><@util.renderValue ref=ref dataId=dataId dataKey=dataKey/></span>
		</@util.renderProperty>
		<@util.renderProperty ref="countryCode" dataId="country" dataKey="countryDisplayValue" nlsKey="label.countryDisplayValue" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
			&nbsp;<span class='country-name bidiAware'><@util.renderValue ref=ref dataId=dataId dataKey=dataKey/></span>
		</@util.renderProperty>
		<p class='extended-address bidiAware'>
			<@util.renderProperty ref="bldgId" nlsKey="label.bldgId" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
				<@util.renderNls nlsKey=nlsKey/>
				<@util.renderValue ref=ref/>
				&nbsp;|&nbsp;						
			</@util.renderProperty>
			<@util.renderProperty ref="floor" nlsKey="label.floor" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
				<@util.renderNls nlsKey=nlsKey/>
				<@util.renderValue ref=ref/>
				&nbsp;|&nbsp;						
			</@util.renderProperty>
			<@util.renderProperty ref="officeName" nlsKey="label.officeName" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
				<@util.renderNls nlsKey=nlsKey/>
				<@util.renderValue ref=ref/>
			</@util.renderProperty>
		</p>
	</div>
	<@util.renderProperty ref="telephoneNumber" nlsKey="label.telephoneNumber" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<p class='tel'><@util.renderValue ref=ref/></p>
	</@util.renderProperty>
	<@util.renderProperty ref="email" nlsKey="label.email" hideIfEmpty=true;  ref, dataId, dataKey, nlsKey, nlsBundle>
		<p><@util.renderValue ref=ref renderAs="email"/></p>
	</@util.renderProperty>	
</#compress>