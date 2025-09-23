<#-- ***************************************************************** -->
<#--                                                                   -->
<#-- HCL Confidential					                               -->
<#--                                                                   -->
<#-- OCO Source Materials	                                           -->
<#--                                                                   -->
<#-- Copyright HCL Technologies Limited 2013, 2022                     -->
<#--                                                                   -->
<#-- The source code for this program is not published or otherwise    -->
<#-- divested of its trade secrets, irrespective of what has been      -->
<#-- deposited with the U.S. Copyright Office.  All Rights Reserved.   -->
<#--                                                                   -->
<#-- ***************************************************************** -->

<#-- To fetch a localized string from specified bundle and key -->
<#function bundleResource bundle messageKey params>
	<#if bundle??>
		<#-- We need to check to see if the messageKey is in the Map, otherwise -->
		     later calls will fail (silently) 
		-->		
		<#local containsKey=false/>
		<#list bundle?keys as key>
			<#if key == messageKey>
				<#local containsKey=true/>
				<#break>
			</#if>
		</#list>
		
		<#if containsKey>
			<#switch params?size>
				<#case 0>
					<#return bundle(messageKey)/>
					<#break>
				<#case 1>
					<#return bundle(messageKey, params[0])/>
					<#break>
				<#case 2>
					<#return bundle(messageKey, params[0], params[1])/>
					<#break>
				<#case 3>
					<#return bundle(messageKey, params[0], params[1], params[2])/>
					<#break>
				<#case 4>
					<#return bundle(messageKey, params[0], params[1], params[2], params[3])/>
					<#break>
				<#case 5>
					<#return bundle(messageKey, params[0], params[1], params[2], params[3], params[4])/>
					<#break>
				<#default>
					<#stop "resource function doesn't support more than 5 parameters for a message due to language reason. And it's seldom to have more than 5 parameters in a message. However, you can extend the limit by changing the function if you really want to."/>
			</#switch>
		<#else>
			<#return messageKey/>
		</#if>
	<#else>
		<#return messageKey/>
	</#if>
	
</#function>

<#-- To render the id of vcard display name element-->
<#function getDisplayNameId>
	<#local displayid=""/>
	<#if config.sametime.enabled && config.sametime.inputType="uid">
		<#local displayid=profile.uid/>
	<#elseif profile.email?has_content && exposeEmail>
		<#local displayid=profile.email/>
	<#elseif profile.userId?has_content>
		<#local displayid=profile.userId/>
	</#if>
	<#return displayid + "vcardNameElem"/>
</#function>

<#function getProfileUrl key=profile.key>
		<#return request.contextPath + "/html/profileView.do?key=" + key/>	
</#function>

<#-- 
RTC 134572
Migrated data that has line feeds need to show html BR tags instead.  However,
some migrated data had SOME embedded html in it (i.e. <p dir="ltr"> ...</p> wrapped 
around plain text) but the were actual line breaks in the value instead of <br/>
tags.  This weird mix of html and non html content was causing an issue in 
IC profiles once migrated.

So we need to remove the weird migrated html from the data then see if there is any
actual html in the value.  If not, then replace the \n with BR tags.
-->
<#function replaceLineBreaksWithTags value>
	<#if (value?contains("\n"))>
		<#if value?ends_with("</p>") && (value?starts_with("<p dir=\"ltr\">") || value?starts_with("<p dir=\"rtl\">"))>
			<#local tempValue=value?substring(value?index_of(">") + 1, value?last_index_of("<"))/>
			<#if !(tempValue?contains("<"))>
				<#local value>${value?replace("\n", "<br/>")}</#local>
			</#if>
		<#elseif !(value?contains("<"))>
			<#local value>${value?replace("\n", "<br/>")}</#local>
		</#if>
	</#if>
	<#return value/>	
</#function>

<#function getNetworkUrl key=profile.key>
		<#return request.contextPath + "/html/networkView.do?widgetId=friends&requireAuth=true&key=" + key/>	
</#function>

<#-- To render a mail link
	 email: the email to render
-->
<#macro renderEmail email>
	<a href="mailto:${email}"  class="bidiSTT_EMAIL">${email}</a>
</#macro>

<#-- To render an hcard
	 userName: the display name for the user
	 userId: the external id of the user to render 
	 userEmail: the email of the user, defaults to empty if not provided
-->
<#macro renderHCard userName userId userEmail="" className="">
	
	<#if userName?has_content && userId?has_content>
		<span class="vcard">
			<span class="x-lconn-userid" style="display: none;">${userId}</span>
			<#if userEmail?has_content>
				<span class="email" style="display: none;">${userEmail}</span>
			</#if>
			<#local ariaLabel=""/>
			<#if profile.userState = "inactive">
				<#local ariaLabel>aria-label="<@renderNls nlsKey="label.inactive.user.msg"/>"</#local>
			</#if>
			<a class="fn url person bidiAware ${className}" ${ariaLabel} href="${request.contextPath}/html/simpleSearch.do?searchFor=${userId}&searchBy=userid">
			${userName}
			</a>			
		</span>
	</#if>
