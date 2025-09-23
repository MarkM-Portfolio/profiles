/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.widgets.linkRoll.Item");

dojo.require("lconn.profiles.widgets._Base");
dojo.require("lconn.profiles.widgets.utils");

(function() {

	dojo.declare(
		"lconn.profiles.widgets.linkRoll.Item",
		[lconn.profiles.widgets._Base],
		{
			templatePath: dojo.moduleUrl("lconn.profiles", "widgets/linkRoll/templates/Item.html"),
			
			iconUrl: "{dogearSvcLocation}/favicon?host={host}",
			
			linkName: "",
			linkUrl: "",
			
			canRemoveLinks: false,
			
			//reference to the instance of the lconn.profiles.LinkRoll widget which spawned this one.
			containerWidget: null,

			
			postCreate: function() {

				this.inherited(arguments);
				
				var link = plusDecode_(xmlDecode_(this.linkUrl));

				//set the link name and href values
				dojo.attr(this.linkNode, {
					href: encodeURI(link),
					innerHTML: ""
				});
				dojo.place(document.createTextNode(this.linkName), this.linkNode, "only");
				
				//if dogear is there, use it to render the icon
				if (dojo.exists("WidgetPlacementConfig.availableServices") && dojo.exists("WidgetPlacementConfig.params.dogearSvcRef") && WidgetPlacementConfig.availableServices.dogear) {
					var host = null;
					try {
						host = new com.ibm.oneui.util.Url(this.linkUrl).host;						
					} catch (e) {}
					
					if (host) {
					
						var replObj = {
							"dogearSvcLocation": WidgetPlacementConfig.params.dogearSvcRef,
							"host": host
						};

						dojo.attr(this.iconNode, {
							src: dojo.replace(this.iconUrl, replObj)
						});
						
						dojo.removeClass(this.iconNode, this.hiddenClassName);
					}
				}				
				
				//show the "delete Link" icon if we are allowed
				if (this.containerWidget && this.containerWidget.canRemoveLinks) {
					dojo.removeClass(this.removeNode, this.hiddenClassName);
				}

			},
			
			removeLink: function() {
				if (this.containerWidget && typeof this.containerWidget.removeLink == "function") {
					this.containerWidget.removeLink(this);
				}
			}
			
		}
	);
	
	//shortcut functions
	var xmlDecode_ = lconn.profiles.widgets.utils.XmlEncoder.decode;
	var plusDecode_ = lconn.profiles.widgets.utils.PlusEncoder.decode;
	
})();

