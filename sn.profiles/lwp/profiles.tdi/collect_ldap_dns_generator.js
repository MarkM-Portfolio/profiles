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

//
// I will not filter via the search base
//
function suppliesSearchBase() { return false; }

function getNextSearchBase() { return null; }

//
// I will filter via the search filter
//
function suppliesSearchFilter() { return true; }

//if the script fails due to LDAP administrative limits (ie) the LDAP server
//times out.  You can change these two variables and pick-up where the
//script failed.  MAKE A BACKUP OF THE ORIGINAL COLLECT.DNS
//FILE.  The second run will overwrite the first!
//For the firstLetter A=0, for secondLetter A=-1
//the original values are
//var firstLetter = 0;
//var secondLetter = -1;

var firstLetter = 0;
var secondLetter = -1;
var special = 1;

function getNextSearchFilter() { 
 // The goal is to build custom search filters such that
 //the returned results will arrive in "chunks" or "pages".
 //The resulting search filters will look something like this:
 //(&(uid=aa*)(objectClass=*))
 //(&(uid=ab*)(objectClass=*))
 var sf1 = "(&(uid=";
 var sf2 = ")(objectClass=*))";
 var filter = null;

 // first get the uid's which start with a plus
 if (special == 1) {
	filter = "(&(uid=+*)(objectClass=*))";
	special = 2;
	return filter;
 }
  
  // second special case, starts with 'C-'
 if (special == 2) {
	filter = "(&(uid=C-*)(objectClass=*))";
	special = 0;
	return filter;
 }
 
//we are done with letters, let's do numbers now..
 if (firstLetter == 35 && secondLetter == 35) {

	// now we are really done!
		return null; 



 } else {
 
 //reset the 2nd letter index and increment the 1st letter index
	if (secondLetter == 35){
	   secondLetter = 0;
       firstLetter = firstLetter+1;
	} else {
	
		// increment the 2nd letter index,
		 secondLetter = secondLetter + 1;
		}
 filter = sf1+createSearchTerm()+sf2;
 task.logmsg('*********************************');
 task.logmsg("firstLetter="+firstLetter);
 task.logmsg("secondLetter="+secondLetter);
 task.logmsg('THE FILTER IS '+filter);
 task.logmsg('*********************************');
 return filter;
 }
}

function createSearchTerm(){
	var aa = 97;
	var num = 22;
	var char1;
	var char2;

	if (firstLetter < 26)
		char1 = String.fromCharCode(aa+firstLetter);
	else
		char1 = String.fromCharCode(num+firstLetter);


	if (secondLetter < 26)
		char2 = String.fromCharCode(aa+secondLetter);
	else
		char2 = String.fromCharCode(num+secondLetter);

	return char1+char2+'*';
}