</#macro>

<#macro renderLink url message>
	<a href="${url}" target="_blank" class="bidiSTT_URL">${message}</a>
</#macro>

<#macro renderPhoto key="" displayName="" width="55" height="55">

	<#local params=[profile.displayName]/>
	<#local url=request.contextPath + "/photo.do?key=" + profile.key/>
	
	<#if key?has_content>
		<#local url=request.contextPath + "/photo.do?key=" + key/>
	</#if>
	<#if displayName?has_content>
		<#local params=[displayName]/>
	</#if>	
	
	<#local alttext = bundleResource(nls["template"],"label.searchresults.photo.alttext",params) />
	<img src="${url}" class="photo" width="${width}" height="${height}" alt="${alttext}"/>	
</#macro>

<#macro renderSearchResultColumn isFirst=false isLast=false>
	<#local style=""/>
	<#local styleCls=""/>
	<#if isFirst>
		<#if rowIndex=0>
			<@renderSearchResultsTableHeader/>
		</#if>
		<#local style="width:70px;"/>
		<#local styleCls="lotusFirstCell"/>
	</#if>
	<#if isLast>
		<#local styleCls="lotusLastCell"/>
	</#if>
	<td style="${style}" class="${styleCls}">
	<#nested>
	<#if isFirst=false && isLast=false && profile.userState = "inactive">
		<span><@renderNls nlsKey="label.inactive.user.msg"/></span>
	</#if>
	</td>
</#macro>

<#macro renderSearchResultsTableHeader>
		<th class="lotusHidden">
			<@renderNls nlsKey="label.searchresults.tableheading.photo"/>
		</th>
		<th class="lotusHidden">
			<@renderNls nlsKey="label.searchresults.tableheading.employeeinfo"/>
		</th>
		<th class="lotusHidden">
			<@renderNls nlsKey="label.searchresults.tableheading.contactinfo"/>
		</th>
	</tr><tr class="lotusFirstRow">

</#macro>

<#macro renderSection sectionLabel>
	<#local shouldRender=((section)?? && section=sectionLabel) || ((request.query.section)?? && request.query.section=sectionLabel)/>
	<#if shouldRender>
		<#nested>
	</#if>
</#macro>

<#-- Renders field label for the property 
	 ref: the id reference of the property to render per the profile type definition
	 editMode: flag on whether to render ui
	 nlsKey: the key into the resource bundle that contains the property label
	 nlsBundle: the bundle identifier that contains the property label (optional)   
	 renderLabelAs: if set to "select" and editMode=true, will create an html select object using the labelOptions
	 labelOptions: if renderLabelAs="select", this array will populate the select options
-->	
<#macro renderLabel ref="" editMode=false renderAs="" nlsKey="" nlsBundle="template" isDisabled=false isPerson=false isRichText=false renderLabelAs="" labelOptions=[] labelDefault="">
	<#local labelValue=labelDefault/>
	<#local isLabelEditable=false/>
	
	<#if profileType[ref].isExtension>
		<#if profileType[ref].labelUpdatability == "readwrite">
			<#local isLabelEditable=true/>
		</#if>
		<#if (profile.extension[ref + ".label"])?has_content>
			<#local labelValue=profile.extension[ref + ".label"]/>	
		<#elseif (profileType[ref].label)?has_content>
			<#local labelValue=profileType[ref].label/>
		</#if>			
	</#if>
	
	<#if !labelValue?has_content>
		<#local labelValue><@renderNls nlsKey=nlsKey nlsBundle=nlsBundle ref=ref/></#local>
	</#if>
	
	<#local labelDisplay><@renderNls nlsKey=labelValue nlsBundle=nlsBundle ref=ref/></#local>
	
	<#if editMode>
		<#local forValue>${ref}</#local>
		<#if !isDisabled>
			<#if isPerson>
				<#local forValue>${ref}.displayName</#local>
			<#elseif isRichText>
				<#local forValue>${ref}.uilabel</#local>
			</#if>
			
			<#if isLabelEditable>
				<#local editlabeltext = bundleResource(nls["template"],"label.editableLabel",[labelDisplay]) />
				<label for="${forValue}" style="display:none;">${labelDisplay}</label><label for="${ref}.label" style="display:none;">${editlabeltext}</label>
				<#if renderLabelAs== "select">
					<select id="${ref}.label" onchange="dataChange(this);" name="attribute(${ref}.label)">
						<#list labelOptions as option>
							<#local optionValue=option/>
							
							<#local optionSelected=""/>
							<#if labelValue == optionValue><#local optionSelected>selected="selected"</#local></#if>
							
							<#-- if this option is listed as the default, we want to blank out the value
							     since we don't want the label stored if it's the default.
							-->
							<#if optionValue == labelDefault><#local optionValue=""/></#if>
							
							<#local optionDisplay><@renderNls nlsKey=option nlsBundle=nlsBundle/></#local>
							<option ${optionSelected} value="${optionValue}">${optionDisplay?trim}</option>

						</#list>					
					</select>
				<#else>
					<input id="${ref}.label" value="${labelDisplay?trim}" onchange="dataChange(this);" onkeypress="dataChange(this);" name="attribute(${ref}.label)"/>
				</#if>
			<#else>
				<#if isRichText>
					<label><span id="${forValue}">${labelDisplay}</span></label>
				<#else>
					<label for="${forValue}">${labelDisplay}</label>
				</#if>
			</#if>		
			
		<#else>
			<label>${labelDisplay}</label>
		</#if>
	<#else>
		${labelDisplay}
	</#if>
	
