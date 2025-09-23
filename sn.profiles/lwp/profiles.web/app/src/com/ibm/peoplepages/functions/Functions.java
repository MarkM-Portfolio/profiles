/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2006, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.peoplepages.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ibm.connections.directory.services.data.DSObject;
import com.ibm.connections.directory.services.exception.DSException;
import com.ibm.lconn.core.web.util.resourcebundle.UILabelConfig;
import com.ibm.lconn.profiles.config.ProfilesConfig;
import com.ibm.lconn.profiles.config.ui.UIAttributeConfig;
import com.ibm.lconn.profiles.internal.util.ActiveContentFilter;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClient;
import com.ibm.lconn.profiles.internal.util.waltz.WaltzClientFactory;
import com.ibm.peoplepages.util.StringUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;


/**
 * @author sberajaw
 */
public class Functions
{
  private static String JAVASCRIPT = "javascript";
  
  private static final Logger logger = Logger.getLogger(Functions.class.getName());
  
  public static<Key,Value> Map<Key,Value> convertToMap(Map<Key,Value> enumeratedMap)
  {
    return new HashMap<Key,Value>(enumeratedMap);
  }
  
  public static<Key,Value> Map<Key,Value> removeParam(Map<Key,Value> parameters, String name)
  {
	parameters.remove(name);
	return new HashMap<Key,Value>(parameters);
  }  
  
	public static String getTenantName() {
		return getTenantName(null);
	}
	
	public static String getTenantName(String orgId) {
		String ret = null;
		if (orgId == null) {
			Employee currUser = AppContextAccess.getCurrentUserProfile();
			if (currUser != null) {
				orgId = currUser.getTenantKey();
			}
		}
		// use directory service to look up the org. it has a central cache that almost certainly has the
		// object as someone from the org must be accessing via the ui. we don't want to build yet another
		// cache.
		try {
			if (orgId != null) {
				WaltzClient wc = WaltzClientFactory.INSTANCE().getWaltzClient();
				DSObject o = wc.exactOrganizationIDMatch(orgId);
				if (o != null) {
					ret = o.get_name();
				}
			}
		}
		catch (DSException e) {
			logger.log(Level.WARNING,"error retrieving org from directory services: "+orgId);
		}
		return ret;
	} 
  
  public static String removeTagString(String tagParam, String tag){
	
	  List tagList = StringUtil.parseTags(tagParam);	 
	  tagList.remove(tag);	  
	  String newTags = StringUtil.convertTagListToString(tagList);
	  return newTags;
  }

  public static<Key,Value> Map<Key,Value> replaceParam(Map<Key,Value> parameters, String name, String value)
  {
    return new HashMap<Key,Value>(parameters);
  }

  public static String filter(String source)
  {
    return ActiveContentFilter.filter(source);
  }


  public static String encodeForJsonString(String str)
  {
    //we will NOT encode the apos.  This function assumes that the json string will 
    //be enclosed with double quotes!
    
    // We will use this method to endode these characters in
    // so they can be inserted into a JSON string
	//  \n  \r  \t  \b  \f  \  "  
    if (str != null)
    {
      int strlen = str.length();
      StringBuffer sb = new StringBuffer(strlen + strlen / 4);

      for (int i = 0; i < strlen; i++)
      {
        char pos = str.charAt(i);

        switch (pos)
        {
          case '\\':
            sb.append("\\\\");
            break;
          case '"':
            sb.append("\\\"");
            break;
          case '\n':
            sb.append("\\n");
            break;
          case '\r':
            sb.append("\\r");
            break;
          case '\t':
            sb.append("\\t");
            break;
          case '\b':
            sb.append("\\b");
            break;
          case '\f':
            sb.append("\\f");
            break;
          default:
            sb.append(pos);
            break;
        }
      }

      return sb.toString();
    }
    else
    {
      return null;
    }
  }  
  
  public static String escapeUnwiseURLChars(String uri)
  {

    // According to the RFC 2396 under the section:
    //   2.4.3. Excluded US-ASCII Characters,
    // spaces are disallowed in addition to the following unwise
    // characters:
    //      "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`" | "<" | ">"
    // We will use this method to escape the space and unwise characters in
    // the a URI since they are found to be used in practice, eventhough unwise
    // and have no special meaning in the URI syntax.
    if (uri != null)
    {
      if (uri.regionMatches(true, 0, JAVASCRIPT, 0, JAVASCRIPT.length()))
      {
        uri = uri.substring(JAVASCRIPT.length());
      }

      StringBuffer sb = new StringBuffer(uri.length() + uri.length() / 4);

      for (int i = 0; i < uri.length(); i++)
      {
        char pos = uri.charAt(i);

        switch (pos)
        {
          case '|':
            sb.append("%7C");
            break;
          case ' ':
            sb.append("%20");
            break;
          case '\\':
            sb.append("%5C");
            break;
          case '^':
            sb.append("%5E");
            break;
          case '{':
            sb.append("%7B");
            break;
          case '}':
            sb.append("%7D");
            break;
          case '[':
            sb.append("%5B");
            break;
          case ']':
            sb.append("%5D");
            break;
          case '`':
            sb.append("%60");
            break;
          case '<':
            sb.append("%3C");
            break;
          case '>':
            sb.append("%3E");
            break;
          default:
            sb.append(pos);
            break;
        }
      }

      return sb.toString();
    }
    else
    {
      return null;
    }
  }

  /**
   * 
   * 
   * @param attrId
   * @return
   */
  public static UILabelConfig findAdvancedSearchLabel(String attrId) {
	  UIAttributeConfig config = ProfilesConfig.instance().getSFormLayoutConfig().getAttributeMap().get(attrId);
	  
	  if (config != null) {
		  UILabelConfig label = config.getLabel();
		  if (label != null && label.getBidref() == null 
			  && "label.advanced.searchForm.attribute.tags".equals(label.getKey())) 
		  {
			  return tagsCorrected;
		  }
		  return config.getLabel();
	  }
	  
	  return null;  
  }
  
  public static boolean allowJsonp() {
	  return ProfilesConfig.instance().getDataAccessConfig().isAllowJsonpJavelin();
  }
  
  private static final UILabelConfig tagsCorrected = 
	  new UILabelConfig(null, "label.advanced.searchForm.attribute.profileTags");
}
