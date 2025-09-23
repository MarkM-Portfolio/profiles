/* *************************************************************** */
/*                                                                 */
/* HCL Confidential                                                */
/*                                                                 */
/* OCO Source Materials                                            */
/*                                                                 */
/* Copyright HCL Technologies Limited 2013, 2022                   */
/*                                                                 */
/* The source code for this program is not published or otherwise  */
/* divested of its trade secrets, irrespective of what has been    */
/* deposited with the U.S. Copyright Office.                       */
/*                                                                 */
/* *************************************************************** */

dojo.provide("lconn.profiles.actionBar.ActionBar");


dojo.require("dojo.data.ItemFileWriteStore");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("dijit.Menu");
dojo.require("dijit.MenuItem");
dojo.require("dijit.form.Button");
dojo.require("dojox.html.entities");

dojo.require("lconn.core.MenuUtility");

dojo.requireLocalization("lconn.profiles", "ui");

(function() {
	dojo.declare(
		"lconn.profiles.actionBar.ActionBar", 
		[dijit._Widget, dijit._Templated],
		{
			
			//The maximum number of buttons.  If the number of actions
			//exceeds this, then the last button will turn into "More Actions"
			//button with a menu drop down for the rest of the items
			maxButtons: 6,
			
			templatePath: dojo.moduleUrl("lconn.profiles", "actionBar/templates/ActionBar.html"),
			
			blankGif: this._blankGif || dojo.config.blankGif || dijit._Widget.prototype._blankGif,
			strings: {},
			hiddenClassName: "lotusHidden",
			disabledClassName: "lotusBtnDisabled",
			
			_moreActionsShown: false,
			
			
			focusIdx: -1,
			
			_actions: [],
			_actionBarConnects: [],
			
			_runtime: {},
			
			
			// holds the menu and list of actions created so we can destroy them when this is destroyed
			_menu: null,
			
			postMixInProperties: function() {	
				this.inherited(arguments);
				
				this._actions = [];
				this._actionBarConnects = [];

				if(window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()) {
					this.templatePath = dojo.moduleUrl("lconn.profiles", "actionBar/templates/cnxActionBar.html");
					this.maxButtons = 3;
				}
				
				//load our strings...
				var defaultStrings = dojo.i18n.getLocalization("lconn.profiles", "ui");
				if (typeof defaultStrings !== "undefined") {
					this.strings = dojo.mixin(
						defaultStrings,
						this.strings
					);
				} else {
					this.strings = dojo.mixin(
						generalrs || {},
						this.strings
					);			
				}				

				
			},
			
			
			postCreate: function() {
				this.inherited(arguments);
				
				//any stylesheets defined in the template need to be loaded
				this._loadStylesFromTemplate();
				
			},			
			
			//adding a store here connects it into our observable callback
			addStore: function addStore(store, callback) {
			
				if (typeof callback !== "function") callback = function() {};
				
				var cbLoadData_ = dojo.hitch(this, function() {
					store.fetch({
						onComplete: dojo.hitch(this, function(items, request) {
							dojo.forEach(items, dojo.hitch(this, function(item) {
								this._storeChangeHandler(item, store);
							}));
							callback();
						}),
						onError: dojo.hitch(this, function(err, request) {
							this.onError(err);
							callback();
						})
					});
				});
				
				if (typeof store.loadData === "function") {
					store.loadData(cbLoadData_);
				} else {
					cbLoadData_();
				}

			},
			
			_renderActionItem: function(item, store, callback) {
				
				var label = store.getLabel(item);
				
				var tip = label;
				if (store.hasAttribute(item, "tooltip")) {
					tip = store.getValue(item, "tooltip");
				}
				
				var id = store.getValue(item, "id");
				
				var disabled = false;
				if (store.hasAttribute(item, "disabled")) {
					disabled = !!(store.getValue(item, "disabled"));
				}

				var icon = "";
				if (store.hasAttribute(item, "icon")) {
					icon = store.getValue(item, "icon");
				}
				
				item.focusIdx = this._actions.length;
				var doFocus = (this.focusIdx > -1 && this.focusIdx === item.focusIdx);
				
				
				// place invite and follow icons on top of actionbar
				if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()) {
					dojo.attr(this.singleButtonNode, { "title": tip, });
					dojo.attr(this.singleButtonImageNode, {src: icon});
					dojo.removeClass(this.singleButtonImageNode, this.hiddenClassName);

						//we set everything we need, clone it, cleanse it, and insert it
					var cnxNode = dojo.clone(this.singleButtonNode);
					dojo.attr(cnxNode, { id: "btn_" + id });
					dojo.removeClass(cnxNode, this.hiddenClassName);
					
					if (disabled) {
						dojo.addClass(cnxNode, this.disabledClassName);
						dojo.attr(cnxNode, "aria-disabled", "true");
					}
						
					dojo.removeAttr(cnxNode, "dojoattachpoint");
					dojo.query("[dojoattachpoint]", cnxNode).forEach(function(snode) {
						dojo.removeAttr(snode, "dojoattachpoint");
					});
						
					//connect the click of our cloned button to the _actionClick method
					this._actionBarConnects.push(
						dojo.connect(cnxNode, "onclick", ((disabled) ? function () { } : dojo.hitch(this, this._actionClick, item, store, cnxNode.id)))
					);

					if (cnxNode.id === 'btn_actn__personCardAddAsMyColleagues' 
						|| cnxNode.id === 'btn_actn__label_following_follow' 
						|| cnxNode.id === 'btn_actn__label.following.following' 
						|| cnxNode.id === 'btn_actn__personCardInNetwork'
						|| cnxNode.id === 'btn_actn__personCardAcceptInv') {
							dojo.place(dojo.doc.createTextNode(label), cnxNode);
							dojo.place(cnxNode, this.cnxButtonsNode);
							dojo.removeClass(this.cnxButtonsNode, this.hiddenClassName);
					}
				}
				if (this._moreActionsShown) {
					//setup our menu dijit if not done already
					if (!this._menu) {
						//setup the menu
						this._menu = new dijit.Menu({
							targetNodeIds:[this.menuNode.id]
						});
						this._menu.onCancel = function(){
							this.hide();
							this.destroy();
						};
						
						dojo.removeClass(this.moreActionsNode, this.hiddenClassName);
					}
					
					if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled() && id !== 'actn__personCardAddAsMyColleagues' 
						&& id !== 'actn__label_following_follow' 
						&& id !== 'actn__label.following.following' 
						&& id !== 'actn__personCardInNetwork'
						&& id !== 'actn__personCardAcceptInv') {
							//add the menu item to the list of more actions instead of a new button
						this._menu.addChild(new dijit.MenuItem({
							"label": dojox.html.entities.encode(label),
							"title": tip,
							"disabled": disabled,
							"onClick": ((disabled)?function() {}:dojo.hitch(this, this._actionClick, item, store, this.moreActionsNode.id)),
							"iconClass": "AEMenuItemIcon_" + this.id + " AEMenuItemIcon_" + id
						}));
					} else if( window.ui && typeof window.ui._check_ui_enabled === 'function' && !window.ui._check_ui_enabled()) {
						//add the menu item to the list of more actions instead of a new button
						this._menu.addChild(new dijit.MenuItem({
							"label": dojox.html.entities.encode(label),
							"title": tip,
							"disabled": disabled,
							"onClick": ((disabled)?function() {}:dojo.hitch(this, this._actionClick, item, store, this.moreActionsNode.id)),
							"iconClass": "AEMenuItemIcon_" + this.id + " AEMenuItemIcon_" + id
						}));
					}
					//if there is an icon defined, we need to find the icon in the menu item and update the src to use it...
					if (icon && icon.length > 0) {
						dojo.query("img", this._menu.domNode).forEach(function(imgNode){
							if (dojo.hasClass(imgNode, "AEMenuItemIcon_" + id)) {
								dojo.attr(imgNode, {src: icon});
							}
						});
					}
					
					if (doFocus) {
						this.moreActionsNode.focus();
					}
					
					
				} else {
					//update our single button, clone it, and insert it.
					dojo.attr(this.singleButtonNode, {"title": tip,});
					if(window.ui && typeof window.ui._check_ui_enabled === 'function' && !window.ui._check_ui_enabled()) {
						dojo.place(dojo.doc.createTextNode(label), this.singleButtonTextNode, "only");
					}
					if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled() && icon && icon.length > 0) {
						dojo.attr(this.singleButtonImageNode, {src: icon});
						dojo.removeClass(this.singleButtonImageNode, this.hiddenClassName);
					} else {
						dojo.attr(this.singleButtonImageNode, {src: this.blankGif});
						dojo.addClass(this.singleButtonImageNode, this.hiddenClassName);					
					}
					
					//we set everything we need, clone it, cleanse it, and insert it
					var newnode = dojo.clone(this.singleButtonNode);
					dojo.attr(newnode, {id: "btn_" + id});
					dojo.removeClass(newnode, this.hiddenClassName);
					
					if (disabled) {
						dojo.addClass(newnode, this.disabledClassName);
						dojo.attr(newnode, "aria-disabled", "true");
					}
					
					dojo.removeAttr(newnode, "dojoattachpoint");
					dojo.query("[dojoattachpoint]", newnode).forEach(function(snode) {
						dojo.removeAttr(snode, "dojoattachpoint");
					});
					
					//connect the click of our cloned button to the _actionClick method
					this._actionBarConnects.push(
						dojo.connect(newnode, "onclick", ((disabled)?function() {}:dojo.hitch(this, this._actionClick, item, store, newnode.id)))
					);
					//Place items on action bar excluding the once displayed on top of actionbar
					if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled() && newnode.id !== 'btn_actn__personCardAddAsMyColleagues' 
						&& newnode.id !== 'btn_actn__label_following_follow' 
						&& newnode.id !== 'btn_actn__label.following.following' 
						&& newnode.id !== 'btn_actn__personCardInNetwork'
						&& newnode.id !== 'btn_actn__personCardAcceptInv') {
							dojo.place(newnode, this.buttonsNode, "last");
							dojo.removeClass(this.buttonsNode, this.hiddenClassName);
					} else if(window.ui && typeof window.ui._check_ui_enabled === 'function' && !window.ui._check_ui_enabled()) {
						dojo.place(newnode, this.buttonsNode, "last");
						dojo.removeClass(this.buttonsNode, this.hiddenClassName);
					}
					if (doFocus) {
						newnode.focus();
					}
					
					this._runtime._lastSingleButton = newnode;
					this._runtime._lastSingleItem = item;
					this._runtime._lastSingleStore = store;
				
				}
				
				if (typeof callback === "function") {
					callback();
				}
				
			},
					
			// gets called whenever there is an action added to any of the registered stores
			// so anyone can create a store and 
			_storeChangeHandler: function(item, store) {
				
				//we're at our max, we need to remove the last button
				//and add a "More Actions" button and add the action in the last
				//button to the first item in the drop down menu
				if (!this._moreActionsShown && this._actions.length >= this.maxButtons) {
				
					this._moreActionsShown = true;  //sets the flag to tell the rendered to add it to the More Actions menu
					
					if (this._runtime._lastSingleButton && this._runtime._lastSingleItem && this._runtime._lastSingleStore) { 
						this._runtime._lastSingleButton.parentNode.removeChild(this._runtime._lastSingleButton);  //remove the last button added
					
						//re-add the last button item to the menu
						this._renderActionItem(this._runtime._lastSingleItem, this._runtime._lastSingleStore, dojo.hitch(this, function() {
							delete this._runtime._lastSingleButton;
							delete this._runtime._lastSingleItem;
							delete this._runtime._lastSingleStore;
						})); 
					}
					
				}
				
				this._renderActionItem(item, store);

				// remove items from action bar to place it on cnxActionBar
				if (window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled() && (item.id[0] !== 'actn__personCardAddAsMyColleagues' 
				&& item.id[0] !== 'actn__label.following.following'
				&& item.id[0] !== 'actn__label_following_follow'
				&& item.id[0] !== 'actn__personCardAcceptInv'
				&& item.id[0] !== 'actn__personCardInNetwork')) {
					this._actions.push(item);  //add this item to the list of actions.
				} else if(window.ui && typeof window.ui._check_ui_enabled === 'function' && !window.ui._check_ui_enabled() ){
					this._actions.push(item);
				}

			},

			//helper function to pull the store item data, with a default
			_getItemValue: function(item, store, nam, def) {
				var ret = def;
				if (store.hasAttribute(item, nam)) {
					ret = store.getValue(item, nam);
				}
				return ret;				
			},

			_lastActionClicked: null,
			getLastActionId: function() {
				return this._runtime.lastActionClicked;
			},

			_actionClick: function(item, store, focusnodeid) {
				this._runtime.lastActionClicked = focusnodeid;

				var f = this._getItemValue(item, store, "onClick", null);
				var url = dojox.html.entities.encode(this._getItemValue(item, store, "url", null));

				if (typeof f === "function") {
					f(item);

				} else if (typeof url === "string") {

					if (this._getItemValue(item, store, "new_window", false)) {
						//replace all alpha-numeric chars with _ for IE compatibility
						var winname = this._getItemValue(item, store, "window_name", ("win_" + item.id).replace(/\W/g, '_'));

						var winfeat = this._getItemValue(item, store, "window_features", "");

						open(url, winname, winfeat);
					} else {
						location.href = url;
					}
				}

			},

			//all the data has been pulled, open the menu
			_showMoreActions: function(evt) {			
				menuUtility.openMenu(evt, this._menu.id);		
			},
			
			
			_loadStylesFromTemplate: function() {
				// if there are style tags, we need to set them different for IE8 and older
				if (dojo.isIE < 9) {
					var cached = this.templateString;
					try {
						if (!cached) {
							cached = dijit._Templated.getCachedTemplate(this.templatePath, this.templateString, this._skipNodeCache);
						}
					} catch (e) {}
					
					if (dojo.isString(cached)) {
						dojo.forEach(
							(this._stringRepl(cached)).split("</style>"),
							dojo.hitch(this, function(str) {
								try {
									var arr = str.split("<style");
									if (arr.length > 1) {									
										str = arr[1].substring(arr[1].indexOf(">")+1);
										this._loadStylesheet(str);
									}
								} catch (ee) {
									if (window.console) {
										console.error("Error parsing style tag in template for: " + this.id);
										console.log(ee);
									}
								}

							})
						);
						
					}
				}		
			},
			
			_loadStylesheet: function(styleHtml) {
				var style = dojo.create("style", {type: "text/css"});
				if (style.styleSheet) { // IE sets styles this way
					style.styleSheet.cssText = styleHtml;
				} else {
					dojo.place(dojo.doc.createTextNode(styleHtml), style);
				}
				dojo.place(style, this.stylesNode, "last"); 		
			},
			
			reset: function() {
				if (this._menu && typeof this._menu.destroy === "function") {
					this._menu.destroy();
				}
				this._menu = null;
				
				dojo.forEach(this._actionBarConnects, dojo.disconnect);
				
				this._actionBarConnects = [];
				this._actions = [];
				this.focusIdx = -1;
				this._runtime = {};
				
				this._moreActionsShown = false;
				dojo.addClass(this.moreActionsNode, this.hiddenClassName);

				this.buttonsNode.innerHTML = "";
				if(window.ui && typeof window.ui._check_ui_enabled === 'function' && window.ui._check_ui_enabled()){
					this.cnxButtonsNode.innerHTML = "";
				}
			},
			
			destroy: function() {
			
				this.inherited(arguments);
				
				this.reset();

			},
	 
			onError: function(err) {
				if (console) { //TODO - better error handling?
					console.error(err);
				}
			}		

		}
	);

})();	
