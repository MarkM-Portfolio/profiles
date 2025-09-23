/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2008, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

/*
 * Extends lconn.core.PeopleTypeAhead
 */

dojo.provide("lconn.profiles.PeopleTypeAhead");

dojo.require("lconn.core.TypeAhead");
dojo.require("dijit.form.ComboBox");
dojo.require('lconn.core.NameUtil');
dojo.require('lconn.core.Res');
dojo.require('dijit.Tooltip');

dojo.declare(
    "lconn.profiles.PeopleTypeAhead",
    [lconn.core.PeopleTypeAhead],
    {
        submitFormOnNonSelectingEnter: true,
    	
        _openResultList: function(/*Object*/ results, /*Object*/ dataObject){
	        if( this.disabled || 
	            this.readOnly || 
	            (dataObject.query != this._lastQuery)
	        ){
	            return;
	        }
	        this._popupWidget.clearResultList();
	        
	        if (results.length) {
	            // Fill in the textbox with the first item from the drop down list,
	            // and highlight the characters that were auto-completed. For
	            // example, if user typed "CA" and the drop down list appeared, the
	            // textbox would be changed to "California" and "ifornia" would be
	            // highlighted.
	            
	            var zerothvalue = new String(this.formatItem(results[0]));
	            if (zerothvalue && this.autoComplete && !this._prev_key_backspace &&
	            (dataObject.query != "")) {
	                // when the user clicks the arrow button to show the full list,
	                // startSearch looks for "*".
	                // it does not make sense to autocomplete
	                // if they are just previewing the options available.
	                this._autoCompleteText(zerothvalue);
	            }
	        }
	        dataObject._maxOptions = this._maxOptions;
	        
	        // show our list (only if we have content, else nothing)
	        // this._showResultList( results, dataObject );
	        this._showResultGrid(results, dataObject );
	    }, 
	    
	    _showResultGrid: function(/*Object*/ results, /*Object*/ dataObject) {
	    }
    }
);
    
