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

dojo.provide("lconn.profiles.ProfilesTagWidget");

(function() {

	var DEFAULT_DELIM = " ";
	
	var _getTagDelimiter = function() {
		return (dojo.exists("WidgetPlacementConfig.tagListDelimiter") ? WidgetPlacementConfig.tagListDelimiter : DEFAULT_DELIM);
	};
	
	var _hasDefaultTagDelimiter = function() {
		return (_getTagDelimiter() == DEFAULT_DELIM);
	};
	
	var _updateTagListWithDelimiter = function(tag, delim1, delim2) {
		if (typeof delim1 == "undefined") delim1 = DEFAULT_DELIM;
		if (typeof delim2 == "undefined") delim2 = _getTagDelimiter();

		return tag.replace(new RegExp(delim1, "g"), delim2);
	};

	dojo.require("lconn.core.CommonTags.TagWidget");
	dojo.declare("lconn.profiles.ProfilesTagWidget", [lconn.core.CommonTags.TagWidget],{

		removeUrl: null,

		postCreate: function() {
			if (!_hasDefaultTagDelimiter()) {
				this.ajaxCall = new lconn.profiles.ProfilesTagWidgetAjaxCall();
				this.ajaxCall.TAG_URL=this.tagUrl;
				this.ajaxCall.REDIRECT_URL=this.redirectUrl;
				this.ajaxCall.TAG_TEMPLATE=this.tagTemplate;
				this.ajaxCall.URL_PARAMETERS=this.urlParameters;

				if (this.handleAs == 'xml' || this.handleAs == 'json')
					this.ajaxCall.HANDLE_AS = this.handleAs;
				else
					this.ajaxCall.HANDLE_AS = 'xml';//default to xml
			}
			
			this.inherited(arguments);
		},

		prepareData: function() {
			this.inherited(arguments);

			if (this.selectedTags) {
				if (_hasDefaultTagDelimiter()) {
					if (this.selectedTags.indexOf(_getTagDelimiter()) == -1 && this.selectedTags.indexOf("+") > -1) {
						this.selectedTags = this.selectedTags.replace(/\+/g, _getTagDelimiter());

						this._selectedTagsArr = (this.selectedTags.split(_getTagDelimiter())).sort();
					}
				} else {
					this.selectedTags = _updateTagListWithDelimiter(this.selectedTags);
					this._selectedTagsArr = (this.selectedTags.split(_getTagDelimiter())).sort();
				}
			}

		},
		
		updateView: function() {
			this.inherited(arguments);
			
			if (this.selectedTags) {
				//a11y changes
				try {
					dojo.attr(this._selectedTagsSection, {
						"role": "region",
						"aria-label": this.nls.rs_tagCloudSelectedTags + ": " + this.selectedTags
					});
					
					//setting the role of listbox for the UL isn't working with JAWS.  It should just be a 
					//region with links.
					dojo.query('LI[role="presentation"]', this._selectedTagsSection).removeAttr("role");
					dojo.query('A[role="option"]', this._selectedTagsSection).removeAttr("role");
					dojo.query('UL[role="listbox"]', this._selectedTagsSection).removeAttr("role");					
					
				} catch (e) {
					if (window.console) {
						console.info("Unable to update selected Tags accessibility labels");
						console.log(e);
					}
				}
			}
			
			if (this.tags != null && this.tags.length > 0) {
				var relatedTag = "";
				try {
					 var length = this.tags.length;
					 
					 for (var j = 0; j < length; j++) {
						var tag = this.tags[j];

						if (!this.existsInRelated(tag)){
							if (typeof tag.displayName !== "undefined") {
								relatedTag += " " + tag.displayName;
							} else {
								relatedTag += " " + tag.name;
							}
						}
					}
					

					if (!this._relatedDescribedByNode) {
						this._relatedDescribedByNode = document.createElement('div');
						dojo.addClass(this._relatedDescribedByNode, "lotusHidden");
						this._relatedDescribedByNode.id = this.id + "_relTagDesc"; 
						this._relatedDescribedByNode.innerHTML = this.nls.rs_tagCloudRelatedTagsDescription;
						dojo.place(this._relatedDescribedByNode, this._relatedTagsSection, "last");
					}
					
					dojo.attr(this._relatedTagsSection, {
						"role": "region",
						"aria-label": this.nls.rs_tagCloudRelatedTags + ": " + relatedTag,
						"aria-describedBy": this._relatedDescribedByNode.id
					});
					
					//setting the role of listbox for the UL isn't working with JAWS.  It should just be a 
					//region with links.					
					dojo.query('LI[role="presentation"]', this._relatedTagsSection).removeAttr("role");
					dojo.query('A[role="option"]', this._relatedTagsSection).removeAttr("role");
					dojo.query('UL[role="listbox"]', this._relatedTagsSection).removeAttr("role");

				} catch (e) {
					if (window.console) {
						console.info("Unable to update related Tags accessibility labels");
						console.log(e);
					}				
				}
			}
			
		},
		
		//we need to make sure hitting enter on the typeahead submits the search
		_createTypeAhead: function() {
			this.inherited(arguments);
			
			var tempp = dijit.byId(this.id + 'commonTagsTypeAhead');
			if (tempp != null) {
				tempp.submitFormOnKey = true;
			}
		},
		
		//used to remove any page parameter to force the next results to the first page.
		_preProcessTag: function () {
	
			//if we are adding a tag, make sure we're go back to the first page
			//so we need to force the removal of the page parameter from the url
			//if it's there
			try{
				if (this.ajaxCall.URL_PARAMETERS.page){
					delete this.ajaxCall.URL_PARAMETERS.page;
				}
			}
			catch (e){}	
		},
		
		//called after add or remove
		_postProcessTag: function() {

		},

		//override the original function in order to manipulate the url parameters and the redirects of the results
		_removeSelectedTag: function (tag) {
			
			this._preProcessTag();
			
			var length = this._selectedTagsArr.length;
			for (var i=0; i<length; i++) {
				if(tag == this._selectedTagsArr[i]) {
					this._selectedTagsArr.splice(i,1);
					break;
				}
			}
			this.selectedTags = this._selectedTagsArr.join(_getTagDelimiter());

			var origUrl = this.ajaxCall.REDIRECT_URL;
			if (this.removeUrl && this._selectedTagsArr.length == 0) {
				this.ajaxCall.REDIRECT_URL = this.removeUrl;
			}

			if (this.redirectWhenClickTag == false)
				this.reload(false);
			else
				this.ajaxCall.redirect((this.selectedTags == ""?null:this.selectedTags));
			
			this.ajaxCall.REDIRECT_URL = origUrl;
			
			this._postProcessTag();
		},

		//override the original function in order to manipulate the url parameters
		_addSelectedTag: function(tag) {

			this._preProcessTag();
			
			this.inherited(arguments);
			
			this._postProcessTag();

		}

	});



	dojo.require("lconn.core.CommonTags.AjaxCall");
	dojo.declare("lconn.profiles.ProfilesTagWidgetAjaxCall", [lconn.core.CommonTags.AjaxCall], {
	
		encodeTagParameter: function (searchTag) {
			if (_hasDefaultTagDelimiter()) return this.inherited(arguments);
			
			return encodeURIComponent(searchTag);
		},
		
		generateTagUrl: function (urlToForm, searchTag) {
			if (_hasDefaultTagDelimiter()) return this.inherited(arguments);

			//second argument is the searchTag
			arguments[1] = _updateTagListWithDelimiter(arguments[1]);
			return this.inherited(arguments);
		}
	});

})();