</#macro>

<#-- Conditionally renders macro nested content if the property on the profile's type exists 
	 ref: the id reference of the property to render per the profile type definition
	 dataId: the id of the data container that should be used render the property value (optional - must be specified with dataKey)
	 dataKey: the key into the data container should be used to render the property value (optional - must be specified with dataKey)
	 nlsKey: the key into the resource bundle that contains the property label
	 nlsBundle: the bundle identifier that contains the property label (optional)   
	 hideIfEmpty: only render the property if it has a non-empty value, if a dataId/dataKey is provided, only render the property if that has a value (optional)
	 isMultiline: flag to control rendering a simple text input versus text area (optional, default is simple input)
-->		
<#macro renderProperty ref="key" nlsKey="" dataId="" dataKey="" nlsBundle="template" hideIfEmpty=false allowAdminEdit=false renderAs="" hideLabel=false isMultiline=false>
	<#if (profileType[ref])?has_content>		
		<#local doShow=false/>
		<#if !hideIfEmpty>
			<#local doShow=true/>
		<#elseif 
				(dataId != "" && dataKey != "" && (data[dataId][dataKey])?has_content) || 
				(profileType[ref].isExtension && (profile.extension[ref])?has_content) ||
				((profile[ref])?has_content && dataKey = "")
		>
			<#local doShow=true/>
		</#if>		
		<#if doShow>
			<#local nestValue><#nested ref, dataId, dataKey, nlsKey, nlsBundle></#local>
			<#if nestValue?has_content>
				${nestValue}
			<#else>
				<#local value><@renderValue ref=ref dataId=dataId dataKey=dataKey /></#local>
				<#local value>${value?trim}</#local>
				<#local label=""/>				
				<#if !hideLabel>
					<#local label><@renderLabel ref=ref nlsKey=nlsKey nlsBundle=nlsBundle /></#local>
					<#local label>${label?trim}</#local>
				</#if>
				<#if isMultiline>
					<#local value>${value?replace("\n", "<br/>")}</#local>
				</#if>
				<tr>
					<th scope="row">${label}</th>
					<td><p>${value}</p></td>
				</tr>
			</#if>
		</#if>
	</#if>	
</#macro>

<#-- Conditionally renders macro nested content if the property on the profile's type exists 
	 If it doesn't exist, returns <br/>
	 ref: the id reference of the property to render per the profile type definition
	 dataId: the id of the data container that should be used render the property value (optional - must be specified with dataKey)
	 dataKey: the key into the data container should be used to render the property value (optional - must be specified with dataKey)
	 nlsKey: the key into the resource bundle that contains the property label
	 nlsBundle: the bundle identifier that contains the property label (optional)   
	 hideIfEmpty: only render the property if it has a non-empty value, if a dataId/dataKey is provided, only render the property if that has a value (optional)
	 isMultiline: flag to control rendering a simple text input versus text area (optional, default is simple input)
-->		
<#macro renderPropertyOrEmptyRow ref="key" nlsKey="" dataId="" dataKey="" nlsBundle="template" hideIfEmpty=false allowAdminEdit=false renderAs="" hideLabel=false isMultiline=false>
	<#if (profileType[ref])?has_content>		
		<@renderProperty ref=ref dataId=dataId dataKey=dataKey nlsBundle=nlsBundle hideIfEmpty=hideIfEmpty allowAdminEdit=allowAdminEdit renderAs=renderAs hideLabel=hideLabel isMultiline=isMultiline/>
	<#else>
		<br/>
	</#if>	
	
</#macro>

<#-- Render the value of the property 
	 ref: the id reference of the property to render per the profile type definition
	 dataId: the id of the data container that should be used render the property value (optional - must be specified with dataKey)
	 dataKey: the key into the data container should be used to render the property value (optional - must be specified with dataKey)
	 renderAs: specify how to render the property (optional - "hcard", "email", "blogUrl", "link", "photo")
	 userId: used when rendering hcard to specify the external id of the user (optional)
	 userEmail: used when rendering hcard to specify the email of the user (optional)   
