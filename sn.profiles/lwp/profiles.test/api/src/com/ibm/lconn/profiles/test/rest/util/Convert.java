/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2009, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class Convert
{
  public static String toURLEncoded(String s)
  {
    try
    {
      return SPACE_ENCODER.matcher(URLEncoder.encode(s, "UTF-8")).replaceAll("%20");
    }
    catch (UnsupportedEncodingException e)
    {
      throw new IllegalStateException("UTF-8 not supported", e);
    }
  }

  private static final Pattern SPACE_ENCODER = Pattern.compile("[+]");

  public static String toURLDecoded(String s)
  {
    try
    {
      return URLDecoder.decode(s, "utf-8");
    }
    catch (UnsupportedEncodingException e)
    {
      return s;
    }
  }

  public static final char HEX_VALUES[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  final static int BYTES_PER_HEX_DIGIT = 4;

  public static String UUID_toString(UUID uuid)
  {
    if (uuid == null)
      return null;

    // Normal form is 32 hex digits (128-bit) in 5 groups (4 separating dashes)
    char[] buf = new char[32 + 4];
    int charPos = buf.length;
    int radix = 1 << BYTES_PER_HEX_DIGIT;
    long mask = radix - 1;
    long msb = uuid.getMostSignificantBits();
    long lsb = uuid.getLeastSignificantBits();

    do
    {
      if (charPos == 24)
      {
        buf[--charPos] = '-';
      }

      // Pad with zeros as needed
      if (lsb == 0)
      {
        buf[--charPos] = '0';
      }
      else
      {
        buf[--charPos] = HEX_VALUES[(int) (lsb & mask)];
        lsb >>>= BYTES_PER_HEX_DIGIT;
      }
    }
    while (charPos > 19);

    do
    {
      if (charPos == 9 || charPos == 14 || charPos == 19)
      {
        buf[--charPos] = '-';
      }

      // Pad with zeros as needed
      if (msb == 0)
      {
        buf[--charPos] = '0';
      }
      else
      {
        buf[--charPos] = HEX_VALUES[(int) (msb & mask)];
        msb >>>= BYTES_PER_HEX_DIGIT;
      }
    }
    while (charPos > 0);

    return new String(buf);
  }

  public static final Map<String, Object> getParameters(String queryString)
  {
    Map<String, Object> parameters = new LinkedHashMap<String, Object>();
    try
    {
      if (queryString != null)
      {
        StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
        while (tokenizer.hasMoreTokens())
        {
          String token = tokenizer.nextToken();
          int equalsMark = token.indexOf("=");
          if (equalsMark != -1)
          {
            String key = token.substring(0, equalsMark);
            String value = token.substring(equalsMark + 1);
            if (key.length() > 0 && value.length() > 0)
            {
              key = URLDecoder.decode(key, "UTF-8");
              value = URLDecoder.decode(value, "UTF-8");

              Object existingValue = parameters.put(key, value);
              if (existingValue instanceof String[])
              {
                String[] existingArray = (String[]) existingValue;
                String[] newArray = new String[existingArray.length + 1];
                newArray[existingArray.length] = value;
                System.arraycopy(existingArray, 0, newArray, 0, existingArray.length);
              }
              else if (existingValue instanceof String)
                parameters.put(key, new String[] { (String) existingValue, value });
            }
          }
        }
      }
    }
    catch (UnsupportedEncodingException e)
    {
      throw new UnsupportedOperationException(e);
    }
    return parameters;
  }

  public static final String writeParameters(Map map)
  {
    String query = "";

    if (map != null && !map.isEmpty())
    {
      StringBuilder sbf = new StringBuilder(100);
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();)
      {
        Map.Entry entry = (Map.Entry) iter.next();
        if (entry.getKey() == null)
          continue;

        String key = entry.getKey().toString();
        if (entry.getValue() != null && entry.getValue().getClass().isArray())
        {
          Object[] values = (Object[]) entry.getValue();
          for (int i = 0; i < values.length; i++)
          {
            if (sbf.length() > 0)
              sbf.append("&");

            sbf.append(Convert.toURLEncoded(key.toString()));
            sbf.append("=");
            if (values[i] != null)
              sbf.append(Convert.toURLEncoded(values[i].toString()));
          }
        }
        else
        {
          if (sbf.length() > 0)
            sbf.append("&");

          sbf.append(Convert.toURLEncoded(key.toString()));
          sbf.append("=");
          if (entry.getValue() != null)
            sbf.append(Convert.toURLEncoded(entry.getValue().toString()));
        }
      }
      query = sbf.toString();
    }

    return query;
  }

  public static final StringBuilder writeParameters(Map map, StringBuilder sbf)
  {
    if (sbf == null)
    {
      sbf = new StringBuilder(100);
    }
    if (map != null && !map.isEmpty())
    {
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();)
      {
        Map.Entry entry = (Map.Entry) iter.next();
        if (entry.getKey() == null)
          continue;

        if (sbf.length() > 0)
          sbf.append("&");

        String key = entry.getKey().toString();
        if (entry.getValue() != null && entry.getValue().getClass().isArray())
        {
          Object[] values = (Object[]) entry.getValue();
          for (int i = 0; i < values.length; i++)
          {
            sbf.append(Convert.toURLEncoded(key.toString()));
            sbf.append("=");
            if (values[i] != null)
              sbf.append(Convert.toURLEncoded(values[i].toString()));
          }
        }
        else
        {
          sbf.append(Convert.toURLEncoded(key.toString()));
          sbf.append("=");
          if (entry.getValue() != null)
            sbf.append(Convert.toURLEncoded(entry.getValue().toString()));
        }
      }
    }
    return sbf;
  }
}
