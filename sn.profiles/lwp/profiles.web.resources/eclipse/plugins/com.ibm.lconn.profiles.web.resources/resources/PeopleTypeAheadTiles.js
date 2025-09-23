/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2001, 2014                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/* author: Robert Barber                                             */

/*
 * Extends lconn.core.PeopleTypeAhead
 *
 * This class is used to render the input box for the simple search on
 * the Directory search page.  It supports a typeahead feature that shows
 * "tiles" of people below the search box instead of the drop-down list
 * provided by the lconn.core.PeopleTypeAhead.
 */

dojo.provide("lconn.profiles.PeopleTypeAheadTiles");

dojo.require("lconn.core.PeopleTypeAhead");
dojo.require("lconn.core.PeopleDataStore");

dojo.require("lconn.profiles.SearchTiles");


dojo.declare(
    "lconn.profiles.PeopleTypeAheadTiles",
    [lconn.core.PeopleTypeAhead],
    {
	
		templatePath: dojo.moduleUrl("lconn.profiles", "templates/PeopleTypeAheadTiles.html"),
	
		submitFormOnNonSelectingEnter: true,
		autoComplete: false,
		
		profilesSvcLocation: "/profiles",
		count: 21,
		minChars: 3,
		searchDelay: 250,
		hasDownArrow: false,
		autocomplete: true,
		multipleValues: false,
		liveNameSupport: true,
		expandThumbnails: true,
		showEmail: true,
		lang: "",
		lastMod: "",
		messages: {},
		defaultValue: '',
		
		resultsSectionId: null,
		
		_tilesWidget: null,
		_resultsNode: null,
		
		
		postMixInProperties: function() {
		
			var newMessages = dojo.clone(this.messages);

			this.inherited(arguments);
			
	    	this.store = new lconn.core.PeopleDataStore({
				jsId: "peopleTypeAheadStore",
				queryParam: "name",
				url: this.profilesSvcLocation + "/html/nameTypeahead.do" +
						"?count="+ this.count +
						"&extended=true" + 
						((this.lastMod != "")?"&lastMod="+this.lastMod:"") +    		
						((this.lang != "")?"&lang="+this.lang:"")
			});
			
			if (!this.resultsSectionId) {
				this.resultsSectionId = this.id + "_popup";
			}

			this.messages = dojo.mixin(dojo.clone(this.messages), newMessages);
			
			if (!this.searchDelay) this.searchDelay = 250;
			
			//make sure this exists since the template attaches to it.
			if (typeof this.compositionend !== "function") {
				this.compositionend = function() {};
			}
			
		},
		
		postCreate: function() {
			this.inherited(arguments);
			
			// try to find the node to render the tiles.  If none is found
			// we'll put a div right after the search box
			this._resultsNode = dojo.byId(this.resultsSectionId);
			if (!this._resultsNode) {
				var newNode = dojo.create("div", {id: this.resultsSectionId});
				dojo.place(newNode, this.domNode, "after");
			}
			
			if (this.defaultValue && this.defaultValue.length > 0) {
				this.focusNode.focus();
				this.setValue(this.defaultValue.replace(/\*/g, ""));
			}
			
			// lang needed to be removed for a11y compliance
			dojo.removeAttr(this.domNode, "lang");
			
		},
		
		//override
		getTextBoxValue: function() {
			var ret = this.inherited(arguments);
			
			if (ret == this.hintText) {
				return "";
			}
			
			return ret;
			
		},
		
		//override
		_hideResultList: function() {
			if (this.getTextBoxValue().length == 0) {
				this.reset();
			}
		},

		//clears all of the search tiles
		reset: function() {
			if (this._tilesWidget) {
				this._tilesWidget.reset();
			}
		},	
		
		//override
		_openResultList: function(/*Object*/ results, /*Object*/ dataObject) {
					
			if (this._resultsNode) {
				if (!this._tilesWidget) {
					// lconn.profiles.SearchTiles is the container for all of the search tiles
					this._tilesWidget = new lconn.profiles.SearchTiles(
						{
							profilesSvcLocation: this.profilesSvcLocation,
							expandThumbnails: this.expandThumbnails,
							showEmail: this.showEmail,
							messages: this.messages
						},
						this._resultsNode
					);					
				}
				
				this._tilesWidget.setValue(this.getTextBoxValue());
				this._tilesWidget.setResults(results);

				if (this.liveNameSupport && typeof profiles_AddLiveNameSupport == "function") {
					profiles_AddLiveNameSupport(this._resultsNode.id);
				}
				
			}
			
		},
		
		

        /* 
		* The remaining functions were copied from lconn.core.PeopleTypeAhead 
		* This was needed because of changes where NOTED
		*/
        _onKeyPress: function(/*Event*/ evt){
           // summary:
           //    Handles keyboard events
           var key = evt.charOrCode;
           // except for cutting/pasting case - ctrl + x/v
           if(evt.altKey || ((evt.ctrlKey || evt.metaKey) && (key != 'x' && key != 'v')) || key == dojo.keys.SHIFT){
              return; // throw out weird key combinations and spurious events
           }
           var doSearch = false;
           var searchFunction = "_startSearchFromInput";
           var pw = this._popupWidget;
           var dk = dojo.keys;
           var highlighted = null;
           this._prev_key_backspace = false;
           this._abortQuery();
           if(this._isShowingNow){
              pw.handleKey(key);
              highlighted = pw.getHighlightedOption();
           }
           switch(key){
              case dk.PAGE_DOWN:
              case dk.DOWN_ARROW:
                 if(!this._isShowingNow){
                    this._arrowPressed();
                    doSearch = true;
                    searchFunction = "_startSearchAll";
                 }
                 dojo.stopEvent(evt);
                 break;                 
              case dk.PAGE_UP:
              case dk.UP_ARROW:
                 if(!this._isShowingNow){
                    this._arrowPressed();
                    doSearch = true;
                    searchFunction = "_startSearchAll";
                 }
                 dojo.stopEvent(evt);
                 break;

              case dk.ENTER:
                 if(highlighted){
                    // only stop event on prev/next
                    if(highlighted == pw.nextButton){
                       this._nextSearch(1);
                       dojo.stopEvent(evt);
                       break;
                    }else if(highlighted == pw.previousButton){
                       this._nextSearch(-1);
                       dojo.stopEvent(evt);
                       break;
                    }else if(highlighted == pw.searchButton) {
                       pw.searchDirectory();
                       dojo.stopEvent(evt);
                       return;
                    } else if ( highlighted.item && parseInt(highlighted.item.type) < 0 ) {
                       dojo.stopEvent(evt);
                       break;
                    }
                    pw.attr('value', { target: highlighted });
                 }else{
                    // Update 'value' (ex: KY) according to currently displayed text
                    this._setBlurValue(); // set value if needed
                    this._setCaretPos(this.focusNode, this.focusNode.value.length); // move cursor to end and cancel highlighting
                    
                    // If nothing is selected or the popup isn't open, allow the form to be submitted
                    if (this.submitFormOnNonSelectingEnter) {
                       if (this.searchTimer) {
                          clearTimeout(this.searchTimer);
                          this.searchTimer = null;
                       }
                       this._lastQuery = null;
                       
                       break;
                    }
                 }
                 // default case:
                 if (this._isShowingNow) {
                    this._lastQuery = null; // in case results come back later
                    this._hideResultList();
                 }
                 // lconn.core: conditionally submit
                 if (!this.submitFormOnKey)
                    evt.preventDefault();
                 // fall through
                 break;
              case dk.TAB:
            	  var newvalue = this.attr('displayedValue');
                  // if the user had More Choices selected fall into the
                  // _onBlur handler
                  if(pw && (
                     newvalue == pw._messages["previousMessage"] ||
                     newvalue == pw._messages["nextMessage"])
                  ){
                     break;
                  }
                  if(highlighted){
                     //in 1.4 pw.attr will call this._selectOption();
                     // lconn.core: we need this for keyboard accessibility
                     pw.attr('value', { target: highlighted });
                  }
                  if(this._isShowingNow){
                     this._lastQuery = null; // in case results come back later
                     this._hideResultList();
                  }
                 break;
              case ' ':
                 if(highlighted){
                    dojo.stopEvent(evt);
                    this._selectOption();
                    this._hideResultList();
                 }else{
                    doSearch = true;
                 }
                 break;

              case dk.ESCAPE:
                 if(this._isShowingNow){
                    dojo.stopEvent(evt);
                    this._hideResultList();
                    
                    if(this._currentInput){
                        //lconn.core - restore the original typed value when user escapes from the dropdown
                        this.focusNode.value = this._currentInput
                        delete this._currentInput;
                    }
                 }
                 break;

              case dk.DELETE:
              case dk.BACKSPACE:
                 this._prev_key_backspace = true;
                 doSearch = true;
                 break;

              default:
				// NOTED - remarked out for tiles
                  //dijit.setWaiState(this.focusNode,"activedescendant", this.focusNode.id);
              	  this.focusNode.focus();
                 // Non char keys (F1-F12 etc..)  shouldn't open list.
                 // Ascii characters and IME input (Chinese, Japanese etc.) should.
                 // On IE and safari, IME input produces keycode == 229, and we simulate
                 // it on firefox by attaching to compositionend event (see compositionend method)
                 doSearch = typeof key == 'string' || key == 229;
           }
           if(doSearch){
              // need to wait a tad before start search so that the event
              // bubbles through DOM and we have value visible
              this.item = undefined; // undefined means item needs to be set
              this.searchTimer = setTimeout(dojo.hitch(this, searchFunction),1);
           }
        },
		

      _startSearch: function(/*String*/ key, opt) {
    	  opt = opt || {};
         if (opt.searchImmediately) {
            opt.searchBoth = true;
         }

         var popupId = this.id + "_popup";
         if(!this._popupWidget){
		   //NOTED - hack to short circuit the built-in popup from lconn.core.PeopleTypeAhead
		   /*
            this._popupWidget = new lconn.core.PeopleTypeAheadMenu({
               _strings: this._strings,
               rs_searchDirectory: this.searchDirectory,
               NoResultsMessage: this.NoResultsMessage,
               HeaderMessage: this.HeaderMessage,
               disableSearchDirectory: this.disableSearchDirectory,
               onChange: dojo.hitch(this, this._selectOption),
               id:popupId,
               inputWidget: this,
               disableBizCard: this.disableBizCard
            });
            */
			this._popupWidget = {
				handleKey: function() {},
				getHighlightedOption: function() {},
				searchDirectory: function() {},
				attr: function() {},
				_messages: {},
				domNode: this._resultsNode
			};
			
            // waiRole, waiState
            var role = this.textbox.getAttribute("wairole");
            if(role){
               dijit.setWaiRole(this.textbox, role);
            } 
            dijit.setWaiState(this._popupWidget.domNode, "live", "polite"); 
            dijit.removeWaiState(this.focusNode,"activedescendant");
            dijit.setWaiState(this.textbox,"owns", popupId); // associate popup with textbox
         } else {
			// NOTED - remarked out for tiles
             //dijit.setWaiState(this.focusNode,"activedescendant", popupId);
         }
         
         // create a new query to prevent accidentally querying for a hidden
         // value from FilteringSelect's keyField
         this.item = null; // #4872
         var query = dojo.clone(this.query); // #5970
         this._lastQuery = query = key;
         // #5970: set _lastQuery, *then* start the timeout
         // otherwise, if the user types and the last query returns before the timeout,
         // _lastQuery won't be set and their input gets rewritten
         this.searchTimer=setTimeout(dojo.hitch(this, function(query, _this){
            var dataObject=this.store.fetch({
               queryOptions: dojo.mixin({
                  ignoreCase:this.ignoreCase,
                  deep:true
               }, opt),
               query: query,
               onComplete:dojo.hitch(this, "_openResultList"),
               onError: function(errText){
                  console.error('dijit.form.ComboBox: ' + errText);
                  dojo.hitch(_this, "_hideResultList")();
               },
               start:0,
               count:this.pageSize
            });
            
            var nextSearch = function(dataObject, direction){
               dataObject.start += dataObject.count*direction;
               // #4091:
               //      tell callback the direction of the paging so the screen
               //      reader knows which menu option to shout
               dataObject.direction = direction;
               this.store.fetch(dataObject);
            }
            this._nextSearch = this._popupWidget.onPage = dojo.hitch(this, nextSearch, dataObject);
            this._popupWidget.searchDirectory=dojo.hitch(this, dojo.hitch(this, function() {
               //this._startSearch(key, {searchDirectory:true});
               dataObject.queryOptions.searchDirectory=true;
               this.store.fetch(dataObject);
            }));
            
         }, query, this), opt.searchImmediately ? 1 : this.searchDelay);
      }		
		
		
    }
);
    
