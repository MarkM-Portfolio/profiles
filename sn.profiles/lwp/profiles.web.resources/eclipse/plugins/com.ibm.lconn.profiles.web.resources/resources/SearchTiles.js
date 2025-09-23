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

/**
 * This class is the container class to display individual search tiles on the
 * Directory search page.
 * 
 * @class lconn.profiles.SearchTiles
 * @author: Robert Barber
 */

dojo.provide("lconn.profiles.SearchTiles");

dojo.require("dijit._Widget");
dojo.require("dijit._Templated");

dojo.require("lconn.profiles.SearchTile");

dojo.declare("lconn.profiles.SearchTiles", [
      dijit._Widget,
      dijit._Templated
], /** @lends lconn.profiles.SearchTiles.prototype */
{
   messages : {
      noResultsText : ''
   },

   templatePath : dojo.moduleUrl("lconn.profiles", "templates/SearchTiles.html"),

   profilesSvcLocation : "/profiles",

   /**
    * flag as to whether the "zoom" of photos should show
    */
   expandThumbnails : true,

   _value : "",
   _count : 0,
   _results : [],

   _styleNodeId : null,

   _tiles : [],

   postMixInProperties : function() {

      this.inherited(arguments);

      // make sure our node ids are set
      if (!this._styleNodeId) {
         this._styleNodeId = this.id + "_searchTiles";
      }

   },

   postCreate : function() {

      this.inherited(arguments);

      // if there are style tags, we need to set them different for IE8 and
      // older
      if (dojo.isIE < 9) {
         var cached = dijit._Templated.getCachedTemplate(this.templatePath, this.templateString, this._skipNodeCache);

         if (dojo.isString(cached)) {
            dojo.forEach((this._stringRepl(cached)).split("</style>"), dojo.hitch(this, function(str) {
               try {
                  var arr = str.split("<style");
                  if (arr.length > 1) {
                     str = arr[1].substring(arr[1].indexOf(">") + 1);
                     var style = dojo.create("style", {
                        type : "text/css"
                     });
                     style.styleSheet.cssText = str;
                     dojo.place(style, this.domNode, "first");
                  }
               }
               catch (ee) {
                  if (window.console) {
                     console.error("Error parsing style tag in template for: " + this.id);
                     console.log(ee);
                  }
               }

            }));

         }
      }

   },

   // clear all of the existing tiles
   reset : function() {
      // destroy the dijits
      while (this._tiles.length > 0) {
         var tile = this._tiles.pop();
         tile.destroy();
      }

      // remove the dom nodes
      while (this.baseNode.lastChild) {
         this.baseNode.removeChild(this.baseNode.lastChild);
      }

      dojo.place(document.createTextNode(""), this.infoNode, "only");
   },

   setValue : function(val) {
      this._value = val;
   },

   // results are passed in from the typeahead control
   setResults : function(results) {
      this._results = results;
      this._count = this._results.length;

      // clear out any existing tiles before re-populating...
      this.reset();

      if (this._count == 0) {
         dojo.attr(this.domNode, "aria-label", this.messages.noResultsText);

      }
      else {
         dojo.attr(this.domNode, "aria-label", (this.messages.resultsHeadingText).replace(/{0\}/, this._value));

         for (var n = 0; n < this._count; n++) {
            this._addItem(this._results[n], n);
         }
      }

      dojo.place(document.createTextNode(dojo.attr(this.domNode, "aria-label")), this.infoNode, "only");

   },

   _addItem : function(p, i) {

      var node = dojo.create("div");
      dojo.place(node, this.baseNode, "last");

      var props = {
         messages : this.messages,
         person : p,
         showEmail : this.showEmail,
         expandThumbnails : this.expandThumbnails,
         profilesSvcLocation : this.profilesSvcLocation
      };

      // instantiate a new lconn.profiles.SearchTile for each profile tile
      this._tiles.push(new lconn.profiles.SearchTile(props, node));

   }

});