-->
<#macro renderValue ref="key" dataId="" dataKey="" renderAs="" userId="" userEmail="" className="">
	<#local isRichText=false/><#if (profileType[ref])?has_content><#local isRichText=profileType[ref].isRichText/></#if>
	<#local value=""/>
	<#if dataId != "" && dataKey != "" && (data[dataId][dataKey])??>
		<#local value=data[dataId][dataKey]/>
	<#elseif (profileType[ref])?has_content>
		<#if profileType[ref].isExtension>
			<#if (profile.extension[ref])?has_content>
				<#local value=profile.extension[ref]/>
			</#if>			
		<#else>
			<#if (profile[ref])?has_content>				
				<#local value=profile[ref]/>
			</#if>			
		</#if>					
	</#if>
	<#if value?has_content>
		<#if renderAs = "photo">
			<@renderPhoto/>
		<#elseif renderAs = "hcard">
			<@renderHCard userName=value userId=userId userEmail=userEmail className=className/>		
		<#elseif renderAs = "email">
			<a href="mailto:${value}" class="bidiAware">${value}</a>		
		<#elseif renderAs = "blogUrl">
			<#if !((value?lower_case)?starts_with("http"))>				
				<#local value="http://" + value/>
			</#if>
			<#local params=[]/>
			<#local bundleString = bundleResource(nls.template, "label.blogUrl.info", params)/>		
			<@renderLink url=value message=bundleString/>
		<#elseif renderAs = "link">
			<#local params=[]/>
			<#local bundleString = bundleResource(nls.template, "label.link.info", params)/>
			<@renderLink url=value message=bundleString/>
		<#else>		
			<#-- RTC 134572 - Migrated data that has line feeds need to show html BR tags instead -->
			<#if isRichText>
				<#local value=replaceLineBreaksWithTags(value)/>
			</#if>
			${value}
		</#if>
	</#if>		
</#macro>


<#-- Render the value of the property or insert blank row <br/> if value doesn't exist
	 ref: the id reference of the property to render per the profile type definition
	 dataId: the id of the data container that should be used render the property value (optional - must be specified with dataKey)
	 dataKey: the key into the data container should be used to render the property value (optional - must be specified with dataKey)
	 renderAs: specify how to render the property (optional - "hcard", "email", "blogUrl", "link", "photo")
	 userId: used when rendering hcard to specify the external id of the user (optional)
	 userEmail: used when rendering hcard to specify the email of the user (optional)   
-->
<#macro renderValueOrEmptyRow ref="key" dataId="" dataKey="" renderAs="" userId="" userEmail="" className="">
	
	<#local isRichText=false/><#if (profileType[ref])?has_content><#local isRichText=profileType[ref].isRichText/></#if>
	<#local value=""/>

	<#if dataId != "" && dataKey != "" && (data[dataId][dataKey])??>
		<#local value=data[dataId][dataKey]/>
	<#elseif (profileType[ref])?has_content>
		<#if profileType[ref].isExtension>
			<#if (profile.extension[ref])?has_content>
				<#local value=profile.extension[ref]/>
			</#if>			
		<#else>
			<#if (profile[ref])?has_content>				
				<#local value=profile[ref]/>
			</#if>			
		</#if>					
	</#if>
	<#if value?has_content>
		<#if renderAs = "photo">
			<@renderPhoto/>
		<#elseif renderAs = "hcard">
			<@renderHCard userName=value userId=userId userEmail=userEmail className=className/>		
		<#elseif renderAs = "email">
			<a href="mailto:${value}" class="bidiAware ${className}">${value}</a>		
		<#elseif renderAs = "blogUrl">
			<#if !((value?lower_case)?starts_with("http"))>				
				<#local value="http://" + value/>
			</#if>
			<#local params=[]/>
			<#local bundleString = bundleResource(nls.template, "label.blogUrl.info", params)/>		
			<@renderLink url=value message=bundleString/>
		<#elseif renderAs = "link">
			<#local params=[]/>
			<#local bundleString = bundleResource(nls.template, "label.link.info", params)/>
			<@renderLink url=value message=bundleString/>
		<#else>		
			<#-- RTC 134572 - Migrated data that has line feeds need to show html BR tags instead -->
			<#if isRichText>
				<#local value=replaceLineBreaksWithTags(value)/>
			</#if>
			${value}

		</#if>
	
	<#else>
		<br/>
	</#if>	

</#macro>

<#-- To render a localized resource string 
	 bundleId: the id of the bundle that contains the label (optional, defaults to "template")
	 labelKey: the key for the label	 
-->
<#macro renderNls nlsKey="" ref="" nlsBundle="template" params...>
	<#if !(nlsKey?has_content) && (ref?has_content)><#local nlsKey>label.${ref}</#local></#if>
	<#local bundleString = bundleResource(nls[nlsBundle],nlsKey,params) />
	<#if bundleString??>
		${bundleString} 
	<#else>
		${nlsKey}		
	</#if>
