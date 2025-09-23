/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2015                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.multiWidget");

(function() {
	lconn.profiles.multiWidget = function() {
		// Check cnx8-ui is enable or not for JS
		this.check_ui_enabled = function() {
			if (typeof dojo.cookie('cnx8-ui') == 'undefined') {
			return 0;
			}
			return parseInt(dojo.cookie('cnx8-ui')) == 1;
		}
		// TODO - This whole widget needs to be re-written.  It cannot cleanly handle the 
		// "More actions" link to the right.  Possibly convert to use dojo _templated instead
		// if this embedded html.
		this.onLoad = function() {
			var widgetInstanceId = this.iContext.widgetId;
			var mode = this.iContext.getiDescriptor().getItemValue("mode");
			var widgetViewMode = "normal";

			console.log('cnx8-ui Value = ', this.check_ui_enabled());

			var widgetDataRoot = this.iContext.getRootElement();
			if (widgetDataRoot) {
				var htmlData = '<div id="'+widgetInstanceId+'_widgetId_container" style="clear: both;">'+
					'<div id="lconn_'+widgetInstanceId+'_CenterTabsDiv" class="lotusTabContainer" style="height: auto;">'+   	
						'<ul id="lconn_'+widgetInstanceId+'_CenterTabsUL" class="lotusTabs" role="tablist" style="min-width: 700px;">'+
						'</ul>'+
					'</div>'+
					'<div id="'+widgetInstanceId+'_widgetId_moreActions" class="lotusWidget2" style="z-index: 100; display: none; border-width: 0px;">'+
						'<h2>'+
							'<a id="'+widgetInstanceId+'_widgetId_moreActionsLink" aria-haspopup="true" role="button" href="javascript:void(0);" class="lotusIcon lotusActionMenu">'+
								'<img alt="" src="'+dijit._Widget.prototype._blankGif+'">'+
								'<span class="lotusAltText"></span>'+
							'</a>'+
						'</h2>'+
					'</div>'+
					'<div id="lconn_'+widgetInstanceId+'_CenterTabContent" class="profileContents" tabindex="0">'+
						'<span id="widget-container-'+widgetInstanceId+'" class="widgetContainer"></span>'+
					'</div>'+
				'</div>';
				if(this.check_ui_enabled()) {
					htmlData = '<div id="'+widgetInstanceId+'_widgetId_container" style="clear: both;">'+
						'<div id="lconn_'+widgetInstanceId+'_CenterTabsDiv" style="height: auto;border-bottom:none;">'+   	
							'<ul id="lconn_'+widgetInstanceId+'_CenterTabsUL" class="lotusTabs" role="tablist" style="min-width: 700px;">'+
							'</ul>'+
						'</div>'+
						'<div id="'+widgetInstanceId+'_widgetId_moreActions" class="lotusWidget2" style="z-index: 100; display: none; border-width: 0px;">'+
							'<h2>'+
								'<a id="'+widgetInstanceId+'_widgetId_moreActionsLink" aria-haspopup="true" role="button" href="javascript:void(0);" class="lotusIcon lotusActionMenu">'+
									'<img alt="" src="'+dijit._Widget.prototype._blankGif+'">'+
									'<span class="lotusAltText"></span>'+
								'</a>'+
							'</h2>'+
						'</div>'+
						'<div id="lconn_'+widgetInstanceId+'_CenterTabContent" class="profileContents" style="padding-top: 10px;" tabindex="0">'+
							'<span id="widget-container-'+widgetInstanceId+'" class="widgetContainer"></span>'+
						'</div>'+
					'</div>';
				}
				dojo.create("div", {
					innerHTML: htmlData	
				}, widgetDataRoot);
				

				// reset widget config parameters that may collide with a previous tabs widget
				WidgetPlacementConfig.TabContainerDomId = "lconn_"+widgetInstanceId+"_CenterTabsUL";
				WidgetPlacementConfig.TempWidgetContainerDomId = null;
				WidgetPlacementConfig.firstWidget = null;
				WidgetPlacementConfig.defaultPageId = profilesData.config.pageId;


				var panelWidget = lconn.core.WidgetPlacement.addTabsWithOnclickCalls(
					"lconn_"+widgetInstanceId+"_CenterTabsUL", // TabContainerDomId
					widgetInstanceId, // TempWidgetContainerDomId
					null, // intialDisplayDomId 
					null, // cancelCallBack
					null, // FirstTabItemDomId
					null, // widgetMode 
					[ {moreActionsContainerNode: dojo.byId(widgetInstanceId+'_widgetId_moreActions')} ]  // attributesMap
				);
					
				
				if (panelWidget) {
					dojo.connect(
						panelWidget, 
						"_selectItem", 
						dojo.hitch(
							panelWidget, 
							function() {
								this.allItems[this.selIdx].focus();
							}
						)
					);
				}
		
				// activate the first displayed widget
				if (WidgetPlacementConfig.firstWidget != null) {
					activateTabbedWidget(
							WidgetPlacementConfig.firstWidget.widgetDefinitionNode, 
							WidgetPlacementConfig.firstWidget.widgetInstanceNode,
							"view", 
							null, 
							"lconn_"+widgetInstanceId+"_CenterTabsUL",
							false,
							null);
				}

				this._startResizeMonitor();
			}
		};
		
		
		// Because of the way this widget is put together, we cannot cleanly put the 
		// "More Actions" icon to the right of the tabs.  The only way this'll work without
		// re-writing the whole thing is to get the absolute position of the tab control and 
		// manually place the icon to the right of it. We also need to monitor it's position
		// to account for the user resizing the page or the dynamic loading of content on the
		// page (i.e. Activity Stream)
		this._oldAttr = {};
		this._startResizeMonitor = function() {
			var container = dojo.byId(this.iContext.widgetId+'_widgetId_container');
			var moreActions = dojo.byId(this.iContext.widgetId+'_widgetId_moreActions');
			
			setInterval(dojo.hitch(this, function() {
				try {
					var pos = dojo.marginBox(container);
					
					var attr = {
						display: "inline",
						top: parseInt(pos.t, 10)+"px"
					};

					//if we aren't in high contrast mode
					if (!dojo.hasClass(dojo.body(), "lotusImagesOff")) {  
						attr.position = "absolute";
					}
					
					//deal with bidi...
					if (dojo.isBodyLtr()) {
						attr.left = parseInt(pos.l+pos.w-dojo.position(moreActions).w, 10)+"px";
					} else {
						attr.left = parseInt(pos.l, 10)+"px";
					}
					
					//check to see if this has changed...
					if (dojo.toJson(attr) !== dojo.toJson(this._oldAttr)) {
						dojo.style(moreActions, attr);
						
						//store for future comparison
						this._oldAttr = attr;
					}
					
				} catch (e) {}
			}), 500);
		
		};	
	}

})();