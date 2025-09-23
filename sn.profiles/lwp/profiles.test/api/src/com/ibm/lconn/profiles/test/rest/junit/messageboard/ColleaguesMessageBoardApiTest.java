/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012, 2013                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.messageboard;

import java.text.SimpleDateFormat;
import java.util.List;
import junit.framework.Assert;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.junit.connection.ColleagueTest;
import com.ibm.lconn.profiles.test.rest.model.BoardEntry;
import com.ibm.lconn.profiles.test.rest.model.BoardFeed;
import com.ibm.lconn.profiles.test.rest.model.ColleagueConnection;
import com.ibm.lconn.profiles.test.rest.model.ConnectionEntry.STATUS;
import com.ibm.lconn.profiles.test.rest.model.CommentEntry;
import com.ibm.lconn.profiles.test.rest.model.CommentFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.GetColleaguesEntriesParameters;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;
import com.ibm.lconn.profiles.test.rest.util.VerifyColleaguesParameters;

public class ColleaguesMessageBoardApiTest extends AbstractTest {

	
	public void testGetMessagesOfColleagues() throws Exception{
		
		setUpColleagueConnection();
		
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(TestProperties.getInstance().getOtherEmail(),true), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testOthersBoardMessage" + System.currentTimeMillis()+"_1";

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(TestProperties.getInstance().getOtherEmail(),true), NO_HEADERS, HTTPResponseValidator.OK));
		boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		
		Thread.sleep(sleepTime);
		String commentsLink1 = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed1 = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink1, NO_HEADERS, HTTPResponseValidator.OK));
		CommentEntry comment1 = new CommentEntry();
		comment1.setContent("testCommentMessage1_" + System.currentTimeMillis()+"_1");
		Entry commentEntry1 = comment1.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink1, commentEntry1, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		URLBuilder.addQueryParameters(url, params);
		
		// GET Comment Entry Feed
		Feed entryFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		for(Entry ent : entryFeed.getEntries()){
			String colleagueEmail = ent.getAuthor().getEmail();
			if(!userEmail.equals(colleagueEmail)){
				VerifyColleaguesParameters parameters = new VerifyColleaguesParameters();
				parameters.setSourceEmail(userEmail);
				parameters.setTargetEmail(colleagueEmail);
				StringBuilder url2 = new StringBuilder(urlBuilder.getVerifyColleaguesUrl());
				URLBuilder.addQueryParameters(url2, parameters);
				Entry e = mainTransport.doAtomGet(Entry.class, url2.toString(), NO_HEADERS, HTTPResponseValidator.OK);
				boolean connected = false;
				for (Category category:e.getCategories()){
					if(category.getTerm().equals("accepted")){
						connected = true;
						break;
					}
				}
				Assert.assertEquals(true, connected);
			}
		}
		
		tearDownColleagueConnection();
	}
	
	public void testGetMessagesOfColleaguesUsingPageSize() throws Exception{
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		int pageSize = 2;
		params.setEmail(userEmail);
		params.setPageSize(pageSize);
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(entryFeed.getPageSize(), pageSize);
	}
	
	public void testGetMessagesOfColleaguesUsingCommentsAll() throws Exception{
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setComments("all");
		urlBuilder.addQueryParameters(url, params);
		
		Feed entryFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean hasComments = false;
		for (Entry boardEntry : entryFeed.getEntries()){
			for(Category c : boardEntry.getCategories()){
				String term = c.getTerm();
				if (term.equals("comment")){
					hasComments = true;
					break;
				}
			}
		}
		Assert.assertEquals(true, hasComments);
	}
	
	public void testGetMessagesOfColleaguesUsingCommentsNone() throws Exception{
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setComments("none");
		urlBuilder.addQueryParameters(url, params);
		
		Feed entryFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean hasComments = false;
		for (Entry boardEntry : entryFeed.getEntries()){
			for(Category c : boardEntry.getCategories()){
				String term = c.getTerm();
				if (term.equals("comment")){
					hasComments = true;
					break;
				}
			}
		}
		Assert.assertEquals(false, hasComments);
	}
	
	public void testGetMessagesOfColleaguesUsingMessageTypeStatus() throws Exception{
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setMessageType("status");
		urlBuilder.addQueryParameters(url, params);
		
		Feed entryFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean onlyStatusEntries = true;
		for (Entry boardEntry : entryFeed.getEntries()){
			for(Category c : boardEntry.getCategories()){
				String term = c.getTerm();
				String sch = c.getScheme().toString();
				if (term.equals("simpleEntry") && sch.equals(ApiConstants.SocialNetworking.SCHEME_MESSAGE_TYPE)){
					onlyStatusEntries = false;
					break;
				}
			}
		}
		Assert.assertEquals(true, onlyStatusEntries);
	}
	
	public void testGetMessagesOfColleaguesUsingMessageTypeSimpleEntry() throws Exception{
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setMessageType("simpleEntry");
		urlBuilder.addQueryParameters(url, params);
		
		Feed entryFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean onlySimpleEntries = true;
		for (Entry boardEntry : entryFeed.getEntries()){
			for(Category c : boardEntry.getCategories()){
				String term = c.getTerm();
				String sch = c.getScheme().toString();
				if (term.equals("status") && sch.equals(ApiConstants.SocialNetworking.SCHEME_MESSAGE_TYPE)){
					onlySimpleEntries = false;
					break;
				}
			}
		}
		Assert.assertEquals(true, onlySimpleEntries);
	}

	public void testGetMessagesOfColleaguesUsingSortOrderDescSortByPublished() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setSortBy("published");
		params.setSortOrder("desc");
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = entryFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getCreated().before(entries.get(i).getCreated()));
			}
		}
	}
	
	public void testGetMessagesOfColleaguesUsingSortOrderAscSortByPublished() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setSortBy("published");
		params.setSortOrder("asc");
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = entryFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getCreated().after(entries.get(i).getCreated()));
			}
		}
	}
	
	public void testGetMessagesOfColleaguesUsingSortOrderDescSortByLastMod() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setSortBy("lastMod");
		params.setSortOrder("desc");
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = entryFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getUpdated().before(entries.get(i).getUpdated()));
			}
		}
	}
	
	public void testGetMessagesOfColleaguesUsingSortOrderAscSortByLastMod() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setSortBy("lastMod");
		params.setSortOrder("asc");
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = entryFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getUpdated().after(entries.get(i).getUpdated()));
			}
		}
	}
	
	
	public void testGetMessagesOfColleaguesUsingSinceAndSinceEntryIdDesc() throws Exception {
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setPageSize(10);
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage1 = entryFeed.getEntries().get(0);
		BoardEntry boardMessage2 = entryFeed.getEntries().get(1);
		BoardEntry boardMessage3 = entryFeed.getEntries().get(3);

		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.S");
		String tempDate = formater.format(boardMessage3.getCreated());
		String offset = ""+boardMessage3.getCreated().getTimezoneOffset();
		String since = ""+tempDate.split(",")[0]+"T"+tempDate.split(",")[1]+offset;

		String sinceEntryId = boardMessage3.getAtomId().split("entry:")[1];
		
		url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		params.setSince(since);
		params.setSinceEntryId(sinceEntryId);
		params.setSortBy("published");
		params.setSortOrder("desc");
		params.setPageSize(10);
		urlBuilder.addQueryParameters(url, params);

		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		if(boardEntriesFeed.getEntries().size() > 1){
			for (int i=0;i<boardEntriesFeed.getEntries().size()-1;i++){
				Assert.assertEquals(true, (boardEntriesFeed.getEntries().get(i+1).getCreated().before(boardEntriesFeed.getEntries().get(i).getCreated()) || boardEntriesFeed.getEntries().get(i+1).getCreated().equals(boardEntriesFeed.getEntries().get(i).getCreated())));
			}
		}
	}
	
	public void testGetMessagesOfColleaguesUsingSinceAndSinceEntryIdAsc() throws Exception {
		
		StringBuilder url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		GetColleaguesEntriesParameters params = new GetColleaguesEntriesParameters();
		String userEmail = TestProperties.getInstance().getOtherEmail();
		params.setEmail(userEmail);
		params.setPageSize(10);
		urlBuilder.addQueryParameters(url, params);
		
		BoardFeed entryFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage1 = entryFeed.getEntries().get(0);
		BoardEntry boardMessage2 = entryFeed.getEntries().get(1);
		BoardEntry boardMessage3 = entryFeed.getEntries().get(3);
		
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss.S");
		String tempDate = formater.format(boardMessage3.getCreated());
		String offset = ""+boardMessage3.getCreated().getTimezoneOffset();
		String since = ""+tempDate.split(",")[0]+"T"+tempDate.split(",")[1]+offset;
		String sinceEntryId = boardMessage3.getAtomId().split("entry:")[1];
		
		url = new StringBuilder(urlBuilder.getColleagueEntriesUrl());
		params.setSince(since);
		params.setSinceEntryId(sinceEntryId);
		params.setSortBy("published");
		params.setSortOrder("asc");
		params.setPageSize(10);
		urlBuilder.addQueryParameters(url, params);

		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		if(boardEntriesFeed.getEntries().size() > 1){
			for (int i=0;i<boardEntriesFeed.getEntries().size()-1;i++){
				Assert.assertEquals(true, (boardEntriesFeed.getEntries().get(i+1).getCreated().after(boardEntriesFeed.getEntries().get(i).getCreated()) || boardEntriesFeed.getEntries().get(i+1).getCreated().equals(boardEntriesFeed.getEntries().get(i).getCreated())));
			}
		}
	}
	
	ColleagueConnection existingConnectionMain;
	ColleagueConnection existingConnectionOther;
	
	// override setUp()/tearDown() later if needed. At this time only one test requires the connection, so we only do this cycle once for that test
	private void setUpColleagueConnection() throws Exception {
		boolean doConnect = false;
		boolean doDelete = false;
		// save existing connection, if any, to restore later.
		existingConnectionMain = ColleagueTest.getColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		existingConnectionOther = ColleagueTest.getColleagueConnection(otherTransport, mainTransport, ColleagueConnection.STATUS_ALL, false);

		if (null == existingConnectionMain && null == existingConnectionOther) {
			// users are not connected, need to connect them
			doConnect = true;
		}
		else if (null != existingConnectionMain && null != existingConnectionOther) {
			// users are connected, if the connection is not confirmed delete+create 
			if(!STATUS.accepted.equals(existingConnectionMain.getStatus())) {
				doDelete = true;
				doConnect = true;				
			}
		}
		else {
			// unexpected state, clean up
			doDelete = true;
			// after delete, users are not connected, need to connect them
			doConnect = true;
			// set up for tearDown()
			existingConnectionMain = null;
			existingConnectionOther = null;
		}
		
		if (doDelete) {
			ColleagueTest.deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		}
		
		if(doConnect){
			String msg = System.currentTimeMillis() + " " + this.getClass().getSimpleName();
			ColleagueTest.createColleagueConnection(mainTransport, otherTransport, msg + " invitation message", msg + " accept message", true, false);
		}
	}
	/**
	 * restore connection to pre-test state
	 * 
	 * @throws Exception
	 */
	private void tearDownColleagueConnection() throws Exception {
		// restore old connection, if there was one. This isn't a "real restore" of the original connection, it only sets up a connection
		// between the users similar to before the test.
		if (null != existingConnectionMain && null != existingConnectionOther) {
			// first clean up test connection
			ColleagueTest.deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
			
			if (STATUS.pending.equals(existingConnectionMain.getStatus())) {
				ColleagueTest.createColleagueConnection(otherTransport, mainTransport, existingConnectionOther.getContent(),
						existingConnectionMain.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}
			else if (STATUS.unconfirmed.equals(existingConnectionMain.getStatus())) {
				ColleagueTest.createColleagueConnection(mainTransport, otherTransport, existingConnectionMain.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), true);
			}
			else {
				// TODO: if needed, determine invite/accept order for this case
				ColleagueTest.createColleagueConnection(mainTransport, otherTransport, existingConnectionMain.getContent(),
						existingConnectionOther.getContent(), STATUS.accepted.equals(existingConnectionMain.getStatus()), false);
			}

			ColleagueConnection restoredConnectionMain = ColleagueTest.getColleagueConnection(mainTransport, otherTransport,
					ColleagueConnection.STATUS_ALL, false);
			ColleagueConnection restoredConnectionOther = ColleagueTest.getColleagueConnection(otherTransport, mainTransport,
					ColleagueConnection.STATUS_ALL, false);

			assertEquals(existingConnectionMain.getStatus(), restoredConnectionMain.getStatus());
			assertEquals(existingConnectionOther.getStatus(), restoredConnectionOther.getStatus());
		} else {
			// no valid prior connection, simply clean up test connection
			ColleagueTest.deleteColleagueConnection(mainTransport, otherTransport, ColleagueConnection.STATUS_ALL, false);
		}
	}
}
