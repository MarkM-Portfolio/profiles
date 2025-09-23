/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

var oneNumberMeansFilterLessThanOnly = true;
var count = 0;
var done = false;
var uidFieldName = "uid";
var minUid = null;
var maxUid = null;
var maxUidArrayIndex = 0;
var maxUidArray = ["013000000","033000000","056000000","077000000","096000000",
					  "125000000","213000000","356000000","550000000","722000000","862000000",
					  "980000000","BZ0000000","CC0000000","S00000000"];


function getLdapFilterPrefix() {
	return "(&";
}

function getLdapFilterSuffix() {
	return "(objectclass=ibmPerson))";
}

function getCurrentMaxUid() {
	return maxUid;
}

function getCurrentMinUid() {
	return minUid;
}


function getNextMaxUid() {
	var result = null;
	if(maxUidArrayIndex < maxUidArray.length) {
		result = maxUidArray[maxUidArrayIndex];
		++maxUidArrayIndex;
	}

	return result;
}

function getNextLdapFilterString() {
	var result = null;
	if(!done) {
		minUid = maxUid;
		maxUid = getNextMaxUid();
		if(maxUid != null) {
			++count;
			if(minUid == null) {
				result = getLdapFilterPrefix() + "(" + uidFieldName + "<=" + maxUid + ")" + getLdapFilterSuffix();
			}
			else {
				result = getLdapFilterPrefix() + "(" + uidFieldName + ">=" + minUid + ")(" + uidFieldName + "<=" + maxUid +")" + 	getLdapFilterSuffix();
			}
		}
		else {
			/* If we have only one filter number total, and flag set according, we only want numbers less */
			if((count > 1) || !oneNumberMeansFilterLessThanOnly) {
				result = getLdapFilterPrefix() + "(" + uidFieldName + ">=" + minUid + ")" +	getLdapFilterSuffix();
			}
			done = true;
		}
	}

	return result;
}
