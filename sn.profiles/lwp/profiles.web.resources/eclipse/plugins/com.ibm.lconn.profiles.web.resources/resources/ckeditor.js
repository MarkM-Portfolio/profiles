/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.ckeditor");
dojo.require("lconn.core.ckeditor");

(function() {

	var lcEditorDirtyCheck_ = {};
	var initCount_ = 0;
	
	lconn.profiles.ckeditor = {
				
		getAllInstances: function() {
			if (window.CKEDITOR && CKEDITOR.instances) {
				return CKEDITOR.instances
			} else {
				return {};
			}
		},
		
		getInstance: function(sName) {
			return this.getAllInstances()[sName];
		},
		
		getData: function(sName) {
		
			// The ckeditor puts some formatting tags in the value even when
			// there is nothing else.  We need to detect for the "blank" value
			// with just the formatting tags.
			var isDataEmpty_ = function(data) {
				try {
					var data2 = dojo.trim(data.replace(/\&nbsp;|\n|\t/g, ""));
					if (data2.indexOf("<p") === 0 && data2.indexOf("</p>", data2.length - 4) !== -1) {
						data2 = data2.substring(2, data2.length - 4);
						if (data2.indexOf(">") > -1) {
							data2 = data2.substring(data2.indexOf(">") + 1);
						}
						return (dojo.trim(data2) == "");
					}
				} catch (e) {}
				
				return false;
			};	
		
			var inst = this.getInstance(sName);
			if (inst && typeof inst.getData === "function") {
				var newdata = inst.getData();
				if (isDataEmpty_(newdata)) {
					return "";
				} else {
					try {
						//we are going to scrub out the dir attribute ckeditor puts on the data
						//when there are latin chars in the text and the dir is set to rtl.
						//this occurs when the previously populated data with Latin chars that
						//doesn't have a surround <p> element and the user edits it with a RTL language.
						var dummyNode = dojo.create("div", {
							innerHTML: newdata
						});
						var bChanged = false;
						dojo.query("P[dir='rtl']", dummyNode).forEach(function(node) {
							if (lconn.profiles.ProfilesCore.hasLatinChars(node.innerText || node.textContent)) {
								dojo.removeAttr(node, "dir");
								bChanged = true;
							}
						});
						if (bChanged) {
							newdata = dummyNode.innerHTML;
						}
					} catch (e) {
						if (window.console) {
							console.warn("An error has occured scrubbing the ckeditor content.");
							console.warn(e);
						}
					}
					
					return newdata;
				}
			} else {
				return null;
			}
		},
		
		checkDirty: function(sName) {
			var inst = this.getInstance(sName);
			if (inst && typeof inst.checkDirty === "function") {
				return inst.checkDirty();
			} else {
				return false;
			}
		},
		
		resetDirty: function(sName) {
			var inst = this.getInstance(sName);
			
			if (inst && typeof inst.resetDirty === "function") {
				inst.resetDirty();
				lcEditorDirtyCheck_[sName] = false;
				return true;
			} else {
				return false;
			}
		},
		
		canRenderEditors: function () {
			return (dojo.exists("profilesData.config.pageId") && profilesData.config.pageId == "editProfileView");
		},
		
		init: function() {
			if (this.canRenderEditors()) { 
				var textAreaList = this._getNodesToProcess(); 	
				if (textAreaList.length > 0) {
					lconn.core.ckeditor.async(dojo.hitch(this, this._loadAllInstances));
					
					// Japanese single byte yen (backslash) shows incorrect
					// For IE only, the BODY tag inside the iframe of the dojo rich text field (dijit.Editor) 
					// does not inherit the lotusJapanese class from DOM's top body				
					if (dojo.isIE) {
						dojo.query(".rte").addClass("lotusJapanese");
					}
					
					
				} else {
					if (initCount_++ < 400) { // make sure we don't loop forever
						setTimeout(dojo.hitch(this, this.init), 250);
					}
				}
			}
		},
		
		_loadAllInstances: function() {

			var textAreaList = this._getNodesToProcess();
			
			// create editors for each text area
			for (var i=0; i < textAreaList.length; i++) {
				editor = CKEDITOR.replace(
					textAreaList[i].id, 
					lconn.profiles.ckeditor._options
				);
								
				editor.on("instanceReady", 
					dojo.hitch(this, function(e) {
						//code to detect when the ckeditor is dirty since the control doesn't have a native "onchange" event to use
						var sName = e.editor.name;
						
						lcEditorDirtyCheck_[sName] = false;
						setInterval(function() {
							if (lcEditorDirtyCheck_[sName] === false && e.editor.checkDirty()) {
								lcEditorDirtyCheck_[sName] = true;
								dataChange(e);
							}
						}, 500);
						
						if (dojo.byId(sName + "_RTE_loading")) {
							dojo.addClass(sName + "_RTE_loading", "lotusHidden");
						}
						
						//since we have multiple versions of the ckeditor loaded, we need to differntiate between them.
						dojo.query('[role="group"]', e.editor.container.$).forEach(function(el) {
							if (dojo.hasAttr(el, "aria-labelledby")) {
								var lbl = dojo.byId(dojo.attr(el, "aria-labelledby"));
								if (lbl) dojo.place(dojo.doc.createTextNode(lbl.innerHTML + "-" + sName), lbl, "only");;
								
							}
						});
					})
				);
			}		
		
		},
		
		_getNodesToProcess: function() {			
			var textAreaList = dojo.query(".rte textarea");
			if (textAreaList.length == 0) {
				textAreaList = dojo.query(".rte div._ckeditorvalue");
			}
			return textAreaList;			
		},
		
		config: {
			toolbar_ProfilesToolbar: [
				{
					name: 'tools',
					items: ['Undo','Redo','MenuPaste','Find','LotusSpellChecker','ShowBlocks','IbmPermanentPen']
				},
				{
					name: 'styleboxes',
					items: ['Format','Font','FontSize']
				},
				{
					name: 'stylebuttons',
					items: ['Bold','Italic','Underline','Strike','TextColor','BGColor','CopyFormatting','Subscript','Superscript','RemoveFormat']
				},
				{
					name: 'paragraph',
					items: ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock','NumberedList','BulletedList','Indent','Outdent','Blockquote','BidiLtr','BidiRtl','Language' ]
				},
				{
					name: 'insert',
					items: ['Table','Image','Embed','Link','PageBreak','HorizontalRule','SpecialChar']
				}
			],
			language: djConfig.locale,
			toolbar: "ProfilesToolbar",
			
			//@mentions not supported while editing your profile
			ibmMentionDisabled: true,
			
			//iframes are stripped out by the ACF, don't allow the user to paste one in.
			removePlugins: 'ibmpasteiframe',
			
			ibmEnableSvgIcons: true,
			
			resize_maxWidth: 850,
			height: 400
		}
	};
	
	
	lconn.core.ckeditor.addCustomConfig(function(opts) {
		if (!lconn.profiles.ckeditor.canRenderEditors()) return; //if we're not on an edit page, don't add our config.
		
		var cfg = dojo.mixin(opts, lconn.profiles.ckeditor.config);

		// CKEDITOR does a bunch of voodoo to the opts variable properties after this is set.
		// So we need to make sure we get the options we expect.
		// To make sure none of the properties for this are altered by the core CKEDITOR we
		// are going to serialize and deserialize the object.
		lconn.profiles.ckeditor._options = dojo.fromJson(dojo.toJson(cfg));
		try {
			CKEDITOR.editorConfig(cfg);
		} catch (e) {
			if (window.console) {
				console.error("Unabled to set ckeditor parameters");
				console.error(e);
			}
		}

	});	
	
	// let's load it up!
	dojo.addOnLoad(dojo.hitch(lconn.profiles.ckeditor, lconn.profiles.ckeditor.init));
	
})();
