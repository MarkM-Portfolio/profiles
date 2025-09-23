/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2011, 2012                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.model;

import java.util.ArrayList;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

public class BoardFeed extends AtomFeed<BoardEntry>
{

  public BoardFeed(Feed f) throws Exception
  {
    super(f);

    // get the entry children
    entries = new ArrayList<BoardEntry>(f.getEntries().size());
    for (Entry e : f.getEntries())
    {
      entries.add(new BoardEntry(e));
    }
  }

  public BoardFeed validate() throws Exception
  {
    super.validate();
    for (BoardEntry e : entries)
    {
      e.validate();
    }
    return this;
  }

}