</#macro>

<#-- To render a form attribute in an edit profile form
	 ref: the id reference of the property to render per the profile type definition
	 dataId: the id of the data container that should be used render the property value (optional - must be specified with dataKey)
	 dataKey: the key into the data container should be used to render the property value (optional - must be specified with dataKey)
	 nlsBundle: the resource bundle that contains the label (optional)
	 isMultiline: flag to control rendering a simple text input versus text area (optional, default is simple input)
	 isDisabled: flag to control rendering an input field, or just a simple read-only view (optional, default false)	 
	 isPerson: flag to control rendering a person picker (optional, default false)
	 singleColumnLayout: flag to control rendering as single column versus two column layout (optional, default is false)
	 isSelect: flag to control rendering a select field
	 options: a sequence of hashes where option.label is the display value, and option.value is the actual value for use in a select control
	 renderLabelAs: if set to "select", will create an html select object using the labelOptions
	 labelOptions: if renderLabelAs="select", this array will populate the select options
	 isBidiTextDir: if set to  "true", bidi text direction support should be applied on the field (optional, default false)
-->
<#macro renderFormControl ref nlsKey="" dataId="" dataKey="" nlsBundle="template" options=[] isMultiline=false isDisabled=false isPerson=false singleColumnLayout=false isSelect=false renderLabelAs="" isBidiTextDir=false labelOptions=[] labelDefault="">		
	<#local value=""/>
	<#local isRichText=false/>
	<#-- ensure this is a valid field to render (i.e. its on the profile type), if not we exit -->			
	<#if (profileType[ref])?has_content>
		<#local isRichText=profileType[ref].isRichText/>
		<#if dataId != "" && dataKey != "" && (data[dataId][dataKey])??>
			<#local value=data[dataId][dataKey]/>
		<#elseif profileType[ref].isExtension>
			<#if (profile.extension[ref])?has_content>
				<#local value=profile.extension[ref]/>
			</#if>			
		<#else>
			<#if (profile[ref])?has_content>				
				<#local value=profile[ref]/>
			</#if>			
		</#if>
		<#-- if the field does not support editing, we will render the control as a readonly value -->
		<#if profileType[ref].updatability != "readwrite">
			<#local isDisabled=true/>
		</#if>					
	<#else>
		<#return/>		
	</#if>	
	<#local label><@renderLabel 
		ref=ref 
		editMode=true 
		labelDefault=labelDefault 
		nlsKey=nlsKey nlsBundle=nlsBundle 
		isDisabled=isDisabled isPerson=isPerson isRichText=isRichText 
		renderLabelAs=renderLabelAs labelOptions=labelOptions 
	/></#local>
	<#-- we have a valid field so render it -->
	<#if singleColumnLayout>
		<tr class="lotusFormFieldRow">
			<td>${label}</td>
		</tr>
		<tr class="lotusFormFieldRow">
	<#else>
		<tr class="lotusFormFieldRow">
			<td class="lotusFormLabel">${label}</td>		
	</#if>
		<td>
			<#if !isDisabled>
				<#if isRichText>
					<@renderRichTextControl ref=ref value=value/>
				<#elseif isMultiline>
					<textarea class="lotusText<#if isBidiTextDir> bidiAware </#if>" style="resize: none;" id="${ref}" rows="6" onchange="dataChange(this);" onkeypress="dataChange(this);" name="attribute(${ref})">${value}</textarea>
				<#elseif isPerson>
					<#local displayValue=value/>
					<#local internalValue=""/>
					<#if profileType[ref].isExtension>
						<#if (profile.extension[ref])?has_content>
							<#local internalValue=profile.extension[ref]/>
						</#if>
					<#else>
						<#if (profile[ref])?has_content>
							<#local internalValue=profile[ref]/>
						</#if>
					</#if>
					<@renderTypeAheadControl ref=ref displayValue=value internalValue=internalValue/>
				<#elseif isSelect>
					<select id="${ref}" name="attribute(${ref})" onchange="dataChange(this);">
						<#list options as option>
							<#if option.value == value>
								<option selected="selected" value="${option.value}">${option.label}</option>
							<#else>
								<option value="${option.value}">${option.label}</option>
							</#if>
						</#list>
					</select>
				<#else>
					<input value="${value}" type="text" class="lotusText<#if isBidiTextDir> bidiAware </#if>" id="${ref}" onchange="dataChange(this);" onkeypress="dataChange(this);" name="attribute(${ref})"/>
				</#if>
			<#else>
				<div id="${ref}_ro" class="disabled<#if isBidiTextDir> bidiAware </#if>">${value}</div>							
			</#if>									
		</td>
	</tr>

</#macro>

