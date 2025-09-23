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
/**
 * ItemSet Attribute supports userKey
 */
dojo.provide("lconn.profiles.widgets.linkRoll.Container");

dojo.require("lconn.profiles.widgets.linkRoll.Control");

(function() {
	dojo.declare(
		"lconn.profiles.widgets.linkRoll.Container",
		null,
		{	
			_widgetInst: null, //holds a placeholder for the instantiated widget
		
			_itemSetKeys: ["userKey"],
			_itemSetValues: {},
			
			_rootDiv: null,
			_divId: null,
		
			onLoad: function() {
				if (this.iContext.widgetId) {
					this._divId = this.iContext.widgetId + "_widgetId_container";
					this._rootDiv = dojo.create("div", {
						id: this._divId
					}, this.iContext.getRootElement());
					
					//holds the arguments to be passd into the LinkRoll dijit
					var instArgs = {};
					
					//pull any itemset data from the xml definition
					var attributesItemSet = this.iContext.getiWidgetAttributes();
					
					for (var ii = 0; ii < this._itemSetKeys.length; ii++) {
						var val = attributesItemSet.getItemValue(this._itemSetKeys[ii]);
						if (typeof val === "string" && val.length > 0) {
							this._itemSetValues[this._itemSetKeys[ii]] = val;
						}					
					}
					
					this._itemSetValues["widgetId"] = this.iContext.widgetId;
					this._itemSetValues["mode"] = this.iContext._mode;

				}
			},
			
			onview: function() {
				if (this._rootDiv) {
					this._destroyWidget();
					this._widgetInst = new lconn.profiles.widgets.linkRoll.Control(this._itemSetValues, this._rootDiv);
				}
			},
		
			onUnload: function() {
				//destroy our widget on unload
				this._destroyWidget();
			},
		
			_destroyWidget: function() {
				if (this._widgetInst && typeof this._widgetInst.destroyRecursive === "function") {
					this._widgetInst.destroyRecursive();
					this._widgetInst = null;
				}		
			}
		}
	);

})();
