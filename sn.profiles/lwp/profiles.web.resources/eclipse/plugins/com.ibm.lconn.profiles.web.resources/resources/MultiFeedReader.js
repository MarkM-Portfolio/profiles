/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.MultiFeedReader");

dojo.require("lconn.profiles.ProfilesCore");
dojo.require("lconn.profiles.ProfilesXSL");
dojo.require("lconn.core.aria.TabPanel");

(function() {

	var services_ = [];
		
	lconn.profiles.MultiFeedReader.loadWidgetContent = function(iContext)
	{	
		var _serviceEnabled = function(feedUrl)
		{
			var temp = feedUrl.indexOf("{");
			var temp2 = feedUrl.indexOf("SvcRef}");
			
			var serviceName = null;
			if(temp != -1 && temp2 != -1)
			{
				serviceName = feedUrl.substring(temp+1,temp2);
				if(WidgetPlacementConfig.availableServices[serviceName] == true)
					return true;
				else
					return false;
			}
			
			return true;
		}
		
		var _switchTab = function(svcName)
		{
			var currService = null;
			for (var i = 0; i < services_.length; i++)	{
				lconn.profiles.ProfilesCore.hide(services_[i].name + "FeedContentContainer");
				dojo.removeClass(dojo.byId(services_[i].name + "MultiFeedReaderTab"), "lotusSelected");
				if (services_[i].name == svcName) {
					currService = services_[i];
				}
			}
			
			lconn.profiles.ProfilesCore.show(svcName + 'FeedContentContainer');

			var tab = dojo.byId(svcName + "MultiFeedReaderTab");
			dojo.addClass(tab, "lotusSelected");
			if (tab && tab.getAttribute("loaded") != "true")	{
				_loadFeedContent(currService);
				tab.setAttribute("loaded", "true");
			}
		}
		
		var _AddTab = function(svcName, resourceBundId, isSelected, isFirst)
		{

			var multiFeedReaderLotusTabs = document.getElementById("multiFeedReaderLotusTabs");
			var li = document.createElement("li");
			li.id = svcName + "MultiFeedReaderTab";
			if( isFirst ) li.className = "lotusFirst "; // this is needed for lotusInlinelist
			if(isSelected)
			{
				li.className += "lotusSelected";
				li.setAttribute("loaded","true");
			}

			var stringT = generalrs[resourceBundId];
			if (stringT.indexOf("{0}") > -1 && dojo.exists("profilesData.displayedUser.displayName")) {
				stringT = stringT.replace("{0}", profilesData.displayedUser.displayName);
			}
			var a = document.createElement("a");
			a.id="a"+li.id;
			a.href="javascript:void(0);";
			a.onclick = function(){
				_switchTab(svcName);
			};
			_tempLog("stringT: " + stringT);
			a.appendChild(document.createTextNode(stringT));
			li.appendChild(a);
			multiFeedReaderLotusTabs.appendChild(li);
			
			// Setup aria helper 		
			dojo.attr(dojo.byId(a.id), {
					role: "tab"
				});
		}
		
		var _AddFeedContainer = function(svcName, showDivElem)
		{
			_tempLog("svcName: " + svcName);
			var multiFeedReaderContentContainer = document.getElementById("multiFeedReaderContentContainer");
			var div = document.createElement("div");
			div.id = svcName + "FeedContentContainer";
			if(!showDivElem)
			{
				div.style.display="none";
				div.style.visibility="hidden";
			}
			var stringT = generalrs.widgetLoading;
			div.appendChild(document.createTextNode(stringT));
			multiFeedReaderContentContainer.appendChild(div);
		};

		var _loadFeedContent = function(service)
		{
			var feedUrl = service.url;
			var alternateUrl = service.alternateUrl || "";
			var containerId = service.name + "FeedContentContainer";
			
			//var temp = new com.ibm.enabler.utilities.HttpUrl(dataUrl);
			//var dataUrl = temp.toProxifiedString();
			var dataUrl = lconn.profiles.ProfilesCore.getProxifiedURL(feedUrl);
			var resourceIds = ["multiFeedReaderSeeAllFeeds","multiFeedReaderNoFeeds","multiFeedReaderUpdatedBy","multiFeedReaderCreatedBy"];
			var params = new Array;
			params.push(["containerId", containerId]);
			params.push(["alternateUrl", alternateUrl]);
			params.push(["blankGif", (dojo.config.blankGif || dojo.moduleUrl("dojo", "resources/blank.gif")).toString()]);

			var data = { dataObj: null, noContent: false };
			
			var callback_ = function() {

				if (data.noContent) {
					dojo.byId(containerId).innerHTML = generalrs.multiFeedReaderNoFeeds;
				} else /*if (!data.noContent)*/ {		
					lconn.profiles.ProfilesXSL.loadContentObj( data.dataObj, "feedreader.xsl", containerId, resourceIds, params);
					
					var tempskdfd = function()
					{
						if (dojo.registerModulePath) dojo.registerModulePath("lconn.core",applicationContext+"/js_build/lconn/core");
						dojo.requireLocalization("dojo.cldr","gregorian");
						dojo.requireLocalization("lconn.core", "strings");
						
						var doifdofidfuoidu = containerId+"FeedTableContainer";
						var tempdod = document.getElementById(doifdofidfuoidu);
						var tempList = tempdod.getElementsByTagName("span");
						for (var i = 0; tempList != null && i < tempList.length; i++)
						{
							var currentTdElem = tempList[i];
							if(currentTdElem.getAttribute("lcNodeType") == "AtomFeedDate")
							{
								try
								{
									var tempsoidfd = lconn.core.DateUtil.AtomDateToString(currentTdElem.innerHTML);
									currentTdElem.innerHTML = tempsoidfd;
								}
								catch(exception)
								{
									//TODO handle this case		  			
								}
								
							}
						}
						profiles_AddLiveNameSupport(doifdofidfuoidu);	
					};
					processUntilElementIsFound(containerId+"FeedTableContainer", tempskdfd, null, null, false);
				}			
			};
			
			// Do explicit Get to catch error in dojo
			lconn.profiles.xhrGet({sync: false, url: dataUrl, handleAs: "text", expectedContentType: "xml", 
				load: function(response, ioArgs) {
					data.dataObj = lconn.core.xslt.loadXmlString(response);
					callback_();
				},
				error: function(response, ioArgs) {
					if( ioArgs.xhr.status == 404 ) {
						data.noContent = true;
					} else if ( ioArgs.xhr.status == 400 && ioArgs.args.url.indexOf("service/atom/communities") != -1) { // inactive user that never went to communities? SPR#DABS88NHJK
						data.noContent = true;
					} else { 
						lconn.profiles.ProfilesCore.DefaultXHRErrorHandler(response, ioArgs);
					}
					callback_();
				}
			});
			
			
		};
		
		var _tempLog = function(stringContent)
		{
			if(window.debugWidgets) console.log(stringContent);
			//var multiFeedReaderContentContainer = document.getElementById("multiFeedReaderContentContainer");
			//multiFeedReaderContentContainer.appendChild(document.createTextNode(stringContent));
			//multiFeedReaderContentContainer.appendChild(document.createElement("br"));
		};

		var _AddServiceListText = function(elUL,text)
		{
			var multiFeedReaderLotusTabs = document.getElementById(elUL);
			if( multiFeedReaderLotusTabs ) { 
				var li = document.createElement("li");
				li.className = "lotusFirst";
				li.appendChild(document.createTextNode(text));
				multiFeedReaderLotusTabs.appendChild(li);
			}
		};
		/** end of internal function */
		
		
		var attributesItemSet = iContext.getiWidgetAttributes();
		var attrNames = attributesItemSet.getAllNames();
		
		services_ = [];
		
		//first, we need to get a list of the services.
		for (var i = 0; attrNames != null && i < attrNames.length; i++) {
			var currentAttrName = attrNames[i];
			var idx = currentAttrName.indexOf("FeedUrl");
			if (idx != -1) {
				var surl = attributesItemSet.getItemValue(currentAttrName);
				
				//we need to make sure the feed is enabled before we add it to the list
				if (_serviceEnabled(surl)) {
					var sname = currentAttrName.substring(0, idx);
					
					var service = {
						name: sname,
						url: surl,
						resourceId: attributesItemSet.getItemValue(sname + "ResourceId"),
						eval: attributesItemSet.getItemValue(sname + "Eval"),
						alternateUrl: attributesItemSet.getItemValue(sname + "FeedAlternateUrl")
					};

					//added code to do a js Eval as to whether to show the service.
					var yn = true;
					if (service.eval) {
						try {
							yn = false;
							eval("yn = (" + service.eval + ")");
						} catch (e) {
							//eval failed, just drop it.
						}
					}
					
					//go ahead and allow it
					if (yn) {
						services_.push(service);
					}
				}
			}
		}
		
		
		if (services_.length > 0) {
			for (var i = 0; i < services_.length; i++) {
				var isFirst = (i == 0);
				var service = services_[i];
				
				
				if (isFirst) {
					_AddServiceListText("multiFeedReaderLotusTabs",generalrs["multiFeedReaderShow"]);
				}
				
				_AddTab(service.name, service.resourceId, isFirst, isFirst);
				_AddFeedContainer(service.name, isFirst);				

				
			}
			
			//load the first one
			_loadFeedContent(services_[0]);
			
		} else {
		
			//no feeds found
			_AddServiceListText("multiFeedReaderLotusTabs",generalrs["multiFeedReaderNoFeeds"]); 
		}
		
		
		// Setup aria helper for this toolbar above
		if( lconn.core.aria && typeof(lconn.core.aria.TabPanel) == "function")
			new lconn.core.aria.TabPanel( "multiFeedReaderLotusTabs" );   
	}

})();