<#macro renderBidiEnforce>
	<script type="text/javascript">
		dojo.addOnLoad( function(){
			lconn.core.globalization.bidiUtil.enforceTextDirectionOnPage();
		});
	</script>
</#macro>

<#macro renderRichTextControl ref value>
	<#-- RTC 134572 - Migrated data that has line feeds need to show html BR tags instead -->
	<#local value=replaceLineBreaksWithTags(value)/>
	<div id="${ref}_RTE" class="rte" aria-labelledby="${ref}.uilabel">
		<div id="${ref}_RTE_loading" class="lotusSection">
			<img src="${request.contextPath}/nav/common/styles/images/loading.gif" alt="<@renderNls nlsKey="label.loading"/>"/>			
		</div>
		<div id="${ref}" class="_ckeditorvalue lotusHidden">${value}</div>
		<input type="hidden" name="attribute(${ref})"/>
		<script type="text/javascript">editPageEditorsIds.push("${ref}");</script>		
	</div>			
</#macro>

<#macro renderTypeAheadControl ref displayValue internalValue>	
	<div class="nameTypeaheadControl">
		<input type="text" value="${displayValue}" id="${ref}.displayName" onchange="dataChange(this);" onkeypress="dataChange(this);"/>
		<input type="hidden" id="${ref}" name="attribute(${ref})" value="${internalValue}"/>						
	</div>

	<script type="text/javascript">
		dojo.addOnLoad(
		    function() {
				// create data store widget programmatically
		    	var peopleTypeAheadStore = new lconn.core.PeopleDataStore({
		    		jsId:"peopleTypeAheadStore",
		    		queryParam:"name",
		    		url:"${request.contextPath}/html/nameTypeahead.do"
		    	});
				
				// get the text field references before creating the widget so we can pull the original value
				var textField = dojo.byId("${ref}.displayName");
				var valueField = dojo.byId("${ref}");

				// create the typeahead widget
			    var peopleTypeAhead = new lconn.core.PeopleTypeAhead( 
					{
						minChars:3,
						store:peopleTypeAheadStore,
						searchDelay:400,
						hasDownArrow:false,
						autocomplete:false,
						multipleValues:false
					}, 
					textField
				);
				
				// we need to now set the values in the new widget and set the correct style
				if (textField.value.length > 0) {
					peopleTypeAhead.focusNode.value = textField.value;
					peopleTypeAhead.focusNode.hasInput = true;
					peopleTypeAhead.focusNode.style.color = "#000";
				}
				dojo.attr(peopleTypeAhead.focusNode, "role", "combobox");
				peopleTypeAhead.updateHintText();
				peopleTypeAhead.focusNode.style.display = "inline";
								
				// when the field loses focus, we'll determine if anything changed.
				dojo.connect(
					peopleTypeAhead, 
					"onBlur",
					function() {
						if (valueField.value != this.focusNode.value) {
							if (peopleTypeAhead.itemSelected_) {
								dojo.byId("${ref}").value = peopleTypeAhead.itemSelected_.uid;
							} else {
								dojo.byId("${ref}").value = this.focusNode.value;
							}
							dataChange(this.focusNode);
						}
						this.updateHintText();						
					}
				);
				
				// when the user selects an item, we just want the name.
			    dojo.connect( 
				    peopleTypeAhead, 
				    "onSelect", 
			   		function(item) {
			   			if(!item) return false;
						peopleTypeAhead.itemSelected_ = item;
						if (item.name) this.focusNode.value = item.name;
						dataChange(this.focusNode);
					}
				);		
			}
		);
	</script>
<script>
(function () {
	// If no timezone is selected by the user, set browser's timezone as default selection on profile edit 
	// If the browser timezone is not available in the provided options set defualt selection to empty
	document.addEventListener('DOMContentLoaded', function(event) {
		if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()) {
			var currentTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
			var selectElem = document.getElementById("tableEditProfileContactInfo");
			if(selectElem) {
				selectElem = selectElem.getElementsByTagName("select")[0];
				if(!selectElem.options[selectElem.selectedIndex]) {
					var getOptions = selectElem.options;
					var newOption = document.createElement("option");
					for (i = 0; i < getOptions.length; i++) {
						if(currentTimezone == getOptions[i].value) {
							newOption.text = getOptions[i].text;
							newOption.value = currentTimezone;
							break;
						} else {
							newOption.text = "Select";
							newOption.value = "";
						}
					}
					selectElem.add(newOption, selectElem[0]);
					selectElem.selectedIndex= 0;
				}
			}
		}
	})
} ());
</script>
</#macro>

<#-- Returns true if the viewer has a connection with the rendered profile
		This function will always return false when not rendering the profileDetails.ftl and businessCardInfo.ftl templates
-->
<#function hasConnection>	
	<#return (connection?has_content) && connection.status="accepted"/>
</#function>

<#-- Returns true if the viewer is viewing their own profile
-->
<#function isMe>
	<#return currentUser.authenticated && currentUser.key = profile.key/>
