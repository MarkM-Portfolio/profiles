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

function validateMinByteLength(form) {
	var isValid = true;
    var focusField = null;
    var i = 0;
    var errorMsgs = new Array();
    var formName = form.getAttributeNode("name"); 

    oMinByteLength = eval('new ' + formName.value + '_minbytelength()');        
    for (x in oMinByteLength) {
    	var field = form[oMinByteLength[x][0]];

        if (typeof(field) != 'undefined' && 
        	(field.type == 'hidden' ||
        	 field.type == 'text' ||
             field.type == 'password' ||
             field.type == 'textarea') &&
             field.disabled == false) {

        	var minByteLength = parseInt(oMinByteLength[x][2]("minbytelength"));
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
           	
            if (bytesNeeded < minByteLength) {
            	if (i == 0) {
                	focusField = field;
                }
                
                var errorMsg = oMinByteLength[x][1];
                var avgBytesPerChar = bytesNeeded/strLength;
                var minChars = Math.ceil(minByteLength/avgBytesPerChar);
                errorMsg = errorMsg.replace(/\(minbytelength\)/g, minChars);
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
