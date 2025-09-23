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
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.data;

import java.util.Date;

public interface DatabaseRecord
{
    public String getRecordTitle();
    public String getRecordType();
    public String getRecordSearchString();
    public String getRecordSummary();
    public Date getRecordUpdated();
    public String getRecordId();
}