</#function>

<#-- Returns true if the viewer is authenticted
-->
<#function isAuthenticated>
	<#return currentUser.authenticated />
</#function>

<#-- Returns a sequence of hashes that map the timezone.label and timezone.value -->
<#function availableTimezones>
	<#local params=[]/>	
	<#return
		[
		{ "label": bundleResource(nls.template, "Etc/GMT+12", params), "value":"Etc/GMT+12" },
		{ "label": bundleResource(nls.template, "Pacific/Pago_Pago", params), "value":"Pacific/Pago_Pago" },
		{ "label": bundleResource(nls.template, "Pacific/Honolulu", params), "value":"Pacific/Honolulu" },
		{ "label": bundleResource(nls.template, "America/Anchorage", params), "value":"America/Anchorage" },
		{ "label": bundleResource(nls.template, "America/Los_Angeles", params), "value":"America/Los_Angeles" },
		{ "label": bundleResource(nls.template, "America/Tijuana", params), "value":"America/Tijuana" },
		{ "label": bundleResource(nls.template, "America/Phoenix", params), "value":"America/Phoenix" },
		{ "label": bundleResource(nls.template, "America/Chihuahua", params), "value":"America/Chihuahua" },
		{ "label": bundleResource(nls.template, "America/Denver", params), "value":"America/Denver" },
		{ "label": bundleResource(nls.template, "America/Guatemala", params), "value":"America/Guatemala" },
		{ "label": bundleResource(nls.template, "America/Chicago", params), "value":"America/Chicago" },
		{ "label": bundleResource(nls.template, "America/Mexico_City", params), "value":"America/Mexico_City" },
		{ "label": bundleResource(nls.template, "America/Regina", params), "value":"America/Regina" },
		{ "label": bundleResource(nls.template, "America/Lima", params), "value":"America/Lima" },
		{ "label": bundleResource(nls.template, "America/New_York", params), "value":"America/New_York" },
		{ "label": bundleResource(nls.template, "America/Indianapolis", params), "value":"America/Indianapolis" },
		{ "label": bundleResource(nls.template, "America/Halifax", params), "value":"America/Halifax" },
		{ "label": bundleResource(nls.template, "America/La_Paz", params), "value":"America/La_Paz" },
		{ "label": bundleResource(nls.template, "America/Manaus", params), "value":"America/Manaus" },
		{ "label": bundleResource(nls.template, "America/Santiago", params), "value":"America/Santiago" },
		{ "label": bundleResource(nls.template, "America/St_Johns", params), "value":"America/St_Johns" },
		{ "label": bundleResource(nls.template, "America/Sao_Paulo", params), "value":"America/Sao_Paulo" },
		{ "label": bundleResource(nls.template, "America/Buenos_Aires", params), "value":"America/Buenos_Aires" },
		{ "label": bundleResource(nls.template, "America/Godthab", params), "value":"America/Godthab" },
		{ "label": bundleResource(nls.template, "America/Montevideo", params), "value":"America/Montevideo" },
		{ "label": bundleResource(nls.template, "Atlantic/South_Georgia", params), "value":"Atlantic/South_Georgia" },
		{ "label": bundleResource(nls.template, "Atlantic/Azores", params), "value":"Atlantic/Azores" },
		{ "label": bundleResource(nls.template, "Atlantic/Cape_Verde", params), "value":"Atlantic/Cape_Verde" },
		{ "label": bundleResource(nls.template, "Africa/Casablanca", params), "value":"Africa/Casablanca" },
		{ "label": bundleResource(nls.template, "Europe/London", params), "value":"Europe/London" },
		{ "label": bundleResource(nls.template, "Europe/Amsterdam", params), "value":"Europe/Amsterdam" },
		{ "label": bundleResource(nls.template, "Europe/Belgrade", params), "value":"Europe/Belgrade" },
		{ "label": bundleResource(nls.template, "Europe/Brussels", params), "value":"Europe/Brussels" },
		{ "label": bundleResource(nls.template, "Europe/Warsaw", params), "value":"Europe/Warsaw" },
		{ "label": bundleResource(nls.template, "Africa/Lagos", params), "value":"Africa/Lagos" },
		{ "label": bundleResource(nls.template, "Asia/Amman", params), "value":"Asia/Amman" },
		{ "label": bundleResource(nls.template, "Europe/Athens", params), "value":"Europe/Athens" },
		{ "label": bundleResource(nls.template, "Asia/Beirut", params), "value":"Asia/Beirut" },
		{ "label": bundleResource(nls.template, "Africa/Cairo", params), "value":"Africa/Cairo" },
		{ "label": bundleResource(nls.template, "Africa/Harare", params), "value":"Africa/Harare" },
		{ "label": bundleResource(nls.template, "Europe/Helsinki", params), "value":"Europe/Helsinki" },
		{ "label": bundleResource(nls.template, "Asia/Jerusalem", params), "value":"Asia/Jerusalem" },
		{ "label": bundleResource(nls.template, "Europe/Minsk", params), "value":"Europe/Minsk" },
		{ "label": bundleResource(nls.template, "Africa/Windhoek", params), "value":"Africa/Windhoek" },
		{ "label": bundleResource(nls.template, "Asia/Baghdad", params), "value":"Asia/Baghdad" },
		{ "label": bundleResource(nls.template, "Asia/Kuwait", params), "value":"Asia/Kuwait" },
		{ "label": bundleResource(nls.template, "Europe/Moscow", params), "value":"Europe/Moscow" },
		{ "label": bundleResource(nls.template, "Africa/Nairobi", params), "value":"Africa/Nairobi" },
		{ "label": bundleResource(nls.template, "Asia/Tbilisi", params), "value":"Asia/Tbilisi" },
		{ "label": bundleResource(nls.template, "Asia/Tehran", params), "value":"Asia/Tehran" },
		{ "label": bundleResource(nls.template, "Asia/Muscat", params), "value":"Asia/Muscat" },
		{ "label": bundleResource(nls.template, "Asia/Baku", params), "value":"Asia/Baku" },
		{ "label": bundleResource(nls.template, "Asia/Yerevan", params), "value":"Asia/Yerevan" },
		{ "label": bundleResource(nls.template, "Asia/Kabul", params), "value":"Asia/Kabul" },
		{ "label": bundleResource(nls.template, "Asia/Yekaterinburg", params), "value":"Asia/Yekaterinburg" },
		{ "label": bundleResource(nls.template, "Asia/Karachi", params), "value":"Asia/Karachi" },
		{ "label": bundleResource(nls.template, "Asia/Calcutta", params), "value":"Asia/Calcutta" },
		{ "label": bundleResource(nls.template, "Asia/Colombo", params), "value":"Asia/Colombo" },
		{ "label": bundleResource(nls.template, "Asia/Katmandu", params), "value":"Asia/Katmandu" },
		{ "label": bundleResource(nls.template, "Asia/Almaty", params), "value":"Asia/Almaty" },
		{ "label": bundleResource(nls.template, "Asia/Dhaka", params), "value":"Asia/Dhaka" },
		{ "label": bundleResource(nls.template, "Asia/Rangoon", params), "value":"Asia/Rangoon" },
		{ "label": bundleResource(nls.template, "Asia/Bangkok", params), "value":"Asia/Bangkok" },
		{ "label": bundleResource(nls.template, "Asia/Krasnoyarsk", params), "value":"Asia/Krasnoyarsk" },
		{ "label": bundleResource(nls.template, "Asia/Hong_Kong", params), "value":"Asia/Hong_Kong" },
		{ "label": bundleResource(nls.template, "Asia/Irkutsk", params), "value":"Asia/Irkutsk" },
		{ "label": bundleResource(nls.template, "Asia/Singapore", params), "value":"Asia/Singapore" },
		{ "label": bundleResource(nls.template, "Australia/Perth", params), "value":"Australia/Perth" },
		{ "label": bundleResource(nls.template, "Asia/Taipei", params), "value":"Asia/Taipei" },
		{ "label": bundleResource(nls.template, "Asia/Tokyo", params), "value":"Asia/Tokyo" },
		{ "label": bundleResource(nls.template, "Asia/Seoul", params), "value":"Asia/Seoul" },
		{ "label": bundleResource(nls.template, "Asia/Yakutsk", params), "value":"Asia/Yakutsk" },
		{ "label": bundleResource(nls.template, "Australia/Adelaide", params), "value":"Australia/Adelaide" },
		{ "label": bundleResource(nls.template, "Australia/Darwin", params), "value":"Australia/Darwin" },
		{ "label": bundleResource(nls.template, "Australia/Brisbane", params), "value":"Australia/Brisbane" },
		{ "label": bundleResource(nls.template, "Australia/Sydney", params), "value":"Australia/Sydney" },
		{ "label": bundleResource(nls.template, "Pacific/Guam", params), "value":"Pacific/Guam" },
		{ "label": bundleResource(nls.template, "Australia/Hobart", params), "value":"Australia/Hobart" },
		{ "label": bundleResource(nls.template, "Asia/Vladivostok", params), "value":"Asia/Vladivostok" },
		{ "label": bundleResource(nls.template, "Pacific/Noumea", params), "value":"Pacific/Noumea" },
		{ "label": bundleResource(nls.template, "Pacific/Auckland", params), "value":"Pacific/Auckland" },
		{ "label": bundleResource(nls.template, "Pacific/Fiji", params), "value":"Pacific/Fiji" },
		{ "label": bundleResource(nls.template, "Pacific/Tongatapu", params), "value":"Pacific/Tongatapu" }		
		]
	/>
</#function>
