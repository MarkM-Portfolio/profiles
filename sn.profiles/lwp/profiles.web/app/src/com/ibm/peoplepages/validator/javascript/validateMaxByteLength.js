/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*  																 */
/* author: sberajaw                                                  */
/* ***************************************************************** */

function validateMaxByteLength(form) {
	var isValid = true;
    var focusField = null;
    var i = 0;
    var errorMsgs = new Array();
    var formName = form.getAttributeNode("name"); 

    oMaxByteLength = eval('new ' + formName.value + '_maxbytelength()');        
    for (x in oMaxByteLength) {
    	var field = form[oMaxByteLength[x][0]];

        if (typeof(field) != 'undefined' && 
        	(field.type == 'hidden' ||
        	 field.type == 'text' ||
             field.type == 'password' ||
             field.type == 'textarea') &&
             field.disabled == false) {

        	var maxByteLength = parseInt(oMaxByteLength[x][2]("maxbytelength"));
        	var str = field.value;
            var strLength = parseInt(field.value.length);
    		var bytesNeeded = 0;
    		for(var j = 0; j < strLength; j++ ) {
         		var ch = str.charCodeAt(j);
         		
         		if (ch < 0x80) {          
            		bytesNeeded++;
         		} 
         		else if (ch < 0x0800) {          
            		bytesNeeded += 2;
         		} 
         		else if (ch < 0x10000) {          
            		bytesNeeded += 3;
         		} 
         		else {                        
            		bytesNeeded += 4;
         		}
         	}
           	
            if (bytesNeeded > maxByteLength) {
            	if (i == 0) {
                	focusField = field;
                }
                
                var errorMsg = oMaxByteLength[x][1];
                var avgBytesPerChar = bytesNeeded/strLength;
                var maxChars = Math.floor(maxByteLength/avgBytesPerChar);
                errorMsg = errorMsg.replace(/\(maxbytelength\)/g, maxChars);
                errorMsgs[i++] = errorMsg;
                isValid = false;
            }
        }
    }
    if (errorMsgs.length > 0) {
    	focusField.focus();
        alert(errorMsgs.join('\n'));
    }
        
    return isValid;
}
