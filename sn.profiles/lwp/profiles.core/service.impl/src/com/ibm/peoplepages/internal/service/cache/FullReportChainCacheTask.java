/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2007, 2010                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.cache;

import java.sql.Timestamp;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FullReportChainCacheTask extends TimerTask
{
  private static final Log LOG = LogFactory.getLog(FullReportChainCacheTask.class);

  public void run()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("FullReportChainCacheTask starting at " + new Timestamp(System.currentTimeMillis()));
    }

    FullRprtToChainCache reportsChain = FullRprtToChainCache.getInstance();
    reportsChain.loadCache();

    if (LOG.isDebugEnabled())
    {
      LOG.debug("FullReportChainCacheTask completed at " + new Timestamp(System.currentTimeMillis()));
    }
  }

}
