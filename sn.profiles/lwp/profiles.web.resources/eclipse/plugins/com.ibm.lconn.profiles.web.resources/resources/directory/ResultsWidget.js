/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Corp. 2015, 2022                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

dojo.provide("lconn.profiles.directory.ResultsWidget");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");
dojo.require("lconn.core.widgetUtils");

dojo.require("com.ibm.social.personcard.widget.PersonWidget");
dojo.require("lconn.profiles.directory.Localization");


dojo.declare(
	"lconn.profiles.directory.ResultsWidget",
	[dijit._Widget, dijit._Templated, lconn.profiles.directory.Localization],
{

	templatePath: dojo.moduleUrl("lconn.profiles", "directory/templates/ResultsWidget.html"),
	widgetsInTemplate: false,
	
	blankGif: "",
	createdWidgets: null,
	lastRow: null,
	lastRowConfidence: "",
	lowElementsPerRow: 3,
	mediumElementsPerRow: 2,
	highElementsPerRow: 1,
	maxRowsPerPage: 5,
	lastRowId: "DirectoryResultWidgetLastRow",
	resultCount: 0,
	toGiveFocus: null,
	topFixedElement: null,
	allowAdvancedSearch: true,
	
	constructor: function() {
		this.inherited(arguments);
		
		this.blankGif = lconn.core.widgetUtils.addVersionNumber(djConfig.blankGif.substr(0, djConfig.blankGif.indexOf("?")));
	},
	
	startup: function() {
		this.inherited(arguments);
		
		this.domNode.style.display = "none";
		this.createdWidgets = [];
		this.connect(window, "onscroll", "_onContainerScroll");
		// we need to connect to lotusFrame for CNX8 UI - it will not fire in cnx7 because lotusFrame is not scrolling itself
		this.connect(dojo.byId("lotusFrame"), "onscroll", "_onContainerScroll");
		
		var fullSearch = this.noResultsMessage.getElementsByTagName("a")[0];
		if(this.allowAdvancedSearch) {
			fullSearch.setAttribute("role", "button");
			this.connect(fullSearch, "onclick", "onFullSearchClicked");
		} else {
			dojo.addClass(fullSearch.parentElement, "lotusHidden");
		}
		this.connect(this.loadingFrameFocusable, "onfocus", dojo.hitch(this, "_onEntryFocused", this.loadingFrame));
	},
	
	uninitialize: function() {
		this._cleanAll();
		this.inherited(arguments);
	},
	
	_cleanAll: function() {
		//scroll Top
		window.scroll(0,0);
		//remove all element in resultContainer
		this.resultContainer.innerHTML = "";
		
		if (this.createdWidgets) {
			for (var i=this.createdWidgets.length-1; i>=0; i--) {
				this.createdWidgets[i].destroyRecursive();
			}
		}
		
		this.createdWidgets = [];
		this.domNode.style.display = "none";
		this.resultCountA11y.innerHTML = "";
		dojo.addClass(this.noResultsMessage, "lotusHidden");
		this.resultContainer.appendChild(this.loadingFrame);
	},
	
	setResults: function(data, totalResults, pageSize) {
		this._cleanAll();
		
		this.resultCount = totalResults;
		
		if(!data || !totalResults) {
			this.domNode.style.display = "";
			dojo.removeClass(this.noResultsMessage, "lotusHidden");
			dojo.addClass(this.resultCountNode, "lotusHidden");
			this._checkLastPage(0, 0, 0);
			this.resultCount = 0;
			this.updateA11yResultCount("");
			this.onResultSet();
			return;
		}
		
		/* TEST high confidence
		data[0].confidence = "high";
		data[0].jobResponsibility = "Software Engineer";
		data[0].address = "123 SomeWhere Street, Dublin";
		data[0].phoneNumber = "1234567890";
		data[0].tags = ["test", "123", "test2"];
		*/
		
		this.domNode.style.display = "";
		this.totalResults.innerHTML = this.getString("TOTAL_RESULTS", {count: totalResults});
		this.allResults.innerHTML = totalResults == 1 ? this.getString("ONE_RESULT") : this.getString("ALL_RESULTS", {count: totalResults});
		dojo.removeClass(this.resultCountNode, "lotusHidden");
		
		this.appendResults(data, totalResults, pageSize);
		this._checkMoreResultsNeeded();
		this.onResultSet();
	},
	
	appendResults: function(data, totalResults, pageSize) {
		var high = [];
		var medium = [];
		var low = [];
		
		this._checkLastPage(data.length, totalResults, pageSize);
		
		for(var i=0; i<data.length; i++) {
			if(data[i].confidence == "high") {
				high.push(data[i]);
			} else if(data[i].confidence == "medium") {
				medium.push(data[i]);
			} else {
				low.push(data[i]);
			}
		}
		
		this._insertRows(high, this.highElementsPerRow, "high");
		this._insertRows(medium, this.mediumElementsPerRow, "medium");
		this._insertRows(low, this.lowElementsPerRow, "low");
		
		// Parse any vcards in there, including the comment poster
		if ( typeof SemTagSvc !== "undefined" ) {
			SemTagSvc.parseDom(null, this.resultContainer);
		}
		
		if(document.activeElement == this.loadingFrameFocusable && this.toGiveFocus) {
			this.toGiveFocus.focus();
			this.toGiveFocus = null;
		}
		
		this.domNode.style.display = "";
	},
	
	_createNewRow: function() {
		var row = document.createElement("div");
		row.style.margin = "20px 0px";
		return row;
	},
	
	_insertRows: function(rows, maxPerRow, confidence) {
		
		if(rows.length > 0) {
			this.lastRow = document.getElementById(this.lastRowId);
			if(!this.lastRow) {
				this.lastRow = this._createNewRow();
			} else {
				this.lastRow.removeAttribute("id");
				
				if(this.lastRowConfidence != confidence) {
					this.lastRow = this._createNewRow();
				}
			}
			this.lastRowConfidence = confidence;
		} else {
			return;
		}
		
		for(var i=0; i<rows.length; i++) {
			var entryWidget = new com.ibm.social.personcard.widget.PersonWidget({
				userId: (rows[i].id ? rows[i].id : ""),
				displayName: (rows[i].name ? rows[i].name : ""),
				preferredName: (rows[i].preferredFirstName ? rows[i].preferredFirstName : ""),
				givenNames: (rows[i].givenNames ? rows[i].givenNames : null),
				jobResponsibility: (rows[i].jobResponsibility ? rows[i].jobResponsibility : ""),
				mail: (rows[i].email ? rows[i].email : ""),
				address: (rows[i].city ? rows[i].city : "") + (rows[i].city && rows[i].country ? ", " : "") + (rows[i].country ? rows[i].country : ""),
				phone: (rows[i].workPhone ? rows[i].workPhone : ""),
				tags: (rows[i].tag ? rows[i].tag : null),
				confidence: rows[i].confidence
			});
			entryWidget.startup();
			this.createdWidgets.push(entryWidget);
			//Set display: 'inline-block' to render each widget in a row
			entryWidget.domNode.style.display = "inline-block";
			entryWidget.domNode.style.width = (100/maxPerRow) + "%";
			entryWidget.domNode.style.cursor = "pointer";
			this.connect(entryWidget.domNode, "onmouseover", dojo.hitch(this, "_switchResultSelection", entryWidget.domNode, true));
			this.connect(entryWidget.domNode, "onmouseout", dojo.hitch(this, "_switchResultSelection", entryWidget.domNode, false));
			this.connect(entryWidget, "onTagClicked", "onTagClicked");
			this.connect(entryWidget, "onNameFocused", "_onEntryFocused");
			entryWidget.connect(entryWidget.domNode, "onclick", dojo.hitch(entryWidget, "personClick"));
			
			if(document.activeElement == this.loadingFrameFocusable && !this.toGiveFocus) {
				this.toGiveFocus = entryWidget.linkNode;
			}
			
			if(this.lastRow.children.length >= maxPerRow) {
				if(this.lastRow.parentNode != this.resultContainer) {
					if(this.resultContainer.contains(this.loadingFrame)) {
						this.resultContainer.insertBefore(this.lastRow, this.loadingFrame);
					} else {
						this.resultContainer.appendChild(this.lastRow);
					}
				}
				this.lastRow = this._createNewRow();
			}
			
			this.lastRow.appendChild(entryWidget.domNode);
		}
		
		if(this.lastRow && this.lastRow.children.length > 0) {
			if(this.lastRow.children.length < maxPerRow) {
				this.lastRow.id = this.lastRowId;
			}
			if(this.lastRow.parentNode != this.resultContainer) {
				if(this.resultContainer.contains(this.loadingFrame)) {
					this.resultContainer.insertBefore(this.lastRow, this.loadingFrame);
				} else {
					this.resultContainer.appendChild(this.lastRow);
				}
			}
		}
	},
	
	_checkLastPage: function(currentReqLenght, totalResults, pageSize) {
		if(totalResults <= pageSize || currentReqLenght < pageSize) {
			if(this.resultContainer.contains(this.loadingFrame)) {
				this.resultContainer.removeChild(this.loadingFrame);
			}
		}
	},
	
	_setContentHeight: function() {
		var height = 0;
		for(var i=0; i<this.maxRowsPerPage && i<this.resultContainer.children.length; i++) {
			if(this.resultContainer.children[i].getAttribute("dojoattachpoint") != "loadingFrame") {
				height += this.resultContainer.children[i].offsetHeight + /* padding-top */ 20;
			}
		}
		
		height += /* padding-bottom last row */ 20;
		
		this.resultContainerScroller.style.height = height + "px";
		
		setTimeout(dojo.hitch(this, this._checkMoreResultsNeeded), 1);
	},
	
	_checkElementIsDisplayed: function() {
		if(!this.resultContainer.contains(this.loadingFrame)) {
			return false;
		}
		
		var windowHeight = dojo.isIE <= 8 ? document.documentElement.clientHeight : window.innerHeight;
		
		var elemPos = dojo.position(this.loadingFrame);

		return (elemPos.y < windowHeight);
	},
	
	_checkEndScrolling: function() {
		var scrollTop = dojo.isChrome ? document.body.scrollTop : document.documentElement.scrollTop;
		var scrollHeight =  dojo.isChrome ? document.body.scrollHeight : document.documentElement.scrollHeight;
		var windowHeight = dojo.isIE <= 8 ? document.documentElement.clientHeight : window.innerHeight;
		if(scrollTop < 0) {
			scrollTop = scrollHeight - windowHeight;
		}
		var resutlPos = dojo.position(this.resultCountNode);
		
		var stringId = "";
		if((resutlPos.y <= windowHeight && !this.resultContainer.contains(this.loadingFrame))
				|| (windowHeight + scrollTop >= scrollHeight && (!this.loadingFrame || !this.resultContainer.contains(this.loadingFrame)))) {
			this.totalResults.style.display = "none";
			this.allResults.style.display = "";
			stringId = "ALL_RESULTS";
		} else {
			this.totalResults.style.display = "";
			this.allResults.style.display = "none";
			stringId = "TOTAL_RESULTS_A11Y";
		}
		this.updateA11yResultCount(stringId);
	},
	
	_checkMoreResultsNeeded: function() {
		setTimeout(dojo.hitch(this, "_checkEndScrolling"), 1);
		
		if(!this.loadingFrame) {
			return;
		}
		
		setTimeout(dojo.hitch(this, function() {
			if(this._checkElementIsDisplayed()) {
				this.onScrollToUpdate();
			}
		}), 1);
	},
	
	_switchResultSelection: function(element, shouldBeSelected, evt) {
		if(shouldBeSelected) {
			dojo.addClass(element, "pfSelected");
		} else {
			dojo.removeClass(element, "pfSelected");
		}
	},
	
	_onContainerScroll: function() {
		this._checkMoreResultsNeeded();
	},
	
	_onEntryFocused: function(element) {
		var windowHeight = dojo.isIE <= 8 ? document.documentElement.clientHeight : window.innerHeight;
		var fixedTopElemPos = dojo.position(this.topFixedElement);
		var elemPos = dojo.position(element);
		
		if((elemPos.y + elemPos.h) > windowHeight) {
			window.scrollBy(0, (elemPos.y + elemPos.h) - windowHeight);
		} else if(elemPos.y < (fixedTopElemPos.y + fixedTopElemPos.h)) {
			window.scrollBy(0, elemPos.y - (fixedTopElemPos.y + fixedTopElemPos.h));
		}
	},
	
	onScrollToUpdate: function() {
	},
	
	onResultSet: function() {
	},
	
	onFullSearchClicked: function() {
	},
	
	onTagClicked: function(tag) {
	},
	
	setTopFixedElement: function(element) {
		this.topFixedElement = element;
	},
	
	setCountNodes: function(container, total, all) {
		this.resultCountNode = container;
		this.totalResults = total;
		this.allResults = all;
	},
	
	updateA11yResultCount: function(stringId) {
		if(this.resultCount == 1) {
			stringId = "ONE_RESULT";
		}
		this.resultCountA11y.innerHTML = this.resultCount > 0 ? this.getString(stringId, {count: this.resultCount}) : this.NO_RESULTS_MSG;
	}
});