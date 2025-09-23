/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2009, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.codes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.lconn.profiles.data.codes.AbstractCode;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.BaseCodesService;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.TestAppContext;

/*
 *
 */
public abstract class BaseCodesServiceTest<CT extends AbstractCode<?>, CS extends BaseCodesService<CT>> extends BaseTransactionalTestCase {

	protected final Class<? extends AbstractCode<?>> codeType;
	protected final Class<CS> serviceCls;
	protected BaseCodesService<CT> service = null;

	protected BaseCodesServiceTest(Class<? extends AbstractCode<?>> codeType, Class<CS> serviceCls) {
		this.codeType = codeType;
		this.serviceCls = serviceCls;
	}
	
	protected void onSetUpBeforeTransactionDelegate() throws Exception {
		if (service == null) {
			service = AppServiceContextAccess.getContextObject(serviceCls);
		}
		runAsAdmin(Boolean.TRUE);
	}
	
	/*
	 * Clears out old codes and creates test codes
	 */
	protected void onSetUpInTransaction() {
		List<CT> codes = service.getAll();
		for (CT c : codes){
			service.delete(c.getCodeId());
		}

		codes = codesToAdd();
		for (CT c: codes){
			service.create(c);
		}
	}
	
	/*
	 * Hook for sub-class to return a set of test data
	 */
	protected abstract List<CT> codesToAdd();
	
	/*
	 * Hook to creat new value to test update API
	 */
	protected abstract CT newValue(String codeId, Map<String,? extends Object> values);
	
	public final void testCreateRead() {
		boolean orig = service.codeCache().isEnabled();
		enableCache();
		tCreateRead();
		disableCache();
		tCreateRead();
		setCache(orig);
	}
	
	public final void testUpdate() {
		boolean orig = service.codeCache().isEnabled();
		enableCache();
		tUpdate(); //tCreateRead();
		disableCache();
		tUpdate(); //tCreateRead();
		setCache(orig);
	}
	
	public final void testDelete() {
		boolean orig = service.codeCache().isEnabled();
		enableCache();
		tDelete(); //tCreateRead();
		disableCache();
		tDelete();
		setCache(orig); //setCache(orig);
	}
	
	private void enableCache() {
		service.codeCache().enable();
	}

	private void disableCache() {
		service.codeCache().disable();
	}

	private void setCache(boolean orig) {
		if (orig) enableCache();
		else disableCache();
	}

	protected void tCreateRead() {
		// test getAll
		Map<String,CT> expected = new HashMap<String,CT>();
		for (CT c : codesToAdd()) expected.put(c.getCodeId(), c);
		
		Map<String,CT> actual = new HashMap<String,CT>();
		for (CT c : codesToAdd()) actual.put(c.getCodeId(), c);

		assertEquals(expected, actual);
		
		// test getById
		for (CT c : expected.values())
			assertEquals(c, service.getById(c.getCodeId()));
	}
	
	protected void tUpdate() {
		Map<String,CT> expected = new HashMap<String,CT>();		
		for (CT c : codesToAdd()) expected.put(c.getCodeId(), c);
		
		CT before = getRandomCode();
		CT after = null;
		// find a code that is not equal to 'before'
		do {
			after = getRandomCode();
		} while (before.equals(after));
		
		after = newValue(before.getCodeId(), after.valueMap());
		service.update(after);
		System.out.println("After: " + after);
		
		assertEquals(after, service.getById(before.getCodeId()));
	}
	
	protected void tDelete() {
		Map<String,CT> expected = new HashMap<String,CT>();		
		for (CT c : codesToAdd()){
			System.out.println("adding codeId: "+ c.getCodeId());
			expected.put(c.getCodeId(), c);
		}
		
		CT toDel = getRandomCode();
		System.out.println("deleting codeId: "+ toDel.getCodeId());
		service.delete(toDel.getCodeId());
		expected.remove(toDel.getCodeId()); //expected.remove(toDel);
		
		Map<String,CT> actual = new HashMap<String,CT>();
		for (CT c : codesToAdd()){
			if ( c.getCodeId().equals(toDel.getCodeId())== false){
				actual.put(c.getCodeId(), c);
			}
		}
		assertEquals(expected, actual);
	}
	
	protected final CT getRandomCode() {
		List<CT> codes = codesToAdd();
		int rand = new java.util.Random().nextInt(codes.size());
		int i = 0;
		
		for (CT c : codes) {
			if (i++ == rand)
				return c;
		}
		
		return null;
	}
	
}
