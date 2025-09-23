/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.internal.service.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.CacheConfig.ObjectCacheConfig;
import com.ibm.lconn.profiles.data.EmployeeCollection;
import com.ibm.lconn.profiles.data.ReportToRetrievalOptions;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.exception.DataAccessRetrieveException;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.resources.ResourceManager;
import com.ibm.peoplepages.util.appcntx.AdminContext;


public class FullRprtToChainCache extends BaseCache
{
  private Log LOG = LogFactory.getLog(FullRprtToChainCache.class);

  private static class Holder {
	  protected static final FullRprtToChainCache instance = new FullRprtToChainCache();
  }
  
  public static FullRprtToChainCache getInstance()
  {
    return Holder.instance;
  }

  /**
   * This creation method does a simple init of a hashtable with 500 elements.
   */
   FullRprtToChainCache()
  {
    this(500);
  }

  /**
   * This creation method does a simple init of a hashtable
   * where size is specified by initSize.
   * @param initSize the initial size of the hashtable
   */
  private FullRprtToChainCache(int initSize)
  {
    cacheStatKey = FullRprtToChainCache.class.getName();
    ht = new java.util.concurrent.ConcurrentHashMap<String,Object>(initSize);
  }

  public void loadCache()
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("loading cache");
    }
    final long start = System.currentTimeMillis();
    
    Map<String,Object> mHT = new java.util.concurrent.ConcurrentHashMap<String,Object>();
    
    ObjectCacheConfig config = ProfilesConfig.instance().getCacheConfig().getFullReportsToChainConfig();
    int preloadMax = config.getSize();
    String ceouid = config.getCEOUid();
    Employee e = null;
    ArrayList<String> uids = new ArrayList<String>(preloadMax);
    OrgStructureService orgStructSvc = AppServiceContextAccess.getContextObject(OrgStructureService.class);
    
    // TODO: RTC 80971: make MT/multi-tenant aware
    AppServiceContextAccess.setContext((ApplicationContext) AdminContext.getInternalProcessContext(Tenant.SINGLETENANT_KEY));
    
    uids.add(ceouid);

    for (int i = 0; i < uids.size(); i++)
    {
      String uid = uids.get(i);
      List<Employee> people = new ArrayList<Employee>();
      try
      {
    	  ReportToRetrievalOptions setOptions = new ReportToRetrievalOptions();
    	  setOptions.setProfileOptions(ProfileRetrievalOptions.EVERYTHING);
    	  setOptions.setIncludeCount(false);
    	  EmployeeCollection ecoll = orgStructSvc.getPeopleManaged(ProfileLookupKey.forUid(uid),setOptions);
    	  people = ecoll.getResults();
      }
      catch (DataAccessRetrieveException dare)
      {
        LOG.error(dare.getMessage(), dare);
      }
      Iterator<Employee> iPeople = people.iterator();

      boolean REACHEDMAX = false;

      while ((iPeople.hasNext()) && (!REACHEDMAX))
      {
        if (getCacheSize() >= preloadMax)
        {
          REACHEDMAX = true;
          i = uids.size() - 1;

        }
        else
        {
          e = iPeople.next();
          addCacheEntry(mHT, e.getUid(), e);
          uids.add(e.getUid());
        }
      }

    }
    if (LOG.isInfoEnabled())
    {
      Object[] tokens = {new Integer(getCacheSize())};
      LOG.info(ResourceManager.format("info.cache.fullreportchain.size", tokens));
    }
    long loadTime = (int)(System.currentTimeMillis() - start);
    
    // switch to new HT
    this.ht = mHT;
    
    INITIALIZED = true;

    if (LOG.isInfoEnabled())
    {
      Object[] tokens = {new Long(loadTime)};
      LOG.info(ResourceManager.format("info.cache.fullreportchain.loadtime", tokens));
    }
  }
}
