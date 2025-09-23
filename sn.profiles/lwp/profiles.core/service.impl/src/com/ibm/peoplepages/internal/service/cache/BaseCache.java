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

package com.ibm.peoplepages.internal.service.cache;

import java.lang.ref.SoftReference;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class BaseCache
{
  protected boolean INITIALIZED = false;
  protected Map<String, Object> ht = null;
  protected String cacheStatKey;

  public abstract void loadCache();

  private Log LOG = LogFactory.getLog(BaseCache.class);

  public Object getCacheEntry(String key)
  {
    boolean hit = false;
    Object o = ht.get(key);
    if (o != null)
    {
      if (o instanceof SoftReference)
      {
    	  SoftReference<?> ref = (SoftReference<?>) o;
    	  o = ref.get();
    	  
    	  if (o == null)
    	  {
    		  ht.remove(key);
    	  }
    	  else
    	  {
    		  hit = true;
    	  }
      }
      else
      {
    	  hit = true;
      }
    }  
    return o;
  }

  public void addCacheEntry(String key, Object value)
  {
    ht.put(key, value);
  }
  
  public void addCacheEntry(Map<String, Object> localeHash, String key, Object value)
  {
	  localeHash.put(key, value);
  }

  public void addEntryAsSoftReference(String key, Object value)
  {
    ht.put(key, new SoftReference<Object>(value));
  }

  public void remove(String key)
  {
    if (ht.containsKey(key))
    { //we already have it
      ht.remove(key);
    }
  }

  public boolean isINITIALIZED()
  {
    return INITIALIZED;
  }

  public int getCacheSize()
  {
    return ht.size();
  }

  /**
   * Empties the cache.
   */
  public void resetCache()
  {
	  if (ht != null)
	  {
		  ht.clear();
	  }
  }
}
