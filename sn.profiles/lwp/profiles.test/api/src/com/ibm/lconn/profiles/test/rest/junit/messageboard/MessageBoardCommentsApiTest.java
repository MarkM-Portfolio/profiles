/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2012                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.rest.junit.messageboard;

import java.util.List;

import junit.framework.Assert;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;

import com.ibm.lconn.profiles.test.rest.junit.AbstractTest;
import com.ibm.lconn.profiles.test.rest.model.BoardEntry;
import com.ibm.lconn.profiles.test.rest.model.BoardFeed;
import com.ibm.lconn.profiles.test.rest.model.CommentEntry;
import com.ibm.lconn.profiles.test.rest.model.CommentFeed;
import com.ibm.lconn.profiles.test.rest.model.ProfileService;
import com.ibm.lconn.profiles.test.rest.util.ApiConstants;
import com.ibm.lconn.profiles.test.rest.util.GetEntryCommentsParameters;
import com.ibm.lconn.profiles.test.rest.util.HTTPResponseValidator;

public class MessageBoardCommentsApiTest extends AbstractTest {

	public void testGetEnrtyCommentsUsingPageSize() throws Exception {

		// get the authenticated users profile service document
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		String testMessage = "testStatusMessage" + System.currentTimeMillis();
		
		// ADD Board Entry
		BoardEntry message = new BoardEntry();
		message.setContent(testMessage);
		Entry entry = message.toEntry();
		mainTransport.doAtomPost(Entry.class, boardLink, entry, NO_HEADERS, HTTPResponseValidator.CREATED);

		// GET Board Entry
		profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		String entryId = boardMessage.getAtomId().split("entry:")[1];
		
		String commentsLink = boardMessage.getLinkHref(ApiConstants.SocialNetworking.TERM_REPLIES);
		// ADD comment to Board Entry
		String testComment1 = "testComment1_" + System.currentTimeMillis();
		CommentEntry comment1 = new CommentEntry();
		comment1.setContent(testComment1);
		Entry commentEntry1 = comment1.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry1, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		String testComment2 = "testComment2_" + System.currentTimeMillis();
		CommentEntry comment2 = new CommentEntry();
		comment2.setContent(testComment2);
		Entry commentEntry2 = comment2.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry2, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		String testComment3 = "testComment3_" + System.currentTimeMillis();
		CommentEntry comment3 = new CommentEntry();
		comment3.setContent(testComment3);
		Entry commentEntry3 = comment3.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry3, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		String testComment4 = "testComment4_" + System.currentTimeMillis();
		CommentEntry comment4 = new CommentEntry();
		comment4.setContent(testComment4);
		Entry commentEntry4 = comment4.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry4, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		Thread.sleep(sleepTime);
		// ADD comment to Board Entry
		String testComment5 = "testComment5_" + System.currentTimeMillis();
		CommentEntry comment5 = new CommentEntry();
		comment5.setContent(testComment5);
		Entry commentEntry5 = comment5.toEntry();
		mainTransport.doAtomPost(Entry.class, commentsLink, commentEntry5, NO_HEADERS, HTTPResponseValidator.CREATED);
		
		StringBuilder url = new StringBuilder(urlBuilder.getEntryCommentsUrl());
		GetEntryCommentsParameters params = new GetEntryCommentsParameters();
		params.setEntryId(entryId);
		int pageSize = 2;
		params.setPageSize(pageSize);
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		CommentFeed cFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		Assert.assertEquals(pageSize, cFeed.getPageSize());

	}
	
	public void testGetEntryCommentsUsingPage() throws Exception{
		
		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		String entryId = boardMessage.getAtomId().split("entry:")[1];
		
		StringBuilder url = new StringBuilder(urlBuilder.getEntryCommentsUrl());
		GetEntryCommentsParameters params = new GetEntryCommentsParameters();
		params.setEntryId(entryId);
		int pageSize = 1;
		params.setPageSize(pageSize);
		params.setPage("2");
		urlBuilder.addQueryParameters(url, params);
		
		// GET Comment Entry Feed
		Feed cFeed = mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK);
		Assert.assertEquals(2, Integer.parseInt(cFeed.getExtension(ApiConstants.OpenSearch.QN_START_INDEX).getText()));
		
		
	}
	
	public void testGetEntryCommentsUsingSortOrderDescSortByPublished() throws Exception {

		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		String entryId = boardMessage.getAtomId().split("entry:")[1];
		
		StringBuilder url = new StringBuilder(urlBuilder.getEntryCommentsUrl());
		GetEntryCommentsParameters params = new GetEntryCommentsParameters();
		params.setEntryId(entryId);
		params.setSortOrder("desc");
		params.setSortBy("published");
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		CommentFeed cFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<CommentEntry> entries = cFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getCreated().before(entries.get(i).getCreated()));
			}
		}
	}
	
	public void testGetEntryCommentsUsingSortOrderAscSortByPublished() throws Exception {

		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		String entryId = boardMessage.getAtomId().split("entry:")[1];
		
		StringBuilder url = new StringBuilder(urlBuilder.getEntryCommentsUrl());
		GetEntryCommentsParameters params = new GetEntryCommentsParameters();
		params.setEntryId(entryId);
		params.setSortOrder("asc");
		params.setSortBy("published");
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		CommentFeed cFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<CommentEntry> entries = cFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getCreated().after(entries.get(i).getCreated()));
			}
		}
	}
	
	public void testGetEntryCommentsUsingSortOrderDescSortByLastMod() throws Exception {

		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		String entryId = boardMessage.getAtomId().split("entry:")[1];
		
		StringBuilder url = new StringBuilder(urlBuilder.getEntryCommentsUrl());
		GetEntryCommentsParameters params = new GetEntryCommentsParameters();
		params.setEntryId(entryId);
		params.setSortOrder("desc");
		params.setSortBy("lastMod");
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		CommentFeed cFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<CommentEntry> entries = cFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getUpdated().before(entries.get(i).getUpdated()));
			}
		}
	}
	
	public void testGetEntryCommentsUsingSortOrderAscSortByLastMod() throws Exception {

		ProfileService profilesService = ProfileService.parseFrom(mainTransport.doAtomGet(Service.class,
				urlBuilder.getProfilesServiceDocument(), NO_HEADERS, HTTPResponseValidator.OK));
		String boardLink = profilesService.getLinkHref(ApiConstants.SocialNetworking.REL_BOARD);
		
		BoardFeed boardFeed = new BoardFeed(mainTransport.doAtomGet(Feed.class, boardLink, NO_HEADERS, HTTPResponseValidator.OK));
		BoardEntry boardMessage = boardFeed.getEntries().get(0);
		String entryId = boardMessage.getAtomId().split("entry:")[1];
		
		StringBuilder url = new StringBuilder(urlBuilder.getEntryCommentsUrl());
		GetEntryCommentsParameters params = new GetEntryCommentsParameters();
		params.setEntryId(entryId);
		params.setSortOrder("asc");
		params.setSortBy("lastMod");
		urlBuilder.addQueryParameters(url, params);
		
		// GET Board Entry Feed
		CommentFeed cFeed = new CommentFeed(mainTransport.doAtomGet(Feed.class, url.toString(), NO_HEADERS, HTTPResponseValidator.OK));
		List<CommentEntry> entries = cFeed.getEntries();
		if(entries.size() > 1){
			for (int i=0;i<entries.size()-1;i++){
				Assert.assertEquals(true, entries.get(i+1).getUpdated().after(entries.get(i).getUpdated()));
			}
		}
	}

}
