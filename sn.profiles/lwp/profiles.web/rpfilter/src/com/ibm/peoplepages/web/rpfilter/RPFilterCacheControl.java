/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2007, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.peoplepages.web.rpfilter;

/**
 * Cache-Control object.
 * 
 *
 */
public final class RPFilterCacheControl
{
	private static final String MAX_AGE = "max-age";
	private static final String PROXY_MAX_AGE = "s-maxage";
	private static final String PUBLIC = "public";
	private static final String PRIVATE = "private";
	private static final String NO_CACHE = "no-cache";
	private static final String NO_STORE = "no-store";
	private static final String MUST_REVALIDATE = "must-revalidate";
	private static final String PROXY_REVALIDATE = "proxy-revalidate";
	
	private static final int PUBLIC_V = 1;
	private static final int PRIVATE_V = -1;
	private static final int NO_V = 0;

	private int maxAge = RPFilterResponse.UNSET_MAX_AGE;
	private int proxyMaxAge = RPFilterResponse.UNSET_MAX_AGE;
	private boolean noCache = false;
	private boolean noStore = false;
	private boolean proxyRevalidate = false;
	private boolean mustRevalidate = false;
	private int publicPrivate = 0; /* public = 1; private = -1 */
	
	public RPFilterCacheControl() {
		super();
	}

	
	public int getMaxAge() {
		return maxAge;
	}

	/*
	 * specifies the maximum amount of time that an representation will be
	 * considered fresh. Similar to Expires, this directive is relative to
	 * the time of the request, rather than absolute. [seconds] is the
	 * number of seconds from the time of the request you wish the
	 * representation to be fresh for. 
	 */
	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}



	public boolean isMustRevalidate() {
		return mustRevalidate;
	}


	/**
	 * tells caches that they must obey any freshness information you give
	 * them about a representation. HTTP allows caches to serve stale
	 * representations under special conditions; by specifying this header,
	 * you’re telling the cache that you want it to strictly follow your
	 * rules.
	 * 
	 * @param mustRevalidate mustRevalidate
	 */
	public void setMustRevalidate(boolean mustRevalidate) {
		this.mustRevalidate = mustRevalidate;
	}



	public boolean isNoCache() {
		return noCache;
	}


	/**
	 * forces caches to submit the request to the origin server for
	 * validation before releasing a cached copy, every time. This is useful
	 * to assure that authentication is respected (in combination with
	 * public), or to maintain rigid freshness, without sacrificing all of
	 * the benefits of caching.
	 * 
	 * @param noCache noCache
	 */
	public void setNoCache(boolean noCache) {
		this.noCache = noCache;
	}



	public boolean isNoStore() {
		return noStore;
	}


	/**
	 * instructs caches not to keep a copy of the representation under any
	 * conditions.
	 * 
	 * @param noStore noStore
	 */
	public void setNoStore(boolean noStore) {
		this.noStore = noStore;
	}



	public int getProxyMaxAge() {
		return proxyMaxAge;
	}


	/**
	 * similar to max-age, except that it only applies to shared (e.g.,
	 * proxy) caches.
	 * 
	 * @param proxyMaxAge proxyMaxAge
	 */
	public void setProxyMaxAge(int proxyMaxAge) {
		this.proxyMaxAge = proxyMaxAge;
	}



	public boolean isProxyRevalidate() {
		return proxyRevalidate;
	}


	/**
	 * similar to must-revalidate, except that it only applies to proxy
	 * caches.
	 * 
	 * @param proxyRevalidate proxyRevalidate
	 */
	public void setProxyRevalidate(boolean proxyRevalidate) {
		this.proxyRevalidate = proxyRevalidate;
	}



	public boolean isPublic() { 
		return publicPrivate == PUBLIC_V;
	}


	/**
	 * marks authenticated responses as cacheable; normally, if HTTP
	 * authentication is required, responses are automatically uncacheable.
	 * 
	 * @param publicC publicC
	 */
	public void setPublic(boolean publicC) {
		if (publicC)
			this.publicPrivate = PUBLIC_V;
		else
			this.publicPrivate = NO_V;
	}



	public boolean isPrivate() {
		return publicPrivate == PRIVATE_V;
	}


	/**
	 * marks authenticated responses as non-cache able accept by requesting client.
	 * 
	 * @param privateC privateC
	 */
	public void setPrivate(boolean privateC) {
		if (privateC)
			this.publicPrivate = PRIVATE_V;
		else
			this.publicPrivate = NO_V;
	}

	public String toString() {
		return getCacheControl().toString();
	}
	
	/**
	 * Returns a string version of the current cache control settings
	 * 
	 * @return StringBuffer
	 */
	public StringBuffer getCacheControl() {

		StringBuffer cc = new StringBuffer();
		int opsCnt = 0;
		if (noStore) {
			cc.append(NO_STORE);
		} else {

			if (maxAge != RPFilterResponse.UNSET_MAX_AGE) {
				opsCnt = append(cc, opsCnt, MAX_AGE, maxAge);
			}

			if (proxyMaxAge != RPFilterResponse.UNSET_MAX_AGE) {
				opsCnt = append(cc, opsCnt, PROXY_MAX_AGE, proxyMaxAge);
			}

			if (noCache) {
				opsCnt = append(cc, opsCnt, NO_CACHE);
			}

			if (proxyRevalidate) {
				opsCnt = append(cc, opsCnt, PROXY_REVALIDATE);
			}

			if (mustRevalidate) {
				opsCnt = append(cc, opsCnt, MUST_REVALIDATE);
			}

			if (publicPrivate == PUBLIC_V) {
				opsCnt = append(cc, opsCnt, PUBLIC);
			}
			
			else if (publicPrivate == PRIVATE_V) {
				opsCnt = append(cc, opsCnt, PRIVATE);
			}

		}

		return cc;
	}

	private final int append(StringBuffer cc, int opsCnt, String str) {
		if (opsCnt > 0)
			cc.append(",");
		cc.append(str);
		return ++opsCnt;
	}

	private final int append(StringBuffer cc, int opsCnt, String str,
			int val) {
		if (opsCnt > 0)
			cc.append(",");
		cc.append(str);
		cc.append("=");
		cc.append(val);
		return ++opsCnt;
	}
}
