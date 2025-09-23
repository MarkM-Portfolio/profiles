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

function func_map_to_db_SECRETARY_UID(fieldname) {
	var result = null;

	/*---------------------------------------------------------------
	 * In IBM, the UID is the serial number concatenated with the
	 * country code field, so we will grab both values from work
	 * and concatenate them                                          
	 *---------------------------------------------------------------*/
	var serial = work.getString("secretaryserialnumber");
	var cc = work.getString("secretarycountrycode");
	/*task.logmsg("+++serial is " + serial + ", cc is " + cc);*/
	if ( (serial != null) && (cc != null) ) {
		result = serial + cc;
	}
	
	return result;
}

function func_map_to_db_UID(fieldname) {
	var result = null;

	/*---------------------------------------------------------------
	 * In IBM, the UID is the uid field.  It can also be found in
	 * the DN, if the uid field is not available, which could 
	 * happen on updates from the TDS changelog. So we will look
	 * first for the uid field, and if not available, get from
	 * the DN.  The DN is $dn in the LDAP connector.                                     
	 *---------------------------------------------------------------*/
	result = work.getString("uid");
	if(result == null) {
		var dn = work.getString("$dn");
		if(dn != null) {
			if(dn.startsWith("uid=")) {
				var commaPos = dn.indexOf(",");
				if(commaPos > 4) {
					result = dn.substring(4, commaPos);
				}
			}
			else {
				var start = dn.indexOf(",uid=");
				if(start > 0) {
					start = start + 5; /* skip past ,uid= */
					var commaPos = dn.indexOf(",", start);
					if(commaPos > start) {
						result = dn.substring(start, commaPos);
					}
				}
			}
		}
	}

	return result;
}
