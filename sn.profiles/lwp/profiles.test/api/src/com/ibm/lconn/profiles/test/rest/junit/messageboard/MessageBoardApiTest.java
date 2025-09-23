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

import java.util.List;

import junit.framework.Assert;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.model.BoardEntry;
import com.ibm.lconn.profiles.test.rest.model.BoardFeed;
import com.ibm.lconn.profiles.test.rest.model.CommentEntry;
import com.ibm.lconn.profiles.test.rest.model.CommentFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileEntry;
import com.ibm.lconn.profiles.test.rest.model.ProfileFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.model.StatusEntry;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.GetBoardEntriesParameters;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;
import com.ibm.lconn.profiles.test.rest.util.TestProperties;
import com.ibm.lconn.profiles.test.rest.util.URLBuilder;

public class MessageBoardApiTest extends AbstractTest {

	public void testDeleteStatusMessage() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		String profileStatusLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_STATUS);

		// DELETE status message
		mainTransport.doAtomDelete(profileStatusLink, NO_HEADERS, HTTPResponseValidator.OK);
		Assert.assertNull(mainTransport.doAtomGet(Entry.class, profileStatusLink, NO_HEADERS, HTTPResponseValidator.NO_CONTENT));

	}

	public void testAddStatusMessage() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		String profileStatusLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_STATUS);
		String testStatusMessage = "testStatusEntryMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD new status message
		StatusEntry statusEntry = new StatusEntry();
		statusEntry.setContent(testStatusMessage);
		Entry entry = statusEntry.toEntry();
		mainTransport.doAtomPut(null, profileStatusLink, entry, NO_HEADERS, HTTPResponseValidator.OK);

		// GET newly added status message
		entry = mainTransport.doAtomGet(Entry.class, profileStatusLink, NO_HEADERS,
				HTTPResponseValidator.OK);
		// prettyPrint(entry);
		StatusEntry statusMessage2 = new StatusEntry(entry);
		Assert.assertEquals(testStatusMessage, statusMessage2.getContent());

		// GET again with no URL params (RTC 73856)
		profileStatusLink = URLBuilder.removeAllParameters(profileStatusLink);
		statusMessage2 = new StatusEntry(mainTransport.doAtomGet(Entry.class, profileStatusLink, NO_HEADERS,
				HTTPResponseValidator.OK));
		Assert.assertEquals(testStatusMessage, statusMessage2.getContent());
		
		// check "inclUserStatus" parameter ... undocumented parameter used by Mobile in v4.0/4.5
		// first, call GET without specifying parameter to make certain status is not included in response
		String profileFeedURL = profilesService.getProfileFeedUrl();
		Feed profileSelfFeedRaw = mainTransport.doAtomGet(Feed.class, profileFeedURL, NO_HEADERS, HTTPResponseValidator.OK);
		ProfileFeed profileSelfFeed = new ProfileFeed(profileSelfFeedRaw).validate();
		ProfileEntry profileEntry = profileSelfFeed.getEntries().get(0);
		Assert.assertNull("Status message unexpectedly included in response", profileEntry.getStatusMessage());
		// second, add "inclUserStatus" parameter and make certain status is included in response
		StringBuilder profileFeedURLInclUserStatus = new StringBuilder(profileFeedURL);
		URLBuilder.addQueryParameter(profileFeedURLInclUserStatus, "inclUserStatus", "true", false);
		profileSelfFeedRaw = mainTransport.doAtomGet(Feed.class, profileFeedURLInclUserStatus.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		profileSelfFeed = new ProfileFeed(profileSelfFeedRaw).validate();
		profileEntry = profileSelfFeed.getEntries().get(0);
		Assert.assertEquals(testStatusMessage, profileEntry.getStatusMessage());
		
		// verify unauthenticated response
		anonymousTransport.doAtomGet(Entry.class, profileStatusLink, NO_HEADERS,
				HTTPResponseValidator.UNAUTHORIZED);
	}

	public void testAddCommentToStatusMessage() throws Exception {
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));

		String profileStatusLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_STATUS);
		String testStatusMessage = "testStatusEntryMessage" + System.currentTimeMillis();
		String testComment = "testCommentStatusMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD new status message
		StatusEntry statusEntry = new StatusEntry();
		statusEntry.setContent(testStatusMessage);
		Entry entry = statusEntry.toEntry();
		mainTransport.doAtomPut(null, profileStatusLink, entry, NO_HEADERS, HTTPResponseValidator.OK);

		// GET newly added status message
		StatusEntry statusMessage2 = new StatusEntry(mainTransport.doAtomGet(Entry.class, profileStatusLink, NO_HEADERS,
				HTTPResponseValidator.OK));
		Assert.assertEquals(testStatusMessage, statusMessage2.getContent());

		// GET Comments of status message
		String commentsLink = statusMessage2.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

		Thread.sleep(sleepTime);
		// ADD comment to status message
		CommentEntry comment = new CommentEntry();
		comment.setContent(testComment);
		Entry commentEntry = comment.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry, NO_HEADERS, HTTPResponseValidator.CREATED);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 1);

	}

	public void testAddBoardEntry() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(TestProperties.getInstance().getOtherEmail(),true), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage_" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		ProfileService updatedProfilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(TestProperties.getInstance().getOtherEmail(),true), NO_HEADERS, HTTPResponseValidator.OK));
		String updatedBoardLink = updatedProfilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, updatedBoardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

	}

	public void testAddCommentToBoardEntry() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage" + System.currentTimeMillis();
		String testComment = "testCommentBoardEntryMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

		// GET Comments of Board Entry
		String commentsLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		CommentEntry comment = new CommentEntry();
		comment.setContent(testComment);
		Entry commentEntry = comment.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry, NO_HEADERS, HTTPResponseValidator.CREATED);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 1);

	}

	public void testDeleteCommentBoardEntry() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage" + System.currentTimeMillis();
		String testComment = "testCommentBoardEntryMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		Thread.sleep(sleepTime);
		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

		// GET Comments of Board Entry
		String commentsLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		CommentEntry comment = new CommentEntry();
		comment.setContent(testComment);
		Entry commentEntry = comment.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry, NO_HEADERS, HTTPResponseValidator.CREATED);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 1);

		// DELETING Comment of Board Entry
		CommentEntry comment1 = commentsFeed.getEntries().get(0);
		String deleteCommentLink = comment1.getLinkHref(ApiConstants.SocialNetworking.REL_SELF);
		mainTransport.doAtomDelete(deleteCommentLink, NO_HEADERS, HTTPResponseValidator.OK);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

	}

	public void testDeleteBoardEntry() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

		// DELETING Board Entry
		String deleteEntryLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.REL_SELF);
		mainTransport.doAtomDelete(deleteEntryLink, NO_HEADERS, HTTPResponseValidator.OK);

	}
	public void testAddBoardEntryToOthersBoard() throws Exception {

		// get other users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(TestProperties.getInstance().getOtherEmail(),true), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testOthersBoardMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

	}
	
	public void testGetBoardEntriesUsingPageSize() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		int pageSize = 20;
		params.setEmail(TestProperties.getInstance().getEmail());
		params.setPageSize(pageSize);
		urlBuilder.addQueryParameters(url, params);
		
		
		// GET Board Entry Feed
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(pageSize,boardFeed.getPageSize());

	}
	
	public void testGetBoardEntriesUsingCommentsNone() throws Exception {

		
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage" + System.currentTimeMillis();
		String testComment = "testCommentBoardEntryMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

		// GET Comments of Board Entry
		String commentsLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		CommentEntry comment = new CommentEntry();
		comment.setContent(testComment);
		Entry commentEntry = comment.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry, NO_HEADERS, HTTPResponseValidator.CREATED);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 1);
		
		
		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setComments("none");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		Feed boardEntriesFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean hasComments = false;
		for (Entry boardEntry : boardEntriesFeed.getEntries()){
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
	
	public void testGetBoardEntriesUsingCommentsAll() throws Exception {

		
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage" + System.currentTimeMillis();
		String testComment = "testCommentBoardEntryMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

		// GET Comments of Board Entry
		String commentsLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		CommentEntry comment = new CommentEntry();
		comment.setContent(testComment);
		Entry commentEntry = comment.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry, NO_HEADERS, HTTPResponseValidator.CREATED);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 1);
		
		
		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setComments("all");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		Feed boardEntriesFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean hasComments = false;
		for (Entry boardEntry : boardEntriesFeed.getEntries()){
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
	
	public void testGetBoardEntriesUsingCommentsInline() throws Exception {

		
		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testBoardMessage" + System.currentTimeMillis();
		String testComment = "testCommentBoardEntryMessage" + System.currentTimeMillis();

		Thread.sleep(sleepTime);
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		Assert.assertEquals(boardMessage.getContent(), testMessage);

		// GET Comments of Board Entry
		String commentsLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		CommentFeed commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 0);

		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		CommentEntry comment = new CommentEntry();
		comment.setContent(testComment);
		Entry commentEntry = comment.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry, NO_HEADERS, HTTPResponseValidator.CREATED);
		commentsFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, commentsLink, NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(commentsFeed.getEntries().size(), 1);
		
		
		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setComments("inline");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		boolean hasComments = false;
		for (BoardEntry be : boardEntriesFeed.getEntries()){
			if(be.getComments()!=null && be.getComments().size() > 0){
				hasComments = true;
			}
		}
		Assert.assertEquals(true, hasComments);
	}
	
	public void testGetBoardEntriesUsingMessageTypeStatus() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setMessageType("status");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		Feed boardEntriesFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean onlyStatusEntries = true;
		for (Entry boardEntry : boardEntriesFeed.getEntries()){
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
	
	public void testGetBoardEntriesUsingMessageTypeSimpleEntry() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setMessageType("simpleEntry");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		Feed boardEntriesFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		boolean onlyStatusEntries = true;
		for (Entry boardEntry : boardEntriesFeed.getEntries()){
			for(Category c : boardEntry.getCategories()){
				String term = c.getTerm();
				String sch = c.getScheme().toString();
				if (term.equals("status") && sch.equals(ApiConstants.SocialNetworking.SCHEME_MESSAGE_TYPE)){
					onlyStatusEntries = false;
					break;
				}
			}
			
		}
		Assert.assertEquals(true, onlyStatusEntries);
	}
	
	
	
	public void testGetBoardEntriesUsingSortOrderDescSortByPublished() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setSortOrder("desc");
		params.setSortBy("published");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = boardEntriesFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getCreated().before(entries.get(i).getCreated()));
			}
		}
	}
	public void testGetBoardEntriesUsingSortOrderAscSortByPublished() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setSortOrder("asc");
		params.setSortBy("published");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = boardEntriesFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getCreated().after(entries.get(i).getCreated()));
			}
		}
	}
	
	public void testGetBoardEntriesUsingSortOrderDescSortByLastMod() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setSortOrder("desc");
		params.setSortBy("lastMod");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = boardEntriesFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getUpdated().before(entries.get(i).getUpdated()));
			}
		}
	}
	public void testGetBoardEntriesUsingSortOrderAscSortByLastMod() throws Exception {

		// get other users profile service document
		StringBuilder url = new StringBuilder(urlBuilder.getProfilesBoardEntriesUrl());
		GetBoardEntriesParameters params = new GetBoardEntriesParameters();
		params.setSortOrder("asc");
		params.setSortBy("lastMod");
		params.setEmail(TestProperties.getInstance().getEmail());
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		BoardFeed boardEntriesFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<BoardEntry> entries = boardEntriesFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getUpdated().after(entries.get(i).getUpdated()));
			}
		}
	}
	
}
